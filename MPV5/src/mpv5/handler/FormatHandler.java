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
package mpv5.handler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 */
public class FormatHandler {

    public static final int TYPE_BILL = 0;
    public static final int TYPE_OFFER = 1;
    public static final int TYPE_ORDER = 2;
    public static final int TYPE_CONTACT = 3;
    public static final int TYPE_CUSTOMER = 4;
    public static final int TYPE_MANUFACTURER = 5;
    public static final int TYPE_SUPPLIER = 6;
    public static final int TYPE_PRODUCT = 7;
    public static final int TYPE_SERVICE = 8;
    private int type;
    private Integer startCount = null;
    private java.text.NumberFormat format;
    public static NumberFormat DEFAULT_FORMAT = new DecimalFormat("000000");
    /**
     * This string identifies potential start values from the format string. Use as
     * START_VALUE_IDENTIFIERstartvalueSTART_VALUE_IDENTIFIERformat
     */
    public static String START_VALUE_IDENTIFIER = "@SV@";

    /**
     * Determines the format type of the given {@link DatabaseObject}
     * @param obj
     * @return An int value representing the format type
     */
    public synchronized static int determineType(DatabaseObject obj) {
        if (obj instanceof Item) {
            if (((Item) obj).__getInttype() == Item.TYPE_BILL) {
                return TYPE_BILL;
            } else if (((Item) obj).__getInttype() == Item.TYPE_OFFER) {
                return TYPE_OFFER;
            } else if (((Item) obj).__getInttype() == Item.TYPE_ORDER) {
                return TYPE_ORDER;
            }
        } else if (obj instanceof Contact) {
            if (((Contact) obj).__getIscustomer()) {
                return TYPE_CUSTOMER;
            } else if (((Contact) obj).__getIsmanufacturer()) {
                return TYPE_MANUFACTURER;
            } else if (((Contact) obj).__getIssupplier()) {
                return TYPE_SUPPLIER;
            } else {
                return TYPE_CONTACT;
            }
        } else if (obj instanceof Product) {
            if (((Product) obj).__getInttype() == Product.TYPE_PRODUCT) {
                return TYPE_PRODUCT;
            } else {
                return TYPE_SERVICE;
            }
        }
        return -1;
    }

    /**
     *
     * @param forObject
     */
    public FormatHandler(DatabaseObject forObject) {
        this.type = determineType(forObject);
        this.format = getFormat();
    }

    /**
     *
     * @return
     */
    public synchronized NumberFormat getFormat() {
        QueryCriteria c = new QueryCriteria();
        c.add("usersids", MPV5View.getUser().__getIDS());
        c.add("inttype", this.getType());
        try {
            Object[][] frm = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname, ids", c);
            String val = frm[frm.length][0].toString();
            int id = Integer.valueOf(frm[frm.length][1].toString());
            try {
                if (val.startsWith(START_VALUE_IDENTIFIER)) {
                    startCount = Integer.valueOf(val.split(START_VALUE_IDENTIFIER)[1]);
                    val = val.split(START_VALUE_IDENTIFIER)[2];
                    QueryHandler.instanceOf().clone(Context.getFormats()).update("cname", id, val);
                }
            } catch (NumberFormatException numberFormatException) {
                Log.Debug(this, numberFormatException);
                return DEFAULT_FORMAT;
            }
            return new DecimalFormat(val);
        } catch (Exception ex) {
            Log.Debug(this, "Format not found, using default format instead!");
            return DEFAULT_FORMAT;
        }
    }

    /**
     * Fetches the next number from the database
     * @param forThis
     * @return
     */
    public synchronized static int getNextNumber(DatabaseObject forThis) {
        int newN = 0;
        if (forThis.getContext().equals(Context.getContact())) {
            ReturnValue val = QueryHandler.getConnection().freeQuery(
                    //                    "LOCK TABLE " + forThis.getDbIdentity() + " IN EXCLUSIVE MODE;" +
                    "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + ")", MPSecurityManager.VIEW, null);
            Log.Debug(FormatHandler.class, "Last number found: " + val.getData()[0][0]);
            newN = ((Contact) forThis).getFormatHandler().getIntegerPartOf(val.getData()[0][0].toString());
        } else {
            throw new UnsupportedOperationException("FormatHandler#getNextNumber is not defined for " + forThis);
        }
        return newN+1;
    }

    /**
     * Formats a given number by the determined number format, <br/>if the {@link setStartCount(Integer) } has not been set.
     * Returns the defined start value then.
     * @param number
     * @return A formatted number
     */
    public synchronized String toString(int number) {
        if (startCount == null) {
            return format.format((double) number);
        } else {
            String n = format.format((double) startCount);
            startCount = null;
            return n;
        }
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the startCount
     */
    public Integer getStartCount() {
        return startCount;
    }

    /**
     * @param startCount the startCount to set
     */
    public void setStartCount(Integer startCount) {
        this.startCount = startCount;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(java.text.NumberFormat format) {
        this.format = format;
    }

    /**
     *
     * @param string
     * @return
     */
    private synchronized int getIntegerPartOf(String string) {
        try {
            return Integer.valueOf(format.parse(string).toString());
        } catch (ParseException ex) {
            Log.Debug(ex);
            return 0;
        }
    }
}
