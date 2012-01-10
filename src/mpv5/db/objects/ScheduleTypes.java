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
package mpv5.db.objects;

import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;

/**
 *
 *  
 */
public class ScheduleTypes extends DatabaseObject {
    private int UserIds_;

    public ScheduleTypes() {
        setContext(Context.getScheduleTypes());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }

    public void setUser(Integer valueOf) {
        UserIds_ = valueOf;
    }

    @Displayable(false)
    @Override
    public int __getGroupsids() {
        return super.__getGroupsids();
    }

    @Displayable(false)
    @Override
    public Date __getDateadded() {
        return super.__getDateadded();
    }

    
    @Displayable(true)
    public int __getUserSids() {
        return UserIds_;
    }

    public void setUserSids(int ID) {
        UserIds_ = ID;
    }
}
