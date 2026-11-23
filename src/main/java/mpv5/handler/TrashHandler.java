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
        if (!deleteFinally(type, id, message)) {
            return;
        }
        Runnable runnable = new Runnable() {

            public void run() {
                QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(message, mpv5.db.objects.User.getCurrentUser().__getCname(), type, id, mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Deletes a record of the trash for good, unless it must be kept
     * (retention of invoices and of the documents of a contact, see
     * {@link Privacy#whyKeep}); deletes its properties too
     *
     * @param type
     * @param id
     * @param message
     * @return true if deleted
     */
    public static boolean deleteFinally(final String type, final int id, final String message) {
        Context context = Context.getMatchingContext(type);
        if (context != null) {
            String keep = Privacy.whyKeep(context, id);
            if (keep != null) {
                Log.Debug(TrashHandler.class, "Not deleted: " + keep);
                REFUSED.add(keep);
                return false;
            }
        }
        boolean deleted = QueryHandler.instanceOf().clone(type).delete(new String[][]{{"ids", String.valueOf(id), ""}}, message);
        if (deleted && context != null) {
            //the properties: stored with any context of the table (e.g. customer, supplier for contacts)
            QueryHandler.instanceOf().clone(Context.getValueProperties()).freeUpdateQuery(
                    "DELETE FROM %%tablename%% WHERE contextids IN (" + Privacy.contextIds(context.getDbIdentity())
                    + ") AND objectids = " + id,
                    mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, null);
        }
        return deleted;
    }

    /**
     * Why records were not deleted (for the message after emptying the trash)
     */
    private static final java.util.List<String> REFUSED = new java.util.ArrayList<String>();

    /**
     * @return why records were not deleted since the last call, empty if all
     * were deleted
     */
    public static synchronized java.util.List<String> takeRefused() {
        java.util.List<String> l = new java.util.ArrayList<String>(REFUSED);
        REFUSED.clear();
        return l;
    }

    /**
     * Fetches the trashbin data
     * @return
     */
    public static Object[][] getData() {
        Object[][] data = QueryHandler.instanceOf().clone("trashbin").select("cname, rowid, description", new String[]{"deleteme", "1", ""});
        mpv5.db.objects.User u = mpv5.db.objects.User.getCurrentUser();
        boolean restricted = u != null && u.isGroupRestricted();
        int userGroupId = restricted ? u.__getGroupsids() : -1;
        java.util.List<Object[]> filtered = new java.util.ArrayList<Object[]>(data.length);
        for (int i = 0; i < data.length; i++) {
            String type = data[i][0].toString().toUpperCase();
            if (restricted) {
                try {
                    Context ctx = Context.getMatchingContext(type);
                    int rowId = Integer.parseInt(String.valueOf(data[i][1]));
                    DatabaseObject dbo = DatabaseObject.getObject(ctx, rowId, true);
                    if (dbo.__getGroupsids() != userGroupId) {
                        continue;
                    }
                } catch (Exception ex) {
                    continue;
                }
            }
            data[i][0] = type;
            filtered.add(data[i]);
        }
        return filtered.toArray(new Object[0][]);
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
