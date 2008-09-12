/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package mp4.installation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mp4.globals.Constants;

/**
 *
 * @author anti43
 */
public class DesktopIcon implements Constants{
    public static void createLinuxDesktopIcon(){
    
        BufferedWriter out = null;
        File fol = new File(USER_HOME + File.separator + PROG_NAME + File.separator + LIB_DIR);

        try {
            out = new BufferedWriter(new FileWriter(USER_HOME + File.separator + DESKTOP + File.separator + ICON_NAME_LIN, false));


            out.write("[Desktop Entry]");
            out.newLine();

            out.write("Encoding=UTF-8");
            out.newLine();

            out.write("Name="+Constants.PROG_NAME);
            out.newLine();

            out.write("GenericName=MP Rechnungs und Kundenverwaltung");
            out.newLine();

            out.write("Exec=java -jar " + Constants.USER_HOME + File.separator + Constants.PROG_NAME + File.separator + Constants.JAR_NAME);
            out.newLine();

            out.write("Icon=" + Constants.USER_HOME + File.separator + Constants.PROG_NAME + File.separator +"lib" + File.separator + "mpd.png");
            out.newLine();

            out.write("Type=Application");
            out.newLine();

            out.write("Terminal=False");
            out.newLine();

            out.write("StartupNotify=false");
            out.newLine();

            out.write("Categories=Office;X-SuSE-Core-Office;");
            out.newLine();


            out.close();
        } catch (IOException ex) {
           ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    
    }
    
        public static void createWindowsDesktopIcon(){
    
        BufferedWriter out = null;
        File fol = new File(USER_HOME + File.separator + PROG_NAME + File.separator + LIB_DIR);

        try {
            out = new BufferedWriter(new FileWriter(USER_HOME + File.separator + DESKTOP + File.separator + ICON_NAME_WIN, false));
            out.write("[InternetShortcut]");
            out.newLine();
            out.write("URL=file://"+Constants.USER_HOME + File.separator + Constants.PROG_NAME + File.separator + Constants.JAR_NAME);
            out.newLine();
            out.close();
        } catch (IOException ex) {
           ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    
    }
}
