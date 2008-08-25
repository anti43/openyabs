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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import mp4.installation.Setup;
import mp3.classes.interfaces.Constants;
import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp3.classes.utils.FileReaderWriter;
import mp3.classes.utils.Log;
import mp3.classes.utils.SplashScreen;
import mp4.frames.mainframe;
import mp4.installation.Verzeichnisse;

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
    
    
    public static String PDFDIR = null;
    public static String TEMPLATEDIR = null;
    public static String BACKUP_DIR = null;

    private static void getOS() {
        if (System.getProperty("os.name").contains("Windows")) {
            IS_WINDOWS = true;
        } else {
            IS_WINDOWS = false;
        }
    }

    private static void parseArgs(String[] args) {
        String arg = "";
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                arg = args[i];
                if (arg.contentEquals(argFORCECOPYFILES)) {
                    FORCE_FILE_COPY = true;
                } else if (arg.contentEquals(argFORCECREATEDB)) {
                    FORCE_CREATE_DATABASE = true;
                }
                if (arg.contentEquals(argNOCOPYFILES)) {
                    FORCE_NO_FILE_COPY = true;
                } else if (arg.contentEquals(argNOCREATEDB)) {
                    FORCE_NO_DATABASE = true;
                } else if (arg.contentEquals(argVERBOSE)) {
                    Log.setLogLevel(Log.LOGLEVEL_HIGH);
                } else if (arg.contentEquals(argCHANGE_TEMPLATE_DIR)) {
                    try {
                        TEMPLATEDIR = arg.split("=")[1];
                    } catch (Exception e) {
                        TEMPLATEDIR = null;
                    }
                } else if (arg.contentEquals(argCHANGE_BACKUP_DIR)) {
                    try {
                        BACKUP_DIR = arg.split("=")[1];
                    } catch (Exception e) {
                        BACKUP_DIR = null;
                    }
                } else if (arg.contentEquals(argCHANGE_PDF_DIR)) {
                    try {
                        PDFDIR = arg.split("=")[1];
                    } catch (Exception e) {
                        PDFDIR = null;
                    }
                }
            }
        }
    }

    public Main() {

        Log.setLogLevel(Log.LOGLEVEL_HIGH);

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
                Verzeichnisse.copyFiles();
            } catch (Exception ex) {
                Popup.error(ex.getMessage(), "Es ist ein Fehler aufgetreten:");
            }
        }
        if (FORCE_CREATE_DATABASE) {
            new Setup().createDatabase();
        }
    }

    private boolean findDatabase() {
        FileReaderWriter f = new FileReaderWriter(Constants.SETTINGS_FILE);
        String[] dat = f.read().split(COLON);
        String db = dat[0] + File.separator + Constants.DATABASENAME;
        File test = new File(db);
        return test.exists();
    }

    private boolean findSettings() {
        //Settings Datei suchen und schreiben
        File df = new File(Constants.SETTINGS_FILE);
        if (df.exists()) {
        } else {
            if (df.mkdirs()) {
                FileReaderWriter f = new FileReaderWriter(Constants.SETTINGS_FILE);
                f.write(Constants.MPPATH + COLON + VERSION);
            } else {
                Popup.notice(PERMISSION_DENIED);
                System.err.println(PERMISSION_DENIED);
                System.exit(1);
            }
        }
        return df.exists();
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
}
