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
package mpv5.ui.misc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import mpv5.logging.Log;

/**
 *
 * @author anti
 */
public class TableViewPersistenceHandler {

    /**
     * 
     * @param target
     * @param identifier
     * @param index
     */
    public TableViewPersistenceHandler(JTable target, JComponent identifier, int index) {

        Enumeration<TableColumn> cols = target.getColumnModel().getColumns();
        while (cols.hasMoreElements()){

            TableColumn col = cols.nextElement();
            col.addPropertyChangeListener(new ColumnListener(target, identifier, index));
        }
    }

    class ColumnListener implements PropertyChangeListener {

        private JTable table;
        private final JComponent identifier;
        private final int index;
        private final JTable target;

        private ColumnListener(JTable target, JComponent identifier, int index) {
            this.identifier = identifier;
            this.index = index;
            this.target = target;

        }

        public void propertyChange(PropertyChangeEvent e) {

            Log.Debug(this, e.getPropertyName());
            if (e.getPropertyName().equals("columWidth")) {
                TableColumn tableColumn = (TableColumn) e.getSource();
                int index = table.getColumnModel().getColumnIndex(tableColumn.getHeaderValue());

                TableColumnModel columnModel = table.getColumnModel();
                Enumeration<TableColumn> cols = columnModel.getColumns();
                while (cols.hasMoreElements()) {
                    TableColumn column = cols.nextElement();
                    Integer modelIndex = new Integer(column.getModelIndex());

                    Log.Debug(modelIndex, column.getPreferredWidth());
                }
            }
        }
    }
}
