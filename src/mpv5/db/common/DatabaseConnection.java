/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.sql.Driver;
import javax.swing.JProgressBar;
import mpv5.logging.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import mpv5.globals.LocalSettings;
import mpv5.ui.dialogs.Popup;

/**
 *
 *  
 */
public class DatabaseConnection {

    private static java.sql.Connection conn;
    private static DatabaseConnection connector;

    /**
     * @return the connector
     */
    public static DatabaseConnection getConnector() {
        return connector;
    }

    /**
     * @return the user
     */
    public static String getUser() {
        return user;
    }

    /**
     * @return the password
     */
    public static String getPassword() {
        return password;
    }

    /**
     * @return the prefix
     */
    public static String getPrefix() {
        return prefix;
    }
    private ConnectionTypeHandler ctype;
    private static String user;
    private static String password;
    private static String prefix = "";

    /**
     * 
     * @return Database connector
     * @throws Exception 
     */
    public static synchronized DatabaseConnection instanceOf() throws Exception {
        if (getConnector() == null) {
            connector = new DatabaseConnection();
            getConnector().connect(false);
        }
        return getConnector();
    }
    private Statement statement;
    private JProgressBar prog;

    /**
     *
     * @return
     */
    public java.sql.Connection getConnection() {
        return conn;
    }

    /**
     * Test-Verbindung zur Datenbank herstellen.
     * @param predefinedDriver
     * @param user
     * @param password
     * @param location
     * @param dbname
     * @param create
     * @return Connection
     * @throws Exception
     */
    public boolean connect(String predefinedDriver, String user, String password, String location, String dbname, String prefix, boolean create) throws Exception {

        try {
            ctype = new ConnectionTypeHandler();
            getCtype().setDRIVER(predefinedDriver);
            getCtype().setURL(location);
            getCtype().setDBName(dbname);
            getCtype().setPrefix(prefix);
            DatabaseConnection.user = user;
            DatabaseConnection.password = password;
            DatabaseConnection.prefix = prefix;

            DriverManager.registerDriver((Driver) Class.forName(getCtype().getDriver()).newInstance());
            Log.Debug(this, "Driver: " + getCtype().getDriver());
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }

        return reconnect(create);

    }

    public boolean reconnect(boolean create) throws SQLException {
        try {
            Log.Debug(this, "RECONNECT::Datenbankverbindung: " + getCtype().getConnectionString(create));
            conn = DriverManager.getConnection(getCtype().getConnectionString(create), user, password);
            if (conn != null //&& conn.isValid(10)//does not work with MySQL Connector/J 5.0
                    ) {
                connector = this;
                return true;
            } else {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Database Error: " + ex.getMessage());
            Popup.notice(ex.getLocalizedMessage());
            Log.Debug(this, ex);
            Log.Debug(this, ex.getNextException());

            DatabaseConnection.shutdown();
            throw ex;
        }
    }

    /**
     * Verbindung zur Datenbank herstellen.
     * @return Connection
     */
    private Connection connect(boolean create) throws Exception {

        ctype = new ConnectionTypeHandler();

        if (getCtype().getDriver() != null) {
            Log.Debug(this, "Datenbanktreiber: " + getCtype().getDriver());

            try {
                Class.forName(getCtype().getDriver()).newInstance();
            } catch (ClassNotFoundException ex) {
//                Popup.error(ex);
                //possibly not fatal, driver can have a different class [org.apache.derby.jdbc.ClientDriver,org.apache.derby.jdbc.EmbeddedDriver]
                Log.Debug(this, ex.getMessage());
            }
            user = LocalSettings.getProperty("dbuser");
            password = LocalSettings.getProperty("dbpassword");
            prefix = LocalSettings.getProperty("dbprefix");

            reconnect(create);
        } else {
            throw new UnsupportedOperationException("Datenbanktreiber: undefined");
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

    public boolean runQueries(String[] queries) throws SQLException {
        if (prog != null) {
            prog.setStringPainted(true);
            prog.setMaximum(queries.length);
            prog.setMinimum(0);
            prog.setValue(0);
        }
        Statement stm = this.getConnection().createStatement();
//        for (int i = 0; i < queries.length; i++) {
//            stm.addBatch(queries[i]);
//        }
//        Log.PrintArray(queries);
        try {
//            stm.executeBatch();
            for (int i = 0; i < queries.length; i++) {
                try {
                    String string = queries[i];
                    if (prefix != null) {
                        string = string.replace(" table ", " table " + prefix);
                        string = string.replace(" TABLE ", " TABLE " + prefix);
                        string = string.replace(" on ", " on " + prefix);
                        string = string.replace(" ON ", " ON " + prefix);
                        string = string.replace(" into ", " into " + prefix);
                        string = string.replace(" INTO ", " INTO " + prefix);
                    }
                    Log.Print(string);
                    stm.execute(string);
                    if (prog != null) {
                        prog.setValue(i);
                    }
                    Thread.sleep(100);
                } catch (Exception ex) {
                    throw ex;
                }
            }
            return true;
        } catch (Exception sQLException) {
            Log.Debug(this, sQLException.getMessage());
            return false;
        } finally {
            if (prog != null) {
                prog.setValue(0);
            }
        }
    }

    /**
     * Set a progressbar
     * @param progressbar
     */
    public void setProgressbar(JProgressBar progressbar) {
        prog = progressbar;
    }

    /**
     * @return the ctype
     */
    public ConnectionTypeHandler getCtype() {
        return ctype;
    }

    /**
     * @return the statement
     */
    public Statement getStatement() {
        return statement;
    }
}
