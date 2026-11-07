package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.utils.numberformat.FormatNumber;
import org.junit.jupiter.api.Test;

/**
 * Money calculations: exact decimals, staggered prices, documents as
 * positions
 */
class MoneyFixesTest {

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private static SubItem position(String quantity, String price, String discount, String tax) {
        SubItem s = new SubItem();
        s.setQuantityvalue(new BigDecimal(quantity));
        s.setExternalvalue(new BigDecimal(price));
        s.setDiscount(new BigDecimal(discount));
        s.setTaxpercentvalue(new BigDecimal(tax));
        return s;
    }

    @Test
    void parsingIsExactAndUsesTheCurrentLocale() {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals(new BigDecimal("1234.56"), FormatNumber.parseDezimal("1.234,56"));
            assertEquals(new BigDecimal("0.1"), FormatNumber.parseDezimal("0,1"));
            //more digits than a double holds
            assertEquals(new BigDecimal("12345678901234567.89"), FormatNumber.parseDezimal("12345678901234567,89"));
            assertEquals("1.234,57", FormatNumber.formatDezimal(new BigDecimal("1234.565")));
            Locale.setDefault(Locale.US);
            //the format follows a locale change (it used to be fixed at class load)
            assertEquals(new BigDecimal("1234.56"), FormatNumber.parseDezimal("1,234.56"));
            assertEquals("1,234.57", FormatNumber.formatDezimal(new BigDecimal("1234.565")));
        } finally {
            Locale.setDefault(old);
        }
    }

    @Test
    void floatsAreNotWidenedToBinaryNoise() {
        assertEquals(new BigDecimal("0.1"), FormatNumber.getBigDecimal(0.1f));
        assertEquals(new BigDecimal("0.1"), FormatNumber.getBigDecimal(0.1d));
        //2.675 is 2.67499999... as double: rounded from its decimal string
        assertAmount("2.68", FormatNumber.round(2.675d));
    }

    @Test
    void staggeredPriceTakesTheHighestReachedTierInAnyOrder() {
        List<BigDecimal[]> tiers = new ArrayList<BigDecimal[]>();
        //stored in this order: the last one used to win
        tiers.add(new BigDecimal[]{new BigDecimal("100"), new BigDecimal("8.00")});
        tiers.add(new BigDecimal[]{new BigDecimal("10"), new BigDecimal("9.00")});
        BigDecimal normal = new BigDecimal("10.00");
        assertAmount("10.00", Product.priceFor(tiers, new BigDecimal("9"), normal));
        //the minimum itself counts
        assertAmount("9.00", Product.priceFor(tiers, new BigDecimal("10"), normal));
        assertAmount("9.00", Product.priceFor(tiers, new BigDecimal("99"), normal));
        assertAmount("8.00", Product.priceFor(tiers, new BigDecimal("150"), normal));
        //a free tier is a price, not "no tier"
        tiers.add(new BigDecimal[]{new BigDecimal("1000"), BigDecimal.ZERO});
        assertAmount("0", Product.priceFor(tiers, new BigDecimal("1000"), normal));
    }

    @Test
    void documentAsPositionsKeepsNetVatAndGrossPerRate() {
        //100.00 at 19 % with 10 % discount, 50.00 at 7 %
        SubItem[] positions = {position("2", "50", "10", "19"), position("1", "50", "0", "7")};
        List<SubItem> ps = ItemService.asPositions(positions, "A-1", new BigDecimal("150"),
                new BigDecimal("10"), new BigDecimal("20.60"), "Order A-1");
        assertEquals(2, ps.size());
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (SubItem s : ps) {
            assertAmount("1", s.__getQuantityvalue());
            net = net.add(s.__getExternalvalue());
            tax = tax.add(mpv5.utils.numberformat.InvoiceMath.lineTax(BigDecimal.ONE, s.__getExternalvalue(), null, s.__getTaxpercentvalue()));
            assertTrue(s.__getDescription().startsWith("Order A-1 ("), s.__getDescription());
        }
        //net after discount: 90 + 50; VAT 17.10 + 3.50
        assertAmount("140.00", net);
        assertAmount("20.60", tax);
        assertAmount("90.00", ps.get(ps.get(0).__getTaxpercentvalue().intValue() == 19 ? 0 : 1).__getExternalvalue());
    }

    @Test
    void documentWithOneRateIsOnePositionWithThatRate() {
        SubItem[] positions = {position("3", "10", "0", "19"), position("0", "0", "0", "0")};
        List<SubItem> ps = ItemService.asPositions(positions, "A-2", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "Order A-2");
        assertEquals(1, ps.size());
        assertAmount("30.00", ps.get(0).__getExternalvalue());
        assertAmount("19", ps.get(0).__getTaxpercentvalue());
        assertEquals("Order A-2", ps.get(0).__getDescription());
    }

    @Test
    void copiedPositionsKeepQuantityDiscountAndRate() {
        SubItem src = position("12", "59.80", "5", "19");
        src.setMeasure("h");
        src.setOriginalproductsids(42);
        SubItem c = ItemService.copyPosition(src);
        assertAmount("12", c.__getQuantityvalue());
        assertAmount("59.80", c.__getExternalvalue());
        assertAmount("5", c.__getDiscount());
        assertAmount("19", c.__getTaxpercentvalue());
        assertEquals("h", c.__getMeasure());
        assertEquals(42, c.__getOriginalproductsids());
        assertEquals(-1, c.__getIDS().intValue());
    }

    @Test
    void productListMarkupIsRoundedToCents() {
        assertEquals(11.0, mpv5.db.objects.ProductlistSubItem.withPercent("10", 10), 0d);
        assertEquals(9.0, mpv5.db.objects.ProductlistSubItem.withPercent(10.0, -10), 0d);
        assertEquals(1.12, mpv5.db.objects.ProductlistSubItem.withPercent("0.99", 13), 0d);
    }

    @Test
    void optionalColumnFactorIsItsNumberOrOne() {
        Locale old = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertAmount("3", SubItem.optionalFactor("3"));
            assertAmount("2.5", SubItem.optionalFactor("2,5"));
            assertAmount("3", SubItem.optionalFactor("3 Monate"));
            assertAmount("1", SubItem.optionalFactor(""));
            assertAmount("1", SubItem.optionalFactor(null));
            assertAmount("1", SubItem.optionalFactor("Option B"));
            assertAmount("4", SubItem.optionalFactor(4));
        } finally {
            Locale.setDefault(old);
        }
    }

    @Test
    void staggeredPriceFollowsTheQuantityUnlessTypedByHand() {
        List<BigDecimal[]> tiers = new ArrayList<BigDecimal[]>();
        tiers.add(new BigDecimal[]{new BigDecimal("10"), new BigDecimal("9.00")});
        BigDecimal normal = new BigDecimal("10.00");
        //normal price, quantity reaches the tier: tier price
        assertAmount("9.00", ItemService.staggeredPrice(tiers, normal, new BigDecimal("10"), new BigDecimal("10.00")));
        //tier price, quantity below again: normal price
        assertAmount("10.00", ItemService.staggeredPrice(tiers, normal, new BigDecimal("2"), new BigDecimal("9.00")));
        //already right: nothing to do
        assertEquals(null, ItemService.staggeredPrice(tiers, normal, new BigDecimal("12"), new BigDecimal("9")));
        //a price of the user's own stays
        assertEquals(null, ItemService.staggeredPrice(tiers, normal, new BigDecimal("12"), new BigDecimal("8.50")));
        //no tiers: the price is never touched
        assertEquals(null, ItemService.staggeredPrice(new ArrayList<BigDecimal[]>(), normal, new BigDecimal("12"), new BigDecimal("10")));
    }
}
