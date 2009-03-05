/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import mpv5.globals.Messages;
import mpv5.items.div.Contact;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;

import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.text.RandomText;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * @author Andreas
 */
public class QueryHandler implements Cloneable {

    private static QueryHandler instance;
    private DatabaseConnection conn = null;
    private Connection sqlConn = null;
    private Statement stm = null;
    public String[] resultArray = null;
    private ResultSet resultSet = null;
    private String table = "NOTABLE";
    public String resultString = null;
    private static JFrame comp = new JFrame();
    private Context context;

    /**
     * !Use "Clone" method before actually do anything!
     * @return The one and only instance of the database connection
     */
    public static QueryHandler instanceOf() {
        if (instance == null) {
            instance = new QueryHandler();
        }
        return instance;
    }

    private QueryHandler() {
        try {
            conn = DatabaseConnection.instanceOf();
            sqlConn = conn.getConnection();
        } catch (Exception ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks the uniqueness of a unique constraint
     * Works only for columns with equal data type
     * @param constraint  {"column1","column2"}
     * @param values {"value1",value2<any/>}
     * @return true if the key constraint is not existing yet
     */
    public boolean checkConstraint(String[] constraint, Object[] values) {

        Object[][] val = select("ids", constraint, values);
        if (val != null && val.length > 0) {
            Log.Debug(this, "Uniqueness check failed!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This is a convenience bridge between views and unique constraint checks.
     * If the given objects is from type JTextField or LabeledTextField, the TextFields background will flash red<br/>
     * if the uniqueness check fails, nothing will happen otherwise
     * @param uniqueColumns to be separated with a comma
     * @param object An array of textfields
     * @return true if no uniqueness failure has been hidden
     */
    public boolean checkUniqueness(String uniqueColumns, JTextField[] object) {
        boolean returnv = true;
        for (int i = 0; i < object.length; i++) {
            if (!checkUniqueness(uniqueColumns.split(",")[i], (object[i]).getText())) {
                TextFieldUtils.blinkerRed(object[i]);
                returnv = false;
            }
        }

        return returnv;
    }

    /**
     *
     * @param context
     * @return
     */
    public QueryHandler setContext(Context context) {
        table = context.getDbIdentity();
        return this;
    }

    /**
     *
     * @param id
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(int id) throws NodataFoundException {
        ReturnValue data = freeReturnQuery("SELECT * FROM " + table + " WHERE " + table + ".ids = " + id, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve data such as "SELECT * FROM table"
     * @return All rows in the current context
     * @throws NodataFoundException
     */
    public ReturnValue select() throws NodataFoundException {
        ReturnValue data = freeReturnQuery("SELECT * FROM " + table, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve all IDS such as "SELECT ids FROM table"
     * @return All row IDs in the current context
     * @throws NodataFoundException
     */
    public ReturnValue selectIndexes() throws NodataFoundException {
        ReturnValue data = freeReturnQuery("SELECT ids FROM " + table, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return data;
        }
    }

    /**
     *
     * @param columns If null, the column specified with "needle" is returned
     * @param needle
     * @param value
     * @return
     */
    public Object[] getValuesFor(String[] columns, String needle, String value) {
        String cols = needle;
        if (columns != null) {
            cols = "";
            for (int i = 0; i < columns.length; i++) {
                String string = columns[i];
                cols += string + ",";
            }
            cols = cols.substring(0, cols.length() - 1);
        }

        if (context != null) {
            if (value == null) {
                return ArrayUtilities.ObjectToSingleColumnArray(freeReturnQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions().substring(5, context.getConditions().length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            } else {
                return ArrayUtilities.ObjectToSingleColumnArray(freeReturnQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + needle + " LIKE '%" + value + "%' AND " + context.getConditions().substring(5, context.getConditions().length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            }
        } else if (value == null) {
            return ArrayUtilities.ObjectToSingleColumnArray(freeReturnQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences(), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        } else {
            return ArrayUtilities.ObjectToSingleColumnArray(freeReturnQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + "  WHERE " + needle + " LIKE '%" + value + "%'", mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        }
    }

    /**
     * 
     * @param what
     * @param where
     * @param zeitraum
     * @return
     */
    public Object[][] selectBetween(String what, String[] where, vTimeframe zeitraum) {
        String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(zeitraum.getStart()) + "' AND '" + DateConverter.getSQLDateString(zeitraum.getEnd()) + "'";
        String query;
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + "  " + str;
        } else {
            query = "SELECT " + what + " FROM " + table + "  " + str;
        }
        String message = "Database Error (select) :";
        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param where
     * @param zeitraum
     * @param additionalCondition
     * @return
     */
    public ArrayList<Double> selectYearlySums(String what, String[] where, vTimeframe zeitraum, String additionalCondition) {

        Date temdate = zeitraum.getStart();
        ArrayList<Double> values = new ArrayList<java.lang.Double>();
        String query;
        do {
            String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(temdate) + "' AND '" + DateConverter.getSQLDateString(DateConverter.addYear(temdate)) + "'";

            if (where != null) {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + "  " + str + " " + additionalCondition;
            } else {
                query = "SELECT SUM(" + what + ") FROM " + table + "  " + str + " " + additionalCondition;
            }

            Object[][] o = freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                values.add(Double.valueOf(String.valueOf(o[0][0])));
            } else {
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    public ArrayList<Double> selectMonthlySums(String what, String[] where, vTimeframe zeitraum, String additionalCondition) {

        Date temdate = zeitraum.getStart();
        ArrayList<Double> values = new ArrayList<java.lang.Double>();
        String query;
        do {
            String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(temdate) + "' AND '" + DateConverter.getSQLDateString(DateConverter.addMonth(temdate)) + "'";

            if (where != null) {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + "  " + str + " " + additionalCondition;
            } else {
                query = "SELECT SUM(" + what + ") FROM " + table + "  " + str + " " + additionalCondition;
            }

            Object[][] o = freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                Log.Debug(this, "Summe: " + o[0][0]);
                values.add(Double.valueOf(String.valueOf(o[0][0])));
            } else {
                Log.Debug(this, "Summe: " + 0);
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order, Boolean like) {
        start();
        String query;
        String l1 = "";
        String l2 = "";
        String k = " = ";
        String j = "";

        String wher = "";
        java.util.Date date;

        if (like != null) {
            if (like) {
                if (where != null && where[0].endsWith("datum")) {
                    k = " BETWEEN ";
                    date = DateConverter.getDate(where[1]);
                    where[1] = "'" + DateConverter.getSQLDateString(date) + "'" + " AND " + "'" + DateConverter.getSQLDateString(DateConverter.addMonth(date)) + "'";
                    where[2] = " ";
                } else {
                    l1 = "%";
                    l2 = "%";
                    k = " LIKE ";
                }
            }
        }

        if (where != null) {

            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".ids" +
                    " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " ORDER BY " + table + "." + order;
        } else {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN  " + leftJoinTable + " ON " +
                    table + "." + leftJoinKey + " = " + leftJoinTable + ".ids " +
                    "  ORDER BY " + table + "." + order;
        }

        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    public int selectCountBetween(java.util.Date date1, java.util.Date date2) {
        return selectCount("datum", "BETWEEN '" + DateConverter.getSQLDateString(date1) + "' AND '" + DateConverter.getSQLDateString(date2) + "'");
    }

    public void setTable(String newTable) {
        this.table = newTable;
    }

    public String getTable() {
        return table;
    }

    /**
     * For the wait-cursor
     * @param main 
     */
    public static void setWaitCursorFor(JFrame main) {
        comp = main;
    }

    /**
     * This will flush the table of the current context, be careful!
     * This should never be triggered by a user, the user right will not be checked!
     * @param dbIdentity
     */
    public void truncate(String dbIdentity) {
        freeQuery(table, MPSecurityManager.SYSTEM_RIGHT, "Truncating table: " + table);
    }

    /**
     * Checks the uniqueness of the data
     * @param values
     * @param uniquecols
     * @return
     */
    public boolean checkUniqueness(String[] values, int[] uniquecols) {
        if (uniquecols != null) {
            for (int i = 0; i < uniquecols.length; i++) {
                int j = uniquecols[i];
                Object[][] val = select(values[0].split(",")[j], new String[]{values[0].split(",")[j], values[1].split(",")[j], values[2]});
                if (val != null && val.length > 0) {
                    MPV5View.addMessage(Messages.VALUE_ALREADY_EXISTS + values[1].split(",")[j]);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks the uniqueness of STRING data
     * @param column
     * @param value
     * @return
     */
    public boolean checkUniqueness(String column, String value) {
        return checkUniqueness(new String[]{column, value, "'"}, new int[]{0});
    }

    private void stop() {
        comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void start() {
        comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

//    public String getNextStringNumber(String colName) {
//        Integer s = getNextIndexOfStringCol(colName, null);
//        return originalvalue.substring(0, substringcount) + s;
//    }
    public Integer getCount() {
        start();
        int i = selectCount(null, null);
        stop();

        i = (i < 0) ? -i : i;
        return i;
    }

    /**
     * Inserts data into the current context's table. If "saveValues" is not <code>true</code><br/>
     * All data will be parsed and the following chars will be replaced:<br/><br/>
     *  - ' will become `<br/>
     *  - (;;2#4#1#1#8#0#;;) will become '<br/>
     *  - (;;,;;) will become ,<br/>
     * <br/>
     * this prevents the SQL String to be broken by invalid characters.<br/>
     * @param what
     * @param uniquecols
     * @param jobmessage
     * @param saveValues
     * @return id (unique) of the inserted row
     */
    public int insert(String[] what, int[] uniquecols, String jobmessage, boolean saveValues) {
        if (!saveValues) {
            what[1] = what[1].replace("'", "`");
            what[1] = what[1].replaceAll("\\(;;2#4#1#1#8#0#;;\\)", "'");
            what[1] = what[1].replaceAll("\\(;;\\,;;\\)", ",");
        }
//        if (what[2] == null) {
//            what[2] = "";
//        }
        String query = null;
        start();
        query = "INSERT INTO " + table + " (" + what[0] + " ) VALUES (" + what[1] + ") ";

        if (uniquecols != null) {
            if (!checkUniqueness(what, uniquecols)) {
                return 0;
            }
        }

        return freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, jobmessage).getId();
    }

    /**
     * Insert values to db
     * @param what  : {set, value, "'"}
     *   this.insert("name,wert", "'Sprache (Waehrung, z.B. Schweiz:  de_CH' ,'de_DE'");
     * @param jobmessage The message to be displayd after a successful run
     * @param saveValues If true, values are not masked; see {@link #insert(java.lang.String[], int[], java.lang.String, boolean) insert}
     * @return id of inserted row
     */
    public int insert(String[] what, String jobmessage, boolean saveValues) {
        return insert(what, (int[]) null, jobmessage, saveValues);
    }

    /**
     * 
     * @param what  : {set, values}
     * @param where : {value, comparison, "'"}
     * @param jobmessage
     */
    public void update(String[] what, String[] where, String jobmessage) {

        start();
        what[1] = what[1].replaceAll("'", "`");
        what[1] = what[1].replaceAll("\\(;;2#4#1#1#8#0#;;\\)", "'");
        String query;
        Log.Debug(this, what);

        String[] a = what[0].split(",");
        String[] b = what[1].split("\\(;;\\,;;\\)");
        String c = "";

        for (int i = 0; i < a.length; i++) {
            c = c + a[i] + " = " + b[i] + ", ";
        }

        c = c.substring(0, c.length() - 2);

        query = "UPDATE " + table + " SET " + c + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2];
        freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.EDIT, jobmessage);
        stop();
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @return last matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectLast(String what, String[] where) throws NodataFoundException {

        Object[][] data = select(what, where, what, false);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[0];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @return first matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectFirst(String what, String[] where) throws NodataFoundException {

        Object[][] data = select(what, where, what, false);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[data.length - 1];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param searchFoLike
     * @return first matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectFirst(String what, String[] where, boolean searchFoLike) throws NodataFoundException {

        Object[][] data = select(what, where, what, searchFoLike);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[data.length - 1];
        }
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     *
     * @param what
     * @param where : {value, comparison, "'"}
     * @param searchFoLike
     * @return last matching result as string array
     * @throws NodataFoundException
     */
    @SuppressWarnings("unchecked")
    public Object[] selectLast(String what, String[] where, boolean searchFoLike) throws NodataFoundException {

        Object[][] data = select(what, where, what, searchFoLike);

        if (data == null || data.length == 0) {
            throw new NodataFoundException();
        } else {
            return data[0];
        }
    }

    /**
     * 
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey 
     * @param order 
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order) {
        return select(what, where, leftJoinTable, leftJoinKey, order, null);
    }

    /**
     * 
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey 
     * @return results as multidimensional string array
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey) {
        return select(what, where, leftJoinTable, leftJoinKey, "id", null);
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where) {
        start();
        String query;
        if (where != null && where[0] != null && where[1] != null) {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + context.getConditions().substring(5, context.getConditions().length());
        } else {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions().substring(5, context.getConditions().length());
        }
        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param whereColumns  {"column1","column2"}
     * @param haveValues {"value1",value2<any/>}
     * @return
     */
    public Object[][] select(String what, String[] whereColumns, Object[] haveValues) {
        String query = "SELECT " + what + " FROM " + table + " WHERE ";
        for (int i = 0; i < haveValues.length; i++) {

            Object object = haveValues[i];
            String column = whereColumns[i];
            if (object instanceof Number) {
                query += column + "=" + object.toString();
            } else {
                query += column + "='" + object.toString() + "'";
            }

            if ((i + 1) != haveValues.length) {
                query += " AND ";
            }
        }
        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @param order 
     * @param like - datum will be returned between given and given + 1 month
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public Object[][] select(String what, String[] where, String order, boolean like) {
        start();
        String l1 = "";
        String l2 = "";
        String k = " = ";
        String j = "";

        if (order == null) {
            order = "ids DESC ";
        }

        String ord = " ORDER BY " + table + "." + order;
        String wher = "";
        java.util.Date date;


        if (like) {
            if (where != null && where[0].endsWith("datum")) {
                k = " BETWEEN ";
                date = DateConverter.getDate(where[1]);
                where[1] = "'" + DateConverter.getSQLDateString(date) + "'" + " AND " + "'" + DateConverter.getSQLDateString(DateConverter.addMonth(date)) + "'";
                where[2] = " ";
            } else {
                l1 = "%";
                l2 = "%";
                k = " LIKE ";
            }
        }

        if (where == null) {
            wher = "  " + context.getConditions();
        } else {
            wher = " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " AND " + context.getConditions().substring(5, context.getConditions().length()) + " ";
        }
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + wher + ord;

        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * Deletes the given data permanently from the database
     * @param where : {value, comparison, "'"}
     *                eg new String[][]{{"ids", "8" , ""},{"ids", "9",""}}
     * @param jobmessage
     * @return True if the deletion was not terminated by constraints
     */
    public boolean delete(String[][] where, String jobmessage) {
        start();
        String str = "";
        String query = null;
        ReturnValue retval = null;

        if (where != null) {
            for (int i = 0; i < where.length; i++) {
                str = str + table + "." + where[i][0] + " = " + where[i][2] + where[i][1] + where[i][2];
            }
            query = "DELETE FROM " + table + " WHERE " + str;
            retval = freeQuery(query, mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, jobmessage);
        }

        if (retval == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Deletes the given data permanently from the database
     * @param whereColumns
     * @param haveValues
     * @param jobmessage
     * @return 
     */
    public boolean delete(String[] whereColumns, Object[] haveValues, String jobmessage) {
        String query = "DELETE FROM " + table + " WHERE ";
        for (int i = 0; i < haveValues.length; i++) {
            Object object = haveValues[i];
            String column = whereColumns[i];
            if (object instanceof Number) {
                query += column + "=" + object.toString();
            } else {
                query += column + "='" + object.toString() + "'";
            }

            if ((i + 1) != haveValues.length) {
                query += " AND ";
            }
        }
        ReturnValue retval = freeQuery(query, mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, jobmessage);

        if (retval == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Convenience method for delete(where, null);
     * @param where : {value, comparison, "'"}
     * @throws Exception 
     */
    public void delete(String[][] where) throws Exception {
        delete(where, null);
    }

    /**
     * 
     * @param newTable
     * @return a clone of this ConnectionHandler (with database connection)
     */
    public QueryHandler clone(String newTable) {
        QueryHandler theClone = null;
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(newTable);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     *
     * @param context
     * @return clone of this ConnectionHandler (with database connection)
     */
    public QueryHandler clone(Context context) {
        QueryHandler theClone = null;
        this.context = context;
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(context.getDbIdentity());
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     * 
     * @param what
     * @param where
     * @param order
     * @param like
     * @param integer
     * @return
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public Object[][] select(String what, String[] where, String order, boolean like, boolean integer) {
        start();
        String l = "";
        String k = " = ";
        String j = "";
        String ord = " ORDER BY " + table + "." + order;
        String wher = "";
        wher = "";

        if (integer) {
            if (where[1].equals("")) {
                where[1] = "0";
            }
            where[2] = "";
//            l = "%";
            k = " = ";
//            j = " OR WHERE " + where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2];
        }
        java.util.Date date;
        if (like) {
            if (where != null && where[0].contains("datum")) {
                k = " BETWEEN ";
                date = DateConverter.getDate(where[1]);
                where[1] = "'" + DateConverter.getSQLDateString(date) + "'" + " AND " + "'" + DateConverter.getSQLDateString(DateConverter.addMonth(date)) + "'";
                where[2] = " ";
            } else {
                l = "%";
                k = " LIKE ";
            }
        }

        if (where == null) {
        } else {
            wher = where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2] + " AND " + wher + " AND " + context.getConditions().substring(5, context.getConditions().length());
            if (where.length > 3) {
                wher = wher + " AND " + where[3] + " " + k + " " + where[5] + l + where[4] + l + where[5] + " " + context.getConditions() + " ";
            }
        }
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + wher + ord;

        return freeReturnQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * 
     * @param from
     * @param where
     * @return
     */
    public int selectCount(String from, String where) {
        String wher = "";

        if (where != null) {
            wher = " WHERE " + from + " " + where;
        }
        String query = "SELECT COUNT(*) AS rowcount FROM " + table + " " + wher;
        String message = "Database Error (SelectCount:COUNT):";
        stm = null;
        resultSet = null;

        Log.Debug(this, query, true);
        try {
            // Select-Anweisung ausführen
            stm = sqlConn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            if (resultSet.first()) {
                Log.Debug(this, "Count " + resultSet.getInt("rowcount"));
                return resultSet.getInt("rowcount");
            } else {
                stop();
                return 0;
            }
        } catch (SQLException ex) {
            Log.Debug(this, message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            stop();
            return 0;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
    }

    /**
     * Free SQL Statement!
     * @param string
     * @param action
     * @param jobmessage
     * @return
     */
    @SuppressWarnings("unchecked")
    public ReturnValue freeQuery(String string, int action, String jobmessage) {
        return freeQuery(string, null, action, jobmessage);
    }

    @SuppressWarnings("unchecked")
    public ReturnValue freeQuery(String query, JTextArea log, int action, String jobmessage) {

        if (!mpv5.usermanagement.MPSecurityManager.check(context, action)) {
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity(),
                    Messages.ACCESS_DENIED);
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        }

        start();
        query = query.replace("%%tablename%%", table);
        ReturnValue retval = null;
        String message = "Database Error (freeQuery) :";
        stm = null;
        resultSet = null;
        boolean bool;
        ResultSetMetaData rsmd;
        Object[][] data = null;
        ArrayList z;
        int id = -1;
        String[] columnnames = null;
        try {
            // Select-Anweisung ausführen
            stm = sqlConn.createStatement();
            Log.Debug(this, query, true);
            if (log != null) {
                log.append("\n " + query);
            }
            bool = stm.execute(query);
            if (log != null) {
                log.append("\n " + stm.getUpdateCount() + " rows affected.");
            }
            stop();

            ResultSet keys = stm.getGeneratedKeys();
            if (keys != null && keys.next()) {
                id = keys.getInt(1);
            }

            if (bool) {
                resultSet = stm.getResultSet();
                ArrayList spalten = new ArrayList();
                ArrayList zeilen = new ArrayList();
                rsmd = resultSet.getMetaData();

                columnnames = new String[rsmd.getColumnCount()];
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    columnnames[i - 1] = rsmd.getColumnName(i);
                }

                while (resultSet.next()) {
                    spalten = new ArrayList();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        spalten.add(resultSet.getObject(i));
                    }
                    zeilen.add(spalten);
                }
                data = new Object[zeilen.size()][spalten.size()];

                for (int h = 0; h < zeilen.size(); h++) {
                    z = (ArrayList) zeilen.get(h);
                    for (int i = 0; i < spalten.size(); i++) {
                        data[h][i] = z.get(i);
                    }
                }
            }
            retval = new ReturnValue(id, data, columnnames);

        } catch (SQLException ex) {
            stop();
            jobmessage = Messages.ERROR_OCCURED;
            Log.Debug(this, message + ex.getMessage());
            retval = null;
            if (log != null) {
                log.append("\n " + ex.getMessage());
            }
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    stop();
                    jobmessage = Messages.ERROR_OCCURED;
                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    stop();
                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
        }
        stop();
        if (jobmessage != null) {
            MPV5View.addMessage(jobmessage);
        }
        return retval;

    }

    /**
     *
     * @param query
     * @param action
     * @param jobmessage
     * @return
     */
    public ReturnValue freeUpdateQuery(String query, int action, String jobmessage) {
        return freeUpdateQuery(query, null, action, jobmessage);
    }

    @SuppressWarnings("unchecked")
    public ReturnValue freeUpdateQuery(String query, JTextArea log, int action, String jobmessage) {

        if (!mpv5.usermanagement.MPSecurityManager.check(context, action)) {
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity(),
                    Messages.ACCESS_DENIED);
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        } else {
//              Log.Debug(this, Messages.SECURITYMANAGER_ALLOWED+
//                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
        }

        start();
        try {
            query = query.replace("%%tablename%%", table);
        } catch (Exception e) {
        }
        ReturnValue retval = null;
        String message = "Database Error (freeQuery) :";
        stm = null;
        resultSet = null;
        Integer id = -1;
        ResultSet keys;

        try {
            // Select-Anweisung ausführen
            stm = sqlConn.createStatement();
            Log.Debug(this, query, true);
            if (log != null) {
                log.append("\n " + query);
            }
            stm.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            if (log != null) {
                log.append("\n " + stm.getUpdateCount() + " rows affected.");
            }
            stop();

            keys = stm.getGeneratedKeys();

            if (keys != null && keys.next()) {
                id = keys.getInt(1);
            }

            retval = new ReturnValue(id, null, null);

        } catch (SQLException ex) {
            stop();
            jobmessage = Messages.ERROR_OCCURED;
            Log.Debug(this, message + ex.getMessage());
            if (log != null) {
                log.append("\n " + ex.getMessage());
            }
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    stop();
                    jobmessage = Messages.ERROR_OCCURED;
                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    stop();
                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
        }
        stop();
        if (jobmessage != null) {
            MPV5View.addMessage(jobmessage);
        }
        return retval;

    }

    /**
     * Free SQL Select Statement!
     * @param query
     * @param action 
     * @param jobmessage 
     * @return Your Data
     */
    @SuppressWarnings({"unchecked"})
    public ReturnValue freeReturnQuery(String query, int action, String jobmessage) {

        query = query.replace("%%tablename%%", table);

        if (!mpv5.usermanagement.MPSecurityManager.check(context, action)) {
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED +
                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity(),
                    Messages.ACCESS_DENIED);
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        } else {
//              Log.Debug(this, Messages.SECURITYMANAGER_ALLOWED+
//                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity()
//                    );
        }

        start();
        String message = "Database Error (selectFreeQuery) :";

        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        Object[][] data = null;
        ArrayList z;
        int id = -1;
        String[] columnnames = null;

        try {
            // Select-Anweisung ausführen
            stm = sqlConn.createStatement();
            Log.Debug(this, query, true);
            resultSet = stm.executeQuery(query);
            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();
            rsmd = resultSet.getMetaData();

            ResultSet keys = stm.getGeneratedKeys();
            if (keys != null && keys.next()) {
                id = keys.getInt(1);
            }

            columnnames = new String[rsmd.getColumnCount()];
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnnames[i - 1] = rsmd.getColumnName(i);

//                System.out.print( rsmd.getColumnName(i) +",");
            }

//            Log.PrintArray(columnnames);

            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));
                }
                zeilen.add(spalten);
            }
            data = new Object[zeilen.size()][spalten.size()];

            for (int h = 0; h < zeilen.size(); h++) {
                z = (ArrayList) zeilen.get(h);
                for (int i = 0; i < spalten.size(); i++) {
                    data[h][i] = z.get(i);
                }
            }
        } catch (SQLException ex) {
            Log.Debug(this, "Datenbankfehler: " + query, true);
            Log.Debug(this, message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            jobmessage = Messages.ERROR_OCCURED;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    jobmessage = Messages.ERROR_OCCURED;
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        if (jobmessage != null) {
            MPV5View.addMessage(jobmessage);
        }
//        Log.PrintArray(data);
        return new ReturnValue(id, data, columnnames);
    }

    /**
     * This method inserts a file into a "filename<unique/>, filedata" table
     * @param file
     * @return The UNIQUE name of the inserted file in db
     * @throws java.io.FileNotFoundException
     */
    public String insertFile(File file) throws FileNotFoundException {
        start();
        String name = null;
        String query = "INSERT INTO " + table + "(cname, data, dateadded) VALUES (?, ?, ?)";
        String jobmessage = null;
        try {
            int fileLength = (int) file.length();
            name = new RandomText(23).getString();
            java.io.InputStream fin = new java.io.FileInputStream(file);
            PreparedStatement ps = sqlConn.prepareStatement(query);
            ps.setString(1, name);
            ps.setDate(3, new java.sql.Date(new Date().getTime()));
            ps.setBinaryStream(2, fin, fileLength);
            ps.execute();
            sqlConn.commit();
        } catch (SQLException ex) {
            Log.Debug(this, "Datenbankfehler: " + query, true);
            Log.Debug(this, ex);
            Popup.error(ex.getMessage(), "Datenbankfehler");
            jobmessage = Messages.ERROR_OCCURED;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    jobmessage = Messages.ERROR_OCCURED;
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        if (jobmessage != null) {
            MPV5View.addMessage(jobmessage);
        }
        return name;
    }

    /**
     * This method returns a list of files from a "filename, filedata" table
     * @param filename The unique filename
     * @return A list with temporary files
     * @throws IOException
     */
    public ArrayList<File> retrieveFiles(String filename) throws IOException {
        start();
        String query = "SELECT data FROM " + table + " WHERE cname= '" + filename + "'";
        String jobmessage = null;
        ArrayList<File> list = null;
        try {
            stm = sqlConn.createStatement();
            list = new ArrayList<File>();

            ResultSet rs = stm.executeQuery(query);
            Log.Debug(this, query);
            while (rs.next()) {
                byte[] buffer = new byte[1024];
                File f = FileDirectoryHandler.getTempFile();
                BufferedInputStream inputStream = new BufferedInputStream(rs.getBinaryStream(1), 1024);
                FileOutputStream outputStream = new FileOutputStream(f);
                int readBytes = 0;
                while (readBytes != -1) {
                    readBytes = inputStream.read(buffer, 0, buffer.length);
                    if (readBytes != -1) {
                        outputStream.write(buffer, 0, readBytes);
                    }
                }
                inputStream.close();
                outputStream.close();
                list.add(f);
            }

        } catch (SQLException ex) {
            Log.Debug(this, "Datenbankfehler: " + ex.getMessage(), true);
            Popup.error(ex.getMessage(), "Datenbankfehler");
            jobmessage = Messages.ERROR_OCCURED;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    jobmessage = Messages.ERROR_OCCURED;
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        if (jobmessage != null) {
            MPV5View.addMessage(jobmessage);
        }
        return list;
    }

    /**
     * A convenience method to retrieve one file from db, or null of no file
     * with the specified name is available.
     * @param name
     * @param targetFile 
     * @return The target file or NULL
     */
    public File retrieveFile(String name, File targetFile) {
        ArrayList<File> list;
        try {
            list = retrieveFiles(name);
        } catch (Exception ex) {
            Log.Debug(this, name + " not found in " + table);
            return null;
        }
        if (list.size() == 0) {
            return null;
        } else {
            URI k;
            try {
                k = FileDirectoryHandler.copyFile(list.get(0), targetFile.getParentFile(), targetFile.getName());
            } catch (IOException ex) {
                Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            File f= new File(k);
            return f;
        }
    }

    /**
     * A convenience method to retrieve one file from db, or null of no file
     * with the specified name is available.
     * @param name
     * @return A file or NULL
     */
    public File retrieveFile(String name) {
        ArrayList<File> list;
        try {
            list = retrieveFiles(name);
        } catch (Exception ex) {
            Log.Debug(this, name + " not found in " + table);
            return null;
        }
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * Remove a file from the db
     * @param fileid
     * @throws java.lang.Exception
     */
    public void removeFile(String fileid) throws Exception {
        delete(new String[][]{{"cname", fileid, "'"}});
    }

    /**
     * This is a convenience method to insert files associated with contacts
     * @param file The file
     * @param dataOwner The contact
     * @param descriptiveText Describe the file
     * @return True if the insert was a success
     */
    public boolean insertFile(File file, Contact dataOwner, String descriptiveText) {
        try {
            String s = insertFile(file);
            QueryHandler.instanceOf().clone(Context.getFilesToContacts()).insert(new String[]{"cname,contactsids,filename, description", "'" + file.getName() + "', " + dataOwner.__getIDS() + " ,'" + s + "','" + descriptiveText + "'"}, Messages.FILE_SAVED + file.getName(), true);
            return true;
        } catch (FileNotFoundException fileNotFoundException) {
            return false;
        }
    }
}
