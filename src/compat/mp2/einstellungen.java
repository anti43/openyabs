package compat.mp2;

import java.sql.*;

/*
 * settingsDBO.java
 *
 * Created on 15. Februar 2007, 08:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anti
 */
public class einstellungen {

    /** Creates a new instance of settingsDBO */
    public einstellungen(mp_db_connector DBC) {
        s = DBC.getStatement();
    }
    public Statement stmt;
    public String sqlQuery;
    public ResultSet rs;
    public Connection con;
    public int counter;
    public String[][] kundennummer = new String[1000][20];
    public String[] adr_daten = new String[15];
    public String framework = "embedded";
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public String protocol = "jdbc:derby:";
    private String databasename = "mpdb_embed";
    private Connection conn = null;
    public Statement s;
    private String table = "settings";
    private String error;

    public String getPathToPdf() {

        String setArray = null;

        sqlQuery = "SELECT pdfpath FROM " + table + " WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public Statement getStatement() {

        return s;
    }

    public String getUpdateserver() {
               String setArray = null;

        sqlQuery = "SELECT updateserver FROM " + table + " WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }
    
    public String getVersion() {
               String setArray = null;

        sqlQuery = "SELECT version FROM " + table + " WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public String setStartTab(Integer tab) {
        String setTab = "0";

        sqlQuery = "UPDATE settings SET starttab = '" + tab + "' WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return setTab;
    }

    public String getStartTab() {
        String setTab = "0";

        sqlQuery = "SELECT starttab FROM settings WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setTab = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setTab;
    }

    public int[] getSkin() {
        int[] setArray = new int[1];

        sqlQuery = "SELECT skin FROM " + table + " WHERE ID = 1";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray[0] = rs.getInt(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public String getBrowser() {
        String setArray = "";

        sqlQuery = "SELECT browser FROM settings WHERE ID = 0";

        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public String[] getSettings() {
        String[] data = new String[10];
        sqlQuery = "SELECT * FROM settings";
        ResultSet rs;

        try {
            rs = s.executeQuery(sqlQuery);

            while (rs.next()) {

                for (int i = 0; i < 10; i++) {

                    data[i] = rs.getString(i + 1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public String[] getData() {
        String[] data = new String[5];
        sqlQuery = "SELECT * FROM daten";
        ResultSet rs;

        try {
            rs = s.executeQuery(sqlQuery);

            while (rs.next()) {

                for (int i = 0; i < 5; i++) {

                    data[i] = rs.getString(i + 1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public void close() throws SQLException {
        conn.close();
    }

    public boolean validateNetto() {

        if (this.getNetto().equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    boolean resetAll() {

        

        try {
            //System.out.println(sqlQuery);
            sqlQuery = "DELETE FROM kunden";
            s.executeUpdate(sqlQuery);
            sqlQuery = "DELETE FROM rechnungen";
            s.executeUpdate(sqlQuery);
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    boolean resetKunden() {
        sqlQuery = "DELETE FROM kunden";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    boolean resetRechnungen() {
        sqlQuery = "DELETE FROM rechnungen";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    void setSkin(int[] skin) {
        sqlQuery = "UPDATE " + table + " SET " + "skin" + " = " + skin[0] + " WHERE ID = 1";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    void setdata(String[] field, String[] data) {
        for (int i = 0; i < field.length; i++) {
            sqlQuery = "UPDATE daten SET " + field[i] + " = '" + data[i] + "'";
            //System.out.println(sqlQuery);
            try {
                s.executeUpdate(sqlQuery);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    void setSettings(String[] field, String[] data) {
        for (int i = 0; i < field.length; i++) {
            sqlQuery = "UPDATE settings SET " + field[i] + " = '" + data[i] + "'";
            //System.out.println(sqlQuery);
            try {
                s.executeUpdate(sqlQuery);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    void insertdata(String[] field, String[] values) {

        String a = "";
        String b = "";

        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }
        //System.out.println(a);
        for (int i = 0; i < field.length; i++) {

            b = b + field[i] + ",";
        }
        //System.out.println(b);
        a = a.substring(0, a.length() - 1);
        b = b.substring(0, b.length() - 1);


        sqlQuery = "INSERT INTO daten(" + b + ") VALUES (" + a + ")";

        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    void setMWSTS(String integer) {
        sqlQuery = "UPDATE " + table + " SET " + "mwst" + " = '" + integer + "' WHERE ID = 1";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getMWSTS() {
        String setArray = null;

        sqlQuery = "SELECT mwsts FROM settings";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public Integer getPopups() {
        Integer setArray = 0;

        sqlQuery = "SELECT popups FROM " + table + " WHERE ID = 1";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getInt(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public void setPopup(Integer i) {
        sqlQuery = "UPDATE " + table + " SET " + "popups" + " = " + i.toString() + " WHERE ID = 1";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getTemplate() {
        String setArray = null;

        sqlQuery = "SELECT template FROM " + table + " WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    public String getPdfViewer() {
        String setArray = null;

        sqlQuery = "SELECT pdfviewer FROM " + table + " WHERE ID = 0";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
        }
        return setArray;
    }

    void setPDFViewer(String string) {
        sqlQuery = "UPDATE " + table + " SET " + "pdfviewer" + " = '" + string + "' WHERE ID = 0";

        try {
            //System.out.println(sqlQuery);
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getNetto() {
        String setArray = null;

        sqlQuery = "SELECT allnetto FROM settings";
        ResultSet rs;
        //System.out.println(sqlQuery);
        try {
            rs = s.executeQuery(sqlQuery);

            if (rs.first()) {

                setArray = rs.getString(1);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
        }
        return setArray;
    }
}