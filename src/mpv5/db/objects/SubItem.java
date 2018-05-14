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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.Triggerable;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Headers;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.ItemPanel2;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import static mpv5.utils.numberformat.FormatNumber.*;

/**
 *
 *
 */
public final class SubItem extends DatabaseObject implements Triggerable {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_PRODUCT = 2;
    public static final int TYPE_NORMAL = 0;
    private static final long serialVersionUID = 1L;

    public static LinkedList<SubItem> saveModel(Item dataOwner, MPTableModel model, boolean deleteRemovedSubitems, boolean cloneSubitems) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        LinkedList<SubItem> items = new LinkedList<SubItem>();
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            SubItem it = new SubItem();
            it.setOrdernr(i);
            Log.Print(Arrays.asList(row));
            try {
                if (!cloneSubitems && row[0] != null && Integer.valueOf(row[0].toString()) > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()));
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(getBigDecimal(row[1]));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(getBigDecimal(row[5]));
            it.setInternalvalue(getBigDecimal(row[5]));//not supported yet
            it.setMeasure(String.valueOf(row[3]));

            if (row[10] instanceof Product) {
                try {
                    it.setOriginalproductsids(((Product) row[10]).__getIDS());
                } catch (Exception e) {
                    Log.Debug(e);
                }
            } else {
            }
            it.setLinkurl(String.valueOf(row[12 + 1]));
            it.setQuantityvalue(getBigDecimal(row[2]));
            it.setTaxpercentvalue(getBigDecimal(row[6]));
            if (row[15].toString().trim().length() > 0) {//maybe not set at all from prodctorders
                it.setDiscount(getBigDecimal(row[15]));
            }
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }
            it.save(true);
            items.add(it.getOrdernr(), it);
        }

        if (deleteRemovedSubitems) {
            for (int i = 0; i < deletionQueue.size(); i++) {
                try {
                    int myid = deletionQueue.get(i);
                    if (myid > 0) {
                        QueryHandler.delete(SubItem.getObject(Context.getSubItem(), myid));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        }
        deletionQueue.clear();
        return items;

    }

    public static LinkedList<SubItem> saveModelMinus(Item dataOwner, MPTableModel model, boolean deleteRemovedSubitems, boolean cloneSubitems, Item item) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        LinkedList<SubItem> items = new LinkedList<SubItem>();
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            SubItem it = new SubItem();
            it.setOrdernr(i);
            Log.Print(Arrays.asList(row));
            try {
                if (!cloneSubitems && row[0] != null && Integer.valueOf(row[0].toString()) > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()));
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(getBigDecimal(row[1]));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(Item.getTypeString(item.__getInttype()) + ":" + DateConverter.getDefDateString(item.__getDateend()));
            it.setExternalvalue(getBigDecimal(row[5]).negate());
            it.setInternalvalue(getBigDecimal(row[5]).negate());//not supported yet
            it.setMeasure(String.valueOf(row[3]));

            if (row[10] instanceof Product) {
                try {
                    it.setOriginalproductsids(((Product) row[10]).__getIDS());
                } catch (Exception e) {
                    Log.Debug(e);
                }
            } else {
            }
            it.setLinkurl(String.valueOf(row[12 + 1]));
            it.setQuantityvalue(getBigDecimal(row[2]));
            it.setTaxpercentvalue(getBigDecimal(row[6]));
            if (row[15].toString().trim().length() > 0) {//maybe not set at all from prodctorders
                it.setDiscount(getBigDecimal(row[15]));
            }
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }
            it.save(true);
            items.add(it.getOrdernr(), it);
        }

        if (deleteRemovedSubitems) {
            for (int i = 0; i < deletionQueue.size(); i++) {
                try {
                    int myid = deletionQueue.get(i);
                    if (myid > 0) {
                        QueryHandler.delete(SubItem.getObject(Context.getSubItem(), myid));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        }
        deletionQueue.clear();
        return items;

    }

    /**
     * Save the model of SubItems
     *
     * @param dataOwner
     * @param model
     * @param deleteRemovedSubitems
     * @return
     */
    public static List<SubItem> saveModel(Item dataOwner, MPTableModel model, boolean deleteRemovedSubitems) {
        return saveModel(dataOwner, model, deleteRemovedSubitems, false);
    }

    /**
     * Convert the model to a list of String arrays
     *
     * @param dataOwner
     * @param model
     * @param t
     * @return
     */
    public static LinkedList<String[]> convertModel(Item dataOwner, MPTableModel model, Template t) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        LinkedList<String[]> rowsk = new LinkedList<String[]>();
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);

            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }

            final SubItem it = new SubItem();
            it.setOrdernr(i);
            try {
                if (row[0] != null && Integer.parseInt(row[0].toString()) > 0) {
                    it.setIDS(Integer.parseInt(row[0].toString()));
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(getBigDecimal(row[1]));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(getBigDecimal(row[5]));
            it.setInternalvalue(getBigDecimal(row[5]));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(getInteger(row[10]));
            if (row[10] instanceof Product) {
                try {
                    it.setOriginalproductsids(((Product) row[10]).__getIDS());
                } catch (Exception e) {
                    Log.Debug(e);
                }
            }
            it.setQuantityvalue(getBigDecimal(row[2]));
            it.setTaxpercentvalue(getBigDecimal(row[6]));
            it.setLinkurl(String.valueOf(row[12 + 1]));
            it.setDiscount(getBigDecimal(row[15]));

            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }

//            it.save();
            rowsk.add(it.getOrdernr(), it.toStringArray());
        }

        return rowsk;
    }
    private static List<Integer> deletionQueue = new ArrayList<Integer>();

    /**
     * Mark a subitem for deletion
     *
     * @param valueAt INT or Entity
     */
    public static void addToDeletionQueue(Object valueAt) {
        Log.Debug(SubItem.class, "Adding to deletionqueue: " + valueAt);
        if (valueAt != null) {
            try {
                Integer isd = Integer.valueOf(valueAt.toString());
                deletionQueue.add(isd);
            } catch (NumberFormatException numberFormatException) {
                Integer isd = ((Entity) valueAt).getValue();
                deletionQueue.add(isd);
            }
        }
    }

    /**
     * UN-Mark a subitem for deletion
     *
     * @param valueAt INT or Entity
     */
    public static void removeFromDeletionQueue(Object valueAt) {
        Log.Debug(SubItem.class, "Removing from deletionqueue: " + valueAt);
        if (valueAt != null) {
            try {
                Integer isd = Integer.valueOf(valueAt.toString());
                deletionQueue.remove(isd);
            } catch (NumberFormatException numberFormatException) {
                Integer isd = ((Entity) valueAt).getValue();
                deletionQueue.add(isd);
            }
        }
    }
    private int itemsids;
    private int ordernr;
    private int inttype;
    private int originalproductsids;
    private BigDecimal countvalue = BigDecimal.ZERO;
    private BigDecimal quantityvalue = BigDecimal.ZERO;
    private String measure = "";
    private String description = "";
    private String linkurl = "";
    private BigDecimal internalvalue = BigDecimal.ZERO;
    private BigDecimal externalvalue = BigDecimal.ZERO;
    private BigDecimal taxpercentvalue = BigDecimal.ZERO;
    private BigDecimal totalnetvalue = BigDecimal.ZERO;
    private BigDecimal totalbrutvalue = BigDecimal.ZERO;
    private Date datedelivery;
    private BigDecimal totaltaxvalue = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal discvalue = BigDecimal.ZERO;

    public SubItem() {
        setContext(Context.getSubItem());
    }

    /**
     *
     * @return
     */
    public static SubItem getDefaultItem() {
        SubItem i = new SubItem();

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            String defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
            i.setMeasure(defunit);
        }
        BigDecimal deftax = BigDecimal.ZERO;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid);
        }
        i.setTaxpercentvalue(deftax);

        BigDecimal defdiscount = BigDecimal.ZERO;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defdiscount")) {
            defdiscount = new BigDecimal(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defdiscount"));
        }
        i.setDiscount(defdiscount);

        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            Double defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(new BigDecimal(defcount));
        } else {
            i.setQuantityvalue(BigDecimal.ONE);
        }

        return i;
    }

    /**
     *
     * @param product
     */
    public SubItem(Product product) {
        this();
        setCname(product.__getCname());
        setDateadded(new Date());
        setDatedelivery(new Date());

////////////////format///////////////////////////////////////////////////////////
        Context contextl = product.getContext();
        String params;
        String vars;
        String key = contextl + mpv5.ui.beans.LightMPComboBox.VALUE_SEARCHFIELDS;
        String prop = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(key);
        if (prop != null && prop.contains("_$")) {
            Log.Debug(this, "format: " + prop);
            try {
                params = "ids";
                vars = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(key);
                String[] vaars = vars.split("_\\$");

                for (int i = 0; i < vaars.length; i++) {
                    try {
                        if (vaars[i] != null && vaars[i].contains("$_")) {
                            params += "," + vaars[i].split("\\$_")[0].replace(",", "").replace("'", "`");
                        }
                    } catch (Exception e) {
                        Log.Debug(e);
                    }
                }

                QueryCriteria qc = new QueryCriteria("ids", product.__getIDS());
                Object[][] result = QueryHandler.instanceOf().clone(Context.getProduct()).select(params, qc);

                String formatString = vars;
                if (formatString != null) {
                    vaars = formatString.split("_\\$");
                    Log.Debug(SubItem.class, formatString + " - length of var string: " + vaars.length);
                }
                String x = "";
                String format = formatString;
                for (int j = 1; j < result[0].length; j++) {
                    String k = String.valueOf(result[0][j]);
                    if (format == null) {
                        x += k;
                    } else {
                        try {
                            format = format.replaceFirst("_\\$(.*?)\\$_", k);
                        } catch (Exception e) {
                            Log.Debug(e);
                        }
                        x = format;
                        Log.Debug(this, x);
                    }
                }

                setDescription(VariablesHandler.parse(x, product));
            } catch (Exception x) {
                Log.Debug(x);
                setDescription(VariablesHandler.parse(product.__getCname(), product));
            }
        } else {
            Log.Debug(SubItem.class, "No format defined..");
            setDescription(VariablesHandler.parse(product.__getCname(), product));
        }
///////////////end format////////////////////////////////////////////////////////
        setExternalvalue(product.findPriceFor(1d));
        setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
        setInternalvalue(product.__getInternalnetvalue());
        setMeasure(product.__getMeasure());
        setOriginalproductsids(product.__getIDS());
        setQuantityvalue(BigDecimal.ONE);
        setTaxpercentvalue(Tax.getTaxValue(product.__getTaxids()));
        setLinkurl(product.__getUrl());
        calculate(this);
    }

    /**
     * Generates a SubItem with useless sample data
     *
     * @return
     */
    public static SubItem getRandomItem() {
        SubItem i = new SubItem();
        i.fillSampleData();
        i.setGroupsids(1);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            String defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
            i.setMeasure(defunit);
        }
        BigDecimal deftax = BigDecimal.ZERO;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid);
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(new BigDecimal(defcount));
        }
        return i;
    }

    /**
     * Generates a String array out of this SubItem
     *
     * @return
     */
    public synchronized String[] toStringArray() {
        calculate(this);

        List<String> all = new LinkedList<String>();

        boolean textitem = getInttype() != TYPE_TEXT;
        String[] possibleCols = new String[]{
            ////////////////// The exported columns///////////////////////////////////////
            /*1*/textitem ? String.valueOf(formatInteger(this.__getCountvalue())) : "",
            /*2*/ textitem ? String.valueOf(formatDezimal(this.__getQuantityvalue())) : "",
            /*3*/ textitem ? __getMeasure() : "",
            /*4*/ __getDescription(),
            /*5*/ textitem ? String.valueOf(formatLokalCurrency(this.__getExternalvalue())) : "",
            /*6*/ textitem ? String.valueOf(formatLokalCurrency(this.__getTotalnetvalue())) : "",
            /*7*/ textitem ? String.valueOf(formatPercent(this.__getTaxpercentvalue())) : "",
            /*8*/ textitem ? String.valueOf(formatLokalCurrency(this.getTotalTaxValue())) : "",
            /*9*/ textitem ? String.valueOf(formatLokalCurrency(this.__getTotalbrutvalue())) : "",
            /*10*/ __getLinkurl(),
            /*11*/ __getCname(),
            /*12*/ textitem ? String.valueOf(formatPercent(this.__getDiscount())) : "",
            /*13*/ textitem ? String.valueOf(formatLokalCurrency(this.totalnetvalue.subtract(this.discvalue))) : ""
        ///////////////////////////////////////////////////////////////////////////////
        };
        List<String> l = Arrays.asList(possibleCols);
        all.addAll(l);
        int lsize = all.size();

        if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.productsresolved", false) && __getOriginalproductsids() > 0) {
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), __getOriginalproductsids());
                List<String[]> vals = p.getValues3();
                Collections.sort(vals, new Comparator<String[]>() {
                    @Override
                    public int compare(String[] o1, String[] o2) {
                        return o1[0].compareTo(o2[0]);
                    }
                });
                for (int i = 0; i < vals.size(); i++) {
                    String[] v = vals.get(i);
                    all.add(v[1]);
                    if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                        Log.Debug(this, "xtablecol:" + (lsize + i + 1) + ": " + v[0] + " ");
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }

        return all.toArray(new String[0]);
    }

    /**
     * @return the itemsids
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemsids the itemsids to set
     */
    public void setItemsids(int itemsids) {
        this.itemsids = itemsids;
    }

    /**
     * @return the originalproductsids
     */
    public int __getOriginalproductsids() {
        return originalproductsids;
    }

    /**
     * @param originalproductsids the originalproductsids to set
     */
    public void setOriginalproductsids(int originalproductsids) {
        this.originalproductsids = originalproductsids;
        if (originalproductsids > 0) {
            setInttype(TYPE_PRODUCT);
        } else {
            setInttype(TYPE_NORMAL);
        }
    }

    /**
     * @return the count
     */
    public BigDecimal __getCountvalue() {
        return countvalue;
    }

    /**
     * @param count the count to set
     */
    public void setCountvalue(BigDecimal count) {
        this.countvalue = count;
    }

    /**
     * @return the quantity
     */
    public BigDecimal __getQuantityvalue() {
        return quantityvalue;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantityvalue(BigDecimal quantity) {
        this.quantityvalue = quantity;
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            setInttype(TYPE_TEXT);
        }
    }

    /**
     * @return the measure
     */
    public String __getMeasure() {
        return measure;
    }

    /**
     * @param measure the measure to set
     */
    public void setMeasure(String measure) {
        this.measure = measure;
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
     * @return the taxpercent
     */
    public BigDecimal __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    /**
     * @param taxpercent the taxpercent to set
     */
    public void setTaxpercentvalue(BigDecimal taxpercent) {
        this.taxpercentvalue = taxpercent;
    }

    /**
     * @return the datedelivery
     */
    public Date __getDatedelivery() {
        return datedelivery;
    }

    /**
     * @param datedelivery the datedelivery to set
     */
    public void setDatedelivery(Date datedelivery) {
        this.datedelivery = datedelivery;
    }

    /**
     *
     * @return
     */
    public BigDecimal __getDiscount() {
        return discount;
    }

    /**
     *
     * @param discount
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getDiscvalue() {
        return discvalue;
    }

    public void defDiscvalue(BigDecimal discvalue) {
        this.discvalue = discvalue;
    }

    @Override
    public JComponent getView() {
        try {
            Item dos = (Item) DatabaseObject.getObject(Context.getInvoice(), __getItemsids());
            ItemPanel2 ip = null;
            switch (dos.__getInttype()) {
                case Constants.TYPE_OFFER:
                    ip = new ItemPanel2(Context.getOffer(), dos.__getInttype());
                    break;
                case Constants.TYPE_ORDER:
                    ip = new ItemPanel2(Context.getOrder(), dos.__getInttype());
                    break;
                case Constants.TYPE_INVOICE:
                    ip = new ItemPanel2(Context.getInvoice(), dos.__getInttype());
                    break;
                case Constants.TYPE_CREDIT:
                    ip = new ItemPanel2(Context.getCredit(), dos.__getInttype());
                    break;
                case Constants.TYPE_PART_PAYMENT:
                    ip = new ItemPanel2(Context.getPartPayment(), dos.__getInttype());
                    break;
                case Constants.TYPE_DEPOSIT:
                    ip = new ItemPanel2(Context.getDeposit(), dos.__getInttype());
                    break;
            }
            return ip;
        } catch (NodataFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * Generates a table model out of the given SubItems
     *
     * @param items
     * @return
     */
    public static MPTableModel toModel(SubItem[] items) {
        return toModel(items, false);
    }
    /**
     * The ADD button index
     */
    public static int COLUMNINDEX_ADD = 11;
    /**
     * The CLEAR button index
     */
    public static int COLUMNINDEX_REMOVE = 12;
    /**
     * Compares by ordernr
     */
    public static Comparator<SubItem> ORDER_COMPARATOR = new Comparator<SubItem>() {
        @Override
        public int compare(SubItem o1, SubItem o2) {
            return (o1.getOrdernr() > o2.getOrdernr()) ? 1 : -1;
        }
    };

    /**
     * Generates a table model out of the given SubItems
     *
     * @param items
     * @param removeSubitemIds
     * @return
     */
    public static MPTableModel toModel(SubItem[] items, boolean removeSubitemIds) {
        Object[][] data = new Object[items.length][];
        List<SubItem> tlist = new LinkedList<SubItem>();
        tlist.addAll(Arrays.asList(items));
        Collections.sort(tlist, SubItem.ORDER_COMPARATOR);

        for (int i = 0; i < tlist.size(); i++) {
            SubItem subItem = tlist.get(i);
            if (subItem != null) {
                data[i] = subItem.getRowData(i + 1);
            } else {
                data[i] = getDefaultItem().getRowData(i + 1);
            }
            if (removeSubitemIds) {
                data[i][0] = -1;
            }
        }

        MPTableModel model = new MPTableModel(
                new Class[]{
                    /*"Internal ID", Position,      "Count",          "Measure",    "Text",      "Netto Price",     "Tax Rate",       "Total Price",*/
                    Integer.class, Integer.class, BigDecimal.class, String.class, Object.class, BigDecimal.class, BigDecimal.class, BigDecimal.class,
                    /*"Tax value",       "Net 2",         "Product ID",   "",            "",           "Link",        "Optional",   "Discount",       "Discount Value"*/
                    BigDecimal.class, BigDecimal.class, Product.class, JButton.class, JButton.class, String.class, String.class, BigDecimal.class, BigDecimal.class,
                    /*Discount with tax*/
                    BigDecimal.class},
                new boolean[]{false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true, true, true},
                data,
                Headers.SUBITEMS.getValue());

        model.setContext(Context.getSubItem());
        String defunit = null;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
        }
        BigDecimal deftax = BigDecimal.ZERO;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
        }
        model.defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0, 0.0, 0.0, null, "A", "C", "", "", 0.0, 0.0});
        model.setAutoCountColumn(1);

        return model;
    }

    /**
     * Turn this SubItem into table row data
     *
     * @param row
     * @return
     */
    public synchronized Object[] getRowData(int row) {
        Object[] data = new Object[17];
        for (int i = 0; i < data.length; i++) {
            data[0] = __getIDS();
            data[1] = row;
            data[2] = __getQuantityvalue();
            data[3] = __getMeasure();
            data[4] = __getDescription();
            data[5] = __getExternalvalue();
            data[6] = __getTaxpercentvalue();
            data[7] = __getQuantityvalue().multiply(__getExternalvalue().multiply(((__getTaxpercentvalue().divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE)))));
            data[8] = 0.0;
            data[9] = 0.0;
            if (__getOriginalproductsids() > 0) {
                try {
                    data[10] = Product.getObject(Context.getProduct(), __getOriginalproductsids());
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
            data[11] = "A";
            data[12] = "C";
            data[12 + 1] = __getLinkurl();
            data[14] = __getCname();
            data[15] = __getDiscount();
            data[16] = 0.0;
        }
        return data;
    }

    /**
     * Turn this SubItem into table row data
     *
     * @param m
     * @param row
     * @return
     */
    public static synchronized SubItem getFromModel(MPTableModel m, int row) {
        SubItem t = new SubItem();

        if (m.getValueAt(row, 0) != null) {
            t.setIDS(Integer.valueOf(m.getValueAt(row, 0).toString()));
        }
        if (m.getValueAt(row, 1) != null) {
            t.setCountvalue(getBigDecimal(m, row, 1));
        }
        if (m.getValueAt(row, 2) != null) {
            t.setQuantityvalue(getBigDecimal(m, row, 2));
        }
        if (m.getValueAt(row, 3) != null) {
            t.setMeasure(m.getValueAt(row, 3).toString());
        }
        if (m.getValueAt(row, 4) != null) {
            t.setDescription(m.getValueAt(row, 4).toString());
        }
        if (m.getValueAt(row, 5) != null) {
            t.setExternalvalue(getBigDecimal(m, row, 5));
        }
        if (m.getValueAt(row, 6) != null) {
            t.setTaxpercentvalue(getBigDecimal(m, row, 6));
        }
        if (m.getValueAt(row, 10) != null) {
            try {
                t.setOriginalproductsids(((Product) m.getValueAt(row, 10)).__getIDS());
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
        if (m.getValueAt(row, 13) != null) {
            t.setLinkurl(m.getValueAt(row, 13).toString());
        }
        if (m.getValueAt(row, 14) != null) {
            t.setCname(m.getValueAt(row, 14).toString());
        }
        if (m.getValueAt(row, 15) != null) {
            t.setDiscount(getBigDecimal(m, row, 15));
        }

        return t;
    }

    /**
     * @return the internalvalue
     */
    public BigDecimal __getInternalvalue() {
        return internalvalue;
    }

    /**
     * @param internalvalue the internalvalue to set
     */
    public void setInternalvalue(BigDecimal internalvalue) {
        this.internalvalue = internalvalue;
    }

    /**
     * @return the externalvalue
     */
    public BigDecimal __getExternalvalue() {
        return externalvalue;
    }

    /**
     * @param externalvalue the externalvalue to set
     */
    public void setExternalvalue(BigDecimal externalvalue) {
        this.externalvalue = externalvalue;
    }

    @Override
    public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {

        if (map.containsKey("originalproductsids")) {
            try {
                try {
                    map.put("originalproduct", DatabaseObject.getObject(Context.getProduct(), Integer.valueOf(map.get("originalproductsids").toString())));
                    map.remove("originalproductsids");
                } catch (NodataFoundException ex) {
                    map.put("originalproduct", null);
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        return super.resolveReferences(map);
    }

    /**
     * @return the totalnetvalue
     */
    public BigDecimal __getTotalnetvalue() {
        return totalnetvalue;
    }

    /**
     * @param totalnetvalue the totalnetvalue to set
     */
    public void setTotalnetvalue(BigDecimal totalnetvalue) {
        this.totalnetvalue = totalnetvalue;
    }

    /**
     * @return the totalbrutvalue
     */
    public BigDecimal __getTotalbrutvalue() {
        return totalbrutvalue;
    }

    /**
     * @param totalbrutvalue the totalbrutvalue to set
     */
    public void setTotalbrutvalue(BigDecimal totalbrutvalue) {
        this.totalbrutvalue = totalbrutvalue;
    }

    static void calculate(SubItem s) {
        BigDecimal disc = s.__getDiscount().divide(BD100, 9, BigDecimal.ROUND_HALF_UP);
        s.setTotalbrutvalue(s.quantityvalue.multiply(s.externalvalue.multiply(((s.taxpercentvalue.divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP)).add(BigDecimal.ONE)))));
        s.setTotalbrutvalue(s.totalbrutvalue.subtract(s.totalbrutvalue.multiply(disc)));
        s.defTotaltaxvalue(s.quantityvalue.multiply(s.externalvalue.multiply((s.taxpercentvalue.divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP)))));
        s.defTotaltaxvalue(s.totaltaxvalue.subtract(s.totaltaxvalue.multiply(disc)));
        s.setTotalnetvalue(s.quantityvalue.multiply(s.externalvalue));
        s.defDiscvalue(s.totalnetvalue.multiply(disc));
    }

    @Override
    public boolean save() {
        calculate(this);
        return super.save();
    }

    private void defTotaltaxvalue(BigDecimal value) {
        totaltaxvalue = value;
    }

    /**
     *
     * @return
     */
    public BigDecimal getTotalTaxValue() {
        return totaltaxvalue;
    }

    /**
     * @return the linkurl
     */
    public String __getLinkurl() {
        return linkurl;
    }

    /**
     * @param linkurl the linkurl to set
     */
    public void setLinkurl(String linkurl) {
        this.linkurl = linkurl;
    }

    @Override
    public boolean save(boolean b) {
        if (getCname().length() == 0) {
            setCname("   ");
        }
        return super.save(b);
    }

    @Override
    public void triggerOnCreate() {
        if (__getOriginalproductsids() > 0 && getContext().equals(Context.getInvoice())) {
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), originalproductsids);
                if (p.__getIntinventorytype() == 1) {
                    p.setStockvalue(p.__getStockvalue().subtract(__getQuantityvalue()));
                    p.save(true);
                }
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());//jemand hats gelöscht:-/
                originalproductsids = -1;
                save(true);
            }
        }
    }

    @Override
    public void triggerOnUpdate() {
    }

    @Override
    public void triggerOnDelete() {
        // Log.Debug(this, "Deleting... " + IDENTITY);
    }

    @Override
    public String toString() {
        if (description != null && description.length() > 15) {
            return description.substring(0, 14);
        } else {
            return description;
        }
    }

    /**
     * @return the order
     */
    @Persistable(true)
    public int getOrdernr() {
        return ordernr;
    }

    /**
     * @param ordernr the order to set
     */
    public void setOrdernr(int ordernr) {
        this.ordernr = ordernr;
    }

    /**
     * @return the inttype
     */
    @Persistable(true)
    public int getInttype() {
        return inttype;
    }

    /**
     * @param inttype the inttype to set
     */
    public void setInttype(int inttype) {
        this.inttype = inttype;
    }

    @Persistable(false)
    public Product getProduct() {
        if (originalproductsids > 0) {
            try {
                return (Product) Product.getObject(Context.getProduct(), originalproductsids);
            } catch (NodataFoundException ex) {
                Logger.getLogger(SubItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
