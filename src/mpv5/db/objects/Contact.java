package mpv5.db.objects;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.Templateable;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.panels.ContactPanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.text.RandomText;

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
    private int payterm = 0;
    private boolean ismale = true;
    private boolean isenabled = true;
    private boolean iscompany = false;
    private boolean iscustomer = false;
    private boolean ismanufacturer = false;
    private boolean issupplier = false;
    private String country = "";
    private FormatHandler formatHandler;
//   public final static int TYPE_CONTACT = 0;
//   public final static int TYPE_CUSTOMER = 1;
//   public final static int TYPE_SUPPLIER = 2;
//   public final static int TYPE_MANUFACTURER = 3;

//    public static String getTypeString(int typ) {
//        switch (typ) {
//            case TYPE_CONTACT:
//                return Messages.TYPE_CONTACT.getValue();
//            case TYPE_CUSTOMER:
//                return Messages.TYPE_CUSTOMER.getValue();
//            case TYPE_SUPPLIER:
//                return Messages.TYPE_SUPPLIER.getValue();
//            case TYPE_MANUFACTURER:
//                return Messages.TYPE_MANUFACTURER.getValue();
//        }
//        return Messages.TYPE_CONTACT.getValue();
//    }
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

    /**
     * @return the payterm
     */
    public int __getPayterm() {
        return payterm;
    }

    /**
     * @param payterm the payterm to set
     */
    public void setPayterm(int payterm) {
        this.payterm = payterm;
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
        return getCname() + " (" + cnumber + ")";
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
    public Map<String, Object> resolveReferences(Map<String, Object> map) {

        map.put("address", getDefaultaddress());
        int i = 0;
        List<Address> data = getAdresses();
        for (Address a : data) {
            map.put("address" + i, a);
            i++;
        }
        map.put("invoiceaddress", getInvoiceaddress(data));
        map.put("deliveryaddress", getDeliveryaddress(data));

        if (!map.containsKey("country")) {
            try {
                map.put("country", LanguageManager.getCountryName(Integer.valueOf("country")));
            } catch (Exception numberFormatException) {
                //already resolved?
            }
        }

        try {
            if (ismale) {
                map.put("gender", Messages.CONTACT_TYPE_MALE.toString());
                map.put("intro", Messages.CONTACT_INTRO_MALE.toString());
            } else {
                map.put("gender", Messages.CONTACT_TYPE_FEMALE.toString());
                map.put("intro", Messages.CONTACT_INTRO_FEMALE.toString());
            }
            
            if(title.length()==0){
                 map.put("title", map.get("gender"));
            }
        } catch (Exception numberFormatException) {
            //already resolved?
        }

        if (!map.containsKey("fullname")) {
            try {
                if (prename != null && prename.length() > 0) {
                    map.put("fullname", prename + " " + getCname());
                } else {
                    map.put("fullname", getCname());
                }
            } catch (Exception numberFormatException) {
                //already resolved?
            }
        }
        Log.Debug(this, map);
        return super.resolveReferences(map);
    }

    @Override
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
     *
     * @return A (possibly empty) list of {@link ValueProperty}s
     */
    public List<ValueProperty> getProperties() {
        return ValueProperty.getProperties(this);
    }

    @Override
    public boolean delete() {
        try {
            List<DatabaseObject> it = getReferencedObjects(this, Context.getOffer(), getObject(Context.getOffer()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getOrder(), getObject(Context.getOrder()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getInvoice(), getObject(Context.getInvoice()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getDeposit(), getObject(Context.getDeposit()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getPartPayment(), getObject(Context.getPartPayment()));
            for (int i = 0; i < it.size(); i++) {
                it.get(i).delete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getCredit(), getObject(Context.getCredit()));
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
            List<DatabaseObject> it = getReferencedObjects(this, Context.getOffer(), getObject(Context.getOffer()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getOrder(), getObject(Context.getOrder()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getInvoice(), getObject(Context.getInvoice()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getDeposit(), getObject(Context.getDeposit()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getPartPayment(), getObject(Context.getPartPayment()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
            it.clear();
            it = getReferencedObjects(this, Context.getCredit(), getObject(Context.getCredit()), true);
            for (int i = 0; i < it.size(); i++) {
                it.get(i).undelete();
            }
        } catch (Exception e) {
        }

        return super.undelete();
    }

    @Override
    public int templateType() {
        return Constants.TYPE_CONTACT;
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
        return (__getStreet() == null ? __getStreet() : "") + ", " + (__getCity() == null ? __getCity() : "");
    }

    @Persistable(false)
    public Address getInvoiceaddress() {
        return getInvoiceaddress(null);
    }

    @Persistable(false)
    public Address getInvoiceaddress(List<Address> from) {
        List<Address> data = from == null ? getAdresses() : from;
        for (Address data1 : data) {
            if (data1.__getInttype() == 0 || data1.__getInttype() == 2) {
                return data1;
            }
        }

        return getDefaultaddress();
    }

    @Persistable(false)
    public Address getDeliveryaddress() {
        return getDeliveryaddress(null);
    }

    @Persistable(false)
    public Address getDeliveryaddress(List<Address> from) {

        List<Address> data = from == null ? getAdresses() : from;
        for (Address data1 : data) {
            if (data1.__getInttype() == 1 || data1.__getInttype() == 2) {
                return data1;
            }
        }

        return getDefaultaddress();
    }

    @Persistable(false)
    public List<Address> getAdresses() {
        List<Address> data = Collections.EMPTY_LIST;
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getAddress(), new Address());
            Collections.sort(data, new Comparator<Address>() {
//[0 = billing adress, 1 = delivery adress, 2 = both, 3 = undefined]
                @Override
                public int compare(Address o1, Address o2) {
                    return Integer.valueOf(o1.__getInttype()).compareTo(o2.__getInttype());
                }
            });

        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
        return data;

    }

    @Persistable(false)
    public Address getDefaultaddress() {
        Address def = new Address();
        def.setCity(city);
        def.setCountry(country);
        def.setDepartment(department);
        def.setCompany(company);
        def.setCname(getCname());
        def.setIsmale(ismale);
        def.setPrename(prename);
        def.setStreet(street);
        def.setTaxnumber(taxnumber);
        def.setTitle(title);
        def.setZip(zip);
        def.setContactsids(ids);
        Log.Debug(this, "returning default address");
        return def;
    }

    public static Contact findByCname(String cn) {
        try {
            return (Contact) DatabaseObject.getObject(Context.getContact(), cn);
        } catch (NodataFoundException ex) {
            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Safely import a database object from external sources (xml, csv etc)
     * Override this for ensuring the existance of DObject specific mandatory
     * values.
     *
     * @return
     */
    @Override
    public boolean saveImport() {
        Log.Debug(this, "Starting import..");
        Log.Debug(this, "Setting IDS to -1");
        ids = -1;
        Log.Debug(this, "Setting intaddedby to " + mpv5.db.objects.User.getCurrentUser().__getIDS());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());

        if (__getGroupsids() <= 0 || !DatabaseObject.exists(Context.getGroup(), __getGroupsids())) {
            Log.Debug(this, "Setting groups to users group.");
            setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        }

        if (__getCname() == null || __getCname().trim().length() < 1) {
            setCname("Import name missing");
        }
        if (__getCNumber() == null) {
            setCNumber(RandomText.getText());
        }

        return save();
    }
}
