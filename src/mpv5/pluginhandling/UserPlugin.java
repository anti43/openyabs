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

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.images.MPIcon;

/**
 * This binds plugins to users
 *  
 */
public class UserPlugin extends DatabaseObject {

    private int pluginsids;
    private int usersids;

    public UserPlugin() {
       setContext(Context.getPluginsToUsers());
    }

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * @return the contactsids
     */
    public int __getUsersids() {
        return usersids;
    }

    /**
     * @param contactsids the contactsids to set
     */
    public void setUsersids(int contactsids) {
        this.usersids = contactsids;
    }

    /**
     * @return the pluginsids
     */
    public int __getPluginsids() {
        return pluginsids;
    }

    /**
     * @param pluginsids the pluginsids to set
     */
    public void setPluginsids(int pluginsids) {
        this.pluginsids = pluginsids;
    }


    @Override
    public MPIcon getIcon() {
        return null;
    }

}
