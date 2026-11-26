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

/**
 * A YaBS plugin. Implement it, list the class in
 * {@code META-INF/services/mpv5.plugin.api.YabsExtension} of your jar and drop
 * the jar into the plugins folder (or install it in Settings &gt; Plugins).
 * <p>
 * Everything a plugin adds through the {@link ExtensionContext} (commands,
 * settings pages, search results, listeners ...) is removed automatically
 * when it stops, so {@link #stop()} only needs to free what the plugin made
 * itself (threads, files, connections).
 * <pre>
 * public class Hello implements YabsExtension {
 *     public String id() { return "hello"; }
 *     public String name() { return "Hello"; }
 *     public void start(ExtensionContext ctx) {
 *         ctx.addCommand("Say hello", "Shows a greeting", () -&gt; ctx.notify("Hello!"));
 *     }
 * }
 * </pre>
 */
public interface YabsExtension {

    /**
     * @return a stable, unique id (e.g. "com.example.hello"); also names the
     * settings of the plugin
     */
    String id();

    /**
     * @return the name shown to the user
     */
    String name();

    /**
     * @return what the plugin does, one or two sentences
     */
    default String description() {
        return "";
    }

    /**
     * @return the version, e.g. "1.0"
     */
    default String version() {
        return "1.0";
    }

    /**
     * @return who made it
     */
    default String author() {
        return "";
    }

    /**
     * @return the minimum YaBS release version required by this extension (e.g. "2.0.9"),
     * or null if any version is supported
     */
    default String minYabsVersion() {
        return null;
    }

    /**
     * Where YaBS looks for new versions of this plugin: a GitHub repository
     * as "owner/repository" (e.g. "example/yabs-hello"). Its latest release
     * (no pre-release) must be tagged with the version ("1.2.0" or "v1.2.0")
     * and have the plugin jar as an asset (the asset of the same file name as
     * the installed jar, otherwise the only .jar). YaBS compares the tag with
     * {@link #version()}, tells the user about a newer one and installs it on
     * request.
     * <p>
     * The plugins made with YaBS (folder plugins/ of the YaBS repository) need
     * nothing here: they are checked against the plugin list
     * (openyabs-plugins.json) of the latest YaBS release.
     *
     * @return "owner/repository" on GitHub, or null for no update check of
     * its own
     */
    default String updateSource() {
        return null;
    }

    /**
     * Starts the plugin: register commands, pages, listeners ... (called on
     * the Swing thread)
     *
     * @param ctx what YaBS offers to the plugin
     * @throws Exception the plugin is marked as failed with the message, YaBS
     * goes on
     */
    void start(ExtensionContext ctx) throws Exception;

    /**
     * Stops the plugin (called on the Swing thread). The contributions made
     * through the context are removed by YaBS.
     */
    default void stop() {
    }
}
