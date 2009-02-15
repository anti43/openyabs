/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
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
    public static final String DBDRIVER = "dbdriver";
    public static final String DBUSER = "dbuser";
    public static final String DBPASSWORD = "dbpassword";
    public static final String PROXYHOST = "proxyhost";
    public static final String PROXYPORT = "proxyport";
    public static final String PROXYUSER = "proxyuser";
    public static final String PROXYPASSWORD = "proxypassword";
    public static final String OFFICE_HOME = "/opt/openoffice.org3/";
    private static PropertyStore predefinedSettings = new PropertyStore(new String[][]{ //        {LAF,UIManager.getSystemLookAndFeelClassName()}
                {"defaultfont", "Tahoma"}
            });
    public static final String DEFAULT_FONT = "defaultfont";
    public static String PRINT_DEVAPP = "devappprint";
    public static String PROXYUSE = "useproxy";

    /**
     * Applies the environmental settings
     */
    public static void apply() {
        Properties systemSettings = System.getProperties();

        //Proxy settings
        if (!getProperty(PROXYHOST).equals("null")) {
            systemSettings.put("http.proxyHost", getProperty(PROXYHOST));
            systemSettings.put("http.proxyPort", getProperty(PROXYPORT));
            if (!getProperty(PROXYUSER).equals("null")) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(getProperty(PROXYUSER), getProperty(PROXYPASSWORD).toCharArray());
                    }
                });
            }
        }//End proxy settings
    }

    /**
     * Get a properties value, or the String "null" of N/A
     * @param name
     * @return
     */
    public static String getProperty(String name) {
        if (cookie.getProperty(name) != null) {
            return cookie.getProperty(name);
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "null");
        }
        return cookie.getProperty(name);
    }

    /**
     * Add or change a property
     * @param name
     * @param value
     */
    public static void setProperty(String name, String value) {
        Log.Debug(LocalSettings.class, "Changing property '" + name + "' to: " + value);
        cookie.changeProperty(name, value);
    }

    /**
     * Save the local settings to disk
     */
    public static void save() {
        XMLWriter x = new XMLWriter();
        try {
            x.newDoc("localsettings");
            x.parse("connection", "1", cookie);
            x.createOrReplace(new File(Main.SETTINGS_FILE));
        } catch (Exception ex) {
            Popup.error(Messages.ERROR_SAVING_LOCALSETTINGS, ex);
            Log.Debug(LocalSettings.class, ex);
        }
    }

    /**
     * Read the local settings from disk
     * @throws java.lang.Exception
     */
    public static void read() throws Exception {

        Log.Debug(LocalSettings.class, "Reading in local settings..");
        XMLReader read = new XMLReader();
        read.newDoc(new File(Main.SETTINGS_FILE));
        cookie = read.readInto("connection", cookie);
        cookie.print();
        Log.Debug(LocalSettings.class, "Finished local settings.");
    }

    /**
     *
     * @return The local settings file
     */
    public static File getLocalFile() {
        return new File(Main.SETTINGS_FILE);
    }
}
