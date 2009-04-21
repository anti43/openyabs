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
import mpv5.data.*;


/**
 * This class is used to store criterias for <code>Select</code> queries
 * @author anti43
 */
public class QueryCriteria {

    /**
     * Generate a <code>SaveString<code/>
     * @param s
     * @return
     */
    public static SaveString getSaveStringFor(String s) {
        return new SaveString(s, true);
    }
    private HashMap<String, SaveString> list = new HashMap<String, SaveString>();
    public QueryCriteria() {
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
    private String getValuesString() {
        String[] k = getKeys();
        String s = "";

        for (int i = 0; i < k.length; i++) {
            mpv5.db.common.SaveString v = getValue(k[i]);
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


}

