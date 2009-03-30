/*
 *  This file is part of MP by anti43 /GPL.
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
package mpv5.db.common;

import java.util.HashMap;
import mpv5.logging.Log;

/**
 * This class is used to put any kind of data before passing it to the <code>QueryHandler</code> for transmission.
 * @author anti43
 */
public class DataStringHandler {

    /**
     * Generate a <code>SaveString<code/>
     * @param s
     * @return
     */
    public static SaveString getSaveStringFor(String s) {
        return new SaveString(s, true);
    }
    private HashMap<String, SaveString> list = new HashMap<String, SaveString>();

    /**
     * This is a legacy constructor, to speed up the conversion of old style data into new style data
     * what  : {set, values}
     * @param what
     */
    public DataStringHandler(String[] what) {
        String[] keys = what[0].split(",");
        String[] vals = what[1].split(",");

        for (int i = 0; i < vals.length; i++) {
            add(keys[i], vals[i]);
        }
    }

    public DataStringHandler() {
    }

    /**
     * Adds a key with a value
     * @param <T> 
     * @param key
     * @param value
     */
    public <T extends Number> void add(String key, T value) {
        String string = String.valueOf(value);
        list.put(key, new SaveString(string, false));
    }

    /**
     * Adds a key with a value
     * @param key
     * @param value
     */
    public void add(String key, boolean value) {
        if (value) {
            list.put(key, new SaveString("1", false));
        } else {
            list.put(key, new SaveString("0", false));
        }
    }

    /**
     * Adds a key with a value
     * @param key
     * @param value
     */
    public void add(String key, String value) {
        String string = String.valueOf(value);
        list.put(key, new SaveString(string, true));
    }

    /**
     * Mask backslashes with even more backslashes
     * @param string
     * @return
     */
    public String maskBackslashes(String string) {
        Log.Debug(DataStringHandler.class, "Masking Backslashes!");
        return new SaveString(string.replaceAll("\\\\", "\\\\\\\\"), true).toString();
    }

    /**
     *
     * @return An array of keys
     */
    public String[] getKeys() {
        return list.keySet().toArray(new String[]{});
    }

    /**
     * returns the value of the given key
     * @param key
     * @return
     */
    public SaveString getValue(String key) {
        return list.get(key);
    }

    /**
     * Generates a comma separated String represantion of the current values<br/>
     * with String values wrapped in single quotes
     * @return
     */
    public String getValuesString() {
        String[] k = getKeys();
        String s = "";

        for (int i = 0; i < k.length; i++) {
            SaveString v = getValue(k[i]);
            s += v.getWrapper() + v.toString() + v.getWrapper() + ",";
        }
        return s.substring(0, s.length() - 1);
    }
    
    /**
     * 
     * @return An array of all values, in getKeys() - order
     */
    public String[] getValues() {
        return getValuesString().split(",");
    }

    /**
     * Generates a comma separated String representation of the current keys
     * @return
     */
    public String getKeysString() {
        String[] k = getKeys();
        String s = "";

        for (int i = 0; i < k.length; i++) {
            s += k[i] + ",";
        }
        return s.substring(0, s.length() - 1);
    }
}

/**
 * Represents a <code>SaveString</code> value which <br><br/>
 * <li>Does not contain single quotes and<br/>
 * is considered to be safe to be inserted into a database<li/>
 */
class SaveString {

    private static final long serialVersionUID = 1L;
    private String string = "";
    private String wrapper = "";

    public SaveString(String str, boolean wrapped) {
        this.string = str.replace("'", "`");
        if (wrapped) {
            this.wrapper = "'";
        }
    }

    /**
     * Gets the String value of this SaveString
     * @return the save, decoded string
     */
    @Override
    public String toString() {
        return string;
    }

    public String getWrapper() {
        return wrapper;
    }
}
