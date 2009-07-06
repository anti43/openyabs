/*
 * 
This file is part of MP.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mpv5.utils.tables;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * 
 */
public class Selection {

    private final int id;
    private JTable table;
    private int removed = 0;
    private final int r;
    private boolean noerror;

    public Selection(JTable table) {
        this.table = table;
        r=table.getSelectedRow();
        id = Integer.valueOf(String.valueOf(table.getValueAt(r, 0)));
        noerror = true;
        if (table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
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
