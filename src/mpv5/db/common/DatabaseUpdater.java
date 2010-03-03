package mpv5.db.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.objects.Template;
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
        UPDATES_DERBY.put(1.12, new String[]{
                    "ALTER TABLE templates ADD COLUMN printer VARCHAR(50) DEFAULT '" + Template.PRINTER_UNDEFINED + "' NOT NULL ",});
        UPDATES_DERBY.put(1.14, new String[]{
                    "ALTER TABLE products DROP CONSTRAINT const8",});
        UPDATES_DERBY.put(1.15, new String[]{
//            intaddedby,dateadded,productsids,contactsids,groupsids,cname
                    "CREATE TABLE productstosuppliers(ids BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY " +
                            "(START WITH 1, INCREMENT BY 1), productsids BIGINT REFERENCES products(ids) ON DELETE CASCADE, " +
                            "contactsids BIGINT REFERENCES contacts(ids) ON DELETE CASCADE," +
                            "cname VARCHAR(250) DEFAULT NULL," +
                            "groupsids BIGINT DEFAULT 0," +
                            "dateadded DATE NOT NULL," +
                            "intaddedby BIGINT DEFAULT 0)",});
        ////////////////////////////////////////////////////////////////////////////////////////////
        // mysql updates
        UPDATES_MYSQL.put(1.11, new String[]{
                    "ALTER TABLE products ADD COLUMN stockvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN thresholdvalue DOUBLE DEFAULT 0 NOT NULL",
                    "ALTER TABLE products ADD COLUMN intinventorytype SMALLINT DEFAULT 0 NOT NULL"
                });
        UPDATES_MYSQL.put(1.12, new String[]{
                    "ALTER TABLE templates ADD COLUMN printer VARCHAR(50) DEFAULT '" + Template.PRINTER_UNDEFINED + "' NOT NULL ",});
        UPDATES_MYSQL.put(1.14, new String[]{
                    "ALTER TABLE products DROP KEY CONST8",});
        UPDATES_MYSQL.put(1.15, new String[]{
                    "CREATE TABLE productstosuppliers ("
                    + "ids             	BIGINT(20) UNSIGNED NOT NULL PRIMARY KEY auto_increment,"
                    + "productsids      BIGINT(20) UNSIGNED NOT NULL,"
                    + "accountsids      BIGINT(20) UNSIGNED NOT NULL,"
                    + "cname            VARCHAR(250) DEFAULT NULL," +
                      "groupsids        BIGINT(20) DEFAULT 1," +
                      "dateadded        DATE NOT NULL," +
                      "intaddedby       BIGINT(20) UNSIGNED DEFAULT 0"
                    + ")ENGINE=MyISAM DEFAULT CHARSET=utf8",

                    "ALTER TABLE productstosuppliers "
                    + "ADD CONSTRAINT products0843168601 "
                    + "FOREIGN KEY(contactsids) "
                    + "REFERENCES contacts(ids) "
                    + "ON DELETE CASCADE ",

                    "ALTER TABLE productstosuppliers "
                    + "ADD CONSTRAINT products0843168602 "
                    + "FOREIGN KEY(productsids) "
                    + "REFERENCES products(ids) "
                    + "ON DELETE CASCADE "
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
                        if (vers > newVersion) {
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
                        if (vers > newVersion) {
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
