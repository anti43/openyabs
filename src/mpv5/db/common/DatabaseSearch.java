package mpv5.db.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;

/**
 *
 * Convenience class for database searches. <br/>Does <b>NOT</b> automatically
 * take care of multi-client capability needs.
 */
public class DatabaseSearch {

    public static String ALL_COLUMNS = "*";
    private final Context context;
    private int ROWLIMIT = 0;

    /**
     * Initiates a new search
     *
     * @param context
     */
    public DatabaseSearch(Context context) {
        this.context = context;
    }

    /**
     * Initiates a new search
     *
     * @param obj The DBOBJ the search will rely on
     */
    public DatabaseSearch(DatabaseObject obj) {
        this.context = obj.getContext();
        context.setDbIdentity(obj.getDbIdentity());
        context.setSubID(Context.DEFAULT_SUBID);
    }

    /**
     * Initiates a new search and allows to limit the resulting rows
     *
     * @param context
     * @param rowlimit
     */
    public DatabaseSearch(Context context, int rowlimit) {
        this.context = context;
        this.ROWLIMIT = rowlimit;
    }
// /**
//     * Initiates a new search and allows to limit the resulting rows
//     * @param rowlimit
//     */
//    public DatabaseSearch(int rowlimit) {
//        this.ROWLIMIT = rowlimit;
//    }

    /**
     * Get multiple values from a search
     *
     * @return select("*", null);
     */
    public Object[][] getValuesFor() {
        return QueryHandler.instanceOf().clone(context, ROWLIMIT).select("*", (String[]) null);
    }

    /**
     * Do a fulltextsearch
     *
     * @param val
     * @return The result
     * @deprecated SLOW
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Object[][] getFulltextSearchvaluesFor(String val) {
        Log.Debug(this, "Fulltextlookup for " + val);
        ArrayList<Context> contexts = Context.getSearchableContexts();
        Object[][] data = new Object[][]{};
        for (int i = 0; i < contexts.size(); i++) {
            try {
                Context contx = contexts.get(i);
                ReturnValue rdata = QueryHandler.instanceOf().freeQuery(QueryHandler.instanceOf().clone(contx, ROWLIMIT).buildQuery(val, DatabaseObject.getObject(contx).getStringVars().toArray(new String[]{})), MPSecurityManager.VIEW, null);
                Object[][] ndata = rdata.getData();
//                Log.PrintArray(ndata);
                Object[] idcol = new Object[ndata.length];
                for (int j = 0; j < idcol.length; j++) {
                    idcol[j] = new DatabaseObject.Entity(contx, Integer.valueOf(ndata[j][0].toString()));
                }
                ndata = ArrayUtilities.replaceColumn(ndata, 0, idcol);
                data = ArrayUtilities.merge(data, ndata);
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
        return data;
    }

    /**
     * Do a fulltextsearch
     *
     * @param val
     * @return The result
     */
    @SuppressWarnings("unchecked")
    public List<Integer> searchObjectIdsFor(String val) {
        Log.Debug(this, "searchObjectIdsFor for " + val);
        List<Integer> l = new ArrayList<Integer>();
        try {
            Context contx = context;
            ReturnValue rdata = QueryHandler.instanceOf().clone(contx, ROWLIMIT).freeQuery(QueryHandler.instanceOf().clone(contx, ROWLIMIT)
                    .buildIdQuery(val, DatabaseObject.getObject(contx).getStringVars().toArray(new String[]{})), MPSecurityManager.VIEW, null);
            Object[] ndata = rdata.getFirstColumn();

            if (ndata != null) {
                for (int j = 0; j < ndata.length; j++) {
                    l.add(Integer.valueOf(ndata[j].toString()));
                }
            }

        } catch (Exception e) {
            Log.Debug(e);
        }

        return l;
    }

    /**
     *
     * @param val
     * @return
     * @throws NodataFoundException
     */
    public List<DatabaseObject> searchObjectsFor(String val) throws NodataFoundException {
        List<Integer> data = searchObjectIdsFor(val);
        return DatabaseObject.getObjects(context, data);
    }

    /**
     * Get multiple values from a search
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames) {
        return QueryHandler.instanceOf().clone(context, ROWLIMIT).select(resultingFieldNames, new String[]{null, "", "'"});
    }

    /**
     * Get multiple values from a search, where the search column is a number
     * column
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @param what Which column do you like to take for the condition?
     * @param value
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what, Number value) {
        return QueryHandler.instanceOf().clone(context, ROWLIMIT).select(resultingFieldNames, new String[]{what, value.toString(), ""});
    }

    /**
     * Get multiple values from a search, where the search column is a String
     * column
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @param what Which column do you like to take for the condition?
     * @param value
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what, String value) {
        return getValuesFor2(resultingFieldNames, value, null, false, false, new String[]{what});
    }

    /**
     * Get multiple values from a search
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @param possibleColumns Which columns do you like to take for the
     * condition?
     * @param where And what value should the column value have?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String[] possibleColumns, Number where) {
        try {
            ArrayList<Object[]> list = new ArrayList<Object[]>();
            QueryCriteria2 c = new QueryCriteria2();
            ArrayList<QueryParameter> l = new ArrayList<QueryParameter>();
            for (int i = 0; i < possibleColumns.length; i++) {
                String string = possibleColumns[i];
                l.add(new QueryParameter(context, string, where, QueryParameter.EQUALS));
            }
            c.or(l);
            list.addAll(Arrays.asList(QueryHandler.instanceOf().clone(context, ROWLIMIT).select(resultingFieldNames, c).getData()));

            return list.toArray(new Object[][]{});
        } catch (NodataFoundException ex) {
            return new Object[0][0];
        }
    }

    /**
     * Get multiple values from a search. Will split word by whitespace
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @param possibleColumns Which columns do you like to take for the
     * condition?
     * @param where And what value should the column value have?
     * @param searchForLike Shall we search with "like" condition?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String[] possibleColumns, String where, boolean searchForLike) {
        return getValuesFor2(resultingFieldNames, where, null, true, searchForLike, possibleColumns);
    }

    /**
     * Get multiple values from a search, ignores reference tables and is
     * herewith faster
     *
     * @param resultingFieldNames What do you like to get (columns)?
     * @param possibleColumns Which columns do you like to take for the
     * condition?
     * @param groups
     * @param splitByWhitespace
     * @param search And what value should the column value have?
     * @param searchForLike Shall we search with "like" condition?
     * @return
     */
    public Object[][] getValuesFor2(String resultingFieldNames, String search, List<Group> groups, boolean splitByWhitespace, boolean searchForLike, String... possibleColumns) {
        QueryCriteria2 qc = new QueryCriteria2();
        if (possibleColumns == null || possibleColumns.length == 0) {
            possibleColumns = DatabaseObject.getObject(context).getStringVars().toArray(new String[0]);
        }

        if (search != null) {
            List<List<QueryParameter>> ps = new ArrayList<List<QueryParameter>>();
            List<QueryParameter> not = new ArrayList<QueryParameter>();
            String[] strings = null;
            if (splitByWhitespace) {
                strings = search.split("\\s+");
            } else {
                strings = new String[]{search};
            }
            for (int i = 0; i < strings.length; i++) {
                ArrayList<QueryParameter> psx = new ArrayList<QueryParameter>();
                ps.add(psx);
                String string = strings[i];
                for (String en : possibleColumns) {
                    if (!string.startsWith("-")) {
                        psx.add(new QueryParameter(context, en, string, searchForLike ? QueryParameter.LIKE : QueryParameter.EQUALS));
                    } else {
                        not.add(new QueryParameter(context, en, string.substring(1), searchForLike ? QueryParameter.LIKE : QueryParameter.EQUALS));
                    }
                }
            }
            for (int i = 0; i < ps.size(); i++) {
                qc.or(ps.get(i));
            }
            qc.and(not);
        }
        List<QueryParameter> ps = new ArrayList<QueryParameter>();
        if (User.getCurrentUser().isGroupRestricted()) {
            ps.add(new QueryParameter(context, "groupsids", User.getCurrentUser().__getGroupsids(), QueryParameter.EQUALS));
        } else if (groups != null && !groups.isEmpty()) {
            for (int i = 0; i < groups.size(); i++) {
                Group group = groups.get(i);
                ps.add(new QueryParameter(context, "groupsids", group.__getIDS(), QueryParameter.EQUALS));
            }
        }
        qc.or(ps);
        try {
            return QueryHandler.instanceOf().clone(context, ROWLIMIT).select(resultingFieldNames, qc).getData();
        } catch (NodataFoundException ex) {
            Log.Debug(search, ex.getMessage());
            return new Object[0][0];
        }
    }

    /**
     * Get multiple values from a search
     *
     * @param resultingFieldNames What do you like to get (columns, comma
     * separated)?
     * @param what Which column do you like to take for the condition?
     * @param where And what value should the column value have?
     * @param searchForLike Shall we search with "like" condition?
     * @return
     */
    public Object[][] getValuesFor(String resultingFieldNames, String what, String where, boolean searchForLike) {
        QueryCriteria2 c = new QueryCriteria2();
        c.and(new QueryParameter(context, what, where, searchForLike ? QueryParameter.LIKE : QueryParameter.EQUALS));
        try {
            return QueryHandler.instanceOf().clone(context, ROWLIMIT).select(resultingFieldNames, c).getData();
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
            return new Object[0][0];
        }
    }

//
//    /**
//     * Get a single dimension list from a search after values from the column
//     * where the value is LIKE the given needle
//     * @param what Which column do you like to get and search through?
//     * @param needle
//     * @return
//     * @throws NodataFoundException If no data was found matching your search
//     */
//    public Object[] searchFor(String what, String needle) throws NodataFoundException {
//        return searchFor(null, what, needle);
//    }
    /**
     * Get a single dimension list from a search after values from the column
     * where the value is LIKE the given needle
     *
     * @param columns Which columns to get?
     * @param what Which column do you like to search through?
     * @param needle
     * @param exactMatch
     * @return
     * @throws NodataFoundException If no data was found matching your search
     */
    public Object[] searchFor(String[] columns, String what, String needle, boolean exactMatch) throws NodataFoundException {
        Object[] data = QueryHandler.instanceOf().clone(context, ROWLIMIT).getValuesFor(columns, what, needle, exactMatch);
        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }

    /**
     * Get a single dimension list from a search after values from the column
     * where the value is LIKE the given needle
     *
     * @param columns Which columns to get?
     * @param what Which column do you like to search through?
     * @param needle
     * @return
     * @throws NodataFoundException If no data was found matching your search
     */
    public Object[] searchFor(String[] columns, String what, String needle) throws NodataFoundException {
        Object[] data = QueryHandler.instanceOf().clone(context, ROWLIMIT).getValuesFor(columns, what, needle, false);
        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }

    /**
     *
     * @param ext
     * @param self
     * @param value
     * @return
     */
    public List<Integer> searchObjectIdsFor(Context[] ext, Context[] self, String value) {
        Log.Debug(this, "Search parameter: " + value);
        Set<Integer> data = new TreeSet<Integer>();

        for (Integer s : new DatabaseSearch(context, ROWLIMIT).searchObjectIdsFor(value)) {
            data.add(s);
        }

        for (Context exct : ext) {

            String subitemids = "0";
            for (Integer s : new DatabaseSearch(exct, ROWLIMIT).searchObjectIdsFor(value)) {
                subitemids = s + "," + subitemids;
            }

            final String x = Context.getAliasFor(context, exct);
            Object[] sdata = QueryHandler.instanceOf().clone(context, ROWLIMIT).freeQuery("select " + x + "ids from " + exct.getDbIdentity() + " where ids in(" + subitemids + ")", MPSecurityManager.VIEW, null).getFirstColumn();
            if (sdata != null) {
                for (int i = 0; i < sdata.length; i++) {
                    try {
                        data.add(Integer.valueOf(sdata[i].toString()));
                    } catch (NumberFormatException numberFormatException) {
                        Log.Debug(numberFormatException);
                    }
                }
            }
        }
        for (Context selfc : self) {
            String contactsids = "0";
            for (Integer s : new DatabaseSearch(selfc, ROWLIMIT).searchObjectIdsFor(value)) {
                contactsids = s + "," + contactsids;
            }
            Object[] sdata = QueryHandler.instanceOf().clone(context, ROWLIMIT).freeQuery("select ids from items where " + selfc.getDbIdentity() + "ids in(" + contactsids + ")", MPSecurityManager.VIEW, null).getFirstColumn();
            if (sdata != null) {
                for (int i = 0; i < sdata.length; i++) {
                    try {
                        data.add(Integer.valueOf(sdata[i].toString()));
                    } catch (NumberFormatException numberFormatException) {
                        Log.Debug(numberFormatException);
                    }
                }
            }
        }

        return new ArrayList<Integer>(data);
    }

    /**
     *
     * @param sf
     * @param ext
     * @param self
     * @param value
     * @return
     */
    public Object[][] searchDataFor(String sf, Context[] ext, Context[] self, String value) {
        List<Integer> data = searchObjectIdsFor(ext, self, value);
        String dboids = "0";
        for (Integer id : data) {
            dboids = id + "," + dboids;
        }
        return QueryHandler.instanceOf().clone(context, ROWLIMIT).freeQuery("select " + String.valueOf(sf) + " from %%tablename%% " +  String.valueOf(context.getConditions(false)) + " AND ids in (" + dboids + ") AND " +  String.valueOf(context.getNoTrashSQLString()) , MPSecurityManager.VIEW, null).getData();
    }
    
     public List<DatabaseObject> searchDataFor(Context[] ext, Context[] self, String value) {
        try {
            List<Integer> data = searchObjectIdsFor(ext, self, value);
            return DatabaseObject.getObjects(context, data);
        } catch (NodataFoundException ex) {
            return Collections.EMPTY_LIST;
        }
     }
}
