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

import mpv5.db.common.NodataFoundException;
import mpv5.db.common.ReturnValue;
import mpv5.handler.TemplateHandler;
import mpv5.logging.*;
import mpv5.utils.reflection.Java10UrlClassloader;
import org.jdesktop.application.SingleFrameApplication;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectLock;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Template;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.pluginhandling.YabsPluginLoader;
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
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;
import mpv5.ui.dialogs.subcomponents.wizard_DBSettings_simple_1;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileExecutor;
import mpv5.utils.files.FileMonitor;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.print.PrintJob2;
import mpv5.utils.reflection.ClasspathTools;
import mpv5.utils.text.RandomText;
import mpv5.utils.xdocreport.YabsFontFactoryImpl;
import org.jdesktop.application.FrameView;

/**
 * The main class of the application.
 */
public class Main implements Runnable {

    public static SplashScreen splash;
    private static boolean removeplugs = false;
    /**
     * Is true if the application is running, false if in editor
     */
    public static boolean INSTANTIATED = false;
    private static Integer FORCE_INSTALLER;
    public static boolean HEADLESS = false;
    public static String WINDOW_TITLE = Constants.VERSION;
    public static int SINGLE_PORT = 65531;
    public static Java10UrlClassloader classLoader;

    /**
     * Use this method to (re) cache data from the database to avoid unnecessary
     * db queries
     */
    public static void cache() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Log.Debug(Main.class, Messages.CACHE);
                User.cacheUser();
                Log.Debug(Main.class, Messages.CACHED_OBJECTS + ": " + Context.getUser());
                LanguageManager.getCountriesAsComboBoxModel();
                Log.Debug(Main.class, Messages.CACHED_OBJECTS + ": " + Context.getCountries());
                if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.cachefonts", false)) {
                    YabsFontFactoryImpl.cacheFonts();
                    Log.Debug(Main.class, Messages.CACHED_OBJECTS + ": " + Context.getFonts());
                }
            }
        };
        new Thread(runnable).start();
//        Account.cacheAccounts();//pre cache accounts
//        YabsView.addMessage(Messages.CACHED_OBJECTS + ": " + Context.getAccounts());
//        DatabaseObject.cacheObjects();//Is called by User.login() later
    }

    /**
     * Add the processes to close on exit
     *
     * @param application
     */
    public static void addProcessToClose(Process application) {
        oap.add(application);
    }
    private static List<Process> oap = new ArrayList<Process>();

    private static void readLocalSettings() throws Exception {
        splash.nextStep(Messages.LOCAL_SETTINGS.toString());
        try {
            LocalSettings.read();
            LocalSettings.apply();
            Log.Print("Done with local settings file: " + LocalSettings.getLocalFile());
        } catch (Exception ex) {
            Log.Print(ex);
            Log.Print("Local settings file not readable: " + LocalSettings.getLocalFile());
        }
    }

    private static void readGlobalSettings() {
        try {
            GlobalSettings.read();
        } catch (Exception ex) {
            Log.Debug(Main.class, ex);
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

    public static void extStart(Class<YabsApplication> app, Class<MPView> view, String... args) throws Exception {
        APPLICATION_CLASS = app;
        VIEW_CLASS = view;
        main(args);
    }

    /**
     * Launch the application
     */
    public static void start() {
        splash.setVisible(true);
//        splash.nextStep(Messages.LAUNCH.toString());
        splash.nextStep(Messages.DBCONN_UPDATE_BEPATIENT.toString());
        Log.Debug(Main.class, "Trying to launch application now..");

        try {
            File f = new File(FileDirectoryHandler.getTempDir(), Constants.VERSION + ".start");
            if (!f.exists()) {
                System.setProperty("yabsFirstStartVersion", Constants.VERSION);
                FileDirectoryHandler.writeTo(f, System.currentTimeMillis());
            }
            Log.Debug(Main.class, "CACHE_DIR: " + f.getCanonicalPath());
        } catch (Exception exception) {
            Log.Debug(exception);
        }
        Runnable runnable = new Runnable() {

            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    SingleFrameApplication.launch(APPLICATION_CLASS, new String[]{});
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
     * Sometimes it is nice to have a good goodbye message at hand ;-)
     *
     *
     * <b>"So Long, and Thanks for All the Fish."</b>
     */
    public static final String GOODBYE_MESSAGE = "So Long, and Thanks for All the Fish.";
    /**
     * Indicates whether the server component shall start
     */
    public static boolean START_SERVER = false;
    /**
     * Indicates whether lib check shall be skipped
     */
    public static boolean SKIP_LIBCHECK = false;
    /**
     * Indicates whether template check shall be skipped
     */
    public static boolean SKIP_TPLCHECK = false;
    /**
     * The Yabs Application (JSAF)
     */
    public static Class<YabsApplication> APPLICATION_CLASS;
    /**
     * The Yabs View (JSAF FrameView)
     */
    public static Class<MPView> VIEW_CLASS;

    /**
     * At startup create and show the main frame of the application.
     */
    public void startup() {

        checkLibs();
        checkSingleInstance();

        Log.Debug(this, "Startup procedure... ");
        getApplication().getContext().getLocalStorage().setDirectory(new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)));

        splash.nextStep(Messages.FIRST_INSTANCE.toString());
        splash.nextStep(Messages.DB_CHECK.toString());

        ControlPanel_Fonts.applyFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));
        if (FORCE_INSTALLER == null) {
            Log.Debug(this, "Probing database connection... ");
            if (probeDatabaseConnection()) {

                splash.nextStep(Messages.DBCONN_UPDATE_BEPATIENT.toString());
                QueryHandler.instanceOf();

                readGlobalSettings();
                Log.Debug(this, "Loading Yabs... ");

                Main.splash.nextStep(Messages.INIT_LOGIN.toString());
                try {
                    login();
                } catch (NodataFoundException nodataFoundException) {
                    Log.Debug(nodataFoundException);
                }
                Main.splash.nextStep(Messages.INIT_GUI.toString());
                if (!HEADLESS) {
                    try {
                        @SuppressWarnings("unchecked")
                        FrameView view = (FrameView) VIEW_CLASS.getDeclaredConstructor(SingleFrameApplication.class).newInstance(getApplication());

                        enableOSXFullscreen(view.getFrame());

                        getApplication().setMainView(view);
                        getApplication().show(view);

                        requestToggleFullScreen(view.getFrame());

//                        Log.Print(Arrays.asList(getApplication().getMainView().getClass().getInterfaces()));
                    } catch (Exception ex) {
                        Log.Debug(ex);
                        System.exit(1);
                    }
                    YabsViewProxy.instance().register((YabsView) getApplication().getMainView());
                }
                go(false);

            } else if (Popup.Y_N_dialog(splash, Messages.NO_DB_CONNECTION, Messages.FIRST_START.toString())) {
                Log.Debug(this, "Loading database config wizard...");
                splash.setVisible(false);

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

    public void shutdown() {
        if (Log.isDebugging()) {
            Log.Print(Messages.getMissing());
        }

        if (!HEADLESS) {
            getApplication().getMainFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }

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
                    Log.Debug(this, "Killing process: " + p);
                    p.destroy();
                    p.waitFor();
                } catch (Exception n) {
                    Log.Debug(this, n);
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
        }

        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
//            Log.Debug(Main.class, QueryHandler.instanceOf().getStatistics());
        }
        Log.Print(GOODBYE_MESSAGE);

        try {
            //Cleanup
            FileDirectoryHandler.deleteDirectoryContent2(new File(FileDirectoryHandler.getTempDir2()), ".properties", ".xml", ".start");
        } catch (IOException ex) {
        }
    }

    /**
     * Main method launching the application.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        classLoader = new Java10UrlClassloader(new URL[0], Main.class.getClassLoader());

        INSTANTIATED = true;
        if (APPLICATION_CLASS == null) {//fallback
            APPLICATION_CLASS = YabsApplication.class;
        }
        if (VIEW_CLASS == null) {//fallback
            VIEW_CLASS = MPView.class;
        }

        try {
            getOS();
            setEnv(null);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            splash = new SplashScreen(new ImageIcon(Main.class.getResource(mpv5.globals.Constants.SPLASH_IMAGE)));
            splash.init(12);
            Log.Print(Messages.START_MESSAGE);
            splash.nextStep(Messages.INIT.toString());

            parseArgs(args);
            runStartScripts();
            readLocalSettings();
            LanguageManager.preLoadCachedLanguage();

            setDerbyLog();
            start();
        } catch (Exception e) {
            Popup.error(null, e);
            System.exit(1);
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
        String os = System.getProperties().getProperty("os.name").toLowerCase();
        osIsMacOsX = "mac os x".equals(os);
        osIsWindows = os != null && os.contains("windows");
        osIsLinux = os != null && os.contains("linux");
        osIsSolaris = os != null && os.contains("solaris");
    }

    /**
     * Set the dirs
     *
     * @param rootDir The root dir or null, defaults to: USER_HOME +
     * File.separator + ".yabs"
     */
    public static void setEnv(String rootDir) {

        if (osIsMacOsX) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", Constants.TITLE);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.application.name", Constants.TITLE);
//            Application app = Application.getApplication();
//
//            app.setAboutHandler(new AboutHandler() {
//                @Override
//                public void handleAbout(AboutEvent ae) {
//                    new About(new ImageIcon(About.class.getResource(mpv5.globals.Constants.ABOUT_IMAGE)));
//                }
//            });
//
////	app.setPreferencesHandler(new PreferencesHandler() {
////                @Override
////		public void handlePreferences(PreferencesEvent pe) {}
////	});
//            app.setQuitHandler(new QuitHandler() {
//                @Override
//                public void handleQuitRequestWith(QuitEvent event, QuitResponse response) {
//                    ((YabsApplication) YabsApplication.getInstance()).shutdown();
//                }
//            });
        }

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

        org.apache.commons.cli2.builder.DefaultOptionBuilder obuilder = new org.apache.commons.cli2.builder.DefaultOptionBuilder();
        org.apache.commons.cli2.builder.ArgumentBuilder abuilder = new org.apache.commons.cli2.builder.ArgumentBuilder();
        org.apache.commons.cli2.builder.GroupBuilder gbuilder = new org.apache.commons.cli2.builder.GroupBuilder();

        org.apache.commons.cli2.Argument option = abuilder.withName("option").withMinimum(1).withMaximum(1).create();
        org.apache.commons.cli2.Argument filearg = abuilder.withName("=file").withMinimum(1).withMaximum(1).create();
        org.apache.commons.cli2.Argument dirarg = abuilder.withName("directory").withMinimum(1).withMaximum(1).create();
        org.apache.commons.cli2.Argument number = abuilder.withName("number").withMinimum(1).withMaximum(1).create();

        org.apache.commons.cli2.Option server = obuilder.withShortName("server").withShortName("serv").withDescription("start built-in server component").create();
        org.apache.commons.cli2.Option ignoreLibs = obuilder.withShortName("ignorelibs").withShortName("il").withDescription("ignore Libs checking").create();
        org.apache.commons.cli2.Option ignoreTpls = obuilder.withShortName("ignoretpls").withShortName("it").withDescription("ignore Templateupdate checking").create();
        org.apache.commons.cli2.Option showenv = obuilder.withShortName("showenv").withShortName("se").withDescription("show environmental variables").create();
        org.apache.commons.cli2.Option help = obuilder.withShortName("help").withShortName("h").withDescription("print this message").create();
        org.apache.commons.cli2.Option license = obuilder.withShortName("license").withShortName("li").withDescription("print license").create();
        org.apache.commons.cli2.Option version = obuilder.withShortName("version").withDescription("print the version information and exit").create();
        org.apache.commons.cli2.Option verbose = obuilder.withShortName("verbose").withDescription("be extra verbose").create();
        org.apache.commons.cli2.Option nolfs = obuilder.withShortName("nolf").withDescription("use java native metal L&F").create();
        org.apache.commons.cli2.Option debug = obuilder.withShortName("debug").withDescription("enable debug logging").create();
        org.apache.commons.cli2.Option removeplugins = obuilder.withShortName("removeplugins").withDescription("remove all plugins which would be loaded").create();
        org.apache.commons.cli2.Option removelangs = obuilder.withShortName("noi18n").withDescription("remove all languages which would be loaded").create();
        org.apache.commons.cli2.Option forceinstall = obuilder.withShortName("finstall").withDescription("force-install").create();
        org.apache.commons.cli2.Option logfile = obuilder.withShortName("logfile").withShortName("l").withDescription("use file for log").withArgument(filearg).create();
        org.apache.commons.cli2.Option mpdir = obuilder.withShortName("appdir").withShortName("dir").withShortName("path").withDescription("set the application main dir (used for caching, settings, temp files)").withArgument(dirarg).create();
        org.apache.commons.cli2.Option connectionInstance = obuilder.withShortName("connectionInstance").withShortName("conn").withDescription("use stored connection with this ID").withArgument(number).create();
        org.apache.commons.cli2.Option windowlog = obuilder.withShortName("windowlog").withDescription("enables logging to the MP Log Console").create();
        org.apache.commons.cli2.Option consolelog = obuilder.withShortName("consolelog").withDescription("enables logging to STDOUT").create();
        org.apache.commons.cli2.Option printtest = obuilder.withShortName("printtest").withDescription("test PDF printing").create();
        org.apache.commons.cli2.Option checkResources = obuilder.withShortName("checkRes").withDescription("checks the base resource file").create();
        org.apache.commons.cli2.Option params = obuilder.withShortName("params").withDescription("optional parameters \"param1:value1;param2:value2..\"").withArgument(option).create();

        org.apache.commons.cli2.Group options = gbuilder.withName("options").
                withOption(help).
                withOption(version).
                withOption(verbose).
                withOption(debug).
                withOption(license).
                withOption(nolfs).
                withOption(showenv).
                withOption(removeplugins).
                withOption(removelangs).
                withOption(connectionInstance).
                withOption(logfile).
                withOption(server).
                withOption(windowlog).
                withOption(consolelog).
                withOption(mpdir).
                withOption(printtest).
                withOption(params).
                withOption(forceinstall).
                withOption(ignoreLibs).
                withOption(ignoreTpls).
                withOption(checkResources).
                create();

        org.apache.commons.cli2.util.HelpFormatter hf = new org.apache.commons.cli2.util.HelpFormatter();
        org.apache.commons.cli2.commandline.Parser p = new org.apache.commons.cli2.commandline.Parser();
        p.setGroup(options);
        p.setHelpFormatter(hf);

        org.apache.commons.cli2.CommandLine cl = p.parseAndHelp(args);

        if (cl == null) {
            Log.Print("Cannot parse arguments");
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

            if (cl.hasOption(forceinstall)) {
                FORCE_INSTALLER = RandomText.getInteger();
            }

            if (cl.hasOption(help)) {
                hf.print();
                System.exit(0);
            }

            if (cl.hasOption(license)) {
                try {
                    System.out.print(new FileReaderWriter(new File(Main.class.getResource("/license/gpl-3").toURI())).read());
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
                        YConsole.setLogFile(((String) cl.getValue(logfile)).split("=")[1]);
                    } catch (Exception e) {
                        Log.Debug(Main.class, "Error while writing to: " + e.getMessage());
                    }
                }
            }

            if (cl.hasOption(nolfs)) {
                Main.nolf = true;
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

            if (cl.hasOption(ignoreLibs)) {
                SKIP_LIBCHECK = true;
            }

            if (cl.hasOption(ignoreTpls)) {
                SKIP_TPLCHECK = true;
            }

            YConsole.setLogStreams(cl.hasOption(logfile), cl.hasOption(consolelog), cl.hasOption(windowlog));

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

            if (cl.hasOption(checkResources)) {
                LanguageManager.checkBundle(null, true);
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
            File d = FileDirectoryHandler.getTempFile("derby");
            d.createNewFile();
            Log.Debug(Main.class, "Setting derby log to " + d.getPath());
            p.put("derby.stream.error.file", d.getPath());
//            p.setProperty("derby.drda.startNetworkServer", "true");

        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    /**
     *
     * @param lafname
     */
    public static void setLaF(final String lafname) {
        if (!Main.nolf && !HEADLESS) {
            try {
                if (lafname != null && lafname.trim().length() > 0) {
                    try {
                        Class.forName(lafname);
                    } catch (ClassNotFoundException ex) {
                        Log.Debug(Main.class, "Laf not valid here: " + lafname);
                        return;
                    }
                    UIManager.setLookAndFeel(lafname);

//                    BasicLookAndFeel darcula = new DarculaLaf();
//                    UIManager.setLookAndFeel(darcula);
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                LookAndFeelAddons.setAddon(LookAndFeelAddons.getBestMatchAddonClassName());

            } catch (Exception exe) {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

                } catch (Exception ex) {
                    Log.Debug(Main.class, ex);
                }
                Log.Debug(Main.class, exe);
            }
            try {
                if (getApplication().isReady() && getApplication().getMainView().getFrame() != null && (getApplication().getMainView()).getFrame().isShowing()) {
                    SwingUtilities.updateComponentTreeUI((getApplication().getMainView()).getFrame());
                    ((YabsView) getApplication().getMainView()).getIdentifierFrame().validate();
                }
            } catch (Exception e) {
                Log.Debug(Main.class, e.getMessage());
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

        splash.nextStep(Messages.CACHE.toString());
        cache();

        Main.splash.nextStep(Messages.INIT_PLUGINS.toString());

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    loadPlugins();
                } catch (Exception e) {
                    Popup.error(e);
                }
            }
        };
        new Thread(runnable).start();
        splash.dispose();

        if (!HEADLESS) {
//            if (START_SERVER) {
//                MPServer.runServer();
//            }
        }

        /* Runnable runnable3 = new Runnable() {

            @Override
            public void run() {
                WSIManager.instanceOf().start();
            }
        };

        new Thread(runnable3).start();*/
        if (!HEADLESS) {
            try {
                (new Scheduler()).start();
            } catch (Exception e) {
                Log.Debug(e);
            }
        }

        if (!HEADLESS) {
//            if (GlobalSettings.getBooleanProperty("org.openyabs.updates.enable") && !LocalSettings.getBooleanProperty(LocalSettings.SUPPRESS_UPDATE_CHECK)) {
//                Runnable runnable1 = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (checkUpdates()) {
//                            Notificator.raiseNotification(Messages.UPDATE_AVAILABLE, false);
//                        }
//                    }
//                };
//
//                new Thread(runnable1).start();
//            }
        }


        if (!HEADLESS) {
           // checkTpls();
        }
    }

    private void loadPlugins() {
        if (!HEADLESS) {
            if (!removeplugs) {
                try {
                    YabsPluginLoader.queuePlugins();
                    YabsPluginLoader.loadPlugins();
                } catch (Throwable e) {
                    Log.Debug(e);
                    Popup.notice("Plugin ERROR " + e);
                }
            } else {
                try {
                    List data = DatabaseObject.getReferencedObjects(mpv5.db.objects.User.getCurrentUser(), Context.getPluginsToUsers());
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
    }

    private void login() throws NodataFoundException {
        if (!LocalSettings.getProperty("lastuser").equals("INSTANCE")) {
            User usern1 = new User();
            Log.Debug(this, "Checking for auto login.. ");

            try {
                if (usern1.fetchDataOf(Integer.valueOf(LocalSettings.getProperty("lastuser")))) {
                    Log.Debug(this, "Trying to login user: " + usern1);
                    User user = mpv5.usermanagement.MPSecurityManager.checkAuthInternal(usern1, LocalSettings.getProperty("lastuserpw"));
                    Log.Debug(this, "Found user: " + user);
                    if (user != null) {
                        user.login();
                    } else {
                        doLoginToInstanceScreen();
                    }
                }
            } catch (Exception nodataFoundException) {
                doLoginToInstanceScreen();
            }
        } else {
            doLoginToInstanceScreen();
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

    private boolean firstInstance() {
        return true;
    }

    private void showDbWiz(Integer forConnId) {
        try {
            Log.setLogLevel(Log.LOGLEVEL_DEBUG);
            YConsole.setLogFile("install.log");
            Log.Debug(this, new Date());
            System.setProperty("yabs_firststart", "true");
        } catch (Exception ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            User u = User.DEFAULT;
            u.setInthighestright(MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE);
            User._setUser(User.DEFAULT);
        } catch (Exception e) {
            Log.Debug(e);
        }

        Wizard w = new Wizard(true);
        w.addPanel(new wizard_DBSettings_simple_1(w, forConnId));
        w.showWiz();
    }

    /**
     * Checks if updates are available
     *
     * @return
     */
    public static boolean checkUpdates() {

        try {
            //Just a basic check
            String url = GlobalSettings.getProperty("org.openyabs.updates.url");
            if ("null".equals(url)) {
                return false;
            }
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            // When the available version is not the current version, we assume there is an update available
            Log.Debug(Main.class, url + " " + con.getResponseMessage());

            return (con.getResponseCode() != HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            Log.Debug(Main.class, e.getMessage());
            //When we cant reach it at all, no update presumably
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static SingleFrameApplication getApplication() {
        return (SingleFrameApplication) SingleFrameApplication.getInstance(APPLICATION_CLASS);
    }

    private void checkSingleInstance() {
        if (LocalSettings.hasProperty(LocalSettings.DBTYPE)
                && LocalSettings.getProperty(LocalSettings.DBTYPE).equals("single")) {
            try {
                Socket test = new Socket("localhost", Main.SINGLE_PORT);
                Log.Print("*** Already running!");
                System.exit(1);
            } catch (Exception e) {
                Log.Print("*** First instance.. running!");
                new Thread(this).start();
            }
        }
    }

    @Override
    public void run() {
        Socket clientSocket;
        try {
            // Create the server socket
            ServerSocket serverSocket = new ServerSocket(Main.SINGLE_PORT, 10);
            while (true) {
                // Wait for a connection
                clientSocket = serverSocket.accept();
                java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        getApplication().getMainFrame().setVisible(true);
                        getApplication().getMainFrame().requestFocus();
                    }
                });

                clientSocket.close();
            }
        } catch (IOException ioe) {
            Log.Debug(ioe);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkLibs() {
        splash.nextStep(Messages.CHECK_LIBS.toString());
        if (!SKIP_LIBCHECK) {
            final String[] libs;
            try {
                libs = ClasspathTools.findLibsFromManifest(Main.class);
                if (libs != null) {
                    Runnable runnable1 = new Runnable() {

                        @Override
                        public void run() {
                            File libdir = new File(Constants.LIBS_DIR);
                            if (!libdir.exists()) {
                                Log.Debug(Main.class,
                                        "Libcheck failed in " + Constants.LIBS_DIR);
                                return;
                            }

                            boolean failed = false;
                            for (int i = 0; i < libs.length; i++) {
                                Log.Debug(Main.class, "Checking: " + libs[i]);
                                File lib = new File(libs[i]);
                                failed = !lib.canRead();
                                if (failed) {
                                    Notificator.raiseNotification("Missing: " + libs[i], false);
                                }
                            }
                            if (failed == true) {
                                Notificator.raiseNotification(Messages.MISSING_LIBS.toString(), false);
                                //YabsApplication.getInstance().exit();
                            }
                        }
                    };

                    new Thread(runnable1).start();
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkTpls() {
        splash.nextStep(Messages.CHECK_TPLUPDATE.toString());
        if (!SKIP_TPLCHECK) {
            Runnable runnable1 = new Runnable() {

                Object[][] data = null;

                @Override
                public void run() {
                    try {
                        ReturnValue rv = QueryHandler.instanceOf().clone(Context.getTemplate()).select(false);
                        data = rv.getData();
                        for (int i = 0; i < data.length; i++) {
                            final Template tpl = (Template) Template.getObject(Context.getTemplate(), Integer.parseInt(data[i][0].toString()));
                            FileMonitor.FileChangeListener filecl = new FileMonitor.FileChangeListener() {

                                @Override
                                public void fileChanged(String fileName) {
                                    QueryHandler.instanceOf().clone(Context.getFiles()).updateFile(new File(fileName), tpl.__getFilename());
                                    tpl.setDescription(tpl.__getDescription() + "\n - Updated: " + new Date());
                                    tpl.save(true);
                                    TemplateHandler.clearCache();
                                }
                            };

                            if (!tpl.__getPathtofile().equals("")) {
                                if (tpl.__getisupdateenabled()) {
                                    File file = new File(tpl.__getPathtofile());
                                    if (file.exists()) {
                                        Log.Debug(this, tpl.__getLastmodified());
                                        Log.Debug(this, file.lastModified());
                                        if (tpl.__getLastmodified() < file.lastModified()) {
                                            QueryHandler.instanceOf().clone(Context.getFiles()).updateFile(file, tpl.__getFilename());
                                            tpl.setDescription(tpl.__getDescription() + "\n - Updated: " + new Date());
                                            tpl.setLastmodified(file.lastModified());
                                            tpl.save(true);
                                            TemplateHandler.clearCache();
                                        }
                                    }
                                    FileMonitor.getInstance().addFileChangeListener(filecl, tpl.__getPathtofile(), 1000l);
                                }
                            }
                        }
                    } catch (NodataFoundException ex) {
                        Log.Debug(this, ex.getMessage());
                       // YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_DEFINDED, Color.YELLOW);
                    }
                }
            };
            new Thread(runnable1).start();
        }
    }

    private void doLoginToInstanceScreen() {
        DatabaseConnection.shutdown();
        LoginToInstanceScreen.load();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void enableOSXFullscreen(Window window) {
        if (!Main.osIsMacOsX) {
            return;
        }
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void requestToggleFullScreen(Window window) {
        if (!Main.osIsMacOsX || !User.getCurrentUser().getProperties().getProperty("org.openyabs.macproperty", "fullscreen")) {
            return;
        }
        try {
            Class appClass = Class.forName("com.apple.eawt.Application");
            Class params[] = new Class[]{};

            Method getApplication = appClass.getMethod("getApplication", params);
            Object application = getApplication.invoke(appClass);
            Method requestToggleFulLScreen = application.getClass().getMethod("requestToggleFullScreen", Window.class);

            requestToggleFulLScreen.invoke(application, window);
        } catch (Exception e) {
            System.out.println("An exception occurred while trying to toggle full screen mode");
        }
    }
}
