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
import enoa.handler.TemplateHandler;
import java.awt.Color;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import mpv5.YabsViewProxy;
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
import mpv5.ui.panels.ItemPanel;
import mpv5.utils.export.Export;
import mpv5.utils.images.MPIcon;
import mpv5.utils.jobs.Job;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.TypeConversion;

/**
 *
 *
 */
public class ProductOrder extends DatabaseObject implements Formattable, Templateable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns a localized string representation of the given item status
     *
     * @param status
     * @return
     */
    public static String getStatusString(int status) {
        switch (status) {
            case (Item.STATUS_QUEUED):
                return Messages.STATUS_QUEUED.toString();
            case (Item.STATUS_IN_PROGRESS):
                return Messages.STATUS_IN_PROGRESS.toString();
            case (Item.STATUS_PAUSED):
                return Messages.STATUS_PAUSED.toString();
            case (Item.STATUS_FINISHED):
                return Messages.STATUS_FINISHED.toString();
            case (Item.STATUS_PAID):
                return Messages.STATUS_PAID.toString();
            case (Item.STATUS_CANCELLED):
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
                return new Integer(Item.STATUS_QUEUED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[1] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(Item.STATUS_IN_PROGRESS);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[2] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(Item.STATUS_PAUSED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[3] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(Item.STATUS_FINISHED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[4] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(Item.STATUS_PAID);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        en[5] = new MPEnum() {

            @Override
            public Integer getId() {
                return new Integer(Item.STATUS_CANCELLED);
            }

            @Override
            public String getName() {
                return getStatusString(getId());
            }
        };
        return en;
    }
    private Contact contact;
    private BigDecimal netvalue = BigDecimal.ZERO;
    private BigDecimal taxvalue = BigDecimal.ZERO;
    private BigDecimal discountvalue = BigDecimal.ZERO;
    private Date datetodo;
    private Date dateend;
    private int intstatus;
    private int inttype;
    private String description = "";
    private String cnumber = "";

    private FormatHandler formatHandler;

    public ProductOrder() {
        setContext(Context.getProductOrder());
    }

    /**
     * @return the taxvalue
     */
    @Persistable(true)
    public BigDecimal getTaxvalue() {
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
    @Persistable(true)
    public BigDecimal getDiscountvalue() {
        return discountvalue;
    }

    /**
     * @param the discountvalue to set
     */
    public void setDiscountvalue(BigDecimal discountvalue) {
        this.discountvalue = discountvalue;
    }

    /**
     * @return the datetodo
     */
    @Persistable(true)
    public Date getDatetodo() {
        return datetodo;
    }

    /**
     * @param datetodo the datetodo to set
     */
    public void setDatetodo(Date datetodo) {
        this.datetodo = datetodo;
    }

    /**
     * @return the dateend
     */
    @Persistable(true)
    public Date getDateend() {
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
        ItemPanel p = new ItemPanel(Context.getItem(), getInttype());
        return p;
    }

    /**
     * @return the description
     */
    @Persistable(true)
    public String getDescription() {
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
    @Persistable(true)
    public int getIntstatus() {
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
    @Persistable(true)
    public int getInttype() {
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
    @Persistable(true)
    public BigDecimal getNetvalue() {
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
        if (getIntstatus() == Item.STATUS_QUEUED) {
            return new MPIcon("/mpv5/resources/images/22/kontact_mail.png");
        } else if (getIntstatus() == Item.STATUS_IN_PROGRESS) {
            return new MPIcon("/mpv5/resources/images/22/run.png");
        } else if (getIntstatus() == Item.STATUS_PAUSED) {
            return new MPIcon("/mpv5/resources/images/22/kalarm.png");
        } else if (getIntstatus() == Item.STATUS_FINISHED) {
            return new MPIcon("/mpv5/resources/images/22/knewstuff.png");
        } else if (getIntstatus() == Item.STATUS_PAID) {
            return new MPIcon("/mpv5/resources/images/22/ok.png");
        } else if (getIntstatus() == Item.STATUS_CANCELLED) {
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
        setCname(getCnumber());
        Log.Debug(this, "ensureUniqueness result: " + getCnumber());
    }

    /**
     * Fetches all related {@link ProductOrderSubItem}s to this {@link Item}<br/>
     * If no ProductOrderSubItems are assigned, returns an empty default list of
     * default ProductOrderSubItems
     *
     * @return
     */
    public ProductOrderSubItem[] getProductOrderSubitems() {
        List<DatabaseObject> data = new ArrayList<DatabaseObject>();
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getProductOrderSubitem(), DatabaseObject.getObject(Context.getProductOrderSubitem()), false);
        } catch (NodataFoundException ex) {
            for (int i = 0; i < 6; i++) {
                data.add(ProductOrderSubItem.getDefaultItem());
            }
        }
        return data.toArray(new ProductOrderSubItem[]{});
    }

    /**
     * @return the cnumber
     */
    @Persistable(true)
    public String getCnumber() {
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
        resolveValueProperties(map);

        try {
            if (map.containsKey("intstatus")) {
                map.put("status", getStatusString(Integer.valueOf(map.get("intstatus").toString())));
            }
        } catch (Exception numberFormatException) {
            //already resolved?
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

        List<ProductOrderSubItem> data;
        List<String[]> data2;
        ArrayList<String[]> list = new ArrayList<String[]>();

        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getProductOrderSubitem(), new ProductOrderSubItem(), false);
            Collections.sort(data, ProductOrderSubItem.ORDER_COMPARATOR);

            for (int i = 0; i < data.size(); i++) {
                ProductOrderSubItem t = data.get(i);
                list.add(t.toStringArray());

                if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.pdftable", false)) {
                    data2 = t.getValues3();
                    for (int j = 0; j < data2.size(); j++) {
                        String[] strings = data2.get(j);
                        map.put("ProductOrderSubItem" + i + "." + strings[0].toLowerCase(), strings[1]);
                    }
                }
            }

            if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.hidecountfortext", true)) {
                int skipcount = 0;
                for (int i = 0; i < data.size(); i++) {
                    ProductOrderSubItem t = data.get(i);
                    if (t.getInttype() == ProductOrderSubItem.TYPE_TEXT) {
                        skipcount--;
                        Log.Debug(this, "Skipping text ProductOrderSubItem..");
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
        map.put("netvalue", FormatNumber.formatDezimal(getNetvalue()));
        map.put("taxvalue", FormatNumber.formatDezimal(getTaxvalue()));
        map.put("grosvalue", FormatNumber.formatDezimal(getTaxvalue().add(getNetvalue())));
        map.put("discountvalue", FormatNumber.formatDezimal(getDiscountvalue()));

        map.put("netvaluef", FormatNumber.formatLokalCurrency(getNetvalue()));
        map.put("taxvaluef", FormatNumber.formatLokalCurrency(getTaxvalue()));
        map.put("grosvaluef", FormatNumber.formatLokalCurrency(getTaxvalue().add(getNetvalue())));
        map.put("discountvaluef", FormatNumber.formatPercent(getDiscountvalue()));

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
        map.put("dateend", df.format(getDateend()));
        map.put("datetodo", df.format(getDatetodo()));

        return super.resolveReferences(map);
    }

    public void defineFormatHandler(FormatHandler handler) {
        formatHandler = handler;
    }

    @Override
    public boolean save(boolean silent) {
        boolean saved = super.save(silent);
        if (saved) {
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreatepdf")) {
                if (TemplateHandler.isLoaded(this)) {
                    new Job(Export.createFile(this.getFormatHandler().toUserString(), TemplateHandler.loadTemplate(this), this), Export.wait(User.getSaveDir(this))).execute();
                } else {
                    YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")", Color.YELLOW);
                }
            }
            if (mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.property", "autocreateodt")) {
                if (TemplateHandler.isLoaded(this)) {
                    new Job(Export.sourceFile(this.getFormatHandler().toUserString(), TemplateHandler.loadTemplate(this), this), Export.wait(User.getSaveDir(this))).execute();
                } else {
                    YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")", Color.YELLOW);
                }
            }
        }
        return saved;
    }

    @Override
    public String toString() {
        if (getContact() != null) {
            return getContact().__getCname() + "-" + getCname();
        } else {
            return super.toString();
        }
    }

    @Override
    public boolean reset() {
        if (ids > 0) {
            ProductOrderSubItem[] data = getProductOrderSubitems();
            for (int i = 0; i < data.length; i++) {
                ProductOrderSubItem ProductOrderSubItem = data[i];
                ProductOrderSubItem.removeFromDeletionQueue(ProductOrderSubItem.__getIDS());
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
        ProductOrderSubItem[] it = getProductOrderSubitems();
        for (int i = 0; i < it.length; i++) {
            it[i].delete();
        }
        setCnumber(FormatHandler.DELETED_IDENTIFIER + getCnumber());
        setCname(getCnumber());
        save(true);
        return super.delete();
    }

    @Override
    public boolean undelete() {

        List<DatabaseObject> data = new ArrayList<DatabaseObject>();
        try {
            data = DatabaseObject.getReferencedObjects(this, Context.getProductOrderSubitem(), DatabaseObject.getObject(Context.getProductOrderSubitem()), true);
            for (int i = 0; i < data.size(); i++) {
                data.get(i).undelete();
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

        setCnumber(getCnumber().replaceFirst(FormatHandler.DELETED_IDENTIFIER, ""));
        setCname(getCnumber());

        return super.undelete();
    }

    @Override
    public int templateType() {
        return getInttype();
    }

    /**
     *
     * @return
     */
    @Override
    public int templateGroupIds() {
        try {
            return getGroup().__getGroupsids();
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            return 1;
        }
    }

    /**
     * @return the status
     */
    @Persistable(false)
    public String getStatus() {
        return getStatusString(intstatus);
    }

    /**
     * @return the contact
     */
    @Persistable(true)
    public Contact getContact() {
        return contact;
    }

    /**
     * @param contact the contact to set
     */
    @Persistable(true)
    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
