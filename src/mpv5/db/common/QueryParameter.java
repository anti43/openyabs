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
package mpv5.db.common;

/**
 *
 * @author andreas
 */
public class QueryParameter {

    /**
     * a = a
     */
    public static final int EQUALS = 0;
    /**
     * a <> a
     */
    public static final int NOTEQUAL = 1;
    /**
     * UPPER(a) LIKE %A%
     */
    public static final int LIKE = 2;
    /**
     * UPPER(a) NOT LIKE %A%
     */
    public static final int NOTLIKE = 3;
    /**
     * a => a
     */
    public static final int GREATEREQUALS = 4;
    /**
     * a =< a
     */
    public static final int LOWEREQUALS = 5;
    private Context context;
    private String key;
    private Object value;
    private int condition;

    /**
     * 
     * @param context
     * @param key
     * @param value
     * @param condition
     */
    public QueryParameter(Context context, String key, Object value, int condition) {
        this.context = context;
        this.key = key;
        this.value = value;
        this.condition = condition;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return getContext().getDbIdentity() + "." + key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.setValue(value);
    }

    /**
     * @return the condition
     */
    public int getCondition() {
        return condition;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(int condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return getKey() + ": " + getValue();
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
