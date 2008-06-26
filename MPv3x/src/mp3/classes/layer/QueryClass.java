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

package mp3.classes.layer;

import java.io.File;
import javax.swing.JProgressBar;
import mp3.database.util.Conn;
import mp3.classes.interfaces.Constants;


/**
 *
 * @author anti
 */
public class QueryClass extends mp3.database.util.Query implements mp3.classes.interfaces.Structure,Cloneable {

    private static QueryClass qc = null;
    private static mp3.classes.visual.main.mainframe mainf;
    private static JProgressBar bar;
    
    public static void kill() {
        qc=null;
             
                File f = new File(Constants.DATABASEPATH+File.separator+Constants.DATABASENAME +File.separator+"dbex.lck");
                f.deleteOnExit();
                File fi = new File(Constants.DATABASEPATH+File.separator+Constants.DATABASENAME +File.separator+"db.lck");
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
    private QueryClass() throws Exception {
        super(Conn.getConnection(), TABLE_MYDATA);

    }
    
    /**
     * 
     * @param bar
     * @param main
     */
    public static void setProgressBarOn (JProgressBar bar, mp3.classes.visual.main.mainframe main){
            QueryClass.mainf = main;
            QueryClass.bar=bar;
             
    }
    
    public static QueryClass instanceOf(){
    
    
        if(qc ==null) {
            try {
                qc = new QueryClass();
                qc.setProgressBar(bar, mainf);
                return qc;
            } catch (Exception ex) {
               Popup.warn(ex.getMessage(), Popup.ERROR);
            }
        }else {
            qc.setProgressBar(bar,mainf);
            return qc;
        }
        return null;
    }

   
    
}
