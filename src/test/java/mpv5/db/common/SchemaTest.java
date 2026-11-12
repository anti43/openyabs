package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import mpv5.globals.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The neutral schema file: up to date, and it creates a working database on
 * H2 (always), PostgreSQL and MySQL/MariaDB (when YABS_TEST_POSTGRES /
 * YABS_TEST_MYSQL contain a JDBC URL of an empty database, user and password
 * in the URL).
 */
class SchemaTest {

    @TempDir
    Path tmp;

    @Test
    void fileIsUpToDate() throws Exception {
        String file = Files.readString(Path.of("src/main/resources" + Schema.RESOURCE), StandardCharsets.UTF_8);
        assertEquals(SchemaExport.generate(), file, "src/main/resources/db/schema.sql is out of date: run ./gradlew generateSchema");
        assertEquals(Constants.DATABASE_VERSION, Schema.get().getVersion(), 0.00001);
    }

    @Test
    void h2() throws Exception {
        try (Connection c = DriverManager.getConnection(H2Dialect.url(tmp.toString(), "yabs", true), "sa", "")) {
            check(c, SqlDialect.H2);
        }
    }

    @Test
    void postgres() throws Exception {
        String url = System.getenv("YABS_TEST_POSTGRES");
        assumeTrue(url != null && !url.isEmpty(), "YABS_TEST_POSTGRES not set");
        try (Connection c = DriverManager.getConnection(url)) {
            clean(c, "DROP SCHEMA public CASCADE", "CREATE SCHEMA public");
            check(c, SqlDialect.POSTGRES);
        }
    }

    @Test
    void mysql() throws Exception {
        String url = System.getenv("YABS_TEST_MYSQL");
        assumeTrue(url != null && !url.isEmpty(), "YABS_TEST_MYSQL not set");
        try (Connection c = DriverManager.getConnection(url)) {
            String db = c.getCatalog();
            clean(c, "DROP DATABASE " + db, "CREATE DATABASE " + db + " CHARACTER SET utf8mb4");
            c.setCatalog(db);
            check(c, SqlDialect.MYSQL);
        }
    }

    /**
     * An existing MySQL database (old structure and updates): update 1.1986
     * adds the triggers and fills the search index and the trash bin
     */
    @Test
    void mysqlUpdateAddsTriggers() throws Exception {
        String url = System.getenv("YABS_TEST_MYSQL");
        assumeTrue(url != null && !url.isEmpty(), "YABS_TEST_MYSQL not set");
        try (Connection c = DriverManager.getConnection(url)) {
            String db = c.getCatalog();
            clean(c, "DROP DATABASE " + db, "CREATE DATABASE " + db + " CHARACTER SET utf8mb4");
            c.setCatalog(db);
            new SchemaUpdate(r -> c, false, null).runAll(DatabaseInstallation.MYSQL_STRUCTURE);
            clean(c, "INSERT INTO groups (cname, description, dateadded) VALUES ('Alle', 'all', '2009-04-03')",
                    "INSERT INTO users (fullname, password, cname, laf, locale, mail, language, inthighestright, datelastlog, isenabled, dateadded)"
                    + " VALUES ('Administrator', 'x', 'admin', '', 'de_DE', '', 'buildin_en', 0, '2009-04-03', 1, '2009-04-03')",
                    "INSERT INTO globalsettings (cname, groupsids, value) VALUES ('yabs_dbversion', 1, '1.0')");
            java.util.Map<Double, String[]> all = new DatabaseUpdater().getStructureForMigration(ConnectionTypeHandler.MYSQL);
            java.util.Map<Double, String[]> old = new java.util.TreeMap<Double, String[]>(all);
            old.keySet().removeIf(v -> v > 1.1985);
            assertEquals(1.1985, new SchemaUpdate(r -> c, false, null).run(1.0, old), 0.00001);
            //data of the old database, without triggers
            clean(c, "INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('K1', 'Kunde Eins', '2020-01-01', 1)",
                    "INSERT INTO contacts (cnumber, cname, dateadded, groupsids, invisible) VALUES ('K2', 'Kunde Zwei', '2020-01-01', 1, 1)");
            assertEquals("0", one(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"));

            assertEquals(Constants.DATABASE_VERSION, new SchemaUpdate(r -> c, false, null).run(1.1985, all), 0.00001);
            assertEquals(java.util.List.of(), SchemaUpdate.getWarnings());
            assertEquals("2", one(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"), "search index filled");
            assertEquals("K2 (Kunde Zwei)", one(c, "SELECT description FROM trashbin WHERE cname = 'contacts'"), "trash bin filled");
            //and the triggers work from now on
            clean(c, "INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('K3', 'Kunde Drei', '2020-01-01', 1)");
            assertEquals("3", one(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"));
        }
    }

    private static void clean(Connection c, String... sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            for (String q : sql) {
                s.execute(q);
            }
        }
    }

    private static String one(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    /**
     * Schema, initial rows, the rows of the updates; then the triggers:
     * search index and trash bin
     */
    private static void check(Connection c, SqlDialect d) throws Exception {
        Schema schema = Schema.get();
        try (Statement s = c.createStatement()) {
            for (String q : schema.structureFor(d)) {
                try {
                    s.execute(q);
                } catch (SQLException e) {
                    throw new SQLException(d + ": " + e.getMessage() + "\n" + q, e);
                }
            }
            s.execute("INSERT INTO groups (cname, description, dateadded) VALUES ('Alle', 'all', '2009-04-03 00:00:00')");
            s.execute("INSERT INTO users (fullname, password, cname, laf, locale, mail, language, inthighestright, datelastlog, isenabled, dateadded)"
                    + " VALUES ('Administrator', 'x', 'admin', '', 'de_DE', '', 'buildin_en', 0, '2009-04-03 00:00:00', 1, '2009-04-03 00:00:00')");
            s.execute("INSERT INTO globalsettings (cname, groupsids, value) VALUES ('yabs_dbversion', 1, '1.0')");
            for (String q : schema.dataFor(d)) {
                s.execute(q);
            }
        }
        assertEquals(String.valueOf(Constants.DATABASE_VERSION), one(c, "SELECT value FROM globalsettings WHERE cname = 'yabs_dbversion'"));
        assertEquals("4", one(c, "SELECT COUNT(*) FROM scheduletypes"));
        try (Statement s = c.createStatement()) {
            s.execute("INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('K1', 'Kunde Eins', '2020-01-01 10:00:00', 1)");
            s.execute("UPDATE contacts SET invisible = 1 WHERE cnumber = 'K1'");
        }
        assertEquals("1", one(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"), d + ": search index");
        assertEquals("K1 (Kunde Eins)", one(c, "SELECT description FROM trashbin WHERE cname = 'contacts'"), d + ": trash bin");
        try (Statement s = c.createStatement()) {
            s.execute("DELETE FROM contacts WHERE cnumber = 'K1'");
        }
        assertEquals("0", one(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"), d + ": search index after delete");
    }
}
