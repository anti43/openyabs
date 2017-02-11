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
package mpv5.handler;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.*;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import static mpv5.globals.Constants.*;

/**
 *
 */
public class FormatHandler {

    public static enum TYPES implements MPEnum {

        TYPE_INVOICE(Constants.TYPE_INVOICE, Messages.TYPE_INVOICE.getValue()),
        TYPE_OFFER(Constants.TYPE_OFFER, Messages.TYPE_OFFER.getValue()),
        TYPE_ORDER(Constants.TYPE_ORDER, Messages.TYPE_ORDER.getValue()),
        TYPE_CONTACT(Constants.TYPE_CONTACT, Messages.TYPE_CONTACT.getValue()),
        TYPE_CUSTOMER(Constants.TYPE_CUSTOMER, Messages.TYPE_CUSTOMER.getValue()),
        TYPE_MANUFACTURER(Constants.TYPE_MANUFACTURER, Messages.TYPE_MANUFACTURER.getValue()),
        TYPE_SUPPLIER(Constants.TYPE_SUPPLIER, Messages.TYPE_SUPPLIER.getValue()),
        TYPE_PRODUCT(Constants.TYPE_PRODUCT, Messages.TYPE_PRODUCT.getValue()),
        TYPE_SERVICE(Constants.TYPE_SERVICE, Messages.TYPE_SERVICE.getValue()),
        TYPE_REVENUE(Constants.TYPE_REVENUE, Messages.TYPE_REVENUE.getValue()),
        TYPE_EXPENSE(Constants.TYPE_EXPENSE, Messages.TYPE_EXPENSE.getValue()),
        TYPE_CONVERSATION(Constants.TYPE_CONVERSATION, Messages.TYPE_CONVERSATION.getValue()),
        TYPE_ACTIVITY(Constants.TYPE_ACTIVITY, Messages.TYPE_ACTIVITY.getValue()),
        TYPE_PRODUCTORDER(Constants.TYPE_PRODUCT_ORDER, Messages.TYPE_PRODUCT_ORDER.getValue()),
        TYPE_ORDER_CONFIRMATION(Constants.TYPE_ORDER_CONFIRMATION, Messages.TYPE_CONFIRMATION.getValue()),
        TYPE_CONTRACT(Constants.TYPE_CONTRACT, Messages.TYPE_CONTRACT.getValue()),
        TYPE_DELIVERY_NOTE(Constants.TYPE_DELIVERY_NOTE, Messages.TYPE_DELIVERY.getValue()),
        TYPE_DEPOSIT(Constants.TYPE_DEPOSIT, Messages.TYPE_DEPOSIT.getValue()),
        TYPE_PART_PAYMENT(Constants.TYPE_PART_PAYMENT, Messages.TYPE_PART_PAYMENT.getValue()),
        TYPE_CREDIT(Constants.TYPE_CREDIT, Messages.TYPE_CREDIT.getValue());
        int ids;
        String names;

        TYPES(int id, String name) {
            ids = id;
            names = name;
        }

        @Override
        public Integer getId() {
            return ids;
        }

        @Override
        public String getName() {
            return names;
        }
    }
    public static String INTEGERPART_IDENTIFIER = "{0,number,000000}";
    public static String DELETED_IDENTIFIER = "!";
    private DatabaseObject source = null;
    public static YMessageFormat DEFAULT_FORMAT = new YMessageFormat(INTEGERPART_IDENTIFIER, null);
    /**
     * This string identifies potential start values from the format string. Use
     * as START_VALUE_IDENTIFIERstartvalueSTART_VALUE_IDENTIFIERformat
     */
    public static String START_VALUE_IDENTIFIER = "@SV@";

    /**
     * Determines the format type of the given {@link DatabaseObject}
     *
     * @param obj
     * @return An int value representing the format type
     */
    public synchronized static int determineType(DatabaseObject obj) {
        if (obj instanceof Item) {
            return ((Item) obj).__getInttype();
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
            return ((Product) obj).__getInttype();
        } else if (obj instanceof Revenue) {
            return TYPE_REVENUE;
        } else if (obj instanceof Expense) {
            return TYPE_EXPENSE;
        } else if (obj instanceof Conversation) {
            return TYPE_CONVERSATION;
        } else if (obj instanceof ActivityList) {
            return TYPE_ACTIVITY;
        } else if (obj instanceof ProductOrder) {
            return TYPE_PRODUCT_ORDER;
        }
        return -1;
    }

    /**
     *
     * @param forObject
     */
    public FormatHandler(DatabaseObject forObject) {
        this.source = forObject;
    }

    @Override
    public String toString() {
        return "Format: " + getFormat().format(43) + " for " + source + " (" + determineType(source) + ")";
    }

    private FormatHandler() {
    }
    private static Map<String, YMessageFormat> formatCache = null;

    /**
     *
     * @return
     */
    public synchronized YMessageFormat getFormat() {

        int typ = determineType(source);
        String key = mpv5.db.objects.User.getCurrentUser().__getIDS() + "@" + typ;
        String adminKey = User.ADMIN_ID + "@" + typ;

        if (formatCache != null) {
            if (!formatCache.containsKey(adminKey) && !formatCache.containsKey(key)) {
                Log.Debug(this, "Format caching did not result in a matching format.");
                return DEFAULT_FORMAT;
            }

            if (formatCache.containsKey(key)) {
                return formatCache.get(key);
            } else {
                return formatCache.get(adminKey);
            }
        }

        //Formats not cached yet
        formatCache = new ConcurrentHashMap<String, YMessageFormat>();


//        c.addAndCondition("usersids", mpv5.db.objects.User.getCurrentUser().__getIDS());
//        c.addAndCondition("inttype", determineType(source));
        try {
            Object[][] formats = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname, ids, inttype, usersids", false);
            if (formats.length > 0) {
                for (int i = 0; i < formats.length; i++) {
                    Integer startCount = null;
                    String value = formats[i][0].toString();
                    try {
                        int formatId = Integer.valueOf(formats[i][1].toString());

                        if (value.startsWith(START_VALUE_IDENTIFIER) && Integer.valueOf(formats[i][2].toString()) == typ) {
                            startCount = Integer.valueOf(value.split(START_VALUE_IDENTIFIER)[1]);
                            value = value.split(START_VALUE_IDENTIFIER)[2];
                            QueryHandler.instanceOf().clone(Context.getFormats()).update("cname", formatId, value);
                            Log.Debug(this, "Setting start count to: " + startCount);
                        }
                    } catch (Exception numberFormatException) {
                        Log.Debug(this, numberFormatException);
                        return DEFAULT_FORMAT;
                    }
                    try {
                        YMessageFormat mFormat = new YMessageFormat(value, startCount);
                        Log.Debug(this, "Format found: " + mFormat.toPattern() + ", caching for " + formats[i][3].toString() + "@" + formats[i][2].toString());
                        formatCache.put(formats[i][3].toString() + "@" + formats[i][2].toString(), mFormat);

                    } catch (Exception e) {
                        Log.Debug(this, e);
                        return DEFAULT_FORMAT;
                    }
                }

                return getFormat();
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
     * Fetches the next number from the database
     *
     * @param format
     * @return
     */
    public synchronized int getNextNumber(YMessageFormat format) {

        if (format.startValue() == null) {
            int newN = 0;
            DatabaseObject forThis = source;
            if (Context.FORMATTABLE_CONTEXTS.contains(forThis.getContext())) {

                String query;
                if (forThis.getContext().equals(Context.getInvoice())
                        || forThis.getContext().equals(Context.getOffer())
                        || forThis.getContext().equals(Context.getOrder())
                        || forThis.getContext().equals(Context.getCredit())
                        || forThis.getContext().equals(Context.getDeposit())
                        || forThis.getContext().equals(Context.getPartPayment())) {
                    query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE inttype ="
                            + ((Item) forThis).__getInttype() + " and invisible=0)";
                } else if (forThis.getContext().equals(Context.getProduct())) {
                    query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE inttype ="
                            + ((Product) forThis).__getInttype() + " and invisible=0)";
                } else if (forThis.getContext().equals(Context.getProductOrder())) {
                    query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE inttype ="
                            + ((ProductOrder) forThis).getInttype() + " and invisible=0)";
                } else if (forThis.getContext().equals(Context.getContact())
                        || forThis.getContext().equals(Context.getCompany())
                        || forThis.getContext().equals(Context.getContactsCompanies())
                        || forThis.getContext().equals(Context.getCustomer())
                        || forThis.getContext().equals(Context.getSupplier())
                        || forThis.getContext().equals(Context.getManufacturer())) {
                    if (((Contact) forThis).__getIscustomer()) {
                        query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE iscustomer = 1 and invisible=0)";
                    } else if (((Contact) forThis).__getIsmanufacturer()) {
                        query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE ismanufacturer = 1 and invisible=0)";
                    } else if (((Contact) forThis).__getIssupplier()) {
                        query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE issupplier = 1 and invisible=0)";
                    } else {
                        query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + " WHERE issupplier = 0  AND ismanufacturer = 0 AND iscustomer = 0 and invisible=0)";
                    }
                } else {
                    query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE ids = (SELECT MAX(ids) from " + forThis.getDbIdentity() + ") and invisible=0";
                }

                ReturnValue val = QueryHandler.getConnection().freeQuery(
                        query, MPSecurityManager.VIEW, null);

                if (val.hasData()) {
                    Log.Debug(FormatHandler.class, "Last number found: " + val.getFirstEntry());
                    try {
                        newN = ((Formattable) forThis).getFormatHandler().getIntegerPartOf(format, val.getFirstEntry().toString());
                    } catch (Exception e) {
                        Log.Debug(e);
                    }
                    Log.Debug(FormatHandler.class, "Counter part: " + newN);
                    return getNextNumber(format, newN);
                } else {
                    Log.Debug(FormatHandler.class, "First entry?: " + newN);
                    return getNextNumber(format, 0);
                }
            } else {
                throw new UnsupportedOperationException("FormatHandler#getNextNumber is not defined for " + forThis.getContext());
            }
        } else {
            int tmp = format.startValue();
            Log.Debug(FormatHandler.class, "Found Startcount: " + tmp + " for " + format.toPattern());
            format.setStartValue(null);
            return tmp;
        }
    }

    private synchronized int getNextNumber(final YMessageFormat format, final int lastNumber) {
        DatabaseObject forThis = source;

        String query;
        String cnumber = toString(format, lastNumber + 1);
        if (forThis.getContext().equals(Context.getInvoice())
                || forThis.getContext().equals(Context.getOffer())
                || forThis.getContext().equals(Context.getOrder())
                || forThis.getContext().equals(Context.getCredit())
                || forThis.getContext().equals(Context.getDeposit())
                || forThis.getContext().equals(Context.getPartPayment())) {
            query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND inttype ="
                    + ((Item) forThis).__getInttype();
        } else if (forThis.getContext().equals(Context.getProduct())) {
            query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND inttype ="
                    + ((Product) forThis).__getInttype();
        } else if (forThis.getContext().equals(Context.getProductOrder())) {
            query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND inttype ="
                    + ((ProductOrder) forThis).getInttype();
        } else if (forThis.getContext().equals(Context.getContact())
                || forThis.getContext().equals(Context.getCompany())
                || forThis.getContext().equals(Context.getContactsCompanies())
                || forThis.getContext().equals(Context.getCustomer())
                || forThis.getContext().equals(Context.getSupplier())
                || forThis.getContext().equals(Context.getManufacturer())) {

            if (((Contact) forThis).__getIscustomer()) {
                query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND iscustomer = 1";
            } else if (((Contact) forThis).__getIsmanufacturer()) {
                query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND ismanufacturer = 1";
            } else if (((Contact) forThis).__getIssupplier()) {
                query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND issupplier = 1";
            } else {
                query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "' AND (issupplier = 0  AND ismanufacturer = 0 AND iscustomer = 0)";
            }
        } else {
            query = "SELECT cnumber FROM " + forThis.getDbIdentity() + " WHERE cnumber = '" + cnumber + "'";
        }

        Log.Debug(FormatHandler.class, "Checking : " + cnumber);
        ReturnValue val2 = QueryHandler.getConnection().freeQuery(
                query, MPSecurityManager.VIEW, null);
        if (val2.hasData()) {
            Log.Debug(FormatHandler.class, "Already existing..: " + val2.getData()[0][0]);
            return getNextNumber(format, lastNumber + 1);
        } else {
            int nu = lastNumber + 1;
            Log.Debug(FormatHandler.class, "Not existing yet : " + cnumber + ", returning next one: " + nu);
            return nu;
        }
    }

    /**
     * Formats a given number by the determined number format, 
     * if the {@link setStartCount(Integer)
     * } has not been set. Returns the defined start value then.
     *
     * @param format
     * @param number
     * @return A formatted number
     */
    public synchronized String toString(final MessageFormat format, final int number) {
        String fs = format.format(new Object[]{number});
        Log.Debug(FormatHandler.class, format.toPattern() + " ? " + number + " : " + fs);
        String x = VariablesHandler.parseNoScript(fs, source);
        if (x.length() == 0) {
            x = "0000" + number;
        }
        return x;
    }

    /**
     * Returns the user defined (if defined) String representation of the parent
     * object
     *
     * @return A formatted number
     */
    public synchronized String toUserString() {

        String s = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("saveformat");
        if (s != null && s.length() > 0) {
            if (s.contains("/")) {
                s = s.substring(s.lastIndexOf("/") + 1);
            }
        } else {
            s = source.toString();
        }


        return VariablesHandler.parse(s, source);
    }

    /**
     * @return the type
     */
    public int getType() {
        return determineType(source);
    }

//    /**
//     * @param format the format to set
//     */
//    public void setFormat(MessageFormat format) {
//        this.format = format;
//    }
//
//    /**
//     * @param formatPattern the format to set, as String pattern
//     */
//    public void setFormat(String formatPattern) {
////        Pattern escaper = Pattern.compile("(['{])");
//        formatPattern = VariablesHandler.parse(formatPattern, source);
////        formatPattern = escaper.matcher(formatPattern).replaceAll("''$1");
////        Log.Debug(this, formatPattern);
//        this.format = new MessageFormat(formatPattern);
//    }
    /**
     *
     * @param string
     * @return
     */
    private synchronized int getIntegerPartOf(YMessageFormat format, String string) {
        Log.Debug(this, "In getIntegerPartOf " + string + " with " + User.getCurrentUser().getName());
        int startindex = 0;
        String prop = GlobalSettings.getProperty(format.toPattern() + "_startposition");
        Log.Debug(this, "GlobalSettings.getProperty: " + prop + " for " + format.toPattern() + "_startposition");
        if (prop != null && !prop.equals("null")) {
            try {
                startindex = Integer.valueOf(prop);
            } catch (NumberFormatException numberFormatException) {
                Log.Debug(numberFormatException);
            }
        }

        try {
            Number n = null;
            YMessageFormat f;
            try {
                if (startindex < 0) {
                    Log.Debug(this, " Converting startindex " + startindex + " to " + (string.length() + startindex));
                    startindex = string.length() + startindex;
                }
                Log.Debug(this, "Original pattern: " + format.toPattern());

                
                
                f = new YMessageFormat((VariablesHandler.parseNoScript(format.toPattern(), source)).substring(startindex), null);
                Log.Debug(this, "Pattern: " + f.toPattern() + " for String: " + string + " Starting at " + startindex);
                Object[] result = f.parse(string, new ParsePosition(startindex));
                if (result != null) {
                    n = (Number) result[0];
                } else {
                    Log.Debug(this, "Parsing failed!");
                    throw new IllegalArgumentException(f.toPattern());
                }
            } catch (Exception e) {
                Log.Debug(this, e);
                Popup.error(YabsViewProxy.instance().getIdentifierFrame(), Messages.ERROR_OCCURED + "\n" + "Pattern: " + format.toPattern() + " for String: " + string + " Starting at " + startindex);
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

    /**
     * Returns the next determined number String
     *
     * @return
     */
    public synchronized String next() {
        YMessageFormat format = getFormat();
        return toString(format, getNextNumber(format));
    }

    public static class YMessageFormat extends java.text.MessageFormat {

        private static final long serialVersionUID = 1L;
        private Integer startValue = null;

        public YMessageFormat(String pattern, Integer startValue) {
            super(pattern.replace(DELETED_IDENTIFIER, ""));
            this.startValue = startValue;
        }

        public Integer startValue() {
            return startValue;
        }

        public void setStartValue(Integer val) {
            startValue = val;
        }
    }
}
