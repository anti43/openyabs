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
package mpv5.db.objects;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.ui.frames.MPV5View;

/**
 *
 * @author anti
 */
public class Property extends DatabaseObject {

    public Property() {
        context.setDbIdentity(Context.IDENTITY_PROPERTIES_TO_USERS);
        context.setIdentityClass(this.getClass());
    }
    private String value;
    private int usersids;

    @Override
    public JComponent getView() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @return the value
     */
    public String __getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean save() {
//        QueryCriteria c = new QueryCriteria();
//        c.add("cname", cname);
//        c.add("usersids", usersids);
//
//        try {//delete old entries
//             ArrayList<DatabaseObject> obj = DatabaseObject.getObjects(context, c);
//            for (int i = 0; i < obj.size(); i++) {
//                obj.get(i).delete();
//            }
//
        QueryCriteria c = new QueryCriteria();
        c.add("usersids", MPV5View.getUser().__getIDS());
        c.add("cname", cname);
        QueryHandler.instanceOf().clone(Context.getProperties()).delete(c);
//        } catch (NodataFoundException ignore) { }

        return super.save();
    }

    /**
     * @return the usersids
     */
    public int __getUsersids() {
        return usersids;
    }

    /**
     * @param usersids the usersids to set
     */
    public void setUsersids(int usersids) {
        this.usersids = usersids;
    }
}
