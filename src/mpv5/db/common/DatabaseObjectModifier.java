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
package mpv5.db.common;

import java.util.HashMap;
import java.util.Map;
import mpv5.ui.panels.ChangeNotApprovedException;

/**
 * Any {@link MPPlugin} which implements this interface will be registered automatically to the modifiers.
 * Any {@link DatabaseObject} will be passed through all DatabaseObjectModifiers <br/>
 * registered with loaded {@link mpv5.pluginhandling.MP5Plugin}s.
 * {@link mpv5.pluginhandling.MP5Plugin}s shall unregister from the {@link mpv5.pluginhandling.YabsPluginLoader} themselves on unload.
 */
public interface DatabaseObjectModifier {

    /**
     * This method will be called during construction of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object
     * @return
     */
    DatabaseObject modifyOnExplode(DatabaseObject object);

    /**
     * This method will be called during saving of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object
     * @return
     */
    DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException;

    
    /**
     * This method will be called after creation of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object 
     * @return
     */
    DatabaseObject executeAfterCreate(DatabaseObject object);
    
    /**
     * This method will be called after creation of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object 
     * @return
     */
    DatabaseObject executeAfterSave(DatabaseObject object);
    /**
     * This method will be called during reference-resolving any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param map
     * @return
     */
    Map<String, Object> modifyOnResolve(Map<String, Object> map);

    /**
     * This method will be called during saving of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object
     * @return
     */
    DatabaseObject modifyOnDelete(DatabaseObject object) throws ChangeNotApprovedException;
}
