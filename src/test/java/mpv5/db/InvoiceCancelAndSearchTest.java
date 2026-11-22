package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JLabel;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.handler.ItemService;
import mpv5.handler.JournalLoader;
import mpv5.handler.TemplateHandler;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.renderer.TableCellRendererForDatabaseObjects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies invoice cancellation without the GoBD plugin, journal filtering for
 * cancelled invoices and their cancellations, and date-only rendering in
 * document lists.
 */
class InvoiceCancelAndSearchTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void cancelCreatesCreditNoteAndJournalCancelledFilterFindsBoth() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);

        Contact customer = (Contact) DatabaseObject.getObject(Context.getContact());
        customer.setCname("Storno Kunde GmbH");
        customer.setisCustomer(true);
        customer.setDateadded(new Date());
        assertTrue(customer.save(true));

        Item invoice = createInvoice(customer, "Beratung", new BigDecimal("200.00"));
        MPTableModel positions = SubItem.toModel(invoice.getSubitems(), true);

        // Cancel invoice without GoBD plugin
        Item storno = ItemService.cancel(invoice, positions);
        assertNotNull(storno, "cancellation credit note should be created");
        assertTrue(storno.isExisting());
        assertEquals(Item.TYPE_CREDIT, storno.__getInttype());
        assertEquals(invoice.__getIDS(), storno.__getRefinvoiceids());
        assertTrue(storno.__getDescription().contains(invoice.__getCnumber()),
                "cancellation description should mention original invoice number");
        assertEquals(0, invoice.__getNetvalue().compareTo(storno.__getNetvalue()),
                "cancellation net value should match original invoice");

        Item reloadedInvoice = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        assertEquals(Item.STATUS_CANCELLED, reloadedInvoice.__getIntstatus(),
                "original invoice should be marked cancelled");

        // Cancelling an already-cancelled invoice is refused
        assertNull(ItemService.cancel(reloadedInvoice, positions),
                "already-cancelled invoice must not be cancelled a second time");

        // A regular credit note for a non-cancelled invoice must NOT appear under "cancelled"
        Item activeInvoice = createInvoice(customer, "Wartung", new BigDecimal("100.00"));
        Item normalCredit = ItemService.toInvoice(activeInvoice, Item.TYPE_CREDIT,
                SubItem.toModel(activeInvoice.getSubitems(), true), q -> false);
        assertNotNull(normalCredit);
        assertEquals(activeInvoice.__getIDS(), normalCredit.__getRefinvoiceids());

        // Journal filtered by STATUS_CANCELLED includes the cancelled invoice and its cancellation,
        // but excludes the active invoice and the normal credit note
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date from = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 3);
        Date to = cal.getTime();

        JournalLoader.Filter f = new JournalLoader.Filter();
        f.time = new vTimeframe(from, to);
        f.items.addAll(Arrays.asList(Context.getInvoice(), Context.getCredit()));
        f.includeInvoices = true;
        f.incomeStatus = JournalLoader.STATUS_CANCELLED;

        List<mpv5.handler.JournalCalc.Entry> entries = JournalLoader.load(f).entries;
        Set<Integer> ids = entries.stream()
                .filter(e -> e.obj instanceof Item)
                .map(e -> ((Item) e.obj).__getIDS())
                .collect(Collectors.toSet());

        assertTrue(ids.contains(invoice.__getIDS()), "cancelled invoice should be in cancelled filter");
        assertTrue(ids.contains(storno.__getIDS()), "cancellation credit note should be in cancelled filter");
        assertFalse(ids.contains(activeInvoice.__getIDS()), "active invoice must not be in cancelled filter");
        assertFalse(ids.contains(normalCredit.__getIDS()), "credit note for non-cancelled invoice must not be in cancelled filter");
    }

    @Test
    void databaseObjectCellRendererFormatsDatesWithoutTime() {
        TableCellRendererForDatabaseObjects renderer = new TableCellRendererForDatabaseObjects();
        Timestamp ts = Timestamp.valueOf("2026-11-21 19:37:22.645");
        JTable table = new JTable(new Object[][]{{ts}}, new Object[]{"Belegdatum"});

        Component c = renderer.getTableCellRendererComponent(table, ts, false, false, 0, 0);
        assertInstanceOf(JLabel.class, c);
        assertEquals(DateConverter.getDefDateString(ts), ((JLabel) c).getText(),
                "document date should be rendered without timestamp");
    }

    private static Item createInvoice(Contact customer, String posName, BigDecimal net) throws Exception {
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(customer.__getIDS());
        inv.setGroupsids(1);
        inv.setAccountsids(1);
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setIntstatus(Item.STATUS_QUEUED);
        inv.setDateadded(new Date());
        assertTrue(inv.save(true));

        SubItem s = new SubItem();
        s.setItemsids(inv.__getIDS());
        s.setCname(posName);
        s.setDescription(posName);
        s.setQuantityvalue(BigDecimal.ONE);
        s.setExternalvalue(net);
        s.setInternalvalue(net);
        s.setTaxpercentvalue(new BigDecimal("19"));
        s.setOrdernr(1);
        s.setDateadded(new Date());
        assertTrue(s.save(true));

        ItemService.updateTotals(inv);
        return (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
    }
}
