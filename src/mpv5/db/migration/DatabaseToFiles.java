/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class DatabaseToFiles {

    private String backupPath;

    public DatabaseToFiles(String backupPath, boolean restore) {
        this.backupPath = backupPath;
        if (!restore) {
            this.backupPath = this.backupPath + File.separatorChar + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if (!new File(this.backupPath).exists()) {
                new File(this.backupPath).mkdir();
            } else {
                this.backupPath = this.backupPath + "-" + RandomText.getText();
                new File(this.backupPath).mkdir();
            }
        }
    }

    public void performFiles() throws SQLException {
        try {
            final Connection conn = DatabaseConnection.instanceOf().getConnection();
            switch (ConnectionTypeHandler.getDriverType()) {
                case ConnectionTypeHandler.DERBY:
                    Thread thread = new Thread(new Runnable() {

                        public void run() {
                            try {
                                new DerbyToDerby(conn).writeDerbyToDerby(backupPath,
                                        MPView.identifierFrame,
                                        Messages.EXPORT_TO_DERBY.toString());
                            } catch (SQLException ex) {
                                Log.Debug(DatabaseToFiles.class, ex.getLocalizedMessage());
                            } catch (FileNotFoundException ex) {
                                Log.Debug(DatabaseToFiles.class, ex.getLocalizedMessage());
                            } catch (IOException ex) {
                                Log.Debug(DatabaseToFiles.class, ex.getLocalizedMessage());
                            }
                        }
                    });
                    thread.start();

                    break;
                case ConnectionTypeHandler.MYSQL:
                    thread = new Thread(new Runnable() {

                        public void run() {
                            try {
                                new MySQLToDerby(conn).writeMySQLToDerby(backupPath,
                                        MPView.identifierFrame,
                                        Messages.EXPORT_TO_DERBY.toString());
                            } catch (SQLException ex) {
                                Log.Debug(DatabaseToFiles.class, ex.getLocalizedMessage());
                            }
                        }
                    });
                    thread.start();

                    break;
                case ConnectionTypeHandler.CUSTOM:
                    /*FIXME JAN: What should be done here? */
                    Popup.warn(Messages.NOT_YET_IMPLEMENTED.toString());
                    break;
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    public void createFromBackup(final String dbname, final DatabaseConnection dc, final WizardMaster master) throws SQLException {
        try {
            ConnectionTypeHandler.setCREATEDERBY("createFrom="
                    + backupPath + File.separator
                    + dbname);
            if (dc.connect(master.getStore().getProperty("driver"),
                    master.getStore().getProperty("user"),
                    master.getStore().getProperty("password"),
                    master.getStore().getProperty("url"),
                    master.getStore().getProperty("dbname"),
                    master.getStore().getProperty("dbprefix"),
                    true)) {
                ConnectionTypeHandler.resetCREATEXXX();
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    public void restoreFromBackup() throws SQLException {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    mpv5.YabsViewProxy.instance().setWaiting(true);
                    mpv5.YabsViewProxy.instance().getProgressbar().setValue(33);
                    mpv5.YabsViewProxy.instance().getProgressbar().setString(Messages.ACTION_SHUTINGDOWN.toString());
                    mpv5.YabsViewProxy.instance().getProgressbar().setStringPainted(true);
                    DatabaseConnection dc = DatabaseConnection.instanceOf();
                    DatabaseConnection.shutdown();
                    mpv5.YabsViewProxy.instance().setWaiting(true);
                    ConnectionTypeHandler.setCREATEDERBY("restoreFrom="
                            + backupPath + File.separator
                            + LocalSettings.getProperty(LocalSettings.DBNAME));
                    mpv5.YabsViewProxy.instance().getProgressbar().setValue(66);
                    mpv5.YabsViewProxy.instance().getProgressbar().setString(Messages.ACTION_RESTORING.toString());
                    mpv5.YabsViewProxy.instance().getProgressbar().setStringPainted(true);
                    if (dc.reconnect(true)) {
                        ConnectionTypeHandler.resetCREATEXXX();
                        if (!QueryHandler.instanceOf().reset()) {
                            Popup.notice(Messages.ERROR_OCCURED.toString() + "\n"
                                    + Messages.RESTART_REQUIRED.toString());
                            Main.getApplication().exit();
                        }
                    }
                } catch (Exception ex) {
                    Log.Debug(ex);
                } finally {
                    mpv5.YabsViewProxy.instance().getProgressbar().setValue(100);
                    mpv5.YabsViewProxy.instance().getProgressbar().setString(Messages.DONE.toString());
                    mpv5.YabsViewProxy.instance().getProgressbar().setStringPainted(true);
                    mpv5.YabsViewProxy.instance().setWaiting(false);
                }
            }
        };
        new Thread(runnable).start();
    }
}