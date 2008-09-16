/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp4.datenbank.verbindung;

import java.awt.Cursor;

import java.sql.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import mp4.items.Popup;
import mp4.logs.*;
import mp4.utils.datum.DateConverter;
import mp4.utils.datum.vTimeframe;

/**
 *
 * @author anti
 */
public abstract class Query implements mp4.datenbank.installation.Tabellen {

    private Connection conn = null;
    private Statement stm = null;
    public String[] resultArray = null;
    private ResultSet resultSet = null;
    private String table = "";
    public String resultString = null;
    private String query;
    private String message;
    private String originalvalue = "";
    private int startcount = 0;
    private int resultCount;
    private int substringcount = 0;
    private static JFrame comp = new JFrame();
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

    public Query(String table) throws Exception {
        this.table = table;
        this.conn = Conn.getConnection();
    }

    /**
     * Free SQL Statement!
     * @param string
     * @return 
     */
    @SuppressWarnings("unchecked")
    public boolean freeQuery(String string) {

        start();
        query = string;
        message = "Database Error (freeQuery) :";
        stm = null;
        resultSet = null;
        boolean bool;
        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement();
            Log.Debug(query, true);
            bool  = stm.execute(query);
            stop();
            return bool;
        } catch (SQLException ex) {
            stop();
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    stop();
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    stop();
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        return false;
    }
    /**/

    public String[][] selectBetween(String what, String[] where, vTimeframe zeitraum) {

        String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(zeitraum.getStart()) + "' AND '" + DateConverter.getSQLDateString(zeitraum.getEnd()) + "'";

        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + " AND deleted = 0 " + str;
        } else {
            query = "SELECT " + what + " FROM " + table + " WHERE deleted = 0 " + str;
        }
        message = "Database Error (select) :";

        return selectFreeQuery(query, message);
    }

    public ArrayList<Double> selectMonthlySums(String what, String[] where, vTimeframe zeitraum, String additionalCondition) {

        Date temdate = zeitraum.getStart();
        ArrayList<Double> values = new ArrayList();

        do {
            String str = "AND datum BETWEEN '" + DateConverter.getSQLDateString(temdate) + "' AND '" + DateConverter.getSQLDateString(DateConverter.addMonth(temdate)) + "'";

            if (where != null) {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + " AND deleted = 0 " + str + " " + additionalCondition;
            } else {
                query = "SELECT SUM(" + what + ") FROM " + table + " WHERE deleted = 0 " + str + " " + additionalCondition;
            }

            String[][] o = selectFreeQuery(query, message);
            if (o != null && o[0][0] != null && !o[0][0].equals("null")) {
                Log.Debug("Summe: " + o[0][0]);
                values.add(Double.valueOf(o[0][0]));
            } else {
                Log.Debug("Summe: " + 0);
                values.add(0d);
            }
            temdate = DateConverter.addMonth(temdate);
        } while (temdate.before(zeitraum.getEnd()));

        return values;

    }

    /**
     * Free SQL Select Statement!
     * @param query 
     * @param message 
     * @return Your Data
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public String[][] selectFreeQuery(String query, String message) {
        start();
        if (message == null) {
            message = "Database Error (selectFreeQuery) :";
        }
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String[][] p = null;
        ArrayList z;

        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement();
            Log.Debug(query, true);
            resultSet = stm.executeQuery(query);
            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();
            rsmd = resultSet.getMetaData();

            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));
                }
                zeilen.add(spalten);
            }
            p = new String[zeilen.size()][spalten.size()];

            for (int h = 0; h < zeilen.size(); h++) {
                z = (ArrayList) zeilen.get(h);
                for (int i = 0; i < spalten.size(); i++) {
                    p[h][i] = String.valueOf(z.get(i));
                }
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        return p;
    }

    public String[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order, Boolean like) {
        start();

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
                    " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".id" +
                    " WHERE " + table + "." + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " AND " + table + ".deleted = 0 ORDER BY " + order;
        } else {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN  " + leftJoinTable + " ON " +
                    table + "." + leftJoinKey + " = " + leftJoinTable + ".id " +
                    " WHERE " + table + ".deleted = 0  ORDER BY " + order;
        }

        return selectFreeQuery(query, null);
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

    private int getCurrentIndex() {
        return getNextIndexOfIntCol("id") - 1;
    }

    private void stop() {
        comp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void start() {
        comp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }

    public String getNextStringNumber(String colName) {
        Integer s = getNextIndexOfStringCol(colName);
        return originalvalue.substring(0, substringcount) + s;
    }

    public Integer getCount() {
        start();

        query = "SELECT ALL COUNT(1) FROM " + table;
        message = "Database Error (getNextIndex:COUNT):";

        int i = selectCount(null, null);
        stop();

        i = (i < 0) ? -i : i;
        return i;
    }

    /**
     * 
     * @param colName 
     * @return the next 
     */
    public Integer getNextIndexOfStringCol(String colName) {

        start();
        query = "SELECT " + colName + " FROM " + table + " ORDER BY ID ASC";
        message = "Database Error (getNextIndex):";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String index = null;
        Integer i = null;


        Log.Debug(query, true);

        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);
            rsmd = resultSet.getMetaData();

            if (resultSet.last()) {
                index = resultSet.getString(colName);
                originalvalue = index;

                while (i == null && index.length() > 0) {
                    try {
                        i = Integer.valueOf(index);

                    } catch (NumberFormatException numberFormatException) {
                        substringcount++;
                        index = index.substring(1, index.length());
                        i = null;
                    }
                }

                if (i == null) {

                    query = "SELECT ALL COUNT(1) FROM " + table;
                    message = "Database Error (getNextIndex:COUNT):";
                    stm = null;
                    resultSet = null;
                    Log.Debug(query, true);
                    // Select-Anweisung ausführen
                    stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
                    resultSet = stm.executeQuery(query);
                    resultSet.first();
                    i = resultSet.getInt(1);
                }
            } else {
                i = 0;
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            stop();

            return null;

        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();

        i = (i < 0) ? -i : i;
        i += 1;
        return i;

    }

    /**
     * 
     * @param colName 
     * @return the next 
     */
    public Integer getNextIndexOfIntCol(String colName) {

        start();
        query = "SELECT " + colName + " FROM " + table;
        message = "Database Error (getNextIndex):";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        String integer = "0";
        Integer i = 0;
        Integer oldi = 0;
        Log.Debug(query, true);
        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);
            rsmd = resultSet.getMetaData();
            while (resultSet.next()) {
                integer = resultSet.getString(colName);
                try {
                    i = Integer.valueOf(integer);
                    if (i > oldi) {
                        oldi = i;
                    }
                } catch (NumberFormatException numberFormatException) {
                    query = "SELECT ALL COUNT(1) FROM " + table;
                    message = "Database Error (getNextIndex:COUNT):";
                    stm = null;
                    resultSet = null;
                    Log.Debug(query, true);
                    // Select-Anweisung ausführen
                    stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
                    resultSet = stm.executeQuery(query);

                    if (resultSet.next()) {
                        oldi = resultSet.getInt(1);
                    } else {
                        stop();
                        return null;
                    }
                }
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            stop();
            return null;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        try {
            oldi++;
            stop();
            return oldi;
        } catch (NumberFormatException numberFormatException) {
            Log.Debug(numberFormatException);
        }
        stop();
        return null;
    }

    /**
     * 
     * @param what  : {set, value, "'"}
     *   this.insert("name,wert", "'Sprache (Waehrung, z.B. Schweiz:  de_CH' ,'de_DE'");
     * @param unique 
     * @return id of inserted row
     */
    public int insert(String[] what, int[] unique) {

        what[1] = what[1].replaceAll("'", "`");
        what[1] = what[1].replaceAll("\\(;;2#4#1#1#8#0#;;\\)", "'");
        what[1] = what[1].replaceAll("\\(;;\\,;;\\)", ",");

        start();

       
           if (unique!=null) {
            for (int i = 0; i < unique.length; i++) {
                int j = unique[i];
                String[][] val = select(what[0].split(",")[j], new String[]{what[0].split(",")[j], what[1].split(",")[j], what[2]});
                if (val != null && val.length > 0) {
                    Popup.error("Wert für `" + what[0].split(",")[j] + "´ existiert bereits: " + val[0][0] +
                            ",\nund kann nicht mehrfach eingetragen werden.",
                            "Fehler beim Überprüfen der Datenkonsistenz");
                    
                    return -1;
                }
            }
        }
        

        query = "INSERT INTO " + table + " (" + what[0] + " ) VALUES (" + what[1] + ") ";
        Log.Debug(query, true);
        message = "Database Error:";
        stm = null;
        resultSet = null;
        ResultSet res;
        int id = 0;

        try {
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultCount = stm.executeUpdate(query);
            id = getCurrentIndex();
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            return 0;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        return id;
    }

    /**
     * 
     * @param what  : {set, values}
     * @param where : {value, comparison, "'"}
     * @return count of rows affected
     */
    public int update(String[] what, String[] where) {

        start();
        what[1] = what[1].replaceAll("'", "`");
        what[1] = what[1].replaceAll("\\(;;2#4#1#1#8#0#;;\\)", "'");

        Log.Debug(what);

        String[] a = what[0].split(",");
        String[] b = what[1].split("\\(;;\\,;;\\)");
        String c = "";

        for (int i = 0; i < a.length; i++) {
            c = c + a[i] + " = " + b[i] + ", ";
        }

        c = c.substring(0, c.length() - 2);

        query = "UPDATE " + table + " SET " + c + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2];
        message = "Database Error:";
        stm = null;
        resultSet = null;
        Log.Debug(query, true);
        try {
            // Select-Anweisung ausführenconn.createStatement(rs.TYPE_SCROLL_INSENSITIVE,rs.CONCUR_READ_ONLY);
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultCount = stm.executeUpdate(query);
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }

        stop();
        return resultCount;
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @param ghosts deleted
     * @return last matching result as string array
     */
    @SuppressWarnings("unchecked")
    public String[] selectLast(String what, String[] where) {

        String l = "";
        String k = " = ";
        String deleted = " ";
      

        start();
        query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + k + where[2] + l + where[1] + l + where[2] + deleted;
        message = "Database Error:";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String[] pax = null;
        Log.Debug(query, true);

        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            rsmd = resultSet.getMetaData();

            if (resultSet.last()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));
                }
            }
            pax = new String[spalten.size()];
            for (int i = 0; i < pax.length; i++) {
                pax[i] = String.valueOf(spalten.get(i));
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        if (pax.length < 1) {
            pax = null;
        }
        return pax;
    }

    /**
     * if "where" is "null", everything is selected (without "where" -clause)
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @return first matching result as string array
     */
    @SuppressWarnings("unchecked")
    public String[] selectFirst(String what, String[] where) {

        start();
        query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " ";
        message = "Database Error:";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String[] pax = null;

        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);
            ArrayList spalten = new ArrayList();
            rsmd = resultSet.getMetaData();

            if (resultSet.first()) {
                for (int i = 1; i <=
                        rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));
                }
            }

            pax = new String[spalten.size()];
            for (int i = 0; i <
                    pax.length; i++) {
                pax[i] = String.valueOf(spalten.get(i));
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        return pax;
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
    public String[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey, String order) {
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
    public String[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey) {
        return select(what, where, leftJoinTable, leftJoinKey, "id", null);
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @param ghosts 
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public String[][] select(String what, String[] where, boolean ghosts) {
        String str = "";
        if (ghosts) {
            str = " OR deleted = 1";
        }
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + " AND deleted = 0" + str;
        } else {
            query = "SELECT " + what + " FROM " + table + " WHERE deleted = 0" + str;
        }
        message = "Database Error (select) :";

        return selectFreeQuery(query, message);
    }

    /**
     * 
     * @param what
     * @param where : {value, comparison, "'"}
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public String[][] select(String what, String[] where) {
        start();
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2];
        } else {
            query = "SELECT " + what + " FROM " + table;

        }
        message = "Database Error (select) :";

        return selectFreeQuery(query, message);
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
    public String[][] select(String what, String[] where, String order, boolean like) {
        start();
        String l1 = "";
        String l2 = "";
        String k = " = ";
        String j = "";

        String ord = " ORDER BY " + order;
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
            wher = " WHERE deleted = 0 ";
        } else {
            wher = " WHERE " + where[0] + " " + k + " " + where[2] + l1 + where[1] + l2 + where[2] + " ";
        }

        query = "SELECT " + what + " FROM " + table + wher + ord;

        message = "Database Error (select) :";

        return selectFreeQuery(query, message);
    }

    /**
     * 
     * @param where : {value, comparison, "'"}
     * @return number of rows affected
     */
    public int delete(String[][] where) {

        start();
        String str = "";

        if (where != null) {
            for (int i = 0; i < where.length; i++) {
                str = str + where[i][0] + " = " + where[i][2] + where[i][1] + where[i][2];
            }
            query = "DELETE FROM " + table + " WHERE " + str;
        } else {
            return 0;
        }

        message = "Database Error:";
        stm = null;
        resultSet = null;
        Log.Debug(query, true);
        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement();
            resultCount = stm.executeUpdate(query);
            Log.Debug("Entries deleted: " + resultCount);
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            return 0;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
        stop();
        return resultCount;
    }

    /**
     * 
     * @param where : {value, comparison, "'"}
     * @return number of rows affected
     */
    public int delete(String[] where) {
        return delete(new String[][]{where});
    }

    /**
     * 
     * @param newTable
     * @return a clone of this ConnectionHandler (with database connection)
     */
    public Query clone(String newTable) {
        Query theClone = null;
        try {
            theClone = (Query) this.clone();
            theClone.setTable(newTable);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Query.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param ghosts 
     * @return
     */
    @SuppressWarnings({"unchecked", "unchecked"})
    public String[][] select(String what, String[] where, String order, boolean like, boolean integer, boolean ghosts) {
        start();
        String l = "";
        String k = " = ";
        String j = "";
        String ord = " ORDER BY " + order;
        String wher = "";
        wher = " deleted = 0 ";
        if (ghosts) {
            wher = "";
        }
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
            wher = where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2] + " AND " + wher;
            if (where.length > 3) {
                wher = wher + " AND " + where[3] + " " + k + " " + where[5] + l + where[4] + l + where[5] + " ";
            }
        }

        query = "SELECT " + what + " FROM " + table + " WHERE " + wher + ord;
//        Log.Debug(query, true);
        message = "Database Error (select) :";

        return selectFreeQuery(query, message);
    }

    public int selectCount(String from, String where) {
        String wher = "";

        if (where != null) {
            wher = " WHERE " + from + " " + where;
        }

        query = "SELECT COUNT(*) AS rowcount FROM " + table + " " + wher;
        message = "Database Error (SelectCount:COUNT):";
        stm = null;
        resultSet = null;

        Log.Debug(query, true);
        try {
            // Select-Anweisung ausführen
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            if (resultSet.first()) {
                Log.Debug("Count " + resultSet.getInt("rowcount"));
                return resultSet.getInt("rowcount");
            } else {
                stop();
                return 0;
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Popup.error(message + ex.getMessage(), "Datenbankfehler");
            stop();
            return 0;
        } finally {
            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Popup.error(message + ex.getMessage(), "Datenbankfehler");
                }
            }
        }
    }
}
