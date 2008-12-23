/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.io.File;
import java.util.Locale;
import mpv5.data.PropertyStore;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.utils.xml.XMLWriter;

/**
 *
 * @author Andreas
 */
public class LocalSettings {

    private static PropertyStore cookie = new PropertyStore();

    public static String getProperty(String name) {
        if (cookie.getProperty(name) != null) {
            return cookie.getProperty(name);
        } else {
            cookie.addProperty(name, "NA");
        }
        return cookie.getProperty(name);
    }

    public static void setProperty(String name, String value) {
        cookie.changeProperty(name, value);
    }

    public static void save(){

        XMLWriter x = new XMLWriter();
        x.createOrReplace(new File(""));
    }

    public static void read(){


    }


}
