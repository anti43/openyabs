/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author anti
 */
public class LazyCellRenderer extends DefaultTableCellRenderer {

    protected JTable table;
    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

    public LazyCellRenderer(JTable table) {
        super();
        this.table = table;
    }

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
        return this;
    }

    /**
     * Set this renderer to the given column + editor
     * @param column
     */
    public void setRendererTo(int column) {

        TableColumn col = table.getColumnModel().getColumn(table.getColumnModel().getColumnIndex(table.getModel().getColumnName(column)));
        col.setCellEditor(new TableCellEditorForDezimal(new JFormattedTextField()));
        col.setCellRenderer(this);
    }
}
