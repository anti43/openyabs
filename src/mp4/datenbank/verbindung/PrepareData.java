/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mp4.datenbank.verbindung;

/**
 *
 * @author anti43
 */
public class PrepareData {

    public static String prepareString(String string) {
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
}
