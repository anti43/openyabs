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

import enoa.handler.TableHandler;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.panels.ItemPanel;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.TypeConversion;

/**
 *
 *  
 */
public class Item extends DatabaseObject implements Formattable {

    /**
     * Returns a localized string representation of the given item status
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
        return "";
    }

    /**
     * Returns all possible status messages
     * @return
     */
    public static MPEnum[] getStatusStrings() {
        MPEnum[] en = new MPEnum[6];
        en[0] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_QUEUED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[1] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_IN_PROGRESS);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[2] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_PAUSED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[3] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_FINISHED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[4] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_PAID);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[5] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(STATUS_CANCELLED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        return en;
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
            case (TYPE_DELIVERY_NOTE):
                return Messages.TYPE_DELIVERY.toString();
            case (TYPE_ORDER_CONFIRMATION):
                return Messages.TYPE_CONFIRMATION.toString();
        }
        return "";
    }
    private int contactsids;
    private int accountsids;
    private BigDecimal netvalue = new BigDecimal("0");
    private BigDecimal taxvalue = new BigDecimal("0");
    private BigDecimal shippingvalue = new BigDecimal("0");
//    private BigDecimal discountvalue = new BigDecimal("0");
    private Date datetodo;
    private Date dateend;
    private int intreminders;
    private int intstatus;
    private int inttype;
    private String description = "";
    private String cnumber = "";
    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_PAUSED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_PAID = 4;
    public static final int STATUS_CANCELLED = 5;
    public static final int TYPE_BILL = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_OFFER = 2;
    public static final int TYPE_DELIVERY_NOTE = 3;
    public static final int TYPE_ORDER_CONFIRMATION = 4;
    private FormatHandler formatHandler;

    public Item() {
        context = Context.getItem();
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
    public BigDecimal __getTaxvalue() {
        return taxvalue;
    }

    /**
     * @param taxvalue the taxvalue to set
     */
    public void setTaxvalue(BigDecimal taxvalue) {
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
        ItemPanel p = new ItemPanel(Context.getItem(), __getInttype());
        return p;
    }

    /**
     * @return the defaultaccountsids
     */
    public int __getAccountsids() {
        return accountsids;
    }

    /**
     * @param defaultaccountsids the defaultaccountsids to set
     */
    public void setAccountsids(int defaultaccountsids) {
        this.accountsids = defaultaccountsids;
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
    public BigDecimal __getNetvalue() {
        return netvalue;
    }

    /**
     * @param netvalue the netvalue to set
     */
    public void setNetvalue(BigDecimal netvalue) {
        this.netvalue = netvalue;
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        if (__getIntstatus() == STATUS_QUEUED) {
            return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
        } else if (__getIntstatus() == STATUS_IN_PROGRESS) {
            return new MPIcon("/mpv5/resources/images/22/run.png");
        } else if (__getIntstatus() == STATUS_PAUSED) {
            return new MPIcon("/mpv5/resources/images/22/kalarm.png");
        } else if (__getIntstatus() == STATUS_FINISHED) {
            return new MPIcon("/mpv5/resources/images/22/knewstuff.png");
        } else if (__getIntstatus() == STATUS_PAID) {
            return new MPIcon("/mpv5/resources/images/22/ok.png");
        } else if (__getIntstatus() == STATUS_CANCELLED) {
            return new MPIcon("/mpv5/resources/images/22/file_temporary.png");
        } else {
            return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
        }
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
        setCnumber(getFormatHandler().next());
        setCName(__getCnumber());
    }

    /**
     * Fetches all related {@link Subitem}s to this {@link Item}<br/>
     * If no subitems are assigned, returns an empty default list of default subitems
     * @return
     */
    public SubItem[] getSubitems() {
        ArrayList<DatabaseObject> data = new ArrayList<DatabaseObject>();
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getSubItem(), DatabaseObject.getObject(Context.getSubItem()));
        } catch (NodataFoundException ex) {
            for (int i = 0; i < 6; i++) {
                data.add(SubItem.getDefaultItem());
            }
        }
        SubItem[] t = new SubItem[data.size()];
        for (int i = 0; i < data.size(); i++) {
            t[i] = (SubItem) data.get(i);
        }
        return data.toArray(new SubItem[]{});
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
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {


        try {
            if (map.containsKey("intstatus")) {
                map.remove("intstatus");
                map.put("status", getStatusString(Integer.valueOf(map.get("intstatus").toString())));
                map.remove("intstatus");
            }
        } catch (Exception numberFormatException) {
            //already resolved?
        }

        try {
            if (map.containsKey("inttype")) {
                map.put("type", getTypeString(Integer.valueOf(map.get("inttype").toString())));
                map.remove("inttype");
            }
        } catch (Exception numberFormatException) {
            //already resolved?
        }

        if (map.containsKey("defaultaccountsids")) {
            try {
                try {
                    map.put("account", DatabaseObject.getObject(Context.getAccounts(), Integer.valueOf(map.get("defaultaccountsids").toString())));
                    map.remove("defaultaccountsids");
                } catch (NodataFoundException ex) {
                    map.put("account", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        if (map.containsKey("contactsids")) {
            try {
                try {
                    map.put("contact", DatabaseObject.getObject(Context.getContact(), Integer.valueOf(map.get("contactsids").toString())));
                    map.remove("contactsids");
                } catch (NodataFoundException ex) {
                    map.put("contact", null);
                    Log.Debug(this, ex.getMessage());
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        if (map.containsKey("taxids")) {
            try {
                map.put("tax", FormatNumber.formatPercent(Tax.getTaxValue(Integer.valueOf(map.get("taxids").toString()))));
                map.remove("taxids");
            } catch (NumberFormatException numberFormatException) {
                Log.Debug(numberFormatException);
            }
        }

        List<SubItem> data;
        List<String[]> data2;
        Vector<String[]> list = new Vector<String[]>();

        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getSubItem(), new SubItem());
            for (int i = 0; i < data.size(); i++) {
                SubItem t = data.get(i);
                list.add(t.toStringArray());
                data2 = t.getValues3();
                for (int j = 0; j < data2.size(); j++) {
                    String[] strings = data2.get(j);
                    map.put("subitem" + i + "." + strings[0].toLowerCase(), strings[1]);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        //TODO convert to proper BigDecimal operations rather than * + etc
        map.put(TableHandler.KEY_TABLE + "1", list);
        map.put("netvalue", FormatNumber.formatDezimal(__getNetvalue()));
        map.put("taxvalue", FormatNumber.formatDezimal(__getTaxvalue()));
        map.put("grosvalue", FormatNumber.formatDezimal(__getTaxvalue().doubleValue() + __getNetvalue().doubleValue()));
        map.put("shippednetvalue", FormatNumber.formatDezimal(__getNetvalue().doubleValue() + __getShippingvalue().doubleValue()));

        map.put("shippedtaxvalue", FormatNumber.formatDezimal(__getTaxvalue().doubleValue()));
        map.put("shippedgrosvalue", FormatNumber.formatDezimal((__getTaxvalue().doubleValue() + __getNetvalue().doubleValue()) + __getShippingvalue().doubleValue()));
        map.put("shippingvalue", FormatNumber.formatDezimal(__getShippingvalue()));
        map.put("shippingvaluef", FormatNumber.formatLokalCurrency(__getShippingvalue()));
        map.put("netvaluef", FormatNumber.formatLokalCurrency(__getNetvalue()));
        map.put("taxvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().doubleValue()));
        map.put("grosvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().doubleValue() + __getNetvalue().doubleValue()));
        map.put("shippednetvaluef", FormatNumber.formatLokalCurrency(__getNetvalue().doubleValue() + __getShippingvalue().doubleValue()));
        map.put("shippedtaxvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().doubleValue()));
        map.put("shippedgrosvaluef", FormatNumber.formatLokalCurrency(((__getTaxvalue().doubleValue() + __getNetvalue().doubleValue())) + __getShippingvalue().doubleValue()));

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("shiptax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("shiptax", new Integer(0));
            Double shiptax = Tax.getTaxValue(taxid).doubleValue();
            map.put("shippingtaxvalue", __getShippingvalue().doubleValue() * (shiptax / 100));
            map.put("shippingtaxvaluef", FormatNumber.formatLokalCurrency(__getShippingvalue().doubleValue() * (shiptax / 100)));
            map.put("shippingtaxpercentvaluef", FormatNumber.formatPercent(shiptax));
            map.put("shippinggrosvalue", __getShippingvalue().doubleValue() * (shiptax / 100) + __getShippingvalue().doubleValue());
            map.put("shippinggrosvaluef", FormatNumber.formatLokalCurrency(__getShippingvalue().doubleValue() * (shiptax / 100) + __getShippingvalue().doubleValue()));
            map.put("shippedtaxvalue", __getTaxvalue().doubleValue() + __getShippingvalue().doubleValue() * (shiptax / 100));
            map.put("shippedtaxvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().doubleValue() + __getShippingvalue().doubleValue() * (shiptax / 100)));
            map.put("shippedgrosvalue", (__getTaxvalue().doubleValue() + __getNetvalue().doubleValue()) + __getShippingvalue().doubleValue() * (shiptax / 100) + __getShippingvalue().doubleValue());
            map.put("shippedgrosvaluef", FormatNumber.formatLokalCurrency(((__getTaxvalue().doubleValue() + __getNetvalue().doubleValue())) + __getShippingvalue().doubleValue() * (shiptax / 100) + __getShippingvalue().doubleValue()));
        }
        //date format localization
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("item.date.locale")) {
            Locale l = null;
            try {
                l = TypeConversion.stringToLocale(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale"));
            } catch (Exception e) {
            }
            if (l != null) {
                map.put("dateadded", DateFormat.getDateInstance(DateFormat.SHORT, l).format(__getDateadded()));
            } else {
                Log.Debug(this, "Error while using item.date.locale");
            }
        }

        return super.resolveReferences(map);
    }

    /**
     * @return the shippingvalue
     */
    public BigDecimal __getShippingvalue() {
        return shippingvalue;
    }

    /**
     * @param shippingvalue the shippingvalue to set
     */
    public void setShippingvalue(BigDecimal shippingvalue) {
        this.shippingvalue = shippingvalue;
    }

//    /**
//     * @return the discountvalue
//     */
//    public BigDecimal  __getDiscountvalue() {
//        return discountvalue;
//    }
//
//    /**
//     * @param discountvalue the discountvalue to set
//     */
//    public void setDiscountvalue(BigDecimal  discountvalue) {
//        this.discountvalue = discountvalue;
//    }
    public void defineFormatHandler(FormatHandler handler) {
        formatHandler = handler;
    }

    /**
     * Create a revenue entry out of this item
     */
    public void createRevenue() {
        Revenue r = new Revenue();
        r.setAccountsids(accountsids);
        r.setNetvalue(netvalue);
//        r.setTaxpercentvalue(taxvalue);
        r.setBrutvalue(netvalue.add(taxvalue));
        if (description != null && description.length() > 0) {
            r.setDescription(description);
        } else {
            r.setDescription(Messages.AUTO_GENERATED_VALUE + " " + cnumber);
        }
        r.save();
    }
//    @Override
//    public boolean save() {
//        boolean saved = super.save();
//        if (intstatus == STATUS_PAID && inttype == TYPE_BILL && saved && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(MPView.tabPane, "autocreaterevenue")) {
//            createRevenue();
//        }
//        return saved;
//    }

    @Override
    public String toString() {
        try {
            return ((Contact) getObject(Context.getContact(), contactsids)).__getCName() + "-" + cname;
        } catch (NodataFoundException ex) {
            return super.toString();
        }
    }

    /**
     * Fetches all properties for this item from the db
     * @return A (possibly empty) list of {@link ValueProperty}s
     */
    public List<ValueProperty> getProperties() {
        return ValueProperty.getProperties(this);
    }
}
