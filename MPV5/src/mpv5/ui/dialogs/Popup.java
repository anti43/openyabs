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
package mpv5.ui.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import mpv5.globals.Messages;

/**
 *
 * @author anti
 */
public class Popup {

    public static String WARN = Messages.WARNING;
    public static String ERROR = Messages.ERROR_OCCURED;
    public static String NOTICE = Messages.NOTICE;
    public static String GENERAL_ERROR = Messages.ERROR_OCCURED;
    public static Component identifier = null;

    /**
     * A Y_N_dialog
     * @param text
     * @return
     */
    public static boolean Y_N_dialog(String text) {
        if (JOptionPane.showConfirmDialog(identifier, prepareText(text), Messages.ARE_YOU_SURE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new javax.swing.ImageIcon(new Popup().getClass().getResource("/mpv5/resources/images/32/warning.png"))) == JOptionPane.YES_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows a notice
     * @param parent
     * @param text
     */
    public static void notice(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, prepareText(text), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a warning
     * @param parent
     * @param text
     */
    public static void warn(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, prepareText(text), Popup.WARN, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows an error
     * @param parent
     * @param text
     */
    public static void error(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, prepareText(text), Popup.ERROR, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A convenience method to show a notice popup with
     * the mainframe as parent
     * @param text
     */
    public static void warn(String text) {
        JOptionPane.showMessageDialog(identifier, prepareText(text), Popup.WARN, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A convenience method to show a notice popup with
     * the mainframe as parent
     * @param text
     */
    public static void notice(String text) {
        JOptionPane.showMessageDialog(identifier, prepareText(text), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A convenience method to show an error popup with
     * the mainframe as parent
     * @param x
     */
    public static void error(Exception x) {
        error(identifier, Messages.ERROR_OCCURED + "\n" + x.getMessage());
    }

    private static Object prepareText(String s) {
        JTextArea text = new JTextArea(s);
        JScrollPane scroll = new JScrollPane(text);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setBorder(new EmptyBorder(1, 1, 1, 1));
        text.setBackground(JOptionPane.getRootFrame().getBackground());
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.setPreferredSize(new Dimension(300, 80));
        return scroll;
    }
//
//    /**
//     * A popup..
//     * @param text
//     */
//    public Popup(String text) {
//        JOptionPane.showMessageDialog(identifier, prepareText(text), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    /**
//     *  A popup..
//     * @param text
//     * @param label
//     */
//    public Popup(String text, String label) {
//        JOptionPane.showMessageDialog(identifier, prepareText(text), label, JOptionPane.INFORMATION_MESSAGE);
//    }

    private Popup() {
    }
}
