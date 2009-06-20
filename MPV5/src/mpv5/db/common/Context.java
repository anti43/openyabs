/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.util.ArrayList;
import java.util.Arrays;
import mpv5.globals.Headers;
import mpv5.db.objects.Account;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.HistoryItem;
import mpv5.db.objects.Property;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.ContactFile;
import mpv5.db.objects.Item;
import mpv5.db.objects.ItemsList;
import mpv5.db.objects.Message;
import mpv5.db.objects.SubItem;
import mpv5.pluginhandling.Plugin;
import mpv5.db.objects.User;
import mpv5.pluginhandling.UserPlugin;
import mpv5.ui.frames.MPV5View;

/**
 *
 * Context controls Database Queries for all native MP {@link DatabaseObject}s
 */
public class Context {

    //********** tablenames ****************************************************
    public static String IDENTITY_CONTACTS = "contacts";
    public static String IDENTITY_USERS = "users";
    public static String IDENTITY_ITEMS = "items";
    public static String IDENTITY_SUBITEMS = "subitems";
    public static String IDENTITY_LANGUAGES = "languages";
    public static String IDENTITY_COUNTRIES = "countries";
    public static String IDENTITY_PRODUCTS = "products";
    public static String IDENTITY_FILES = "files";
    public static String IDENTITY_LOCK = "tablelock";
    public static String IDENTITY_FAVS = "favourites";
    public static String IDENTITY_ADDRESS = "addresses";
    public static String IDENTITY_GROUPS = "groups";
//    public static String IDENTITY_GROUPS_TO_PARENTGROUP = "groupstoparents";
    public static String IDENTITY_SCHEDULE = "schedule";
    public static String IDENTITY_HISTORY = "history";
    public static String IDENTITY_FILES_TO_CONTACTS = "filestocontacts";
    public static String IDENTITY_SEARCHINDEX = "searchindex";
    public static String IDENTITY_PLUGINS_TO_USERS = "pluginstousers";
    public static String IDENTITY_PLUGINS = "plugins";
    public static String IDENTITY_PROPERTIES_TO_USERS = "userproperties";
    public static String IDENTITY_ACCOUNTS = "accounts";
    public static String IDENTITY_ITEMS_TO_ACCOUNTS = "itemstoaccounts";
    public static String IDENTITY_MESSAGES = "messages";
    public static String IDENTITY_MESSAGES_TO_ITEMS = "messagestoitems";
    public static String IDENTITY_ITEMSLIST = "itemslists";
    public static String IDENTITY_FORMATS_T_USERS = "formatstousers";

    //********** identity classes **********************************************
    private static Class IDENTITY_CONTACTS_CLASS = Contact.class;
    private static Class IDENTITY_ADDRESS_CLASS = Address.class;
    private static Class IDENTITY_USERS_CLASS = User.class;
    private static Class IDENTITY_ITEMS_CLASS = Item.class;
    private static Class IDENTITY_CONTACTS_FILES_CLASS = ContactFile.class;
    private static Class HISTORY_ITEMS_CLASS = HistoryItem.class;
    private static Class IDENTITY_SUBITEMS_CLASS = SubItem.class;
    private static Class IDENTITY_USER_PLUGINS_CLASS = UserPlugin.class;
    private static Class IDENTITY_PLUGINS_CLASS = Plugin.class;
    private static Class IDENTITY_PROPERTIES_CLASS = Property.class;
    private static Class IDENTITY_ACCOUNTS_CLASS = Account.class;
    private static Class IDENTITY_MESSAGES_CLASS = Message.class;
    private static Class IDENTITY_ITEMSLIST_CLASS = ItemsList.class;

    //********** unique constraints *******************************************
    public static String UNIQUECOLUMNS_USER = "cname";
    public static String UNIQUECOLUMNS_ITEMS = "cname";
    public static String UNIQUECOLUMNS_GROUPS = "cname";
    private static String UNIQUECOLUMNS_DEFAULT = "cname";
    public static String DETAIL_CONTACT_SEARCH = "prename,cname,street,city,country,notes";

    //********** conditions ****************************************************
    private boolean isCompany = false;
    private boolean isCustomer = false;
    private boolean isManufacturer = false;
    private boolean isSupplier = false;
    private Integer itemStatus = null;
    private Integer itemType = null;
    public static final String CONDITION_DEFAULT = "%%tablename%%" + "." + "IDS>0";
    public static final String CONDITION_CONTACTS_COMPANY = IDENTITY_CONTACTS + "." + "iscompany";
    public static final String CONDITION_CONTACTS_CUSTOMER = IDENTITY_CONTACTS + "." + "iscustomer";
    public static final String CONDITION_CONTACTS_MANUFACTURER = IDENTITY_CONTACTS + "." + "ismanufacturer";
    public static final String CONDITION_CONTACTS_SUPPLIER = IDENTITY_CONTACTS + "." + "issupplier";
    public static final String CONDITION_ITEMS_TYPE = IDENTITY_ITEMS + "." + "inttype";
    public static final String CONDITION_ITEMS_STATUS = IDENTITY_ITEMS + "." + "intstatus";

    //********** searchfields **************************************************
    public static final String SEARCH_NAME = "cname";
    public static final String SEARCH_CONTACT_NUMBER = "cnumber";
    public static final String SEARCH_CONTACT_CITY = "city";
    //********** defaults ******************************************************
    public static String DEFAULT_SUBID = "ids, cname";
    public static String DEFAULT_CONTACT_SEARCH = "ids, cnumber, cname, city";
    public static String DEFAULT_USER_SEARCH = "ids, cname, mail, lastlogdate";
    public static String DEFAULT_ITEM_SEARCH = "ids, cname, dateadded, netvalue, taxvalue";
    public static String DEFAULT_PRODUCT_SEARCH = "ids, cname, cnumber, description";

    //********** table fields ********************************************************
    public static String DETAILS_CONTACTS = IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER," +
            IDENTITY_CONTACTS + "." + "TITLE," + IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME," +
            IDENTITY_CONTACTS + "." + "STREET," + IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY," +
            IDENTITY_CONTACTS + "." + "MAINPHONE," + IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE," +
            IDENTITY_CONTACTS + "." + "WORKPHONE," + IDENTITY_CONTACTS + "." + "COMPANY," + IDENTITY_CONTACTS + "." + "MAILADDRESS," +
            IDENTITY_CONTACTS + "." + "WEBSITE," + IDENTITY_CONTACTS + "." + "NOTES," +
            IDENTITY_CONTACTS + "." + "TAXNUMBER";
//            + IDENTITY_CONTACTS + "." +
//            "DATEADDED," + IDENTITY_CONTACTS + "." + "ISACTIVE," +
//            IDENTITY_CONTACTS + "." + "ISCUSTOMER," + IDENTITY_CONTACTS + "." + "ISMANUFACTURER," + IDENTITY_CONTACTS + "." + "ISSUPPLIER," +
//            IDENTITY_CONTACTS + "." + "ISCOMPANY," + IDENTITY_CONTACTS + "." + "ISMALE," + IDENTITY_CONTACTS + "." + "ISENABLED," +
//            IDENTITY_CONTACTS + "." + "ADDEDBY," + IDENTITY_CONTACTS + "." + "RESERVE1," + IDENTITY_CONTACTS + "." + "RESERVE2," +
//            IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER," + IDENTITY_CONTACTS + "." + "TITLE," +
//            IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME," + IDENTITY_CONTACTS + "." + "STREET," +
//            IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY," + IDENTITY_CONTACTS + "." + "MAINPHONE," +
//            IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE," + IDENTITY_CONTACTS + "." + "WORKPHONE," +
//            IDENTITY_CONTACTS + "." + "MAILADDRESS," + IDENTITY_CONTACTS + "." + "company," + IDENTITY_CONTACTS + "." + "WEBSITE," +
//            IDENTITY_CONTACTS + "." + "NOTES," + IDENTITY_CONTACTS + "." + "taxnumber," + IDENTITY_CONTACTS + "." + "DATEADDED," ;
//            IDENTITY_CONTACTS + "." + "ISACTIVE," + IDENTITY_CONTACTS + "." + "ISCUSTOMER," + IDENTITY_CONTACTS + "." + "ISMANUFACTURER," +
//            IDENTITY_CONTACTS + "." + "ISSUPPLIER," + IDENTITY_CONTACTS + "." + "ISCOMPANY," + IDENTITY_CONTACTS + "." + "ISMALE," +
//            IDENTITY_CONTACTS + "." + "ISENABLED," + IDENTITY_CONTACTS + "." + "ADDEDBY";
    public static String DETAILS_USERS = IDENTITY_USERS + "." + "IDS," + IDENTITY_USERS + "." + "CNAME," +
            IDENTITY_USERS + "." + "fullname," +
            IDENTITY_USERS + "." + "mail," +
            IDENTITY_USERS + "." + "isenabled," +
            IDENTITY_USERS + "." + "isloggedin";
//            IDENTITY_USERS + "." + "locale," +
//            IDENTITY_USERS + "." + "language," +
//            IDENTITY_USERS + "." + "laf," +
//            IDENTITY_USERS + "." + "inthighestright," +
//            IDENTITY_USERS + "." + "datelastlog";
    public static String DETAILS_ITEMS =
            IDENTITY_ITEMS + "." + "IDS," +
            IDENTITY_ITEMS + "." + "CNAME," +
            IDENTITY_ITEMS + "." + "dateadded," +
            IDENTITY_ITEMS + "." + "netvalue," +
            IDENTITY_ITEMS + "." + "taxvalue, " +
            IDENTITY_ITEMS + "." + "datetodo";
    //ids date group number type status value
    public static String DETAILS_JOURNAL = IDENTITY_ITEMS + "." + "IDS," +
            IDENTITY_ITEMS + "." + "dateadded," +
            IDENTITY_GROUPS + "0." + "CNAME," +
            IDENTITY_ITEMS + "." + "CNAME," +
            IDENTITY_ITEMS + "." + "inttype," +
            IDENTITY_ITEMS + "." + "intstatus," +
            IDENTITY_ITEMS + "." + "netvalue," +
            IDENTITY_ITEMS + "." + "taxvalue";
    public static String DETAILS_HISTORY = getHistory().getDbIdentity() + ".ids, " + getHistory().getDbIdentity() + ".cname, " + getHistory().getDbIdentity() + ".username, " + Context.getGroup().getDbIdentity() + "0.cname," + Context.getHistory().getDbIdentity() + ".dateadded";
    public static String DETAILS_FILES = Context.getFiles().getDbIdentity() + "0.cname," + getFilesToContacts().getDbIdentity() + ".cname, " + Context.getFiles().getDbIdentity() + "0.dateadded," + Context.getFilesToContacts().getDbIdentity() + ".description";

    //**************************************************************************
    /**
     * Contexts which are protected by the Securitymanager
     * @return
     */
    public static ArrayList<Context> getSecuredContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getCompany());
//        list.add(getUser()); Needs to be non-secure, to update user details on close
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getAddress());
        list.add(getItem(null, null));
        list.add(getBill());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSubItem());
        list.add(getSchedule());
        list.add(getCountries());
        list.add(getContact());
        list.add(getProducts());
        list.add(getAccounts());
        list.add(getMessages());
        list.add(getItemsList());

        return list;
    }

    /**
     * Contexts which are groupable
     * @return
     */
    public static ArrayList<Context> getGroupableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getCompany());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getItem(null, null));
        list.add(getBill());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProducts());
        list.add(getAccounts());
        list.add(getMessages());
        list.add(getItemsList());

        return list;
    }

    /**
     * Contexts which can be moved to trash rather than delete
     * @return
     */
    public static ArrayList<Context> getTrashableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getCompany());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getItem(null, null));
        list.add(getBill());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProducts());
        list.add(getFiles());
        list.add(getMessages());
        list.add(getItemsList());

        return list;
    }

    /**
     * Contexts which are monitored by the History
     * @return
     */
    public static ArrayList<Context> getArchivableContexts() {
        return getSecuredContexts();
    }

    /**
     * Contexts which can be used in a user's Search
     * @return
     */
    public static ArrayList<Context> getSearchableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getUser());
//        list.add(getAddress());
        list.add(getItem(null, null));
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProducts());
        list.add(getAccounts());
        return list;
    }

    /**
     * LOckable Contexts
     * @return
     */
    public static ArrayList<Context> getLockableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getUser());
        list.add(getItem(null, null));
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProducts());
        list.add(getAccounts());
        list.add(getCompany());
        list.add(getCustomer());
        list.add(getSupplier());
        list.add(getManufacturer());
        return list;
    }

    /**
     * Importable Contexts
     * @return
     */
    public static ArrayList<Context> getImportableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getItem(null, null));
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProducts());
        list.add(getAccounts());
        return list;
    }

    /**
     *
     * @return All availbale contexts
     */
    public static ArrayList<Context> getContexts() {
        return allContexts;
    }
    /**
     * A list of all available contexts
     */
    private static ArrayList<Context> allContexts = new ArrayList<Context>(Arrays.asList(new Context[]{
                getCompany(),
                getContact(),
                getCustomer(),
                getFavourites(),
                getFiles(),
                getLanguage(),
                getLock(),
                getManufacturer(),
                getSupplier(),
                getUser(),
                getAddress(),
                getItem(null, null),
                getBill(),
                getOrder(),
                getOffer(),
                getSubItem(),
                getGroup(),
                getSchedule(),
                getFilesToContacts(),
                getHistory(),
                getCountries(),
                getProducts(),
                getPlugins(),
                getPluginsToUsers(),
                getProperties(),
                getAccounts(),
                getItemsToAccounts(),
                getMessages(),
                getMessagesToItems(),
                getItemsList(),
                getFormats()
            }));
    private String[] searchHeaders;
    private ArrayList<String[]> references = new ArrayList<String[]>();
    private boolean exclusiveConditionsAvailable = false;
    private String exclusiveCondition;
    private String uniqueColumns;
    private int id = -1;

    /**
     * Create a new Context instance with the given do as owner
     * @param parentobject
     */
    public Context(DatabaseObject parentobject) {
        if (parentobject != null) {
            setOwner(parentobject);
        }
    }
    private Class identityClass = null;
    /*
     * The DB Identity name - usually the table
     */
    private String dbIdentity = null;

    /*
     * The DB Sub Identity name - maybe a column name
     */
    private String subID = null;

    /*
     * The search default result columns
     */
    private String defResultFields = null;
    private DatabaseObject parent;

    private Context() {
    }

    /**
     *
     * @return The unique constraints
     */
    public String getUniqueColumns() {
        return uniqueColumns;
    }

    /**
     *
     * @param customer
     * @param supplier
     * @param manufacturer
     * @param company
     */
    public void setContactConditions(boolean customer, boolean supplier, boolean manufacturer, boolean company) {
        setCustomer(customer);
        setSupplier(supplier);
        setManufacturer(manufacturer);
        setCompany(company);
    }

//    /**
//     * Set conditions to get exclusive data
//     * @param done
//     * @param active
//     * @param bill
//     * @param order
//     * @param offer
//     */
//    public void setExclusiveItemConditions(boolean done, boolean active, boolean bill, boolean order, boolean offer) {
//
//        String cond = "    ";
//        boolean first = true;
//
//        if (done) {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_DONE + "=1 AND ";
//        } else {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_DONE + "=0 AND ";
//        }
//        if (active) {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_ACTIVE + "=1 AND ";
//        } else {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_ACTIVE + "=0 AND ";
//        }
//        if (bill) {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_BILL + "=1 AND ";
//        } else {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_BILL + "=0 AND ";
//        }
//        if (order) {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_ORDER + "=1 AND ";
//        } else {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_ORDER + "=0 AND ";
//        }
//        if (offer) {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_OFFER + "=1 AND ";
//        } else {
//            if (first) {
//                cond += "WHERE ";
//            }
//            first = false;
//            cond += " " + CONDITION_ITEMS_OFFER + "=0 AND ";
//        }
//
//
//        if (!first) {
//            cond = cond.substring(4, cond.length() - 4);
//            if (MPV5View.getUser().__getIsrgrouped() && getGroupableContexts().contains(this)) {
//                cond = "AND   " + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids();
//            }
//        } else {
//
//            if (MPV5View.getUser().__getIsrgrouped() && getGroupableContexts().contains(this)) {
//                cond = "WHERE " + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids();
//            } else {
//                cond = "WHERE " + CONDITION_DEFAULT;
//            }
//        }
//
//        if (getTrashableContexts().contains(this)) {
//            cond += " AND invisible = 0 ";
//        }
//
//        exclusiveCondition = cond;
//    }
    /**
     * Set conditions to get exclusive data (customer = false results in all data without any customer)
     * @param customer
     * @param supplier
     * @param manufacturer
     * @param company
     */
    public void setExclusiveContactConditions(boolean customer, boolean supplier, boolean manufacturer, boolean company) {

        String cond = "    ";
        boolean first = true;

        if (customer) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_CUSTOMER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_CUSTOMER + "=0 AND ";
        }
        if (supplier) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_SUPPLIER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_SUPPLIER + "=0 AND ";
        }
        if (manufacturer) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=0 AND ";
        }
        if (company) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_COMPANY + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_COMPANY + "=0 AND ";
        }

        if (!first) {
            cond = cond.substring(4, cond.length() - 4);
            if (MPV5View.getUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond += "AND   (" + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            }
        } else {

            if (MPV5View.getUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            } else {
                cond = "WHERE " + CONDITION_DEFAULT;
            }
        }

        if (getTrashableContexts().contains(this)) {
            cond += " AND invisible = 0 ";
        }

        exclusiveCondition = cond;
    }

    /**
     *
     * @return DB condition string
     */
    public String getConditions() {
        if (!exclusiveConditionsAvailable) {
            String cond = "    ";
            boolean first = true;
            if (isCompany()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_COMPANY + "=1 OR ";
            }
            if (isCustomer()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_CUSTOMER + "=1 OR ";
            }
            if (isManufacturer()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=1 OR ";
            }
            if (isSupplier()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_SUPPLIER + "=1 OR ";
            }

            if (itemType != null) {
                if (first) {
                    cond += "WHERE ";
                    first = false;
                }
                cond += " " + CONDITION_ITEMS_TYPE + "=" + getItemType() + " OR ";
            }

            if (itemStatus != null) {
                if (first) {
                    cond += "WHERE ";
                    first = false;
                }
                cond += " " + CONDITION_ITEMS_STATUS + "=" + getItemStatus() + " OR ";
            }

            if (!first) {
                cond = cond.substring(4, cond.length() - 3);
                if (MPV5View.getUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond += "AND   (" + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                }
            } else {

                if (MPV5View.getUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                } else {
                    cond = "WHERE " + CONDITION_DEFAULT;
                }
            }

            if (getTrashableContexts().contains(this)) {
                cond += " AND invisible = 0 ";
            }

            return cond;
        } else {
            return exclusiveCondition.toString();
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to implement multi-client capability.<br/>
     * <br/>
     * <b>If the current Context does not support grouping, or the current user is not Group restricted, this will return " ".</b>
     * @return
     */
    public String getGroupRestrictionSQLString() {
        if (MPV5View.getUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
            return " (" + dbIdentity + "." + "GROUPSIDS = " + MPV5View.getUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
        } else {
            return " ";
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to avoid having already trashed elements in the resulting data.
     * @return
     */
    public String getNoTrashSQLString() {
        if (getTrashableContexts().contains(this)) {
            return " invisible = 0 ";
        } else {
            return " ";
        }
    }

    /**
     * Define the owner of this Context
     * @param parentobject
     */
    public void setOwner(DatabaseObject parentobject) {
        this.parent = parentobject;
    }

    /**
     * Remove all exclusive conditions or reuse them
     * @param bool
     */
    public void useExclusiveConditions(boolean bool) {
        exclusiveConditionsAvailable = bool;
    }

    /**
     * Add a self-table reference to this context
     * @param referencekey
     * @param referenceidkey
     */
    public void addReference(String referencekey, String referenceidkey) {
        String alias = this.getDbIdentity();
        references.add(new String[]{this.getDbIdentity(), referencekey, referenceidkey, alias});
    }

    /**
     *
     * @param group
     */
    public void addReference(Context group) {
        String alias = group.getDbIdentity();
        references.add(new String[]{alias, "ids", alias + "ids", alias, this.getDbIdentity()});
    }

    /**
     * Add a foreign table reference to this context<br/><br/>
     *  Context c= Context.getFilesToContacts();<br/>
     *  c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");<br/>
     * @param referencetable The table which will be joined
     * @param referencekey The key column of the joined table
     * @param referenceidkey They key column in the original table
     */
    public void addReference(String referencetable, String referencekey, String referenceidkey) {
        String alias = referencetable;
        references.add(new String[]{referencetable, referencekey, referenceidkey, alias, this.getDbIdentity()});
    }

    /**
     * @return the dbIdentity
     */
    public String getDbIdentity() {
        return dbIdentity;
    }

    public String getSearchFields() {
        return defResultFields;
    }

    public String[] getSearchHeaders() {
        return searchHeaders;
    }

    public DatabaseObject getParent() {
        return parent;
    }

    /**
     * @param dbIdentity the dbIdentity to set
     */
    public void setDbIdentity(String dbIdentity) {
        this.dbIdentity = dbIdentity;
    }

    /**
     * @return the subID
     */
    public String getSubID() {
        return subID;
    }

    /**
     * @return the IDENTITY CLASS
     */
    public Class getIdentityClass() {
        return identityClass;
    }

    /**
     * @param subID the subID to set
     */
    public void setSubID(String subID) {
        this.subID = subID;
    }

    public String getReferences() {
        String cond = "";
        if (references.size() > 0) {
            for (int i = 0; i < references.size(); i++) {
                if (references.get(i).length == 4) {
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " AS " + references.get(i)[3] + i + " ON " + references.get(i)[3] + i + "." + references.get(i)[1] + " = " + references.get(i)[3] + "." + references.get(i)[2];
                } else if (references.get(i).length == 5) {
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " AS " + references.get(i)[3] + i + " ON " + references.get(i)[3] + i + "." + references.get(i)[1] + " = " + references.get(i)[4] + "." + references.get(i)[2];
                }
            }
        }
        return cond;
    }

    /**
     * @return the Company
     */
    private boolean isCompany() {
        return isCompany;
    }

    /**
     * @param Company the Company to set
     */
    private void setCompany(boolean Company) {
        this.isCompany = Company;
    }

    /**
     * @return the Customer
     */
    private boolean isCustomer() {
        return isCustomer;
    }

    /**
     * @param Customer the Customer to set
     */
    private void setCustomer(boolean Customer) {
        this.isCustomer = Customer;
    }

    /**
     * @return the Manufacturer
     */
    private boolean isManufacturer() {
        return isManufacturer;
    }

    private void setId(int id) {
        this.id = id;
    }

    /**
     * @param Manufacturer the Manufacturer to set
     */
    private void setManufacturer(boolean Manufacturer) {
        this.isManufacturer = Manufacturer;
    }

    /**
     * @return the Supplier
     */
    private boolean isSupplier() {
        return isSupplier;
    }

    /**
     * @param Supplier the Supplier to set
     */
    private void setSupplier(boolean Supplier) {
        this.isSupplier = Supplier;
    }

    /**
     * @return the itemStatus
     */
    public int getItemStatus() {
        return itemStatus;
    }

    /**
     * @param itemStatus the itemStatus to set
     */
    public void setItemStatus(int itemStatus) {
        this.itemStatus = itemStatus;
    }

    /**
     * @return the itemType
     */
    public int getItemType() {
        return itemType;
    }

    /**
     * @param itemType the itemType to set
     */
    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

//    /**
//     * @return the isDone
//     */
//    private boolean isIsDone() {
//        return isDone;
//    }
//
//    /**
//     * @param isDone the isDone to set
//     */
//    private void setIsDone(boolean isDone) {
//        this.isDone = isDone;
//    }

//    /**
//     * @return the isActive
//     */
//    private boolean isIsActive() {
//        return isActive;
//    }
//
//    /**
//     * @param isActive the isActive to set
//     */
//    private void setIsActive(boolean isActive) {
//        this.isActive = isActive;
//    }
//
//    /**
//     * @return the isBill
//     */
//    public boolean isIsBill() {
//        return isBill;
//    }
//
//    /**
//     * @param isBill the isBill to set
//     */
//    public void setIsBill(boolean isBill) {
//        this.isBill = isBill;
//    }
//
//    /**
//     * @return the isOrder
//     */
//    public boolean isIsOrder() {
//        return isOrder;
//    }
//
//    /**
//     * @param isOrder the isOrder to set
//     */
//    public void setIsOrder(boolean isOrder) {
//        this.isOrder = isOrder;
//    }
//
//    /**
//     * @return the isOffer
//     */
//    public boolean isIsOffer() {
//        return isOffer;
//    }
//
//    /**
//     * @param isOffer the isOffer to set
//     */
//    public void setIsOffer(boolean isOffer) {
//        this.isOffer = isOffer;
//    }
    public static Context getCompany() {
        Context c = new Context(new Contact());
        c.setCompany(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(0);

        return c;
    }

    /**
     * 
     * @param type 
     * @param status
     * @return
     */
    public static Context getItem(Integer type, Integer status) {
        Context c = new Context(new Item());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        if (status != null) {
            c.setItemStatus(status);
        }
        if (type != null) {
            c.setItemType(type);
        }
        c.setId(1);

        return c;
    }

    public static Context getItems() {
        return getItem(null, null);
    }

    public static Context getSubItem() {
        Context c = new Context(new SubItem());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SUBITEMS);
        c.setIdentityClass(IDENTITY_SUBITEMS_CLASS);
        c.setId(2);

        return c;
    }

    public static Context getCustomer() {
        Context c = new Context(new Contact());
        c.setCustomer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(3);

        return c;
    }

    public static Context getManufacturer() {
        Context c = new Context(new Contact());
        c.setManufacturer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(4);

        return c;
    }

    public static Context getSupplier() {
        Context c = new Context(new Contact());
        c.setSupplier(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(5);

        return c;
    }

    public static Context getContact() {
        Context c = new Context(new Contact());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(6);

        return c;
    }

    public static Context getUser() {
        Context c = new Context(new User());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_USERS);
        c.setSearchFields(DEFAULT_USER_SEARCH);
        c.setSearchHeaders(Headers.USER_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_USERS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_USER;
        c.setId(7);

        return c;
    }

    public static Context getSchedule() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SCHEDULE);
        c.setIdentityClass(Schedule.class);
        c.setId(8);

        return c;
    }

    public static Context getLanguage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LANGUAGES);
        c.setId(9);

        return c;
    }

    public static Context getFiles() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES);
        c.setId(10);

        return c;
    }

    public static Context getLock() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LOCK);
        c.setId(11);

        return c;
    }
//
//    public static Context getGroupToParentGroup() {
//        Context c = new Context();
//        c.setSubID(DEFAULT_SUBID);
//        c.setDbIdentity(IDENTITY_GROUPS_TO_PARENTGROUP);
//        c.setId(12);
//
//        return c;
//    }

    public static Context getGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GROUPS);
        c.setIdentityClass(Group.class);
        c.uniqueColumns = UNIQUECOLUMNS_GROUPS;
        c.setId(14);

        return c;
    }

    public static Context getFavourites() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FAVS);
        c.setIdentityClass(Favourite.class);
        c.setId(15);

        return c;
    }

    public static Context getAddress() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ADDRESS);
        c.setIdentityClass(IDENTITY_ADDRESS_CLASS);
        c.setId(16);

        return c;
    }

    public static Context getBill() {
        Context c = new Context(new Item());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_BILL);
        c.setId(17);

        return c;
    }

    public static Context getOrder() {
        Context c = new Context(new Item());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_ORDER);
        c.setId(18);

        return c;
    }

    public static Context getOffer() {
        Context c = new Context(new Item());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_OFFER);
        c.setId(19);

        return c;
    }

    public static Context getFilesToContacts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_CONTACTS);
        c.setIdentityClass(IDENTITY_CONTACTS_FILES_CLASS);
        c.setId(20);

        return c;
    }

    public static Context getHistory() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_HISTORY);
        c.setIdentityClass(HISTORY_ITEMS_CLASS);
        c.setId(21);

        return c;
    }

    public static Context getCountries() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_COUNTRIES);
        c.setId(22);

        return c;
    }

    public static Context getProducts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTS);
        c.setId(23);

        return c;
    }

    public static Context getSearchIndex() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SEARCHINDEX);
        c.setId(24);

        return c;
    }

    public static Context getPluginsToUsers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS_TO_USERS);
        c.setIdentityClass(IDENTITY_USER_PLUGINS_CLASS);
        c.setId(25);

        return c;
    }

    public static Context getPlugins() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS);
        c.setIdentityClass(IDENTITY_PLUGINS_CLASS);
        c.setId(26);

        return c;
    }

    public static Context getProperties() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PROPERTIES_TO_USERS);
        c.setIdentityClass(IDENTITY_PROPERTIES_CLASS);
        c.setId(27);

        return c;
    }

    public static Context getAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ACCOUNTS);
        c.setIdentityClass(IDENTITY_ACCOUNTS_CLASS);
        c.setId(28);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static Context getItemsToAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS_TO_ACCOUNTS);
        c.setId(29);

        return c;
    }

    public static Context getMessages() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_MESSAGES);
        c.setIdentityClass(IDENTITY_MESSAGES_CLASS);
        c.setId(30);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static Context getMessagesToItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_MESSAGES_TO_ITEMS);
        c.setId(31);

        return c;
    }

    public static Context getItemsList() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMSLIST);
        c.setIdentityClass(IDENTITY_ITEMSLIST_CLASS);
        c.setId(32);

        return c;
    }

    public static Context getFormats() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FORMATS_T_USERS);
        c.setId(33);

        return c;
    }

    /**
     *
     * @param contextdbidentity
     * @return The matching context or null if not existing
     */
    public static Context getMatchingContext(String contextdbidentity) {
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getDbIdentity().equalsIgnoreCase(contextdbidentity)) {
                return context;
            }
        }
        return null;
    }

    public void setSearchFields(String fields) {
        defResultFields = fields;
    }

    public void setSearchHeaders(String[] headers) {
        searchHeaders = headers;
    }

    /**
     * @param identityClass the identityClass to set
     */
    public void setIdentityClass(Class identityClass) {
        this.identityClass = identityClass;
    }

    @Override
    public String toString() {
        return dbIdentity.toUpperCase();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (((Context) o).getDbIdentity().equals(getDbIdentity())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.dbIdentity != null ? this.dbIdentity.hashCode() : 0);
        return hash;
    }
}