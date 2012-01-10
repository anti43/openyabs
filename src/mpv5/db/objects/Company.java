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
import mpv5.utils.images.MPIcon;

/**
 * Represents a user company
 */
public class Company extends DatabaseObject {

    private String state = "";
    private String phoneprefix = "";
    private String business = "";
    private String taxadvisor = "";
    private String city = "";
    private String taxadvjob = "";
    private String street = "";
    private String stb = "";
    private String email = "";
    private String name = "";
    private String zipcode = "";
    private String phone = "";
    private String firstname = "";
    private String taxauthority = "";
    private String taxnumber = "";
    private String taxadvmandant = "";

    public Company() {
        setContext(Context.getCompany());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    /**
     * @return the state
     */
    public String __getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the phoneprefix
     */
    public String __getPhoneprefix() {
        return phoneprefix;
    }

    /**
     * @param phoneprefix the phoneprefix to set
     */
    public void setPhoneprefix(String phoneprefix) {
        this.phoneprefix = phoneprefix;
    }

    /**
     * @return the business
     */
    public String __getBusiness() {
        return business;
    }

    /**
     * @param business the business to set
     */
    public void setBusiness(String business) {
        this.business = business;
    }

    /**
     * @return the taxadvisor
     */
    public String __getTaxadvisor() {
        return taxadvisor;
    }

    /**
     * @param taxadvisor the taxadvisor to set
     */
    public void setTaxadvisor(String taxadvisor) {
        this.taxadvisor = taxadvisor;
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
     * @return the taxadvjob
     */
    public String __getTaxadvjob() {
        return taxadvjob;
    }

    /**
     * @param taxadvjob the taxadvjob to set
     */
    public void setTaxadvjob(String taxadvjob) {
        this.taxadvjob = taxadvjob;
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
     * @return the stb
     */
    public String __getStb() {
        return stb;
    }

    /**
     * @param stb the stb to set
     */
    public void setStb(String stb) {
        this.stb = stb;
    }

    /**
     * @return the email
     */
    public String __getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the name
     */
    public String __getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the zipcode
     */
    public String __getZipcode() {
        return zipcode;
    }

    /**
     * @param zipcode the zipcode to set
     */
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    /**
     * @return the phone
     */
    public String __getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the firstname
     */
    public String __getFirstname() {
        return firstname;
    }

    /**
     * @param firstname the firstname to set
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    /**
     * @return the taxauthority
     */
    public String __getTaxauthority() {
        return taxauthority;
    }

    /**
     * @param taxauthority the taxauthority to set
     */
    public void setTaxauthority(String taxauthority) {
        this.taxauthority = taxauthority;
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
     * @return the taxadvmandant
     */
    public String __getTaxadvmandant() {
        return taxadvmandant;
    }

    /**
     * @param taxadvmandant the taxadvmandant to set
     */
    public void setTaxadvmandant(String taxadvmandant) {
        this.taxadvmandant = taxadvmandant;
    }
}
