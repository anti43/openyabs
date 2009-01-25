/*
 * 
 * 
 */
package mpv5.utils.ui;

import javax.swing.*;
import mpv5.ui.beans.LabeledTextField;

/**
 *
 * @author anti43
 */
public class PanelUtils {

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

    /**
     * Clears the text of the given components'
     * JTextfieds, LabeledTextFields, JEditorpanes and JTextareas
     * @param panel
     */
    public static void clearText(JPanel panel) {
        JTextField jt = null;
        Object p;
        JEditorPane ja = null;
        JTextArea je = null;
        LabeledTextField le = null;


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
                if (p.getClass().isInstance(new LabeledTextField())) {
                    le = (LabeledTextField) panel.getComponents()[i];
                    le.set_Text("");
                }
            } catch (Exception exception) {
            }
        }
    }

    /**
     * Enables/disables the subcomponents of a panel entirely (not the panel itself!)
     * @param component
     * @param state True means enabled, false disabled
     */
    public static void enableSubComponents(JComponent component, boolean state) {
        for (int i = 0; i < component.getComponents().length; i++) {
            component.getComponents()[i].setEnabled(state);
        }
    }
}
