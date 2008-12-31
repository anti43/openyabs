/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.ui.dialogs.Popup;

/**
 *
 * @author Andreas
 */
public class DatabaseConnection {

    private static java.sql.Connection conn;
    private static DatabaseConnection connector;
    private ConnectionTypeHandler ctype;
    private static String user;
    private static String password;

    /**
     * 
     * @return Database connector
     * @throws Exception 
     */
    public static DatabaseConnection instanceOf() throws Exception {
        if (connector == null) {
            connector = new DatabaseConnection();
            connector.connect(false);
        }
        return connector;
    }
    private Statement statement;


    public java.sql.Connection getConnection() {
            return conn;
    }

    /**
     * Test-Verbindung zur Datenbank herstellen.
     * @param predefinedDriver
     * @param user
     * @param password
     * @param location
     * @param create
     * @return Connection
     * @throws Exception
     */
    public boolean connect(String predefinedDriver, String user, String password, String location, boolean create) throws Exception {

        ctype = new ConnectionTypeHandler();
        ctype.setDRIVER(predefinedDriver);
        ctype.setURL(location);

        try {
            Log.Debug(this, "Datenbanktreiber: " + ctype.getDriver(), true);
            Class.forName(ctype.getDriver()).newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            Popup.warn(ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();
        }

        try {
            Log.Debug(this, "Datenbankverbindung: " + ctype.getConnectionString(create), true);
            conn = DriverManager.getConnection(ctype.getConnectionString(create), user, password);
            if (conn != null && conn.isValid(10)) {
                connector = this;
                return true;
            } else {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Database Error: " + ex.getMessage());
            Log.Debug(this, ex);
            Log.Debug(this, ex.getNextException());
            Log.Debug(this, ex.getNextException().getNextException());
            Log.Debug(this, ex.getNextException().getNextException().getNextException());
            Popup.warn(ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();
            return false;
        }

    }

    /**
     * Verbindung zur Datenbank herstellen.
     * @return Connection
     */
    private Connection connect(boolean create) throws Exception {

        ctype = new ConnectionTypeHandler();
        try {
            Log.Debug(this, "Datenbanktreiber: " + ctype.getDriver(), true);
            try {
                Class.forName(ctype.getDriver()).newInstance();
            } catch (ClassNotFoundException ex) {
                Popup.warn(Messages.DB_DRIVER_INVALID + ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();

            }
            user = LocalSettings.getProperty("dbuser");
            password = LocalSettings.getProperty("dbpassword");

        } catch (Exception ex) {
            ex.printStackTrace();
            Popup.warn(ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();
        }

        try {
            Log.Debug(this, "Datenbankverbindung: " + ctype.getConnectionString(create), true);
            conn = DriverManager.getConnection(ctype.getConnectionString(create), user, password);

        } catch (SQLException ex) {
            System.out.println("Database Error: " + ex.getMessage());
            Log.Debug(this, ex);
            Log.Debug(this, ex.getNextException());
            Log.Debug(this, ex.getNextException().getNextException());
            Log.Debug(this, ex.getNextException().getNextException().getNextException());
            Popup.warn(ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();

            throw new Exception("Datenbank konnte nicht gestartet werden.");
        }
        return conn;
    }

    /**
     * Verbindung trennen
     */
    public static void shutdown() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    public boolean runQueries(String[] queries) throws SQLException{
        Statement stm = this.getConnection().createStatement();
        for (int i = 0; i < queries.length; i++) {
            stm.addBatch(queries[i]);
        }
        Log.PrintArray(queries);
        try {
            stm.executeBatch();
            return true;
        } catch (SQLException sQLException) {
            Log.Debug(sQLException);
            return false;
        }
    }
}
