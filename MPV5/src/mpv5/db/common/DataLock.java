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
package mpv5.db.common;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPV5View;
import mpv5.usermanagement.User;
import sun.jdbc.odbc.ee.ConnectionHandler;

/**
 * This class is an own implementation of the db concurrency problem
 * @author Andreas
 */
public class DataLock {

    private Integer itemid;
    private String itemname;
    private String itemtable;
    private Integer ID = 0;
    private User lockowner;
    @SuppressWarnings("unchecked")
    public static ArrayList<Integer> locks = new ArrayList<Integer>();

    /**
     * Cretes a DataLock object for the given item
     * @param context
     * @param id The item id
     */
    public DataLock(Context context, Integer id, String itemname) {
        this.itemid = id;
        this.itemtable = context.getDbIdentity();
        this.itemname = itemname;
//        Log.Debug(this, "Rowlock: New lock for: " + itemid + " : " + itemtable, true);
    }

    private DataLock() {
    }

    /**
     * Truncate the Lockdatabase
     */
    public void flush() {
        int count = QueryHandler.instanceOf().clone(Context.getLock().getDbIdentity()).selectCount(null, null);
        QueryHandler.instanceOf().clone(Context.getLock()).truncate(Context.getLock().getDbIdentity());
        Popup.notice(this, count + " Objekte released.");
    }

    public static void lateRelease() {
        DataLock lockhandler = new DataLock();
        String[][] data = new String[DataLock.locks.size()][];
        for (int ix = 0; ix < DataLock.locks.size(); ix++) {
            Integer id = DataLock.locks.get(ix);
            Log.Debug(lockhandler, "Rowlock: Late releasing .." + id, true);
            data[ix] = new String[]{"ids", id.toString(), ""};
        }
        try {
            QueryHandler.instanceOf().clone(Context.getLock()).delete(data);
        } catch (Exception ex) {
            Log.Debug(DataLock.class, ex);
        }
    }

    /**
     * Try to lock the dataset
     * @return True if this was possible
     */
    public boolean lockRow() {
        if (!ConnectionTypeHandler.isInSingleUserMode()) {
//            Log.Debug(this, "Rowlock: Lock an item: " + itemid, true);
            Object[][] tabledata = QueryHandler.instanceOf().clone(Context.getLock()).
                    select("rowid, userid", new String[]{"cname", getItemtable(), "'"});
            for (int i = 0; i < tabledata.length; i++) {
                Object[] strings = tabledata[i];
                if (strings[0].equals(getItemid().toString())) {
                    try {
                        this.lockowner = new User(Integer.valueOf(String.valueOf(strings[1])));
                        if (lockowner.getID().intValue() == MPV5View.getUser().getID().intValue()) {
//                            Log.Debug(this, "Rowlock: Item already locked by you!", true);
                            return true;
                        }
                    } catch (Exception numberFormatException) {
                        lockowner = null;
                        Log.Debug(this, numberFormatException, true);
                    }
//                    Log.Debug(this, "Rowlock: Already locked by: " + lockowner, true);
                    MPV5View.addMessage("Datensatz ist zur Zeit gesperrt von: " + lockowner + " und kann nicht editiert werden.");
                    return false;
                }
            }
            ID = QueryHandler.instanceOf().clone(Context.getLock()).insert(new String[]{"cname,rowid,userid", PrepareData.finalize(
                        PrepareData.prepareString(getItemtable()) +
                        PrepareData.prepareNumber(getItemid()) +
                        PrepareData.prepareNumber(MPV5View.getUser().getID()))}, itemname + Messages.LOCKED);
            locks.add(ID);
//            Log.Debug(this, "Rowlock: Lock successfull: " + ID, true);
            return true;
        } else {
//            Log.Debug(this, "Rowlock: Lock not enabled for:" + ConnectionTypeHandler.getDriverName(), true);
            return true;
        }
    }

    /**
     * Unlock the dataset
     */
    public void unLockRow() {
        if (ID.intValue() != 0) {
//            Log.Debug(this, "Rowlock: Unlocking .." + ID, true);
            try {
                try {
                    QueryHandler.instanceOf().clone(Context.getLock()).delete(new String[][]{{"ids", ID.toString(),""}});
                } catch (Exception ex) {
                    Log.Debug(DataLock.class, ex);
                }
                locks.remove(ID);
            } catch (Exception e) {
//                Log.Debug(this, "Rowlock: Nothing to release..");
            }
        }
    }

    @Override
    public void finalize() {
       unLockRow();
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