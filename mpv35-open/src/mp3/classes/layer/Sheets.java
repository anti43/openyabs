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

package mp3.classes.layer;

import javax.swing.JTable;
import mp3.classes.interfaces.Structure;
import mp3.classes.interfaces.Queries;
import mp3.database.util.Query;

/**
 *
 * @author anti
 */
public abstract class Sheets implements Queries, Structure {
    public Integer id = 0;
    public boolean isSaved = false;
    private Query q;

    /**
     * 
     * @param query
     */
    public Sheets (Query query) {

        q = query;
    }

    public int delete(String id) {
        String[] where = {"id", id, ""};
        return q.delete(where);
    }

    @Override
    public void finalize() {

        if (!isSaved) {
            this.save();
        }

    }

    public String getId() {
        return id.toString();
    }

    /**
     * 
     * @param set
     * @param value
     * @param id
     * @return rowcount
     */
    public int update(String set, String value, String id) {
        String[] where = {"id", id, ""};
        String[] what = {set, value, "'"};
        return q.update(what, where);
    }

       public String[] selectFirst(String what, String from, String where, boolean id) {
        String hk = "'";
        if (id) {
            hk = "";
        }

        String[] wher = {from, where, hk};
      
        return q.selectFirst(what, wher);
    }
    
    public String[] selectLast(String what, String from, String where, boolean id,boolean like) {
        String hk = "'";
        if (id) {
            hk = "";
        }

        String[] wher = {from, where, hk};
      
        return q.selectLast(what, wher,false,like);
    }
    
    public String[][] select(String what, String from, String where, boolean id) {
        String hk = "'";
        if (id) {
            hk = "";
        }

        String[] wher = {from, where, hk};
        return q.select(what, wher);
    }

    public int insert(String set, String value) {
        String[] str = {set, value, ""};
        return q.insert(str);
    }

    public void destroy() {
        this.delete(this.id.toString());
        this.id = 0;
    }
    /**
     * Hides he first column of a table (usually "id")
     * @param table
     */
    public void stripFirst (JTable table) {
       
    table.getColumn(table.getColumnName(0)).setMinWidth(0);
    table.getColumn(table.getColumnName(0)).setMaxWidth(0);
        

    }
    /**
     * to be overwritten..
     */
    public abstract void save ();
}
