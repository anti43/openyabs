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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.ui.panels.ProductListsPanel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class ProductlistSubItem extends DatabaseObject {

    private static final long serialVersionUID = 1L;

    /**
     * Save the model of SubItems
     * @param dataOwner
     * @param model
     */
    public static void saveModel(MPTableModel model, int listid) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        Log.Debug(ProductlistSubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            ProductlistSubItem it = new ProductlistSubItem();
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(ProductlistSubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setProductlistsids(listid);
            it.setCountvalue(Double.valueOf(row[1].toString()));
            it.setDescription(row[4].toString());
            it.setExternalvalue(Double.valueOf(row[5].toString()));
            it.setInternalvalue(Double.valueOf(row[5].toString()));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(Double.valueOf(row[2].toString()));
            it.setTaxpercentvalue(Double.valueOf(row[6].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }
            it.save(true);
        }
    }

    /**
     * Convert the model to a list of String arrays
     * @param dataOwner
     * @param model
     * @param t
     * @return
     */
    public static Vector<String[]> convertModel(MPTableModel model, Template t, int listid) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        Vector<String[]> rowsk = new Vector<String[]>();
        final List<ProductlistSubItem> its = new Vector<ProductlistSubItem>();
        Log.Debug(ProductlistSubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);

            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }

            final ProductlistSubItem it = new ProductlistSubItem();
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(ProductlistSubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setProductlistsids(listid);
            it.setCountvalue(Double.valueOf(row[1].toString()));
            it.setDescription(row[4].toString());
            it.setExternalvalue(Double.valueOf(row[5].toString()));
            it.setInternalvalue(Double.valueOf(row[5].toString()));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(Double.valueOf(row[2].toString()));
            it.setTaxpercentvalue(Double.valueOf(row[6].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }

            rowsk.add(it.toStringArray(t));
        }

        return rowsk;
    }

    /**
     * Add some value
     * @param table 
     * @param percentValue
     * @param panel dirty dirty
     */
    public static void changeValueFields(JTable table, Integer percentValue, ProductListsPanel panel) {
        List<Object[]> rowsl = ((MPTableModel) table.getModel()).getValidRows(new int[]{4});
        Log.Debug(ProductlistSubItem.class, "Rows found: " + rowsl.size());
        ProductlistSubItem[] items = new ProductlistSubItem[rowsl.size()];
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            ProductlistSubItem it = new ProductlistSubItem();
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(ProductlistSubItem.class, e.getMessage());
            }
            it.setCname(row[14].toString());
            it.setCountvalue(Double.valueOf(row[1].toString()));
            it.setDescription(row[4].toString());
            it.setExternalvalue(Double.valueOf(row[5].toString()) * (((Double.valueOf(percentValue) / 100) + 1)));
            it.setInternalvalue(Double.valueOf(row[5].toString()) * (((Double.valueOf(percentValue) / 100) + 1)));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(Double.valueOf(row[2].toString()));
            it.setTaxpercentvalue(Double.valueOf(row[6].toString()));
            it.setLinkurl((row[12 + 1].toString()));
            calculate(it);
            items[i] = it;

        }

        table.setModel(toModel(items));

        if (panel != null) {
            panel.formatTable();
        }
    }

    /**
     * Convert a Product into a Row
     * @param product
     * @return
     */
    public static ProductlistSubItem toRow(Product product) {
        return new ProductlistSubItem(product);
    }
    private int productlistsids;
    private int originalproductsids;
    private double countvalue;
    private double quantityvalue;
    private String measure = "";
    private String description = "";
    private double internalvalue;
    private double externalvalue;
    private double taxpercentvalue;
    private double totalnetvalue;
    private double totalbrutvalue;
    private double totaltaxvalue;
    private String linkurl = "";

    public ProductlistSubItem() {
        setContext(Context.getProductListItems());
    }

    /**
     *
     * @return
     */
    public static ProductlistSubItem getDefaultItem() {
        ProductlistSubItem i = new ProductlistSubItem();
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            String defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
            i.setMeasure(defunit);
        }
        Double deftax = 0d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid).doubleValue();
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(defcount);
        }
        return i;
    }

    /**
     *
     * @param o
     */
    public ProductlistSubItem(Product o) {
        this();
        setCname(o.__getCname());
        setDateadded(new Date());
        setDescription(o.__getDescription());
        setExternalvalue(o.__getExternalnetvalue().doubleValue());
        setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
        setInternalvalue(o.__getInternalnetvalue().doubleValue());
        setMeasure(o.__getMeasure());
        setOriginalproductsids(o.__getIDS());
        setQuantityvalue(1);
        setTaxpercentvalue(Tax.getTaxValue(o.__getTaxids()).doubleValue());
        setLinkurl(o.__getUrl());
        calculate(this);
    }

    /**
     * Generates a SubItem with useless sample data
     * @return
     */
    public static ProductlistSubItem getRandomItem() {
        ProductlistSubItem i = new ProductlistSubItem();
        i.fillSampleData();
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            String defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
            i.setMeasure(defunit);
        }
        Double deftax = 0d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid).doubleValue();
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
            i.setQuantityvalue(defcount);
        }
        return i;
    }

    /**
     * Generates a String array out of this SubItem
     * @param template
     * @return
     */
    public String[] toStringArray(Template template) {
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
            __getCname()
        ///////////////////////////////////////////////////////////////////////////////
        };

        String format = template.__getFormat();
        int[] intcols;
        try {
            String[] cols = format.split(",");
            intcols = new int[cols.length];
            for (int i = 0; i < intcols.length; i++) {
                String string = cols[i];
                intcols[i] = Integer.valueOf(string).intValue();
            }
        } catch (Exception ex) {
            Log.Debug(this, "An error occured, using default format now. " + ex.getMessage());
            intcols = new int[possibleCols.length];
            for (int i = 0; i < intcols.length; i++) {
                intcols[i] = i + 1;
            }
        }
        String[] form = new String[intcols.length];
        for (int i = 0; i < intcols.length; i++) {
            form[i] = possibleCols[intcols[i] - 1];
        }

        return form;
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
    public double __getCountvalue() {
        return countvalue;
    }

    /**
     * @param count the count to set
     */
    public void setCountvalue(double count) {
        this.countvalue = count;
    }

    /**
     * @return the quantity
     */
    public double __getQuantityvalue() {
        return quantityvalue;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantityvalue(double quantity) {
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
    public double __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    /**
     * @param taxpercent the taxpercent to set
     */
    public void setTaxpercentvalue(double taxpercent) {
        this.taxpercentvalue = taxpercent;
    }

    @Override
    public JComponent getView() {
        return null;
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
    public static MPTableModel toModel(ProductlistSubItem[] items) {
        //"Internal ID", "ID", "Count", "Measure", "Description", "Netto Price", "Tax Value", "Total Price"
        Object[][] data = new Object[items.length][11];
        for (int i = 0; i < data.length; i++) {
            data[i] = items[i].getRowData(i + 1);
        }
        MPTableModel model = new MPTableModel(
                new Class[]{Integer.class, Integer.class, Double.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Integer.class, Object.class, Object.class, Object.class, String.class, String.class, String.class},
                new boolean[]{false, false, true, true, true, true, true, false, false, false, false, true, true, false, false, true, true},
                data,
                Headers.SUBITEMS.getValue());
        model.setContext(Context.getSubItem());
        String defunit = null;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defunit")) {
            defunit = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defunit");
        }
        Double deftax = 0d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", 0);
            deftax = Tax.getTaxValue(taxid).doubleValue();
        }
        Double defcount = 1d;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("defcount")) {
            defcount = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("defcount", 0d);
        }
        model.defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0, 0.0, 0.0, 0, "A", "C", "x", "y"});
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
            data[7] = __getQuantityvalue() * __getExternalvalue() * ((__getTaxpercentvalue() / 100) + 1);
            data[8] = 0.0;
            data[9] = 0.0;
            data[10] = Integer.valueOf(__getOriginalproductsids());
            data[11] = "A";
            data[12] = "C";
            data[12 + 1] = __getLinkurl();
            data[14] = __getCname();

        }
        return data;
    }

    /**
     * @return the internalvalue
     */
    public double __getInternalvalue() {
        return internalvalue;
    }

    /**
     * @param internalvalue the internalvalue to set
     */
    public void setInternalvalue(double internalvalue) {
        this.internalvalue = internalvalue;
    }

    /**
     * @return the externalvalue
     */
    public double __getExternalvalue() {
        return externalvalue;
    }

    /**
     * @param externalvalue the externalvalue to set
     */
    public void setExternalvalue(double externalvalue) {
        this.externalvalue = externalvalue;
    }

    @Override
    public java.util.Map<String, Object> resolveReferences(java.util.Map<String, Object> map) {
        super.resolveReferences(map);

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

        return map;
    }

    /**
     * @return the totalnetvalue
     */
    public double __getTotalnetvalue() {
        return totalnetvalue;
    }

    /**
     * @param totalnetvalue the totalnetvalue to set
     */
    public void setTotalnetvalue(double totalnetvalue) {
        this.totalnetvalue = totalnetvalue;
    }

    /**
     * @return the totalbrutvalue
     */
    public double __getTotalbrutvalue() {
        return totalbrutvalue;
    }

    /**
     * @param totalbrutvalue the totalbrutvalue to set
     */
    public void setTotalbrutvalue(double totalbrutvalue) {
        this.totalbrutvalue = totalbrutvalue;
    }

    private static void calculate(ProductlistSubItem s) {
        s.setTotalbrutvalue(s.quantityvalue * s.externalvalue * ((s.taxpercentvalue / 100) + 1));
        s.defTotaltaxvalue(s.quantityvalue * s.externalvalue * (s.taxpercentvalue / 100));
        s.setTotalnetvalue(s.quantityvalue * s.externalvalue);
    }

    @Override
    public boolean save() {
        calculate(this);
        return super.save();
    }

    @Override
    public boolean save(boolean b) {
        if (getCname().length() == 0) {
            setCname("   ");
        }
        return super.save(b);
    }

    private void defTotaltaxvalue(double value) {
        totaltaxvalue = value;
    }

    /**
     * 
     * @return
     */
    public double getTotalTaxValue() {
        return totaltaxvalue;
    }

    /**
     * Get the items of this list
     * @param listid
     * @param listname
     * @return
     * @throws NodataFoundException
     */
    public static List<ProductlistSubItem> getList(int listid) throws NodataFoundException {
        QueryCriteria c = new QueryCriteria("productlistsids", listid);
        ArrayList<ProductlistSubItem> data = getObjects(new ProductlistSubItem(), c);
        return data;
    }

    /**
     * @return the productlistsids
     */
    public int __getProductlistsids() {
        return productlistsids;
    }

    /**
     * @param productlistsids the productlistsids to set
     */
    public void setProductlistsids(int productlistsids) {
        this.productlistsids = productlistsids;
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
}
