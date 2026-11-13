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
 * The real application code on a new database, like a new installation:
 * structure (from the schema file), initial data, the first start setup
 * (templates), then contacts, products and invoices saved and loaded again,
 * trash and restore. Runs without a display. One subclass per database, each
 * in its own JVM (the application keeps the connection in static fields).
 */
abstract class AppOnDatabase {

    @TempDir
    Path tmp;

    /**
     * @param driver the JDBC driver (ConnectionTypeHandler.*_DRIVER)
     * @param location H2: folder, servers: host:port
     * @param dbname the database (created)
     * @param user
     * @param password
     */
    /**
     * @param contact
     * @param lastNumber a credit note with this number is saved first (the
     * last number), null: none
     * @return the number of a new credit note
     */
    private static String newCredit(int contact, String lastNumber) throws Exception {
        if (lastNumber != null) {
            Item old = (Item) DatabaseObject.getObject(Context.getCredit());
            old.setInttype(Item.TYPE_CREDIT);
            old.setCnumber(lastNumber);
            old.setCname(lastNumber);
            old.setContactsids(contact);
            old.setDateadded(new Date());
            old.setAccountsids(1);
            assertTrue(old.save(true));
            //a new record gets the next number on its first save: the old number by a second save
            old.setCnumber(lastNumber);
            assertTrue(old.save(true));
        }
        Item n = (Item) DatabaseObject.getObject(Context.getCredit());
        n.setInttype(Item.TYPE_CREDIT);
        n.setCname("Gutschrift");
        n.setContactsids(contact);
        n.setDateadded(new Date());
        n.setAccountsids(1);
        assertTrue(n.save(true));
        return ((Item) DatabaseObject.getObject(Context.getCredit(), n.__getIDS())).__getCnumber();
    }

    void newInstallationSaveAndLoad(String driver, String location, String dbname, String user, String password) throws Exception {
        if (System.getenv("YABS_TEST_DEBUG") != null) {
            mpv5.logging.Log.setLogLevel(mpv5.logging.Log.LOGLEVEL_DEBUG);
        }
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, driver);
        LocalSettings.setProperty(LocalSettings.DBNAME, dbname);
        LocalSettings.setProperty(LocalSettings.DBUSER, user);
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, password);
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.save(0);
        try {
            //like the first start assistant
            DatabaseConnection conn = new DatabaseConnection();
            assertTrue(conn.connect(driver, user, password, location, dbname, "", true));
            assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
            assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
            QueryHandler.instanceOf();//version check: nothing to update
            Object[][] version = QueryHandler.instanceOf().clone("globalsettings").select("value", new String[]{"cname", "yabs_dbversion", "'"});
            assertEquals(String.valueOf(mpv5.globals.Constants.DATABASE_VERSION), String.valueOf(version[0][0]).trim(), "created at the current version");
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

            //payments on the invoice (60 net + 19 % = 71.40): open amount, paid when complete, open again when removed
            inv2.setIntstatus(Item.STATUS_FINISHED);
            assertTrue(inv2.save(true));
            java.text.SimpleDateFormat ymd = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date day1 = ymd.parse("2026-03-01");
            Date day2 = ymd.parse("2026-03-15");
            assertTrue(mpv5.handler.Payments.add(inv2, new BigDecimal("30"), day1, "Anzahlung"));
            assertEquals(0, new BigDecimal("41.40").compareTo(mpv5.handler.Payments.open(inv2)));
            assertEquals(Item.STATUS_FINISHED, ((Item) DatabaseObject.getObject(Context.getInvoice(), inv2.__getIDS())).__getIntstatus());
            assertTrue(mpv5.handler.Payments.add(inv2, new BigDecimal("41.40"), day2, "Rest"));
            Item paid = (Item) DatabaseObject.getObject(Context.getInvoice(), inv2.__getIDS());
            assertEquals(Item.STATUS_PAID, paid.__getIntstatus(), "paid in full");
            assertEquals("2026-03-15", ymd.format(paid.__getDateend()), "pay date = last payment");
            java.util.List<mpv5.handler.Payments.Payment> pl = mpv5.handler.Payments.of(inv2.__getIDS());
            assertEquals(2, pl.size());
            assertEquals("Anzahlung", pl.get(0).description);
            assertEquals("2026-03-01", ymd.format(pl.get(0).date));
            assertEquals(1, mpv5.handler.Payments.between(day2, day2).size(), "by pay date");
            java.util.Map<String, Object> ff = paid.getFormFields();
            assertEquals(mpv5.utils.numberformat.FormatNumber.formatLokalCurrency(new BigDecimal("71.40")), ff.get("item.paidvaluef"));
            assertEquals(mpv5.utils.numberformat.FormatNumber.formatLokalCurrency(new BigDecimal("0.00")), ff.get("item.openvaluef"));
            mpv5.handler.Payments.remove(paid, pl.get(1).id);
            assertEquals(Item.STATUS_FINISHED, ((Item) DatabaseObject.getObject(Context.getInvoice(), inv2.__getIDS())).__getIntstatus(), "open again");
            inv2 = (Item) DatabaseObject.getObject(Context.getInvoice(), inv2.__getIDS());

            //search index and trash bin (database triggers), restore
            Object[][] index = QueryHandler.instanceOf().clone("searchindex").select("dbidentity, rowid", new String[]{"dbidentity", "contacts", "'"});
            assertTrue(index.length > 0, "search index");
            assertTrue(c2.delete());
            Object[][] trash = QueryHandler.instanceOf().clone("trashbin").select("cname, rowid", new String[]{"deleteme", "1", ""});
            assertTrue(Arrays.stream(trash).anyMatch(r -> "contacts".equalsIgnoreCase(String.valueOf(r[0]))));
            assertTrue(c2.undelete());

            //what YaBS saves on close: user properties (UPDATE, else INSERT) and the layout (binary value property)
            admin.getProperties().changeProperty("order.alert.days", "7");
            admin.saveProperties();
            admin.getProperties().changeProperty("order.alert.days", "8");
            admin.saveProperties();
            Object[][] up = QueryHandler.instanceOf().clone("userproperties").select("value", new String[]{"cname", "order.alert.days", "'"});
            assertEquals("8", String.valueOf(up[0][0]), "user property saved and changed");
            //updateOrCreate: no row yet -> INSERT with the columns of the criteria (were table-qualified)
            mpv5.db.common.QueryCriteria2 qc = new mpv5.db.common.QueryCriteria2();
            qc.and(new mpv5.db.common.QueryParameter(Context.getUserProperties(), "usersids", admin.__getIDS(), mpv5.db.common.QueryParameter.EQUALS),
                    new mpv5.db.common.QueryParameter(Context.getUserProperties(), "groupsids", 1, mpv5.db.common.QueryParameter.EQUALS),
                    new mpv5.db.common.QueryParameter(Context.getUserProperties(), "cname", "test.updateorcreate", mpv5.db.common.QueryParameter.EQUALS));
            mpv5.db.common.QueryData qd = new mpv5.db.common.QueryData();
            qd.add("value", "x");
            qd.add("dateadded", mpv5.utils.date.DateConverter.getSQLDateString(new Date()));
            QueryHandler.instanceOf().clone(Context.getUserProperties()).updateOrCreate(qd, qc, null);
            Object[][] uoc = QueryHandler.instanceOf().clone("userproperties").select("value", new String[]{"cname", "test.updateorcreate", "'"});
            assertEquals(1, uoc.length, "updateOrCreate inserted the row");
            java.util.HashMap<String, String> layout = new java.util.HashMap<String, String>();
            layout.put("divider", "220");
            mpv5.db.objects.ValueProperty.addOrUpdateProperty("layoutinfo", layout, admin);
            layout.put("divider", "240");
            mpv5.db.objects.ValueProperty.addOrUpdateProperty("layoutinfo", layout, admin);
            Object v = mpv5.db.objects.ValueProperty.getProperty(admin, "layoutinfo").getValueObj();
            assertEquals("240", ((java.util.Map<?, ?>) v).get("divider"), "layout (binary) saved and changed");

            //scripts of the script manager (stored as #script#, cached): run on save, a changed script applies at once
            mpv5.db.objects.ValueProperty.addOrUpdateProperty("modifyOnSave", "#dbo.setCname(dbo.__getCname().trim())#",
                    Context.getProduct(), mpv5.db.objects.Group.getDefault());
            Product ps = (Product) DatabaseObject.getObject(Context.getProduct());
            ps.setCname("  Tee  ");
            ps.setCnumber("T-1");
            ps.setDateadded(new Date());
            assertTrue(ps.save(true));
            assertEquals("Tee", ((Product) DatabaseObject.getObject(Context.getProduct(), ps.__getIDS())).__getCname(), "script ran");
            mpv5.db.objects.ValueProperty.addOrUpdateProperty("modifyOnSave", "#dbo.setCname(dbo.__getCname().toUpperCase())#",
                    Context.getProduct(), mpv5.db.objects.Group.getDefault());
            ps.setCname("kaffee");
            assertTrue(ps.save(true));
            assertEquals("KAFFEE", ((Product) DatabaseObject.getObject(Context.getProduct(), ps.__getIDS())).__getCname(), "changed script ran");

            //backup page: local H2 can be zipped (with its size), servers not (with the reason)
            mpv5.db.common.DatabaseBackup.Status bs = mpv5.db.common.DatabaseBackup.status();
            if (ConnectionTypeHandler.H2_DRIVER.equals(driver)) {
                assertTrue(bs.possible, bs.reason);
                assertTrue(bs.size > 0, "size of the database files");
                assertTrue(bs.location.endsWith(dbname + ".mv.db"), bs.location);
            } else {
                assertTrue(!bs.possible && bs.reason != null && bs.size < 0, bs.type + " " + bs.reason);
            }

            //number formats (format manager): restart with a new year, keep counting, set the next number
            int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            mpv5.handler.NumberFormatBuilder nf = new mpv5.handler.NumberFormatBuilder().setPrefix("GS-")
                    .setDate(mpv5.handler.NumberFormatBuilder.DatePart.YEAR).setSeparator("-").setDigits(5).setRestart(true);
            mpv5.handler.FormatHandler.store(Item.TYPE_CREDIT, User.ADMIN_ID, nf.pattern(), nf.parsePosition(), null);
            assertEquals("GS-" + year + "-00001", newCredit(c.__getIDS(), "GS-" + (year - 1) + "-00007"), "restart with the new year");
            nf.setRestart(false);
            mpv5.handler.FormatHandler.store(Item.TYPE_CREDIT, User.ADMIN_ID, nf.pattern(), nf.parsePosition(), null);
            //(another old number: the app refuses a number that exists already)
            assertEquals("GS-" + year + "-00010", newCredit(c.__getIDS(), "GS-" + (year - 1) + "-00009"), "keep counting");
            mpv5.handler.FormatHandler.store(Item.TYPE_CREDIT, User.ADMIN_ID, nf.pattern(), nf.parsePosition(), 100);
            assertEquals("GS-" + year + "-00100", newCredit(c.__getIDS(), null), "next number set");
            //a next number is kept until a record of its kind is numbered, also when another kind is numbered first
            mpv5.handler.FormatHandler.store(Item.TYPE_CREDIT, User.ADMIN_ID, nf.pattern(), nf.parsePosition(), 200);
            Contact other = (Contact) DatabaseObject.getObject(Context.getContact());
            other.setCname("Zweite GmbH");
            other.setisCustomer(true);
            other.setDateadded(new Date());
            assertTrue(other.save(true));
            assertTrue(!other.__getCNumber().contains(mpv5.handler.FormatHandler.START_VALUE_IDENTIFIER), other.__getCNumber());
            assertEquals("GS-" + year + "-00200", newCredit(c.__getIDS(), null), "next number set, after a contact");
            assertEquals("GS-" + year + "-00201", newCredit(c.__getIDS(), null), "then counting on");
            mpv5.handler.FormatHandler.clearCache();
            assertEquals("GS-" + year + "-00202", newCredit(c.__getIDS(), null), "the next number was used once only");

            //mail (log mode, no questions): nothing is sent without sender, recipient and template
            java.util.List<String> log = new java.util.ArrayList<String>();
            admin.setMail("");
            mpv5.utils.export.Export.mail(null, inv2, c2, log);
            assertTrue(log.get(0).contains(mpv5.globals.Messages.MAIL_NO_SENDER.toString()), log.toString());
            admin.setMail("chef@example.org");
            c2.setMailaddress("");
            mpv5.utils.export.Export.mail(null, inv2, c2, log);
            assertTrue(log.get(1).contains(mpv5.globals.Messages.NO_MAIL_DEFINED.toString()), log.toString());
            c2.setMailaddress("kunde@example.org");
            mpv5.utils.export.Export.mail(null, inv2, c2, log);
            assertTrue(log.get(2).contains(mpv5.globals.Messages.MAIL_NO_TEMPLATE_BATCH.toString()), log.toString());
        } finally {
            DatabaseConnection.shutdown();
        }
    }
}
