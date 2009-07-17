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

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.db.common.Context;
import mpv5.ui.beans.LightMPComboBox;

/**
 *
 */
public class CellRendererWithMPComboBox extends JLabel implements TableCellRenderer {

    private final Context c;
    private final JTable table;
    private Vector<JLabel> labels = new Vector<JLabel>();
    private DefaultTableCellRenderer rend = new DefaultTableCellRenderer();
    private JLabel label = new JLabel();

    /**
     * Create a new CellRenderer which holds a MPComboBox with the given Context's data as model. Will not assign itself to any column.
     * @param c
     * @param table
     */
    public CellRendererWithMPComboBox(Context c, JTable table) {
        super();
        this.c = c;
        this.table = table;
    }

    /**
     * Set this renderer to the given column
     * @param column
     */
    public void setRendererTo(int column) {
        TableColumn col = table.getColumnModel().getColumn(column);
        col.setCellEditor(new MPComboBoxEditor(new LightMPComboBox(c, table)));
        col.setCellRenderer(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus && isSelected) {
            if (isSelected) {
                label.setForeground(table.getSelectionForeground());
                label.setBackground(table.getSelectionBackground());
            } else {
                label.setForeground(table.getForeground());
                label.setBackground(table.getBackground());
            }
             label.setText((value == null) ? "" : value.toString());
            return label;
        } else {
            label.setText((value == null) ? "" : value.toString());
            return label;
        }
    }

    class MPComboBoxEditor extends LazyCellEditor {

        private final LightMPComboBox box;

        public MPComboBoxEditor(LightMPComboBox b) {
            super(b);
            this.box = b;
            b.setLightWeightPopupEnabled(false);
//            b.addKeyListener(new KeyListener() {
//
//                @Override
//                public void keyTyped(KeyEvent e) {
//                   if(e.getKeyCode() == e.VK_DOWN){
//
//                   }
//                }
//
//                @Override
//                public void keyPressed(KeyEvent e) {
//
//                }
//
//                @Override
//                public void keyReleased(KeyEvent e) {
//
//                }
//            });
        }

    }
}




