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
package mpv5.db.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;

/**
 * This class is used to lock objects to prevent parallel editing.
 */
public class DatabaseObjectLock {

    private boolean LOCKED = false;
    private DatabaseObject dbo;

    /**
     * Creates a new lock for the given DatabaseObject.
     * <br/>This does not result in a locked object. Call {@link check()} to verify.
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
    public synchronized boolean check() {
        if (dbo.isExisting() && !dbo.readOnly) {
            return lock(dbo);
        } else {
            return false;
        }
    }

    private synchronized boolean lock(DatabaseObject dbo) {
        try {
            Log.Debug(this, "Trying to lock item " + dbo);
            QueryHandler.instanceOf().clone(Context.getLock()).insertLock(dbo.getContext(), dbo.__getIDS(), mpv5.ui.frames.MPV5View.getUser());
            LOCKED = true;
        } catch (UnableToLockException ex) {
            Log.Debug(this, ex.getMessage());
            LOCKED = false;
            dbo.readOnly = true;
        }

        return LOCKED;
    }
}
