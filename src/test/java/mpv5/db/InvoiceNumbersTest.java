package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.handler.FormatHandler;
import mpv5.handler.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Numbers of invoices, credit notes, deposit and part payment invoices are
 * unique among all of them (#212)
 */
class InvoiceNumbersTest {

    @TempDir
    Path tmp;

    @Test
    void sharedRangeIsDetected() {
        Map<Integer, String> p = new HashMap<Integer, String>();
        //nothing stored: all four use the default format
        assertTrue(FormatHandler.invoiceTypesShareARange(p));
        p.putAll(FormatHandler.defaultInvoiceFormats(true));
        assertFalse(FormatHandler.invoiceTypesShareARange(p));
        p.put(Item.TYPE_CREDIT, p.get(Item.TYPE_INVOICE));
        assertTrue(FormatHandler.invoiceTypesShareARange(p));
        assertEquals("RE-[YEAR]-{0,number,00000}", FormatHandler.defaultInvoiceFormats(true).get(Item.TYPE_INVOICE));
        assertEquals("CN-[YEAR]-{0,number,00000}", FormatHandler.defaultInvoiceFormats(false).get(Item.TYPE_CREDIT));
    }

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
    void newDatabasesHavePrefixesAndOneRangeIsShared() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        String year = new java.text.SimpleDateFormat("yyyy").format(new Date());

        //a new database: a prefix per kind
        Item invoice = save(Item.TYPE_INVOICE, c.__getIDS());
        Item credit = save(Item.TYPE_CREDIT, c.__getIDS());
        Item deposit = save(Item.TYPE_DEPOSIT, c.__getIDS());
        Item part = save(Item.TYPE_PART_PAYMENT, c.__getIDS());
        assertEquals("RE-" + year + "-00001", invoice.__getCnumber());
        assertEquals("GS-" + year + "-00001", credit.__getCnumber());
        assertEquals("AZ-" + year + "-00001", deposit.__getCnumber());
        assertEquals("AR-" + year + "-00001", part.__getCnumber());

        //the same format for invoices and credit notes (as older databases): one range, no number twice
        String plain = FormatHandler.INTEGERPART_IDENTIFIER;
        FormatHandler.store(Item.TYPE_INVOICE, User.ADMIN_ID, plain, 0, null);
        FormatHandler.store(Item.TYPE_CREDIT, User.ADMIN_ID, plain, 0, null);
        Item i1 = save(Item.TYPE_INVOICE, c.__getIDS());
        Item c1 = save(Item.TYPE_CREDIT, c.__getIDS());
        Item i2 = save(Item.TYPE_INVOICE, c.__getIDS());
        assertEquals("000001", i1.__getCnumber());
        assertEquals("000002", c1.__getCnumber(), "the credit note skips the number of the invoice");
        assertEquals("000003", i2.__getCnumber());

        //who uses a number (the manual number editor)
        int[] used = ItemService.invoiceNumberUsedBy("000002", i1.__getIDS());
        assertNotNull(used);
        assertEquals(Item.TYPE_CREDIT, used[0]);
        assertNull(ItemService.invoiceNumberUsedBy("000002", c1.__getIDS()), "not the document itself");
        assertNull(ItemService.invoiceNumberUsedBy("000099", -1));
        //offers and orders have ranges of their own: the first offer is 000001 although an invoice has it
        Item offer = save(Item.TYPE_OFFER, c.__getIDS());
        assertEquals("000001", offer.__getCnumber());
    }
}
