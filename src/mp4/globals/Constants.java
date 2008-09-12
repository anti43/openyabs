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

package mp4.globals;

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
    public static String TITLE = "[Teatime]";
    public static String VERSION_URL = "Not Used";
    public static String JAR_NAME = "MPV4.jar";
    public static String PROG_NAME = "MP-RUK_4";
    public static String SEP = File.separator;
    public static String JAVA_VERSION = "Java Version: " + System.getProperty("java.version");
    public static String PLUGIN_IDENTIFIER = "MP4Plugin";
       
    public String TEMPLATES_DIR = "templates";
    public String LIB_DIR = "lib";
    public String PDF = "PDF";
    public String OFFER_SAVE_DIR = "Angebote";
    public String CACHE_DIR = "Cache";
    public String PLUGIN_DIR = "Plugins";
    public String BILL_SAVE_DIR = "Rechnungen";
    public String ARREAR_SAVE_DIR = "Mahnungen";
    public String PRODUCT_SAVE_DIR = "Produkte";
    public String BACKUPS_SAVE_DIR =  "backups";
    public String ICON_NAME_LIN = "mp.desktop";
    public String ICON_NAME_WIN = "Rechnungs-Kundenverwaltung.url";
    public String DESKTOP = "Desktop";
    public String DBROOTDIR = System.getProperty("user.home") + File.separator + ".mp";
     /**
     * Home directory of user
     */
    public static String USER_HOME = System.getProperty("user.home");
    
     /**
     * Name of databasedir 
     */
    public static final String DATABASENAME = "mpv40Database";
   
     /**
     * Full path to database dir 
     */
    public static final String MPPATH = Constants.USER_HOME + File.separator +".mp" ;
    
    /**
     * Full path to settings file 
     */
    public static String SETTINGS_FILE  = Constants.USER_HOME + File.separator +".mp" + File.separator + "settings40.mp";

    public String[] MONTHS= new String[]{"Jan","Feb","Mar","Apr","Mai","Jun","Jul","Aug","Sep","Okt","Nov","Dez"};

    public String[] KONTEN_GRUPPEN = new String[]{
            "1110 Waren, Rohstoffe und Hilfsstoffe einschl. der Nebenkosten",
            "1120 Bezogene Leistungen (z.B. Fremdleistungen) ",
            "1130 Ausgaben für eigenes Personal  ",
            "1140 Aufwendungen für geringwertige Wirtschaftsgüter ",
            "1150 Miete / Pacht für Geschäftsräume und betrieblich genutzte Grundstücke ",
            "1160 Sonstige Aufwendungen für betrieblich genutzte Grundstücke ",
            "1170 Abziehbare Aufwendungen für ein häusliches Arbeitszimmer ",
            "1180 Reisekosten,Aufwendungen für doppelte Haushaltsführung ",
            "1190 Geschenke – abziehbar ",
            "1200 Geschenke – nicht abziehbar ",
            "1210 Bewirtung – abziehbar ",
            "1220 Bewirtung – nicht abziehbar ",
            "1230 Übrige Betriebsausgaben ",
            "1240 Fortbildung und Fachliteratur ",
            "1250 Rechts- und Steuerberatung, Buchführung ",
            "1260 Porto, Telefon, Büromaterial ",
            "1270 An das Finanzamt gezahlte und ggf. verrechnete Umsatzsteuer ",
            
            "<html>&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "Gezahlte Vorsteuerbeträge: &nbsp;",
            
            "<html>&nbsp;",
            
            "<html><p align=right><b><font color=red>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;Summe Betriebsausgaben: &nbsp;",
            "<html>&nbsp;",
            
            "2100 Betriebseinnahmen als umsatzsteuerlicher Kleinunternehmer ",
            "2110 Umsatzsteuerpflichtige Betriebseinnahmen ",
            "2120 Sonstige Sach-, Nutzungs- und Leistungsentnahmen ",
            "2130 Private Kfz-Nutzung ",
            "2140 Vom Finanzamt erstattete und ggf. verrechnete Umsatzsteuer",
            "<html>&nbsp;&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;" +
                    "Vereinnahmte Vorsteuerbeträge: &nbsp;",
            
            "<html>&nbsp;",
            "<html><p align=right><b><font color=green>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "Summe Betriebseinnahmen: &nbsp;",
            
            "<html>&nbsp;",
            
            "<html><p align=right>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "<b>Gesamtübersicht:&nbsp;",
            
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+&nbsp;Einnahmen: &nbsp;" +
                    "</b>",
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;" +
                    "Ausgaben: &nbsp;</b>",
            "<html><p align=right><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                                      "&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "=&nbsp;&nbsp;&nbsp;&nbsp;Summe: &nbsp;</b>"
                    
        };
   
}
