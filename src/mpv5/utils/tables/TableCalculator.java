
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

//~--- non-JDK imports --------------------------------------------------------

import mpv5.logging.Log;

import mpv5.ui.beans.LabeledTextField;

import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.LazyCellEditor;

//~--- JDK imports ------------------------------------------------------------

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
 * @Deprecated 
 * use <link>DynamicTableCalculator</link> DynamicArithmetic for being more flexible
 */
@Deprecated
public class TableCalculator implements Runnable {

    /**
     * <b>/</b>
     */
    public static final int ACTION_DIVIDE = 1;

    /**
     * <b>*</b>
     */
    public static final int ACTION_MULTIPLY = 3;

    /**
     * <b>-</b>
     */
    public static final int ACTION_SUBSTRACT = 2;

    /**
     * <b>+</b>
     */
    public static final int              ACTION_SUM = 0;
    private boolean                      onScreen   = true;
    private HashMap<Integer, JComponent> sumcols    = new HashMap<Integer, JComponent>();
    private final int                    action;
    private final int[]                  columnsToCalculate;
    private final int[]                  percentageColumns;
    private int[]                        sumColumn;

//  public static NumberFormat DECIMALFORMAT = FormatNumber.getDefaultDecimalFormat();
    private final JTable table;
    private final int[]  targetColumns;

    /**
     *
     * @param table the table which we want to calculate
     * @param columnsToCalculate the columns to calculate
     * @param targetColumns the columns which old the result of a row
     * @param percentageColumns The columsn which shall be treatened as percent values
     * @param action The action to take
     * @param sumColumn The columns to sum up vertically (+)
     */
    public TableCalculator(JTable table, int[] columnsToCalculate, int[] targetColumns, int[] percentageColumns,
                           int action, int[] sumColumn) {
        super();
        this.table              = table;
        this.columnsToCalculate = columnsToCalculate;
        this.targetColumns      = targetColumns;
        this.percentageColumns  = percentageColumns;
        this.action             = action;
        this.sumColumn          = sumColumn;
    }

    /**
     *
     * @param row
     * @return
     */
    public synchronized BigDecimal calculate(int row) {
        BigDecimal val = BigDecimal.ZERO;

        try {
            switch (action) {
            case ACTION_SUM :
                for (int i = 0; i < columnsToCalculate.length; i++) {
                    int j = columnsToCalculate[i];

                    if (table.getModel().getValueAt(row, j) != null) {
                        val.add(BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row, j).toString())));
                    }
                }

                break;

            case ACTION_SUBSTRACT :
                for (int i = 0; i < columnsToCalculate.length; i++) {
                    int j = columnsToCalculate[i];

                    if (table.getModel().getValueAt(row, j) != null) {
                        if (i == 0) {
                            val = BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row, j).toString()));
                        } else {
                            val = val.subtract(BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row,
                                    j).toString())));
                        }
                    }
                }

                break;

            case ACTION_DIVIDE :
                for (int i = 0; i < columnsToCalculate.length; i++) {
                    int j = columnsToCalculate[i];

                    if (table.getModel().getValueAt(row, j) != null) {
                        boolean percentagec = false;

                        for (int k = 0; k < percentageColumns.length; k++) {
                            int l = percentageColumns[k];

                            if (l == j) {
                                percentagec = true;
                            }
                        }

                        if (!percentagec) {
                            if (i == 0) {
                                val = BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()));
                            } else {
                                val = val.divide(BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString())), 2, BigDecimal.ROUND_HALF_UP);
                            }
                        } else {
                            if (i == 0) {
                                val = BigDecimal.valueOf(((Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()) / 100) + 1d));
                            } else {
                                val = val.divide(BigDecimal.valueOf((Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()) / 100) + 1), 2, BigDecimal.ROUND_HALF_UP);
                            }
                        }
                    }
                }

                break;

            case ACTION_MULTIPLY :
                for (int i = 0; i < columnsToCalculate.length; i++) {
                    int j = columnsToCalculate[i];

                    if (table.getModel().getValueAt(row, j) != null) {
                        boolean percentagec = false;

                        for (int k = 0; k < percentageColumns.length; k++) {
                            int l = percentageColumns[k];

                            if (l == j) {
                                percentagec = true;
                            }
                        }

                        if (!percentagec) {
                            if (i == 0) {
                                val = BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()));
                            } else {
                                val = val.multiply(BigDecimal.valueOf(Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString())));
                            }
                        } else {
                            if (i == 0) {
                                val = BigDecimal.valueOf(((Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()) / 100) + 1));
                            } else {
                                val = val.multiply(BigDecimal.valueOf((Double.valueOf(table.getModel().getValueAt(row,
                                        j).toString()) / 100) + 1));
                            }
                        }
                    }
                }

                break;
            }

            for (int i = 0; i < targetColumns.length; i++) {
                int j = targetColumns[i];

                ((MPTableModel) table.getModel()).setValueAt(val.doubleValue(), row, j, true);
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

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Log.Debug(ex);
                    }
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

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(LabeledTextField value, int sumColumn) {
        sumcols.put(new Integer(sumColumn), value.getTextField());
    }

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(JTextField value, int sumColumn) {
        sumcols.put(new Integer(sumColumn), value);
    }

    /**
     * Define where the values are displayed
     * @param value
     * @param sumColumn
     */
    public void addLabel(JLabel value, int sumColumn) {
        sumcols.put(new Integer(sumColumn), value);
    }

    private void sumUp() {
        TableCellEditor e = table.getCellEditor();

        if ((e != null) && (e instanceof LazyCellEditor)) {
            ((LazyCellEditor) e).stopCellEditingSilent();
        }

        for (int i = 0; i < sumColumn.length; i++) {
            int k = sumColumn[i];

            if (sumcols.containsKey(new Integer(k))) {
                Double ovalue = 0d;

                for (int j = 0; j < table.getRowCount(); j++) {
                    if (table.getModel().getValueAt(j, k) != null) {
                        ovalue += Double.valueOf(table.getModel().getValueAt(j, k).toString());
                    }
                }

                JComponent t = sumcols.get(new Integer(k));

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


//~ Formatted by Jindent --- http://www.jindent.com
