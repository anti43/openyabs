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
import java.util.List;
import javax.swing.table.TableModel;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Headers;

/**
 *
 *  
 */
public class ImportTableModel extends MPTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param objs
     * @param selectOnlyNonExisting
     * @return
     */
    public static ImportTableModel getModel(ArrayList<ArrayList<DatabaseObject>> objs, boolean selectOnlyNonExisting) {
        ArrayList<DatabaseObject> l = new ArrayList<DatabaseObject>();

        for (int i = 0; i < objs.size(); i++) {
            ArrayList<DatabaseObject> arrayList = objs.get(i);
            for (int j = 0; j < arrayList.size(); j++) {
                DatabaseObject databaseObject = arrayList.get(j);
                l.add(databaseObject);
            }
        }
        return new ImportTableModel(l, selectOnlyNonExisting);
    }

    /**
     *
     * @param list
     * @param selectOnlyNonExisting
     */
    public ImportTableModel(ArrayList<DatabaseObject> list, boolean selectOnlyNonExisting) {
        super();

        Object[][] data = new Object[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            DatabaseObject databaseObject = list.get(i);
            List<String[]> t = databaseObject.getValues();

            String sdata = "";
            for (int j = 0; j < t.size(); j++) {
                String[] strings = t.get(j);
                sdata += strings[0] + ": " + strings[1] + "  ";
            }
            data[i][0] = databaseObject;
            if (selectOnlyNonExisting) {
                if (DatabaseObject.exists(databaseObject.getContext(), databaseObject.__getIDS())) {
                    data[i][1] = false;
                } else {
                    data[i][1] = true;
                }
            }
            data[i][2] = databaseObject.getDbIdentity();
            data[i][3] = databaseObject.__getCname();
            data[i][4] = sdata;
        }

        setCanEdits(new boolean[]{false, true, false, false, false, false});
        setTypes(new Class[]{Object.class, Boolean.class, Object.class, Object.class, Object.class, Object.class});
        setDataVector(data, Headers.IMPORT.getValue());

    }


}
