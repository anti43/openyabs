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
