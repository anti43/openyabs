
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
package mpv5.utils.tables;

//~--- JDK imports ------------------------------------------------------------
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 *
 */
public class Selection {

    private int id;
    private boolean noerror;
    private final int row;
    private final JTable table;

    public Selection(final JTable table) {
        this.table = table;
        if (table.getCellEditor() != null) {
            try {table.getCellEditor().stopCellEditing();} catch (Exception e) {}
        }
        row = table.getSelectedRow();
        try {
//            System.err.print("@AAAAAAAAAAAAAAAAAAAAAAAAA@    " + row);
            id = Integer.valueOf(String.valueOf(
                    table.getValueAt(row,
                    table.convertColumnIndexToView(0))));
            noerror = true;
        } catch (Exception numberFormatException) {
            noerror = false;
        }
    }

    public synchronized int getId() {
        return id;
    }

    public Object[] getRowData() {
        Object[] data = new Object[table.getModel().getColumnCount()];

        for (int idx = 0; idx < data.length; idx++) {
            data[idx] = table.getModel().getValueAt(table.getSelectedRow(), idx);
        }

        return data;
    }

    public void removeRow() {
        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
        Object[] columnNames = new Object[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (idx != table.getSelectedRow()) {
                    data[idx][i] = model.getValueAt(idx, i);
                }
            }
        }

        table.setModel(new DefaultTableModel(data, columnNames));
    }

    public boolean rowHasData(int testcol) {
        if (table.getModel().getRowCount() > 0) {
            for (int i = 0; i < table.getModel().getColumnCount(); i++) {
                if (table.getModel().getValueAt(table.getSelectedRow(), testcol) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkID() {
        return noerror;
    }
}
//~ Formatted by Jindent --- http://www.jindent.com

