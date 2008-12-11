/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import mpv5.items.contacts.Contact;

/**
 *
 * @author Administrator
 */
public class Context {

    public static final String CONDITION_COMPANY = "company = 1";
    public static final String CONDITION_CUSTOMER = "customer = 1";
    public static final String CONDITION_MANUFACTURER = "manufacturer = 1";
    public static final String CONDITION_SUPPLIER = "supplier = 1";

    public static final String SEARCH_NAME = "cname";
    public static final String SEARCH_NUMBER = "cnumber";
    public static final String SEARCH_DETAILS = "!%&";
    public static final String SEARCH_ALL = "!**!";

    public static String DEFAULT_SUBID = "cname";
    public static String IDENTITY_CONTACTS = "contacts";
    private boolean Company = false;
    private boolean Customer = false;
    private boolean Manufacturer = false;
    private boolean Supplier = false;

    
    public Context(DatabaseObject parentobject){
    this.parent = parentobject;
    }

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
    private String defResultFields = "*";
    private DatabaseObject parent;

    /**
     * @return the dbIdentity
     */
    public String getDbIdentity() {
        return dbIdentity;
    }

    public String getDefaultSearchFields() {
        return defResultFields;
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
     * @param subID the subID to set
     */
    public void setSubID(String subID) {
        this.subID = subID;
    }

    public String getConditions() {
        String cond = "    ";
        if (isCompany()) {
            cond += "WHERE " + CONDITION_COMPANY + " AND ";
        }
        if (isCustomer()) {
            cond += "WHERE " + CONDITION_CUSTOMER + " AND ";
        }
        if (isManufacturer()) {
            cond += "WHERE " + CONDITION_MANUFACTURER + " AND ";
        }
        if (isSupplier()) {
            cond += "WHERE " + CONDITION_SUPPLIER + " AND ";
        }

        return cond.substring(0, cond.length() - 4);
    }

    /**
     * @return the Company
     */
    public boolean isCompany() {
        return Company;
    }

    /**
     * @param Company the Company to set
     */
    public void setCompany(boolean Company) {
        this.Company = Company;
    }

    /**
     * @return the Customer
     */
    public boolean isCustomer() {
        return Customer;
    }

    /**
     * @param Customer the Customer to set
     */
    public void setCustomer(boolean Customer) {
        this.Customer = Customer;
    }

    /**
     * @return the Manufacturer
     */
    public boolean isManufacturer() {
        return Manufacturer;
    }

    /**
     * @param Manufacturer the Manufacturer to set
     */
    public void setManufacturer(boolean Manufacturer) {
        this.Manufacturer = Manufacturer;
    }

    /**
     * @return the Supplier
     */
    public boolean isSupplier() {
        return Supplier;
    }

    /**
     * @param Supplier the Supplier to set
     */
    public void setSupplier(boolean Supplier) {
        this.Supplier = Supplier;
    }

    public static Context getCompany() {
        Context c = new Context(new Contact());
        c.setCompany(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        return c;
    }

    public static Context getCustomer() {
        Context c = new Context(new Contact());
        c.setCustomer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        return c;
    }

    public static Context getManufacturer() {
        Context c = new Context(new Contact());
        c.setManufacturer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        return c;
    }

    public static Context getSupplier() {
        Context c = new Context(new Contact());
        c.setSupplier(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        return c;
    }
}
