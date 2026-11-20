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
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
    volatile boolean creatingStorno = false;
    /**
     * The inventory seal (records that existed before the plugin or were
     * changed without it)
     */
    final Inventory inventory;

    Gobd(ExtensionContext yabs) {
        this.yabs = yabs;
        this.inventory = new Inventory(this);
    }

    /**
     * A database error of the GoBD tables (never swallowed: a failed write is
     * no lock, a failed read is not "not locked")
     */
    static final class GobdException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        GobdException(String message, Throwable cause) {
            super(message, cause);
        }

        GobdException(String message) {
            super(message);
        }
    }

    // ---------------------------------------------------------------- basics
    /**
     * Lenient (for views and tables of YaBS): an error gives no rows
     */
    Object[][] rows(String sql) {
        return yabs.select(sql);
    }

    /**
     * Strict, with parameters
     *
     * @throws GobdException on a database error
     */
    Object[][] q(String sql, Object... params) {
        try {
            return Db.query(Db.connection(), sql, params);
        } catch (SQLException e) {
            throw new GobdException("GoBD-Datenbankfehler: " + e.getMessage(), e);
        }
    }

    /**
     * Strict, with parameters
     *
     * @return the first row, null if none
     * @throws GobdException on a database error
     */
    Object[] q1(String sql, Object... params) {
        Object[][] r = q(sql, params);
        return r.length > 0 ? r[0] : null;
    }

    /**
     * Strict, with parameters
     *
     * @throws GobdException on a database error
     */
    void u(String sql, Object... params) {
        try {
            Db.update(Db.connection(), sql, params);
        } catch (SQLException e) {
            throw new GobdException("GoBD-Datenbankfehler: " + e.getMessage(), e);
        }
    }

    Object[] row(String sql) {
        Object[][] r = rows(sql);
        return r.length > 0 ? r[0] : null;
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
     * A success message without a dialog: the notification bar of YaBS, and
     * the notification log of the record if there is one
     *
     * @param msg
     * @param about the record, or null
     */
    void notifyQuiet(String msg, DatabaseObject about) {
        String m = msg.replace("\n", " | ");
        if (about != null && about.__getIDS() > 0) {
            mpv5.ui.dialogs.Notificator.raiseNotification(m, false, true, about);
        } else {
            mpv5.ui.dialogs.Notificator.raiseNotification(m, false);
        }
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
        } catch (Exception | LinkageError e) {
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
        u("CREATE TABLE IF NOT EXISTS gobd_document_lock ("
                + " document_id BIGINT PRIMARY KEY, chain_seq BIGINT NULL, document_number VARCHAR(100) NOT NULL UNIQUE,"
                + " status VARCHAR(20) NOT NULL, finalized_at TIMESTAMP NOT NULL, finalized_by VARCHAR(100) NOT NULL,"
                + " customer_snapshot TEXT NOT NULL, positions_snapshot TEXT NOT NULL, archive_path VARCHAR(512) NOT NULL,"
                + " artifact_format VARCHAR(30) NOT NULL, sha256_hash CHAR(64) NOT NULL, prev_sha256_hash CHAR(64) NOT NULL,"
                + " cancels_document_id BIGINT NULL)");
        u("CREATE TABLE IF NOT EXISTS gobd_audit_log ("
                + " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " user_id VARCHAR(100) NOT NULL, entity_type VARCHAR(50) NOT NULL, entity_id BIGINT NOT NULL,"
                + " action VARCHAR(50) NOT NULL, old_value VARCHAR(2000) NULL, new_value VARCHAR(2000) NULL,"
                + " file_hash CHAR(64) NULL, details VARCHAR(1000) NULL)");
        for (String alter : new String[]{
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS chain_seq BIGINT NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS customer_snapshot TEXT NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS positions_snapshot TEXT NULL",
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS old_value VARCHAR(2000) NULL",
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS new_value VARCHAR(2000) NULL",
            //hash chain of the audit log
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS prev_hash CHAR(64) NULL",
            "ALTER TABLE gobd_audit_log ADD COLUMN IF NOT EXISTS row_hash CHAR(64) NULL",
            //version 2 of the hash chain: the record (booking data + file hash) is hashed, not only the file
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS hash_version INT NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS file_sha256 CHAR(64) NULL",
            "ALTER TABLE gobd_document_lock ADD COLUMN IF NOT EXISTS record_data TEXT NULL"}) {
            u(alter);
        }
        //receipts of expenses: the files are not in the database but byte-identical in
        //<archive>/eingangsbelege/<year>/ with a SHA256SUMS file; the table holds metadata and a hash chain
        u("CREATE TABLE IF NOT EXISTS gobd_receipt ("
                + " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, expense_id BIGINT NOT NULL, chain_seq BIGINT NOT NULL,"
                + " receipt_ref VARCHAR(250) NULL, original_name VARCHAR(512) NOT NULL, archive_path VARCHAR(1024) NOT NULL,"
                + " file_size BIGINT NOT NULL, artifact_format VARCHAR(30) NOT NULL, sha256 CHAR(64) NOT NULL,"
                + " chain_hash CHAR(64) NOT NULL, prev_chain_hash CHAR(64) NOT NULL, expense_snapshot VARCHAR(2000) NULL,"
                + " imported_at TIMESTAMP NOT NULL, imported_by VARCHAR(100) NOT NULL)");
        //receipts of income (table revenue_receipts of YaBS): the same, a hash chain of its own
        u("CREATE TABLE IF NOT EXISTS gobd_revenue_receipt ("
                + " id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, revenue_id BIGINT NOT NULL, chain_seq BIGINT NOT NULL,"
                + " receipt_ref VARCHAR(250) NULL, original_name VARCHAR(512) NOT NULL, archive_path VARCHAR(1024) NOT NULL,"
                + " file_size BIGINT NOT NULL, artifact_format VARCHAR(30) NOT NULL, sha256 CHAR(64) NOT NULL,"
                + " chain_hash CHAR(64) NOT NULL, prev_chain_hash CHAR(64) NOT NULL, revenue_snapshot VARCHAR(2000) NULL,"
                + " imported_at TIMESTAMP NOT NULL, imported_by VARCHAR(100) NOT NULL, record_data TEXT NULL)");
        //income from e-invoices: the original under <archive>/einnahmen/<year>/ with SHA256SUMS and hash chain
        u("CREATE TABLE IF NOT EXISTS gobd_income_lock ("
                + " revenue_id BIGINT PRIMARY KEY, chain_seq BIGINT NOT NULL, document_number VARCHAR(100) NOT NULL,"
                + " status VARCHAR(20) NOT NULL, imported_at TIMESTAMP NOT NULL, imported_by VARCHAR(100) NOT NULL,"
                + " original_name VARCHAR(512) NOT NULL, archive_path VARCHAR(1024) NOT NULL, file_size BIGINT NOT NULL,"
                + " artifact_format VARCHAR(30) NOT NULL, file_sha256 CHAR(64) NOT NULL UNIQUE, chain_hash CHAR(64) NOT NULL,"
                + " prev_chain_hash CHAR(64) NOT NULL, financial_snapshot VARCHAR(2000) NOT NULL)");
        for (String alter : new String[]{
            "ALTER TABLE gobd_income_lock ADD COLUMN IF NOT EXISTS hash_version INT NULL",
            "ALTER TABLE gobd_income_lock ADD COLUMN IF NOT EXISTS record_data TEXT NULL",
            "ALTER TABLE gobd_receipt ADD COLUMN IF NOT EXISTS record_data TEXT NULL"}) {
            u(alter);
        }
        migrateChainSeq();
        inventory.initSchema();
        //one successor per link: two clients can not fork a chain
        for (String index : new String[]{
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_document_lock_seq ON gobd_document_lock (chain_seq)",
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_income_lock_seq ON gobd_income_lock (chain_seq)",
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_receipt_seq ON gobd_receipt (chain_seq)",
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_receipt_ref ON gobd_receipt (receipt_ref)",
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_revenue_receipt_seq ON gobd_revenue_receipt (chain_seq)",
            "CREATE UNIQUE INDEX IF NOT EXISTS gobd_revenue_receipt_ref ON gobd_revenue_receipt (receipt_ref)",
            //older entries have no prev_hash: NULLS DISTINCT (MODE=Derby counts NULLs as equal)
            "ALTER TABLE gobd_audit_log ADD CONSTRAINT IF NOT EXISTS gobd_audit_log_prev_u UNIQUE NULLS DISTINCT (prev_hash)"}) {
            try {
                u(index);
            } catch (GobdException e) {
                //old duplicates: the check of the archive reports them
                Log.Debug(Gobd.class, e);
            }
        }
    }

    /**
     * Old locks have no chain_seq (or the same one twice). Sorted by COALESCE(chain_seq, document_id)
     * old and new ones were mixed up, and new locks were chained to the wrong
     * predecessor. Gives all locks a chain_seq in the order of their links
     * (prev_sha256_hash), the rest (breaks) in the old order. The hashes stay
     * as they are: a break is shown by the check of the archive.
     */
    void migrateChainSeq() {
        Object[][] all = q("SELECT document_id, sha256_hash, prev_sha256_hash, chain_seq FROM gobd_document_lock"
                + " ORDER BY COALESCE(chain_seq, document_id) ASC, finalized_at ASC, document_id ASC");
        boolean missing = false;
        Set<String> seqs = new HashSet<String>();
        for (Object[] r : all) {
            //none, or twice (two clients at the same time)
            missing |= r[3] == null || !seqs.add(str(r[3]));
        }
        if (!missing) {
            return;
        }
        List<Object[]> rest = new ArrayList<Object[]>(Arrays.asList(all));
        List<Object[]> ordered = new ArrayList<Object[]>();
        String current = GobdUtil.GENESIS_HASH;
        while (!rest.isEmpty()) {
            Object[] next = rest.get(0);
            for (Object[] r : rest) {
                if (str(r[2]).equalsIgnoreCase(current)) {
                    next = r;
                    break;
                }
            }
            rest.remove(next);
            ordered.add(next);
            current = str(next[1]);
        }
        try {
            Db.tx(c -> {
                //first out of the way (no clash with the unique index of an earlier run;
                //not NULL: MODE=Derby does not allow two NULLs in a unique index)
                Db.update(c, "UPDATE gobd_document_lock SET chain_seq = -document_id");
                for (int i = 0; i < ordered.size(); i++) {
                    Db.update(c, "UPDATE gobd_document_lock SET chain_seq = ? WHERE document_id = ?", (long) (i + 1), ordered.get(i)[0]);
                }
                logAudit(c, "PLUGIN", 0L, "CHAIN_SEQ_MIGRATED", null, "LOCKS=" + ordered.size(), null,
                        "Festschreibungen ohne chain_seq in der Reihenfolge der Hash-Verkettung nummeriert.");
                return null;
            });
        } catch (Exception e) {
            throw new GobdException("GoBD: Nummerierung der Hash-Kette fehlgeschlagen: " + e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------- audit
    /**
     * Writes an entry of the audit log (hash-chained, in its own transaction
     * or in the running one)
     *
     * @throws GobdException if it could not be written
     */
    void logAudit(String entityType, long entityId, String action, String oldVal, String newVal, String fileHash, String details) {
        try {
            Db.tx(c -> {
                logAudit(c, entityType, entityId, action, oldVal, newVal, fileHash, details);
                return null;
            });
        } catch (GobdException e) {
            throw e;
        } catch (Exception e) {
            throw new GobdException("GoBD-Audit-Log konnte nicht geschrieben werden: " + e.getMessage(), e);
        }
    }

    /**
     * Writes an entry of the audit log on the connection: row_hash =
     * SHA256(record || prev_hash), prev_hash = row_hash of the entry before
     */
    static void logAudit(Connection c, String entityType, long entityId, String action, String oldVal, String newVal, String fileHash,
            String details) throws SQLException {
        String user = trunc(currentUser(), 100);
        String type = trunc(entityType, 50);
        String act = trunc(action, 50);
        String o = trunc(oldVal, 2000);
        String n = trunc(newVal, 2000);
        String d = trunc(details == null ? "" : details, 1000);
        //CHAR(64): no blanks, no other lengths (they would come back padded)
        String fh = blank(fileHash) || fileHash.trim().length() != 64 ? null : fileHash.trim();
        for (int attempt = 0;; attempt++) {
            Object[] last = Db.first(c, "SELECT row_hash FROM gobd_audit_log WHERE row_hash IS NOT NULL ORDER BY id DESC FETCH FIRST 1 ROWS ONLY");
            String prev = last == null ? GobdUtil.GENESIS_HASH : str(last[0]);
            Timestamp now = nowSeconds();
            String hash = GobdUtil.recordHash(auditRecord(now, user, type, entityId, act, o, n, fh, d), prev);
            try {
                Db.update(c, "INSERT INTO gobd_audit_log (created_at, user_id, entity_type, entity_id, action, old_value, new_value, file_hash, details,"
                        + " prev_hash, row_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", now, user, type, entityId, act, o, n, fh, d, prev, hash);
                return;
            } catch (SQLException e) {
                //another client wrote an entry in between: chain to that one
                if (attempt >= 4 || !Db.isUniqueViolation(e)) {
                    throw e;
                }
            }
        }
    }

    /**
     * @return the record of an audit log entry that is hashed
     */
    static String auditRecord(Object createdAt, String user, String type, Object entityId, String action, String oldVal, String newVal,
            String fileHash, String details) {
        return GobdUtil.record("created_at", GobdUtil.fmtTimestamp(createdAt), "user", user, "entity_type", type, "entity_id", entityId,
                "action", action, "old", oldVal == null ? "<null>" : oldVal, "new", newVal == null ? "<null>" : newVal,
                "file_hash", fileHash == null ? "<null>" : fileHash.trim(), "details", details);
    }

    /**
     * @return now, without the parts of a second (the same after reading it
     * back from the database)
     */
    static Timestamp nowSeconds() {
        return new Timestamp(System.currentTimeMillis() / 1000L * 1000L);
    }

    // ---------------------------------------------------------------- locks
    /**
     * @return document_id, document_number, status, finalized_at, finalized_by,
     * customer_snapshot, positions_snapshot, archive_path, artifact_format,
     * sha256_hash, prev_sha256_hash, cancels_document_id; null if not finalized
     */
    Object[] lockRecord(long docId) {
        return q1("SELECT document_id, document_number, status, finalized_at, finalized_by, customer_snapshot, positions_snapshot,"
                + " archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id, record_data FROM gobd_document_lock WHERE document_id = ?", docId);
    }

    /**
     * @return revenue_id, chain_seq, document_number, status, imported_at,
     * imported_by, original_name, archive_path, file_size, artifact_format,
     * file_sha256, chain_hash, prev_chain_hash, financial_snapshot; null if none
     */
    Object[] incomeLockRecord(long revId) {
        return q1("SELECT revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name, archive_path, file_size,"
                + " artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot FROM gobd_income_lock WHERE revenue_id = ?", revId);
    }

    /**
     * The end of a chain, read in the transaction that adds the next link
     *
     * @param c
     * @param table
     * @param hashColumn
     * @return {next chain_seq, hash of the last link (GENESIS if none)}
     */
    static Object[] chainEnd(Connection c, String table, String hashColumn) throws SQLException {
        Object[] last = Db.first(c, "SELECT chain_seq, " + hashColumn + " FROM " + table + " WHERE chain_seq IS NOT NULL ORDER BY chain_seq DESC FETCH FIRST 1 ROWS ONLY");
        if (last == null || blank(str(last[1]))) {
            return new Object[]{1L, GobdUtil.GENESIS_HASH};
        }
        return new Object[]{GobdUtil.toLong(last[0], 0L) + 1L, str(last[1])};
    }

    /**
     * Adds a link to a chain: runs the insert in a transaction, again if
     * another client added a link in between (unique chain_seq)
     */
    static <T> T addLink(Db.Work<T> work) throws Exception {
        for (int attempt = 0;; attempt++) {
            try {
                return Db.tx(work);
            } catch (Exception e) {
                if (attempt >= 4 || !Db.isUniqueViolation(e)) {
                    throw e;
                }
            }
        }
    }

    /**
     * The final number of a draft: the number format of its type (number
     * formats in the settings of YaBS), else the own range RE-/ST-YYYY-NNNNN
     *
     * @param doc
     * @param year
     * @return the number
     */
    String nextNumber(Item doc, int year) {
        if (mpv5.handler.FormatHandler.hasFormat(doc, doc.__getInttype())) {
            return doc.getFormatHandler().next();
        }
        return nextSequenceNumber(doc.__getInttype() == Item.TYPE_CREDIT ? "ST" : "RE", year);
    }

    /**
     * The number of a cancellation invoice: the number format "Cancellation
     * invoice" ({@link mpv5.globals.Constants#TYPE_CANCELLATION}) if one is
     * set, going on from the last cancellation invoice; else ST-YYYY-NNNNN.
     * Any format is allowed (§ 14 Abs. 4 Nr. 4 UStG: unique, one or more
     * ranges).
     *
     * @param storno the credit note (with its date)
     * @return the number
     */
    String nextCancellationNumber(Item storno) {
        int type = mpv5.globals.Constants.TYPE_CANCELLATION;
        if (mpv5.handler.FormatHandler.hasFormat(storno, type)) {
            return new mpv5.handler.FormatHandler(storno, type, this::lastCancellationNumber).next();
        }
        return nextSequenceNumber("ST", GobdUtil.yearOf(storno.__getDateadded()));
    }

    /**
     * @return the number of the last cancellation invoice, null if none
     */
    String lastCancellationNumber() {
        Object[] r = q1("SELECT document_number FROM gobd_document_lock WHERE cancels_document_id IS NOT NULL"
                + " ORDER BY chain_seq DESC, finalized_at DESC FETCH FIRST 1 ROWS ONLY");
        return r == null ? null : str(r[0]);
    }

    String nextSequenceNumber(String prefix, int year) {
        String pattern = prefix + "-" + year + "-";
        int max = 0;
        for (String sql : new String[]{
            "SELECT document_number FROM gobd_document_lock WHERE document_number LIKE ? ESCAPE '!'",
            "SELECT cnumber FROM items WHERE cnumber LIKE ? ESCAPE '!'"}) {
            for (Object[] r : q(sql, GobdUtil.likeEscape(pattern) + "%")) {
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

    /**
     * The tax relevant fields of an invoice (6 decimals: also a change below a
     * cent is a change)
     */
    static String financialCore(Item doc) {
        return itemCore(doc.__getCnumber(), doc.__getInttype(), doc.__getContactsids(), doc.__getAccountsids(), doc.__getDateadded(),
                doc.__getDatetodo(), doc.__getDateend(), doc.__getNetvalue(), doc.__getDiscountvalue(), doc.__getTaxvalue(),
                doc.__getDiscountGrosvalue(), doc.__getDescription());
    }

    /**
     * The columns of the items table read by {@link #itemCoreOfRow}
     */
    static final String ITEM_CORE_COLUMNS = "cnumber, inttype, contactsids, accountsids, dateadded, datetodo, dateend, netvalue, discountvalue,"
            + " taxvalue, discountgrosvalue, description";

    /**
     * {@link #financialCore} of a row with {@link #ITEM_CORE_COLUMNS}
     */
    static String itemCoreOfRow(Object[] db) {
        return itemCore(db[0], db[1], db[2], db[3], db[4], db[5], db[6], db[7], db[8], db[9], db[10], db[11]);
    }

    private static String itemCore(Object num, Object type, Object contact, Object acc, Object issue, Object delivery, Object end,
            Object net, Object disc, Object tax, Object discGross, Object desc) {
        return String.join("|",
                "Num=" + str(num),
                "Type=" + str(type),
                "Contact=" + str(contact),
                "Acc=" + str(acc),
                "IssueDate=" + fmtDate(issue),
                "DeliveryDate=" + fmtDate(delivery),
                "EndDate=" + fmtDate(end),
                "Net=" + GobdUtil.fmtExact(net),
                "Disc=" + GobdUtil.fmtExact(disc),
                "Tax=" + GobdUtil.fmtExact(tax),
                "DiscGross=" + GobdUtil.fmtExact(discGross),
                "Desc=" + str(desc).trim());
    }

    /**
     * @return the tax relevant fields of the invoice in the database, null if
     * it could not be read
     */
    String itemCoreInDb(long docId) {
        Object[] db = row("SELECT " + ITEM_CORE_COLUMNS + " FROM items WHERE ids = " + docId);
        return db == null ? null : itemCoreOfRow(db);
    }

    /**
     * @return the positions of the invoice in the database (description,
     * quantity, price, discount, tax rate; 6 decimals)
     */
    String positionsCoreInDb(long docId) {
        StringBuilder b = new StringBuilder();
        for (Object[] p : rows("SELECT description, quantityvalue, externalvalue, discount, taxpercentvalue FROM subitems WHERE itemsids = "
                + docId + " ORDER BY ids ASC")) {
            if (b.length() > 0) {
                b.append(" ;; ");
            }
            b.append(positionLine(p[0], p[1], p[2], p[3], p[4]));
        }
        return b.toString();
    }

    /**
     * @return one position as in {@link #positionsCoreInDb} (6 decimals)
     */
    static String positionLine(Object description, Object quantity, Object price, Object discount, Object taxRate) {
        return GobdUtil.fmtExact(quantity) + "x " + str(description).trim() + " @ " + GobdUtil.fmtExact(price)
                + " -" + GobdUtil.fmtExact(discount) + "% USt " + GobdUtil.fmtExact(taxRate) + "%";
    }

    static String revenueSnapshot(Revenue rev) {
        return moneySnapshot(rev.__getCnumber(), rev.__getDateadded(), rev.__getAccountsids(), rev.__getNetvalue(), rev.__getTaxpercentvalue(),
                rev.__getBrutvalue(), rev.__getDescription(), false);
    }

    static String expenseSnapshot(Expense exp) {
        return moneySnapshot(exp.__getCnumber(), exp.__getDateadded(), exp.__getAccountsids(), exp.__getNetvalue(), exp.__getTaxpercentvalue(),
                exp.__getBrutvalue(), exp.__getDescription(), false);
    }

    /**
     * For comparing: {@link #revenueSnapshot} with 6 decimals
     */
    static String revenueCore(Revenue rev) {
        return moneySnapshot(rev.__getCnumber(), rev.__getDateadded(), rev.__getAccountsids(), rev.__getNetvalue(), rev.__getTaxpercentvalue(),
                rev.__getBrutvalue(), rev.__getDescription(), true);
    }

    /**
     * For comparing: {@link #expenseSnapshot} with 6 decimals
     */
    static String expenseCore(Expense exp) {
        return moneySnapshot(exp.__getCnumber(), exp.__getDateadded(), exp.__getAccountsids(), exp.__getNetvalue(), exp.__getTaxpercentvalue(),
                exp.__getBrutvalue(), exp.__getDescription(), true);
    }

    /**
     * The same as {@link #revenueSnapshot} / {@link #expenseSnapshot} from the
     * columns cnumber, dateadded, accountsids, netvalue, taxpercentvalue,
     * brutvalue, description
     */
    static String moneySnapshotOfRow(Object[] db) {
        return moneySnapshot(db[0], db[1], db[2], db[3], db[4], db[5], db[6], false);
    }

    /**
     * {@link #moneySnapshotOfRow} with 6 decimals
     */
    static String moneyCoreOfRow(Object[] db) {
        return moneySnapshot(db[0], db[1], db[2], db[3], db[4], db[5], db[6], true);
    }

    private static String moneySnapshot(Object num, Object date, Object acc, Object net, Object taxPct, Object gross, Object desc, boolean exact) {
        return String.join("|",
                "Num=" + str(num),
                "Date=" + fmtDate(date),
                "Acc=" + str(acc),
                "Net=" + (exact ? GobdUtil.fmtExact(net) : fmtDec(net)),
                "TaxPct=" + (exact ? GobdUtil.fmtExact(taxPct) : fmtDec(taxPct)),
                "Gross=" + (exact ? GobdUtil.fmtExact(gross) : fmtDec(gross)),
                "Desc=" + str(desc).trim());
    }

    /**
     * The record of a finalized invoice that is hashed (version 2)
     */
    static String documentRecord(Object seq, Object docId, Object number, Object finalizedAt, Object by, Object format, Object fileSha,
            Object cancels, String core, String positions) {
        return GobdUtil.record("v", 2, "seq", seq, "document_id", docId, "number", number, "finalized_at", GobdUtil.fmtTimestamp(finalizedAt),
                "finalized_by", by, "format", format, "file_sha256", fileSha, "cancels", cancels, "core", core, "positions", positions);
    }

    /**
     * The record of an imported income that is hashed (version 2)
     */
    static String incomeRecord(Object seq, Object revId, Object number, Object importedAt, Object by, Object originalName, Object format,
            Object fileSha, Object fileSize, String core) {
        return GobdUtil.record("v", 2, "seq", seq, "revenue_id", revId, "number", number, "imported_at", GobdUtil.fmtTimestamp(importedAt),
                "imported_by", by, "original_name", originalName, "format", format, "file_sha256", fileSha, "file_size", fileSize, "core", core);
    }

    /**
     * The receipts of YaBS (files linked to expenses or income) and their
     * hash chains of the plugin
     */
    enum Receipts {
        EXPENSE("expense_receipts", "expensesids", "expenses", "gobd_receipt", "expense_id", "expense_snapshot", "Ausgabe", "Eingangsbeleg"),
        REVENUE("revenue_receipts", "revenuesids", "revenues", "gobd_revenue_receipt", "revenue_id", "revenue_snapshot", "Einnahme", "Einnahmenbeleg");

        /**
         * The table of YaBS, its column of the expense / income, the table of
         * the expenses / income
         */
        final String table, ownerColumn, bookingTable;
        /**
         * The chain of the plugin, its column of the expense / income, of the
         * snapshot
         */
        final String chainTable, chainOwnerColumn, snapshotColumn;
        /**
         * For messages
         */
        final String bookingName, receiptName;

        Receipts(String table, String ownerColumn, String bookingTable, String chainTable, String chainOwnerColumn, String snapshotColumn,
                String bookingName, String receiptName) {
            this.table = table;
            this.ownerColumn = ownerColumn;
            this.bookingTable = bookingTable;
            this.chainTable = chainTable;
            this.chainOwnerColumn = chainOwnerColumn;
            this.snapshotColumn = snapshotColumn;
            this.bookingName = bookingName;
            this.receiptName = receiptName;
        }

        /**
         * The entity type in the audit log
         */
        String entityType() {
            return name();
        }
    }

    /**
     * The record of a receipt of an expense that is hashed
     */
    static String receiptRecord(Object seq, Object ref, Object expenseId, Object originalName, Object fileSize, Object fileSha,
            Object importedAt, Object by, String core) {
        return receiptRecord(Receipts.EXPENSE, seq, ref, expenseId, originalName, fileSize, fileSha, importedAt, by, core);
    }

    /**
     * The record of a receipt of an expense or income that is hashed
     */
    static String receiptRecord(Receipts kind, Object seq, Object ref, Object ownerId, Object originalName, Object fileSize, Object fileSha,
            Object importedAt, Object by, String core) {
        return GobdUtil.record("v", 2, "seq", seq, "receipt_ref", ref, kind.chainOwnerColumn, ownerId, "original_name", originalName,
                "file_size", fileSize, "file_sha256", fileSha, "imported_at", GobdUtil.fmtTimestamp(importedAt), "imported_by", by, "core", core);
    }

    /**
     * The columns of expenses and income read by {@link #moneyCoreOfRow}
     */
    static final String MONEY_CORE_COLUMNS = "cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description";

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
        return validateMandatoryFields(doc, true);
    }

    /**
     * @param doc
     * @param withNumber false: before the final number is given
     * @return the missing mandatory fields of § 14 Abs. 4 UStG
     */
    /**
     * @param doc
     * @return the service date (Leistungsdatum, datetodo), else the document
     * date (as the core does since #208)
     */
    static java.util.Date serviceDate(Item doc) {
        return doc.__getDatetodo() != null ? doc.__getDatetodo() : doc.__getDateadded();
    }

    static List<String> validateMandatoryFields(Item doc, boolean withNumber) {
        List<String> errors = new ArrayList<String>();
        String num = str(doc.__getCnumber()).trim();
        if (withNumber && GobdUtil.isDraftNumber(num)) {
            errors.add("Endgueltige fortlaufende Rechnungsnummer fehlt (aktuell: '" + num + "') (§ 14 Abs. 4 Nr. 4 UStG)");
        }
        if (doc.__getDateadded() == null) {
            errors.add("Ausstellungsdatum fehlt (§ 14 Abs. 4 Nr. 3 UStG)");
        }
        //the service date (datetodo); empty means the document date (core, #208)
        if (serviceDate(doc) == null) {
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
            //only the file of the final state: it has the final number (not a draft number)
            //and the invoice was not saved after it was created (see executeAfterSave)
            if (EInvoice.mentionsNumber(recent, str(doc.__getCnumber()))) {
                String fileName = recent.getFileName().toString();
                String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "pdf";
                String format = ext.equals("xml") ? "XRECHNUNG_XML" : "ZUGFERD_PDFA3";
                Path target = GobdUtil.uniquePath(yearDir, safeDocNum, ext);
                Files.copy(recent, target);
                return new Archived(target, Files.readAllBytes(target), format);
            }
            status("GoBD: Die zuletzt erzeugte Datei " + recent.getFileName() + " zeigt nicht die Belegnummer " + doc.__getCnumber()
                    + " - archiviert wird stattdessen der Datensatz als XML.");
        }
        Path target = GobdUtil.uniquePath(yearDir, safeDocNum, "xml");
        BigDecimal net = doc.getNetAfterDiscount();
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<GoBDArchivedInvoice xmlns=\"urn:openyabs:gobd:archive:1.0\" standard=\"EN16931\">\n");
        xml.append("  <DocumentId>").append(doc.__getIDS()).append("</DocumentId>\n");
        xml.append("  <DocumentNumber>").append(GobdUtil.escapeXml(doc.__getCnumber())).append("</DocumentNumber>\n");
        xml.append("  <DocumentType>").append(doc.__getInttype() == Item.TYPE_CREDIT ? "381_CREDIT_NOTE" : "380_INVOICE").append("</DocumentType>\n");
        xml.append("  <IssueDate>").append(GobdUtil.escapeXml(fmtDate(doc.__getDateadded()))).append("</IssueDate>\n");
        xml.append("  <DeliveryDate>").append(GobdUtil.escapeXml(fmtDate(serviceDate(doc)))).append("</DeliveryDate>\n");
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
        final long docId = doc.__getIDS();
        String oldNumber = doc.__getCnumber();
        int oldStatus = doc.__getIntstatus();
        Date oldEnd = doc.__getDateend();
        boolean numberedNow = false;
        Path archived = null;
        boolean committed = false;
        try {
            if (lockRecord(docId) != null) {
                status("GoBD: Beleg " + doc.__getCnumber() + " ist bereits festgeschrieben.");
                return false;
            }
            //check first: a failed check uses up no number
            List<String> problems = validateMandatoryFields(doc, false);
            if (!problems.isEmpty()) {
                error("GoBD-Festschreibung abgebrochen (§ 14 Abs. 4 UStG):\n- " + String.join("\n- ", problems));
                return false;
            }
            //the year of the invoice (issue date), not of the day of finalizing
            int year = GobdUtil.yearOf(doc.__getDateadded());
            if (GobdUtil.isDraftNumber(doc.__getCnumber())) {
                doc.setCnumber(nextNumber(doc, year));
                numberedNow = true;
            }
            //status finished and end date (without an own save)
            mpv5.handler.ItemService.ensureFinishedAndEndDate(doc, false);
            if (!doc.save(true)) {
                throw new GobdException("Der Beleg konnte nicht gespeichert werden.");
            }
            problems = validateMandatoryFields(doc, true);
            if (!problems.isEmpty()) {
                throw new GobdException("Pflichtangaben fehlen (§ 14 Abs. 4 UStG):\n- " + String.join("\n- ", problems));
            }
            //what is frozen: the invoice as it is in the database now
            final String core = itemCoreInDb(docId);
            if (core == null) {
                throw new GobdException("Der Beleg konnte nicht aus der Datenbank gelesen werden.");
            }
            final String positions = positionsCoreInDb(docId);
            final String number = doc.__getCnumber();
            //a number is locked once: another invoice type with the same number (e.g. invoice and credit
            //note 000001, the same number format) is refused with a message, not a database error (#212)
            Object[] taken = q1("SELECT document_id FROM gobd_document_lock WHERE document_number = ? AND document_id <> ?", number, docId);
            if (taken != null) {
                throw new GobdException(duplicateNumberMessage(number, ((Number) taken[0]).longValue()));
            }
            String safeDocNum = GobdUtil.safeFileName(blank(number) ? "BELEG_" + docId : number);
            final String customerSnap = contactSnapshot((long) doc.__getContactsids());
            final String positionsSnap = positionsSnapshot(doc);
            final Archived a = archiveArtifact(doc, year, safeDocNum, customerSnap);
            archived = a.path;
            final String fileSha = GobdUtil.sha256(a.bytes);
            final Timestamp at = nowSeconds();
            final String user = trunc(currentUser(), 100);
            String hash = addLink(c -> {
                if (Db.first(c, "SELECT document_id FROM gobd_document_lock WHERE document_id = ?", docId) != null) {
                    throw new GobdException("Beleg " + number + " wurde gleichzeitig an anderer Stelle festgeschrieben.");
                }
                Object[] same = Db.first(c, "SELECT document_id FROM gobd_document_lock WHERE document_number = ?", number);
                if (same != null) {
                    throw new GobdException(duplicateNumberMessage(number, ((Number) same[0]).longValue()));
                }
                Object[] end = chainEnd(c, "gobd_document_lock", "sha256_hash");
                String prev = (String) end[1];
                String record = documentRecord(end[0], docId, number, at, user, a.format, fileSha, cancelsDocId, core, positions);
                String h = GobdUtil.recordHash(record, prev);
                Db.update(c, "INSERT INTO gobd_document_lock (document_id, chain_seq, document_number, status, finalized_at, finalized_by,"
                        + " customer_snapshot, positions_snapshot, archive_path, artifact_format, sha256_hash, prev_sha256_hash, cancels_document_id,"
                        + " hash_version, file_sha256, record_data) VALUES (?, ?, ?, 'FINALIZED', ?, ?, ?, ?, ?, ?, ?, ?, ?, 2, ?, ?)",
                        docId, end[0], number, at, user, customerSnap, positionsSnap, a.path.toString(), a.format, h, prev, cancelsDocId,
                        fileSha, record);
                logAudit(c, "INVOICE", docId, "FINALIZED", "STATUS=DRAFT", "STATUS=FINALIZED | SNAPSHOT=" + customerSnap, h,
                        "Festgeschrieben: " + number + " (" + a.format + ")");
                return h;
            });
            committed = true;
            GobdUtil.makeReadOnly(a.path);
            lastGeneratedFiles.remove(docId);
            yabs.refreshView(doc);
            notifyQuiet("GoBD-Festschreibung erfolgreich:\nBeleg: " + number + " (" + a.format + ")\nSHA-256: "
                    + hash.substring(0, 16) + "...\nArchiv: " + a.path.getFileName(), doc);
            return true;
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
            error("GoBD-Festschreibung fehlgeschlagen:\n" + e.getMessage());
            return false;
        } finally {
            if (!committed) {
                undoFailedFinalize(doc, archived, numberedNow, oldNumber, oldStatus, oldEnd);
            }
            finalizingIds.remove(doc.__getIDS());
        }
    }

    /**
     * @param number
     * @param otherId the finalized document with this number
     * @return why a document with a number already locked is not finalized
     */
    static String duplicateNumberMessage(String number, long otherId) {
        return "Die Nummer " + number + " ist bereits für einen anderen festgeschriebenen Beleg (ID " + otherId + ") vergeben."
                + " Rechnungen, Gutschriften, Anzahlungs- und Abschlagsrechnungen brauchen Nummern, die unter allen eindeutig sind"
                + " (§ 14 Abs. 4 Nr. 4 UStG). Bitte vergeben Sie dem Beleg eine eigene Nummer (Nummer ändern) und legen Sie in"
                + " Systemsteuerung › Formate je Belegart ein eigenes Präfix fest (z. B. RE-, GS-, AZ-, AR-).";
    }

    /**
     * After a failed finalizing: no archived file without lock, no final
     * number without lock (no gap in the numbers)
     */
    private void undoFailedFinalize(Item doc, Path archived, boolean numberedNow, String oldNumber, int oldStatus, Date oldEnd) {
        if (archived != null) {
            try {
                archived.toFile().setWritable(true);
                Files.deleteIfExists(archived);
            } catch (Exception e) {
                Log.Debug(Gobd.class, e);
            }
        }
        if (numberedNow) {
            try {
                if (lockRecord(doc.__getIDS()) == null) {
                    doc.setCnumber(oldNumber);
                    doc.setIntstatus(oldStatus);
                    doc.setDateend(oldEnd);
                    doc.save(true);
                }
            } catch (Exception e) {
                Log.Debug(Gobd.class, e);
            }
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
        creatingStorno = true;
        try {
            //a new record of the credit notes (the context comes with it)
            Item storno = (Item) DatabaseObject.getObject(Context.getCredit());
            storno.setContactsids(original.__getContactsids());
            storno.setAccountsids(original.__getAccountsids());
            storno.setGroupsids(original.__getGroupsids());
            storno.setInttype(Item.TYPE_CREDIT);
            storno.setDateadded(new Date());
            storno.setDatetodo(original.__getDatetodo() != null ? original.__getDatetodo() : new Date());
            //the number after the date: the format may use [YEAR] ... of the record.
            //Saving a new record takes the number of its format handler (ensureUniqueness),
            //for a credit note the next credit note number: here the cancellation number.
            final String stornoNumber = nextCancellationNumber(storno);
            storno.setCnumber(stornoNumber);
            storno.defineFormatHandler(new mpv5.handler.FormatHandler(storno) {
                @Override
                public synchronized String next() {
                    return stornoNumber;
                }
            });
            //a credit note is a reduction by its type: the amounts as in the invoice (positive). Negative
            //amounts were counted twice negative (the journal showed the cancellation as receivable, the
            //e-invoice credit note had negative amounts)
            storno.setNetvalue(nz(original.__getNetvalue()));
            storno.setTaxvalue(nz(original.__getTaxvalue()));
            storno.setDiscountvalue(nz(original.__getDiscountvalue()));
            storno.setDiscountGrosvalue(nz(original.__getDiscountGrosvalue()));
            storno.setDescription("Stornorechnung zu Rechnung Nr. " + original.__getCnumber());
            //the cancelled invoice: on the document and in the e-invoice (BT-25 / BT-26)
            storno.setRefinvoiceids(original.__getIDS());
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
                rev.setQuantityvalue(nz(line.__getQuantityvalue()));
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
                u("UPDATE gobd_document_lock SET status = 'CANCELLED' WHERE document_id = ?", original.__getIDS());
                try {
                    original.setIntstatus(Item.STATUS_CANCELLED);
                    original.save(true);
                } catch (Exception ignored) {
                    //the lock says cancelled
                }
                yabs.refreshView(original);
                logAudit("INVOICE", original.__getIDS(), "CANCELLED", "STATUS=FINALIZED", "STATUS=CANCELLED BY " + storno.__getCnumber(),
                        str(origLock[9]), "Storniert durch Stornobeleg " + storno.__getCnumber());
                notifyQuiet("Beleg " + original.__getCnumber() + " storniert durch " + storno.__getCnumber() + ".", original);
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
                + " INNER JOIN gobd_document_lock g ON i.ids = g.document_id WHERE g.status = 'FINALIZED' AND i.inttype IN (" + types + ") ORDER BY i.ids DESC"
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

    // ---------------------------------------------------------------- income: locked by its receipt
    /**
     * Links a receipt file to an existing income and locks the income. YaBS
     * archives the file with its checksum (revenue_receipts); the lock refers
     * to that receipt. E-invoices as new income are imported by YaBS (import
     * button, drag and drop, e-invoice folder) and locked by
     * {@link #onActivity(Activity.Entry)}.
     *
     * @param sourceFile
     * @param targetRevenue the saved income
     * @param silent no dialogs
     * @return the income, null if not linked
     */
    Revenue importIncomeFile(Path sourceFile, Revenue targetRevenue, boolean silent) {
        if (targetRevenue == null || targetRevenue.__getIDS() <= 0 || sourceFile == null || !Files.isRegularFile(sourceFile)) {
            return null;
        }
        try {
            mpv5.handler.RevenueReceipts.Receipt r = mpv5.handler.RevenueReceipts.attach(targetRevenue, sourceFile.toFile());
            if (r == null) {
                return null;
            }
            lockIncome(targetRevenue, r.originalName, r.archivePath, r.fileSize, r.sha256, silent);
            return targetRevenue;
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
            if (!silent) {
                error("GoBD: Beleg konnte nicht mit der Einnahme verknuepft werden:\n" + e.getMessage());
            }
            return null;
        }
    }

    /**
     * A receipt of an income was archived by YaBS: the income is locked with
     * its first receipt
     *
     * @param revenueId
     * @param archived the archived file (path in revenue_receipts)
     */
    void incomeReceiptArchived(long revenueId, File archived) {
        if (revenueId <= 0 || archived == null || incomeLockRecord(revenueId) != null) {
            return;
        }
        Object[] r = q1("SELECT originalname, archivepath, filesize, sha256 FROM revenue_receipts WHERE revenuesids = ? AND archivepath = ?",
                revenueId, archived.getAbsolutePath());
        if (r == null) {
            return;
        }
        try {
            Revenue rev = (Revenue) DatabaseObject.getObject(Context.getRevenue(), (int) revenueId);
            lockIncome(rev, str(r[0]), str(r[1]), GobdUtil.toLong(r[2], 0), str(r[3]), true);
        } catch (Exception e) {
            Log.Debug(Gobd.class, e);
        }
    }

    /**
     * Locks an income: a link of the income chain with the receipt (archived
     * by YaBS) and the booking data; nothing if it is locked already
     *
     * @return true if locked now
     */
    boolean lockIncome(Revenue rev, String origName, String archivePath, long size, String fileSha, boolean silent) throws Exception {
        final long revId = rev.__getIDS();
        if (incomeLockRecord(revId) != null) {
            return false;
        }
        Path file = Paths.get(archivePath);
        String lower = str(origName).toLowerCase();
        String format = "RECEIPT_FILE";
        if (lower.endsWith(".xml")) {
            format = "XRECHNUNG_XML";
        } else if (lower.endsWith(".pdf") && Files.isRegularFile(file) && EInvoice.extractEmbeddedXml(file) != null) {
            format = "ZUGFERD_PDFA3";
        }
        final String docNum = blank(rev.__getCnumber()) ? "#" + revId : rev.__getCnumber();
        Object[] db = row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description FROM revenues WHERE ids = " + revId);
        if (db == null) {
            throw new GobdException("Die Einnahme konnte nicht aus der Datenbank gelesen werden.");
        }
        final String core = moneyCoreOfRow(db);
        final String finSnap = revenueSnapshot(rev);
        final String fmt = format;
        final Timestamp at = nowSeconds();
        final String user = trunc(currentUser(), 100);
        final String sha = str(fileSha).trim().toLowerCase();
        //not synchronized: the transaction runs on the Swing thread, one after the other; locked
        //meanwhile (the import and its activity event): nothing
        String linked = addLink(c -> {
            if (Db.first(c, "SELECT revenue_id FROM gobd_income_lock WHERE revenue_id = ?", revId) != null) {
                return null;
            }
            //an invoice with several VAT rates is booked as one income per rate, all with the same file:
            //locked once (file_sha256 is unique); the other parts are protected by their receipts
            if (Db.first(c, "SELECT revenue_id FROM gobd_income_lock WHERE file_sha256 = ?", sha) != null) {
                return null;
            }
            Object[] end = chainEnd(c, "gobd_income_lock", "chain_hash");
            String prev = (String) end[1];
            String record = incomeRecord(end[0], revId, docNum, at, user, origName, fmt, sha, size, core);
            String h = GobdUtil.recordHash(record, prev);
            Db.update(c, "INSERT INTO gobd_income_lock (revenue_id, chain_seq, document_number, status, imported_at, imported_by, original_name,"
                    + " archive_path, file_size, artifact_format, file_sha256, chain_hash, prev_chain_hash, financial_snapshot, hash_version, record_data)"
                    + " VALUES (?, ?, ?, 'LOCKED', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 2, ?)",
                    revId, end[0], docNum, at, user, origName, archivePath, size, fmt, sha, h, prev, trunc(finSnap, 2000), record);
            logAudit(c, "REVENUE", revId, "INCOME_LOCKED", null, finSnap, sha,
                    "Einnahme " + docNum + " (" + fmt + ") mit Beleg " + origName + " festgeschrieben.");
            return h;
        });
        if (linked == null) {
            return false;
        }
        chainReceipts(Receipts.REVENUE);
        yabs.refreshView(rev);
        String text = "GoBD: Einnahme " + docNum + " festgeschrieben (" + fmt + ", SHA-256: " + sha.substring(0, Math.min(16, sha.length())) + "...)";
        if (silent) {
            status(text);
        } else {
            notifyQuiet(text + ("RECEIPT_FILE".equals(fmt)
                    ? "\n(Hinweis: Einfacher Beleg - Werte koennen nicht automatisch auf rechnerische Richtigkeit geprueft werden.)" : ""), rev);
        }
        return true;
    }

    // ---------------------------------------------------------------- receipts of expenses and income
    /**
     * Chains the receipts of expenses and income (expense_receipts,
     * revenue_receipts of YaBS) that are not chained yet
     *
     * @return the number of receipts chained now
     */
    int chainReceipts() {
        return chainReceipts(Receipts.EXPENSE) + chainReceipts(Receipts.REVENUE);
    }

    /**
     * Chains the receipts of the kind that are not chained yet into the chain
     * of the plugin: SHA256(record || prev), the record with the file hash and
     * the booking data of the expense / income. Only receipts whose file is
     * there and has the hash of the database (else the check reports them).
     *
     * @return the number of receipts chained now
     */
    //not synchronized: a thread holding the lock would wait for the Swing thread (Db.tx) while that
    //waits for the lock (deadlock); the transactions run one after the other on the Swing thread
    int chainReceipts(Receipts kind) {
        Object[][] open = rows("SELECT r.ids, r." + kind.ownerColumn + ", r.originalname, r.archivepath, r.filesize, r.sha256, r.mimetype FROM "
                + kind.table + " r WHERE NOT EXISTS (SELECT 1 FROM " + kind.chainTable + " g WHERE g.receipt_ref = CAST(r.ids AS VARCHAR(250)))"
                + " ORDER BY r.ids ASC");
        int chained = 0;
        for (Object[] r : open) {
            try {
                Path f = Paths.get(str(r[3]));
                String sha = str(r[5]).trim().toLowerCase();
                if (!Files.isRegularFile(f) || !GobdUtil.sha256(f).equals(sha)) {
                    continue;
                }
                final long ownerId = GobdUtil.toLong(r[1], -1);
                Object[] booking = row("SELECT " + MONEY_CORE_COLUMNS + " FROM " + kind.bookingTable + " WHERE ids = " + ownerId);
                if (booking == null) {
                    continue;
                }
                final String ref = str(r[0]);
                final String name = str(r[2]);
                final long size = GobdUtil.toLong(r[4], -1);
                final String core = moneyCoreOfRow(booking);
                final String snapshot = trunc(moneySnapshotOfRow(booking), 2000);
                final String lower = name.toLowerCase();
                final String format = lower.endsWith(".xml") ? "XML" : lower.endsWith(".pdf") ? "PDF" : blank(str(r[6])) ? "FILE" : trunc(str(r[6]), 30);
                final Timestamp at = nowSeconds();
                final String user = trunc(currentUser(), 100);
                String linked = addLink(c -> {
                    //chained meanwhile (another caller)
                    if (Db.first(c, "SELECT chain_hash FROM " + kind.chainTable + " WHERE receipt_ref = ?", ref) != null) {
                        return null;
                    }
                    Object[] end = chainEnd(c, kind.chainTable, "chain_hash");
                    String prev = (String) end[1];
                    String record = receiptRecord(kind, end[0], ref, ownerId, name, size, sha, at, user, core);
                    String h = GobdUtil.recordHash(record, prev);
                    Db.update(c, "INSERT INTO " + kind.chainTable + " (" + kind.chainOwnerColumn + ", chain_seq, receipt_ref, original_name, archive_path,"
                            + " file_size, artifact_format, sha256, chain_hash, prev_chain_hash, " + kind.snapshotColumn + ", imported_at, imported_by, record_data)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            ownerId, end[0], ref, name, f.toString(), size, format, sha, h, prev, snapshot, at, user, record);
                    logAudit(c, kind.entityType(), ownerId, "RECEIPT_CHAINED", null, snapshot, sha,
                            kind.receiptName + " " + name + " in die Hash-Kette aufgenommen.");
                    return h;
                });
                if (linked != null) {
                    chained++;
                }
            } catch (Exception e) {
                Log.Debug(Gobd.class, e);
            }
        }
        return chained;
    }

    // ---------------------------------------------------------------- the guard
    /**
     * @param obj
     * @param isDelete
     * @return false if the change of a finalized record is not allowed (the
     * user is told why)
     */
    boolean checkWriteAllowed(DatabaseObject obj, boolean isDelete) {
        try {
            return checkWrite(obj, isDelete);
        } catch (RuntimeException e) {
            //fail closed: what can not be checked is not changed
            Log.Debug(Gobd.class, e);
            error("GoBD-Schutz: Die Aenderung wurde nicht gespeichert, weil die GoBD-Pruefung fehlschlug:\n" + e.getMessage());
            return false;
        }
    }

    private boolean checkWrite(DatabaseObject obj, boolean isDelete) {
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
        Object[] db = row("SELECT " + ITEM_CORE_COLUMNS + ", intstatus, intreminders FROM items WHERE ids = " + doc.__getIDS());
        if (db != null) {
            //compared with the frozen state of the lock (version 2), else with the database
            String frozen = GobdUtil.recordField(str(lock[12]), "core");
            boolean unchanged = frozen != null
                    ? frozen.equals(GobdUtil.recordValue(financialCore(doc)))
                    : itemCoreOfRow(db).equals(financialCore(doc));
            if (unchanged) {
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
        Object[][] receipts = q("SELECT ids, sha256 FROM revenue_receipts WHERE revenuesids = ?", rev.__getIDS());
        if (incLock == null && receipts.length == 0) {
            return true;
        }
        String lockHash = incLock != null ? str(incLock[10]) : str(receipts[0][1]);
        String docNum = incLock != null ? str(incLock[2]) : (blank(rev.__getCnumber()) ? "#" + rev.__getIDS() : rev.__getCnumber());
        String lockInfo = incLock != null ? str(incLock[13]) : "Receipts=" + receipts.length;
        if (isDelete || isMovedToTrash(rev)) {
            logAudit("REVENUE", rev.__getIDS(), "BLOCKED_DELETE", lockInfo, "Delete/Trash=true", lockHash,
                    "Loesch- oder Papierkorb-Versuch an GoBD-gesperrter Einnahme " + docNum + ".");
            error("GoBD-Loeschschutz (§§ 146, 147 AO):\nEinnahme " + docNum + " ist durch einen archivierten Originalbeleg festgeschrieben"
                    + " und darf weder geloescht noch in den Papierkorb verschoben werden!");
            return false;
        }
        //revenues have no ispaid column: paid is status 4 (Item.STATUS_PAID)
        Object[] db = row("SELECT cnumber, dateadded, accountsids, netvalue, taxpercentvalue, brutvalue, description, status, dateend"
                + " FROM revenues WHERE ids = " + rev.__getIDS());
        if (db != null && moneyCoreOfRow(db).equals(revenueCore(rev))) {
            int oldStatus = GobdUtil.toInt(db[7], 0);
            int newStatus = rev.__getStatus();
            String oldEnd = fmtDate(db[8]);
            String newEnd = fmtDate(rev.__getDateend());
            boolean oldPaid = oldStatus == Item.STATUS_PAID;
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
        Object[][] receipts = q("SELECT ids, sha256 FROM expense_receipts WHERE expensesids = ?", exp.__getIDS());
        if (receipts.length == 0) {
            return true;
        }
        String hash = str(receipts[0][1]);
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
        String dbCore = moneyCoreOfRow(db);
        String newCore = expenseCore(exp);
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
                        try {
                            logAudit("CONTACT", c.__getIDS(), "MASTERDATA_CHANGED", oldSnap, newSnap, null, "Kundenstammdaten geaendert (" + c.__getCname() + ")");
                        } catch (RuntimeException e) {
                            Log.Debug(Gobd.class, e);
                            error("GoBD-Schutz: Die Stammdaten-Aenderung konnte nicht protokolliert werden:\n" + e.getMessage());
                            throw veto(obj);
                        }
                        status("GoBD: Stammdaten-Aenderung protokolliert (" + c.__getCname() + ").");
                    }
                }
                return obj;
            }

            @Override
            public DatabaseObject executeAfterCreate(DatabaseObject obj) {
                if (obj instanceof Item && isTaxRelevant(obj) && obj.__getIDS() > 0) {
                    Item doc = (Item) obj;
                    try {
                        logAudit("INVOICE", doc.__getIDS(), "DRAFT_CREATED", null, "Cnumber=" + doc.__getCnumber(), null, "Entwurf angelegt: " + doc.__getCnumber());
                    } catch (RuntimeException e) {
                        Log.Debug(Gobd.class, e);
                        error("GoBD-Audit-Log konnte nicht geschrieben werden:\n" + e.getMessage());
                    }
                }
                return obj;
            }

            @Override
            public DatabaseObject executeAfterSave(DatabaseObject object) {
                //a file created before this save shows an old state: not for the archive
                if (object instanceof Item && !finalizingIds.contains(object.__getIDS())) {
                    lastGeneratedFiles.remove((long) object.__getIDS());
                }
                inventory.track(object);
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
                //after the delete (it follows this call on the Swing thread)
                javax.swing.SwingUtilities.invokeLater(() -> inventory.track(obj));
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
        //a receipt of an income archived by YaBS (import, drag and drop, e-invoice folder, link):
        //the income is locked with it
        if (entry.context != null && entry.id > 0 && Context.getRevenue() != null
                && String.valueOf(entry.context.getDbIdentity()).equalsIgnoreCase(String.valueOf(Context.getRevenue().getDbIdentity()))) {
            incomeReceiptArchived(entry.id, entry.file);
            chainReceipts(Receipts.REVENUE);
            return;
        }
        //a receipt of an expense archived by YaBS: into the hash chain now
        if (entry.context != null && entry.id > 0 && Context.getExpense() != null
                && String.valueOf(entry.context.getDbIdentity()).equalsIgnoreCase(String.valueOf(Context.getExpense().getDbIdentity()))) {
            chainReceipts(Receipts.EXPENSE);
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
            //the record is not known: the open tab may be another one. Only remember the
            //file if it shows the number of the open invoice, and never finalize automatically.
            mpv5.ui.panels.DataPanel tab = yabs.currentView();
            if (tab != null && tab.getDataOwner() instanceof Item) {
                Item it = (Item) tab.getDataOwner();
                if (it.__getIDS() > 0 && isGobdItemType(it.__getInttype()) && !GobdUtil.isDraftNumber(it.__getCnumber())
                        && EInvoice.mentionsNumber(entry.file.toPath(), str(it.__getCnumber())) && lockRecord(it.__getIDS()) == null) {
                    lastGeneratedFiles.put((long) it.__getIDS(), entry.file.toPath());
                    status("Belegdatei erstellt (" + entry.file.getName() + "). Beleg " + it.__getCnumber()
                            + " jetzt ueber 'Plugins > GoBD: 1. Beleg festschreiben' sichern.");
                }
            }
            return;
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

    // ---------------------------------------------------------------- locked documents
    /**
     * The provider of the archived originals for YaBS: print, PDF, preview, ODT
     * and e-invoice of a finalized invoice open the archived file instead of
     * creating a new one; if the file is missing, YaBS offers a stamped
     * duplicate, which this provider checks and records in the audit log
     *
     * @return
     */
    mpv5.plugin.api.LockedDocumentProvider lockedDocuments() {
        return new mpv5.plugin.api.LockedDocumentProvider() {
            @Override
            public File lockedDocument(DatabaseObject record) {
                Object[] lock = lockOf(record);
                return lock == null || blank(str(lock[7])) ? null : new File(str(lock[7]).trim());
            }

            @Override
            public String duplicateRefused(DatabaseObject record) {
                Object[] lock = lockOf(record);
                if (lock == null) {
                    return null;
                }
                String changed = changedSinceFinalized(record.__getIDS(), str(lock[12]));
                if (changed == null) {
                    return null;
                }
                String num = str(lock[1]);
                logAudit("INVOICE", record.__getIDS(), "DUPLICATE_REFUSED", "ARCHIVE_MISSING=" + str(lock[7]), "CHANGED=" + changed, null,
                        "Archivierte Originaldatei von Beleg " + num + " fehlt; Duplikat abgelehnt, die Daten weichen vom festgeschriebenen Stand ab.");
                return "Die Daten von Beleg " + num + " in der Datenbank weichen vom festgeschriebenen Stand ab (" + changed + ")."
                        + " Ein Duplikat zeigte einen anderen Inhalt als das Original. Bitte die GoBD-Pruefung ausfuehren"
                        + " und das Original aus der Datensicherung wiederherstellen.";
            }

            @Override
            public List<String> duplicateWarnings(DatabaseObject record) {
                Object[] lock = lockOf(record);
                if (lock == null || !(record instanceof Item)) {
                    return Collections.emptyList();
                }
                String then = str(lock[5]);
                String now = contactSnapshot((long) ((Item) record).__getContactsids());
                if (blank(then) || then.equals(now)) {
                    return Collections.emptyList();
                }
                return Collections.singletonList("Die Kundendaten wurden seit der Festschreibung geaendert."
                        + "\nBei Festschreibung: " + then + "\nJetzt: " + now);
            }

            @Override
            public void duplicateCreated(DatabaseObject record, File missing, File duplicate) {
                Object[] lock = lockOf(record);
                String sha = null;
                try {
                    sha = GobdUtil.sha256(Files.readAllBytes(duplicate.toPath()));
                } catch (Exception e) {
                    Log.Debug(Gobd.class, e);
                }
                logAudit("INVOICE", record.__getIDS(), "DUPLICATE_CREATED", "ARCHIVE_MISSING=" + missing, "DUPLICATE=" + duplicate, sha,
                        "Archivierte Originaldatei von Beleg " + (lock == null ? record.__getCname() : str(lock[1]))
                        + " fehlt; Duplikat erstellt (auf jeder Seite als Kopie gestempelt, kein Original, nicht archiviert).");
            }
        };
    }

    /**
     * @return the lock of a finalized invoice, null for other records
     */
    private Object[] lockOf(DatabaseObject record) {
        if (!(record instanceof Item) || !isGobdItemType(((Item) record).__getInttype()) || record.__getIDS() <= 0) {
            return null;
        }
        return lockRecord(record.__getIDS());
    }

    /**
     * @param docId
     * @param record the finalized record (hash version 2), may be empty
     * @return the changed parts (invoice, positions), null if the database
     * still holds what was finalized (or nothing to compare with)
     */
    String changedSinceFinalized(long docId, String record) {
        if (blank(record)) {
            return null;
        }
        List<String> changed = new ArrayList<String>();
        String core = itemCoreInDb(docId);
        if (core == null || !GobdUtil.recordValue(core).equals(GobdUtil.recordField(record, "core"))) {
            changed.add("Belegdaten");
        }
        if (!GobdUtil.recordValue(positionsCoreInDb(docId)).equals(GobdUtil.recordField(record, "positions"))) {
            changed.add("Positionen");
        }
        return changed.isEmpty() ? null : String.join(", ", changed);
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
     * Logs the start of the plugin: version, user, settings, and whether the
     * session before ended without a stop (crash, killed, power off)
     *
     * @param version of the plugin
     */
    void logStarted(String version) {
        Object[] last = q1("SELECT action, created_at FROM gobd_audit_log WHERE action IN ('PLUGIN_STARTED', 'PLUGIN_STOPPED')"
                + " ORDER BY id DESC FETCH FIRST 1 ROWS ONLY");
        String before;
        if (last == null) {
            before = "Erster Start des Plugins.";
        } else if ("PLUGIN_STARTED".equals(str(last[0]))) {
            before = "Die vorige Sitzung (Start " + GobdUtil.fmtTimestamp(last[1]) + ") endete ohne Stopp des Plugins (Absturz oder Abbruch).";
        } else {
            before = "Zuletzt gestoppt am " + GobdUtil.fmtTimestamp(last[1]) + ".";
        }
        String settings = String.join(" | ",
                "VERSION=" + version,
                "ARCHIVE=" + archiveRoot(),
                "AUTOLOCK_ON_EXPORT=" + yabs.settings().getBoolean(S_AUTOLOCK, true));
        logAudit("PLUGIN", 0L, "PLUGIN_STARTED", "STATE=STOPPED", "STATE=RUNNING | " + settings, null,
                "GoBD-Konformitaets-Plugin gestartet oder aktiviert. " + before);
    }

    /**
     * Forgets the state
     */
    void stop() {
        try {
            logAudit("PLUGIN", 0L, "PLUGIN_STOPPED", "STATE=RUNNING", "STATE=STOPPED", null, "GoBD-Konformitaets-Plugin wurde gestoppt oder deaktiviert.");
        } catch (Exception ignored) {
            //no database
        }
        inventory.stop();
        creatingStorno = false;
        finalizingIds.clear();
        lastGeneratedFiles.clear();
        masterDataBeforeSave.clear();
    }
}
