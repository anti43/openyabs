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
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;

/**
 *
 *  anti
 */
public class Item extends DatabaseObject {

    /**
     * Returns a localized string represenation of the given item status
     * @param status
     * @return
     */
    public static String getStatusString(int status) {
        switch (status) {
            case (STATUS_QUEUED):
                return Messages.STATUS_QUEUED.toString();
            case (STATUS_IN_PROGRESS):
                return Messages.STATUS_IN_PROGRESS.toString();
            case (STATUS_PAUSED):
                return Messages.STATUS_PAUSED.toString();
            case (STATUS_FINISHED):
                return Messages.STATUS_FINISHED.toString();
            case (STATUS_PAID):
                return Messages.STATUS_PAID.toString();
            case (STATUS_CANCELLED):
                return Messages.STATUS_CANCELLED.toString();
        }
        return null;
    }

    /**
     * Returns a localized string represenation of the given item type
     * @param type
     * @return
     */
    public static String getTypeString(int type) {
        switch (type) {
            case (TYPE_BILL):
                return Messages.TYPE_BILL.toString();
            case (TYPE_OFFER):
                return Messages.TYPE_OFFER.toString();
            case (TYPE_ORDER):
                return Messages.TYPE_ORDER.toString();
        }
        return null;
    }
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
    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_PAID = 4;
    public static final int STATUS_CANCELLED = 5;
    public static final int TYPE_BILL = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_OFFER = 2;
    private FormatHandler formatHandler;

    public Item() {
        context.setDbIdentity(Context.IDENTITY_ITEMS);
        context.setIdentityClass(Item.class);
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

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * @return the formatHandler
     */
    public FormatHandler getFormatHandler() {
        if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }

    @Override
    public void ensureUniqueness() {
        setCName(getFormatHandler().toString(getFormatHandler().getNextNumber()));
    }
}
