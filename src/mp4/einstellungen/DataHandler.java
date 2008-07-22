/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp4.einstellungen;

import mp4.datenbank.verbindung.ConnectionHandler;
import mp3.classes.utils.Log;

/**
 *
 * @author Andreas
 */
public class DataHandler extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    public DataHandler(String table) {
        super(ConnectionHandler.instanceOf().clone(table));
    }

    public boolean getBoolean(String ofKey) {
        String values = getString(ofKey);
        try {
            if (Integer.valueOf(values).intValue() == 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception numberFormatException) {
            setString(ofKey, "0", true);
            return getBoolean(ofKey);
        }
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Integer getInteger(String ofKey) {
        String values = "";
        
        try {
            values = getString(ofKey);
        } catch (Exception e) {
            setString(ofKey, "0", true);
            return getInteger(ofKey);
        }
        try {
            return Integer.valueOf(values);
        } catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    void setBoolean(String ofKey, boolean value) {
        if (value) {
            if (!setString(ofKey, "1", false)) {
                setString(ofKey, "1", true);
            }
        } else {
            if (!setString(ofKey, "0", false)) {
                setString(ofKey, "0", true);
            }
        }
    }

    public String getString(String ofKey) {
        String[][] values = new String[1][1];
        try {
            values = this.select("wert", "name", ofKey, false);
        } catch (Exception e) {
            setString(ofKey, "", true);
            getString(ofKey);
        }
        if (values.length >= 1) {
            return values[0][0];
        } else {
            return null;
        }
    }

    public void setInteger(String ofKey, Integer value) {
        if (!setString(ofKey, value.toString(), false)) {
            setString(ofKey, value.toString(), true);
        }
    }

    private boolean setString(String key, String value, boolean newKey) {

        if (newKey) {
            Log.Debug("Creating new key: " + key);
            if (this.insert("name, wert", "(;;2#4#1#1#8#0#;;)" + key + "(;;2#4#1#1#8#0#;;)" + ",(;;2#4#1#1#8#0#;;)" + value + "(;;2#4#1#1#8#0#;;)") == 1) {
                return true;
            } else {
                return false;
            }
        } else {
            Log.Debug("Updating key: " + key);
            if (this.update("wert", "(;;2#4#1#1#8#0#;;)" + value + "(;;2#4#1#1#8#0#;;)", "name", key) == 1) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setString(String ofKey, String value) {

        if (!setString(ofKey, value, false)) {
            setString(ofKey, value, true);
        }
    }
}


