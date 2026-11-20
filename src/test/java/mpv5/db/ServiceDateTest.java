package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.utils.date.DateConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The service date (Leistungsdatum, datetodo) and the date of the last change
 * of a document (#208)
 */
class ServiceDateTest {

    @TempDir
    Path tmp;

    @Test
    void serviceDateDefaultsToTheDocumentDateAndTheLastChangeIsSetOnSave() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 20, 10, 0, 0);
        Date invoiceDate = cal.getTime();
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(c.__getIDS());
        inv.setCname("Beratung");
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setDateadded(invoiceDate);
        inv.setAccountsids(1);
        assertNull(inv.__getDatetodo());
        Date before = new Date(System.currentTimeMillis() - 1000);
        assertTrue(inv.save(true));

        Item saved = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
        assertEquals(DateConverter.getDefDateString(invoiceDate), DateConverter.getDefDateString(saved.__getDatetodo()),
                "no service date: the invoice date");
        assertNotNull(saved.__getDatelastupdated());
        assertFalse(saved.__getDatelastupdated().before(before), saved.__getDatelastupdated().toString());

        //a service date of its own is kept; the last change is set again
        cal.set(2026, Calendar.OCTOBER, 21, 0, 0, 0);
        saved.setDatetodo(cal.getTime());
        saved.setDatelastupdated(new Date(0));
        Date service = cal.getTime();
        assertTrue(saved.save(true));
        Item again = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
        assertEquals(DateConverter.getDefDateString(service), DateConverter.getDefDateString(again.__getDatetodo()));
        assertTrue(again.__getDatelastupdated().getTime() > 0);

        //template fields
        Map<String, Object> fields = again.getFormFields();
        assertFalse(field(fields, "servicedate").isEmpty(), fields.toString());
        assertFalse(field(fields, "datelastupdated").isEmpty(), fields.toString());
    }

    @Test
    void theDemoInvoiceOfThePreviousReleaseGetsTheServiceDate() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        mpv5.db.objects.Template t = null;
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
            if ("Demo - Rechnung".equals(o.__getCname())) {
                t = (mpv5.db.objects.Template) o;
            }
        }
        assertNotNull(t);
        assertTrue(content(t).contains("item.servicedate"), "new installations: the current template");

        //an installation of the previous release: its unchanged demo invoice is replaced
        java.io.File old = tmp.resolve("previous.odt").toFile();
        try (java.io.InputStream in = getClass().getResourceAsStream("/templates/demo-00-previous.odt")) {
            java.nio.file.Files.copy(in, old.toPath());
        }
        mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFiles()).updateFileNow(old, t.__getFilename());
        t.forgetFile();
        assertFalse(content(t).contains("item.servicedate"));
        String format = t.__getFormat();
        mpv5.handler.FirstStartSetup.upgradeDemoTemplates();
        t.forgetFile();
        mpv5.db.objects.Template again = (mpv5.db.objects.Template) DatabaseObject.getObject(Context.getTemplate(), t.__getIDS());
        again.forgetFile();
        assertTrue(content(again).contains("item.servicedate"), "upgraded");
        assertEquals(format, again.__getFormat(), "the column format stays");
    }

    private static String content(mpv5.db.objects.Template t) throws Exception {
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(t.getFile())) {
            return new String(z.getInputStream(z.getEntry("content.xml")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static String field(Map<String, Object> fields, String name) {
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            if (e.getKey().endsWith("." + name) && e.getKey().indexOf('.') == e.getKey().lastIndexOf('.')) {
                return String.valueOf(e.getValue());
            }
        }
        return "";
    }
}
