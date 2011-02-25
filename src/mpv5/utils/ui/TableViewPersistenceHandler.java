
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
package mpv5.utils.ui;

//~--- non-JDK imports --------------------------------------------------------
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.objects.User;

import mpv5.ui.misc.MPTable;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import mpv5.logging.Log;
import org.jdesktop.application.SessionStorage;

/**
 *
 * @author anti
 */
public final class TableViewPersistenceHandler {

    private List<TableColumnModelListener> listeners = new ArrayList<TableColumnModelListener>();
    private final JComponent identifier;
    private final JTable target;
    private final SessionStorage storage;
    private final TableColumnModelListener cListener;
    private final String saveFile;

    /**
     *
     * @param target
     * @param identifier
     */
    public TableViewPersistenceHandler(final JTable target, final JComponent identifier) {

        saveFile = User.getCurrentUser().__getCName() + "_" + target.getName()+ "_" + identifier.getName() + ".xml";
        Log.Print(saveFile);
        storage = mpv5.YabsApplication.getApplication().getContext().getSessionStorage();
        cListener = new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
                Log.Print(e);
                persist();
            }

            public void columnMarginChanged(ChangeEvent e) {
                Log.Print(e);
                persist();
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        };
        this.target = target;
        this.identifier = identifier;
//        target.setColumnModel(new MPColumnModel());

        try {
            restore();
        } catch (IOException ex) {
            persist();
            Log.Debug(ex);
        }

        set();
    }

    /**
     * Set a listener
     */
    public void set() {

        try {
            restore();
        } catch (Exception ex) {
          Log.Debug(this, ex);
//            User.getCurrentUser().getLayoutProperties().remove(target.getName() + "@" + identifier.getName());
            target.createDefaultColumnsFromModel();
            persist();
        }
        remove();
        target.getColumnModel().addColumnModelListener(cListener);
    }

    /**
     * Remove the listener again
     */
    public void remove() {
        target.getColumnModel().removeColumnModelListener(cListener);
    }

    private void restore() throws IOException {
        storage.restore(target, saveFile);
    }

    private void persist() {
        try {
            storage.save(target, saveFile);
            Log.Print(saveFile);
        } catch (IOException ex) {
            Log.Debug(ex);
        }
    }
}
