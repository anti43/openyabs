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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.table.TableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.ItemPanel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 *  
 */
public class SubItem extends DatabaseObject {

    /**
     * Save the model of SubItems
     * @param dataOwner
     * @param model
     */
    public static void saveModel(Item dataOwner, MPTableModel model) {
        List<Object[]> rowsl = model.getValidRows(new int[]{4});
        Log.Debug(SubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
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
            it.setCName(row[4].toString());
            it.setItemsids(dataOwner.__getIDS());
            it.setCountvalue(Double.valueOf(row[2].toString()));
            it.setDatedelivery(dataOwner.__getDatetodo());
            it.setDescription(row[4].toString());
            it.setExternalvalue(Double.valueOf(row[5].toString()));
            it.setInternalvalue(Double.valueOf(row[5].toString()));//not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(Integer.valueOf(row[10].toString()));
            it.setQuantityvalue(Double.valueOf(row[2].toString()));
            it.setTaxpercentvalue(Double.valueOf(row[6].toString()));
            calculate(it);

            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(dataOwner.__getGroupsids());
            }
            it.save(true);
        }
    }
    private int itemsids;
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
    private Date datedelivery;

    public SubItem() {
        context.setDbIdentity(Context.IDENTITY_SUBITEMS);
        context.setIdentityClass(SubItem.class);
    }

    /**
     *
     * @return
     */
    public static SubItem getDefaultItem() {
        SubItem i = new SubItem();
        if (MPView.getUser().getProperties().hasProperty("defunit")) {
            String defunit = MPView.getUser().getProperties().getProperty("defunit");
            i.setMeasure(defunit);
        }
        Double deftax = 0d;
        if (MPView.getUser().getProperties().hasProperty("deftax")) {
            int taxid = MPView.getUser().getProperties().getProperty("deftax", 0);
            deftax = Item.getTaxValue(taxid);
            i.setTaxpercentvalue(deftax);
        }
        Double defcount = 1d;
        if (MPView.getUser().getProperties().hasProperty("defcount")) {
            defcount = MPView.getUser().getProperties().getProperty("defcount", 0d);
            i.setCountvalue(defcount);
        }
        return i;
    }

    /**
     *
     * @param o
     */
    public SubItem(Product o) {
        this();
        setCName(o.__getCName());
        setDateadded(new Date());
        setDatedelivery(new Date());
        setDescription(o.__getDescription());
        setExternalvalue(o.__getExternalnetvalue());
        setGroupsids(MPView.getUser().__getGroupsids());
        setIntaddedby(MPView.getUser().__getIDS());
        setInternalvalue(o.__getInternalnetvalue());
        setMeasure(o.__getMeasure());
        setOriginalproductsids(o.__getIDS());
        setQuantityvalue(1);
        setTaxpercentvalue(Item.getTaxValue(o.__getTaxids()));
        calculate(this);
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
            Item dos = (Item) DatabaseObject.getObject(Context.getItems(), __getItemsids());
            ItemPanel ip = new ItemPanel(Context.getItems(), dos.__getInttype());
            ip.setDataOwner(dos, true);
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
        //"Internal ID", "ID", "Count", "Measure", "Description", "Netto Price", "Tax Value", "Total Price"
        Object[][] data = new Object[items.length][11];
        for (int i = 0; i < data.length; i++) {
            data[i] = items[i].getRowData(i);
        }
        MPTableModel model = new MPTableModel(
                new Class[]{Integer.class, Integer.class, Double.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class, Double.class, Integer.class},
                new boolean[]{false, false, true, true, true, true, true, false, false, false, false},
                data,
                Headers.SUBITEMS.getValue());
        model.setContext(Context.getSubItem());
        String defunit = null;
        if (MPView.getUser().getProperties().hasProperty("defunit")) {
            defunit = MPView.getUser().getProperties().getProperty("defunit");
        }
        Double deftax = 0d;
        if (MPView.getUser().getProperties().hasProperty("deftax")) {
            int taxid = MPView.getUser().getProperties().getProperty("deftax", 0);
            deftax = Item.getTaxValue(taxid);
        }
        Double defcount = 1d;
        if (MPView.getUser().getProperties().hasProperty("defcount")) {
            defcount = MPView.getUser().getProperties().getProperty("defcount", 0d);
        }
        model.defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0, 0.0, 0.0, 0});
        model.setAutoCountColumn(1);

        return model;
    }

    /**
     * Turn this SubItem into table row data
     * @param row
     * @return
     */
    public synchronized Object[] getRowData(int row) {
        Object[] data = new Object[11];
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
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {
        super.resolveReferences(map);

        if (map.containsKey("originalproductsids")) {
            try {
                try {
                    map.put("originalproduct", DatabaseObject.getObject(Context.getProducts(), Integer.valueOf(map.get("originalproductsids").toString())));
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

    private static void calculate(SubItem s) {
        s.setTotalbrutvalue(s.quantityvalue * s.externalvalue * s.taxpercentvalue / 100);
        s.setTotalnetvalue(s.quantityvalue * s.externalvalue);
    }

    @Override
    public boolean save() {
        calculate(this);
        return super.save();
    }
}
