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
package mpv5.utils.renderer;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.db.common.Context;
import mpv5.logging.Log;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 *
 */
public class CellRendererWithMPComboBox extends mpv5.ui.beans.MPCombobox implements TableCellRenderer {

    private final Context c;
    private final JTable table;

    /**
     * Create a new CellRenderer which holds a MPComboBox. Will not assign itself to any column.
     * @param c
     * @param table
     */
    public CellRendererWithMPComboBox(Context c, JTable table) {
        this.c = c;
        this.table = table;
    }

    /**
     * Set this renderer to the given column
     * @param column
     */
    public void setRendererTo(int column) {
        TableColumn col = table.getColumnModel().getColumn(column);
        col.setCellEditor(new MPComboBoxEditor(c));
        col.setCellRenderer(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

//            Select the current value
            try {
//                getComboBox().setSelectedIndex(1);
                setSelectedItem(((MPComboBoxModelItem)value).getValue());
            } catch (Exception e) {
                Log.Debug(e);
            }
            return this;

        } else {
            return new DefaultCellRenderer();
        }
    }

    class MPComboBoxEditor extends DefaultCellEditor {

        private final Context c;

        public MPComboBoxEditor(Context c) {
            super(new mpv5.ui.beans.MPCombobox(c, table).getComboBox());
            this.c = c;
        }
    }
}




