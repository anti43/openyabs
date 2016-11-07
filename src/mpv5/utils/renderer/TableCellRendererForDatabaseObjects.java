/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.DatabaseObject;

/**
 *
 * @author anti
 */
public class TableCellRendererForDatabaseObjects extends DefaultTableCellRenderer {

    private DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    private int dbColumn = 0;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setForeground(adaptee.getForeground());
        setBackground(adaptee.getBackground());
        setBorder(adaptee.getBorder());
        setFont(adaptee.getFont());
        setText(adaptee.getText());
        if (hasFocus) {
            setBackground(Color.BLUE);
            setForeground(Color.WHITE);
        }
        if (!isSelected) {
            setBackground(((DatabaseObject) table.getModel().getValueAt(table.convertRowIndexToModel(row), getDbColumn())).getColor());
        }
        return this;
    }

    /**
     * @return the dbColumn
     */
    public int getDbColumn() {
        return dbColumn;
    }

    /**
     * @param dbColumn the dbColumn to set
     */
    public void setDbColumn(int dbColumn) {
        this.dbColumn = dbColumn;
    }
}
