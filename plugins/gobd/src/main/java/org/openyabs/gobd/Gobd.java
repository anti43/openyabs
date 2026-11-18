/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.gobd;

import static org.openyabs.gobd.GobdUtil.blank;
import static org.openyabs.gobd.GobdUtil.esc;
import static org.openyabs.gobd.GobdUtil.fmtDate;
import static org.openyabs.gobd.GobdUtil.fmtDec;
import static org.openyabs.gobd.GobdUtil.str;
import static org.openyabs.gobd.GobdUtil.trunc;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Account;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.handler.Activity;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.panels.ChangeNotApprovedException;

/**
 * The GoBD rules: the database tables of the plugin, the audit log, the
 * finalizing (Festschreibung) of invoices with the SHA-256 hash chain,
 * cancellation invoices, the import of e-invoices as income and the guard
 * that blocks changes of finalized records
 */
public final class Gobd {

    /**
     * Invoices under § 14 UStG: invoices, credit notes (also the own
     * cancellation invoices), deposit and part payment invoices. Never orders,
     * offers or confirmations.
     */
    public static final List<Integer> ITEM_TYPES = Arrays.asList(Item.TYPE_INVOICE, Item.TYPE_CREDIT, Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT);
    static final String S_ARCHIVE = "gobd.archivePath";
    static final String S_AUTOLOCK = "gobd.autoLockOnExport";
    static final String S_AUTOINCOME = "gobd.autoImportIncome";
    static final String S_WATCH = "gobd.incomeWatchPath";

    final ExtensionContext yabs;
    /**
     * The files (PDF, ZUGFeRD, XRechnung) last created for a document
     */
    final Map<Long, Path> lastGeneratedFiles = new ConcurrentHashMap<Long, Path>();
    /**
     * Master data of contacts before saving (old/new in the audit log)
     */
    final Map<String, String> masterDataBeforeSave = new ConcurrentHashMap<String, String>();
    final Set<Integer> finalizingIds = Collections.synchronizedSet(new HashSet<Integer>());
    final Set<String> seenWatchFiles = Collections.synchronizedSet(new HashSet<String>());
    volatile boolean creatingStorno = false;
    private Timer incomeWatchTimer;

    Gobd(ExtensionContext yabs) {
        this.yabs = yabs;
    }

    // ---------------------------------------------------------------- basics
    Object[][] rows(String sql) {
        return yabs.select(sql);
    }

    Object[] row(String sql) {
        Object[][] r = rows(sql);
        return r.length > 0 ? r[0] : null;
    }

    void exec(String sql) {
        yabs.execute(sql);
    }

    void error(String msg) {
        yabs.error(msg);
    }

    void status(String msg) {
        mpv5.ui.dialogs.Notificator.raiseNotification(msg, false);
    }

    void popup(String msg) {
        yabs.popup(msg);
    }

    /**
     * @return the archive folder (settings, else the data folder of the
     * plugin), created on demand
     */
    Path archiveRoot() {
        String fallback = yabs.dataFolder().toPath().resolve("gobd-archive").toAbsolutePath().toString();
        Path p = Paths.get(yabs.settings().get(S_ARCHIVE, fallback).trim());
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
        }
        return p;
    }

    static String currentUser() {
        try {
            User u = User.getCurrentUser();
            String n = u == null ? null : u.__getCname();
            return blank(n) ? System.getProperty("user.name", "system") : n;
        } catch (Exception e) {
            return System.getProperty("user.name", "system");
        }
    }

    static boolean isGobdItemType(int type) {
        return ITEM_TYPES.contains(type);
    }

    static boolean isTaxRelevant(DatabaseObject obj) {
        if (obj instanceof Item) {
            return isGobdItemType(((Item) obj).__getInttype());
        }
        String ctx = obj == null || obj.getContext() == null ? "" : String.valueOf(obj.getContext().getDbIdentity()).toLowerCase();
        return ctx.contains("expense") || ctx.contains("revenue");
    }

    /**
     * @return true if the record is moved to the trash (invisible)
     */
    static boolean isMovedToTrash(DatabaseObject obj) {
        for (String m : new String[]{"__getInvisible", "isInvisible"}) {
            try {
                Object v = obj.getClass().getMethod(m).invoke(obj);
                if (v != null) {
                    return !v.toString().equals("0") && !v.toString().equalsIgnoreCase("false");
                }
            } catch (NoSuchMethodException e) {
                //try the next
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    static ChangeNotApprovedException veto(DatabaseObject obj) {
        return new ChangeNotApprovedException(obj);
    }

    // ---------------------------------------------------------------- schema
    /**
     * Creates the tables of the plugin (H2, MySQL / MariaDB, PostgreSQL)
     */
    void initSchema() {
        exec("CREATE TABLE IF NOT EXISTS gobd_document_lock ("
                + " document_id BIGINT PRIMARY KEY, chain_seq BIGINT NULL, document_number VARCHAR(100) NOT NULL UNIQUE,"
                + " status VARCHAR(20) NOT NULL, finalized_at TIMESTAMP NOT NULL, finalized_by VARCHAR(100) NOT NULL,"
                + " customer_snapshot TEXT NOT NULL, positions_snapshot TEXT NOT NULL, archive_path VARCHAR(512) NOT NULL,"
                + " artifact_format VARCHAR(30) NOT NULL, sha256_hash CHAR(64) NOT NULL, prev_sha256_hash CHAR(64) NOT NULL,"
                + " cancels_document_id BIGINT NULL)");
        exec("CREATE TABLE IF NOT EXISTS gobd_audit_log ("
                + " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " user_id VARCHAR(100) NOT NULL, entity_type VARCHAR(50) NOT NULL, entity_id BIGINT NOT NULL,"
                + " action VARCHAR(50) NOT NULL, old_value VARCHAR(2000) NULL, new_value VARCHAR(2000) NULL,"
                + " file_hash CHAR(64) NULL, details VARCHAR(1000) NULL)");
        for (String alter : new String[]{
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS chain_seq BIGINT NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS customer_snapshot TEXT NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS positions_snapshot TEXT NULL",
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS old_value VARCHAR(2000) NULL",
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS new_value VARCHAR(2000) NULL"}) {
            exec(alter);
        }
        //receipts of expenses: the files are not in the database but byte-identical in
        //<archive>/eingangsbelege/<year>/ with a SHA256SUMS file; the table holds metadata and a hash chain
        exec("CREATE TABLE IF NOT EXISTS gobd_receipt ("
                + " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, expense_id BIGINT NOT NULL, chain_seq BIGINT NOT NULL,"
                + " receipt_ref VARCHAR(250) NULL, original_name VARCHAR(512) NOT NULL, archive_path VARCHAR(1024) NOT NULL,"
                + " file_size BIGINT NOT NULL, artifact_format VARCHAR(30) NOT NULL, sha256 CHAR(64) NOT NULL,"
                + " chain_hash CHAR(64) NOT NULL, prev_chain_hash CHAR(64) NOT NULL, expense_snapshot VARCHAR(2000) NULL,"
                + " imported_at TIMESTAMP NOT NULL, imported_by VARCHAR(100) NOT NULL)");
        //income from e-invoices: the original under <archive>/einnahmen/<year>/ with SHA256SUMS and hash chain
        exec("CREATE TABLE IF NOT EXISTS gobd_income_lock ("
                + " revenue_id BIGINT PRIMARY KEY, chain_seq BIGINT NOT NULL, document_number VARCHAR(100) NOT NULL,"
                + " status VARCHAR(20) NOT NULL, imported_at TIMESTAMP NOT NULL, imported_by VARCHAR(100) NOT NULL,"
                + " original_name VARCHAR(512) NOT NULL, archive_path VARCHAR(1024) NOT NULL, file_size BIGINT NOT NULL,"
                + " artifact_format VARCHAR(30) NOT NULL, file_sha256 CHAR(64) NOT NULL UNIQUE, chain_hash CHAR(64) NOT NULL,"
                + " prev_chain_hash CHAR(64) NOT NULL, financial_snapshot VARCHAR(2000) NOT NULL)");
    }

    // ---------------------------------------------------------------- audit
    void logAudit(String entityType, long entityId, String action, String oldVal, String newVal, String fileHash, String details) {
        String o = trunc(oldVal, 2000);
        String n = trunc(newVal, 2000);
        String d = trunc(details == null ? "" : details, 1000);
        exec("INSERT INTO gobd_audit_log (created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details)"
                + " VALUES (CURRENT_TIMESTAMP, '" + esc(currentUser()) + "', '" + esc(entityType) + "', " + entityId + ", '" + esc(action) + "', "
                + (o != null ? "'" + esc(o) + "'" : "NULL") + ", " + (n != null ? "'" + esc(n) + "'" : "NULL") + ", "
                + (fileHash != null ? "'" + esc(fileHash) + "'" : "NULL") + ", '" + esc(d) + "')");
    }

    // ---------------------------------------------------------------- locks
    /**
     * @return document_id, document_number, status, finalized_at, finalized_by,
     * customer_snapshot, positions_snapshot, archive_path, artifact_format,
     * sha256_hash, prev_sha256_hash, cancels_document_id; null if not finalized
     */
    Object[] lockRecord(long docId) {
        return row("SELECT document_id, document_number, status, finalized_at, finalized_by, customer_snapshot, positions_snapshot,"
                + " archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id FROM gobd_document_lock WHERE document_id = " + docId);
    }

    /**
     * @return revenue_id, chain_seq, document_number, status, imported_at,
     * imported_by, original_name, archive_path, file_size, artifact_format,
     * file_sha256, chain_hash, prev_chain_hash, financial_snapshot; null if none
     */
    Object[] incomeLockRecord(long revId) {
        return row("SELECT revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name, archive_path, file_size,"
                + " artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot FROM gobd_income_lock WHERE revenue_id = " + revId);
    }

    String latestChainHash() {
        Object[] r = row("SELECT sha256_hash FROM gobd_document_lock ORDER BY COALESCE(chain_seq, document_id) DESC, finalized_at DESC, document_id DESC");
        return r != null && r[0] != null && !r[0].toString().isEmpty() ? r[0].toString() : GobdUtil.GENESIS_HASH;
    }

    long nextChainSeq() {
        Object[] r = row("SELECT COALESCE(MAX(chain_seq), 0) FROM gobd_document_lock");
        return (r == null ? 0L : GobdUtil.toLong(r[0], 0L)) + 1L;
    }

    String latestIncomeChainHash() {
        Object[] r = row("SELECT chain_hash FROM gobd_income_lock ORDER BY chain_seq DESC, imported_at DESC, revenue_id DESC");
        return r != null && r[0] != null && !r[0].toString().isEmpty() ? r[0].toString() : GobdUtil.GENESIS_HASH;
    }

    long nextIncomeChainSeq() {
        Object[] r = row("SELECT COALESCE(MAX(chain_seq), 0) FROM gobd_income_lock");
        return (r == null ? 0L : GobdUtil.toLong(r[0], 0L)) + 1L;
    }

    String nextSequenceNumber(String prefix, int year) {
        String pattern = prefix + "-" + year + "-";
        int max = 0;
        for (String sql : new String[]{
            "SELECT document_number FROM gobd_document_lock WHERE document_number LIKE '" + esc(pattern) + "%'",
            "SELECT cnumber FROM items WHERE cnumber LIKE '" + esc(pattern) + "%'"}) {
            for (Object[] r : rows(sql)) {
                String val = str(r[0]).trim();
                if (val.startsWith(pattern) && GobdUtil.isInteger(val.substring(pattern.length()))) {
                    max = Math.max(max, Integer.parseInt(val.substring(pattern.length()).trim()));
                }
            }
        }
        return String.format("%s-%d-%05d", prefix, year, max + 1);
    }

    // ---------------------------------------------------------------- snapshots
    static List<SubItem> subItems(Item doc) {
        try {
            List<SubItem> l = DatabaseObject.getReferencedObjects(doc, Context.getSubItem(), new SubItem(), false);
            return l == null ? new ArrayList<SubItem>() : l;
        } catch (NodataFoundException e) {
            return new ArrayList<SubItem>();
        }
    }

    static String financialCore(Item doc) {
        return String.join("|",
                "Num=" + str(doc.__getCnumber()),
                "Type=" + doc.__getInttype(),
                "Contact=" + doc.__getContactsids(),
                "Acc=" + doc.__getAccountsids(),
                "IssueDate=" + fmtDate(doc.__getDateadded()),
                "DeliveryDate=" + fmtDate(doc.__getDatetodo()),
                "EndDate=" + fmtDate(doc.__getDateend()),
                "Net=" + fmtDec(doc.__getNetvalue()),
                "Disc=" + fmtDec(doc.__getDiscountvalue()),
                "Tax=" + fmtDec(doc.__getTaxvalue()),
                "DiscGross=" + fmtDec(doc.__getDiscountGrosvalue()),
                "Desc=" + str(doc.__getDescription()).trim());
    }

    static String revenueSnapshot(Revenue rev) {
        return String.join("|",
                "Num=" + str(rev.__getCnumber()),
                "Date=" + fmtDate(rev.__getDateadded()),
                "Acc=" + rev.__getAccountsids(),
                "Net=" + fmtDec(rev.__getNetvalue()),
                "TaxPct=" + fmtDec(rev.__getTaxpercentvalue()),
                "Gross=" + fmtDec(rev.__getBrutvalue()),
                "Desc=" + str(rev.__getDescription()).trim());
    }

    static String expenseSnapshot(Expense exp) {
        return String.join("|",
                "Num=" + str(exp.__getCnumber()),
                "Date=" + fmtDate(exp.__getDateadded()),
                "Acc=" + exp.__getAccountsids(),
                "Net=" + fmtDec(exp.__getNetvalue()),
                "TaxPct=" + fmtDec(exp.__getTaxpercentvalue()),
                "Gross=" + fmtDec(exp.__getBrutvalue()),
                "Desc=" + str(exp.__getDescription()).trim());
    }

    /**
     * The same as {@link #revenueSnapshot} / {@link #expenseSnapshot} from the
     * columns cnumber, dateadded, accountsids, netvalue, taxpercentvalue,
     * brutvalue, description
     */
    static String moneySnapshotOfRow(Object[] db) {
        return String.join("|",
                "Num=" + str(db[0]),
                "Date=" + fmtDate(db[1]),
                "Acc=" + db[2],
                "Net=" + fmtDec(db[3]),
                "TaxPct=" + fmtDec(db[4]),
                "Gross=" + fmtDec(db[5]),
                "Desc=" + str(db[6]).trim());
    }

    static String contactSnapshot(Contact c) {
        return String.join(" | ",
                "ID=" + c.__getIDS(),
                "Nr=" + str(c.__getCNumber()),
                "Firma=" + str(c.__getCompany()),
                ("Name=" + str(c.__getPrename()) + " " + str(c.__getCname())).trim(),
                "Strasse=" + str(c.__getStreet()),
                "PLZ=" + str(c.__getZip()),
                "Ort=" + str(c.__getCity()),
                "Land=" + str(c.__getCountry()),
                "SteuerNr=" + str(c.__getTaxnumber()));
    }

    static String contactSnapshot(long contactId) {
        if (contactId <= 0) {
            return "Kein Kontakt zugewiesen";
        }
        try {
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), (int) contactId);
            return c == null ? "Kontakt #" + contactId + " (nicht gefunden)" : contactSnapshot(c);
        } catch (Exception e) {
            return "Kontakt #" + contactId;
        }
    }

    static String positionsSnapshot(Item doc) {
        try {
            List<SubItem> l = subItems(doc);
            if (l.isEmpty()) {
                return "Keine Positionen";
            }
            return l.stream().map(s -> fmtDec(s.__getQuantityvalue()) + "x " + str(s.__getDescription()) + " @ "
                    + fmtDec(s.__getExternalvalue()) + " EUR (USt " + fmtDec(s.__getTaxpercentvalue()) + "%)")
                    .collect(Collectors.joining(" ;; "));
        } catch (Exception e) {
            return "Positionen nicht lesbar";
        }
    }

    static List<String> validateMandatoryFields(Item doc) {
        List<String> errors = new ArrayList<String>();
        String num = str(doc.__getCnumber()).trim();
        if (GobdUtil.isDraftNumber(num)) {
            errors.add("Endgueltige fortlaufende Rechnungsnummer fehlt (aktuell: '" + num + "') (§ 14 Abs. 4 Nr. 4 UStG)");
        }
        if (doc.__getDateadded() == null) {
            errors.add("Ausstellungsdatum fehlt (§ 14 Abs. 4 Nr. 3 UStG)");
        }
        if (doc.__getDatetodo() == null) {
            errors.add("Leistungs-/Lieferdatum fehlt (§ 14 Abs. 4 Nr. 6 UStG)");
        }
        if (doc.__getContactsids() <= 0) {
            errors.add("Leistungsempfaenger/Kontakt fehlt (§ 14 Abs. 4 Nr. 1 UStG)");
        } else {
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), doc.__getContactsids());
                if (c != null && (blank(c.__getStreet()) || blank(c.__getCity()))) {
                    errors.add("Vollstaendige Anschrift (Strasse/Ort) des Leistungsempfaengers fehlt (§ 14 Abs. 4 Nr. 1 UStG)");
                }
            } catch (Exception ignored) {
                //no contact
            }
        }
        if (doc.__getNetvalue() == null || doc.getGrossAmount() == null) {
            errors.add("Entgelt / Bruttobetrag fehlt (§ 14 Abs. 4 Nr. 7 UStG)");
        }
        if (subItems(doc).isEmpty()) {
            errors.add("Mindestens eine Rechnungsposition ist erforderlich (§ 14 Abs. 4 Nr. 5 UStG)");
        }
        return errors;
    }

    // ---------------------------------------------------------------- finalizing
    private static final class Archived {

        final Path path;
        final byte[] bytes;
        final String format;

        Archived(Path path, byte[] bytes, String format) {
            this.path = path;
            this.bytes = bytes;
            this.format = format;
        }
    }

    /**
     * Archives the e-invoice / PDF just created for the document, else an XML
     * of its data
     */
    private Archived archiveArtifact(Item doc, int year, String safeDocNum, String customerSnap) throws Exception {
        Path yearDir = archiveRoot().resolve(String.valueOf(year));
        Files.createDirectories(yearDir);
        Path recent = lastGeneratedFiles.get((long) doc.__getIDS());
        if (recent != null && Files.exists(recent)) {
            String fileName = recent.getFileName().toString();
            String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "pdf";
            String format = ext.equals("xml") ? "XRECHNUNG_XML" : "ZUGFERD_PDFA3";
            Path target = GobdUtil.uniquePath(yearDir, safeDocNum, ext);
            Files.copy(recent, target);
            GobdUtil.makeReadOnly(target);
            return new Archived(target, Files.readAllBytes(target), format);
        }
        Path target = GobdUtil.uniquePath(yearDir, safeDocNum, "xml");
        BigDecimal net = nz(doc.__getNetvalue()).subtract(nz(doc.__getDiscountvalue()));
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<GoBDArchivedInvoice xmlns=\"urn:openyabs:gobd:archive:1.0\" standard=\"EN16931\">\n");
        xml.append("  <DocumentId>").append(doc.__getIDS()).append("</DocumentId>\n");
        xml.append("  <DocumentNumber>").append(GobdUtil.escapeXml(doc.__getCnumber())).append("</DocumentNumber>\n");
        xml.append("  <DocumentType>").append(doc.__getInttype() == Item.TYPE_CREDIT ? "381_CREDIT_NOTE" : "380_INVOICE").append("</DocumentType>\n");
        xml.append("  <IssueDate>").append(GobdUtil.escapeXml(fmtDate(doc.__getDateadded()))).append("</IssueDate>\n");
        xml.append("  <DeliveryDate>").append(GobdUtil.escapeXml(fmtDate(doc.__getDatetodo()))).append("</DeliveryDate>\n");
        xml.append("  <ContactId>").append(doc.__getContactsids()).append("</ContactId>\n");
        xml.append("  <FrozenCustomerSnapshot>").append(GobdUtil.escapeXml(customerSnap)).append("</FrozenCustomerSnapshot>\n");
        xml.append("  <NetAmount currencyID=\"EUR\">").append(fmtDec(net)).append("</NetAmount>\n");
        xml.append("  <TaxAmount currencyID=\"EUR\">").append(fmtDec(nz(doc.__getTaxvalue()))).append("</TaxAmount>\n");
        xml.append("  <GrossAmount currencyID=\"EUR\">").append(fmtDec(nz(doc.getGrossAmount()))).append("</GrossAmount>\n");
        xml.append("  <LineItems>\n");
        for (SubItem line : subItems(doc)) {
            xml.append("    <LineItem>\n");
            xml.append("      <Description>").append(GobdUtil.escapeXml(line.__getDescription())).append("</Description>\n");
            xml.append("      <Quantity>").append(fmtDec(line.__getQuantityvalue())).append("</Quantity>\n");
            xml.append("      <UnitPrice>").append(fmtDec(line.__getExternalvalue())).append("</UnitPrice>\n");
            xml.append("      <DiscountPercent>").append(fmtDec(line.__getDiscount())).append("</DiscountPercent>\n");
            xml.append("      <TaxRatePercent>").append(fmtDec(line.__getTaxpercentvalue())).append("</TaxRatePercent>\n");
            xml.append("    </LineItem>\n");
        }
        xml.append("  </LineItems>\n");
        xml.append("</GoBDArchivedInvoice>\n");
        byte[] bytes = xml.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
        GobdUtil.makeReadOnly(target);
        return new Archived(target, bytes, "XRECHNUNG_XML");
    }

    static BigDecimal nz(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b;
    }

    /**
     * Finalizes (festschreiben) an invoice: final number, status finished,
     * archived file, snapshots, hash chain, audit log
     *
     * @param doc
     * @param cancelsDocId the invoice a cancellation invoice cancels, or null
     * @return true if finalized now
     */
    boolean finalizeDocument(Item doc, Long cancelsDocId) {
        if (doc == null || !isGobdItemType(doc.__getInttype())) {
            return false;
        }
        if (!finalizingIds.add(doc.__getIDS())) {
            return false;
        }
        try {
            if (lockRecord(doc.__getIDS()) != null) {
                status("GoBD: Beleg " + doc.__getCnumber() + " ist bereits festgeschrieben.");
                return false;
            }
            int year = LocalDate.now().getYear();
            if (GobdUtil.isDraftNumber(doc.__getCnumber())) {
                doc.setCnumber(nextSequenceNumber(doc.__getInttype() == Item.TYPE_CREDIT ? "ST" : "RE", year));
            }
            //status finished and end date (without an own save)
            mpv5.handler.ItemService.ensureFinishedAndEndDate(doc, false);
            doc.save(true);

            List<String> problems = validateMandatoryFields(doc);
            if (!problems.isEmpty()) {
                error("GoBD-Festschreibung abgebrochen (§ 14 Abs. 4 UStG):\n- " + String.join("\n- ", problems));
                return false;
            }
            String safeDocNum = GobdUtil.safeFileName(blank(doc.__getCnumber()) ? "BELEG_" + doc.__getIDS() : doc.__getCnumber());
            String customerSnap = contactSnapshot((long) doc.__getContactsids());
            String positionsSnap = positionsSnapshot(doc);
            Archived a = archiveArtifact(doc, year, safeDocNum, customerSnap);

            long chainSeq = nextChainSeq();
            String prevHash = latestChainHash();
            String hash = GobdUtil.chained(a.bytes, prevHash);
            exec("INSERT INTO gobd_document_lock (document_id, chain_seq, document_number, status, finalized_at, finalized_by,"
                    + " customer_snapshot, positions_snapshot, archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id)"
                    + " VALUES (" + doc.__getIDS() + ", " + chainSeq + ", '" + esc(doc.__getCnumber()) + "', 'FINALIZED', CURRENT_TIMESTAMP, '"
                    + esc(currentUser()) + "', '" + esc(customerSnap) + "', '" + esc(positionsSnap) + "', '" + esc(a.path.toString()) + "', '"
                    + esc(a.format) + "', '" + esc(hash) + "', '" + esc(prevHash) + "', " + (cancelsDocId != null ? String.valueOf(cancelsDocId) : "NULL") + ")");
            yabs.refreshView(doc);
            logAudit("INVOICE", doc.__getIDS(), "FINALIZED", "STATUS=DRAFT", "STATUS=FINALIZED | SNAPSHOT=" + customerSnap, hash,
                    "Festgeschrieben: " + doc.__getCnumber() + " (" + a.format + ")");
            yabs.notify("GoBD-Festschreibung erfolgreich:\nBeleg: " + doc.__getCnumber() + " (" + a.format + ")\nSHA-256: "
                    + hash.substring(0, 16) + "...\nArchiv: " + a.path.getFileName(), doc);
            return true;
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
            error("GoBD-Festschreibung fehlgeschlagen:\n" + e.getMessage());
            return false;
        } finally {
            finalizingIds.remove(doc.__getIDS());
        }
    }

    /**
     * Cancels a finalized invoice with a finalized cancellation invoice
     *
     * @param original
     * @return true if cancelled
     */
    boolean stornoDocument(Item original) {
        if (original == null) {
            return false;
        }
        Object[] origLock = lockRecord(original.__getIDS());
        if (origLock == null || "CANCELLED".equals(str(origLock[2]))) {
            error("Dieser Beleg wurde bereits storniert oder ist nicht festgeschrieben.");
            return false;
        }
        String stornoNum = nextSequenceNumber("ST", LocalDate.now().getYear());
        creatingStorno = true;
        try {
            //a new record of the credit notes (the context comes with it)
            Item storno = (Item) DatabaseObject.getObject(Context.getCredit());
            storno.setContactsids(original.__getContactsids());
            storno.setAccountsids(original.__getAccountsids());
            storno.setGroupsids(original.__getGroupsids());
            storno.setInttype(Item.TYPE_CREDIT);
            storno.setCnumber(stornoNum);
            storno.setDateadded(new Date());
            storno.setDatetodo(original.__getDatetodo() != null ? original.__getDatetodo() : new Date());
            storno.setNetvalue(nz(original.__getNetvalue()).negate());
            storno.setTaxvalue(nz(original.__getTaxvalue()).negate());
            storno.setDiscountvalue(nz(original.__getDiscountvalue()).negate());
            storno.setDiscountGrosvalue(nz(original.__getDiscountGrosvalue()).negate());
            storno.setDescription("Stornorechnung zu Rechnung Nr. " + original.__getCnumber());
            storno.save(true);
            if (storno.__getIDS() <= 0) {
                error("Stornorechnung konnte nicht in der Datenbank gespeichert werden.");
                return false;
            }
            for (SubItem line : subItems(original)) {
                SubItem rev = new SubItem();
                rev.setItemsids(storno.__getIDS());
                rev.setOrdernr(line.getOrdernr());
                rev.setCname(blank(line.__getCname()) ? " " : line.__getCname());
                rev.setMeasure(str(line.__getMeasure()));
                rev.setDescription(str(line.__getDescription()));
                rev.setQuantityvalue(nz(line.__getQuantityvalue()).negate());
                rev.setExternalvalue(nz(line.__getExternalvalue()));
                rev.setInternalvalue(nz(line.__getInternalvalue()));
                rev.setDiscount(nz(line.__getDiscount()));
                rev.setTaxpercentvalue(nz(line.__getTaxpercentvalue()));
                rev.setDatedelivery(storno.__getDatetodo());
                rev.setDateadded(new Date());
                rev.setGroupsids(storno.__getGroupsids());
                rev.save();
            }
            if (finalizeDocument(storno, (long) original.__getIDS())) {
                exec("UPDATE gobd_document_lock SET status = 'CANCELLED' WHERE document_id = " + original.__getIDS());
                try {
                    original.setIntstatus(Item.STATUS_CANCELLED);
                    original.save(true);
                } catch (Exception ignored) {
                    //the lock says cancelled
                }
                yabs.refreshView(original);
                logAudit("INVOICE", original.__getIDS(), "CANCELLED", "STATUS=FINALIZED", "STATUS=CANCELLED BY " + storno.__getCnumber(),
                        str(origLock[9]), "Storniert durch Stornobeleg " + storno.__getCnumber());
                yabs.notify("Beleg " + original.__getCnumber() + " storniert durch " + storno.__getCnumber() + ".", original);
                return true;
            }
            return false;
        } finally {
            creatingStorno = false;
        }
    }

    /**
     * Lets the user pick an invoice
     *
     * @param title
     * @param onlyLocked finalized ones (else the not finalized ones)
     * @return the invoice, null if none or cancelled
     */
    Item selectInvoice(String title, boolean onlyLocked) {
        String types = ITEM_TYPES.stream().map(String::valueOf).collect(Collectors.joining(", "));
        String sql = onlyLocked
                ? "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i"
                + " INNER JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.status = 'FINALIZED' AND i.inttype IN (" + types + ")"
                : "SELECT i.ids, i.cnumber, (COALESCE(i.netvalue, 0) - COALESCE(i.discountvalue, 0) + COALESCE(i.taxvalue, 0)) FROM items i"
                + " LEFT JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.document_id IS NULL AND i.inttype IN (" + types + ") ORDER BY i.ids DESC";
        Object[][] r = rows(sql);
        if (r.length == 0) {
            popup(onlyLocked ? "Keine festgeschriebenen Rechnungen vorhanden." : "Keine offenen (unfestgeschriebenen) Rechnungen vorhanden.");
            return null;
        }
        String[] choices = new String[r.length];
        for (int i = 0; i < r.length; i++) {
            choices[i] = r[i][0] + " | " + r[i][1] + " (" + fmtDec(r[i][2]) + " EUR)";
        }
        String selected = (String) JOptionPane.showInputDialog(null, "Bitte Beleg auswaehlen:", title, JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
        if (blank(selected)) {
            return null;
        }
        try {
            return (Item) DatabaseObject.getObject(Context.getInvoice(), Integer.parseInt(selected.split("\\|")[0].trim()));
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
            return null;
        }
    }

    // ---------------------------------------------------------------- income import
    int defaultIncomeAccountId() {
        Object[] r = row("SELECT ids FROM accounts WHERE intaccounttype = " + Account.INCOME + " ORDER BY ids ASC");
        return r == null ? 1 : GobdUtil.toInt(r[0], 1);
    }

    /**
     * Imports a ZUGFeRD PDF / XRechnung XML as finalized income, or links a
     * file as original receipt to an existing income
     *
     * @param sourceFile
     * @param targetRevenue the income to link, null for a new one
     * @param silent no dialogs (the watch folder)
     * @return the income, null if not imported
     */
    Revenue importIncomeFile(Path sourceFile, Revenue targetRevenue, boolean silent) {
        try {
            return importIncome(sourceFile, targetRevenue, silent);
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
            if (!silent) {
                error("GoBD-Einnahmen-Import fehlgeschlagen:\n" + e.getMessage());
            }
            return null;
        }
    }

    private Revenue importIncome(Path sourceFile, Revenue targetRevenue, boolean silent) throws Exception {
        if (sourceFile == null || !Files.isRegularFile(sourceFile)) {
            if (!silent) {
                error("GoBD-Einnahmen-Import: Datei existiert nicht (" + sourceFile + ").");
            }
            return null;
        }
        byte[] fileBytes = Files.readAllBytes(sourceFile);
        String fileSha = GobdUtil.sha256(fileBytes);
        Object[] dupe = row("SELECT revenue_id, document_number FROM gobd_income_lock WHERE file_sha256 = '" + esc(fileSha) + "'");
        if (dupe != null) {
            if (!silent) {
                error("GoBD-Dublettenschutz:\nDie Datei '" + sourceFile.getFileName() + "' (SHA-256: " + fileSha.substring(0, 12)
                        + "…) wurde bereits als Einnahme '" + dupe[1] + "' (#" + dupe[0] + ") importiert!");
            }
            return null;
        }
        String origName = sourceFile.getFileName().toString();
        String lower = origName.toLowerCase();
        byte[] xmlBytes = null;
        String format = "RECEIPT_FILE";
        if (lower.endsWith(".pdf")) {
            xmlBytes = EInvoice.extractEmbeddedXml(sourceFile);
            if (xmlBytes != null) {
                format = "ZUGFERD_PDFA3";
            }
        } else if (lower.endsWith(".xml")) {
            xmlBytes = fileBytes;
            format = "XRECHNUNG_XML";
        }
        EInvoice.Parsed parsed = null;
        if (xmlBytes != null) {
            parsed = EInvoice.parse(xmlBytes);
            if (!parsed.valid()) {
                if (!silent) {
                    error("GoBD-Einnahmen-Import abgelehnt (EN 16931 Validierungsfehler in '" + origName + "'):\n- " + String.join("\n- ", parsed.errors));
                }
                return null;
            }
        } else if (targetRevenue == null) {
            if (!silent) {
                error("GoBD-Einnahmen-Import abgelehnt:\nDatei '" + origName + "' enthaelt kein eingebettetes ZUGFeRD / Factur-X / XRechnung XML"
                        + " (factur-x.xml / zugferd-invoice.xml / xrechnung.xml).");
            }
            return null;
        }

        String docNum = parsed != null ? parsed.documentNumber : (blank(targetRevenue.__getCnumber()) ? origName : targetRevenue.__getCnumber());
        Object[] sameNum = row("SELECT revenue_id, file_sha256 FROM gobd_income_lock WHERE document_number = '" + esc(docNum) + "'");
        if (sameNum != null && (targetRevenue == null || GobdUtil.toLong(sameNum[0], -1) != (long) targetRevenue.__getIDS())) {
            if (!silent) {
                error("GoBD-Dublettenschutz:\nEine Einnahme mit der Belegnummer '" + docNum + "' ist bereits festgeschrieben (Einnahme #" + sameNum[0] + ")!");
            }
            return null;
        }

        Revenue rev = targetRevenue != null ? targetRevenue : new Revenue();
        Date issueDate = parsed != null ? parsed.issueDate : (rev.__getDateadded() != null ? rev.__getDateadded() : new Date());
        Date dueDate = parsed != null ? (parsed.dueDate != null ? parsed.dueDate : parsed.issueDate)
                : (rev.__getDateend() != null ? rev.__getDateend() : issueDate);
        if (parsed != null) {
            String partner = str(parsed.partnerName).trim();
            rev.setCnumber(docNum);
            rev.setCname(docNum);
            rev.setDescription(partner.isEmpty() ? "E-Rechnung " + docNum + " (" + origName + ")" : partner + " - E-Rechnung " + docNum + " (" + origName + ")");
            rev.setNetvalue(parsed.netAmount);
            rev.setTaxpercentvalue(parsed.taxPercent);
            rev.setBrutvalue(parsed.grossAmount);
            rev.setDateadded(issueDate);
            rev.setDateend(dueDate);
            if (rev.__getAccountsids() <= 0) {
                rev.setAccountsids(defaultIncomeAccountId());
            }
            if (rev.__getGroupsids() <= 0) {
                rev.setGroupsids(1);
            }
            if (targetRevenue == null) {
                rev.setStatus(Item.STATUS_QUEUED);
                rev.setIspaid(false);
            }
        }
        if (!rev.save(true) || rev.__getIDS() <= 0) {
            if (!silent) {
                error("GoBD-Einnahmen-Import: Einnahme '" + docNum + "' konnte nicht gespeichert werden.");
            }
            return null;
        }
        //issue date (BT-2), due date (BT-9) and number (BT-1) exactly as in the e-invoice
        if (parsed != null) {
            rev.setCnumber(docNum);
            rev.setCname(docNum);
            rev.setDateadded(issueDate);
            rev.setDateend(dueDate);
            rev.save(true);
        }

        int year = issueDate.toInstant().atZone(ZoneId.systemDefault()).getYear();
        Path yearDir = archiveRoot().resolve("einnahmen").resolve(String.valueOf(year));
        Files.createDirectories(yearDir);
        String ext = origName.contains(".") ? origName.substring(origName.lastIndexOf('.') + 1).toLowerCase() : "pdf";
        Path archived = GobdUtil.uniquePath(yearDir, GobdUtil.safeFileName(docNum), ext);
        Files.copy(sourceFile, archived, StandardCopyOption.REPLACE_EXISTING);
        GobdUtil.makeReadOnly(archived);
        Path sums = yearDir.resolve("SHA256SUMS");
        Files.write(sums, (fileSha + "  " + archived.getFileName() + "\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        long chainSeq = nextIncomeChainSeq();
        String prevChain = latestIncomeChainHash();
        String chainHash = GobdUtil.chained(fileBytes, prevChain);
        String finSnap = revenueSnapshot(rev);
        exec("INSERT INTO gobd_income_lock (revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name,"
                + " archive_path, file_size, artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot) VALUES ("
                + rev.__getIDS() + ", " + chainSeq + ", '" + esc(docNum) + "', 'LOCKED', CURRENT_TIMESTAMP, '" + esc(currentUser()) + "', '"
                + esc(origName) + "', '" + esc(archived.toString()) + "', " + fileBytes.length + ", '" + esc(format) + "', '" + esc(fileSha) + "', '"
                + esc(chainHash) + "', '" + esc(prevChain) + "', '" + esc(finSnap) + "')");
        logAudit("REVENUE", rev.__getIDS(), "INCOME_IMPORTED", null, finSnap, fileSha,
                "Einnahme " + docNum + " (" + format + ") revisionssicher importiert und festgeschrieben.");
        yabs.refreshView(rev);
        final Revenue shown = rev;
        yabs.ui(() -> {
            try {
                mpv5.ui.panels.ModernRevenuePanel.instanceOf().refresh();
                mpv5.ui.panels.ModernRevenuePanel.instanceOf().setDataOwner(shown, true);
            } catch (Exception ignored) {
                //no income panel
            }
        });
        String amounts = "Netto: " + fmtDec(rev.__getNetvalue()) + " EUR | Brutto: " + fmtDec(rev.__getBrutvalue()) + " EUR\nSHA-256: "
                + fileSha.substring(0, 16) + "...\nArchiv: " + archived.getFileName();
        if (!silent) {
            yabs.notify(parsed == null
                    ? "GoBD-Einnahmen-Beleg archiviert:\nBeleg: " + docNum + " (" + format + ")\n" + amounts
                    + "\n(Hinweis: Einfacher PDF-Beleg - Werte koennen nicht automatisch auf rechnerische Richtigkeit geprueft werden.)"
                    : "GoBD-Einnahmen-Import erfolgreich:\nBeleg: " + docNum + " (" + format + ")\n" + amounts, rev);
        } else {
            status("GoBD Auto-Import: Einnahme " + docNum + " (" + origName + ") importiert und festgeschrieben.");
        }
        return rev;
    }

    /**
     * Starts or stops the watch folder for the automatic income import
     */
    synchronized void configureIncomeWatch() {
        if (incomeWatchTimer != null) {
            incomeWatchTimer.cancel();
            incomeWatchTimer = null;
        }
        boolean enabled = yabs.settings().getBoolean(S_AUTOINCOME, false);
        String dir = yabs.settings().get(S_WATCH, "").trim();
        if (!enabled || dir.isEmpty()) {
            return;
        }
        final Path watchDir = Paths.get(dir);
        if (!Files.isDirectory(watchDir)) {
            return;
        }
        incomeWatchTimer = new Timer("gobd-income-watch", true);
        incomeWatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!Files.isDirectory(watchDir)) {
                    return;
                }
                try (Stream<Path> s = Files.list(watchDir)) {
                    for (Path p : (Iterable<Path>) s::iterator) {
                        String fn = p.getFileName().toString().toLowerCase();
                        if (Files.isRegularFile(p) && (fn.endsWith(".pdf") || fn.endsWith(".xml"))
                                && seenWatchFiles.add(p.toAbsolutePath() + ":" + Files.size(p))) {
                            importIncomeFile(p, null, true);
                        }
                    }
                } catch (Exception e) {
                    Log.Debug(Gobd.class, e);
                }
            }
        }, 5000L, 15000L);
    }

    // ---------------------------------------------------------------- the guard
    /**
     * @param obj
     * @param isDelete
     * @return false if the change of a finalized record is not allowed (the
     * user is told why)
     */
    boolean checkWriteAllowed(DatabaseObject obj, boolean isDelete) {
        if (obj instanceof Item && isTaxRelevant(obj) && obj.__getIDS() > 0) {
            Item doc = (Item) obj;
            Object[] lock = lockRecord(doc.__getIDS());
            if (lock != null) {
                return checkInvoice(doc, lock, isDelete);
            }
        }
        if (obj instanceof SubItem) {
            SubItem sub = (SubItem) obj;
            if (sub.__getItemsids() > 0) {
                Object[] lock = lockRecord(sub.__getItemsids());
                if (lock != null) {
                    logAudit("SUBITEM", sub.__getIDS(), isDelete ? "BLOCKED_DELETE" : "BLOCKED_EDIT", str(lock[6]), sub.__getDescription(), str(lock[9]),
                            "Positionsaenderung an festgeschriebenem Beleg " + lock[1] + " verhindert.");
                    error("GoBD-Schutz: Position gehoert zu festgeschriebenem Beleg " + lock[1] + " und ist gesperrt.");
                    return false;
                }
            }
        }
        if (obj instanceof Revenue && obj.__getIDS() > 0) {
            return checkRevenue((Revenue) obj, isDelete);
        }
        if (obj instanceof Expense && obj.__getIDS() > 0) {
            return checkExpense((Expense) obj, isDelete);
        }
        if (obj instanceof Contact && obj.__getIDS() > 0 && !isDelete) {
            masterDataBeforeSave.put("CONTACT_" + obj.__getIDS(), contactSnapshot((long) obj.__getIDS()));
        }
        return true;
    }

    private boolean checkInvoice(Item doc, Object[] lock, boolean isDelete) {
        String hash = str(lock[9]);
        if (isDelete || isMovedToTrash(doc)) {
            logAudit("INVOICE", doc.__getIDS(), "BLOCKED_DELETE", str(lock[5]), "Delete/Trash=true", hash,
                    "Loesch- oder Papierkorb-Versuch an festgeschriebenem Beleg " + lock[1] + ".");
            error("GoBD-Loeschschutz (§§ 146, 147 AO):\nBeleg " + lock[1] + " ist festgeschrieben und darf weder geloescht noch in den Papierkorb verschoben werden!\n"
                    + "Bitte erstellen Sie stattdessen eine Stornorechnung.");
            return false;
        }
        Object[] db = row("SELECT cnumber, inttype, contactsids, accountsids, dateadded, datetodo, dateend, netvalue, discountvalue, taxvalue,"
                + " discountgrosvalue, description, intstatus, intreminders FROM items WHERE ids = " + doc.__getIDS());
        if (db != null) {
            String dbCore = String.join("|", "Num=" + str(db[0]), "Type=" + db[1], "Contact=" + db[2], "Acc=" + db[3],
                    "IssueDate=" + fmtDate(db[4]), "DeliveryDate=" + fmtDate(db[5]), "EndDate=" + fmtDate(db[6]),
                    "Net=" + fmtDec(db[7]), "Disc=" + fmtDec(db[8]), "Tax=" + fmtDec(db[9]), "DiscGross=" + fmtDec(db[10]),
                    "Desc=" + str(db[11]).trim());
            if (dbCore.equals(financialCore(doc))) {
                int oldStatus = GobdUtil.toInt(db[12], 0);
                int newStatus = doc.__getIntstatus();
                int oldReminders = GobdUtil.toInt(db[13], 0);
                int newReminders = doc.__getIntreminders();
                if (newStatus == Item.STATUS_CANCELLED && oldStatus != Item.STATUS_CANCELLED && !creatingStorno) {
                    logAudit("INVOICE", doc.__getIDS(), "BLOCKED_MANUAL_CANCEL", "intstatus=" + oldStatus, "intstatus=" + newStatus, hash,
                            "Manuelles Stornieren ohne Stornorechnung verboten.");
                    error("GoBD-Schutz: Ein festgeschriebener Beleg darf nicht einfach auf 'Storniert' gesetzt werden.\n"
                            + "Bitte nutzen Sie 'Plugins > GoBD: 2. Beleg stornieren (Stornorechnung)'.");
                    return false;
                }
                if (oldStatus != newStatus) {
                    String o = Item.getStatusString(oldStatus);
                    String n = Item.getStatusString(newStatus);
                    logAudit("INVOICE", doc.__getIDS(), "PAYMENT_STATUS_CHANGED", "status=" + oldStatus + "(" + o + ")", "status=" + newStatus + "(" + n + ")",
                            hash, "Zahlungsstatus fuer Beleg " + lock[1] + " protokolliert.");
                    status("GoBD-Audit: Status fuer Beleg " + lock[1] + " protokolliert (" + o + " -> " + n + ").");
                }
                if (oldReminders != newReminders) {
                    logAudit("INVOICE", doc.__getIDS(), "DUNNING_CHANGED", "intreminders=" + oldReminders, "intreminders=" + newReminders, hash,
                            "Mahnstufe fuer Beleg " + lock[1] + " protokolliert.");
                    status("GoBD-Audit: Mahnstufe fuer Beleg " + lock[1] + " protokolliert (" + oldReminders + " -> " + newReminders + ").");
                }
                return true;
            }
        }
        logAudit("INVOICE", doc.__getIDS(), "BLOCKED_EDIT", str(lock[5]), financialCore(doc), hash,
                "Unzulaessige Aenderung steuerrelevanter Felder an Beleg " + lock[1] + ".");
        error("GoBD-Schutz (§ 146 Abs. 4 AO):\nBeleg " + lock[1] + " ist festgeschrieben! Betraege, Steuern, Datum, Positionen und Empfaenger duerfen nicht mehr geaendert werden.\n"
                + "(Hinweis: Nur Zahlungsstatus-/Mahnstufen-Aenderungen werden protokolliert.)");
        return false;
    }

    private boolean checkRevenue(Revenue rev, boolean isDelete) {
        Object[] incLock = incomeLockRecord(rev.__getIDS());
        Object[][] receipts = rows("SELECT ids, originalname, archivepath, sha256 FROM revenue_receipts WHERE revenuesids = " + rev.__getIDS());
        if (incLock == null && receipts.length == 0) {
            return true;
        }
        String lockHash = incLock != null ? str(incLock[10]) : str(receipts[0][3]);
        String docNum = incLock != null ? str(incLock[2]) : (blank(rev.__getCnumber()) ? "#" + rev.__getIDS() : rev.__getCnumber());
        String lockInfo = incLock != null ? str(incLock[13]) : "Receipts=" + receipts.length;
        if (isDelete || isMovedToTrash(rev)) {
            logAudit("REVENUE", rev.__getIDS(), "BLOCKED_DELETE", lockInfo, "Delete/Trash=true", lockHash,
                    "Loesch- oder Papierkorb-Versuch an GoBD-gesperrter Einnahme " + docNum + ".");
            error("GoBD-Loeschschutz (§§ 146, 147 AO):\nEinnahme " + docNum + " ist durch einen archivierten Originalbeleg festgeschrieben"
                    + " und darf weder geloescht noch in den Papierkorb verschoben werden!");
            return false;
        }
        Object[] db = row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description, status, dateend, ispaid"
                + " FROM revenues WHERE ids = " + rev.__getIDS());
        if (db != null && moneySnapshotOfRow(db).equals(revenueSnapshot(rev))) {
            int oldStatus = GobdUtil.toInt(db[7], 0);
            int newStatus = rev.__getStatus();
            String oldEnd = fmtDate(db[8]);
            String newEnd = fmtDate(rev.__getDateend());
            boolean oldPaid = GobdUtil.isTrue(db[9]);
            boolean newPaid = rev.__getIspaid();
            if (oldStatus != newStatus || !oldEnd.equals(newEnd) || oldPaid != newPaid) {
                logAudit("REVENUE", rev.__getIDS(), "INCOME_PAYMENT_STATUS_CHANGED", "status=" + oldStatus + "|ispaid=" + oldPaid + "|dateend=" + oldEnd,
                        "status=" + newStatus + "|ispaid=" + newPaid + "|dateend=" + newEnd, lockHash,
                        "Zahlungsstatus/Zahlungsdatum fuer Einnahme " + docNum + " protokolliert.");
                status("GoBD-Audit: Zahlungsstatus fuer Einnahme " + docNum + " protokolliert.");
            }
            return true;
        }
        logAudit("REVENUE", rev.__getIDS(), "BLOCKED_EDIT", lockInfo, revenueSnapshot(rev), lockHash,
                "Unzulaessige Aenderung steuerrelevanter Felder an Einnahme " + docNum + ".");
        error("GoBD-Schutz (§ 146 Abs. 4 AO):\nEinnahme " + docNum + " ist festgeschrieben! Betraege, Steuersatz, Leistungsdatum, Konto, Nummer und Beschreibung"
                + " duerfen nicht mehr geaendert werden.\n(Hinweis: Nur Zahlungsstatus und Zahlungsdatum bleiben aenderbar.)");
        return false;
    }

    private boolean checkExpense(Expense exp, boolean isDelete) {
        Object[][] receipts = rows("SELECT ids, originalname, archivepath, sha256 FROM expense_receipts WHERE expensesids = " + exp.__getIDS());
        if (receipts.length == 0) {
            return true;
        }
        String hash = str(receipts[0][3]);
        String name = blank(exp.__getCnumber()) ? "#" + exp.__getIDS() : exp.__getCnumber();
        if (isDelete || isMovedToTrash(exp)) {
            logAudit("EXPENSE", exp.__getIDS(), "BLOCKED_DELETE", "Receipts=" + receipts.length, "Delete/Trash=true", hash,
                    "Loeschung einer Ausgabe mit archivierten Belegen verhindert.");
            error("GoBD-Schutz (Rz. 61-64 AO):\nAusgabe #" + exp.__getIDS() + " besitzt " + receipts.length + " archivierte Eingangsbelege und darf nicht geloescht werden!");
            return false;
        }
        Object[] db = row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description, ispaid, dateend FROM expenses WHERE ids = " + exp.__getIDS());
        if (db == null) {
            return true;
        }
        String dbCore = moneySnapshotOfRow(db);
        String newCore = expenseSnapshot(exp);
        if (dbCore.equals(newCore)) {
            boolean oldPaid = GobdUtil.isTrue(db[7]);
            boolean newPaid = exp.__getIspaid();
            String oldEnd = fmtDate(db[8]);
            String newEnd = fmtDate(exp.__getDateend());
            if (oldPaid != newPaid || !oldEnd.equals(newEnd)) {
                logAudit("EXPENSE", exp.__getIDS(), "EXPENSE_PAYMENT_STATUS_CHANGED", "ispaid=" + oldPaid + "|dateend=" + oldEnd,
                        "ispaid=" + newPaid + "|dateend=" + newEnd, hash, "Zahlungsstatus/Zahlungsdatum fuer Ausgabe " + name + " protokolliert.");
                status("GoBD-Audit: Zahlungsstatus fuer Ausgabe " + name + " protokolliert.");
            }
            return true;
        }
        logAudit("EXPENSE", exp.__getIDS(), "BLOCKED_EDIT", dbCore, newCore, hash, "Unzulaessige Aenderung steuerrelevanter Felder an Ausgabe " + name + ".");
        error("GoBD-Schutz (§ 146 Abs. 4 AO):\nAusgabe " + name + " ist mit " + receipts.length + " archivierten Originalbeleg(en) verknuepft und festgeschrieben!\n"
                + "Betraege, Steuersatz, Belegdatum, Konto, Nummer und Beschreibung duerfen nicht mehr geaendert werden.\n"
                + "(Hinweis: Nur Zahlungsstatus und Zahlungsdatum bleiben aenderbar.)");
        return false;
    }

    /**
     * @return the record modifier: blocks changes and deletion of finalized
     * records, logs master data changes and new drafts
     */
    DatabaseObjectModifier modifier() {
        return new DatabaseObjectModifier() {
            @Override
            public DatabaseObject modifyOnExplode(DatabaseObject object) {
                return object;
            }

            @Override
            public DatabaseObject modifyOnSave(DatabaseObject obj) throws ChangeNotApprovedException {
                if (!checkWriteAllowed(obj, false)) {
                    throw veto(obj);
                }
                if (obj instanceof Contact && obj.__getIDS() > 0) {
                    Contact c = (Contact) obj;
                    String oldSnap = masterDataBeforeSave.remove("CONTACT_" + c.__getIDS());
                    String newSnap = contactSnapshot(c);
                    if (oldSnap != null && !oldSnap.equals(newSnap)) {
                        logAudit("CONTACT", c.__getIDS(), "MASTERDATA_CHANGED", oldSnap, newSnap, null, "Kundenstammdaten geaendert (" + c.__getCname() + ")");
                        status("GoBD: Stammdaten-Aenderung protokolliert (" + c.__getCname() + ").");
                    }
                }
                return obj;
            }

            @Override
            public DatabaseObject executeAfterCreate(DatabaseObject obj) {
                if (obj instanceof Item && isTaxRelevant(obj) && obj.__getIDS() > 0) {
                    Item doc = (Item) obj;
                    logAudit("INVOICE", doc.__getIDS(), "DRAFT_CREATED", null, "Cnumber=" + doc.__getCnumber(), null, "Entwurf angelegt: " + doc.__getCnumber());
                }
                return obj;
            }

            @Override
            public DatabaseObject executeAfterSave(DatabaseObject object) {
                return object;
            }

            @Override
            public Map<String, Object> modifyOnResolve(Map<String, Object> map) {
                return map;
            }

            @Override
            public DatabaseObject modifyOnDelete(DatabaseObject obj) throws ChangeNotApprovedException {
                if (!checkWriteAllowed(obj, true)) {
                    throw veto(obj);
                }
                return obj;
            }
        };
    }

    // ---------------------------------------------------------------- activity
    /**
     * A PDF / e-invoice of an invoice was created: remembered for the
     * archive, and the invoice is finalized (setting autoLockOnExport)
     *
     * @param entry
     */
    void onActivity(Activity.Entry entry) {
        if (entry.file == null || (entry.kind != null && entry.kind.name().equals("DELETED"))) {
            return;
        }
        String fileName = entry.file.getName().toLowerCase();
        if (!(fileName.endsWith(".pdf") || fileName.endsWith(".xml"))) {
            return;
        }
        String ctx = entry.context == null ? "" : String.valueOf(entry.context.getDbIdentity()).toLowerCase();
        if (ctx.contains("order") || ctx.contains("offer") || ctx.contains("confirmation") || ctx.contains("delivery")) {
            return;
        }
        Set<String> invoiceCtxs = new HashSet<String>();
        for (Context c : new Context[]{Context.getInvoice(), Context.getCredit(), Context.getDeposit(), Context.getPartPayment()}) {
            if (c != null) {
                invoiceCtxs.add(String.valueOf(c.getDbIdentity()).toLowerCase());
            }
        }
        Item doc = null;
        if (entry.id > 0 && invoiceCtxs.contains(ctx)) {
            try {
                DatabaseObject dbo = DatabaseObject.getObject(entry.context != null ? entry.context : Context.getInvoice(), (int) entry.id);
                if (dbo instanceof Item && isGobdItemType(((Item) dbo).__getInttype())) {
                    doc = (Item) dbo;
                }
            } catch (Exception ignored) {
                //gone
            }
        } else if (entry.id <= 0) {
            mpv5.ui.panels.DataPanel tab = yabs.currentView();
            if (tab != null && tab.getDataOwner() instanceof Item) {
                Item it = (Item) tab.getDataOwner();
                if (it.__getIDS() > 0 && isGobdItemType(it.__getInttype())) {
                    doc = it;
                }
            }
        }
        if (doc == null || doc.__getIDS() <= 0) {
            return;
        }
        lastGeneratedFiles.put((long) doc.__getIDS(), entry.file.toPath());
        if (creatingStorno || finalizingIds.contains(doc.__getIDS())) {
            return;
        }
        if (lockRecord(doc.__getIDS()) == null) {
            if (yabs.settings().getBoolean(S_AUTOLOCK, true)) {
                finalizeDocument(doc, null);
            } else {
                status("Belegdatei erstellt (" + entry.file.getName() + "). Jetzt ueber 'Plugins > GoBD: 1. Beleg festschreiben' sichern.");
            }
        }
    }

    // ---------------------------------------------------------------- files
    /**
     * Opens an archived file, or says where it is / that it is missing
     *
     * @param archivePath
     */
    void openArchived(String archivePath) {
        if (blank(archivePath)) {
            error("GoBD-Pruefung (Rz. 61): Zu diesem Datensatz ist kein archivierter Originalbeleg hinterlegt!");
            return;
        }
        File f = new File(archivePath.trim());
        if (!f.exists()) {
            error("Archivdatei nicht gefunden:\n" + archivePath);
            return;
        }
        yabs.openFile(f);
    }

    /**
     * Stops the watch folder and forgets the state
     */
    void stop() {
        try {
            logAudit("PLUGIN", 0L, "PLUGIN_STOPPED", "STATE=RUNNING", "STATE=STOPPED", null, "GoBD-Konformitaets-Plugin wurde gestoppt oder deaktiviert.");
        } catch (Exception ignored) {
            //no database
        }
        synchronized (this) {
            if (incomeWatchTimer != null) {
                incomeWatchTimer.cancel();
                incomeWatchTimer = null;
            }
        }
        seenWatchFiles.clear();
        creatingStorno = false;
        finalizingIds.clear();
        lastGeneratedFiles.clear();
        masterDataBeforeSave.clear();
    }
}
