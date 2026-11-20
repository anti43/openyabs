package mpv5.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import mpv5.db.objects.Item;
import mpv5.utils.date.DateConverter;
import org.junit.jupiter.api.Test;

/**
 * The retention rules of {@link Privacy} (#220)
 */
class PrivacyRulesTest {

    private static Date date(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, m - 1, d, 12, 0, 0);
        return c.getTime();
    }

    @Test
    void periodsStartAtTheEndOfTheCalendarYear() {
        //an invoice of 15.03.2026: 8 years from 31.12.2026
        assertEquals("31.12.2034", DateConverter.getDefDateString(Privacy.keepUntil(date(2026, 3, 15), 8)));
        assertEquals("31.12.2034", DateConverter.getDefDateString(Privacy.keepUntil(date(2026, 12, 31), 8)));
        assertEquals("31.12.2032", DateConverter.getDefDateString(Privacy.keepUntil(date(2026, 1, 1), 6)));
    }

    @Test
    void whatMustBeKept() {
        //issued invoices, credit notes, part payment and deposit invoices: 8 years
        for (int t : new int[]{Item.TYPE_INVOICE, Item.TYPE_CREDIT, Item.TYPE_PART_PAYMENT, Item.TYPE_DEPOSIT}) {
            assertEquals(Privacy.YEARS_BOOKING_RECORDS, Privacy.retentionYears(t, true, false));
            assertEquals(0, Privacy.retentionYears(t, false, false), "a draft is no invoice");
        }
        //business letters: 6 years
        for (int t : new int[]{Item.TYPE_ORDER, Item.TYPE_DELIVERY_NOTE, Item.TYPE_ORDER_CONFIRMATION}) {
            assertEquals(Privacy.YEARS_BUSINESS_LETTERS, Privacy.retentionYears(t, false, false));
        }
        assertEquals(Privacy.YEARS_BUSINESS_LETTERS, Privacy.retentionYears(Item.TYPE_OFFER, false, true), "accepted offer");
        assertEquals(0, Privacy.retentionYears(Item.TYPE_OFFER, true, false), "offer not accepted");
    }

    @Test
    void issuedAndOpen() {
        assertFalse(Privacy.isIssued(Item.STATUS_IN_PROGRESS, false, false));
        assertTrue(Privacy.isIssued(Item.STATUS_IN_PROGRESS, true, false), "a PDF / e-invoice was written");
        assertTrue(Privacy.isIssued(Item.STATUS_QUEUED, false, true), "a payment was booked");
        assertTrue(Privacy.isIssued(Item.STATUS_FINISHED, false, false));

        assertTrue(Privacy.isOpen(Item.TYPE_INVOICE, Item.STATUS_FINISHED, true));
        assertFalse(Privacy.isOpen(Item.TYPE_INVOICE, Item.STATUS_PAID, true));
        assertFalse(Privacy.isOpen(Item.TYPE_INVOICE, Item.STATUS_CANCELLED, true));
        assertFalse(Privacy.isOpen(Item.TYPE_INVOICE, Item.STATUS_IN_PROGRESS, false), "a draft");
        assertFalse(Privacy.isOpen(Item.TYPE_CREDIT, Item.STATUS_FINISHED, true));
    }
}
