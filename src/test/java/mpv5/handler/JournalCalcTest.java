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

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    @Test
    void distributeKeepsEveryCent() {
        java.util.List<BigDecimal> p = JournalCalc.distribute(bd("100.00"), Arrays.asList(bd("1"), bd("1"), bd("1")));
        assertEquals(Arrays.asList(bd("33.34"), bd("33.33"), bd("33.33")), p);
        p = JournalCalc.distribute(bd("-10.00"), Arrays.asList(bd("1"), bd("2")));
        assertEquals(bd("-10.00"), p.get(0).add(p.get(1)));
        assertEquals(Arrays.asList(bd("5.00"), bd("0.00")), JournalCalc.distribute(bd("5.00"), Arrays.asList(bd("0"), bd("0"))));
    }

    @Test
    void mixedDocumentIsSplitByItsItemsToTheStoredAmounts() {
        java.util.Map<BigDecimal, BigDecimal[]> lines = new java.util.TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        lines.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});
        lines.put(bd("7"), new BigDecimal[]{bd("100.00"), bd("7.00")});
        //10 % discount on the document: the stored amounts are 180.00 / 23.40
        java.util.Map<BigDecimal, BigDecimal[]> r = JournalCalc.split(bd("180.00"), bd("23.40"), lines);
        assertEquals(bd("90.00"), r.get(bd("19"))[0]);
        assertEquals(bd("17.10"), r.get(bd("19"))[1]);
        assertEquals(bd("90.00"), r.get(bd("7"))[0]);
        assertEquals(bd("6.30"), r.get(bd("7"))[1]);
        assertTrue(JournalCalc.split(bd("1"), bd("0"), null).isEmpty());
        assertEquals(JournalCalc.rateKey(bd("19.00")), JournalCalc.rateKey(bd("19")));
        assertEquals("16", JournalCalc.rateKey(bd("16.000")).toPlainString());
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
        paid.rates.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});
        credit.rates.put(bd("19"), new BigDecimal[]{bd("10.00"), bd("1.90")});
        expense.rates.put(bd("7"), new BigDecimal[]{bd("20.00"), bd("1.40")});
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
        assertEquals(new BigDecimal("90.00"), t.incomeByRate.get(bd("19"))[0]);
        assertEquals(new BigDecimal("20.00"), t.expensesByRate.get(bd("7"))[0]);
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
