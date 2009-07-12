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
    private final int[] percentageColumns;
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
    private boolean onScreen = true;

    /**
     *
     * @param table
     * @param columnsToCalculate
     * @param targetColumns
     * @param percentageColumns
     * @param action
     */
    public TableCalculator(JTable table, int[] columnsToCalculate, int[] targetColumns, int[] percentageColumns, int action) {
        super();
        this.table = table;
        this.columnsToCalculate = columnsToCalculate;
        this.targetColumns = targetColumns;
        this.percentageColumns = percentageColumns;
        this.action = action;
    }

    /**
     * 
     * @param row
     * @return 
     */
    public synchronized double calculate(int row) {

        Double val = 0d;

        try {
            switch (action) {
                case ACTION_SUM:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(row, j) != null) {
                            if (table.getValueAt(j, row) != null) {
                                val += Double.valueOf(table.getValueAt(row, j).toString());
                            }
                        }
                    }
                    break;
                case ACTION_SUBSTRACT:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(row, j) != null) {
                            if (table.getValueAt(j, row) != null) {
                                val -= Double.valueOf(table.getValueAt(row, j).toString());
                            }
                        }
                    }
                    break;
                case ACTION_DIVIDE:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(row, j) != null) {
                            boolean percentagec = false;
                            for (int k = 0; k < percentageColumns.length; k++) {
                                int l = percentageColumns[k];
                                if (l == j) {
                                    percentagec = true;
                                }
                            }
                            if (!percentagec) {
                                if (i == 0) {
                                    val = Double.valueOf(table.getValueAt(row, j).toString());
                                } else {
                                    val = val / Double.valueOf(table.getValueAt(row, j).toString());
                                }
                            } else {
                                if (i == 0) {
                                    val = ((Double.valueOf(table.getValueAt(row, j).toString()) / 100) + 1);
                                } else {
                                    val = val / ((Double.valueOf(table.getValueAt(row, j).toString()) / 100) + 1);
                                }
                            }
                        }
                    }
                    break;
                case ACTION_MULTIPLY:
                    for (int i = 0; i < columnsToCalculate.length; i++) {
                        int j = columnsToCalculate[i];
                        if (table.getValueAt(row, j) != null) {
                            boolean percentagec = false;
                            for (int k = 0; k < percentageColumns.length; k++) {
                                int l = percentageColumns[k];
                                if (l == j) {
                                    percentagec = true;
                                }
                            }
                            if (!percentagec) {
                                if (i == 0) {
                                    val = Double.valueOf(table.getValueAt(row, j).toString());
                                } else {
                                    val = val * Double.valueOf(table.getValueAt(row, j).toString());
                                }
                            } else {
                                if (i == 0) {
                                    val = ((Double.valueOf(table.getValueAt(row, j).toString()) / 100) + 1);
                                } else {
                                    val = val * ((Double.valueOf(table.getValueAt(row, j).toString()) / 100) + 1);

                                }
                            }
                        }
                    }
                    break;
            }

            for (int i = 0; i < targetColumns.length; i++) {
                int j = targetColumns[i];
                table.setValueAt(val, row, j);
            }

        } catch (NumberFormatException numberFormatException) {
            Log.Debug(this, numberFormatException.getMessage());
        }
        return val;
    }

    @Override
    public void run() {
        while (isUsed()) {
            while (table.isShowing()) {
                if (!table.isEditing()) {
                    calculateOnce();
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
//                        Log.Debug(this, "Row " + i + " : " + calculate(i));
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Log.Debug(ex);
            }
        }
    }

    /**
     * @return the onScreen
     */
    public boolean isUsed() {
        return onScreen;
    }

    /**
     * @param onScreen the onScreen to set
     */
    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
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
}
