/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compat.mp2;

/**
 *
 * @author anti43
 */
public class mainFrame {
    public String[] initialColumnNames = {"id", "KNR", "Name", "Firma", "Telefon"};
    public String[] initialColumnNames2 = {"id", "KNR", "Name", "Firma", "Ort"};
    public String[] initialRColumnNames = {"id", "Rechnungen", "Datum"};
    private mp_db_connector dbconn;

    public mainFrame() {
        dbconn = new mp_db_connector();
    }

    public mp_db_connector getDbconn() {
        return dbconn;
    }

    void error(String string) {
       
    }

    mp_db_connector getDBConn() {
        return getDbconn();
    }

    void hinweis(String string) {
      
    }

    void progress(boolean b) {
        
    }
}


