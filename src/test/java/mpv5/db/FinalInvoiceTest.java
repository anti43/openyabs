package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.handler.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The final invoice with deducted deposits (#221), the scenario of the issue:
 * order 100 x 100,00 at 19 % + 1.000,00 at 7 %; deposit 1: 3.000 at 19 %,
 * paid; deposit 2: 2.000 at 19 % + 500 at 7 %, not paid
 */
class FinalInvoiceTest {

    @TempDir
    Path tmp;

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static SubItem line(String text, String qty, String price, String rate) {
        SubItem s = new SubItem();
        s.setDescription(text);
        s.setCname(text);
        s.setQuantityvalue(bd(qty));
        s.setExternalvalue(bd(price));
        s.setTaxpercentvalue(bd(rate));
        return s;
    }

    private static Item document(int type, Contact c, int order, int status, SubItem... lines) throws Exception {
        Item i = (Item) DatabaseObject.getObject(ItemService.contextOf(type));
        i.setContactsids(c.__getIDS());
        i.setCname("Beleg");
        i.setInttype(type);
        i.setDateadded(new Date());
        i.setAccountsids(1);
        i.setIntstatus(status);
        if (order > 0) {
            i.setReforderids(order);
        }
        assertTrue(i.save(true));
        for (int k = 0; k < lines.length; k++) {
            lines[k].setItemsids(i.__getIDS());
            lines[k].setOrdernr(k);
            lines[k].setDateadded(new Date());
            lines[k].setGroupsids(1);
            assertTrue(lines[k].save(true));
        }
        ItemService.updateTotals(i);
        return (Item) DatabaseObject.getObject(ItemService.contextOf(type), i.__getIDS());
    }

    private static void money(String expected, BigDecimal actual, String what) {
        assertEquals(0, bd(expected).compareTo(actual.setScale(2, java.math.RoundingMode.HALF_UP)), what + ": " + actual);
    }

    @Test
    void depositsAreDeductedIdentifiedAndOnlyOnce() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));

        Item order = document(Item.TYPE_ORDER, c, 0, Item.STATUS_QUEUED,
                line("Montage", "100", "100", "19"), line("Material", "1", "1000", "7"));
        money("11000.00", order.getNetAfterDiscount(), "order net");
        money("1970.00", order.__getTaxvalue(), "order VAT");
        Item d1 = document(Item.TYPE_DEPOSIT, c, order.__getIDS(), Item.STATUS_PAID, line("Anzahlung 1", "1", "3000", "19"));
        //a part payment as well: merging deposits and part payments crashed (ClassCastException)
        Item d2 = document(Item.TYPE_PART_PAYMENT, c, order.__getIDS(), Item.STATUS_QUEUED,
                line("Abschlag Montage", "1", "2000", "19"), line("Abschlag Material", "1", "500", "7"));

        List<String> asked = new ArrayList<String>();
        Item fin = ItemService.toInvoice(order, Item.TYPE_INVOICE,
                SubItem.toModel(order.getSubitems(), true), q -> asked.add(q));
        assertNotNull(fin);
        fin = (Item) DatabaseObject.getObject(Context.getInvoice(), fin.__getIDS());
        //11.000 - 3.000 - 2.500 = 5.500 net; 1.970 - 570 - 380 - 35 = 985 VAT
        money("5500.00", fin.getNetAfterDiscount(), "final net");
        money("985.00", fin.__getTaxvalue(), "final VAT");

        SubItem[] subs = fin.getSubitems();
        assertEquals(5, subs.length, "2 positions of the order, 1 + 2 deductions (per VAT rate)");
        boolean d1Line = false, d2Unpaid = false;
        for (int k = 0; k < subs.length; k++) {
            String text = subs[k].__getDescription();
            if (text.contains(d1.__getCnumber())) {
                d1Line = true;
                assertFalse(text.contains(mpv5.globals.Messages.FINAL_DEDUCTION_UNPAID.toString().trim()), text);
                assertTrue(subs[k].__getExternalvalue().signum() < 0, "deducted");
            }
            if (text.contains(d2.__getCnumber())) {
                d2Unpaid |= text.contains(mpv5.globals.Messages.FINAL_DEDUCTION_UNPAID.toString().trim());
            }
        }
        assertTrue(d1Line, "the number of the deposit is in the deduction");
        assertTrue(d2Unpaid, "the unpaid part payment is marked");
        //position numbers go on (they restarted at 1 for each deposit)
        java.util.Set<Integer> numbers = new java.util.HashSet<Integer>();
        for (SubItem s : subs) {
            numbers.add(s.__getCountvalue().intValue());
        }
        assertEquals(5, numbers.size(), "no number twice: " + numbers);

        //asked: deduct the deposits, and about the unpaid part payment
        assertTrue(asked.stream().anyMatch(q -> q.contains(d2.__getCnumber())), asked.toString());
        assertTrue(asked.stream().noneMatch(q -> q.contains(d1.__getCnumber())), "the paid deposit is not asked about: " + asked);

        //the deposits remember the final invoice
        List<Item> deducted = ItemService.deductedIn(fin);
        assertEquals(2, deducted.size());
        Map<String, Object> fields = fin.getFormFields();
        assertTrue(field(fields, "deductedinvoices").contains(d1.__getCnumber()), fields.toString());
        assertEquals("true", field(fields, "finalinvoice"));

        //a second final invoice from the order: asked whether to deduct again - no
        final String first = fin.__getCnumber();
        final Item firstInvoice = fin;
        asked.clear();
        Item fin2 = ItemService.toInvoice(order, Item.TYPE_INVOICE,
                SubItem.toModel(order.getSubitems(), true), q -> {
                    asked.add(q);
                    return !q.contains(first);
                });
        fin2 = (Item) DatabaseObject.getObject(Context.getInvoice(), fin2.__getIDS());
        money("11000.00", fin2.getNetAfterDiscount(), "nothing deducted twice");
        assertTrue(asked.stream().anyMatch(q -> q.contains(first)), asked.toString());
        assertEquals(2, ItemService.deductedIn(firstInvoice).size(), "still deducted in the first one");
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
