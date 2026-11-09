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
import java.util.function.Supplier;
import javax.swing.JComponent;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.handler.Activity;
import mpv5.handler.SearchService;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.PluginSettings;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;

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
        State state = State.STOPPED;
        String error;

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
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<Runnable>();
    private final File folder;
    private Host host;
    private boolean activityHooked;

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

    private Installed add(YabsExtension e, File jar, URLClassLoader loader) {
        String id;
        try {
            id = e.id();
        } catch (Throwable t) {
            Log.Debug(ExtensionManager.class, t);
            return null;
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
        List<Installed> found = loadFile(target);
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

    private void removeContributions(String owner) {
        commands.removeIf(c -> c.owner.equals(owner));
        pages.removeIf(p -> p.owner.equals(owner));
        providers.removeIf(p -> p.owner.equals(owner));
        listeners.removeIf(p -> p.owner.equals(owner));
        for (Owned<DatabaseObjectModifier> m : modifiers) {
            if (m.owner.equals(owner)) {
                mpv5.pluginhandling.YabsPluginLoader.removeModifier(m.value);
                modifiers.remove(m);
            }
        }
    }

    /**
     * @param owner
     * @return how many things the plugin added (commands, pages, providers,
     * listeners, modifiers)
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
