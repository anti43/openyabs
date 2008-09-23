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
import java.sql.*;
import mp4.globals.Constants;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.utils.files.FileReaderWriter;
import mp4.items.visual.Popup;
import mp4.logs.*;
import mp4.panels.misc.SplashScreen;
import mp4.datenbank.installation.Daten;
import mp4.datenbank.installation.Struktur;
import mp4.main.Main;

/**
 *
 * @author anti43
 */
public class Conn implements Strings {

    private static Conn connector;

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

    /**
     * 
     * @param url 
     * @param create 
     * @return Database connector
     * @throws Exception 
     */
    public static Conn instanceOf(String url, boolean create) throws Exception {
        if (connector == null) {
            connector = new Conn(url, create);
        }
        return connector;

    }
    private static Statement statement;
    private static java.sql.Connection conn;
    /**
     * Verbindung zur Datenbank
     */
    /**
     * JDBC-Treiber-Name. Muss im Klassenpfad sein.
     */
    private static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    /**
     * Verbindungs-URL. Erstellt beim ersten Aufruf eine neue Datenbank.
     */
    private static String URL = "";
    private static boolean tablesCreated = false;
    private SplashScreen splash;

    /**
     * 
     * @param url
     * @param create 
     */
    private Conn(String url, boolean create) throws Exception {
        if (splash != null) {
            splash.setMessage(DB_INIT);
        }
        URL = "jdbc:derby:" + url + ";create=" + create + ";";
        this.connect();
//        try {
//            FileReaderWriter f = new FileReaderWriter(Constants.SETTINGS_FILE);
//            String[] dat = f.read().split(";");
//            f.write(url + ";" + dat[1]);
//
//        } catch (Exception exception) {
//
//            Popup.notice(SETTINGS_NOT_FOUND + exception.getMessage());
//        }

//        tablesCreated = this.query(Structure.tables);
        tablesCreated = this.query(Struktur.SQL_COMMAND);
        this.query(Daten.SQL_COMMAND);
        Conn.reboot();
    }

    /**
     * 
     */
    private Conn() throws Exception {
        if (splash != null) {
            splash.setMessage(DB_INIT);
        }
        try {
            FileReaderWriter rw = new FileReaderWriter(Main.SETTINGS_FILE);
            String[] dat = rw.read().split(";");
            URL = "jdbc:derby:" + dat[0] + File.separator + Constants.DATABASENAME;
        } catch (Exception exception) {
            Popup.notice(SETTINGS_NOT_FOUND + exception.getMessage());
        }
        this.connect();

    }

    /**
     * Verbindung zur Datenbank herstellen. 
     * @return Connection
     */
    private Connection connect() throws Exception {



        // Treiber laden
        try {
            Class.forName(DRIVER).newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();

            Popup.warn(ex.getMessage(), Popup.ERROR);
            Conn.shutdown();
        }

        // Verbindung herstellen
        try {
            conn = DriverManager.getConnection(URL);
            // Benötige Ressourcen für eine SQL-Anweisung bereitstellen 
            statement = conn.createStatement();
            new File(Constants.USER_HOME+File.separator+"derby.log").deleteOnExit();
        } catch (SQLException ex) {
            System.out.println("Database Error:" + ex.getMessage());
            ex.printStackTrace();
            Log.Debug(ex);
//            Popup.warn(ex.getMessage(), Popup.ERROR);
            Conn.shutdown();
            System.exit(1);

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


            File f = new File(Main.MPPATH + File.separator + Constants.DATABASENAME + File.separator + "dbex.lck");
            f.deleteOnExit();
            File fi = new File(Main.MPPATH+ File.separator + Constants.DATABASENAME + File.separator + "db.lck");
            fi.deleteOnExit();
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

//    @Override
//    public void finalize(){
//        
//        Out.out("Closing connection..");
////        try {
////            conn.close();
////        } catch (SQLException ex) {
////            Logger.getLogger(Conn.class.getName()).log(Level.SEVERE, null, ex);
////        }
//    }
    public static Connection getConnection() throws Exception {
        if (connector == null) {
            connector = new Conn();
        }
        return connector.connect();
    }

    private boolean query(String[] querys) {
        String message = "Database error:";
        statement = null;
        ResultSet resultSet = null;

        try {

            statement = conn.createStatement();
            for (int i = 0; i < querys.length; i++) {
                Log.Debug(querys[i]);
                statement.execute(querys[i]);

            }
        } catch (SQLException ex) {
            System.out.println(message + ex.getMessage());
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
        return true;
    }

    public static boolean isTablesCreated() {
        return tablesCreated;
    }

    public void setSplash(SplashScreen splash) {
        this.splash = splash;
    }
}
