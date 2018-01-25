package mpv5.ui.dialogs;

import ag.ion.bion.officelayer.runtime.IOfficeProgressMonitor;
import javax.swing.SwingUtilities;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.HistoryItem;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *
 * Use this class to pass messages to the user
 *
 * @author anti
 */
public class Notificator {


    /**
     * Raise a notification to the user
     *
     * @param message
     */
    public static void raiseNotification(final Object message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Popup.notice(message);
                if(message instanceof Throwable){
                    Log.Debug((Throwable)message);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Raise a notification to the user
     *
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
     *
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
                    QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(message.toString(), mpv5.db.objects.User.getCurrentUser().__getCname(), fdbid, fids, fgids);
                }
            };
            new Thread(runnable).start();
        }
    }

    public static synchronized IOfficeProgressMonitor getOfficeMonitor() {
        return new IOfficeProgressMonitor() {
                private boolean c;
                private int ix;

                public void beginTask(String string, int i) {
                    Log.Debug(IOfficeProgressMonitor.class, string + " - " + i);
                    YabsViewProxy.instance().addMessage(string);
                    YabsViewProxy.instance().setProgressMaximumValue(i);
                }

                public void worked(final int i) {
                    ix+=i;
//                    Log.Debug(IOfficeProgressMonitor.class, "worked - " + ix);
                    Runnable runnable = new Runnable() {
                        public void run() {
                            YabsViewProxy.instance().setProgressValue(ix);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }

                public void beginSubTask(String string) {
//                    Log.Debug(IOfficeProgressMonitor.class, string);
                }

                public boolean needsDone() {
                    return true;
                }

                public void done() {
                    Log.Debug(IOfficeProgressMonitor.class, "done");
                    YabsViewProxy.instance().setProgressReset();
                }

                public void setCanceled(boolean bln) {
                    c = bln;
                    Log.Debug(IOfficeProgressMonitor.class, "canceled");
                    YabsViewProxy.instance().setProgressReset();
                }

                public boolean isCanceled() {
                    return c;
                }
            };
    }
}
