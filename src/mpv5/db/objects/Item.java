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


import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import mpv5.utils.export.ODTFile2;
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
    public static String getTypeString(Integer type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case (TYPE_INVOICE):
                return Messages.TYPE_INVOICE.toString();
            case (TYPE_OFFER):
                return Messages.TYPE_OFFER.toString();
            case (TYPE_ORDER):
                return Messages.TYPE_ORDER.toString();
            case (TYPE_ORDER_CONFIRMATION):
                return Messages.TYPE_CONFIRMATION.toString();
            case (TYPE_DELIVERY_NOTE):
                return Messages.TYPE_DELIVERY.toString();
            case (TYPE_DEPOSIT):
                return Messages.TYPE_DEPOSIT.toString();
            case (TYPE_PART_PAYMENT):
                return Messages.TYPE_PART_PAYMENT.toString();
            case (TYPE_CREDIT):
                return Messages.TYPE_CREDIT.toString();
        }
        return "";
    }

    public static MPEnum[] getItemEnum() {
        MPEnum[] en = new MPEnum[7];
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
                return TYPE_INVOICE;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_INVOICE);
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
        en[4] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_DEPOSIT;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_DEPOSIT);
            }
        };
        en[5] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_PART_PAYMENT;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_PART_PAYMENT);
            }
        };
        en[6] = new MPEnum() {

            @Override
            public Integer getId() {
                return TYPE_CREDIT;
            }

            @Override
            public String getName() {
                return getTypeString(TYPE_CREDIT);
            }
        };

        return en;
    }

    public static Item createFor(Contact dataOwner) {
        Item i = (Item) DatabaseObject.getObject(Context.getInvoice());
        i.setContactsids(dataOwner.__getIDS());
        i.setCnumber(Messages.NEW_INVOICE.getValue());
        i.buildCname();
        i.setInttype(Item.TYPE_INVOICE);
        i.setDateadded(new Date());
        i.setGroupsids(dataOwner.__getGroupsids());
        return i;
    }
    private int contactsids;
    private int reforderids;
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
    private FormatHandler formatHandler;

    public Item() {
        //context not known
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
        ItemPanel2 p = null;
        switch (__getInttype()) {
            case Item.TYPE_INVOICE:
                return new ItemPanel2(Context.getInvoice(), __getInttype());
            case Item.TYPE_ORDER:
                return new ItemPanel2(Context.getOrder(), __getInttype());
            case Item.TYPE_OFFER:
                return new ItemPanel2(Context.getOffer(), __getInttype());
            case Item.TYPE_DEPOSIT:
                return new ItemPanel2(Context.getDeposit(), __getInttype());
            case Item.TYPE_PART_PAYMENT:
                return new ItemPanel2(Context.getPartPayment(), __getInttype());
            case Item.TYPE_CREDIT:
                return new ItemPanel2(Context.getCredit(), __getInttype());
        }
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
     * <li>QUEUED = 0; <li>IN_PROGRESS= 1; <li>PAUSED = 2;
     * <li>FINNISHED= 3;
     *
     * @return the intstatus
     */
    public int __getIntstatus() {
        return intstatus;
    }

    /**
     * <li>QUEUED = 0; <li>IN_PROGRESS= 1; <li>PAUSED = 2;
     * <li>FINNISHED= 3;
     *
     * @param intstatus the intstatus to set
     */
    public void setIntstatus(int intstatus) {
        this.intstatus = intstatus;
    }

    /**
     * <li>TYPE_INVOICE = 0; <li>TYPE_ORDER = 1;
     * <li>TYPE_OFFER = 2; <li>TYPE_DEPOSIT = 22;
     * <li>TYPE_PART_PAYMENT = 21; <li>TYPE_CREDIT = 20;
     *
     * @return the inttype
     */
    public int __getInttype() {
        return inttype;
    }

    /**
     * <li>TYPE_INVOICE = 0; <li>TYPE_ORDER = 1;
     * <li>TYPE_OFFER = 2; <li>TYPE_DEPOSIT = 22;
     * <li>TYPE_PART_PAYMENT = 21; <li>TYPE_CREDIT = 20;
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
        switch (__getIntstatus()) {
            case STATUS_QUEUED:
                return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
            case STATUS_IN_PROGRESS:
                return new MPIcon("/mpv5/resources/images/22/run.png");
            case STATUS_PAUSED:
                return new MPIcon("/mpv5/resources/images/22/kalarm.png");
            case STATUS_FINISHED:
                return new MPIcon("/mpv5/resources/images/22/knewstuff.png");
            case STATUS_PAID:
                return new MPIcon("/mpv5/resources/images/22/ok.png");
            case STATUS_CANCELLED:
                return new MPIcon("/mpv5/resources/images/22/file_temporary.png");
            default:
                return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
        }
    }

    @Persistable(false)
    public Item getReferenceOrder() {
        if (reforderids > 0) {
            try {
                return (Item) getObject(Context.getOrder(), reforderids);
            } catch (NodataFoundException ex) {
                Logger.getLogger(Item.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public Integer __getRefOrderIDS() {
        if (reforderids > 0) {
            return reforderids;
        } else {
            return (Integer) null;
        }
    }

    public void setReforderids(int reforderids) {
        this.reforderids = reforderids;
    }

    public BigDecimal __getDiscountgrosvalue() {
        return discountgrosvalue;
    }

    public void setDiscountgrosvalue(BigDecimal discountgrosvalue) {
        this.discountgrosvalue = discountgrosvalue;
    }

    /**
     * @return the formatHandler
     */
    @Override
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
        buildCname();
        Log.Debug(this, "ensureUniqueness result: " + __getCnumber());
    }

    /**
     * Fetches all related {@link Subitem}s to this {@link Item} If no subitems
     * are assigned, returns an empty default list of default subitems
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
        //done before call resolveValueProperties(map);

        Log.Debug(this, "\n\n\n\n\n\n"+ map.keySet()+"\n\n\n\n\n\n");

        map.put("status", getStatus());
        map.put("type", getTypeString(inttype));
        if (accountsids > 0) {
            try {
                map.put("account", DatabaseObject.getObject(Context.getAccounts(), accountsids));
            } catch (NodataFoundException ex) {
                Logger.getLogger(Item.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            map.put("contact", DatabaseObject.getObject(Context.getContact(), contactsids));
        } catch (NodataFoundException ex) {
            Logger.getLogger(Item.class.getName()).log(Level.SEVERE, null, ex);
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

        map.put(ODTFile2.KEY_TABLE + "1", list);

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
        map.put("grosvaluef", FormatNumber.formatLokalCurrency(getGrossAmount()));
        map.put("discountvaluef", FormatNumber.formatLokalCurrency(__getDiscountvalue()));
        map.put("discounttaxf", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue().subtract(__getDiscountvalue())));

        map.put("discountgrosvaluef", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue()));

        Map<Integer, BigDecimal> taxGroups = new HashMap<Integer, BigDecimal>();
        for (SubItem s : getSubitems()) {
            Integer taxPercentValue = s.__getTaxpercentvalue().intValue();
            if (!taxGroups.containsKey(taxPercentValue)) {
                taxGroups.put(taxPercentValue, BigDecimal.ZERO);
            }
            taxGroups.put(taxPercentValue, taxGroups.get(taxPercentValue).add(s.getTotalTaxValue()));
        }

        if (taxGroups.size() > 0) {
            //backwards compatibility
            map.put("tax", FormatNumber.formatPercent(taxGroups.keySet().iterator().next()));
        }

        int index = 0;
        String taxGroupsText = "";
        for (Map.Entry<Integer, BigDecimal> taxGroup : taxGroups.entrySet()) {
            index++;
            map.put("taxgroupvalue" + index, FormatNumber.formatDezimal(taxGroup.getValue()));
            map.put("taxgroup" + index, taxGroup.getKey());

            taxGroupsText += taxGroup.getKey() + "%\t" + FormatNumber.formatLokalCurrency(taxGroup.getValue()) + "\n";
        }

        map.put("taxgroups", taxGroupsText);

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
        map.put("description", evaluateAll(__getDescription()));
        map.put("reference", getReferenceOrder());

        return super.resolveReferences(map);
    }

    @Override
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
        buildCname();
        boolean saved = super.save(silent);
        if (saved) {
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreatepdf") || GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.autocreatepdf")) {
                toPdf(false);
            }
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreateodt") || GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.autocreateodt")) {
                toOdt(false);
            }
        }
        return saved;
    }

    @Override
    public String toString() {
        try {
            if (getCname().equals(cnumber)) {
                return ((Contact) getObject(Context.getContact(), contactsids)).__getCname()
                        + "-" + getCname()
                        + " (" + (FormatNumber.formatLokalCurrency(getGrossAmount())) + ")";
            } else {
                return getCname();
            }
        } catch (NodataFoundException ex) {
            return getCname() + " (" + (FormatNumber.formatLokalCurrency(getGrossAmount())) + ")";
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
        setCname(__getCname());
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
        setCname(__getCname());

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
     * @throws mpv5.db.common.NodataFoundException is persisting via contactsids
     */
    @Persistable(false)
    @Relation(true)
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
    @Relation(true)
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

    @Override
    public boolean hasView() {
        switch (__getInttype()) {
            case Item.TYPE_INVOICE:
            case Item.TYPE_ORDER:
            case Item.TYPE_OFFER:
            case Item.TYPE_PART_PAYMENT:
            case Item.TYPE_CREDIT:
            case Item.TYPE_DEPOSIT:
                return true;
            default:
                return false;
        }
    }

    public BigDecimal getGrossAmount() {
        return __getTaxvalue().add(__getNetvalue().subtract(__getDiscountvalue()));
    }

    private void buildCname() {
        if (cnumber == null) {
            setCname("<not set>");
        } else {
            if (ValueProperty.hasProperty(this.getContext(), "IntItemLabel")) {
                try {
                    ValueProperty script = ValueProperty.getProperty(this.getContext(), "IntItemLabel");
                    Object s = script.getValue();
                    if (s != null) {
                        setCname(this.evaluateAll(String.valueOf(s)));
                    }
                } catch (NodataFoundException ex) {
                    setCname(cnumber);
                }
            } else {
                setCname(cnumber);
            }
        }
    }
}
