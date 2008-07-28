/*
 * 
This file is part of MP by anti43 /GPL.

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
package mp4.utils.tabellen;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import mp3.classes.utils.Log;
import mp4.utils.zahlen.FormatNumber;

/**
 *
 * @author Anti43
 */
public class DataModelUtils {

    public static void addToTable(JTable table, Object[] row) {

        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount() + 1][row.length];
        Object[] columnNames = new Object[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < row.length; i++) {
                data[idx][i] = model.getValueAt(idx, i);
            }
        }

        data[model.getRowCount()] = row;
        table.setModel(new DefaultTableModel(data, columnNames));
    }

    public static void addToTable(JTable table, Object[][] rows) {

        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount() + rows.length][rows[0].length];
        Object[] columnNames = new Object[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < rows[0].length; i++) {
                data[idx][i] = model.getValueAt(idx, i);
            }
        }
        for (int i = 0; i < rows.length; i++) {
            data[model.getRowCount() + i] = rows[i];
        }
        table.setModel(new DefaultTableModel(data, columnNames));
    }

    /**
     * 
     * @param table
     * @param countcol
     * @param nettocol
     * @param taxcol
     * @return
     */
    public static CalculatedTableValues calculateTableCols(JTable table, int countcol, int nettocol, int taxcol) {

        TableModel m = table.getModel();
        Double bruttobetrag = 0d;
        Double nettobetrag = 0d;
        Double allovertax = 0d;
        boolean error = false;

        for (int i = 0; i < m.getRowCount(); i++) {
            if (m.getValueAt(i, nettocol) != null && m.getValueAt(i, countcol) != null && m.getValueAt(i, taxcol) != null) {
                try {
                    bruttobetrag = bruttobetrag + (FormatNumber.parseDezimal(m.getValueAt(i, nettocol).toString()) * (Double.valueOf((FormatNumber.parseDezimal(m.getValueAt(i, taxcol).toString()) / 100) + 1)));
                    nettobetrag = nettobetrag + (FormatNumber.parseDezimal(m.getValueAt(i, nettocol).toString()));
                    allovertax = allovertax + ((FormatNumber.parseDezimal(m.getValueAt(i, taxcol).toString())) + 100);
                } catch (Exception exception) {
                    Log.Debug(exception);
                    error = true;
                }
            }
        }

        if (nettobetrag > 0) {
            allovertax = (allovertax / nettobetrag);
        } else {
            allovertax = 0d;
        }

        if (!error) {
            return new CalculatedTableValues(bruttobetrag, nettobetrag, allovertax);
        } else {
            return null;
        }
    }

    public static Object[][] changeToClassValue(Object[][] prods, Class aClass, int[] cols) {

        Object[][] data = new Object[prods.length][prods[0].length];

        for (int idx = 0; idx < prods.length; idx++) {
            for (int k = 0; k < cols.length; k++) {
                if (aClass.getName().matches("java.lang.Boolean")) {
                    if (prods[idx][cols[k]].equals("1")) {
                        data[idx][cols[k]] = true;
                    } else {
                        data[idx][cols[k]] = false;
                    }
                }
            }

            for (int h = 0; h < prods[0].length; h++) {
                if (data[idx][h] == null) {
                    data[idx][h] = prods[idx][h];
                }
            }
        }
        return data;
    }

    public static Object[][] tableModelToArray(JTable table) {

        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                data[idx][i] = model.getValueAt(idx, i);
            }
        }

        return data;
    }

    public static void removeSelectedRowFromTable(JTable table) {
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

    public static Object[][] reverseArray(Object[][] array) {
        //Reverse order
           int i = 0, j = array.length - 1;
        while (i < j) {
            Object[] h = array[i];
            array[i] = array[j];
            array[j] = h;
            i++;
            j--;
        }
        return array;
    }

    public static void addRowToTable(JTable table) {
        Object[] o = new Object[table.getModel().getColumnCount()];
        for (int idx = 0; idx < o.length; idx++) {
            o[idx] = null;
        }
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.addRow(o);
    }
}
