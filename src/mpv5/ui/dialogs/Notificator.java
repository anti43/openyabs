package mpv5.ui.dialogs;

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
    public static void raiseNotification(Object message) {
        Popup.notice(message);
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
                MPView.addMessage(message.toString());
            }
        }
    }
}
