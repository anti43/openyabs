/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.models.MPTableModel;

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
            it.setExternalvalue(Double.valueOf(row[5].toString()));//Discount not supported yet
            it.setInternalvalue(Double.valueOf(row[5].toString()));//Discount not supported yet
            it.setMeasure(row[3].toString());
            it.setOriginalproductsids(0);//not yet implemented
            it.setQuantityvalue(Double.valueOf(row[2].toString()));
            it.setTaxpercentvalue(Double.valueOf(row[6].toString()));

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
        throw new UnsupportedOperationException("Not supported yet.");
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
        Object[][] data = new Object[items.length][8];
        for (int i = 0; i < data.length; i++) {
            data[i][0] = items[i].__getIDS();
            data[i][1] = Integer.valueOf(i + 1);
            data[i][2] = items[i].__getCountvalue();
            data[i][3] = items[i].__getMeasure();
            data[i][4] = items[i].__getDescription();
            data[i][5] = items[i].__getExternalvalue();
            data[i][6] = items[i].__getTaxpercentvalue();
            data[i][7] = items[i].__getCountvalue() * items[i].__getExternalvalue() * ((items[i].__getTaxpercentvalue() / 100) + 1);
        }
        MPTableModel model = new MPTableModel(
                new Class[]{Integer.class, Integer.class, Double.class, String.class, String.class, Double.class, Double.class, Double.class, Double.class},
                new boolean[]{false, false, true, true, true, true, true, false, false},
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
        model.defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0, 0.0});
        model.setAutoCountColumn(1);
        return model;
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
}
