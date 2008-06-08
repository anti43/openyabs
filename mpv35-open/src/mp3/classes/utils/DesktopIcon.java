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

package mp3.classes.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import mp3.classes.interfaces.ProtectedStrings;

/**
 *
 * @author anti43
 */
public class DesktopIcon implements ProtectedStrings{
    public static void createLinuxDesktopIcon(){
    
        BufferedWriter out = null;
        File fol = new File(USER_HOME + File.separator + PROG_NAME + File.separator + LIB_DIR);

        try {
            out = new BufferedWriter(new FileWriter(USER_HOME + File.separator + DESKTOP + File.separator + ICON_NAME_LIN, false));


            out.write("[Desktop Entry]");
            out.newLine();

            out.write("Encoding=UTF-8");
            out.newLine();

            out.write("Name=MPv3.5");
            out.newLine();

            out.write("GenericName=MP Rechnungs und Kundenverwaltung");
            out.newLine();

            out.write("Exec=java -jar " + System.getProperty("user.home") + File.separator + "MPv35" + File.separator + "mpv35.jar");
            out.newLine();

            out.write("Icon=" + fol + File.separator + "mpd.png");
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
//[InternetShortcut]
//URL=http://delphi.about.com
//IconIndex=0
//IconFile=C:\MyFolder\MyDelphiProgram.exe

            out.write("[InternetShortcut]");
            out.newLine();

            out.write("URL=file://"+System.getProperty("user.home") + File.separator + "MPv35" + File.separator + "mpv35.jar");
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
