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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.Templateable;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.panels.ItemPanel2;
import mpv5.utils.images.MPIcon;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.TypeConversion;

/**
 *
 *
 */
public class Item extends DatabaseObject implements Formattable, Templateable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns a localized string representation of the given item status
     *
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
        return Messages.NA.toString();
    }

    /**
     * Returns all possible status messages
     *
     * @return
     */
    public static MPEnum[] getStatusStrings() {
        MPEnum[] en = new MPEnum[6];
        en[0] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_QUEUED;
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[1] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_IN_PROGRESS;
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[2] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_PAUSED;
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[3] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_FINISHED;
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[4] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_PAID;
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[5] = new MPEnum() {

            @Override
            public Integer getId() {
                return STATUS_CANCELLED;
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
     *
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
//            case (TYPE_DELIVERY_NOTE):
//                return Messages.TYPE_DELIVERY.toString();
//            case (TYPE_ORDER_CONFIRMATION):
//                return Messages.TYPE_CONFIRMATION.toString();
        }
        return "";
    }

    public static MPEnum[] getItemEnum() {
        MPEnum[] en = new MPEnum[4];
        en[0] = new MPEnum() {

            @Override
            public Integer getId() {
                return -1;
            }

            @Override
            public String getName() {
                return Messages.ALL.toString();
            }
        };
        en[1] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_BILL;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_BILL);
            }
        };
        en[2] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_OFFER;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_OFFER);
            }
        };
        en[3] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_ORDER;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_ORDER);
            }
        };
//        en[4] = new MPEnum() {
//
//            @Override
//            public Integer getId() {
//                return new Integer(3);
//            }
//
//            @Override
//            public String getName() {
//                return getTypeString(3);
//            }
//        };
//        en[5] = new MPEnum() {
//
//            @Override
//            public Integer getId() {
//                return new Integer(4);
//            }
//
//            @Override
//            public String getName() {
//                return getTypeString(4);
//            }
//        };        
        return en;
    }
    private int contactsids;
    private int accountsids;
    private BigDecimal netvalue = BigDecimal.ZERO;
    private BigDecimal taxvalue = BigDecimal.ZERO;
    private BigDecimal discountvalue = BigDecimal.ZERO;
    private BigDecimal discountgrosvalue = BigDecimal.ZERO;
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
//    public static final int TYPE_BILL = 0;
//    public static final int TYPE_ORDER = 1;
//    public static final int TYPE_OFFER = 2;
//    public static final int TYPE_DELIVERY_NOTE = 3;
//    public static final int TYPE_ORDER_CONFIRMATION = 4;
    private FormatHandler formatHandler;

    public Item() {
        setContext(Context.getItem());
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
     * @return the Discountvalue
     */
    public BigDecimal __getDiscountvalue() {
        return discountvalue;
    }

    /**
     * @param discountvalue
     */
    public void setDiscountvalue(BigDecimal discountvalue) {
        this.discountvalue = discountvalue;
    }

    /**
     * @return the Discountbrutvalue
     */
    public BigDecimal __getDiscountGrosvalue() {
        return discountgrosvalue;
    }

    /**
     * @param discountgrosvalue
     */
    public void setDiscountGrosvalue(BigDecimal discountgrosvalue) {
        this.discountgrosvalue = discountgrosvalue;
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
        ItemPanel2 p = new ItemPanel2(Context.getItem(), __getInttype());
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
     * <li>QUEUED = 0; <li>IN_PROGRESS= 1; <li>PAUSED = 2; <li>FINNISHED= 3;
     *
     * @return the intstatus
     */
    public int __getIntstatus() {
        return intstatus;
    }

    /**
     * <li>QUEUED = 0; <li>IN_PROGRESS= 1; <li>PAUSED = 2; <li>FINNISHED= 3;
     *
     * @param intstatus the intstatus to set
     */
    public void setIntstatus(int intstatus) {
        this.intstatus = intstatus;
    }

    /**
     * <li>TYPE_BILL = 0; <li>TYPE_ORDER = 1; <li>TYPE_OFFER = 2;
     *
     * @return the inttype
     */
    public int __getInttype() {
        return inttype;
    }

    /**
     * <li>TYPE_BILL = 0; <li>TYPE_ORDER = 1; <li>TYPE_OFFER = 2;
     *
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
    public synchronized FormatHandler getFormatHandler() {
        if (formatHandler == null) {
            formatHandler = new FormatHandler(this);
        }
        return formatHandler;
    }

    @Override
    public synchronized void ensureUniqueness() {
        Log.Debug(this, "In ensureUniqueness for " + this.getClass());
        setCnumber(getFormatHandler().next());
        setCname(__getCnumber());
        Log.Debug(this, "ensureUniqueness result: " + __getCnumber());
    }

    /**
     * Fetches all related {@link Subitem}s to this {@link Item}<br/> If no
     * subitems are assigned, returns an empty default list of default subitems
     *
     * @return
     */
    public SubItem[] getSubitems() {
        List<DatabaseObject> data = new ArrayList<DatabaseObject>();
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getSubItem(), DatabaseObject.getObject(Context.getSubItem()), false);
        } catch (NodataFoundException ex) {
            for (int i = 0; i < 6; i++) {
                data.add(SubItem.getDefaultItem());
            }
        }
//        SubItem[] t = new SubItem[data.size()];
//        for (int i = 0; i < data.size(); i++) {
//            t[i] = (SubItem) data.get(i);
//        }
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
    public Map<String, Object> resolveReferences(Map<String, Object> map) {
        resolveValueProperties(map);

        try {
            if (map.containsKey("intstatus")) {
                map.put("status", getStatusString(Integer.valueOf(map.get("intstatus").toString())));
            }
        } catch (Exception numberFormatException) {
            //already resolved?
        }

        try {
            if (map.containsKey("inttype")) {
                map.put("type", getTypeString(Integer.valueOf(map.get("inttype").toString())));
                map.remove("inttype");
            }
        } catch (NumberFormatException numberFormatException) {
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
        ArrayList<String[]> list = new ArrayList<String[]>();

        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getSubItem(), new SubItem(), false);
            Collections.sort(data, SubItem.ORDER_COMPARATOR);

            for (int i = 0; i < data.size(); i++) {
                SubItem t = data.get(i);
                list.add(t.toStringArray());

                if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.pdftable", false)) {
                    data2 = t.getValues3();
                    for (String[] strings : data2) {
                        map.put("subitem" + i + "." + strings[0].toLowerCase(), strings[1]);
                    }
                }
            }

            if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.hidecountfortext", true)) {
                int skipcount = 0;
                for (int i = 0; i < data.size(); i++) {
                    SubItem t = data.get(i);
                    if (t.getInttype() == SubItem.TYPE_TEXT) {
                        skipcount--;
                        Log.Debug(this, "Skipping text subitem..");
                    } else {
                        list.get(i)[0] = String.valueOf(Integer.valueOf(list.get(i)[0]) + skipcount);
                    }
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        map.put(TableHandler.KEY_TABLE + "1", list);

        //values
        map.put("netnodiscvalue", FormatNumber.formatDezimal(__getNetvalue())); //Netto Listenpreis
        map.put("netvalue", FormatNumber.formatDezimal(__getNetvalue().subtract(__getDiscountvalue()))); //Netto Listenpreis mit Rabatt
        map.put("taxvalue", FormatNumber.formatDezimal(__getTaxvalue()));
        map.put("grosnodiscvalue", FormatNumber.formatDezimal(__getTaxvalue().add(__getNetvalue()))); //Brutto Listenpreis
        map.put("grosvalue", FormatNumber.formatDezimal(__getTaxvalue().add(__getNetvalue().subtract(__getNetvalue())))); //Brutto Listenpreis mit Rabatt
        map.put("discountvalue", FormatNumber.formatDezimal(__getDiscountvalue()));
        map.put("discounttax", FormatNumber.formatDezimal(__getDiscountGrosvalue().subtract(__getDiscountvalue())));

        map.put("netnodiscvaluef", FormatNumber.formatLokalCurrency(__getNetvalue()));
        map.put("netvaluef", FormatNumber.formatLokalCurrency(__getNetvalue().subtract(__getDiscountvalue())));
        map.put("taxvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue()));
        map.put("grosnodiscvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().add(__getNetvalue()).add(__getDiscountGrosvalue()).subtract(__getDiscountvalue())));
        map.put("grosvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue().add(__getNetvalue().subtract(__getDiscountvalue()))));
        map.put("discountvaluef", FormatNumber.formatLokalCurrency(__getDiscountvalue()));
        map.put("discounttaxf", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue().subtract(__getDiscountvalue())));

        map.put("discountgrosvaluef", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue()));
        
        Locale l = Locale.getDefault();
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("item.date.locale")) {
            try {
                l = TypeConversion.stringToLocale(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale"));
            } catch (Exception e) {
            }
            if (l != null) {
            } else {
                Log.Debug(this, "Error while using item.date.locale");
            }
        }
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, l);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("org.openyabs.exportproperty.dateformat")) {
            String dd = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.exportproperty.dateformat");
            try {
                df = new SimpleDateFormat(dd, l);
            } catch (Exception e) {
                Log.Debug(this, "Error while using default.date.format: " + e);
            }
        }
        map.put("dateend", df.format(__getDateend()));
        map.put("datetodo", df.format(__getDatetodo()));
        map.put("description", evaluate(__getDescription()));
        

        return super.resolveReferences(map);
    }

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
        r.setBrutvalue(netvalue.add(taxvalue).subtract(discountvalue));
        if (description != null && description.length() > 0) {
            r.setDescription(description);
        } else {
            r.setDescription(Messages.AUTO_GENERATED_VALUE + " " + cnumber);
        }
        r.save();
    }

    @Override
    public boolean save(boolean silent) {
        boolean saved = super.save(silent);
        if (saved) {
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreatepdf")|| GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.autocreatepdf")) {
               toPdf(false);
            }
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreateodt")|| GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.autocreateodt")) {
               toOdt(false);
            }
        }
        return saved;
    }

    @Override
    public String toString() {
        try {
            return ((Contact) getObject(Context.getContact(), contactsids)).__getCname() + "-" + getCname();
        } catch (NodataFoundException ex) {
            return super.toString();
        }
    }

    @Override
    public boolean reset() {
        if (ids > 0) {
            SubItem[] data = getSubitems();
            for (SubItem subItem : data) {
                SubItem.removeFromDeletionQueue(subItem.__getIDS());
            }
        }
        return super.reset();
    }

    /**
     * Fetches all properties for this item from the db
     *
     * @return A (possibly empty) list of {@link ValueProperty}s
     */
    public List<ValueProperty> getProperties() {
        return ValueProperty.getProperties(this);
    }

    @Override
    public boolean delete() {
        SubItem[] it = getSubitems();
        for (SubItem it1 : it) {
            it1.delete();
        }
        setCnumber(FormatHandler.DELETED_IDENTIFIER + __getCnumber());
        setCname(__getCnumber());
        save(true);
        return super.delete();
    }

    @Override
    public boolean undelete() {

        List<DatabaseObject> data = new ArrayList<DatabaseObject>();
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getSubItem(), DatabaseObject.getObject(Context.getSubItem()), true);
            for (DatabaseObject data1 : data) {
                data1.undelete();
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        setCnumber(__getCnumber().replaceFirst(FormatHandler.DELETED_IDENTIFIER, ""));
        setCname(__getCnumber());

        return super.undelete();
    }

    @Override
    public int templateType() {
        return __getInttype();
    }

    /**
     *
     * @return
     */
    @Override
    public int templateGroupIds() {
        return __getGroupsids();
    }

    /**
     * @return the contact
     * @throws mpv5.db.common.NodataFoundException
     * is persisting via contactsids
     */
    @Persistable(false)
    public Contact getContact() throws NodataFoundException {
        return (Contact) getObject(Context.getContact(), contactsids);
    }

    /**
     * @param contact the contact to set
     */
    @Persistable(false)
    public void setContact(Contact contact) {
        setContactsids(contact.__getIDS());
    }

    /**
     * @return the account
     * @throws mpv5.db.common.NodataFoundException
     */
    @Persistable(false)
    public Account getAccount() throws NodataFoundException {
        return (Account) getObject(Context.getAccounts(), accountsids);
    }

    /**
     * @param account the account to set
     */
    @Persistable(false)
    public void setAccount(Account account) {
        setAccountsids(account.__getIDS());
    }

    /**
     * @return the status
     */
    @Persistable(false)
    public String getStatus() {
        return getStatusString(intstatus);
    }
}
