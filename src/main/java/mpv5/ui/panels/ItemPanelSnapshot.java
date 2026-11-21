/*
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
 */
package mpv5.ui.panels;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import mpv5.db.objects.Product;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

/**
 * Canonical UI-state snapshot helpers for invoice/item panels so export
 * actions only auto-save when the user actually edited the record.
 */
final class ItemPanelSnapshot {

    private ItemPanelSnapshot() {
    }

    static String comboId(LabeledCombobox box) {
        if (box == null) {
            return "";
        }
        MPComboBoxModelItem item = box.getSelectedItem();
        if (item != null && item.isValid()) {
            return String.valueOf(item.getId()).trim();
        }
        return "";
    }

    static String text(String s) {
        return s == null ? "" : s;
    }

    static String date(LabeledDateChooser chooser) {
        if (chooser == null || chooser.getDateChooser() == null || !chooser.getDateChooser().hasDate()) {
            return "";
        }
        Date d = chooser.getDate();
        return d == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    static String positions(MPTableModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<Object[]> rows = model.getValidRows(new int[]{4});
        for (Object[] row : rows) {
            sb.append(intId(cell(row, 0))).append('\u001f')
                    .append(decimal(cell(row, 1))).append('\u001f')
                    .append(decimal(cell(row, 2))).append('\u001f')
                    .append(textOf(cell(row, 3))).append('\u001f')
                    .append(textOf(cell(row, 4))).append('\u001f')
                    .append(decimal(cell(row, 5))).append('\u001f')
                    .append(decimal(cell(row, 6))).append('\u001f')
                    .append(productId(cell(row, 10))).append('\u001f')
                    .append(textOf(cell(row, 13))).append('\u001f')
                    .append(textOf(cell(row, 14))).append('\u001f')
                    .append(decimal(cell(row, 15))).append('\u001e');
        }
        return sb.toString();
    }

    private static Object cell(Object[] row, int idx) {
        return row != null && idx >= 0 && idx < row.length ? row[idx] : null;
    }

    private static String intId(Object o) {
        if (o == null) {
            return "";
        }
        String s = o.toString().trim();
        if (s.isEmpty()) {
            return "";
        }
        try {
            int id = Integer.parseInt(s);
            return id > 0 ? Integer.toString(id) : "";
        } catch (NumberFormatException e) {
            return s;
        }
    }

    private static String productId(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof Product) {
            int id = ((Product) o).__getIDS();
            return id > 0 ? Integer.toString(id) : "";
        }
        if (o instanceof MPComboBoxModelItem) {
            MPComboBoxModelItem item = (MPComboBoxModelItem) o;
            return item.isValid() ? intId(item.getId()) : "";
        }
        return intId(o);
    }

    private static String decimal(Object o) {
        if (o == null) {
            return "0";
        }
        String s = o.toString().trim();
        if (s.isEmpty()) {
            return "0";
        }
        try {
            BigDecimal bd = FormatNumber.getBigDecimal(o);
            if (bd == null) {
                return "0";
            }
            bd = bd.stripTrailingZeros();
            return bd.signum() == 0 ? "0" : bd.toPlainString();
        } catch (Exception e) {
            return s;
        }
    }

    private static String textOf(Object o) {
        return o == null ? "" : o.toString();
    }
}
