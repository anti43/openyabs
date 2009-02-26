/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.items.contacts;

import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti43
 */
public class Contact extends DatabaseObject {

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
    private Date dateadded = new Date();
    private String addedby = "";
    private String company = "";
    private String department = "";
    private boolean ismale = true;
    private boolean isenabled = true;
    private boolean iscompany = false;
    private boolean iscustomer = false;
    private boolean ismanufacturer = false;
    private boolean issupplier = false;

    public Contact() {
        context.setDbIdentity(Context.IDENTITY_CONTACTS);
        context.setIdentityClass(this.getClass());
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
    public String __getCName() {
        return cname;
    }

    /**
     * @param name the name to set
     */
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
     * @return the dateadded
     */
    public Date __getDateadded() {
        return dateadded;
    }

    /**
     * @param dateadded the dateadded to set
     */
    public void setDateadded(Date dateadded) {
        this.dateadded = dateadded;
    }

    /**
     * @return the addedby
     */
    public String __getAddedby() {
        return addedby;
    }

    /**
     * @param addedby the addedby to set
     */
    public void setAddedby(String addedby) {
        this.addedby = addedby;
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
//    /**
//     * @return the role
//     */
//    public Roles __getRole() {
//        return role;
//    }
//
//    /**
//     * @param role the role to set
//     */
//    public void setRole(Roles role) {
//        this.role = role;
//    }
//
//    public void toXML(){
//        FileOutputStream fileOut = null;
//
//        try {
//            fileOut = new FileOutputStream("bean.xml");
//            JOXBeanOutputStream joxOut = new JOXBeanOutputStream(fileOut);
//            joxOut.writeObject(this.getDbIdentity(), this);
//            joxOut.close();
//        } catch (IOException ex) {
//            Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                fileOut.close();
//            } catch (IOException ex) {
//                Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//    }
}
