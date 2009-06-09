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
package mpv5.data;

import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;

/**
 *
 */
public class TrashHandler {

    public static void delete(String type, int id) {
        QueryHandler.instanceOf().clone(type).delete(new String[][]{{"ids", String.valueOf(id), ""}}, Messages.DELETED.getValue());
    }

    public static Object[][] getData() {
        Object[][] data = QueryHandler.instanceOf().clone("trashbin").select("cname, rowid, description", new String[]{"deleteme", "1", ""});
        for (int i = 0; i < data.length; i++) {
            data[i][0] = data[i][0].toString().toUpperCase();
        }
        return data;
    }

    public static void restore(String type, int id) {
        QueryData q = new QueryData();
        q.add("invisible", 0);
        QueryHandler.instanceOf().clone(type).update(q, id, Messages.RESTORED.getValue());
        try {
            QueryCriteria qu = new QueryCriteria();
            qu.add("rowid", id);
            qu.add("cname", type.toLowerCase());
            QueryHandler.instanceOf().clone("trashbin").delete(qu);
        } catch (Exception ignore) {}
    }
}
