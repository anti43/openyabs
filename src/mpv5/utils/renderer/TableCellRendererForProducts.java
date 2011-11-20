/*
 * 
This file is part of YaBS.

YaBS is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

YaBS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with YaBS.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mpv5.utils.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.db.objects.Product;

public class TableCellRendererForProducts extends DefaultTableCellRenderer {

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    private final JTable t;

    /**
     * 
     * @param t
     * @param color
     */
    public TableCellRendererForProducts(JTable t) {
        super();
        this.t = t;
    }

    /**
     * Set this renderer to the given column + editor
     * @param column
     */
    public void setRendererTo(int column) {

        TableColumn col = t.getColumnModel().getColumn(t.getColumnModel().getColumnIndex(t.getModel().getColumnName(column)));
        col.setCellEditor(new TableCellEditorForDezimal(new JFormattedTextField()));
        col.setCellRenderer(this);
    }


    @Override
    protected void setValue(Object value) {
        if (value instanceof Product) {
            value = ((Product)value).__getCnumber();
        }
        super.setValue(value);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof Product) {
            value = ((Product)value).__getCnumber();
        }
        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
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
}
