package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import org.junit.jupiter.api.Test;

class JournalCalcTest {

    private static Entry e(int type, int status, String net, String tax) {
        Entry e = new Entry();
        e.type = type;
        e.status = status;
        e.net = new BigDecimal(net);
        e.tax = new BigDecimal(tax);
        e.gross = e.net.add(e.tax);
        return e;
    }

    @Test
    void ratesSnapToTheGermanRates() {
        assertEquals(19, JournalCalc.rateOf(new BigDecimal("100.00"), new BigDecimal("19.00")));
        assertEquals(7, JournalCalc.rateOf(new BigDecimal("100.00"), new BigDecimal("7.01")));
        assertEquals(0, JournalCalc.rateOf(new BigDecimal("100.00"), BigDecimal.ZERO));
        assertEquals(JournalCalc.MIXED, JournalCalc.rateOf(new BigDecimal("200.00"), new BigDecimal("26.00")));
    }

    @Test
    void cashBasis() {
        Entry paid = e(Constants.TYPE_INVOICE, Item.STATUS_PAID, "100.00", "19.00");
        Entry open = e(Constants.TYPE_INVOICE, Item.STATUS_IN_PROGRESS, "50.00", "9.50");
        Entry cancelled = e(Constants.TYPE_INVOICE, Item.STATUS_CANCELLED, "70.00", "13.30");
        Entry credit = e(Constants.TYPE_CREDIT, Item.STATUS_PAID, "10.00", "1.90");
        Entry expense = e(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "20.00", "1.40");
        Entry unpaidExpense = e(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_UNPAID, "30.00", "5.70");
        Entry offer = e(Constants.TYPE_OFFER, Item.STATUS_PAID, "999.00", "0.00");
        List<Entry> l = Arrays.asList(paid, open, cancelled, credit, expense, unpaidExpense, offer);
        Totals t = JournalCalc.compute(l, Collections.<Integer>emptySet());
        assertTrue(paid.counted);
        assertFalse(open.counted);
        assertFalse(cancelled.counted);
        assertFalse(offer.counted);
        assertFalse(unpaidExpense.counted);
        //the credit note reduces the income and the output VAT
        assertEquals(new BigDecimal("90.00"), t.incomeNet);
        assertEquals(new BigDecimal("17.10"), t.incomeTax);
        assertEquals(new BigDecimal("20.00"), t.expenseNet);
        assertEquals(new BigDecimal("1.40"), t.expenseTax);
        assertEquals(new BigDecimal("70.00"), t.profitNet());
        assertEquals(new BigDecimal("-10.00"), credit.effect);
        assertEquals(new BigDecimal("59.50"), t.openReceivables);
        assertEquals(new BigDecimal("35.70"), t.openPayables);
        assertEquals(new BigDecimal("90.00"), t.incomeByRate.get(19)[0]);
        assertEquals(new BigDecimal("20.00"), t.expensesByRate.get(7)[0]);
    }

    @Test
    void invoicePaidInPartsCountsThroughItsPayments() {
        Entry inv = e(Constants.TYPE_INVOICE, Item.STATUS_PAID, "100.00", "19.00");
        inv.itemId = 5;
        Entry pay = e(Constants.TYPE_PAYMENT, Item.STATUS_PAID, "50.00", "9.50");
        pay.itemId = 5;
        Totals t = JournalCalc.compute(Arrays.asList(inv, pay), Collections.singleton(5));
        assertFalse(inv.counted);
        assertTrue(pay.counted);
        assertEquals(new BigDecimal("50.00"), t.incomeNet);
        //the volume counts the invoice, not the payment
        assertEquals(new BigDecimal("100.00"), t.volumeNet);
    }
}
