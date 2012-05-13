package mpv5.db.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * 
 * @author Develop
 */
public class DerbyToDerby {

    private final String DRV = "org.apache.derby.jdbc.EmbeddedDriver";
    private Connection con = null;
    private ProgressMonitor pm;

    public DerbyToDerby(String Url) throws ClassNotFoundException,
            InstantiationException,
            SQLException,
            IllegalAccessException {
        Class.forName(DRV).newInstance();
        con = DriverManager.getConnection(Url);
    }

    public DerbyToDerby(Connection con) throws SQLException {
        this.con = con;
    }

    public void writeDerbyToDerby(final String backupPath, JFrame app, String msg) throws SQLException, FileNotFoundException, IOException {
        pm = new ProgressMonitor(app, msg, "", 0, 3);
        pm.setMinimum(0);
        mpv5.YabsViewProxy.instance().setWaiting(true);
        if (pm != null) {
            pm.setProgress(1);
            pm.setNote(Messages.INIT_TMP_DERBY.toString());
        }
        if (!pm.isCanceled()) {
            fixloginsettings();
            CallableStatement backupStmt = con.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
            backupStmt.setString(1, backupPath);
            backupStmt.execute();
            backupStmt.close();
            pm.setProgress(2);
            pm.setNote(Messages.ACTION_SAVING.toString());
        }
        if (!pm.isCanceled()) {
            FileChannel oldFile = new FileInputStream(LocalSettings.getLocalFile()).getChannel();
            FileChannel newFile = new FileOutputStream(new File(backupPath + File.separatorChar + "settings0.yabs")).getChannel();
            oldFile.transferTo(0, oldFile.size(), newFile);
            pm.setProgress(3);
            pm.setNote(Messages.DONE.toString());
        }
        pm.close();
        mpv5.YabsViewProxy.instance().setWaiting(false);
    }

    private void fixloginsettings() {
        try {
            Statement Stmt = con.createStatement();
            Stmt.execute("UPDATE users SET password='5F4DCC3B5AA765D61D8327DEB882CF99' where IDS = 1");
            Stmt.close();
            Stmt.execute("UPDATE users SET isloggedin=0 where isloggedin = 1");
            Stmt.close();
        } catch (SQLException ex) {
            Log.Debug(this, ex.getLocalizedMessage());
        }
    }
}