package mpv5.ui.dialogs;

import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.HistoryItem;
import mpv5.globals.Messages;
import mpv5.ui.frames.MPView;

/**
 *
 * Use this class to pass messages to the user
 * @author anti
 */
public class Notificator {

    /**
     * Raise a notification to the user
     * @param message
     */
    public static void raiseNotification(final Object message) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Popup.notice(message);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Raise a notification to the user
     * @param message
     * @param popup 
     */
    public static void raiseNotification(Object message, boolean popup) {
        if (popup) {
            raiseNotification(message);
        } else {
            if (message != null) {
                mpv5.YabsViewProxy.instance().addMessage(message.toString());
            }
        }
    }

    /**
     * Raise a notification to the user
     * @param message
     * @param popup
     * @param log write to log
     * @param source  
     */
    public static void raiseNotification(final Object message, boolean popup, boolean log, DatabaseObject source) {
        if (popup) {
            raiseNotification(message);
        } else {
            if (message != null) {
                mpv5.YabsViewProxy.instance().addMessage(message.toString());
            }
        }

        if (log) {
            final String fdbid = Messages.NOTIFICATION.getValue();
            final int fids = source.__getIDS();
            final int fgids = source.__getGroupsids();
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(message.toString(), mpv5.db.objects.User.getCurrentUser().__getCName(), fdbid, fids, fgids);
                }
            };
            new Thread(runnable).start();
        }
    }
}
