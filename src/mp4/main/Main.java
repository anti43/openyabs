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
package mp4.main;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import javax.swing.UIManager;
import mp4.datenbank.verbindung.ConnectionTypeHandler;
import mp4.einstellungen.SettingsFile;
import mp4.frames.LoggerWindow;
import mp4.frames.mainframe;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.installation.Setup;
import mp4.installation.Verzeichnisse;
import mp4.items.visual.Popup;
import mp4.logs.Log;
import mp4.panels.misc.SplashScreen;
import mp4.utils.export.druck.DruckJob;
import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.*;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.*;

/**
 *
 * @author anti43
 */
public class Main implements Strings {

    private static SplashScreen splash;
    public static boolean IS_WINDOWS = false;
    public static boolean FORCE_NO_DATABASE = false;
    public static boolean FORCE_NO_FILE_COPY = false;
    public static boolean FORCE_CREATE_DATABASE = false;
    public static boolean FORCE_FILE_COPY = false;
    public static String PDF_DIR = null;
    public static String TEMPLATE_DIR = null;
    public static String BACKUP_DIR = null;
    public static String MPPATH = null;
    public static String SETTINGS_FILE = null;
    public static String LOCAL_LOGIN_FILE = null;
    public static String APP_DIR = null;
    public static String USER_HOME = null;
    public static String DESKTOP = null;
    public static SettingsFile settingsfile = null;
    

    private static void getOS() {
        if (System.getProperty("os.name").contains("Windows")) {
            IS_WINDOWS = true;
        } else {
            IS_WINDOWS = false;
        }
    }

    public static void setEnv() {

        if (IS_WINDOWS) {
            DruckJob.FORCE_WIN_PRINT = true;
            USER_HOME = System.getenv("USERPROFILE");
            DESKTOP = USER_HOME + File.separator + "Desktop";
            MPPATH = USER_HOME + File.separator + ".mp";
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
            LOCAL_LOGIN_FILE = Main.MPPATH + File.separator + "login.mp";
            APP_DIR = USER_HOME + File.separator + Constants.PROG_NAME;
            settingsfile = new SettingsFile();
        } else {
            DruckJob.FORCE_WIN_PRINT = false;
            USER_HOME = System.getProperty("user.home");
            DESKTOP = USER_HOME + File.separator + "Desktop";
            MPPATH = USER_HOME + File.separator + ".mp";
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
            LOCAL_LOGIN_FILE = Main.MPPATH + File.separator + "login.mp";
            APP_DIR = USER_HOME + File.separator + Constants.PROG_NAME;
            settingsfile = new SettingsFile();
        }
    }

    private static void parseArgs(String[] args) {

        Log.PrintArray(args);

        DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        ArgumentBuilder abuilder = new ArgumentBuilder();
        GroupBuilder gbuilder = new GroupBuilder();

        Argument option = abuilder.withName("=option").withMinimum(1).withMaximum(1).create();
        Argument filearg = abuilder.withName("=file").withMinimum(1).withMaximum(1).create();
        Argument dirarg = abuilder.withName("=directory").withMinimum(1).withMaximum(1).create();

        Option help = obuilder.withShortName("help").withShortName("h").withDescription("print this message").create();
        Option license = obuilder.withShortName("license").withShortName("li").withDescription("print license").create();
        Option version = obuilder.withShortName("version").withDescription("print the version information and exit").create();
        Option verbose = obuilder.withShortName("verbose").withDescription("be extra verbose").create();
        Option dbtype = obuilder.withShortName("dbdriver").withShortName("r").withDescription("DB Driver: derby (default), mysql, custom").withArgument(option).create();
        Option debug = obuilder.withShortName("debug").withDescription("debug logging").create();
        Option nodb = obuilder.withShortName("nodb").withDescription("force no database").create();
        Option nocopy = obuilder.withShortName("nocopy").withDescription("force no copy of files").create();
        Option forcedb = obuilder.withShortName("forcedb").withDescription("force database").create();
        Option forcecopy = obuilder.withShortName("forcecopy").withDescription("force copy of files").create();
        Option dbpath = obuilder.withShortName("dbpath").withShortName("d").withDescription("use database path").withArgument(dirarg).create();
        Option instpath = obuilder.withShortName("instpath").withShortName("i").withDescription("use installation path").withArgument(dirarg).create();
        Option logfile = obuilder.withShortName("logfile").withShortName("l").withDescription("use file for log").withArgument(filearg).create(); 
        Option pdfdir = obuilder.withShortName("pdfdir").withShortName("p").withDescription("use pdfdir").withArgument(dirarg).create();
        Option backupdir = obuilder.withShortName("backupdir").withShortName("b").withDescription("use backupdir").withArgument(dirarg).create();
        Option templatedir = obuilder.withShortName("templatedir").withShortName("t").withDescription("use templatedir").withArgument(dirarg).create();

        Group options = gbuilder.withName("options").withOption(help).withOption(version).withOption(verbose).withOption(debug).withOption(nodb).
                withOption(nocopy).withOption(license).
                withOption(forcedb).withOption(forcecopy).withOption(dbpath).withOption(dbtype).withOption(instpath).
                withOption(logfile).withOption(pdfdir).withOption(backupdir).withOption(templatedir).create();


        HelpFormatter hf = new HelpFormatter();
        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);
        CommandLine cl = p.parseAndHelp(args);
        if (cl == null) {
            System.err.println("Cannot parse arguments");
            System.exit(1);
        }
        if (cl.hasOption(help)) {
            hf.print();
            System.exit(0);
        }

        if (cl.hasOption(license)) {
            System.out.print(Strings.GPL);
        }

        if (cl.hasOption(version)) {
            System.out.println("MP Version: " + Constants.VERSION);
            System.exit(0);
        }
        if (cl.hasOption(verbose)) {
            Log.setLogLevel(Log.LOGLEVEL_HIGH);

        }

        if (cl.hasOption(debug)) {
            Log.setLogLevel(Log.LOGLEVEL_DEBUG);

        }

        if (cl.hasOption(forcecopy)) {
            FORCE_FILE_COPY = true;

        }

        if (cl.hasOption(forcedb)) {
            FORCE_CREATE_DATABASE = true;

        }

        if (cl.hasOption(nocopy)) {
            FORCE_NO_FILE_COPY = true;

        }

        if (cl.hasOption(nodb)) {
            FORCE_NO_DATABASE = true;

        }

        if (cl.hasOption(templatedir)) {
            TEMPLATE_DIR = ((String) cl.getValue("templatedir")).split("=")[1];

        }

        if (cl.hasOption(backupdir)) {
            BACKUP_DIR = ((String) cl.getValue(backupdir)).split("=")[1];

        }

        if (cl.hasOption(pdfdir)) {
            PDF_DIR = ((String) cl.getValue(pdfdir)).split("=")[1];

        }

        if (cl.hasOption(logfile)) {
            try {
                LoggerWindow.setLogFile(((String) cl.getValue(logfile)).split("=")[1]);
            } catch (Exception e) {
                Log.Debug(Main.class, "Fehler beim Schreiben der Logdatei: " + e.getMessage(), true);
            }
        }

        if (cl.hasOption(dbpath)) {
            Main.settingsfile.setDBPath(((String) cl.getValue(dbpath)).split("=")[1]);
        }

        if (cl.hasOption(dbtype)) {
            if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("derby")) {
                Main.settingsfile.setDBDriver(ConnectionTypeHandler.DERBY_DRIVER);
            } else if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("mysql")) {
                Main.settingsfile.setDBDriver(ConnectionTypeHandler.MYSQL_DRIVER);
            } else if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("custom")) {
                Main.settingsfile.setDBDriver(ConnectionTypeHandler.CUSTOM_DRIVER);
            }
        }

        if (cl.hasOption(instpath)) {
            APP_DIR = ((String) cl.getValue(instpath)).split("=")[1];
        }

    }

    public Main() throws Exception {
        setDerbyLog();
        setLaF();
        splash = new SplashScreen(TEST_CONF);
        doArgCommands();

        Log.Debug(this, "MP Datei: " + SETTINGS_FILE, true);
        if (settingsfile.getFile().exists() && settingsfile.getFile().canRead()) {
            try {
                settingsfile.read();
                splash.setComp(new mainframe(splash, this));
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(1);
            }
//Falls Datenbank nicht vorhanden, aber mpsettings Datei:
//        } 
//        else if (settingsfile.getFile().exists() && settingsfile.getFile().canRead()) {
//            settingsfile.read();
//            checkDB_Location();
//            String db = settingsfile.getDBPath() + File.separator + Constants.DATABASENAME;
//            Log.Debug(this, db + " not found :-(", true);
//            Popup.notice(this, "Datenbank existiert nicht am angegebenen Ort.\n" + db);
//
//            splash.setMessage("Versuche, Datenbank anzulegen");
//
//            if (new Setup(true).createDatabase()) {
//                Popup.notice(this, "Datenbank angelegt in\n" + db);
//                splash.setMessage("Starte MP");
//                splash.dispose();
//                new Main();
//            } else {
//                Popup.notice(this, "Es ist ein Fehler aufgetreten, Programm wird beendet.");
//                System.exit(1);
//            }
//Falls Datenbank und mpsettings nicht vorhanden, Installer starten
        } else if (createSettingsFile()) {
            checkDB_Location();
            splash.setComp(new Setup());
        } else {
            Popup.notice(this, PERMISSION_DENIED);
            System.err.println(PERMISSION_DENIED);
            System.exit(1);
        }
    }

    private void checkDB_Location() {
        new File(settingsfile.getDBPath()).mkdirs();
    }

    private void doArgCommands() {
        if (FORCE_FILE_COPY) {
            try {
                Verzeichnisse.buildPath();
                Verzeichnisse.copyFiles();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (FORCE_CREATE_DATABASE) {
            new Setup().createDatabase();
        }
    }

    private boolean createSettingsFile() throws IOException {
        return settingsfile.create();
    }

    public static void main(String[] args) {

        System.out.print(Strings.START_MESSAGE);

        getOS();
        setEnv();
        parseArgs(args);

        try {
            new Main();
        } catch (Exception e) {
            Popup.warn(e.getMessage(), Popup.ERROR);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setDerbyLog() {
        Properties p = System.getProperties();
        p.put("derby.stream.error.file", MPPATH + File.separator + "derby.log");
    }

    private void setLaF() {
        try {
            UIManager.setLookAndFeel(new TinyLookAndFeel());
            LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());
        } catch (Exception exe) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
            Log.Debug(this, exe.getMessage());
        }
    }

    public static void printEnv() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
    }
}
