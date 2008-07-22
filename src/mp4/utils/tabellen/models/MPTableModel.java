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
package mp4.utils.tabellen.models;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Andreas
 */
class MPTableModel extends DefaultTableModel {

    private Class[] types;
    private boolean[] canEdits;

    public MPTableModel(Class[] types, boolean[] canEdits, Object[][] data, Object[] columnNames) {
        super(data, columnNames);
        setTypes(types);
        setCanEdits(canEdits);
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
}
