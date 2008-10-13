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
package mp4.einstellungen;

import java.io.File;
import mp4.datenbank.verbindung.ConnectionType;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.logs.Log;
import mp4.main.Main;
import mp4.utils.files.FileReaderWriter;

/**
 * This class holds control over the file-stored settings of MP
 * @author anti43
 */
public class SettingsFile {

    private String DBPath = Main.MPPATH;
    private String Version = Constants.VERSION;
    private String DBUser = "";
    private String DBPassword = "";
    private String DBDriver = ConnectionType.DERBY_DRIVER;
    private FileReaderWriter file;
    private String[] dat;
    private File workfile;

 
    public SettingsFile() {
        workfile = new File(Main.SETTINGS_FILE);
        file = new FileReaderWriter(workfile);
    }
     
    /**
     * Reads in the settings file
     * @return true if read in was successfull
     */
    public boolean read() {

        dat = file.read1Line().split(";");

        try {
            setVersion(dat[0]);
            setDBPath(dat[1]);
            setDBUser(dat[2]);
            setDBPassword(dat[3]);
            setDBDriver(dat[4]);
        } catch (Exception e) {
            Log.Debug("Settings file not valid:" + file, true);
            return false;
        }

        return true;
    }

    /**
     * Creates a new settings file
     * @return this
     */
    public boolean create() {
        Log.Debug("Anlegen der MP Settings Datei: " + workfile.getParent(), true);

        workfile.getParentFile().mkdirs();
        
        if (file.writeOnce(getVersion() + Strings.COLON +
                getDBPath() + Strings.COLON +
                getDBUser() + Strings.COLON +
                getDBPassword() + Strings.COLON +
                getDBDriver())) {
            file.write(Strings.SETTINGS_EXAMPLE);
        } else {
            Popup.notice(Strings.PERMISSION_DENIED);
            System.err.println(Strings.PERMISSION_DENIED);
            System.exit(1);
        }
        return workfile.canRead();
    }

    public File getFile() {
        return workfile;
    }

    /**
     * Saves the settings to file
     */
    public void save() {
        file.writeOnce(getVersion() + Strings.COLON +
                getDBPath() + Strings.COLON +
                getDBUser() + Strings.COLON +
                getDBPassword() + Strings.COLON +
                getDBDriver());
        file.write(Strings.SETTINGS_EXAMPLE);
    }

    public String getDBPath() {
        return DBPath;
    }

    public void setDBPath(String DBPath) {
        this.DBPath = DBPath;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String Version) {
        this.Version = Version;
    }

    public String getDBUser() {
        return DBUser;
    }

    public void setDBUser(String DBUser) {
        this.DBUser = DBUser;
    }

    public String getDBPassword() {
        return DBPassword;
    }

    public void setDBPassword(String DBPassword) {
        this.DBPassword = DBPassword;
    }

    public String getDBDriver() {
        return DBDriver;
    }

    public void setDBDriver(String DBDriver) {
        this.DBDriver = DBDriver;
    }
}
