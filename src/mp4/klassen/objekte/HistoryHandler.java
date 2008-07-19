

package mp4.klassen.objekte;

import mp3.classes.layer.QueryClass;

/**
 *
 * @author anti43
 */
public class HistoryHandler {
    private static HistoryHandler handler;
    private static HistoryItem history;
    
    /**
     * validates the history-data by reloading it.
     */
    public static void validate(){
    
        history = new HistoryItem(QueryClass.instanceOf());
    }
    /**
     * HistoryHandler is a singleton
     * @return the instance of HH
     */
    public static HistoryHandler instanceOf(){
    
        if (handler == null) {
            handler = new HistoryHandler();
        }
    
        return handler;
    }

    /**
     * 
     * @return the History as String Array
     */
    public static String[][] getHistory() {
        validate();
        return history.getHistory();
    }

}
