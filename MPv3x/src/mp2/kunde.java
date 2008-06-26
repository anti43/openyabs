/*
 * kundenClass.java
 *
 * Created on 6. März 2007, 13:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mp2;


import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mp3.classes.utils.Log;



/**
 *
 * @author anti
 */
public class kunde {

    public String[] printFields={"Kundennummer", "Firma", "Name", "Vorname",  "Str", "PLZ", "Ort"};
    
    private String[] NKField = {"Kundennummer", "Firma", "Anrede", "Vorname", "Name", "Str", "PLZ", "Ort", "Tel", "Mobil", "Mail", "Webseite", "Notizen", "nn", "deleted"};
   
    public Integer id = 0;
    public String Kundennummer = "";
    public String Firma = "";
    public String Anrede = "";
    public String Vorname = "";
    public String Name = "";
    public String Str = "";
    public String PLZ = "";
    public String Ort = "";
    public String Tel = "";
    public String Mobil = "";
    public String Mail = "";
    public String Webseite = "";
    public String Notizen = "";
    public String nn = "";
    public Integer deleted = 0;
    private Statement s;
    private String sqlQuery;
    private mainFrame frame;
    private String table = "kunden";
    private String error;
    private String[] search = new String[100];
    private String[][] answer;

    /** Creates a new instance of kundenClass */
    public kunde(mainFrame frames) {
        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        frame = frames;
    }

    public kunde(mainFrame frames, String kundennummer, boolean printer) {

        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        frame = frames;

        this.getDataByNr(kundennummer);
    }

    public kunde(mainFrame frames, String Id) {
        try {
            mp_db_connector dbconn;

            dbconn = frames.getDBConn();
            s = dbconn.getStatement();

            frame = frames;

            this.getDataById(Id);
        } catch (SQLException ex) {
          //  Logger.getLogger(kunde.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public kunde(mainFrame frames, String Kundennummer, String Firma, String Anrede, String Vorname, String Name, String Str, String PLZ, String Ort, String Tel, String Mobil, String Mail, String Webseite, String Notizen) {
        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        frame = frames;


        String[] values = new String[15];

        values[0] = Kundennummer;
        values[1] = Firma;
        values[2] = Anrede;
        values[3] = Vorname;
        values[4] = Name;
        values[5] = Str;
        values[6] = PLZ;
        values[7] = Ort;
        values[8] = Tel;
        values[9] = Mobil;
        values[10] = Mail;
        values[11] = Webseite;
        values[12] = Notizen;


        String a = "";
        String b = "";

        for (int i = 0; i < values.length - 2; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < NKField.length - 2; i++) {

            b = b + NKField[i] + ",";
        }

        a = a.substring(0, a.length() - 1);
        b = b.substring(0, b.length() - 1);

        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";

        frame.progress(true); ////System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
            frame.error("Keine gültige Eingabe !");
        }

        frame.hinweis("Datensatz angelegt !");
    }

    public kunde(mainFrame frames, String[] data) {
        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        frame = frames;

        String[] values = data;

        String a = "";
        String b = "";

        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < NKField.length - 2; i++) {

            b = b + NKField[i] + ",";
        }

        a = a.substring(0, a.length() - 1);
        b = b.substring(0, b.length() - 1);

        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
            frame.error("Keine gültige Eingabe !");
        }

        frame.hinweis("Datensatz angelegt !");
    }

    public String[][] askForNamesAnd2() {
        sqlQuery = "SELECT id,kundennummer,name,firma,ort FROM kunden WHERE deleted = 0 ORDER BY Name";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }




        int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
         answer = new String[p][5];
        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);
                answer[i][3] = rs.getString(4);
                answer[i][4] = rs.getString(5);

                i++;
                while (rs.next()) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);
                    answer[i][3] = rs.getString(4);
                    answer[i][4] = rs.getString(5);

                    i++;
                }
            } else {

                answer[0][0] = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Daten gefunden.");
        frame.progress(false);
        return answer;
    }
public String[][] askForAll() {
        sqlQuery = "SELECT * FROM kunden WHERE deleted = 0 ORDER BY Name";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }




        int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
         answer = new String[p][7];
        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);
                answer[i][3] = rs.getString(4);
                answer[i][4] = rs.getString(5);
                answer[i][5] = rs.getString(6);
                answer[i][6] = rs.getString(7);

                i++;
                while (rs.next()) {


                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);
                answer[i][3] = rs.getString(4);
                answer[i][4] = rs.getString(5);
                answer[i][5] = rs.getString(6);
                answer[i][6] = rs.getString(7);

                    i++;
                }
            } else {

                answer[0][0] = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Druckdaten ermittelt.");
        frame.progress(false);
        return answer;
    }
    public String[][] askForNamesAndWhere(String[] ids) {
        String whatsup = "";

        if (ids != null) {
            for (int i = 0; ids[i] != null; i++) {

                whatsup = whatsup + " id = " + ids[i] + " OR ";
            }



            whatsup = whatsup.substring(0, whatsup.length() - 4);

            whatsup = whatsup + " AND ";

            sqlQuery = "SELECT id,kundennummer,name,firma,tel FROM kunden WHERE " + whatsup + " deleted = 0 ORDER BY Name";
            frame.progress(true); ////System.out.println(sqlQuery);

            ResultSet rs = null;
            try {
                rs = s.executeQuery(sqlQuery);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

                    int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
            answer = new String[p][5];

            try {

                if (rs.first()) {
                    int i = 0;

                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);
                    answer[i][3] = rs.getString(4);
                    answer[i][4] = rs.getString(5);

                    i++;
                    while (rs.next()) {


                        answer[i][0] = rs.getString(1);
                        answer[i][1] = rs.getString(2);
                        answer[i][2] = rs.getString(3);
                        answer[i][3] = rs.getString(4);
                        answer[i][4] = rs.getString(5);

                        i++;
                    }
                } else {

                    answer[0][0] = null;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            frame.hinweis("Daten gefunden.");
            ids = null;
            frame.progress(false);
            return answer;
        } else {
            frame.progress(false);
            answer = new String[1][1];
                answer[0][0]="";
                return answer;
        }
    }

    public String[][] askForNamesAnd() {

        sqlQuery = "SELECT * FROM kunden WHERE deleted = 0 ORDER BY Name";
        Log.Debug(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        
                int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        answer = new String[p][14];

        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);
                answer[i][3] = rs.getString(4);
                answer[i][4] = rs.getString(5);
                answer[i][5] = rs.getString(6);
                answer[i][6] = rs.getString(7);
                answer[i][7] = rs.getString(8);
                answer[i][8] = rs.getString(9);
                answer[i][9] = rs.getString(10);
                answer[i][10] = rs.getString(11);
                answer[i][11] = rs.getString(12);
                answer[i][12] = rs.getString(13);
                answer[i][13] = rs.getString(14);
             

                i++;
                while (rs.next()) {


                    answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);
                answer[i][3] = rs.getString(4);
                answer[i][4] = rs.getString(5);
                answer[i][5] = rs.getString(6);
                answer[i][6] = rs.getString(7);
                answer[i][7] = rs.getString(8);
                answer[i][8] = rs.getString(9);
                answer[i][9] = rs.getString(10);
                answer[i][10] = rs.getString(11);
                answer[i][11] = rs.getString(12);
                answer[i][12] = rs.getString(13);
                answer[i][13] = rs.getString(14);
             

                    i++;
                }
            } else {

                answer = new String[1][1];
                answer[0][0]="null";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
         Log.Debug("Daten gefunden.");
        frame.progress(false);
        return answer;
    }

    public void delete(String toString) {

        String hk = "'";
        error = "in den Mülleimer verschoben.";

        sqlQuery = "UPDATE " + table + " SET deleted = 1 WHERE id = " + toString;
        frame.progress(true); ////System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            frame.error("Datensatz nicht " + error + " !");
            ex.printStackTrace();
        }
        frame.hinweis("Datensatz " + error + " !");
    }

    public void update(String[] field, String[] value, String id) throws SQLException {
        String hk = "'";
        error = "aktualisiert";
        for (int i = 0; i <= field.length - 1; i++) {
            sqlQuery = "UPDATE " + table + " SET " + field[i] + " = " + hk + value[i] + hk + " WHERE id = " + id + "";
            frame.progress(true); ////System.out.println(sqlQuery);
            try {
                s.executeUpdate(sqlQuery);
            } catch (SQLException ex) {
                frame.error("Datensatz nicht " + error + " !");
                ex.printStackTrace();
            }
            frame.hinweis("Datensatz " + error + " !");
        }
    }

    public void update(String[] values, String id) throws SQLException {
        String hk = "'";
        error = "aktualisiert";
        for (int i = 0; i <= values.length - 1; i++) {
            //System.out.println(i);
            sqlQuery = "UPDATE " + table + " SET " + NKField[i] + " = " + hk + values[i] + hk + " WHERE id = " + id + "";
            frame.progress(true); ////System.out.println(sqlQuery);
            try {
                s.executeUpdate(sqlQuery);
            } catch (SQLException ex) {
                frame.error("Datensatz nicht " + error + " !");
                ex.printStackTrace();
            }
            frame.hinweis("Datensatz " + error + " !");
        }
    }

    public void save() {

        String[] values = new String[15];
        values[0] = Kundennummer;
        values[1] = Firma;
        values[2] = Anrede;
        values[3] = Vorname;
        values[4] = Name;
        values[5] = Str;
        values[6] = PLZ;
        values[7] = Ort;
        values[8] = Tel;
        values[9] = Mobil;
        values[10] = Mail;
        values[11] = Webseite;
        values[12] = Notizen;
        values[13] = "";
        values[14] = deleted.toString();


        String a = "";
        String b = "";
        String c = "";


        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < NKField.length; i++) {

            b = b + NKField[i] + ",";
        }


        for (int i = 0; i < NKField.length; i++) {

            c = c + b + "=" + a + ",";
        }

        c = c.substring(0, c.length() - 1);


        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";

        sqlQuery = "UPDATE kunden SET " + c + " WHERE id = '" + id + "'";

        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
            frame.error("Fehler beim speichern !");
        }

        frame.hinweis("Datensatz gespeichert !");
    }

    public String[] getDataById(String ids) throws SQLException {
        String[] values = new String[15];
//        if(ids.equals(null)) {
//            ids = "1";
//        }

         sqlQuery = "SELECT * FROM " + table + " WHERE id = " + ids;
Log.Debug(sqlQuery);
        ResultSet rs = s.executeQuery(sqlQuery);
        if (rs.first()) {

            for (int i = 0; i < 14; i++) {

                values[i] = rs.getString(i + 1);
            }
        } else {

//            new mpPopup("Der Kunde existiert nicht");
        }

        id = Integer.valueOf(ids);
        Kundennummer = values[1];
        Firma = values[2];
        Anrede = values[3];
        Vorname = values[4];
        Name = values[5];
        Str = values[6];
        PLZ = values[7];
        Ort = values[8];
        Tel = values[9];
        Mobil = values[10];
        Mail = values[11];
        Webseite = values[12];
        Notizen = values[13];
        nn = values[14];



        frame.progress(false);
        return values;
    }
    
     public String[] getALLL() throws SQLException {
        String[] values = new String[15];
//        if(ids.equals(null)) {
//            ids = "1";
//        }

         sqlQuery = "SELECT * FROM " + table;
Log.Debug(sqlQuery);
        ResultSet rs = s.executeQuery(sqlQuery);
        if (rs.first()) {

            for (int i = 0; i < 14; i++) {

                values[i] = rs.getString(i + 1);
            }
        } else {

//            new mpPopup("Der Kunde existiert nicht");
        }

//        id = Integer.valueOf(ids);
        Kundennummer = values[1];
        Firma = values[2];
        Anrede = values[3];
        Vorname = values[4];
        Name = values[5];
        Str = values[6];
        PLZ = values[7];
        Ort = values[8];
        Tel = values[9];
        Mobil = values[10];
        Mail = values[11];
        Webseite = values[12];
        Notizen = values[13];
        nn = values[14];



        frame.progress(false);
        return values;
    }


    public String[] searchIds(String field, String value) throws SQLException {

        search = null;
        search = new String[100];

        String like = "like";
        String hk = "'";
        String upper = "LOWER(";
        String close = ") ";

        value = "%" + value + "%";

        sqlQuery = "SELECT id FROM " + table + " WHERE " + upper + " " + field + close + " " + like + " " + hk + value + hk + " OR UPPER(" + field + close + " " + like + " " + hk + value + hk + " OR " + field + " " + like + " " + hk + value + hk + " AND  deleted = 0";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = s.executeQuery(sqlQuery);

        if (rs.first()) {
            rs.previous();

            int e = 0;

            while (rs.next()) {


                search[e] = rs.getString(1);

                e++;
            }
        } else {

            frame.error("Kein Datensatz gefunden, der Ihrer Suche entspricht.");
            frame.progress(false);
            return null;
        }

        frame.hinweis("Daten gefunden. Es werden max. 100 Kunden angezeigt.");
        frame.progress(false);
        return search;
    }

    public String[] getData(String kundennummer) throws SQLException {
        String[] values = new String[15];

        sqlQuery = "SELECT * FROM " + table + " WHERE kundennummer = '" + kundennummer + "' AND deleted = 0";

        ResultSet rs = s.executeQuery(sqlQuery);
        while (rs.next()) {

            for (int i = 0; i < 14; i++) {

                values[i] = rs.getString(i + 1);
            }
        }

        id = Integer.valueOf(values[0]);
        Kundennummer = kundennummer;
        Firma = values[2];
        Anrede = values[3];
        Vorname = values[4];
        Name = values[5];
        Str = values[6];
        PLZ = values[7];
        Ort = values[8];
        Tel = values[9];
        Mobil = values[10];
        Mail = values[11];
        Webseite = values[12];
        Notizen = values[13];
        nn = values[14];



        frame.progress(false);
        return values;
    }
//    public String[] getData(String Id, boolean bool) throws SQLException {
//        String[] values = new String[16];
//
//        sqlQuery = "SELECT * FROM " + table + " WHERE id = " + Id + " AND deleted = 0";
//
//        ResultSet rs = s.executeQuery(sqlQuery);
//        while (rs.next()) {
//
//            for (int i = 0; i < 16; i++) {
//
//                values[i] = rs.getString(i + 1);
//            }
//        }
//
//        id = Integer.valueOf(Id);
//        Kundennummer = values[1];
//        Firma = values[2];
//        Anrede = values[3];
//        Vorname = values[4];
//        Name = values[5];
//        Str = values[6];
//        PLZ = values[7];
//        Ort = values[8];
//        Tel = values[9];
//        Mobil = values[10];
//        Mail = values[11];
//        Webseite = values[12];
//        Notizen = values[13];
//        nn = values[14];
//        deleted = Integer.valueOf(values[15]);
//
//
//
//
//       frame.progress (false); return  values;
//    }

    public String[][] askForAllNames() {



        sqlQuery = "SELECT name,kundennummer FROM kunden WHERE deleted = 0 ORDER BY Name";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        answer = new String[p][2];

        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);

                i++;
                while (rs.next()) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);

                    i++;
                }
            } else {

                answer[0][0] = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Daten gefunden. Es werden max. 2000 Kunden angezeigt.");
        frame.progress(false);
        return answer;
    }

    public String[][] askForNames() {



        sqlQuery = "SELECT name,kundennummer FROM kunden WHERE deleted = 0 ORDER BY Name";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        int p = 0;
        try {

            if (rs.first()) {


                p++;
                while (rs.next()) {

                    p++;
                }
            } else {

                p = 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        answer = new String[p][2];

        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);

                i++;
                while (rs.next()) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);

                    i++;
                }
            } else {

                answer[0][0] = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Daten gefunden. Es werden max. 100 Kunden angezeigt.");
        frame.progress(false);
        return answer;
    }

    public String[][] askForDeletedNames() {
        String[][] deletedSouls = new String[2000][3];
        sqlQuery = "SELECT name,kundennummer FROM kunden WHERE deleted = 1 ORDER BY Name";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try {

            if (rs.first()) {
                int i = 0;

                deletedSouls[i][0] = rs.getString(0);
                deletedSouls[i][1] = rs.getString(1);
                deletedSouls[i][2] = rs.getString(2);

                i++;
                while (rs.next()) {


                    deletedSouls[i][0] = rs.getString(0);
                    deletedSouls[i][1] = rs.getString(1);
                    deletedSouls[i][2] = rs.getString(2);

                    i++;
                }
            } else {

                deletedSouls[0][0] = null;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.progress(false);
        return deletedSouls;
    }

    public String askForHighestNumber() {
          sqlQuery = "SELECT kundennummer FROM kunden WHERE deleted = 0 ORDER BY kundennummer";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String answer="0";

        try {

            if (rs.first()) {
            

                answer = rs.getString(1);
                


              
                while (rs.next()) {


                    answer = rs.getString(1);
     
                }
            } else {

                answer = "0";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
       
        frame.progress(false); return answer;
    }

    void letReIncarnate(String id) {
        String hk = "";

        error = "wiederhergestellt";

        sqlQuery = "UPDATE " + table + " SET deleted = '0' WHERE id= '" + id + "'";
        frame.progress(true); ////System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            frame.error("Datensatz nicht " + error + " !");
            ex.printStackTrace();
        }
        frame.hinweis("Datensatz " + error + " !");
    }

    private String[] getDataByNr(String kundennummer) {
        try {

            String[] values = new String[16];

            sqlQuery = "SELECT * FROM " + table + " WHERE kundennummer = '" + kundennummer + "'";
            frame.progress(true); ////System.out.println(sqlQuery);
            ResultSet rs = s.executeQuery(sqlQuery);
            if (rs.first()) {

                for (int i = 0; i < 16; i++) {

                    values[i] = rs.getString(i + 1);
                    // new out(values[i]);
                }
            }
            if (rs.next()) {

                frame.error("Es existieren mehr als ein Kunde mit dieser Kundennummer!");
            }

            id = Integer.valueOf(values[0]);
            Kundennummer = values[1];
            Firma = values[2];
            Anrede = values[3];
            Vorname = values[4];
            Name = values[5];
            Str = values[6];
            PLZ = values[7];
            Ort = values[8];
            Tel = values[9];
            Mobil = values[10];
            Mail = values[11];
            Webseite = values[12];
            Notizen = values[13];
            nn = values[14];





            frame.progress(false);
            return values;
        } catch (SQLException ex) {
            Logger.getLogger(kunde.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
//    public boolean neu(String Kundennummer, String Firma, String Anrede, String Vorname, String Name, String Str, String PLZ, String Ort, String Tel, String Mobil, String Mail, String Webseite, String Notizen, String aktiv) {
//        String[] values = new String[15];
//
//        values[0] = Kundennummer;
//        values[1] = Firma;
//        values[2] = Anrede;
//        values[3] = Vorname;
//        values[4] = Name;
//        values[5] = Str;
//        values[6] = PLZ;
//        values[7] = Ort;
//        values[8] = Tel;
//        values[9] = Mobil;
//        values[10] = Mail;
//        values[11] = Webseite;
//        values[12] = Notizen;
//        values[13] = nn;
//        values[14] = deleted.toString();
//
//        if (this.neuEintragen(values)) {
//           frame.progress (false); return  true;
//        } else {
//           frame.progress (false); return  false;
//        }
//    }
//
//    private boolean neuEintragen(String[] values) {
//
//        String a = "";
//        String b = "";
//
//        for (int i = 0; i < values.length; i++) {
//
//            a = a + "'" + values[i] + "',";
//        }
//
//        for (int i = 0; i < NKField.length; i++) {
//
//            b = b + NKField[i] + ",";
//        }
//
//        a = a.substring(0, a.length() - 1);
//        b = b.substring(0, b.length() - 1);
//
//        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";
//        try {
//            s.executeUpdate(sqlQuery);
//        } catch (SQLException j) {
//            j.printStackTrace();
//            frame.error("Keine gültige Eingabe !");
//           frame.progress (false); return  false;
//        }
//
//        frame.hinweis("Datensatz angelegt !");
//       frame.progress (false); return  true;
//    }
//    public String eintragen(String[] field, String[] values) throws SQLException {
//        String a = "";
//        String b = "";
//
//        for (int i = 0; i < values.length; i++) {
//
//            a = a + "'" + values[i] + "',";
//        }
//
//        for (int i = 0; i < field.length; i++) {
//
//            b = b + field[i] + ",";
//        }
//
//        a = a.substring(0, a.length() - 1);
//        b = b.substring(0, b.length() - 1);
//
//        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";
//        try {
//            s.executeUpdate(sqlQuery);
//        } catch (SQLException j) {
//
//            frame.error("Keine gültige Eingabe !");
//           frame.progress (false); return  "toolong";
//        }
//
//        frame.hinweis("Datensatz angelegt !");
//       frame.progress (false); return  "ok";
//    }
}