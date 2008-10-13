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
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.globals.Constants;
import mp4.logs.Log;
import mp4.main.Main;
import mp4.utils.files.FileReaderWriter;

/**
 *
 * @author Andreas
 */
public class ConnectionType {

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
    public static String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static String CUSTOM_DRIVER = "";    //Available SQL Files
    public static File DERBY_FILE = new File("ext/sql/derby.sql");
    public static File MYSQL_FILE = new File("ext/sql/mysql.sql");
    public static File CUSTOM_FILE = new File("ext/sql/custom.sql");    //Connection string
    private String CONNECTION_STRING = null;
    private String URL = Main.settings.getDBPath();
    private int mode = 2;

    public ConnectionType() {
        if (Main.settings.getDBDriver().equals(DERBY_DRIVER)) {
            this.mode = 0;
        } else if (Main.settings.getDBDriver().equals(MYSQL_DRIVER) ) {
            this.mode = 1;
        } else {
            this.mode = 2;
        }
    }

    public ConnectionType(int mode) {
        this.mode = mode;
    }

    public String getConnectionString(boolean withCreate) {

        switch (mode) {
            case DERBY:
                String cstring = "jdbc:derby:" + getURL() + File.separator + Constants.DATABASENAME + ";";
                if (withCreate) {
                    cstring += "create=true;";
                }
                setConnectionString(cstring);
                break;
            case MYSQL:
                setConnectionString("jdbc:mysql://" + getURL() + ":3306/" +  Constants.DATABASENAME + ";");
                if (withCreate) {
                    Log.Debug("Sie müssen die MYSQL Datenbank manuell anlegen.", true);
                }
                break;
            case CUSTOM:
                if (withCreate) {
                    Log.Debug("Sie müssen die SQL Datenbank manuell anlegen.", true);
                }
                break;
        }
        return CONNECTION_STRING;
    }

    public String[] getSQL_Command() {
       File filen = null;
        switch (mode) {
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

            Log.Debug("SQL Datei: " + filen.getCanonicalPath(), true);
        } catch (IOException ex) {
            Log.Debug(ex);
        }
        return new FileReaderWriter(filen).readLines();
    }

    public void setConnectionString(String conn_string) {
        this.CONNECTION_STRING = conn_string;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getDriver() {
        switch (mode) {
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
