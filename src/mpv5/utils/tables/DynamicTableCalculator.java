
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
package mpv5.utils.tables;

import mpv5.logging.Log;

import mpv5.ui.beans.LabeledTextField;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.LazyCellEditor;
import java.math.BigDecimal;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * This class provides auto calculation functions for table cell values (Double.class required!), works only with {@link MPTableModel}s!
 */
public class DynamicTableCalculator implements Runnable {

    private HashMap<Integer, JComponent> sumcols = new HashMap<Integer, JComponent>();
    private final JTable table;
    private final int[] targetColumns;
    private final String term;
    private final Double dZero = new Double("0");

    /**
     *
     * @param table the table which we want to calculate
     * @param targetColumns the columns which old the result of a row
     * @param term the Formula to calculate
     */
    public DynamicTableCalculator(JTable table, String term, int[] targetColumns) {
        super();
        this.table = table;
        this.targetColumns = targetColumns;
        this.term = term;
    }

    /**
     *
     * @param row
     * @return
     */
    public synchronized BigDecimal calculate(int row) {
        BigDecimal val = BigDecimal.ZERO;
        HashMap<Integer, BigDecimal> values = new HashMap<Integer, BigDecimal>();
        for (int i=0; i < table.getModel().getColumnCount(); i++) {
            Object rowVal=table.getModel().getValueAt(row, i);
            if (rowVal instanceof Number){
                values.put(i, FormatNumber.getBigDecimal(rowVal));
            }
        }
        try {
            DynamicArithmetic da = new DynamicArithmetic(term, values);
            val = da.result;
        } catch (ParseFormatException ex) {
            Log.Debug(this, ex);
        }

        for (int i = 0; i < targetColumns.length; i++) {
            int j = targetColumns[i];
            ((MPTableModel) table.getModel()).setValueAt(val.doubleValue(), row, j, true);
        }
        return val;
    }

    @Override
    public void run() {
        while (table.isShowing()) {
            if (!table.isEditing()) {
                calculateOnce();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Log.Debug(ex);
                }
            }
        }
    }

    /**
     * Start calculating, will run always while the table is showing on the screen
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     * Calculate once
     */
    public void calculateOnce() {
        for (int i = 0; i < table.getRowCount(); i++) {
            calculate(i);
        }

        sumUp();
    }

    /**
     * Checks whether the cell is not a target cell for this calculator
     * @param row
     * @param column
     * @return
     */
    public boolean isTargetCell(int row, int column) {
        for (int i = 0; i < targetColumns.length; i++) {
            int col = targetColumns[i];

            if (col == column) {
                return true;
            }
        }

        return false;
    }

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(LabeledTextField value, int sumColumn) {
        sumcols.put(sumColumn, value.getTextField());
    }

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(JTextField value, int sumColumn) {
        sumcols.put(sumColumn, value);
    }

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(JLabel value, int sumColumn) {
        sumcols.put(sumColumn, value);
    }

    private void sumUp() {
        TableCellEditor e = table.getCellEditor();

        if ((e != null) && (e instanceof LazyCellEditor)) {
            ((LazyCellEditor) e).stopCellEditingSilent();
        }

        for (int i = 0; i < targetColumns.length; i++) {
            int k = targetColumns[i];

            if (sumcols.containsKey(k)) {
                BigDecimal ovalue = BigDecimal.ZERO;
                for (int j = 0; j < table.getRowCount(); j++) {
                    Object rowVal=table.getModel().getValueAt(j, k);
                    if (rowVal != null) {
                        //ovalue += Double.valueOf(table.getModel().getValueAt(j, k).toString());
                        //ovalue.add((BigDecimal) rowVal);
                        //Log.Debug(this,ovalue+"+"+FormatNumber.getBigDecimal(rowVal));
                        ovalue=ovalue.add(FormatNumber.getBigDecimal(rowVal));
                        //Log.Debug(this,"="+ovalue);
                    }
                }
                JComponent t = sumcols.get(Integer.valueOf(k));
                //Log.Debug(this,ovalue);
                if (t instanceof JLabel) {
                    ((JLabel) t).setText(FormatNumber.formatDezimal(ovalue));
                } else if (t instanceof JTextField) {
                    ((JTextField) t).setText(FormatNumber.formatDezimal(ovalue));
                } else if (t instanceof JTextArea) {
                    ((JTextArea) t).setText(FormatNumber.formatDezimal(ovalue));
                } else if (t instanceof JEditorPane) {
                    ((JEditorPane) t).setText(FormatNumber.formatDezimal(ovalue));
                }
            }
        }
    }

    /**
     *
     * @return
     */
    public JTable getTable() {
        return table;
    }
}
