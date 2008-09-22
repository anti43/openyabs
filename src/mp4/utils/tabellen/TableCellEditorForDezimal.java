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

import java.awt.Color;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author anti43
 */
public class TableCellEditorForDezimal extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;

    private NumberFormat format = new java.text.DecimalFormat("#,##0.00;(#,##0.00)");

    public TableCellEditorForDezimal(final JFormattedTextField tf) {
        super(tf);
        
        tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBorder(null);

        delegate = new EditorDelegate() {

            @Override
            public void setValue(Object param) {
                Double _value = (Double) param;
                if (_value == null) {
                    tf.setValue(format.format(0.0));
                } else {
                    double _d = _value.doubleValue();
                    String _format = format.format(_d);
                    tf.setValue(_format);
                }
            }

            @Override
            public Object getCellEditorValue() {
                try {
                    String _field = tf.getText();
                    Number _number = format.parse(_field);
                    double _parsed = _number.doubleValue();
                    Double d = new Double(_parsed);
                    tf.setBackground(Color.white);
                    return d;
                } catch (ParseException e) {
                    tf.setBackground(Color.red);
                    return new Double(0.0);
                }
            }
        };


    }
}


    
    
