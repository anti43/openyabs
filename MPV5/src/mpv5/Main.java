/*
 *  This file is part of MP.
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
package mpv5;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.common.NodataFoundException;
import mpv5.ui.frames.MPV5View;
import mpv5.logging.*;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectLock;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.SplashScreen;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.subcomponents.wizard_DBSettings_1;


import mpv5.db.objects.User;
import mpv5.pluginhandling.UserPlugin;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;
import mpv5.utils.files.FileDirectoryHandler;
import org.apache.commons.cli2.*;
import org.apache.commons.cli2.builder.*;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.*;

/**
 * The main class of the application.
 */
public class Main extends SingleFrameApplication {

    public static SplashScreen splash;
    private static boolean removeplugs = false;
    /**
     * Is true if the application is running, false if in editor
     */
    public static boolean INSTANTIATED = false;

    /**
     * Use this method to (re) cache data from the database to avoid uneccessary db queries
     */
    public static void cache() {
        User.cacheUser();
    }

    private static void useNetbookOpt() {
        ControlPanel_Fonts.applyFont(new Font("Dialog", Font.PLAIN, 11));
        MPV5View.setNavBarAnimated(false);
        MPV5View.setTabPaneScrolled(true);
    }
    private File lockfile = new File(MPPATH + File.separator + "." + Constants.PROG_NAME + Constants.VERSION + "." + "lck");

    /**
     * Read in local settings and launch the application
     */
    public static void start() {

        splash.nextStep(Messages.LOCAL_SETTINGS.toString());
        try {
            LocalSettings.read();
            LocalSettings.apply();
        } catch (Exception ex) {
            Log.Debug(Main.class, ex.getMessage());
            Log.Debug(Main.class, "Local settings file not readable: " + LocalSettings.getLocalFile());
        }

        splash.nextStep(Messages.LAUNCH.toString());
        Runnable runnable = new Runnable() {

            public void run() {
                launch(Main.class, new String[]{});
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    /**
     * Indicates whether this is the first start of the application
     */
    public static boolean firstStart;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        getContext().getLocalStorage().setDirectory(new File(Main.MPPATH));
        splash.nextStep(Messages.FIRST_INSTANCE.toString());
        if (LocalSettings.getProperty(LocalSettings.DBTYPE).equals("single") && !firstInstance()) {
            System.exit(1);
        }
        splash.nextStep(Messages.DB_CHECK.toString());
        if (probeDatabaseConnection()) {
            go(false);
        } else if (Popup.Y_N_dialog(Messages.NO_DB_CONNECTION, Messages.ERROR_OCCURED.toString())) {
            splash.dispose();
            showDbWiz();
        } else {
            System.exit(1);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     * @param root
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Main
     */
    public static Main getApplication() {
        return Application.getInstance(Main.class);
    }

    @Override
    protected void shutdown() {
        MPV5View.setWaiting(true);
        MPV5View.setProgressRunning(true);
        DatabaseObjectLock.releaseAllObjectsFor(MPV5View.getUser());
        try {
            LocalSettings.save();
            if (!MPV5View.getUser().isDefault()) {
                MPV5View.getUser().logout();
            }
        } catch (Exception e) {
            Log.Debug(e);
        }
        super.shutdown();
    }

    /**
     * Main method launching the application.
     * @param args
     */
    public static void main(String[] args) {

        INSTANTIATED = true;
        try {
            splash = new SplashScreen(new ImageIcon(Test.class.getResource(mpv5.globals.Constants.SPLASH_IMAGE)));
            splash.init(8);
            Log.Print(Messages.START_MESSAGE);

            splash.nextStep(Messages.INIT.toString());
            getOS();
            setEnv();
            parseArgs(args);
            setDerbyLog();
            start();
        } catch (Exception e) {
            splash.dispose();
            e.printStackTrace();
        }
    }
    /**
     * Inicates if the OS is a Windows version
     */
    public static boolean IS_WINDOWS = false;
    /**
     * The path for db, cache, session files
     */
    public static String MPPATH = null;
    /**
     * The local settings file
     */
    public static String SETTINGS_FILE = null;
    /**
     * The directory where the application files go
     */
    public static String APP_DIR = null;
    /**
     * The user home directory
     */
    public static String USER_HOME = null;
    /**
     * A shortcut to the user desktop
     */
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
            APP_DIR = USER_HOME + File.separator + Constants.PROG_NAME;

        } else {

            USER_HOME = System.getProperty("user.home");
            DESKTOP = USER_HOME + File.separator + "Desktop";
            MPPATH = USER_HOME + File.separator + ".mp";
            SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".mp";
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
        Argument number = abuilder.withName("number").withMinimum(1).withMaximum(1).create();

        Option showenv = obuilder.withShortName("showenv").withShortName("se").withDescription("show environmental variables").create();
        Option netbook = obuilder.withShortName("netbook").withShortName("net").withDescription("use netbook size optimizations").create();
        Option help = obuilder.withShortName("help").withShortName("h").withDescription("print this message").create();
        Option license = obuilder.withShortName("license").withShortName("li").withDescription("print license").create();
        Option version = obuilder.withShortName("version").withDescription("print the version information and exit").create();
        Option verbose = obuilder.withShortName("verbose").withDescription("be extra verbose").create();
        Option nolf = obuilder.withShortName("nolf").withDescription("use system L&F instead of Tiny L&F").create();
        Option debug = obuilder.withShortName("debug").withDescription("debug logging").create();
        Option removeplugins = obuilder.withShortName("removeplugins").withDescription("remove all plugins which would be loaded").create();
        Option logfile = obuilder.withShortName("logfile").withShortName("l").withDescription("use file for log").withArgument(filearg).create();
        Option connectionInstance = obuilder.withShortName("connectionInstance").withShortName("conn").withDescription("Use stored connection with this ID").withArgument(number).create();

        Group options = gbuilder.withName("options").
                withOption(help).
                withOption(version).
                withOption(verbose).
                withOption(debug).
                withOption(license).
                withOption(nolf).
                withOption(netbook).
                withOption(showenv).
                withOption(removeplugins).
                withOption(connectionInstance).
                withOption(logfile).create();

        HelpFormatter hf = new HelpFormatter();
        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);

        CommandLine cl = p.parseAndHelp(args);

        if (cl == null) {
            System.err.println("Cannot parse arguments");
        } else {

            if (cl.hasOption(connectionInstance)) {
                LocalSettings.setConnectionID(Integer.valueOf(String.valueOf(cl.getValue(connectionInstance))));
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
                Log.setLogLevel(Log.LOGLEVEL_NORMAL);

            }

            if (cl.hasOption(debug)) {
                Log.setLogLevel(Log.LOGLEVEL_DEBUG);

            }

            if (cl.hasOption(logfile)) {
                try {
                    LogConsole.setLogFile(((String) cl.getValue(logfile)).split("=")[1]);
                } catch (Exception e) {
                    Log.Debug(Main.class, "Fehler beim Schreiben der Logdatei: " + e.getMessage());
                }
            }

            if (cl.hasOption(nolf)) {
                setLaF(null);
            }

            if (cl.hasOption(netbook)) {
                useNetbookOpt();
            }

            if (cl.hasOption(removeplugins)) {
                removeplugs = true;
            }

            if (cl.hasOption(showenv)) {
                printEnv();
            }
        }

        Log.Print("\nOptions used:");
        for (int idx = 0; idx < cl.getOptions().size(); idx++) {
            Log.Print(cl.getOptions().get(idx));
        }
        Log.Print("\n");
    }

    /**
     * Redirect derby log file
     */
    public static void setDerbyLog() {
        Properties p = System.getProperties();
        p.put("derby.stream.error.file", FileDirectoryHandler.getTempFile());
    }

    /**
     * 
     * @param lafname 
     */
    public static void setLaF(String lafname) {
        try {
            if (lafname != null) {
                UIManager.setLookAndFeel(lafname);
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());
            if (MPV5View.identifierFrame != null && MPV5View.identifierFrame.isShowing()) {
                MPV5View.identifierFrame.setVisible(false);
                SwingUtilities.updateComponentTreeUI(MPV5View.identifierFrame);
                MPV5View.identifierFrame.setVisible(true);
            }
        } catch (Exception exe) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                Log.Debug(Main.class, ex.getMessage());
            }
            Log.Debug(Main.class, exe.getMessage());
        }
    }

    /**
     *
     */
    public static void printEnv() {
        Properties sysprops = System.getProperties();
        Enumeration propnames = sysprops.propertyNames();

        while (propnames.hasMoreElements()) {
            String propname = (String) propnames.nextElement();
            Log.Debug(Main.class, "System env: " + propname.toUpperCase() + " : " + System.getProperty(propname));
        }
    }

    /**
     *
     * @param firststart
     */
    public void go(boolean firststart) {
        splash.nextStep(Messages.CACHE.toString());
        cache();
        setLaF(null);
        Main.splash.nextStep(Messages.INIT_LOGIN.toString());
        login();
        Main.splash.nextStep(Messages.INIT_PLUGINS.toString());
        loadPlugins();
        Main.splash.nextStep(Messages.INIT_GUI.toString());
        super.show(new MPV5View(this));
        firstStart = firststart;
        if (Main.firstStart) {
            getApplication().getMainFrame().setSize(MPV5View.initialSize);
        }
        SwingUtilities.updateComponentTreeUI(MPV5View.identifierFrame);
        splash.dispose();
    }

    private void loadPlugins() {

        if (!removeplugs) {
            try {
                MPPLuginLoader loadr = new MPPLuginLoader();
                MPV5View.queuePlugins(loadr.getPlugins());
            } catch (Exception e) {
                Log.Debug(e);
            }
        } else {
            try {
                ArrayList data = DatabaseObject.getReferencedObjects(MPV5View.getUser(), Context.getPluginsToUsers());

                for (int i = 0; i < data.size(); i++) {
                    try {
                       ((UserPlugin) data.get(i)).delete();
                    } catch (Exception e) {
                        Log.Debug(e);
                    }
                }

            } catch (NodataFoundException ex) {
                Log.Debug(Main.class, ex.getMessage());
            }
        }
    }

    private void login() {
        if (!LocalSettings.getProperty("lastuser").equals("null") && !LocalSettings.getProperty("lastuserpw").equals("null")) {
            User usern1 = new User();
            Log.Debug(this, "Checking for auto login.. ");
            if (usern1.fetchDataOf(Integer.valueOf(LocalSettings.getProperty("lastuser")))) {
                Log.Debug(this, "Trying to login user: " + usern1);
                User user = mpv5.usermanagement.MPSecurityManager.checkAuthInternal(usern1, LocalSettings.getProperty("lastuserpw"));
                if (user != null) {
                    user.login();
                }
            }
        }
    }

    private boolean probeDatabaseConnection() {
        try {
            DatabaseConnection.instanceOf();
            return true;
        } catch (Exception ex) {
            Log.Debug(this, "Could not connect to database.");
//            Log.Debug(this, ex);
            return false;
        }
    }

    private boolean firstInstance() {
        try {
            if (lockfile.exists()) {
                Log.Debug(this, "Application already running.");
                return false;
            } else {
                FileWriter x = new FileWriter(lockfile);
                x.write("Locked on " + new Date() + ". Instance[0]");
                x.close();
                lockfile.deleteOnExit();
                Log.Debug(this, "Application will start now.");
                return true;
            }
        } catch (Exception e) {
            Log.Debug(this, "Application encountered some problem. Will try to continue anyway.");
            return true;
        }

    }

    private void showDbWiz() {
        try {
            Log.setLogLevel(Log.LOGLEVEL_DEBUG);
            LogConsole.setLogFile("install.log");
            Log.Debug(this, new Date());
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Wizard w = new Wizard(true);
        w.addPanel(new wizard_DBSettings_1(w));
        w.showWiz();
    }
}
