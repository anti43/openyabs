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
package mpv5.handler;

import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *
 */
public class TrashHandler {

    /**
     * Finally delete an item
     * @param type
     * @param id
     * @param message
     */
    public static void delete(final String type, final int id, final String message) {
        QueryHandler.instanceOf().clone(type).delete(new String[][]{{"ids", String.valueOf(id), ""}}, message);
        Runnable runnable = new Runnable() {

            public void run() {
                QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(message, mpv5.db.objects.User.getCurrentUser().__getCname(), type, id, mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Fetches the trashbin data
     * @return
     */
    public static Object[][] getData() {
        Object[][] data = QueryHandler.instanceOf().clone("trashbin").select("cname, rowid, description", new String[]{"deleteme", "1", ""});
        for (int i = 0; i < data.length; i++) {
            data[i][0] = data[i][0].toString().toUpperCase();
        }
        return data;
    }

    /**
     * Restores the given item
     * @param type
     * @param id
     * @param message
     */
    public static void restore(final String type, final int id, final String message) {

        Context context = Context.getMatchingContext(type);
        try {
            DatabaseObject dbo = DatabaseObject.getObject(context, id, true);
            Log.Debug(TrashHandler.class, "Restoring: " + dbo);
            dbo.undelete();
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        Runnable runnable = new Runnable() {

            public void run() {
                QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(message, mpv5.db.objects.User.getCurrentUser().__getCname(), type, id, mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
}
