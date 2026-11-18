package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import mpv5.db.objects.Item;
import org.junit.jupiter.api.Test;

class ItemServiceTest {

    @Test
    void billsArePayableDocuments() {
        assertTrue(ItemService.isBill(Item.TYPE_INVOICE));
        assertTrue(ItemService.isBill(Item.TYPE_DEPOSIT));
        assertTrue(ItemService.isBill(Item.TYPE_PART_PAYMENT));
        assertFalse(ItemService.isBill(Item.TYPE_OFFER));
        assertFalse(ItemService.isBill(Item.TYPE_ORDER));
        assertFalse(ItemService.isBill(Item.TYPE_CREDIT));
    }

    @Test
    void billsCannotBePausedOthersCannotBePaid() {
        assertEquals(Collections.singletonList(Item.STATUS_PAUSED), ItemService.statusesToSkip(Item.TYPE_INVOICE, false));
        assertEquals(Collections.singletonList(Item.STATUS_PAID), ItemService.statusesToSkip(Item.TYPE_OFFER, false));
        //a loaded credit note is treated like a bill, a new one not (as in the classic view)
        assertEquals(Collections.singletonList(Item.STATUS_PAUSED), ItemService.statusesToSkip(Item.TYPE_CREDIT, true));
        assertEquals(Collections.singletonList(Item.STATUS_PAID), ItemService.statusesToSkip(Item.TYPE_CREDIT, false));
    }

    @Test
    void comparisonRowShowsUsedShareOfTheOrder() {
        ItemService.CompareRow r = new ItemService.CompareRow(new mpv5.db.objects.Product());
        r.ordered = new BigDecimal("4");
        r.used = new BigDecimal("3");
        Object[] row = r.toArray();
        assertEquals(new BigDecimal("4"), row[0]);
        assertEquals(new BigDecimal("3"), row[1]);
        assertEquals(75f, (Float) row[4], 0.001f);
        assertEquals(0.75f, (Float) row[5], 0.001f);
        //nothing ordered: no division by zero
        ItemService.CompareRow none = new ItemService.CompareRow(new mpv5.db.objects.Product());
        none.used = BigDecimal.ONE;
        assertEquals(0f, (Float) none.toArray()[5], 0.001f);
    }

    @Test
    void ensureFinishedAndEndDateSetsStatusAndEnddate() {
        Item item = new Item();
        item.setIntstatus(Item.STATUS_QUEUED);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2026, java.util.Calendar.NOVEMBER, 1, 10, 0, 0);
        Date workedOn = cal.getTime();
        item.setDatetodo(workedOn);
        item.setDateend(null);

        ItemService.ensureFinishedAndEndDate(item);

        assertEquals(Item.STATUS_FINISHED, item.__getIntstatus());
        assertEquals(workedOn, item.__getDateend());

        // Stale enddate (older than workedOn) must be updated to workedOn
        cal.set(2026, java.util.Calendar.OCTOBER, 20, 10, 0, 0);
        Date oldEnd = cal.getTime();
        item.setDateend(oldEnd);

        ItemService.ensureFinishedAndEndDate(item);

        assertEquals(Item.STATUS_FINISHED, item.__getIntstatus());
        assertEquals(workedOn, item.__getDateend());

        // Valid future enddate (after workedOn) is preserved
        cal.set(2026, java.util.Calendar.NOVEMBER, 15, 10, 0, 0);
        Date futureEnd = cal.getTime();
        item.setDateend(futureEnd);

        ItemService.ensureFinishedAndEndDate(item);

        assertEquals(Item.STATUS_FINISHED, item.__getIntstatus());
        assertEquals(futureEnd, item.__getDateend());
    }
}
