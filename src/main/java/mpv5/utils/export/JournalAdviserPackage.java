/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.globals.Constants;
import mpv5.handler.ExpenseReceipts;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import mpv5.handler.RevenueReceipts;
import mpv5.logging.Log;
import org.mustangproject.Invoice;

/**
 * The package for the tax adviser (Steuerberater) of a journal: one ZIP with
 * <ul>
 * <li>{@code journal.csv}: one line per document (semicolon, decimal comma,
 * UTF-8 with BOM - opens in Excel),</li>
 * <li>{@code journal_steuersaetze.csv}: one line per document and tax rate
 * (documents with several rates split by their items) - for the import into
 * bookkeeping software,</li>
 * <li>{@code zusammenfassung.txt}: period, filters, EÜR sums, amounts by tax
 * rate, open items and the e-invoice check,</li>
 * <li>{@code belege/}: the archived receipts of expenses and income (incoming
 * e-invoices in their original format),</li>
 * <li>{@code e-rechnungen/}: the XRechnung XML of the outgoing invoices to
 * contacts who get e-invoices.</li>
 * </ul>
 * The texts are German: the package is for the German EÜR / tax adviser.
 */
public final class JournalAdviserPackage {

    /**
     * Gross amount up to which an invoice is a small-amount invoice
     * (Kleinbetragsrechnung, § 33 UStDV) and needs no e-invoice
     */
    public static final BigDecimal SMALL_AMOUNT = new BigDecimal("250.00");

    /**
     * The result of the e-invoice check of the outgoing invoices
     */
    public static final class EInvoiceCheck {

        /**
         * invoices (invoices, deposits, part payments, credit notes) in the
         * journal
         */
        public int invoices;
        /**
         * of which to companies, above the small amount
         */
        public int b2b;
        /**
         * of which the contact gets an e-invoice
         */
        public int b2bEInvoice;
        /**
         * the numbers of the b2b invoices without e-invoice
         */
        public final List<String> b2bWithoutEInvoice = new ArrayList<String>();
    }

    private final List<Entry> entries;
    private final Totals totals;
    private final Date from;
    private final Date to;
    private final List<String> filters;
    private final Map<Integer, Contact> contacts = new HashMap<Integer, Contact>();
    private final Map<BigDecimal, String> rateLabels;
    private int receipts;
    private int einvoices;
    private final List<String> problems = new ArrayList<String>();

    /**
     * @param entries the journal rows (as loaded)
     * @param totals their sums
     * @param from start of the period
     * @param to end of the period
     * @param filters the filters as text lines (for the summary)
     */
    public JournalAdviserPackage(List<Entry> entries, Totals totals, Date from, Date to, List<String> filters) {
        this(entries, totals, from, to, filters, rateLabels());
    }

    /**
     * @param entries the journal rows (as loaded)
     * @param totals their sums
     * @param from start of the period
     * @param to end of the period
     * @param filters the filters as text lines (for the summary)
     * @param rateLabels the names of the tax rates ({@link #rateLabels()})
     */
    public JournalAdviserPackage(List<Entry> entries, Totals totals, Date from, Date to, List<String> filters, Map<BigDecimal, String> rateLabels) {
        this.entries = entries;
        this.totals = totals;
        this.from = from;
        this.to = to;
        this.filters = filters;
        this.rateLabels = rateLabels == null ? rateLabels() : rateLabels;
    }

    /**
     * @return the tax rates of the control panel (Taxes): rate - name (may be
     * empty)
     */
    public static Map<BigDecimal, String> rateLabels() {
        Map<BigDecimal, String> m = new java.util.TreeMap<BigDecimal, String>();
        try {
            for (mpv5.db.common.DatabaseObject o : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getTaxes())) {
                mpv5.db.objects.Tax tax = (mpv5.db.objects.Tax) o;
                BigDecimal k = JournalCalc.rateKey(tax.__getTaxvalue());
                String name = tax.__getCname() == null ? "" : tax.__getCname().trim();
                if (k != null && (!m.containsKey(k) || m.get(k).isEmpty())) {
                    m.put(k, name);
                }
            }
        } catch (Exception e) {
            Log.Debug(JournalAdviserPackage.class, e.getMessage());
        }
        return m;
    }

    /**
     * @param rate the stored rate, null: unknown (a document without items)
     * @param labels the names of the control panel
     * @param unknown the text for null
     * @return "19 %" or "19 % (name)" with the name of the control panel
     */
    public static String rateLabel(BigDecimal rate, Map<BigDecimal, String> labels, String unknown) {
        if (rate == null) {
            return unknown;
        }
        BigDecimal k = JournalCalc.rateKey(rate);
        String s = k.toPlainString().replace('.', ',') + " %";
        String name = labels == null ? null : labels.get(k);
        if (name == null || name.isEmpty() || name.equals(s)) {
            return s;
        }
        //a name with the rate in it ("MwSt 19%") is enough
        return name.replace(" ", "").contains(k.toPlainString().replace('.', ',') + "%")
                || name.replace(" ", "").contains(k.toPlainString() + "%") ? name : s + " (" + name + ")";
    }

    /**
     * @return the number of receipts written by {@link #write}
     */
    public int getReceipts() {
        return receipts;
    }

    /**
     * @return the number of e-invoices written by {@link #write}
     */
    public int getEInvoices() {
        return einvoices;
    }

    /**
     * @return what could not be added (e.g. incomplete e-invoices, missing
     * receipt files)
     */
    public List<String> getProblems() {
        return problems;
    }

    /**
     * @param item
     * @return true for the item types which are invoices in the sense of the
     * e-invoice rules
     */
    public static boolean isInvoice(Entry e) {
        return e.obj instanceof Item && (e.type == Constants.TYPE_INVOICE || e.type == Constants.TYPE_CREDIT
                || e.type == Constants.TYPE_DEPOSIT || e.type == Constants.TYPE_PART_PAYMENT);
    }

    /**
     * @param c
     * @return true if the contact is a business (company flag or company
     * name)
     */
    public static boolean isBusiness(Contact c) {
        return c != null && (c.__getisCompany() || (c.__getCompany() != null && c.__getCompany().trim().length() > 0));
    }

    private Contact contact(Item i) {
        Integer id = i.__getContactsids();
        if (!contacts.containsKey(id)) {
            Contact c = null;
            try {
                c = i.getContact();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            contacts.put(id, c);
        }
        return contacts.get(id);
    }

    /**
     * Checks the outgoing invoices: which go to businesses (above the small
     * amount) and whether their contact gets an e-invoice. Cancelled
     * documents are left out.
     *
     * @param entries
     * @return the check
     */
    public EInvoiceCheck checkEInvoices() {
        EInvoiceCheck c = new EInvoiceCheck();
        for (Entry e : entries) {
            if (!isInvoice(e) || JournalCalc.isCancelled(e)) {
                continue;
            }
            c.invoices++;
            Item i = (Item) e.obj;
            Contact k = contact(i);
            if (!isBusiness(k) || e.gross.abs().compareTo(SMALL_AMOUNT) <= 0) {
                continue;
            }
            c.b2b++;
            if (EInvoiceDelivery.effectiveFor(k).isEInvoice()) {
                c.b2bEInvoice++;
            } else {
                c.b2bWithoutEInvoice.add(e.number);
            }
        }
        return c;
    }

    /**
     * Writes the ZIP
     *
     * @param target
     * @throws IOException
     */
    public void write(File target) throws IOException {
        receipts = 0;
        einvoices = 0;
        problems.clear();
        EInvoiceCheck check = checkEInvoices();
        Set<String> names = new HashSet<String>();
        File tmp = Files.createTempDirectory("yabs-journal").toFile();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(target.toPath()))) {
            byte[] csv = csv().getBytes(StandardCharsets.UTF_8);
            put(zip, names, "journal.csv", concat(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, csv));
            put(zip, names, "journal_steuersaetze.csv", concat(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                    csvByRate().getBytes(StandardCharsets.UTF_8)));
            receipts(zip, names);
            eInvoices(zip, names, tmp);
            put(zip, names, "zusammenfassung.txt", summary(check).getBytes(StandardCharsets.UTF_8));
        } finally {
            for (File f : tmp.listFiles() == null ? new File[0] : tmp.listFiles()) {
                f.delete();
            }
            tmp.delete();
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    private static void put(ZipOutputStream zip, Set<String> names, String name, byte[] data) throws IOException {
        String n = name;
        int i = 2;
        while (names.contains(n)) {
            int dot = name.lastIndexOf('.');
            n = dot > 0 ? name.substring(0, dot) + "_" + i + name.substring(dot) : name + "_" + i;
            i++;
        }
        names.add(n);
        zip.putNextEntry(new ZipEntry(n));
        zip.write(data);
        zip.closeEntry();
    }

    /**
     * @param s
     * @return a file name part without characters which are not allowed
     */
    static String safe(String s) {
        return (s == null ? "" : s).replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private void receipts(ZipOutputStream zip, Set<String> names) {
        for (Entry e : entries) {
            try {
                if (e.obj instanceof Expense) {
                    for (ExpenseReceipts.Receipt r : ExpenseReceipts.of(((Expense) e.obj).__getIDS())) {
                        addReceipt(zip, names, "belege/ausgaben/", e, r.file(), r.originalName);
                    }
                } else if (e.obj instanceof Revenue) {
                    for (RevenueReceipts.Receipt r : RevenueReceipts.of(((Revenue) e.obj).__getIDS())) {
                        addReceipt(zip, names, "belege/einnahmen/", e, r.file(), r.originalName);
                    }
                }
            } catch (Exception ex) {
                Log.Debug(this, ex);
                problems.add(e.number + ": " + ex.getMessage());
            }
        }
    }

    private void addReceipt(ZipOutputStream zip, Set<String> names, String dir, Entry e, File f, String original) throws IOException {
        if (f == null || !f.isFile()) {
            problems.add(e.number + ": Beleg fehlt (" + (f == null ? original : f.getPath()) + ")");
            return;
        }
        String name = original == null || original.trim().isEmpty() ? f.getName() : original;
        put(zip, names, dir + safe(e.number) + "_" + safe(name), Files.readAllBytes(f.toPath()));
        receipts++;
    }

    private void eInvoices(ZipOutputStream zip, Set<String> names, File tmp) {
        for (Entry e : entries) {
            if (!isInvoice(e) || JournalCalc.isCancelled(e)) {
                continue;
            }
            Item i = (Item) e.obj;
            Contact k = contact(i);
            if (k == null || !EInvoiceDelivery.effectiveFor(k).isEInvoice() || !ZugpferdFile.supports(i)) {
                continue;
            }
            try {
                Invoice inv = ZugpferdFile.createInvoice(i, ZugpferdFile.Format.XRECHNUNG);
                List<String> p = ZugpferdFile.validate(inv);
                if (!p.isEmpty()) {
                    problems.add(e.number + ": E-Rechnung unvollständig: " + String.join(", ", p));
                    continue;
                }
                File x = new File(tmp, safe(e.number) + ".xml");
                ZugpferdFile.writeXml(inv, x);
                put(zip, names, "e-rechnungen/" + safe(e.number) + ".xml", Files.readAllBytes(x.toPath()));
                einvoices++;
            } catch (Exception ex) {
                Log.Debug(this, ex);
                problems.add(e.number + ": E-Rechnung: " + ex.getMessage());
            }
        }
    }

    private static String date(Date d) {
        return d == null ? "" : new SimpleDateFormat("dd.MM.yyyy").format(d);
    }

    /**
     * @param v
     * @return the amount with decimal comma and without thousands separator
     * (as DATEV and Excel (de) read it)
     */
    public static String amount(BigDecimal v) {
        DecimalFormat f = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY));
        return f.format(v == null ? BigDecimal.ZERO : v);
    }

    /**
     * @param s
     * @return the CSV cell (always quoted)
     */
    public static String cell(Object s) {
        String v = s == null ? "" : String.valueOf(s);
        return "\"" + v.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    /**
     * @param e
     * @return Einnahme / Ausgabe / -
     */
    static String side(Entry e) {
        if (!e.isAccounting()) {
            return "-";
        }
        return e.type == Constants.TYPE_EXPENSE || e.type == Constants.TYPE_CREDIT ? "Ausgabe/Minderung" : "Einnahme";
    }

    static String typeName(Entry e) {
        switch (e.type) {
            case Constants.TYPE_INVOICE:
                return "Rechnung";
            case Constants.TYPE_CREDIT:
                return "Gutschrift";
            case Constants.TYPE_DEPOSIT:
                return "Anzahlungsrechnung";
            case Constants.TYPE_PART_PAYMENT:
                return "Abschlagsrechnung";
            case Constants.TYPE_REVENUE:
                return "Einnahme";
            case Constants.TYPE_EXPENSE:
                return "Ausgabe";
            case Constants.TYPE_PAYMENT:
                return "Zahlungseingang";
            case Constants.TYPE_ORDER:
                return "Auftrag";
            case Constants.TYPE_OFFER:
                return "Angebot";
            default:
                return String.valueOf(e.type);
        }
    }

    static String statusName(Entry e) {
        if (JournalCalc.isPaid(e)) {
            return "bezahlt";
        } else if (JournalCalc.isCancelled(e)) {
            return "storniert";
        }
        return "offen";
    }

    String rateName(BigDecimal rate) {
        return rateLabel(rate, rateLabels, "unbekannt (ohne Positionen)");
    }

    /**
     * @param e
     * @return the amounts of the entry by rate (one of unknown rate if none
     * are known)
     */
    static Map<BigDecimal, BigDecimal[]> ratesOf(Entry e) {
        if (e.rates != null && !e.rates.isEmpty()) {
            return e.rates;
        }
        Map<BigDecimal, BigDecimal[]> m = new java.util.TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        m.put(null, new BigDecimal[]{e.net, e.tax});
        return m;
    }

    /**
     * @param o
     * @return the document date and the pay date of the record
     */
    private static Date[] dates(Object o) {
        try {
            if (o instanceof Item) {
                return new Date[]{((Item) o).__getDateadded(), ((Item) o).__getDateend()};
            } else if (o instanceof Expense) {
                return new Date[]{((Expense) o).__getDateadded(), ((Expense) o).__getDateend()};
            } else if (o instanceof Revenue) {
                return new Date[]{((Revenue) o).__getDateadded(), ((Revenue) o).__getDateend()};
            }
        } catch (Exception e) {
            Log.Debug(JournalAdviserPackage.class, e.getMessage());
        }
        return new Date[]{null, null};
    }

    /**
     * @return the journal as CSV (without BOM)
     */
    public String csv() {
        StringBuilder b = new StringBuilder();
        String[] head = {"Datum", "Belegdatum", "Zahlungsdatum", "Belegnummer", "Belegart", "Einnahme/Ausgabe", "Status",
            "Zaehlt zum Ergebnis", "Kontakt", "Gruppe", "Konto", "Buchungstext", "Netto", "Steuersaetze", "Steuer", "Brutto",
            "Ergebnis netto (EUeR)"};
        for (int i = 0; i < head.length; i++) {
            b.append(i > 0 ? ";" : "").append(cell(head[i]));
        }
        b.append("\r\n");
        for (Entry e : entries) {
            Date[] d = e.type == Constants.TYPE_PAYMENT ? new Date[]{null, e.date} : dates(e.obj);
            String contact = (e.contact + " " + e.prename).trim();
            Object[] row = {date(e.date), date(d[0]), date(d[1]), e.number, typeName(e), side(e), statusName(e),
                e.counted ? "ja" : "nein", contact, e.group, e.account, e.description,
                amount(e.net), rateNames(e), amount(e.tax), amount(e.gross), amount(e.effect)};
            for (int i = 0; i < row.length; i++) {
                b.append(i > 0 ? ";" : "").append(cell(row[i]));
            }
            b.append("\r\n");
        }
        return b.toString();
    }

    private String rateNames(Entry e) {
        List<String> l = new ArrayList<String>();
        for (BigDecimal r : ratesOf(e).keySet()) {
            l.add(rateName(r));
        }
        return String.join(", ", l);
    }

    /**
     * @return the journal by tax rate: one line per document and rate (without
     * BOM)
     */
    public String csvByRate() {
        StringBuilder b = new StringBuilder();
        String[] head = {"Datum", "Belegnummer", "Belegart", "Einnahme/Ausgabe", "Status", "Zaehlt zum Ergebnis", "Kontakt", "Gruppe",
            "Konto", "Buchungstext", "Steuersatz (%)", "Steuersatz", "Netto", "Steuer", "Brutto", "Ergebnis netto (EUeR)"};
        for (int i = 0; i < head.length; i++) {
            b.append(i > 0 ? ";" : "").append(cell(head[i]));
        }
        b.append("\r\n");
        for (Entry e : entries) {
            String contact = (e.contact + " " + e.prename).trim();
            int sign = e.effect.signum();
            for (Map.Entry<BigDecimal, BigDecimal[]> r : ratesOf(e).entrySet()) {
                BigDecimal net = r.getValue()[0];
                BigDecimal tax = r.getValue()[1];
                Object[] row = {date(e.date), e.number, typeName(e), side(e), statusName(e), e.counted ? "ja" : "nein", contact, e.group,
                    e.account, e.description, r.getKey() == null ? "" : r.getKey().toPlainString().replace('.', ','), rateName(r.getKey()),
                    amount(net), amount(tax), amount(net.add(tax)), amount(sign == 0 ? BigDecimal.ZERO : sign < 0 ? net.negate() : net)};
                for (int i = 0; i < row.length; i++) {
                    b.append(i > 0 ? ";" : "").append(cell(row[i]));
                }
                b.append("\r\n");
            }
        }
        return b.toString();
    }

    /**
     * @param check the e-invoice check
     * @return the summary (German)
     */
    public String summary(EInvoiceCheck check) {
        String nl = "\r\n";
        StringBuilder b = new StringBuilder();
        b.append("Journal für den Steuerberater").append(nl);
        b.append("Zeitraum: ").append(date(from)).append(" - ").append(date(to)).append(nl);
        b.append("Erstellt: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())).append(nl);
        for (String f : filters) {
            b.append(f).append(nl);
        }
        b.append(nl).append("Ergebnis (Einnahmen-Überschuss-Rechnung, nur bezahlte Belege)").append(nl);
        b.append(line("Betriebseinnahmen netto", totals.incomeNet));
        b.append(line("Umsatzsteuer", totals.incomeTax));
        b.append(line("Betriebseinnahmen brutto", totals.incomeGross));
        b.append(line("Betriebsausgaben netto", totals.expenseNet));
        b.append(line("Vorsteuer", totals.expenseTax));
        b.append(line("Betriebsausgaben brutto", totals.expenseGross));
        b.append(line("Überschuss netto", totals.profitNet()));
        b.append(line("Umsatzsteuer - Vorsteuer", totals.profitTax()));
        b.append(nl).append("Einnahmen nach Steuersatz (netto / Umsatzsteuer)").append(nl);
        for (Map.Entry<BigDecimal, BigDecimal[]> r : totals.incomeByRate.entrySet()) {
            b.append(String.format("  %-30s %15s %15s", rateName(r.getKey()), amount(r.getValue()[0]), amount(r.getValue()[1]))).append(nl);
        }
        b.append(nl).append("Ausgaben nach Steuersatz (netto / Vorsteuer)").append(nl);
        for (Map.Entry<BigDecimal, BigDecimal[]> r : totals.expensesByRate.entrySet()) {
            b.append(String.format("  %-30s %15s %15s", rateName(r.getKey()), amount(r.getValue()[0]), amount(r.getValue()[1]))).append(nl);
        }
        b.append("  (Steuers\u00e4tze wie im Beleg gespeichert; Belege mit mehreren S\u00e4tzen nach ihren Positionen aufgeteilt)").append(nl);
        b.append(nl).append("Offene Posten der gezeigten Belege").append(nl);
        b.append(line("Offene Forderungen (brutto)", totals.openReceivables));
        b.append(line("Offene Verbindlichkeiten (brutto)", totals.openPayables));
        b.append(nl).append("Inhalt").append(nl);
        b.append("  journal.csv            ").append(entries.size()).append(" Buchungen, eine Zeile je Beleg (Semikolon, Dezimalkomma, UTF-8)").append(nl);
        b.append("  journal_steuersaetze.csv  eine Zeile je Beleg und Steuersatz").append(nl);
        b.append("  belege/                ").append(receipts).append(" archivierte Belege (Originaldateien, auch empfangene E-Rechnungen)").append(nl);
        b.append("  e-rechnungen/          ").append(einvoices).append(" ausgehende E-Rechnungen (XRechnung, aus den Belegdaten erzeugt)").append(nl);
        b.append(nl).append("E-Rechnung (Ausgangsrechnungen im Zeitraum)").append(nl);
        b.append("  Rechnungen gesamt: ").append(check.invoices).append(nl);
        b.append("  davon an Unternehmen über 250 EUR: ").append(check.b2b).append(", als E-Rechnung: ").append(check.b2bEInvoice).append(nl);
        if (!check.b2bWithoutEInvoice.isEmpty()) {
            b.append("  ohne E-Rechnung: ").append(String.join(", ", check.b2bWithoutEInvoice)).append(nl);
        }
        if (!problems.isEmpty()) {
            b.append(nl).append("Nicht enthalten / zu prüfen").append(nl);
            for (String p : problems) {
                b.append("  - ").append(p).append(nl);
            }
        }
        return b.toString();
    }

    private static String line(String label, BigDecimal v) {
        return String.format("  %-36s %15s", label, amount(v)) + "\r\n";
    }

    /**
     * Writes bytes (helper for callers which only want the CSV)
     *
     * @param out
     * @param s
     * @throws IOException
     */
    public static void writeCsv(OutputStream out, String s) throws IOException {
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.write(s.getBytes(StandardCharsets.UTF_8));
    }
}
