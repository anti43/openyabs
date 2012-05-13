/*
 *  This file is part of YaBS.
 *  
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import mpv5.logging.Log;
import java.io.File;
import java.io.IOException;

import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;

/**
 * This class handles the different DB connection types (derby, mysql, custom)
 *  
 */
public class ConnectionTypeHandler {

    /**
     * Use embedded derby database
     */
    public static final int DERBY = 0;
    public static String DERBY_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    /**
     * Use myql database driver
     */
    public static final int MYSQL = 1;
    public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    /**
     * Use custom database driver
     */
    public static final int CUSTOM = 2;
    //Available Drivers
    public static String CUSTOM_DRIVER = "custom.driver";// (specify path with type declaration jdbc:sql://<path>:port)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String[] DRIVERS = {DERBY_DRIVER, MYSQL_DRIVER, CUSTOM_DRIVER};
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static File DERBY_FILE = null;
    public static File MYSQL_FILE = null;
    public static File CUSTOM_FILE = null;

    public static String getDriverName() {
        return CONNECTION_STRING;
    }

    public static int getDriverType() {
        return PREDEFINED_DRVER;
    }
    private static String CONNECTION_STRING = null;
    private static Integer PREDEFINED_DRVER = null;
    private static String URL;
    private static String DBNAME;
    private static String DBPREFIX = "";
    private static String CREATEDERBY = "create=true;";

    /**
     * Returns a database engine dependent version of CHAR() as a string
     * @param column
     * @return
     */
    public static String getToChar(String column) {
        if (getDriverType() == DERBY) {
            return "CHAR(" + column + ")";
        } else if (getDriverType() == MYSQL) {
            return "CAST(" + column + " AS CHAR)";
        } else {
            return "CHAR(" + column + ")";
        }
    }

    /**
     * Uses a database engine dependent version of CONCAT to concatenate column0 with all following cols
     * @param column0 
     * @param columnNames 
     * @return
     */
    public static String concat(String column0, String... columnNames) {
        String result = column0;
        for (int i = 0; i < columnNames.length; i++) {
            String column = columnNames[i];
            if (getDriverType() == DERBY) {
                result += "||" + column;
            } else if (getDriverType() == MYSQL) {
                result = " CONCAT(" + result + "," + column + ")";
            } else {
                result += "||" + column;
            }
        }

        return result;
    }

    /**
     * Constructs a new ConnHandler
     */
    public ConnectionTypeHandler() {

        setDRIVER(LocalSettings.getProperty(LocalSettings.DBDRIVER));
        ConnectionTypeHandler.URL = LocalSettings.getProperty(LocalSettings.DBPATH);
        ConnectionTypeHandler.DBNAME = LocalSettings.getProperty(LocalSettings.DBNAME);
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
                String cstring = "jdbc:derby:" + getURL() + File.separator + DBNAME + ";";
                if (withCreate) {
                    cstring += ConnectionTypeHandler.CREATEDERBY;
                }
                setConnectionString(cstring);
                break;
            case MYSQL:
                if (withCreate) {
                    setConnectionString("jdbc:mysql://" + getURL());
                } else {
                    setConnectionString("jdbc:mysql://" + getURL() + "/" + DBNAME);
                }
                break;
            case CUSTOM:
                setConnectionString(getURL() + "/" + DBNAME);
                if (withCreate) {
                    Log.Debug(this, Messages.CREATE_DATABASE_OWN.toString());
                }
                break;
        }
        return CONNECTION_STRING;
    }

    /**
     * Get the SQL command for creating the tables - for the choosen driver
     * @return The SQL command for creating the tables
     * @deprecated Will always return null. Use {@link DatabaseInstallation#getStructure()} instead.
     */
    @Deprecated
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

            Log.Debug(this, "SQL Datei: " + filen.getCanonicalPath());
        } catch (IOException ex) {
            Log.Debug(this, ex);
        }
//        if (filen.exists()) {
//            return new FileReaderWriter(filen).readLines();
//        } else {
//            Log.Debug(this, "SQL Datei " + filen.getName() + " not found. Trying in-build SQL Script.");
//            return Struktur.SQL_COMMAND;
//        }
        return null;
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
        ConnectionTypeHandler.URL = URL;
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

    /**
     *
     * @param predefinedDriver
     */
    public final void setDRIVER(String predefinedDriver) {

        if (predefinedDriver != null && !predefinedDriver.equalsIgnoreCase("null")) {
            if (predefinedDriver.equalsIgnoreCase(DERBY_DRIVER)) {
                PREDEFINED_DRVER = DERBY;
            } else if (predefinedDriver.equalsIgnoreCase(MYSQL_DRIVER)) {
                PREDEFINED_DRVER = MYSQL;
            } else {
                PREDEFINED_DRVER = CUSTOM;
                CUSTOM_DRIVER = predefinedDriver;
            }
        }
    }

    /**
     * Try to load the given driver class
     * @param driver
     * @throws ClassNotFoundException
     */
    public static void testDriver(String driver) throws ClassNotFoundException {
        Class.forName(driver);
    }

    public static String getDBNAME() {
        return DBNAME;
    }
    
    public void setDBName(String dbname) {
        ConnectionTypeHandler.DBNAME = dbname;
    }

    public void setPrefix(String prefix) {
        DBPREFIX = prefix;
    }

    public String getPrefix() {
        return DBPREFIX;
    }

    public static String getCREATEDERBY() {
        return CREATEDERBY;
    }

   public static void setCREATEDERBY(String CREATEDERBY) {
        ConnectionTypeHandler.CREATEDERBY = CREATEDERBY;
    }
    
    public static void resetCREATEXXX() {
       CREATEDERBY = "create=true;";
    }
}
