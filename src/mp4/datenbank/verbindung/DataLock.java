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
package mp4.datenbank.verbindung;

import java.util.ArrayList;

import mp4.benutzerverwaltung.User;
import mp4.datenbank.installation.Tabellen;
import mp4.frames.mainframe;
import mp4.items.visual.Popup;
import mp4.logs.Log;

/**
 * This class is an own implementation of the db concurrency problem
 * @author Andreas
 */
public class DataLock extends mp4.datenbank.verbindung.EasyQuery {

    private Integer itemid;
    private String itemtable;
    private Integer ID = 0;
    private User lockowner;
    @SuppressWarnings("unchecked")
    public static ArrayList<Integer> locks = new ArrayList<Integer>();

    /**
     * Creates an empty Datalock
     */
    public DataLock() {
        super(ConnectionHandler.instanceOf().clone(Tabellen.TABLE_ROWLOCK));
    }

    /**
     * Cretes a DataLock object for the given item
     * @param query The queryhandler
     * @param id The item id
     */
    public DataLock(Query query, Integer id) {
        super(ConnectionHandler.instanceOf().clone(Tabellen.TABLE_ROWLOCK));
        this.itemid = id;
        this.itemtable = query.getTable();
        Log.Debug(this, "Rowlock: New lock for: " + itemid + " : " + itemtable, true);
    }

    /**
     * Truncate the Lockdatabase
     */
    public void flush() {
        int count = ConnectionHandler.instanceOf().clone(Tabellen.TABLE_ROWLOCK).selectCount(null, null);
        freeQuery("DELETE FROM " + Tabellen.TABLE_ROWLOCK);
        Popup.notice(count + " Objekte released.");
    }

    /**
     * Try to lock the dataset
     * @return True if this was possible
     */
    public boolean lockRow() {
        if (!ConnectionTypeHandler.isInSingleUserMode()) {
            Log.Debug(this, "Rowlock: Lock an item: " + itemid, true);
            String[][] tabledata = select("rowid, userid", "tablename", getItemtable(), false);
            for (int i = 0; i < tabledata.length; i++) {
                String[] strings = tabledata[i];
                if (strings[0].equals(getItemid().toString())) {
                    try {
                        this.lockowner = new User(Integer.valueOf(strings[1]));
                        if (lockowner.getId().intValue() == mainframe.getUser().getId().intValue()) {
                            Log.Debug(this, "Rowlock: Item already locked by you!", true);
                            return true;
                        }
                    } catch (Exception numberFormatException) {
                        lockowner = null;
                        Log.Debug(this, numberFormatException, true);
                    }
                    Log.Debug(this, "Rowlock: Already locked by: " + lockowner, true);
                    mainframe.setErrorText("Datensatz ist zur Zeit gesperrt von: " + lockowner + " und kann nicht editiert werden.");
                    return false;
                }
            }
            ID = insert(Tabellen.TABLE_ROWLOCK_FIELDS, PrepareData.finalize(
                    PrepareData.prepareString(getItemtable()) +
                    PrepareData.prepareNumber(getItemid()) +
                    PrepareData.prepareNumber(mainframe.getUser().getId())));
            locks.add(ID);
            Log.Debug(this, "Rowlock: Lock successfull: " + ID, true);
            mainframe.setInfoText("Datensatz ist nun reserviert und kann editiert werden.");
            return true;
        } else {
            Log.Debug(this, "Rowlock: Lock not enabled for:" + ConnectionTypeHandler.getDriverName(), true);
            return true;
        }
    }

    /**
     * Unlock the dataset
     */
    public void unLockRow() {
        if (ID.intValue() != 0) {
            Log.Debug(this, "Rowlock: Unlocking .." + ID, true);
            try {
                delete(getID());
                locks.remove(ID);
            } catch (Exception e) {
                Log.Debug(this, "Rowlock: Nothing to release..");
            }
        }
    }

    @Override
    public void finalize() {
        if (ID.intValue() != 0) {
            Log.Debug(this, "Rowlock: Late releasing .." + ID, true);
            delete(ID);
        }
    }

    public Integer getItemid() {
        return itemid;
    }

    public String getItemtable() {
        return itemtable;
    }

    public Integer getID() {
        return ID;
    }

    public User getLockowner() {
        return lockowner;
    }
}
