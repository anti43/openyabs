/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.util.ArrayList;
import java.util.Arrays;
import mpv5.globals.Headers;
import mpv5.items.div.Address;
import mpv5.items.div.Contact;
import mpv5.items.div.Item;
import mpv5.items.div.SubItem;
import mpv5.usermanagement.User;

/**
 *
 * @author anti43
 */
public class Context {

    //********** tablenames ****************************************************
    public static String IDENTITY_CONTACTS = "contacts";
    public static String IDENTITY_USERS = "users";
    public static String IDENTITY_ITEMS = "items";
    public static String IDENTITY_SUBITEMS = "subitems";
    public static String IDENTITY_LANGUAGES = "languages";
    public static String IDENTITY_FILES = "files";
    public static String IDENTITY_LOCK = "tablelock";
    public static String IDENTITY_FAVS = "favourites";
    public static String IDENTITY_ADDRESS = "addresses";
    public static String IDENTITY_GROUPS = "groups";
    public static String IDENTITY_GROUPS_TO_PARENTGROUP = "groupstoparents";

    //********** identity classes **********************************************
    private static Class IDENTITY_CONTACTS_CLASS = Contact.class;
    private static Class IDENTITY_ADDRESS_CLASS = Address.class;
    private static Class IDENTITY_USERS_CLASS = User.class;
    private static Class IDENTITY_ITEMS_CLASS = Item.class;
    private static Class IDENTITY_SUBITEMS_CLASS = SubItem.class;

    //********** unique constraints *******************************************
    public static String UNIQUECOLUMNS_USER = "cname";
    public static String UNIQUECOLUMNS_ITEMS = "cname";

    //********** conditions ****************************************************

    private boolean isCompany = false;
    private boolean isCustomer = false;
    private boolean isManufacturer = false;
    private boolean isSupplier = false;
    private boolean isDone = false;
    private boolean isActive = false;

    public static final String CONDITION_DEFAULT = "%%tablename%%" + "." + "IDS>0";
    public static final String CONDITION_CONTACTS_COMPANY = IDENTITY_CONTACTS + "." + "iscompany";
    public static final String CONDITION_CONTACTS_CUSTOMER = IDENTITY_CONTACTS + "." + "iscustomer";
    public static final String CONDITION_CONTACTS_MANUFACTURER = IDENTITY_CONTACTS + "." + "ismanufacturer";
    public static final String CONDITION_CONTACTS_SUPPLIER = IDENTITY_CONTACTS + "." + "issupplier";
    public static final String CONDITION_ITEMS_DONE = IDENTITY_ITEMS + "." + "isfinished";
    public static final String CONDITION_ITEMS_ACTIVE = IDENTITY_ITEMS + "." + "isactive";

    //********** searchfields **************************************************
    public static final String SEARCH_NAME = "cname";
    public static final String SEARCH_CONTACT_NUMBER = "cnumber";
    public static final String SEARCH_CONTACT_CITY = "city";


    //********** defaults ******************************************************
    public static String DEFAULT_SUBID = "ids, cname";
    public static String DEFAULT_CONTACT_SEARCH = "ids, cnumber, cname, city";
    public static String DEFAULT_USER_SEARCH = "ids, cname, mail, lastlogdate";
    public static String DEFAULT_ITEM_SEARCH = "ids, cname, dateadded, value";

    //********** table fields ********************************************************
    public static String DETAILS_CONTACTS = IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER," +
            IDENTITY_CONTACTS + "." + "TITLE," + IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME," +
            IDENTITY_CONTACTS + "." + "STREET," + IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY," +
            IDENTITY_CONTACTS + "." + "MAINPHONE," + IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE," +
            IDENTITY_CONTACTS + "." + "WORKPHONE," + IDENTITY_CONTACTS + "." + "MAILADDRESS," +
            IDENTITY_CONTACTS + "0." + "CNAME," + IDENTITY_CONTACTS + "." + "WEBSITE," + IDENTITY_CONTACTS + "." + "NOTES," +
            IDENTITY_CONTACTS + "." + "taxnumber," + IDENTITY_CONTACTS + "." + "DATEADDED," + IDENTITY_CONTACTS + "." + "ISACTIVE," +
            IDENTITY_CONTACTS + "." + "ISCUSTOMER," + IDENTITY_CONTACTS + "." + "ISMANUFACTURER," + IDENTITY_CONTACTS + "." + "ISSUPPLIER," +
            IDENTITY_CONTACTS + "." + "ISCOMPANY," + IDENTITY_CONTACTS + "." + "ISMALE," + IDENTITY_CONTACTS + "." + "ISENABLED," +
            IDENTITY_CONTACTS + "." + "ADDEDBY," + IDENTITY_CONTACTS + "." + "RESERVE1," + IDENTITY_CONTACTS + "." + "RESERVE2," +
            IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER," + IDENTITY_CONTACTS + "." + "TITLE," +
            IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME," + IDENTITY_CONTACTS + "." + "STREET," +
            IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY," + IDENTITY_CONTACTS + "." + "MAINPHONE," +
            IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE," + IDENTITY_CONTACTS + "." + "WORKPHONE," +
            IDENTITY_CONTACTS + "." + "MAILADDRESS," + IDENTITY_CONTACTS + "." + "company," + IDENTITY_CONTACTS + "." + "WEBSITE," +
            IDENTITY_CONTACTS + "." + "NOTES," + IDENTITY_CONTACTS + "." + "taxnumber," + IDENTITY_CONTACTS + "." + "DATEADDED," +
            IDENTITY_CONTACTS + "." + "ISACTIVE," + IDENTITY_CONTACTS + "." + "ISCUSTOMER," + IDENTITY_CONTACTS + "." + "ISMANUFACTURER," +
            IDENTITY_CONTACTS + "." + "ISSUPPLIER," + IDENTITY_CONTACTS + "." + "ISCOMPANY," + IDENTITY_CONTACTS + "." + "ISMALE," +
            IDENTITY_CONTACTS + "." + "ISENABLED," + IDENTITY_CONTACTS + "." + "ADDEDBY";
    public static String DETAILS_USERS = IDENTITY_USERS + "." + "IDS," + IDENTITY_USERS + "." + "CNAME," +
            IDENTITY_USERS + "." + "fullname," +
            IDENTITY_USERS + "." + "mail," +
            IDENTITY_USERS + "." + "isenabled," +
            IDENTITY_USERS + "." + "isloggedin," +
            IDENTITY_USERS + "." + "locale," +
            IDENTITY_USERS + "." + "language," +
            IDENTITY_USERS + "." + "laf," +
            IDENTITY_USERS + "." + "inthighestright," +
            IDENTITY_USERS + "." + "datelastlog";
  public static String DETAILS_ITEMS = IDENTITY_ITEMS + "." + "IDS," + IDENTITY_ITEMS + "." + "CNAME," +
            IDENTITY_CONTACTS + "." + "CNAME," + IDENTITY_ITEMS + "."  + "dateadded," + IDENTITY_ITEMS + "." + "isactive," +
            IDENTITY_ITEMS + "." + "value," +
            IDENTITY_ITEMS + "." + "taxvalue, " +
            IDENTITY_ITEMS + "." + "datetodo, " +
            IDENTITY_ITEMS + "." + "intreminders";

    //**************************************************************************
    public static ArrayList<Context> getSecuredContexts() {
        ArrayList<Context> list = new ArrayList<Context>();
        list.add(getCompany());
//        list.add(getUser()); Needs to be non-secure, to update user details on close
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getAddress());
        list.add(getItem(true, false));
        list.add(getSubItem());
        return list;
    }
    /**
     * A list of all available contexts
     */
    public static ArrayList<Context> allContexts = new ArrayList<Context>(Arrays.asList(new Context[]{
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
                getItem(true, false),
                getSubItem(),
                getGroup(),
                getGroupToParentGroup()
            }));

    private String[] searchHeaders;
    private ArrayList<String[]> references = new ArrayList<String[]>();
    private boolean exclusiveConditionsAvailable = false;
    private String exclusiveCondition;
    private String uniqueColumns;

    /**
     * Create a new Context instance with the given do as owner
     * @param parentobject
     */
    public Context(DatabaseObject parentobject) {
        setOwner(parentobject);
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

    public void setContactConditions(boolean customer, boolean supplier, boolean manufacturer, boolean company) {
        setCustomer(customer);
        setSupplier(supplier);
        setManufacturer(manufacturer);
        setCompany(company);
    }



    /**
     * Set conditions to get exclusive data
     * @param done
     * @param active
     */
    public void setExclusiveItemConditions(boolean done, boolean active) {

        String cond = "    ";
        boolean first = true;

        if (done) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_ITEMS_DONE + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_ITEMS_DONE + "=0 AND ";
        }
        if (active) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_ITEMS_ACTIVE + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_ITEMS_ACTIVE+ "=0 AND ";
        }


        if (!first) {
            cond = cond.substring(4, cond.length() - 4);
        } else {
            cond = "WHERE " + CONDITION_DEFAULT;
        }

        exclusiveCondition = cond;
    }

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
        } else {
            cond = "WHERE " + CONDITION_DEFAULT;
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
                if (isIsActive()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_ITEMS_ACTIVE + "=1 OR ";
            }
                if (isIsDone()) {
                if (first) {
                    cond += "WHERE ";
                }
                first = false;
                cond += " " + CONDITION_ITEMS_DONE + "=1 OR ";
            }

            if (!first) {
                cond = cond.substring(4, cond.length() - 3);
            } else {
                cond = "WHERE " + CONDITION_DEFAULT;
            }
            return cond;
        } else {
            return exclusiveCondition.toString();
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
     * @param referencetable
     * @param referencekey
     * @param referenceidkey
     */
    public void addReference(String referencetable, String referencekey, String referenceidkey) {
        String alias = referencetable;
        references.add(new String[]{referencetable, referencekey, referenceidkey, alias});
    }

    /**
     * Add a foreign table reference to this context
     * @param referencetable
     * @param referencekey
     * @param referenceidkey
     * @param calledtable
     */
    public void addReference(String referencetable, String referencekey, String referenceidkey, String calledtable) {
        String alias = referencetable;
        references.add(new String[]{referencetable, referencekey, referenceidkey, alias, calledtable});
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
     * @return the isDone
     */
    private boolean isIsDone() {
        return isDone;
    }

    /**
     * @param isDone the isDone to set
     */
    private void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    /**
     * @return the isActive
     */
    private boolean isIsActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    private void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public static Context getCompany() {
        Context c = new Context(new Contact());
        c.setCompany(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT);
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);

        return c;
    }

     public static Context getItem(boolean active, boolean done) {
        Context c = new Context(new Item());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setSearchHeaders(Headers.ITEM_DEFAULT);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setIsActive(active);
        c.setIsDone(done);

        return c;
    }

       public static Context getSubItem() {
        Context c = new Context(new SubItem());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SUBITEMS);
        c.setIdentityClass(IDENTITY_SUBITEMS_CLASS);

        return c;
    }

    public static Context getCustomer() {
        Context c = new Context(new Contact());
        c.setCustomer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT);
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);

        return c;
    }

    public static Context getManufacturer() {
        Context c = new Context(new Contact());
        c.setManufacturer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT);
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);

        return c;
    }

    public static Context getSupplier() {
        Context c = new Context(new Contact());
        c.setSupplier(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT);
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);

        return c;
    }

    public static Context getContact() {
        Context c = new Context(new Contact());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
        c.setSearchHeaders(Headers.CONTACT_DEFAULT);
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);

        return c;
    }

    public static Context getUser() {
        Context c = new Context(new User());
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_USERS);
        c.setSearchFields(DEFAULT_USER_SEARCH);
        c.setSearchHeaders(Headers.USER_DEFAULT);
        c.setIdentityClass(IDENTITY_USERS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_USER;

        return c;
    }

    public static Context getLanguage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LANGUAGES);

        return c;
    }

    public static Context getFiles() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES);

        return c;
    }

    public static Context getLock() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LOCK);

        return c;
    }

     public static Context getGroupToParentGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GROUPS_TO_PARENTGROUP);

        return c;
    }

    public static Context getGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GROUPS);

        return c;
    }

    public static Context getFavourites() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FAVS);

        return c;
    }

    public static Context getAddress() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ADDRESS);
        c.setIdentityClass(IDENTITY_ADDRESS_CLASS);
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
        return dbIdentity;
    }


}
