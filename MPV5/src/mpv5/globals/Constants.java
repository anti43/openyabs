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
package mpv5.globals;

import java.io.File;

/**
 *
 * @author anti43
 */
public interface Constants {

    /**
     * Version info
     */
    public static String VERSION = "4.00";
    public static String REVISION = "450";
    public static String RELEASE_VERSION = "4";
    public static String TITLE = "[Vienna]";
    public static String VERSION_URL = "Not Used";
    public static String JAR_NAME = "MPV4.jar";
    public static String PROG_NAME = "MP-" + Constants.VERSION + "";
    public static String JAVA_VERSION = "Java Version: " + System.getProperty("java.version");
    public static String PLUGIN_IDENTIFIER = "MP4Plugin";
    public static String PLUGIN_LOAD_CLASS = "plugin.Main";

    /**
     * Name of databasedir 
     */
    public static final String DATABASENAME = "mpv" + Constants.RELEASE_VERSION  + "Database";
    /**
     * Full path to database dir 
     */
//    public static final String MPPATH = Main.MPPATH;
  
    public static String SEP = File.separator;
    public static String TEMPLATES_DIR = "Vorlagen";
    public static String LIB_DIR = "lib";
    public static String PDF = "PDF";
    public static String OFFER_SAVE_DIR = "Angebote";
    public static String CACHE_DIR = "Cache";
    public static String PLUGIN_DIR = "Plugins";
    public static String BILL_SAVE_DIR = "Rechnungen";
    public static String ARREAR_SAVE_DIR = "Mahnungen";
    public static String PRODUCT_SAVE_DIR = "Produkte";
    public static String LIEFERSCHEIN_SAVE_DIR = "Lieferscheine";
    public static String BACKUPS_SAVE_DIR = "Backup";
    public static String ICON_NAME_LIN = "mp.desktop";
    public static String ICON_NAME_WIN = "MP-Rechnungs-Kundenverwaltung.url";

    public static String FELDTRENNER = ";";
   
}
