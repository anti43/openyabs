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

import mpv5.db.common.Triggerable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.panels.ItemPanel;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class SubItem extends DatabaseObject implements Triggerable {

    public static List<SubItem> saveModel(Item dataOwner, MPTableModel model, boolean deleteRemovedSubitems, boolean cloneSubitems) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        List<SubItem> items = new Vector<SubItem>();
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            SubItem it = new SubItem();
            try {
                if (!cloneSubitems && row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCName(row[14].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(new BigDecimal(row[1].toString()));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(new BigDecimal(row[5].toString()));
            it.setInternalvalue(new BigDecimal(row[5].toString()));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            it.setQuantityvalue(new BigDecimal(row[2].toString()));
            it.setTaxpercentvalue(new BigDecimal(row[6].toString()));
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }
            it.save(true);
            items.add(it);
        }

        for (int i = 0; i < deletionQueue.size(); i++) {
            if (deleteRemovedSubitems) {
                try {
                    SubItem.getObject(Context.getSubItem(), deletionQueue.get(i)).delete();
                } catch (NodataFoundException ex) {
                }
            } else {
                deletionQueue.remove(i);
            }
        }
        deletionQueue.clear();
        return items;

    }

    /**
     * Save the model of SubItems
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
     * @param dataOwner
     * @param model
     * @param t
     * @return
     */
    public static Vector<String[]> convertModel(Item dataOwner, MPTableModel model, Template t) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        Vector<String[]> rowsk = new Vector<String[]>();
        final List<SubItem> its = new Vector<SubItem>();
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);

            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }

            final SubItem it = new SubItem();
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCName(row[14].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(new BigDecimal(row[1].toString()));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(new BigDecimal(row[5].toString()));
            it.setInternalvalue(new BigDecimal(row[5].toString()));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(new BigDecimal(row[2].toString()));
            it.setTaxpercentvalue(new BigDecimal(row[6].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }

//            it.save();
            rowsk.add(it.toStringArray());
        }

        return rowsk;
    }

    /**
     * Add some value
     * @param table 
     * @param percentValue
     * @param panel dirty dirty
     */
    public static void changeValueFields(JTable table, Integer percentValue, ItemPanel panel) {
        List<Object[]> rowsl = ((MPTableModel) table.getModel()).getValidRows(new int[]{4});
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        SubItem[] items = new SubItem[rowsl.size()];
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            SubItem it = new SubItem();
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(SubItem.class, e.getMessage());
            }
            it.setCName(row[14].toString());
//            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(new BigDecimal(row[1].toString()));
//            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(new BigDecimal(row[5].toString()).multiply(((new BigDecimal(percentValue.toString()).divide(new BigDecimal("100"))).add(BigDecimal.ONE))));
            it.setInternalvalue(new BigDecimal(row[5].toString()).multiply(((new BigDecimal(percentValue.toString()).divide(new BigDecimal("100"))).add(BigDecimal.ONE))));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(new BigDecimal(row[2].toString()));
            it.setTaxpercentvalue(new BigDecimal(row[6].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            calculate(it);
            items[i] = it;

//
//            if (!it.isExisting()) {
//                it.setDateadded(new Date());
//                it.setGroupsids(dataOwner.__getGroupsids());
//            }
//            it.save(true);
        }

        table.setModel(toModel(items));

        if (panel != null) {
            panel.formatTable();
        }
    }
    private static List<Integer> deletionQueue = new Vector<Integer>();

    /**
     * Mark a subitem for deletion
     * @param valueAt
     */
    public static void addToDeletionQueue(Object valueAt) {
        Log.Debug(SubItem.class, "Adding to deletionqueue: " + valueAt);
        if (valueAt != null) {
            try {
                Integer isd = Integer.valueOf(valueAt.toString());
                deletionQueue.add(isd);
            } catch (NumberFormatException numberFormatException) {
            }
        }
    }

    /**
     * UN-Mark a subitem for deletion
     * @param valueAt
     */
    public static void removeFromDeletionQueue(Object valueAt) {
        Log.Debug(SubItem.class, "Removing from deletionqueue: " + valueAt);
        if (valueAt != null) {
            try {
                Integer isd = Integer.valueOf(valueAt.toString());
                deletionQueue.remove(isd);
            } catch (NumberFormatException numberFormatException) {
            }
        }
    }
    private int itemsids;
    private int originalproductsids;
    private BigDecimal countvalue = new BigDecimal("0");
    private BigDecimal quantityvalue = new BigDecimal("0");
    private String measure = "";
    private String description = "";
    private String linkurl = "";
    private BigDecimal internalvalue = new BigDecimal("0");
    private BigDecimal externalvalue = new BigDecimal("0");
    private BigDecimal taxpercentvalue = new BigDecimal("0");
    private BigDecimal totalnetvalue = new BigDecimal("0");
    private BigDecimal totalbrutvalue = new BigDecimal("0");
    private Date datedelivery;
    private BigDecimal totaltaxvalue = new BigDecimal("0");

    public SubItem() {
        context = Context.getSubItem();
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
        BigDecimal deftax = new BigDecimal("0");
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", new Integer(0));
            deftax = Tax.getTaxValue(taxid);
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(new BigDecimal(defcount.toString()));
        }

        i.setQuantityvalue(new BigDecimal("1"));

        return i;
    }

    /**
     *
     * @param product 
     */
    public SubItem(Product product) {
        this();
        setCName(product.__getCName());
        setDateadded(new Date());
        setDatedelivery(new Date());

////////////////format///////////////////////////////////////////////////////////
        Context contextl = product.getContext();
        String params = "cname";
        String vars = null;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(contextl + mpv5.ui.beans.LightMPComboBox.VALUE_SEARCHFIELDS)
                && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(contextl + mpv5.ui.beans.LightMPComboBox.VALUE_SEARCHFIELDS).contains("_$")) {
            try {
                params = "ids";
                vars = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(contextl + mpv5.ui.beans.LightMPComboBox.VALUE_SEARCHFIELDS);
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
                    Log.Debug(MPComboBoxModelItem.class, "Length of var string: " + vaars.length);
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
                    }
                }

                setDescription(VariablesHandler.parse(x, product));
            } catch (NodataFoundException nodataFoundException) {
                setDescription(VariablesHandler.parse(product.__getCName(), product));
            }
        } else {
            Log.Debug(SubItem.class, "No format defined..");
            setDescription(VariablesHandler.parse(product.__getCName(), product));
        }
///////////////end format////////////////////////////////////////////////////////
        setExternalvalue(product.__getExternalnetvalue());
        setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
        setInternalvalue(product.__getInternalnetvalue());
        setMeasure(product.__getMeasure());
        setOriginalproductsids(product.__getIDS());
        setQuantityvalue(new BigDecimal("1"));
        setTaxpercentvalue(Tax.getTaxValue(product.__getTaxids()));
        setLinkurl(product.__getUrl());
        calculate(this);
    }

    /**
     * Generates a SubItem with useless sample data
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
        BigDecimal deftax = new BigDecimal("0");
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid);
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(new BigDecimal(defcount.toString()));
        }
        return i;
    }

    /**
     * Generates a String array out of this SubItem
     * @return
     */
    public String[] toStringArray() {
        calculate(this);

        String[] possibleCols = new String[]{
            ////////////////// The exported columns///////////////////////////////////////
            String.valueOf(FormatNumber.formatInteger(this.__getCountvalue())),
            String.valueOf(FormatNumber.formatDezimal(this.__getQuantityvalue())),
            __getMeasure(),
            __getDescription(),
            String.valueOf(FormatNumber.formatLokalCurrency(this.__getExternalvalue())),
            String.valueOf(FormatNumber.formatLokalCurrency(this.__getTotalnetvalue())),
            String.valueOf(FormatNumber.formatPercent(this.__getTaxpercentvalue())),
            String.valueOf(FormatNumber.formatLokalCurrency(this.getTotalTaxValue())),
            String.valueOf(FormatNumber.formatLokalCurrency(this.__getTotalbrutvalue())),
            __getLinkurl(),
            __getCName()
        ///////////////////////////////////////////////////////////////////////////////
        };

        return possibleCols;
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public void setCName(String name) {
        this.cname = name;
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

    @Override
    public JComponent getView() {
        try {
            Item dos = (Item) DatabaseObject.getObject(Context.getItem(), __getItemsids());
            ItemPanel ip = new ItemPanel(Context.getItem(), dos.__getInttype());
            return ip;
        } catch (Exception ex) {
            Log.Debug(ex);
            return null;
        }
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    /**
     * Generates a table model out of the given SubItems
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
     * Generates a table model out of the given SubItems
     * @param items
     * @return
     */
    public static MPTableModel toModel(SubItem[] items, boolean removeSubitemIds) {
        //"Internal ID", "ID", "Count", "Measure", "Description", "Netto Price", "Tax Value", "Total Price"
        Object[][] data = new Object[items.length][];
        for (int i = 0; i < data.length; i++) {
            data[i] = items[i].getRowData(i + 1);
            if (removeSubitemIds) {
                data[i][0] = -1;
            }
        }

        MPTableModel model = new MPTableModel(
                new Class[]{Integer.class, Integer.class, BigDecimal.class, String.class, Object.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, Integer.class, JButton.class, JButton.class, String.class, String.class},
                new boolean[]{false, false, true, true, true, true, true, false, false, false, false, true, true, true, true, true},
                data,
                Headers.SUBITEMS.getValue());

        model.setContext(Context.getSubItem());
        String defunit = null;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
        }
        BigDecimal deftax = new BigDecimal("0");
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
        }
        model.defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0, 0.0, 0.0, 0, "A", "C", "", ""});
        model.setAutoCountColumn(1);

        return model;
    }

    /**
     * Turn this SubItem into table row data
     * @param row
     * @return
     */
    public synchronized Object[] getRowData(int row) {
        Object[] data = new Object[15];
        for (int i = 0; i < data.length; i++) {
            data[0] = __getIDS();
            data[1] = Integer.valueOf(row);
            data[2] = __getQuantityvalue();
            data[3] = __getMeasure();
            data[4] = __getDescription();
            data[5] = __getExternalvalue();
            data[6] = __getTaxpercentvalue();
            data[7] = __getQuantityvalue().multiply(__getExternalvalue().multiply(((__getTaxpercentvalue().divide(new BigDecimal("100")).add(BigDecimal.ONE)))));
            data[8] = 0.0;
            data[9] = 0.0;
            data[10] = Integer.valueOf(__getOriginalproductsids());
            data[11] = "A";
            data[12] = "C";
            data[12 + 1] = __getLinkurl();
            data[14] = __getCName();
        }
        return data;
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
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {

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

    private static void calculate(SubItem s) {
        s.setTotalbrutvalue(s.quantityvalue.multiply(s.externalvalue.multiply(((s.taxpercentvalue.divide(new BigDecimal("100"))).add(BigDecimal.ONE)))));
        s.defTotaltaxvalue(s.quantityvalue.multiply(s.externalvalue.multiply((s.taxpercentvalue.divide(new BigDecimal("100"))))));
        s.setTotalnetvalue(s.quantityvalue.multiply(s.externalvalue));
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
        if (cname.length() == 0) {
            cname = "   ";
        }
        return super.save(b);
    }

    @Override
    public void triggerOnCreate() {
        if (__getOriginalproductsids() > 0) {
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), originalproductsids);
                if (p.__getIntinventorytype() == 1) {
                    p.setStockvalue(p.__getStockvalue().subtract(BigDecimal.ONE));
                    p.save(true);
                }

                if (p.__getIntinventorytype() == 1 && p.__getStockvalue().intValue() <= p.__getThresholdvalue().intValue()) {
                    Notificator.raiseNotification(Messages.INVENTORY_STOCK_TRESHOLD + p.toString());
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }

    @Override
    public void triggerOnUpdate() {
    }

    @Override
    public void triggerOnDelete() {
    }
}
