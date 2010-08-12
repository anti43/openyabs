
package mpv5.globals;

import java.util.List;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.logging.Log;
import mpv5.utils.text.TypeConversion;

/**
 *
 *  
 */
public class GlobalSettings {

    private static PropertyStore cookie = new PropertyStore();
    private static PropertyStore predefinedSettings = new PropertyStore(new String[][]{
               
            });

    /**
     * Applies the environmental settings
     */
    public static synchronized void apply() {
     
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
    private static int connectionID = 2;

    /**
     * Specify the connection id to be used from the config table, default is 2
     * @param id
     */
    public static void setConnectionID(Integer id) {
        connectionID = id;
        Log.Debug(GlobalSettings.class, "Using conn id: " + id);
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
        return (cookie.hasProperty(propertyname) && !cookie.getProperty(propertyname).equals("null")) || (predefinedSettings.hasProperty(propertyname) && !predefinedSettings.getProperty(propertyname).equals("null"));
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
        Log.Debug(GlobalSettings.class, "Changing property '" + name + "' to: " + value);
        cookie.changeProperty(name, value);
    }


    /**
     * Read the global settings from DB
     * @throws java.lang.Exception
     */
    public static synchronized void read() throws Exception {

        try {
            Log.Debug(GlobalSettings.class, "Reading in global settings where ID =" + connectionID);
           
            QueryCriteria2 c = new QueryCriteria2();
            c.and(new QueryParameter(Context.getGlobalSettings(), "groupsids", connectionID, QueryParameter.EQUALS));

            Object[][] data = QueryHandler.instanceOf().clone(Context.getGlobalSettings()).select("cname, value", c).getData();
            cookie.addAll(data);

            Log.Debug(GlobalSettings.class, "Finished global settings.");
        } catch (Exception e) {
            Log.Debug(GlobalSettings.class, e.getCause().getMessage());
        }
    }



    /**
     * Save the global settings to DB
     */
    public synchronized static void save() {
        //Remove old data
        QueryHandler.instanceOf().clone(Context.getGlobalSettings()).delete(new QueryCriteria("groupsids", connectionID));
        //Write new values
        List<String[]> list = cookie.getList();

        for (int i = 0; i < list.size(); i++) {
            String[] val = list.get(i);
            QueryData data = new QueryData();
            data.add("groupsids", connectionID);
            data.add("cname", val[0]);
            data.add("value", val[1]);
            QueryHandler.instanceOf().clone(Context.getGlobalSettings()).insert(data, null);
        }
    }
}
