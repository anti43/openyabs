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
import mpv5.ui.popups.Popup;
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
        try {
            return conn;
        } catch (Exception ex) {
           return null;
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
            Class.forName(ctype.getDriver()).newInstance();
            user = LocalSettings.getDBUser();
            password = LocalSettings.getDBPassword();

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

}
