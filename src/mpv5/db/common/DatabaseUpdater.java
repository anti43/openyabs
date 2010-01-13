package mpv5.db.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;

/**
 * This class provides database updates
 * @author anti
 */
public class DatabaseUpdater {

    private HashMap<Double, String[]> UPDATES_DERBY = new HashMap<Double, String[]>();
    private HashMap<Double, String[]> UPDATES_MYSQL = new HashMap<Double, String[]>();
//stockvalue
//thresholdvalue
//    private int inventorytype = 1;

    public DatabaseUpdater() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // derby updates
        UPDATES_DERBY.put(1.11, new String[]{
                    "ALTER TABLE products ADD COLUMN stockvalue DOUBLE DEFAULT 0 NOT NULL ",
                    "ALTER TABLE products ADD COLUMN thresholdvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN intinventorytype SMALLINT DEFAULT 0 NOT NULL"
                });
        ////////////////////////////////////////////////////////////////////////////////////////////
        // mysql updates
        UPDATES_MYSQL.put(1.11, new String[]{
                    "ALTER TABLE products ADD COLUMN stockvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN thresholdvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN intinventorytype SMALLINT DEFAULT 0 NOT NULL"
                });

    }

    /**
     * Update the database from the specified to the max available version
     * @param version
     */
    public void updateFrom(double version) {
        Log.Debug(DatabaseUpdater.class, "Updating database from " + version);

        double newVersion = version;
        
        if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
            for (Iterator<Double> keys = UPDATES_DERBY.keySet().iterator(); keys.hasNext();) {
                Double vers = keys.next();
                if (vers.doubleValue() > version) {
                    String[] val = UPDATES_DERBY.get(vers);
                    try {
                        DatabaseConnection.instanceOf().runQueries(val);
                        if(vers > newVersion) {
                            newVersion = vers;
                        }
                    } catch (Exception ex) {
                        Log.Debug(ex);
                    }
                }
            }
        } else {
            for (Iterator<Double> keys = UPDATES_MYSQL.keySet().iterator(); keys.hasNext();) {
                Double vers = keys.next();
                if (vers.doubleValue() > version) {
                    String[] val = UPDATES_MYSQL.get(vers);
                    try {
                        DatabaseConnection.instanceOf().runQueries(val);
                        if(vers > newVersion) {
                            newVersion = vers;
                        }
                    } catch (Exception ex) {
                        Log.Debug(ex);
                    }
                }
            }
        }

        setVersion(newVersion);
    }

    private void setVersion(double newVersion) {
        try {
            DatabaseConnection.instanceOf().runQueries(
                    new String[]{
                "UPDATE globalsettings SET value ='" + newVersion + "' WHERE cname = 'yabs_dbversion'"});
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }

    }
}
