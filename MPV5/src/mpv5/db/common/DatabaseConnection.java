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
            connector.connect(true);
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
        // Treiber laden
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

        // Verbindung herstellen
        try {
            Log.Debug(this, "Datenbankverbindung: " + ctype.getConnectionString(create), true);
            conn = DriverManager.getConnection(ctype.getConnectionString(create), user, password);
            // Benötige Ressourcen für eine SQL-Anweisung bereitstellen
            statement = conn.createStatement();

        } catch (SQLException ex) {
            System.out.println("Database Error:" + ex.getMessage());
//            ex.printStackTrace();
            Log.Debug(this, ex);
            Popup.warn(ex.getMessage(), Popup.ERROR);
            DatabaseConnection.shutdown();
//            System.exit(1);

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
