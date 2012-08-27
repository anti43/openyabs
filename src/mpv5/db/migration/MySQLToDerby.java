package mpv5.db.migration;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseUpdater;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * 
 * @author Develop
 */
public class MySQLToDerby {

    private final String DRV = "com.mysql.jdbc.Driver";
    private String schema = "";
    private Connection con = null;
    private DatabaseMetaData Meta = null;
    private ProgressMonitor pm;
    private File file;
    private String DBNAME = "migration";
    private Connection derby;
    private HashMap<String, HashMap<String, ArrayList<String>>> SQL = new HashMap<String, HashMap<String, ArrayList<String>>>();
    private ArrayList<String> tabnames = new ArrayList<String>();

    public MySQLToDerby(String Url) throws ClassNotFoundException,
            InstantiationException,
            SQLException,
            IllegalAccessException {
        Class.forName(DRV).newInstance();
        con = DriverManager.getConnection(Url);
    }

    public MySQLToDerby(Connection con) throws SQLException {
        this.con = con;
        Meta = con.getMetaData();
    }

    public void writeMySQLToDerby(String Path, JFrame app, String msg) throws SQLException {
        file = new File(Path);
        pm = new ProgressMonitor(app, msg, "", 0, 1);
        pm.setMinimum(0);
        mpv5.YabsViewProxy.instance().setWaiting(true);
        if (pm != null) {
            pm.setProgress(1);
            pm.setNote(Messages.INIT_TMP_DERBY.toString());
        }
        if (!pm.isCanceled()) {
            createBackupDB();
        }
        if (!pm.isCanceled()) {
            explodeTables();
        }
        if (!pm.isCanceled()) {
            runQueries();
        }
        if (pm != null) {
            pm.setProgress(pm.getMaximum());
            pm.setNote(Messages.DONE.toString());
        }
        fixloginsettings();
        mpv5.YabsViewProxy.instance().setWaiting(false);
        pm.close();
    }

    private void explodeTables() throws SQLException {
        String[] types = {"TABLE"};
        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
        ResultSet tables = Meta.getTables(null, schema, "%", types);
        while (tables.next() && !pm.isCanceled()) {
            String tableName = tables.getString("TABLE_NAME");
            tableName = tableName.toLowerCase();
            pm.setProgress(tables.getRow());
            pm.setNote(Messages.ACTION_CALCULATING.toString() + tableName);
            StringBuilder result = new StringBuilder();
            result.append("DELETE FROM ").append(tableName).append("");
            ArrayList<String> entry = new ArrayList<String>();
            entry.add(result.toString());
            hm.put(tableName, entry);
            SQL.put(Messages.ACTION_SAVING.toString(), hm);
            dumpTable(con, tableName);

        }
        tables.close();
    }

    private void dumpTable(Connection dbConn, String tableName) throws SQLException {

        PreparedStatement stmt = dbConn.prepareStatement("SELECT * FROM " + tableName);
        ResultSet rs = stmt.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        ArrayList<String> entry;
        int u = 0;
        while (rs.next() && !pm.isCanceled()) {
            u++;
            StringBuilder result = new StringBuilder();
            HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
            result.append("INSERT INTO ").append(tableName).append(" VALUES (");
            for (int i = 0; i < columnCount; i++) {
                if (i > 0) {
                    result.append(", ");
                }
                Object value = rs.getObject(i + 1);
                if (value == null) {
                    result.append("NULL");
                } else {
                    if (metaData.getColumnType(i + 1) == java.sql.Types.BLOB
                            || metaData.getColumnType(i + 1) == java.sql.Types.LONGVARBINARY) {
                        result.append("0x#BLOB");
                    } else if (metaData.getColumnType(i + 1) == java.sql.Types.BIGINT
                            || metaData.getColumnType(i + 1) == java.sql.Types.SMALLINT
                            || metaData.getColumnType(i + 1) == java.sql.Types.DOUBLE
                            || metaData.getColumnType(i + 1) == java.sql.Types.INTEGER) {
                        result.append(value.toString());
                    } else {
                        String outputValue = value.toString();
                        outputValue = outputValue.replaceAll("'", "\\'");
                        result.append("'").append(outputValue).append("'");
                    }
                }
            }
            result.append(")");
            HashMap<String, ArrayList<String>> tmp = SQL.get(Messages.ACTION_SAVING.toString());
            if (tmp != null) {
                if (tmp.containsKey(tableName)) {
                    entry = tmp.get(tableName);
                    entry.add(result.toString());
                } else {
                    entry = new ArrayList<String>();
                    entry.add(result.toString());
                    tmp.put(tableName, entry);
                }
            } else {
                entry = new ArrayList<String>();
                entry.add(result.toString());
                hm.put(tableName, entry);
                SQL.put(Messages.ACTION_SAVING.toString(), hm);
            }
        }
        pm.setMaximum(pm.getMaximum() + u);
        rs.close();
        stmt.close();
    }

    private void WriteSQLDumpToDERBYFiles() {
        String[] types = {"TABLE"};
        String tabname;
        HashMap<String, ArrayList<String>> tmp1 = null;
        ArrayList<String> tmp2 = null;
        HashMap<String, ArrayList<String>> vals = new HashMap<String, ArrayList<String>>();
        tmp1 = SQL.get(Messages.ACTION_SAVING.toString());
        int u;
        for (u = 0; u < tabnames.size(); u++) {
            tabname = tabnames.get(u).toLowerCase();
            tmp2 = tmp1.get(tabname);
            if (tmp2 != null) {
                vals.put(String.valueOf(u), tmp2);
                tmp1.remove(tabname);
            }
        }
        ResultSet rs;
        StringBuilder result = null;
        ResultSet cols;
        for (Iterator<String> keys = tmp1.keySet().iterator(); keys.hasNext();) {
            tabname = keys.next();
            tmp2 = tmp1.get(tabname);
            if (tmp2 != null) {
                try {
                    rs = derby.getMetaData().getTables(null, schema, tabname, types);
                    if (!rs.next()) {
                        try {
                            result = new StringBuilder();
                            result.append("CREATE TABLE ").append(tabname);
                            result.append(" (");
                            cols = Meta.getColumns(null, schema, tabname, "%");
                            while (cols.next()) {
                                result.append(cols.getString("COLUMN_NAME").toLowerCase()).append(" ");
                                result.append(cols.getString("TYPE_NAME"));
                                if (cols.getString("TYPE_NAME").equalsIgnoreCase("VARCHAR")) {
                                    result.append("(").append(cols.getString("COLUMN_SIZE")).append(")").append(" ");
                                }
                                if (cols.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES")) {
                                    result.append(" NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1)");
                                }
                                if (cols.getString("COLUMN_DEF") != null) {
                                    result.append(" DEFAULT ");
                                    result.append(cols.getString("COLUMN_DEF"));
                                }
                                if (cols.getString("IS_NULLABLE").equalsIgnoreCase("NO")) {
                                    result.append(" NOT NULL");
                                } else {
                                    if (cols.getString("COLUMN_DEF") == null) {
                                        result.append(" DEFAULT ");
                                        result.append(cols.getString("COLUMN_DEF"));
                                    }
                                }
                                if (!cols.isLast()) {
                                    result.append(",");
                                }
                            }
                            result.append(", PRIMARY KEY  (ids))");
                            cols.close();
                            tmp2.add(0, result.toString());
                        } catch (SQLException ex) {
                            Log.Debug(this, ex.getLocalizedMessage());
                        }
                    }
                } catch (SQLException ex) {
                    Log.Debug(this, ex.getLocalizedMessage());
                }
                vals.put(String.valueOf(u++), tmp2);
            }
        }
        tmp1.clear();
        tmp1.putAll(vals);
    }

    private void createBackupDB() {
        Map<Double, String[]> UPDATES_DERBY;
        ArrayList<String> create = new ArrayList<String>(Arrays.asList(DatabaseInstallation.getStructureForMigration(ConnectionTypeHandler.DERBY)));
        ArrayList<String> update = new ArrayList<String>();
        ArrayList<String> tmp = null;
        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
        hm.put("all", create);
        SQL.put(Messages.ACTION_CREATING.toString(), hm);
        UPDATES_DERBY = new DatabaseUpdater().getStructureForMigration(ConnectionTypeHandler.DERBY);
        for (Iterator<Double> keys = UPDATES_DERBY.keySet().iterator(); keys.hasNext();) {
            Double vers = keys.next();
            String[] val = UPDATES_DERBY.get(vers);
            try {
                tmp = new ArrayList<String>(Arrays.asList(val));
                update.addAll(tmp);
            } catch (Exception ex) {
                if (ex.getMessage().contains("does not exist")) {
                    Log.Debug(this, ex.getLocalizedMessage());
                }
            }
        }
        hm = new HashMap<String, ArrayList<String>>();
        pm.setMaximum(pm.getMaximum() + create.size() + update.size());
        hm.put("all", update);
        SQL.put(Messages.ACTION_UPDATING.toString(), hm);
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private void runQueries() {
        Statement stm;
        String tabname;
        String msg;
        String query = null;
        int lastProgress = 0;
        try {
            String cstring = "jdbc:derby:" + file.getAbsolutePath() + File.separator + DBNAME + ";create=true;";
            Class.forName(DRV).newInstance();
            pm.setNote(Messages.INIT_TMP_DERBY.toString());
            derby = DriverManager.getConnection(cstring);
            stm = derby.createStatement();
            msg = Messages.ACTION_CREATING.toString();
            HashMap<String, ArrayList<String>> val1 = SQL.get(msg);
            for (Iterator<String> tabs = val1.keySet().iterator(); tabs.hasNext();) {
                tabname = tabs.next();
                ArrayList<String> list = val1.get(tabname);
                for (int i = 0; i < list.size(); i++) {
                    query = list.get(i);
                    if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                        Log.Print(query);
                    }
                    execStmt(query, stm, msg, lastProgress + i);
                    if (pm.isCanceled()) {
                        i = list.size();
                    }
                }
                lastProgress += list.size();
            }
            msg = Messages.ACTION_UPDATING.toString();
            HashMap<String, ArrayList<String>> val2 = SQL.get(msg);
            for (Iterator<String> tabs = val2.keySet().iterator(); tabs.hasNext();) {
                tabname = tabs.next();
                ArrayList<String> list = val2.get(tabname);
                for (int i = 0; i < list.size(); i++) {
                    query = list.get(i);
                    if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                        Log.Print(query);
                    }
                    execStmt(query, stm, msg, lastProgress + i);
                    if (pm.isCanceled()) {
                        i = list.size();
                    }
                }
                lastProgress += list.size();
            }
            WriteSQLDumpToDERBYFiles();
            msg = Messages.ACTION_SAVING.toString();
            HashMap<String, ArrayList<String>> val3 = SQL.get(msg);
            for (int u = 0; u < val3.size(); u++) {
                ArrayList<String> list = val3.get(String.valueOf(u));
                for (int i = 0; i < list.size(); i++) {
                    query = list.get(i);
                    if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
                        Log.Print(query);
                    }
                    execStmt(query, stm, msg, lastProgress + i);
                    if (pm.isCanceled()) {
                        i = list.size();
                    }
                }
                lastProgress += list.size();
            }
        } catch (InstantiationException ex) {
            Log.Debug(MySQLToDerby.class, ex.getLocalizedMessage());
        } catch (IllegalAccessException ex) {
            Log.Debug(MySQLToDerby.class, ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            Log.Debug(MySQLToDerby.class, ex.getLocalizedMessage());
        } catch (Exception ex) {
            pm.close();
            pm = null;
            Log.Print(query);
            Log.Debug(this, ex.getLocalizedMessage());
        }
    }

    private void execStmt(String query, Statement stm, String msg, int i) {
        String tabname;
        ResultSet rs;
        String tmp;
        if (pm != null) {
            pm.setProgress(i);
            try {
                if (query.startsWith("DELETE")) {
                    pm.setNote(msg + Messages.FLUSHING_TMP_TABLE.toString());
                    stm.execute(query);
                } else if (query.contains("0x#BLOB")) {
                    pm.setNote(msg + query);
                    tmp = query.substring(query.indexOf("(") + 1, query.indexOf(","));
                    PreparedStatement ps1;
                    ps1 = con.prepareStatement("Select data from files where ids = ?");
                    ps1.setInt(1, Integer.parseInt(tmp));
                    rs = ps1.executeQuery();
                    rs.next();
                    String sql = query.replace("0x#BLOB", "?");
                    PreparedStatement ps2;
                    ps2 = derby.prepareStatement(sql);
                    ps2.setBinaryStream(1, rs.getBinaryStream("data"));
                    ps2.execute();
                } else if (query.startsWith("INSERT INTO")
                        && !msg.equalsIgnoreCase(Messages.ACTION_SAVING.toString())) {
                    pm.setNote(msg + query);
                    Log.Print("Insert rejected because not necessary!");
                } else if (query.startsWith("CREATE TABLE")) {
                    pm.setNote(msg + query);
                    stm.execute(query);
                    tabname = query.toUpperCase().replace("CREATE TABLE", "");
                    tabname = tabname.substring(0, tabname.indexOf("(")).trim();
                    tabnames.add(tabname);
                    Log.Print("Tablename for Ordering of inserts saved!" + tabname);
                } else {
                    pm.setNote(msg + query);
                    stm.execute(query);
                }
            } catch (SQLException ex) {
                Log.Debug(this, ex.getLocalizedMessage());
            }
        }
    }

    private void fixloginsettings() {
        try {
            Statement Stmt = derby.createStatement();
            Stmt.execute("UPDATE users SET password='5F4DCC3B5AA765D61D8327DEB882CF99' where IDS = 1");
            Stmt.execute("UPDATE users SET isloggedin=0 where isloggedin = 1");
            Stmt.close();
        } catch (SQLException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
    }
}