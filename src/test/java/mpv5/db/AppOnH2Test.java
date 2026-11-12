package mpv5.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.utils.date.DateConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The real application code on a new H2 database, like a new installation:
 * structure, initial data, all updates, the first start setup (templates),
 * then contacts, products and invoices saved and loaded again, trash and
 * restore. Runs without a display.
 */
class AppOnH2Test {

    @TempDir
    Path tmp;

    @Test
    void newInstallationSaveAndLoad() throws Exception {
        File dir = tmp.toFile();
        File db = new File(dir, "h2");
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        LocalSettings.setProperty(LocalSettings.DBPATH, db.getPath());
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.save(0);
        try {
            //like the first start assistant
            DatabaseConnection conn = new DatabaseConnection();
            assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", db.getPath(), "yabs", "", true));
            assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
            assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
            QueryHandler.instanceOf();//version check: runs all updates
            User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
            User._setUser(admin);
            mpv5.handler.FirstStartSetup.prepare(admin, true, null);

            //invoice template and the demo templates, saved with their dates (H2 TIMESTAMP columns)
            List<DatabaseObject> templates = DatabaseObject.getObjects(Context.getTemplate());
            assertEquals(19, templates.size());
            assertNotNull(((Template) templates.get(0)).__getDateadded());

            Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
            c.setCname("Muster GmbH");
            c.setPrename("Erika");
            c.setCity("Köln");
            c.setisCustomer(true);
            c.setDateadded(new Date());
            assertTrue(c.save(true));
            Contact c2 = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS());
            assertEquals("Muster GmbH", c2.__getCname());
            assertEquals("Erika", c2.__getPrename());
            assertEquals("Köln", c2.__getCity());
            assertTrue(c2.__getIscustomer());
            assertEquals(DateConverter.getDefDateString(c.__getDateadded()), DateConverter.getDefDateString(c2.__getDateadded()));

            Product p = (Product) DatabaseObject.getObject(Context.getProduct());
            p.setCname("Kaffee");
            p.setCnumber("A-1");
            p.setExternalnetvalue(new BigDecimal("2.50"));
            p.setStockvalue(new BigDecimal("12"));
            p.setDateadded(new Date());
            assertTrue(p.save(true));
            Product p2 = (Product) DatabaseObject.getObject(Context.getProduct(), p.__getIDS());
            assertEquals(0, new BigDecimal("2.50").compareTo(p2.__getExternalnetvalue()));
            assertEquals(0, new BigDecimal("12").compareTo(p2.__getStockvalue()));

            Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
            inv.setContactsids(c.__getIDS());
            inv.setCname("Rechnung Test");
            inv.setInttype(Item.TYPE_INVOICE);
            inv.setDatetodo(new Date());
            inv.setDateend(new Date());
            inv.setDateadded(new Date());
            inv.setAccountsids(1);
            assertTrue(inv.save(true));
            for (int i = 1; i <= 3; i++) {
                SubItem s = new SubItem();
                s.setItemsids(inv.__getIDS());
                s.setCname("Pos " + i);
                s.setQuantityvalue(new BigDecimal(i));
                s.setExternalvalue(new BigDecimal("10.00"));
                s.setInternalvalue(new BigDecimal("10.00"));
                s.setTaxpercentvalue(new BigDecimal("19"));
                s.setOrdernr(i);
                s.setDateadded(new Date());
                assertTrue(s.save(true));
            }
            mpv5.handler.ItemService.updateTotals(inv);
            Item inv2 = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
            assertTrue(inv2.__getCnumber() != null && !inv2.__getCnumber().isEmpty());
            assertEquals(3, inv2.getSubitems().length);
            assertEquals(0, new BigDecimal("60").compareTo(inv2.__getNetvalue()));
            assertEquals(c.__getIDS(), inv2.__getContactsids());
            assertEquals(DateConverter.getDefDateString(inv.__getDatetodo()), DateConverter.getDefDateString(inv2.__getDatetodo()));

            //trash bin (database triggers) and restore
            assertTrue(c2.delete());
            Object[][] trash = QueryHandler.instanceOf().clone("trashbin").select("cname, rowid", new String[]{"deleteme", "1", ""});
            assertTrue(Arrays.stream(trash).anyMatch(r -> "contacts".equalsIgnoreCase(String.valueOf(r[0]))));
            assertTrue(c2.undelete());
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}
