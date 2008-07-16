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
package mp3.classes.interfaces;

import java.io.File;

/**
 *
 * @author anti43
 */
public interface ProtectedStrings {

    public String JAR_NAME = "mpv36-open.jar";
    public String PROG_NAME = "MP_Rechnungs-Kundenverwaltung_3.6";
    public String USER_HOME = System.getProperty("user.home");
    public String SEP = File.separator;
    public String JAVA_VERSION = "Java Version: " + System.getProperty("java.version");
    public String TEMPLATES_DIR = "templates";
    public String LIB_DIR = "lib";
    public String PDF = "PDF";
    public String OFFER_SAVE_DIR = "Angebote";
    public String BILL_SAVE_DIR = "Rechnungen";
    public String ARREAR_SAVE_DIR = "Mahnungen";
    public String BACKUPS_SAVE_DIR =  "backups";
    public String ICON_NAME_LIN = "mp.desktop";
    public String ICON_NAME_WIN = "Rechnungs-Kundenverwaltung.url";
    public String DESKTOP = "Desktop";
    
   
}
