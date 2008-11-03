/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp4.datenbank.verbindung;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mp4.datenbank.installation.*;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.logs.*;
import mp4.main.Main;
import mp4.panels.misc.SplashScreen;

/**
 *
 * @author anti43
 */
public class Conn implements Strings {

    private static Conn connector;
    private ConnectionTypeHandler ctype;

    public static void reboot() throws Exception {
        Conn.shutdown();
        connector = new Conn();
    }

    /**
     * 
     * @return Database connector
     * @throws Exception 
     */
    public static Conn instanceOf() throws Exception {
        if (connector == null) {
          
            connector = new Conn();
        }
        return connector;
    }

    private static Statement statement;
    private static java.sql.Connection conn;
    private static boolean tablesCreated = false;
    private SplashScreen splash;
    private String user = null;
    private String password = null;

    private Conn() throws Exception {
        if (splash != null) {
            splash.setMessage(DB_INIT);
        }

    }

    /**
     * MP Datenbank anlegen und Daten einfuegen
     * @throws Exception 
     */
    public void createDatabase() throws Exception {
        getCreatingConnection();
        tablesCreated = this.query(ctype.getTableCreating_SQLCommand());
        this.query(Daten.SQL_COMMAND);
        Conn.shutdown();
    }

    /**
     * Verbindung zur Datenbank herstellen. 
     * @return Connection
     */
    private Connection connect(boolean create) throws Exception {
        
        ctype = new ConnectionTypeHandler();
        // Treiber laden
        try {
            Log.Debug(this,"Datenbanktreiber: " + ctype.getDriver(), true);
            Class.forName(ctype.getDriver()).newInstance();
            user = Main.settingsfile.getDBUser();
            password = Main.settingsfile.getDBPassword();

        } catch (Exception ex) {
            ex.printStackTrace();
            Popup.warn(ex.getMessage(), Popup.ERROR);
            Conn.shutdown();
        }

        // Verbindung herstellen
        try {
            Log.Debug(this,"Datenbankverbindung: " + ctype.getConnectionString(create), true);
            conn = DriverManager.getConnection(ctype.getConnectionString(create), user, password);
            // Benötige Ressourcen für eine SQL-Anweisung bereitstellen 
            statement = conn.createStatement();
            
        } catch (SQLException ex) {
            System.out.println("Database Error:" + ex.getMessage());
//            ex.printStackTrace();
            Log.Debug(this,ex);
            Popup.warn(ex.getMessage(), Popup.ERROR);
            Conn.shutdown();
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
            ConnectionHandler.unlockDbOnExit();
        } catch (SQLException ex) {

            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Query ausführen
     * @param query 
     * 
     */
    public static void query(String query) {
        String message = "Database Error:";
        statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            statement.execute(query);
            System.out.println(statement.getUpdateCount());
        } catch (SQLException ex) {
            System.out.println(query);
            System.out.println(message + ex.getMessage());
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
        }
    }

    /**
     * Alle Tabellennamen anzeigen
     * @param table 
     * 
     */
    public static void showTable(String table) {
        String query = "select * from " + table;
        String message = "Database error:";
        statement = null;
        ResultSet resultSet = null;

        try {
            // Select-Anweisung ausführen
            statement = conn.createStatement();
            resultSet = statement.executeQuery(query);
            // Alle Tabllennamen anzeigen
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }
        } catch (SQLException ex) {
            System.out.println(message + ex.getMessage());
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
        }
    }

    public static Connection getCreatingConnection() throws Exception {
        if (connector == null) {
            connector = new Conn();
        }
        return connector.connect(true);
    }

    public static Connection getConnection() throws Exception {
        if (connector == null) {
            connector = new Conn();
        }
        return connector.connect(false);
    }

    private boolean query(String[] querys) {
        String message = "Database error:";
        statement = null;
        ResultSet resultSet = null;
        boolean error = false;

        try {

            statement = conn.createStatement();
            for (int i = 0; i < querys.length; i++) {

                try {
                    Log.Debug(this,querys[i]);
                    statement.execute(querys[i]);
                } catch (SQLException e) {
                    System.err.println(message + e.getMessage());
                    Log.Debug(this,e);
                    error = true;
                }

            }
        } catch (SQLException ex) {
            System.err.println(message + ex.getMessage());
            return false;
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    System.out.println(message + ex.getMessage());
                }
            }
        }
        if (!error) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTablesCreated() {
        return tablesCreated;
    }

    public void setSplash(SplashScreen splash) {
        this.splash = splash;
    }
}
