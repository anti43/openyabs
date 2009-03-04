/*
 *  This file is part of MP by anti43 /GPL.
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
package mpv5.utils.ui;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import mpv5.ui.beans.LabeledTextField;

/**
 *
 * @author anti
 */
public class TextFieldUtils {

    public static void blinkerGrey(LabeledTextField field) {
        new blinker(field.getTextField(), 2, Color.GRAY).execute();
    }

    /**
     * Lets a text field blink
     * @param field
     */
    public static void blinkerRed(JTextField field) {
        new blinker(field, 2, Color.RED).execute();
    }

    /** 
     * Lets a text field blink
     * @param lfield
     */
    public static void blinkerRed(LabeledTextField lfield) {
        new blinker(lfield.getTextField(), 2, Color.RED).execute();
    }

    private static class blinker extends SwingWorker<Void, Void> {
        private JTextField filed;
        private int count;
        private Color color;

        private blinker(JTextField field, int i, Color col) {
            filed = field;
            count = i;
            color = col;
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i < count; i++) {
                filed.setBackground(color);
                Thread.sleep(550);
                filed.setBackground(Color.LIGHT_GRAY);
                Thread.sleep(550);
            }
            return null;
        }
    }
}
