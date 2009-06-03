package mpv5.db.common;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 *  Andreas
 */
public class DatabaseSearch {
    public static String ALL_COLUMNS = "*";

    private Context context;

    /**
     *
     * @param context
     */
    public DatabaseSearch(Context context) {
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
     * @return select("*", null);
     */
    public Object[][] getValuesFor() {
        return QueryHandler.instanceOf().clone(context).select("*",(String[]) null);
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
     * Get multiple values from a search, where the search column is a number column
     * @param resultingFieldNames What do you like to get (columns)?
     * @param what Which column do you like to take for the condition?
    * @param  value
    * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what,  Number value) {
         return QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{what, value.toString(), ""});
    }

    /**
     * Get multiple values from a search
     * @param resultingFieldNames What do you like to get (columns)?
     * @param possibleColumns Which columns do you like to take for the condition?
     * @param where And what value should the column value have?
     * @param searchForLike Shall we search with "like" condition?
     * @return 
     */
    public Object[][] getValuesFor(String resultingFieldNames, String[] possibleColumns, String where, boolean searchForLike) {
     ArrayList<Object[]> list = new ArrayList<Object[]>();
        for (int i = 0; i < possibleColumns.length; i++) {
            String string = possibleColumns[i];
             list.addAll(Arrays.asList( QueryHandler.instanceOf().clone(context).select(resultingFieldNames, new String[]{string, where, "'"}, null, searchForLike)));
        }
     return list.toArray(new Object[][]{});
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
     * @throws NodataFoundException If no data was found matching your search
     */
    public Object[] searchFor(String what, String needle) throws NodataFoundException {
        return searchFor(null, what, needle);
    }

    /**
     * Get a single dimension list from a search after values from the column
     * where the value is LIKE the given needle
     * @param columns Which columns to get?
     * @param what Which column do you like to search through?
     * @param needle
     * @param exactMatch
     * @return
     * @throws NodataFoundException If no data was found matching your search
     */
    public Object[] searchFor(String[] columns, String what, String needle , boolean exactMatch) throws NodataFoundException {
        Object[] data = QueryHandler.instanceOf().clone(context).getValuesFor(columns, what, needle, exactMatch);
        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }
    /**
     * Get a single dimension list from a search after values from the column
     * where the value is LIKE the given needle
     * @param columns Which columns to get?
     * @param what Which column do you like to search through?
     * @param needle
     * @return
     * @throws NodataFoundException If no data was found matching your search
     */
    public Object[] searchFor(String[] columns, String what, String needle) throws NodataFoundException {
        Object[] data = QueryHandler.instanceOf().clone(context).getValuesFor(columns, what, needle, false);
        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }


    /**
     * Search for an ID in this context
     * @param what The column which you like to search through
     * @param needle The value of the row in that column
     * @return An id if there is a matching dataset found, NULL otherwise
     */
    public Integer searchForID( String what, String needle) {
        Object[] data;
        try {
            data = QueryHandler.instanceOf().clone(context).selectLast("ids", new String[]{what, needle, "'"}, true);
            return Integer.valueOf(data[0].toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
