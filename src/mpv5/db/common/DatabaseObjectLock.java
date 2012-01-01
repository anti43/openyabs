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

import java.util.Vector;
import mpv5.db.objects.User;
import mpv5.logging.Log;

/**
 * This class is used to lock objects to prevent parallel editing.
 */
public class DatabaseObjectLock {

    private boolean LOCKED = false;
    private DatabaseObject dbo;
    public static Vector<DatabaseObject> lockedObjects = new Vector<DatabaseObject>();

    /**
     * Creates a new lock for the given DatabaseObject.
     * <br/>This does not result in a locked object. Call {@link aquire()} to verify.
     * <br/>A Lock may fail if the DO is already locked, or not existing
     * @param dbo
     */
    public DatabaseObjectLock(DatabaseObject dbo) {
        this.dbo = dbo;
    }

    /**
     * Checks if this lock is valid
     * @return TRUE if this Lock does represent a valid lock - if the DO is locked for the current user
     */
    public synchronized boolean aquire() {
        if (dbo.__getIDS().intValue() > 0 && !dbo.isReadOnly()) {
            for (int i = 0; i < lockedObjects.size(); i++) {
                DatabaseObject databaseObject = lockedObjects.get(i);
                if (databaseObject.equals(dbo)) {
                    Log.Debug(this, "Already locked for you: " + dbo);
                    return true;
                }
            }
            return lock(dbo);
        } else {
            return false;
        }
    }

    /**
     * Releases this lock, if valid. Does nothing if this lock is not valid at all.
     */
    public void release() {
        if (LOCKED) {
            Log.Debug(this, "Releasing item " + dbo);
            QueryHandler.instanceOf().clone(Context.getLock()).removeLock(dbo.getContext(), dbo.__getIDS(), mpv5.db.objects.User.getCurrentUser());
            lockedObjects.remove(dbo);
            LOCKED = false;
        }
    }

    private synchronized boolean lock(DatabaseObject dbo) {
        if (!LOCKED) {
            try {
                Log.Debug(this, "Trying to lock item " + dbo);
                QueryHandler.instanceOf().clone(Context.getLock()).insertLock(dbo.getContext(), dbo.__getIDS(), mpv5.db.objects.User.getCurrentUser());
                lockedObjects.add(dbo);
                LOCKED = true;
            } catch (UnableToLockException ex) {
                Log.Debug(this, ex.getMessage());
//                Log.Debug(this, ex );
                LOCKED = false;
                dbo.ReadOnly(true);
            }
        }

        return LOCKED;
    }

    /**
     * Releases all the users objects, and all locked objects from this program instance
     * @param user
     */
    public static void releaseAllObjectsFor(User user) {
        try {
            QueryHandler.instanceOf().clone(Context.getLock()).delete(new String[][]{{"usersids", user.__getIDS().toString(), ""}});
            for (int i = 0; i < lockedObjects.size(); i++) {
                DatabaseObject databaseObject = lockedObjects.get(i);
                databaseObject.release();
            }
            lockedObjects.removeAllElements();
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }
}
