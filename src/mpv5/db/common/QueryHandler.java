/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;

import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.text.RandomText;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * Use this class to access the MP database.
 *
 * @see QueryHandler#instanceOf() 
 * @see QueryHandler#getConnection()
 * @see QueryHandler#clone(mpv5.db.common.Context) 
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
    public static synchronized QueryHandler instanceOf() {
        if (instance == null) {
            instance = new QueryHandler();
        }
        return instance;
    }
    private DataPanel viewToBeNotified = null;
    private static Integer ROW_LIMIT = null;
    private int limit = 0;
    private boolean runInBackground = false;

    private QueryHandler() {
        try {
            conn = DatabaseConnection.instanceOf();
            sqlConn = conn.getConnection();
            versionCheck();
            runFixes();
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    private void versionCheck() {
        try {

            Statement versionCheck = sqlConn.createStatement();
            Log.Debug(this, "Checking database version..");
            ResultSet versionData = versionCheck.executeQuery("SELECT value FROM globalsettings WHERE cname = 'yabs_dbversion'");
            if (versionData.next()) {
                double dbversion = Double.valueOf(versionData.getString(1));
                Log.Debug(this, "Database version found: " + dbversion);
                if (dbversion >= Constants.DATABASE_MAX_VERSION.doubleValue()) {
                    throw new UnsupportedOperationException("Database version is too high! Required min version: " + Constants.DATABASE_VERSION + " Required max version: " + Constants.DATABASE_MAX_VERSION);
                } else if (dbversion < Constants.DATABASE_VERSION.doubleValue()) {
                    new DatabaseUpdater().updateFrom(dbversion);
                }
            } else {
                Log.Debug(this, "Database version info can not be found.");
                throw new UnsupportedOperationException("Database version cannot be validated! Required version: " + Constants.DATABASE_VERSION);
            }
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    private void runFixes() {
        try {
            Statement runfixes = sqlConn.createStatement();

            //Issue #239////////////////////////////////////////////////////////
            runfixes.setMaxRows(1);
            ResultSet firstgroup = runfixes.executeQuery("SELECT groupsids FROM groups ORDER BY ids ASC");
            if (firstgroup.next()) {
                int gids = firstgroup.getInt(1);
                if (gids != 0) {
                    runfixes.execute("update groups set groupsids = 0 where ids = 1");
                    Log.Debug(this, "Corrected group 1 to fix Issue #239");
                }
            }
            ResultSet firstaccount = runfixes.executeQuery("SELECT intparentaccount FROM accounts ORDER BY ids ASC");
            if (firstaccount.next()) {
                int gids = firstaccount.getInt(1);
                if (gids != 0) {
                    runfixes.execute("update accounts set intparentaccount = 0 where ids = 1");
                    Log.Debug(this, "Corrected account 1 to fix Issue #239");
                }
            }
            ////////////////////////////////////////////////////////////////////
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    /**
     * Set the global row limit for select queries. 0 is unlimited.
     * @param limit
     */
    public static synchronized void setRowLimit(int limit) {
        if (ROW_LIMIT == null || limit > ROW_LIMIT.intValue() || limit < ROW_LIMIT.intValue()) {
            Log.Debug(QueryHandler.class, "Setting global row limit to: " + limit);
            ROW_LIMIT = limit;
        }
    }

    /**
     *
     * @param c
     */
    public QueryHandler(DatabaseConnection c) {
        try {
            conn = c;
            sqlConn = conn.getConnection();
        } catch (Exception ex) {
            Log.Debug(ex);
            Popup.error(ex);
        }
    }

    private void setLimit(int limit) {
        if (limit > this.limit || limit < this.limit) {
            Log.Debug(QueryHandler.class, "Setting row limit for this connection to: " + limit);
            this.limit = limit;
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
        for (int i = 0; i < values.length; i++) {
            Object object = values[i];

            if (!(object instanceof Number)) {
                values[i] = "'" + object.toString() + "'";
            }

        }

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
     * Returns a full column
     * @param columnName
     * @param maximumRowCount If >0 , this is the row count limit
     * @return The column
     * @throws NodataFoundException
     */
    public Object[] getColumn(String columnName, int maximumRowCount) throws NodataFoundException {
        ReturnValue data = null;
        if (maximumRowCount > 0) {
            data = freeSelectQuery("SELECT TOP(" + maximumRowCount + ") "
                    + columnName + " FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        } else {
            data = freeSelectQuery("SELECT "
                    + columnName + " FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        }
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return ArrayUtilities.ObjectToSingleColumnArray(data.getData());
        }
    }

    public Object[][] getColumns(String[] columnNames, int maximumRowCount) throws NodataFoundException {
        ReturnValue data = null;
        String columnName = "";
        for (int i = 0; i < columnNames.length; i++) {
            String string = columnNames[i];
            if (i < columnNames.length - 1) {
                columnName += string + ",";
            } else {
                columnName += string;
            }
        }
        if (maximumRowCount > 0) {
            data = freeSelectQuery("SELECT TOP(" + maximumRowCount + ") "
                    + columnName + " FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        } else {
            data = freeSelectQuery("SELECT "
                    + columnName + " FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        }
        if (data.getData().length == 0) {
            throw new NodataFoundException();
        } else {
            return data.getData();
        }
    }

    /**
     *
     * @param context
     * @return
     */
    public QueryHandler setContext(Context context) {
        table = context.getDbIdentity();
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            table = DatabaseConnection.getPrefix() + table;
        }
        this.context = context;
        return this;
    }

    /**
     *
     * @param id
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(int id) throws NodataFoundException {
        ReturnValue data = freeSelectQuery("SELECT * FROM " + table + " WHERE " + table + ".ids = " + id + " AND " + context.getConditions().substring(6, context.getConditions().length()), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context, id);
        } else {
            return data;
        }
    }

    /**
     * No condition checking!
     * @param columns
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue freeSelect(String columns) throws NodataFoundException {
        ReturnValue data = freeSelectQuery("SELECT " + columns + " FROM " + table, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
        } else {
            return data;
        }
    }

    /**
     * This is a convenience method to retrieve data such as
     * <code>select("*", criterias.getKeys(), criterias.getValues())<code/>
     * @param columns
     * @param criterias
     * @return
     * @throws NodataFoundException
     */
    public Object[][] select(String columns, QueryCriteria criterias) throws NodataFoundException {
        return select(columns, criterias.getKeys(), criterias.getValues());
    }

    /**
     *
     * @param columns
     * @param criterias
     * @param time
     * @param timeCol
     * @return
     * @throws NodataFoundException
     */
    public Object[][] select(String columns, QueryCriteria criterias, vTimeframe time, String timeCol) throws NodataFoundException {

        String dateCriterium = table + "." + timeCol + " >= '" + DateConverter.getSQLDateString(time.getStart()) + "' AND " + table + "." + timeCol + " <= '" + DateConverter.getSQLDateString(time.getEnd()) + "'";
        String query = "SELECT " + columns + " FROM " + table + " " + context.getReferences() + " WHERE ";

        for (int i = 0; i < criterias.getKeys().length; i++) {

            Object object = criterias.getValues()[i];
            String column = criterias.getKeys()[i];
            query += table + "." + column + "=" + String.valueOf(object);

            if ((i + 1) != criterias.getValues().length) {
                query += " AND ";
            }
        }
        if (criterias.getKeys().length > 0 && !query.endsWith("AND ")) {
            query += " AND ";
        }
        query += context.getConditions().substring(6, context.getConditions().length()) + " AND ";
        query += dateCriterium;
        query += criterias.getOrder();
        ReturnValue p = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (p.hasData()) {
            return p.getData();
        } else {
            throw new NodataFoundException(context);
        }
    }

    /**
     *
     * @param columns
     * @param criterias
     * @param time
     * @param timeCol
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(String columns, QueryCriteria2 criterias, vTimeframe time, String timeCol) throws NodataFoundException {
        String dateCriterium = table + "." + timeCol + " >= '" + DateConverter.getSQLDateString(time.getStart()) + "' AND " + table + "." + timeCol + " <= '" + DateConverter.getSQLDateString(time.getEnd()) + "'";
        String query = "SELECT " + columns + " FROM " + table + " " + context.getReferences() + " WHERE ";

        if (criterias.getQuery().length() > 6) {
            query += criterias.getQuery() + " AND ";
        }
        query += context.getConditions().substring(6, context.getConditions().length()) + " AND ";
        query += dateCriterium;
        query += criterias.getOrder();
        ReturnValue p = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (p.hasData()) {
            return p;
        } else {
            throw new NodataFoundException(context);
        }
    }

    /**
     * Requires 'dateadded' column
     * @param columns
     * @param criterias
     * @param time
     * @return
     * @throws NodataFoundException 
     */
    public Object[][] select(String columns, QueryCriteria criterias, vTimeframe time) throws NodataFoundException {
        return select(columns, criterias, time, "dateadded");
    }

    /**
     * Requires 'dateadded' column
     * @param columns
     * @param criterias
     * @param time
     * @return
     * @throws NodataFoundException
     */
    public ReturnValue select(String columns, QueryCriteria2 criterias, vTimeframe time) throws NodataFoundException {
        return select(columns, criterias, time, "dateadded");
    }

    /**
     * Convenience method to retrieve * from where the criterias match
     * @param criterias
     * @return
     * @throws mpv5.db.common.NodataFoundException
     */
    public ReturnValue select(QueryCriteria criterias) throws NodataFoundException {
        String query = "SELECT * FROM " + table + " " + context.getReferences() + " WHERE ";
        for (int i = 0; i < criterias.getValues().length; i++) {

            Object object = criterias.getValues()[i];
            String column = criterias.getKeys()[i];
            query += column + "=" + String.valueOf(object);

            if ((i + 1) != criterias.getValues().length) {
                query += " AND ";
            } else {
                query += " AND " + context.getConditions().substring(6, context.getConditions().length());
            }
        }

        query += criterias.getOrder();
        ReturnValue data = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (!data.hasData()) {
            throw new NodataFoundException(context);
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
        ReturnValue data = freeSelectQuery("SELECT * FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
        if (data.getData().length == 0) {
            throw new NodataFoundException(context);
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
        ReturnValue data = freeSelectQuery("SELECT ids FROM " + table + " " + context.getConditions(), mpv5.usermanagement.MPSecurityManager.VIEW, null);
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
     * @param exactMatch 
     * @return
     */
    public Object[] getValuesFor(String[] columns, String needle, String value, boolean exactMatch) {
        String cols = needle;
        if (columns != null) {
            cols = "";
            for (int i = 0; i < columns.length; i++) {
                String string = columns[i];
                cols += string + ",";
            }
            cols = cols.substring(0, cols.length() - 1);
        }
        String f = " = '";
        String g = "'";

        if (!exactMatch) {
            f = " LIKE '%";
            g = "%'";
        }

        if (context != null) {
            if (value == null) {
                return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions().substring(6, context.getConditions().length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            } else {
                return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + " WHERE " + needle + f + value + g + " AND " + context.getConditions().substring(6, context.getConditions().length()), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
            }
        } else if (value == null) {
            return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences(), mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        } else {
            return ArrayUtilities.ObjectToSingleColumnArray(freeSelectQuery("SELECT " + cols + " FROM " + table + " " + context.getReferences() + "  WHERE " + needle + f + value + g, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData());
        }
    }

    /**
     * 
     * @param what
     * @param where
     * @param datecolumn
     * @param zeitraum
     * @return
     */
    public Object[][] select(String what, String[] where, String datecolumn, vTimeframe zeitraum) {
        String dateCriterium = datecolumn + " >= '" + DateConverter.getSQLDateString(zeitraum.getStart()) + "' AND " + datecolumn + " <= '" + DateConverter.getSQLDateString(zeitraum.getEnd()) + "'";
        String query;
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + dateCriterium;
        } else {
            query = "SELECT " + what + " FROM " + table + "  WHERE " + dateCriterium;
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
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

            Object[][] o = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                values.add(Double.valueOf(String.valueOf(o[0][0])));
            } else {
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    /**
     *
     * @param what
     * @param where
     * @param zeitraum
     * @param additionalCondition
     * @return
     */
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

            Object[][] o = freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
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

    /**
     *
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey
     * @param order
     * @param like
     * @return
     */
    public Object[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order, Boolean like) {
//        start();
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

            query = "SELECT " + what + " FROM " + table
                    + " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".ids"
                    + " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " ORDER BY " + table + "." + order;
        } else {
            query = "SELECT " + what + " FROM " + table
                    + " LEFT OUTER JOIN  " + leftJoinTable + " ON "
                    + table + "." + leftJoinKey + " = " + leftJoinTable + ".ids "
                    + "  ORDER BY " + table + "." + order;
        }

        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param date1
     * @param date2
     * @return
     */
    public int selectCountBetween(java.util.Date date1, java.util.Date date2) throws SQLException {
        return selectCount("dateadded", "BETWEEN '" + DateConverter.getSQLDateString(date1) + "' AND '" + DateConverter.getSQLDateString(date2) + "'");
    }

    /**
     *
     * @param newTable
     */
    public void setTable(String newTable) {
        this.table = newTable;
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            this.table = DatabaseConnection.getPrefix() + table;
        }
    }

    /**
     * Sets the table and the context.. dont use this.
     * @param newTable
     */
    public void setTable2(String newTable) {
        this.table = newTable;
        if (DatabaseConnection.getPrefix() != null && DatabaseConnection.getPrefix().equals("null")) {
            this.table = DatabaseConnection.getPrefix() + table;
        }
        this.context = Context.getMatchingContext(newTable);
        if (this.context == null) {
            this.context = Context.DEFAULT;
            this.context.setDbIdentity(newTable);
        }
    }

    /**
     *
     * @return
     */
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
     * @param vals
     * @param uniquecols
     * @return
     */
    public boolean checkUniqueness(QueryData vals, int[] uniquecols) {
        String[] values = vals.getKeys();

        if (uniquecols != null) {
            for (int i = 0; i < uniquecols.length; i++) {
                int j = uniquecols[i];
                Object[][] val = select(values[j], new String[]{values[j], vals.getValue(values[j]).toString(), vals.getValue(values[j]).getWrapper()});
                if (val != null && val.length > 0) {
                    MPView.addMessage(Messages.VALUE_ALREADY_EXISTS + vals.getValue(values[j]).toString());
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
        QueryData t = new QueryData();
        t.add(column, value);
        return checkUniqueness(t, new int[]{0});
    }
    private static int RUNNING_JOBS = 0;
    private static Thread JOB_WATCHDOG;

    public synchronized void stop() {
        if (!runInBackground) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    try {//Avoid Cursor flickering
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (RUNNING_JOBS <= 1) {
                        comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        MPView.setProgressReset();
                    }
                    RUNNING_JOBS--;
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    public synchronized void start() {
        if (JOB_WATCHDOG == null) {
            JOB_WATCHDOG = new Thread(new Watchdog());
            JOB_WATCHDOG.start();
        }
        if (!runInBackground) {
            RUNNING_JOBS++;
            if (RUNNING_JOBS > 5) {
                comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            MPView.setProgressMaximumValue(RUNNING_JOBS);
        }
    }

    /**
     * Returns exactly one value from a column
     * @param columnName The column where to take the result from
     * @param compareColumn The column to compare
     * @param compareValue The value to compare to
     * @return A value
     * @throws NodataFoundException
     */
    public Object getValue(String columnName, String compareColumn, Object compareValue) throws NodataFoundException {
        String quote = "";
        if (compareValue instanceof String) {
            quote = "'";
        } else if (compareValue instanceof Date) {
            quote = "'";
            compareValue = DateConverter.getSQLDateString((Date) compareValue);
        }
        return selectLast(columnName, new String[]{compareColumn, compareValue.toString(), quote})[0];
    }

    /**
     * Returns map view of the found {@link DatabaseObject} with the given ID in the current {@link Context}
     * @param id
     * @return  A HashMap
     * @throws NodataFoundException If no Object with the given ID was found in the current Context
     */
    public Map<String, String> getValuesFor(int id) throws NodataFoundException {
        ReturnValue rv = select(id);
        String[] cols = rv.getColumnnames();
        Object[][] data = rv.getData();
        HashMap<String, String> map = new HashMap<String, String>(data[0].length);
        for (int i = 0; i < data[0].length; i++) {
            map.put(cols[i], String.valueOf(data[0][i]));
        }
        return map;
    }
    /**
     * This string is used to replace backslashes in sql queries (if escaping is enabled)
     */
    public static String BACKSLASH_REPLACEMENT_STRING = "<removedbackslash>";

    private String escapeBackslashes(String query) {
        return query.replace("\\", BACKSLASH_REPLACEMENT_STRING);
    }

    private String rescapeBackslashes(String query) {
        return query.replace(BACKSLASH_REPLACEMENT_STRING, "\\");
    }

    /**
     * Checks if any data which would match the given criteria is existing in the database
     * @param qc
     * @return true if matching data was found
     */
    public boolean checkExistance(QueryCriteria2 qc) {
        try {
            return select("ids", qc, new vTimeframe(new Date(0), new Date())).hasData();
        } catch (NodataFoundException ex) {
            return false;
        }
    }

    class Watchdog implements Runnable {

        @Override
        public void run() {
            int oldValue = 0;
            while (true) {
                if (RUNNING_JOBS != oldValue) {
                    MPView.setProgressValue(RUNNING_JOBS);
                    oldValue = RUNNING_JOBS;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

//    public String getNextStringNumber(String colName) {
//        Integer s = getNextIndexOfStringCol(colName, null);
//        return originalvalue.substring(0, substringcount) + s;
//    }
    /**
     * Count the rows of the current table
     * @return
     */
    public Integer getCount() throws SQLException {
        int i = selectCount(null, null);
        i = (i < 0) ? -i : i;
        return i;
    }

    /**
     * Insert values to db
     * @param what  : {set, value, "'"}
     *   this.insert("name,wert", "'Sprache (Waehrung, z.B. Schweiz:  de_CH' ,'de_DE'");
     * @param jobmessage The message to be displayd after a successful run
     * @return id of inserted row
     */
    public int insert(QueryData what, String jobmessage) {
        String query = query = "INSERT INTO " + table + " (" + what.getKeysString() + " ) VALUES (" + what.getValuesString() + ") ";
        return freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.CREATE_OR_DELETE, jobmessage).getId();
    }

    /**
     *  This is a special insert method for the History feature
     * @param message
     * @param username
     * @param dbidentity
     * @param item
     * @param groupid
     */
    public synchronized void insertHistoryItem(String message, String username, String dbidentity, int item, int groupid) {
        try {
            if (psHistory == null) {
                try {
                    String query = "INSERT INTO " + Context.getHistory().getDbIdentity() + " (cname, username, dbidentity, intitem, groupsids, dateadded) VALUES (?, ?, ?, ?, ?, ?)";
                    psHistory = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psHistory.setString(1, message);
            psHistory.setString(2, username);
            psHistory.setString(3, dbidentity);
            psHistory.setInt(4, item);
            psHistory.setInt(5, groupid);
            psHistory.setDate(6, new java.sql.Date(new java.util.Date().getTime()));
            psHistory.execute();
        } catch (SQLException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private static PreparedStatement psHistory;

    /**
     * This is a special insert method for the Lock feature
     * @param context
     * @param id
     * @param user
     * @return
     * @throws UnableToLockException
     */
    synchronized boolean insertLock(Context context, int id, User user) throws UnableToLockException {
        try {
            if (psLock == null) {
                try {
                    String query = "INSERT INTO " + Context.getLock().getDbIdentity() + " (cname, rowid, usersids) VALUES (?, ?, ?)";
                    psLock = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psLock.setString(1, context.getDbIdentity());
            psLock.setInt(2, id);
            psLock.setInt(3, user.__getIDS());
            return psLock.execute();
        } catch (SQLException ex) {
            throw new UnableToLockException(context, id, user);
        }
    }
    private static PreparedStatement psLock;

    void removeLock(Context context, int id, User user) {
        try {
            if (psUnLock == null) {
                try {
                    String query = "DELETE FROM " + Context.getLock().getDbIdentity() + " WHERE cname = ? AND rowid = ? AND usersids = ?";
                    psUnLock = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                } catch (SQLException ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            psUnLock.setString(1, context.getDbIdentity());
            psUnLock.setInt(2, id);
            psUnLock.setInt(3, user.__getIDS());
            psUnLock.execute();
        } catch (SQLException ex) {
            Log.Debug(ex);
        }
    }
    private static PreparedStatement psUnLock;

    /**
     * Updates the given column at the row with the given id
     * @param columnName
     * @param id
     * @param value
     */
    public void update(String columnName, Integer id, Object value) {
        QueryData d = new QueryData();
        d.add(columnName, value);
        update(d, new String[]{"ids", id.toString(), ""}, null);
    }

    /**
     * 
     * @param what  : {set, values}
     * @param where : {value, comparison, "'"}
     * @param jobmessage
     */
    public void update(QueryData what, String[] where, String jobmessage) {

        String query;
        String[] a = what.getKeys();
        String c = "";

        for (int i = 0; i < a.length; i++) {
            c += a[i] + " = " + what.getValue(a[i]).getWrapper() + what.getValue(a[i]).toString() + what.getValue(a[i]).getWrapper() + ", ";
        }

        if (c.length() > 2) {
            c = c.substring(0, c.length() - 2);
        }

        query = "UPDATE " + table + " SET " + c + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2];
        freeUpdateQuery(query, mpv5.usermanagement.MPSecurityManager.EDIT, jobmessage);
    }

    /**
     *
     * @param q The data
     * @param criteria Only the "ids" criterium will be used
     * @param jobmessage
     */
    public void update(QueryData q, QueryCriteria criteria, String jobmessage) {
        update(q, Integer.valueOf(criteria.getValue("ids").toString()), jobmessage);
    }

    /**
     *
     * @param q The data
     * @param doId The row id
     * @param jobmessage
     */
    public void update(QueryData q, int doId, String jobmessage) {
        update(q, new String[]{"ids", String.valueOf(doId), ""}, jobmessage);
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
//        start();
        String query;
        if (where != null && where[0] != null && where[1] != null) {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + table + "." + where[0] + " = " + where[2] + where[1] + where[2] + " AND " + context.getConditions().substring(6, context.getConditions().length());
        } else {
            query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + context.getConditions().substring(6, context.getConditions().length());
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     *
     * @param what
     * @param whereColumns  {"column1","column2"}
     * @param haveValues {"value1",value2<any/>}
     * @return
     */
    public Object[][] select(String what, String[] whereColumns, Object[] haveValues) {
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE ";
        for (int i = 0; i < haveValues.length; i++) {

            Object object = haveValues[i];
            String column = whereColumns[i];
            query += table + "." + column + "=" + String.valueOf(object);

            if ((i + 1) != haveValues.length) {
                query += " AND ";
            } else {
                query += " AND " + context.getConditions().substring(6, context.getConditions().length());
            }
        }
        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
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
//        start();
        String l1 = "";
        String l2 = "";
        String k = " = ";
        String j = "";

        if (order == null) {
            order = "ids ";
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
            wher = " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " AND " + context.getConditions().substring(6, context.getConditions().length()) + " ";
        }
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + wher + ord;

        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * Creates a {@link PreparedStatement}
     * @param columns 
     * @param conditionColumns 
     * @param order 
     * @param like 
     * @return A {@link PreparedStatement}
     * @throws SQLException
     */
    public PreparedStatement buildPreparedSelectStatement(String columns[], String[] conditionColumns, String order, boolean like) throws SQLException {
        String cols = "";
        if (columns != null && columns.length > 0) {
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i];
                cols += "," + column;
            }
            cols = cols.substring(1);
        } else {
            cols = "*";
        }
        String conds = "";
        if (conditionColumns != null && conditionColumns.length > 0) {
            for (int i = 0; i < conditionColumns.length; i++) {
                String string = conditionColumns[i];
                if (!like) {
                    conds += string + " LIKE ? AND ";
                } else {
                    conds += string + " = ? AND ";
                }
            }
            conds = " WHERE " + conds.substring(0, conds.length() - 4);
            if (context.getGroupRestrictionSQLString() != null) {
                conds += " AND " + context.getGroupRestrictionSQLString();
            }
            if (context.getNoTrashSQLString() != null) {
                conds += " AND " + mpv5.db.common.Context.getItem().getNoTrashSQLString();
            }
        }
        String query = "SELECT " + cols + " FROM " + table + conds;
        if (order != null) {
            query += " ORDER BY " + order;
        }

        return sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
    }

    /**
     * Executes the given statement
     * @param statement
     * @param values Length must match the conditionColumns argument of the build call of the statement
     * @return
     * @throws java.sql.SQLException
     * @deprecated possible not returning the desired results yet
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public ReturnValue executeStatement(PreparedStatement statement, Object[] values) throws SQLException {

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                Object object = values[i];
                statement.setObject(i + 1, object);
            }
        }

        ResultSet set = statement.executeQuery();
        ReturnValue val = new ReturnValue();
        ArrayList spalten = new ArrayList();
        ArrayList zeilen = new ArrayList();
        ResultSetMetaData rsmd = set.getMetaData();
        ResultSet keys = statement.getGeneratedKeys();
        int id = 0;
        if (keys != null && keys.next()) {
            id = keys.getInt(1);
        }
        String[] columnnames = new String[rsmd.getColumnCount()];
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columnnames[i - 1] = rsmd.getColumnName(i);
        }

        while (set.next()) {
            spalten = new ArrayList();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                spalten.add(set.getObject(i));
            }
            zeilen.add(spalten);
        }
        Object[][] data = new Object[zeilen.size()][spalten.size()];
        ArrayList z;

        for (int h = 0; h < zeilen.size(); h++) {
            z = (ArrayList) zeilen.get(h);
            for (int i = 0; i < spalten.size(); i++) {
                data[h][i] = z.get(i);
            }
        }
        val.setData(data);
        val.setColumnnames(columnnames);
        val.setId(id);
        return val;
    }

    /**
     * Deletes the given data permanently from the database
     * @param where : {value, comparison, "'"}
     *                eg new String[][]{{"ids", "8" , ""},{"ids", "9",""}}
     * @param jobmessage
     * @return True if the deletion was not terminated by constraints
     */
    public boolean delete(String[][] where, String jobmessage) {
//        start();
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
//            if (object instanceof Number) {
            query += column + "=" + object.toString();
//            } else {
//                query += column + "='" + object.toString() + "'";
//            }

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
     * Deletes the given data permanently from the database
     * @param criterias
     */
    public void delete(QueryCriteria criterias) {
        delete(criterias.getKeys(), criterias.getValues(), null);
    }

    /**
     * 
     * @param tablename
     * @return a clone of this ConnectionHandler (with database connection)
     */
    public QueryHandler clone(String tablename) {
        QueryHandler theClone = null;
        this.context = Context.getMatchingContext(tablename);
        if (context == null) {
            context = Context.DEFAULT;
        }
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(tablename);
            theClone.runInBackground = false;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     * Returns a new db connection without any table or Context association.
     * @return A QueryHandler object
     */
    public static QueryHandler getConnection() {
        QueryHandler qh = null;
        try {
            qh = (QueryHandler) QueryHandler.instanceOf().clone();
            qh.setContext(Context.DEFAULT);
            qh.runInBackground = false;
            return qh;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
            theClone.runInBackground = false;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     *
     * @param context
     * @param limit
     * @return
     */
    public QueryHandler clone(Context context, int limit) {
        QueryHandler theClone = null;
        this.context = context;
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(context.getDbIdentity());
            theClone.setLimit(limit);
            theClone.runInBackground = false;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     *
     * @param context
     * @param limit Set a (temporary) rowlimit
     * @param inBackground If true, the main view is NOT notified about the jobs
     * @return
     */
    public QueryHandler clone(Context context, int limit, boolean inBackground) {
        QueryHandler theClone = null;
        this.context = context;
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(context.getDbIdentity());
            theClone.setLimit(limit);
            theClone.runInBackground = inBackground;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return theClone;
    }

    /**
     * 
     * @param c
     * @param viewToBeNotified
     * @return
     */
    public QueryHandler clone(Context c, DataPanel viewToBeNotified) {
        this.viewToBeNotified = viewToBeNotified;
        QueryHandler theClone = null;
        this.context = c;
        try {
            theClone = (QueryHandler) this.clone();
            theClone.setTable(context.getDbIdentity());
            theClone.runInBackground = false;
        } catch (CloneNotSupportedException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
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
//        start();
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
            wher = where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2] + " AND " + wher + " AND " + context.getConditions().substring(6, context.getConditions().length());
            if (where.length > 3) {
                wher = wher + " AND " + where[3] + " " + k + " " + where[5] + l + where[4] + l + where[5] + " " + context.getConditions() + " ";
            }
        }
        String query = "SELECT " + what + " FROM " + table + " " + context.getReferences() + " WHERE " + wher + ord;

        return freeSelectQuery(query, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
    }

    /**
     * Returns the count of rows which match the condition.
     * <br/>Example:
     * <br/>selectCount("cname", "LIKE %%")
     * @param what
     * @param condition
     * @return
     * @throws SQLException
     */
    public int selectCount(String what, String condition) throws SQLException {
        String wher = "";
        start();

        if (condition != null) {
            wher = " WHERE " + what + " " + condition;
        }
        String query = "SELECT COUNT(1) AS rowcount FROM " + table + " " + wher;
        String message = "Database Error (SelectCount:COUNT):";
        stm = null;
        resultSet = null;

        Log.Debug(this, query);
        try {
            // Select-Anweisung ausführen
            stm = sqlConn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            if (resultSet.first()) {
                Log.Debug(this, "Count " + resultSet.getInt("rowcount"));
                stop();
                return resultSet.getInt("rowcount");
            } else {
                stop();
                return 0;
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            stop();
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(ex);
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
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        }

        if (TypeConversion.stringToBoolean(LocalSettings.getProperty(LocalSettings.DBESCAPE))) {
            query = escapeBackslashes(query);
        }

        updateStatistics(query);

        start();
        if (table != null) {
            query = query.replace("%%tablename%%", table);
        }
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
            Log.Debug(this, query);
            if (log != null) {
                log.append("\n " + query + "\n\n_________________________________________________________________________________\n");
            }
            bool = stm.execute(query);

            try {
                ResultSet keys = stm.getGeneratedKeys();
                if (keys != null && keys.next()) {
                    id = keys.getInt(1);
                }
            } catch (SQLException sQLException) {
                Log.Debug(sQLException);
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
                        Object object = resultSet.getObject(i);
                        if (object instanceof String && TypeConversion.stringToBoolean(LocalSettings.getProperty(LocalSettings.DBESCAPE))) {
                            object = rescapeBackslashes((String) object);
                        }
                        spalten.add(object);
                    }
                    zeilen.add(spalten);
                }
                data = new Object[zeilen.size()][spalten.size()];

                for (int h = 0; h < zeilen.size(); h++) {
                    z = (ArrayList) zeilen.get(h);
                    for (int i = 0; i < spalten.size(); i++) {
                        data[h][i] = z.get(i);
                        if (log != null) {
                            log.append("| " + data[h][i] + "\t");
                        }
                    }
                    if (log != null) {
                        log.append("\n ");
                    }
                }
            }
            retval = new ReturnValue(id, data, columnnames);
            if (log != null) {
                log.append("\n " + stm.getUpdateCount() + " rows affected.");
            }

        } catch (SQLException ex) {

            jobmessage = Messages.ERROR_OCCURED.toString();
            Log.Debug(this, message + ex.getMessage());
//            retval = null;
            if (log != null) {
                log.append("\n " + ex.getMessage());
            }
        } finally {
            // Alle Ressourcen wieder freigeben
            stop();
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {

                    jobmessage = Messages.ERROR_OCCURED.toString();
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

                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
        }

        if (jobmessage != null && retval != null) {
            MPView.addMessage(jobmessage);
            retval.setMessage(jobmessage);
        }
//
//        if (log != null && retval.hasData()) {
//            Object[][] data1 = retval.getData();
//            for (int i = 0; i < data1.length; i++) {
//                Object[] objects = data1[i];
//                log.append(" \n");
//                for (int j = 0; j < objects.length; j++) {
//                    Object object = objects[j];
//                    log.append("\t" + object);
//                }
//            }
//        }

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
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        } else {
//              Log.Debug(this, Messages.SECURITYMANAGER_ALLOWED+
//                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
        }
        updateStatistics(query);
        start();

        if (TypeConversion.stringToBoolean(LocalSettings.getProperty(LocalSettings.DBESCAPE))) {
            query = escapeBackslashes(query);
        }

        if (table != null) {
            query = query.replace("%%tablename%%", table);
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
            Log.Debug(this, "freeUpdateQuery::" + query);
            if (log != null) {
                log.append("\n " + query);
            }
            stm.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            if (log != null) {
                log.append("\n " + stm.getUpdateCount() + " rows affected.");
            }


            try {
                keys = stm.getGeneratedKeys();

                if (keys != null && keys.next()) {
                    id = keys.getInt(1);
                }
            } catch (SQLException sQLException) {
                Log.Debug(sQLException);
            }

            retval = new ReturnValue(id, null, null);

        } catch (SQLException ex) {

            jobmessage = Messages.ERROR_OCCURED.toString();
            Log.Debug(this, message + ex.getMessage());
            if (log != null) {
                log.append("\n " + ex.getMessage());
            }
        } finally {
            // Alle Ressourcen wieder freigeben
            stop();
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {

                    jobmessage = Messages.ERROR_OCCURED.toString();
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

                    Log.Debug(this, message + ex.getMessage());
                    if (log != null) {
                        log.append(" \n" + ex.getMessage());
                    }
                }
            }
        }

        if (jobmessage != null) {
            MPView.addMessage(jobmessage);
            retval.setMessage(jobmessage);
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
    public ReturnValue freeSelectQuery(String query, int action, String jobmessage) {

        if (table != null) {
            query = query.replace("%%tablename%%", table);
        }

        if (!mpv5.usermanagement.MPSecurityManager.check(context, action)) {
            Log.Debug(this, Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            Popup.warn(Messages.SECURITYMANAGER_DENIED
                    + mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity());
            return new ReturnValue(-1, new Object[0][0], new String[0]);
        } else {
//              Log.Debug(this, Messages.SECURITYMANAGER_ALLOWED+
//                    mpv5.usermanagement.MPSecurityManager.getActionName(action) + Messages.CONTEXT + context.getDbIdentity()
//                    );
        }
        updateStatistics(query);
        start();

        if (TypeConversion.stringToBoolean(LocalSettings.getProperty(LocalSettings.DBESCAPE))) {
            query = escapeBackslashes(query);
        }
        String message = "Database Error (selectFreeQuery) :";

        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        Object[][] data = null;
        ArrayList z;
        int id = -1;
        String[] columnnames = null;

        try {
            stm = sqlConn.createStatement();
            if (ROW_LIMIT != null && ROW_LIMIT.intValue() >= 0) {
                stm.setMaxRows(ROW_LIMIT.intValue());
            }
            if (this.limit > 0) {
                stm.setMaxRows(limit);
            }

            Log.Debug(this, "freeSelectQuery::" + query);
            try {
                resultSet = stm.executeQuery(query);
            } catch (SQLException sQLException) {
                Log.Debug(sQLException);
                throw sQLException;
            }
            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();
            rsmd = resultSet.getMetaData();

            try {
                ResultSet keys = stm.getGeneratedKeys();
                if (keys != null && keys.next()) {
                    id = keys.getInt(1);
                }
            } catch (SQLException sQLException) {
                Log.Debug(sQLException);
            }

            columnnames = new String[rsmd.getColumnCount()];
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnnames[i - 1] = rsmd.getColumnName(i);
            }

//            Log.PrintArray(columnnames);

            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    Object object = resultSet.getObject(i);
                    if (object instanceof String && TypeConversion.stringToBoolean(LocalSettings.getProperty(LocalSettings.DBESCAPE))) {
                        object = rescapeBackslashes((String) object);
                    }
                    spalten.add(object);
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
            Log.Debug(this, "Datenbankfehler: " + query);
            Log.Debug(this, message + ex.getMessage());
            Popup.error(ex);
            jobmessage = Messages.ERROR_OCCURED.toString();
        } finally {
            // Alle Ressourcen wieder freigeben
            stop();
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    jobmessage = Messages.ERROR_OCCURED.toString();
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, message + ex.getMessage());
                    Popup.error(ex);
                }
            }
        }

        if (jobmessage != null) {
            MPView.addMessage(jobmessage);
        }
//        Log.PrintArray(data);
        return new ReturnValue(id, data, columnnames, jobmessage);
    }

    /**
     * This method inserts a file into a "filename<unique/>, filedata" table
     * @param file
     * @return The UNIQUE name of the inserted file in db
     * @throws java.io.FileNotFoundException
     */
    public synchronized String insertFile(final File file) throws FileNotFoundException {

        String name = null;
        String query = "INSERT INTO " + table + "(cname, data, dateadded, filesize, intaddedby) VALUES (?, ?, ?, ?, ?)";
        String jobmessage = null;
        Log.Debug(this, "Adding file: " + file.getName());
        backgroundSqlQuery j;

        try {
            start();
            int fileLength = (int) file.length();
            name = new RandomText(23).getString();
            java.io.InputStream fin = new java.io.FileInputStream(file);
            PreparedStatement ps = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(5, mpv5.db.objects.User.getCurrentUser().getID());
            ps.setLong(4, file.length());
            ps.setDate(3, new java.sql.Date(new Date().getTime()));
            ps.setBinaryStream(2, fin, fileLength);
            j = new backgroundSqlQuery(ps);
            j.execute();

            Log.Debug(this, "File added: " + name);

        } catch (Exception ex) {
            Log.Debug(this, "Datenbankfehler: " + query);
            Log.Debug(this, ex);
            Popup.error(ex);
            jobmessage = Messages.ERROR_OCCURED.toString();
        } finally {

            stop();
        }
        if (viewToBeNotified != null) {
            viewToBeNotified.refresh();
        }
        if (jobmessage != null) {
            MPView.addMessage(jobmessage);
        }
        return name;

    }

    /**
     * This method returns a list of files from a "filename, filedata" table
     * @param filename The unique filename
     * @return A list with temporary files
     * @throws IOException
     */
    public synchronized ArrayList<File> retrieveFiles(String filename) throws IOException {
        start();
        String query = "SELECT data, filesize FROM " + table + " WHERE cname= '" + filename + "'";
        String jobmessage = null;
        ArrayList<File> list = null;
        try {
            stm = sqlConn.createStatement();
            list = new ArrayList<File>();

            if (ROW_LIMIT != null && ROW_LIMIT.intValue() >= 0) {
                stm.setMaxRows(ROW_LIMIT.intValue());
            }
            if (this.limit > 0) {
                stm.setMaxRows(limit);
            }

            ResultSet rs = stm.executeQuery(query);
            Log.Debug(this, query);
            while (rs.next()) {

                MPView.setProgressMaximumValue(rs.getInt(2));
                byte[] buffer = new byte[1024];
                File f = FileDirectoryHandler.getTempFile();
                BufferedInputStream inputStream = new BufferedInputStream(rs.getBinaryStream(1), 1024);
                FileOutputStream outputStream = new FileOutputStream(f);
                int readBytes = 0;
                int read = 0;
                while (readBytes != -1) {
                    readBytes = inputStream.read(buffer, 0, buffer.length);
                    read += buffer.length;
                    if (readBytes != -1) {
                        outputStream.write(buffer, 0, readBytes);
                    }
                    MPView.setProgressValue(read);
                }
                inputStream.close();
                outputStream.close();
                list.add(f);
                MPView.setProgressReset();
            }

        } catch (SQLException ex) {
            Log.Debug(this, "Datenbankfehler: " + ex.getMessage());
            Popup.error(ex);
            jobmessage = Messages.ERROR_OCCURED.toString();
        } finally {
            // Alle Ressourcen wieder freigeben
            stop();
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    jobmessage = Messages.ERROR_OCCURED.toString();
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(this, ex.getMessage());
                    Popup.error(ex);
                }
            }
        }
        if (viewToBeNotified != null) {
            viewToBeNotified.refresh();
        }
        if (jobmessage != null) {
            MPView.addMessage(jobmessage);
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
    public synchronized File retrieveFile(String name, File targetFile) {
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
                k = FileDirectoryHandler.copyFile(list.get(0), targetFile.getParentFile(), targetFile.getName(), false);
            } catch (IOException ex) {
                Log.Debug(ex);
                return null;
            }
            File f = new File(k);
            return f;
        }
    }

    /**
     * A convenience method to retrieve one file from db, or null of no file
     * with the specified name is available.
     * @param name
     * @return A virtual file or NULL
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
     * This is a convenience method to insert files
     * @param file The file
     * @param dataOwner The owner
     * @param descriptiveText Describe the file
     * @return True if the insert was a success
     */
    public boolean insertFile(File file, DatabaseObject dataOwner, SaveString descriptiveText) {
        try {
            Context tc = null;
            if (dataOwner.getContext().equals(Context.getContact())) {
                tc = Context.getFilesToContacts();
            } else if (dataOwner.getContext().equals(Context.getItem())) {
                tc = Context.getFilesToItems();
            } else if (dataOwner.getContext().equals(Context.getProduct())) {
                tc = Context.getFilesToProducts();
            } else if (dataOwner.getContext().equals(Context.getTemplate())) {
                tc = Context.getTemplate();
            } else {
                throw new UnsupportedOperationException("Not yet implemented for " + dataOwner.getContext());
            }
            new backgroundFileInsert(file, dataOwner, descriptiveText, tc).execute();
            return true;
        } catch (Exception e) {
            Log.Debug(e);
            return false;
        }
    }
    private static HashMap<String, Integer> stats = new HashMap<String, Integer>();

    private synchronized void updateStatistics(String query) {
        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
            if (stats.containsKey(query)) {
                int old = stats.get(query);
                stats.put(query, old + 1);
            } else {
                stats.put(query, 1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized String getStatistics() {
        List keys = new LinkedList(stats.keySet());
        Collections.sort(keys, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                return (stats.get(o1)).compareTo(((Integer) stats.get(o2)));
            }
        });
        Iterator it = keys.iterator();
        Log.Debug(this, "Database statistics:");
        String str = "";
        String s;
        while (it.hasNext()) {
            s = it.next().toString();
            str += "Count: " + stats.get(s) + " for query: " + s + "\n";
        }

        return str;
    }

    class backgroundSqlQuery extends SwingWorker<Void, Void> {

        private PreparedStatement ps;

        public backgroundSqlQuery(PreparedStatement ps) {
            this.ps = ps;
            this.addPropertyChangeListener(new changeListener());
        }

        @Override
        protected Void doInBackground() {

            setProgress(0);
            try {
                Object obj = new Object();
                synchronized (obj) {
                    ps.execute();
                    if (!sqlConn.getAutoCommit()) {
                        sqlConn.commit();
                    }
                }
            } catch (SQLException ex) {
                Log.Debug(this, ex);
                Popup.error(ex);
            } finally {
                MPView.setProgressReset();
            }
            return null;
        }

        @Override
        public void done() {
            setProgress(100);
            if (viewToBeNotified != null) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        viewToBeNotified.refresh();
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        }

        class changeListener implements PropertyChangeListener {

            /**
             * Invoked when task's progress property changes.
             */
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    int progress = (Integer) evt.getNewValue();
                    Log.Debug(this, "Progress changed to: " + progress);
                    if (progress == 0) {
                        MPView.setProgressRunning(true);
                    } else if (progress == 100) {
                        MPView.setProgressReset();
                    }
                }
            }
        }
    }

    class backgroundFileInsert extends SwingWorker<String, Void> {

        private File file;
        private String name;
        private DatabaseObject dataOwner;
        private String descriptiveText;
        private final Context tcontext;

        public backgroundFileInsert(File file, DatabaseObject owner, SaveString description, Context tcontext) {
            this.addPropertyChangeListener(new changeListener());
            this.file = file;
            this.dataOwner = owner;
            this.descriptiveText = description.toString();
            this.tcontext = tcontext;
        }

        @Override
        protected String doInBackground() {

            Object obj = new Object();
            synchronized (obj) {
//                setProgress(0);
                String query = "INSERT INTO " + table + "(cname, data, dateadded, filesize) VALUES (?, ?, ?, ?)";
                String jobmessage = null;
                Log.Debug(this, "Adding file: " + file.getName());
                MPView.addMessage(Messages.PROCESSING + file.getName());

                try {
                    int fileLength = (int) file.length();
                    name = new RandomText(23).getString();
                    java.io.InputStream fin = new java.io.FileInputStream(file);
                    PreparedStatement ps = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setLong(4, file.length());
                    ps.setDate(3, new java.sql.Date(new Date().getTime()));
                    ps.setBinaryStream(2, fin, fileLength);
                    ps.execute();
                    if (!sqlConn.getAutoCommit()) {
                        sqlConn.commit();
                    }
                } catch (Exception ex) {
                    MPView.setProgressReset();
                    Log.Debug(this, "Datenbankfehler: " + query);
                    Log.Debug(this, ex);
                    Popup.error(ex);
                    jobmessage = Messages.ERROR_OCCURED.toString();
                }

                if (jobmessage != null) {
                    MPView.addMessage(jobmessage);
                }
            }

            return name;
        }

        @Override
        public void done() {
            QueryData x;
            try {
                String filename = file.getName();
                String fileextension = (filename.lastIndexOf(".") == -1) ? "" : filename.substring(filename.lastIndexOf(".") + 1, filename.length());

                x = new QueryData(new String[]{"cname,filename, description, dateadded", file.getName() + "," + get() + "," + descriptiveText + "," + DateConverter.getTodayDBDate()});
                if (!dataOwner.getContext().equals(Context.getTemplate())) {
                    x.add(dataOwner.getType() + "sids", dataOwner.__getIDS());
                }
                x.add("intaddedby", mpv5.db.objects.User.getCurrentUser().__getIDS());
                x.add("intsize", file.length());
                x.add("mimetype", fileextension);
                QueryHandler.instanceOf().clone(tcontext).insert(x, Messages.FILE_SAVED + file.getName());
                MPView.addMessage(Messages.FILE_SAVED + file.getName());
                if (viewToBeNotified != null) {
                    viewToBeNotified.refresh();
                }
            } catch (InterruptedException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                mpv5.logging.Log.Debug(ex);//Logger.getLogger(QueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        class changeListener implements PropertyChangeListener {

            /**
             * Invoked when task's progress property changes.
             */
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(Thread.currentThread().getName() + evt.getNewValue());
                if ("state".equals(evt.getPropertyName())) {

                    Log.Debug(this, "Progress changed to: " + evt.getNewValue());
                    if (StateValue.STARTED == evt.getNewValue()) {
                        MPView.setProgressRunning(true);
                    } else if (StateValue.DONE == evt.getNewValue()) {
                        MPView.setProgressReset();
                    }
                }
            }
        }
    }
}
