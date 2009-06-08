/*
 *  This file is part of MP.
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
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

/**
 *
 *  
 */
public class MPTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;
    private Class[] types;
    private boolean[] canEdits;

    public MPTableModel() {
        super();
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});
    }

    public MPTableModel(Object[][] data) {
        super();

        if (data.length > 0) {
            String[] header = new String[data[0].length];
            for (int i = 0; i < header.length; i++) {
                header[i] = String.valueOf(i);
            }
            setDataVector(data, header);
        }

        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});
    }

    public MPTableModel(ArrayList<DatabaseObject> list, String[] header) {
        super();
        Object[][] data = new Object[0][0];
        if (list.size() > 0 && list.get(0) != null) {
            data = new Object[list.size()][list.get(0).getValues().size()];
        }
        ArrayList<String[]> sdata;



        for (int i = 0; i < list.size(); i++) {
            DatabaseObject databaseObject = list.get(i);
            sdata = databaseObject.getValues();
            for (int j = 0; j < sdata.size(); j++) {
                String[] strings = sdata.get(j);
                data[i][j] = strings[1];
            }
        }

        setDataVector(data, header);

        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});

    }

    public MPTableModel(Object[][] datstr, String[] header) {
        super(datstr, header);
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});

    }

    public MPTableModel(Object[][] datstr, String[] header, Class[] types) {
        super(datstr, header);
        setEditable(false);
        setTypes(types);
    }
    public MPTableModel(Class[] types, boolean[] canEdits, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        setTypes(types);
        setCanEdits(canEdits);
    }

     public MPTableModel(Class[] types, boolean[] canEdits, Object[] columnNames) {
        super(columnNames,1);
        setTypes(types);
        setCanEdits(canEdits);
    }

    public MPTableModel(Class[] types, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        setTypes(types);
        setEditable(false);
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        getTypes()[columnIndex].toString();//Check for non-null
        return getTypes()[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        new Boolean(getCanEdits()[columnIndex]).toString();//Check for non-null
        return getCanEdits()[columnIndex];
    }

    public Class[] getTypes() {
        return types;
    }

    public void setTypes(Class[] types) {
        this.types = types;
    }

    public boolean[] getCanEdits() {
        return canEdits;
    }

    public void setCanEdits(boolean[] canEdits) {
        this.canEdits = canEdits;
    }

    public Vector getColumnIdentifiers() {
        return columnIdentifiers;
    }

    private void setEditable(boolean b) {
        setCanEdits(new boolean[]{b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b,
                    b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b,
                    b, b, b, b, b, b, b, b, b});
    }
}
