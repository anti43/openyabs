/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.migration;

import java.io.File;
import java.sql.SQLException;
import javax.swing.JTextField;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.usermanagement.MPSecurityManager;

/**
 *
 * @author Jan Hahnisch
 */
public class MigrationWB {

    private static MigrationWB me;
    private final DialogForFile df;

    private MigrationWB() {
        df = new DialogForFile(DialogForFile.DIRECTORIES_ONLY);
        df.setDialogTitle(Messages.SAVE_BACKUP.toString());
        if (LocalSettings.getProperty(LocalSettings.BACKUP_DIR) == null) {
            df.setCurrentDirectory(new File("."));
        } else {
            df.setCurrentDirectory(new File(LocalSettings.getProperty(LocalSettings.BACKUP_DIR)));
        }
    }

    public static synchronized MigrationWB instanceOf() {
        if (me == null) {
            me = new MigrationWB();
        }
        return me;
    }

    public void doExportToMySQL() {
        if (MPSecurityManager.checkAdminAccess()) {
            String txt;
            try {
                JTextField tf = new JTextField();
                df.setDialogType(DialogForFile.SAVE_DIALOG);
                if (df.getFilePath(tf)) {
                    txt = tf.getText();
                    mpv5.YabsViewProxy.instance().setWaiting(true);
                    new DatabaseToSQL(txt, false).ExportToDump();
                    mpv5.YabsViewProxy.instance().setWaiting(false);
                }
            } catch (Exception ex) {
                Log.Debug(this, ex.getLocalizedMessage());
            }
        } else {
            Popup.OK_dialog(Messages.ADMIN_ACCESS.toString(), Messages.NOTICE.toString());
        }
    }

    public void doExportToDerby() {
        if (MPSecurityManager.checkAdminAccess()) {
            String txt;
            try {
                JTextField tf = new JTextField();
                df.setDialogType(DialogForFile.SAVE_DIALOG);
                if (df.getFilePath(tf)) {
                    txt = tf.getText();
                    new DatabaseToFiles(txt, false).performFiles();
                }
            } catch (SQLException ex) {
                Log.Debug(ex);
            }
        } else {
            Popup.OK_dialog(Messages.ADMIN_ACCESS.toString(), Messages.NOTICE.toString());
        }
    }

    public void doImport() {
        boolean proceed = Popup.Y_N_dialog(Messages.OVERRITE_DATABASE.toString(), Messages.Alert.toString());
        if (proceed && MPSecurityManager.checkAdminAccess()) {
            String txt;
            JTextField tf;
            df.setDialogTitle(Messages.OPEN_BACKUP.toString());
            df.setDialogType(DialogForFile.OPEN_DIALOG);
            try {
                switch (ConnectionTypeHandler.getDriverType()) {
                    case ConnectionTypeHandler.DERBY:
                        tf = new JTextField();
                        if (df.getFilePath(tf)) {
                            txt = tf.getText();
                            new DatabaseToFiles(txt, true).restoreFromBackup();
                        }
                        break;
                    case ConnectionTypeHandler.MYSQL:
                        tf = new JTextField();
                        df.setFileSelectionMode(DialogForFile.FILES_ONLY);
                        if (df.getFilePath(tf)) {
                            txt = tf.getText();
                            new DatabaseToSQL(txt, true).restoreFromDump();
                        }
                        break;
                    default:
//                        Implement code for custom DB here!!!
                        break;
                }
            } catch (Exception ex) {
                Log.Debug(this, ex.getLocalizedMessage());
            }
        } else if (proceed) {
            Popup.OK_dialog(Messages.ADMIN_ACCESS.toString(), Messages.NOTICE.toString());
        }
    }

    public void doRestore(String URL, String dump, String db, DatabaseConnection conn, WizardMaster master) {
        try {
            if (master.getStore().getProperty("driver").contains("derby")
                    && !db.equals("")) {
                new DatabaseToFiles(URL, true).createFromBackup(db, conn, master);
            }else if (master.getStore().getProperty("driver").contains("mysql")
                    && !dump.equals("")) {
                new DatabaseToSQL(URL, true).createFromDump(dump, conn, master);
            }
        } catch (Exception ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
    }
}