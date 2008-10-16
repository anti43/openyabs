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
package mp4.items;

import mp4.datenbank.installation.Tabellen;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.datenbank.verbindung.EasyQuery;
import mp4.datenbank.verbindung.Query;

/**
 *
 * @author anti43
 */
class DatabaseRowLocker extends EasyQuery implements Tabellen {

    private String table;
    private int rowlockid;

    public DatabaseRowLocker(Query query) {
        super(ConnectionHandler.instanceOf().clone(TABLE_ROWLOCK));
        this.table = query.getTable();
    }

    public boolean lockRow(Integer rowid) {
       rowlockid = insert(TABLE_ROWLOCK_FIELDS, table + "," + rowid + "," + mp4.frames.mainframe.getUser().getId());
        if (rowlockid != 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean unlockRow() {
        if (delete(rowlockid)!=0) {
            return true;
        } else {
            return false;
        }
    }
}
