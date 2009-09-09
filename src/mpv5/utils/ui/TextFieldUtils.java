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
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import mpv5.ui.beans.LabeledTextField;


/**
 *
 *  
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

    /**
     *
     * @param component
     * @param color
     */
    public static void blink(Component component, Color color) {
        new blinker(component, 2, color).execute();
    }

    public static void blinker(JTextField jTextField1, Color color) {
        blink(jTextField1, color);
    }

    private static class blinker extends SwingWorker<Void, Void> {

        private Component fi;
        private int count;
        private Color color;

        private blinker(Component field, int i, Color col) {
            fi = field;
            count = i;
            color = col;
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i < count; i++) {
                fi.setBackground(color);
                fi.validate();
                Thread.sleep(550);
                fi.setBackground(Color.LIGHT_GRAY);
                Thread.sleep(550);
                fi.validate();
            }
            return null;
        }
    }
}
