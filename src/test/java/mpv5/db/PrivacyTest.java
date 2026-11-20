package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Conversation;
import mpv5.db.objects.Item;
import mpv5.db.objects.ValueProperty;
import mpv5.handler.Privacy;
import mpv5.handler.TrashHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Deleting a contact with documents which must be kept: blocked instead
 * (GDPR Art. 17 Abs. 3 b, Art. 18), data export (Art. 15, 20), the trash keeps
 * what must be kept (#220)
 */
class PrivacyTest {

    @TempDir
    Path tmp;

    private static Item item(Contact c, int type, String number, Date date, int status) {
        Item i = (Item) DatabaseObject.getObject(Privacy.contextOf(type));
        i.setContactsids(c.__getIDS());
        i.setCname(number);
        i.setCnumber(number);
        i.setInttype(type);
        i.setDateadded(date);
        i.setIntstatus(status);
        i.setAccountsids(1);
        assertTrue(i.save(true));
        return i;
    }

    private static int count(String sql) {
        return Integer.parseInt(String.valueOf(QueryHandler.getConnection().freeQuery(sql,
                mpv5.usermanagement.MPSecurityManager.VIEW, null).getData()[0][0]));
    }

    @Test
    void contactWithInvoicesIsBlockedNotDeleted() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Muster");
        c.setPrename("Max");
        c.setStreet("Ring 2");
        c.setCity("Berlin");
        c.setMainphone("030 123");
        c.setMailaddress("max@muster.example");
        c.setNotes("likes coffee");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        ValueProperty.addOrUpdateProperty("hobby", "sailing", c);
        ValueProperty.addOrUpdateProperty(mpv5.utils.export.ZugpferdFile.KEY_BUYER_REFERENCE, "04011000-1", c);
        Conversation talk = (Conversation) DatabaseObject.getObject(Context.getConversation());
        talk.setContactsids(c.__getIDS());
        talk.setCname("Call");
        talk.setContent("talked about the offer");
        talk.setDate(new Date());
        talk.setTimestamp(new Date());
        talk.setDateadded(new Date());
        assertTrue(talk.save(true));

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 15, 12, 0, 0);
        Item paid = item(c, Item.TYPE_INVOICE, "R-1", cal.getTime(), Item.STATUS_PAID);
        Item offer = item(c, Item.TYPE_OFFER, "A-1", cal.getTime(), Item.STATUS_FINISHED);
        Item open = item(c, Item.TYPE_INVOICE, "R-2", cal.getTime(), Item.STATUS_FINISHED);

        Privacy.Report r = Privacy.check(c);
        assertTrue(r.mustKeep());
        assertEquals("31.12.2034", mpv5.utils.date.DateConverter.getDefDateString(r.keepUntil));
        assertEquals(2, r.retained.size(), r.retained.toString());
        assertEquals(1, r.deletable.size(), "the offer was not accepted");
        assertEquals(1, r.open.size());

        //no deletion: neither of the contact nor (by cascade) of its invoices
        assertFalse(c.delete());
        assertEquals(0, count("SELECT invisible FROM contacts WHERE ids = " + c.__getIDS()));
        assertEquals(3, count("SELECT COUNT(*) FROM items WHERE contactsids = " + c.__getIDS() + " AND invisible = 0"));

        //an open invoice: pay or cancel it first
        assertThrows(IllegalStateException.class, () -> Privacy.block(c));
        open.setIntstatus(Item.STATUS_CANCELLED);
        assertTrue(open.save(true));

        //the person asks for a copy first
        File zip = tmp.resolve("data.zip").toFile();
        Privacy.export(c, zip);
        Set<String> names = new HashSet<String>();
        String contactJson;
        try (ZipFile z = new ZipFile(zip)) {
            for (ZipEntry e : java.util.Collections.list(z.entries())) {
                names.add(e.getName());
            }
            contactJson = new String(z.getInputStream(z.getEntry("contact.json")).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        assertTrue(names.containsAll(java.util.Arrays.asList("README.txt", "contact.json", "documents.json", "conversations.json", "own_fields.json")), names.toString());
        assertTrue(contactJson.contains("max@muster.example"), contactJson);

        Privacy.block(c);
        Contact blocked = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
        assertTrue(Privacy.isBlocked(blocked));
        assertEquals("Muster", blocked.__getCname(), "the name is on the invoices");
        assertEquals("Ring 2", blocked.__getStreet());
        assertTrue(blocked.__getMainphone() == null || blocked.__getMainphone().isEmpty());
        assertTrue(blocked.__getMailaddress() == null || blocked.__getMailaddress().isEmpty());
        assertTrue(blocked.__getNotes() == null || blocked.__getNotes().isEmpty());
        assertEquals(0, count("SELECT COUNT(*) FROM conversations WHERE contactsids = " + c.__getIDS()));
        assertEquals(0, count("SELECT COUNT(*) FROM items WHERE ids = " + offer.__getIDS()), "the offer is deleted");
        assertEquals(2, count("SELECT COUNT(*) FROM items WHERE ids IN (" + paid.__getIDS() + "," + open.__getIDS() + ")"),
                "the invoices are kept");
        assertFalse(ValueProperty.hasProperty(blocked, "hobby"));
        assertTrue(ValueProperty.hasProperty(blocked, mpv5.utils.export.ZugpferdFile.KEY_BUYER_REFERENCE), "used for the invoices");

        //the trash does not delete what must be kept
        assertNotNull(Privacy.whyKeep(Context.getContact(), c.__getIDS()));
        assertNotNull(Privacy.whyKeep(Context.getInvoice(), paid.__getIDS()));
        assertFalse(TrashHandler.deleteFinally("contacts", c.__getIDS(), "test"));
        assertFalse(TrashHandler.deleteFinally("items", paid.__getIDS(), "test"));
        assertEquals(1, count("SELECT COUNT(*) FROM items WHERE ids = " + paid.__getIDS()));
        assertFalse(TrashHandler.takeRefused().isEmpty());

        //not before the end of the retention period
        assertFalse(Privacy.deleteFinally(c));
        assertTrue(Privacy.expired().isEmpty());
    }

    @Test
    void contactWithoutDocumentsIsDeletedAsBefore() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Interessent");
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        ValueProperty.addOrUpdateProperty("note", "x", c);
        Item draft = item(c, Item.TYPE_INVOICE, "ENTWURF-1", new Date(), Item.STATUS_IN_PROGRESS);
        assertFalse(Privacy.check(c).mustKeep(), "a draft invoice need not be kept");
        assertTrue(c.delete());
        assertEquals(1, count("SELECT invisible FROM contacts WHERE ids = " + c.__getIDS()), "in the trash");
        assertEquals(1, count("SELECT invisible FROM items WHERE ids = " + draft.__getIDS()), "the draft too");
        //in the trash the record keeps its properties (restored with it)
        assertTrue(ValueProperty.hasProperty(c, "note"));
        assertTrue(TrashHandler.deleteFinally("contacts", c.__getIDS(), "test"));
        assertEquals(0, count("SELECT COUNT(*) FROM contacts WHERE ids = " + c.__getIDS()));
        assertEquals(0, count("SELECT COUNT(*) FROM valueproperties WHERE contextids = " + Context.getContact().getId()
                + " AND objectids = " + c.__getIDS()), "deleted with the record");
    }
}
