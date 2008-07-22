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

import java.sql.SQLException;
import mp3.classes.layer.QueryClass;
import mp3.classes.utils.Log;

/**
 *
 * @author Andreas
 */
public class DataHandler extends mp3.classes.layer.Things implements mp4.datenbank.struktur.Tabellen {

    public DataHandler(String table) {
        super(QueryClass.instanceOf().clone(table));
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
        String[][] values = new String[1][1];
        try {
            values = this.select("wert", "name", ofKey, false);
        } catch (Exception e) {
            setString(ofKey, "0", true);
            return getInteger(ofKey);
        }

        try {
            return Integer.valueOf(values[0][0]);
        } catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    void setBoolean(String key, boolean value) {
        if (value) {
            try {
                setString(key, "1", false);
            } catch (Exception e) {
                setString(key, "1", true);
            }
        } else {
            try {
                setString(key, "0", false);
            } catch (Exception e) {
                setString(key, "0", true);
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

    public void setInteger(String ofKey, Integer MAINFRAME_TAB) {
        try {
            setString(ofKey, MAINFRAME_TAB.toString(), false);
        } catch (Exception e) {
            setString(ofKey, MAINFRAME_TAB.toString(), true);
        }
    }

    private void setString(String key, String value, boolean newKey) {

        if (newKey) {
            this.insert("name, wert", "(;;2#4#1#1#8#0#;;)" + key + "(;;2#4#1#1#8#0#;;)" + ",(;;2#4#1#1#8#0#;;)" + value + "(;;2#4#1#1#8#0#;;)");
        } else {
            try {
                this.update("wert", "(;;2#4#1#1#8#0#;;)" + value + "(;;2#4#1#1#8#0#;;)", "name", key);
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }
    
     public void setString(String key, String value) {

         try {
             setString(key, value, false);
         } catch (Exception e) {
             setString(key, value, true);
         }
    }
}


