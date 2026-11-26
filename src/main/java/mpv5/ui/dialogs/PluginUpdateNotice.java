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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.PluginUpdateCheck;
import mpv5.handler.PluginUpdateCheck.Update;
import mpv5.handler.UpdateCheck;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;

/**
 * Tells the user about newer versions of the installed plugins: at start (if
 * the update check is switched on; an update the user skipped is not shown
 * again) or when asked in Settings &gt; Plugins, and installs them on request
 */
public final class PluginUpdateNotice {

    /**
     * Local setting: the updates the user chose to skip ("id@version,...")
     */
    public static final String SKIPPED = "pluginupdatesskipped";
    private static final List<Runnable> listeners = new java.util.concurrent.CopyOnWriteArrayList<Runnable>();

    private PluginUpdateNotice() {
    }

    /**
     * @param r called (in any thread) when a check or an installation is done
     */
    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
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
     * check): asks GitHub and shows a notice if there are newer plugins
     */
    public static void checkAtStart() {
        if (!UpdateCheck.enabledAtStart()) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                return;
            }
            List<Update> found = PluginUpdateCheck.check(ExtensionManager.instance().list());
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
        Thread t = new Thread(() -> {
            List<Update> found = PluginUpdateCheck.check(ExtensionManager.instance().list());
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
        Thread t = new Thread(() -> {
            List<String> done = new ArrayList<String>();
            Set<String> updated = new java.util.HashSet<String>();
            List<String> failed = new ArrayList<String>();
            for (Update u : updates) {
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
