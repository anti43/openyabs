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
import mp4.logs.Log;
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
    public static final int CUSTOM = 2;   
    
    //Available Drivers
    public static String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static String CUSTOM_DRIVER = "";    
    //Available SQL Files
    public static File DERBY_FILE = new File("ext/sql/derby.sql");
    public static File MYSQL_FILE = new File("ext/sql/mysql.sql");
    public static File CUSTOM_FILE = new File("ext/sql/custom.sql");    
    //Connection string
    private String CONNECTION_STRING = null;
    private String URL = null;
    private int mode = 0;

    
    public ConnectionType(){}
    
    public ConnectionType(int mode, String dburl){
        this.mode = mode;
        this.URL = dburl;
    }
    
    public String getConnectionString(boolean withCreate) {

        switch (mode) {
            case DERBY:
                setURL("jdbc:derby:" + getURL() + ";create=" + withCreate + ";");
                break;
            case MYSQL:
                setURL("jdbc:mysql:" + getURL());
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
        return URL;
    }
    
    public String getSQL_Command(){
          switch (mode) {
            case DERBY:
                return new FileReaderWriter(DERBY_FILE).read();
            case MYSQL:
                return new FileReaderWriter(MYSQL_FILE).read();
            case CUSTOM:
                return new FileReaderWriter(CUSTOM_FILE).read();
        }
          return null;
    }

    public void setCONNECTION_STRING(String CONNECTION_STRING) {
        this.CONNECTION_STRING = CONNECTION_STRING;
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
