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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
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

    public static enum TYPES implements MPEnum {

        TYPE_BILL(FormatHandler.TYPE_BILL, Messages.TYPE_BILL.getValue()),
        TYPE_OFFER(FormatHandler.TYPE_OFFER, Messages.TYPE_OFFER.getValue()),
        TYPE_ORDER(FormatHandler.TYPE_ORDER, Messages.TYPE_ORDER.getValue()),
        TYPE_CONTACT(FormatHandler.TYPE_CONTACT, Messages.TYPE_CONTACT.getValue()),
        TYPE_CUSTOMER(FormatHandler.TYPE_CUSTOMER, Messages.TYPE_CUSTOMER.getValue()),
        TYPE_MANUFACTURER(FormatHandler.TYPE_MANUFACTURER, Messages.TYPE_MANUFACTURER.getValue()),
        TYPE_SUPPLIER(FormatHandler.TYPE_SUPPLIER, Messages.TYPE_SUPPLIER.getValue()),
        TYPE_PRODUCT(FormatHandler.TYPE_PRODUCT, Messages.TYPE_PRODUCT.getValue()),
        TYPE_SERVICE(FormatHandler.TYPE_SERVICE, Messages.TYPE_SERVICE.getValue());
        int ids;
        String names;

        TYPES(int id, String name) {
            ids = id;
            names = name;
        }

        @Override
        public int getId() {
            return ids;
        }

        @Override
        public String getName() {
            return names;
        }
    }
    public static String INTEGERPART_IDENTIFIER = "{0,number,000000}";
    private int type;
    private DatabaseObject source = null;
    private Integer startCount = null;
    private java.text.MessageFormat format;
    public static MessageFormat DEFAULT_FORMAT = new MessageFormat(INTEGERPART_IDENTIFIER);
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
        this.source = forObject;
        this.type = determineType(forObject);
        this.format = getFormat();
    }

    @Override
    public String toString() {
        return "Format: " + format.format(43) + " for " + source + " (" + type + ")";
    }

    private FormatHandler() {
    }

    /**
     *
     * @return
     */
    public synchronized MessageFormat getFormat() {
        QueryCriteria c = new QueryCriteria();
        c.add("usersids", MPView.getUser().__getIDS());
        c.add("inttype", this.getType());
        try {
            Object[][] frm = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname, ids", c);
            if (frm.length > 0) {
                String val = frm[frm.length - 1][0].toString();
                try {
                    int id = Integer.valueOf(frm[frm.length - 1][1].toString());

                    if (val.startsWith(START_VALUE_IDENTIFIER)) {
                        startCount = Integer.valueOf(val.split(START_VALUE_IDENTIFIER)[1]);
                        val = val.split(START_VALUE_IDENTIFIER)[2];
                        QueryHandler.instanceOf().clone(Context.getFormats()).update("cname", id, val);
                    }
                } catch (NumberFormatException numberFormatException) {
                    Log.Debug(this, numberFormatException);
                    return DEFAULT_FORMAT;
                }
                return new MessageFormat(VariablesHandler.parse(val, source));
            } else {
                Log.Debug(this, "Format not found, using default format instead!");
                return DEFAULT_FORMAT;
            }
        } catch (Exception ex) {
            Log.Debug(ex);
            Log.Debug(this, "Format not found, using default format instead!");
            return DEFAULT_FORMAT;
        }
    }
    /**
     * Contains all formattable Contexts
     */
    public static List<Context> FORMATTABLE_CONTEXTS = new Vector<Context>(Arrays.asList(new Context[]{
                Context.getContact(), Context.getProducts(), Context.getItems()
            }));

    /**
     * Fetches the next number from the database
     * @return
     */
    public synchronized int getNextNumber() {
        if (startCount == null) {
            int newN = 0;
            DatabaseObject forThis = source;
            if (FORMATTABLE_CONTEXTS.contains(forThis.getContext())) {
                ReturnValue val = QueryHandler.getConnection().freeQuery(
                        //                    "LOCK TABLE " + forThis.getDbIdentity() + " IN EXCLUSIVE MODE;" +
                        "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + ")", MPSecurityManager.VIEW, null);
                if (val.hasData()) {
                    Log.Debug(FormatHandler.class, "Last number found: " + val.getData()[0][0]);
                    newN = ((Formattable) forThis).getFormatHandler().getIntegerPartOf(val.getData()[0][0].toString());
                    Log.Debug(FormatHandler.class, "Counter part: " + newN);
                    return getNextNumber(newN);
                } else {
                    return 1;
                }
            } else {
                throw new UnsupportedOperationException("FormatHandler#getNextNumber is not defined for " + forThis);
            }
        } else {
            int tmp = startCount.intValue();
            startCount = null;
            return tmp;
        }
    }

    private synchronized int getNextNumber(int lastNumber) {
        DatabaseObject forThis = source;
        ReturnValue val2 = QueryHandler.getConnection().freeQuery("SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + toString(lastNumber + 1) + "' ", MPSecurityManager.VIEW, null);
        if (val2.hasData()) {
            Log.Debug(FormatHandler.class, "Already existing..: " + val2.getData()[0][0]);
            return getNextNumber(lastNumber + 1);
        } else {
            return lastNumber + 1;
        }
    }

    /**
     * Formats a given number by the determined number format, <br/>if the {@link setStartCount(Integer) } has not been set.
     * Returns the defined start value then.
     * @param number
     * @return A formatted number
     */
    public synchronized String toString(int number) {
        return format.format(new Object[]{number});
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
    public void setFormat(MessageFormat format) {
        this.format = format;
    }

    /**
     * @param formatPattern the format to set, as String pattern
     */
    public void setFormat(String formatPattern) {
//        Pattern escaper = Pattern.compile("(['{])");
        formatPattern = VariablesHandler.parse(formatPattern, source);
//        formatPattern = escaper.matcher(formatPattern).replaceAll("''$1");
//        Log.Debug(this, formatPattern);
        this.format = new MessageFormat(formatPattern);
    }

    /**
     *
     * @param string
     * @return
     */
    private synchronized int getIntegerPartOf(String string) {
        try {
            Number n = null;
            try {
                n = (Number) format.parse(string, new ParsePosition(0))[0];
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            if (n == null) {
                n = 0;
            }
            return n.intValue();
        } catch (Exception ex) {
            Log.Debug(ex);
            return 0;
        }
    }
}
