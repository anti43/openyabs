/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
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
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;

/**
 *
 */
public class LightMPComboBox extends JComboBox {

    private static final long serialVersionUID = 1L;
    public boolean SEARCH_ON_ENTER = false;
    public static final String VALUE_SEARCHFIELDS = ".lightmpcomboboxsearchvalue";
    private Context context;
    private int sortmode = 0;
    private JTable table;

    /**
     * If this combobox is within a table cell, set the table here
     * @param table
     */
    public void setTable(JTable table) {
        this.table = table;
    }
    private boolean initiated;

    /** Creates new form LabeledTextField */
    public LightMPComboBox() {
        super();
        getEditor().getEditorComponent().addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (SEARCH_ON_ENTER && (e.getKeyCode() == KeyEvent.VK_F2) && context != null) {
                    search();
                }
//                  if (SEARCH_ON_ENTER && (e.getKeyCode() == KeyEvent.VK_DOWN) && context != null) {
//                    showPopup();
//                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


        getComponent(0).addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SEARCH_ON_ENTER && !initiated) {
                    if (table == null) {
                        setModel(new DefaultComboBoxModel(new String[]{""}));
                    }
                    search();
//                    if (table != null) {
//                        showPopup();
////                    }
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

        setRenderer(new ComboBoxRendererForTooltip());
        setModel();
    }

    /**
     * Creates a new ComboBox with {@link MPCombobox#setSearchOnEnterEnabled(boolean)} enabled and the given search {@link Context}
     *
     * @param c
     * @param table
     */
    public LightMPComboBox(Context c, JTable table) {
        this();
        setSearchOnEnterEnabled(true);
        setContext(c);
        setTable(table);
        putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < receiver.size(); i++) {
                    MPCBSelectionChangeReceiver mPCBSelectionChangeReceiver = receiver.get(i);
                    mPCBSelectionChangeReceiver.changeSelection((MPComboBoxModelItem) getItemAt(getSelectedIndex()), context);
                }
            }
        });
    }

    public JComboBox getComboBox() {
        return this;
    }

    /**
     * Triggers the search functionality
     */
    public void search() {
        initiated = true;
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ComboBoxEditor cbField = getEditor();
                Object value = cbField.getItem();
                if (value == null) {
                    value = "";
                }
                String params = "ids, cname";
                String vars = null;
                if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty(context + VALUE_SEARCHFIELDS)) {
                    params = "ids";
                    vars = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(context + VALUE_SEARCHFIELDS);
                    String[] vaars = vars.split("_\\$");

                    for (int i = 0; i < vaars.length; i++) {
                        try {
                            if (vaars[i] != null && vaars[i].contains("$_")) {
                                params += "," + vaars[i].split("\\$_")[0].replace(",", "").replace("'", "`");
                            }
                        } catch (Exception e) {
                            Log.Debug(e);
                        }
                    }
                }

                Object[][] data = new DatabaseSearch(context, 200).getValuesFor2(params, String.valueOf(value), null, true, true);
                setModel(MPComboBoxModelItem.toModel(MPComboBoxModelItem.toItems(data, true, MPComboBoxModelItem.COMPARE_BY_VALUE, vars)));
                try {
                    table.editCellAt(table.getSelectedRow(), 4);
                } catch (Exception e) {
                }
//                   showPopup();//avoid flickering
            }
        };
        new Thread(runnable).start();
    }

    /**
     *
     * @return The model
     */
    public MPComboboxModel getMPModel() {
        return (MPComboboxModel) getModel();
    }

    /**
     *
     * @param values
     */
    public void setModel(MPEnum[] values) {
        setModel(MPComboBoxModelItem.toModel(values));
    }

    /**
     *
     * @param values
     * @param compareMode
     */
    public void setModel(MPEnum[] values, int compareMode) {
        setModel(MPComboBoxModelItem.toModel(values, compareMode, new java.util.Vector<Integer>()));
    }

    /**
     * Delegates to setModel(MPComboBoxModelItem.toModel(data));
     * {id (hidden), value (shown in the list)}
     * @param data
     */
    public void setModel(Object[][] data) {
        setModel(MPComboBoxModelItem.toModel(data));
    }

    /**
     * Set the model. Should contain only {@link MPComboBoxModelItem}s
     * @param model
     */
    public void setModel(MPComboboxModel model) {
        super.setModel(model);
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
     * Sets the item with the given value as selected item
     * @param valueOfItem
     */
    public void setSelectedItem(String valueOfItem) {
        setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(valueOfItem, getModel()));
    }

    @Override
    public void setSelectedIndex(int index) {
        super.setSelectedIndex(index);
//        for (int i = 0; i < receiver.size(); i++) {
//            MPCBSelectionChangeReceiver mPCBSelectionChangeReceiver = receiver.get(i);
//            mPCBSelectionChangeReceiver.changeSelection((MPComboBoxModelItem) getItemAt(index), context);
//        }
    }
    List<MPCBSelectionChangeReceiver> receiver = new Vector<MPCBSelectionChangeReceiver>();

    /**
     *
     * @param rec
     */
    public void addSelectionChangeReceiver(MPCBSelectionChangeReceiver rec) {
        receiver.add(rec);
    }

//    /**
//     * Sets the item with the given ID as selected item
//     * @param ID
//     */
//    public void setSelectedItem(Object ID) {
//            setSelectedIndex(MPComboBoxModelItem.getItemID(ID,getModel()));
//    }
    /**
     * If set to true, hitting "Enter" on the text field will trigger a search for the entered value and popup the results if any.
     * <br/>{@link LabeledCombobox#setContext(Context)} must be called before this can work.
     * @param enabled
     */
    public void setSearchOnEnterEnabled(boolean enabled) {
        SEARCH_ON_ENTER = enabled;
        setEditable(true);
    }

    /**
     * Set the context for database queries
     * @param c
     */
    public void setContext(Context c) {
        this.context = c;
    }

    /**
     * @return the _text
     */
    public MPComboBoxModelItem getValue() {
        return (MPComboBoxModelItem) getSelectedItem();
    }

    @Override
    public void setEnabled(boolean enabled) {
        setEnabled(enabled);
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
        setSelectedItem(text);
        setPopupVisible(false);
    }

    /**
     * Sets an empty model
     */
    public void setModel() {
        setModel(new Vector<DatabaseObject>());
    }
}
