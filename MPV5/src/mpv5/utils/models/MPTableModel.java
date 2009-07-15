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
package mpv5.utils.models;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.TableView.TableRow;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Item;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.TableCalculator;

/**
 *
 *  A custom table model which implements various convenience methods
 */
public class MPTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;
    private Class[] types;
    private boolean[] canEdits;
    private Context context;
    private Object[] predefinedRow;
    private Integer autoCountColumn;
    private TableCalculator calculator;

    /**
     * Creates an empty, uneditable model 
     */
    public MPTableModel() {
        super();
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});
    }

    /**
     * Creates an uneditable model out of the given data
     * @param data
     */
    public MPTableModel(Object[][] data) {
        super();

        if (data.length > 0) {
            String[] header = new String[data[0].length];
            for (int i = 0; i < header.length; i++) {
                header[i] = String.valueOf(i);
            }
            setDataVector(data, header);
        }

        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});
    }

//    /**
//     * Creates an uneditable model out of the given data
//     * @param list
//     * @param header
//     */
//    public MPTableModel(ArrayList<DatabaseObject> list, String[] header) {
//        super();
//        nativeMode = true;
//        setDataVector(MPTableModelRow.toRows(list), header);
//
//    }
    /**
     * Creates an uneditable model out of the given data
     * @param datstr
     * @param header
     */
    public MPTableModel(Object[][] datstr, String[] header) {
        super(datstr, header);
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});

    }

    /**
     * Creates an uneditable model out of the given data
     * @param data
     * @param header
     */
    public MPTableModel(Object[][] data, Headers header) {
        super(data, header.getValue());
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});

    }

    /**
     * Creates an uneditable model out of the given data
     * @param datstr
     * @param header
     * @param types
     */
    public MPTableModel(Object[][] datstr, String[] header, Class[] types) {
        super(datstr, header);
        setEditable(false);
        setTypes(types);
    }

    /**
     * Creates an uneditable model out of the given data
     * @param types
     * @param canEdits
     * @param data
     * @param columnNames
     */
    public MPTableModel(Class[] types, boolean[] canEdits, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        setTypes(types);
        setCanEdits(canEdits);
    }

    /**
     * Creates a model out of the given data
     * @param types
     * @param canEdits
     * @param columnNames
     */
    public MPTableModel(Class[] types, boolean[] canEdits, Object[] columnNames) {
        super(columnNames, 1);
        setTypes(types);
        setCanEdits(canEdits);
    }

    /**
     * Creates an uneditable model out of the given data
     * @param types
     * @param data
     * @param columnNames
     */
    public MPTableModel(Class[] types, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        setTypes(types);
        setEditable(false);
    }
//
//    /**
//     * Creates an editable model out of the given data and switches this model to native mode.<br/>
//     * In native mode, all non NULL rows of the model are available as {@link MPTableModelRow}
//     * @param nativerowdata
//     * @param columnNames
//     */
//    public MPTableModel(MPTableModelRow[] nativerowdata, Object[] columnNames) {
//        this();
//    }

    /**
     * Creates a special table model for the given context and switches this model to native mode.<br/>
     * In native mode, all non NULL rows of the model are available as {@link MPTableModelRow}
     * @param context
     * @param table (optional) If not null, custom renderers are registered for some column class values
     */
    public MPTableModel(Context context, JTable table) {
        super();
        this.context = context;

        if (context.equals(Context.getSubItem())) {
            String defunit = null;
            if (MPV5View.getUser().getProperties().hasProperty("defunit")) {
                defunit = MPV5View.getUser().getProperties().getProperty("defunit");
            }
            Double deftax = 0d;
            if (MPV5View.getUser().getProperties().hasProperty("deftax")) {
                int taxid = MPV5View.getUser().getProperties().getProperty("deftax", 0);
                deftax = Item.getTaxValue(taxid);
            }
            Double defcount = 1d;
            if (MPV5View.getUser().getProperties().hasProperty("defcount")) {
                defcount = MPV5View.getUser().getProperties().getProperty("defcount", 0d);
            }

            setDataVector(new Object[][]{
                        {0, 1, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 2, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 3, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 4, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 5, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 6, defcount, defunit, null, 0.0, deftax, 0.0},
                        {0, 7, defcount, defunit, null, 0.0, deftax, 0.0}}, Headers.SUBITEMS);
            setCanEdits(new boolean[]{false, false, true, true, true, true, true, false});
            setTypes(new Class[]{Integer.class, Integer.class, Double.class, String.class, String.class, Double.class, Double.class, Double.class});
            defineRow(new Object[]{0, 0, defcount, defunit, null, 0.0, deftax, 0.0});
            autoCountColumn = 1;

            if (table != null) {
                table.setDefaultRenderer(Double.class, new DoubleRenderer());
            }
        }
    }

    /**
     * Set the cell calculator for this model
     * @param cv
     */
    public void setCalculator(TableCalculator cv) {
        this.calculator = cv;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        getTypes()[columnIndex].toString();//Check for non-null
        return getTypes()[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        new Boolean(getCanEdits()[columnIndex]).toString();//Check for non-null
        return getCanEdits()[columnIndex];
    }

    /**
     * 
     * @return
     */
    public Class[] getTypes() {
        return types;
    }

    /**
     * 
     * @param types
     */
    public void setTypes(Class[] types) {
        this.types = types;
    }

    /**
     * 
     * @return
     */
    public boolean[] getCanEdits() {
        return canEdits;
    }

    /**
     * 
     * @param canEdits
     */
    public void setCanEdits(boolean[] canEdits) {
        this.canEdits = canEdits;
    }

    /**
     * 
     * @return
     */
    public Vector getColumnIdentifiers() {
        return columnIdentifiers;
    }

    /**
     * Set the table editable
     * @param bool
     */
    public void setEditable(boolean bool) {
        setCanEdits(new boolean[]{bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool,
                    bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool, bool,
                    bool, bool, bool, bool, bool, bool, bool, bool, bool});
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getValueAt(int row, int column) {
        Object o = super.getValueAt(row, column);
        Class t = getColumnClass(column);
        if (!t.getName().equals("java.lang.Object")) {
            if (o != null && (t.isAssignableFrom(Double.class) ||
                    t.isAssignableFrom(double.class) ||
                    t.isAssignableFrom(float.class) ||
                    t.isAssignableFrom(Float.class))) {
                return FormatNumber.formatDezimal(Double.valueOf(o.toString()));
            } else {
                return o;
            }
        } else {
            return o;
        }
    }

    private void setDataVector(Object[][] object, Headers head) {
        super.setDataVector(object, head.getValue());
    }

    @Override
    public void fireTableCellUpdated(int row, int column) {
        if (calculator != null && !calculator.isTargetCell(row, column)) {
            calculator.calculateOnce();
        }
        super.fireTableCellUpdated(row, column);
    }

    /**
     * Checks if the model has empty rows
     * @param columnsToCheck The columns to be checked for emptyness
     * @return TRUE if the last row of the model has a NULL value at the specified columns
     */
    public boolean hasEmptyRows(int[] columnsToCheck) {
        boolean empty = true;
        for (int i = 0; i < columnsToCheck.length; i++) {
            if (getValueAt(getRowCount() - 1, columnsToCheck[i]) != null) {
                empty = false;
            }
        }
        return empty;
    }

    /**
     * Adds rows to the end of the model. The new rows will contain null values.<br/>
     * If this.context is defined, Context specific values may be added.
     * @param count
     */
    public void addRow(int count) {
        for (int i = 0; i < count; i++) {
            if (predefinedRow == null) {
                addRow((Object[]) null);
            } else {
                if (autoCountColumn != null) {
                    predefinedRow[autoCountColumn] = getRowCount() + 1;
                }
                addRow(predefinedRow);
            }
        }
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    private void defineRow(Object[] object) {
        predefinedRow = object;
    }

    /**
     * Returns all rows where the specified columns are not NULL. <br/>
     * 
     * @param columns
     * @return
     */
    public  List<Object[]> getValidRows(int[] columns) {

        List<Object[]> rows = new Vector<Object[]>();
        for (int ki = 0; ki < getRowCount(); ki++) {
            boolean valid = true;
            for (int i = 0; i < columns.length; i++) {
                int j = columns[i];
                if (getValueAt(ki, j) == null) {
                    valid = false;
                }
            }
            if (valid) {
                Object[] t = new Object[getColumnCount()];
                for (int i = 0; i < getColumnCount(); i++) {
                    t[i] = getValueAt(ki, i);
                    Log.Debug(this, "getvalueat " + t[i]);
                }
                rows.add(t);
            }
        }
        return rows;
    }

    /**
     * Default Renderers
     **/
    static class NumberRenderer extends DefaultTableCellRenderer.UIResource {

        public NumberRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
    }

    /**
     * 
     */
    static class DoubleRenderer extends NumberRenderer {

        NumberFormat formatter;

        public DoubleRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = NumberFormat.getInstance();
            }
            try {
                //Values already parsed in getValueAt(row, colum) of MPTablemodel
                setText((value == null) ? "" : value.toString());
            } catch (Exception e) {
                Log.Debug(MPTableModel.class, "Error caused by: " + value);
            }
        }
    }

    /**
     * 
     */
    static class DateRenderer extends DefaultTableCellRenderer.UIResource {

        DateFormat formatter;

        public DateRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = DateFormat.getDateInstance();
            }
            try {
                setText((value == null) ? "" : formatter.format(value));
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }

    /**
     * 
     */
    static class IconRenderer extends DefaultTableCellRenderer.UIResource {

        public IconRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public void setValue(Object value) {
            try {
                setIcon((value instanceof Icon) ? (Icon) value : null);
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }
}
