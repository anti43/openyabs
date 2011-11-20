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

    /**
     *
     * @param tf
     */
    public TableCellEditorForDezimal(final JFormattedTextField tf) {
        super(tf);
        super.setClickCountToStart(1);
        tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setBorder(null);
        delegate = new EditorDelegate() {
            boolean isMousePressed = false;

            @Override
            public void setValue(Object param) {
                if (isMousePressed
                        && param != null && (param.getClass() == Double.class || param.getClass() == BigDecimal.class)) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            tf.selectAll();
                        }
                    });
                    try {
                        tf.setText(FormatNumber.formatDezimal((Number) param));
                    } catch (Exception e) {
                        try {
                            tf.setText(FormatNumber.formatDezimal(new BigDecimal(String.valueOf(param))));
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
                    Number _number = FormatNumber.parseDezimal(_field);

                    if (_number != null) {
                        double _parsed = _number.doubleValue();
                        Double d = new Double(_parsed);
//                        tf.setBackground(Color.white);
                        return d;
                    } else {
//                        tf.setBackground(Color.white);
                        return new Double(0.0);
                    }
                } catch (Exception e) {
                    Log.Debug(this, e.getMessage());
//                    tf.setBackground(Color.red);
                    return new Double(0.0);
                }
            }
            
            @Override
            public boolean isCellEditable(EventObject anEvent) {
	    if (anEvent instanceof MouseEvent) { 
                isMousePressed = true;
		return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
	    }
            isMousePressed = false;
	    return true;
            }
        };


    }
}


    
    
