/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LabeledTextField.java
 *
 * Created on 20.11.2008, 19:26:39
 */
package mpv5.ui.beans;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.objects.Contact;
import mpv5.handler.MPEnum;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.models.*;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;

/**
 *
 *  
 */
public class LabeledCombobox extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private String _text;
    private String _label;
    private Class clazz;
    public static boolean SEARCH_ON_ENTER = false;
    private Context context;
    private int sortmode = 0;

    /** Creates new form LabeledTextField */
    public LabeledCombobox() {
        initComponents();
        jComboBox1.getEditor().getEditorComponent().addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {

                if (SEARCH_ON_ENTER && e.getKeyCode() == KeyEvent.VK_ENTER && context != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            ComboBoxEditor cbField = jComboBox1.getEditor();
                            Object value = cbField.getItem();
//                            if(value instanceof MPComboBoxModelItem
                            jComboBox1.setSelectedItem(new MPComboBoxModelItem(-1, value.toString()));
                            Object[][] data = new DatabaseSearch(context, 50).getValuesFor("ids, cname", "cname", jComboBox1.getSelectedItem().toString(), true);
//                    if (data.length > 0) {
                            jComboBox1.setModel(MPComboBoxModelItem.toModel(MPComboBoxModelItem.toItems(data, true)));
                            if (data.length > 1) {
                                jComboBox1.setPopupVisible(true);
                            }
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
//                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        jComboBox1.setRenderer(new ComboBoxRendererForTooltip());
    }

    public JComboBox getComboBox() {
        return jComboBox1;
    }

    /**
     *
     * @return The model
     */
    public MPComboboxModel getModel() {
        return (MPComboboxModel) jComboBox1.getModel();
    }

    /**
     *
     * @param values
     */
    public void setModel(MPEnum[] values) {
        jComboBox1.setModel(MPComboBoxModelItem.toModel(values));
    }

    /**
     * 
     * @param values
     * @param compareMode
     */
    public void setModel(MPEnum[] values, int compareMode) {
        jComboBox1.setModel(MPComboBoxModelItem.toModel(values, compareMode));
    }

    /**
     * Uses enum.name() as ID, and enum.toString() as value.
     * @param values
     */
    public void setModel(Enum[] values) {
        String[][] val = new String[values.length][2];
        for (int i = 0; i < values.length; i++) {
            Enum enum1 = values[i];
            val[i][0] = enum1.name();
            val[i][1] = enum1.toString();
        }
        setModel(val);
    }

    /**
     * Delegates to setModel(MPComboBoxModelItem.toModel(data));
     * {id (hidden), value (shown in the list)}
     * @param data
     */
    public void setModel(Object[][] data) {
        jComboBox1.setModel(MPComboBoxModelItem.toModel(data));
    }

    /**
     *
     * @return An {@link MPComboBoxModelItem} or null if nothing is selected
     */
    public MPComboBoxModelItem getSelectedItem() {
        if (jComboBox1.getSelectedItem() instanceof MPComboBoxModelItem) {
            return (MPComboBoxModelItem) jComboBox1.getSelectedItem();
        } else {
            return null;
        }
    }

    /**
     * Set the model. Should contain only {@link MPComboBoxModelItem}s
     * @param model
     */
    public void setModel(MPComboboxModel model) {
        jComboBox1.setModel(model);
    }


    /**
     * Convenience Method to set a single {@link DatabaseObject} as the model of the combobox.<br/>
     * Will set the DO as the selected item after adding.
     * @param obj
     */
    public void setModel(DatabaseObject obj) {
        setModel(new Vector<DatabaseObject>(Arrays.asList(new DatabaseObject[]{
        obj
        })));
        setSelectedIndex(0);
    }

    /**
     * Convenience Method to set a {@link List} of {@link DatabaseObject}s as the model of the combobox.<br/>
     * @param vector
     */
    public void setModel(List<DatabaseObject> vector) {
        setModel(new MPComboboxModel(MPComboBoxModelItem.toItems(vector)));
    }

    /**
     * Delegates to getComboBox().setSelectedIndex(itemID);
     * @param itemID
     */
    public void setSelectedIndex(int itemID) {
        getComboBox().setSelectedIndex(itemID);
    }

    /**
     * Sets the item with the given value as selected item
     * @param valueOfItem
     */
    public void setSelectedItem(String valueOfItem) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(valueOfItem, jComboBox1.getModel()));
    }

    /**
     * Sets the item with the given ID as selected item
     * @param ID
     */
    public void setSelectedItem(Object ID) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(ID, jComboBox1.getModel()));
    }

    /**
     * If set to true, hitting "Enter" on the text field will trigger a search for the entered value and popup the results if any.
     * <br/>{@link LabeledCombobox#setContext(Context)} must be called before this can work.
     * @param enabled
     */
    public void setSearchOnEnterEnabled(boolean enabled) {
        SEARCH_ON_ENTER = enabled;
        jComboBox1.setEditable(true);
    }

    /**
     * Set the context for database queries
     * @param c
     */
    public void setContext(Context c) {
        this.context = c;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        setOpaque(false);

        jLabel1.setText("text");

        jComboBox1.setAutoscrolls(true);
        jComboBox1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jComboBox1KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jComboBox1KeyTyped(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, 0, 111, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBox1KeyTyped
    }//GEN-LAST:event_jComboBox1KeyTyped

    private void jComboBox1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jComboBox1KeyPressed
    }//GEN-LAST:event_jComboBox1KeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the _text
     */
    public MPComboBoxModelItem getValue() {
        return (MPComboBoxModelItem) jComboBox1.getSelectedItem();
    }

    /**
     * @return the _label
     */
    public String get_Label() {
        return jLabel1.getText();
    }

    /**
     * @param label the _label to set
     */
    public void set_Label(String label) {
        this._label = label;
        jLabel1.setText(_label);
        this.setToolTipText(_text);
        jLabel1.setToolTipText(_text);
    }

    @Deprecated
    public void set_LabelFont(Font font) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        jLabel1.setEnabled(enabled);
        jComboBox1.setEnabled(enabled);
    }

    /**
    Sets the selected item in the combo box display area to the object in the argument. If anObject is in the list, the display area shows anObject selected.
    If anObject is not in the list and the combo box is uneditable, it will not change the current selection. For editable combo boxes, the selection will change to anObject.
    If this constitutes a change in the selected item, ItemListeners added to the combo box will be notified with one or two ItemEvents. If there is a current selected item, an ItemEvent will be fired and the state change will be ItemEvent.DESELECTED. If anObject is in the list and is not currently selected then an ItemEvent will be fired and the state change will be ItemEvent.SELECTED.
    ActionListeners added to the combo box will be notified with an ActionEvent when this method is called.
    Parameters:
    anObject - the list object to select; use null to clear the selection
     * @param text
     */
    public void setValue(String text) {
        jComboBox1.setSelectedItem(text);
        jComboBox1.setPopupVisible(false);
    }


}
