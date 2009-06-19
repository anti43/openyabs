/*
 *  This file is part of MP.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.db.common.DatabaseObject;
import mpv5.handler.MPEnum;

/**
 * A MPComboBoxModelItem consists of a visible "value" part and an invisible "ID" part.
 * 
 */
public class MPComboBoxModelItem extends DefaultComboBoxModel implements Comparable<MPComboBoxModelItem> {

    private static final long serialVersionUID = 1L;
    /**
     * The id field may have any class.
     */
    public Class ID_CLASS = String.class;

    /**
     * Convenience method for Integer IDs. Returns the index of the item with the given id
     * @param uid
     * @param model
     * @return
     */
    public static int getItemID(Integer uid, ComboBoxModel model) {
        return getItemID(String.valueOf(uid), model);
    }

    /**
     * Returns the index of the item with the given id
     * @param uid
     * @param model
     * @return
     */
    public static int getItemID(Object uid, ComboBoxModel model) {
        for (int i = 0; i < model.getSize(); i++) {
//            Log.Debug(MPComboBoxModelItem.class, ((MPComboBoxModelItem) model.getElementAt(i)).id + " comparing with: " + uid);
            if (((MPComboBoxModelItem) model.getElementAt(i)).id.equals(uid)) {
//                Log.Debug(MPComboBoxModelItem.class, "Found at Index:" + i);
                return i;
            }
        }
        return 0;
    }

    /**
     * Convenience method for String IDs. Returns the index of the item with the given id
     * @param uid
     * @param model
     * @return
     */
    public static int getItemID(String uid, ComboBoxModel model) {
        for (int i = 0; i < model.getSize(); i++) {
//            Log.Debug(MPComboBoxModelItem.class, ((MPComboBoxModelItem) model.getElementAt(i)).id + " comparing with: " + uid);
            if (((MPComboBoxModelItem) model.getElementAt(i)).id.equals(uid)) {
//                Log.Debug(MPComboBoxModelItem.class, "Found at Index:" + i);
                return i;
            }
        }
        return 0;
    }

    /**
     * Returns the index of the item with the given id
     * @param value
     * @param model
     * @return
     */
    public static int getItemIDfromValue(String value, ComboBoxModel model) {
        for (int i = 0; i < model.getSize(); i++) {
//            Log.Debug(MPComboBoxModelItem.class, ((MPComboBoxModelItem) model.getElementAt(i)).name + " comparing with: " + value);
            if (((MPComboBoxModelItem) model.getElementAt(i)).name.equals(value)) {
//                Log.Debug(MPComboBoxModelItem.class, "Found at Index:" + i);
                return i;
            }
        }
        return 0;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), value (shown in the list)}
     * @param items
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(items[i][0], String.valueOf(items[i][1]));
        }

        Arrays.sort(array);

        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), value (shown in the list)}
     * @param items
     * @return
     */
    public static MPComboBoxModelItem[] toItems(ArrayList<DatabaseObject> items) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(items.get(i).__getIDS(), items.get(i).__getCName());
        }

        Arrays.sort(array);

        return array;
    }

    /**
     * Converts an enum<id, name> to mp combo box items
     * {id (hidden), value (shown in the list)}
     * @param items
     * @return
     */
    public static MPComboBoxModelItem[] toItems(MPEnum[] items) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(items[i].getId(), items[i].getName());
        }

        Arrays.sort(array);

        return array;
    }

    /**
     * Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {enum id (hidden), value (shown in the list)}
     * @param data
     * @return
     */
    public static MPComboboxModel toModel(MPEnum[] data) {
        return new MPComboboxModel(toItems(data));
    }

    /**
     * Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {id (hidden), value (shown in the list)}
     * @param data
     * @return
     */
    public static MPComboboxModel toModel(Object[][] data) {
        return new MPComboboxModel(toItems(data));
    }
    private Object id;
    private String name;

    /**
     * Creates a new item with the given id and value
     * @param id
     * @param name
     */
    public MPComboBoxModelItem(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.ID_CLASS = id.getClass();
    }

    /**
     * Creates a new item with the given id and value
     * @param id
     * @param name
     */
    public MPComboBoxModelItem(Object id, String name) {
        this.id = id;
        this.name = name;
        this.ID_CLASS = id.getClass();
    }

    /**
     * To determine the class of the ID field object
     * @return A class
     */
    public Class getIDClass() {
        return ID_CLASS;
    }

    /**
     * Creates a new instance of the class represented by this items' ID Class object.
     * The class is instantiated as if by a new expression with an empty argument list.
     *
     * @return An Object or null if the ID class has no public empty constructor
     */
    public Object getInstanceOfIDClass() {
        try {
            return getIDClass().newInstance();
        } catch (InstantiationException ex) {
            Logger.getLogger(MPComboBoxModelItem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MPComboBoxModelItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * The String value of the ID field
     * @return A {@link String} representation of this items' ID Object - <b>NEVER<b/> null
     */
    public String getId() {
        return String.valueOf(id);
    }

    /**
     * The ID field
     * @return the id Object
     */
    public Object getIdObject() {
        return id;
    }

    /**
     * If the id is greater than 0 or longer than 0 if a String index or non-null for other ID Objects
     * @return true if the ID appears to be valid as defined above.
     */
    public boolean isValid() {
        if (id != null) {
            try {
                if (Integer.valueOf(id.toString()).intValue() > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                if (id instanceof String && ((String) id).length() > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    /**

     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Define the ID field of this item
     * @param id the id to set
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return name;
    }

    /**
     * Delegates to {@link MPComboBoxModelItem#setValue(String)}
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the displayed value of this item
     * @param value
     */
    public void setValue(String value) {
        this.name = value;
    }

    
    /**
     * Returns the value of the item
     */
    @Override
    public String toString() {
        return name;
    }

    
    /**
     * MPComboBoxModelItems are compared to their values!
     * <br/>The ID field is <b>NOT</b> part of the compare.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    @Override
    public int compareTo(MPComboBoxModelItem to) {
        return name.compareTo(to.getValue());
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (anotherObject == null || !(anotherObject instanceof MPComboBoxModelItem)) {
            return false;
        }

        MPComboBoxModelItem mPComboBoxModelItem = (MPComboBoxModelItem) anotherObject;
        
        if (mPComboBoxModelItem.getIdObject().equals(id) && mPComboBoxModelItem.getValue().equals(getValue())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
