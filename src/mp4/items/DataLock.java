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
import mp4.datenbank.verbindung.PrepareData;
import mp4.datenbank.verbindung.Query;
import mp4.frames.mainframe;

/**
 *
 * @author Andreas
 */
public class DataLock extends mp4.datenbank.verbindung.EasyQuery{
    private Integer itemid;
    private String itemtable;
    private Integer ID;

    public DataLock(Query query, Integer id) {
        super(ConnectionHandler.instanceOf().clone(Tabellen.TABLE_ROWLOCK));
        this.itemid = id;
        this.itemtable = query.getTable();
    }

    public boolean lockRow() {
        String[][] tabledata = select("rowid", "tablename", itemtable, false);
        for (int i = 0; i < tabledata.length; i++) {
            String[] strings = tabledata[i];
            if(strings[0].equals(itemid.toString())) {
                return false;
            }
        } 
        insert(Tabellen.TABLE_ROWLOCK_FIELDS, PrepareData.finalize(
                PrepareData.prepareString(itemtable) + 
                PrepareData.prepareNumber(itemid) + 
                PrepareData.prepareNumber(mainframe.getUser().getId())));
        return true; 
    }

    public void unLockRow() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
