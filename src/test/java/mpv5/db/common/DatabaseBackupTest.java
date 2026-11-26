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

    @Test
    void zipReportsMonotonicByteProgress() throws Exception {
        File dbdir = tmp.resolve("dbprog").toFile();
        dbdir.mkdirs();
        File data = new File(dbdir, "yabs.mv.db");
        byte[] payload = new byte[600 * 1024];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i * 31);
        }
        java.nio.file.Files.write(data.toPath(), payload);

        java.util.List<long[]> updates = new java.util.ArrayList<long[]>();
        File zip = tmp.resolve("progress.zip").toFile();
        DatabaseBackup.zip(dbdir, java.util.Collections.singletonList(data), zip,
                (written, total) -> updates.add(new long[]{written, total}));

        assertTrue(zip.isFile() && zip.length() > 0);
        assertTrue(updates.size() >= 3, "Expected start, intermediate, and final progress updates, got " + updates.size());
        assertEquals(0L, updates.get(0)[0]);
        assertEquals(payload.length, updates.get(0)[1]);
        long prev = -1L;
        for (long[] u : updates) {
            assertTrue(u[0] >= prev, "Progress must be monotonic");
            assertTrue(u[0] <= u[1], "Written bytes must not exceed total");
            assertEquals(payload.length, u[1]);
            prev = u[0];
        }
        assertEquals(payload.length, updates.get(updates.size() - 1)[0]);

        assertEquals("512 B", DatabaseBackup.formatBytes(512));
        assertTrue(DatabaseBackup.formatBytes(2048).endsWith("KB"));
        assertTrue(DatabaseBackup.formatBytes(5L * 1024 * 1024).endsWith("MB"));
        assertTrue(DatabaseBackup.formatBytes(2L * 1024 * 1024 * 1024).endsWith("GB"));
    }

    @Test
    void shutdownDialogRunsSynchronouslyOffEdt() {
        java.util.concurrent.atomic.AtomicBoolean ran = new java.util.concurrent.atomic.AtomicBoolean(false);
        mpv5.ui.dialogs.ShutdownDialog.runWithProgress(null, p -> {
            p.step(10, "step 1");
            p.indeterminate("working", "detail");
            p.step(100, "done", "");
            ran.set(true);
        });
        assertTrue(ran.get(), "Shutdown task must execute synchronously when called off-EDT / headless");
    }

    @Test
    void shutdownDialogPumpsEdtAndClosesCleanlyWhenWorkerFinishes() throws Exception {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        java.util.concurrent.atomic.AtomicBoolean ranOnWorker = new java.util.concurrent.atomic.AtomicBoolean(false);
        javax.swing.SwingUtilities.invokeAndWait(() -> mpv5.ui.dialogs.ShutdownDialog.runWithProgress(null, p -> {
            ranOnWorker.set(!javax.swing.SwingUtilities.isEventDispatchThread());
            p.step(20, "Saving settings...");
            try {
                Thread.sleep(40);
            } catch (InterruptedException ignored) {
            }
            p.step(75, "Backing up...", "C:\\temp\\backups");
            p.step(100, "Done.");
        }));
        assertTrue(ranOnWorker.get(), "Shutdown work must run off the EDT so the closing popup repaints smoothly");
    }
}
