/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.db.common;

import java.util.ArrayList;

/**
 *
 * @author Andreas
 */
public class DatabaseSearch {
    private Context context;

    public DatabaseSearch(Context context){
        this.context = context;
    }

    public DatabaseSearch(String contextname) {
        this.context = new Context();
        context.setDbIdentity(contextname);
        context.setSubID(Context.DEFAULT_SUBID);
    }

    public ArrayList<String> searchFor(String needle){
       return QueryHandler.instanceOf().setContext(context).getValuesFor(needle);
    }
}
