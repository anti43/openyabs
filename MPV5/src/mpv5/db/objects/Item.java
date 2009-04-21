/*
 *  This file is part of MP by anti43 /GPL.
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
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public class Item extends DatabaseObject{


    private int contactsids;
    private boolean isactive;
    private boolean isfinished;
    private double value;
    private double taxvalue;
    private Date datetodo;
    private Date dateend;
    private int intreminders;
    private String groupname = "";
    private String cnumber = "";
    private boolean isbill;
    private boolean isorder;
    private boolean isoffer;


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
     * @return the isactive
     */
    public boolean __getisIsactive() {
        return isactive;
    }

    /**
     * @param isactive the isactive to set
     */
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }

    /**
     * @return the isfinished
     */
    public boolean __getisIsfinished() {
        return isfinished;
    }

    /**
     * @param isfinished the isfinished to set
     */
    public void setIsfinished(boolean isfinished) {
        this.isfinished = isfinished;
    }

    /**
     * @return the value
     */
    public double __getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
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

    /**
     * @return the groupname
     */
    public String __getGroupname() {
        return groupname;
    }

    /**
     * @param groupname the groupname to set
     */
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

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

    /**
     * @return the isbill
     */
    public boolean __getisIsbill() {
        return isbill;
    }

    /**
     * @param isbill the isbill to set
     */
    public void setIsbill(boolean isbill) {
        this.isbill = isbill;
    }

    /**
     * @return the isorder
     */
    public boolean __getisIsorder() {
        return isorder;
    }

    /**
     * @param isorder the isorder to set
     */
    public void setIsorder(boolean isorder) {
        this.isorder = isorder;
    }

    /**
     * @return the isoffer
     */
    public boolean __getisIsoffer() {
        return isoffer;
    }

    /**
     * @param isoffer the isoffer to set
     */
    public void setIsoffer(boolean isoffer) {
        this.isoffer = isoffer;
    }

    /**
     * @return the cnumber
     */
    public String __getCnumber() {
        return cnumber;
    }

    /**
     * @param cnumber the cnumber to set
     */
    public void setCnumber(String cnumber) {
        this.cnumber = cnumber;
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
