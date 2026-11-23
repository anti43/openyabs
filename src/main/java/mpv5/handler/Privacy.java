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
package mpv5.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Data protection (GDPR) for contacts, within the retention duties of tax and
 * commercial law.
 * <p>
 * A person may ask for erasure (Art. 17 GDPR). Records which must be kept
 * (Art. 17 Abs. 3 lit. b: § 147 AO, § 14b UStG, § 257 HGB) are not erased:
 * issued invoices, credit notes, part payment and deposit invoices and the
 * income booked for them for {@value #YEARS_BOOKING_RECORDS} years, orders,
 * order confirmations, delivery notes, accepted offers, activity lists and
 * purchase orders (business letters) for {@value #YEARS_BUSINESS_LETTERS}
 * years, each from the end of the calendar year of the document. They must
 * not be changed either (GoBD), so the name and the addresses of the contact
 * (which the documents show) stay. Everything else about the person is
 * deleted ({@link #block}) and the contact is blocked (restriction of
 * processing, Art. 18 GDPR) until the last of these periods ends; then it can
 * be deleted with all documents ({@link #deleteFinally}).
 * <p>
 * Open invoices (issued, not paid, not cancelled) are a claim (Art. 17 Abs. 3
 * lit. e): they must be paid or cancelled (credit note) first.
 * <p>
 * {@link #export} writes everything stored about the person (Art. 15, 20) to
 * a ZIP file.
 */
public final class Privacy {

    /**
     * § 147 Abs. 3 AO, § 14b UStG (since 2025): invoices, booking records
     */
    public static final int YEARS_BOOKING_RECORDS = 8;
    /**
     * § 147 Abs. 3 AO, § 257 Abs. 4 HGB: business letters
     */
    public static final int YEARS_BUSINESS_LETTERS = 6;
    /**
     * Value property of a blocked contact: when it was blocked (yyyy-MM-dd)
     */
    public static final String KEY_BLOCKED = "privacy.blocked";
    /**
     * Value property of a blocked contact: until when its documents must be
     * kept (yyyy-MM-dd)
     */
    public static final String KEY_KEEP_UNTIL = "privacy.keepuntil";
    /**
     * Properties of a contact kept when it is blocked: they are used to print
     * the kept documents again (e-invoice buyer reference, payment terms, the
     * account the customer pays to)
     */
    static final Set<String> KEPT_PROPERTIES = new HashSet<String>(Arrays.asList(
            mpv5.utils.export.ZugpferdFile.KEY_BUYER_REFERENCE,
            mpv5.utils.export.ZugpferdFile.KEY_PEPPOL_ID,
            mpv5.utils.export.ZugpferdFile.KEY_EORI,
            mpv5.utils.export.PaymentTerms.KEY_SKONTO_PERCENT,
            mpv5.utils.export.PaymentTerms.KEY_SKONTO_DAYS,
            mpv5.utils.export.PaymentTerms.KEY_IMMEDIATE,
            mpv5.utils.export.BankAccount.KEY_CONTACT,
            KEY_BLOCKED, KEY_KEEP_UNTIL));
    private static final int[] INVOICE_TYPES = {Item.TYPE_INVOICE, Item.TYPE_CREDIT, Item.TYPE_PART_PAYMENT, Item.TYPE_DEPOSIT};
    private static final int[] LETTER_TYPES = {Item.TYPE_ORDER, Item.TYPE_DELIVERY_NOTE, Item.TYPE_ORDER_CONFIRMATION};

    private Privacy() {
    }

    /**
     * A document of the contact and what the law says about it
     */
    public static final class Document {

        public final int id;
        public final int type;
        public final String number;
        public final Date date;
        public final int status;
        public final BigDecimal gross;
        /**
         * must be kept until (the end of this day), null: may be deleted
         */
        public final Date keepUntil;
        /**
         * an issued invoice which is neither paid nor cancelled
         */
        public final boolean open;

        Document(int id, int type, String number, Date date, int status, BigDecimal gross, Date keepUntil, boolean open) {
            this.id = id;
            this.type = type;
            this.number = number;
            this.date = date;
            this.status = status;
            this.gross = gross;
            this.keepUntil = keepUntil;
            this.open = open;
        }

        /**
         * @return e.g. "Invoice R-2026-040 · 12.06.2026"
         */
        @Override
        public String toString() {
            return Item.getTypeString(type) + " " + number + (date == null ? "" : " · " + DateConverter.getDefDateString(date));
        }
    }

    /**
     * What may happen to the data of a contact
     */
    public static final class Report {

        /**
         * documents (and income) which must be kept
         */
        public final List<Document> retained = new ArrayList<Document>();
        /**
         * documents which may be deleted (drafts, offers not accepted)
         */
        public final List<Document> deletable = new ArrayList<Document>();
        /**
         * issued invoices neither paid nor cancelled
         */
        public final List<Document> open = new ArrayList<Document>();
        /**
         * booked income of the contact which must be kept
         */
        public int revenues;
        /**
         * the end of the last retention period, null if nothing must be kept
         */
        public Date keepUntil;

        /**
         * @return true if records of the contact must be kept: the contact
         * can not be deleted, only blocked
         */
        public boolean mustKeep() {
            return keepUntil != null;
        }

        /**
         * @param today
         * @return true if all retention periods are over
         */
        public boolean expired(Date today) {
            return keepUntil == null || keepUntil.before(DateConverter.getStartOfDay(today));
        }
    }

    /**
     * The retention period of a document
     *
     * @param documentDate the date of the document (or booking)
     * @param years
     * @return the last day it must be kept: December 31st of the year
     * documentDate + years
     */
    public static Date keepUntil(Date documentDate, int years) {
        Calendar c = Calendar.getInstance();
        c.setTime(documentDate == null ? new Date() : documentDate);
        c.set(c.get(Calendar.YEAR) + years, Calendar.DECEMBER, 31, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * @param type the kind of document
     * @param issued true if it left the house (finished, paid, cancelled, a
     * file was written or a payment booked)
     * @param referenced true if another document refers to it (an accepted
     * offer)
     * @return the years it must be kept, 0: it may be deleted
     */
    public static int retentionYears(int type, boolean issued, boolean referenced) {
        for (int t : INVOICE_TYPES) {
            if (t == type) {
                //a draft invoice which never left the house is no invoice
                return issued ? YEARS_BOOKING_RECORDS : 0;
            }
        }
        for (int t : LETTER_TYPES) {
            if (t == type) {
                return YEARS_BUSINESS_LETTERS;
            }
        }
        if (type == Item.TYPE_OFFER) {
            return referenced ? YEARS_BUSINESS_LETTERS : 0;
        }
        return YEARS_BUSINESS_LETTERS;
    }

    /**
     * @param type
     * @param status
     * @param issued
     * @return true for an issued invoice which is neither paid nor cancelled
     */
    public static boolean isOpen(int type, int status, boolean issued) {
        return issued && (type == Item.TYPE_INVOICE || type == Item.TYPE_PART_PAYMENT || type == Item.TYPE_DEPOSIT)
                && status != Item.STATUS_PAID && status != Item.STATUS_CANCELLED;
    }

    /**
     * @param status
     * @param hasFile a file (PDF, e-invoice) was written for it
     * @param hasPayment a payment was booked for it
     * @return true if the document left the house
     */
    public static boolean isIssued(int status, boolean hasFile, boolean hasPayment) {
        return status == Item.STATUS_FINISHED || status == Item.STATUS_PAID || status == Item.STATUS_CANCELLED
                || hasFile || hasPayment;
    }

    /**
     * @param o a record
     * @return true for a saved invoice, credit note, deposit or part payment
     * invoice which was issued (finished, paid, cancelled, a file written for
     * it or a payment): deleting it warns (#213)
     */
    public static boolean isIssuedInvoice(mpv5.db.common.DatabaseObject o) {
        if (!(o instanceof Item) || !o.isExisting()) {
            return false;
        }
        Item i = (Item) o;
        boolean invoice = false;
        for (int t : INVOICE_TYPES) {
            invoice |= t == i.__getInttype();
        }
        return invoice && isIssued(i.__getIntstatus(),
                !ids("SELECT itemsids FROM item_files WHERE itemsids = " + i.__getIDS()).isEmpty(),
                !ids("SELECT itemsids FROM payments WHERE itemsids = " + i.__getIDS()).isEmpty());
    }

    /**
     * What the law says about the data of a contact (including documents in
     * the trash)
     *
     * @param c
     * @return
     */
    public static Report check(Contact c) {
        Report r = new Report();
        int id = c.__getIDS();
        Set<Integer> withFile = ids("SELECT DISTINCT itemsids FROM item_files WHERE itemsids IN (SELECT ids FROM items WHERE contactsids = " + id + ")");
        Set<Integer> withPayment = ids("SELECT DISTINCT itemsids FROM payments WHERE itemsids IN (SELECT ids FROM items WHERE contactsids = " + id + ")");
        Set<Integer> referenced = ids("SELECT DISTINCT reforderids FROM items WHERE reforderids IS NOT NULL");
        for (Object[] row : rows("SELECT ids, inttype, cnumber, dateadded, intstatus, netvalue, discountvalue, taxvalue FROM items WHERE contactsids = " + id)) {
            int ids = toInt(row[0]);
            int type = toInt(row[1]);
            int status = toInt(row[4]);
            Date date = toDate(row[3]);
            boolean issued = isIssued(status, withFile.contains(ids), withPayment.contains(ids));
            int years = retentionYears(type, issued, referenced.contains(ids));
            Date keep = years > 0 ? keepUntil(date, years) : null;
            boolean open = isOpen(type, status, issued);
            Document d = new Document(ids, type, row[2] == null ? "" : String.valueOf(row[2]), date, status,
                    money(row[5]).subtract(money(row[6])).add(money(row[7])), keep, open);
            (keep != null ? r.retained : r.deletable).add(d);
            if (open) {
                r.open.add(d);
            }
            later(r, keep);
        }
        //income booked for the contact: booking records
        for (Object[] row : rows("SELECT dateadded FROM revenues WHERE contactsids = " + id)) {
            r.revenues++;
            later(r, keepUntil(toDate(row[0]), YEARS_BOOKING_RECORDS));
        }
        //activity lists and purchase orders: business documents
        for (String table : new String[]{"activitylists", "productorders"}) {
            for (Object[] row : rows("SELECT dateadded FROM " + table + " WHERE contactsids = " + id)) {
                later(r, keepUntil(toDate(row[0]), YEARS_BUSINESS_LETTERS));
            }
        }
        mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
        if (em != null) {
            for (mpv5.plugin.api.PrivacyContributor contributor : em.privacyContributors()) {
                try {
                    later(r, contributor.retentionUntil(c));
                } catch (Throwable t) {
                    Log.Debug(Privacy.class, t);
                }
            }
        }
        return r;
    }

    private static void later(Report r, Date keep) {
        if (keep != null && (r.keepUntil == null || keep.after(r.keepUntil))) {
            r.keepUntil = keep;
        }
    }

    /**
     * @param c
     * @return true if the contact is blocked (restriction of processing)
     */
    public static boolean isBlocked(Contact c) {
        if (c == null || !c.isExisting()) {
            return false;
        }
        return rows("SELECT ids FROM valueproperties WHERE cname = '" + KEY_BLOCKED + "' AND objectids = " + c.__getIDS()
                + " AND contextids IN (" + contextIds("contacts") + ")").length > 0;
    }

    /**
     * @param table e.g. "contacts", "items"
     * @return the ids of all contexts of the table, e.g. contact, customer,
     * supplier, manufacturer (properties are stored with any of them)
     */
    public static String contextIds(String table) {
        StringBuilder b = new StringBuilder();
        for (Context c : Context.getContexts()) {
            if (table.equalsIgnoreCase(c.getDbIdentity())) {
                b.append(b.length() == 0 ? "" : ",").append(c.getId());
            }
        }
        return b.length() == 0 ? "-1" : b.toString();
    }

    /**
     * @param c
     * @return the contact with the contact context, which the blocking is
     * stored with
     */
    private static Contact canonical(Contact c) {
        try {
            return c.getContext().getId() == Context.getContact().getId() ? c
                    : (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
        } catch (Exception e) {
            return c;
        }
    }

    /**
     * @param c a blocked contact
     * @return until when its documents must be kept, null if unknown
     */
    public static Date blockedUntil(Contact c) {
        try {
            ValueProperty p = ValueProperty.getProperty(canonical(c), KEY_KEEP_UNTIL);
            return p == null ? null : new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(p.getValue()));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * What blocking removes, for the confirmation
     */
    public static final String[] CLEARED_FIELDS = {"mainphone", "workphone", "mobilephone", "fax", "mailaddress",
        "website", "notes", "bankaccount", "bankid", "bankname", "bankcurrency", "bankcountry"};

    /**
     * Erasure as far as the law allows (Art. 17 Abs. 3 lit. b, Art. 18 GDPR):
     * deletes everything about the person which is not needed for the
     * documents that must be kept and blocks the contact until the end of the
     * last retention period. The documents and the name and addresses of the
     * contact (which the documents show) are not changed.
     * <ul>
     * <li>cleared: phone, fax, email, website, notes and bank details of the
     * contact</li>
     * <li>deleted: offers not accepted, draft invoices, conversations, files
     * of the contact, appointments, web shop links, own fields (except those
     * used to print the kept documents, {@link #KEPT_PROPERTIES})</li>
     * </ul>
     *
     * @param c
     * @return the report the blocking was based on
     * @throws IllegalStateException if open invoices exist or nothing must be
     * kept (then the contact can simply be deleted)
     * @throws Exception if the database refused a change
     */
    public static Report block(Contact c) throws Exception {
        Report r = check(c);
        if (!r.open.isEmpty()) {
            throw new IllegalStateException(Messages.PRIVACY_OPEN_INVOICES + ": " + r.open);
        }
        if (!r.mustKeep()) {
            throw new IllegalStateException(Messages.PRIVACY_NOTHING_TO_KEEP.toString());
        }
        int id = c.__getIDS();
        //documents which need not be kept: deleted for good (positions, payments, reminders cascade)
        for (Document d : r.deletable) {
            deleteItem(d);
        }
        //everything else about the person
        update("DELETE FROM conversations WHERE contactsids = " + id);
        for (Object[] row : rows("SELECT filename FROM filestocontacts WHERE contactsids = " + id)) {
            update("DELETE FROM filestocontacts WHERE filename = '" + row[0] + "'");
            update("DELETE FROM files WHERE cname = '" + row[0] + "'");
        }
        update("DELETE FROM schedule WHERE contactsids = " + id);
        update("DELETE FROM wscontactsmapping WHERE contactsids = " + id);
        StringBuilder kept = new StringBuilder();
        for (String k : KEPT_PROPERTIES) {
            kept.append(kept.length() == 0 ? "" : ",").append('\'').append(k).append('\'');
        }
        update("DELETE FROM valueproperties WHERE objectids = " + id + " AND contextids IN (" + contextIds("contacts")
                + ") AND cname NOT IN (" + kept + ")");
        c.setMainphone("");
        c.setWorkphone("");
        c.setMobilephone("");
        c.setFax("");
        c.setMailaddress("");
        c.setWebsite("");
        c.setNotes("");
        c.setBankaccount("");
        c.setBankid("");
        c.setBankname("");
        c.setBankcurrency("");
        c.setBankcountry("");
        c.setisEnabled(false);
        if (!c.save(true)) {
            throw new IllegalStateException(Messages.NOT_POSSIBLE + ": " + c.__getCname());
        }
        String until = new SimpleDateFormat("yyyy-MM-dd").format(r.keepUntil);
        Contact own = canonical(c);
        ValueProperty.addOrUpdateProperty(KEY_BLOCKED, new SimpleDateFormat("yyyy-MM-dd").format(new Date()), own);
        ValueProperty.addOrUpdateProperty(KEY_KEEP_UNTIL, until, own);
        Activity.record(c, Activity.Kind.WARNING, String.format(Messages.PRIVACY_BLOCKED_LOG.toString(),
                r.deletable.size(), DateConverter.getDefDateString(r.keepUntil)));
        Log.Debug(Privacy.class, "Contact " + id + " blocked until " + until);
        return r;
    }

    /**
     * Deletes a contact with all its documents for good - only when nothing
     * must be kept any more (all retention periods are over) and no invoice
     * is open
     *
     * @param c
     * @return false if records must still be kept
     */
    public static boolean deleteFinally(Contact c) {
        Report r = check(c);
        if (!r.expired(new Date()) || !r.open.isEmpty()) {
            return false;
        }
        int id = c.__getIDS();
        for (Document d : r.retained) {
            deleteItem(d);
        }
        for (Document d : r.deletable) {
            deleteItem(d);
        }
        for (Object[] row : rows("SELECT filename FROM filestocontacts WHERE contactsids = " + id)) {
            update("DELETE FROM filestocontacts WHERE filename = '" + row[0] + "'");
            update("DELETE FROM files WHERE cname = '" + row[0] + "'");
        }
        update("DELETE FROM valueproperties WHERE contextids IN (" + contextIds("contacts") + ") AND objectids = " + id);
        update("DELETE FROM addresses WHERE contactsids = " + id);
        mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
        if (em != null) {
            for (mpv5.plugin.api.PrivacyContributor contributor : em.privacyContributors()) {
                try {
                    contributor.onDeleteFinally(c);
                } catch (Throwable t) {
                    Log.Debug(Privacy.class, t);
                }
            }
        }
        //the rest (income, conversations, appointments, ...) by the database (ON DELETE CASCADE)
        update("DELETE FROM contacts WHERE ids = " + id);
        Activity.record(c, Activity.Kind.DELETED, String.format(Messages.PRIVACY_DELETED_LOG.toString(), c.__getCNumber()));
        return true;
    }

    private static void deleteItem(Document d) {
        update("DELETE FROM valueproperties WHERE contextids IN (" + contextIds("items") + ") AND objectids = " + d.id);
        update("DELETE FROM items WHERE ids = " + d.id);
    }

    /**
     * Whether a record in the trash may be deleted for good
     *
     * @param c the context of the record
     * @param id
     * @return null if it may, else why not (the end of the retention period)
     */
    public static String whyKeep(Context c, int id) {
        try {
            if (c.equals(Context.getContact())) {
                Contact contact = (Contact) DatabaseObject.getObject(Context.getContact(), id, true);
                Report r = check(contact);
                return r.expired(new Date()) ? null : String.format(Messages.PRIVACY_KEEP_CONTACT.toString(),
                        contact.__getCname(), DateConverter.getDefDateString(r.keepUntil));
            }
            for (Object[] row : rows("SELECT ids, inttype, cnumber, dateadded, intstatus, reforderids FROM items WHERE ids = " + id)) {
                int type = toInt(row[1]);
                boolean issued = isIssued(toInt(row[4]), !ids("SELECT itemsids FROM item_files WHERE itemsids = " + id).isEmpty(),
                        !ids("SELECT itemsids FROM payments WHERE itemsids = " + id).isEmpty());
                boolean referenced = !ids("SELECT ids FROM items WHERE reforderids = " + id).isEmpty();
                boolean invoice = false;
                for (int t : INVOICE_TYPES) {
                    invoice |= t == type;
                }
                //only invoices: other documents may be deleted by the user as before
                if (!invoice) {
                    return null;
                }
                int years = retentionYears(type, issued, referenced);
                Date keep = years > 0 ? keepUntil(toDate(row[3]), years) : null;
                if (keep != null && !keep.before(DateConverter.getStartOfDay(new Date()))) {
                    return String.format(Messages.PRIVACY_KEEP_DOCUMENT.toString(), row[2], DateConverter.getDefDateString(keep));
                }
            }
        } catch (Exception e) {
            Log.Debug(Privacy.class, e);
        }
        return null;
    }

    /**
     * The blocked contacts whose retention period is over: they can be
     * deleted now
     *
     * @return the contacts, may be empty
     */
    public static List<Contact> expired() {
        List<Contact> l = new ArrayList<Contact>();
        Date today = DateConverter.getStartOfDay(new Date());
        for (Object[] row : rows("SELECT objectids FROM valueproperties WHERE cname = '" + KEY_BLOCKED
                + "' AND contextids = " + Context.getContact().getId())) {
            try {
                Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), toInt(row[0]), true);
                Date until = blockedUntil(c);
                if (until != null && until.before(today) && check(c).expired(today)) {
                    l.add(c);
                }
            } catch (Exception e) {
                Log.Debug(Privacy.class, e);
            }
        }
        return l;
    }

    /**
     * Writes everything stored about the person (Art. 15, 20 GDPR) to a ZIP
     * file: one JSON file per kind of data, the files of the contact and the
     * files written for its documents (if they still exist)
     *
     * @param c
     * @param zip the target
     * @return the target
     * @throws Exception
     */
    public static File export(Contact c, File zip) throws Exception {
        int id = c.__getIDS();
        String items = "SELECT ids FROM items WHERE contactsids = " + id;
        Map<String, String> queries = new LinkedHashMap<String, String>();
        queries.put("contact", "SELECT * FROM contacts WHERE ids = " + id);
        queries.put("addresses", "SELECT * FROM addresses WHERE contactsids = " + id);
        queries.put("documents", "SELECT * FROM items WHERE contactsids = " + id + " ORDER BY dateadded, ids");
        queries.put("positions", "SELECT * FROM subitems WHERE itemsids IN (" + items + ") ORDER BY itemsids, ordernr");
        queries.put("payments", "SELECT * FROM payments WHERE itemsids IN (" + items + ")");
        queries.put("reminders", "SELECT * FROM reminders WHERE itemsids IN (" + items + ")");
        queries.put("income", "SELECT * FROM revenues WHERE contactsids = " + id);
        queries.put("conversations", "SELECT * FROM conversations WHERE contactsids = " + id);
        queries.put("activity_lists", "SELECT * FROM activitylists WHERE contactsids = " + id);
        queries.put("purchase_orders", "SELECT * FROM productorders WHERE contactsids = " + id);
        queries.put("appointments", "SELECT * FROM schedule WHERE contactsids = " + id);
        queries.put("files_of_contact", "SELECT ids, cname, description, filename, intsize, mimetype, dateadded FROM filestocontacts WHERE contactsids = " + id);
        queries.put("document_files", "SELECT * FROM item_files WHERE itemsids IN (" + items + ")");
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip))) {
            put(out, "README.txt", String.format(Messages.PRIVACY_EXPORT_README.toString(), c.__getCname(),
                    DateConverter.getDefDateString(new Date())));
            for (Map.Entry<String, String> q : queries.entrySet()) {
                put(out, q.getKey() + ".json", json(q.getValue()));
            }
            //own fields of the contact
            List<Map<String, Object>> props = new ArrayList<Map<String, Object>>();
            try {
                for (ValueProperty p : ValueProperty.getProperties(canonical(c))) {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("name", p.getKey());
                    m.put("value", String.valueOf(p.getValue()));
                    props.add(m);
                }
            } catch (Exception e) {
                Log.Debug(Privacy.class, e);
            }
            put(out, "own_fields.json", MAPPER.writeValueAsString(props));
            //the files of the contact (stored in the database)
            for (Object[] row : rows("SELECT filename, cname FROM filestocontacts WHERE contactsids = " + id)) {
                try {
                    File f = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(String.valueOf(row[0]));
                    if (f != null && f.isFile()) {
                        put(out, "files/" + row[0] + "_" + safe(String.valueOf(row[1])), f);
                    }
                } catch (Exception e) {
                    Log.Debug(Privacy.class, e);
                }
            }
            //the files written for the documents (PDF, e-invoice), if they are still there
            for (Object[] row : rows("SELECT filepath, filename FROM item_files WHERE itemsids IN (" + items + ")")) {
                File f = new File(String.valueOf(row[0]));
                if (f.isFile()) {
                    put(out, "documents/" + safe(String.valueOf(row[1])), f);
                }
            }
            //additional personal data from running plugins (e.g. Heilpraktiker patient data)
            mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
            if (em != null) {
                for (mpv5.plugin.api.PrivacyContributor contributor : em.privacyContributors()) {
                    try {
                        Map<String, String> extra = contributor.exportData(c);
                        if (extra != null) {
                            for (Map.Entry<String, String> entry : extra.entrySet()) {
                                if (entry.getKey() != null && entry.getValue() != null) {
                                    put(out, safe(entry.getKey()), entry.getValue());
                                }
                            }
                        }
                    } catch (Throwable t) {
                        Log.Debug(Privacy.class, t);
                    }
                }
            }
        }
        Activity.record(c, Activity.Kind.FILE, String.format(Messages.PRIVACY_EXPORT_LOG.toString(), zip.getName()));
        return zip;
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper()
            .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

    private static String json(String sql) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            ReturnValue rv = QueryHandler.instanceOf().clone(Context.getContact()).freeSelectQuery(sql, MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                String[] names = rv.getColumnnames();
                for (Object[] row : rv.getData()) {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    for (int i = 0; i < row.length && i < names.length; i++) {
                        Object v = row[i];
                        //dates as text, binary data (stored files) is not written here
                        if (v instanceof Date) {
                            v = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) v);
                        } else if (v instanceof byte[] || v instanceof java.sql.Blob) {
                            v = "(binary)";
                        } else if (v != null && !(v instanceof Number) && !(v instanceof Boolean)) {
                            v = String.valueOf(v);
                        }
                        m.put(names[i].toLowerCase(), v);
                    }
                    list.add(m);
                }
            }
        } catch (Exception e) {
            Log.Debug(Privacy.class, e);
        }
        return MAPPER.writeValueAsString(list);
    }

    private static void put(ZipOutputStream out, String name, String text) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
    }

    private static void put(ZipOutputStream out, String name, File f) throws Exception {
        out.putNextEntry(new ZipEntry(name));
        try (InputStream in = new FileInputStream(f)) {
            in.transferTo(out);
        }
        out.closeEntry();
    }

    private static String safe(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static Context contextOf(int type) {
        switch (type) {
            case Item.TYPE_OFFER:
                return Context.getOffer();
            case Item.TYPE_ORDER:
                return Context.getOrder();
            case Item.TYPE_DEPOSIT:
                return Context.getDeposit();
            case Item.TYPE_PART_PAYMENT:
                return Context.getPartPayment();
            case Item.TYPE_CREDIT:
                return Context.getCredit();
            default:
                return Context.getInvoice();
        }
    }

    private static Object[][] rows(String sql) {
        try {
            ReturnValue rv = QueryHandler.instanceOf().clone(Context.getContact()).freeSelectQuery(sql, MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                return rv.getData();
            }
        } catch (Exception e) {
            Log.Debug(Privacy.class, e);
        }
        return new Object[0][0];
    }

    private static Set<Integer> ids(String sql) {
        Set<Integer> s = new HashSet<Integer>();
        for (Object[] row : rows(sql)) {
            if (row[0] != null) {
                s.add(toInt(row[0]));
            }
        }
        return s;
    }

    private static void update(String sql) {
        ReturnValue rv = QueryHandler.instanceOf().clone(Context.getContact()).freeUpdateQuery(sql, MPSecurityManager.CREATE_OR_DELETE, null);
        if (rv == null) {
            throw new IllegalStateException(Messages.NOT_POSSIBLE + ": " + sql);
        }
    }

    private static int toInt(Object o) {
        try {
            return (int) Double.parseDouble(String.valueOf(o).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static BigDecimal money(Object o) {
        try {
            return o == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(o).trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    static Date toDate(Object o) {
        if (o instanceof Date) {
            return (Date) o;
        }
        if (o == null) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(o).trim().substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }
}
