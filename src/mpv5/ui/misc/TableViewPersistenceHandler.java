
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

//~--- non-JDK imports --------------------------------------------------------
import com.l2fprod.common.demo.Main;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import mpv5.db.objects.User;

import mpv5.ui.misc.MPTable;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
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
    private final PropertyChangeListener wListener;
    private boolean observeColumnState = true;

    /**
     *
     * @param target
     * @param identifier
     */
    public TableViewPersistenceHandler(final MPTable target, final JComponent identifier) {

        saveFile = User.getCurrentUser().__getCName() + "_" + target.getName() + "_" + identifier.getName() + ".xml";
        //Log.Print(saveFile);
        storage = mpv5.YabsApplication.getApplication().getContext().getSessionStorage();
        wListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (isObserveColumnState() && evt.getPropertyName().equals("preferredWidth")) {
//                    Log.Debug(this, evt);
                    //  persist();
                }
            }
        };
        cListener = new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
                if (isObserveColumnState()) {
//                    Log.Debug(this, e);
                    //  persist();
                }
            }

            public void columnMarginChanged(ChangeEvent e) {
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        };
        this.target = target;
        this.identifier = identifier;

        //        target.setColumnModel(new MPColumnModel());
        //
        //        try {
        //            restore();
        //        } catch (IOException ex) {
        //            Log.Debug(ex);
        //            persist();
        //        }
        //
        set();
    }

    /**
     * Set a listener
     */
    protected boolean set() {

        boolean res = true;
        try {
            Log.Debug(this, "Setting restore listeners for table: " + target.getName());
            res = restore();
        } catch (Exception ex) {
            Log.Debug(this, ex);
            res = false;
//            User.getCurrentUser().getLayoutProperties().remove(target.getName() + "@" + identifier.getName());
//            target.createDefaultColumnsFromModel();
//            persist();
        }
        target.getColumnModel().addColumnModelListener(cListener);
        Enumeration<TableColumn> cols = target.getColumnModel().getColumns();
        while (cols.hasMoreElements()) {
            cols.nextElement().addPropertyChangeListener(wListener);
        }
        return res;
    }

    private boolean restore() throws IOException {
        if (hasData()) {
            Log.Debug(this, "Trying to restore table: " + target.getName());
            storage.restoreSingle(target, saveFile);
            return true;
        } else {
            Log.Debug(this, "Cannot restore table: " + target.getName());
            return false;
        }
    }

    private void persist() {
        try {
            storage.saveSingle(target, saveFile);
            //  Log.Print(saveFile);
        } catch (IOException ex) {
            Log.Debug(ex);
        }
    }

    protected boolean hasData() {
        File x = new File(FileDirectoryHandler.getTempDir() + saveFile);
        return x.exists() && x.canWrite() && x.canRead();
    }

    /**
     * @return the observeColumnState
     */
    protected boolean isObserveColumnState() {
        return observeColumnState;
    }

    /**
     * @param observeColumnState the observeColumnState to set
     */
    protected void setObserveColumnState(boolean observeColumnState) {
        this.observeColumnState = observeColumnState;
    }
}
