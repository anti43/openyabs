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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultCellEditor;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.TableCellEditorForDezimal;
import mpv5.utils.tables.DynamicTableCalculator;

/**
 *
 * A custom table model which implements various convenience methods
 */
public class MPTableModel extends DefaultTableModel implements Cloneable, TableColumnModelListener {

    private static final long serialVersionUID = 1L;
    private Class[] types;
    private boolean[] canEdits;
    private Context context;
    private Object[] predefinedRow;
    private Integer autoCountColumn;
    private ArrayList<DynamicTableCalculator> calculators = new ArrayList<DynamicTableCalculator>();
    private JTable owner;

    /**
     * Creates an empty, uneditable model
     */
    public MPTableModel() {
        super();
        setEditable(false);

        Class[] typs = new Class[333];
        for (int i = 0; i < 333; i++) {
            typs[i] = Object.class;
        }
        setTypes(typs);
    }

    /**
     * May get slow on lots of data within the given Context
     *
     * @param c
     * @param target
     */
    public MPTableModel(final Context c, final JTable target) {
        super();
        try {
            setContext(c);
            final ArrayList<DatabaseObject> data = DatabaseObject.getObjects(c, true);
            final Object[][] dat = new Object[data.size()][];
            final Class[] classes = new Class[100];
            int j = 0;
            LinkedList<String> head = new LinkedList<String>();
            Entry<String, Object> e;
            for (DatabaseObject o : data) {
                final HashMap<String, Object> mod = o.getValues4();
                Set<Entry<String, Object>> set = mod.entrySet();
                dat[j] = new Object[set.size()];
                Iterator<Entry<String, Object>> it = set.iterator();
                int i = 0;
                while (it.hasNext()) {
                    e = it.next();
                    dat[j][i] = e.getValue();
                    if (j == 0) {
                        Class class1 = e.getValue().getClass();
                        classes[i] = class1;
                        head.add(i, e.getKey().toUpperCase());
                    }
                    i++;
                }
                j++;
            }
            final Object[] headers = new Object[head.size()];
            int x = 0;
            for (String hea : head) {
                headers[x] = hea;
                x++;
            }
            setDataVector(dat, headers);
            setEditable(false);
            setTypes(classes);
            if (target != null) {
                target.setAutoCreateRowSorter(true);
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
    }

    public <T extends DatabaseObject> MPTableModel(final List<T> list, final JTable target) {
        super();
        final Object[][] dat = new Object[list.size()][];
        final Class[] classes = new Class[100];

        int j = 0;
        LinkedList<String> head = new LinkedList<String>();
        Entry<String, Object> e;
        for (DatabaseObject object : list) {
            if (context == null) {
                setContext(object.getContext());
            }
            final HashMap<String, Object> mod = object.getValues4();
            Set<Entry<String, Object>> set = mod.entrySet();
            dat[j] = new Object[set.size()];
            Iterator<Entry<String, Object>> it = set.iterator();
            int i = 0;
            while (it.hasNext()) {
                e = it.next();
                dat[j][i] = e.getValue();
                if (j == 0) {
                    Class class1 = e.getValue().getClass();
                    classes[i] = class1;
                    head.add(i, e.getKey().toUpperCase());
                }
                i++;
            }
            j++;
        }

        final Object[] headers = new Object[head.size()];
        int x = 0;
        for (String hea : head) {
            headers[x] = hea;
            x++;
        }
        setEditable(false);
        setTypes(classes);
        try {
//            Log.PrintArray(dat);
            Object[] h = getDefaultHeader(15);
            setDataVector(dat, h);
        } catch (Exception ge) {
        }
        if (target != null) {
            target.setAutoCreateRowSorter(true);
        }
        target.repaint();
        target.getParent().validate();
    }

    @Override
    public MPTableModel clone() {
        MPTableModel t = new MPTableModel(types, canEdits, getData(), predefinedRow);
        return t;
    }

    /**
     * Creates an uneditable model out of the given data
     *
     * @param data
     */
    public MPTableModel(Object[][] data) {
        super();

        if (data != null && data.length > 0) {
            String[] header = new String[data[0].length];
            for (int i = 0; i < header.length; i++) {
                header[i] = "" + i;
            }
            setDataVector(data, header);
            Class[] typs = new Class[header.length];
            for (int i = 0; i < header.length; i++) {
                typs[i] = Object.class;
            }
            setTypes(typs);
        }

        setEditable(false);
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
     *
     * @param datstr
     * @param header
     */
    public MPTableModel(Object[][] datstr, String[] header) {
        super();

        setDataVector(datstr, header);
        setEditable(false);
        Class[] typs = new Class[header.length];
        for (int i = 0; i < header.length; i++) {
            typs[i] = Object.class;
        }
        setTypes(typs);
    }

    /**
     * Creates an uneditable model out of the given data
     *
     * @param data
     * @param header
     */
    public MPTableModel(Object[][] data, Headers header) {
        super(data, header.getValue());
        setEditable(false);

        Class[] typs = new Class[header.getValue().length];
        for (int i = 0; i < header.getValue().length; i++) {
            typs[i] = Object.class;
        }
        setTypes(typs);
    }

    /**
     * Creates an uneditable model out of the given data
     *
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
     *
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
     *
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
     *
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
     * Creates an uneditable model out of the given data
     *
     * @param types
     * @param data
     * @param columnNames
     */
    public MPTableModel(List<Object[]> list, String[] header) {
        super();
        int width = 0;
        for (int i = 0; i < list.size(); i++) {
            Object[] objects = list.get(i);
            if (objects.length > width) {
                width = objects.length;
            }
        }

        if (list.size() > 0) {
            if (header == null) {
                header = new String[width];
                for (int i = 0; i < header.length; i++) {
                    header[i] = String.valueOf(i);
                }
            }
            Object[][] arr = ArrayUtilities.listToTableArray(list);
            setDataVector(arr, header);
        }

        setEditable(false);
        Class[] typs = new Class[header.length];
        for (int i = 0; i < header.length; i++) {
            typs[i] = Object.class;
        }
        setTypes(typs);
    }

    public MPTableModel(List<ValueProperty> properties) {
        super();
        final Object[][] data = new Object[properties.size() + 10][2];
        for (int i = 0; i < properties.size(); i++) {
            ValueProperty valueProperty = properties.get(i);
            data[i][0] = valueProperty.__getCname();
            data[i][1] = valueProperty.getValueObj();
        }

        setDataVector(data, new Object[]{Messages.PROPERTY, Messages.VALUE});
        setEditable(true);
        setTypes(new Class<?>[]{String.class, Object.class});
    }

    /**
     * Add a cell calculator for this model
     *
     * @param cv
     */
    public void addCalculator(DynamicTableCalculator cv) {
        this.calculators.add(cv);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        try {
            getTypes()[columnIndex].toString();//Check for non-null
        } catch (Exception e) {
            return Object.class;
        }
        //not sortable
        if (getTypes()[columnIndex].equals(void.class)) {
            return Object.class;
        } else {
            return getTypes()[columnIndex];
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        new Boolean(getCanEdits()[columnIndex]).toString();//Check for non-null
        return getCanEdits()[columnIndex];
    }

    public void setCellEditable(int rowIndex, int columnIndex, boolean editable) {
        new Boolean(getCanEdits()[columnIndex]).toString();//Check for non-null
        getCanEdits()[columnIndex] = editable;
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
    public final void setTypes(Class... types) {
        this.types = types;
    }

    /**
     *
     * @return
     */
    public boolean[] getCanEdits() {
        return canEdits;
    }

//    /**
//     *
//     * @param canEdits
//     */
//    public void setCanEdits(boolean[] canEdits) {
//        this.canEdits = canEdits;
//    }
    /**
     *
     * @return
     */
    public Vector getColumnIdentifiers() {
        return columnIdentifiers;
    }

    /**
     * Set the table editable
     *
     * @param bool
     */
    public final void setEditable(boolean bool) {
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
     *
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
            DynamicTableCalculator calculator = calculators.get(i);
            if (calculator != null && !calculator.isTargetCell(row, column)) {
                calculator.calculateOnce();
            }
        }

        fireTableChanged(new TableModelEvent(this, row, row, column));
    }

    /**
     * Checks if the model has empty rows
     *
     * @param columnsToCheck The columns to be checked for emptyness
     * @return TRUE if the last row of the model has a NULL value at the
     * specified columns
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
     * Checks if the model has empty rows
     *
     * @param columnsToCheck The columns to be checked for emptyness
     * @return the count of rows in the model with a NULL or empty "" value at
     * the specified columns
     */
    public int getEmptyRows(int[] columnsToCheck) {
        int count = 0;
        for (int j = 0; j < getRowCount(); j++) {
            boolean found = false;
            for (int i = 0; i < columnsToCheck.length; i++) {
                if (getValueAt(j, columnsToCheck[i]) == null || String.valueOf(getValueAt(j, columnsToCheck[i])).length() == 0) {
                    found = true;
                }
            }
            if (found) {
                count++;
            }
        }
        return count;
    }

    /**
     * Removes the invalid rows
     *
     * @param columnsToCheck The columns to be checked for emptyness
     */
    public void removeEmptyRows(int[] columnsToCheck) {
        synchronized (this) {
            List<Integer> remove = new ArrayList<>();
            int rc = getRowCount();
            Log.Debug(this, "removeEmptyRows rows from " + rc);
            for (int row = 0; row < rc; row++) {
                Log.Debug(this, "removeEmptyRows row " + row);
                for (int col = 0; col < columnsToCheck.length; col++) {
                    Object v = getValueAt(row, columnsToCheck[col]);
                    if (v == null || String.valueOf(v).length() == 0) {
                        Log.Debug(this, "removeEmptyRows Column " + columnsToCheck[col] + " in row " + row + " IS empty: " + String.valueOf(v));
                        remove.add(row); 
                    } else {
                        Log.Debug(this, "removeEmptyRows Column " + columnsToCheck[col] + " in row " + row + " is not empty: " + String.valueOf(v));
                    }
                }
            }
            removeRows(remove.toArray(new Integer[remove.size()]));
        }
    }

    public void removeRows(Integer[] indices) {
        Arrays.sort(indices);
        for (int i = indices.length - 1; i >= 0; i--) {
            super.removeRow(indices[i]); 
        }
        rearrangeAutocountColumn();
    }

    /**
     * Adds rows to the end of the model. The new rows will contain null
     * values.<br/> If this.context is defined, Context specific values may be
     * added.
     *
     * @param count
     */
    public void addRow(int count) {
        synchronized (this) {
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
    }

    @Override
    public void removeRow(int row) {
        synchronized (this) {
            super.removeRow(row);
            rearrangeAutocountColumn();
            Log.Debug(this, "removeRow " + row);
        }
    }

    public void insertRow(int row) {
        synchronized (this) {
            if (predefinedRow == null) {
                super.insertRow(row, (Object[]) null);
            } else {
                super.insertRow(row, predefinedRow);
            }
            rearrangeAutocountColumn();
        }
    }

    @Override
    public void moveRow(int start, int end, int to) {
        synchronized (this) {
            super.moveRow(start, end, to);
            rearrangeAutocountColumn();
        }
    }

    @SuppressWarnings("unchecked")
    private void rearrangeAutocountColumn() {
        if (autoCountColumn == null) {
            return;
        }
        int index = 0;
        for (Object rowVector : dataVector) {
            ((Vector) rowVector).setElementAt(index + 1, autoCountColumn);
            fireTableCellUpdated(index++, autoCountColumn);
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
    public final void setContext(Context context) {
        this.context = context;
    }

    /**
     * Define a row which is used in addRow(int)
     *
     * @param object
     */
    public void defineRow(Object[] object) {
        predefinedRow = object;
    }

    /**
     * Returns all rows where all of the specified columns are not NULL. <br/>
     *
     * @param columns
     * @return
     */
    public LinkedList<Object[]> getValidRows(int[] columns) {

        LinkedList<Object[]> rows = new LinkedList<Object[]>();
        for (int ki = 0; ki < getRowCount(); ki++) {
            boolean valid = true;
            for (int i = 0; i < columns.length; i++) {
                int j = columns[i];
                if (getValueAt(ki, j) == null || getValueAt(ki, j).toString().length() == 0) {
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
     *
     * @param rowData
     * @param row
     * @param columnToIgnore
     */
    public synchronized void setRowAt(Object[] rowData, int row, Integer... columnToIgnore) {

        if (getRowCount() <= row) {
            addRow(1);
        }
        List<Integer> ignores = Arrays.asList(columnToIgnore);
        for (Integer i = 0; i < rowData.length; i++) {
            if (!ignores.contains(i)) {
                Object object = rowData[i];
                try {
                    setValueAt(object, row, i);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Set the auto increment column for addRows(int)
     *
     * @param column
     */
    public void setAutoCountColumn(int column) {
        autoCountColumn = column;
    }

    /**
     * Returns the data as array
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object[][] getData() {
        Object[][] k = new Object[getRowCount()][getColumnCount()];
        for (int i = 0; i < k.length; i++) {
            for (int j = 0; j < k[i].length; j++) {
                k[i][j] = getValueAt(i, j);
            }
        }
        return k;
    }
    
    /**
     * Returns the data as array
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object[][] getDataLikeDisplayed() {
        JTable tab = this.getOwner();
        if (tab == null) {
            return this.getData();
        } else {
            Object[][] k = new Object[getRowCount()][getColumnCount()];
            for (int i = 0; i < k.length; i++) {
                for (int j = 0; j < k[i].length; j++) {
                    k[i][j] = getValueAt(tab.convertRowIndexToModel(i), tab.convertColumnIndexToModel(j));
                }
            }
            return k;
        }
    }

    /**
     * Returns the last valid row in a table whereas "valid" is defined as "all
     * columns in columns are not null"
     *
     * @param columns
     * @return
     */
    public int getLastValidRow(int[] columns) {

        int row = 0;
        for (int ki = 0; ki < getRowCount(); ki++) {
            for (int i = 0; i < columns.length; i++) {
                int j = columns[i];
                if (getValueAt(ki, j) == null || getValueAt(ki, j).toString().length() == 0) {
                } else {
                    row = ki;
                }
            }
        }
        return row;
    }

    /**
     * Add all rows
     *
     * @param rowsl
     */
    public void addRows(List<Object[]> rowsl) {
        for (Object[] obj : rowsl) {
            addRow(obj);
        }
    }

    /**
     * Adds all rows using the toArray() method of {@link DatabaseObject}
     *
     * @param l
     */
    public void addRows(ArrayList<DatabaseObject> l) {
        for (DatabaseObject b : l) {
            addRow(b.toArray());
        }
    }

    /**
     * Returns an Object where the objects ID is stored in the first column of
     * the given row; <b>From database/cache, NOT from the model!</b>
     *
     * @param <T>
     * @param selectedRow
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends DatabaseObject> T getRowAt(int selectedRow, T target) {
        try {
            return (T) target.getObject(target.getContext(), Integer.valueOf(String.valueOf(getValueAt(selectedRow, 0))));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
            return null;
        }
    }

    /**
     * Set the editable columns
     *
     * @param editable
     */
    public final void setCanEdits(boolean... editable) {
        this.canEdits = editable;
    }

    public static TableCellEditor getTablecCellEditorFor(Class<?> clazz) {

        if (clazz.isAssignableFrom(Number.class)) {
            return new TableCellEditorForDezimal(new JFormattedTextField());
        } else {
            return new DefaultCellEditor(new JTextField());
        }
    }

    /**
     * @return the owner
     */
    public JTable getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(JTable owner) {
        this.owner = owner;
    }

    @Override
    public void columnAdded(TableColumnModelEvent e) {
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        resort();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    private void resort() {
    }

    private Object[] getDefaultHeader(int x) {
        Object[] obj = new Object[x];
        for (int i = 0; i < x; i++) {
            obj[i] = i;
        }

        return obj;
    }

    public void removeColumn(int column) {
        columnIdentifiers.remove(column);
        for (Object row : dataVector) {
            if (row instanceof Vector) {
                ((Vector) row).remove(column);
            }
        }
        fireTableStructureChanged();
    }

    /**
     * Default renderers
     *
     */
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
