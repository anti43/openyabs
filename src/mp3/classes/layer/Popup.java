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
package mp3.classes.layer;

import javax.swing.JOptionPane;

/**
 *
 * @author anti
 */
public class Popup {

    public static String WARN = "Achtung!";
    public static String ERROR = "Fehler";
    public static String NOTICE = "Hinweis";
    public static String GENERAL_ERROR = "Es ist ein Fehler aufgetreten.";

    /**
     * 
     * @param text
     * @return
     */
    public static boolean Y_N_dialog(String text) {
        if (JOptionPane.showConfirmDialog(null, text, "Sind Sie sicher?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE , new javax.swing.ImageIcon(new Popup().getClass().getResource("/bilder/medium/messagebox_warning.png"))) == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void warn(String text, String label) {

        //new PopupView(text, label).setVisible(true);

        JOptionPane.showMessageDialog(null, text, label, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void error(String text, String label) {

        //new PopupView(text, label).setVisible(true);

        JOptionPane.showMessageDialog(null, text, label, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void notice(String text, String label) {

        //new PopupView(text, label).setVisible(true);

        JOptionPane.showMessageDialog(null, text, label, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     */
    public static void notice(String text) {

        //new PopupView(text, label).setVisible(true);

        JOptionPane.showMessageDialog(null, text, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    public Popup(String string) {
        JOptionPane.showMessageDialog(null, string, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *  A popup..
     * @param text
     * @param label
     */
    public Popup(String text, String label) {
        JOptionPane.showMessageDialog(null, text, label, JOptionPane.INFORMATION_MESSAGE);
    }

    private Popup() {
    }
}
