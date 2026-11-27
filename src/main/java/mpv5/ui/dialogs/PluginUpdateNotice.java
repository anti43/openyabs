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
package mpv5.ui.dialogs;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.PluginUpdateCheck;
import mpv5.handler.PluginUpdateCheck.Available;
import mpv5.handler.PluginUpdateCheck.Catalog;
import mpv5.handler.PluginUpdateCheck.Update;
import mpv5.handler.UpdateCheck;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.builtin.UpdateManager;

/**
 * Tells the user about newer versions of the installed plugins: at start (if
 * the update check is switched on and the built-in {@link UpdateManager} plugin
 * is enabled; an update the user skipped is not shown again) or when asked in
 * Settings &gt; Plugins, fetches the available plugins catalog
 * ({@value PluginUpdateCheck#INDEX_ASSET}) and installs plugins or updates on
 * request
 */
public final class PluginUpdateNotice {

    /**
     * Local setting: the updates the user chose to skip ("id@version,...")
     */
    public static final String SKIPPED = "pluginupdatesskipped";
    private static final List<Runnable> listeners = new java.util.concurrent.CopyOnWriteArrayList<Runnable>();
    private static final AtomicBoolean catalogLoading = new AtomicBoolean(false);
    private static volatile boolean catalogFailed;
    private static final Set<String> installing = ConcurrentHashMap.newKeySet();

    private PluginUpdateNotice() {
    }

    /**
     * @param r called (in any thread) when a check, catalog load or
     * installation changes state
     */
    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
    }

    /**
     * Notifies listeners that plugin update/catalog state changed (e.g. when
     * the built-in {@link UpdateManager} plugin starts).
     */
    public static void notifyChanged() {
        fire();
    }

    /**
     * Resets catalog status flags and notifies listeners (e.g. when the
     * built-in {@link UpdateManager} plugin stops).
     */
    public static void resetState() {
        catalogFailed = false;
        fire();
    }

    /**
     * @return true while {@value PluginUpdateCheck#INDEX_ASSET} is being
     * fetched in the background
     */
    public static boolean isCatalogLoading() {
        return catalogLoading.get();
    }

    /**
     * @return true if the last attempt to fetch the plugin catalog failed
     */
    public static boolean isCatalogFailed() {
        return catalogFailed;
    }

    /**
     * @param id plugin id
     * @return true while the plugin is currently being downloaded/installed
     */
    public static boolean isInstalling(String id) {
        return id != null && installing.contains(id);
    }

    private static void fire() {
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (Exception e) {
                Log.Debug(PluginUpdateNotice.class, e);
            }
        }
    }

    /**
     * In the background some seconds after the start (after the YaBS update
     * check): asks GitHub for updates and caches the plugin catalog
     */
    public static void checkAtStart() {
        if (!UpdateManager.isActive() || !UpdateCheck.enabledAtStart()) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                return;
            }
            if (!UpdateManager.isActive()) {
                return;
            }
            List<Update> found = PluginUpdateCheck.check(ExtensionManager.instance().list());
            if (!UpdateManager.isActive()) {
                return;
            }
            if (PluginUpdateCheck.lastCatalog() == null && catalogLoading.compareAndSet(false, true)) {
                try {
                    catalogFailed = PluginUpdateCheck.fetchCatalog() == null;
                } finally {
                    catalogLoading.set(false);
                }
            }
            if (!UpdateManager.isActive()) {
                return;
            }
            fire();
            if (found == null) {
                return;
            }
            Set<String> skipped = skipped();
            List<Update> show = new ArrayList<Update>();
            for (Update u : found) {
                if (!skipped.contains(u.key())) {
                    show.add(u);
                }
            }
            if (!show.isEmpty()) {
                SwingUtilities.invokeLater(() -> show(null, show, false));
            }
        }, "plugin update check");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Asks GitHub now and says what it found
     *
     * @param parent
     */
    public static void checkNow(final Component parent) {
        if (!UpdateManager.isActive()) {
            Popup.notice(parent, Messages.UPDATES_PLUGIN_DISABLED.toString());
            return;
        }
        Thread t = new Thread(() -> {
            Catalog before = PluginUpdateCheck.lastCatalog();
            List<Update> found = PluginUpdateCheck.check(ExtensionManager.instance().list());
            if (found != null && UpdateManager.isActive()) {
                if (PluginUpdateCheck.lastCatalog() == before && catalogLoading.compareAndSet(false, true)) {
                    try {
                        catalogFailed = PluginUpdateCheck.fetchCatalog() == null;
                    } finally {
                        catalogLoading.set(false);
                    }
                } else if (PluginUpdateCheck.lastCatalog() != null) {
                    catalogFailed = false;
                }
            }
            fire();
            SwingUtilities.invokeLater(() -> {
                if (found == null) {
                    Popup.notice(parent, Messages.UPDATE_CHECK_FAILED.toString());
                } else if (found.isEmpty()) {
                    Popup.notice(parent, Messages.PLUGIN_UPDATE_NONE.toString());
                } else {
                    show(parent, found, true);
                }
            });
        }, "plugin update check");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Fetches the latest {@value PluginUpdateCheck#INDEX_ASSET} from GitHub in
     * the background and notifies listeners when done.
     *
     * @param parent
     * @param notifyOnFailure show an error popup if GitHub cannot be reached
     */
    public static void loadCatalog(final Component parent, final boolean notifyOnFailure) {
        if (!UpdateManager.isActive()) {
            if (notifyOnFailure) {
                Popup.notice(parent, Messages.UPDATES_PLUGIN_DISABLED.toString());
            }
            return;
        }
        if (!catalogLoading.compareAndSet(false, true)) {
            return;
        }
        catalogFailed = false;
        fire();
        Thread t = new Thread(() -> {
            Catalog loaded = null;
            try {
                loaded = PluginUpdateCheck.fetchCatalog();
                if (UpdateManager.isActive()) {
                    catalogFailed = (loaded == null);
                } else {
                    PluginUpdateCheck.setLastCatalog(null);
                    loaded = null;
                }
            } finally {
                catalogLoading.set(false);
                fire();
            }
            if (loaded == null && notifyOnFailure && UpdateManager.isActive()) {
                SwingUtilities.invokeLater(() -> Popup.notice(parent, Messages.UPDATE_CHECK_FAILED.toString()));
            }
        }, "plugin catalog");
        t.setDaemon(true);
        t.start();
    }

    private static void show(Component parent, List<Update> updates, boolean asked) {
        StringBuilder s = new StringBuilder(Messages.PLUGIN_UPDATE_FOUND.toString()).append("\n");
        List<Update> installable = new ArrayList<Update>();
        for (Update u : updates) {
            s.append("\n").append(String.format(Messages.PLUGIN_UPDATE_LINE.toString(), u.name, u.version, u.installedVersion));
            if (u.installable()) {
                installable.add(u);
            } else if (u.download == null) {
                s.append("  ").append(Messages.PLUGIN_UPDATE_NO_DOWNLOAD);
            } else {
                s.append("  ").append(String.format(Messages.PLUGIN_UPDATE_NEEDS_YABS.toString(), u.minYabsVersion));
            }
        }
        s.append("\n\n").append(Messages.UPDATE_HOW_TO_OFF);
        String install = Messages.PLUGIN_UPDATE_INSTALL.toString();
        String open = Messages.UPDATE_OPEN.toString();
        String later = Messages.UPDATE_LATER.toString();
        String skip = Messages.UPDATE_SKIP.toString();
        List<Object> options = new ArrayList<Object>();
        if (!installable.isEmpty()) {
            options.add(install);
        }
        options.add(open);
        if (!asked) {
            options.add(skip);
        }
        options.add(later);
        int c = JOptionPane.showOptionDialog(parent, s.toString(), Messages.PLUGIN_UPDATE_TITLE.toString(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options.toArray(), options.get(0));
        Object chosen = c < 0 ? later : options.get(c);
        if (chosen == install) {
            install(parent, installable);
        } else if (chosen == open) {
            Set<String> pages = new LinkedHashSet<String>();
            for (Update u : updates) {
                if (u.page != null) {
                    pages.add(u.page);
                }
            }
            for (String p : pages) {
                browse(parent, p);
            }
        } else if (chosen == skip) {
            Set<String> k = skipped();
            for (Update u : updates) {
                k.add(u.key());
            }
            LocalSettings.setProperty(SKIPPED, String.join(",", k));
            LocalSettings.save();
        }
    }

    /**
     * Downloads the updates (in the background) and installs them (they
     * replace the installed jars)
     *
     * @param parent
     * @param updates
     */
    public static void install(final Component parent, final List<Update> updates) {
        if (!UpdateManager.isActive()) {
            Popup.notice(parent, Messages.UPDATES_PLUGIN_DISABLED.toString());
            return;
        }
        List<Update> todo = new ArrayList<Update>();
        for (Update u : updates) {
            if (installing.add(u.id)) {
                todo.add(u);
            }
        }
        if (todo.isEmpty()) {
            return;
        }
        fire();
        Thread t = new Thread(() -> {
            List<String> done = new ArrayList<String>();
            Set<String> updated = new java.util.HashSet<String>();
            List<String> failed = new ArrayList<String>();
            try {
                for (Update u : todo) {
                    File dir = null;
                    try {
                        dir = Files.createTempDirectory("yabs-plugin-update").toFile();
                        File jar = PluginUpdateCheck.download(u, dir);
                        final File f = jar;
                        final Exception[] err = new Exception[1];
                        //plugins start in the Swing thread
                        SwingUtilities.invokeAndWait(() -> {
                            try {
                                ExtensionManager.instance().install(f);
                            } catch (Exception e) {
                                err[0] = e;
                            }
                        });
                        if (err[0] != null) {
                            throw err[0];
                        }
                        Log.Print("Plugin update: " + u.id + " " + u.installedVersion + " -> " + u.version);
                        done.add(u.name + " " + u.version);
                        updated.add(u.id);
                    } catch (Exception e) {
                        Log.Debug(PluginUpdateNotice.class, e);
                        Log.Print("Plugin update: " + u.id + " failed (" + e.getMessage() + ")");
                        failed.add(u.name + ": " + e.getMessage());
                    } finally {
                        delete(dir);
                        installing.remove(u.id);
                    }
                }
            } finally {
                for (Update u : todo) {
                    installing.remove(u.id);
                }
            }
            //the installed ones are no longer shown as updates
            List<Update> left = new ArrayList<Update>(PluginUpdateCheck.lastResult());
            left.removeIf(x -> updated.contains(x.id));
            PluginUpdateCheck.setLastResult(left);
            fire();
            SwingUtilities.invokeLater(() -> {
                StringBuilder s = new StringBuilder();
                if (!done.isEmpty()) {
                    s.append(String.format(Messages.PLUGIN_UPDATE_DONE.toString(), String.join(", ", done)));
                }
                if (!failed.isEmpty()) {
                    if (s.length() > 0) {
                        s.append("\n\n");
                    }
                    s.append(Messages.PLUGIN_UPDATE_FAILED).append("\n").append(String.join("\n", failed));
                }
                Popup.notice(parent, s.toString());
            });
        }, "plugin update");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Downloads an available plugin from {@value PluginUpdateCheck#INDEX_ASSET}
     * to a temporary folder (in the background), verifies its checksum, installs
     * it into the plugins folder, and removes the temporary download.
     *
     * @param parent
     * @param plugin
     */
    public static void installAvailable(final Component parent, final Available plugin) {
        if (!UpdateManager.isActive()) {
            Popup.notice(parent, Messages.UPDATES_PLUGIN_DISABLED.toString());
            return;
        }
        if (plugin == null || !installing.add(plugin.id)) {
            return;
        }
        fire();
        Thread t = new Thread(() -> {
            File dir = null;
            Exception failure = null;
            try {
                dir = Files.createTempDirectory("yabs-plugin-install").toFile();
                File jar = PluginUpdateCheck.download(plugin, dir);
                final File f = jar;
                final Exception[] err = new Exception[1];
                //plugins start in the Swing thread
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        ExtensionManager.instance().install(f);
                    } catch (Exception e) {
                        err[0] = e;
                    }
                });
                if (err[0] != null) {
                    throw err[0];
                }
                List<Update> left = new ArrayList<Update>(PluginUpdateCheck.lastResult());
                if (left.removeIf(x -> plugin.id.equals(x.id))) {
                    PluginUpdateCheck.setLastResult(left);
                }
                Log.Print("Plugin install: " + plugin.id + " " + plugin.version);
            } catch (Exception e) {
                failure = e;
                Log.Debug(PluginUpdateNotice.class, e);
                Log.Print("Plugin install: " + plugin.id + " failed (" + e.getMessage() + ")");
            } finally {
                delete(dir);
                installing.remove(plugin.id);
                fire();
            }
            final Exception failed = failure;
            SwingUtilities.invokeLater(() -> {
                if (failed == null) {
                    mpv5.YabsViewProxy.instance().addMessage(
                            String.format(Messages.PLUGINS_INSTALL_DONE.toString(), plugin.name, plugin.version));
                } else {
                    Popup.notice(parent, Messages.PLUGINS_INSTALL_FAILED + ": " + plugin.name + "\n" + failed.getMessage());
                }
            });
        }, "plugin install");
        t.setDaemon(true);
        t.start();
    }

    private static Set<String> skipped() {
        String v = LocalSettings.getProperty(SKIPPED);
        Set<String> s = new LinkedHashSet<String>();
        if (v != null && !v.trim().isEmpty() && !"null".equals(v)) {
            s.addAll(Arrays.asList(v.trim().split("\\s*,\\s*")));
        }
        return s;
    }

    private static void browse(Component parent, String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            Log.Debug(PluginUpdateNotice.class, e);
            Popup.notice(parent, url);
        }
    }

    private static void delete(File dir) {
        if (dir == null) {
            return;
        }
        File[] fs = dir.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (!f.delete()) {
                    f.deleteOnExit();
                }
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }
}
