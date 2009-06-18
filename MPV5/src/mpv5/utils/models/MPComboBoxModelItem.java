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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.db.common.DatabaseObject;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;

/**
 *
 * 
 */
public class MPComboBoxModelItem extends DefaultComboBoxModel implements Comparable<MPComboBoxModelItem> {

    private static final long serialVersionUID = 1L;

    /**
     * Returns the index of the item with the given id
     * @param uid
     * @param model
     * @return
     */
    public static int getItemID(int uid, ComboBoxModel model) {
        return getItemID(String.valueOf(uid), model);
    }

    /**
     * Returns the index of the item with the given id
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
            array[i] = new MPComboBoxModelItem(String.valueOf(items[i][0]), String.valueOf(items[i][1]));
        }
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
        return array;
    }

    /**
     * Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {enum id (hidden), value (shown in the list)}
     * @param data
     * @return
     */
    public static DefaultComboBoxModel toModel(MPEnum[] data) {
        return new DefaultComboBoxModel(toItems(data));
    }

    /**
     * Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {id (hidden), value (shown in the list)}
     * @param data
     * @return
     */
    public static DefaultComboBoxModel toModel(Object[][] data) {
        return new DefaultComboBoxModel(toItems(data));
    }
    private String id;
    private String name;

    /**
     *
     * @param id
     * @param name
     */
    public MPComboBoxModelItem(int id, String name) {
        this.id = String.valueOf(id);
        this.name = name;
    }

    /**
     *
     * @param id
     * @param name
     */
    public MPComboBoxModelItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * If the id is greater than 0 or longer than 0 if a String index
     * @return
     */
    public boolean isValid() {
        try {
            if (Integer.valueOf(id).intValue() > 0) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException numberFormatException) {
            if (id.length() > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id.toString();
    }

    /**
     * @return the value
     */
    public String getValue() {
        return name;
    }

    /**
     * Name = Value
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    /**
     * Returns the value of the item
     */
    public String toString() {
        return name;
    }

    @Override
    /**
     * MPComboBoxModelItems are compared to equal if their values are equal!
     */
    public int compareTo(MPComboBoxModelItem to) {
        return name.compareTo(to.getValue());
    }
}
