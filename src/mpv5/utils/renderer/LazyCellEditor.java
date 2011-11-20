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
package mpv5.utils.renderer;

import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 */
public class LazyCellEditor extends DefaultCellEditor {

    public LazyCellEditor(JComboBox c) {
        super(c);
    }

    public LazyCellEditor(final JTextField tf) {
        super(tf);
        super.setClickCountToStart(1);
        delegate = new EditorDelegate() {

            boolean isMousePressed = false;

            @Override
            public void setValue(Object value) {
                if(isMousePressed && value != null){
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        tf.selectAll();
                    }
                });
                tf.setText(value.toString());
                } else {
                tf.setText(""); }
            }

            @Override
            public Object getCellEditorValue() {
                return tf.getText();
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

    public boolean stopCellEditingSilent() {
        return true;
    }
}
