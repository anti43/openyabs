package mpv5.db.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import mpv5.globals.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The Derby structure and all updates, converted for H2
 */
class H2SchemaTest {

    @TempDir
    Path tmp;

    private static String query(Connection c, String sql) throws Exception {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    @Test
    void newH2DatabaseIsUpdatedToCurrentVersion() throws Exception {
        try (Connection c = DriverManager.getConnection(H2Dialect.url(tmp.toString(), "yabs", true), "sa", "")) {
            new SchemaUpdate(r -> c, true, null).runAll(H2Dialect.convert(DatabaseInstallation.DERBY_STRUCTURE));
            try (Statement s = c.createStatement()) {
                s.execute("INSERT INTO globalsettings (cname, groupsids, value) VALUES ('yabs_dbversion', 1, '1.0')");
                s.execute("INSERT INTO groups (cname, description, dateadded) VALUES ('Alle', 'all', '2009-04-03')");
                s.execute("INSERT INTO users (fullname, password, cname, laf, locale, mail, language, inthighestright, datelastlog, isenabled, dateadded)"
                        + " VALUES ('Administrator', 'x', 'admin', '', 'de_DE', '', 'buildin_en', 0, '2009-04-03', 1, '2009-04-03')");
            }
            double v = new SchemaUpdate(r -> c, true, null).run(1.0,
                    H2Dialect.convert(new DatabaseUpdater().getStructureForMigration(ConnectionTypeHandler.DERBY)));
            assertEquals(Constants.DATABASE_VERSION, v, 0.00001);
            assertEquals("0", query(c, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE DATA_TYPE = 'SMALLINT' AND TABLE_SCHEMA = 'PUBLIC'"));
            //the emulated triggers fill the search index and the trash bin
            try (Statement s = c.createStatement()) {
                s.execute("INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('K1', 'Kunde Eins', '2020-01-01', 1)");
                s.execute("UPDATE contacts SET invisible = 1 WHERE cnumber = 'K1'");
                s.execute("INSERT INTO activitylists (cname, cnumber, dateadded) VALUES ('a', 'A1', '2020-01-01')");
            }
            assertEquals("1", query(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"));
            assertEquals("K1 (Kunde Eins)", query(c, "SELECT description FROM trashbin WHERE cname = 'contacts'"));
            try (Statement s = c.createStatement()) {
                s.execute("DELETE FROM contacts WHERE cnumber = 'K1'");
            }
            assertEquals("0", query(c, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"));
        }
    }
}
