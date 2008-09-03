/*
 *  This file is part of MP by anti43 /GPL.
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
package mp4.utils.tabellen;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import mp3.classes.utils.Log;
import mp4.utils.tabellen.models.MPTableModel;

/**
 *
 * @author Andreas
 */
public class TableFormat {

    public static void makeUneditable(JTable table) {

        try {
            ((MPTableModel) table.getModel()).setCanEdits(new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false});
        } catch (Exception e) {
            Log.Debug("Can not change this table to uneditable.", true);
        }
    }

    public static void makeUneditableColumns(JTable table, Integer[] desiredCol) {
        boolean[] unedits;
        try {
            unedits = new boolean[desiredCol.length];
            for (int i = 0; i < desiredCol.length; i++) {
                if (desiredCol[i] != null) {
                    unedits[i] = false;
                } else {
                    unedits[i] = true;
                }
            }

            ((MPTableModel) table.getModel()).setCanEdits(unedits);
        } catch (Exception e) {
            Log.Debug("Can not change this table to uneditable.", true);
        }
    }

    /**
     * Stops the tables' cell editor
     * @param jTable1
     */
    public static void stopEditing(JTable jTable1) {
        TableCellEditor editor = jTable1.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
    }

    /**
     * Hides he first column of a table (usually "id")
     * @param table
     * 
     */
    public static void stripFirst(JTable table) {
        try {
            table.getColumn(table.getColumnName(0)).setMinWidth(0);
            table.getColumn(table.getColumnName(0)).setMaxWidth(0);
        } catch (Exception e) {
            Log.Debug(e.getMessage());
        }
    }

    /**
     * Resizes a tables cols
     * @param table
     * @param desiredColSizes
     * @param fixed Should the cols be non-resizable
     */
    public static void resizeCols(JTable table, Integer[] desiredColSizes, boolean fixed) {

        for (int i = 0; i < desiredColSizes.length; i++) {
            if (desiredColSizes[i] != null) {
                table.getColumn(table.getColumnName(i)).setMinWidth(desiredColSizes[i]);
                table.getColumn(table.getColumnName(i)).setPreferredWidth(desiredColSizes[i]);
                if (fixed) {
                    table.getColumn(table.getColumnName(i)).setMaxWidth(desiredColSizes[i]);
                }
            }
        }
    }
}
