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

import mp4.items.visual.Popup;
import java.io.File;
import mp4.globals.Constants;
import mp4.main.Main;

/**
 *
 * @author anti
 */
public class ConnectionHandler extends mp4.datenbank.verbindung.Query implements mp4.datenbank.installation.Tabellen, Cloneable {

    private static ConnectionHandler qc = null;

    public static void unlockDbOnExit() {
        qc = null;

        File f = new File(Main.MPPATH+ File.separator + Constants.DATABASENAME + File.separator + "dbex.lck");
        f.deleteOnExit();
        File fi = new File(Main.MPPATH+ File.separator + Constants.DATABASENAME + File.separator + "db.lck");
        fi.deleteOnExit();

    }
//    /***
//     * Open the configurationfile "settings.mp"
//     * @return the values in the file
//     */
//    public static String getPath() {
//        FileReaderWriter rw = new FileReaderWriter("settings.mp");
//        return rw.read();
//    }
    /**
     * 
     * Singleton!
     */
    private ConnectionHandler() throws Exception {
        super(TABLE_CONFIG_DATA);
    }

    public static ConnectionHandler instanceOf() {
        if (qc == null) {
            try {
                qc = new ConnectionHandler();
                
                return qc;
            } catch (Exception ex) {
                if(Popup.Y_N_dialog(ex.getMessage() + "\nProgramm/Instanz beenden?")) {
                    System.exit(2);
                } 
            }
        } else {
            
            return qc;
        }
        return null;
    }
}
