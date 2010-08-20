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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.logging.Log;
import mpv5.utils.ui.ComponentStateManager;

/**
 *
 * @author anti
 */
public class TableViewPersistenceHandler {

    private final ColumnListener t;
    private final JTable target;

    /**
     * 
     * @param target
     * @param identifier
     */
    public TableViewPersistenceHandler(JTable target, JComponent identifier) {
        t = new ColumnListener(target, identifier);
        this.target = target;

        try {
            ComponentStateManager.reload(User.getCurrentUser().getLayoutProperties().get(target + "@" + identifier), target);
        } catch (Exception ex) {
            //not set
        }
    }

    /**
     * Set a listener
     */
    public void set() {
        target.getColumnModel().addColumnModelListener(t);
    }

    /**
     * Remove the listener again
     */
    public void remove() {
        target.getColumnModel().removeColumnModelListener(t);
    }

    class ColumnListener implements TableColumnModelListener {

        private JTable table;
        private final JComponent identifier;
        private String layout;

        private ColumnListener(JTable target, JComponent identifier) {
            this.identifier = identifier;
            this.table = target;

        }

        public void columnAdded(TableColumnModelEvent e) {
            layout = ComponentStateManager.persist(table);
            User.getCurrentUser().getLayoutProperties().put(target.getName() + "@" + identifier.getName(), layout);
        }

        public void columnRemoved(TableColumnModelEvent e) {
            layout = ComponentStateManager.persist(table);
             User.getCurrentUser().getLayoutProperties().put(target.getName() + "@" + identifier.getName(), layout);
        }

        public void columnMoved(TableColumnModelEvent e) {
            layout = ComponentStateManager.persist(table);
             User.getCurrentUser().getLayoutProperties().put(target.getName() + "@" + identifier.getName(), layout);
        }

        public void columnMarginChanged(ChangeEvent e) {
            layout = ComponentStateManager.persist(table);
             User.getCurrentUser().getLayoutProperties().put(target.getName() + "@" + identifier.getName(), layout);
        }

        public void columnSelectionChanged(ListSelectionEvent e) {
        }

    }
}
