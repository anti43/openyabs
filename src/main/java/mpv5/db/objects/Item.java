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
import mpv5.utils.date.DateConverter;
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
    /**
     * the invoice a credit note corrects or cancels (#211)
     */
    private int refinvoiceids;
    /**
     * deposits / part payment invoices: the final invoice they were deducted
     * in (#221)
     */
    private int finalinvoiceids;
    private int accountsids;
    private BigDecimal netvalue = BigDecimal.ZERO;
    private BigDecimal taxvalue = BigDecimal.ZERO;
    private BigDecimal discountvalue = BigDecimal.ZERO;
    private BigDecimal discountgrosvalue = BigDecimal.ZERO;
    /**
     * the service date (Leistungsdatum, § 14 Abs. 4 Nr. 6 UStG, BT-72); the
     * document date if not set (see {@link #save(boolean)})
     */
    private Date datetodo;
    private Date dateend;
    /**
     * when the document was saved last, set by {@link #save(boolean)} (not
     * editable)
     */
    private Date datelastupdated;
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
     * @return the service date (Leistungsdatum), may be null for a document not
     * saved yet
     */
    public Date __getDatetodo() {
        return datetodo;
    }

    /**
     * @param datetodo the service date (Leistungsdatum); null: the document
     * date
     */
    public void setDatetodo(Date datetodo) {
        this.datetodo = datetodo;
    }

    /**
     * @return the service date (Leistungsdatum): the stored one, else the
     * document date
     */
    public Date getServiceDate() {
        Date added = __getDateadded();
        return datetodo != null ? datetodo : added != null && added.getTime() > 0 ? added : null;
    }

    /**
     * @return when the document was saved last, null if unknown (saved before
     * YaBS stored it)
     */
    public Date __getDatelastupdated() {
        return datelastupdated;
    }

    /**
     * Set by {@link #save(boolean)}; needed to read it from the database
     *
     * @param datelastupdated
     */
    public void setDatelastupdated(Date datelastupdated) {
        this.datelastupdated = datelastupdated;
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
        //modern or classic view, as chosen by the user
        mpv5.ui.panels.DataPanel modern = mpv5.YabsViewProxy.instance().getModernView(this);
        if (modern != null) {
            return (JComponent) modern;
        }
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
                return new MPIcon("/images/22/kontact_mail.png");
            case STATUS_IN_PROGRESS:
                return new MPIcon("/images/22/run.png");
            case STATUS_PAUSED:
                return new MPIcon("/images/22/kalarm.png");
            case STATUS_FINISHED:
                return new MPIcon("/images/22/knewstuff.png");
            case STATUS_PAID:
                return new MPIcon("/images/22/ok.png");
            case STATUS_CANCELLED:
                return new MPIcon("/images/22/file_temporary.png");
            default:
                return new MPIcon("/images/22/kontact_mail.png");
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

    /**
     * @return the id of the invoice this credit note corrects or cancels, null
     * if none
     */
    public Integer __getRefinvoiceids() {
        return refinvoiceids > 0 ? refinvoiceids : null;
    }

    /**
     * @param refinvoiceids the id of the invoice this credit note corrects or
     * cancels, &lt;= 0 for none
     */
    public void setRefinvoiceids(int refinvoiceids) {
        this.refinvoiceids = Math.max(refinvoiceids, 0);
    }

    /**
     * @return the id of the final invoice this deposit / part payment invoice
     * was deducted in, null if none
     */
    public Integer __getFinalinvoiceids() {
        return finalinvoiceids > 0 ? finalinvoiceids : null;
    }

    /**
     * @param finalinvoiceids the final invoice this deposit was deducted in,
     * &lt;= 0 for none
     */
    public void setFinalinvoiceids(int finalinvoiceids) {
        this.finalinvoiceids = Math.max(finalinvoiceids, 0);
    }

    /**
     * @return the invoice (invoice, deposit or part payment invoice) this
     * credit note corrects or cancels, null if none
     */
    @Persistable(false)
    public Item getCorrectedInvoice() {
        if (refinvoiceids > 0) {
            for (int type : new int[]{TYPE_INVOICE, TYPE_DEPOSIT, TYPE_PART_PAYMENT}) {
                try {
                    return (Item) getObject(mpv5.handler.ItemService.contextOf(type), refinvoiceids);
                } catch (NodataFoundException ex) {
                    //another type
                }
            }
        }
        return null;
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

                /*@Deprecated
                if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.pdftable", false)) {
                    data2 = t.getValues3();
                    for (String[] strings : data2) {
                        map.put("subitem" + i + "." + strings[0].toLowerCase(), strings[1]);
                    }
                }*/
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
        map.put("netvalue", FormatNumber.formatDezimal(getNetAfterDiscount())); //Netto Listenpreis mit Rabatt
        map.put("taxvalue", FormatNumber.formatDezimal(__getTaxvalue()));
        //net/discount are before tax, the tax is after discount (per VAT rate)
        map.put("grosnodiscvalue", FormatNumber.formatDezimal(getGrossAmount().add(__getDiscountGrosvalue()))); //Brutto Listenpreis
        map.put("grosvalue", FormatNumber.formatDezimal(getGrossAmount())); //Brutto Listenpreis mit Rabatt
        map.put("discountvalue", FormatNumber.formatDezimal(__getDiscountvalue()));
        map.put("discounttax", FormatNumber.formatDezimal(__getDiscountGrosvalue().subtract(__getDiscountvalue())));

        map.put("netnodiscvaluef", FormatNumber.formatLokalCurrency(__getNetvalue()));
        map.put("netvaluef", FormatNumber.formatLokalCurrency(getNetAfterDiscount()));
        map.put("taxvaluef", FormatNumber.formatLokalCurrency(__getTaxvalue()));
        map.put("grosnodiscvaluef", FormatNumber.formatLokalCurrency(getGrossAmount().add(__getDiscountGrosvalue())));
        map.put("grosvaluef", FormatNumber.formatLokalCurrency(getGrossAmount()));
        //payments received (part payments) and what is still open
        if (isExisting()) {
            java.math.BigDecimal paid = mpv5.handler.Payments.paid(__getIDS());
            map.put("paidvaluef", FormatNumber.formatLokalCurrency(paid));
            map.put("openvaluef", FormatNumber.formatLokalCurrency(mpv5.handler.Payments.open(this)));
        }
        map.put("discountvaluef", FormatNumber.formatLokalCurrency(__getDiscountvalue()));
        map.put("discounttaxf", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue().subtract(__getDiscountvalue())));

        map.put("discountgrosvaluef", FormatNumber.formatLokalCurrency(__getDiscountGrosvalue()));

        //tax per VAT rate as on the invoice (EN 16931: rounded once per rate, so the
        //groups add up to the tax total); rates with decimals (7.7 %) are kept apart
        Map<String, BigDecimal> taxGroups = new java.util.LinkedHashMap<String, BigDecimal>();
        java.util.List<mpv5.utils.numberformat.InvoiceMath.Line> lines = new java.util.ArrayList<mpv5.utils.numberformat.InvoiceMath.Line>();
        SubItem[] subitems = getSubitems();
        for (SubItem s : subitems) {
            lines.add(new mpv5.utils.numberformat.InvoiceMath.Line(s.effectiveQuantity(), s.__getExternalvalue(), s.__getDiscount(), s.__getTaxpercentvalue()));
        }
        //the tax rate of each position (customer, product, value): VAT category and note (#209)
        java.util.List<Tax> taxes = Tax.all();
        Contact taxContact = null;
        try {
            taxContact = __getContactsids() > 0 ? getContact() : null;
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        java.util.function.Function<SubItem, Tax> taxRates = mpv5.utils.export.ZugpferdFile.taxRates(taxContact, taxes);
        for (Map.Entry<BigDecimal, BigDecimal[]> rate : mpv5.utils.numberformat.InvoiceMath.totals(lines).byRate.entrySet()) {
            taxGroups.put(rate.getKey().stripTrailingZeros().toPlainString(), rate.getValue()[1]);
        }

        if (taxGroups.size() > 0) {
            //backwards compatibility
            map.put("tax", FormatNumber.formatPercent(new BigDecimal(taxGroups.keySet().iterator().next())));
        }

        int index = 0;
        String taxGroupsText = "";
        for (Map.Entry<String, BigDecimal> taxGroup : taxGroups.entrySet()) {
            index++;
            map.put("taxgroupvalue" + index, FormatNumber.formatDezimal(taxGroup.getValue()));
            map.put("taxgroup" + index, taxGroup.getKey());

            taxGroupsText += taxGroup.getKey() + "%\t" + FormatNumber.formatLokalCurrency(taxGroup.getValue()) + "\n";
        }

        map.put("taxgroups", taxGroupsText);
        //the VAT breakdown per rate (§ 14 Abs. 4 Nr. 8 UStG), the highest rate first; the keys
        //above (tax, taxgroupN, taxgroupvalueN, taxgroups) stay for older templates
        java.util.Map<BigDecimal, Tax> rateOfGroup = new java.util.HashMap<BigDecimal, Tax>();
        for (SubItem s : subitems) {
            BigDecimal r = s.__getTaxpercentvalue() == null ? BigDecimal.ZERO : s.__getTaxpercentvalue();
            if (!rateOfGroup.containsKey(r.stripTrailingZeros())) {
                rateOfGroup.put(r.stripTrailingZeros(), taxRates.apply(s));
            }
        }
        putVatGroups(map, lines, r -> rateOfGroup.containsKey(r.stripTrailingZeros())
                ? rateOfGroup.get(r.stripTrailingZeros()) : Tax.forRate(r, taxes));
        map.put("taxnotes", taxNotes(subitems, taxRates, mpv5.utils.export.SmallBusiness.isEnabled(),
                mpv5.utils.export.ZugpferdFile.companySettings().apply(mpv5.utils.export.ZugpferdFile.KEY_TAX_EXEMPTION_REASON)));

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
        map.put("dateend", __getDateend() == null ? "" : df.format(__getDateend()));
        map.put("datetodo", __getDatetodo() == null ? "" : df.format(__getDatetodo()));
        //the service date (Leistungsdatum): the document date if not set
        map.put("servicedate", getServiceDate() == null ? "" : df.format(getServiceDate()));
        map.put("datelastupdated", __getDatelastupdated() == null ? "" : df.format(__getDatelastupdated()));
        map.put("description", evaluateAll(__getDescription()));
        map.put("reference", getReferenceOrder());
        //the invoice a credit note corrects or cancels (§ 14 Abs. 4 / § 31 Abs. 5 UStDV: a clear reference)
        Item corrected = getCorrectedInvoice();
        map.put("correctedinvoicenumber", corrected == null ? "" : corrected.__getCnumber());
        map.put("correctedinvoicedate", corrected == null || corrected.__getDateadded() == null ? "" : df.format(corrected.__getDateadded()));
        map.put("correctedinvoice", corrected == null ? "" : String.format(mpv5.globals.Messages.ITEM_CORRECTED_INVOICE_TEXT.toString(),
                corrected.__getCnumber(), corrected.__getDateadded() == null ? "" : df.format(corrected.__getDateadded())));
        //a final invoice: the deposits / part payment invoices deducted in it (§ 14 Abs. 5 UStG, #221)
        putDeductedDeposits(map, __getInttype() == TYPE_INVOICE ? mpv5.handler.ItemService.deductedIn(this)
                : new ArrayList<Item>(), df);
        putPaymentFields(map, df);

        return super.resolveReferences(map);
    }

    /**
     * Template fields of a final invoice: item.finalinvoice ("true" /
     * "false"), item.deductedinvoices (one line per deducted deposit / part
     * payment invoice: number, date, net, VAT), item.deductednetf and
     * item.deductedtaxf (their sums)
     *
     * @param map
     * @param deposits the deducted deposits
     * @param df
     */
    static void putDeductedDeposits(Map<String, Object> map, List<Item> deposits, DateFormat df) {
        StringBuilder text = new StringBuilder();
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (Item d : deposits) {
            BigDecimal n = d.getNetAfterDiscount();
            BigDecimal t = d.__getTaxvalue() == null ? BigDecimal.ZERO : d.__getTaxvalue();
            net = net.add(n);
            tax = tax.add(t);
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(String.format(mpv5.globals.Messages.FINAL_DEDUCTED_LINE.toString(), getTypeString(d.__getInttype()),
                    d.__getCnumber(), d.__getDateadded() == null ? "" : df.format(d.__getDateadded()),
                    FormatNumber.formatLokalCurrency(n), FormatNumber.formatLokalCurrency(t)));
        }
        map.put("finalinvoice", String.valueOf(!deposits.isEmpty()));
        map.put("deductedinvoices", text.toString());
        map.put("deductednetf", deposits.isEmpty() ? "" : FormatNumber.formatLokalCurrency(net));
        map.put("deductedtaxf", deposits.isEmpty() ? "" : FormatNumber.formatLokalCurrency(tax));
    }

    /**
     * The names of the fields of a VAT group: in the list {@code vatgroups}
     * (a table row of the template repeated per rate: {@code <vatgroups.rate>}
     * ...) and as {@code item.vatgroup1.rate}, {@code item.vatgroup2.rate} ...
     */
    public static final String[] VATGROUP_FIELDS = {"rate", "ratevalue", "name", "net", "netf", "vat", "vatf", "gross", "grossf", "category", "note"};

    /**
     * The notes the positions need on the invoice (reverse charge,
     * intra-community supply, export, exemption ...), one per line: the notes
     * of their tax rates (#209); "" with the small business exemption (it has
     * its own note, companyinfo.smallbusinessnote)
     *
     * @param subitems
     * @param taxRates the tax rate of a position
     * @param smallBusiness
     * @param exemptReason the reason of exempt positions (E) whose rate has no
     * note, null or empty for the default
     * @return the notes, "" if none
     */
    static String taxNotes(SubItem[] subitems, java.util.function.Function<SubItem, Tax> taxRates, boolean smallBusiness, String exemptReason) {
        if (smallBusiness) {
            return "";
        }
        java.util.Set<String> notes = new java.util.LinkedHashSet<String>();
        for (SubItem s : subitems) {
            if (s.getInttype() == SubItem.TYPE_TEXT || s.__getDescription() == null || s.__getDescription().trim().isEmpty()) {
                continue;
            }
            BigDecimal rate = mpv5.utils.numberformat.InvoiceMath.rate(s.__getTaxpercentvalue());
            Tax t = taxRates == null ? null : taxRates.apply(s);
            mpv5.utils.export.VatCategory c = SubItem.vatCategory(t, rate);
            if (c != mpv5.utils.export.VatCategory.S && c.needsReason()) {
                String note = SubItem.exemptionReason(t, c, exemptReason);
                if (note != null && !note.trim().isEmpty()) {
                    notes.add(note.trim());
                }
            }
        }
        return String.join("\n", notes);
    }

    /**
     * Template fields of the VAT breakdown (one group per rate, net, VAT and
     * gross as on the e-invoice): item.vatgroups (the list for a repeated
     * table row), item.vatgroupN.rate / .ratevalue / .name / .net / .netf /
     * .vat / .vatf / .gross / .grossf, item.vatgroupcount and
     * item.hasmultipletaxrates ("true" / "false")
     *
     * @param map
     * @param lines the positions
     */
    static void putVatGroups(Map<String, Object> map, List<mpv5.utils.numberformat.InvoiceMath.Line> lines) {
        final List<Tax> taxes = Tax.all();
        putVatGroups(map, lines, r -> Tax.forRate(r, taxes));
    }

    /**
     * @param map
     * @param lines the positions
     * @param taxOfRate the tax rate of a group (its category and note, item.vatgroupN.category / .note)
     * @see #putVatGroups(Map, List)
     */
    static void putVatGroups(Map<String, Object> map, List<mpv5.utils.numberformat.InvoiceMath.Line> lines,
            java.util.function.Function<BigDecimal, Tax> taxOfRate) {
        List<mpv5.utils.numberformat.InvoiceMath.RateTotal> groups = mpv5.utils.numberformat.InvoiceMath.byRateDescending(lines);
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (int i = 0; i < groups.size(); i++) {
            Map<String, String> row = vatGroup(groups.get(i));
            Tax t = taxOfRate == null ? null : taxOfRate.apply(groups.get(i).rate);
            mpv5.utils.export.VatCategory c = SubItem.vatCategory(t, groups.get(i).rate);
            row.put("category", c.code());
            row.put("note", c.needsReason() ? SubItem.exemptionReason(t, c, null) : "");
            rows.add(row);
            for (Map.Entry<String, String> e : row.entrySet()) {
                map.put("vatgroup" + (i + 1) + "." + e.getKey(), e.getValue());
            }
        }
        map.put("vatgroups", rows);
        map.put("vatgroupcount", groups.size());
        map.put("hasmultipletaxrates", String.valueOf(groups.size() > 1));
    }

    /**
     * @param g
     * @return the fields of one VAT group ({@link #VATGROUP_FIELDS})
     */
    static Map<String, String> vatGroup(mpv5.utils.numberformat.InvoiceMath.RateTotal g) {
        Map<String, String> row = new java.util.LinkedHashMap<String, String>();
        row.put("rate", FormatNumber.formatDezimal(g.rate) + " %");
        row.put("ratevalue", g.rate.stripTrailingZeros().toPlainString());
        row.put("name", vatGroupName(g.rate));
        row.put("net", FormatNumber.formatDezimal(g.net));
        row.put("netf", FormatNumber.formatLokalCurrency(g.net));
        row.put("vat", FormatNumber.formatDezimal(g.tax));
        row.put("vatf", FormatNumber.formatLokalCurrency(g.tax));
        row.put("gross", FormatNumber.formatDezimal(g.gross));
        row.put("grossf", FormatNumber.formatLokalCurrency(g.gross));
        return row;
    }

    /**
     * @param rate
     * @return the name of the tax rate in the settings (Taxes), else "VAT
     * 19,00 %" / "Tax free"
     */
    static String vatGroupName(BigDecimal rate) {
        try {
            if (mpv5.db.common.DatabaseConnection.getConnector() != null
                    && mpv5.db.common.DatabaseConnection.getConnector().getConnection() != null) {
                for (DatabaseObject o : DatabaseObject.getObjects(Context.getTaxes())) {
                    Tax t = (Tax) o;
                    if (t.__getTaxvalue() != null && t.__getTaxvalue().compareTo(rate) == 0
                            && t.__getCname() != null && !t.__getCname().trim().isEmpty()) {
                        return t.__getCname().trim();
                    }
                }
            }
        } catch (Exception e) {
            //no tax rates: the default name
        }
        return rate.signum() == 0 ? mpv5.globals.Messages.VAT_GROUP_FREE.toString()
                : String.format(mpv5.globals.Messages.VAT_GROUP_NAME.toString(), FormatNumber.formatDezimal(rate) + " %");
    }

    /**
     * Template fields for the payment (account the customer pays to, payment
     * term, Skonto), as set for the contact in the tab "E-Invoice", defaults
     * from the company info: item.paymentiban, item.paymentbic,
     * item.paymentbank, item.paymentaccountname, item.paymentdays,
     * item.paymentduedate, item.paymentterms ("Zahlbar bis 29.11.2026" or
     * "Zahlbar sofort ohne Abzug"), item.skontopercent, item.skontodays,
     * item.skontodate, item.skontoamount, item.skontopayamount
     */
    private void putPaymentFields(Map<String, Object> map, DateFormat df) {
        String[] keys = {"paymentiban", "paymentbic", "paymentbank", "paymentaccountname", "paymentdays",
            "paymentduedate", "paymentterms", "skontopercent", "skontodays", "skontodate", "skontoamount", "skontopayamount"};
        for (String k : keys) {
            map.put(k, "");
        }
        try {
            Contact contact = getContact();
            Template template = null;
            try {
                template = mpv5.handler.TemplateHandler.loadTemplate((long) templateGroupIds(), __getInttype());
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            mpv5.utils.export.PaymentTerms terms = mpv5.utils.export.PaymentTerms.forContact(contact,
                    contact.__getPayterm(), mpv5.utils.export.ZugpferdFile.paytermOf(template),
                    mpv5.utils.export.ZugpferdFile.companySettings());
            Date issue = __getDateadded() != null ? __getDateadded() : new Date();
            map.put("paymentdays", String.valueOf(terms.getDays()));
            map.put("paymentduedate", df.format(terms.dueDate(issue)));
            map.put("paymentterms", terms.text(issue));
            mpv5.utils.export.BankAccount account = terms.getAccount();
            if (account != null) {
                map.put("paymentiban", mpv5.utils.text.Iban.format(account.getIban()));
                map.put("paymentbic", account.getBic());
                map.put("paymentbank", account.getBank());
                map.put("paymentaccountname", account.getName());
            }
            if (terms.hasSkonto()) {
                java.math.BigDecimal gross = getGrossAmount();
                java.math.BigDecimal skonto = terms.skontoAmount(gross);
                map.put("skontopercent", terms.formatPercent());
                map.put("skontodays", String.valueOf(terms.getSkontoDays()));
                map.put("skontodate", df.format(DateConverter.addDays(issue, terms.getSkontoDays())));
                map.put("skontoamount", FormatNumber.formatDezimal(skonto));
                map.put("skontopayamount", FormatNumber.formatDezimal(gross.subtract(skonto)));
            }
        } catch (Exception e) {
            Log.Debug(this, "Payment fields: " + e.getMessage());
        }
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
        //net after discount, so that gross - net is the tax
        r.setNetvalue(netvalue.subtract(discountvalue));
        r.setBrutvalue(getGrossAmount());
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
        //no service date: the service was rendered on the document date
        if (datetodo == null) {
            Date added = __getDateadded();
            datetodo = added != null && added.getTime() > 0 ? added : new Date();
        }
        datelastupdated = new Date();
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

    /**
     * Net amount (Entgelt) after deducting line and document discounts:
     * {@code netvalue - discountvalue}. Always satisfies
     * {@code getNetAfterDiscount() + __getTaxvalue() == getGrossAmount()}.
     *
     * @return net amount after discount (never null)
     */
    public BigDecimal getNetAfterDiscount() {
        BigDecimal net = __getNetvalue() == null ? BigDecimal.ZERO : __getNetvalue();
        BigDecimal disc = __getDiscountvalue() == null ? BigDecimal.ZERO : __getDiscountvalue();
        return net.subtract(disc);
    }

    public BigDecimal getGrossAmount() {
        BigDecimal tax = __getTaxvalue() == null ? BigDecimal.ZERO : __getTaxvalue();
        return tax.add(getNetAfterDiscount());
    }

    private void buildCname() {
        if (cnumber == null) {
            setCname("<not set>");
        } else {
            //the label script of the script manager (all documents or the type, see ScriptScope)
            String script = mpv5.db.common.ScriptScope.script(this, "IntItemLabel");
            if (script != null) {
                setCname(this.evaluateAll(script));
            } else {
                setCname(cnumber);
            }
        }
    }
}
