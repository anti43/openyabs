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
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 *
 *
 */
public class Address extends DatabaseObject {

    private static final long serialVersionUID = 9171590645272392699L;

    private String taxnumber = "";
    private String title = "";
    private String prename = "";
    private String street = "";
    private String zip = "";
    private String city = "";
    private String company = "";
    private String department = "";
    private String country = "";
    private boolean ismale = true;
    private int contactsids = 0;
    private int inttype = 2;// [0 = billing adress, 1 = delivery adress, 2 = both, 3 = undefined]
    public static final int TYPE_INVOICE = 0;
    public static final int TYPE_DELIVERY = 1;
    public static final int TYPE_ALL = 2;
    public static final int TYPE_NONE = 3;

    public Address() {
        setContext(Context.getAddress());
    }

    /**
     * @return the taxnumber
     */
    public String __getTaxnumber() {
        return taxnumber;
    }

    /**
     * @param taxnumber the taxnumber to set
     */
    public void setTaxnumber(String taxnumber) {
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
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * @return the ismale
     */
    public boolean __getIsmale() {
        return ismale;
    }

    /**
     * @param ismale the ismale to set
     */
    public void setIsmale(boolean ismale) {
        this.ismale = ismale;
    }

    /**
     * @return the contactsids
     */
    public int __getContactsids() {
        return contactsids;
    }

    /**
     * @param contactsids the contactsids to set
     */
    public void setContactsids(int contactsids) {
        this.contactsids = contactsids;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    /**
     * @return the inttype
     */
    public int __getInttype() {
        return inttype;
    }

    /**
     * @param inttype the inttype to set
     */
    public void setInttype(int inttype) {
        this.inttype = inttype;
    }

    @Override
    public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {
        try {
            if (map.containsKey("inttype")) {
                // [0 = billing adress, 1 = delivery adress, 2 = both, 3 = undefined]
                switch (__getInttype()) {
                    case TYPE_ALL:
                        map.put("type", Messages.ADDRESS_TYPE_BOTH);
                        break;
                    case TYPE_DELIVERY:
                        map.put("type", Messages.ADDRESS_TYPE_DELIVERY);
                        break;
                    case TYPE_INVOICE:
                        map.put("type", Messages.ADDRESS_TYPE_INVOICE);
                        break;
                    default:
                        break;
                }

                map.remove("inttype");
            }
        } catch (Exception n) {
            //already resolved?
            Log.Debug(n);
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
                    map.put("fullname", prename + " " + getCname());
                } else {
                    map.put("fullname", getCname());
                }
            } catch (Exception numberFormatException) {
                //already resolved?
            }
        }
        return super.resolveReferences(map);
    }

    public String getDisplayName() {
        if (__getCname() == null || __getCname().length() == 0) {
            return "";
        }

        if (prename == null || prename.length() == 0) {
            return __getCname();
        }

        if (city == null || city.length() == 0) {
            return getCname() + ", " + prename;
        }

        if (company == null || company.length() == 0) {
            return getCname() + ", " + prename + " (" + city + ")";
        }

        return getCname() + ", " + prename + " (" + city + ") " + company;
    }
}
