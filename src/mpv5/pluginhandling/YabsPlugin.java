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
package mpv5.pluginhandling;


import java.awt.Image;
import mpv5.YabsView;
import mpv5.ui.frames.MPView;

/**
 * This class is to be implemented on MP5 plugins
 *  
 */
public interface YabsPlugin{


    /**
     * Initializes the plugin
     * @param view YabsView
     * @return
     */
    public abstract YabsPlugin load(Object view);


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
     * @return The uid of the plugin, should be randomly generated,
     * <br/>to be unique for each instance of the plugin!
     */
    public abstract Long getUID();

    /**
     * Check whether this plugin is enabled
     * @return True if the plugin is to be loaded
     */
    public abstract boolean isEnabled();

    /**
     * Check whether this plugin inherits <code>JPanel</code> and shall be displayed on the main tab pane
     * @return True if the plugin is to be displayed on the main tab pane
     */
    public abstract boolean isComponent();

    /**
     * Check whether this plugin is a <code>Runnable<code/>
     * @return TRUE if this is a {@link Runnable}
     */
    public abstract boolean isRunnable();

    /**
     * Inidcates whether the plugin has bee previously loaded
     * @return TRUE if {@link MP5Plugin#load(MP5View)} has been called
     */
    public abstract boolean isLoaded();

    /**
     * If this returns null, a default icon is used to represent this plugin
     * @return An Icon which graphically represents the plugin
     */
    public abstract Image getIcon();
}
