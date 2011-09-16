
/*
*
*
 */
package mpv5.ui.misc;

//~--- non-JDK imports --------------------------------------------------------

import mpv5.ui.beans.LabeledTextField;

//~--- JDK imports ------------------------------------------------------------

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;


/**
 *
 *
 */
public class PanelUtils {

    /**
     * Cuts the text of a textfield to the given length
     * @param jTextField
     * @param length
     */
    public static void cut(JTextField jTextField, int length) {
        try {
            jTextField.setText(jTextField.getText().substring(0, length));
        } catch (Exception exception) {}
    }

    /**
     * Clears the text of the given components'
     * JTextfieds, LabeledTextFields, JEditorpanes and JTextareas
     * @param panel
     */
    public static void clearText(JPanel panel) {
        JTextField       jt = null;
        Object           p;
        JEditorPane      ja = null;
        JTextArea        je = null;
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
            } catch (Exception exception) {}
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

    public static void setTitle(JPanel aThis, String cname_) {
        if (aThis.getParent() instanceof JViewport || aThis.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = null;
            String title1 = cname_;
            //this->viewport->scrollpane->tabbedpane
            if (aThis.getParent().getParent().getParent() instanceof JTabbedPane) {
                jTabbedPane = (JTabbedPane) aThis.getParent().getParent().getParent();
            } else {
                try {
                    jTabbedPane = (JTabbedPane) aThis.getParent();
                } catch (Exception e) {
                    //Free floating window
                    ((JFrame) aThis.getRootPane().getParent()).setTitle(title1);
                }
            }
            if (jTabbedPane != null) {
                jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title1);
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
