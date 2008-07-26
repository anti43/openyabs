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

/**
 *
 * @author Andreas
 */
public class TableFormat {

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
            table.getColumn(table.getColumnName(0)).setMinWidth(0);
            table.getColumn(table.getColumnName(0)).setMaxWidth(0);
       }
    
    /**
     * Resizes a tables cols
     * @param table
     * @param desiredColSizes
     * @param fixed Should the cols be non-resizable
     */
    public static void resizeCols(JTable table, Integer[] desiredColSizes, boolean fixed){
        for (int i = 0; i < desiredColSizes.length; i++) {
            table.getColumn(table.getColumnName(i)).setMinWidth(desiredColSizes[i]);
            table.getColumn(table.getColumnName(i)).setPreferredWidth(desiredColSizes[i]);
            if (fixed) {
                table.getColumn(table.getColumnName(i)).setMaxWidth(desiredColSizes[i]);
            }
        }
    }
}
