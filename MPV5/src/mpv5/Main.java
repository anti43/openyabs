/*
 * Main.java
 */

package mpv5;

import mpv5.ui.frames.MPV5View;
import mpv5.logging.*;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.*;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.*;

/**
 * The main class of the application.
 */
public class Main extends SingleFrameApplication {
   
    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new MPV5View(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Main
     */
    public static Main getApplication() {
        return Application.getInstance(Main.class);
    }

    @Override protected void shutdown(){
    System.out.println(Main.getApplication().getMainFrame().getSize());

        super.shutdown();
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        
        System.out.print(Messages.START_MESSAGE);

        getOS();
        setEnv();
        parseArgs(args);
        setDerbyLog(); 

        launch(Main.class, args);

    }




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
  

    private static void getOS() {
        if (System.getProperty("os.name").contains("Windows")) {
            IS_WINDOWS = true;
        } else {
            IS_WINDOWS = false;
        }
    }

    public static void setEnv() {

        if (IS_WINDOWS) {
          
            USER_HOME = System.getenv("USERPROFILE");
            DESKTOP = USER_HOME + File.separator + "Desktop";
            MPPATH = USER_HOME + File.separator + ".mp";
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
            LOCAL_LOGIN_FILE = Main.MPPATH + File.separator + "login.mp";
            APP_DIR = USER_HOME + File.separator + Constants.PROG_NAME;
            
        } else {
          
            USER_HOME = System.getProperty("user.home");
            DESKTOP = USER_HOME + File.separator + "Desktop";
            MPPATH = USER_HOME + File.separator + ".mp";
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
            LOCAL_LOGIN_FILE = Main.MPPATH + File.separator + "login.mp";
            APP_DIR = USER_HOME + File.separator + Constants.PROG_NAME;
      
        }
    }

    private static void parseArgs(String[] args) {

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
        Option nolf = obuilder.withShortName("nolf").withDescription("use system L&F instead of Tiny L&F").create();
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
                withOption(nocopy).withOption(license).withOption(nolf).
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
//            System.out.print(Messages.GPL);
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
            LocalSettings.setDBPath(((String) cl.getValue(dbpath)).split("=")[1]);
        }

        if (cl.hasOption(dbtype)) {
            if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("derby")) {
                LocalSettings.setDBDriver(ConnectionTypeHandler.DERBY_DRIVER);
            } else if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("mysql")) {
                LocalSettings.setDBDriver(ConnectionTypeHandler.MYSQL_DRIVER);
            } else if (((String) cl.getValue(dbtype)).toLowerCase().endsWith("custom")) {
                LocalSettings.setDBDriver(ConnectionTypeHandler.CUSTOM_DRIVER);
            }
        }

        if (cl.hasOption(instpath)) {
            APP_DIR = ((String) cl.getValue(instpath)).split("=")[1];
        }

        if (!cl.hasOption(nolf)) {
            setLaF(null);
        }
    }


    public static void setDerbyLog() {
        Properties p = System.getProperties();
        p.put("derby.stream.error.file", MPPATH + File.separator + "derby.log");
    }

    public static void setLaF(LookAndFeel lf) {
        try {
            if (lf != null) {
                UIManager.setLookAndFeel(lf);
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());
        } catch (Exception exe) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                Log.Debug(Main.class, ex.getMessage());
            }
            Log.Debug(Main.class, exe.getMessage());
        }
    }

    public static void printEnv() {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            System.out.format("%s=%s%n", envName, env.get(envName));
        }
    }


}
