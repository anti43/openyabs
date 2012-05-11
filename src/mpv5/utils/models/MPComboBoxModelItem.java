/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.db.common.DatabaseObject;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;

/**
 * A MPComboBoxModelItem consists of a visible "value" part and an invisible "ID" part.
 * 
 */
public class MPComboBoxModelItem extends DefaultComboBoxModel implements Comparable<MPComboBoxModelItem> {

    private static final long serialVersionUID = 1L;
    public static int COMPARE_BY_ID = 0;
    /**
     * (default)
     */
    public static int COMPARE_BY_VALUE = 1;
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
        return getItemID((Object) new Integer(uid), model);
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
        return -1;
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
            if (((MPComboBoxModelItem) model.getElementAt(i)).id.toString().equals(uid)) {
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
    public synchronized static int getItemIDfromValue(String value, ComboBoxModel model) {
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
        return toItems(items, COMPARE_BY_VALUE);
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), values (shown in the list)}
     * @param items
     * @param compareMode
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, int compareMode) {
        return toItems(items, compareMode, false);
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), values (shown in the list)}
     * @param items
     * @param compareMode
     * @param convertIndexToInteger
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, int compareMode, boolean convertIndexToInteger) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length];
        for (int i = 0; i < array.length; i++) {
            String x = "";
            for (int j = 1; j < items[i].length; j++) {
                String k = String.valueOf(items[i][j]).trim();
                x += k + " ";
            }

            if (convertIndexToInteger) {
                array[i] = new MPComboBoxModelItem(Integer.valueOf(items[i][0].toString()), x.substring(0, x.length() - 1));
            } else {
                array[i] = new MPComboBoxModelItem((items[i][0]), x.substring(0, x.length() - 1));
            }
            array[i].setCompareMode(compareMode);
        }
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), values (shown in the list)}
     * @param items
     * @param compareMode
     * @param formatString format _$value1$_ xx _$value2$_ etc.
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, int compareMode, String formatString) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length];
        String[] vaars = null;
        if (formatString != null) {
            vaars = formatString.split("_\\$");
            Log.Debug(MPComboBoxModelItem.class, "Length of var string: " + vaars.length);
        }
        for (int i = 0; i < items.length; i++) {
            String x = "";
            String format = formatString;
            for (int j = 1; j < items[i].length; j++) {
                String k = String.valueOf(items[i][j]);
                if (format == null) {
                    x += k;
                } else {
                    try {
                        format = format.replaceFirst("_\\$(.*?)\\$_", k);
                    } catch (Exception e) {
                        Log.Debug(e);
                    }
                    x = format;
                }
            }

            array[i] = new MPComboBoxModelItem(items[i][0], x);
            array[i].setCompareMode(compareMode);
        }
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), value (shown in the list)}
     *
     * @param items
     * @param sortValuesNaturally If TRUE, sorts the Items into ascending order, according to the natural ordering of its values
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, boolean sortValuesNaturally) {
        MPComboBoxModelItem[] array = toItems(items);
        if (sortValuesNaturally) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), value (shown in the list)}
     *
     * @param items
     * @param sortValuesNaturally If TRUE, sorts the Items into ascending order, according to the natural ordering of its values
     * @param convertIndexToInteger
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, boolean sortValuesNaturally, boolean convertIndexToInteger) {
        MPComboBoxModelItem[] array = toItems(items, COMPARE_BY_VALUE, convertIndexToInteger);
        if (sortValuesNaturally) {
            Arrays.sort(array);
        }
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {id (hidden), value (shown in the list)}
     *
     * @param items
     * @param sortValuesNaturally If TRUE, sorts the Items into ascending order, according to the natural ordering of its values
     * @param compareMode 
     * @param format _$value1$_ xx _$value2$_ etc.
     * @return
     */
    public static MPComboBoxModelItem[] toItems(Object[][] items, boolean sortValuesNaturally, int compareMode, String format) {
        MPComboBoxModelItem[] array = toItems(items, compareMode, format);
        if (sortValuesNaturally) {
            Arrays.sort(array);
        }

//        Log.PrintArray(items);
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {Integer id (hidden), String value (shown in the list)}
     * @param items
     * @return
     */
    public static MPComboBoxModelItem[] toItems(List<DatabaseObject> items) {
        return toItems(items, COMPARE_BY_VALUE);
    }

    /**
     * Converts an array to mp combo box items
     * {Integer id (hidden), String value (shown in the list)}
     * @param items
     * @return
     */
    public static MPComboBoxModelItem[] toItems(List items, boolean simple) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(items.get(i), items.get(i).toString());
            array[i].setCompareMode(COMPARE_BY_VALUE);
        }
        return array;
    }

    /**
     * Converts an array to mp combo box items
     * {Integer id (hidden), String value (shown in the list)}
     * @param items
     * @param compareMode
     * @return
     */
    public static MPComboBoxModelItem[] toItems(List<DatabaseObject> items, int compareMode) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(new Integer(items.get(i).__getIDS()), items.get(i).__getCname());
            array[i].setCompareMode(compareMode);
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
        return toItems(items, COMPARE_BY_VALUE, new Vector<Integer>());
    }

    /**
     * Converts an enum<id, name> to mp combo box items
     * {id (hidden), value (shown in the list)}
     * @param items
     * @param compareMode
     * @param skip
     * @return
     */
    public static MPComboBoxModelItem[] toItems(MPEnum[] items, int compareMode, List<Integer> skip) {
//        Log.PrintArray(items);
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length - skip.size()];
        for (int i = 0, z = 0; i < items.length; i++) {
            if (!skip.contains(new Integer(i))) {
                array[z] = new MPComboBoxModelItem(items[i].getId(), items[i].getName());
                array[z].setCompareMode(compareMode);
                z++;
            }
        }
        return array;
    }

    /**
     * Creates a {@link MPComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {enum id (hidden), value (shown in the list)}
     * @param data
     * @return
     */
    public static MPComboboxModel toModel(MPEnum[] data) {
        return new MPComboboxModel(toItems(data));
    }

    /**
     * Creates a {@link MPComBoxModel} containing a {@link MPComboBoxModelItem}
     * {ids (hidden), cname (shown in the list)}
     * @param data
     * @return
     */
    public static MPComboboxModel toModel(DatabaseObject data) {
        return new MPComboboxModel(toItems(new Vector<DatabaseObject>(Arrays.asList(new DatabaseObject[]{data}))));
    }

    /**
     * Creates a {@link MPComBoxModel} containing a {@link MPComboBoxModelItem}
     * {entity (hidden), dbo (shown in the list)}
     * @param <T>
     * @param data
     * @return
     */
    public static <T extends DatabaseObject> MPComboboxModel toModel0(List<T> data) {
        Object[][] l = new Object[data.size()][2];
        for (int i = 0; i < data.size(); i++) {
            DatabaseObject databaseObject = data.get(i);
            l[i][0] = databaseObject.IDENTITY;
            l[i][0] = databaseObject;
        }
        return new MPComboboxModel(toItems(l));
    }

    /**
     * Delegates to new MPComboboxModel(data);
     * @param data
     * @return
     */
    public static MPComboboxModel toModel(MPComboBoxModelItem[] data) {
        return new MPComboboxModel(data);
    }

    /**
     *  Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * @param list
     * @return
     */
    public static MPComboboxModel toModel(List<MPComboBoxModelItem> list) {
        return new MPComboboxModel(list.toArray(new MPComboBoxModelItem[]{}));
    }

    /**
     * Creates a {@link DefaultComBoxModel} containing an array of {@link MPComboBoxModelItem}
     * {enum id (hidden), value (shown in the list)}
     * @param data
     * @param compareMode
     * @param skip
     * @return
     */
    public static MPComboboxModel toModel(MPEnum[] data, int compareMode, List<Integer> skip) {
        return new MPComboboxModel(toItems(data, compareMode, skip));
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
    private int comparemode = COMPARE_BY_VALUE;

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
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(MPComboBoxModelItem.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(MPComboBoxModelItem.class.getName()).log(Level.SEVERE, null, ex);
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
     * Define how the items compare to each other
     * @param mode
     */
    public void setCompareMode(int mode) {
        this.comparemode = mode;
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
        return name != null && !"".equals(name) ? name : "";
    }

//     * <br/>The ID field is <b>NOT</b> part of the compare.
//     *
//     * Note: this class has a natural ordering that is inconsistent with equals.
    /**
     * MPComboBoxModelItems are compared to their ids and values!
     * @param to 
     */
    @Override
    public int compareTo(MPComboBoxModelItem to) {
        final int EQUAL = 0;
        if (this == to) {
            return EQUAL;
        }
        if (to.getIdObject().equals(id) && to.getValue().equals(getValue())) {
            return EQUAL;
        }

        if (comparemode == COMPARE_BY_VALUE) {
            int comparison = this.getValue().compareTo(to.getValue());
            if (comparison != EQUAL) {
                return comparison;
            }
            comparison = this.getId().compareTo(to.getId());
            if (comparison != EQUAL) {
                return comparison;
            }
        } else {
            int comparison = this.getId().compareTo(to.getId());
            if (comparison != EQUAL) {
                return comparison;
            }
            comparison = this.getValue().compareTo(to.getValue());
            if (comparison != EQUAL) {
                return comparison;
            }
        }

        assert this.equals(to) : "compareTo inconsistent with equals.";
        return EQUAL;
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
