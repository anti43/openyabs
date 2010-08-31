/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5;

import java.io.IOException;
import java.util.ArrayList;
import mpv5.db.common.NodataFoundException;
import mpv5.ui.frames.MPView;
import mpv5.logging.*;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;
import enoa.connection.NoaConnection;
import enoa.handler.TemplateHandler;
import java.awt.Font;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectLock;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.SplashScreen;
import mpv5.ui.dialogs.Wizard;

import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.GlobalSettings;
import mpv5.handler.Scheduler;
import mpv5.i18n.LanguageManager;
import mpv5.pluginhandling.UserPlugin;
import mpv5.server.MPServer;
import mpv5.ui.dialogs.LoginToInstanceScreen;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;
import mpv5.ui.dialogs.subcomponents.wizard_DBSettings_simple_1;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileExecutor;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.print.PrintJob2;
import mpv5.utils.text.RandomText;
import mpv5.webshopinterface.WSIManager;
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
    private static Integer FORCE_INSTALLER;
    private static boolean CLEAR_LOCK = false;

    /**
     * Use this method to (re) cache data from the database to avoid unnecessary db queries
     */
    public static void cache() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                MPView.addMessage(Messages.CACHE);
                User.cacheUser();
                MPView.addMessage(Messages.CACHED_OBJECTS + ": " + Context.getUser());
                LanguageManager.getCountriesAsComboBoxModel();
                MPView.addMessage(Messages.CACHED_OBJECTS + ": " + Context.getCountries());

            }
        };
        new Thread(runnable).start();
//        Account.cacheAccounts();//pre cache accounts
//        MPView.addMessage(Messages.CACHED_OBJECTS + ": " + Context.getAccounts());
//        DatabaseObject.cacheObjects();//Is called by User.login() later
    }

    private static void useNetbookOpt() {
        MPView.setNavBarAnimated(false);
        MPView.setTabPaneScrolled(true);
    }

    /**
     * Add the processes to close on exit
     * @param officeApplication
     */
    public static void addProcessToClose(Process application) {
        oap.add(application);
    }
    private static List<Process> oap = new Vector<Process>();

    private static void readLocalSettings() {
        splash.nextStep(Messages.LOCAL_SETTINGS.toString());
        try {
            LocalSettings.read();
            LocalSettings.apply();
        } catch (Exception ex) {
            Log.Debug(Main.class, ex.getMessage());
            Log.Debug(Main.class, "Local settings file not readable: " + LocalSettings.getLocalFile());
        }

        try {
            GlobalSettings.read();
        } catch (Exception ex) {
            Log.Debug(Main.class, ex.getMessage());
        }
    }

    private static void readImports() {
        splash.nextStep(Messages.IMPORT_LANGUAGES.toString());
        File ilang = new File(Constants.LANGUAGES_DIR);
        try {
            Log.Debug(Main.class, "Checking: " + ilang.getPath());
            if (ilang.isDirectory() && ilang.canRead()) {
                File[] languages = FileDirectoryHandler.getFilesOfDirectory(ilang);
                for (int i = 0; i < languages.length; i++) {
                    File file = languages[i];
                    if (QueryHandler.instanceOf().clone(Context.getLanguage()).checkUniqueness("longname", file.getName())) {
                        Log.Debug(Main.class, "Importing: " + file.getPath());
                        LanguageManager.importLanguage(file.getName(), file);
                    }
                    file.deleteOnExit();
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
            Popup.error(e);
        }

        splash.nextStep(Messages.IMPORT_TEMPLATES.toString());
        File itemp = new File(Constants.TEMPLATES_DIR);
        try {
            Log.Debug(Main.class, "Checking: " + itemp.getPath());
            if (itemp.isDirectory() && itemp.canRead()) {
                File[] templates = FileDirectoryHandler.getFilesOfDirectory(itemp);
                for (int i = 0; i < templates.length; i++) {
                    File file = templates[i];
                    Log.Debug(Main.class, "Importing: " + file.getPath());
                    TemplateHandler.importTemplate(file);
                    file.deleteOnExit();
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
            Popup.error(e);
        }

        splash.nextStep(Messages.IMPORT_PLUGINS.toString());
        File iplug = new File(Constants.PLUGINS_DIR);
        try {
            Log.Debug(Main.class, "Checking: " + iplug.getPath());
            if (iplug.isDirectory() && iplug.canRead()) {
                File[] plugins = FileDirectoryHandler.getFilesOfDirectory(iplug);
                for (int i = 0; i < plugins.length; i++) {
                    File file = plugins[i];
                    Log.Debug(Main.class, "Importing: " + file.getPath());
                    MPPLuginLoader.importPlugin(file.getName(), file);
                    file.deleteOnExit();
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
            Popup.error(e);
        }

    }

    private static void runStartScripts() {
        if (User.PROPERTIES_OVERRIDE.hasProperty("startupcommand")) {
            try {
                FileExecutor.run(User.PROPERTIES_OVERRIDE.getProperty("startupcommand"));
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }
    private File lockfile = new File(MPPATH + File.separator + "." + Constants.PROG_NAME + "." + "lck");

    /**
     * Launch the application
     */
    public static void start() {

        splash.nextStep(Messages.LAUNCH.toString());
        Log.Debug(Main.class, "Trying to launch application now..");
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    launch(Main.class, new String[]{});
                } catch (Exception e) {
                    Log.Debug(e);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    /**
     * Indicates whether this is the first start of the application
     */
    public static boolean firstStart;
    /**
     * Sometimes it is nice to have a good goodbye message at hand ;-)<br/><br/>
     * <b>"So Long, and Thanks for All the Fish."</b>
     */
    public static final String GOODBYE_MESSAGE = "So Long, and Thanks for All the Fish.";
    /**
     * Indicates whether the server component shall start
     */
    public static boolean START_SERVER = false;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        Log.Debug(this, "Startup procedure... ");
        getContext().getLocalStorage().setDirectory(new File(Main.MPPATH));
        splash.nextStep(Messages.FIRST_INSTANCE.toString());
        if (LocalSettings.getProperty(LocalSettings.DBTYPE).equals("single") && !firstInstance()) {
            System.exit(1);
        }
        splash.nextStep(Messages.DB_CHECK.toString());
        ControlPanel_Fonts.applyFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));
        if (FORCE_INSTALLER == null) {
            Log.Debug(this, "Probing database connection... ");
            if (probeDatabaseConnection()) {
                Log.Debug(this, "Loading Yabs... ");
                go(false);
            } else if (Popup.Y_N_dialog(splash, Messages.NO_DB_CONNECTION, Messages.FIRST_START.toString())) {
                Log.Debug(this, "Loading database config wizard...");
                splash.dispose();
                showDbWiz(null);
            } else {
                Log.Debug(this, "Cancelled by user.");
                System.exit(1);
            }
        } else {
            Log.Debug(this, "Forced to start the database config wiz... ");
            showDbWiz(FORCE_INSTALLER);
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
    public void shutdown() {
        MPView.setWaiting(true);
        MPView.setProgressRunning(true);
        DatabaseObjectLock.releaseAllObjectsFor(mpv5.db.objects.User.getCurrentUser());
        try {
            GlobalSettings.save();
            LocalSettings.save();
            if (!mpv5.db.objects.User.getCurrentUser().isDefault()) {

                try {
                    ValueProperty.addOrUpdateProperty("layoutinfo", User.getCurrentUser().getLayoutProperties(), User.getCurrentUser());
                } catch (Exception ex) {
                    Log.Debug(ex);
                }

                try {
                    if (User.getCurrentUser().getProperties().hasProperty("shutdowncommand")) {
                        String commands = User.getCurrentUser().getProperties().getProperty("shutdowncommand");
                        FileExecutor.run(commands);
                    }
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
                mpv5.db.objects.User.getCurrentUser().logout();
            }
            for (int i = 0; i < oap.size(); i++) {
                Process p = oap.get(i);
                try {
                    p.destroy();
                } catch (Exception n) {
                }
            }
            NoaConnection.stopOOOServer();
        } catch (Exception e) {
            Log.Debug(e);
        }

        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
            Log.Debug(Main.class, QueryHandler.instanceOf().getStatistics());
        }
        Log.Print(GOODBYE_MESSAGE);
        try {
            clearLockFile();
        } catch (Exception e) {
        }

        super.shutdown();
    }

    /**
     * Main method launching the application.
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        INSTANTIATED = true;
        try {
            splash = new SplashScreen(new ImageIcon(Main.class.getResource(mpv5.globals.Constants.SPLASH_IMAGE)));
            splash.init(11);
            Log.Print(Messages.START_MESSAGE);

            splash.nextStep(Messages.INIT.toString());
            getOS();
            setEnv(null);
            parseArgs(args);
            runStartScripts();
            readLocalSettings();
            setDerbyLog();
            readImports();
            start();
        } catch (Exception e) {
            throw e;
        }
    }
    /**
     * The path for db, cache, session files
     */
    public static String MPPATH = null;
    /**
     * The local settings file
     */
    public static String SETTINGS_FILE = null;
    /**
     * The user home directory
     */
    public static String USER_HOME = null;
    /**
     * A shortcut to the user desktop
     */
    public static String DESKTOP = null;
    private static boolean nolf = false;
    /**
     * True if MacOS X has been detected
     */
    public static boolean osIsMacOsX = false;
    /**
     * True if a Windows version has been detected
     */
    public static boolean osIsWindows = false;
    /**
     * True if a Linux distribution has been detected
     */
    public static boolean osIsLinux = false;
    /**
     * True if a Solaris distribution has been detected
     */
    public static boolean osIsSolaris = false;

    private static void getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        osIsMacOsX = "mac os x".equals(os);
        osIsWindows = os != null && os.indexOf("windows") != -1;
        osIsLinux = os != null && os.indexOf("linux") != -1;
        osIsSolaris = os != null && os.indexOf("solaris") != -1;
    }

    /**
     * Set the dirs
     * @param rootDir The root dir or null, defaults to: USER_HOME + File.separator + ".yabs"
     */
    public static void setEnv(String rootDir) {

        if (osIsWindows) {
            USER_HOME = System.getenv("USERPROFILE");
        } else {
            USER_HOME = System.getProperty("user.home");
        }

        DESKTOP = USER_HOME + File.separator + "Desktop";
        if (rootDir == null) {
            MPPATH = USER_HOME + File.separator + ".yabs";
        } else {
            MPPATH = rootDir;
        }
        SETTINGS_FILE = Main.MPPATH + File.separator + "settings" + Constants.RELEASE_VERSION + ".yabs";
    }

    private static void parseArgs(String[] args) {

        DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        ArgumentBuilder abuilder = new ArgumentBuilder();
        GroupBuilder gbuilder = new GroupBuilder();

        Argument option = abuilder.withName("option").withMinimum(1).withMaximum(1).create();
        Argument filearg = abuilder.withName("=file").withMinimum(1).withMaximum(1).create();
        Argument dirarg = abuilder.withName("directory").withMinimum(1).withMaximum(1).create();
        Argument number = abuilder.withName("number").withMinimum(1).withMaximum(1).create();

        Option server = obuilder.withShortName("server").withShortName("serv").withDescription("start built-in server component").create();
        Option clear = obuilder.withShortName("clear").withDescription("clear the status of the yabs lock file").create();
        Option showenv = obuilder.withShortName("showenv").withShortName("se").withDescription("show environmental variables").create();
        Option netbook = obuilder.withShortName("netbook").withShortName("net").withDescription("use netbook size optimizations").create();
        Option help = obuilder.withShortName("help").withShortName("h").withDescription("print this message").create();
        Option license = obuilder.withShortName("license").withShortName("li").withDescription("print license").create();
        Option version = obuilder.withShortName("version").withDescription("print the version information and exit").create();
        Option verbose = obuilder.withShortName("verbose").withDescription("be extra verbose").create();
        Option nolfs = obuilder.withShortName("nolf").withDescription("use java native metal L&F").create();
        Option debug = obuilder.withShortName("debug").withDescription("enable debug logging").create();
        Option removeplugins = obuilder.withShortName("removeplugins").withDescription("remove all plugins which would be loaded").create();
        Option removelangs= obuilder.withShortName("noi18n").withDescription("remove all languages which would be loaded").create();
        Option logfile = obuilder.withShortName("logfile").withShortName("l").withDescription("use file for log").withArgument(filearg).create();
        Option mpdir = obuilder.withShortName("appdir").withShortName("dir").withShortName("path").withDescription("set the application main dir (used for caching, settings, temp files)").withArgument(dirarg).create();
        Option connectionInstance = obuilder.withShortName("connectionInstance").withShortName("conn").withDescription("use stored connection with this ID").withArgument(number).create();
        Option windowlog = obuilder.withShortName("windowlog").withDescription("enables logging to the MP Log Console").create();
        Option consolelog = obuilder.withShortName("consolelog").withDescription("enables logging to STDOUT").create();
        Option printtest = obuilder.withShortName("printtest").withDescription("test PDF printing").create();
        Option params = obuilder.withShortName("params").withDescription("optional parameters \"param1:value1;param2:value2..\"").withArgument(option).create();


        Group options = gbuilder.withName("options").
                withOption(help).
                withOption(version).
                withOption(verbose).
                withOption(debug).
                withOption(license).
                withOption(nolfs).
                withOption(netbook).
                withOption(showenv).
                withOption(removeplugins).
                withOption(removelangs).
                withOption(connectionInstance).
                withOption(logfile).
                withOption(server).
                withOption(windowlog).
                withOption(consolelog).
                withOption(mpdir).
                withOption(clear).
                withOption(printtest).
                withOption(params).
                create();

        HelpFormatter hf = new HelpFormatter();
        Parser p = new Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);

        CommandLine cl = p.parseAndHelp(args);



        if (cl == null) {
            System.err.println("Cannot parse arguments");
        } else {
            if (cl.hasOption(mpdir)) {
                setEnv(cl.getValue(mpdir).toString());
            }

            if (cl.hasOption(connectionInstance)) {
                try {
                    if (!LocalSettings.hasConnectionID(Integer.valueOf(String.valueOf(cl.getValue(connectionInstance))))) {
                        FORCE_INSTALLER = Integer.valueOf(String.valueOf(cl.getValue(connectionInstance)));
                    }
                    Log.Debug(Main.class, "Switching connection id to: " + Integer.valueOf(String.valueOf(cl.getValue(connectionInstance))));
                    LocalSettings.setConnectionID(Integer.valueOf(String.valueOf(cl.getValue(connectionInstance))));
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }

            if (cl.hasOption(help)) {
                hf.print();
                System.exit(0);
            }

            if (cl.hasOption(license)) {
                try {
                    System.out.print(new FileReaderWriter(new File(Main.class.getResource("/mpv5/resources/license/gpl-3").toURI())).read());
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }

            if (cl.hasOption(version)) {
                System.out.println("YABS Version: " + Constants.VERSION);
            }

            if (cl.hasOption(verbose)) {
                Log.setLogLevel(Log.LOGLEVEL_NORMAL);

            }

            if (cl.hasOption(debug)) {
                Log.setLogLevel(Log.LOGLEVEL_DEBUG);
            }

            if (cl.hasOption(logfile)) {
                if (String.valueOf(cl.getValue(logfile)).split("=").length != 2) {
                    Log.Debug(Main.class, "logfile must be specified (-logfile=file.log)!");
                } else {
                    try {
                        LogConsole.setLogFile(((String) cl.getValue(logfile)).split("=")[1]);
                    } catch (Exception e) {
                        Log.Debug(Main.class, "Error while writing to: " + e.getMessage());
                    }
                }
            }

            if (cl.hasOption(nolfs)) {
                setLaF("javax.swing.plaf.metal.MetalLookAndFeel");
                Main.nolf = true;
            }

            if (cl.hasOption(netbook)) {
                useNetbookOpt();
            }

            if (cl.hasOption(removeplugins)) {
                removeplugs = true;
            }

             if (cl.hasOption(removelangs)) {
               LanguageManager.disableLanguages();
            }

            if (cl.hasOption(showenv)) {
                printEnv();
            }

            if (cl.hasOption(server)) {
                START_SERVER = true;
            }

            if (cl.hasOption(clear)) {
                CLEAR_LOCK = true;
            }

            LogConsole.setLogStreams(cl.hasOption(logfile), cl.hasOption(consolelog), cl.hasOption(windowlog));

            if (cl.hasOption(params)) {
                try {
                    String[] parameters = String.valueOf(cl.getValue(params)).replace("\"", "").split(";");

                    for (int i = 0; i < parameters.length; i++) {
                        String[] opt = parameters[i].split(":");
                        String key = opt[0];
                        String val = opt[1];
                        User.PROPERTIES_OVERRIDE.addProperty(key, val);
                    }
                } catch (Exception exception) {
                    Log.Debug(exception);
                }
            }

            if (cl.hasOption(printtest)) {
                try {
                    PrintJob2 printJob2 = new PrintJob2(new File("printer_test.pdf"), "pdf");
                    System.exit(0);
                } catch (Exception ex) {
                    Log.Debug(ex);
                    System.exit(0);
                }
            }
        }

        if (cl != null) {
            Log.Print("\nOptions used:");

            for (int idx = 0; idx
                    < cl.getOptions().size(); idx++) {
                Log.Print(cl.getOptions().get(idx));
            }
            Log.Print("\n");
        }
    }

    /**
     * Redirect derby log file
     */
    public static void setDerbyLog() {
        Properties p = System.getProperties();
        try {
            File d = File.createTempFile(RandomText.getText(), ".~mp");
            d.createNewFile();
            p.put("derby.stream.error.file", d.getPath());
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    /**
     * 
     * @param lafname 
     */
    public static void setLaF(String lafname) {
        if (!Main.nolf) {
            try {
                if (lafname != null) {
                    UIManager.setLookAndFeel(lafname);
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());


                if (MPView.getIdentifierFrame() != null && MPView.getIdentifierFrame().isShowing()) {
                    MPView.getIdentifierFrame().setVisible(false);
                    SwingUtilities.updateComponentTreeUI(MPView.getIdentifierFrame());
                    MPView.getIdentifierFrame().setVisible(true);
                }
            } catch (Exception exe) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                } catch (Exception ex) {
                    Log.Debug(Main.class, ex);
                }
                Log.Debug(Main.class, exe);
            }
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
        writeLockFile(new FileReaderWriter(lockfile));
        setLaF(null);
        Main.splash.nextStep(Messages.INIT_LOGIN.toString());
        try {
            login();
        } catch (NodataFoundException nodataFoundException) {
            Log.Debug(nodataFoundException);
        }
        splash.nextStep(Messages.CACHE.toString());
        cache();
        Main.splash.nextStep(Messages.INIT_GUI.toString());

        super.show(new MPView(this));
        firstStart = firststart;

        if (Main.firstStart) {
            getApplication().getMainFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        SwingUtilities.updateComponentTreeUI(MPView.getIdentifierFrame());
        Main.splash.nextStep(Messages.INIT_PLUGINS.toString());
        loadPlugins();
        splash.dispose();

        if (START_SERVER) {
            MPServer serv = new MPServer();
            serv.start();
            MPView.getIdentifierView().showServerStatus(serv.isAlive());
        } else {
            MPView.getIdentifierView().showServerStatus(false);
        }
        if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
            final Thread startServerThread;

            if (!LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE)) {
                Runnable runnable2 = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Log.Debug(Main.class, "Starting OpenOffice as background service..");
                            NoaConnection.startOOServerIfNotRunning(LocalSettings.getProperty(LocalSettings.OFFICE_HOME), LocalSettings.getIntegerProperty(LocalSettings.OFFICE_PORT));
                        } catch (Exception n) {
                            Log.Debug(Main.class, n.getMessage());
                        }
                    }
                };
                startServerThread = new Thread(runnable2);
                startServerThread.start();

            } else {
                startServerThread = null;
            }

            Runnable runnable3 = new Runnable() {

                public void run() {
                    WSIManager.instanceOf().start();
                }
            };

            new Thread(runnable3).start();
            Runnable runnable1 = new Runnable() {

                public void run() {
                    boolean running = true;
                    while (running) {
                        if (startServerThread == null || !startServerThread.isAlive()) {
                            try {
                                Thread.sleep(3333);
                            } catch (InterruptedException ex) {
                            }
                            //Needed to move this to here; otherwise the oo connection may not be initialised
                            TemplateHandler.cacheTemplates();
                            running = false;
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                }
            };

            new Thread(runnable1).start();
        }
        Scheduler.getInstance().start();

        if (!LocalSettings.getBooleanProperty(LocalSettings.SUPPRESS_UPDATE_CHECK)) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if (checkUpdates()) {
                        MPView.addMessage(Messages.UPDATE_AVAILABLE);
                    }
                }
            };

            new Thread(runnable).start();
        }

        Runnable runnable1 = new Runnable() {

            @Override
            public void run() {
                try {
                    //Cleanup old files
                    FileDirectoryHandler.deleteDirectoryContent(new File(FileDirectoryHandler.getTempDir2()));
                } catch (IOException ex) {
                    Log.Debug(ex);
                }
            }
        };

        new Thread(runnable1).start();
    }

    private void loadPlugins() {
        if (!removeplugs) {
            try {
                MPPLuginLoader.queuePlugins(MPView.getPluginLoader().getPlugins());
                MPView.getPluginLoader().loadPlugins();
            } catch (Exception e) {
                Log.Debug(e);
            }
        } else {
            try {
                ArrayList data = DatabaseObject.getReferencedObjects(mpv5.db.objects.User.getCurrentUser(), Context.getPluginsToUsers());
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

    private void login() throws NodataFoundException {
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
        } else {
            LoginToInstanceScreen.load();
        }
    }

    private boolean probeDatabaseConnection() {
        try {
            DatabaseConnection.instanceOf();
            Log.Debug(this, "Connected to database: " + DatabaseConnection.instanceOf().getCtype().getURL());
            return true;
        } catch (Exception ex) {
            Log.Debug(this, "Could not connect to database: " + ex);
            return false;
        }
    }
    private static final String instanceIdentifier = ". Instance[";

    private boolean firstInstance() {
        if (!CLEAR_LOCK) {
            try {
                FileReaderWriter x = new FileReaderWriter(lockfile);
                if (lockfile.exists()) {
                    String[] xc = x.readLines();
                    for (int i = 0; i < xc.length; i++) {
                        String line = xc[i];
                        try {
                            if (line.length() > 0 && line.substring(line.lastIndexOf(instanceIdentifier) + instanceIdentifier.length(), line.lastIndexOf("]")).equals(String.valueOf(LocalSettings.getConnectionID()))) {
                                String message =
                                        "It looks like the application is already running.\n"
                                        + "\nThis may be caused by"
                                        + "\n\t- another instance of YaBS started with the same connection id (" + LocalSettings.getConnectionID() + ") "
                                        + "\n\t  (or no connection id at all)"
                                        + "\n\t- a previously crashed YaBS instance"
                                        + "\n\t- a manually killed YaBS instance"
                                        + "\n\t- a crash or kill of the JVM\n"
                                        + "\nYou might want to start YaBS once with the option -clear or to delete " + lockfile + " to get rid of this message.";
                                Log.Debug(this, message);
                                Popup.notice(message, 800, 200);

                                if (Log.getLoglevel() != Log.LOGLEVEL_DEBUG) {
                                    System.err.println(message);
                                }
                                return false;
                            }
                        } catch (Exception e) {
                            Log.Debug(this, line);
                            Log.Debug(e);
                        }
                    }
                    return true;
                } else {
                    return true;
                }
            } catch (Exception e) {
                Log.Debug(e);
                Log.Debug(this, "Application encountered some problem. Will try to continue anyway.");
                return true;
            }
        } else {
            return lockfile.delete();
        }
    }

    private void showDbWiz(Integer forConnId) {
        try {
            Log.setLogLevel(Log.LOGLEVEL_DEBUG);
            LogConsole.setLogFile("install.log");
            Log.Debug(this, new Date());

        } catch (Exception ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        Wizard w = new Wizard(true);
        w.addPanel(new wizard_DBSettings_simple_1(w, forConnId));
        w.showWiz();
    }

    private boolean writeLockFile(FileReaderWriter x) {
        try {
            x.write0("Locked on " + new Date() + instanceIdentifier + LocalSettings.getConnectionID() + "]");
            Log.Debug(this, "Application will start now: " + lockfile);
            lockfile.deleteOnExit();
            return true;
        } catch (Exception e) {
            Log.Debug(e);
            return false;
        }
    }

    private void clearLockFile() {
        FileReaderWriter x = new FileReaderWriter(lockfile);
        String[] lines = x.readLines();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if ((!line.contains(instanceIdentifier)) || (line.length() > 0 && line.substring(line.lastIndexOf(instanceIdentifier) + instanceIdentifier.length(), line.lastIndexOf("]")).equals(String.valueOf(LocalSettings.getConnectionID())))) {
                lines[i] = null;
            }
        }
        x.flush();
        x.write0(lines);
    }

    /**
     * Checks if updates are available
     */
    private boolean checkUpdates() {

        try {
            //Just a basic check
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(Constants.CURRENT_VERSION_URL).openConnection();
            con.setRequestMethod("GET");
            // When the available version is not the current version, we assume there is an update available
            Log.Debug(Main.class, Constants.CURRENT_VERSION_URL + " " + con.getResponseMessage());

            return (con.getResponseCode() != HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            Log.Debug(Main.class, e.getMessage());
            //When we cant reach it at all, no update presumably
            return false;
        }
    }
}
