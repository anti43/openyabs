package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mpv5.db.objects.SubItem;
import mpv5.utils.numberformat.InvoiceMath;
import org.junit.jupiter.api.Test;
import org.mustangproject.Invoice;
import org.mustangproject.ZUGFeRD.TransactionCalculator;
import org.mustangproject.ZUGFeRD.VATAmount;

class InvoiceMathTest {

    static InvoiceMath.Line l(String q, String p, String d, String t) {
        return new InvoiceMath.Line(new BigDecimal(q), new BigDecimal(p), new BigDecimal(d), new BigDecimal(t));
    }

    /**
     * The reported case: the same service (110.40 net) gave 20.97 or 20.98 VAT
     * depending on how it was split into lines
     */
    @Test
    void sameServiceSameTotalsWhateverTheSplit() {
        InvoiceMath.Totals one = InvoiceMath.totals(Arrays.asList(l("1", "110.40", "0", "19")));
        InvoiceMath.Totals split = InvoiceMath.totals(Arrays.asList(
                l("3", "12.533333", "0", "19"), l("2", "18.80", "0", "19"), l("1", "35.20", "0", "19")));
        assertEquals(new BigDecimal("110.40"), one.net);
        assertEquals(new BigDecimal("20.98"), one.tax);
        assertEquals(new BigDecimal("131.38"), one.gross);
        assertEquals(one.net, split.net);
        assertEquals(one.tax, split.tax);
        assertEquals(new BigDecimal("22.14"), InvoiceMath.totals(Arrays.asList(l("1", "116.50", "0", "19"))).tax,
                "116.50 x 19% = 22.135 -> 22.14 (double arithmetic gave 22.13)");
    }

    @Test
    void discountAndMixedRates() {
        InvoiceMath.Totals t = InvoiceMath.totals(Arrays.asList(
                l("2", "100", "10", "19"), l("1.5", "80", "0", "19"), l("1", "10", "0", "7")));
        assertEquals(new BigDecimal("330.00"), t.amount);
        assertEquals(new BigDecimal("20.00"), t.discount);
        assertEquals(new BigDecimal("310.00"), t.net);
        assertEquals(new BigDecimal("57.70"), t.tax);//300*19% + 10*7%
        assertEquals(new BigDecimal("367.70"), t.gross);
        assertEquals(new BigDecimal("2.85"), InvoiceMath.lineTax(new BigDecimal("1"), new BigDecimal("15"), BigDecimal.ZERO, new BigDecimal("19")));
    }

    /**
     * YaBS totals must equal the totals of the e-invoice, computed by Mustang
     */
    @Test
    void matchesTheEInvoice() {
        Random r = new Random(7);
        String[] prices = {"12.345678", "0.333333", "19.999", "7.123456", "100", "45.6789", "3.333333", "1.005", "29.95", "0.125", "8.403361", "36.798333"};
        String[] qtys = {"1", "3", "0.5", "1.25", "7", "2.333", "12", "0.75", "-1", "13.125"};
        String[] discs = {"0", "0", "0", "3", "12.5", "10", "2.5", "33.333"};
        String[] taxes = {"19", "19", "7", "0", "19.00"};
        for (int n = 0; n < 300; n++) {
            List<InvoiceMath.Line> lines = new ArrayList<InvoiceMath.Line>();
            List<SubItem> subs = new ArrayList<SubItem>();
            int count = 1 + r.nextInt(15);
            for (int i = 0; i < count; i++) {
                String q = qtys[r.nextInt(qtys.length)], p = prices[r.nextInt(prices.length)];
                String d = discs[r.nextInt(discs.length)], t = taxes[r.nextInt(taxes.length)];
                lines.add(l(q, p, d, t));
                subs.add(ZugpferdFileTest.line("L" + i, "", q, "Stk", p, t, d));
            }
            Map<String, String> c = ZugpferdFileTest.company();
            Invoice inv = ZugpferdFile.createInvoice(ZugpferdFileTest.invoice(), subs.toArray(new SubItem[0]),
                    ZugpferdFile.createSender(c::get), ZugpferdFileTest.buyer(), "ref", 0, null);
            TransactionCalculator tc = new TransactionCalculator(inv);
            InvoiceMath.Totals t = InvoiceMath.totals(lines);
            String ctx = "case " + n;
            assertEquals(0, tc.getTaxBasis().compareTo(t.net), ctx + " net " + tc.getTaxBasis() + " vs " + t.net);
            assertEquals(0, tc.getGrandTotal().compareTo(t.gross), ctx + " gross " + tc.getGrandTotal() + " vs " + t.gross);
            BigDecimal vat = BigDecimal.ZERO;
            for (VATAmount v : tc.getTaxDetails()) {
                vat = vat.add(InvoiceMath.round2(v.getCalculated()));//per rate, rounded
            }
            assertEquals(0, vat.compareTo(t.tax), ctx + " tax " + vat + " vs " + t.tax);
        }
    }
}
