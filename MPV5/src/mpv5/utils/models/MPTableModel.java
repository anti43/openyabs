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

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Andreas
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

    public MPTableModel(Object[][] datstr, String[] header) {
        super(datstr, header);
        setEditable(false);

        setTypes(new Class[]{Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class,
                    Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class, Object.class});

    }

    public MPTableModel(Class[] types, boolean[] canEdits, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
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
        return getTypes()[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
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
