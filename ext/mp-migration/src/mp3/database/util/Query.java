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
package mp3.database.util;

import java.awt.Cursor;
import mp3.classes.interfaces.Structure;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import mp3.classes.utils.Log;
import mp3.classes.visual.main.mainframe;

/**
 *
 * @author anti
 */
public abstract class Query implements Structure {


    private Connection conn = null;
    private Statement stm = null;
    public String[] resultArray = null;
    public String resultString = null;
    private String table = "";
    private ResultSet resultSet = null;
    private int resultCount;
    private String[][] p;
    private String query;
    private ArrayList z;
    private String message;
    private JProgressBar progressBar;
    private List labels;
    private int startcount = 0;
    private mainframe main;


    public Query(Connection conn, String table) {
        this.table = table;
        this.conn = conn;

    }

    /**
     * Free SQL Statement!
     * @param string
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean freeQuery(String string) {



        query = string;


        message = "Database Error (freeQuery) :";
        stm = null;
        resultSet = null;



        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);

            return stm.execute(query);


        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }

        return false;
    }

    /**
     * Free SQL Select Statement!
     * @param string (Query)
     * @return Your Data
     */
    @SuppressWarnings("unchecked")
    public String[][] selectFreeQuery(String string) {
        start();
        labels = new ArrayList();
        //LEFT JOIN Orders ON Employees.Employee_ID=Orders.Employee_ID

        query = string;


        message = "Database Error (selectFreeQuery) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        boolean labelsDone = false;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();



            rsmd = resultSet.getMetaData();


            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));

                    if (!labelsDone) {
                        getLabels().add(rsmd.getColumnName(i));
                    }
                }
                labelsDone = true;
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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
    }

    public void setTable(String newTable) {
        this.table = newTable;
    }

    /**
     * Sets the progressbar for this derby-queries
     * @param bar
     * @param main 
     */
    public void setProgressBar(JProgressBar bar, mainframe main) {

        this.progressBar = bar;
        this.main = main;

    }

    private int getCurrentIndex() {
        return getNextIndex("id") - 1;
    }

    @SuppressWarnings("deprecation")
    private void stop() {

        if (progressBar != null) {
            progressBar.setIndeterminate(false);

            main.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

    }

    private void start() {
        if (progressBar != null) {
            progressBar.setIndeterminate(true);

            main.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }

    }

    /**
     * 
     * @param index
     * @return the next 
     */
    public String getNextIndexString(String index) {

        start();
        query = "SELECT " + index + " FROM " + table;
        message = "Database Error (getNextIndex):";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String integer = "0";
        Integer i = 0;
        Integer oldi = 0;
        Log.Debug(query);
        String k = "0";
        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

//            ArrayList spalten = new ArrayList();
            //ArrayList zeilen = new ArrayList();

            rsmd = resultSet.getMetaData();

            if (resultSet.last()) {

               
                integer = resultSet.getString(index);

                try {
                    i = Integer.valueOf(integer);
                    i++;
                    k = String.valueOf(i);

                } catch (NumberFormatException numberFormatException) {
             
                  
                    try {
                        k = integer;
                      

                        i = Integer.valueOf(integer.substring(integer.length() - 5, integer.length()).replaceAll("-", "#"));
                        i++;
                   
                        k = integer.substring(0, integer.length() - 5);

                        k = k + i;
                       
                        stop();

                    } catch (Exception numberFormatException1) {
                   

                        try {
                            k = integer;

                            i = Integer.valueOf(integer.substring(integer.length() - 4, integer.length()).replaceAll("-", "#"));
                            i++;

                            k = integer.substring(0, integer.length() - 4);
                            k = k + i;

                            stop();

                        } catch (Exception numberFormatException12) {

                          

                            try {
                                    k = integer;
                    

                        i = Integer.valueOf(integer.substring(integer.length() - 3, integer.length()).replaceAll("-", "#"));
                        i++;
                 
                        k = integer.substring(0, integer.length() - 3);

                        k = k + i;
                    
                        stop();


                            } catch (Exception numberFormatException123) {

                                try {
                                    k = integer;

                                    i = Integer.valueOf(integer.substring(integer.length() - 2, integer.length()).replaceAll("-", "#"));
                                    i++;

                                    k = integer.substring(0, integer.length() - 2);

                                    k = k + i;

                                    stop();

                                } catch (Exception numberFormatException1234) {

                                    try {
                                        k = integer;

                                        i = Integer.valueOf(integer.substring(integer.length() - 1, integer.length()).replaceAll("-", "#"));
                                        i++;

                                        k = integer.substring(0, integer.length() - 1);

                                        k = k + i;

                                        stop();

                                    } catch (Exception numberFormatException12345) {


                                        query = "SELECT ALL COUNT(1) FROM " + table;
                                        message = "Database Error (getNextIndex:COUNT):";
                                        stm = null;
                                        resultSet = null;

                                        Log.Debug(query);

                                        // Select-Anweisung ausf�hren
                                        stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
                                        resultSet = stm.executeQuery(query);

                                        resultSet.first();
                                        
                                        oldi = resultSet.getInt(1);
                                        oldi++;
                                        stop();
                                        k = String.valueOf(oldi);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
            stop();
            return "1";

        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }

        stop();
        
        if(k.equals("0"))k="1";
        
        return k;

    }

    /**
     * 
     * @param index
     * @return the next 
     */
    public Integer getNextIndex(String index) {

        start();
        query = "SELECT " + index + " FROM " + table;
        message = "Database Error (getNextIndex):";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String integer = "0";
        Integer i = 0;
        Integer oldi = 0;
        Log.Debug(query);
        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

//            ArrayList spalten = new ArrayList();
            //ArrayList zeilen = new ArrayList();

            rsmd = resultSet.getMetaData();

            while (resultSet.next()) {


                integer = resultSet.getString(index);

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

                    Log.Debug(query);

                    // Select-Anweisung ausf�hren
                    stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
                    resultSet = stm.executeQuery(query);

                    if (resultSet.next()) {
                        oldi = resultSet.getInt(1);

                    } else {
                        stop();
                        return 0;
                    }

                }

            }


        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
            stop();
            return 0;

        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }

        try {
            oldi++;

            stop();
            return oldi;

        } catch (NumberFormatException numberFormatException) {
        }
        stop();
        return 0;

    }


    /**
     * 
     * @param what  : {set, value, "'"}
     *   this.insert("name,wert", "'Sprache (Waehrung, z.B. Schweiz:  de_CH' ,'de_DE'");
     * @return id of inserted row
     */
    public int insert(String[] what) {
        
        what[1] = what[1].replaceAll("'", "`");
        
        what[1] = what[1].replaceAll("\\(;;2#4#1#1#8#0#;;\\)", "'");

        what[1] = what[1].replaceAll("\\(;;\\,;;\\)", ",");

        start();
        query = "INSERT INTO " + table + " (" + what[0] + " ) VALUES (" + what[1] + ") ";
        Log.Debug(query);
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
            Log.Debug(ex);
            return 0;
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
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
        Log.Debug(query);
        try {

            // Select-Anweisung ausf�hrenconn.createStatement(rs.TYPE_SCROLL_INSENSITIVE,rs.CONCUR_READ_ONLY);
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultCount = stm.executeUpdate(query);


        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
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
    public String[] selectLast(String what, String[] where, boolean ghosts, boolean like) {


        String l = "";
        String k = " = ";
        String deleted = " ";

        if (ghosts) {
            deleted = "";
        }

        if (like) {
            l = "%";
            k = " LIKE ";
        }

        start();
        query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + k + where[2] + l + where[1] + l + where[2] + deleted;
        message = "Database Error:";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        String[] pax = null;

        Log.Debug(query);

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            //ArrayList zeilen = new ArrayList();

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
            Log.Debug(ex);

        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
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
            // Select-Anweisung ausf�hren
            stm = conn.createStatement(resultSet.TYPE_SCROLL_INSENSITIVE, resultSet.CONCUR_READ_ONLY);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            //ArrayList zeilen = new ArrayList();

            rsmd = resultSet.getMetaData();

            if (resultSet.first()) {

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
            Log.Debug(ex);

        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
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
     * @param leftJoinTableFields
     * @return results as multidimensional string array
     */
//    @SuppressWarnings ("unchecked")
//    public String[][] select(String what, String[] where, String leftJoinTable,String leftJoinKey, String leftJoinTableFields) {
//      
//           start();
//           labels = new ArrayList();
//          LEFT JOIN Orders ON Employees.Employee_ID=Orders.Employee_ID
//          
//        if (where != null) {
//            query = "SELECT " + what + " FROM " + table  + 
//                    " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." 
//                    + leftJoinKey + " = " + leftJoinTable + ".id" + " WHERE " + where[0] + 
//                    " = " + where[2] + where[1] + where[2] + " AND " + table + ".deleted = 0";
//        } else {
//            query = "SELECT " + what + " FROM " + table  +
//                    " LEFT OUTER JOIN  " + leftJoinTable + " ON " + 
//                    table + "." + leftJoinKey + " = " + leftJoinTable + ".id" 
//                    + " WHERE " + table + ".deleted = 0";
//
//        }
//        message = "Database Error (select) :";
//        stm = null;
//        resultSet = null;
//        ResultSetMetaData rsmd;
//        boolean labelsDone = false;
//
//        try {
//             Select-Anweisung ausf�hren
//            stm = conn.createStatement();
//            Log.Debug(query);
//            resultSet = stm.executeQuery(query);
//
//            ArrayList spalten = new ArrayList();
//            ArrayList zeilen = new ArrayList();
//            
//           
//
//            rsmd = resultSet.getMetaData();
//                    
//
//            while (resultSet.next()) {
//                spalten = new ArrayList();
//                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                    spalten.add(resultSet.getObject(i));
//                    
//                   if(!labelsDone) {
//                        getLabels().add(rsmd.getColumnName(i));
//                    }
//                }
//                labelsDone=true;
//                zeilen.add(spalten);
//
//            }
//
//
//            p = new String[zeilen.size()][spalten.size()];
//
//
//            for (int h = 0; h < zeilen.size(); h++) {
//
//                z = (ArrayList) zeilen.get(h);
//
//                for (int i = 0; i < spalten.size(); i++) {
//
//                    p[h][i] = String.valueOf(z.get(i));
//                }
//            }
//
//        } catch (SQLException ex) {
//            Log.Debug(message + ex.getMessage());
//            Log.Debug(ex);
//        } finally {
//
//             Alle Ressourcen wieder freigeben
//            if (resultSet != null) {
//                try {
//                    resultSet.close();
//                } catch (SQLException ex) {
//                    Log.Debug(message + ex.getMessage());
//                    Log.Debug(ex);
//                }
//            }
//            if (stm != null) {
//                try {
//                    stm.close();
//                } catch (SQLException ex) {
//                    Log.Debug(message + ex.getMessage());
//                    Log.Debug(ex);
//                }
//            }
//        }
//        stop();
//        return p;
//        
//    }
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
        start();
        labels = new ArrayList();
        //LEFT JOIN Orders ON Employees.Employee_ID=Orders.Employee_ID

        if (where != null) {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".id" + " WHERE " + where[0] +
                    " = " + where[2] + where[1] + where[2] + " AND " + table + ".deleted = 0 ORDER BY " + order;
        } else {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN  " + leftJoinTable + " ON " +
                    table + "." + leftJoinKey + " = " + leftJoinTable + ".id ORDER BY " + order;
        //+ " WHERE " + table + ".deleted = 0";

        }
        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        boolean labelsDone = false;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();



            rsmd = resultSet.getMetaData();


            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));

                    if (!labelsDone) {
                        getLabels().add(rsmd.getColumnName(i));
                    }
                }
                labelsDone = true;
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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
    }

    /**
     * 
     * @param what
     * @param where
     * @param leftJoinTable
     * @param leftJoinKey 
     * @return results as multidimensional string array
     */
    @SuppressWarnings("unchecked")
    public String[][] select(String what, String[] where, String leftJoinTable, String leftJoinKey) {
        start();
        labels = new ArrayList();
        //LEFT JOIN Orders ON Employees.Employee_ID=Orders.Employee_ID

        if (where != null) {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN " + leftJoinTable + " ON " + table + "." + leftJoinKey + " = " + leftJoinTable + ".id" + " WHERE " + where[0] +
                    " = " + where[2] + where[1] + where[2] + " AND " + table + ".deleted = 0";
        } else {
            query = "SELECT " + what + " FROM " + table +
                    " LEFT OUTER JOIN  " + leftJoinTable + " ON " +
                    table + "." + leftJoinKey + " = " + leftJoinTable + ".id";
        //+ " WHERE " + table + ".deleted = 0";

        }
        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;
        boolean labelsDone = false;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
            resultSet = stm.executeQuery(query);

            ArrayList spalten = new ArrayList();
            ArrayList zeilen = new ArrayList();



            rsmd = resultSet.getMetaData();


            while (resultSet.next()) {
                spalten = new ArrayList();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    spalten.add(resultSet.getObject(i));

                    if (!labelsDone) {
                        getLabels().add(rsmd.getColumnName(i));
                    }
                }
                labelsDone = true;
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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
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
//        if (ghosts) {
//            str = " OR deleted = 1";
//        }
        start();
        if (where != null) {
            query = "SELECT " + what + " FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2] + " " + str;
        } else {
            query = "SELECT " + what + " FROM " + table + " " + str;

        }
        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
    }

    public void export(String filename) {
        start();


        query = "SELECT * INTO OUTFILE " + filename + " FROM " + table;


        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
            resultSet = stm.executeQuery(query);

        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();

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
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            Log.Debug(query);
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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
    }

    @SuppressWarnings("unchecked")
    public String[][] select(String what, String[] where, String order, boolean like) {
        start();
        String l = "";
        String k = " = ";
        String j = "";

        String ord = " ORDER BY " + order;
        String wher = "";

        if (like) {
            l = "%";
            k = " LIKE ";
//            j = " OR WHERE " + where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2];
        }

        if (where == null) {
            wher = " WHERE deleted = 0 ";
        } else {
            wher = " WHERE " + where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2] + " ";

        }



        query = "SELECT " + what + " FROM " + table + wher + ord;
        Log.Debug(query);
        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();

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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
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
        Log.Debug(query);
        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            resultCount = stm.executeUpdate(query);

            Log.Debug("Entries deleted: " + resultCount);
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
            return 0;
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
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

        start();
        if (where != null) {
            query = "DELETE FROM " + table + " WHERE " + where[0] + " = " + where[2] + where[1] + where[2];
        } else {

            query = "DELETE FROM " + table;
        }
        message = "Database Error:";
        stm = null;
        resultSet = null;
        Log.Debug(query);
        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();
            resultCount = stm.executeUpdate(query);

            Log.Debug("Entries deleted: " + resultCount);
        } catch (SQLException ex) {
            Log.Debug(message + ex.getMessage());
            Log.Debug(ex);
            return 0;
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        Log.Debug("Items deleted: " + resultCount);

        return resultCount;
    }

    /**
     * 
     * @param newTable
     * @return a clone of this QueryClass (with database connection)
     */
    public Query clone(String newTable) {
        Query theClone = null;
//        Log.Debug("cloning.");
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
     * @return
     */
    @SuppressWarnings("unchecked")
    public String[][] select(String what, String[] where, String order, boolean like, boolean integer) {
        start();
        String l = "";
        String k = " = ";
        String j = "";

        String ord = " ORDER BY " + order;
        String wher = "";

        if (integer) {

            if (where[1].equals("")) {
                where[1] = "0";
            }
            where[2] = "";
//            l = "%";
            k = " = ";
//            j = " OR WHERE " + where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2];
        }

        if (where == null) {
            wher = " WHERE deleted = 0 ";
        } else {
            wher = " WHERE " + where[0] + " " + k + " " + where[2] + l + where[1] + l + where[2] + " ";

            if (where.length > 3) {

                wher = wher + " AND " + where[3] + " " + k + " " + where[5] + l + where[4] + l + where[5] + " ";


            }
        }



        query = "SELECT " + what + " FROM " + table + wher + ord;
        Log.Debug(query);
        message = "Database Error (select) :";
        stm = null;
        resultSet = null;
        ResultSetMetaData rsmd;

        try {
            // Select-Anweisung ausf�hren
            stm = conn.createStatement();

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
            Log.Debug(ex);
        } finally {

            // Alle Ressourcen wieder freigeben
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
            if (stm != null) {
                try {
                    stm.close();
                } catch (SQLException ex) {
                    Log.Debug(message + ex.getMessage());
                    Log.Debug(ex);
                }
            }
        }
        stop();
        return p;
    }

    public List getLabels() {

        return labels;
    }
}
