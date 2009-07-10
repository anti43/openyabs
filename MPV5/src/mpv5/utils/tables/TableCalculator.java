/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.tables;

import java.text.NumberFormat;
import javax.swing.JTable;
import mpv5.logging.Log;
import mpv5.utils.numberformat.FormatNumber;

/**
 * This class provides auto calculation functions for table cell values (Double.class required!)
 */
public class TableCalculator implements Runnable {

    public static NumberFormat DECIMALFORMAT = FormatNumber.getDefaultDecimalFormat();
    private final JTable table;
    private final int[] columnsToCalculate;
    private final int[] targetColumns;
    private final int action;
    /**
     * <b>+</b>
     */
    public static final int ACTION_SUM = 0;
    /**
     * <b>/</b>
     */
    public static final int ACTION_DIVIDE = 1;
    /**
     * <b>-</b>
     */
    public static final int ACTION_SUBSTRACT = 2;
    /**
     * <b>*</b>
     */
    public static final int ACTION_MULTIPLY = 3;

    /**
     *
     * @param table
     * @param columnsToCalculate
     * @param targetColumns
     * @param action
     */
    public TableCalculator(JTable table, int[] columnsToCalculate, int[] targetColumns, int action) {
        super();
        this.table = table;
        this.columnsToCalculate = columnsToCalculate;
        this.targetColumns = targetColumns;
        this.action = action;
    }

    /**
     * 
     * @param row
     * @return 
     */
    public double calculate(int row) {

        Double val = 0d;
        try {
            switch (action) {
                case ACTION_SUM:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(j, row) != null) {
                            val += Double.valueOf(table.getValueAt(row,j).toString());
                        }
                    }
                    break;
                case ACTION_SUBSTRACT:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(j, row) != null) {
                            val -= Double.valueOf(table.getValueAt(row,j).toString());
                        }
                    }
                    break;
                case ACTION_DIVIDE:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(j, row) != null) {
                            val = val / Double.valueOf(table.getValueAt(row,j).toString());
                        }
                    }
                    break;
                case ACTION_MULTIPLY:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(j, row) != null) {
                            val = val * Double.valueOf(table.getValueAt(row,j).toString());
                        }
                    }
                    break;
            }

            for (int i = 0; i < targetColumns.length; i++) {
                int j = targetColumns[i];
                Log.Debug(this, "Setting value " + val + " at " + row +"@" + j);
                table.setValueAt(val, row, j);
            }

        } catch (NumberFormatException numberFormatException) {
            Log.Debug(this, numberFormatException.getMessage());
        }
        return val;
    }

    @Override
    public void run() {
        while (table.isShowing()) {
            int rc = table.getRowCount();
            for (int i = 0; i < rc; i++) {
                calculate(i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Log.Debug(ex);
                }
            }
        }
    }

    /**
     * Start calculating
     */
    public void start() {
        new Thread(this).start();
    }
}
