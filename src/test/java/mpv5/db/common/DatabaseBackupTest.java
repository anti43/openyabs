package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Zip backups: an H2 database is zipped, old backups are deleted, a backup
 * unzips into a folder that opens as a database
 */
class DatabaseBackupTest {

    @TempDir
    Path tmp;

    @Test
    void zipPruneAndUnzipAnH2Database() throws Exception {
        File dbdir = tmp.resolve("db").toFile();
        try (Connection c = DriverManager.getConnection(H2Dialect.url(dbdir.getPath(), "yabs", true), "sa", "");
                Statement s = c.createStatement()) {
            s.execute("CREATE TABLE t (a BIGINT)");
            s.execute("INSERT INTO t VALUES (42)");
        }
        List<File> files = DatabaseBackup.sources(dbdir.getPath(), "yabs", true);
        assertEquals(1, files.size());

        File backups = tmp.resolve("backups").toFile();
        backups.mkdirs();
        for (int i = 1; i <= 5; i++) {
            DatabaseBackup.zip(dbdir, files, new File(backups, "yabs-2026010" + i + "-120000.zip"));
        }
        assertEquals(2, DatabaseBackup.prune(backups, "yabs", 3));
        assertFalse(new File(backups, "yabs-20260101-120000.zip").exists());
        assertTrue(new File(backups, "yabs-20260105-120000.zip").exists());

        DatabaseBackup.Restored r = DatabaseBackup.unzip(new File(backups, "yabs-20260105-120000.zip"));
        assertTrue(r.h2);
        assertEquals("yabs", r.dbname);
        assertEquals("20260105-120000", r.stamp);
        try (Connection c = DriverManager.getConnection(H2Dialect.url(r.folder.getPath(), r.dbname, false), "sa", "");
                Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT a FROM t")) {
            assertTrue(rs.next());
            assertEquals(42, rs.getInt(1));
        }
    }

    @Test
    void resolvesVariables() {
        java.util.Date d = new java.util.GregorianCalendar(2026, 10, 12).getTime();
        File f = DatabaseBackup.resolve("[HOME]/b/[DBNAME]/[YEAR]-[MONTH]-[DAY]", "yabs", d);
        assertEquals(new File(System.getProperty("user.home") + "/b/yabs/2026-11-12").getPath(), f.getPath());
    }
}
