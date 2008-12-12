/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.db.common;

/**
 *
 * @author Andreas
 */
public class DatabaseSearch {
    private Context context;

    public DatabaseSearch(Context context){
        this.context = context;
    }

    public DatabaseSearch(DatabaseObject obj) {
        this.context = new Context(obj);
        context.setDbIdentity(obj.getDbID());
        context.setSubID(Context.DEFAULT_SUBID);
    }

    public Object[][] getValuesFor(String resultingFieldNames, String what, String where) {
       return QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{what, where, "'"});
    }

    public Object[][] getValuesFor(String resultingFieldNames, String what, String where, boolean searchForLike) {
        return QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{what, where, "'"}, null, searchForLike);
    }

    public Object[] searchFor(String what, String needle){
       return QueryHandler.instanceOf().clone(context).getValuesFor(what,needle);
    }
}
