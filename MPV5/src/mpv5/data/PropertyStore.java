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
package mpv5.data;

import java.util.ArrayList;
import mpv5.logging.Log;

/**
 *
 * @author Administrator
 */
public class PropertyStore {

    public ArrayList<String[]> list = new ArrayList<String[]>();

    /**
     * Creates a new empty property store
     */
    public PropertyStore() {
    }

    /**
     * Creates a new property store and initially adds the given values
     * @param string
     */
    public PropertyStore(String[][] string) {
        for (int i = 0; i < string.length; i++) {
            addProperty(string[i][0], string[i][1]);
        }
    }

    /**
     * Adds a new Property
     * @param name
     * @param value
     */
    public void addProperty(String name, String value) {
        list.add(new String[]{name, value});
    }

    /**
     * 
     * @return The complete list of properties and values
     */
    public ArrayList getList() {
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
                if (list.get(i-1)[0].equals(name)) {
                    return list.get(i-1)[1];
                }
            }
        }
        return null;
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
                if (list.get(i-1)[0].equals(name)) {
                    list.set(i-1, new String[]{name, newvalue});
                    found = true;
                }
            }
        }
        if (!found) {
            addProperty(name, newvalue);
        }
    }

    public String print() {
       String str = "";
        if (list.size() > 0) {
            for (int i = list.size(); i > 0; i--) {
               str += list.get(i-1)[0] + ": " + list.get(i-1)[1] + "\n";
            }
        }
        Log.Debug(this, str);
        return str;
    }
}
