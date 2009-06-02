/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.pluginhandling;

import mpv5.ui.frames.MPV5View;

/**
 * This class is to be implemented on MP5 plugins
 *  anti
 */
public interface MP5Plugin {

    /**
     * Initializes the plugin
     * @param frame
     * @return
     */
    public abstract MP5Plugin load(MPV5View frame);

    /**
     * Unloads the plugin
     */
    public abstract void unload();

    /**
     *
     * @return The name of the plugin
     */
    public abstract String getName();

    /**
     *
     * @return The vendor of the plugin
     */
    public abstract String getVendor();

    /**
     *
     * @return The uid of the plugin
     */
    public abstract Long getUID();

    /**
     * Check whether this plugin is enabled
     * @return True if the plugin is active
     */
    public abstract boolean isEnabled();

    /**
     * Check whether this plugin inherits <code>JPanel</code> and can be displayed
     * @return True if the plugin is to be displayed on the main tab pane
     */
    public abstract boolean isComponent();

    /**
     * Check whether this plugin is a <code>Runnable<code/>
     * @return
     */
    public abstract boolean isRunnable();

    /**
     * Inidcates whether the plugin has bee previously loaded
     * @return
     */
    public abstract boolean isLoaded();
}
