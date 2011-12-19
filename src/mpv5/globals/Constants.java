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

/**
 *
 * 
 */
public interface Constants {

    /**
     * 
     * Full Version number
     */
    public static final String VERSION = "Yabs-1.178";

    /**
     * The original database version of this release
     */
    public static final Double DATABASE_ORIGINAL_VERSION = 1.0;
    /**
     * The minimal Database Version which is compatible with this release
     */
    public static final Double DATABASE_VERSION = 1.193;
    /**
     * The max Database Version which is compatible with this release
     */
    public static final Double DATABASE_MAX_VERSION = 1.9;
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
    public static final String TITLE = "Yabs - cristaline";
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
    public static String SPLASH_IMAGE = "/mpv5/resources/images/background_j.png";
    public static String ABOUT_IMAGE = "/mpv5/resources/images/about1.png";
    public static String ICON = "/mpv5/resources/images/icon.png";
    public static String WEBSITE = "http://openyabs.org";
    public static String CURRENT_VERSION_URL = "http://mp-rechnungs-und-kundenverwaltung.googlecode.com/files/" + VERSION + ".zip";
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
    public static String[] LIBS = {
      //  "AbsoluteLayout.jar",
        "activation.jar",
        "bcprov-jdk16-145.jar",
        "bootstrapconnector.jar",
        "commons-cli-20070823.jar",
        "commons-codec-1.3.jar",
        "commons-io-2.0.1.jar",
        "commons-logging-1.1.jar",
        "core-renderer.jar",
        "derby.jar",
        "derbyclient.jar",
        "derbynet.jar",
        "DTAus.jar",
        "dtdparser121.jar",
        "frame4j.jar",
        "gdata-calendar-2.0.jar",
        "gdata-calendar-meta-2.0.jar",
        "gdata-client-1.0.jar",
        "gdata-client-meta-1.0.jar",
        "gdata-contacts-3.0.jar",
        "gdata-contacts-meta-3.0.jar",
        "gdata-core-1.0.jar",
        "google-api-translate-java-0.92.jar",
        "groovy-1.7.7.jar",
        "guava-r07.jar",
        "httpcore-4.0.1.jar",
        "httpcore-nio-4.0.1.jar",
        "iText-2.1.7.jar",
        "java_uno.jar",
        "java_uno_accessbridge.jar",
        "jdom.jar",
        "jodconverter-2.2.2.jar",
        "jodconverter-cli-2.2.2.jar",
        "jOpenDocument-1.1-jdk5.jar",
        "juh.jar",
        "junit-3.8.1.jar",
        "jurt.jar",
        "jut.jar",
        "l2fprod-common-all.jar",
        "mail.jar",
        "microba-0.4.4.2.jar",
        "MPCalendar.jar",
        "mysql-connector-java-5.1.14-bin.jar",
        "noa-mp.jar",
        "officebean.jar",
        "org.json.jar",
        "PDFRenderer.jar",
        "registry-3.1.3.jar",
        "resolver.jar",
        "ridl.jar",
        "sandbox.jar",
        "serializer.jar",
        "swing-layout-1.0.3.jar",
        "swing-worker-1.1.jar",
        "tinylaf.jar",
        "unoil.jar",
        "unoloader.jar",
        "vcard4j-1_1_3.jar",
        "ws-commons-util-1.0.2.jar",
        "xercesImpl.jar",
        "xml-apis.jar",
        "xmlrpc-client-3.1.2.jar",
        "xmlrpc-common-3.1.2.jar",
        "xmlrpc-server-3.1.2.jar",
        "ybsaf.jar"
    };
}
