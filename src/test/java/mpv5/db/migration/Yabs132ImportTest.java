package mpv5.db.migration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.H2Dialect;
import mpv5.db.common.Schema;
import mpv5.db.common.SchemaUpdate;
import mpv5.db.common.SqlDialect;
import mpv5.globals.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Import of a YaBS 1.3.2 Derby database ({@link Yabs132Fixture}) into a new
 * H2 database: every table, the old escaping, ids, files, the old database
 * unchanged
 */
class Yabs132ImportTest {

    @TempDir
    Path tmp;

    /**
     * A new H2 database as the setup creates it: structure, initial data,
     * plus a template (with file and assignment) and countries as the setup
     * wizard imports them
     */
    private static Connection newH2(Path dir) throws Exception {
        Connection c = DriverManager.getConnection(H2Dialect.url(dir.toString(), "yabs", true), "sa", "");
        SchemaUpdate u = new SchemaUpdate(r -> c, true, null);
        u.runAll(Schema.get().structureFor(SqlDialect.H2));
        u.runAll(DatabaseInstallation.INITIAL_DATA);
        u.runAll(Schema.get().dataFor(SqlDialect.H2));
        try (Statement s = c.createStatement()) {
            s.execute("INSERT INTO files (cname, groupsids, dateadded, data, filesize) VALUES ('newtpl00000001', 1, CURRENT_TIMESTAMP, X'0102', 2)");
            s.execute("INSERT INTO templates (cname, format, description, intsize, mimetype, filename, dateadded, groupsids) "
                    + "VALUES ('Rechnung neu.odt', '1,2', 'Demo', 2, '0', 'newtpl00000001', CURRENT_TIMESTAMP, 1)");
            s.execute("INSERT INTO templatestousers (cname, usersids, templatesids, groupsids) VALUES ('1@1@1', 1, 1, 1)");
            s.execute("INSERT INTO countries (cname, iso, groupsids) VALUES ('Frankreich', 250, 1)");
            s.execute("INSERT INTO globalsettings (cname, groupsids, value) VALUES ('newsetting', 1, 'x')");
            s.execute("INSERT INTO globalsettings (cname, groupsids, value) VALUES ('mailsignature', 1, 'new')");
        }
        return c;
    }

    private static Map<String, String> digest(Path dir) throws Exception {
        Map<String, String> m = new TreeMap<String, String>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                byte[] d = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(p));
                m.put(dir.relativize(p).toString(), java.util.HexFormat.of().formatHex(d) + "/" + Files.getLastModifiedTime(p));
            }
        }
        return m;
    }

    private static String one(Connection c, String sql, Object... params) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), sql);
                return rs.getString(1);
            }
        }
    }

    private static byte[] bytes(Connection c, String sql) throws Exception {
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            assertTrue(rs.next(), sql);
            return rs.getBytes(1);
        }
    }

    private static long count(Connection c, String table) throws Exception {
        return Long.parseLong(one(c, "SELECT COUNT(*) FROM " + table));
    }

    @Test
    void importsEverythingOfYabs132() throws Exception {
        File old = tmp.resolve("old").resolve("yabs").toFile();
        Yabs132Fixture.create(old, "yabs", Yabs132Import.VERSION_132);
        Map<String, Integer> rows = new HashMap<String, Integer>(Yabs132Fixture.ROWS);
        Map<String, String> before = digest(old.toPath());

        try (Connection h2 = newH2(tmp.resolve("h2"))) {
            List<Integer> percent = new ArrayList<Integer>();
            Yabs132Import imp = new Yabs132Import();
            imp.setProgress((m, p) -> percent.add(p));
            Yabs132Import.Result r = imp.importInto(old, h2);
            String summary = r.getSummary();
            System.out.println(summary);
            assertEquals("YABS", r.schema);
            assertEquals(Yabs132Import.VERSION_132, r.oldVersion, 0.00001);
            assertTrue(r.problems.isEmpty(), "nothing lost: " + r.problems);
            assertEquals(100, percent.get(percent.size() - 1));

            //every table with all its rows (the lock is not data, the search index is rebuilt)
            for (Map.Entry<String, Integer> e : rows.entrySet()) {
                String t = e.getKey();
                Yabs132Import.TableResult tr = r.tables.get(t);
                assertNotNull(tr, t);
                assertEquals(e.getValue().longValue(), tr.oldRows, t);
                if (t.equals("TABLELOCK") || t.equals("SEARCHINDEX")) {
                    continue;
                }
                assertEquals(tr.oldRows, tr.imported, t);
                long expected = e.getValue();
                if (t.equals("COUNTRIES")) {
                    expected += 1;//Frankreich of the new database
                } else if (t.equals("GLOBALSETTINGS")) {
                    expected += 1;//newsetting
                } else if (t.equals("TEMPLATES") || t.equals("FILES")) {
                    expected += 1;//the template of the new database
                }
                assertEquals(expected, count(h2, t), t);
            }
            assertEquals(0, count(h2, "tablelock"));
            assertEquals(52, r.tables.size(), "all tables of 1.3.2: " + r.tables.keySet());

            //texts as typed: ' and \ back
            assertEquals(Yabs132Fixture.NAME, one(h2, "SELECT cname FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals(Yabs132Fixture.STREET, one(h2, "SELECT street FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals(Yabs132Fixture.NOTES, one(h2, "SELECT notes FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals("+49 221 123'456", one(h2, "SELECT mainphone FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals("https://example.org/a\\b", one(h2, "SELECT website FROM contacts WHERE cnumber = 'K-00001'"));
            assertNull(one(h2, "SELECT street FROM contacts WHERE cnumber = 'L-00001'"));
            assertEquals(Yabs132Fixture.POSITION, one(h2, "SELECT cname FROM subitems WHERE ordernr = 1 AND itemsids = (SELECT ids FROM items WHERE cnumber = 'RE-00001')"));
            assertEquals(Yabs132Fixture.RTF, one(h2, "SELECT content FROM conversations"));
            assertEquals(Yabs132Fixture.MAIL, one(h2, "SELECT description FROM mails"));
            assertEquals(Yabs132Fixture.PATH, one(h2, "SELECT value FROM userproperties WHERE cname = 'defaultsavedir'"));
            assertEquals(Yabs132Fixture.PATH + "\\Rechnung.odt", one(h2, "SELECT pathtofile FROM templates WHERE filename = 'tplrechnung001'"));
            assertEquals("Grüße, O'Brien \\ Team", one(h2, "SELECT value FROM globalsettings WHERE cname = 'mailsignature'"));
            assertEquals("Select * from contacts where city = 'Köln'", one(h2, "SELECT content FROM massprintrules WHERE cname = 'Kunden in Köln'"));
            assertEquals(0, Long.parseLong(one(h2, "SELECT COUNT(*) FROM contacts WHERE cname LIKE '%removedbackslash%' OR notes LIKE '%removedbackslash%'")));
            assertTrue(r.backslashes > 0 && r.backticks > 0);
            //number formats and numbers keep their `
            assertEquals(Yabs132Fixture.OFFER_FORMAT, one(h2, "SELECT cname FROM formatstousers WHERE inttype = 1"));
            assertEquals("1", one(h2, "SELECT COUNT(*) FROM items WHERE cnumber = 'AN`001'"));

            //ids, references, numbers and dates as they were (contact 2 was deleted in the old database)
            assertEquals("1", one(h2, "SELECT ids FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals("3", one(h2, "SELECT ids FROM contacts WHERE cnumber = 'L-00001'"));
            assertEquals("K-00001", one(h2, "SELECT c.cnumber FROM items i JOIN contacts c ON c.ids = i.contactsids WHERE i.cnumber = 'RE-00001'"));
            assertEquals("2014-07-01", one(h2, "SELECT CAST(dateadded AS DATE) FROM contacts WHERE cnumber = 'K-00001'"));
            assertEquals("2018-10-04", one(h2, "SELECT CAST(dateend AS DATE) FROM items WHERE cnumber = 'RE-00001'"));
            assertEquals("1", one(h2, "SELECT invisible FROM contacts WHERE cnumber = 'H-00001'"));
            assertEquals(one(h2, "SELECT ids FROM items WHERE cnumber = 'AN`001'"), one(h2, "SELECT reforderids FROM items WHERE cnumber = 'AU-00001'"));
            assertEquals("Projekt \\ Phase 1", one(h2, "SELECT description FROM items WHERE cnumber = 'RE-00001'"));
            assertEquals("241.0", one(h2, "SELECT netvalue FROM items WHERE cnumber = 'RE-00001'"));
            assertEquals("Beratung 'Premium'", one(h2, "SELECT p.cname FROM subitems s JOIN products p ON p.ids = s.originalproductsids WHERE s.cname = ?", Yabs132Fixture.POSITION));
            //users with passwords and rights, user settings
            assertEquals("e10adc3949ba59abbe56e057f20f883e", one(h2, "SELECT password FROM users WHERE cname = 'hans'"));
            assertEquals("2", one(h2, "SELECT inthighestright FROM users WHERE cname = 'hans'"));
            assertEquals("Hans O'Brien", one(h2, "SELECT fullname FROM users WHERE cname = 'hans'"));
            assertEquals("Vertrieb 'Süd'", one(h2, "SELECT g.cname FROM users u JOIN groups g ON g.ids = u.groupsids WHERE u.cname = 'hans'"));
            assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", one(h2, "SELECT password FROM users WHERE ids = 1"));
            //files and other binary data unchanged
            assertArrayEquals(Yabs132Fixture.TEMPLATE_FILE, bytes(h2, "SELECT data FROM files WHERE cname = 'tplrechnung001'"));
            assertArrayEquals(Yabs132Fixture.ATTACHMENT, bytes(h2, "SELECT data FROM files WHERE cname = 'att0000000001'"));
            assertArrayEquals(Yabs132Fixture.VALUE_BLOB, bytes(h2, "SELECT value FROM valueproperties"));
            assertArrayEquals(Yabs132Fixture.ATTACHMENT, bytes(h2, "SELECT font FROM fontsforitext"));
            //renamed column: conversations.date is timestamp
            assertEquals("2018-09-02", one(h2, "SELECT CAST(timestamp AS DATE) FROM conversations"));

            //kept of the new database: version, its template (not assigned: the old database has assigned ones), Frankreich, newsetting
            assertEquals(String.valueOf(Constants.DATABASE_VERSION), one(h2, "SELECT value FROM globalsettings WHERE cname = 'yabs_dbversion'"));
            assertEquals("x", one(h2, "SELECT value FROM globalsettings WHERE cname = 'newsetting'"));
            assertEquals("Frankreich", one(h2, "SELECT cname FROM countries WHERE iso = 250"));
            assertEquals("Rechnung neu.odt", one(h2, "SELECT t.cname FROM templates t JOIN files f ON f.cname = t.filename WHERE f.cname = 'newtpl00000001'"));
            assertEquals("2", one(h2, "SELECT COUNT(*) FROM templatestousers"));
            assertEquals("Rechnung O'Brien.odt", one(h2, "SELECT t.cname FROM templatestousers a JOIN templates t ON t.ids = a.templatesids WHERE a.usersids = 1"));

            //search index rebuilt from the imported texts
            assertTrue(r.searchIndex > 0);
            assertEquals("3", one(h2, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'contacts'"));
            assertEquals(String.valueOf(r.searchIndex), one(h2, "SELECT COUNT(*) FROM searchindex"));
            assertTrue(Long.parseLong(one(h2, "SELECT COUNT(*) FROM searchindex WHERE dbidentity = 'products' AND text LIKE '%Beratung ''Premium''%'")) > 0);
            //new rows get ids after the imported ones
            long maxContact = Long.parseLong(one(h2, "SELECT MAX(ids) FROM contacts"));
            try (Statement s = h2.createStatement()) {
                s.execute("INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('K-NEU', 'Neu', CURRENT_TIMESTAMP, 1)");
            }
            assertEquals(String.valueOf(maxContact + 1), one(h2, "SELECT ids FROM contacts WHERE cnumber = 'K-NEU'"));
            //foreign keys checked again
            assertThrows(java.sql.SQLException.class, () -> {
                try (Statement s = h2.createStatement()) {
                    s.execute("INSERT INTO subitems (cname, itemsids, groupsids, dateadded) VALUES ('x', 99999, 1, CURRENT_TIMESTAMP)");
                }
            });
        }
        //the old database is unchanged
        assertEquals(before, digest(old.toPath()));
    }

    @Test
    void backticksCanStay() throws Exception {
        File old = tmp.resolve("old").resolve("yabs").toFile();
        Yabs132Fixture.create(old, null, Yabs132Import.VERSION_132);
        try (Connection h2 = newH2(tmp.resolve("h2"))) {
            Yabs132Import imp = new Yabs132Import();
            imp.setConvertBackticks(false);
            Yabs132Import.Result r = imp.importInto(old, h2);
            assertEquals("APP", r.schema);
            assertEquals(0, r.backticks);
            assertEquals(Yabs132Fixture.NAME.replace('\'', '`'), one(h2, "SELECT cname FROM contacts WHERE cnumber = 'K-00001'"));
        }
    }

    @Test
    void olderDatabaseIsUpdatedOnTheCopy() throws Exception {
        //1.1981: revenues still have ispaid (paid = 1), the update 1.1982 makes it status 4
        File old = tmp.resolve("old").resolve("yabs").toFile();
        Yabs132Fixture.create(old, "yabs", 1.1981);
        Map<String, String> before = digest(old.toPath());
        try (Connection h2 = newH2(tmp.resolve("h2"))) {
            Yabs132Import.Result r = new Yabs132Import().importInto(old, h2);
            assertEquals(1.1981, r.oldVersion, 0.00001);
            assertTrue(r.problems.isEmpty(), r.getSummary());
            assertEquals("4", one(h2, "SELECT status FROM revenues WHERE cnumber = 'E-0001'"));
            assertEquals(Yabs132Fixture.NAME, one(h2, "SELECT cname FROM contacts WHERE cnumber = 'K-00001'"));
        }
        assertEquals(before, digest(old.toPath()));
    }

    @Test
    void targetWithDataIsRefusedAndUnchanged() throws Exception {
        File old = tmp.resolve("old").resolve("yabs").toFile();
        Yabs132Fixture.create(old, "yabs", Yabs132Import.VERSION_132);
        try (Connection h2 = newH2(tmp.resolve("h2"))) {
            try (Statement s = h2.createStatement()) {
                s.execute("INSERT INTO contacts (cnumber, cname, dateadded, groupsids) VALUES ('X1', 'Vorhanden', CURRENT_TIMESTAMP, 1)");
            }
            Yabs132Import.ImportException e = assertThrows(Yabs132Import.ImportException.class, () -> new Yabs132Import().importInto(old, h2));
            assertTrue(e.getMessage().contains("contacts: 1"), e.getMessage());
            assertEquals("1", one(h2, "SELECT COUNT(*) FROM contacts"));
            assertEquals("admin", one(h2, "SELECT cname FROM users WHERE ids = 1"));
        }
    }

    @Test
    void notADatabase() throws Exception {
        try (Connection h2 = newH2(tmp.resolve("h2"))) {
            assertThrows(Yabs132Import.ImportException.class, () -> new Yabs132Import().importInto(tmp.toFile(), h2));
        }
        //a folder with databases: found
        File old = tmp.resolve("yabsdir").resolve("mydb").toFile();
        Yabs132Fixture.create(old, null, Yabs132Import.VERSION_132);
        assertEquals(List.of(old), Yabs132Import.findDatabases(tmp.resolve("yabsdir").toFile()));
    }
}
