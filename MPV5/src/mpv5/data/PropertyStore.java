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
package mpv5.data;

import java.util.ArrayList;
import javax.swing.JComponent;
import mpv5.logging.Log;

/**
 * PropertyStores is used to store key-value pairs
 * 
 */
public class PropertyStore {

    public ArrayList<String[]> list = new ArrayList<String[]>();
    private boolean changed = false;

    /**
     * Creates a new empty property store
     */
    public PropertyStore() {
    }


    /**
     * Creates a new property store and initially adds the given values
     * @param data
     */
    public PropertyStore(Object[][] data) {
       addAll(data);
    }

    /**
     * Adds all
     * @param data {key, value}
     */
    public void addAll(Object[][] data) {
       for (int i = 0; i < data.length; i++) {
            addProperty(data[i][0].toString(), String.valueOf(data[i][1]));
        }
       setChanged(true);
    }

    /**
     * Adds a new Property
     * @param name
     * @param value
     */
    public void addProperty(String name, String value) {
        Log.Debug(this, "Adding property: " + name + " = " + value);
        list.add(new String[]{name, value});
        setChanged(true);
    }



    /**
     *  Generate a List of String(key-value.toString()) pairs
     * @return The complete list of properties and values
     */
    public ArrayList<String[]> getList() {
        return list;
    }

    /**
     * Returns the value of the LAST property with that name
     * @param name
     * @return the value
     */
    public String getProperty(String name) {
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(name)) {
//                    Log.Debug(this, "Found property: " +  list.get(i - 1)[1] + " for " + name);
                    return list.get(i - 1)[1];
                }
            }
        }
        return null;
    }

    /**
     * Return a double property. Will return NULL if the property is not parseable as double
     * @param key
     * @param desiredClass
     * @return
     */
    public double getProperty(String key, double desiredClass) {
        String t = getProperty(key);
        if (t == null) {
            return 0d;
        } else {
            try {
                return Double.valueOf(t);
            } catch (NumberFormatException numberFormatException) {
                return 0d;
            }
        }
    }
  /**
     * Convenience method to retrieve visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + evt.getSource()
     * @param comp
     * @param evt
     * @param b
     * @return
     */
    public double getProperty(JComponent comp, String source, double b) {
        return getProperty(comp.getClass().getName() + "$" + source, b);
    }
       /**
     * Return a Integer property. Will return 0 if the property is not parseable as Integer
     * @param key
     * @param desiredClass
     * @return
     */
    public int getProperty(String key, int desiredClass) {
        String t = getProperty(key);
        if (t == null) {
            return 0;
        } else {
            try {
                return Integer.valueOf(t);
            } catch (NumberFormatException numberFormatException) {
                return 0;
            }
        }
    }
  /**
     * Convenience method to retrieve visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + evt.getSource()
     * @param comp
     * @param evt
     * @param b
     * @return
     */
    public int getProperty(JComponent comp, String source, int b) {
        return getProperty(comp.getClass().getName() + "$" + source, b);
    }
       /**
     * Return a boolean property. Will return false if the property is not parseable as boolean
     * @param key
     * @param desiredClass
     * @return
     */
    public boolean getProperty(String key, boolean desiredClass) {
        String t = getProperty(key);
        if (t == null) {
            return false;
        } else {
            return Boolean.parseBoolean(t);
        }
    }

    /**
     * Convenience method to retrieve visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + evt.getSource()
     * @param comp
     * @param source
     * @param b
     * @return
     */
    public boolean getProperty(JComponent comp, String source, boolean b) {
        return getProperty(comp.getClass().getName() + "$" + source, b);
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not.
     * @param name
     * @param newvalue
     */
    public void changeProperty(String name, String newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(name)) {
                    list.set(i - 1, new String[]{name, newvalue});
                    found = true;setChanged(true);
                    Log.Debug(this, "Change property: " +  list.get(i - 1)[1] + " for " + name);
                }
            }
        }
        if (!found) {
            addProperty(name, newvalue);
        }
        
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not. Stored as comp.getClass().getName() + "$" + evt.getSource()
     * @param comp
     * @param source
     * @param newvalue
     */
    public void changeProperty(JComponent comp, String source,  Object newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(comp.getClass().getName() + "$" + source)) {
                    list.set(i - 1, new String[]{comp.getClass().getName() + "$" + source, String.valueOf(newvalue)});
                    found = true;setChanged(true);
                    Log.Debug(this, "Change property: " +  list.get(i - 1)[1] + " for " + comp.getClass().getName() + "$" + source);
                }
            }
        }
        if (!found) {
            addProperty(comp.getClass().getName() + "$" + source, String.valueOf(newvalue));
        }
    }

    /**
     * Returns True if the local property store does contain a value with the given key name
     * @param propertyname
     * @return True if the key exists
     */
    public boolean hasProperty(String propertyname) {
        if (getProperty(propertyname) == null) {
            return false;
        } else {
            return true;
        }
    }

    public String print() {
        String str = "";
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                str += "Print property: " + list.get(i - 1)[0] + " = " + list.get(i - 1)[1] + "\n";
            }
        }
        Log.Debug(this, str);
        return str;
    }

    public void setChanged(boolean b) {
        this.changed = b;
    }

    /**
     * @return the changed
     */
    public boolean isChanged() {
        return changed;
    }
}
