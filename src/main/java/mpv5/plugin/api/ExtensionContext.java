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
package mpv5.plugin.api;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.JComponent;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.handler.Activity;
import mpv5.ui.panels.DataPanel;

/**
 * What YaBS offers to a plugin. Everything registered here belongs to the
 * plugin and is removed when it stops.
 */
public interface ExtensionContext {

    /**
     * A command: shown in the menu "Plugins" and found by Search anything
     * (Shift Shift)
     *
     * @param title
     * @param description may be empty
     * @param action runs on the Swing thread
     */
    void addCommand(String title, String description, Runnable action);

    /**
     * A page in the settings (group "Plugins"), found by its search too
     *
     * @param title
     * @param description
     * @param page creates the page when it is opened
     */
    void addSettingsPage(String title, String description, Supplier<JComponent> page);

    /**
     * Adds results to Search anything
     *
     * @param provider gets the text the user typed, returns results (called
     * in the background, keep it quick)
     */
    void addSearchProvider(Function<String, List<SearchResult>> provider);

    /**
     * Told about everything that happens: created, changed and deleted
     * records, created files (PDF ...), messages
     *
     * @param listener called on the Swing thread
     */
    void onActivity(Consumer<Activity.Entry> listener);

    /**
     * Takes part when records are saved, deleted ... (the classic hook, e.g.
     * to check or change a record before it is saved)
     *
     * @param modifier
     */
    void addRecordModifier(DatabaseObjectModifier modifier);

    /**
     * Told when a record view ({@link DataPanel}) is opened or populated with
     * a record, so the plugin can mark, decorate or adjust the view
     *
     * @param listener called on the Swing thread with the view
     */
    default void onView(Consumer<DataPanel> listener) {
    }

    /**
     * Adds an export entry to the Journal's Export menu (and optionally extra
     * files to the tax-adviser ZIP package).
     *
     * @param export
     */
    default void addJournalExport(JournalExport export) {
    }

    /**
     * Supplies additional template placeholders (key -> value) when a
     * {@link mpv5.db.common.DatabaseObject} is formatted for ODT/PDF export.
     *
     * @param provider called with the record being exported; may return null or
     * an empty map
     */
    default void addTemplateFields(Function<mpv5.db.common.DatabaseObject, java.util.Map<String, Object>> provider) {
    }

    /**
     * Registers localized message keys for this plugin (automatically scoped by
     * plugin ID and removed when the plugin stops).
     *
     * @param languageCode ISO language/locale tag, e.g. {@code "en"},
     * {@code "de"}, {@code "de_DE"}
     * @param messages map of short key to localized pattern
     */
    default void addTranslations(String languageCode, java.util.Map<String, String> messages) {
    }

    /**
     * Registers localized message keys from a {@link java.util.Properties}
     * instance for this plugin.
     *
     * @param languageCode ISO language/locale tag, e.g. {@code "en"},
     * {@code "de"}
     * @param messages properties of short key to localized pattern
     */
    default void addTranslations(String languageCode, java.util.Properties messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        java.util.Map<String, String> map = new java.util.LinkedHashMap<String, String>();
        for (String k : messages.stringPropertyNames()) {
            map.put(k, messages.getProperty(k));
        }
        addTranslations(languageCode, map);
    }

    /**
     * Loads a UTF-8 {@code .properties} translation resource from the plugin
     * classpath and registers it for the given language code.
     *
     * @param languageCode ISO language/locale tag, e.g. {@code "en"},
     * {@code "de"}
     * @param resourcePath classpath path, e.g.
     * {@code "/org/openyabs/workshop/i18n/messages_de.properties"}
     * @param loader classloader used to locate the resource
     */
    default void addTranslationBundle(String languageCode, String resourcePath, ClassLoader loader) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            return;
        }
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader cl = loader != null ? loader : getClass().getClassLoader();
        try (java.io.InputStream in = cl.getResourceAsStream(normalized)) {
            if (in == null) {
                return;
            }
            java.util.Properties props = new java.util.Properties();
            props.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            addTranslations(languageCode, props);
        } catch (java.io.IOException e) {
            mpv5.logging.Log.Debug(ExtensionContext.class, e);
        }
    }

    /**
     * @return the active OpenYaBS UI language tag (e.g. {@code "de_DE"},
     * {@code "de"}, {@code "en"})
     */
    default String language() {
        return mpv5.plugin.ExtensionManager.resolveActiveLanguage();
    }

    /**
     * Resolves a plugin-scoped translation key for the active OpenYaBS user
     * language (fallback chain: exact locale -> base language -> {@code en} ->
     * key) and formats optional {@link java.text.MessageFormat} arguments.
     *
     * @param key short translation key registered by this plugin
     * @param args optional {@link java.text.MessageFormat} arguments ({0}, {1}...)
     * @return localized string, or {@code key} if not found
     */
    default String tr(String key, Object... args) {
        return trOrDefault(key, key, args);
    }

    /**
     * Resolves a plugin-scoped translation key for the active OpenYaBS user
     * language, falling back to {@code fallback} if not found.
     *
     * @param key short translation key registered by this plugin
     * @param fallback default pattern if the key is missing
     * @param args optional {@link java.text.MessageFormat} arguments ({0}, {1}...)
     * @return localized string
     */
    default String trOrDefault(String key, String fallback, Object... args) {
        String pattern = fallback != null ? fallback : (key == null ? "" : key);
        if (args != null && args.length > 0) {
            try {
                return java.text.MessageFormat.format(pattern, args);
            } catch (IllegalArgumentException ignored) {
                return pattern;
            }
        }
        return pattern;
    }

    /**
     * Opens a tab in the main window
     *
     * @param title
     * @param content
     */
    void openTab(String title, JComponent content);

    /**
     * Shows a short message in the status bar (and in the activity list)
     *
     * @param message
     */
    void notify(String message);

    /**
     * @return the settings of this plugin, stored on this computer
     */
    PluginSettings settings();

    /**
     * @return a folder of this plugin for its own files (created on demand)
     */
    File dataFolder();

    /**
     * Runs work in the background and continues on the Swing thread; failures
     * are shown and logged, the plugin keeps running
     *
     * @param <T>
     * @param work
     * @param then may be null
     */
    <T> void background(Supplier<T> work, Consumer<T> then);

    /**
     * Runs code when the plugin stops (also when it is disabled, reloaded or
     * fails to start); for Java and Groovy plugins alike
     *
     * @param action
     */
    default void onStop(Runnable action) {
    }

    // ------------------------------------------------------------- helpers
    /**
     * Runs a SELECT on the database of YaBS
     *
     * @param sql
     * @return the rows (never null; empty if nothing was found or the query
     * failed)
     */
    default Object[][] select(String sql) {
        mpv5.db.common.ReturnValue r = mpv5.db.common.QueryHandler.getConnection()
                .freeSelectQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        Object[][] data = r == null ? null : r.getData();
        return data == null ? new Object[0][] : data;
    }

    /**
     * Runs a statement (CREATE, INSERT, UPDATE ...) on the database of YaBS
     *
     * @param sql
     */
    default void execute(String sql) {
        mpv5.db.common.QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null);
    }

    /**
     * Runs code on the Swing thread: now if this is the Swing thread, else
     * later
     *
     * @param r
     */
    default void ui(Runnable r) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(r);
        }
    }

    /**
     * @return the record view in the selected tab of the main window, or null
     */
    default DataPanel currentView() {
        try {
            return mpv5.YabsViewProxy.instance().getCurrentTab();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Shows a message as notification pop-up (and in the activity list)
     *
     * @param message
     */
    default void popup(String message) {
        mpv5.ui.dialogs.Notificator.raiseNotification(message, true);
    }

    /**
     * Shows an error or a warning as notification pop-up
     *
     * @param message
     */
    default void error(String message) {
        mpv5.ui.dialogs.Notificator.raiseNotification(message, true);
    }

    /**
     * Shows a message about a record as notification pop-up, logged with the
     * record (a click opens it)
     *
     * @param message
     * @param about may be null
     */
    default void notify(String message, mpv5.db.common.DatabaseObject about) {
        if (about != null && about.__getIDS() > 0) {
            mpv5.ui.dialogs.Notificator.raiseNotification(message, true, true, about);
        } else {
            mpv5.ui.dialogs.Notificator.raiseNotification(message, true);
        }
    }

    /**
     * Opens a file with its program (PDF: the viewer of the settings)
     *
     * @param file
     */
    default void openFile(File file) {
        mpv5.utils.files.FileDirectoryHandler.open(file);
    }

    /**
     * Shows the record in the view of the selected tab again if that view shows
     * it (e.g. after the plugin changed or locked it)
     *
     * @param record
     */
    default void refreshView(mpv5.db.common.DatabaseObject record) {
        if (record == null || record.__getIDS() <= 0) {
            return;
        }
        ui(() -> {
            DataPanel v = currentView();
            if (v != null && v.getDataOwner() != null && v.getDataOwner().getClass() == record.getClass()
                    && v.getDataOwner().__getIDS() == record.__getIDS()) {
                v.setDataOwner(record, true);
            }
        });
    }

    /**
     * @return the version of YaBS, e.g. "2.1.0"
     */
    default String yabsVersion() {
        return mpv5.globals.VersionInfo.number();
    }
}
