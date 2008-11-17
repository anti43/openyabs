/*
 * 
 * 
 */
package mp4.datenbank.verbindung;

import mp4.logs.Log;

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
