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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.text.RandomText;

/**
 *
 *  
 */
public final class ActivityListSubItem extends DatabaseObject {

    private static final long serialVersionUID = 1L;

    /**
     * Save the model of SubItems
     * @param dataOwner
     * @param model
     */
    public static void saveModel(MPTableModel model, int listid) {
        //"Internal ID", "ID", "Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"     
        List<Object[]> rowsl = model.getValidRows(new int[]{5});
        Log.Debug(ActivityListSubItem.class, "Rows found: " + rowsl.size());
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    row[j] = "";
                }
            }
            ActivityListSubItem it = null;
            if (row[10] instanceof ActivityListSubItem) {
                it = (ActivityListSubItem) row[10];
            } else {
                it = new ActivityListSubItem();
            }
            try {
                if (row[0] != null && Integer.valueOf(row[0].toString()).intValue() > 0) {
                    it.setIDS(Integer.valueOf(row[0].toString()).intValue());
                } else {
                    it.setIDS(-1);
                }
            } catch (Exception e) {
                Log.Debug(ActivityListSubItem.class, e.getMessage());
            }

            it.setActivitylistsids(listid);
            it.setDatedoing((Date) row[2]);
            it.setQuantityvalue(new BigDecimal(row[3].toString()));
            it.setMeasure(row[4].toString());
            it.setDescription(row[5].toString());
            it.setInternalvalue(new BigDecimal(row[6].toString()));
            it.setTaxpercentvalue(new BigDecimal(row[7].toString()));
            it.setTotalbrutvalue(new BigDecimal(row[8].toString()));
            it.setProductsids(Integer.valueOf(row[9].toString()));
            it.setCName(new RandomText(20).getString());
            calculate(it);


            if (!it.isExisting()) {
                it.setDateadded(new Date());
                it.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }
            it.save(true);
        }

        for (int i = 0; i < deletionQueue.size(); i++) {
            try {
                QueryHandler.delete(SubItem.getObject(Context.getSubItem(), deletionQueue.get(i)));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }

        deletionQueue.clear();
    }

    /**
     * Convert a Product into a Row
     * @param product
     * @return
     */
    public static ActivityListSubItem toRow(Product product) {
        return new ActivityListSubItem(product);
    }
    private static List<Integer> deletionQueue = new ArrayList<Integer>();

    /**
     * Mark a subitem for deletion
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
    private int activitylistsids;
    private int id;
    private Date datedoing;
    private BigDecimal countvalue = new BigDecimal("0");
    private BigDecimal quantityvalue = new BigDecimal("0");
    private String measure = "";
    private String description = "";
    private int productsids;
    private BigDecimal internalvalue = new BigDecimal("0");
    private BigDecimal taxpercentvalue = new BigDecimal("0");
    private BigDecimal totalbrutvalue = new BigDecimal("0");

    public ActivityListSubItem() {
        context = Context.getActivityListItems();
    }

    /**
     *
     * @return
     */
    public static ActivityListSubItem getDefaultItem() {
        ActivityListSubItem as = new ActivityListSubItem();
        String defunit = "";
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
        as.setMeasure(defunit);
        as.setQuantityvalue(new BigDecimal(defcount));
        as.setTaxpercentvalue(BigDecimal.valueOf(deftax));
        return as;
    }

    /**
     *
     * @param o
     */
    public ActivityListSubItem(Product o) {
        this();
        setCName(o.__getCName());
        setDateadded(new Date());
        setDatedoing(new Date());
        setMeasure(o.__getMeasure());
        setDescription(o.__getDescription());
        setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
        setIntaddedby(mpv5.db.objects.User.getCurrentUser().__getIDS());
        setProductsids(o.__getIDS());
        setInternalvalue(o.__getExternalnetvalue());
        setTaxpercentvalue(Tax.getTaxValue(o.__getTaxids()));
        calculate(this);
    }

    public Date __getDatedoing() {
        return datedoing;
    }

    public void setDatedoing(Date datedoing) {
        this.datedoing = datedoing;
    }

    /**
     * @return the originalproductsids
     */
    public int __getProductsids() {
        return productsids;
    }

    /**
     * @param originalproductsids the originalproductsids to set
     */
    public void setProductsids(int productsids) {
        this.productsids = productsids;
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

    public BigDecimal __getInternalvalue() {
        return internalvalue;
    }

    public void setInternalvalue(BigDecimal internalvalue) {
        this.internalvalue = internalvalue;
    }

    public BigDecimal __getTaxpercentvalue() {
        return taxpercentvalue;
    }

    public void setTaxpercentvalue(BigDecimal taxpercentvalue) {
        this.taxpercentvalue = taxpercentvalue;
    }

    public BigDecimal __getTotalbrutvalue() {
        return totalbrutvalue;
    }

    public void setTotalbrutvalue(BigDecimal totalbrutvalue) {
        this.totalbrutvalue = totalbrutvalue;
    }

    /**
     * @return the productlistsids
     */
    public int __getActivitylistsids() {
        return activitylistsids;
    }

    /**
     * @param productlistsids the productlistsids to set
     */
    public void setActivitylistsids(int activitylistsids) {
        this.activitylistsids = activitylistsids;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String __getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
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
    public static MPTableModel toModel(ActivityListSubItem[] items) {
        Object[][] data = new Object[items.length][11];
        for (int i = 0; i < data.length; i++) {
            data[i] = items[i].getRowData(i + 1);
        }
        MPTableModel model = new MPTableModel(
                new Class[]{
                    Integer.class, // "Internal ID",
                    Integer.class, // "ID",
                    Date.class, // "Date",
                    Double.class, // "Count",
                    String.class, // measure
                    String.class, // "Description",
                    BigDecimal.class, // "Netto Price",
                    BigDecimal.class, // "Tax Value",
                    BigDecimal.class, // "Total Price",
                    Integer.class, // "Product",
                    String.class, // "cname" 
                    ActivityListSubItem.class // "Object" 
                },
                new boolean[]{
                    false, // "Internal ID",
                    false, // "ID",
                    true, // "Date",
                    true, // "Count",
                    true, // measure
                    true, // "Description",
                    true, // "Netto Price",
                    true, // "Tax Value",
                    true, // "Total Price",
                    false, // "Product",
                    false, // "cname" 
                    false}, // Object
                data,
                Headers.ACTIVITY.getValue());
        model.setContext(Context.getActivityListItems());
        String defunit = "";
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
        model.defineRow(new Object[]{0, 0, null, defcount, defunit, null, 0.0, deftax, 0.0, null, null});
        model.setAutoCountColumn(1);

        return model;
    }

    /**
     * Turn this SubItem into table row data
     * @param row
     * @return
     */
    public synchronized Object[] getRowData(int row) {
        //"Internal ID", "ID", "Date", "Count", "Measure", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"
        Object[] data = new Object[12];
        data[0] = __getIDS();
        data[1] = Integer.valueOf(row);
        data[2] = __getDatedoing();
        data[3] = __getQuantityvalue();
        data[4] = __getMeasure();
        data[5] = __getDescription();
        data[6] = __getInternalvalue();
        data[7] = __getTaxpercentvalue();
        data[8] = __getTotalbrutvalue();
        data[9] = Integer.valueOf(__getProductsids());
        data[10] = __getCName();
        if (!__getDescription().equals("")) {
            data[11] = this;
        }

        return data;
    }

    @Override
    public HashMap<String, Object> resolveReferences(HashMap<String, Object> map) {
        if (map.containsKey("originalproductsids")) {
            try {
                try {
                    map.put("product", DatabaseObject.getObject(Context.getProduct(), Integer.valueOf(map.get("productsids").toString())));
                    map.remove("productsids");
                } catch (NodataFoundException ex) {
                    map.put("product", null);
                }
            } catch (NumberFormatException numberFormatException) {
                //already resolved?
            }
        }

        return super.resolveReferences(map);
    }

    private static void calculate(ActivityListSubItem s) {
        s.setTotalbrutvalue(s.quantityvalue.multiply(s.__getInternalvalue()).multiply(s.taxpercentvalue.divide(new BigDecimal(100)).add(new BigDecimal(1))));
    }

    @Override
    public boolean save() {
        calculate(this);
        return super.save();
    }

    @Override
    public boolean save(boolean b) {
        if (cname.length() == 0) {
            cname = "   ";
        }
        calculate(this);
        return super.save(b);
    }

    /**
     * Get the items of this list
     * @param listid
     * @param listname
     * @return
     * @throws NodataFoundException
     */
    public static List<ActivityListSubItem> getList(int listid) throws NodataFoundException {
        QueryCriteria c = new QueryCriteria("activitylistsids", listid);
        ArrayList<ActivityListSubItem> data = getObjects(new ActivityListSubItem(), c);
        return data;
    }

    public static BigDecimal getModelSum(MPTableModel model, int listid) {
        List<Object[]> rowsl = model.getValidRows(new int[]{5});
        Log.Debug(ActivityListSubItem.class, "Rows found: " + rowsl.size());
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            sum.add(FormatNumber.parseDezimal(row[8].toString()));
        }
        return sum;
    }

    /**
     * Save the model of SubItems
     * @param dataOwner
     * @param model
     */
    public void updateRowFromModel(MPTableModel model, int row) {
        //"Internal ID", "ID", "Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"
        Log.Debug(ActivityListSubItem.class, "Row Searching: " + row);
        this.setDatedoing((Date) model.getValueAt(row, 2));
        this.setQuantityvalue(new BigDecimal(model.getValueAt(row, 3).toString()));
        this.setMeasure(model.getValueAt(row, 4).toString());
        this.setDescription(model.getValueAt(row, 5).toString());
        this.setInternalvalue(new BigDecimal(model.getValueAt(row, 6).toString()));
        this.setTaxpercentvalue(new BigDecimal(model.getValueAt(row, 7).toString()));
        this.setTotalbrutvalue(new BigDecimal(model.getValueAt(row, 8).toString()));
        this.setProductsids(Integer.valueOf(model.getValueAt(row, 9).toString()));
        this.setCName(model.getValueAt(row, 10).toString());
    }

    /**
     * Generates a String array out of this ActivityListSubItem
     * @return
     */
    public synchronized String[] toStringArray(Template template) {
        calculate(this);
        String[] possibleCols = new String[]{
            ////////////////// The exported columns///////////////////////////////////////
            String.valueOf(FormatNumber.formatInteger(1)),
            String.valueOf(FormatNumber.formatDezimal(this.__getQuantityvalue())),
            __getMeasure(),
            __getDescription(),
            String.valueOf(FormatNumber.formatLokalCurrency(this.__getInternalvalue())),
            String.valueOf(FormatNumber.formatLokalCurrency(BigDecimal.ZERO)),
            String.valueOf(FormatNumber.formatPercent(this.__getTaxpercentvalue())),
            String.valueOf(FormatNumber.formatLokalCurrency(BigDecimal.ZERO)),
            String.valueOf(FormatNumber.formatLokalCurrency(this.__getTotalbrutvalue())),
            "",
            DateConverter.getDefDateString(__getDatedoing())
        ///////////////////////////////////////////////////////////////////////////////
        };
        List<String> all = new LinkedList<String>();
        List<String> l = Arrays.asList(possibleCols);
        all.addAll(l);

        if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.productsresolved", false) && __getProductsids() > 0) {
            try {
                Product p = (Product) DatabaseObject.getObject(Context.getProduct(), __getProductsids());
                List<String[]> vals = p.getValues3();
                Collections.sort(vals, new Comparator<String[]>() {

                    public int compare(String[] o1, String[] o2) {
                        return o1[0].compareTo(o2[0]);
                    }
                });
                for (int i = 0; i < vals.size(); i++) {
                    String[] v = vals.get(i);
                    all.add(v[1]);
                    if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                        Log.Debug(this, (12 + i) + ": " + v[0] + " ");
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        }

        return all.toArray(new String[0]);
    }
    /**
     * Compares by date
     */
    public static Comparator<ActivityListSubItem> ORDER_COMPARATOR = new Comparator<ActivityListSubItem>() {

        public int compare(ActivityListSubItem o1, ActivityListSubItem o2) {
            return (o1.__getDatedoing().after(o2.__getDatedoing()) ? 1 : -1);
        }
    };
}
