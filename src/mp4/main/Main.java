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

import javax.swing.UIManager;
import mp4.installation.Setup;
import mp4.globals.Constants;
import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.utils.files.FileReaderWriter;
import mp4.logs.*;
import mp4.panels.misc.SplashScreen;
import mp4.frames.Logger;
import mp4.frames.mainframe;
import mp4.installation.Verzeichnisse;
import mp4.utils.export.druck.DruckJob;

/**
 *
 * @author anti43
 */
public class Main implements Strings {

    private static SplashScreen splash;
    private static String VERSION = Constants.VERSION;
    public static boolean IS_WINDOWS = false;
    public static boolean FORCE_NO_DATABASE = false;
    public static boolean FORCE_NO_FILE_COPY = false;
    public static boolean FORCE_CREATE_DATABASE = false;
    public static boolean FORCE_FILE_COPY = false;
    private static final String argVERBOSE = "-verbose";
    private static final String argNOCOPYFILES = "-nocopy";
    private static final String argNOCREATEDB = "-nodb";
    private static final String argFORCECOPYFILES = "-forcecopy";
    private static final String argFORCECREATEDB = "-forcedb";
    private static final String argCHANGE_TEMPLATE_DIR = "-templatedir=";
    private static final String argCHANGE_BACKUP_DIR = "-backupdir=";
    private static final String argCHANGE_PDF_DIR = "-pdfdir=";
    private static final String argFILE_LOGGING = "-log=";
    private static final String argDB_LOCATION = "-dbpath=";
    private static final String argAPP_LOCATION = "-instpath=";
    public static String PDFDIR = null;
    public static String TEMPLATEDIR = null;
    public static String BACKUP_DIR = null;
    public static String MPPATH = Constants.USER_HOME + File.separator + ".mp";
    /**
     * Full path to settings file
     */
    public static String SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
    public static String APP_DIR = Constants.USER_HOME + Constants.SEP + Constants.PROG_NAME;

    private static void getOS() {
        if (System.getProperty("os.name").contains("Windows")) {
            IS_WINDOWS = true;
            DruckJob.FORCE_WIN_PRINT = true;
        } else {
            IS_WINDOWS = false;
        }
    }

    private static void parseArgs(String[] args) {
        String arg = "";
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                arg = args[i];
                if (arg.contains(argFORCECOPYFILES)) {
                    FORCE_FILE_COPY = true;
                } else if (arg.contains(argFORCECREATEDB)) {
                    FORCE_CREATE_DATABASE = true;
                } else if (arg.contains(argNOCOPYFILES)) {
                    FORCE_NO_FILE_COPY = true;
                } else if (arg.contains(argNOCREATEDB)) {
                    FORCE_NO_DATABASE = true;
                } else if (arg.contains(argVERBOSE)) {
                    Log.setLogLevel(Log.LOGLEVEL_HIGH);
                } else if (arg.contains(argCHANGE_TEMPLATE_DIR)) {
                    try {
                        TEMPLATEDIR = arg.split("=")[1];
                    } catch (Exception e) {
                        TEMPLATEDIR = null;
                    }
                } else if (arg.contains(argCHANGE_BACKUP_DIR)) {
                    try {
                        BACKUP_DIR = arg.split("=")[1];
                    } catch (Exception e) {
                        BACKUP_DIR = null;
                    }
                } else if (arg.contains(argCHANGE_PDF_DIR)) {
                    try {
                        PDFDIR = arg.split("=")[1];
                    } catch (Exception e) {
                        PDFDIR = null;
                    }
                } else if (arg.contains(argFILE_LOGGING)) {
                    try {
                        Logger.setLogFile(arg.split("=")[1]);
                    } catch (Exception e) {
                        Log.Debug("Fehler beim Schreiben der Logdatei: " + e.getMessage(), true);
                    }
                } else if (arg.contains(argDB_LOCATION)) {
                    MPPATH = arg.split("=")[1];
                    SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
                } else if (arg.contains(argAPP_LOCATION)) {
                    APP_DIR = arg.split("=")[1];
                }
            }
        }
    }

    public Main() {
        setLaF();
        splash = new SplashScreen(TEST_CONF);
        doArgCommands();

        //Datenbank suchen
        if (findSettings() && findDatabase()) {
            try {
                splash.setComp(new mainframe(splash, this));
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(1);
            }
        //Falls Datenbank nicht vorhanden, Installer starten
        } else {
            splash.setComp(new Setup());
        }
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

    private boolean findDatabase() {
        FileReaderWriter f = new FileReaderWriter(SETTINGS_FILE);
        String[] dat = f.read().split(COLON);
        String db = dat[0] + File.separator + Constants.DATABASENAME;
        File test = new File(db);
        return test.exists();
    }

    private boolean findSettings() {
        //Settings Datei suchen und schreiben
        File df = new File(SETTINGS_FILE);
        Log.Debug("Arbeitsverzeichnis: " + df.getParent(), true);
        if (df.exists()) {
        } else {
            if (df.mkdirs()) {
                FileReaderWriter f = new FileReaderWriter(SETTINGS_FILE);
                f.write(Main.MPPATH + COLON + VERSION);
            } else {
                Popup.notice(PERMISSION_DENIED);
                System.err.println(PERMISSION_DENIED);
                System.exit(1);
            }
        }
        return df.canRead();
    }

    public static void main(String[] args) {

        getOS();
        parseArgs(args);

        try {
            new Main();
        } catch (Exception e) {
            Popup.warn(e.getMessage(), Popup.ERROR);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void setLaF() {
        try {
            UIManager.setLookAndFeel(new TinyLookAndFeel());
            LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());
        } catch (Exception exe) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                Log.Debug(ex.getMessage());
            }
            Log.Debug(exe.getMessage());
        }
    }

    public static void printEnv() {
        System.out.println(System.getenv("HOMEDRIVE"));
        System.out.println(System.getenv("SystemDrive"));
        System.out.println(System.getenv("SystemRoot"));
        System.out.println(System.getenv("USERPROFILE"));
    }
}
