/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.io.File;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.xml.XMLReader;
import mpv5.utils.xml.XMLWriter;

/**
 *
 * @author Andreas
 */
public class LocalSettings {

    private static PropertyStore cookie = new PropertyStore();
    public static final String DBPATH = "dbpath";
    public static String DBDRIVER = "dbdriver";
    public static String DBUSER = "dbuser";
    public static String DBPASSWORD = "dbpassword";
    public static String LAF = "laf";
    private static PropertyStore predefinedSettings = new PropertyStore(new String[][]{
                //        {LAF,UIManager.getSystemLookAndFeelClassName()}
                {LAF, "de.muntjak.tinylookandfeel.TinyLookAndFeel"}
            });

    public static String getProperty(String name) {
        if (cookie.getProperty(name) != null) {
            return cookie.getProperty(name);
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "NA");
        }
        return cookie.getProperty(name);
    }

    public static void setProperty(String name, String value) {
        Log.Debug(LocalSettings.class, "Changing property '" + name + "' to: " + value);
        cookie.changeProperty(name, value);
    }

    public static void save() {
        XMLWriter x = new XMLWriter();
        try {
            x.newDoc("localsettings");
            x.parse( "connection","1", cookie);
            x.createOrReplace(new File(Main.SETTINGS_FILE));
        } catch (Exception ex) {
            Popup.error(Messages.ERROR_SAVING_LOCALSETTINGS, ex);
            Log.Debug(LocalSettings.class, ex);
        }
    }

    public static void read() throws Exception {

        Log.Debug(LocalSettings.class, "Reading in local settings..");
        XMLReader read = new XMLReader();
        read.newDoc(new File(Main.SETTINGS_FILE));
        cookie = read.readInto("connection", cookie);
        cookie.print();
        Log.Debug(LocalSettings.class, "Finished local settings.");
    }
}
