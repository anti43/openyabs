
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
package mpv5.ui.misc;

//~--- non-JDK imports --------------------------------------------------------
import mpv5.ui.beans.LabeledTextField;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Delegates to {@link mpv5.utils.ui.TextFieldUtils}.
 */
public class TextFieldUtils {

    public static void blinkerGrey(LabeledTextField field) {
        mpv5.utils.ui.TextFieldUtils.blinkerGrey(field);
    }

    /**
     * Lets a text field blink
     * @param field
     */
    public static void blinkerRed(JTextField field) {
        mpv5.utils.ui.TextFieldUtils.blinkerRed(field);
    }

    /**
     * Lets a text field blink
     * @param lfield
     */
    public static void blinkerRed(LabeledTextField lfield) {
        mpv5.utils.ui.TextFieldUtils.blinkerRed(lfield);
    }

    /**
     *
     * @param component
     * @param color
     */
    public static void blink(JComponent component, Color color) {
        mpv5.utils.ui.TextFieldUtils.blink(component, color);
    }

    public static void blinker(JTextField jTextField1, Color color) {
        mpv5.utils.ui.TextFieldUtils.blinker(jTextField1, color);
    }
}
//~ Formatted by Jindent --- http://www.jindent.com

