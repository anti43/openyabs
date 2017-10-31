package mpv5.globals;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
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
 *  
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
    public static final String OFFICE_HOME = "ooohome";
    public static final String OFFICE_PORT = "oooport";
    public static final String OFFICE_HOST = "ooohost";
    public static final String OFFICE_LOCALSERVER = "ooolocalserver";
    public static final String OFFICE_BINARY_FOLDER = "ooobinaryfolder";
    public static final String OFFICE_REMOTE = "oootremoteserver";
    public static final String OFFICE_USE = "ooouse";
    public static final String OFFICE_ALT = "oooalt";
    public static final String CACHE_DIR = "cachedir";
    public static final String DEFAULT_FONT = "defaultfont";
    public static final String PRINT_DEVAPP = "devappprint";
    public static final String PROXYUSE = "useproxy";
    public static final String DBNAME = "dbname";
    public static final String DBPREFIX = "dbprefix";
    public static final String DBAUTOLOCK = "dbautolock";
    /**
     * single, multi
     */
    public static final String DBTYPE = "dbtype";
    public static final String SERVER_PORT = "serverport"; 
    public static final String CACHE_SIZE = "cachesize";
    public static final String LAST_USER = "lastuser";
    public static final String LAST_USER_PW = "lastuserpw";
    public static final String DBESCAPE = "dbescape";
    public static final String CLIENT = "client";
    public static final String CALCULATOR = "calculator";
    public static final String BASE_DIR = "basedir";
    public static final String BACKUP_DIR = "backupdir";
    public static final String SUPPRESS_UPDATE_CHECK = "noupdate";
    public static final String CMD_PASSWORD = "cmdpassword";
    public static final String CMD_USER = "cmduser";
    public static final String OFFICE_COMMAND = "ooocommand";
    public static final String OFFICE_AUTOSTART = "oooautostart";
    public static final String OFFICE_KEEPSERVER = "oookeepserver";

    private static PropertyStore predefinedSettings = new PropertyStore(new String[][]{
                {CLIENT, "Default Client"},
                {DEFAULT_FONT, "DejaVu Sans 11"},
                {DBAUTOLOCK, "false"},
                {SERVER_PORT, "4343"},
                {CACHE_SIZE, "500"},
                {LAST_USER, "1"},
                {LAST_USER_PW, "5F4DCC3B5AA765D61D8327DEB882CF99"},
                {OFFICE_PORT, "8100"},
                {OFFICE_HOST, "127.0.0.1"},
                {OFFICE_REMOTE, "false"},
                {OFFICE_USE, "false"},
                {OFFICE_ALT, "true"},
                {OFFICE_LOCALSERVER, "false"},
                {OFFICE_AUTOSTART, "false"},
                {CACHE_DIR, "Cache"},
                {DBESCAPE, "true"},
                {CALCULATOR, ""},
                {CMD_PASSWORD, ""},
                {SUPPRESS_UPDATE_CHECK, "false"}, // MacOS
            //                {OFFICE_HOME, "/Applications/OpenOffice.org.app/Contents/"},
            //                {OFFICE_BINARY_FOLDER, "MacOS"},
            //                {DBTYPE, "multi"}
            // Windows
            //                {OFFICE_BINARY_FOLDER, "program"},
            //                {OFFICE_HOME, ""},
            });
    private static Vector<PropertyStore> cookies;
  
    /**
     * Applies the environmental settings
     */
    public static synchronized void apply() {
        Properties systemSettings = System.getProperties();

        //Proxy settings
        if (hasProperty(PROXYHOST)) {
            systemSettings.put("http.proxyHost", getProperty(PROXYHOST));
            systemSettings.put("http.proxyPort", getProperty(PROXYPORT));
            if (hasProperty(PROXYUSER)) {
                Authenticator.setDefault(new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(getProperty(PROXYUSER), getProperty(PROXYPASSWORD).toCharArray());
                    }
                });
            }
        }//End proxy settings

//        if (hasProperty(DBROW_LIMIT)) {
//            QueryHandler.setRowLimit(Integer.valueOf(getProperty(DBROW_LIMIT)));
//        } else {
//            setProperty(DBROW_LIMIT, "0");
//        }

        if (hasProperty(DBAUTOLOCK)) {
            DatabaseObject.setAutoLockEnabled(TypeConversion.stringToBoolean(getProperty(DBAUTOLOCK)));
        } else {
            setProperty(DBAUTOLOCK, "0");
        }
//        if (!getProperty(SCROLL_ALWAYS).equals("null")) {
//            mpv5.YabsViewProxy.instance().setTabPaneScrolled(TypeConversion.stringToBoolean(getProperty(SCROLL_ALWAYS)));
//        }
    }

    /**
     * Get a properties value, or 0 if N/A
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized static int getIntegerProperty(String name) {

        if (cookie.getProperty(name) != null) {
            return Integer.valueOf(cookie.getProperty(name));
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "0");
        }

        return Integer.valueOf(cookie.getProperty(name));
    }

    /**
     * Get a properties value, or false if N/A
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized static boolean getBooleanProperty(String name) {

        if (cookie.getProperty(name) != null) {
            return TypeConversion.stringToBoolean(cookie.getProperty(name));
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "false");
        }

        return TypeConversion.stringToBoolean(cookie.getProperty(name));
    }

    /**
     * Get a properties value, or 0 if N/A
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized static double getDoubleProperty(String name) {

        if (cookie.getProperty(name) != null) {
            return Double.valueOf(cookie.getProperty(name));
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "0");
        }

        return Double.valueOf(cookie.getProperty(name));
    }

    /**
     * Get a properties value, or the String "null" if N/A
     * @param name
     * @return
     */
    public static synchronized String getProperty(String name) {
        if (cookie.getProperty(name) != null) {
            return cookie.getProperty(name);
        } else if (predefinedSettings.getProperty(name) != null) {
            cookie.changeProperty(name, predefinedSettings.getProperty(name));
        } else {
            cookie.changeProperty(name, "null");
        }
        return cookie.getProperty(name);
    }
    private static int connectionID = 0;

    /**
     * Specify the connection id to be used from the config file, default is 0
     * @param id
     */
    public static void setConnectionID(Integer id) {
        connectionID = id;
        Log.Debug(LocalSettings.class, "Using conn id: " + id);
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
        PropertyStore p = new PropertyStore();
        p.addAll(predefinedSettings.getList().toArray(new String[][]{}));
        p.addAll(cookie.getList().toArray(new String[][]{}));
        return p;
    }

    /**
     * Returns True if the local property store does contain a value with the given key name
     * @param propertyname
     * @return True if the key exists
     */
    public static boolean hasProperty(String propertyname) {
        return (cookie.hasProperty(propertyname)
                && !cookie.getProperty(propertyname).equals("null")
                && cookie.getProperty(propertyname).length() != 0)
                || (predefinedSettings.hasProperty(propertyname)
                && !predefinedSettings.getProperty(propertyname).equals("null"));
    }

    /**
     * Add or change a property
     * @param name
     * @param value
     */
    public static synchronized void setProperty(String name, String value) {
        if (value == null) {
            value = "null";
        }
        Log.Debug(LocalSettings.class, "Changing property '" + name + "' to: " + value);
        cookie.changeProperty(name, value);
    }

    /**
     * Save the local settings to disk, default connection id
     */
    public static synchronized void save() {
        save(connectionID);
    }

    /**
     * Read the local settings from disk
     * @throws java.lang.Exception
     */
    public static synchronized void read() throws Exception {

        try {
            File ls = new File(Main.SETTINGS_FILE);
            if (!ls.canRead()) {
                throw new RuntimeException(ls + " not readable, check permissions!");
            }
            if (!ls.canWrite()) {
                throw new RuntimeException(ls + " not writeable, check permissions!");
            }
            Log.Debug(LocalSettings.class, "Reading in local settings where ID =" + connectionID);
            XMLReader read = new XMLReader();
            read.newDoc(ls, false);
            cookies = (Vector<PropertyStore>) read.readInto("localsettings", "connection");
            for (int i = 0; i < cookies.size(); i++) {
                PropertyStore propertyStore = cookies.get(i);
                if (propertyStore.getProperty("nodeid").equals(String.valueOf(connectionID))) {
                    Log.Debug(LocalSettings.class, "Requested connection id found: " + connectionID);
                    cookie = propertyStore;
                }
            }
//            cookie.print();
            Log.Debug(LocalSettings.class, "Finished local settings.");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     *
     * @return The local settings file
     */
    public static synchronized File getLocalFile() {
        return new File(Main.SETTINGS_FILE);
    }

    /**
     * Test if the current settings file contains the given connection id
     * @param connectionID 
     * @return
     * @throws Exception
     */
    public static boolean hasConnectionID(Integer connectionID) throws Exception {
        XMLReader read = new XMLReader();
        read.newDoc(new File(Main.SETTINGS_FILE), false);
        if (read.readInto("localsettings", "connection", String.valueOf(connectionID), new PropertyStore()).getList().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Save the local settings to disk
     * @param forConnId
     */
    public synchronized static void save(Integer forConnId) {
        Log.Debug(LocalSettings.class, "Writing local settings: " + forConnId);
        if (cookies == null) {
            cookies = new Vector<PropertyStore>();
        }
        if (forConnId != null) {
            connectionID = forConnId;
            cookie.changeProperty("nodeid", forConnId.toString());
            cookies.add(cookie);
        } else {
            connectionID = 0;
            cookie.changeProperty("nodeid", String.valueOf(connectionID));
            cookies.add(cookie);
        }
        XMLWriter x = new XMLWriter();
        try {
            x.newDoc("localsettings", false);
            if (cookies != null) {
                x.parse("connection", cookies);
            } else {
                x.parse("localsettings", String.valueOf(connectionID), cookie);
            }
            x.createOrReplace(new File(Main.SETTINGS_FILE));
        } catch (Exception ex) {
            Popup.warn(Messages.ERROR_SAVING_LOCALSETTINGS);
            Log.Debug(LocalSettings.class, ex);
        }
    }

    /**
     * Finds all available connection IDs
     * @return
     * @throws Exception On read errors
     */
    public static List<Integer> getConnectionIDs() throws Exception {
        List<Integer> list = new Vector<Integer>();
        XMLReader read = new XMLReader();
        read.newDoc(new File(Main.SETTINGS_FILE), false);
        cookies = (Vector<PropertyStore>) read.readInto("localsettings", "connection");
        for (int i = 0; i < cookies.size(); i++) {
            PropertyStore propertyStore = cookies.get(i);
            try {
                list.add(propertyStore.getProperty("nodeid", 0));
            } catch (Exception e) {
                //possibly nodeid is not integer parseable, lets ignore them
                Log.Debug(e);
            }
        }

        return list;
    }

    /**
     * Find all locally available connections
     * @return
     * @throws Exception
     * @deprecated named instances not supported yet
     */
    public static Map<Integer, String> getConnections() throws Exception {
        Map<Integer, String> list = new TreeMap<Integer, String>();

        XMLReader read = new XMLReader();
        read.newDoc(new File(Main.SETTINGS_FILE), false);
        cookies = (Vector<PropertyStore>) read.readInto("localsettings", "connection");
        for (int i = 0; i < cookies.size(); i++) {
            PropertyStore propertyStore = cookies.get(i);
            list.put(propertyStore.getProperty("nodeid", 0), propertyStore.getProperty("nodename"));
        }

        return list;
    }

    /**
     * Removes a connection instance from the local settings file
     * @param forConnId
     * @throws Exception
     */
    public static void removeInstance(Integer forConnId) throws Exception {
        if (cookies == null) {
            cookies = new Vector<PropertyStore>();
        }
        if(forConnId==null){
           forConnId = 0;
        }

        for (int i = 0; i < cookies.size(); i++) {
            PropertyStore propertyStore = cookies.get(i);
            if (String.valueOf(propertyStore.getProperty("nodeid")).equals(String.valueOf(forConnId))) {
                cookies.remove(propertyStore);
            }
        }

        XMLWriter x = new XMLWriter();
        try {
            x.newDoc("localsettings", false);
            if (cookies != null) {
                x.parse("connection", cookies);
                x.createOrReplace(new File(Main.SETTINGS_FILE));
            }
        } catch (Exception ex) {
            Popup.warn(Messages.ERROR_SAVING_LOCALSETTINGS);
            Log.Debug(LocalSettings.class, ex);
        }
    }
}
