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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;
import mpv5.utils.numberformat.FormatNumber;

public class TableCellRendererForDezimal extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    private final JTable t;
    private Color color;
    private NumberFormat format;

    /**
     * 
     * @param t
     * @param color Not used yet
     */
    public TableCellRendererForDezimal(JTable t, Color color) {
        this(t);
        this.color = color;
    }

    /**
     * 
     * @param t
     * @param pattern Overriding FormatNumber.getShortDecimalFormat() and GlobalSettings.getProperty("table.decimal.format")
     */
    public TableCellRendererForDezimal(JTable t, String pattern) {
        this(t);
        if (pattern != null && pattern.length() != 0) {
            try {
                this.format = new DecimalFormat(pattern);
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }

    /**
     * Set this renderer to the given column + editor
     * @param column
     */
    public void setRendererTo(int column) {

        TableColumn col = t.getColumnModel().getColumn(column);
        col.setCellEditor(new TableCellEditorForDezimal(new JFormattedTextField(), format));
        col.setCellRenderer(this);
    }

    /**
     * 
     * @param t
     */
    public TableCellRendererForDezimal(JTable t) {
        super();
        this.t = t;

        if (GlobalSettings.hasProperty("table.decimal.format")) {
            try {
                this.format = new DecimalFormat(GlobalSettings.getProperty("table.decimal.format"));
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Number) {
            value = format.format(value);
        }
        super.setValue(value);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof Number) {
            value = format == null ? FormatNumber.formatDezimal((Number) value) : format.format((Number) value); //format.format(value);
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
