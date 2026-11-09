/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.toolbars;

import java.awt.Color;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ExportablePanel;

/**
 * The actions on the record shown in a view (save, delete, print ...), used
 * by the classic toolbar ({@link DataPanelTB}) and the command bar of the
 * modern views ({@link ModernCommandBar}), so both behave the same.
 */
public final class RecordActions {

    private RecordActions() {
    }

    /**
     * Saves the record of the view
     *
     * @param view
     */
    public static void save(DataPanel view) {
        if (isPendingCopy(view)) {
            saveCopy(view);
            return;
        }
        try {
            DatabaseObject dato = view.getDataOwner();
            view.actionBeforeSave();
            if (dato.getPanelData(view) && dato.save()) {
                try {
                    view.actionAfterSave();
                    view.setDataOwner(dato, true);
                } catch (Exception e) {
                    Log.Debug(RecordActions.class, e);
                }
            } else {
                view.showRequiredFields();
            }
        } catch (ChangeNotApprovedException ex) {
            Log.Debug(RecordActions.class, ex.getMessage());
        }
    }

    /**
     * Saves the data of the view as a new record
     *
     * @param view
     */
    public static void saveAsNew(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        boolean d = dato.getPanelData(view);
        dato.setIDS(-1);
        view.actionBeforeCreate();
        if (d && dato.save()) {
            try {
                view.actionAfterCreate();
                view.setDataOwner(dato, true);
            } catch (Exception e) {
                Log.Debug(RecordActions.class, e);
            }
        } else {
            view.showRequiredFields();
        }
    }

    private static final String COPY_OF = "yabs.copyOf";

    /**
     * Turns the shown record into a copy: a new record, not saved yet. Saving
     * creates it, {@link #revert(DataPanel)} shows the original again.
     *
     * @param view
     * @return true if the view shows a copy now
     */
    public static boolean createCopy(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (dato == null || !dato.isExisting() || !(view instanceof javax.swing.JComponent)) {
            return false;
        }
        //the fields as shown (also not yet saved changes) go into the copy
        if (!dato.getPanelData(view)) {
            view.showRequiredFields();
            return false;
        }
        int original = dato.__getIDS();
        String name = dato.__getCname();
        dato.setIDS(-1);
        view.actionBeforeCreate();
        ((javax.swing.JComponent) view).putClientProperty(COPY_OF, new Object[]{original, name, dato});
        return true;
    }

    /**
     * @param view
     * @return true if the view shows a copy which is not saved yet
     */
    public static boolean isPendingCopy(DataPanel view) {
        if (!(view instanceof javax.swing.JComponent)) {
            return false;
        }
        Object o = ((javax.swing.JComponent) view).getClientProperty(COPY_OF);
        if (o != null && (view.getDataOwner() == null || view.getDataOwner().isExisting()
                || view.getDataOwner() != ((Object[]) o)[2])) {
            //saved meanwhile (e.g. by adding a file), or another record is shown
            ((javax.swing.JComponent) view).putClientProperty(COPY_OF, null);
            return false;
        }
        return o != null;
    }

    /**
     * @param view
     * @return the name of the original of a pending copy, else null
     */
    public static String copyOf(DataPanel view) {
        if (!isPendingCopy(view)) {
            return null;
        }
        return String.valueOf(((Object[]) ((javax.swing.JComponent) view).getClientProperty(COPY_OF))[1]);
    }

    private static void saveCopy(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (dato.getPanelData(view) && dato.save()) {
            ((javax.swing.JComponent) view).putClientProperty(COPY_OF, null);
            try {
                view.actionAfterCreate();
                view.setDataOwner(dato, true);
            } catch (Exception e) {
                Log.Debug(RecordActions.class, e);
            }
        } else {
            view.showRequiredFields();
        }
    }

    /**
     * Discards the changes: shows the saved record again
     *
     * @param view
     */
    public static void revert(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (isPendingCopy(view)) {
            //discard the copy: the original again
            Object[] o = (Object[]) ((javax.swing.JComponent) view).getClientProperty(COPY_OF);
            ((javax.swing.JComponent) view).putClientProperty(COPY_OF, null);
            try {
                view.setDataOwner(DatabaseObject.getObject(dato.getContext(), (Integer) o[0]), true);
            } catch (Exception e) {
                Log.Debug(RecordActions.class, e);
            }
            return;
        }
        if (dato.isExisting()) {
            dato.getPanelData(view);
            dato.reset();
            view.setDataOwner(dato, true);
        }
    }

    /**
     * Deletes the record after asking
     *
     * @param view
     */
    public static void delete(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        //lists what is deleted with the record before asking
        if (dato.isExisting() && mpv5.ui.dialogs.DeleteConfirmDialog.confirm(
                view instanceof java.awt.Component ? (java.awt.Component) view : null, dato)) {
            dato.getPanelData(view);
            dato.delete();
            view.refresh();
            view.setDataOwner(dato.getObject(dato.getContext()), true);
        }
    }

    /**
     * Locks the record for the current user
     *
     * @param view
     * @return true if locked
     */
    public static boolean lock(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (User.getCurrentUser().isDefault()) {
            Popup.notice(Messages.DEFAULT_USER);
        } else if (dato.isExisting()) {
            if (dato.lock()) {
                Popup.notice(dato.toString() + Messages.LOCKED);
                return true;
            } else {
                Popup.notice(Messages.LOCK_FAILED);
            }
        } else {
            Popup.notice(Messages.NOT_SAVED_YET);
        }
        return false;
    }

    /**
     * Prints the record and notes it in the history
     *
     * @param view
     */
    public static void print(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (dato.isExisting()) {
            view.print();
            final String fmessage = Messages.PRINTED + dato.__getCname();
            final String fdbid = dato.getDbIdentity();
            final int fids = dato.__getIDS();
            final int fgids = dato.__getGroupsids();
            new Thread(() -> QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(
                    fmessage, User.getCurrentUser().__getCname(), fdbid, fids, fgids)).start();
        }
    }

    /**
     * @param view
     */
    public static void mail(DataPanel view) {
        if (view.getDataOwner().isExisting()) {
            view.mail();
        }
    }

    /**
     * @param view
     */
    public static void pdf(DataPanel view) {
        if (view.getDataOwner().isExisting() && view instanceof ExportablePanel) {
            ((ExportablePanel) view).pdf();
        }
    }

    /**
     * @param view
     */
    public static void odt(DataPanel view) {
        if (view.getDataOwner().isExisting() && view instanceof ExportablePanel) {
            ((ExportablePanel) view).odt();
        }
    }

    /**
     * Adds the record to the temporary list (clipboard of YaBS)
     *
     * @param view
     */
    public static void copyToClipboard(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (dato.isExisting()) {
            mpv5.YabsViewProxy.instance().addToClipBoard(dato);
        }
    }

    /**
     * Pastes the records of the temporary list into the view
     *
     * @param view
     */
    public static void paste(DataPanel view) {
        DatabaseObject[] items = mpv5.YabsViewProxy.instance().getClipboardItems();
        if (items != null && items.length > 0) {
            view.paste(items);
        }
    }

    /**
     * Saves the record and adds it to the favourites
     *
     * @param view
     */
    public static void addFavourite(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (User.getCurrentUser().isDefault()) {
            Popup.notice(Messages.DEFAULT_USER);
        } else if (dato.isExisting()) {
            dato.getPanelData(view);
            if (dato.save()) {
                new Favourite(dato).save();
                mpv5.YabsViewProxy.instance().reloadFavorites();
                mpv5.YabsViewProxy.instance().addMessage(Messages.ADDED_TO_FAVOURITES + dato.__getCname(), Color.GREEN);
                view.setDataOwner(dato, true);
            } else {
                view.showRequiredFields();
            }
        }
    }

    /**
     * Saves the record and removes it from the favourites
     *
     * @param view
     */
    public static void removeFavourite(DataPanel view) {
        DatabaseObject dato = view.getDataOwner();
        if (!User.getCurrentUser().isDefault() && dato.isExisting()) {
            dato.getPanelData(view);
            if (dato.save()) {
                Favourite.removeFavourite(dato);
                mpv5.YabsViewProxy.instance().reloadFavorites();
                view.setDataOwner(dato, true);
            } else {
                view.showRequiredFields();
            }
        }
    }
}
