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
import java.io.IOException;
import mp4.globals.Constants;
import mp4.logs.Log;
import mp4.main.Main;
import mp4.utils.files.FileReaderWriter;

/**
 * This class handles the different DB connection types (derby, mysql, custom)
 * @author Andreas
 */
public class ConnectionTypeHandler {

    /**
     * Use embedded derby database
     */
    public static final int DERBY = 0;
    /**
     * Use myql database driver
     */
    public static final int MYSQL = 1;
    /**
     * Use custom database driver
     */
    public static final int CUSTOM = 2;    //Available Drivers
    /**
     * Use single user database
     */
    public static final int SINGLE_USER = 0;
    /**
     * Use multi user database
     */
    public static final int MULTI_USER = 1;
    public static String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static String CUSTOM_DRIVER = "";    //Available SQL Files
    public static File DERBY_FILE = new File("ext/sql/derby.sql");
    public static File MYSQL_FILE = new File("ext/sql/mysql.sql");
    public static File CUSTOM_FILE = new File("ext/sql/custom.sql");

    public static boolean isInSingleUserMode() {
        if (MODE == ConnectionTypeHandler.SINGLE_USER) {
            return true;
        } else {
            return false;
        }
    }  

    static String getDriverName() {
        return CONNECTION_STRING;
    }
    private static String CONNECTION_STRING = null;
    private String URL = Main.settingsfile.getDBPath();
    private static Integer PREDEFINED_DRVER = 2;
    private static Integer MODE = 0;

    /**
     * Constructs a new ConnHandler
     */
    public ConnectionTypeHandler() {
        if (Main.settingsfile.getDBDriver().equals(DERBY_DRIVER)) {
            ConnectionTypeHandler.PREDEFINED_DRVER = ConnectionTypeHandler.DERBY;
            ConnectionTypeHandler.MODE = ConnectionTypeHandler.SINGLE_USER;
        } else if (Main.settingsfile.getDBDriver().equals(MYSQL_DRIVER)) {
            ConnectionTypeHandler.PREDEFINED_DRVER = ConnectionTypeHandler.MYSQL;
            ConnectionTypeHandler.MODE = ConnectionTypeHandler.MULTI_USER;
        } else {
            ConnectionTypeHandler.PREDEFINED_DRVER = ConnectionTypeHandler.CUSTOM;
            ConnectionTypeHandler.MODE = ConnectionTypeHandler.MULTI_USER;
        }
    }

    /**
     * Constructs a new ConnHandler with predefind ConnectionTypeHandler.Driver
     * @param driverset 
     */
    public ConnectionTypeHandler(int driverset) {
        ConnectionTypeHandler.PREDEFINED_DRVER = driverset;
    }

    /**
     * 
     * @param withCreate Shall we create a new database?
     * @return The DB connection string
     */
    public String getConnectionString(boolean withCreate) {

        switch (PREDEFINED_DRVER) {
            case DERBY:
                String cstring = "jdbc:derby:" + getURL() + File.separator + Constants.DATABASENAME + ";";
                if (withCreate) {
                    cstring += "create=true;";
                }
                setConnectionString(cstring);
                break;
            case MYSQL:
                setConnectionString("jdbc:mysql://" + getURL() + "/" + Constants.DATABASENAME);
                if (withCreate) {
                    Log.Debug(this, "Sie müssen die MYSQL Datenbank manuell anlegen.", true);
                }
                break;
            case CUSTOM:
                if (withCreate) {
                    Log.Debug(this, "Sie müssen die SQL Datenbank manuell anlegen.", true);
                }
                break;
        }
        return CONNECTION_STRING;
    }

    /**
     * Get the SQL command for creating the tables - for the choosen driver
     * @return The SQL command for creating the tables
     */
    public String[] getTableCreating_SQLCommand() {
        File filen = null;
        switch (PREDEFINED_DRVER) {
            case DERBY:
                filen = DERBY_FILE;
                break;
            case MYSQL:
                filen = MYSQL_FILE;
                break;
            case CUSTOM:
                filen = CUSTOM_FILE;
                break;
        }
        try {

            Log.Debug(this, "SQL Datei: " + filen.getCanonicalPath(), true);
        } catch (IOException ex) {
            Log.Debug(this, ex);
        }
        return new FileReaderWriter(filen).readLines();
    }

    /**
     * Override the JDBC connection string
     * @param conn_string
     */
    public void setConnectionString(String conn_string) {
        ConnectionTypeHandler.CONNECTION_STRING = conn_string;
    }

    /**
     * 
     * @return The DB location
     */
    public String getURL() {
        return URL;
    }

    /**
     * Set the DB location. May be file path, or network path
     * @param URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * Override the JDBC driver string
     * @param predefinedDriver
     */
    public void setDRIVER(int predefinedDriver) {
        ConnectionTypeHandler.PREDEFINED_DRVER = predefinedDriver;
    }

    /**
     * 
     * @return The JDBC driver string
     */
    public String getDriver() {
        switch (PREDEFINED_DRVER) {
            case DERBY:
                return DERBY_DRIVER;
            case MYSQL:
                return MYSQL_DRIVER;
            case CUSTOM:
                return CUSTOM_DRIVER;
        }
        return null;
    }
}
