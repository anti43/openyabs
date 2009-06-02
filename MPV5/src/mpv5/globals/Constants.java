/*
 * 
 *  *  This file is part of MP.
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

/**
 *
 *  anti43
 */
public interface Constants {

    /**
     * 
     * Full Version number
     */
    public static final String VERSION = "5.00-SVN";
    /**
     * Will be dynamically changed by SVN.
     */
    public static final String REVISION = "$Rev$";
    /**
     *
     * The Release version
     */
    public static final String RELEASE_VERSION = "5";
    /**
     *
     * Alias
     */
    public static final String TITLE = "[Foxy Ferret]";
    /**
     *
     * Name of the main JAR
     */
    public static final String JAR_NAME = "MPV5.jar";
    /**
     *
     * Program name
     */
    public static final String PROG_NAME = "MP-" + Constants.VERSION + "";
    /**
     *
     * Running Java Version info
     */
    public static String JAVA_VERSION = "Java Version: " + System.getProperty("java.version");
    /**
     *
     * The marker for valid plugins for this version
     */
    public static String PLUGIN_IDENTIFIER = "MP5Plugin";
    /**
     *
     * The class name to be loaded from plugins
     */
    public static String PLUGIN_LOAD_CLASS = "plugin.Main";
    /**
     * Name of databasedir 
     */
    public static final String DATABASENAME = "mpv" + Constants.RELEASE_VERSION + "db";

    public static String ICON_NAME_LIN = "mp.desktop";
    public static String ICON_NAME_WIN = "MP-Rechnungs-Kundenverwaltung.url";
    public static String XML_ROOT = "mpv5";
    public static String XML_DOCTYPE_ID = "-//http://code.google.com/p/mp-rechnungs-und-kundenverwaltung//DTD mpv5 V 1.0//EN";
    public static String XML_DOCTYPE_URL = "http://mp-rechnungs-und-kundenverwaltung.googlecode.com/files/mpv5.dtd";
    public static String SPLASH_IMAGE = "/mpv5/resources/images/background.png";
}
