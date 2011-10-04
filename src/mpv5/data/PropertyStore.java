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
package mpv5.data;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 * PropertyStores is used to store key-value pairs
 * 
 */
public class PropertyStore {

    private final ArrayList<String[]> list = new ArrayList<String[]>();
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
     * Adds/changes all
     * @param data {key, value}
     */
    public synchronized void addAll(Object[][] data) {
        for (int i = 0; i < data.length; i++) {
            changeProperty(data[i][0].toString(), String.valueOf(data[i][1]));
        }
        setChanged(true);
    }

    /**
     * Adds a new Property, regardless if a property with that name already exists
     * @param name
     * @param value
     */
    public synchronized void addProperty(String name, String value) {
        if (value == null) {
            value = "";
        }
        Log.Debug(this, "Adding or replacing property: " + name + " = " + value);
        list.add(new String[]{name, value});
        setChanged(true);
    }

    /**
     *  Generate a List of String(key-value.toString()) pairs
     * @return The complete list of properties and values
     */
    public List<String[]> getList() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns the value of the LAST property with that name or NULL
     * @param name
     * @return the value
     */
    public synchronized String getProperty(String name) {
        String prop;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(name)) {
                    prop = list.get(i - 1)[1];
                    if (prop != null && !prop.equals("null") && prop.length() != 0) {
                        return prop;
                    }
                }
            }
        }
        changeProperty(name, null);
        return null;
    }

//    /**
//     * Return a double property. Will return NULL if the property is not parseable as double
//     * @param key
//     * @param desiredClass
//     * @return
//     */
//    public synchronized double getProperty(String key, double desiredClass) {
//        String t = getProperty(key);
//        if (t == null) {
//            return 0d;
//        } else {
//            try {
//                return Double.valueOf(t);
//            } catch (NumberFormatException numberFormatException) {
//                return 0d;
//            }
//        }
//    }
//    /**
//     * Convenience method to retrieve visual component properties stored as
//     * <br/>comp.getClass().getName() + "$" + source
//     * @param comp
//     * @param source
//     * @param b
//     * @return
//     */
//    public synchronized double getProperty(JComponent comp, String source, double b) {
//        return getProperty(comp.getClass().getName() + "$" + source, b);
//    }
//    /**
//     * Return a Integer property. Will return 0 if the property is not parseable as Integer
//     * @param key
//     * @param desiredClass
//     * @return
//     */
//    public synchronized int getProperty(String key, int desiredClass) {
//        String t = getProperty(key);
//        if (t == null) {
//            return 0;
//        } else {
//            try {
//                return Integer.valueOf(t);
//            } catch (NumberFormatException numberFormatException) {
//                return 0;
//            }
//        }
//    }
//
//    /**
//     * Convenience method to retrieve visual component properties stored as
//     * <br/>comp.getClass().getName() + "$" + source
//     * @param comp
//     * @param source
//     * @param b
//     * @return
//     */
//    public synchronized int getProperty(JComponent comp, String source, int b) {
//        return getProperty(comp.getClass().getName() + "$" + source, b);
//    }
//    /**
//     * Return a boolean property. Will return false if the property is not parseable as boolean
//     * @param key
//     * @param desiredClass
//     * @return
//     */
//    public synchronized boolean getProperty(String key, boolean desiredClass) {
//        String t = getProperty(key);
//        if (t == null) {
//            return false;
//        } else {
//            return Boolean.parseBoolean(t);
//        }
//    }
    /**
     * Return a property
     * @param <T>
     * @param key
     * @param desiredClass (one of Double, Integer, boolean, Date)
     * @return A value or a new instance of T
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends Object> T getProperty(String key, T desiredClass) {
        String t = getProperty(key);

        if (desiredClass instanceof Double) {
            if (t == null || String.valueOf(t).length() == 0) {
                return (T) new Double(0);
            } else {
                try {
                    return (T) Double.valueOf(t);
                } catch (NumberFormatException numberFormatException) {
                    return (T) new Double(FormatNumber.parseDezimal(t).doubleValue());
                }
            }
        } else if (desiredClass instanceof Integer) {
            if (t == null || String.valueOf(t).length() == 0) {
                return (T) new Integer(0);
            } else {
                try {
                    return (T) Integer.valueOf(t);
                } catch (NumberFormatException numberFormatException) {
                    return (T) new Double(FormatNumber.parseDezimal(t).intValue());
                }
            }
        } else if (desiredClass instanceof Boolean) {
            if (t == null || String.valueOf(t).length() == 0) {
                return (T) Boolean.FALSE;
            } else {
                try {
                    return (T) Boolean.valueOf(t);
                } catch (Exception e) {
                    return (T) Boolean.FALSE;
                }
            }
        } else if (desiredClass instanceof Date) {
            if (t == null || String.valueOf(t).length() == 0) {
                return (T) new Date();
            } else {
                try {
                    return (T) DateConverter.getDate(t);
                } catch (Exception e) {
                    return (T) new Date();
                }
            }
        } else {
            return (T) t;
        }
    }

    /**
     * Convenience method to retrieve (boolean) visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + source
     * @param comp
     * @param source
     * @return
     */
    public synchronized boolean getProperty(JComponent comp, String source) {
        return getProperty(comp.getClass().getName() + "$" + source, true);
    }

    /**
     * Convenience method to retrieve (boolean) visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + source
     * @param comp
     * @param source
     * @return
     */
    public synchronized boolean getProperty(String comp, String source) {
        return getProperty(comp + "$" + source, true);
    }

    /**
     * Convenience method to retrieve (boolean) visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + source
     * @param <T>
     * @param comp
     * @param source
     * @param type
     * @return
     */
    public synchronized <T extends Object> T getProperty(Component comp, String source, T type) {
        return getProperty(comp.getClass().getName() + "$" + source, type);
    }

    /**
     * Convenience method to retrieve visual component properties stored as
     * <br/>comp.getClass().getName() + "$" + source
     * @param <T>
     * @param comp
     * @param target
     * @param type
     * @return
     */
    public synchronized <T extends Object> T getProperty(Component comp, Component target, T type) {
        return getProperty(comp.getClass().getName() + "$" + target.getName(), type);
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not.
     * @param name
     * @param newvalue
     */
    public synchronized void changeProperty(String name, String newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(name)) {
                    list.set(i - 1, new String[]{name, newvalue});
                    found = true;
                    setChanged(true);
                    if (list.get(i - 1)[1] != null && !list.get(i - 1)[1].equals("null")) {
                        Log.Debug(this, "Change property: " + list.get(i - 1)[1] + " for " + name);
                    }
                }
            }
        }
        if (!found) {
            addProperty(name, newvalue);
        }
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not. Stored as comp.getClass().getName() + "$" + source
     * @param comp
     * @param source
     * @param newvalue
     */
    public synchronized void changeProperty(String comp, String source, Object newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(comp + "$" + source)) {
                    list.set(i - 1, new String[]{comp + "$" + source, String.valueOf(newvalue)});
                    found = true;
                    setChanged(true);
                    Log.Debug(this, "Change property: " + list.get(i - 1)[1] + " for " + comp + "$" + source);
                }
            }
        }
        if (!found) {
            addProperty(comp + "$" + source, String.valueOf(newvalue));
        }
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not. Stored as comp.getClass().getName() + "$" + source
     * @param comp
     * @param source
     * @param newvalue
     */
    public synchronized void changeProperty(JComponent comp, String source, Object newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(comp.getClass().getName() + "$" + source)) {
                    list.set(i - 1, new String[]{comp.getClass().getName() + "$" + source, String.valueOf(newvalue)});
                    found = true;
                    setChanged(true);
                    Log.Debug(this, "Change property: " + list.get(i - 1)[1] + " for " + comp.getClass().getName() + "$" + source);
                }
            }
        }
        if (!found) {
            addProperty(comp.getClass().getName() + "$" + source, String.valueOf(newvalue));
        }
    }

    /**
     * Changes the given property, if exists and
     * creates a new, if not. Stored as comp.getClass().getName() + "$" + source
     * @param comp
     * @param source
     * @param newvalue
     */
    public synchronized void changeProperty(Component comp, Component source, Object newvalue) {
        boolean found = false;
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
                if (list.get(i - 1)[0].equalsIgnoreCase(comp.getClass().getName() + "$" + source.getName())) {
                    list.set(i - 1, new String[]{comp.getClass().getName() + "$" + source.getName(), String.valueOf(newvalue)});
                    found = true;
                    setChanged(true);
                    Log.Debug(this, "Change property: " + list.get(i - 1)[1] + " for " + comp.getClass().getName() + "$" + source.getName());
                }
            }
        }
        if (!found) {
            addProperty(comp.getClass().getName() + "$" + source.getName(), String.valueOf(newvalue));
        }
    }

    /**
     * Returns True if the local property store does contain a value with the given key name
     * @param propertyname
     * @return True if the key exists
     */
    public synchronized boolean hasProperty(String propertyname) {
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

    public synchronized void setChanged(boolean b) {
        this.changed = b;
    }

    /**
     * @return the changed
     */
    public synchronized boolean isChanged() {
        return changed;
    }

    /**
     * Adds/changes all
     * @param store
     */
    public void addAll(PropertyStore store) {
        List<String[]> po = store.getList();
        for (int i = 0; i < po.size(); i++) {
            String[] strings = po.get(i);
            changeProperty(String.valueOf(strings[0]), String.valueOf(strings[1]));
        }
        setChanged(true);
    }

    /**
     * Clear all properties
     */
    public void removeAll() {
        list.clear();
        setChanged(true);
    }
}
