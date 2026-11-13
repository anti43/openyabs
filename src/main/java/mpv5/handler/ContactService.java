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
package mpv5.handler;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPTableModel;

/**
 * The non-visual logic of a contact view: creating documents for a contact,
 * loading the records related to it, its files and properties.
 * <p>
 * Contains no Swing code, so every contact view (classic or modern) can use
 * it and it can be tested without a GUI.
 */
public final class ContactService {

    private ContactService() {
    }

    /**
     * Records which can be listed for a contact. The first column of every
     * model is the id of the record.
     */
    public enum Related {

        OFFERS, ORDERS, INVOICES, CREDITS, ACTIVITIES, PRODUCTS;

        /**
         * @return the context of the listed records, used to open one of them
         */
        public Context getContext() {
            switch (this) {
                case OFFERS:
                    return Context.getOffer();
                case ORDERS:
                    return Context.getOrder();
                case INVOICES:
                    return Context.getInvoice();
                case CREDITS:
                    return Context.getCredit();
                case ACTIVITIES:
                    return Context.getActivityList();
                default:
                    return Context.getProduct();
            }
        }

        /**
         * @return true for offers, orders, invoices and credit notes
         */
        public boolean isItem() {
            return this == OFFERS || this == ORDERS || this == INVOICES || this == CREDITS;
        }

        /**
         * @return true if this kind of record exists for customers, false if
         * for suppliers and manufacturers
         */
        public boolean isForCustomers() {
            return this != PRODUCTS;
        }
    }

    /**
     * Loads the records of the given kind which belong to the contact. Offers,
     * orders, invoices, credit notes and activity lists get a last column
     * "Details": their positions as one line (see
     * {@link #detailLine(List)}).
     *
     * @param contact
     * @param related
     * @return a table model, the first column holds the ids
     */
    public static MPTableModel loadRelated(Contact contact, Related related) {
        if (related.isItem()) {
            Object[][] data = new DatabaseSearch(related.getContext())
                    .getValuesFor(Context.DEFAULT_ITEM_SEARCH, "contactsids", contact.__getIDS());
            MPTableModel mod = new MPTableModel(withDetails(related, data), withDetailsHeader(Headers.ITEM_DEFAULT.getValue()));
            mod.setTypes(new Class[]{Integer.class, Object.class, Date.class, Double.class, String.class});
            return mod;
        } else if (related == Related.ACTIVITIES) {
            Object[][] data = new DatabaseSearch(related.getContext(), 1000)
                    .getValuesFor(Context.DEFAULT_ACTIVITYLIST_SEARCH, new String[]{"contactsids"}, contact.__getIDS());
            return new MPTableModel(withDetails(related, data), withDetailsHeader(Headers.ACTIVITY_DEFAULT.getValue()));
        } else {
            Object[][] data = new DatabaseSearch(related.getContext(), 1000)
                    .getValuesFor(Context.DEFAULT_PRODUCT_SEARCH, new String[]{"manufacturersids", "suppliersids"}, contact.__getIDS());
            return new MPTableModel(data, Headers.PRODUCT_DEFAULT);
        }
    }

    /**
     * Maximal length of the details line of a record
     */
    public static final int MAX_DETAIL_LENGTH = 300;
    private static final int MAX_POSITION_LENGTH = 80;
    private static final int IDS_PER_QUERY = 500;

    private static String[] withDetailsHeader(String[] header) {
        String[] h = Arrays.copyOf(header, header.length + 1);
        h[header.length] = Messages.CONTACT_DETAILS.toString();
        return h;
    }

    /**
     * @return the data with the details line of each record as last column
     */
    private static Object[][] withDetails(Related related, Object[][] data) {
        if (data == null) {
            return new Object[0][];
        }
        java.util.Map<Integer, String> details = loadDetails(related, data);
        Object[][] out = new Object[data.length][];
        for (int i = 0; i < data.length; i++) {
            Object[] row = Arrays.copyOf(data[i], data[i].length + 1);
            Integer id = toId(data[i].length > 0 ? data[i][0] : null);
            String d = id == null ? null : details.get(id);
            row[data[i].length] = d == null ? "" : d;
            out[i] = row;
        }
        return out;
    }

    private static Integer toId(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * The positions of the records (subitems of items, entries of activity
     * lists) as one line per record, with one query per 500 records
     */
    private static java.util.Map<Integer, String> loadDetails(Related related, Object[][] data) {
        java.util.Map<Integer, String> result = new java.util.HashMap<Integer, String>();
        Context context;
        String parent;
        String order;
        if (related.isItem()) {
            context = Context.getSubItem();
            parent = "itemsids";
            order = "itemsids, ids";
        } else if (related == Related.ACTIVITIES) {
            context = Context.getActivityListItems();
            parent = "activitylistsids";
            order = "activitylistsids, datedoing, ids";
        } else {
            return result;
        }
        List<Integer> ids = new java.util.ArrayList<Integer>();
        for (Object[] row : data) {
            Integer id = row.length > 0 ? toId(row[0]) : null;
            if (id != null) {
                ids.add(id);
            }
        }
        for (int start = 0; start < ids.size(); start += IDS_PER_QUERY) {
            List<Integer> chunk = ids.subList(start, Math.min(ids.size(), start + IDS_PER_QUERY));
            StringBuilder in = new StringBuilder();
            for (Integer id : chunk) {
                in.append(in.length() == 0 ? "" : ",").append(id);
            }
            try {
                Object[][] positions = QueryHandler.instanceOf().clone(context).freeSelectQuery(
                        "SELECT " + parent + ", quantityvalue, measure, description, cname FROM %%tablename%%"
                        + " WHERE " + parent + " IN (" + in + ") AND invisible = 0 ORDER BY " + order,
                        mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
                java.util.Map<Integer, List<Object[]>> byParent = new java.util.LinkedHashMap<Integer, List<Object[]>>();
                for (Object[] p : positions == null ? new Object[0][] : positions) {
                    Integer id = toId(p[0]);
                    if (id != null) {
                        byParent.computeIfAbsent(id, k -> new java.util.ArrayList<Object[]>())
                                .add(Arrays.copyOfRange(p, 1, p.length));
                    }
                }
                for (java.util.Map.Entry<Integer, List<Object[]>> e : byParent.entrySet()) {
                    result.put(e.getKey(), detailLine(e.getValue()));
                }
            } catch (Exception e) {
                //the details are a help only, the list works without them
                Log.Debug(ContactService.class, e);
            }
        }
        return result;
    }

    /**
     * The positions of a record as one line, e.g. "2 h Consulting · 1 Website
     * relaunch · ...", at most {@link #MAX_DETAIL_LENGTH} characters.
     *
     * @param positions per position: quantity, measure, description, name
     * @return the line, "" if there are no positions
     */
    public static String detailLine(List<Object[]> positions) {
        StringBuilder b = new StringBuilder();
        for (Object[] p : positions) {
            String text = plain(p.length > 2 ? p[2] : null);
            if (text.isEmpty()) {
                text = plain(p.length > 3 ? p[3] : null);
            }
            if (text.isEmpty()) {
                continue;
            }
            String part = shorten(quantity(p.length > 0 ? p[0] : null, p.length > 1 ? p[1] : null) + text, MAX_POSITION_LENGTH);
            if (b.length() > 0) {
                b.append(" \u00b7 ");
            }
            b.append(part);
            if (b.length() >= MAX_DETAIL_LENGTH) {
                break;
            }
        }
        return shorten(b.toString(), MAX_DETAIL_LENGTH);
    }

    /**
     * @return "2 h " for quantity 2 and measure "h", "" for quantity 0 or 1
     * without measure
     */
    private static String quantity(Object quantity, Object measure) {
        double q;
        try {
            q = quantity == null ? 0 : Double.parseDouble(String.valueOf(quantity));
        } catch (NumberFormatException e) {
            q = 0;
        }
        String m = plain(measure);
        if (q == 0 || (q == 1 && m.isEmpty())) {
            return "";
        }
        String n;
        if (q == Math.rint(q) && Math.abs(q) < 1e12) {
            n = String.valueOf((long) q);
        } else {
            java.text.NumberFormat f = java.text.NumberFormat.getNumberInstance();
            f.setMaximumFractionDigits(2);
            n = f.format(q);
        }
        return n + (m.isEmpty() ? "" : " " + m) + " ";
    }

    /**
     * @return the text without HTML tags, line breaks and repeated spaces
     */
    static String plain(Object o) {
        if (o == null) {
            return "";
        }
        String s = String.valueOf(o);
        if (s.equalsIgnoreCase("null")) {
            return "";
        }
        s = s.replaceAll("(?s)<[^>]*>", " ")
                .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&amp;", "&");
        return s.replaceAll("\\s+", " ").trim();
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1).trim() + "\u2026";
    }

    /**
     * The kind of records shown first when a contact is opened
     *
     * @param contact
     * @return null if there is nothing to show
     */
    public static Related defaultRelated(Contact contact) {
        if (!contact.isExisting()) {
            return null;
        }
        if (contact.__getIscustomer()) {
            return Related.INVOICES;
        } else if (contact.__getIsmanufacturer() || contact.__getIssupplier()) {
            return Related.PRODUCTS;
        }
        return null;
    }

    /**
     * Creates (does not save) a new offer, order, invoice, deposit, part
     * payment or credit note for the contact
     *
     * @param contact
     * @param itemType one of Item.TYPE_*
     * @return the new item, or null if the contact is not saved yet or the
     * type is not supported
     */
    public static Item newItem(Contact contact, int itemType) {
        if (contact == null || !contact.isExisting()) {
            return null;
        }
        Context context;
        Messages name;
        switch (itemType) {
            case Item.TYPE_OFFER:
                context = Context.getOffer();
                name = Messages.NEW_OFFER;
                break;
            case Item.TYPE_ORDER:
                context = Context.getOrder();
                name = Messages.NEW_ORDER;
                break;
            case Item.TYPE_INVOICE:
                context = Context.getInvoice();
                name = Messages.NEW_INVOICE;
                break;
            case Item.TYPE_PART_PAYMENT:
                context = Context.getPartPayment();
                name = Messages.NEW_PART_PAYMENT;
                break;
            case Item.TYPE_DEPOSIT:
                context = Context.getDeposit();
                name = Messages.NEW_DEPOSIT;
                break;
            case Item.TYPE_CREDIT:
                context = Context.getCredit();
                name = Messages.NEW_CREDIT;
                break;
            default:
                return null;
        }
        Item i = (Item) DatabaseObject.getObject(context);
        i.setCname(name.getValue());
        i.setInttype(itemType);
        i.setContactsids(contact.__getIDS());
        i.setDateadded(new Date());
        i.setGroupsids(contact.__getGroupsids());
        return i;
    }

    /**
     * Creates (does not save) a new product supplied and manufactured by the
     * contact
     *
     * @param contact
     * @return null if the contact is not saved yet
     */
    public static Product newProduct(Contact contact) {
        if (contact == null || !contact.isExisting()) {
            return null;
        }
        Product p = (Product) DatabaseObject.getObject(Context.getProduct());
        p.setSuppliersids(contact.__getIDS());
        p.setManufacturersids(contact.__getIDS());
        p.setCname(Messages.NEW_PRODUCT.getValue());
        p.setInttype(Product.TYPE_PRODUCT);
        p.setGroupsids(contact.__getGroupsids());
        return p;
    }

    /**
     * Loads one of the related records
     *
     * @param related
     * @param id
     * @return the record, or null if it does not exist (any more)
     */
    public static DatabaseObject getRelated(Related related, int id) {
        try {
            return DatabaseObject.getObject(related.getContext(), id);
        } catch (NodataFoundException ex) {
            Log.Debug(ContactService.class, ex);
            return null;
        }
    }

    /**
     * The additional addresses of the contact
     *
     * @param contact
     * @return never null
     */
    @SuppressWarnings("unchecked")
    public static List<Address> getAddresses(Contact contact) {
        if (contact == null || !contact.isExisting()) {
            return Collections.emptyList();
        }
        try {
            return (List<Address>) (List<?>) DatabaseObject.getReferencedObjects(contact, Context.getAddress());
        } catch (NodataFoundException ex) {
            Log.Debug(ContactService.class, ex);
            return Collections.emptyList();
        }
    }

    /**
     * Creates (does not save) a new additional address for the contact
     *
     * @param contact
     * @return the address
     */
    public static Address newAddress(Contact contact) {
        Address a = new Address();
        a.setCompany(contact.__getCompany());
        a.setTaxnumber(contact.__getTaxnumber());
        return a;
    }

    /**
     * The files of the contact: id, file name (in the database), description
     * ... as in {@link Headers#FILE_REFERENCES}
     *
     * @param contact
     * @return a table model, the first column holds the ids
     */
    public static MPTableModel loadFiles(Contact contact) {
        Context cf = Context.getFilesToContacts();
        cf.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(cf).getValuesFor(Context.DETAILS_FILES_TO_CONTACTS, "contactsids", contact.__getIDS());
        return new MPTableModel(data, Headers.FILE_REFERENCES.getValue());
    }

    /**
     * Stores a file for the contact
     *
     * @param contact must be saved
     * @param file
     * @param description
     * @param viewToBeNotified refreshed when the file is stored, may be null
     * @return false if the contact is not saved yet or storing failed
     */
    public static boolean addFile(Contact contact, File file, String description, DataPanel viewToBeNotified) {
        if (contact == null || !contact.isExisting() || file == null) {
            return false;
        }
        QueryHandler q = viewToBeNotified == null
                ? QueryHandler.instanceOf().clone(Context.getFiles())
                : QueryHandler.instanceOf().clone(Context.getFiles(), viewToBeNotified);
        return q.insertFile(file, contact, QueryCriteria.getSaveStringFor(description == null ? "" : description));
    }

    /**
     * Removes a file from the contact
     *
     * @param contact
     * @param filename the name of the file in the database (column 0 of
     * {@link #loadFiles(Contact)})
     */
    public static void deleteFile(Contact contact, String filename) {
        if (contact == null || !contact.isExisting() || filename == null) {
            return;
        }
        try {
            DatabaseObject.getObject(Context.getFilesToContacts(), "filename", filename).delete();
        } catch (Exception e) {
            Log.Debug(ContactService.class, e.getMessage());
        }
    }

    /**
     * Copies a stored file to the temp directory
     *
     * @param filename the name of the file in the database
     * @param displayName the name of the copy
     * @return the copy, to be opened
     */
    public static File retrieveFile(String filename, String displayName) {
        return QueryHandler.instanceOf().clone(Context.getFiles())
                .retrieveFile(filename, new File(FileDirectoryHandler.getTempDir() + displayName));
    }

    /**
     * The value properties of the contact, with one empty row if there are
     * none (to enter the first one)
     *
     * @param contact
     * @return a table model with the columns key and value
     */
    public static MPTableModel loadProperties(Contact contact) {
        MPTableModel m = new MPTableModel(ValueProperty.getProperties(contact));
        if (m.getDataVector().isEmpty()) {
            return new MPTableModel(Arrays.asList(new ValueProperty[]{new ValueProperty("", "", contact)}));
        }
        return m;
    }

    /**
     * Stores a changed value property: an empty value deletes it
     *
     * @param contact must be saved
     * @param key
     * @param value
     * @return true if something has been changed
     */
    public static boolean updateProperty(Contact contact, String key, String value) {
        if (contact == null || !contact.isExisting() || key == null) {
            return false;
        }
        if (value == null || value.length() == 0) {
            ValueProperty.deleteProperty(contact, key);
            return true;
        }
        String k = key.replaceAll("[^\\w]", "");
        if (k.length() == 0) {
            return false;
        }
        ValueProperty.addOrUpdateProperty(k, value, contact);
        return true;
    }

    /**
     * Loads the group with the given id
     *
     * @param id
     * @return null if not found
     */
    public static Group getGroup(Object id) {
        try {
            return (Group) DatabaseObject.getObject(Context.getGroup(), Integer.valueOf(String.valueOf(id)));
        } catch (NodataFoundException | NumberFormatException ex) {
            Log.Debug(ContactService.class, ex);
            return null;
        }
    }

    /**
     * Company and name of a contact as stored: a company without company
     * name takes the name, and the other way round.
     *
     * @param isCompany
     * @param company may be null
     * @param name may be null
     * @return {company, name}; both empty if neither was given (the contact
     * cannot be saved then)
     */
    public static String[] companyAndName(boolean isCompany, String company, String name) {
        String c = company == null ? "" : company;
        String n = name == null ? "" : name;
        if (isCompany && c.length() == 0) {
            c = n;
        } else if (isCompany && n.length() == 0) {
            n = c;
        }
        return new String[]{c, n};
    }

    /**
     * A bank account the contact can pay to
     */
    public static final class AccountOption {

        /**
         * "" for the default account of the company info
         */
        public final String iban;
        public final String label;

        AccountOption(String iban, String label) {
            this.iban = iban;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * The accounts of the company info the contact can pay to. The first
     * entry is "default"; an account which has been removed from the company
     * info meanwhile but is still set at the contact is kept, so it stays
     * visible.
     *
     * @param contact
     * @return the options, never empty
     */
    public static List<AccountOption> accountOptions(Contact contact) {
        List<AccountOption> options = new java.util.ArrayList<AccountOption>();
        List<mpv5.utils.export.BankAccount> list
                = mpv5.utils.export.BankAccount.companyAccounts(mpv5.utils.export.ZugpferdFile.companySettings());
        options.add(new AccountOption("", Messages.EINVOICE_ACCOUNT_DEFAULT
                + (list.isEmpty() ? "" : " (" + list.get(0) + ")")));
        for (int i = 1; i < list.size(); i++) {
            options.add(new AccountOption(list.get(i).getIban(), list.get(i).toString()));
        }
        String iban = contact == null ? "" : mpv5.utils.export.BankAccount.contactIban(contact);
        if (!iban.isEmpty() && selectedAccount(options, iban) == 0
                && (list.isEmpty() || !list.get(0).getIban().equals(iban))) {
            options.add(new AccountOption(iban, mpv5.utils.text.Iban.format(iban) + " - " + Messages.EINVOICE_ACCOUNT_MISSING));
        }
        return options;
    }

    /**
     * @param options from {@link #accountOptions(Contact)}
     * @param iban the account of the contact
     * @return the index of the option of the account, 0 (default) if none
     */
    public static int selectedAccount(List<AccountOption> options, String iban) {
        for (int i = 1; i < options.size(); i++) {
            if (options.get(i).iban.equals(iban)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Stores the e-invoice and payment settings of a saved contact (they are
     * value properties, so the contact must exist)
     *
     * @param contact
     * @param delivery
     * @param leitweg
     * @param accountIban "" for the default account
     * @param immediate payable immediately
     * @param skontoPercent
     * @param skontoDays
     */
    public static void saveEInvoiceSettings(Contact contact, mpv5.utils.export.EInvoiceDelivery delivery, String leitweg,
            String accountIban, boolean immediate, String skontoPercent, String skontoDays) {
        if (contact == null || !contact.isExisting()) {
            return;
        }
        try {
            mpv5.utils.export.EInvoiceDelivery.save(contact, delivery);
            mpv5.utils.export.BankAccount.saveForContact(contact, accountIban == null ? "" : accountIban);
            mpv5.utils.export.PaymentTerms.saveImmediate(contact, immediate);
            mpv5.utils.export.PaymentTerms.saveContactValue(contact, mpv5.utils.export.PaymentTerms.KEY_SKONTO_PERCENT, skontoPercent);
            mpv5.utils.export.PaymentTerms.saveContactValue(contact, mpv5.utils.export.PaymentTerms.KEY_SKONTO_DAYS, skontoDays);
            mpv5.utils.export.ContactProperty.set(contact, mpv5.utils.export.ZugpferdFile.KEY_BUYER_REFERENCE, leitweg);
        } catch (Exception e) {
            Log.Debug(ContactService.class, e);
        }
    }

    /**
     * @param contact
     * @param key one of the PaymentTerms.KEY_* keys
     * @return the value, "" if not set
     */
    public static String paymentValue(Contact contact, String key) {
        String v = mpv5.utils.export.PaymentTerms.contactValue(contact, key);
        return v == null ? "" : v;
    }

    /**
     * @param contact
     * @return the Leitweg-ID (buyer reference), "" if not set
     */
    public static String leitweg(Contact contact) {
        String v = mpv5.utils.export.ContactProperty.get(contact, mpv5.utils.export.ZugpferdFile.KEY_BUYER_REFERENCE);
        return v == null ? "" : v;
    }

    /**
     * Prints the contact sheet (template type "Contact") of the saved
     * contact, named e.g. "Kontaktdatenblatt-000001-Muster GmbH"; without
     * such a template the view itself is printed (fallback, on the Swing
     * thread)
     *
     * @param contact
     * @param fallback prints the view, may be null
     */
    public static void printSheet(final Contact contact, final Runnable fallback) {
        if (contact == null || !contact.isExisting()) {
            if (fallback != null) {
                fallback.run();
            }
            return;
        }
        //loading the template reads the database and the file: not on the Swing thread
        new Thread(() -> {
            mpv5.db.objects.Template t = TemplateHandler.loadTemplate(contact);
            if (t != null) {
                mpv5.utils.export.Export.print(t, contact);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_LOADED + ": " + Messages.CONTACT_SHEET, java.awt.Color.YELLOW);
                if (fallback != null) {
                    javax.swing.SwingUtilities.invokeLater(fallback);
                }
            }
        }, "print contact sheet").start();
    }
}
