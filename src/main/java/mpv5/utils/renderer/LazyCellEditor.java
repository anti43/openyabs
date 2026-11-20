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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * A text cell editor which starts empty when editing is started with the
 * keyboard (typing replaces the value). If nothing is typed (editing started by
 * a key the table does not use, e.g. Ctrl+S, or by the program) the value of
 * the cell is kept when editing stops.
 */
public class LazyCellEditor extends DefaultCellEditor {

    /**
     * true if the text was changed by the user since editing started
     */
    protected boolean typed;

    /**
     * Sets {@link #typed} when the text of the field changes; reset it after
     * setting the text of the editor
     *
     * @param tf
     */
    protected final void trackTyping(JTextComponent tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                typed = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                typed = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    public LazyCellEditor(JComboBox c) {
        super(c);
    }

    public LazyCellEditor(final JTextField tf) {
        super(tf);
        super.setClickCountToStart(1);
        trackTyping(tf);
        delegate = new EditorDelegate() {

            boolean isMousePressed = false;
            /**
             * the value of the cell: kept if nothing is typed
             */
            Object original;

            @Override
            public void setValue(Object value) {
                original = value;
                if(isMousePressed && value != null){
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        tf.selectAll();
                    }
                });
                tf.setText(value.toString());
                } else {
                tf.setText(""); }
                typed = false;
            }

            @Override
            public Object getCellEditorValue() {
                if (!typed) {
                    return original;
                }
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
