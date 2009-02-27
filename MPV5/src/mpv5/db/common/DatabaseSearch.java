

package mpv5.db.common;

/**
 *
 * @author Andreas
 */
public class DatabaseSearch {
    private Context context;

    /**
     *
     * @param context
     */
    public DatabaseSearch(Context context){
        this.context = context;
    }

    /**
     *
     * @param obj The DBOBJ the search will rely on
     */
    public DatabaseSearch(DatabaseObject obj) {
        this.context = new Context(obj);
        context.setDbIdentity(obj.getDbIdentity());
        context.setSubID(Context.DEFAULT_SUBID);
    }

    /**
     * Get multiple values from a search
     * @param resultingFieldNames What do you like to get (columns)?
     * @param what Which column do you like to take for the condition?
     * @param where And what value should the column value have?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what, String where) {
       return QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{what, where, "'"});
    }

    /**
     * Get multiple values from a search
     * @param resultingFieldNames What do you like to get (columns)?
     * @param what Which column do you like to take for the condition?
     * @param where And what value should the column value have?
     * @param searchForLike Shall we search with "like" condition?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what, String where, boolean searchForLike) {
        return QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{what, where, "'"}, null, searchForLike);
    }

    /**
     * Get a single dimension list from a search after values from the column
     * where the value is LIKE the given needle
     * @param what Which column do you like to get and search through?
     * @param needle
     * @return
     */
    public Object[] searchFor(String what, String needle){
       return QueryHandler.instanceOf().clone(context).getValuesFor(what,needle);
    }
}
