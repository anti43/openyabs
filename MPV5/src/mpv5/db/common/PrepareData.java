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

import mpv5.logging.Log;

/**
 *
 * @author anti43
 */
public class PrepareData {

    public static String prepareString(String string) {
        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.MYSQL) {
           string = maskBackslashes(string);
        }
        return "(;;2#4#1#1#8#0#;;)" + string + "(;;2#4#1#1#8#0#;;)" + "(;;,;;)";
    }

    public static String prepareNumber(Number number) {
        return number + "(;;,;;)";
    }

    public static String prepareBoolean(Boolean bool) {
        if (bool) {
            return "1" + "(;;,;;)";
        } else {
            return "0" + "(;;,;;)";
        }
    }

    public static String finalize(String datastring) {
        if (datastring.endsWith("(;;,;;)")) {
            return datastring.substring(0, datastring.lastIndexOf("(;;,;;)"));
        } else {
            return datastring;
        }
    }

    public static Double parseNumber(String number) {
        return Double.valueOf(number);
    }

    public static boolean parseBoolean(String bool) {
        if (bool.matches("1") || bool.matches("true")) {
            return true;
        } else {
            return false;
        }
    }

    public static String maskBackslashes(String string) {
        Log.Debug(PrepareData.class, "Masking Backslashes!");
        return string.replaceAll("\\\\","\\\\\\\\");
     }
}

