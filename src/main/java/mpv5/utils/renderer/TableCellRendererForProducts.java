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
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Product;
import mpv5.globals.Messages;

public class TableCellRendererForProducts extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    private final JTable t;
    private boolean linkHint;
    //product number by id, for rows that hold the id only (product lists, deleted products)
    private final Map<Integer, String> numbers = new HashMap<Integer, String>();

    /**
     * 
     * @param t
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

    /**
     * Show product numbers as links (the product opens with Ctrl+click)
     *
     * @param linkHint
     */
    public void setLinkHint(boolean linkHint) {
        this.linkHint = linkHint;
    }

    /**
     * @param value the value of the product column
     * @return the text: the number of the product, "(deleted)" if it is gone
     */
    private String text(Object value) {
        if (value instanceof Product) {
            return ((Product) value).__getCnumber();
        } else if (value instanceof Number) {
            int id = ((Number) value).intValue();
            if (id <= 0) {
                return "";
            }
            String n = numbers.get(id);
            if (n == null) {
                try {
                    n = ((Product) DatabaseObject.getObject(Context.getProduct(), id)).__getCnumber();
                } catch (Exception e) {
                    n = "#" + id + " " + Messages.POSITION_PRODUCT_GONE;
                }
                numbers.put(id, n);
            }
            return n;
        }
        return value == null ? "" : value.toString();
    }

    @Override
    protected void setValue(Object value) {
        super.setValue(text(value));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        boolean product = value instanceof Product || (value instanceof Number && ((Number) value).intValue() > 0);
        adaptee.getTableCellRendererComponent(table, text(value), isSelected, hasFocus, row, col);
        setForeground(adaptee.getForeground());
        setBackground(adaptee.getBackground());
        setBorder(adaptee.getBorder());
        setFont(adaptee.getFont());
        setText(adaptee.getText());
        setToolTipText(null);
        if (linkHint && product) {
            //a link to the product: Ctrl+click (or the menu of the position) opens it
            setToolTipText(Messages.POSITION_OPEN_PRODUCT_TIP.toString());
            if (value instanceof Product) {
                Map<TextAttribute, Object> a = new HashMap<TextAttribute, Object>();
                a.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                setFont(getFont().deriveFont(a));
                if (!isSelected) {
                    Color link = UIManager.getColor("Component.linkColor");
                    setForeground(link != null ? link : new Color(0, 102, 204));
                }
            }
        }
        if (hasFocus) {
            setBackground(Color.BLUE);
            setForeground(Color.WHITE);
        }
        return this;
    }
}
