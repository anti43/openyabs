/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;

/**
 *
 *  anti
 */
public class Item extends DatabaseObject {

    private int contactsids;
    private int defaultaccountsids;
    private double netvalue;
    private double taxvalue;
    private Date datetodo;
    private Date dateend;
    private int intreminders;
    private int intstatus;
    private int inttype;
    private String description = "";
    public static int STATUS_QUEUED = 0;
    public static int STATUS_IN_PROGRESS = 1;
    public static int STATUS_PAUSED = 2;
    public static int STATUS_FINISHED = 3;
    public static int TYPE_BILL = 0;
    public static int TYPE_ORDER = 1;
    public static int TYPE_OFFER = 2;

    public Item() {
        context.setDbIdentity(Context.IDENTITY_ITEMS);
        context.setIdentityClass(Item.class);
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public void setCName(String name) {
        this.cname = name;
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
     * @return the taxvalue
     */
    public double __getTaxvalue() {
        return taxvalue;
    }

    /**
     * @param taxvalue the taxvalue to set
     */
    public void setTaxvalue(double taxvalue) {
        this.taxvalue = taxvalue;
    }

    /**
     * @return the datetodo
     */
    public Date __getDatetodo() {
        return datetodo;
    }

    /**
     * @param datetodo the datetodo to set
     */
    public void setDatetodo(Date datetodo) {
        this.datetodo = datetodo;
    }

    /**
     * @return the intreminders
     */
    public int __getIntreminders() {
        return intreminders;
    }

    /**
     * @param intreminders the intreminders to set
     */
    public void setIntreminders(int intreminders) {
        this.intreminders = intreminders;
    }

//    /**
//     * @return the groupname
//     */
////    public String __getGroupname() {
//        return groupname;
//    }
//
//    /**
//     * @param groupname the groupname to set
//     */
//    public void setGroupname(String groupname) {
//        this.groupname = groupname;
//    }
    /**
     * @return the dateend
     */
    public Date __getDateend() {
        return dateend;
    }

    /**
     * @param dateend the dateend to set
     */
    public void setDateend(Date dateend) {
        this.dateend = dateend;
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the defaultaccountsids
     */
    public int __getDefaultaccountsids() {
        return defaultaccountsids;
    }

    /**
     * @param defaultaccountsids the defaultaccountsids to set
     */
    public void setDefaultaccountsids(int defaultaccountsids) {
        this.defaultaccountsids = defaultaccountsids;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *  <li>QUEUED = 0;
     *  <li>IN_PROGRESS= 1;
     *  <li>PAUSED = 2;
     *  <li>FINNISHED= 3;
     * @return the intstatus
     */
    public int __getIntstatus() {
        return intstatus;
    }

    /**
     *  <li>QUEUED = 0;
     *  <li>IN_PROGRESS= 1;
     *  <li>PAUSED = 2;
     *  <li>FINNISHED= 3;
     * @param intstatus the intstatus to set
     */
    public void setIntstatus(int intstatus) {
        this.intstatus = intstatus;
    }

    /**
     *  <li>TYPE_BILL = 0;
     *  <li>TYPE_ORDER = 1;
     *  <li>TYPE_OFFER = 2;
     * @return the inttype
     */
    public int __getInttype() {
        return inttype;
    }

    /**
     *  <li>TYPE_BILL = 0;
     *  <li>TYPE_ORDER = 1;
     *  <li>TYPE_OFFER = 2;
     * @param inttype the inttype to set
     */
    public void setInttype(int inttype) {
        this.inttype = inttype;
    }

    /**
     * @return the netvalue
     */
    public double __getNetvalue() {
        return netvalue;
    }

    /**
     * @param netvalue the netvalue to set
     */
    public void setNetvalue(double netvalue) {
        this.netvalue = netvalue;
    }
}
