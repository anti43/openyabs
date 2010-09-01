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
package mpv5.utils.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.logging.Log;

/**
 *This class represents rows in a {@link MPTableModel}
 */
public class MPTableModelRow {

    /**
     * Converts the given list into MPTableModelRows.
     * @param list 
     * @return
     */
    public static MPTableModelRow[] toRows(List<DatabaseObject> list) {
        MPTableModelRow[] rows = new MPTableModelRow[list.size()];

        for (int i = 0; i < list.size(); i++) {
            DatabaseObject databaseObject = list.get(i);
            rows[i] =new MPTableModelRow(databaseObject);
            List<Object[]> sdata = databaseObject.getValues2();
            for (int j = 0; j < sdata.size(); j++) {
                Object[] strings = sdata.get(j);
                rows[i].setValueAt(strings[1], j);
            }
        }
        return rows;
    }

    /**
     * Converts the given array into MPTableModelRows. The first column of the array must be an int value as it is used for the IDS field of the row.
     * @param context
     * @param object
     * @return
     */
    public static MPTableModelRow[] toRows(Context context, Object[][] object) {
        MPTableModelRow[] rows = new MPTableModelRow[object.length];
        for (int i = 0; i < rows.length; i++) {
            if (Integer.valueOf(object[i][0].toString()).intValue() > 0) {
                try {
                    rows[i] = new MPTableModelRow(DatabaseObject.getObject(context, Integer.valueOf(object[i][0].toString()).intValue()));
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                    rows[i] = new MPTableModelRow(DatabaseObject.getObject(context));
                }
            } else {
                rows[i] = new MPTableModelRow(DatabaseObject.getObject(context));
            }
            for (int j = 1; j < object[i].length; j++) {
                rows[i].setValueAt(object[i][j], j - 1);
            }
        }
        return rows;
    }
    private Context context;
    private int ids;
    private HashMap<Integer, Object> values = new HashMap<Integer, Object>();
    private final DatabaseObject obj;

    public MPTableModelRow(final DatabaseObject obj) {
        this.obj = obj;
        this.context = obj.getContext();
        this.ids = obj.__getIDS();
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return the ids
     */
    public int getIds() {
        return ids;
    }

    /**
     * @return the obj
     */
    public DatabaseObject getObj() {
        return obj;
    }

    /**
     * @return the values
     */
    public HashMap<Integer, Object> getValues() {
        return values;
    }

    /**
     * Returns the value at the given column
     * @param column
     * @return
     */
    public Object getValueAt(int column) {
        return values.get(new Integer(column));
    }

    /**
     * Set or replace the value at the given column
     * @param value
     * @param column
     */
    public void setValueAt(Object value, int column) {
        values.put(new Integer(column), value);
    }
}
