/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.utils.renderer;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public class TableCellRendererForDatabaseObjects extends DefaultTableCellRenderer{


        private DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(adaptee.getForeground());
            setBackground(adaptee.getBackground());
            setBorder(adaptee.getBorder());
            setFont(adaptee.getFont());
            setText(adaptee.getText());

            if (!isSelected) {
                setBackground(((DatabaseObject) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0)).getColor());
            }
            return this;
        }
    
}
