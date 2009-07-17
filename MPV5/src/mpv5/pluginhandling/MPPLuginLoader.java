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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;

/**
 *
 *  This class loads plugins from the database, and utilises caching of the plugin files.
 */
public class MPPLuginLoader {

    public static String pluginSignature = LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator + "%%filename%%-mp5p.jar";
    public static List<DatabaseObjectModifier> registeredModifiers = new Vector<DatabaseObjectModifier>();

    public static Image getDefaultPluginImage() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(MPPLuginLoader.class.getResource("/mpv5/resources/images/48/blockdevice.png"));
        } catch (Exception e) {
            Log.Debug(e);
        }
        return img;
    }

    public static Image getErrorImage() {
         BufferedImage img = null;
        try {
            img = ImageIO.read(MPPLuginLoader.class.getResource("/mpv5/resources/images/48/messagebox_question.png"));
        } catch (Exception e) {
            Log.Debug(e);
        }
        return img;
    }

    /**
     * Loads all plugins which are assigned to the current logged in user from database or cache dir<br/>
     * does NOT call plugin.load()
     * @return An array of instantiated plugins
     */
    public MP5Plugin[] getPlugins() {

        ArrayList<MP5Plugin> list = null;

        try {
            list = new ArrayList<MP5Plugin>();
            ArrayList<File> jars = new ArrayList<File>();
            QueryCriteria criterias = new QueryCriteria();
            criterias.add("usersids", MPView.getUser().__getIDS());
            try {
                ArrayList<UserPlugin> data = DatabaseObject.getObjects(Context.getPluginsToUsers(), criterias);
                for (int i = 0; i < data.size(); i++) {
                    UserPlugin up = data.get(i);
                    Plugin o = ((Plugin) DatabaseObject.getObject(Context.getPlugins(), up.__getPluginsids()));
                    Log.Debug(this, "Found Plugin: " + o);
                    if (!isCachedPlugin(o.__getFilename())) {
                        Log.Debug(this, "Caching plugin: " + pluginSignature.replace("%%filename%%", o.__getFilename()));
                        jars.add(QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(o.__getFilename(), new File(pluginSignature.replace("%%filename%%", o.__getFilename()))));
                    } else {
                        Log.Debug(this, "Using cached plugin: " + pluginSignature.replace("%%filename%%", o.__getFilename()));
                        jars.add(new File(pluginSignature.replace("%%filename%%", o.__getFilename())));
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }

            for (int i = 0; i < jars.size(); i++) {
                File x = jars.get(i);
                MP5Plugin c = checkPlugin(x);
                if (c != null) {

                    list.add(c);
                    if (c instanceof DatabaseObjectModifier) {
                        registeredModifiers.add((DatabaseObjectModifier) c);
                    }
                }
            }
        } catch (Exception e) {
            Popup.error(e);
            Log.Debug(e);
        }

        return list.toArray(new MP5Plugin[0]);
    }

    /**
     * Checks if the plugin is already cached
     * @param filename
     * @return false if the file DOES NOT EXIST in the plugin cache directory, true otherwise
     */
    public boolean isCachedPlugin(String filename) {
        File f = new File(pluginSignature.replace("%%filename%%", filename));
        Log.Debug(this, "Checking cache for " + filename);
        return f.exists() && f.canRead() && (checkPlugin(f) != null);
    }

    /**
     * Checks if the given file is a valid plugin
     * @param pluginCandidate
     * @return the plugin if <code>Constants.PLUGIN_LOAD_CLASS<code/> could be instantiated from this file
     */
    public MP5Plugin checkPlugin(File pluginCandidate) {
        try {
            URL[] urls = {new URL("jar:file:" + pluginCandidate + "!/")};
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            Class c = loader.loadClass(Constants.PLUGIN_LOAD_CLASS);
            Object o = c.newInstance();
            return (MP5Plugin) o;
        } catch (Exception malformedURLException) {
            Log.Debug(malformedURLException);
            return null;
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public MP5Plugin getPlugin(File file) {
        return checkPlugin(file);
    }
}
