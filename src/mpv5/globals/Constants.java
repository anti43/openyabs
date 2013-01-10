/*
 * 
 *  *  This file is part of YaBS.
 *  *  
 *  *      YaBS is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      YaBS is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mpv5.globals;

import java.math.BigDecimal;

/**
 *
 * 
 */
public interface Constants {

    /**
     * 
     * Full Version number
     */
    public static final String VERSION = "Yabs-1.2-Beta2";

    /**
     * The original database version of this release
     */
    public static final Double DATABASE_ORIGINAL_VERSION = 1.0;
    /**
     * The minimal Database Version which is compatible with this release
     */
    public static final Double DATABASE_VERSION = 1.1974;
    /**
     * The max Database Version which is compatible with this release
     */
    public static final Double DATABASE_MAX_VERSION = 2.0;
    /**
     * Will be dynamically changed by SVN.
     */
    public static final String REVISION = "$Rev: $";
    /**
     *
     * The Release version
     */
    public static final String RELEASE_VERSION = "0";
    /**
     *
     * Alias
     */
    public static final String TITLE = "Yabs - Marla";
    /**
     *
     * Name of the main JAR
     */
    public static final String JAR_NAME = "yabs.jar";
    /**
     *
     * Program name
     */
    public static final String PROG_NAME = "Yabs-" + Constants.VERSION + "";
    /**
     *
     * Running Java Version info
     */
    public static String JAVA_VERSION = "Java Version: " + System.getProperty("java.version");
    /**
     *
     * The class name to be loaded from plugins
     */
    public static String PLUGIN_LOAD_CLASS = "plugin.Main";
    /**
     * Name of databasedir 
     */
    public static final String DATABASENAME = "yabs" + Constants.RELEASE_VERSION + "db";
    public static String ICON_NAME_LIN = "yabs.desktop";
    public static String ICON_NAME_WIN = "YaBS.url";
    public static String XML_ROOT = "mpv5";
    public static String XML_DOCTYPE_ID = "-//http://openyabs.org//DTD mpv5 V 1.0//EN";
    public static String XML_DOCTYPE_URL = "http://openyabs.org/files/yabs_import_1.0.dtd";
    public static String SPLASH_IMAGE = "/mpv5/resources/images/background_l.png";
    public static String ABOUT_IMAGE = "/mpv5/resources/images/about1.png";
    public static String ICON = "/mpv5/resources/images/icon.png";
    public static String WEBSITE = "http://openyabs.org";
    public static final String[] CONTRIBUTORS = new String[]{
        "Andreas Weber",
        "Jan Hahnisch",
        "P.Eser",
        "Christian Gabel",
        "Daniel Lang",
        "Daniel Kulesz",
        "Denny Beyer",
        "Michael Stibane",
        "Sven Lindenhahn",
        "Uwe Schoeler",
        "Uwe Stark",
        "P.Heller",
        "Jean-Christoph von Oertzen"};
    public static String LANGUAGES_DIR = "languages";
    public static String PLUGINS_DIR = "plugins";
    public static String TEMPLATES_DIR = "templates";
    public static String BABELFISH_URL = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=";
    public static String FALLBACK_CACHE_DIR = "Cache";//Go for a relative path
    public static String LIBS_DIR = "lib";
    public static BigDecimal BD100 = new BigDecimal("100");
    
    
    public static final int TYPE_BILL = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_OFFER = 2;
    public static final int TYPE_DELIVERY_NOTE = 3;
    public static final int TYPE_ORDER_CONFIRMATION = 4;
    public static final int TYPE_PRODUCT = 5;
    public static final int TYPE_SERVICE = 6;
    public static final int TYPE_REMINDER = 7;
    public static final int TYPE_CONTACT = 8;
    public static final int TYPE_JOURNAL = 9;
    public static final int TYPE_PRODUCT_ORDER = 10;
    public static final int TYPE_CONTRACT = 11;
    public static final int TYPE_CONVERSATION = 12;
    public static final int TYPE_MASSPRINT = 13;
    public static final int TYPE_ACTIVITY = 14;
    public static final int TYPE_CUSTOMER = 15;
    public static final int TYPE_MANUFACTURER = 16;
    public static final int TYPE_SUPPLIER = 17;
    public static final int TYPE_EXPENSE = 18;
    public static final int TYPE_REVENUE = 19;
    
}
