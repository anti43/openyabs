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
import mp4.datenbank.verbindung.ConnectionTypeHandler;
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
public class LocalSettingsFile {

    private int lastUserID = 0;

    private FileReaderWriter file;
    private String[] dat;
    private File workfile;

    public LocalSettingsFile() {
        workfile = new File(Main.LOCAL_LOGIN_FILE);
        file = new FileReaderWriter(workfile);
    }

    /**
     * Delete file on exit
     */
    public void destroy() {
        workfile.deleteOnExit();
    }

    /**
     * Reads in the settings file
     * @return true if read in was successfull
     */
    public boolean read() {

        if(workfile.exists()) {
            try {
                dat = file.read1Line(Strings.COMMENT_SIGN).split(Constants.FELDTRENNER);
                if (!dat[0].equals("") && !dat[0].equals("null")) {
                    setLastUserID(Integer.valueOf(dat[0]));
                }
                 return true;
            } catch (Exception e) {
                Log.Debug(this, "Login file not valid:" + file, true);
                return false;
            }
        }

       return false;
    }

    /**
     * Creates a new settings file
     * @return this
     */
    public boolean create() {
        Log.Debug(this,"Anlegen der MP Login Datei: " + workfile.getParent(), true);

        workfile.getParentFile().mkdirs();

        if (workfile.getParentFile().canWrite()) {
            file.writeOnce(String.valueOf(getLastUserID()));
        } else {
            Popup.notice(Strings.PERMISSION_DENIED);
            System.err.println(Strings.PERMISSION_DENIED);
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
        if (workfile.getParentFile().canWrite()) {
            file.writeOnce(String.valueOf(getLastUserID()));
        } else {
            Popup.notice(Strings.PERMISSION_DENIED);
            System.err.println(Strings.PERMISSION_DENIED);
        }

    }

    public int getLastUserID() {
        return lastUserID;
    }

    public void setLastUserID(int lastUserID) {
        this.lastUserID = lastUserID;
    }

}
