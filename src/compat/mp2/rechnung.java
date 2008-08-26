/*
 * kundenClass.java
 *
 * Created on 6. März 2007, 13:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package compat.mp2;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Locale;



/**
 *
 * @author anti
 */
public class rechnung {

    private String[] NRField = {"rechnungnummer", "nummer", "posten1", "preis1", "posten2", "preis2", "posten3", "preis3", "posten4", "preis4", "posten5", "preis5", "posten6", "preis6", "posten7", "preis7", "posten8", "preis8", "posten9", "preis9", "posten10", "preis10", "datum", "gesamtpreis", "mwst"};
    
    public Integer id = 0;
    public String rechnungnummer = "";
    public String kundenID = "";
    public String posten1 = "";
    public String preis1 = "";
    public String posten2 = "";
    public String preis2 = "";
    public String posten3 = "";
    public String preis3 = "";
    public String posten4 = "";
    public String preis4 = "";
    public String posten5 = "";
    public String preis5 = "";
    public String posten6 = "";
    public String preis6 = "";
    public String posten7 = "";
    public String preis7 = "";
    public String posten8 = "";
    public String preis8 = "";
    public String posten9 = "";
    public String preis9 = "";
    public String posten10 = "";
    public String preis10 = "";
    public String datum = "";
    public Double gesamtpreis = 0d;
    public Double mehrwertsteuer = 0d;
    public Integer deleted = 0;
    private Statement s;
    private String sqlQuery;
    private mainFrame frame;
    private String table = "rechnungen";
    private String error;
    private String[] search = new String[100];
    private String[][] answer;
    private Double gesamtnettopreis =  0d;
    private boolean allIsNetto = false;
    //private Double steuersatz = (Double) 19;
    private boolean nettoPreise = false;
    private einstellungen e;
    private Double steuersatz = new Double(0);
    public Double mehrwertsteuersatz = new Double(0);
    private String gesamtnettopreisstr="0";
    public String gesamtpreisstr="0";
    public String mehrwertsteuerstr="0";

    public rechnung(mainFrame frames) {
        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        e = new einstellungen(dbconn);
        nettoPreise = e.validateNetto();

        frame = frames;
    }

    public rechnung(mainFrame frames, String nummer, boolean b) {

        try {
            mp_db_connector dbconn;

            dbconn = frames.getDBConn();
            s = dbconn.getStatement();
            e = new einstellungen(dbconn);
            nettoPreise = e.validateNetto();
            frame = frames;
            
            this.getDataByNumber(nummer);
        } catch (SQLException ex) {
            Logger.getLogger(kunde.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public rechnung(mainFrame frames, String Id) {
        try {
            mp_db_connector dbconn;

            dbconn = frames.getDBConn();
            s = dbconn.getStatement();
            e = new einstellungen(dbconn);
            nettoPreise = e.validateNetto();
            frame = frames;

            this.getData(Id, true);
        } catch (SQLException ex) {
//            Logger.getLogger(kunde.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public rechnung(mainFrame frames, String rechnungnummer, String kundeID, String posten1, String preis1, String posten2, String preis2, String posten3, String preis3, String posten4, String preis4, String posten5, String preis5, String posten6, String preis6, String posten7, String preis7, String posten8, String preis8, String posten9, String preis9, String posten10, String preis10, String datum, String mehrwertsteuersatz) {

        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();

        e = new einstellungen(dbconn);
        nettoPreise = e.validateNetto();

        try {
            steuersatz = Double.valueOf(mehrwertsteuersatz);
        } catch (NumberFormatException numberFormatException) {
//            frames.error("Kein gültiger Steuersatz, es wird der globale Steuersatz angenommen.");
            steuersatz = Double.valueOf(e.getMWSTS());
        }

        frame = frames;


        if (posten1 == null) {
            posten1 = " ";
        }
        if (posten2 == null) {
            posten2 = " ";
        }
        if (posten3 == null) {
            posten3 = " ";
        }
        if (posten4 == null) {
            posten4 = " ";
        }
        if (posten5 == null) {
            posten5 = " ";
        }
        if (posten6 == null) {
            posten6 = " ";
        }
        if (posten10 == null) {
            posten10 = " ";
        }
        if (posten7 == null) {
            posten7 = " ";
        }
        if (posten8 == null) {
            posten8 = " ";
        }
        if (posten9 == null) {
            posten9 = " ";
        }

        if (preis1 == null) {
            preis1 = "0";
        }
        if (preis2 == null) {
            preis2 = "0";
        }
        if (preis3 == null) {
            preis3 = "0";
        }
        if (preis4 == null) {
            preis4 = "0";
        }
        if (preis5 == null) {
            preis5 = "0";
        }
        if (preis6 == null) {
            preis6 = "0";
        }
        if (preis10 == null) {
            preis10 = "0";
        }
        if (preis7 == null) {
            preis7 = "0";
        }
        if (preis8 == null) {
            preis8 = "0";
        }
        if (preis9 == null) {
            preis9 = "0";
        }

        try {
            gesamtnettopreis = Double.valueOf(preis1) + Double.valueOf(preis2) + Double.valueOf(preis3) + Double.valueOf(preis4) + Double.valueOf(preis5) + Double.valueOf(preis6) + Double.valueOf(preis7) + Double.valueOf(preis8) + Double.valueOf(preis9) + Double.valueOf(preis10);
        } catch (NumberFormatException numberFormatException) {
            //numberFormatException.printStackTrace();
//            new mpPopup("Bitte geben Sie als Preis nur Zahlen ein!");
        }




        mehrwertsteuer = gesamtnettopreis * (steuersatz / 100);
        gesamtpreis = gesamtnettopreis + mehrwertsteuer;


        String[] values = new String[25];

        values[0] = rechnungnummer;
        values[1] = kundeID;
        values[2] = posten1;
        values[3] = preis1;
        values[4] = posten2;
        values[5] = preis2;
        values[6] = posten3;
        values[7] = preis3;
        values[8] = posten4;
        values[9] = preis4;
        values[10] = posten5;
        values[11] = preis5;
        values[12] = posten6;
        values[13] = preis6;
        values[14] = posten7;
        values[15] = preis7;
        values[16] = posten8;
        values[17] = preis8;
        values[18] = posten9;
        values[19] = preis9;
        values[20] = posten10;
        values[21] = preis10;
        values[22] = datum;
        values[23] = gesamtnettopreis.toString();
        values[24] = mehrwertsteuersatz;

        String a = "";
        String b = "";

        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < values.length; i++) {

            b = b + NRField[i] + ",";
        }

        a = a.substring(0, a.length() - 1);
        b = b.substring(0, b.length() - 1);

        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";

//        frame.progress(true); ////System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
//            frame.error("Keine gültige Eingabe !");
        }

//        frame.hinweis("Rechnung angelegt !");
    }

    public rechnung(mainFrame frames, String[] data) {
        mp_db_connector dbconn;

        dbconn = frames.getDBConn();
        s = dbconn.getStatement();
        e = new einstellungen(dbconn);

        try {
            steuersatz = Double.valueOf(data[24]);
        } catch (NumberFormatException numberFormatException) {
            frames.error("Kein gültiger Steuersatz, es wird der globale Steuersatz angenommen.");
            steuersatz = Double.valueOf(e.getMWSTS());
        }
        nettoPreise = e.validateNetto();
        frame = frames;

        String[] values = data;

        String a = "";
        String b = "";

        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < NRField.length; i++) {

            b = b + NRField[i] + ",";
        }

        a = a.substring(0, a.length() - 1);
        b = b.substring(0, b.length() - 1);
//frame.progress(true); ////System.out.println(sqlQuery);
        sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
            frame.error("Keine gültige Eingabe !");
        }

        frame.hinweis("Rechnung angelegt !");
    }

    public String[][] askForNumbersAnd() {

        sqlQuery = "SELECT * FROM rechnungen WHERE deleted = 0 ORDER BY id";
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
        answer = new String[p][26];

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
                answer[i][14] = rs.getString(15);
                answer[i][15] = rs.getString(16);
                answer[i][16] = rs.getString(17);
                answer[i][17] = rs.getString(18);
                answer[i][18] = rs.getString(19);
                answer[i][19] = rs.getString(20);
                answer[i][20] = rs.getString(21);
                answer[i][21] = rs.getString(22);
                answer[i][22] = rs.getString(23);
                answer[i][23] = rs.getString(24);
                answer[i][24] = rs.getString(25);
                 answer[i][25] = rs.getString(26);
                

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
                answer[i][14] = rs.getString(15);
                answer[i][15] = rs.getString(16);
                answer[i][16] = rs.getString(17);
                answer[i][17] = rs.getString(18);
                answer[i][18] = rs.getString(19);
                answer[i][19] = rs.getString(20);
                answer[i][20] = rs.getString(21);
                answer[i][21] = rs.getString(22);
                answer[i][22] = rs.getString(23);
                answer[i][23] = rs.getString(24);
                answer[i][24] = rs.getString(25);
                answer[i][25] = rs.getString(26);

                    i++;
                }
            } else {

                answer = new String[0][0];
               
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Daten gefunden.");
        frame.progress(false);
        return answer;
    }

    public String[][] askForNumbersAndWhere(String rechnungsnummer) {
//WHERE " + upper + " " + field + close + " " + like + " " + hk + value + hk + " OR UPPER(" + field + close + " " + like + " " + hk + value + hk + " OR " + field + " " + like + " " + hk + value + hk + "
        sqlQuery = "SELECT id,rechnungnummer,datum FROM rechnungen WHERE rechnungnummer LIKE '" + rechnungsnummer + "%' ORDER BY id";
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
        answer = new String[p][3];

        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);


                i++;
                while (rs.next() && i < 100) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);


                    i++;
                }
            } else {

                answer = new String[1][1];
                answer[0][0] = "No Data.";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        frame.hinweis("Daten gefunden.");
        frame.progress(false);
        return answer;
    }

    public void delete(String toString) {

        String hk = "'";
        error = "in den Mülleimer verschoben.";

        sqlQuery = "DELETE FROM rechnungen WHERE id = " + toString;
        frame.progress(true); ////System.out.println(sqlQuery);
        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException ex) {
            frame.error("Datensatz nicht " + error + " !");
            ex.printStackTrace();
        }
        frame.hinweis("Datensatz " + error + " !");
    }

    public String[][] askForNumbersOfYear(String year) {
       

            sqlQuery = "SELECT id,rechnungnummer,datum  FROM rechnungen ";
         //  sqlQuery = "SELECT FROM rechnungen WHERE deleted = 0 ORDER BY id";
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
        answer = new String[p][3];

        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);


                i++;
                while (rs.next() && i < 100) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);


                    i++;
                }
            } else {

                answer = new String[1][1];
                answer[0][0] = "No Data.";
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        String[][] answer2 = new String[p+1][3];int pk=0;int i=0;
        
        for(i=0;i<answer.length;i++){
       
            if(answer[i][2].endsWith(year.trim())){
                answer2[pk][0]=answer[i][0];
                  answer2[pk][1]=answer[i][1];
                    answer2[pk][2]=answer[i][2];
                   
            pk++;
            }
        
        }
        i=0;
       // new out(answer2[0][0]);
        
        while(answer2[i][0]!=null){
             i++;   
           
        }
        answer=new String[i][3];
        
        for(int z=0;z<i;z++){
               answer[z][0]=answer2[z][0];
                  answer[z][1]=answer2[z][1];
                    answer[z][2]=answer2[z][2];
         
        }
        
        
        
        frame.hinweis("Rechnungen des Jahres "+year+".");
        frame.progress(false);
        return answer;
    }

    public void printList() {
        throw new UnsupportedOperationException("Not yet implemented");
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

        String[] val = new String[25];



        rechnungnummer = values[0];
        kundenID = values[1];
        posten1 = values[2];
        preis1 = values[3];
        posten2 = values[4];
        preis2 = values[5];
        posten3 = values[6];
        preis3 = values[7];
        posten4 = values[8];
        preis4 = values[9];
        posten5 = values[10];
        preis5 = values[11];
        posten6 = values[12];
        preis6 = values[13];
        posten7 = values[14];
        preis7 = values[15];
        posten8 = values[16];
        preis8 = values[17];
        posten9 = values[18];
        preis9 = values[19];
        posten10 = values[20];
        preis10 = values[21];
        datum = values[22];

        values[23] = values[23].replace(",", ".");
        values[24] = values[24].replace(",", ".");

        gesamtnettopreis = Double.valueOf(values[23]);
        mehrwertsteuersatz = Double.valueOf(values[24]);

        //new out(gesamtnettopreis);
        gesamtpreis = gesamtnettopreis * (1 + (mehrwertsteuersatz / 100));
        mehrwertsteuer = gesamtpreis - gesamtnettopreis;

        for (int i = 0; i < 25; i++) {
            val[i] = values[i];
        }


//        if(posten1==null){posten1=" ";}if(posten2==null){posten2=" ";}if(posten3==null){posten3=" ";}
//        if(posten4==null){posten4=" ";}if(posten5==null){posten5=" ";}if(posten6==null){posten6=" ";}
//        if(posten10==null){posten10=" ";}if(posten7==null){posten7=" ";}if(posten8==null){posten8=" ";}
//        if(posten9==null){posten9=" ";}
//
//        if(preis1.equals("")){preis1="0";}if(preis2.equals("")){preis2="0";}if(preis3.equals("")){preis3="0";}
//        if(preis4.equals("")){preis4="0";}if(preis5.equals("")){preis5="0";}if(preis6.equals("")){preis6="0";}
//        if(preis10.equals("")){preis10="0";}if(preis7.equals("")){preis7="0";}if(preis8.equals("")){preis8="0";}
//        if(preis9.equals("")){preis9="0";}


//        try {
//              //System.out.println(preis1);
//            gesamtnettopreis = Double.valueOf(preis1) + Double.valueOf(preis2) + Double.valueOf(preis3) + Double.valueOf(preis4) + Double.valueOf(preis5) + Double.valueOf(preis6) + Double.valueOf(preis7) + Double.valueOf(preis8) + Double.valueOf(preis9) + Double.valueOf(preis10);
//        } catch (NumberFormatException numberFormatException) {
//            numberFormatException.printStackTrace();
//            new mpPopup("Bitte geben Sie als Preis nur Zahlen ein!");
//        }
//
//        if (e.validateNetto()) {
//
//            mehrwertsteuer =gesamtnettopreis*(1+(steuersatz/100));
//            gesamtpreis = gesamtnettopreis + mehrwertsteuer;
//        } else {
//
//            mehrwertsteuer = (gesamtnettopreis / 100 * steuersatz);
//            gesamtpreis = gesamtnettopreis;
//        }
//
//        val[23]=gesamtpreis.toString();
//        val[24]=mehrwertsteuer.toString();
        String hk = "'";
        error = "aktualisiert";
        for (int i = 0; i < val.length; i++) {
            //System.out.println(i);
            sqlQuery = "UPDATE " + table + " SET " + NRField[i] + " = " + hk + val[i] + hk + " WHERE id = " + id + "";
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

        String[] values = new String[30];

        values[0] = rechnungnummer;
        values[1] = kundenID;
        values[2] = posten1;
        values[3] = preis1;
        values[4] = posten2;
        values[5] = preis2;
        values[6] = posten3;
        values[7] = preis3;
        values[8] = posten4;
        values[9] = preis4;
        values[10] = posten5;
        values[11] = preis5;
        values[12] = posten6;
        values[13] = preis6;
        values[14] = posten7;
        values[15] = preis7;
        values[16] = posten8;
        values[17] = preis8;
        values[18] = posten9;
        values[19] = preis9;
        values[20] = posten10;
        values[21] = preis10;
        values[22] = datum;
        values[23] = gesamtpreis.toString();
        values[24] = mehrwertsteuer.toString();
        values[25] = deleted.toString();


        String a = "";
        String b = "";
        String c = "";


        for (int i = 0; i < values.length; i++) {

            a = a + "'" + values[i] + "',";
        }

        for (int i = 0; i < NRField.length; i++) {

            b = b + NRField[i] + ",";
        }


        for (int i = 0; i < NRField.length; i++) {

            c = c + b + "=" + a + ",";
        }

        c = c.substring(0, c.length() - 1);


        //sqlQuery = "INSERT INTO " + table + "(" + b + ") VALUES (" + a + ")";
        sqlQuery = "UPDATE rechnungen SET " + c + " WHERE id = \'" + id + "\'";

        try {
            s.executeUpdate(sqlQuery);
        } catch (SQLException j) {
            j.printStackTrace();
            frame.error("Fehler beim speichern !");
        }

        frame.hinweis("Datensatz gespeichert !");
    }

    public String[] getData(String Id, boolean bool) throws SQLException {
        String[] values = new String[26];

        sqlQuery = "SELECT * FROM " + table + " WHERE id = " + Id + " AND deleted = 0";

        ResultSet rs = s.executeQuery(sqlQuery);
        while (rs.next()) {

            for (int i = 0; i < 26; i++) {

                values[i] = rs.getString(i + 1);
            }
        }

        id = Integer.valueOf(Id);
        rechnungnummer = values[0 + 1];
        kundenID = values[1 + 1];
        posten1 = values[2 + 1];
        preis1 = values[3 + 1];
        posten2 = values[4 + 1];
        preis2 = values[5 + 1];
        posten3 = values[6 + 1];
        preis3 = values[7 + 1];
        posten4 = values[8 + 1];
        preis4 = values[9 + 1];
        posten5 = values[10 + 1];
        preis5 = values[11 + 1];
        posten6 = values[12 + 1];
        preis6 = values[13 + 1];
        posten7 = values[14 + 1];
        preis7 = values[15 + 1];
        posten8 = values[16 + 1];
        preis8 = values[17 + 1];
        posten9 = values[18 + 1];
        preis9 = values[19 + 1];
        posten10 = values[20 + 1];
        preis10 = values[21 + 1];
        datum = values[22 + 1];
        try {

            //System.out.println(values[25]);
            gesamtnettopreis = (Double.valueOf(values[23 + 1].replace(",", ".")));
            mehrwertsteuersatz = Double.valueOf(values[24 + 1].replace(",", "."));
            mehrwertsteuer = (Double.valueOf(values[23 + 1].replace(",", "."))) * (mehrwertsteuersatz / 100);

            // new out("1"); new out(gesamtnettopreis);  new out(mehrwertsteuersatz);  new out(mehrwertsteuer);
            gesamtpreis = gesamtnettopreis + mehrwertsteuer;
        } catch (Exception numberFormatException) {
//            numberFormatException.printStackTrace();
        }





        frame.progress(false);
        return values;
    }

    public String[][] getData(String kundenIDs) throws SQLException {


        sqlQuery = "SELECT id,rechnungnummer,datum FROM " + table + " WHERE nummer = '" + kundenIDs + "' AND deleted = 0";

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
        answer = new String[p][3];


        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);


                i++;
                while (rs.next() && i < 1000) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);


                    i++;
                }
            } else {

                answer = new String[1][1];
                answer[0][0] = "No Data.";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // frame.hinweis("");
        }
        frame.hinweis("Daten gefunden.");
        frame.progress(false);
        return answer;
    }

    public String askForHighNr() {

        sqlQuery = "SELECT rechnungnummer FROM rechnungen ORDER BY id";
        frame.progress(true); ////System.out.println(sqlQuery);
        ResultSet rs = null;
        try {
            rs = s.executeQuery(sqlQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String answer = "0";

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

        frame.progress(false);
        return answer;
    }

    public String[][] getAllOf(String kundenID) {

        sqlQuery = "SELECT id,rechnungnummer,datum FROM rechnungen WHERE nummer = '" + kundenID + "' ORDER BY id";
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
        answer = new String[p][15];


        try {

            if (rs.first()) {
                int i = 0;

                answer[i][0] = rs.getString(1);
                answer[i][1] = rs.getString(2);
                answer[i][2] = rs.getString(3);


                i++;
                while (rs.next()) {


                    answer[i][0] = rs.getString(1);
                    answer[i][1] = rs.getString(2);
                    answer[i][2] = rs.getString(3);


                    i++;
                }
            } else {

                answer[0][0] = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //frame.hinweis("Es werden max. die letzten 1000 Rechnungen angezeigt.");
        }
        frame.hinweis("Daten gefunden.");
        frame.progress(false);
        return answer;
    }

    void clear() {

        if (preis1.equals("0")) {
            preis1 = "";
        }
        if (preis2.equals("0")) {
            preis2 = "";
        }
        if (preis3.equals("0")) {
            preis3 = "";
        }
        if (preis4.equals("0")) {
            preis4 = "";
        }
        if (preis5.equals("0")) {
            preis5 = "";
        }
        if (preis6.equals("0")) {
            preis6 = "";
        }
        if (preis10.equals("0")) {
            preis10 = "";
        }
        if (preis7.equals("0")) {
            preis7 = "";
        }
        if (preis8.equals("0")) {
            preis8 = "";
        }
        if (preis9.equals("0")) {
            preis9 = "";
        }
    }

    void format() {

        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN);
        df.applyPattern("#,###,##0.00");
        
        try {
            gesamtnettopreisstr = df.format(gesamtnettopreis);
            gesamtpreisstr = df.format(gesamtpreis);
            mehrwertsteuerstr = df.format(mehrwertsteuer);
        } catch (Exception exception) {
        }

    }

    String[][] getModelOf(Integer ida) throws SQLException {

        if (this.id.equals(ida)) {


            if (preis1.equals("0")) {
                preis1 = "";
            }
            if (preis2.equals("0")) {
                preis2 = "";
            }
            if (preis3.equals("0")) {
                preis3 = "";
            }
            if (preis4.equals("0")) {
                preis4 = "";
            }
            if (preis5.equals("0")) {
                preis5 = "";
            }
            if (preis6.equals("0")) {
                preis6 = "";
            }
            if (preis7.equals("0")) {
                preis7 = "";
            }
            if (preis8.equals("0")) {
                preis8 = "";
            }
            if (preis9.equals("0")) {
                preis9 = "";
            }
            if (preis10.equals("0")) {
                preis10 = "";
            }
            String[][] values2 = {{posten1, preis1}, {posten2, preis2}, {posten3, preis3}, {posten4, preis4}, {posten5, preis5}, {posten6, preis6}, {posten7, preis7}, {posten8, preis8}, {posten9, preis9}, {posten10, preis10}};





            frame.progress(false);
            return values2;
        } else {


            frame.progress(false);
            return null;
        }
    }

    private String[] getDataByNumber(String nummer) throws SQLException {
        String[] values = new String[26];



        sqlQuery = "SELECT * FROM " + table + " WHERE rechnungnummer = '" + nummer + "'";

        ResultSet rs = s.executeQuery(sqlQuery);
        while (rs.next()) {

            for (int i = 0; i < 26; i++) {

                values[i] = rs.getString(i + 1);
            }
        }

        id = Integer.valueOf(values[0]);
        rechnungnummer = nummer;
        kundenID = values[1 + 1];
        posten1 = values[2 + 1];
        preis1 = values[3 + 1];
        posten2 = values[4 + 1];
        preis2 = values[5 + 1];
        posten3 = values[6 + 1];
        preis3 = values[7 + 1];
        posten4 = values[8 + 1];
        preis4 = values[9 + 1];
        posten5 = values[10 + 1];
        preis5 = values[11 + 1];
        posten6 = values[12 + 1];
        preis6 = values[13 + 1];
        posten7 = values[14 + 1];
        preis7 = values[15 + 1];
        posten8 = values[16 + 1];
        preis8 = values[17 + 1];
        posten9 = values[18 + 1];
        preis9 = values[19 + 1];
        posten10 = values[20 + 1];
        preis10 = values[21 + 1];
        datum = values[22 + 1];
        try {

            //  //System.out.println(values[25]);
//            gesamtpreis = Double.valueOf(values[23+1].replace(",", "."));
//            mehrwertsteuer = Double.valueOf(values[24+1].replace(",", "."));
//



            gesamtnettopreis = (Double.valueOf(values[23 + 1].replace(",", ".")));
            mehrwertsteuersatz = Double.valueOf(values[24 + 1].replace(",", "."));
            mehrwertsteuer = (Double.valueOf(values[23 + 1].replace(",", "."))) * (mehrwertsteuersatz / 100);

            //new out("2"); new out(gesamtnettopreis);  new out(mehrwertsteuersatz);  new out(mehrwertsteuer);
            gesamtpreis = gesamtnettopreis + mehrwertsteuer;
        } catch (Exception numberFormatException) {
            //numberFormatException.printStackTrace();
        }





        frame.progress(false);
        return values;
    }
//    public String[][] askForDeletedNumbers() {
//        String[][] deletedSouls = new String[2000][3];
//        sqlQuery = "SELECT id,rechnungnummer,nummer FROM rechnungen WHERE deleted = 1 ORDER BY id";
//        frame.progress(true); ////System.out.println(sqlQuery);
//        ResultSet rs = null;
//        try {
//            rs = s.executeQuery(sqlQuery);
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//        try {
//
//            if (rs.first()) {
//                int i = 0;
//
//                deletedSouls[i][0] = rs.getString(0);
//                deletedSouls[i][1] = rs.getString(1);
//                deletedSouls[i][2] = rs.getString(2);
//
//                i++;
//                while (rs.next() && i < 2000) {
//
//
//                    deletedSouls[i][0] = rs.getString(0);
//                    deletedSouls[i][1] = rs.getString(1);
//                    deletedSouls[i][2] = rs.getString(2);
//
//                    i++;
//                }
//            } else {
//
//                deletedSouls[0][0] = null;
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//        frame.progress(false); return deletedSouls;
//    }
//    void letReIncarnate(String id) {
//        String hk = "";
//
//        error = "wiederhergestellt";
//
//        sqlQuery = "UPDATE " + table + " SET deleted = '0' WHERE id= '" + id + "'";
//        frame.progress(true); ////System.out.println(sqlQuery);
//        try {
//            s.executeUpdate(sqlQuery);
//        } catch (SQLException ex) {
//            frame.error("Rechnung nicht " + error + " !");
//            ex.printStackTrace();
//        }
//        frame.hinweis("Rechnung " + error + " !");
//    }
    /*
    public String[][] askForAllNames() {
    sqlQuery = "SELECT name,nummer FROM kunden WHERE deleted = 0 ORDER BY Name";
    frame.progress(true); ////System.out.println(sqlQuery);
    ResultSet rs = null;
    try {
    rs = s.executeQuery(sqlQuery);
    } catch (SQLException ex) {
    ex.printStackTrace();
    }
    answer = new String[2000][16];
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
    frame.progress(false); return answer;
    }
     */
/*
    public String[][] askForNames() {
    sqlQuery = "SELECT name,nummer FROM kunden WHERE deleted = 0 ORDER BY Name";
    frame.progress(true); ////System.out.println(sqlQuery);
    ResultSet rs = null;
    try {
    rs = s.executeQuery(sqlQuery);
    } catch (SQLException ex) {
    ex.printStackTrace();
    }
    answer = new String[100][15];
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
    frame.progress(false); return answer;
    }
     */
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
//            frame.progress(false); return true;
//        } else {
//            frame.progress(false); return false;
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
//        for (int i = 0; i < NRField.length; i++) {
//
//            b = b + NRField[i] + ",";
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
//            frame.progress(false); return false;
//        }
//
//        frame.hinweis("Datensatz angelegt !");
//        frame.progress(false); return true;
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
//            frame.progress(false); return "toolong";
//        }
//
//        frame.hinweis("Datensatz angelegt !");
//        frame.progress(false); return "ok";
//    }
/*
    public String[] searchIds(String field, String value) throws SQLException {
    search=null;
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
    frame.progress(false); return null;
    }
    frame.hinweis("Daten gefunden. Es werden max. 100 Kunden angezeigt.");
    frame.progress(false); return search;
    }
    public String[] getData(String nummer) throws SQLException {
    String[] values = new String[15];
    sqlQuery = "SELECT * FROM " + table + " WHERE nummer = '" + nummer + "' AND deleted = 0";
    ResultSet rs = s.executeQuery(sqlQuery);
    while (rs.next()) {
    for (int i = 0; i < 14; i++) {
    values[i] = rs.getString(i + 1);
    }
    }
    id = Integer.valueOf(values[0]);
    Kundennummer = nummer;
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
    deleted = Integer.valueOf(values[15]);
    frame.progress(false); return values;
    }
     */
/*     public String[][] askForNumbersAndWhere(String[] ids) {
    String whatsup = "";
    if(ids!=null){
    for (int i = 0; ids[i] != null; i++) {
    whatsup = whatsup + " id = " + ids[i] + " OR ";
    }
    whatsup = whatsup.substring(0, whatsup.length() - 4);
    whatsup=whatsup + " AND ";
    sqlQuery = "SELECT id,nummer,name,firma,tel FROM kunden WHERE " + whatsup + " deleted = 0 ORDER BY Name";
    frame.progress(true); ////System.out.println(sqlQuery);
    ResultSet rs = null;
    try {
    rs = s.executeQuery(sqlQuery);
    } catch (SQLException ex) {
    ex.printStackTrace();
    }
    answer = new String[100][15];
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
    frame.hinweis("Daten gefunden. Es werden max. 100 Kunden angezeigt.");
    ids=null;
    frame.progress(false); return answer;
    }else {frame.progress(false); return null;}
    } */
    
    class list implements Printable{

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    
    
    }
}