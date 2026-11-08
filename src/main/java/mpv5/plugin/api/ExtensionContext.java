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
}
