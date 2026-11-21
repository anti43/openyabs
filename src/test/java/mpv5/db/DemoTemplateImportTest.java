package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.handler.FirstStartSetup;
import mpv5.handler.TemplateHandler;
import mpv5.usermanagement.MPSecurityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that bundled demo templates always receive their usage type
 * ({@code mimetype}) and user assignment, including when a second database is
 * created while templates from a previous database are cached, and that
 * already-imported broken demo templates are repaired on login.
 */
class DemoTemplateImportTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void creatingSecondDatabaseAssignsUsageTypesToAllDemoTemplates() throws Exception {
        // 1. First database: prepare and cache all templates in DatabaseObject.cache
        User admin1 = createDatabase("db1");
        FirstStartSetup.prepare(admin1, true, null);
        List<DatabaseObject> firstTemplates = DatabaseObject.getObjects(Context.getTemplate());
        assertEquals(FirstStartSetup.DEMO_TEMPLATES.length + 1, firstTemplates.size());
        for (DatabaseObject o : firstTemplates) {
            // populate DatabaseObject.cache with templates@1..19 from db1
            DatabaseObject.getObject(Context.getTemplate(), o.__getIDS());
        }

        // 2. Second database (like ConnectionManager -> FirstStart): restart and prepare
        User admin2 = createDatabase("db2");
        FirstStartSetup.prepare(admin2, true, null);

        List<DatabaseObject> secondTemplates = DatabaseObject.getObjects(Context.getTemplate());
        assertEquals(FirstStartSetup.DEMO_TEMPLATES.length + 1, secondTemplates.size());

        Map<String, Template> byName = new HashMap<>();
        for (DatabaseObject o : secondTemplates) {
            Template t = (Template) o;
            byName.put(t.__getCname(), t);
            assertNotEquals("odt", t.__getMimetype(), "usage type must be numeric, not 'odt': " + t.__getCname());
            assertEquals("Wizard insert", t.__getDescription(), t.__getCname());
            assertEquals(1, t.__getGroupsids(), t.__getCname());
        }

        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
        for (Object[] d : FirstStartSetup.DEMO_TEMPLATES) {
            int type = (Integer) d[0];
            String name = "Demo - " + d[1];
            Template t = byName.get(name);
            assertNotNull(t, "Missing demo template: " + name);
            assertEquals(String.valueOf(type), t.__getMimetype(), name);
            assertEquals(FirstStartSetup.demoFormat(type), t.__getFormat(), name);
            assertEquals(1, assignedCount(admin2, t.__getIDS()), "Assigned to admin: " + name);
            assertNotNull(TemplateHandler.loadTemplate(1, type), "TemplateHandler loads type " + type);
        }

        Template ku = byName.get(FirstStartSetup.SMALL_BUSINESS_INVOICE_NAME);
        assertNotNull(ku);
        assertEquals(String.valueOf(Constants.TYPE_INVOICE), ku.__getMimetype());
        assertEquals(FirstStartSetup.POSITIONS_FORMAT, ku.__getFormat());
        assertEquals(0, assignedCount(admin2, ku.__getIDS()), "Small business invoice not assigned for regular setup");
    }

    @Test
    void upgradeRepairsDemoTemplatesLeftWithOdtMimetype() throws Exception {
        User admin = createDatabase("db_broken");
        FirstStartSetup.prepare(admin, true, null);

        // Simulate the broken state left when the UPDATE after backgroundFileInsert failed
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
            Template t = (Template) o;
            String brokenName = t.__getCname() + ".odt";
            QueryHandler.getConnection().freeUpdateQuery(
                    "UPDATE templates SET cname = '" + brokenName.replace("'", "''")
                    + "', description = '" + brokenName.replace("'", "''")
                    + "', mimetype = 'odt', format = NULL WHERE ids = " + t.__getIDS(),
                    MPSecurityManager.CREATE_OR_DELETE, null);
        }
        // Also remove templatestousers for Offer to verify missing assignment is restored
        Template offerBefore = null;
        DatabaseObject.clearCache();
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
            if ("Demo - Angebot.odt".equals(o.__getCname())) {
                offerBefore = (Template) o;
            }
        }
        assertNotNull(offerBefore);
        QueryHandler.getConnection().freeUpdateQuery(
                "DELETE FROM templatestousers WHERE templatesids = " + offerBefore.__getIDS(),
                MPSecurityManager.CREATE_OR_DELETE, null);
        TemplateHandler.clearCache();

        // Repair runs as part of upgradeDemoTemplates() on login
        FirstStartSetup.upgradeDemoTemplates();
        DatabaseObject.clearCache();

        Map<String, Template> byName = new HashMap<>();
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
            Template t = (Template) o;
            byName.put(t.__getCname(), t);
        }

        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
        for (Object[] d : FirstStartSetup.DEMO_TEMPLATES) {
            int type = (Integer) d[0];
            String name = "Demo - " + d[1];
            Template t = byName.get(name);
            assertNotNull(t, "Repaired demo template missing: " + name);
            assertEquals(String.valueOf(type), t.__getMimetype(), name);
            assertEquals(FirstStartSetup.demoFormat(type), t.__getFormat(), name);
            assertEquals("Wizard insert", t.__getDescription(), name);
            assertEquals(1, assignedCount(admin, t.__getIDS()), "Assigned after repair: " + name);
            assertNotNull(TemplateHandler.loadTemplate(1, type), "TemplateHandler loads repaired type " + type);
        }

        Template ku = byName.get(FirstStartSetup.SMALL_BUSINESS_INVOICE_NAME);
        assertNotNull(ku);
        assertEquals(String.valueOf(Constants.TYPE_INVOICE), ku.__getMimetype());
        assertEquals(FirstStartSetup.POSITIONS_FORMAT, ku.__getFormat());
        assertEquals(0, assignedCount(admin, ku.__getIDS()), "Small business invoice remains unassigned");
    }

    @Test
    void repairDoesNotDoubleAssignInvoiceWhenWorkingInvoiceAlreadyExists() throws Exception {
        User admin = createDatabase("db_dup");
        FirstStartSetup.prepare(admin, true, null);

        // Corrupt the original Demo - Rechnung to mimetype = 'odt' (still linked in templatestousers)
        Template brokenInvoice = null;
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
            if ("Demo - Rechnung".equals(o.__getCname())) {
                brokenInvoice = (Template) o;
            }
        }
        assertNotNull(brokenInvoice);
        QueryHandler.getConnection().freeUpdateQuery(
                "UPDATE templates SET cname = 'Demo - Rechnung.odt', description = 'Demo - Rechnung.odt', mimetype = 'odt', format = NULL WHERE ids = "
                + brokenInvoice.__getIDS(),
                MPSecurityManager.CREATE_OR_DELETE, null);
        DatabaseObject.clearCache();

        // Simulate an earlier login where ensureInvoiceTemplate imported a second working invoice template
        Template secondInvoice = FirstStartSetup.importTemplate(admin, FirstStartSetup.DEMO_INVOICE_TEMPLATE,
                "Demo - Rechnung", FirstStartSetup.ITEM_FORMAT, Constants.TYPE_INVOICE, 60000, true);
        assertNotNull(secondInvoice);
        assertEquals(1, assignedCount(admin, brokenInvoice.__getIDS()));
        assertEquals(1, assignedCount(admin, secondInvoice.__getIDS()));

        // Repair fixes the broken template's usage type and unlinks the duplicate assignment
        FirstStartSetup.upgradeDemoTemplates();
        DatabaseObject.clearCache();

        Template repaired = (Template) DatabaseObject.getObject(Context.getTemplate(), brokenInvoice.__getIDS());
        assertEquals(String.valueOf(Constants.TYPE_INVOICE), repaired.__getMimetype());
        assertEquals("Demo - Rechnung", repaired.__getCname());
        assertEquals(0, assignedCount(admin, brokenInvoice.__getIDS()), "Duplicate assignment removed");
        assertEquals(1, assignedCount(admin, secondInvoice.__getIDS()), "Existing working invoice stays assigned");
    }

    private User createDatabase(String name) throws Exception {
        File dir = tmp.resolve(name).toFile();
        dir.mkdirs();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = new File(dir, "h2").getPath();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.setProperty(LocalSettings.OFFICE_USE, "false");
        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
        LocalSettings.save(0);
        DatabaseConnection.shutdown();
        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.restart();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        return admin;
    }

    private static int assignedCount(User user, int templateId) {
        Object[][] data = QueryHandler.getConnection().freeQuery(
                "SELECT COUNT(*) FROM templatestousers WHERE usersids = " + user.__getIDS()
                + " AND templatesids = " + templateId,
                MPSecurityManager.VIEW, null).getData();
        return Integer.parseInt(String.valueOf(data[0][0]));
    }
}
