
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
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import org.jdesktop.application.SessionStorage;

/**
 *
 * @author anti
 */
public final class TableViewPersistenceHandler {

    private List<TableColumnModelListener> listeners = new ArrayList<TableColumnModelListener>();
    private final Component identifier;
    private final JTable target;
    private SessionStorage storage;
    private final TableColumnModelListener cListener;
    private final String saveFile;
    private final PropertyChangeListener wListener;
    private final MouseAdapter mListener;

    /**
     * 
     * @param target
     * @param identifier
     */
    public TableViewPersistenceHandler(final MPTable target, final Component identifier) {
        this(target, identifier, false);
    }

    /**
     *
     * @param target
     * @param identifier
     * @param passive  
     */
    public TableViewPersistenceHandler(final MPTable target, final Component identifier, final boolean passive) {
        if (User.getCurrentUser().__getCname() == null || User.getCurrentUser().__getCname().length() == 0) {
            throw new IllegalStateException("The username is not set.");
        }
        if (target.getName() == null || target.getName().length() == 0) {
            throw new IllegalStateException("The table name is not set.");
        }
        if (identifier == null  ) {
            throw new IllegalStateException("The identifier is not set: " + identifier);
        }
        if (identifier.getName() == null || identifier.getName().length() == 0) {
            throw new IllegalStateException("The identifier name is not set: " + identifier);
        }

        saveFile = User.getCurrentUser().__getCname() + "_" + target.getName() + "_" + identifier.getName() + ".xml";
        mListener = new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent evt) {
                persist();
            }
        };
        wListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
//                if (evt.getPropertyName().equals("model")) {
//                    // Log.Debug(this, evt);
//                    setObserveColumnState(true);
//                }
//
////                Log.Debug(this, evt.getPropertyName());
//
//                if (!passive && isObserveColumnState() && evt.getPropertyName().equals("preferredWidth")) {
//                    // Log.Debug(this, evt);
//                    persist();
//                }
            }
        };
        cListener = new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent e) {
            }

            public void columnRemoved(TableColumnModelEvent e) {
            }

            public void columnMoved(TableColumnModelEvent e) {
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
//        try {
//            Log.Debug(this, "Setting restore listeners for table: " + target.getName());
//            res = restore();
//        } catch (Exception ex) {
//            Log.Debug(this, ex);
//            res = false;
////            User.getCurrentUser().getLayoutProperties().remove(target.getName() + "@" + identifier.getName());
////            target.createDefaultColumnsFromModel();
////            persist();
//        }
        target.getColumnModel().addColumnModelListener(cListener);
        target.getTableHeader().addMouseListener(mListener);
        target.addPropertyChangeListener(wListener);
        Enumeration<TableColumn> cols = target.getColumnModel().getColumns();
        while (cols.hasMoreElements()) {
            cols.nextElement().addPropertyChangeListener(wListener);
        }
        return true;
    }

    protected boolean restore() {
        if (hasData()) {
            Log.Debug(this, "Trying to restore table: " + target.getName());
            try {
                getStorage().restoreSingle(target, saveFile);
            } catch (IOException ex) {
                Log.Debug(ex);
                return false;
            } 
            return true;
        } else {
            Log.Debug(this, "Cannot restore table: " + target.getName());
            return false;
        }
    }

    private void persist() {
        try {
            getStorage().saveSingle(target, saveFile);
            Log.Debug(this, "Persisting table state: " + target);
        } catch (IOException ex) {
            Log.Debug(ex);
        }
    }

    protected boolean hasData() {
        File x = new File(FileDirectoryHandler.getTempDir() + saveFile);
        return x.exists() && x.canWrite() && x.canRead();
    }

    /**
     * @return the storage
     */
    public SessionStorage getStorage() {
        if(storage==null){
            storage = mpv5.YabsApplication.getApplication().getContext().getSessionStorage();
        }
        return storage;
    }
}
