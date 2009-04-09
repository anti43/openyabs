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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;
import mpv5.usermanagement.UserPlugin;

/**
 *
 * @author anti
 */
public class MPPLuginLoader {

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
                    if (isCachedPlugin(up.__getFilename()) == null) {
                        Log.Debug(this, "Caching plugin: " + isCachedPlugin(up.__getFilename()));
                        jars.add(QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(up.__getFilename(), isCachedPlugin(up.__getFilename())));
                    } else {
                        Log.Debug(this, "Using cached plugin: " + isCachedPlugin(up.__getFilename()));
                        jars.add(isCachedPlugin(up.__getFilename()));
                    }
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }

            for (int i = 0; i < jars.size(); i++) {
                URL[] urls = {new URL("jar:file:" + jars.get(i).getPath() + "!/")};
                URLClassLoader loader = URLClassLoader.newInstance(urls);
                Class c = loader.loadClass(Constants.PLUGIN_LOAD_CLASS);
                Object o = c.newInstance();
                MP5Plugin m = (MP5Plugin) o;
                list.add(m);
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
     * @return the plugin jar if the file exists in the plugin cache directory
     */
    public File isCachedPlugin(String pluginid) {
        File f = new File(Constants.CACHE_DIR + File.separator + pluginid + "-mp5p.jar");
        if (f.exists()) {
            return f;
        } else {
            return null;
        }
    }
}
