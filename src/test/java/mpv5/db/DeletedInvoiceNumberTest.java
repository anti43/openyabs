package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.handler.ItemService;
import mpv5.handler.Privacy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The number of a deleted invoice is not given out again (#213)
 */
class DeletedInvoiceNumberTest {

    @TempDir
    Path tmp;

    private static Item save(int type, int contact) throws Exception {
        Item i = (Item) DatabaseObject.getObject(ItemService.contextOf(type));
        i.setContactsids(contact);
        i.setCname("Beleg");
        i.setInttype(type);
        i.setDateadded(new Date());
        i.setAccountsids(1);
        assertTrue(i.save(true));
        return (Item) DatabaseObject.getObject(ItemService.contextOf(type), i.__getIDS());
    }

    @Test
    void deletedNumberIsSkipped() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        String y = new java.text.SimpleDateFormat("yyyy").format(new Date());

        save(Item.TYPE_INVOICE, c.__getIDS());
        Item second = save(Item.TYPE_INVOICE, c.__getIDS());
        assertEquals("RE-" + y + "-00002", second.__getCnumber());
        assertFalse(Privacy.isIssuedInvoice(second), "a draft");

        //the last invoice is deleted: in the trash as "!RE-...-00002" (delete() resets the id of the object)
        int secondId = second.__getIDS();
        assertTrue(second.delete());
        Item next = save(Item.TYPE_INVOICE, c.__getIDS());
        assertEquals("RE-" + y + "-00003", next.__getCnumber(), "the number of the deleted invoice is not used again");
        int[] used = ItemService.invoiceNumberUsedBy("RE-" + y + "-00002", -1);
        assertNotNull(used, "the number editor sees the deleted one");
        assertEquals(secondId, used[1]);

        //issued: finished (or printed, paid)
        next.setIntstatus(Item.STATUS_FINISHED);
        assertTrue(next.save(true));
        assertTrue(Privacy.isIssuedInvoice(DatabaseObject.getObject(Context.getInvoice(), next.__getIDS())));
        assertFalse(Privacy.isIssuedInvoice(save(Item.TYPE_OFFER, c.__getIDS())), "not an invoice");
    }
}
