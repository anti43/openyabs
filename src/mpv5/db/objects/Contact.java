package mpv5.db.objects;

import enoa.handler.TemplateHandler;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.Templateable;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.panels.ContactPanel;
import mpv5.utils.images.MPIcon;

/**
 *
 * 
 */
public class Contact extends DatabaseObject implements Formattable, Templateable {
    private static final long serialVersionUID = 1L;

    private String cnumber = "";
    private String taxnumber = "";
    private String title = "";
    private String prename = "";
    private String street = "";
    private String zip = "";
    private String city = "";
    private String mainphone = "";
    private String workphone = "";
    private String fax = "";
    private String mobilephone = "";
    private String mailaddress = "";
    private String website = "";
    private String notes = "";
    private String company = "";
    private String department = "";
    private String bankaccount = "";
    private String bankid = "";
    private String bankname = "";
    private String bankcurrency = "";
    private String bankcountry = "";
    private boolean ismale = true;
    private boolean isenabled = true;
    private boolean iscompany = false;
    private boolean iscustomer = false;
    private boolean ismanufacturer = false;
    private boolean issupplier = false;
    private String country = "";
    private FormatHandler formatHandler;
    public final static int TYPE_CONTACT = 0;
    public final static int TYPE_CUSTOMER = 1;
    public final static int TYPE_SUPPLIER = 2;
    public final static int TYPE_MANUFACTURER = 3;

    public static String getTypeString(int typ) {
        switch (typ) {
            case TYPE_CONTACT:
                return Messages.TYPE_CONTACT.getValue();
            case TYPE_CUSTOMER:
                return Messages.TYPE_CUSTOMER.getValue();
            case TYPE_SUPPLIER:
                return Messages.TYPE_SUPPLIER.getValue();
            case TYPE_MANUFACTURER:
                return Messages.TYPE_MANUFACTURER.getValue();
        }
        return Messages.TYPE_CONTACT.getValue();
    }

    public Contact() {
        setContext(Context.getContact());
    }

    /**
     * @return the number
     */
    public String __getCNumber() {
        return cnumber;
    }

    /**
     * @param number the number to set
     */
    public void setCNumber(String number) {
        this.cnumber = number;
    }

    /**
     * @return the taxnumber
     */
    public String __getTaxnumber() {
        return taxnumber;
    }

    /**
     * @param taxnumber
     */
    public void settaxnumber(String taxnumber) {
        this.taxnumber = taxnumber;
    }

    /**
     * @return the title
     */
    public String __getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the prename
     */
    public String __getPrename() {
        return prename;
    }

    /**
     * @param prename the prename to set
     */
    public void setPrename(String prename) {
        this.prename = prename;
    }

    /**
     * @return the name
     */
    @Override
    public String __getCName() {
        return cname;
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setCName(String name) {
        this.cname = name;
    }

    /**
     * @return the street
     */
    public String __getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the zip
     */
    public String __getZip() {
        return zip;
    }

    /**
     * @param zip the zip to set
     */
    public void setZip(String zip) {
        this.zip = zip;
    }

    /**
     * @return the city
     */
    public String __getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the phone
     */
    public String __getMainphone() {
        return mainphone;
    }

    /**
     * @param phone the phone to set
     */
    public void setMainphone(String phone) {
        this.mainphone = phone;
    }

    /**
     * @return the workphone
     */
    public String __getWorkphone() {
        return workphone;
    }

    /**
     * @param workphone the workphone to set
     */
    public void setWorkphone(String workphone) {
        this.workphone = workphone;
    }

    /**
     * @return the fax
     */
    public String __getFax() {
        return fax;
    }

    /**
     * @param fax the fax to set
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * @return the mobilephone
     */
    public String __getMobilephone() {
        return mobilephone;
    }

    /**
     * @param mobilephone the mobilephone to set
     */
    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    /**
     * @return the mailaddress
     */
    public String __getMailaddress() {
        return mailaddress;
    }

    /**
     * @param mailaddress the mailaddress to set
     */
    public void setMailaddress(String mailaddress) {
        this.mailaddress = mailaddress;
    }

    /**
     * @return the website
     */
    public String __getWebsite() {
        return website;
    }

    /**
     * @param website the website to set
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * @return the notes
     */
    public String __getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the male
     */
    public boolean __getisMale() {
        return ismale;
    }

    /**
     * @param male the male to set
     */
    public void setisMale(boolean male) {
        this.ismale = male;
    }

    /**
     * @return the enabled
     */
    public boolean __getisEnabled() {
        return isenabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setisEnabled(boolean enabled) {
        this.isenabled = enabled;
    }

    /**
     * @return the company
     */
    public boolean __getisCompany() {
        return iscompany;
    }

    /**
     * @param company the company to set
     */
    public void setisCompany(boolean company) {
        this.iscompany = company;
    }

    /**
     * @return the iscustomer
     */
    public boolean __getIscustomer() {
        return iscustomer;
    }

    /**
     * @param iscustomer the iscustomer to set
     */
    public void setisCustomer(boolean iscustomer) {
        this.iscustomer = iscustomer;
    }

    /**
     * @return the ismanufacturer
     */
    public boolean __getIsmanufacturer() {
        return ismanufacturer;
    }

    /**
     * @param ismanufacturer the ismanufacturer to set
     */
    public void setisManufacturer(boolean ismanufacturer) {
        this.ismanufacturer = ismanufacturer;
    }

    /**
     * @return the issupplier
     */
    public boolean __getIssupplier() {
        return issupplier;
    }

    /**
     * @param issupplier the issupplier to set
     */
    public void setisSupplier(boolean issupplier) {
        this.issupplier = issupplier;
    }

    /**
     * @return the company
     */
    public String __getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @return the department
     */
    public String __getDepartment() {
        return department;
    }

    /**
     * @param dep
     */
    public void setDepartment(String dep) {
        this.department = dep;
    }

    /**
     * @return the country
     */
    public String __getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public JComponent getView() {
        ContactPanel x = new ContactPanel(getContext());
        return (JComponent) x;
    }

    @Override
    public MPIcon getIcon() {
        return new MPIcon("/mpv5/resources/images/22/evolution-contacts.png");
    }

    @Override
    public String toString() {
        return cname + " (" + cnumber + ")";
    }

    /**
     * @return the formatHandler
     */
    @Override
    public FormatHandler getFormatHandler() {
        if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }

    @Override
    public void ensureUniqueness() {
//       if (cnumber == null || cnumber.length() == 0) {
        setCNumber(getFormatHandler().next());
//        }
    }

    @Override
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {
        super.resolveReferences(map);
        List<Address> data;
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getAddress(), new Address());
            Collections.sort(data, new Comparator<Address>() {
//[0 = billing adress, 1 = delivery adress, 2 = both, 3 = undefined]
                public int compare(Address o1, Address o2) {
                    return Integer.valueOf(o1.__getInttype()).compareTo(Integer.valueOf(o2.__getInttype()));
                }
            });
            for (int i = 0; i < data.size(); i++) {
                map.put("address" + i, data.get(i));
            }
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }

        if (map.containsKey("country")) {
            try {
                map.put("country", LanguageManager.getCountryName(Integer.valueOf(map.get("country").toString())));
            } catch (Exception numberFormatException) {
                //already resolved?
            }
        }

        try {
            if (map.containsKey("ismale")) {
                if (Boolean.valueOf(map.get("ismale").toString())) {
                    map.put("gender", Messages.CONTACT_TYPE_MALE.toString());
                    map.put("intro", Messages.CONTACT_INTRO_MALE.toString());
                } else {
                    map.put("gender", Messages.CONTACT_TYPE_FEMALE.toString());
                    map.put("intro", Messages.CONTACT_INTRO_FEMALE.toString());
                }

            }
        } catch (Exception numberFormatException) {
            //already resolved?
        }

        if (!map.containsKey("fullname")) {
            try {
                if (prename != null && prename.length() > 0) {
                    map.put("fullname", prename + " " + cname);
                } else {
                    map.put("fullname", cname);
                }
            } catch (Exception numberFormatException) {
                //already resolved?
            }
        }

        return map;
    }

    public void defineFormatHandler(FormatHandler handler) {
        formatHandler = handler;
    }

    /**
     * @return the bankaccount
     */
    public String __getBankaccount() {
        return bankaccount;
    }

    /**
     * @param bankaccount the bankaccount to set
     */
    public void setBankaccount(String bankaccount) {
        this.bankaccount = bankaccount;
    }

    /**
     * @return the bankid
     */
    public String __getBankid() {
        return bankid;
    }

    /**
     * @param bankid the bankid to set
     */
    public void setBankid(String bankid) {
        this.bankid = bankid;
    }

    /**
     * @return the bankname
     */
    public String __getBankname() {
        return bankname;
    }

    /**
     * @param bankname the bankname to set
     */
    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    /**
     * @return the bankcurrency
     */
    public String __getBankcurrency() {
        return bankcurrency;
    }

    /**
     * @param bankcurrency the bankcurrency to set
     */
    public void setBankcurrency(String bankcurrency) {
        this.bankcurrency = bankcurrency;
    }

    /**
     * @return the bankcountry
     */
    public String __getBankcountry() {
        return bankcountry;
    }

    /**
     * @param bankcountry the bankcountry to set
     */
    public void setBankcountry(String bankcountry) {
        this.bankcountry = bankcountry;
    }

    /**
     * Fetches all properties for this contact from the db
     * @return A (possibly empty) list of {@link ValueProperty}s
     */
    public List<ValueProperty> getProperties() {
        return ValueProperty.getProperties(this);
    }

    @Override
    public boolean delete() {
        try {
            List<DatabaseObject> it = getReferencedObjects(this, Context.getItem(), getObject(Context.getItem()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
        } catch (Exception e) {
        }

        return super.delete();
    }

    @Override
    public boolean undelete() {
        try {
            List<DatabaseObject> it = getReferencedObjects(this, Context.getItem(), getObject(Context.getItem()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
        } catch (Exception e) {
        }

        return super.undelete();
    }


    @Override
    public int templateType() {
        return TemplateHandler.TYPE_CONTACT;
    }

    /**
     *
     * @return
     */
    @Override
    public int templateGroupIds() {
        return __getGroupsids();
    }

    @Persistable(false)
    public String getInfoString() {
        return (__getStreet()==null?__getStreet():"") + ", " + (__getCity()==null?__getCity():"") ;
    }
}
