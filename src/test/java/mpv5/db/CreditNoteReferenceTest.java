package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.handler.ItemService;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.ZugpferdFile;
import mpv5.utils.models.MPComboBoxModelItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A credit note refers to the invoice it corrects (#211): stored, as
 * template fields, and as choice in the view
 */
class CreditNoteReferenceTest {

    @TempDir
    Path tmp;

    @Test
    void creditNoteFromAnInvoiceRefersToIt() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));

        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 4, 10, 0, 0);
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(c.__getIDS());
        inv.setCname("Beratung");
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setDateadded(cal.getTime());
        inv.setAccountsids(1);
        assertTrue(inv.save(true));

        SubItem s = new SubItem();
        s.setDescription("Beratung");
        s.setQuantityvalue(BigDecimal.ONE);
        s.setExternalvalue(new BigDecimal("100"));
        s.setTaxpercentvalue(new BigDecimal("19"));
        Item credit = ItemService.toInvoice(inv, Item.TYPE_CREDIT, SubItem.toModel(new SubItem[]{s}, true), q -> false);
        assertNotNull(credit);
        Item saved = (Item) DatabaseObject.getObject(Context.getCredit(), credit.__getIDS());
        assertEquals(inv.__getIDS(), saved.__getRefinvoiceids(), "the corrected invoice is stored");
        assertNotNull(saved.getCorrectedInvoice());
        assertEquals(inv.__getCnumber(), saved.getCorrectedInvoice().__getCnumber());
        assertTrue(ZugpferdFile.supports(saved), "credit notes can be e-invoices");

        Map<String, Object> fields = saved.getFormFields();
        assertEquals(inv.__getCnumber(), field(fields, "correctedinvoicenumber"), fields.toString());
        assertFalse(field(fields, "correctedinvoicedate").isEmpty());
        assertTrue(field(fields, "correctedinvoice").contains(inv.__getCnumber()));

        //an invoice from an order refers to no invoice
        assertNull(inv.__getRefinvoiceids());

        //the choices of the view: the invoices of the customer
        MPComboBoxModelItem[] choices = ItemService.referenceInvoiceChoices(c);
        assertEquals(1, choices.length);
        assertEquals(String.valueOf(inv.__getIDS()), String.valueOf(choices[0].getId()));
        assertTrue(choices[0].getValue().startsWith(inv.__getCnumber()), choices[0].getValue());
        assertTrue(DateConverter.getDefDateString(cal.getTime()).length() > 0);
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
