/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JProgressBar;
import mpv5.Main;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;

/**
 *
 *
 */
public class DatabaseConnection {

   private static java.sql.Connection conn;
   private static DatabaseConnection connector;

   /**
    * @return the connector
    */
   public static DatabaseConnection getConnector() {
      return connector;
   }

   /**
    * @return the user
    */
   public static String getUser() {
      return user;
   }

   /**
    * @return the password
    */
   public static String getPassword() {
      return password;
   }

   /**
    * @return the prefix
    */
   public static String getPrefix() {
      return prefix;
   }
   private ConnectionTypeHandler ctype;
   private static String user;
   private static String password;
   private static String prefix = "";

   /**
    *
    * @return Database connector
    * @throws Exception
    */
   public static synchronized DatabaseConnection instanceOf() throws Exception {
      if (getConnector() == null || getConnector().getConnection() == null) {
         connector = new DatabaseConnection();
         getConnector().connect(false);
         new connectionPing(connector).start();
      }
      return getConnector();
   }
   private Statement statement;
   private JProgressBar prog;

   /**
    *
    * @return
    */
   public java.sql.Connection getConnection() {
      return conn;
   }

   /**
    * Test-Verbindung zur Datenbank herstellen.
    *
    * @param predefinedDriver
    * @param user
    * @param password
    * @param location
    * @param dbname
    * @param create
    * @return Connection
    * @throws Exception
    */
   public boolean connect(String predefinedDriver, String user, String password, String location, String dbname, String prefix, boolean create) throws Exception {

      try {
         ctype = new ConnectionTypeHandler();
         getCtype().setDRIVER(predefinedDriver);
         getCtype().setURL(location);
         getCtype().setDBName(dbname);
         getCtype().setPrefix(prefix);
         DatabaseConnection.user = user;
         DatabaseConnection.password = password;
         DatabaseConnection.prefix = prefix;

         DriverManager.registerDriver((Driver) Class.forName(getCtype().getDriver()).newInstance());
         Log.Debug(this, "Driver: " + getCtype().getDriver());
      } catch (Exception ex) {
         Log.Debug(this, ex);
      }

      return reconnect(create);
   }

   public boolean reconnect(boolean create) throws SQLException {
      Statement stmt;
      String sql;
      if (Log.isDebugging()) {
//         Thread.dumpStack();
      }
      try {
         Log.Debug(this, "RECONNECT::Datenbankverbindung: " + getCtype().getConnectionString(create));
         conn = DriverManager.getConnection(getCtype().getConnectionString(create), user, password);
         boolean result = true;
         if (conn != null //&& conn.isValid(10)//does not work with MySQL Connector/J 5.0
               ) {//mysql (and others) need explicit create database, derby does it by itself
            conn.setAutoCommit(true);
            if (create && ConnectionTypeHandler.getDriverType() != ConnectionTypeHandler.DERBY) {
               stmt = conn.createStatement();
               if (User.PROPERTIES_OVERRIDE.hasProperty("drop_database_on_create")) {
                  try {
                     sql = "DROP DATABASE "
                           + ConnectionTypeHandler.getDBNAME()
                           + ";";
                     stmt.execute(sql);
                  } catch (SQLException ex) {
                     Log.Debug(this, "Database Error cleaning of old DB failed!");
                  }
               }
               try {
                  sql = "CREATE DATABASE "
                        + ConnectionTypeHandler.getDBNAME()
                        + " ;";
                  stmt.execute(sql);
               } catch (SQLException ex) {
                  Log.Debug(ex);
                  result = Popup.Y_N_dialog("Could not create database " + ConnectionTypeHandler.getDBNAME() + ", did you already create it?", "Database Creation");
               }
            }
            conn.setCatalog(ConnectionTypeHandler.getDBNAME());
            connector = this;
            return result;
         } else {
            throw new RuntimeException("Could not create connection: " + getCtype().getConnectionString(create));
         }
      } catch (SQLException ex) {
         System.out.println("Database Error: " + ex.getMessage());
         Popup.notice(ex.getLocalizedMessage());
         Log.Debug(this, ex);
         Log.Debug(this, ex.getNextException());

         DatabaseConnection.shutdown();
         throw ex;
      }
   }

   /**
    * Verbindung zur Datenbank herstellen.
    *
    * @return Connection
    */
   private Connection connect(boolean create) throws Exception {

      ctype = new ConnectionTypeHandler();

      if (getCtype().getDriver() != null) {
         Log.Debug(this, "Datenbanktreiber: " + getCtype().getDriver());

         try {
            Class.forName(getCtype().getDriver()).newInstance();
         } catch (ClassNotFoundException ex) {
//                Popup.error(ex);
            //possibly not fatal, driver can have a different class [org.apache.derby.jdbc.ClientDriver,org.apache.derby.jdbc.EmbeddedDriver]
            Log.Debug(this, ex.getMessage());
         }
         user = LocalSettings.getProperty("dbuser");
         password = LocalSettings.getProperty("dbpassword");
         prefix = LocalSettings.getProperty("dbprefix");

         reconnect(create);
      } else {
         throw new UnsupportedOperationException("Datenbanktreiber: undefined");
      }
      return conn;
   }

   /**
    * Verbindung trennen
    */
   @SuppressWarnings("CallToThreadDumpStack")
   public static void shutdown() {
      try {
         if (conn != null && !conn.isClosed()) {
            if (ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
               try {
                  DriverManager.getConnection(
                        DatabaseConnection.instanceOf().getCtype().getConnectionString(false) + "shutdown=true;", user, password);
               } catch (Exception ex) {
                  Log.Debug(DatabaseConnection.class, ex.getLocalizedMessage());
               }
            }
            conn.close();
            conn = null;
         }

      } catch (SQLException ex) {

         ex.printStackTrace();
         System.exit(1);
      }
   }

   public boolean runQueries(String[] queries) throws SQLException {
      if (prog != null) {
         prog.setStringPainted(true);
         prog.setMaximum(queries.length);
         prog.setMinimum(0);
         prog.setValue(0);
      }
      Statement stm = this.getConnection().createStatement();
//        for (int i = 0; i < queries.length; i++) {
//            stm.addBatch(queries[i]);
//        }
//        Log.PrintArray(queries);
      try {
//            stm.executeBatch();
         for (int i = 0; i < queries.length; i++) {
            try {
               String string = queries[i];
               if (prefix != null) {
                  string = string.replace(" table ", " table " + prefix);
                  string = string.replace(" TABLE ", " TABLE " + prefix);
                  string = string.replace(" on ", " on " + prefix);
                  string = string.replace(" ON ", " ON " + prefix);
                  string = string.replace(" into ", " into " + prefix);
                  string = string.replace(" INTO ", " INTO " + prefix);
               }
               Log.Print(string);
               stm.execute(string);
               if (prog != null) {
                  prog.setValue(i);
               }
               Thread.sleep(100);
            } catch (Exception ex) {
               throw ex;
            }
         }
         return true;
      } catch (Exception sQLException) {
         Log.Debug(this, "-----------> "+sQLException.getMessage());
         return false;
      } finally {
         if (prog != null) {
            prog.setValue(0);
         }
      }
   }

   /**
    * Set a progressbar
    *
    * @param progressbar
    */
   public void setProgressbar(JProgressBar progressbar) {
      prog = progressbar;
   }

   /**
    * @return the ctype
    */
   public ConnectionTypeHandler getCtype() {
      return ctype;
   }

   /**
    * @return the statement
    */
   public Statement getStatement() {
      return statement;
   }

   static class connectionPing extends Thread {

      private DatabaseConnection c;

      public connectionPing(DatabaseConnection c) {
         this.c = c;
         setPriority(MIN_PRIORITY);
      }

      @Override
      public void run() {
         try {
            while (c.getConnection() != null || !c.getConnection().isClosed()) {
               try {
                  sleep(180000);
               } catch (InterruptedException ex) {
                  Log.Debug(ex);
               }
               try {
                  Log.Debug(this, "ping to " + c.getCtype().getURL());
                  c.runQueries(new String[]{"select count(ids) from groups"});
               } catch (SQLException ex) {
                  Log.Debug(ex);
               }
            }
         } catch (SQLException ex) {
            Log.Debug(ex);
         }
      }
   }
}
