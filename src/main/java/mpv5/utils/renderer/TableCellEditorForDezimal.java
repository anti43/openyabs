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

import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.EventObject;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import mpv5.logging.Log;
import mpv5.utils.numberformat.FormatNumber;

/**
 *
 * 
 */
public class TableCellEditorForDezimal extends LazyCellEditor {

    private NumberFormat columnFormat;

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param tf
     */
    public TableCellEditorForDezimal(final JFormattedTextField tf) {
        this(tf, null);
    }

    /**
     *
     * @param tf
     * @param format  
     */
    public TableCellEditorForDezimal(final JFormattedTextField tf, final NumberFormat format) {
        super(tf);
        this.columnFormat = format;
        super.setClickCountToStart(1);
        tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setBorder(null);
        delegate = new EditorDelegate() {

            boolean isMousePressed = false;
            /**
             * the value before editing: kept if the input is not a number
             */
            Object original;

            @Override
            public void setValue(Object param) {
                original = param;
                if (isMousePressed
                        && param != null && (param.getClass() == Double.class || param.getClass() == BigDecimal.class)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            tf.selectAll();
                        }
                    });
                    try {
                        //all decimals: a price of 12.345 must not become 12.35 by clicking into it
                        tf.setText(editFormat().format(new BigDecimal(String.valueOf(param))));
                    } catch (Exception e) {
                        try {
                            param = new BigDecimal(String.valueOf(param));
                            tf.setText(format == null ? FormatNumber.formatDezimal((Number) param) : format.format((Number) param));
                        } catch (Exception ex) {
                            tf.setText(String.valueOf(param));
                        }
                    }
                } else {
                    tf.setText("");
                }

            }

            @Override
            public Object getCellEditorValue() {
                try {
                    String _field = tf.getText();
                    if (_field == null || _field.trim().length() == 0) {
                        return BigDecimal.ZERO;
                    }
                    Number _number;
                    if (format instanceof java.text.DecimalFormat) {
                        //exact, not via double
                        java.text.DecimalFormat f = (java.text.DecimalFormat) format.clone();
                        f.setParseBigDecimal(true);
                        _number = f.parse(_field.trim());
                    } else if (format != null) {
                        _number = format.parse(_field.trim());
                    } else {
                        _number = FormatNumber.parseDezimal(_field);
                    }
                    if (_number != null) {
                        return _number instanceof BigDecimal ? _number : new BigDecimal(_number.toString());
                    }
                } catch (ParseException e) {
                    Log.Debug(this, e);
                } catch (NumberFormatException e) {
                    Log.Debug(this, e);
                }
                //not a number: keep the value it had
                return original instanceof Number ? FormatNumber.getBigDecimal(original) : BigDecimal.ZERO;
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    isMousePressed = true;
                    return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
                }
                isMousePressed = false;
                return true;
            }
        };
    }

    /**
     * @return the format of the column, but with all decimals of the value
     */
    private NumberFormat editFormat() {
        NumberFormat f = columnFormat == null ? FormatNumber.getDefaultDecimalFormat() : (NumberFormat) columnFormat.clone();
        f.setMaximumFractionDigits(Math.max(f.getMaximumFractionDigits(), 10));
        f.setGroupingUsed(false);
        return f;
    }
}
