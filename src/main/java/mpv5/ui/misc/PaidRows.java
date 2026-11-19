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
package mpv5.ui.misc;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Revenue;
import mpv5.ui.modern.ModernTheme;

/**
 * Lists of expenses and income: paid rows light green, unpaid rows light red
 * (a tint of the table background, also in the dark theme); selected rows
 * keep the selection colour. The record is in the first column of the model.
 */
public class PaidRows extends MPTable {

    private static final long serialVersionUID = 1L;
    private static final Color GREEN = new Color(0x1baf7a);
    private static final Color RED = new Color(0xe34948);

    /**
     * @param identifier the panel (for the saved column layout)
     */
    public PaidRows(Component identifier) {
        super(identifier);
    }

    /**
     * @param paid
     * @param base the normal background of the table
     * @return the background of a paid or unpaid row
     */
    public static Color background(boolean paid, Color base) {
        Color b = base == null ? ModernTheme.cardBackground() : base;
        return ModernTheme.blend(b, paid ? GREEN : RED, ModernTheme.isDark() ? 0.22f : 0.13f);
    }

    /**
     * @param o the record of a row
     * @return true/false for an expense or income, null for anything else
     */
    public static Boolean paid(Object o) {
        if (o instanceof Expense) {
            return ((Expense) o).__getIspaid();
        } else if (o instanceof Revenue) {
            return ((Revenue) o).__getIspaid();
        }
        return null;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            Boolean paid = null;
            try {
                paid = paid(getModel().getValueAt(convertRowIndexToModel(row), 0));
            } catch (Exception e) {
                //rows changing while painting
            }
            c.setBackground(paid == null ? getBackground() : background(paid, getBackground()));
            if (c instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) c).setOpaque(true);
            }
        }
        return c;
    }
}
