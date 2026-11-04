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
package mpv5.utils.ui;

import java.awt.Color;
import java.util.function.Predicate;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.ui.beans.LabeledTextField;

/**
 * Live input check for text fields: while the (non empty) value is invalid,
 * the field is shown red and its tooltip says what is expected.
 */
public final class FieldCheck {

    private static final Color INVALID = new Color(255, 200, 200);

    private FieldCheck() {
    }

    /**
     * @param field
     * @param valid check for non empty values
     * @param tooltip shown while the value is valid or empty
     * @param invalidHint shown while the value is invalid
     */
    public static void attach(LabeledTextField field, final Predicate<String> valid, final String tooltip,
            final String invalidHint) {
        final JTextField text = field.getTextField();
        final Color normal = text.getBackground();
        text.setToolTipText(tooltip);
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                String v = text.getText();
                boolean ok = v == null || v.trim().isEmpty() || valid.test(v);
                text.setBackground(ok ? normal : INVALID);
                text.setToolTipText(ok ? tooltip : invalidHint);
            }
        };
        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update.run();
            }
        });
        update.run();
    }
}
