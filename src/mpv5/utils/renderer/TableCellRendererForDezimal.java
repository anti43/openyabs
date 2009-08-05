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
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.utils.numberformat.FormatNumber;

public class TableCellRendererForDezimal extends DefaultTableCellRenderer {

    public static NumberFormat DECIMALFORMAT = FormatNumber.getDefaultDecimalFormat();
    private final JTable t;
    private Color color;

    /**
     * 
     * @param t
     * @param color
     */
    public TableCellRendererForDezimal(JTable t, Color color) {
        super();
        this.t = t;
        this.color = color;
    }

    /**
     * Set this renderer to the given column + editor
     * @param column
     */
    public void setRendererTo(int column) {
        TableColumn col = t.getColumnModel().getColumn(column);
        col.setCellEditor(new TableCellEditorForDezimal(new JFormattedTextField()));
        col.setCellRenderer(this);
    }

    /**
     * 
     * @param t
     */
    public TableCellRendererForDezimal(JTable t) {
        super();
        this.t = t;
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Number) {
            value = DECIMALFORMAT.format(value);
        }
        super.setValue(value);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component p = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        p.setBackground(color == null ? Color.WHITE : color);
        return p;
    }
}
