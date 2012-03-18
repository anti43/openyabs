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
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryParameter;
import javax.swing.JComponent;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import static mpv5.db.common.QueryParameter.*;

/**
 *
 *  
 */
public class UserProperty extends DatabaseObject {

    public UserProperty() {
        setContext(Context.getUserProperties());
    }
    private String value = "";
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
        if (value == null) {
            value = "";
        }
        QueryCriteria2 q = new QueryCriteria2();
        q.and(new QueryParameter(getContext(), "usersids", usersids, EQUALS),
                new QueryParameter(getContext(), "groupsids", __getGroupsids(), EQUALS),
                new QueryParameter(getContext(), "cname", getCname(), EQUALS));
        
        try {
            update(q);
            return true;
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
            QueryHandler.instanceOf().clone(Context.getUserProperties()).delete(q);
            return super.save(true);
        }
    }

    private void update(QueryCriteria2 q) throws NodataFoundException {
        QueryData d = new QueryData();
        d.add("value", value);
        d.add("dateadded", DateConverter.getSQLDateString(new Date()));
        QueryHandler.instanceOf().clone(Context.getUserProperties()).updateOrCreate(d, q, null);
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

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }
}
