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
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.TableView.TableRow;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.globals.Headers;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
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
    private List<TableCalculator> calculators = new Vector<TableCalculator>();

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

    /**
     * Add a cell calculator for this model
     * @param cv
     */
    public void addCalculator(TableCalculator cv) {
        this.calculators.add(cv);
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
        if (column < getColumnCount()) {
            if (row < getRowCount()) {
                return super.getValueAt(row, column);
            } else {
                throw new ArrayIndexOutOfBoundsException("The row " + row + " is not within the models row count of " + getRowCount());
            }
        } else {
            throw new ArrayIndexOutOfBoundsException("The column " + column + " is not within the models column count of " + getColumnCount());
        }
    }

    /**
     * Behaves like setValueAt(row, column)
     * @param aValue
     * @param row
     * @param column
     * @param dontFire If true, does not fire table cell event!
     */
    @SuppressWarnings("unchecked")
    public void setValueAt(Object aValue, int row, int column, boolean dontFire) {
        Vector rowVector = (Vector) dataVector.elementAt(row);
        rowVector.setElementAt(aValue, column);
        if (!dontFire) {
            fireTableCellUpdated(row, column);
        }
    }

    private void setDataVector(Object[][] object, Headers head) {
        super.setDataVector(object, head.getValue());
    }

    @Override
    public synchronized void fireTableCellUpdated(int row, int column) {

        for (int i = 0; i < calculators.size(); i++) {
            TableCalculator calculator = calculators.get(i);
            if (calculator != null && !calculator.isTargetCell(row, column)) {
                calculator.calculateOnce();
            }
        }
        fireTableChanged(new TableModelEvent(this, row, row, column));
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

    /**
     * Define a row which is used in addRow(int)
     * @param object
     */
    public void defineRow(Object[] object) {
        predefinedRow = object;
    }

    /**
     * Returns all rows where the specified columns are not NULL. <br/>
     * 
     * @param columns
     * @return
     */
    public List<Object[]> getValidRows(int[] columns) {

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
                }
                rows.add(t);
            }
        }
        return rows;
    }

    /**
     * Set/replace the given rows data
     * @param rowData
     * @param row
     * @param columnToIgnore
     */
    public synchronized void setRowAt(Object[] rowData, int row, int columnToIgnore) {
        for (int i = 0; i < rowData.length; i++) {
            if (i != columnToIgnore) {
                Object object = rowData[i];
                setValueAt(object, row, i);
            }
        }
    }

    /**
     * Set the auto increment column for addRows(int)
     * @param column
     */
    public void setAutoCountColumn(int column) {
        autoCountColumn = column;
    }

    /**
     * Default Renderers
     **/
    public static class NumberRenderer extends DefaultTableCellRenderer.UIResource {

        public NumberRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }
    }

    /**
     * 
     */
    public static class DoubleRenderer extends NumberRenderer {
        public DoubleRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
 
            try {
                setText((value == null) ? "" : FormatNumber.formatDezimal(Double.valueOf(value.toString())));
            } catch (Exception e) {
                Log.Debug(MPTableModel.class, "Error caused by: " + value);
            }
        }
    }

    /**
     * 
     */
    public static class DateRenderer extends DefaultTableCellRenderer.UIResource {

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
   public static class IconRenderer extends DefaultTableCellRenderer.UIResource {

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
