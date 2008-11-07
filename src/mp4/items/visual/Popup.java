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
package mp4.items.visual;

import java.awt.Component;
import javax.swing.JOptionPane;
import mp4.frames.mainframe;
import mp4.logs.*;
import mp4.panels.misc.NumberFormatEditor;
import mp4.utils.export.pdf.PDF_Rechnung;
import mp4.utils.text.TextFormat;

/**
 *
 * @author anti
 */
public class Popup {

    public static String WARN = "Achtung!";
    public static String ERROR = "Fehler";
    public static String NOTICE = "Hinweis";
    public static String GENERAL_ERROR = "Es ist ein Fehler aufgetreten.";
    public static Component identifier = null;

    /**
     * 
     * @param text
     * @return
     */
    public static boolean Y_N_dialog(String text) {
        if (JOptionPane.showConfirmDialog(identifier, text, "Sind Sie sicher?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE , new javax.swing.ImageIcon(new Popup().getClass().getResource("/bilder/medium/messagebox_warning.png"))) == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    public static void notice(Component parent, String text) {
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(parent, text, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void notice(Object initiator, String text) {
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(mainframe.identifier, text, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void warn(String text, String label) {

        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, label, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void error(String text, String label) {

        Log.Debug(Popup.class,text, true);
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, label, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     * @param label
     */
    public static void notice(String text, String label) {

        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, label, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A popup..
     * @param text
     */
    public static void notice(String text) {
        
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    public Popup(String text) {
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     *  A popup..
     * @param text
     * @param label
     */
    public Popup(String text, String label) {
        text = TextFormat.maxLineLength(text, 120);
        JOptionPane.showMessageDialog(identifier, text, label, JOptionPane.INFORMATION_MESSAGE);
    }

    private Popup() {
    }
}
