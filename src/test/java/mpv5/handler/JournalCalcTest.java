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
    void storedRatesAreBookedUnderTheRatesOfTheControlPanel() {
        java.util.List<BigDecimal> configured = Arrays.asList(bd("19.00"), bd("7.00"), bd("0"));
        assertEquals("19", JournalCalc.normalizeRate(bd("18.96"), configured).toPlainString());
        assertEquals("19", JournalCalc.normalizeRate(bd("19.04"), configured).toPlainString());
        assertEquals("7", JournalCalc.normalizeRate(bd("7.0000001"), configured).toPlainString());
        //not configured (any more): rounded to one decimal place, own line
        assertEquals("16", JournalCalc.normalizeRate(bd("15.99"), configured).toPlainString());
        assertEquals("16", JournalCalc.normalizeRate(bd("16.01"), configured).toPlainString());
        assertEquals("5.5", JournalCalc.normalizeRate(bd("5.5"), configured).toPlainString());
        assertEquals("18.8", JournalCalc.normalizeRate(bd("18.8"), configured).toPlainString());
        assertEquals(null, JournalCalc.normalizeRate(null, configured));
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
    void openOffersAndOrdersAreSummedButNotPartOfTheResult() {
        Entry offer = e(Constants.TYPE_OFFER, Item.STATUS_IN_PROGRESS, "100.00", "19.00");
        Entry accepted = e(Constants.TYPE_OFFER, Item.STATUS_FINISHED, "50.00", "9.50");
        Entry order = e(Constants.TYPE_ORDER, Item.STATUS_QUEUED, "30.00", "5.70");
        Entry invoice = e(Constants.TYPE_INVOICE, Item.STATUS_IN_PROGRESS, "10.00", "1.90");
        Totals t = JournalCalc.compute(Arrays.asList(offer, accepted, order, invoice), Collections.<Integer>emptySet());
        assertEquals(new BigDecimal("100.00"), t.openOffersNet);
        assertEquals(new BigDecimal("119.00"), t.openOffersGross);
        assertEquals(new BigDecimal("30.00"), t.openOrdersNet);
        assertFalse(offer.counted);
        //offers and orders are not part of the document volume
        assertEquals(new BigDecimal("10.00"), t.volumeNet);
        assertEquals(BigDecimal.ZERO, t.incomeNet);
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

    @Test
    void splitDropsEmptyZeroRateBucketWhenNonZeroRatesExist() {
        java.util.Map<BigDecimal, BigDecimal[]> lines = new java.util.TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        lines.put(bd("0"), new BigDecimal[]{bd("0.00"), bd("0.00")});
        lines.put(bd("19"), new BigDecimal[]{bd("52.00"), bd("9.88")});
        java.util.Map<BigDecimal, BigDecimal[]> r = JournalCalc.split(bd("52.00"), bd("9.88"), lines);
        assertEquals(1, r.size());
        assertFalse(r.containsKey(bd("0")));
        assertEquals(bd("52.00"), r.get(bd("19"))[0]);
        assertEquals(bd("9.88"), r.get(bd("19"))[1]);

        //a true zero-amount invoice keeps its rate
        java.util.Map<BigDecimal, BigDecimal[]> zeroOnly = new java.util.TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        zeroOnly.put(bd("0"), new BigDecimal[]{bd("0.00"), bd("0.00")});
        java.util.Map<BigDecimal, BigDecimal[]> rZero = JournalCalc.split(bd("0.00"), bd("0.00"), zeroOnly);
        assertEquals(1, rZero.size());
        assertTrue(rZero.containsKey(bd("0")));

        //a real 0% line with non-zero net is kept alongside 19%
        java.util.Map<BigDecimal, BigDecimal[]> mixed = new java.util.TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
        mixed.put(bd("0"), new BigDecimal[]{bd("10.00"), bd("0.00")});
        mixed.put(bd("19"), new BigDecimal[]{bd("52.00"), bd("9.88")});
        java.util.Map<BigDecimal, BigDecimal[]> rMixed = JournalCalc.split(bd("62.00"), bd("9.88"), mixed);
        assertEquals(2, rMixed.size());
        assertEquals(bd("10.00"), rMixed.get(bd("0"))[0]);
        assertEquals(bd("52.00"), rMixed.get(bd("19"))[0]);
    }

    @Test
    void csvByRatePreservesEffectSignForNegativeAndMixedEntries() {
        Entry negInv = e(Constants.TYPE_INVOICE, Item.STATUS_PAID, "-50.00", "-9.50");
        negInv.number = "INV-NEG";
        negInv.rates.put(bd("19"), new BigDecimal[]{bd("-50.00"), bd("-9.50")});

        Entry negExp = e(Constants.TYPE_EXPENSE, JournalCalc.EXPENSE_PAID, "-20.00", "-3.80");
        negExp.number = "EXP-REFUND";
        negExp.rates.put(bd("19"), new BigDecimal[]{bd("-20.00"), bd("-3.80")});

        Entry zeroSumInv = e(Constants.TYPE_INVOICE, Item.STATUS_PAID, "0.00", "19.00");
        zeroSumInv.number = "INV-MIX";
        zeroSumInv.rates.put(bd("0"), new BigDecimal[]{bd("-100.00"), bd("0.00")});
        zeroSumInv.rates.put(bd("19"), new BigDecimal[]{bd("100.00"), bd("19.00")});

        Entry unpaid = e(Constants.TYPE_INVOICE, Item.STATUS_IN_PROGRESS, "52.00", "9.88");
        unpaid.number = "INV-OPEN";
        unpaid.rates.put(bd("19"), new BigDecimal[]{bd("52.00"), bd("9.88")});

        List<Entry> entries = Arrays.asList(negInv, negExp, zeroSumInv, unpaid);
        Totals totals = JournalCalc.compute(entries, Collections.<Integer>emptySet());
        mpv5.utils.export.JournalAdviserPackage pkg = new mpv5.utils.export.JournalAdviserPackage(
                entries, totals, new java.util.Date(), new java.util.Date(), Collections.<String>emptyList(), Collections.<BigDecimal, String>emptyMap());
        String[] rows = pkg.csvByRate().split("\r\n");
        assertEquals(6, rows.length);
        //last column is Ergebnis netto
        assertTrue(rows[1].contains("\"INV-NEG\"") && rows[1].endsWith("\"-50,00\""), rows[1]);
        assertTrue(rows[2].contains("\"EXP-REFUND\"") && rows[2].endsWith("\"20,00\""), rows[2]);
        assertTrue(rows[3].contains("\"INV-MIX\"") && rows[3].endsWith("\"-100,00\""), rows[3]);
        assertTrue(rows[4].contains("\"INV-MIX\"") && rows[4].endsWith("\"100,00\""), rows[4]);
        assertTrue(rows[5].contains("\"INV-OPEN\"") && rows[5].endsWith("\"0,00\""), rows[5]);
    }

    @Test
    void overpaymentIsExcludedFromIncomeAndVat() {
        // Exact #218 scenario: 100.00 net + 19.00 VAT = 119.00 gross, 200.00 paid
        assertEquals(bd("81.00"), Payments.overpayment(bd("119.00"), bd("200.00")));
        assertEquals(bd("0.00"), Payments.overpayment(bd("119.00"), bd("100.00")));

        Payments.Allocation single = Payments.allocate(bd("119.00"), bd("0.00"), bd("200.00"));
        assertEquals(bd("119.00"), single.incomeGross);
        assertEquals(bd("81.00"), single.surplus);

        // Split payments across dates: 100.00 + 100.00 + 50.00 on a 119.00 invoice
        Payments.Allocation p1 = Payments.allocate(bd("119.00"), bd("0.00"), bd("100.00"));
        Payments.Allocation p2 = Payments.allocate(bd("119.00"), bd("100.00"), bd("100.00"));
        Payments.Allocation p3 = Payments.allocate(bd("119.00"), bd("200.00"), bd("50.00"));
        assertEquals(bd("100.00"), p1.incomeGross);
        assertEquals(bd("0.00"), p1.surplus);
        assertEquals(bd("19.00"), p2.incomeGross);
        assertEquals(bd("81.00"), p2.surplus);
        assertEquals(bd("0.00"), p3.incomeGross);
        assertEquals(bd("50.00"), p3.surplus);

        // JournalCalc totals: revenue & VAT capped at 100.00 / 19.00, surplus tracked separately
        Entry inv = e(Constants.TYPE_INVOICE, Item.STATUS_PAID, "100.00", "19.00");
        inv.itemId = 42;
        Entry pay = e(Constants.TYPE_PAYMENT, Item.STATUS_PAID, "100.00", "19.00");
        pay.itemId = 42;
        pay.overpayment = bd("81.00");
        Entry pureSurplus = e(Constants.TYPE_PAYMENT, Item.STATUS_PAID, "0.00", "0.00");
        pureSurplus.itemId = 42;
        pureSurplus.overpayment = bd("50.00");

        Totals totals = JournalCalc.compute(Arrays.asList(inv, pay, pureSurplus), Collections.singleton(42));
        assertFalse(inv.counted);
        assertTrue(pay.counted);
        assertFalse(pureSurplus.counted);
        assertEquals(bd("100.00"), totals.incomeNet);
        assertEquals(bd("19.00"), totals.incomeTax);
        assertEquals(bd("119.00"), totals.incomeGross);
        assertEquals(bd("131.00"), totals.overpayments);

        mpv5.utils.export.JournalAdviserPackage pkg = new mpv5.utils.export.JournalAdviserPackage(
                Arrays.asList(inv, pay, pureSurplus), totals, new java.util.Date(), new java.util.Date(),
                Collections.<String>emptyList(), Collections.<BigDecimal, String>emptyMap());
        String summary = pkg.summary(new mpv5.utils.export.JournalAdviserPackage.EInvoiceCheck());
        assertTrue(summary.contains("131,00"), summary);
    }
}
