package mpv5.db.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import mpv5.db.objects.Account;
import mpv5.db.objects.Address;
import mpv5.db.objects.Company;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.FileToContact;
import mpv5.db.objects.Group;
import mpv5.db.objects.HistoryItem;
import mpv5.db.objects.UserProperty;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.FileToItem;
import mpv5.db.objects.FileToProduct;
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.ProductsToSuppliers;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.Stage;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.Template;
import mpv5.pluginhandling.Plugin;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.db.objects.WSContactsMapping;
import mpv5.db.objects.WSItemsMapping;
import mpv5.db.objects.WebShop;
import mpv5.logging.Log;
import mpv5.pluginhandling.UserPlugin;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.text.RandomText;
import mpv5.utils.xml.XMLReader;

/**
 *
 * Context controls Database Queries for all native MP {@link DatabaseObject}s
 */
public class Context implements Serializable {

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
    public static String IDENTITY_PGROUPS = "productgroups";
    public static String IDENTITY_SCHEDULE = "schedule";
    public static String IDENTITY_HISTORY = "history";
    public static String IDENTITY_FILES_TO_CONTACTS = "filestocontacts";
    public static String IDENTITY_FILES_TO_PRODUCTS = "filestoproducts";
    public static String IDENTITY_SEARCHINDEX = "searchindex";
    public static String IDENTITY_PLUGINS_TO_USERS = "pluginstousers";
    public static String IDENTITY_TEMPLATES_TO_USERS = "templatestousers";
    public static String IDENTITY_PLUGINS = "plugins";
    public static String IDENTITY_PROPERTIES_TO_USERS = "userproperties";
    public static String IDENTITY_ACCOUNTS = "accounts";
    public static String IDENTITY_ITEMS_TO_ACCOUNTS = "itemstoaccounts";
    public static String IDENTITY_PRODUCTSLISTITEMS = "productlistitems";
    public static String IDENTITY_PRODUCTSLIST = "productlists";
    public static String IDENTITY_FORMATS_T_USERS = "formatstousers";
    public static String IDENTITY_FILES_TO_ITEMS = "filestoitems";
    public static String IDENTITY_MAIL = "mails";
    public static String IDENTITY_TAX = "tax";
    public static String IDENTITY_COMPANIES = "comps";
    public static String IDENTITY_GLOBALSETTINGS = "globalsettings";
    public static String IDENTITY_WEBSHOPS = "webshops";
    public static String IDENTITY_WSMAPPING = "wscontactsmapping";
    public static String IDENTITY_WSIMAPPING = "wsitemsmapping";
    public static String IDENTITY_TEMPLATES = "templates";
    public static String IDENTITY_REMINDERS = "reminders";
    public static String IDENTITY_STAGES = "stages";
    public static String IDENTITY_REVENUE = "revenues";
    public static String IDENTITY_EXPENSE = "expenses";
    public static String IDENTITY_PRODUCTS_TO_SUPPLIERS = "productstosuppliers";
    public static String IDENTITY_VALUE_PROPERTIES = "valueproperties";
    //********** identity classes **********************************************
    private static Class IDENTITY_CONTACTS_CLASS = Contact.class;
    private static Class IDENTITY_ADDRESS_CLASS = Address.class;
    private static Class IDENTITY_USERS_CLASS = User.class;
    private static Class IDENTITY_ITEMS_CLASS = Item.class;
    private static Class IDENTITY_CONTACTS_FILES_CLASS = FileToContact.class;
    private static Class IDENTITY_ITEM_FILES_CLASS = FileToItem.class;
    private static Class HISTORY_ITEMS_CLASS = HistoryItem.class;
    private static Class IDENTITY_SUBITEMS_CLASS = SubItem.class;
    private static Class IDENTITY_USER_PLUGINS_CLASS = UserPlugin.class;
    private static Class IDENTITY_PLUGINS_CLASS = Plugin.class;
    private static Class IDENTITY_PROPERTIES_CLASS = UserProperty.class;
    private static Class IDENTITY_ACCOUNTS_CLASS = Account.class;
    private static Class IDENTITY_ITEMSLIST_CLASS = ProductlistSubItem.class;
    private static Class IDENTITY_MAILS_CLASS = MailMessage.class;
    private static Class IDENTITY_PRODUCTS_CLASS = Product.class;
    private static Class IDENTITY_GROUPS_CLASS = Group.class;
    private static Class IDENTITY_COMPANY_CLASS = Company.class;
    private static Class IDENTITY_PGROUPS_CLASS = ProductGroup.class;
    private static Class IDENTITY_PRODUCTS_FILES_CLASS = FileToProduct.class;
    private static Class IDENTITY_WEBSHOP_CLASS = WebShop.class;
    private static Class IDENTITY_TEMPLATE_CLASS = Template.class;
    private static Class IDENTITY_REMINDER_CLASS = Reminder.class;
    private static Class IDENTITY_STAGE_CLASS = Stage.class;
    private static Class IDENTITY_VALUEPROPERTY_CLASS = ValueProperty.class;
    //********** unique constraints *******************************************
    public static String UNIQUECOLUMNS_USER = "cname";
    public static String UNIQUECOLUMNS_ITEMS = "cname";
    public static String UNIQUECOLUMNS_GROUPS = "cname";
    public static String UNIQUECOLUMNS_DEFAULT = "cname";
    public static String DETAIL_CONTACT_SEARCH = "prename,cname,street,city,country,notes";
    private static final long serialVersionUID = 1L;

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
    public static String DEFAULT_ITEM_SEARCH = "ids, cname, dateadded, netvalue";
    public static String DEFAULT_PRODUCT_SEARCH = "ids, cnumber, cname, description";
    //********** table fields ********************************************************
    public static String DETAILS_CONTACTS = IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER,"
            + IDENTITY_CONTACTS + "." + "TITLE," + IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME,"
            + IDENTITY_CONTACTS + "." + "STREET," + IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY,"
            + IDENTITY_CONTACTS + "." + "MAINPHONE," + IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE,"
            + IDENTITY_CONTACTS + "." + "WORKPHONE," + IDENTITY_CONTACTS + "." + "COMPANY," + IDENTITY_CONTACTS + "." + "MAILADDRESS,"
            + IDENTITY_CONTACTS + "." + "WEBSITE," + IDENTITY_CONTACTS + "." + "NOTES,"
            + IDENTITY_CONTACTS + "." + "TAXNUMBER";
    public static String DETAILS_PRODUCTS =
            IDENTITY_PRODUCTS + "." + "ids,"
            + IDENTITY_PRODUCTS + "." + "cnumber,"
            + IDENTITY_PRODUCTS + "." + "cname,"
            + IDENTITY_PRODUCTS + "." + "ean,"
            + IDENTITY_PRODUCTS + "." + "reference,"
            + IDENTITY_CONTACTS + "." + "cname,"
            + IDENTITY_GROUPS + "." + "cname,"
            + IDENTITY_PRODUCTS + "." + "internalnetvalue,"
            + IDENTITY_PRODUCTS + "." + "externalnetvalue,"
            + IDENTITY_PRODUCTS + "." + "stockvalue";
    public static String DETAILS_USERS = IDENTITY_USERS + "." + "IDS," + IDENTITY_USERS + "." + "CNAME,"
            + IDENTITY_USERS + "." + "fullname,"
            + IDENTITY_USERS + "." + "mail,"
            + IDENTITY_USERS + "." + "isenabled,"
            + IDENTITY_USERS + "." + "isloggedin";
    public static String DETAILS_ITEMS =
            IDENTITY_ITEMS + "." + "IDS,"
            + IDENTITY_ITEMS + "." + "CNAME,"
            + IDENTITY_ITEMS + "." + "dateadded,"
            + IDENTITY_ITEMS + "." + "netvalue,"
            + IDENTITY_ITEMS + "." + "taxvalue, "
            + IDENTITY_ITEMS + "." + "datetodo";
    public static String DETAILS_JOURNAL = IDENTITY_ITEMS + "." + "IDS,"
            + IDENTITY_ITEMS + "." + "{date},"
            + //            IDENTITY_ITEMS + "." + "dateend," +
            IDENTITY_GROUPS + "." + "CNAME,"
            + IDENTITY_ACCOUNTS + "." + "cname,"
            + IDENTITY_ITEMS + "." + "CNAME,"
            + IDENTITY_ITEMS + "." + "inttype,"
            + IDENTITY_ITEMS + "." + "intstatus,"
            + IDENTITY_ITEMS + "." + "netvalue,"
            + IDENTITY_ITEMS + "." + "taxvalue";
    public static String DETAILS_JOURNAL2 = IDENTITY_REVENUE + "." + "IDS,"
            + IDENTITY_REVENUE + "." + "dateadded,"
            + IDENTITY_GROUPS + "." + "CNAME,"
            + IDENTITY_ACCOUNTS + "." + "cname,"
            + IDENTITY_REVENUE + "." + "CNAME,"
            + IDENTITY_REVENUE + "." + "ids,"
            + IDENTITY_REVENUE + "." + "ids,"
            + IDENTITY_REVENUE + "." + "brutvalue,"
            + IDENTITY_REVENUE + "." + "brutvalue";
    public static String DETAILS_JOURNAL3 = IDENTITY_EXPENSE + "." + "IDS,"
            + IDENTITY_EXPENSE + "." + "dateadded,"
            + IDENTITY_GROUPS + "." + "CNAME,"
            + IDENTITY_ACCOUNTS + "." + "cname,"
            + IDENTITY_EXPENSE + "." + "CNAME,"
            + IDENTITY_EXPENSE + "." + "ids,"
            + IDENTITY_EXPENSE + "." + "ispaid,"
            + IDENTITY_EXPENSE + "." + "brutvalue,"
            + IDENTITY_EXPENSE + "." + "brutvalue";
    public static String DETAILS_HISTORY = getHistory().getDbIdentity() + ".ids, " + getHistory().getDbIdentity() + ".cname, " + getHistory().getDbIdentity() + ".username, " + Context.getGroup().getDbIdentity() + ".cname," + Context.getHistory().getDbIdentity() + ".dateadded";
    public static String DETAILS_FILES_TO_CONTACTS = Context.getFiles().getDbIdentity() + ".cname," + getFilesToContacts().getDbIdentity() + ".cname, " + Context.getFiles().getDbIdentity() + ".dateadded," + Context.getFilesToContacts().getDbIdentity() + ".description," + Context.getFilesToContacts().getDbIdentity() + ".intsize," + Context.getFilesToContacts().getDbIdentity() + ".mimetype";
    public static String DETAILS_FILES_TO_ITEMS = Context.getFiles().getDbIdentity() + ".cname," + getFilesToItems().getDbIdentity() + ".cname, " + Context.getFiles().getDbIdentity() + ".dateadded," + Context.getFilesToItems().getDbIdentity() + ".description," + Context.getFilesToItems().getDbIdentity() + ".intsize," + Context.getFilesToItems().getDbIdentity() + ".mimetype";
    public static String DETAILS_FILES_TO_PRODUCTS = Context.getFiles().getDbIdentity() + ".cname," + getFilesToProducts().getDbIdentity() + ".cname, " + Context.getFiles().getDbIdentity() + ".dateadded," + Context.getFilesToProducts().getDbIdentity() + ".description," + Context.getFilesToProducts().getDbIdentity() + ".intsize," + Context.getFilesToProducts().getDbIdentity() + ".mimetype";
//    public static String DETAILS_FILES_TO_TEMPLATES = Context.getTemplate().getDbIdentity() + ".ids," + getTemplate().getDbIdentity() + ".cname, " + Context.getTemplate().getDbIdentity() + ".dateadded," + Context.getTemplate().getDbIdentity() + ".intsize," + Context.getTemplate().getDbIdentity() + ".mimetype";
//    public static String DETAILS_TEMPLATES = Context.getTemplate().getDbIdentity() + ".ids," + getTemplate().getDbIdentity() + ".cname, " + Context.getTemplate().getDbIdentity() + ".mimetype," + " groups0.cname";

    //**************************************************************************
    /**
     * Contexts which can have an export template
     * @return
     */
    public static ArrayList<Context> getTemplateableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getItem(null, null));
        list.add(getInvoice());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getProduct());
        list.add(getReminder());
        return list;
    }

    /**
     * Contexts which are protected by the Securitymanager
     * @return
     */
    public static ArrayList<Context> getSecuredContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getContactsCompanies());
//        list.add(getUser()); Needs to be non-secure, to update user details on close
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getAddress());
        list.add(getItem(null, null));
        list.add(getInvoice());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getCountries());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getCompany());
        list.add(getWebShop());
        list.add(getTemplate());
        list.add(getReminder());
        list.add(getSubItem());

        return list;
    }

    /**
     * Contexts which can get cached
     * @return
     */
    public static ArrayList<Context> getCacheableContexts() {
        return cacheableContexts;
    }
    private static ArrayList<Context> cacheableContexts = new ArrayList<Context>(Arrays.asList(new Context[]{
                getContact(),
                getFavourite(),
                getUser(),
                getAddress(),
                getItem(null, null),
                getSubItem(),
                getGroup(),
                getSchedule(),
                getFilesToContacts(),
                getFilesToItems(),
                getFilesToProducts(),
                getProduct(),
                getAccounts(),
                getMessage(),
                getProductListItems(),
                getCompany(),
                getWebShop(),
                getTemplate(),
                getReminder(),
                getStage(),
                getTaxes(),
                getProductGroup()
            }));

    /**
     * Contexts which are groupable
     * @return
     */
    public static ArrayList<Context> getGroupableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getItem(null, null));
        list.add(getInvoice());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getMessage());
        list.add(getProductListItems());
        list.add(getSearchIndex());
        list.add(getGlobalSettings());
        list.add(getCompany());
        list.add(getWebShop());
        list.add(getTemplate());
        list.add(getReminder());
        list.add(getStage());
        list.add(getExpense());
        list.add(getRevenue());
        list.add(getTaxes());
        return list;
    }

    /**
     * Contexts which can be moved to trash rather than delete
     * @return
     */
    public static ArrayList<Context> getTrashableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getItem(null, null));
        list.add(getInvoice());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSubItem());
        list.add(getContact());
        list.add(getProduct());
        list.add(getFiles());
        list.add(getExpense());
        list.add(getRevenue());
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
        list.add(getSubItem());
        list.add(getItem());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProduct());
////        list.add(getAccounts());
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
        list.add(getProduct());
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getSupplier());
        list.add(getManufacturer());
        list.add(getWebShop());
        return list;
    }

    /**
     * Importable Contexts
     * @return
     */
    public static ArrayList<Context> getImportableContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getItem(null, null));
        list.add(getMessage());
        list.add(getContact());
        list.add(getProduct());
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
                getContactsCompanies(),
                getContact(),
                getCustomer(),
                getFavourite(),
                getFiles(),
                getLanguage(),
                getLock(),
                getManufacturer(),
                getSupplier(),
                getUser(),
                getAddress(),
                getItem(null, null),
                getInvoice(),
                getOrder(),
                getOffer(),
                getSubItem(),
                getGroup(),
                getSchedule(),
                getFilesToContacts(),
                getHistory(),
                getCountries(),
                getProduct(),
                getPlugins(),
                getPluginsToUsers(),
                getTemplatesToUsers(),
                getUserProperties(),
                getAccounts(),
                getItemsToAccounts(),
                getMessage(),
                getProductListItems(),
                getFormats(),
                getTaxes(),
                getGlobalSettings(),
                getCompany(),
                getWebShop(),
                getWebShopItemMapping(),
                getWebShopContactMapping(),
                getTemplate(),
                getReminder(),
                getStage(),
                getProductlist(),
                getExpense(),
                getRevenue(),
                getProductsToSuppliers(),
                getValueProperties()
            }));
//    private String[] searchHeaders;
    private volatile ArrayList<String[]> references = new ArrayList<String[]>();
    private boolean exclusiveConditionsAvailable = false;
    private volatile String exclusiveCondition;
    private volatile String uniqueColumns;
    private volatile int id = -1;

    /**
     * Constructor now private
     */
    private Context() {
    }

    /**
     * Constructs a new Context with a random ID
     * @param tablename The name of the table to use
     * @param targetObjectClass The class of the {@link DatabaseObject} child to be used with this Context
     * @param cacheable If true, the {@link DatabaseObject}s related to this context will be cached
     * @param secured If true, the {@link DatabaseObject}s related to this context will be protected by the {@link MPSecurityManager}
     * @param importable If true, the {@link DatabaseObject}s related to this context will be importable by the {@link XMLReader}
     * @param lockable If true, the {@link DatabaseObject}s related to this context will be loackable to avoid concurrent access
     * @param groupable If true, the {@link DatabaseObject}s related to this context will apply to a users group restriction
     * @param trashable If true, the {@link DatabaseObject}s related to this context will be moved to the trashbin on delete
     * @param archivable If true, actions on the {@link DatabaseObject}s related to this context will be monitored
     */
    public Context(String tablename, Class targetObjectClass, boolean cacheable,
            boolean secured, boolean importable, boolean lockable, boolean groupable,
            boolean trashable, boolean archivable) {
        setId(Integer.valueOf(RandomText.getNumberText()));
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(tablename);
        this.setIdentityClass(targetObjectClass);
        if (archivable) {
            getArchivableContexts().add(this);
        }
        if (cacheable) {
            getCacheableContexts().add(this);
        }
        if (secured) {
            getSecuredContexts().add(this);
        }
        if (importable) {
            getImportableContexts().add(this);
        }
        if (lockable) {
            getLockableContexts().add(this);
        }
        if (groupable) {
            getGroupableContexts().add(this);
        }
        if (trashable) {
            getTrashableContexts().add(this);
        }
        if (archivable) {
            getArchivableContexts().add(this);
        }
    }

    /**
     * Constructs a new Context with a random ID
     * @param tablename The name of the table to use
     * @param targetObjectClass The class of the {@link DatabaseObject} child to be used with this Context
     */
    public Context(String tablename, Class targetObjectClass) {
        setId(Integer.valueOf(RandomText.getNumberText()));
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(tablename);
        this.setIdentityClass(targetObjectClass);
    }

    /**
     * Constructs a new Context with a random ID
     * @param targetObjectClass The tablename will be assumed from the Class' simple name
     */
    public Context(Class targetObjectClass) {
        this(targetObjectClass.getSimpleName(), targetObjectClass);
    }

    /**
     * Constructs a new Context with a random ID.
     * The tablename will be assumed from the targetObject's Class simple name
     * @param targetObject
     */
    public Context(DatabaseObject targetObject) {
        setId(Integer.valueOf(RandomText.getNumberText()));
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(targetObject.getClass().getSimpleName());
        this.setIdentityClass(targetObject.getClass());
    }
    /**
     * A default, empty Context without id
     */
    public static Context DEFAULT = new Context();
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

    /**
     * Set conditions to get exclusive data (e.g. customer = false results in all data without any customer)
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
            if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond += "AND   (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            }
        } else {

            if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            } else {
                cond = "WHERE " + CONDITION_DEFAULT;
            }
        }

        if (getTrashableContexts().contains(this)) {
            cond += " AND " + getDbIdentity() + ".invisible = 0 ";
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
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_COMPANY + "=1 OR ";
            }
            if (isCustomer()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_CUSTOMER + "=1 OR ";
            }
            if (isManufacturer()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=1 OR ";
            }
            if (isSupplier()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_SUPPLIER + "=1 OR ";
            }
            if (itemType != null) {
                if (first) {
                    cond += "(";

                }
                first = false;
                cond += " " + CONDITION_ITEMS_TYPE + "=" + getItemType() + " OR ";
            }
            if (itemStatus != null) {
                if (first) {
                    cond += "(";

                }
                first = false;
                cond += " " + CONDITION_ITEMS_STATUS + "=" + getItemStatus() + " OR ";
            }
            if (!first) {
                cond = "WHERE" + cond.substring(0, cond.length() - 3) + ")";
                if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond += " AND (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                }
            } else {
                if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                } else {
                    cond = "WHERE " + CONDITION_DEFAULT;
                }
            }
            if (getTrashableContexts().contains(this)) {
                cond += " AND " + dbIdentity + ".invisible = 0 ";
            }
//            if (!QueryHandler.isMatchingBraces(cond)) {
//                cond += ")";
//            }
            return cond;
        } else {
            return exclusiveCondition.toString();
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to implement multi-client capability.<br/>
     * <br/>
     * <b>If the current Context does not support grouping, or the current user is not Group restricted, this will return NULL.</b>
     * @return
     */
    public synchronized String getGroupRestrictionSQLString() {
        if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
            return " (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
        } else {
            return null;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to avoid having already trashed elements in the resulting data.
     *
     * @return  " invisible = 0 " or NULL if Context is not trashable
     */
    public synchronized String getNoTrashSQLString() {
        if (getTrashableContexts().contains(this)) {
            return " " + dbIdentity + ".invisible = 0 ";
        } else {
            return null;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to implement multi-client capability.<br/>
     * <br/>
     * <b>If the current Context does not support grouping, or the current user is not Group restricted, this will return NULL.</b>
     * @param tableName
     * @return
     */
    public synchronized String getGroupRestrictionSQLString(String tableName) {
        if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
            return " (" + tableName + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + tableName + "." + "GROUPSIDS = 1)";
        } else {
            return null;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be used to avoid having already trashed elements in the resulting data.
     *
     * @param tableName
     * @return  " invisible = 0 " or NULL if Context is not trashable
     */
    public synchronized String getNoTrashSQLString(String tableName) {
        if (getTrashableContexts().contains(this)) {
            return " " + tableName + ".invisible = 0 ";
        } else {
            return null;
        }
    }

    /**
     * Add MP specific conditions to a sql query
     * @param query
     * @return The query
     */
    public synchronized String prepareSQLString(String query) {

        if (getGroupRestrictionSQLString() != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getGroupRestrictionSQLString();
            } else {
                query = query + " WHERE" + getGroupRestrictionSQLString();
            }
        }
        if (getNoTrashSQLString() != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getNoTrashSQLString();
            } else {
                query = query + " WHERE" + getNoTrashSQLString();
            }
        }
        return query;
    }

    /**
     * Add MP specific conditions to a sql query
     * @param query
     * @param tableName
     * @return The query
     */
    public synchronized String prepareSQLString(String query, String tableName) {

        if (getGroupRestrictionSQLString(tableName) != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getGroupRestrictionSQLString(tableName);
            } else {
                query = query + " WHERE" + getGroupRestrictionSQLString(tableName);
            }
        }
        if (getNoTrashSQLString(tableName) != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getNoTrashSQLString(tableName);
            } else {
                query = query + " WHERE" + getNoTrashSQLString(tableName);
            }
        }
        return query;
    }

//    /**
//     * Define the owner of this Context
//     * @param parentobject
//     */
//    public void setOwner(DatabaseObject parentobject) {
//        this.parent = parentobject;
//    }
    /**
     * Remove all exclusive conditions or reuse them
     * @param bool
     */
    public void useExclusiveConditions(boolean bool) {
        exclusiveConditionsAvailable = bool;
    }

//    /**
//     * Add a self-table reference to this context
//     * @param referencekey
//     * @param referenceidkey
//     */
//    public void addReference(String referencekey, String referenceidkey) {
//        String alias = this.getDbIdentity();
//        references.add(new String[]{this.getDbIdentity(), referencekey, referenceidkey, alias});
//    }
    /**
     * Add a foreign table reference to this context
     * @param c
     */
    public void addReference(Context c) {
        if (!refs.contains(c)) {
            String alias = c.getDbIdentity();
            refs.add(c);
            references.add(new String[]{alias, "ids", alias + "ids", alias, this.getDbIdentity()});
        }
    }
    List<Context> refs = new Vector<Context>();

    /**
     * Add a foreign table reference to this context<br/><br/>
     *  Context c= Context.getFilesToContacts();<br/>
     *  c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");<br/>
     * @param referencetable The table which will be joined
     * @param referencekey The key column of the joined table
     * @param referenceidkey They key column in the original table
     */
    public void addReference(String referencetable, String referencekey, String referenceidkey) {
        synchronized (this) {
            String alias = referencetable;
            references.add(new String[]{referencetable, referencekey, referenceidkey, alias, getDbIdentity()});
        }
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
//
//    public String[] getSearchHeaders() {
//        return searchHeaders;
//    }
//
//    public DatabaseObject getParent() {
//        return parent;
//    }

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
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " ON " + references.get(i)[3] + "." + references.get(i)[1] + " = " + references.get(i)[3] + "." + references.get(i)[2];
                } else if (references.get(i).length == 5) {
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " ON " + references.get(i)[3] + "." + references.get(i)[1] + " = " + references.get(i)[4] + "." + references.get(i)[2];
                }
            }
        }
        return cond;
    }

    /**
     * @return the Company
     */
    public boolean isCompany() {
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

    /**
     * Returns an empty "sample" Object of the context type
     * @param <T> 
     * @return An empty {@link DatabaseObject}
     */
    @SuppressWarnings("unchecked")
    public <T extends DatabaseObject> T getSampleObject() {
        return (T) DatabaseObject.getObject(this);
    }
////////////////////////////////////////////////////////////////////////////////
//generate a new Context Object on each call here, callers may alter them to variants

    public static synchronized Context getContactsCompanies() {
        Context c = new Context();
        c.setCompany(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
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
    public static synchronized Context getItem(Integer type, Integer status) {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
//        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
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

    public static synchronized Context getItem() {
        return getItem(null, null);
    }

    public static synchronized Context getSubItem() {
        Context subitem = new Context();
        subitem.setSubID(DEFAULT_SUBID);
        subitem.setDbIdentity(IDENTITY_SUBITEMS);
        subitem.setIdentityClass(IDENTITY_SUBITEMS_CLASS);
        subitem.setId(2);


        return subitem;
    }

    public static synchronized Context getCustomer() {
        Context c = new Context();
        c.setCustomer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(3);

        return c;
    }

    public static synchronized Context getManufacturer() {
        Context c = new Context();
        c.setManufacturer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(4);

        return c;
    }

    public static synchronized Context getSupplier() {
        Context c = new Context();
        c.setSupplier(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(5);

        return c;
    }

    public static synchronized Context getContact() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(6);

        return c;
    }

    public static synchronized Context getUser() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_USERS);
        c.setSearchFields(DEFAULT_USER_SEARCH);
//        c.setSearchHeaders(Headers.USER_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_USERS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_USER;
        c.setId(7);

        return c;
    }

    public static synchronized Context getSchedule() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SCHEDULE);
        c.setIdentityClass(Schedule.class);
        c.setId(8);

        return c;
    }

    public static synchronized Context getLanguage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LANGUAGES);
        c.setId(9);

        return c;
    }

    public static synchronized Context getFiles() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES);
        c.setId(10);

        return c;
    }

    public static synchronized Context getLock() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LOCK);
        c.setId(11);

        return c;
    }

    public static synchronized Context getGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GROUPS);
        c.setIdentityClass(Group.class);
        c.uniqueColumns = UNIQUECOLUMNS_GROUPS;
        c.setId(14);

        return c;
    }

    public static synchronized Context getFavourite() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FAVS);
        c.setIdentityClass(Favourite.class);
        c.setId(15);

        return c;
    }

    public static synchronized Context getAddress() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ADDRESS);
        c.setIdentityClass(IDENTITY_ADDRESS_CLASS);
        c.setId(16);

        return c;
    }

    public static synchronized Context getInvoice() {
        return getItem(Item.TYPE_BILL, Item.STATUS_IN_PROGRESS);
    }

    public static synchronized Context getOrder() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
//        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_ORDER);
        c.setId(18);

        return c;
    }

    public static synchronized Context getOffer() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
//        c.setSearchHeaders(Headers.ITEM_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_OFFER);
        c.setId(19);

        return c;
    }

    public static synchronized Context getFilesToContacts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_CONTACTS);
        c.setIdentityClass(IDENTITY_CONTACTS_FILES_CLASS);
        c.setId(20);

        return c;
    }

    public static synchronized Context getHistory() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_HISTORY);
        c.setIdentityClass(HISTORY_ITEMS_CLASS);
        c.setId(21);

        return c;
    }

    public static synchronized Context getCountries() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_COUNTRIES);
        c.setId(22);

        return c;
    }

    public static synchronized Context getProduct() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTS);
        c.setIdentityClass(IDENTITY_PRODUCTS_CLASS);
        c.setSearchFields(DEFAULT_PRODUCT_SEARCH);
        c.setId(23);

        return c;
    }

    public static synchronized Context getSearchIndex() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SEARCHINDEX);
        c.setId(24);

        return c;
    }

    public static synchronized Context getPluginsToUsers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS_TO_USERS);
        c.setIdentityClass(IDENTITY_USER_PLUGINS_CLASS);
        c.setId(25);

        return c;
    }

    public static synchronized Context getPlugins() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS);
        c.setIdentityClass(IDENTITY_PLUGINS_CLASS);
        c.setId(26);

        return c;
    }

    public static synchronized Context getUserProperties() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PROPERTIES_TO_USERS);
        c.setIdentityClass(IDENTITY_PROPERTIES_CLASS);
        c.setId(27);

        return c;
    }

    public static synchronized Context getAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ACCOUNTS);
        c.setIdentityClass(IDENTITY_ACCOUNTS_CLASS);
        c.setId(28);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static synchronized Context getItemsToAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS_TO_ACCOUNTS);
        c.setId(29);

        return c;
    }

    public static synchronized Context getMessage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_MAIL);
        c.setIdentityClass(MailMessage.class);
        c.setId(30);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static synchronized Context getProductListItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTSLISTITEMS);
        c.setIdentityClass(IDENTITY_ITEMSLIST_CLASS);
        c.setId(32);

        return c;
    }

    public static synchronized Context getFormats() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FORMATS_T_USERS);
        c.setId(33);

        return c;
    }

    public static synchronized Context getFilesToItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_ITEMS);
        c.setIdentityClass(IDENTITY_ITEM_FILES_CLASS);
        c.setId(34);

        return c;
    }

    public static synchronized Context getTaxes() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TAX);
        c.setIdentityClass(Tax.class);
        c.setId(36);

        return c;
    }

    public static synchronized Context getGlobalSettings() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GLOBALSETTINGS);
        c.setId(37);

        return c;
    }

    public static synchronized Context getProductGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PGROUPS);
        c.setIdentityClass(IDENTITY_PGROUPS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_GROUPS;
        c.setId(38);

        return c;
    }

    public static synchronized Context getFilesToProducts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_PRODUCTS);
        c.setIdentityClass(IDENTITY_PRODUCTS_FILES_CLASS);
        c.setId(20);

        return c;
    }

    public static synchronized Context getCompany() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_COMPANIES);
        c.setIdentityClass(IDENTITY_COMPANY_CLASS);
        c.setId(39);

        return c;
    }

    public static synchronized Context getWebShop() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WEBSHOPS);
        c.setIdentityClass(IDENTITY_WEBSHOP_CLASS);
        c.setId(40);

        return c;
    }

    public static synchronized Context getWebShopContactMapping() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WSMAPPING);
        c.setIdentityClass(WSContactsMapping.class);
        c.setId(41);

        return c;
    }

    public static synchronized Context getWebShopItemMapping() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WSIMAPPING);
        c.setIdentityClass(WSItemsMapping.class);
        c.setId(42);

        return c;
    }

    public static synchronized Context getTemplate() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TEMPLATES);
        c.setIdentityClass(IDENTITY_TEMPLATE_CLASS);
        c.setId(43);

        return c;
    }

    public static synchronized Context getTemplatesToUsers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TEMPLATES_TO_USERS);
        c.setId(44);

        return c;
    }

    public static synchronized Context getReminder() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_REMINDERS);
        c.setIdentityClass(IDENTITY_REMINDER_CLASS);
        c.setId(45);

        return c;
    }

    public static synchronized Context getStage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_STAGES);
        c.setIdentityClass(IDENTITY_STAGE_CLASS);
        c.setId(46);

        return c;
    }

    public static synchronized Context getRevenue() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_REVENUE);
        c.setIdentityClass(Revenue.class);
        c.setId(47);

        return c;
    }

    public static synchronized Context getExpense() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_EXPENSE);
        c.setIdentityClass(Expense.class);
        c.setId(48);

        return c;
    }

    public static synchronized Context getProductlist() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTSLIST);
        c.setSearchFields("DISTINCT ids,cname,description");
        c.setIdentityClass(ProductList.class);
        c.setId(48);

        return c;
    }

    public static synchronized Context getProductsToSuppliers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTS_TO_SUPPLIERS);
        c.setIdentityClass(ProductsToSuppliers.class);
        c.setId(49);

        return c;
    }

    public static synchronized Context getValueProperties() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_VALUE_PROPERTIES);
        c.setIdentityClass(IDENTITY_VALUEPROPERTY_CLASS);
        c.setId(50);

        return c;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param contextdbidentity
     * @return The matching context or null if not existing
     */
    public static synchronized Context getMatchingContext(String contextdbidentity) {
        if (contextdbidentity.toLowerCase().endsWith("ids")) {
            contextdbidentity = contextdbidentity.replace("ids", "");
        }
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getDbIdentity().equalsIgnoreCase(contextdbidentity)) {
                return context;
            }
        }
        Log.Debug(Context.class, "Context not found for name: " + contextdbidentity);
        return null;
    }

    /**
     *
     * @param contextId
     * @return The matching context or null if not existing
     */
    public static synchronized Context getMatchingContext(int contextId) {
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getId() == contextId) {
                return context;
            }
        }
        Log.Debug(Context.class, "Context not found for id: " + contextId);
        return null;
    }

    public void setSearchFields(String fields) {
        defResultFields = fields;
    }

    /**
     * @param identityClass the identityClass to set
     */
    public void setIdentityClass(Class identityClass) {
        this.identityClass = identityClass;
    }

    @Override
    public String toString() {
        return String.valueOf(dbIdentity).toUpperCase() + " [" + id + "]";
    }

    /**
     * Looks for a Context with the specified id
     * @param value
     * @return A Context or null if no matching Context was found
     */
    public static synchronized Context getByID(int value) {
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getId() == value) {
                return context;
            }
        }
        return null;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Context) || o == null) {
            return false;
        }
        if (((Context) o).getDbIdentity().equals(getDbIdentity()) && ((Context) o).getId() == getId()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.id;
        hash = 47 * hash + (this.dbIdentity != null ? this.dbIdentity.hashCode() : 0);
        return hash;
    }
}
