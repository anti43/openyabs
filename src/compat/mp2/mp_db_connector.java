/*
 * dbConn.java
 *
 * Created on 6. März 2007, 13:27
 *
 * Die Datenbankverbindung
 */

package compat.mp2;

import java.sql.*;
import mp3.classes.utils.Log;
import mp3.classes.utils.Out;



/**
 *
 * @author anti
 */
public class mp_db_connector {
    public Statement stmt ;
    public String sqlQuery;
    public ResultSet rs;
    public Connection con;
    public int counter;
    public String framework = "embedded";
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    public String protocol = "jdbc:derby:";
    // private String databasename="mpData_JDB";
    private Connection conn = null;
    public Statement s;
     String homedir = System.getProperty("user.home");
    //private String url= homedir + "\\.mp\\mpDatabase";
     String url= homedir + "/.mp/mpDatabase";


    
    
    public void connection()
    throws SQLException, ClassNotFoundException { // Doit better ...
        try {
            Class.forName(driver).newInstance();  //Load driver
        } catch (ClassNotFoundException ex) {
            //ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            //ex.printStackTrace();
        } catch (InstantiationException ex) {
            //ex.printStackTrace();
        }
        try {
            Log.Debug(url);
            conn = DriverManager.getConnection(protocol+
                    url,"mp","mp");
              s = conn.createStatement(rs.TYPE_SCROLL_INSENSITIVE,rs.CONCUR_READ_ONLY);
        System.out.println(s);
        } catch (SQLException ex) {

        }
        
      
    }
    
    public Statement getStatement(){
        
        return s;
    }

    
    public mp_db_connector() {
        try {
            this.connection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
}
