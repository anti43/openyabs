

package mpv5.ui.dialogs;

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
    public static void raiseNotification(Object message){
       Popup.notice(message);
    }
}
