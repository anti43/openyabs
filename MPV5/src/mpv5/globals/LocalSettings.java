/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.xml.XMLReader;
import mpv5.utils.xml.XMLWriter;

/**
 *
 *  Andreas
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
    public static final String CACHE_DIR = "cachedir";
   
    public static final String DEFAULT_FONT = "defaultfont";
    public static final String PRINT_DEVAPP = "devappprint";
    public static final String PROXYUSE = "useproxy";
    public static final String DBNAME = "dbname";
    public static final String DBROW_LIMIT = "dbrowlimit";
    public static final String DBAUTOLOCK = "dbautolock";

     private static PropertyStore predefinedSettings = new PropertyStore(new String[][]{ //        {LAF,UIManager.getSystemLookAndFeelClassName()}
                {DEFAULT_FONT, "Tahoma"}, {DBROW_LIMIT, "250"}, {DBAUTOLOCK, "true"}
            });

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

        if (!getProperty(DBROW_LIMIT).equals("null")) {
            QueryHandler.setRowLimit(Integer.valueOf(getProperty(DBROW_LIMIT)));
        } else {
            setProperty(DBROW_LIMIT, "0");
        }

          if (!getProperty(DBAUTOLOCK).equals("null")) {
            DatabaseObject.setAutoLockEnabled(TypeConversion.stringToBoolean(getProperty(DBAUTOLOCK)));
        } else {
            setProperty(DBAUTOLOCK, "0");
        }
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
    private static int connectionID = 1;

    /**
     * Specify the connection id to be used from the config file, default is 1
     * @param id
     */
    public static void setConnectionID(Integer id) {
        connectionID = id;
    }

    /**
     * Get the connection id to be used from the config file
     * @return
     */
    public static int getConnectionID() {
        return connectionID;
    }

    /**
     *
     * @return
     */
    public static PropertyStore getPropertyStore() {
        return cookie;
    }

    /**
     * Returns True if the local property store does contain a value with the given key name
     * @param propertyname
     * @return True if the key exists
     */
    public static boolean hasProperty(String propertyname) {
        return cookie.hasProperty(propertyname);
    }

    /**
     * Add or change a property
     * @param name
     * @param value
     */
    public static void setProperty(String name, String value) {
        if (value == null) {
            value = "null";
        }
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
            Popup.warn(Messages.ERROR_SAVING_LOCALSETTINGS);
            Log.Debug(LocalSettings.class, ex);
        }
    }

    /**
     * Read the local settings from disk
     * @throws java.lang.Exception
     */
    public static void read() throws Exception {

        try {
            Log.Debug(LocalSettings.class, "Reading in local settings where ID =" + connectionID);
            XMLReader read = new XMLReader();
            read.newDoc(new File(Main.SETTINGS_FILE), false);
            cookie = read.readInto("localsettings", "connection", String.valueOf(connectionID), cookie);
//            cookie.print();
            Log.Debug(LocalSettings.class, "Finished local settings.");
        } catch (Exception e) {
            throw new FileNotFoundException(Main.SETTINGS_FILE);
        }
    }

    /**
     *
     * @return The local settings file
     */
    public static File getLocalFile() {
        return new File(Main.SETTINGS_FILE);
    }
}
