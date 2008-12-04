/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.handling;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import mpv5.ui.beans.LabeledTextField;

/**
 *
 * @author Administrator
 */
public class DataInjection {

    public DataInjection(javax.swing.JPanel panel, DatabaseObject data) {
        inject(panel, data);
    }

    private void inject(JPanel panel, DatabaseObject data) {
        ArrayList<Method> dat = data.getVars();
        for (int method = 0; method < dat.size(); method++) {
            for (int component = 0; component < panel.getComponents().length; component++) {
                if (dat.get(method).getName().toLowerCase().endsWith(panel.getComponents()[component].getName().toLowerCase())) {
                    try {
                        setValue(panel.getComponents()[component], dat.get(method), data);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(DataInjection.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(DataInjection.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(DataInjection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void setValue(Component component, Method get, DatabaseObject data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object d = get.invoke(data, (Object[]) null);
        if (component.getClass().isInstance(new JRadioButton()) && get.getReturnType().isInstance(boolean.class)) {
            ((JRadioButton) component).setSelected(((Boolean) d));
        } else if (component.getClass().isInstance(new JCheckBox()) && get.getReturnType().isInstance(boolean.class)) {
            ((JCheckBox) component).setSelected(((Boolean) d));
        } else if (component.getClass().isInstance(new JTextField()) && get.getReturnType().isInstance(String.class)) {
            ((JTextField) component).setText((String.valueOf(d)));
        } else if (component.getClass().isInstance(new LabeledTextField()) && get.getReturnType().isInstance(String.class)) {
            ((LabeledTextField) component).set_Text((String.valueOf(d)));
        } else if (component.getClass().isInstance(new JTextArea()) && get.getReturnType().isInstance(String.class)) {
            ((JTextArea) component).setText((String.valueOf(d)));
        } else if (component.getClass().isInstance(new JEditorPane()) && get.getReturnType().isInstance(String.class)) {
            ((JEditorPane) component).setText((String.valueOf(d)));
        }
    }
}
