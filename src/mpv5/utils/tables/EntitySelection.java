/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import mpv5.db.common.DatabaseObject.Entity;
import mpv5.logging.Log;
import mpv5.utils.models.MPTableModel;

public class EntitySelection {

    private Entity[] ids;
    private boolean noerror;
    private int[] rows;
    private final JTable table;

    @SuppressWarnings("unchecked")
    public EntitySelection(JTable table) {
        this.table = table;
        try {
            if (table.getCellEditor() != null) {
                try {
                    table.getCellEditor().stopCellEditing();
                } catch (Exception e) {
                }
            }
            rows = table.getSelectedRows();
            ids = new Entity[rows.length];
            for (int i = 0; i < rows.length; i++) {
                int row = rows[i];
                try {
                    ids[i] = (Entity) table.getValueAt(row, table.convertColumnIndexToView(0));
                } catch (Exception e) {
                    //not in col 0
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        if (((MPTableModel) table.getModel()).getTypes()[j].isAssignableFrom(Entity.class)){
                           ids[i] = (Entity) table.getValueAt(row, table.convertColumnIndexToView(j));
                        }
                    }
                }
            }
            noerror = true;
        } catch (Exception e) {
            noerror = false;
            Log.Debug(e);
        }
    }

    public boolean checkID() {
        return noerror;
    }

    /**
     * @return the rows
     */
    public List<Integer> getRows() {
        List<Integer> rowsi = new ArrayList<Integer>();
        for (int i = 0; i < rows.length; i++) {
            rowsi.add(new Integer(rows[i]));
        }
        return Collections.unmodifiableList(rowsi);
    }

    /**
     * @return the table
     */
    public JTable getTable() {
        return table;
    }

    /**
     * @return the ids
     */
    public List<Entity> getIds() {
        return Collections.unmodifiableList(Arrays.asList(ids));
    }
}
