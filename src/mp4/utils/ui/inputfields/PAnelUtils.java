/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mp4.utils.ui.inputfields;

import javax.swing.*;

/**
 *
 * @author anti43
 */
public class PAnelUtils {

        /**
     * 
     * @param jTextField
     * @param length
     */
    public static void cut(JTextField jTextField, int length) {
        try {
            jTextField.setText(jTextField.getText().substring(0, length));

        } catch (Exception exception) {
        }

    }
    
    public static void clearText(JPanel panel) {
        JTextField jt = null;
        Object p;
        JEditorPane ja = null;
        JTextArea je = null;

        for (int i = 0; i < panel.getComponents().length; i++) {

            try {
                p = (java.lang.Object) panel.getComponents()[i];

                if (p.getClass().isInstance(new JTextField())) {

                    jt = (JTextField) panel.getComponents()[i];
                    jt.setText("");
                }


                if (p.getClass().isInstance(new JEditorPane())) {

                    ja = (JEditorPane) panel.getComponents()[i];
                    ja.setText(null);
                }


                if (p.getClass().isInstance(new JTextArea())) {

                    je = (JTextArea) panel.getComponents()[i];
                    je.setText("");
                }
            } catch (Exception exception) {
            }




        }
    }
}
