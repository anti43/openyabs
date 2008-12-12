/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.data;

import mpv5.db.common.DatabaseObject;
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
import mpv5.ui.panels.DataPanel;

/**
 *
 * @author Administrator
 */
public class DataHandler {

    public DataHandler(){}

    public DataHandler(javax.swing.JPanel target, DatabaseObject source) {
        inject(target, source);
    }

    public DataHandler(DatabaseObject target, javax.swing.JPanel source) {
        parse(source, target);
    }

  

    public DatabaseObject parse(JPanel[] dataPanel, DatabaseObject target) {

        for (int i = 0; i < dataPanel.length; i++) {
            JPanel source = dataPanel[i];
            ArrayList<Method> dat = target.setVars();
            for (int method = 0; method < dat.size(); method++) {
                for (int component = 0; component < source.getComponents().length; component++) {
                    
                    if (dat.get(method).getName().toLowerCase().endsWith(source.getComponents()[component].getName().toLowerCase())) {
                       
                        try {
                            getValue(source.getComponents()[component], dat.get(method), target);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        System.out.println(target.__getCName());
        return target;
    }

    private JPanel inject(JPanel target, DatabaseObject source) {
        ArrayList<Method> dat = source.setVars();
        for (int method = 0; method < dat.size(); method++) {
            for (int component = 0; component < target.getComponents().length; component++) {

                if (dat.get(method).getName().toLowerCase().endsWith(target.getComponents()[component].getName().toLowerCase())) {
                    try {
                        
                        setValue(target.getComponents()[component], dat.get(method), source);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }

        return target;
    }

    public DatabaseObject parse(JPanel source, DatabaseObject target) {
        ArrayList<Method> dat = target.getVars();
        for (int method = 0; method < dat.size(); method++) {
            for (int component = 0; component < source.getComponents().length; component++) {
              

                if (dat.get(method).getName().toLowerCase().endsWith(source.getComponents()[component].getName().toLowerCase())) {
                    try {
                        
                        getValue(source.getComponents()[component], dat.get(method), target);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex) {
                        Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return target;
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

    private void getValue(Component component, Method set, DatabaseObject data) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        
        if (component.getClass().isInstance(new JRadioButton()) && set.getParameterTypes().length==1 && set.getParameterTypes()[0].isInstance(boolean.class)) {
        set.invoke(data, ((JRadioButton) component).isSelected());
        } else if (component.getClass().isInstance(new JCheckBox()) && set.getParameterTypes().length==1  && set.getParameterTypes()[0].isInstance(boolean.class)) {
        set.invoke(data, ((JCheckBox) component).isSelected());
        } else if (component.getClass().getName().endsWith("TextField")) {
        set.invoke(data, ((LabeledTextField) component).get_Text());
//        }
//        else if (component.getClass().isInstance(new LabeledTextField()) && set.getParameterTypes().length==1  && set.getParameterTypes()[0].isInstance(String.class)) {
//        set.invoke(data, ((LabeledTextField) component).get_Text());
//        } else if (component.getClass().isInstance(new JTextArea())  && set.getParameterTypes().length==1 && set.getParameterTypes()[0].isInstance(String.class)) {
//        set.invoke(data, ((JTextArea) component).getText());
//        } else if (component.getClass().isInstance(new JEditorPane())  && set.getParameterTypes().length==1 && set.getParameterTypes()[0].isInstance(String.class)) {
//        set.invoke(data, ((JEditorPane) component).getText());
//
        } else {System.out.println(component.getClass() + " "+set.getParameterTypes()[0].getName()+" " +set.getName());}

    }
}
