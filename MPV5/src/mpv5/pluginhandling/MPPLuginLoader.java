/*
 *  This file is part of MP by anti43 /GPL.
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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;
import mpv5.usermanagement.UserPlugin;

/**
 *
 * @author anti
 */
public class MPPLuginLoader {
public static String pluginSignature = LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator + "%%filename%%-mp5p.jar";
    /**
     * Loads all plugins which are assigned to the current logged in user from database or cache dir<br/>
     * does NOT call plugin.load()
     * @return An array of instantiated plugins
     */
    public MP5Plugin[] getPlugins(){



        ArrayList<MP5Plugin> list = null;
        
        try {
            list = new ArrayList<MP5Plugin>();
            ArrayList<File> jars = new ArrayList<File>();
            QueryCriteria criterias = new QueryCriteria();
            criterias.add("usersids", MPV5View.getUser().__getIDS());
            try {
                ArrayList<UserPlugin> data = DatabaseObject.getObjects(Context.getPluginsToUsers(), criterias);
                for (int i = 0; i < data.size(); i++) {
                    UserPlugin up = data.get(i);
                    if (!isCachedPlugin(up.__getFilename())) {
                        Log.Debug(this, "Caching plugin: " + pluginSignature.replace("%%filename%%", up.__getFilename()));
                        jars.add(QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(up.__getFilename(), new File(pluginSignature.replace("%%filename%%", up.__getFilename()))));
                    } else {
                        Log.Debug(this, "Using cached plugin: " + pluginSignature.replace("%%filename%%", up.__getFilename()));
                        jars.add(new File(pluginSignature.replace("%%filename%%", up.__getFilename())));
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }

            for (int i = 0; i < jars.size(); i++) {
                File x = jars.get(i);
                MP5Plugin c= checkPlugin(x);
                if (c !=null) {
                    list.add(c);
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
     * @param pluginid 
     * @return the plugin jar if the file DOES NOT EXIST  in the plugin cache directory, null otherwise
     */
    public boolean isCachedPlugin(String pluginid) {
        File f = new File(pluginSignature.replace("%%filename%%", pluginid));
        return f.exists() && f.canRead();
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
}
