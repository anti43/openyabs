/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.JComponent;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.globals.VersionInfo;
import mpv5.handler.Activity;
import mpv5.handler.SearchService;
import mpv5.handler.UpdateCheck;
import mpv5.logging.Log;
import java.util.Locale;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.JournalExport;
import mpv5.plugin.api.PluginSettings;
import mpv5.plugin.api.PrivacyContributor;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.panels.DataPanel;

/**
 * Finds, starts and stops the plugins ({@link YabsExtension}): the ones built
 * into YaBS and the jars in the plugins folder. Everything a plugin adds is
 * kept per plugin and removed when it stops; a failing plugin is marked and
 * does not disturb the others.
 */
public final class ExtensionManager {

    /**
     * The state of a plugin
     */
    public enum State {
        RUNNING, STOPPED, DISABLED, FAILED
    }

    /**
     * What the host application does for the plugins (the Swing side)
     */
    public interface Host {

        void openTab(String title, JComponent content);

        void notify(String message);

        /**
         * Runs on the UI thread (later or now)
         */
        void ui(Runnable r);

        /**
         * The plugins or their commands changed (e.g. rebuild the menu)
         */
        void changed();

        void error(String plugin, Throwable t);

        /**
         * @return the record views currently open in the main window
         */
        default List<DataPanel> openViews() {
            return Collections.emptyList();
        }
    }

    /**
     * An installed plugin
     */
    public static final class Installed {

        public final YabsExtension extension;
        /**
         * the jar, null for a plugin built into YaBS
         */
        public final File jar;
        final URLClassLoader loader;
        volatile State state = State.STOPPED;
        volatile String error;

        Installed(YabsExtension extension, File jar, URLClassLoader loader) {
            this.extension = extension;
            this.jar = jar;
            this.loader = loader;
        }

        public State getState() {
            return state;
        }

        /**
         * @return why it failed, or null
         */
        public String getError() {
            return error;
        }

        public String id() {
            return extension.id();
        }

        public boolean isBuiltIn() {
            return jar == null;
        }
    }

    /**
     * A command of a plugin
     */
    public static final class Command {

        public final String owner;
        public final String plugin;
        public final String title;
        public final String description;
        public final Runnable action;

        Command(String owner, String plugin, String title, String description, Runnable action) {
            this.owner = owner;
            this.plugin = plugin;
            this.title = title;
            this.description = description == null ? "" : description;
            this.action = action;
        }

        public boolean matches(String needle) {
            return SearchService.matches(title + " " + description + " " + plugin, needle);
        }
    }

    /**
     * A settings page of a plugin
     */
    public static final class Page {

        public final String owner;
        public final String title;
        public final String description;
        public final Supplier<JComponent> page;

        Page(String owner, String title, String description, Supplier<JComponent> page) {
            this.owner = owner;
            this.title = title;
            this.description = description == null ? "" : description;
            this.page = page;
        }
    }

    private static final class Owned<T> {

        final String owner;
        final T value;

        Owned(String owner, T value) {
            this.owner = owner;
            this.value = value;
        }
    }

    private static ExtensionManager instance;
    private final List<Installed> installed = new CopyOnWriteArrayList<Installed>();
    private final List<Command> commands = new CopyOnWriteArrayList<Command>();
    private final List<Page> pages = new CopyOnWriteArrayList<Page>();
    private final List<Owned<Function<String, List<SearchResult>>>> providers = new CopyOnWriteArrayList<Owned<Function<String, List<SearchResult>>>>();
    private final List<Owned<Consumer<Activity.Entry>>> listeners = new CopyOnWriteArrayList<Owned<Consumer<Activity.Entry>>>();
    private final List<Owned<DatabaseObjectModifier>> modifiers = new CopyOnWriteArrayList<Owned<DatabaseObjectModifier>>();
    private final List<Owned<Consumer<DataPanel>>> viewListeners = new CopyOnWriteArrayList<Owned<Consumer<DataPanel>>>();
    private final List<Owned<JournalExport>> journalExports = new CopyOnWriteArrayList<Owned<JournalExport>>();
    private final List<Owned<Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>>>> templateFieldProviders
            = new CopyOnWriteArrayList<Owned<Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>>>>();
    private final List<Owned<Function<mpv5.db.common.DatabaseObject, String>>> mailWarningProviders
            = new CopyOnWriteArrayList<Owned<Function<mpv5.db.common.DatabaseObject, String>>>();
    private final List<Owned<Predicate<mpv5.db.objects.Item>>> eInvoiceSkipRules
            = new CopyOnWriteArrayList<Owned<Predicate<mpv5.db.objects.Item>>>();
    private final List<Owned<PrivacyContributor>> privacyContributors
            = new CopyOnWriteArrayList<Owned<PrivacyContributor>>();
    private final List<Owned<mpv5.plugin.api.LockedDocumentProvider>> lockedDocumentProviders
            = new CopyOnWriteArrayList<Owned<mpv5.plugin.api.LockedDocumentProvider>>();
    private final java.util.Map<String, java.util.Map<String, java.util.Map<String, String>>> translations
            = new java.util.concurrent.ConcurrentHashMap<String, java.util.Map<String, java.util.Map<String, String>>>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<Runnable>();
    private final File folder;
    private Host host;
    private boolean activityHooked;
    private final java.util.concurrent.atomic.AtomicInteger startEpoch = new java.util.concurrent.atomic.AtomicInteger();

    /**
     * @param folder the plugins folder
     * @param host
     */
    public ExtensionManager(File folder, Host host) {
        this.folder = folder;
        this.host = host;
    }

    /**
     * @return the plugins of YaBS (folder "plugins" in the YaBS folder)
     */
    public static synchronized ExtensionManager instance() {
        if (instance == null) {
            String base = mpv5.Main.MPPATH != null ? mpv5.Main.MPPATH : System.getProperty("user.home");
            instance = new ExtensionManager(new File(base, "plugins"), new SwingHost());
        }
        return instance;
    }

    /**
     * @return the current instance if already created, or {@code null} (avoids
     * initializing Swing host in headless tests)
     */
    public static synchronized ExtensionManager instanceOrNull() {
        return instance;
    }

    /**
     * @return the folder the plugin jars are in
     */
    public File folder() {
        return folder;
    }

    /**
     * @param r told when plugins or their contributions change (any thread)
     */
    public void addChangeListener(Runnable r) {
        if (!changeListeners.contains(r)) {
            changeListeners.add(r);
        }
    }

    /**
     * @param r no longer told
     */
    public void removeChangeListener(Runnable r) {
        changeListeners.remove(r);
    }

    // ------------------------------------------------------------------ discovery
    /**
     * Finds the built-in plugins and the jars in the folder (once)
     */
    public synchronized void discover() {
        if (!installed.isEmpty()) {
            return;
        }
        ServiceLoader<YabsExtension> builtIn = ServiceLoader.load(YabsExtension.class, ExtensionManager.class.getClassLoader());
        for (YabsExtension e : safe(builtIn)) {
            add(e, null, null);
        }
        File[] files = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".jar") || n.toLowerCase().endsWith(".groovy"));
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                loadFile(f);
            }
        }
    }

    private List<YabsExtension> safe(ServiceLoader<YabsExtension> l) {
        List<YabsExtension> list = new ArrayList<YabsExtension>();
        java.util.Iterator<YabsExtension> it = l.iterator();
        while (true) {
            try {
                if (!it.hasNext()) {
                    break;
                }
                list.add(it.next());
            } catch (Throwable t) {
                //a broken entry must not stop the others
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return list;
    }

    /**
     * Loads the plugins of a jar or a Groovy script (without starting them)
     *
     * @param f
     * @return the plugins found
     */
    public synchronized List<Installed> loadFile(File f) {
        if (f.getName().toLowerCase().endsWith(".groovy")) {
            List<Installed> l = new ArrayList<Installed>();
            try {
                Installed i = add(new GroovyExtension(f), f, null);
                if (i != null) {
                    l.add(i);
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
            return l;
        }
        return loadJar(f);
    }

    /**
     * Loads the plugins of a jar (without starting them)
     *
     * @param jar
     * @return the plugins found in the jar
     */
    public synchronized List<Installed> loadJar(File jar) {
        List<Installed> found = new ArrayList<Installed>();
        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, ExtensionManager.class.getClassLoader());
            for (YabsExtension e : safe(ServiceLoader.load(YabsExtension.class, loader))) {
                //only the plugins of this jar, not the built-in ones seen through the parent
                if (e.getClass().getClassLoader() == loader) {
                    Installed i = add(e, jar, loader);
                    if (i != null) {
                        found.add(i);
                    }
                }
            }
        } catch (Throwable t) {
            Log.Debug(ExtensionManager.class, t);
        }
        if (found.isEmpty() && loader != null) {
            close(loader);
        }
        return found;
    }

    /**
     * Set while {@link #install(File)} loads the new file
     */
    private boolean replacing = false;

        private Installed add(YabsExtension e, File jar, URLClassLoader loader) {
        String id;
        try {
            id = e.id();
        } catch (Throwable t) {
            Log.Debug(ExtensionManager.class, t);
            return null;
        }
        if (id != null && replacing && jar != null && find(id) != null) {
            //installing a plugin that is there already from another file (e.g. the Java
            //version of a Groovy script): the old one goes, its file is kept as *.replaced
            Installed old = find(id);
            if (old.jar != null && !old.jar.equals(jar)) {
                stop(old);
                installed.remove(old);
                close(old.loader);
                File moved = new File(old.jar.getPath() + ".replaced");
                if (!old.jar.renameTo(moved)) {
                    Log.Debug(ExtensionManager.class, "Could not rename " + old.jar);
                }
                Log.Debug(ExtensionManager.class, "Plugin " + id + " replaced by " + jar + " (old file: " + moved + ")");
            }
        }
        if (id == null || id.trim().isEmpty() || find(id) != null) {
            Log.Debug(ExtensionManager.class, "Plugin without id or twice: " + id + " (" + jar + ")");
            return null;
        }
        Installed i = new Installed(e, jar, loader);
        if (disabled().contains(id)) {
            i.state = State.DISABLED;
        }
        installed.add(i);
        return i;
    }

    /**
     * Registers a plugin directly (e.g. from code or tests)
     *
     * @param e
     * @return the installed plugin, null if the id is taken
     */
    public synchronized Installed register(YabsExtension e) {
        Installed i = add(e, null, null);
        fireChanged();
        return i;
    }

    // ------------------------------------------------------------------ lifecycle
    /**
     * @return all plugins
     */
    public List<Installed> list() {
        return Collections.unmodifiableList(installed);
    }

    /**
     * @param id
     * @return the plugin or null
     */
    public Installed find(String id) {
        for (Installed i : installed) {
            if (i.id().equals(id)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Discovers and starts all enabled plugins without blocking the user
     * interface (issue #159): finding the plugins and compiling the Groovy
     * scripts runs in the background, the progress shows in the status bar;
     * the plugins then start one after the other on the event thread (where
     * they may build their user interface).
     */
    public void startAllInBackground() {
        final int epoch = startEpoch.incrementAndGet();
        Thread t = new Thread(() -> {
            try {
                if (startEpoch.get() != epoch) {
                    return;
                }
                discover();
                List<Installed> todo = new ArrayList<Installed>();
                for (Installed i : installed) {
                    if (i.state == State.STOPPED) {
                        todo.add(i);
                    }
                }
                int n = 0;
                for (Installed i : todo) {
                    if (startEpoch.get() != epoch) {
                        return;
                    }
                    n++;
                    notifyHost(String.format(mpv5.globals.Messages.PLUGINS_LOADING.toString(), n, todo.size(), name(i)));
                    if (i.extension instanceof GroovyExtension) {
                        try {
                            ((GroovyExtension) i.extension).prepare();
                        } catch (Throwable e) {
                            //start compiles again and reports the error
                            Log.Debug(ExtensionManager.class, e);
                        }
                    }
                }
                final int total = todo.size();
                ui(() -> {
                    if (startEpoch.get() != epoch) {
                        return;
                    }
                    hookActivity();
                    int running = 0;
                    for (Installed i : todo) {
                        if (startEpoch.get() != epoch) {
                            return;
                        }
                        if (i.state == State.STOPPED && start(i)) {
                            running++;
                        } else if (i.state == State.RUNNING) {
                            running++;
                        }
                    }
                    fireChanged();
                    //views opened while the plugins were loading: let the plugins decorate them
                    if (host != null && !viewListeners.isEmpty()) {
                        try {
                            for (DataPanel v : host.openViews()) {
                                fireView(v);
                            }
                        } catch (Throwable e) {
                            Log.Debug(ExtensionManager.class, e);
                        }
                    }
                    if (total > 0) {
                        notifyHost(String.format(mpv5.globals.Messages.PLUGINS_STARTED.toString(), running, total));
                    }
                });
            } catch (Throwable e) {
                Log.Debug(ExtensionManager.class, e);
            }
        }, "yabs-plugins");
        t.setDaemon(true);
        t.start();
    }

    private void notifyHost(String message) {
        if (host != null) {
            try {
                host.notify(message);
            } catch (Throwable e) {
                Log.Debug(ExtensionManager.class, e);
            }
        }
    }

    /**
     * Discovers and starts all enabled plugins
     */
    public void startAll() {
        discover();
        hookActivity();
        for (Installed i : installed) {
            if (i.state == State.STOPPED) {
                start(i);
            }
        }
        fireChanged();
    }

    /**
     * Starts a plugin; a failure is recorded, not thrown
     *
     * @param i
     * @return true if it runs
     */
    public boolean start(Installed i) {
        if (i.state == State.RUNNING) {
            return true;
        }
        hookActivity();
        i.error = null;

        String minVersion = i.extension.minYabsVersion();
        if (minVersion != null && !minVersion.trim().isEmpty()) {
            String current = VersionInfo.number();
            if (!isVersionCompatible(current, minVersion)) {
                i.state = State.FAILED;
                i.error = "Requires YaBS " + minVersion.trim() + " or newer (current: " + current + ")";
                Log.Debug(ExtensionManager.class, "Plugin " + name(i) + " not started: " + i.error);
                if (host != null) {
                    host.error(name(i), new IllegalStateException(i.error));
                }
                fireChanged();
                return false;
            }
        }

        try {
            i.extension.start(new Context(i));
            i.state = State.RUNNING;
            return true;
        } catch (Throwable t) {
            //what it added before the failure goes too
            removeContributions(i.id());
            i.state = State.FAILED;
            i.error = t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
            Log.Debug(ExtensionManager.class, t);
            if (host != null) {
                host.error(name(i), t);
            }
            return false;
        } finally {
            fireChanged();
        }
    }

    /**
     * @param version a version string, e.g. "2.0-dev" or "2.0.4-dev.250"
     * @return true if this is a development/pre-release build rather than a formal release
     */
    public static boolean isDevBuild(String version) {
        if (version == null) {
            return true;
        }
        String v = version.toLowerCase(Locale.ROOT).trim();
        return v.contains("-dev") || v.contains("snapshot") || v.equals("dev");
    }

    /**
     * Checks if current YaBS version satisfies a plugin's minimum release version.
     * <p>
     * For formal releases, semantic version comparison is strictly enforced.
     * For development builds (e.g. {@code 2.0-dev} or CI builds like {@code 2.0.9-dev.123}),
     * plugins targeting the active development release line are permitted so developers
     * and CI tests can run and test new features.
     *
     * @param currentVersion e.g. "2.0.8" or "2.0-dev"
     * @param minRequiredVersion e.g. "2.0.9"
     * @return true if compatible
     */
    public static boolean isVersionCompatible(String currentVersion, String minRequiredVersion) {
        if (minRequiredVersion == null || minRequiredVersion.trim().isEmpty()) {
            return true;
        }
        String min = minRequiredVersion.trim();
        String current = currentVersion != null ? currentVersion.trim() : "";
        if (current.isEmpty()) {
            return true;
        }
        if (Boolean.getBoolean("yabs.plugins.ignoreVersion")) {
            return true;
        }
        if (isDevBuild(current)) {
            // In development builds:
            // 1) If CI pre-release of the target version (e.g. "2.0.9-dev.123" >= "2.0.9")
            String currentCore = splitCore(current);
            String minCore = splitCore(min);
            if (compareCore(currentCore, minCore) >= 0) {
                return true;
            }
            // 2) If working on the active development branch (e.g. "2.0-dev"), allow plugins
            // requiring that release line (e.g. "2.0.9") because dev builds contain the latest code.
            String[] curParts = currentCore.split("\\.");
            String[] minParts = minCore.split("\\.");
            if (curParts.length > 0 && minParts.length > 0) {
                try {
                    int curMajor = Integer.parseInt(curParts[0]);
                    int minMajor = Integer.parseInt(minParts[0]);
                    if (curMajor > minMajor) return true;
                    if (curMajor == minMajor) {
                        if (curParts.length == 1) return true;
                        int curMinor = Integer.parseInt(curParts[1]);
                        int minMinor = minParts.length > 1 ? Integer.parseInt(minParts[1]) : 0;
                        if (curMinor >= minMinor) {
                            return true;
                        }
                    }
                } catch (NumberFormatException ignored) {
                    return true;
                }
            }
            return false;
        }
        return UpdateCheck.compare(current, min) >= 0;
    }

    private static String splitCore(String v) {
        if (v == null) return "";
        String s = v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        int plus = s.indexOf('+');
        if (plus >= 0) {
            s = s.substring(0, plus);
        }
        int dash = s.indexOf('-');
        return dash >= 0 ? s.substring(0, dash) : s;
    }

    private static int compareCore(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            long p = i < x.length ? parseNumber(x[i]) : 0;
            long q = i < y.length ? parseNumber(y[i]) : 0;
            if (p != q) {
                return Long.compare(p, q);
            }
        }
        return 0;
    }

    private static long parseNumber(String s) {
        String digits = s.replaceAll("^(\\d*).*$", "$1");
        try {
            return digits.isEmpty() ? 0 : Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Stops a plugin and removes everything it added
     *
     * @param i
     */
    public void stop(Installed i) {
        if (i.state == State.RUNNING) {
            try {
                i.extension.stop();
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        removeContributions(i.id());
        if (i.state != State.DISABLED) {
            i.state = State.STOPPED;
            i.error = null;
        }
        fireChanged();
    }

    /**
     * Stops all running plugins and removes all of their contributions (e.g.
     * before switching to or creating another database connection). Previously
     * failed plugins are reset to {@link State#STOPPED} so they can be retried
     * on the next {@link #startAll()} / {@link #startAllInBackground()};
     * disabled plugins remain {@link State#DISABLED}.
     */
    public void stopAll() {
        startEpoch.incrementAndGet();
        for (Installed i : installed) {
            if (i.state == State.DISABLED) {
                continue;
            }
            if (i.state == State.RUNNING) {
                try {
                    i.extension.stop();
                } catch (Throwable t) {
                    Log.Debug(ExtensionManager.class, t);
                }
            }
            removeContributions(i.id());
            i.state = State.STOPPED;
            i.error = null;
        }
        fireChanged();
    }

    /**
     * Enables (and starts) or disables (and stops) a plugin; remembered
     *
     * @param i
     * @param enabled
     */
    public void setEnabled(Installed i, boolean enabled) {
        Set<String> d = disabled();
        if (enabled) {
            d.remove(i.id());
            saveDisabled(d);
            if (i.state == State.DISABLED || i.state == State.FAILED) {
                i.state = State.STOPPED;
            }
            start(i);
        } else {
            stop(i);
            i.state = State.DISABLED;
            d.add(i.id());
            saveDisabled(d);
            fireChanged();
        }
    }

    /**
     * Stops and starts a plugin again; a jar is read again from the disk
     *
     * @param i
     * @return the plugin after the reload (a new object for a jar), may be
     * null if the jar is gone
     */
    public synchronized Installed reload(Installed i) {
        boolean wasDisabled = i.state == State.DISABLED;
        stop(i);
        if (i.jar == null) {
            if (!wasDisabled) {
                i.state = State.STOPPED;
                start(i);
            }
            return i;
        }
        File jar = i.jar;
        //all plugins of the jar share the class loader: reload them together
        List<Installed> same = new ArrayList<Installed>();
        for (Installed x : installed) {
            if (jar.equals(x.jar)) {
                same.add(x);
            }
        }
        for (Installed x : same) {
            stop(x);
            installed.remove(x);
        }
        close(i.loader);
        Installed again = null;
        for (Installed x : loadFile(jar)) {
            if (x.state == State.STOPPED) {
                start(x);
            }
            if (x.id().equals(i.id())) {
                again = x;
            }
        }
        fireChanged();
        return again;
    }

    /**
     * Copies a jar into the plugins folder and starts its plugins
     *
     * @param jar
     * @return the plugins of the jar
     * @throws IOException if it can not be copied or has no plugin
     */
    public synchronized List<Installed> install(File jar) throws IOException {
        return installFile(jar);
    }

    /**
     * @param f
     * @return true for a Groovy plugin or a jar with a YaBS plugin
     * (META-INF/services), i.e. a file for {@link #install(File)}, not for the
     * classic plugins of the database (class plugin.Main)
     */
    public static boolean isExtensionFile(File f) {
        if (f == null || !f.isFile()) {
            return false;
        }
        String n = f.getName().toLowerCase(java.util.Locale.ROOT);
        if (n.endsWith(".groovy")) {
            return true;
        }
        if (!n.endsWith(".jar")) {
            return false;
        }
        try (java.util.jar.JarFile j = new java.util.jar.JarFile(f)) {
            return j.getEntry("META-INF/services/" + YabsExtension.class.getName()) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private List<Installed> installFile(File jar) throws IOException {
        //a jar of the same name is replaced
        File target = new File(folder, jar.getName());
        for (Installed x : new ArrayList<Installed>(installed)) {
            if (target.equals(x.jar)) {
                stop(x);
                installed.remove(x);
                close(x.loader);
            }
        }
        folder.mkdirs();
        if (!jar.getCanonicalFile().equals(target.getCanonicalFile())) {
            Files.copy(jar.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        List<Installed> found;
        replacing = true;
        try {
            found = loadFile(target);
        } finally {
            replacing = false;
        }
        if (found.isEmpty()) {
            Files.deleteIfExists(target.toPath());
            throw new IOException("No YaBS plugin in " + jar.getName()
                    + " (META-INF/services/" + YabsExtension.class.getName() + " missing?)");
        }
        hookActivity();
        for (Installed x : found) {
            if (x.state == State.STOPPED) {
                start(x);
            }
        }
        fireChanged();
        return found;
    }

    /**
     * Stops a plugin of a jar and deletes the jar (with the other plugins of
     * the jar)
     *
     * @param i
     * @return true if the jar is gone
     */
    public synchronized boolean uninstall(Installed i) {
        if (i.jar == null) {
            return false;
        }
        for (Installed x : new ArrayList<Installed>(installed)) {
            if (i.jar.equals(x.jar)) {
                stop(x);
                installed.remove(x);
            }
        }
        close(i.loader);
        boolean gone = i.jar.delete() || !i.jar.exists();
        if (!gone) {
            i.jar.deleteOnExit();
        }
        fireChanged();
        return gone;
    }

    private static void close(URLClassLoader l) {
        if (l != null) {
            try {
                l.close();
            } catch (IOException e) {
                Log.Debug(ExtensionManager.class, e);
            }
        }
    }

    // ------------------------------------------------------------------ contributions
    /**
     * @return the commands of the running plugins
     */
    public List<Command> commands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * @return the settings pages of the running plugins
     */
    public List<Page> pages() {
        return Collections.unmodifiableList(pages);
    }

    /**
     * @return the journal exports registered by running plugins
     */
    public List<JournalExport> journalExports() {
        List<JournalExport> l = new ArrayList<JournalExport>();
        for (Owned<JournalExport> e : journalExports) {
            l.add(e.value);
        }
        return Collections.unmodifiableList(l);
    }

    /**
     * Collects additional template placeholders from all running plugins; a
     * failing provider is skipped.
     *
     * @param obj the record being formatted
     * @return map of placeholder key to value (never null)
     */
    public java.util.Map<String, Object> templateFields(mpv5.db.common.DatabaseObject obj) {
        java.util.Map<String, Object> result = new java.util.LinkedHashMap<String, Object>();
        if (obj == null || templateFieldProviders.isEmpty()) {
            return result;
        }
        for (Owned<Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>>> p : templateFieldProviders) {
            try {
                java.util.Map<String, Object> m = p.value.apply(obj);
                if (m != null) {
                    result.putAll(m);
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return result;
    }

    /**
     * Collects pre-send e-mail warnings from all running plugins; a failing
     * provider is skipped.
     *
     * @param obj the record being e-mailed
     * @return list of non-empty warning messages (never null)
     */
    public List<String> collectMailWarnings(mpv5.db.common.DatabaseObject obj) {
        List<String> result = new ArrayList<String>();
        if (obj == null || mailWarningProviders.isEmpty()) {
            return result;
        }
        for (Owned<Function<mpv5.db.common.DatabaseObject, String>> p : mailWarningProviders) {
            try {
                String msg = p.value.apply(obj);
                if (msg != null && !msg.trim().isEmpty()) {
                    result.add(msg.trim());
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return result;
    }

    /**
     * Returns {@code true} if any running plugin instructs YaBS to skip
     * e-invoice generation/delivery for the given item.
     *
     * @param item the invoice being exported or e-mailed
     * @return {@code true} to skip e-invoice and use plain PDF
     */
    public boolean shouldSkipEInvoice(mpv5.db.objects.Item item) {
        if (item == null || eInvoiceSkipRules.isEmpty()) {
            return false;
        }
        for (Owned<Predicate<mpv5.db.objects.Item>> r : eInvoiceSkipRules) {
            try {
                if (r.value.test(item)) {
                    return true;
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return false;
    }

    /**
     * The archived original of a locked record and the plugin that locked it
     */
    public static final class LockedDocument {

        /**
         * the archived file (may be missing on disk)
         */
        public final File file;
        /**
         * the provider that knows the file
         */
        public final mpv5.plugin.api.LockedDocumentProvider provider;

        LockedDocument(File file, mpv5.plugin.api.LockedDocumentProvider provider) {
            this.file = file;
            this.provider = provider;
        }
    }

    /**
     * @return true if a running plugin locks (finalizes) documents, e.g.
     * GoBD
     */
    public boolean locksDocuments() {
        return !lockedDocumentProviders.isEmpty();
    }

    /**
     * Asks the running plugins whether the record is locked with an archived
     * original
     *
     * @param record
     * @return the archived original, null if no plugin locked the record
     */
    public LockedDocument lockedDocument(mpv5.db.common.DatabaseObject record) {
        if (record == null || !record.isExisting() || lockedDocumentProviders.isEmpty()) {
            return null;
        }
        for (Owned<mpv5.plugin.api.LockedDocumentProvider> p : lockedDocumentProviders) {
            try {
                File f = p.value.lockedDocument(record);
                if (f != null) {
                    return new LockedDocument(f, p.value);
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return null;
    }

    /**
     * @return all registered GDPR privacy contributors from running plugins
     */
    public List<PrivacyContributor> privacyContributors() {
        List<PrivacyContributor> l = new ArrayList<PrivacyContributor>();
        for (Owned<PrivacyContributor> c : privacyContributors) {
            l.add(c.value);
        }
        return Collections.unmodifiableList(l);
    }

    /**
     * Resolves a plugin-scoped translation key using the active OpenYaBS user
     * language.
     *
     * @param owner plugin id
     * @param key short translation key
     * @param fallback default pattern if not found
     * @param args optional {@link java.text.MessageFormat} arguments
     * @return localized and formatted string
     */
    public String translate(String owner, String key, String fallback, Object... args) {
        return translateForLocale(owner, resolveActiveLanguage(), key, fallback, args);
    }

    /**
     * Resolves a plugin-scoped translation key for an explicit language tag
     * (fallback chain: exact locale -> base language -> {@code en} -> fallback
     * -> key).
     *
     * @param owner plugin id
     * @param languageTag e.g. {@code "de_DE"}, {@code "de"}, {@code "en"}
     * @param key short translation key
     * @param fallback default pattern if not found (may be null)
     * @param args optional {@link java.text.MessageFormat} arguments
     * @return localized and formatted string
     */
    public String translateForLocale(String owner, String languageTag, String key, String fallback, Object... args) {
        String pattern = null;
        if (owner != null && key != null) {
            java.util.Map<String, java.util.Map<String, String>> byLang = translations.get(owner);
            if (byLang != null && !byLang.isEmpty()) {
                String norm = normalizeLanguageTag(languageTag);
                if (!norm.isEmpty()) {
                    pattern = lookupTranslation(byLang, norm, key);
                    int sep = norm.indexOf('_');
                    if (pattern == null && sep > 0) {
                        pattern = lookupTranslation(byLang, norm.substring(0, sep), key);
                    }
                }
                if (pattern == null) {
                    pattern = lookupTranslation(byLang, "en", key);
                }
            }
        }
        if (pattern == null) {
            pattern = fallback != null ? fallback : (key == null ? "" : key);
        }
        if (args != null && args.length > 0) {
            try {
                return java.text.MessageFormat.format(pattern, args);
            } catch (IllegalArgumentException ignored) {
                return pattern;
            }
        }
        return pattern;
    }

    private static String lookupTranslation(java.util.Map<String, java.util.Map<String, String>> byLang, String lang, String key) {
        java.util.Map<String, String> entries = byLang.get(lang);
        return entries == null ? null : entries.get(key);
    }

    /**
     * Normalizes a language or locale tag to {@code ll} or {@code ll_CC}
     * (lowercase language, uppercase country).
     */
    public static String normalizeLanguageTag(String tag) {
        if (tag == null) {
            return "";
        }
        String t = tag.trim();
        if (t.isEmpty()) {
            return "";
        }
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.equals("buildin_en") || lower.startsWith("buildin")) {
            return "en";
        }
        if (lower.startsWith("de-") || lower.startsWith("de_") || lower.equals("de") || lower.startsWith("deutsch")) {
            // Recognize OpenYaBS bundle names like "DE-Yabs-2.0" as base "de", while preserving "de_DE" / "de-AT"
            String cleaned = t.replace('-', '_');
            String[] parts = cleaned.split("_");
            if (parts.length >= 2 && parts[0].length() == 2 && parts[1].length() == 2 && parts[1].chars().allMatch(Character::isLetter)) {
                return parts[0].toLowerCase(Locale.ROOT) + "_" + parts[1].toUpperCase(Locale.ROOT);
            }
            return "de";
        }
        String cleaned = t.replace('-', '_');
        String[] parts = cleaned.split("_");
        if (parts.length >= 2 && parts[0].length() >= 2 && parts[1].length() == 2 && parts[1].chars().allMatch(Character::isLetter)) {
            return parts[0].toLowerCase(Locale.ROOT) + "_" + parts[1].toUpperCase(Locale.ROOT);
        }
        return parts[0].toLowerCase(Locale.ROOT);
    }

    /**
     * Resolves the currently active OpenYaBS UI language tag from the logged-in
     * user's language and locale settings, falling back to the JVM default
     * locale and finally {@code "en"}.
     */
    public static String resolveActiveLanguage() {
        try {
            mpv5.db.objects.User u = mpv5.db.objects.User.getCurrentUser();
            if (u != null) {
                String lang = u.__getLanguage();
                String locale = u.__getLocale();
                if (lang != null && !lang.trim().isEmpty()) {
                    String normLang = normalizeLanguageTag(lang);
                    if (locale != null && !locale.trim().isEmpty()) {
                        String normLocale = normalizeLanguageTag(locale);
                        if (normLocale.startsWith(normLang + "_")) {
                            return normLocale;
                        }
                    }
                    if (!normLang.isEmpty()) {
                        return normLang;
                    }
                }
                if (locale != null && !locale.trim().isEmpty()) {
                    String normLocale = normalizeLanguageTag(locale);
                    if (!normLocale.isEmpty()) {
                        return normLocale;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        Locale def = Locale.getDefault();
        if (def != null && !def.getLanguage().isEmpty()) {
            String tag = def.getCountry().isEmpty() ? def.getLanguage() : def.getLanguage() + "_" + def.getCountry();
            return normalizeLanguageTag(tag);
        }
        return "en";
    }

    /**
     * Asks the search providers of the plugins; a failing provider is skipped
     *
     * @param needle
     * @return the results with the name of the plugin
     */
    public List<Object[]> search(String needle) {
        List<Object[]> l = new ArrayList<Object[]>();
        for (Owned<Function<String, List<SearchResult>>> p : providers) {
            try {
                List<SearchResult> r = p.value.apply(needle);
                if (r != null) {
                    for (SearchResult s : r) {
                        l.add(new Object[]{name(find(p.owner)), s});
                    }
                }
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        return l;
    }

    /**
     * Tells the listeners of the plugins about an activity
     *
     * @param e
     */
    public void fire(Activity.Entry e) {
        for (Owned<Consumer<Activity.Entry>> l : listeners) {
            try {
                l.value.accept(e);
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
    }

    /**
     * Tells the view listeners of the plugins that a record view was opened
     * or updated
     *
     * @param view
     */
    public void fireView(DataPanel view) {
        if (view == null || viewListeners.isEmpty()) {
            return;
        }
        ui(() -> {
            for (Owned<Consumer<DataPanel>> l : viewListeners) {
                try {
                    l.value.accept(view);
                } catch (Throwable t) {
                    Log.Debug(ExtensionManager.class, t);
                }
            }
        });
    }

    private synchronized void hookActivity() {
        if (activityHooked) {
            return;
        }
        activityHooked = true;
        Activity.addListener(e -> {
            if (e != null && !listeners.isEmpty()) {
                ui(() -> fire(e));
            }
        });
    }

    /**
     * What the plugins want to run when they stop (ExtensionContext.onStop)
     */
    private final java.util.Map<String, List<Runnable>> stopActions = new java.util.concurrent.ConcurrentHashMap<String, List<Runnable>>();

    private void runStopActions(String owner) {
        List<Runnable> l = stopActions.remove(owner);
        if (l != null) {
            for (Runnable r : l) {
                try {
                    r.run();
                } catch (Throwable t) {
                    Log.Debug(ExtensionManager.class, t);
                }
            }
        }
    }

    private void removeContributions(String owner) {
        runStopActions(owner);
        commands.removeIf(c -> c.owner.equals(owner));
        pages.removeIf(p -> p.owner.equals(owner));
        providers.removeIf(p -> p.owner.equals(owner));
        listeners.removeIf(p -> p.owner.equals(owner));
        journalExports.removeIf(p -> p.owner.equals(owner));
        templateFieldProviders.removeIf(p -> p.owner.equals(owner));
        mailWarningProviders.removeIf(p -> p.owner.equals(owner));
        eInvoiceSkipRules.removeIf(p -> p.owner.equals(owner));
        privacyContributors.removeIf(p -> p.owner.equals(owner));
        lockedDocumentProviders.removeIf(p -> p.owner.equals(owner));
        translations.remove(owner);
        boolean hadViews = viewListeners.removeIf(p -> p.owner.equals(owner));
        for (Owned<DatabaseObjectModifier> m : modifiers) {
            if (m.owner.equals(owner)) {
                mpv5.pluginhandling.YabsPluginLoader.removeModifier(m.value);
                modifiers.remove(m);
            }
        }
        if (hadViews && host != null) {
            host.ui(() -> {
                for (DataPanel v : host.openViews()) {
                    v.setPluginBanner(null);
                    v.setPluginBadge(null);
                    v.setContentLocked(false);
                    fireView(v);
                }
            });
        }
    }

    /**
     * @param owner
     * @return how many things the plugin added (commands, pages, providers,
     * listeners, modifiers, view listeners, exports, templates, translations,
     * mail warnings, e-invoice skip rules, privacy contributors)
     */
    public int contributions(String owner) {
        int n = 0;
        for (Command c : commands) {
            n += c.owner.equals(owner) ? 1 : 0;
        }
        for (Page p : pages) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : providers) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : listeners) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : modifiers) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : viewListeners) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : journalExports) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : templateFieldProviders) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : mailWarningProviders) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : eInvoiceSkipRules) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : privacyContributors) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        for (Owned<?> p : lockedDocumentProviders) {
            n += p.owner.equals(owner) ? 1 : 0;
        }
        java.util.Map<String, java.util.Map<String, String>> trMap = translations.get(owner);
        if (trMap != null) {
            n += trMap.size();
        }
        return n;
    }

    private void fireChanged() {
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Throwable t) {
                Log.Debug(ExtensionManager.class, t);
            }
        }
        if (host != null) {
            host.changed();
        }
    }

    private void ui(Runnable r) {
        if (host != null) {
            host.ui(r);
        } else {
            r.run();
        }
    }

    static String name(Installed i) {
        if (i == null) {
            return "";
        }
        try {
            return i.extension.name();
        } catch (Throwable t) {
            return i.id();
        }
    }

    // ------------------------------------------------------------------ persistence
    private File stateFile() {
        return new File(folder, "plugins.properties");
    }

    private Set<String> disabled() {
        Properties p = load(stateFile());
        Set<String> s = new LinkedHashSet<String>();
        for (String x : p.getProperty("disabled", "").split(",")) {
            if (!x.trim().isEmpty()) {
                s.add(x.trim());
            }
        }
        return s;
    }

    private void saveDisabled(Set<String> d) {
        Properties p = load(stateFile());
        p.setProperty("disabled", String.join(",", d));
        store(p, stateFile());
    }

    static Properties load(File f) {
        Properties p = new Properties();
        if (f.isFile()) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException e) {
                Log.Debug(ExtensionManager.class, e);
            }
        }
        return p;
    }

    static void store(Properties p, File f) {
        f.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(f)) {
            p.store(out, "YaBS plugins");
        } catch (IOException e) {
            Log.Debug(ExtensionManager.class, e);
        }
    }

    // ------------------------------------------------------------------ the context
    /**
     * What a plugin gets: everything it adds is noted with its id
     */
    private final class Context implements ExtensionContext {

        private final Installed plugin;
        private final String owner;

        Context(Installed plugin) {
            this.plugin = plugin;
            this.owner = plugin.id();
        }

        @Override
        public void addCommand(String title, String description, Runnable action) {
            commands.add(new Command(owner, name(plugin), title, description, guarded(action)));
            fireChanged();
        }

        @Override
        public void addSettingsPage(String title, String description, Supplier<JComponent> page) {
            pages.add(new Page(owner, title, description, page));
            fireChanged();
        }

        @Override
        public void addSearchProvider(Function<String, List<SearchResult>> provider) {
            providers.add(new Owned<Function<String, List<SearchResult>>>(owner, provider));
        }

        @Override
        public void onActivity(Consumer<Activity.Entry> listener) {
            listeners.add(new Owned<Consumer<Activity.Entry>>(owner, listener));
        }

        @Override
        public void addRecordModifier(DatabaseObjectModifier modifier) {
            modifiers.add(new Owned<DatabaseObjectModifier>(owner, modifier));
            mpv5.pluginhandling.YabsPluginLoader.registeredModifiers.add(modifier);
        }

        @Override
        public void onView(Consumer<DataPanel> listener) {
            viewListeners.add(new Owned<Consumer<DataPanel>>(owner, listener));
            if (host != null) {
                host.ui(() -> {
                    for (DataPanel v : host.openViews()) {
                        try {
                            listener.accept(v);
                        } catch (Throwable t) {
                            failed(t);
                        }
                    }
                });
            }
        }

        @Override
        public void addJournalExport(JournalExport export) {
            if (export != null) {
                journalExports.add(new Owned<JournalExport>(owner, export));
                fireChanged();
            }
        }

        @Override
        public void addTemplateFields(Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>> provider) {
            if (provider != null) {
                templateFieldProviders.add(new Owned<Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>>>(owner, provider));
            }
        }

        @Override
        public void addMailWarningProvider(Function<mpv5.db.common.DatabaseObject, String> provider) {
            if (provider != null) {
                mailWarningProviders.add(new Owned<Function<mpv5.db.common.DatabaseObject, String>>(owner, provider));
            }
        }

        @Override
        public void addEInvoiceSkipRule(Predicate<mpv5.db.objects.Item> rule) {
            if (rule != null) {
                eInvoiceSkipRules.add(new Owned<Predicate<mpv5.db.objects.Item>>(owner, rule));
            }
        }

        @Override
        public void addPrivacyContributor(PrivacyContributor contributor) {
            if (contributor != null) {
                privacyContributors.add(new Owned<PrivacyContributor>(owner, contributor));
            }
        }

        @Override
        public void addLockedDocumentProvider(mpv5.plugin.api.LockedDocumentProvider provider) {
            if (provider != null) {
                lockedDocumentProviders.add(new Owned<mpv5.plugin.api.LockedDocumentProvider>(owner, provider));
            }
        }

        @Override
        public void refreshView(mpv5.db.common.DatabaseObject record) {
            if (host == null) {
                ExtensionContext.super.refreshView(record);
                return;
            }
            if (record == null || record.__getIDS() <= 0) {
                return;
            }
            //every open view of the record, not only the selected tab (the record
            //may be locked while another tab or a progress window is in front)
            host.ui(() -> {
                for (DataPanel v : host.openViews()) {
                    mpv5.db.common.DatabaseObject o = v.getDataOwner();
                    if (o != null && o.getClass() == record.getClass() && o.__getIDS() == record.__getIDS()) {
                        v.setDataOwner(record, true);
                    }
                }
            });
        }

        @Override
        public void addTranslations(String languageCode, java.util.Map<String, String> messages) {
            String norm = normalizeLanguageTag(languageCode);
            if (norm.isEmpty() || messages == null || messages.isEmpty()) {
                return;
            }
            java.util.Map<String, java.util.Map<String, String>> byLang = translations.computeIfAbsent(
                    owner, k -> new java.util.concurrent.ConcurrentHashMap<String, java.util.Map<String, String>>());
            java.util.Map<String, String> bucket = byLang.computeIfAbsent(
                    norm, k -> new java.util.concurrent.ConcurrentHashMap<String, String>());
            for (java.util.Map.Entry<String, String> e : messages.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    bucket.put(e.getKey(), e.getValue());
                }
            }
        }

        @Override
        public String tr(String key, Object... args) {
            return translate(owner, key, key, args);
        }

        @Override
        public String trOrDefault(String key, String fallback, Object... args) {
            return translate(owner, key, fallback, args);
        }

        @Override
        public void openTab(String title, JComponent content) {
            if (host != null) {
                host.ui(() -> host.openTab(title, content));
            }
        }

        @Override
        public void notify(String message) {
            if (host != null) {
                host.notify(message);
            }
        }

        @Override
        public PluginSettings settings() {
            final File f = new File(folder, safeName(owner) + ".properties");
            return new PluginSettings() {
                @Override
                public String get(String key, String fallback) {
                    return load(f).getProperty(key, fallback);
                }

                @Override
                public void set(String key, String value) {
                    Properties p = load(f);
                    if (value == null) {
                        p.remove(key);
                    } else {
                        p.setProperty(key, value);
                    }
                    store(p, f);
                }
            };
        }

        @Override
        public void onStop(Runnable action) {
            if (action != null) {
                stopActions.computeIfAbsent(owner, k -> new CopyOnWriteArrayList<Runnable>()).add(action);
            }
        }

        @Override
        public File dataFolder() {
            File d = new File(folder, safeName(owner));
            d.mkdirs();
            return d;
        }

        @Override
        public <T> void background(Supplier<T> work, Consumer<T> then) {
            Thread t = new Thread(() -> {
                try {
                    final T r = work.get();
                    if (then != null) {
                        ui(() -> {
                            try {
                                then.accept(r);
                            } catch (Throwable x) {
                                failed(x);
                            }
                        });
                    }
                } catch (Throwable x) {
                    failed(x);
                }
            }, "plugin-" + owner);
            t.setDaemon(true);
            t.start();
        }

        private void failed(Throwable t) {
            Log.Debug(ExtensionManager.class, t);
            if (host != null) {
                host.error(name(plugin), t);
            }
        }

        private Runnable guarded(Runnable r) {
            return () -> {
                try {
                    r.run();
                } catch (Throwable t) {
                    failed(t);
                }
            };
        }
    }

    static String safeName(String id) {
        return id.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
