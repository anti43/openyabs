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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.utils.models.*;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;

/**
 *
 *
 */
public class MPCombobox extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    public boolean SEARCH_ON_ENTER = false;
    public boolean SEARCH_WHILE_TYPE = false;
    private Context context;
    private int sortmode = 0;
    private JTable table;
    private boolean instantiated;
    private DataPanel receiver;
    private String prevSearch = "";
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * If this combobox is within a table cell, set the table here
     *
     * @param table
     */
    public void setTable(JTable table) {
        this.table = table;
    }

    /**
     * Creates new form LabeledTextField
     */
    public MPCombobox() {
        initComponents();
        jComboBox1.getEditor().getEditorComponent().addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (SEARCH_WHILE_TYPE && getContext() != null) {
                    search();
                } else if (SEARCH_ON_ENTER && (e.getKeyCode() == KeyEvent.VK_ENTER) && getContext() != null) {
                    search();
                }
                jComboBox1.showPopup();
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        jComboBox1.getComponent(0).addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SEARCH_ON_ENTER && !instantiated) {
                    if (table == null) {
                        jComboBox1.setModel(new DefaultComboBoxModel(new String[]{""}));
                    }
                    search();
//                    if (table != null) {
                    jComboBox1.showPopup();
//                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        jComboBox1.setRenderer(new ComboBoxRendererForTooltip());
//        jComboBox1.setToolTipText(Messages.SEARCHABLE.toString());
        setModel();
    }

    /**
     * Creates a new ComboBox with
     * {@link MPCombobox#setSearchOnEnterEnabled(boolean)} enabled and the given
     * search {@link Context}
     *
     * @param c
     * @param table
     */
    public MPCombobox(Context c, JTable table) {
        this();
        setSearchEnabled(true);
        setContext(c);
        setTable(table);
        getComboBox().putClientProperty("JComboBox.isTableCellEditor", table != null);
    }

    public JComboBox getComboBox() {
        return jComboBox1;
    }

    /**
     * Triggers the search functionality
     */
    public void search() {
        search(false);
    }

    /**
     * Triggers the search functionality
     *
     * @param hidePopup
     */
    public synchronized void search(final boolean hidePopup) {
        if (!Main.INSTANTIATED) {
            return;
        }
        if (SEARCH_ON_ENTER || SEARCH_WHILE_TYPE) {
            instantiated = true;
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    ComboBoxEditor cbField = jComboBox1.getEditor();
                    Object value = cbField.getItem();
                    jComboBox1.setSelectedItem(new MPComboBoxModelItem(-1, value.toString()));

                    Object[][] data = null;
                    if (getComboBox().isEditable()) {
                        if (getContext().equals(Context.getProduct())) {
                            data = new DatabaseSearch(getContext(), 20).getValuesFor2("ids, " + ConnectionTypeHandler.concat("cname", "' '", ConnectionTypeHandler.getToChar("externalnetvalue")), String.valueOf(value), null, true, true);
                        } else if (((getContext().equals(Context.getCustomer()) || getContext().equals(Context.getManufacturer()) || getContext().equals(Context.getSupplier())) && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "companiesovernames")) || getContext().isCompany() == true) {
                            data = new DatabaseSearch(getContext(), 20).getValuesFor("ids, company", "company", value.toString(), true);
                        } else if (getContext().equals(Context.getCustomer()) || getContext().equals(Context.getManufacturer()) || getContext().equals(Context.getSupplier())) {
                            data = new DatabaseSearch(getContext(), 20).getValuesFor("ids, cname, prename", "cname", value.toString(), true);
                        } else {
                            data = new DatabaseSearch(getContext(), 20).getValuesFor(getContext().getSearchFields(), "cname", value.toString(), true);
                        }
                    } else {
                        data = new DatabaseSearch(getContext(), 20).getValuesFor("ids, cname", "cname", "", true);
                    }

                    if (data.length > 0) {
                        jComboBox1.setModel(MPComboBoxModelItem.toModel(MPComboBoxModelItem.toItems(data, true, true)));
               
                        if (table != null) {
                            table.editCellAt(table.getSelectedRow(), 4);
                        }
                        if (!hidePopup) {
                            jComboBox1.showPopup();
                        }
                    }
                    
                    if (data.length == 1) {
                        jComboBox1.setSelectedIndex(0);
                    }
                }
            };
            if (getContext() != null) {
                executor.execute(runnable);
            }
        }
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
        jComboBox1.setModel(MPComboBoxModelItem.toModel(values, compareMode, new java.util.Vector<Integer>()));
    }

    /**
     * Uses enum.name() as ID, and enum.toString() as value.
     *
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
     * Delegates to setModel(MPComboBoxModelItem.toModel(data)); {id (hidden),
     * value (shown in the list)}
     *
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
     * The textual representation of the seleceted item
     *
     * @return
     */
    public String getSelectedValue() {
        return String.valueOf(jComboBox1.getSelectedItem());
    }

    /**
     * Set the model. Should contain only {@link MPComboBoxModelItem}s
     *
     * @param model
     */
    public void setModel(MPComboboxModel model) {
        jComboBox1.setModel(model);
    }

    /**
     * Convenience Method to set a single {@link DatabaseObject} as the model of
     * the combobox.<br/>
     * Will set the DO as the selected item after adding.
     *
     * @param obj
     */
    public void setModel(DatabaseObject obj) {
        Log.Print(this, "setting to model " + String.valueOf(obj));
        setModel(new Vector<DatabaseObject>(Arrays.asList(new DatabaseObject[]{
            obj
        })));
        setSelectedIndex(0);
    }

    /**
     * Convenience Method to set a {@link List} of {@link DatabaseObject}s as
     * the model of the combobox.
     *
     * @param vector
     */
    public void setModel(List<DatabaseObject> vector) {
        setModel(new MPComboboxModel(MPComboBoxModelItem.toItems(vector)));
    }

    /**
     * Delegates to getComboBox().setSelectedIndex(itemID);
     *
     * @param itemID
     */
    public void setSelectedIndex(int itemID) {
        if (itemID >= 0 && itemID < getComboBox().getItemCount()) {
            getComboBox().setSelectedIndex(itemID);
        }
    }

    /**
     * Sets the item with the given value as selected item
     *
     * @param valueOfItem
     */
    public void setSelectedItem(String valueOfItem) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(valueOfItem, jComboBox1.getModel()));
    }

    /**
     * Sets the item with the given ID as selected item
     *
     * @param ID
     */
    public void setSelectedItem(Object ID) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(ID, jComboBox1.getModel()));
    }

    /**
     * If set to true, hitting "Enter" on the text field will trigger a search
     * for the entered value and popup the results if any.
     * {@link LabeledCombobox#setContext(Context)} must be called before this
     * can work.
     *
     * @param enabled
     */
    public void setSearchEnabled(boolean enabled) {
        SEARCH_ON_ENTER = enabled;
        jComboBox1.setEditable(enabled);
        if (enabled) {
            jComboBox1.setToolTipText(Messages.SEARCHABLE.toString());
        }
    }

    /**
     * If set to true, typing on the text field will trigger a search for the
     * entered value and popup the results if any.
     * {@link LabeledCombobox#setContext(Context)} must be called before this
     * can work.
     *
     * @param enabled
     */
    /*public void setSearchWhileTypeEnabled(boolean enabled) {
        SEARCH_WHILE_TYPE = enabled;
        SEARCH_ON_ENTER = enabled;
        jComboBox1.setEditable(enabled);
        if (enabled) {
            jComboBox1.setToolTipText(Messages.SEARCHABLE.toString());
        }
    }*/

    /**
     * Set the context for database queries
     *
     * @param c
     */
    public void setContext(Context c) {
        this.context = c;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jComboBox1.setAutoscrolls(true);
        jComboBox1.setMinimumSize(new java.awt.Dimension(33, 18));
        jComboBox1.setPreferredSize(new java.awt.Dimension(100, 20));
        jComboBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jComboBox1MouseClicked(evt);
            }
        });
        add(jComboBox1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jComboBox1MouseClicked
        search();
    }//GEN-LAST:event_jComboBox1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the _text
     */
    public MPComboBoxModelItem getValue() {
        return (MPComboBoxModelItem) jComboBox1.getSelectedItem();
    }

    @Deprecated
    public void set_LabelFont(Font font) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        jComboBox1.setEnabled(enabled);
    }

    /**
     * Sets the selected item in the combo box display area to the object in the
     * argument. If anObject is in the list, the display area shows anObject
     * selected. If anObject is not in the list and the combo box is uneditable,
     * it will not change the current selection. For editable combo boxes, the
     * selection will change to anObject. If this constitutes a change in the
     * selected item, ItemListeners added to the combo box will be notified with
     * one or two ItemEvents. If there is a current selected item, an ItemEvent
     * will be fired and the state change will be ItemEvent.DESELECTED. If
     * anObject is in the list and is not currently selected then an ItemEvent
     * will be fired and the state change will be ItemEvent.SELECTED.
     * ActionListeners added to the combo box will be notified with an
     * ActionEvent when this method is called. Parameters: anObject - the list
     * object to select; use null to clear the selection
     *
     * @param text
     */
    public void setValue(String text) {
        jComboBox1.setSelectedItem(text);
        jComboBox1.setPopupVisible(false);
    }

    /**
     * Sets an empty model
     */
    public void setModel() {
        setModel(new Vector<DatabaseObject>());
    }

    /**
     * Trigger a search on the combo box
     */
    public void triggerSearch() {
        search(true);
    }

    public void setReceiver(final DataPanel panel) {
        this.receiver = panel;
        jComboBox1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (getSelectedItem() != null) {
                        panel.setDataOwner(DatabaseObject.getObject(getContext(), Integer.valueOf(getSelectedItem().getId())), true);
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        });
    }

    /**
     *
     * @return The id obj of the selected entry or NULL if nothing is selected
     */
    public Object getSelectedItemId() {
        MPComboBoxModelItem x = getSelectedItem();
        if (x == null) {
            return null;
        }
        return x.getIdObject();
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }
}
