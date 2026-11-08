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
}
