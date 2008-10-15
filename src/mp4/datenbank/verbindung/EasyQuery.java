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

import mp4.interfaces.Queries;
import mp4.items.visual.Popup;

/**
 *
 * @author anti43
 */
public class EasyQuery implements Queries {

    private Query q;

    public EasyQuery(Query query) {
        this.q = query;
    }

    /**
     * Needed for cloning subclasses
     */
    public EasyQuery() {
    }

    /**
     * 
     * @param set
     * @param value
     * @param id
     * @return rowcount
     */
    @Override
    public int update(String set, String value, String id) {
        String[] where = {"id", id, ""};
        String[] what = {set, value, "'"};
        return q.update(what, where);
    }

    /**
     * 
     * example: "*", name , anti43 , false 
     * 
     * @param what
     * @param from
     * @param where
     * @param id
     * @return
     */
    @Override
    public String[][] select(String what, String from, String where, boolean id) {
        String hk = "'";
        if (id) {
            hk = "";

            if (from.equals("id") && where.equals("0")) {

                Popup.error(from + " = " + where, "Abfrage nicht möglich.");
                return null;
            }

        }

        String[] wher = {from, where, hk};
        if (from == null) {
            wher = null;
        }

        return q.select(what, wher, false);
    }

    /**
     * 
     * example: "*", name , anti43 , false 
     * 
     * @param what
     * @param from
     * @param where
     * @param id
     * @return
     */
    public String[] selectFirst(String what, String from, String where, boolean id) {
        String hk = "'";
        if (id) {
            hk = "";
            if (from.equals("id") && where.equals("0")) {

                Popup.error(from + " = " + where, "Abfrage nicht möglich.");
                return null;
            }
        }

        String[] wher = {from, where, hk};

        return q.selectFirst(what, wher);
    }

    /**
     * Example: "*", "Name", "anti43", "Name", true
     * will return everyone who`s name is like "anti43" sortet by name.
     * eg. anti43, anti43web
     * 
     * @param what
     * @param from
     * @param where
     * @param order
     * @param like
     * @return A multidimensional string-array containing the data found
     */
    public String[][] select(String what, String from, String where, String order, boolean like) {
        String hk = "'";

        String[] wher = {from, where, hk};
        if (from == null) {
            wher = null;
        }

        return q.select(what, wher, order, like, false, false);
    }

    /**
     * 
     * example: "*", name , anti43 , false 
     * 
     * @param what
     * @param from
     * @param where
     * @param id
     * @return
     * @throws Exception 
     */
    public String[] selectLast(String what, String from, String where, boolean id) throws Exception {
        String hk = "'";
        if (id) {
            hk = "";
            if (from.equals("id") && where.equals("0")) {
                throw new Exception(from + " = " + where + " Abfrage nicht möglich.");
            }
        }

        String[] wher = {from, where, hk};

        return q.selectLast(what, wher);
    }

    @Override
    public int insert(String set, String value, int[] unique) {
        String[] str = {set, value, ""};
        return q.insert(str, unique);
    }

    @Override
    /**
     * Deletes the item with the given id
     * @param id
     * @return 1 if successfull
     */
    public int delete(Integer id) {
        String[] where = {"id", id.toString(), ""};
        return q.delete(where);
    }

    /**
     * Deactivates the item with the given id
     * @param id
     * @return
     */
    public int deactivate(String id) {

        String[] where = {"id", id, ""};
        String[] what = {"deleted", "1", ""};
        return q.update(what, where);
    }

    /**
     * 
     * @param what
     * @return The next number of the given int coulumn name, e.g. 'id'
     */
    public Integer getNextNumber(String what) {
        return q.getNextIndexOfIntCol(what);
    }

    /**
     * 
     * @param what
     * @return The next number of the given String coulumn name, e.g. 'nummer'
     */
    public String getNextStringNumber(String what) {
        return q.getNextStringNumber(what);
    }

    public void setQueryHandler(Query query) {
        q = query;
    }

    public Query getQueryHandler() {
        return q;
    }

    /**
     * Enables a disabled item
     * @param id
     * @return 1 if successfull
     */
    public int unDelete(Integer id) {
        String[] where = {"id", id.toString(), ""};
        String[] what = {"deleted", "0"};
        return q.update(what, where);
    }

    /**
     * 
     * @param set
     * @param value
     * @param from
     * @param where
     * @return
     */
    public int update(String set, String value, String from, String where) {
        String[] wher = {from, where, "'"};
        String[] what = {set, value, "'"};
        return q.update(what, wher);
    }

    /**
     * 
     * @param what
     * @param from
     * @param where
     * @param id
     * @param ghosts
     * @return
     */
    public String[][] select(String what, String from, String where, boolean id, boolean ghosts) {
        String hk = "'";
        String[] wher = null;
        if (id) {
            hk = "";
        }

        if ((from != null) && (where != null)) {
            wher = new String[]{from, where, hk};
        }

        return q.select(what, wher, ghosts);
    }

    /**
     * 
     * 
     * @param query 
     */
    public void freeQuery(String query) {
        q.freeQuery(query);
    }

    public Integer getNextIndex(String of) {
        return q.getNextIndexOfStringCol(of);
    }

    /**
     * Example: "*", "Name", "anti43", "Name", true
     * will return everyone who`s name is lika "anti43" sortet by name.
     * eg. anti43, anti43w, andre
     * 
     * @param what
     * @param from
     * @param where
     * @param order
     * @param like
     * @param integer 
     * @param ghosts 
     * @return A multidimensional string-array containing the data found
     */
    public String[][] select(String what, String from, String where, String order, boolean like, boolean integer, boolean ghosts) {
        String hk = "'";

        String[] wher = {from, where, hk};
        if (from == null) {
            wher = null;
        }

        return q.select(what, wher, order, like, integer, ghosts);
    }

    /**
     * Example: "*", "Name", "anti43", "Name", true
     * will return everyone who`s name is like "anti43" sortet by name.
     * eg. anti43, anti43w
     * 
     * @param what
     * @param from
     * @param where
     * @param order
     * @param like
     * @param additionalTable 
     * @param addTableKey 
     * @return A multidimensional string-array containing the data found
     */
    public String[][] select(String what, String from, String where, String order, boolean like, String additionalTable, String addTableKey) {
        String hk = "'";

        String[] wher = {from, where, hk};
        if (from == null) {
            wher = null;
        }

        return q.select(what, wher, additionalTable, addTableKey, order, like);
    }
}
