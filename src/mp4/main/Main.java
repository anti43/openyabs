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

import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.*;
import org.apache.commons.cli2.commandline.*;
import org.apache.commons.cli2.util.*;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import de.muntjak.tinylookandfeel.TinyLookAndFeel;
import java.io.File;
import java.util.Map;

import java.io.IOException;
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
    public static String PDF_DIR = null;
    public static String TEMPLATE_DIR = null;
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
	DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        ArgumentBuilder abuilder = new ArgumentBuilder();
        GroupBuilder gbuilder = new GroupBuilder();
        Argument filearg   = abuilder.withName("file").withMinimum(1).withMaximum(1).create();
        Argument dirarg    = abuilder.withName("directory").withMinimum(1).withMaximum(1).create();
        Option help        = obuilder.withShortName("help").withShortName("h").withDescription("print this message").create();
        Option version     = obuilder.withShortName("version").withDescription("print the version information and exit").create();
        Option verbose     = obuilder.withShortName("verbose").withDescription("be extra verbose").create();
        Option nodb        = obuilder.withShortName("nodb").withDescription("force no database") .create();
        Option nocopy      = obuilder.withShortName("nocopy").withDescription("force no copy of files").create();
        Option forcedb     = obuilder.withShortName("forcedb").withDescription("force database").create();
        Option forcecopy   = obuilder.withShortName("forcecopy").withDescription("force copy of files").create();
        Option dbpath      = obuilder.withShortName("dbpath")     .withShortName("d").withDescription("use database path").withArgument(dirarg).create();
        Option instpath    = obuilder.withShortName("instpath")   .withShortName("i").withDescription("use installation path").withArgument(dirarg).create();
        Option logfile     = obuilder.withShortName("log")        .withShortName("l").withDescription("use file for log").withArgument(filearg).create();
        Option pdfdir      = obuilder.withShortName("pdfdir")     .withShortName("p").withDescription("use pdfdir").withArgument(dirarg).create();
        Option backupdir   = obuilder.withShortName("backupdir")  .withShortName("b").withDescription("use backupdir").withArgument(dirarg).create();
        Option templatedir = obuilder.withShortName("templatedir").withShortName("t").withDescription("use templatedir").withArgument(dirarg).create();
        Group options = gbuilder.withName("options")
        .withOption( help )
        .withOption( version )
        .withOption( verbose )
        .withOption( nodb )
        .withOption( nocopy )
        .withOption( forcedb )
        .withOption( forcecopy )
        .withOption( dbpath )
        .withOption( instpath )
        .withOption( logfile )
        .withOption( pdfdir )
        .withOption( backupdir )
        .withOption( templatedir )
        .create();

        HelpFormatter hf = new HelpFormatter();
        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);
        CommandLine cl = p.parseAndHelp( args );
        if (cl == null) {
            System.err.println("Cannot parse arguments");
            System.exit(1);
        }
        if (cl.hasOption( help )) {
            hf.print();
            System.exit(0);

        } else if (cl.hasOption( version )) {
            // TODO:

        } else if (cl.hasOption( verbose )) {
            Log.setLogLevel(Log.LOGLEVEL_HIGH);

        } else if (cl.hasOption( forcecopy )) {
            FORCE_FILE_COPY = true;

        } else if (cl.hasOption( forcedb )) {
            FORCE_CREATE_DATABASE = true;

        } else if (cl.hasOption( nocopy )) {
            FORCE_NO_FILE_COPY = true;

        } else if (cl.hasOption( nodb )) {
            FORCE_NO_DATABASE = true;

        } else if (cl.hasOption( templatedir )) {
            TEMPLATE_DIR = (String)cl.getValue( "templatedir" );

        } else if (cl.hasOption( backupdir )) {
            BACKUP_DIR = (String)cl.getValue( backupdir );

        } else if (cl.hasOption( pdfdir )) {
            PDF_DIR = (String)cl.getValue(  pdfdir  );

        } else if (cl.hasOption( logfile )) {
            try {
                Logger.setLogFile( (String)cl.getValue( logfile ) );
            } catch (Exception e) {
                Log.Debug( "Fehler beim Schreiben der Logdatei: " + e.getMessage(), true );
            }

        } else if (cl.hasOption( dbpath )) {
            MPPATH = (String)cl.getValue( dbpath );
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";

        } else if (cl.hasOption( instpath )) {
            APP_DIR = (String)cl.getValue( instpath );
        }
	System.exit(1);
    }

    public Main() throws Exception {
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
        File file = new File(SETTINGS_FILE);
        if (!file.exists() | !file.canRead()) {
            return false;
        }
        FileReaderWriter f = new FileReaderWriter(file);
        String[] dat = f.read().split(COLON);
        String db = dat[0] + File.separator + Constants.DATABASENAME;
        File test = new File(db);
        return test.exists();
    }

    private boolean findSettings() throws IOException {
        //Settings Datei suchen und schreiben
        File df = new File(SETTINGS_FILE);
        Log.Debug("Arbeitsverzeichnis: " + df.getParent(), true);
        if (!df.exists()){
            if (df.getParentFile().mkdirs() && df.createNewFile()) {
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
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
    }
}
