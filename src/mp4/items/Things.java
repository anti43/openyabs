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

import mp4.datenbank.verbindung.DataLock;
import mp4.items.handler.NumberFormatHandler;

import mp4.datenbank.verbindung.EasyQuery;
import mp4.interfaces.Queries;
import mp4.datenbank.verbindung.Query;
import mp4.interfaces.Lockable;

/**
 *
 * @author anti
 */
public abstract class Things extends EasyQuery implements Queries, mp4.datenbank.installation.Tabellen, Lockable {

    public Integer id = 0;
    public boolean isSaved = false;
    public boolean readonly = false;
    public NumberFormatHandler nfh;
    private Query query;
    private DataLock datalock;


    /**
     * 
     * @param query
     */
    public Things(Query query) {
        super(query);
        this.query = query;
    }

    @Override
    public boolean lock() {
        datalock = new DataLock(query, id);
        return getDatalock().lockRow();
    }

    @Override
    public void unlock() {
        try {
            getDatalock().unLockRow();
        } catch (Exception e) {
        }
    }

    @Override
    public void finalize(){
        unlock();
    }
    

    /**
     * Needed for cloning subclasses
     */
    public Things() {
    }

    public Integer getId() {
        return id;
    }

    public NumberFormatHandler getNfh() {
        return nfh;
    }

    public DataLock getDatalock() {
        return datalock;
    }

  

}
