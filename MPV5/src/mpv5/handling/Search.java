/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.handling;

import mpv5.db.Context;
import javax.swing.table.TableModel;

/**
 *
 * @author Administrator
 */
public class Search {

    public static final int NAMESEARCH = 0;
    public static final int NUMBERSEARCH = 1;
    public static final int DETAILSSEARCH = 2;
    public static final int CONTEXTSEARCH = 3;
    
    public Search(Context context, int searchtype) {
    }

    public TableModel getTableModel() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
