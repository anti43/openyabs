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
package mpv5.utils.models;

import java.util.ArrayList;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Headers;

/**
 *
 * @author anti
 */
public class ImportModel extends MPTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param objs
     * @return
     */
    public static ImportModel getModel(ArrayList<ArrayList<DatabaseObject>> objs) {
        ArrayList<DatabaseObject> l = new ArrayList<DatabaseObject>();

        for (int i = 0; i < objs.size(); i++) {
            ArrayList<DatabaseObject> arrayList = objs.get(i);
            for (int j = 0; j < arrayList.size(); j++) {
                DatabaseObject databaseObject = arrayList.get(j);
                l.add(databaseObject);
            }
        }
        return new ImportModel(l);
    }

    /**
     *
     * @param list
     */
    public ImportModel(ArrayList<DatabaseObject> list) {
        super();

        Object[][] data = new Object[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            DatabaseObject databaseObject = list.get(i);
            ArrayList<String[]> t = databaseObject.getValues();

            String sdata = "";
            for (int j = 0; j < t.size(); j++) {
                String[] strings = t.get(j);
                sdata += strings[0] + ": " + strings[1] + "  ";
            }
            data[i][0] = databaseObject;
            data[i][1] = true;
            data[i][2] = databaseObject.getDbIdentity();
            data[i][3] = databaseObject.__getCName();
            data[i][4] = sdata;
        }

        setCanEdits(new boolean[]{false, true, false, false, false, false});
        setTypes(new Class[]{Object.class, Boolean.class, Object.class, Object.class, Object.class, Object.class});
        setDataVector(data, Headers.IMPORT);

    }
}
