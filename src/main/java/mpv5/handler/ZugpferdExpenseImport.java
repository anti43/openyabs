/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Account;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Revenue;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.ModernExpensePanel;
import mpv5.utils.text.TypeConversion;
import org.mustangproject.CalculatedInvoice;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Automatic and on-demand import of incoming ZUGFeRD / Factur-X ({@code .pdf})
 * and XRechnung / ZUGFeRD ({@code .xml}) e-invoices into {@link Expense} records,
 * archiving the original invoice file via {@link ExpenseReceipts}.
 */
public final class ZugpferdExpenseImport {

    /**
     * Local setting key for the watched incoming e-invoice folder.
     */
    public static final String KEY_FOLDER = "zugferdimportdir";

    /**
     * Local setting key enabling automatic import from {@link #KEY_FOLDER}.
     */
    public static final String KEY_AUTO = "zugferdautoimport";

    private static final long POLL_INTERVAL_MS = 15_000L;
    private static final long INITIAL_DELAY_MS = 3_000L;

    private static final Set<String> NON_EINVOICE_HASHES = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Map<String, String> SEEN_FILE_STAMPS = new ConcurrentHashMap<String, String>();
    private static Timer autoTimer;

    private ZugpferdExpenseImport() {
    }

    /**
     * Extracted data from a ZUGFeRD / Factur-X PDF or XRechnung XML invoice.
     */
    public static final class ParsedInvoice {
        public final File sourceFile;
        public final String invoiceNumber;
        public final String sellerName;
        public final String sellerTaxId;
        public final String sellerIban;
        public final Date issueDate;
        public final Date dueDate;
        public final BigDecimal netAmount;
        public final BigDecimal grossAmount;
        public final BigDecimal taxAmount;
        public final BigDecimal taxRate;
        public final String currency;
        public final List<String> lineDescriptions;
        public final List<String> notes;
        /**
         * VAT IDs and tax numbers of the seller and the buyer, the name of the
         * buyer: tell an incoming invoice (expense) from an own one (income)
         */
        public List<String> sellerIds = Collections.emptyList();
        public String buyerName = "";
        public List<String> buyerIds = Collections.emptyList();
        /**
         * the VAT breakdown of the invoice (BG-23): {rate, basis, tax} per rate,
         * empty if the invoice has none
         */
        public List<BigDecimal[]> taxBreakdown = Collections.emptyList();

        public ParsedInvoice(File sourceFile, String invoiceNumber, String sellerName,
                String sellerTaxId, String sellerIban, Date issueDate, Date dueDate,
                BigDecimal netAmount, BigDecimal grossAmount, BigDecimal taxAmount,
                BigDecimal taxRate, String currency, List<String> lineDescriptions,
                List<String> notes) {
            this.sourceFile = sourceFile;
            this.invoiceNumber = invoiceNumber == null ? "" : invoiceNumber.trim();
            this.sellerName = sellerName == null ? "" : sellerName.trim();
            this.sellerTaxId = sellerTaxId == null ? "" : sellerTaxId.trim();
            this.sellerIban = sellerIban == null ? "" : sellerIban.trim();
            this.issueDate = issueDate != null ? issueDate : new Date();
            this.dueDate = dueDate != null ? dueDate : this.issueDate;
            this.netAmount = netAmount != null ? netAmount : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.grossAmount = grossAmount != null ? grossAmount : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.taxAmount = taxAmount != null ? taxAmount : this.grossAmount.subtract(this.netAmount).setScale(2, RoundingMode.HALF_UP);
            this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
            this.currency = currency == null || currency.trim().isEmpty() ? "EUR" : currency.trim();
            this.lineDescriptions = lineDescriptions == null
                    ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<String>(lineDescriptions));
            this.notes = notes == null
                    ? Collections.<String>emptyList()
                    : Collections.unmodifiableList(new ArrayList<String>(notes));
        }

        /**
         * Builds the human-readable expense description combining supplier,
         * invoice number, IBAN, line items and notes.
         */
        public String buildDescription() {
            StringBuilder sb = new StringBuilder();
            if (!sellerName.isEmpty()) {
                sb.append(sellerName);
                if (!invoiceNumber.isEmpty()) {
                    sb.append(" (").append(invoiceNumber).append(")");
                }
            } else if (!invoiceNumber.isEmpty()) {
                sb.append(invoiceNumber);
            }
            if (!sellerIban.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" - IBAN: ");
                } else {
                    sb.append("IBAN: ");
                }
                sb.append(sellerIban);
            }
            if (!lineDescriptions.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(String.join("\n", lineDescriptions));
            }
            if (!notes.isEmpty()) {
                for (String n : notes) {
                    String trimmed = n == null ? "" : n.trim();
                    if (!trimmed.isEmpty() && sb.indexOf(trimmed) < 0) {
                        if (sb.length() > 0) {
                            sb.append("\n");
                        }
                        sb.append(trimmed);
                    }
                }
            }
            if (sb.length() == 0 && sourceFile != null) {
                sb.append(sourceFile.getName());
            }
            return cut(sb.toString().trim(), 100000);
        }

        /**
         * Creates an {@link Expense} populated with the parsed invoice data.
         */
        public Expense toExpense(int accountId, int groupId) {
            Expense e = new Expense();
            String num = cut(invoiceNumber, 250);
            if (!num.isEmpty()) {
                e.setCnumber(num);
                e.setCname(num);
            }
            e.setDescription(buildDescription());
            e.setNetvalue(netAmount);
            e.setBrutvalue(grossAmount);
            e.setTaxpercentvalue(taxRate);
            e.setAccountsids(accountId > 0 ? accountId : 1);
            e.setGroupsids(groupId > 0 ? groupId : 1);
            e.setDateadded(issueDate);
            e.setDateend(dueDate);
            e.setIspaid(false);
            return e;
        }

        /**
         * Creates a {@link Revenue} populated with the parsed invoice data.
         */
        public Revenue toRevenue(int accountId, int groupId) {
            Revenue r = new Revenue();
            String num = cut(invoiceNumber, 250);
            if (!num.isEmpty()) {
                r.setCnumber(num);
                r.setCname(num);
            }
            r.setDescription(buildDescription());
            r.setNetvalue(netAmount);
            r.setBrutvalue(grossAmount);
            r.setTaxpercentvalue(taxRate);
            r.setAccountsids(accountId > 0 ? accountId : 1);
            r.setGroupsids(groupId > 0 ? groupId : 1);
            r.setContactsids(-1);
            r.setRefOrderids(-1);
            r.setDateadded(issueDate != null ? issueDate : new Date());
            r.setDateend(dueDate != null ? dueDate : r.__getDateadded());
            r.setStatus(mpv5.db.objects.Item.STATUS_QUEUED);
            return r;
        }
    }

    /**
     * Summary of a batch or folder import run.
     */
    public static final class Result {
        public int imported;
        public int alreadyImported;
        public int skippedNotEInvoice;
        public final List<Expense> expenses = new ArrayList<Expense>();
        public final List<Revenue> revenues = new ArrayList<Revenue>();
        public final List<ParsedInvoice> parsed = new ArrayList<ParsedInvoice>();
        public final List<String> errors = new ArrayList<String>();
        /**
         * one line per file of the import with detection
         */
        public final List<String> lines = new ArrayList<String>();

        public String summary() {
            String s = String.format(Messages.ZUGFERD_IMPORT_RESULT.toString(),
                    imported, alreadyImported, skippedNotEInvoice);
            return lines.isEmpty() ? s : s + "\n\n" + String.join("\n", lines);
        }
    }

    /**
     * Pluggable sink used to persist a parsed invoice and check for duplicates.
     */
    public interface ImportSink {
        boolean isAlreadyImported(String sha256) throws Exception;

        Expense save(ParsedInvoice invoice, File sourceFile, String sha256) throws Exception;
    }

    /**
     * @return the configured incoming e-invoice folder path, or {@code ""} if unset
     */
    public static String folderPath() {
        if (!LocalSettings.hasProperty(KEY_FOLDER)) {
            return "";
        }
        String p = LocalSettings.getProperty(KEY_FOLDER);
        return p == null || "null".equals(p.trim()) ? "" : p.trim();
    }

    /**
     * Sets the incoming e-invoice folder path in {@link LocalSettings}.
     */
    public static void setFolderPath(String path) {
        String clean = path == null ? "" : path.trim();
        LocalSettings.setProperty(KEY_FOLDER, clean);
        SEEN_FILE_STAMPS.clear();
    }

    /**
     * @return the resolved folder for incoming e-invoices, or {@code null} if not configured
     */
    public static File folder() {
        String p = folderPath();
        if (p.isEmpty()) {
            return null;
        }
        if (p.contains("[")) {
            return ExpenseReceipts.resolve(p, LocalSettings.getProperty(LocalSettings.DBNAME), new Date());
        }
        return new File(p);
    }

    /**
     * @return true if automatic import from the configured folder is enabled (default true)
     */
    public static boolean isAutoImportEnabled() {
        if (!LocalSettings.hasProperty(KEY_AUTO)) {
            return true;
        }
        return TypeConversion.stringToBoolean(LocalSettings.getProperty(KEY_AUTO));
    }

    /**
     * Enables or disables automatic import from the configured folder.
     */
    public static void setAutoImportEnabled(boolean enabled) {
        LocalSettings.setProperty(KEY_AUTO, TypeConversion.booleanToString(enabled));
    }

    /**
     * Starts or restarts the background watcher that automatically imports
     * e-invoices from {@link #folder()} when {@link #isAutoImportEnabled()} is true.
     */
    public static synchronized void startAutoImport() {
        stopAutoImport();
        if (!isAutoImportEnabled()) {
            return;
        }
        File dir = folder();
        if (dir == null) {
            return;
        }
        autoTimer = new Timer("zugferd-auto-import", true);
        autoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    runAutoImportCycle();
                } catch (Throwable t) {
                    Log.Debug(ZugpferdExpenseImport.class, t);
                }
            }
        }, INITIAL_DELAY_MS, POLL_INTERVAL_MS);
    }

    /**
     * Stops the background auto-import timer.
     */
    public static synchronized void stopAutoImport() {
        if (autoTimer != null) {
            autoTimer.cancel();
            autoTimer = null;
        }
    }

    private static void runAutoImportCycle() {
        if (!isAutoImportEnabled()) {
            return;
        }
        File dir = folder();
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] candidates = listCandidateFiles(dir);
        List<File> changed = new ArrayList<File>();
        for (File f : candidates) {
            String key = f.getAbsolutePath();
            String stamp = f.lastModified() + ":" + f.length();
            if (!stamp.equals(SEEN_FILE_STAMPS.get(key))) {
                changed.add(f);
            }
        }
        if (changed.isEmpty()) {
            return;
        }
        Result r = importFiles(changed);
        for (File f : candidates) {
            SEEN_FILE_STAMPS.put(f.getAbsolutePath(), f.lastModified() + ":" + f.length());
        }
        if (r.imported > 0) {
            final String msg = String.format(Messages.EINVOICE_WATCH_IMPORTED.toString(), r.imported, r.expenses.size(), r.revenues.size());
            SwingUtilities.invokeLater(() -> {
                try {
                    YabsViewProxy.instance().addMessage(msg);
                } catch (Exception ignored) {
                }
                ModernExpensePanel.refreshIfOpen();
                mpv5.ui.panels.ModernRevenuePanel.refreshIfOpen();
            });
        }
    }

    /**
     * Imports all new ZUGFeRD / XRechnung files from the configured {@link #folder()}.
     */
    public static Result importConfiguredFolder() {
        File dir = folder();
        if (dir == null || !dir.isDirectory()) {
            return new Result();
        }
        return importFromFolder(dir);
    }

    /**
     * Imports all new ZUGFeRD / XRechnung files from {@code dir} into the database.
     */
    public static Result importFromFolder(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return new Result();
        }
        return importFiles(Arrays.asList(listCandidateFiles(dir)));
    }

    /**
     * Imports all new ZUGFeRD / XRechnung files from {@code dir} using the given {@link ImportSink}.
     */
    public static Result importFromFolder(File dir, ImportSink sink) {
        if (dir == null || !dir.isDirectory()) {
            return new Result();
        }
        return importFiles(Arrays.asList(listCandidateFiles(dir)), sink);
    }

    /**
     * Imports the given files (or directories) into the database.
     */
    public static Result importFiles(Collection<File> files) {
        Result result = new Result();
        if (files == null) {
            return result;
        }
        List<File> flat = new ArrayList<File>();
        for (File f : files) {
            if (f != null && f.isDirectory()) {
                flat.addAll(Arrays.asList(listCandidateFiles(f)));
            } else if (isCandidateFile(f)) {
                flat.add(f);
            }
        }
        //expense or income from the parties of the invoice (EInvoiceImport)
        for (EInvoiceImport.Result r : EInvoiceImport.importFiles(flat, null)) {
            switch (r.outcome) {
                case IMPORTED:
                    result.imported++;
                    result.parsed.add(r.invoice);
                    if (r.record instanceof Expense) {
                        result.expenses.add((Expense) r.record);
                    } else if (r.record instanceof Revenue) {
                        result.revenues.add((Revenue) r.record);
                    }
                    break;
                case NOT_EINVOICE:
                    result.skippedNotEInvoice++;
                    continue;
                case FAILED:
                case INVALID:
                    result.errors.add(r.message());
                    break;
                default:
                    result.alreadyImported++;
            }
            result.lines.add(r.message());
        }
        return result;
    }

    /**
     * Imports the given files (or directories) using the specified {@link ImportSink}.
     */
    public static Result importFiles(Collection<File> files, ImportSink sink) {
        Result result = new Result();
        if (files == null || files.isEmpty() || sink == null) {
            return result;
        }
        List<File> flat = new ArrayList<File>();
        for (File f : files) {
            if (f == null) {
                continue;
            }
            if (f.isDirectory()) {
                flat.addAll(Arrays.asList(listCandidateFiles(f)));
            } else if (isCandidateFile(f)) {
                flat.add(f);
            }
        }
        for (File f : flat) {
            try {
                String sha = ExpenseReceipts.sha256(f);
                if (sink.isAlreadyImported(sha)) {
                    result.alreadyImported++;
                    continue;
                }
                if (NON_EINVOICE_HASHES.contains(sha)) {
                    result.skippedNotEInvoice++;
                    continue;
                }
                ParsedInvoice inv = parse(f);
                if (inv == null) {
                    NON_EINVOICE_HASHES.add(sha);
                    result.skippedNotEInvoice++;
                    continue;
                }
                Expense saved = sink.save(inv, f, sha);
                result.parsed.add(inv);
                if (saved != null) {
                    result.expenses.add(saved);
                }
                result.imported++;
            } catch (Exception e) {
                Log.Debug(ZugpferdExpenseImport.class, e);
                result.errors.add(f.getName() + ": " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Lists candidate {@code .pdf} and {@code .xml} files in a directory, sorted by name.
     */
    public static File[] listCandidateFiles(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return new File[0];
        }
        File[] list = dir.listFiles(ZugpferdExpenseImport::isCandidateFile);
        if (list == null || list.length == 0) {
            return new File[0];
        }
        Arrays.sort(list, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    /**
     * @return true if the file is a readable, non-hidden {@code .pdf} or {@code .xml} file
     */
    public static boolean isCandidateFile(File f) {
        if (f == null || !f.isFile() || f.isHidden() || f.length() <= 0) {
            return false;
        }
        String name = f.getName();
        if (name.startsWith(".") || name.startsWith("~")) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".pdf") || lower.endsWith(".xml");
    }

    private static final class Importer extends ZUGFeRDImporter {
        Document document() {
            return getDocument();
        }
    }

    /**
     * Parses a ZUGFeRD / Factur-X PDF or XRechnung / ZUGFeRD XML file.
     *
     * @param file {@code .pdf} or {@code .xml} file
     * @return the extracted {@link ParsedInvoice}, or {@code null} if the file
     *         is not a ZUGFeRD / XRechnung e-invoice (e.g. a plain PDF without embedded XML)
     */
    public static ParsedInvoice parse(File file) {
        if (!isCandidateFile(file)) {
            return null;
        }
        try {
            Importer importer = new Importer();
            importer.doIgnoreCalculationErrors();
            importer.setPDFFilename(file.getAbsolutePath());

            String utf8 = importer.getUTF8();
            Document doc = importer.document();
            if (doc == null || utf8 == null || utf8.trim().isEmpty()) {
                return null;
            }
            if (!isEInvoiceDocument(doc, utf8)) {
                return null;
            }

            CalculatedInvoice calc = null;
            try {
                CalculatedInvoice ci = new CalculatedInvoice();
                importer.extractInto(ci);
                ci.calculate();
                calc = ci;
            } catch (Throwable ignored) {
                // Minimum/BasicWL profiles or custom XMLs may not support full line-item calculation
            }

            XPath xp = XPathFactory.newInstance().newXPath();

            // 1. Invoice number (BT-1)
            String invoiceNumber = firstNonEmpty(
                    eval(xp, doc, "//*[local-name()='ExchangedDocument']/*[local-name()='ID']"),
                    eval(xp, doc, "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='ID']"),
                    safeCall(importer::getInvoiceID),
                    calc != null ? calc.getNumber() : null
            );

            // 2. Seller name (BT-27)
            String sellerName = firstNonEmpty(
                    calc != null && calc.getSender() != null ? calc.getSender().getName() : null,
                    eval(xp, doc, "//*[local-name()='SellerTradeParty']/*[local-name()='Name']"),
                    eval(xp, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']"),
                    eval(xp, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyName']/*[local-name()='Name']"),
                    safeCall(importer::getHolder)
            );

            // 3. Seller VAT ID / tax ID & IBAN
            String sellerTaxId = firstNonEmpty(
                    calc != null && calc.getSender() != null ? calc.getSender().getVATID() : null,
                    eval(xp, doc, "//*[local-name()='SellerTradeParty']/*[local-name()='SpecifiedTaxRegistration']/*[local-name()='ID']"),
                    eval(xp, doc, "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyTaxScheme']/*[local-name()='CompanyID']")
            );
            String sellerIban = firstNonEmpty(
                    eval(xp, doc, "//*[local-name()='PayeePartyCreditorFinancialAccount']/*[local-name()='IBANID']"),
                    eval(xp, doc, "//*[local-name()='PayeeFinancialAccount']/*[local-name()='ID']"),
                    safeCall(importer::getIBAN)
            );

            // 4. Issue Date (BT-2) and Due Date (BT-9)
            Date issueDate = firstDate(
                    calc != null ? calc.getIssueDate() : null,
                    parseDate(eval(xp, doc, "//*[local-name()='ExchangedDocument']/*[local-name()='IssueDateTime']/*[local-name()='DateTimeString']")),
                    parseDate(eval(xp, doc, "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='IssueDate']")),
                    parseDate(safeCall(importer::getIssueDate))
            );
            Date dueDate = firstDate(
                    calc != null ? calc.getDueDate() : null,
                    parseDate(eval(xp, doc, "//*[local-name()='SpecifiedTradePaymentTerms']/*[local-name()='DueDateDateTime']/*[local-name()='DateTimeString']")),
                    parseDate(eval(xp, doc, "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='DueDate']")),
                    parseDate(safeCall(importer::getDueDate)),
                    issueDate
            );

            // 5. Amounts (Net, Gross, Tax) and Tax Rate
            BigDecimal netAmount = firstDecimal(
                    eval(xp, doc, "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='TaxBasisTotalAmount']"),
                    eval(xp, doc, "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxExclusiveAmount']"),
                    eval(xp, doc, "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='LineTotalAmount']"),
                    safeCall(importer::getTaxBasisTotalAmount),
                    calc != null && calc.getTaxBasis() != null ? calc.getTaxBasis().toPlainString() : null
            );

            BigDecimal grossAmount = firstDecimal(
                    eval(xp, doc, "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='GrandTotalAmount']"),
                    eval(xp, doc, "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxInclusiveAmount']"),
                    eval(xp, doc, "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='DuePayableAmount']"),
                    eval(xp, doc, "//*[local-name()='LegalMonetaryTotal']/*[local-name()='PayableAmount']"),
                    safeCall(importer::getAmount),
                    calc != null && calc.getGrandTotal() != null ? calc.getGrandTotal().toPlainString() : null
            );

            BigDecimal taxAmount = firstDecimal(
                    eval(xp, doc, "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='TaxTotalAmount']"),
                    eval(xp, doc, "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='TaxTotal']/*[local-name()='TaxAmount']"),
                    safeCall(importer::getTaxTotalAmount),
                    calc != null && calc.getVATtotal() != null ? calc.getVATtotal().toPlainString() : null
            );

            BigDecimal taxRate = extractDominantTaxRate(xp, doc, netAmount, grossAmount, taxAmount);

            if (grossAmount == null && netAmount != null) {
                if (taxAmount != null) {
                    grossAmount = netAmount.add(taxAmount);
                } else {
                    grossAmount = netAmount.multiply(BigDecimal.ONE.add(taxRate.movePointLeft(2)));
                }
            }
            if (netAmount == null && grossAmount != null) {
                if (taxAmount != null) {
                    netAmount = grossAmount.subtract(taxAmount);
                } else if (taxRate.signum() > 0) {
                    netAmount = grossAmount.divide(BigDecimal.ONE.add(taxRate.movePointLeft(2)), 2, RoundingMode.HALF_UP);
                } else {
                    netAmount = grossAmount;
                }
            }
            if (netAmount == null) {
                netAmount = BigDecimal.ZERO;
            }
            if (grossAmount == null) {
                grossAmount = BigDecimal.ZERO;
            }
            netAmount = netAmount.setScale(2, RoundingMode.HALF_UP);
            grossAmount = grossAmount.setScale(2, RoundingMode.HALF_UP);
            if (taxAmount == null) {
                taxAmount = grossAmount.subtract(netAmount).setScale(2, RoundingMode.HALF_UP);
            } else {
                taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);
            }

            // 6. Currency
            String currency = firstNonEmpty(
                    eval(xp, doc, "//*[local-name()='InvoiceCurrencyCode']"),
                    eval(xp, doc, "//*[local-name()='DocumentCurrencyCode']"),
                    safeCall(importer::getInvoiceCurrencyCode),
                    "EUR"
            );

            // 7. Line items & Notes
            List<String> lines = extractLineDescriptions(xp, doc, calc);
            List<String> notes = extractNotes(xp, doc, importer);

            ParsedInvoice parsed = new ParsedInvoice(file, invoiceNumber, sellerName, sellerTaxId, sellerIban,
                    issueDate, dueDate, netAmount, grossAmount, taxAmount, taxRate, currency, lines, notes);
            parsed.sellerIds = all(xp, doc,
                    "//*[local-name()='SellerTradeParty']/*[local-name()='SpecifiedTaxRegistration']/*[local-name()='ID']",
                    "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyTaxScheme']/*[local-name()='CompanyID']",
                    "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyLegalEntity']/*[local-name()='CompanyID']");
            if (!parsed.sellerTaxId.isEmpty() && !parsed.sellerIds.contains(parsed.sellerTaxId)) {
                List<String> ids = new ArrayList<String>(parsed.sellerIds);
                ids.add(parsed.sellerTaxId);
                parsed.sellerIds = ids;
            }
            parsed.buyerName = firstNonEmpty(
                    eval(xp, doc, "//*[local-name()='BuyerTradeParty']/*[local-name()='Name']"),
                    eval(xp, doc, "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']"),
                    eval(xp, doc, "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyName']/*[local-name()='Name']"),
                    "").trim();
            parsed.buyerIds = all(xp, doc,
                    "//*[local-name()='BuyerTradeParty']/*[local-name()='SpecifiedTaxRegistration']/*[local-name()='ID']",
                    "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyTaxScheme']/*[local-name()='CompanyID']",
                    "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyLegalEntity']/*[local-name()='CompanyID']");
            parsed.taxBreakdown = taxBreakdown(xp, doc);
            return parsed;
        } catch (Exception e) {
            Log.Debug(ZugpferdExpenseImport.class, "Could not parse ZUGFeRD file " + file + ": " + e.getMessage());
            return null;
        }
    }

    private static boolean isEInvoiceDocument(Document doc, String utf8) {
        Element root = doc.getDocumentElement();
        if (root == null) {
            return false;
        }
        String local = root.getLocalName() != null ? root.getLocalName() : root.getNodeName();
        if (local.contains(":")) {
            local = local.substring(local.indexOf(':') + 1);
        }
        if ("CrossIndustryInvoice".equalsIgnoreCase(local)
                || "CrossIndustryDocument".equalsIgnoreCase(local)
                || "Invoice".equalsIgnoreCase(local)
                || "CreditNote".equalsIgnoreCase(local)) {
            return utf8.contains("ExchangedDocument")
                    || utf8.contains("SpecifiedTradeSettlement")
                    || utf8.contains("LegalMonetaryTotal")
                    || utf8.contains("AccountingSupplierParty");
        }
        return false;
    }

    /**
     * @return {rate, basis, tax} per VAT rate of the document level (CII
     * ApplicableTradeTax, UBL TaxSubtotal), rates with the same percent added
     */
    private static List<BigDecimal[]> taxBreakdown(XPath xp, Document doc) {
        java.util.Map<BigDecimal, BigDecimal[]> m = new java.util.TreeMap<BigDecimal, BigDecimal[]>(Collections.reverseOrder());
        try {
            String[][] syntaxes = {
                {"//*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='ApplicableTradeTax']",
                    "*[local-name()='RateApplicablePercent'] | *[local-name()='ApplicablePercent']", "*[local-name()='BasisAmount']",
                    "*[local-name()='CalculatedAmount']"},
                {"/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='TaxTotal']/*[local-name()='TaxSubtotal']",
                    "*[local-name()='TaxCategory']/*[local-name()='Percent']", "*[local-name()='TaxableAmount']", "*[local-name()='TaxAmount']"}};
            for (String[] x : syntaxes) {
                NodeList nodes = (NodeList) xp.evaluate(x[0], doc, XPathConstants.NODESET);
                for (int i = 0; nodes != null && i < nodes.getLength(); i++) {
                    Node n = nodes.item(i);
                    BigDecimal rate = parseDecimal(eval(xp, n, x[1]));
                    BigDecimal basis = parseDecimal(eval(xp, n, x[2]));
                    BigDecimal tax = parseDecimal(eval(xp, n, x[3]));
                    if (rate == null || basis == null) {
                        continue;
                    }
                    rate = rate.stripTrailingZeros().scale() < 0 ? rate.setScale(0, RoundingMode.HALF_UP) : rate.stripTrailingZeros();
                    if (tax == null) {
                        tax = basis.multiply(rate).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
                    }
                    BigDecimal[] v = m.computeIfAbsent(rate, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    v[0] = v[0].add(basis);
                    v[1] = v[1].add(tax);
                }
                if (!m.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.Debug(ZugpferdExpenseImport.class, e.getMessage());
        }
        List<BigDecimal[]> l = new ArrayList<BigDecimal[]>();
        for (java.util.Map.Entry<BigDecimal, BigDecimal[]> e : m.entrySet()) {
            l.add(new BigDecimal[]{e.getKey(), e.getValue()[0].setScale(2, RoundingMode.HALF_UP), e.getValue()[1].setScale(2, RoundingMode.HALF_UP)});
        }
        return l;
    }

    private static BigDecimal extractDominantTaxRate(XPath xp, Document doc,
            BigDecimal netAmount, BigDecimal grossAmount, BigDecimal taxAmount) {
        try {
            // Check header ApplicableTradeTax (CII) or TaxSubtotal (UBL) and pick the rate with largest basis
            NodeList ciiTaxes = (NodeList) xp.evaluate(
                    "//*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='ApplicableTradeTax']"
                    + " | //*[local-name()='ApplicableSupplyChainTradeSettlement']/*[local-name()='ApplicableTradeTax']",
                    doc, XPathConstants.NODESET);
            BigDecimal bestRate = null;
            BigDecimal bestBasis = null;
            if (ciiTaxes != null && ciiTaxes.getLength() > 0) {
                for (int i = 0; i < ciiTaxes.getLength(); i++) {
                    Node n = ciiTaxes.item(i);
                    BigDecimal rate = parseDecimal(eval(xp, n, "*[local-name()='RateApplicablePercent'] | *[local-name()='ApplicablePercent']"));
                    BigDecimal basis = parseDecimal(eval(xp, n, "*[local-name()='BasisAmount']"));
                    if (rate != null) {
                        if (bestRate == null || (basis != null && (bestBasis == null || basis.compareTo(bestBasis) > 0))) {
                            bestRate = rate;
                            bestBasis = basis;
                        }
                    }
                }
            }
            if (bestRate != null) {
                return bestRate.stripTrailingZeros().scale() < 0
                        ? bestRate.setScale(0, RoundingMode.HALF_UP)
                        : bestRate.stripTrailingZeros();
            }

            NodeList ublTaxes = (NodeList) xp.evaluate(
                    "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='TaxTotal']/*[local-name()='TaxSubtotal']",
                    doc, XPathConstants.NODESET);
            if (ublTaxes != null && ublTaxes.getLength() > 0) {
                for (int i = 0; i < ublTaxes.getLength(); i++) {
                    Node n = ublTaxes.item(i);
                    BigDecimal rate = parseDecimal(eval(xp, n, "*[local-name()='TaxCategory']/*[local-name()='Percent'] | *[local-name()='Percent']"));
                    BigDecimal basis = parseDecimal(eval(xp, n, "*[local-name()='TaxableAmount']"));
                    if (rate != null) {
                        if (bestRate == null || (basis != null && (bestBasis == null || basis.compareTo(bestBasis) > 0))) {
                            bestRate = rate;
                            bestBasis = basis;
                        }
                    }
                }
            }
            if (bestRate != null) {
                return bestRate.stripTrailingZeros().scale() < 0
                        ? bestRate.setScale(0, RoundingMode.HALF_UP)
                        : bestRate.stripTrailingZeros();
            }

            // Fallback to any RateApplicablePercent or TaxCategory/Percent in the document
            BigDecimal anyRate = firstDecimal(
                    eval(xp, doc, "//*[local-name()='ApplicableTradeTax']/*[local-name()='RateApplicablePercent']"),
                    eval(xp, doc, "//*[local-name()='ApplicableTradeTax']/*[local-name()='ApplicablePercent']"),
                    eval(xp, doc, "//*[local-name()='ClassifiedTaxCategory']/*[local-name()='Percent']"),
                    eval(xp, doc, "//*[local-name()='TaxCategory']/*[local-name()='Percent']")
            );
            if (anyRate != null) {
                return anyRate.stripTrailingZeros().scale() < 0
                        ? anyRate.setScale(0, RoundingMode.HALF_UP)
                        : anyRate.stripTrailingZeros();
            }
        } catch (Exception ignored) {
        }

        // Compute from net and tax/gross if not explicitly stated
        if (netAmount != null && netAmount.signum() > 0) {
            BigDecimal tax = taxAmount;
            if (tax == null && grossAmount != null) {
                tax = grossAmount.subtract(netAmount);
            }
            if (tax != null && tax.signum() >= 0) {
                BigDecimal computed = tax.multiply(new BigDecimal("100")).divide(netAmount, 2, RoundingMode.HALF_UP).stripTrailingZeros();
                return computed.scale() < 0 ? computed.setScale(0, RoundingMode.HALF_UP) : computed;
            }
        }
        return BigDecimal.ZERO;
    }

    private static List<String> extractLineDescriptions(XPath xp, Document doc, CalculatedInvoice calc) {
        Set<String> items = new LinkedHashSet<String>();
        if (calc != null && calc.getZFItems() != null) {
            for (IZUGFeRDExportableItem item : calc.getZFItems()) {
                if (item != null && item.getProduct() != null && item.getProduct().getName() != null) {
                    String name = item.getProduct().getName().trim();
                    if (!name.isEmpty()) {
                        items.add(name);
                    }
                }
            }
        }
        if (items.isEmpty()) {
            try {
                NodeList nodes = (NodeList) xp.evaluate(
                        "//*[local-name()='IncludedSupplyChainTradeLineItem']//*[local-name()='SpecifiedTradeProduct']/*[local-name()='Name']"
                        + " | //*[local-name()='InvoiceLine' or local-name()='CreditNoteLine']//*[local-name()='Item']/*[local-name()='Name']",
                        doc, XPathConstants.NODESET);
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        String text = nodes.item(i).getTextContent();
                        if (text != null && !text.trim().isEmpty()) {
                            items.add(text.trim());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return new ArrayList<String>(items);
    }

    private static List<String> extractNotes(XPath xp, Document doc, ZUGFeRDImporter importer) {
        Set<String> notes = new LinkedHashSet<String>();
        try {
            NodeList nList = (NodeList) xp.evaluate(
                    "//*[local-name()='ExchangedDocument']/*[local-name()='IncludedNote']/*[local-name()='Content']"
                    + " | /*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='Note']",
                    doc, XPathConstants.NODESET);
            if (nList != null) {
                for (int i = 0; i < nList.getLength(); i++) {
                    String t = nList.item(i).getTextContent();
                    if (t != null && !t.trim().isEmpty()) {
                        notes.add(t.trim());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String terms = safeCall(importer::getPaymentTerms);
        if (terms != null && !terms.trim().isEmpty()) {
            notes.add(terms.trim());
        }
        return new ArrayList<String>(notes);
    }

    @FunctionalInterface
    private interface StringSupplier {
        String get() throws Exception;
    }

    private static String safeCall(StringSupplier s) {
        try {
            return s.get();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * @return the non empty texts of all nodes the expressions find
     */
    private static List<String> all(XPath xp, Object context, String... expressions) {
        List<String> l = new ArrayList<String>();
        for (String e : expressions) {
            try {
                NodeList nodes = (NodeList) xp.evaluate(e, context, javax.xml.xpath.XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    String t = nodes.item(i).getTextContent();
                    if (t != null && !t.trim().isEmpty() && !l.contains(t.trim())) {
                        l.add(t.trim());
                    }
                }
            } catch (Exception ex) {
                //not this syntax
            }
        }
        return l;
    }

    private static String eval(XPath xp, Object context, String expression) {
        try {
            String val = xp.evaluate(expression, context);
            return val == null ? null : val.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values != null) {
            for (String v : values) {
                if (v != null && !v.trim().isEmpty()) {
                    return v.trim();
                }
            }
        }
        return "";
    }

    private static Date firstDate(Date... dates) {
        if (dates != null) {
            for (Date d : dates) {
                if (d != null) {
                    return d;
                }
            }
        }
        return new Date();
    }

    static Date parseDate(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        for (String pattern : new String[]{"yyyyMMdd", "yyyy-MM-dd", "dd.MM.yyyy"}) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern, Locale.ROOT);
                df.setLenient(false);
                if (s.length() == pattern.length()) {
                    return df.parse(s);
                }
            } catch (Exception ignored) {
            }
        }
        if (s.length() >= 10) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                df.setLenient(false);
                return df.parse(s.substring(0, 10));
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static BigDecimal firstDecimal(String... values) {
        if (values != null) {
            for (String v : values) {
                BigDecimal d = parseDecimal(v);
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }

    static BigDecimal parseDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.contains(",") && !s.contains(".")) {
            s = s.replace(',', '.');
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stripExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String cut(String s, int max) {
        return s == null ? "" : s.length() <= max ? s : s.substring(0, max);
    }
}
