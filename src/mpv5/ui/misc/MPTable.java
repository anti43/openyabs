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
package mpv5.ui.misc;

import java.awt.Component;
import java.awt.Dialog;
import java.util.List;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.ui.panels.ListPanel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;

/**
 * A JTable implementation which keeps its view state saved in the current yabs user profile.
 *
 * Be careful with the column sorting.
 * @author andreas
 */
public class MPTable extends JTable {

    private static final long serialVersionUID = 1L;
    private TableViewPersistenceHandler persistanceHandler;

    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model. The identifier JComponent is used to save the state of the table columns in the actual view.
     * Not using this one constructor may lead to inconsistent behavior of the view if this JTable's parent component has no unique name.
     * It is recommended to use this constructor in Yabs.
     *
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MPTable(JComponent identifier) {
        super();
        setName("43");
        if (identifier.getName() == null) {
            identifier.setName(identifier.getClass().getSimpleName());
        }
        setPersistanceHandler(new TableViewPersistenceHandler(this, identifier));
    }
    
     /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model. The identifier JComponent is used to save the state of the table columns in the actual view.
     * Not using this one constructor may lead to inconsistent behavior of the view if this JTable's parent component has no unique name.
     * It is recommended to use this constructor in Yabs.
     *
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MPTable(Component identifier) {
        super();
        setName("43");
        if (identifier.getName() == null) {
            identifier.setName(identifier.getClass().getSimpleName());
        }
        setPersistanceHandler(new TableViewPersistenceHandler(this, identifier));
    }

    /**
     * Constructs a default <code>JTable</code> that is initialized with a default
     * data model, a default column model, and a default selection
     * model.
     *
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MPTable() {
        super();
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, a default column model,
     * and a default selection model.
     *
     * @param dm        the data model for the table
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MPTable(TableModel dm) {
        super(dm);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code>
     * as the column model, and a default selection model.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @see #createDefaultSelectionModel
     */
    public MPTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code> as the
     * column model, and <code>sm</code> as the selection model.
     * If any of the parameters are <code>null</code> this method
     * will initialize the table with the corresponding default model.
     * The <code>autoCreateColumnsFromModel</code> flag is set to false
     * if <code>cm</code> is non-null, otherwise it is set to true
     * and the column model is populated with suitable
     * <code>TableColumns</code> for the columns in <code>dm</code>.
     *
     * @param dm        the data model for the table
     * @param cm        the column model for the table
     * @param sm        the row selection model for the table
     * @see #createDefaultDataModel
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public MPTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));

    }

    /**
     * Constructs a <code>JTable</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells using
     * <code>DefaultTableModel</code>.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           the number of rows the table holds
     * @param numColumns        the number of columns the table holds
     * @see javax.swing.table.DefaultTableModel
     */
    public MPTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Constructs a <code>JTable</code> to display the values in the
     * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>,
     * with column names, <code>columnNames</code>.  The
     * <code>Vectors</code> contained in <code>rowData</code>
     * should contain the values for that row. In other words,
     * the value of the cell at row 1, column 5 can be obtained
     * with the following code:
     * <p>
     * <pre>((Vector)rowData.elementAt(1)).elementAt(5);</pre>
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public MPTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Constructs a <code>JTable</code> to display the values in the two dimensional array,
     * <code>rowData</code>, with column names, <code>columnNames</code>.
     * <code>rowData</code> is an array of rows, so the value of the cell at row 1,
     * column 5 can be obtained with the following code:
     * <p>
     * <pre> rowData[1][5]; </pre>
     * <p>
     * All rows must be of the same length as <code>columnNames</code>.
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     */
    public MPTable(final Object[][] rowData, final Object[] columnNames) {
        super(rowData, columnNames);
        setName("43");
        setPersistanceHandler(new TableViewPersistenceHandler(this, (JComponent) this.getParent()));
    }

    /**
     * Sets the data model for this table to newModel and registers with it for listener notifications from the new data model.
     * Additionally takes care of the registered {@link TableViewPersistenceHandler}.
     * @param model
     */
    @Override
    public void setModel(TableModel model) {
        synchronized (this) {
            super.setModel(model);
            Log.Debug(this, "done set model");
                   
            if (persistanceHandler != null) {
                persistanceHandler.restore();
                persistanceHandler.set();
            }
        }
    }

    /**
     * Sets the data model for this table to newModel and registers with it for listener notifications from the new data model.
     * Additionally takes care of the registered {@link TableViewPersistenceHandler}.
     * @param model
     * @param fieldCount
     * @param fields 
     */
    public synchronized void setModel(List<DatabaseObject> model, int fieldCount, String fields) {
        Object[][] data = new Object[model.size()][];
        for (int i = 0; i < model.size(); i++) {
            DatabaseObject databaseObject = model.get(i);
            data[i] = databaseObject.toResolvedArray(fieldCount, fields);
        }
        MPTableModel m = new MPTableModel(data);
        setModel(m);
        if (persistanceHandler != null) {
            persistanceHandler.restore();
            persistanceHandler.set();
        }
    }

    /**
     * Reset the columns to initial sizes (if set)
     */
    public synchronized void reset() {
        if (desiredColSizes != null) {
            createDefaultColumnsFromModel();
            try {
                TableFormat.resizeCols(this, desiredColSizes, fixedCols);
            } catch (Exception e) {
            }
        } else {
            createDefaultColumnsFromModel();
        }
        if (persistanceHandler != null) {
            persistanceHandler.restore();
            persistanceHandler.set();
        }
    }
    private Integer[] desiredColSizes;
    private Boolean[] fixedCols;

    /**
     * @param desiredColSizes the desiredColSizes to set
     */
    public void setDesiredColSizes(Integer[] desiredColSizes) {
        this.desiredColSizes = desiredColSizes;
    }

    /**
     * @param fixedCols the fixedCols to set
     */
    public void setFixedCols(Boolean[] fixedCols) {
        this.fixedCols = fixedCols;
    }

    /**
     * Set the initial sizes
     * @param integer
     * @param b
     */
    public void setDefaultColumns(Integer[] integer, Boolean[] b) {
        setDesiredColSizes(integer);
        setFixedCols(b);
    }
    private ListPanel parentc;

    /**
     * Causes the parent container to refresh if its a {@link ListPanel} and not null.
     */
    public void refresh() {
        if (parentc != null) {
            parentc.refresh();
        }
    }

    /**
     * 
     */
    public void setParentListPanel(ListPanel parentc) {
        this.parentc = parentc;
    }

    /**
     * @return the persistanceHandler
     */
    public TableViewPersistenceHandler getPersistanceHandler() {
        return persistanceHandler;
    }

    /**
     * @param persistanceHandler the persistanceHandler to set
     */
    public void setPersistanceHandler(TableViewPersistenceHandler persistanceHandler) {
        this.persistanceHandler = persistanceHandler;
        if (persistanceHandler != null) {
            persistanceHandler.set();
        }
    }
//
//    /**
//     * Gets the value for the cell in the table model at row and column.
//     * Note: Unlike the original JTable implementation, the column is specified in the table <b>TableModel</b> order,
//     * and not in the view's column order.
//     * This is an important distinction because as the user rearranges the columns in the table,
//     * the column at a given index in the view will change.
//     * Meanwhile the user's actions never affect the model's column ordering.
//     * @param col
//     * @param row
//     */
//    @Override
//    public Object getValueAt(int row, int col) {
//        return getModel().getValueAt(row, col);
//    }
//
//    public void setDefaultColumns(Integer[] integer, boolean resizable) {
//
//    }
//
//    @Override
//    public void setAutoCreateRowSorter(boolean yes){
//    //do not do it. Imagine the user resorts the table, then clicks on a row, but the underlying model is not resorted
//        super.setAutoCreateRowSorter(false);
//    }
}
