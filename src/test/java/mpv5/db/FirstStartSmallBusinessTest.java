package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.FirstStartSetup;
import mpv5.utils.export.SmallBusiness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * First start of a small business (Kleinunternehmer): the company info is
 * saved, the small business invoice is the assigned invoice template, the
 * regular demo invoice is there but not assigned.
 */
class FirstStartSmallBusinessTest {

    @TempDir
    Path tmp;

    @Test
    void smallBusinessGetsTheInvoiceWithoutVat() throws Exception {
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.save(0);
        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.instanceOf();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);

        Map<String, String> company = new LinkedHashMap<String, String>();
        company.put("name", "Erika Muster");
        company.put("street", "Hauptstraße 1");
        company.put("zip", "10115");
        company.put("city", "Berlin");
        company.put("country", "Germany");
        company.put("taxnumber", "30/123/45678");
        company.put("account1", "");
        company.put(SmallBusiness.KEY_ENABLED, "true");
        FirstStartSetup.prepare(admin, true, null, company);

        //saved (and read back from the database)
        User again = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        assertEquals("30/123/45678", again.getProperty("companyinfo.taxnumber"));
        assertEquals("true", again.getProperty("companyinfo." + SmallBusiness.KEY_ENABLED));
        assertTrue(FirstStartSetup.isSmallBusiness(again));
        assertTrue(SmallBusiness.isEnabled());

        assertEquals(FirstStartSetup.DEMO_TEMPLATES.length + 1, DatabaseObject.getObjects(Context.getTemplate()).size());
        assertEquals(1, assigned(admin, FirstStartSetup.SMALL_BUSINESS_INVOICE_NAME), "the small business invoice is the invoice template");
        assertEquals(0, assigned(admin, "Demo - Rechnung"), "the regular invoice is there, but not assigned");
        assertEquals(1, assigned(admin, "Demo - Angebot"), "the other templates are assigned as before");
    }

    private static int assigned(User admin, String template) throws Exception {
        return Integer.parseInt(String.valueOf(QueryHandler.getConnection().freeQuery(
                "SELECT COUNT(*) FROM templatestousers a JOIN templates t ON t.ids = a.templatesids WHERE a.usersids = "
                + admin.__getIDS() + " AND t.cname = '" + template + "'",
                mpv5.usermanagement.MPSecurityManager.VIEW, null).getData()[0][0]));
    }
}
