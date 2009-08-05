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

/**
 * Any {@link DatabaseObject} will be passed through all DatabaseObjectModifiers <br/>
 * registered with loaded {@link mpv5.pluginhandling.MP5Plugin}s on loading time of the DatabaseObject.
 * {@link mpv5.pluginhandling.MP5Plugin} s shall unregister from the {@link mpv5.pluginhandling.MPPLuginLoader} themselves on unload.
 */
public interface DatabaseObjectModifier {

    /**
     * This method will be called during construction of any {@link DatabaseObject} if this <br/>
     * {@link DatabaseObjectModifier} is registered with a loaded  {@link mpv5.pluginhandling.MP5Plugin}
     * @param object
     * @return
     */
    DatabaseObject modify(DatabaseObject object);
}
