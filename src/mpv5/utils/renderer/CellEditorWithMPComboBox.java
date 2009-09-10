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
package mpv5.utils.renderer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import mpv5.db.common.Context;
import mpv5.ui.beans.LightMPComboBox;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;

/**
 *
 */
public class CellEditorWithMPComboBox  {

    private final Context c;
    private final JTable table;
    private JLabel label = new JLabel();

    /**
     * Create a new CellRenderer which holds a MPComboBox with the given Context's data as model. Will not assign itself to any column.
     * @param c
     * @param table
     */
    public CellEditorWithMPComboBox(Context c, JTable table) {
        super();
        this.c = c;
        this.table = table;
    }

    /**
     * Set this renderer to the given column
     * @param column
     * @param r 
     */
    public void setEditorTo(int column, MPCBSelectionChangeReceiver r) {
        setEditorTo(column, r, true);
    }

    /**
     * Set this renderer to the given column
     * @param column
     * @param r
     * @param editable 
     */
    public void setEditorTo(int column, MPCBSelectionChangeReceiver r, boolean editable) {
        TableColumn col = table.getColumnModel().getColumn(column);
        LightMPComboBox xc = new LightMPComboBox(c, table);
        if (r != null) {
            xc.addSelectionChangeReceiver(r);
        }
        col.setCellEditor(new MPComboBoxEditor(xc));
        xc.setEditable(editable);
    }

    class MPComboBoxEditor extends LazyCellEditor {

        private final LightMPComboBox box;

        public MPComboBoxEditor(LightMPComboBox b) {
            super(b);
            this.box = b;
            b.setLightWeightPopupEnabled(false);
        }
    }
}




