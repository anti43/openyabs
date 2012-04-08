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
package mpv5.db.common;

import java.util.HashMap;

/**
 * This class is used to store criterias for <code>Select</code> queries
 * 
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
    private String order = "";
    private String FIELD_SEPARATOR = "<SepaRatoR>";
    private boolean in;

    
    /**
     * @deprecated You MUST add at least one and criteria
     */
    public QueryCriteria() {
    }

    /**
     * Convenience constructor, calls add(string, value)
     * @param <T>
     * @param string
     * @param value
     */
    public <T extends Number> QueryCriteria(String string, T value) {
        addAndCondition(string, value);
    }

    /**
     * Convenience constructor, calls add(string, value)
     * @param string
     * @param value
     */
    public QueryCriteria(String string, String value) {
        addAndCondition(string, value);
    }

    /**
     * Convenience constructor, calls add(string, value)
     * @param string
     * @param value
     */
    public QueryCriteria(String string, Object value) {
        if (value instanceof Boolean) {
            addAndCondition(string, (Boolean) value);
        } else if (value instanceof Number) {
            addAndCondition(string, (Number) value);
        } else {
            addAndCondition(string, value.toString());
        }
    }

    /**
     * Adds a key with a value
     * @param <T> 
     * @param key
     * @param value
     */
    public <T extends Number> void addAndCondition(String key, T value) {
        String string = String.valueOf(value);
        list.put(key, new SaveString(string, false));
    }

    /**
     * Adds a key with a value
     * @param key
     * @param value
     */
    public void addAndCondition(String key, boolean value) {
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
    public void addAndCondition(String key, String value) {
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
     * 
     * @param column
     * @param asc
     */
    public void setOrder(String column, boolean asc) {
        this.order = " ORDER BY " + column;
        if (asc) {
            order += " ASC ";
        } else {
            order += "      ";
        }
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
            s += v.getWrapper() + v.toString() + v.getWrapper() + FIELD_SEPARATOR;
        }
        if (s.length() > 1) {
            return s.substring(0, s.length() - FIELD_SEPARATOR.length());
        } else {
            return s;
        }
    }

    /**
     * 
     * @return An array of all values, in getKeys() - order
     */
    public String[] getValues() {
        return getValuesString().split(FIELD_SEPARATOR);
    }

    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    public boolean getIncludeInvisible() {
        return in;
    }

    /**
     * @param in the in to set
     */
    public void setIncludeInvisible(boolean in) {
        this.in = in;
    }


}

