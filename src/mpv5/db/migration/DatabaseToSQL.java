/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.migration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.ProgressMonitor;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.QueryHandler;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.frames.MPView;
import mpv5.utils.text.RandomText;

/**
 *
 * @author Jan Hahnisch
 */
public class DatabaseToSQL {

    private String backupPath;
    private ProgressMonitor pm;

    public DatabaseToSQL(String backupPath, boolean restore) {
        this.backupPath = backupPath;
        if (!restore) {
            try {
                this.backupPath = this.backupPath + File.separatorChar + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                if (!new File(this.backupPath).exists()) {
                    new File(this.backupPath).mkdir();
                } else {
                    this.backupPath = this.backupPath + "-" + RandomText.getText();
                    new File(this.backupPath).mkdir();
                }
                FileChannel oldFile = new FileInputStream(LocalSettings.getLocalFile()).getChannel();
                FileChannel newFile = new FileOutputStream(new File(this.backupPath + File.separatorChar + "settings0.yabs")).getChannel();
                oldFile.transferTo(0, oldFile.size(), newFile);
            } catch (IOException ex) {
                Log.Debug(this, ex.getLocalizedMessage());
            }
        }
    }

    public void ExportToDump() throws SQLException, Exception {
        switch (ConnectionTypeHandler.getDriverType()) {
            case ConnectionTypeHandler.DERBY:
                final DerbyToMySQL dtd = new DerbyToMySQL(
                        DatabaseConnection.instanceOf().getConnection());
                try {
                    dtd.setCharSet("UTF8");
                    dtd.setEngine("MyIsam");
                    Thread thread = new Thread(new Runnable() {

                        public void run() {
                            try {
                                dtd.writeDerbyToMySQLDump(new File(backupPath + File.separatorChar + "DUMP.sql"),
                                        MPView.identifierFrame, Messages.EXPORT_TO_MYSQL.toString());
                            } catch (Exception ex) {
                                Log.Debug(DatabaseToSQL.class, ex.getLocalizedMessage());
                            }
                        }
                    });
                    thread.start();
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
                break;
            case ConnectionTypeHandler.MYSQL:
                final MySQLToMySQL mtd = new MySQLToMySQL(
                        DatabaseConnection.instanceOf().getConnection());
                try {
                    mtd.setCharSet("UTF8");
                    mtd.setEngine("MyIsam");
                    mtd.setSchema(LocalSettings.getProperty(LocalSettings.DBNAME));
                    Thread thread = new Thread(new Runnable() {

                        public void run() {
                            try {
                                mtd.writeMySQLToMySQLDump(new File(backupPath + File.separatorChar + "DUMP.sql"),
                                        MPView.identifierFrame, Messages.EXPORT_TO_MYSQL.toString());
                            } catch (SQLException ex) {
                                Log.Debug(DatabaseToSQL.class, ex.getLocalizedMessage());
                            }
                        }
                    });
                    thread.start();
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
                break;
        }
    }

    public boolean createFromDump(final String dump, final DatabaseConnection dc, final WizardMaster master) throws SQLException {
        BufferedReader in;
        try {
            pm = new ProgressMonitor(
                    MPView.identifierFrame, Messages.ACTION_CREATING.toString(), "", 0, 100);
            dc.connect(master.getStore().getProperty("driver"),
                    master.getStore().getProperty("user"),
                    master.getStore().getProperty("password"),
                    master.getStore().getProperty("url"),
                    master.getStore().getProperty("dbname"),
                    master.getStore().getProperty("dbprefix"),
                    true);
            String tmp = "";
            in = new BufferedReader(new FileReader(backupPath + File.separatorChar + dump));
            String zeile;
            ArrayList<String> zeilen = new ArrayList<String>();
            while ((zeile = in.readLine()) != null) {
                if (!zeile.startsWith("#") && !zeile.startsWith("--")) {
                    if (zeile.endsWith(";")) {
                        zeilen.add(tmp + zeile);
                        tmp = "";
                    } else {
                        if (!zeile.trim().isEmpty()) {
                            tmp = tmp + "\n" + zeile;
                        }
                    }
                }
            }
            pm.setMaximum(zeilen.size() - 1);
            Statement stmt;
            Connection con = dc.getConnection();
            for (int i = 0; i < zeilen.size(); i++) {
                pm.setProgress(i);
                pm.setNote("Inserting Line " + (i + 1) + " of " + zeilen.size());
                if (!zeilen.get(i).equalsIgnoreCase("")) {
                    try {
                        stmt = con.createStatement();
                        stmt.execute(zeilen.get(i));
                    } catch (SQLException ex2) {
                        Log.Debug(this, ex2.getLocalizedMessage());
                    }
                }
            }
            in.close();
        } catch (Exception ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
        return true;
    }

    public boolean restoreFromDump() throws SQLException {
        try {
            restoreMysql();
            return true;
        } catch (Exception ex) {
            Log.Debug(ex);
            return false;
        }
    }

    private void restoreMysql() {
        Thread thread = new Thread(new Runnable() {

            public void run() {
                BufferedReader in;
                String dbname;
                try {
                    pm = new ProgressMonitor(
                            MPView.identifierFrame, Messages.ACTION_RESTORING.toString(), "", 0, 100);
                    String tmp = "";
                    in = new BufferedReader(new FileReader(backupPath));
                    String zeile;
                    ArrayList<String> zeilen = new ArrayList<String>();
                    boolean create = Popup.Y_N_dialog(Messages.DELETE_DATABASE.toString());
                    if (create) {
                        dbname = LocalSettings.getProperty(LocalSettings.DBNAME);
                        zeilen.add("DROP SCHEMA " + dbname + ";");
                        zeilen.add("CREATE SCHEMA " + dbname + ";");
                        zeilen.add("USE " + dbname + ";");
                    }
                    while ((zeile = in.readLine()) != null) {
                        if (!zeile.startsWith("#") && !zeile.startsWith("--")) {
                            if (zeile.endsWith(";")) {
                                zeilen.add(tmp + zeile);
                                tmp = "";
                            } else {
                                if (!zeile.trim().isEmpty()) {
                                    tmp = tmp + "\n" + zeile;
                                }
                            }
                        }
                    }
                    pm.setMaximum(zeilen.size() - 1);
                    Statement stmt;
                    for (int i = 0; i < zeilen.size(); i++) {
                        pm.setProgress(i);
                        pm.setNote("Inserting Line " + (i + 1) + " of " + zeilen.size());
                        if (!zeilen.get(i).equalsIgnoreCase("")) {
                            try {
                                DatabaseConnection dc;
                                dc = DatabaseConnection.instanceOf();
                                stmt = dc.getConnection().createStatement();
                                stmt.execute(zeilen.get(i));
                            } catch (SQLException ex2) {
                                Log.Debug(this, ex2.getLocalizedMessage());
                            }
                        }
                    }
                    in.close();
                    DatabaseConnection dc = DatabaseConnection.instanceOf();
                    DatabaseConnection.shutdown();
                    mpv5.YabsViewProxy.instance().setWaiting(true);
                    if (!dc.reconnect(false)) {
                        if (!QueryHandler.instanceOf().reset()) {
                            Popup.notice(Messages.ERROR_OCCURED.toString() + "\n"
                                    + Messages.RESTART_REQUIRED.toString());
                            Main.getApplication().exit();
                        }
                    } else {
                        mpv5.YabsViewProxy.instance().getProgressbar().setValue(100);
                        Popup.OK_dialog(Messages.DONE.toString(), Messages.ACTION_IMPORTING.toString());
                        mpv5.YabsViewProxy.instance().setWaiting(false);
                        QueryHandler.instanceOf().reset();
                    }
                } catch (Exception ex) {
                    Log.Debug(this, ex.getLocalizedMessage());
                }
            }
        });
        thread.start();
    }
}
