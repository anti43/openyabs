package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), expected + " <> " + actual);
    }

    @Test
    void salePriceAddsTheMarkup() {
        assertAmount("30", ProductService.salePrice(new BigDecimal("20"), new BigDecimal("50")));
        assertAmount("29.9", ProductService.salePrice(new BigDecimal("18.4"), new BigDecimal("62.5")));
        assertAmount("20", ProductService.salePrice(new BigDecimal("20"), null));
        assertAmount("0", ProductService.salePrice(null, new BigDecimal("10")));
    }

    @Test
    void markupFromCostAndSalePrice() {
        assertAmount("62.5", ProductService.markup(new BigDecimal("18.40"), new BigDecimal("29.90")));
        assertAmount("-10", ProductService.markup(new BigDecimal("100"), new BigDecimal("90")));
        assertNull(ProductService.markup(BigDecimal.ZERO, new BigDecimal("10")));
        assertNull(ProductService.markup(null, new BigDecimal("10")));
    }

    @Test
    void grossWithTheTaxFactor() {
        assertAmount("35.581", ProductService.gross(new BigDecimal("29.9"), new BigDecimal("1.19")));
        assertAmount("10", ProductService.gross(new BigDecimal("10"), (BigDecimal) null));
        assertAmount("0", ProductService.gross(null, new BigDecimal("1.07")));
    }

    @Test
    void usageRowShowsCompanyAndPersonAndPlainText() {
        Object[] row = ProductService.usageRow(new Object[]{"7", "R-1", null, "Smith", "ACME Ltd", "2.0", "pcs", "59.8",
            "<p>Ink &amp; toner</p>", "P-1"});
        assertEquals(7, row[0]);
        assertEquals("ACME Ltd (Smith)", row[3]);
        assertEquals(new BigDecimal("2.0"), row[4]);
        assertEquals(new BigDecimal("59.8"), row[5]);
        assertEquals("Ink & toner", row[6]);
        //no description: the name of the position
        assertEquals("P-1", ProductService.usageRow(new Object[]{"7", "", null, "Smith", "", "1", "", "1", null, "P-1"})[6]);
        assertEquals("Smith", ProductService.usageRow(new Object[]{"7", "", null, "Smith", "Smith", "1", "", "1", "", ""})[3]);
    }

    @Test
    void totalsCountDocumentsOnce() {
        MPTableModel m = new MPTableModel(new Object[][]{
            {1, "R-1", null, "A", 2.0, 20.0, ""},
            {1, "R-1", null, "A", 3.0, 30.0, ""},
            {2, "R-2", null, "B", 5.0, 45.5, ""}}, new String[]{"id", "n", "d", "c", "q", "v", "x"});
        BigDecimal[] t = ProductService.usageTotals(m);
        assertEquals(2, t[0].intValue());
        assertEquals(0, new BigDecimal("10").compareTo(t[1]));
        assertEquals(0, new BigDecimal("95.5").compareTo(t[2]));
    }

    @Test
    void usageNetIsAfterDiscount() {
        //2 x 30.00 with 10 % discount: 54.00, not the 60.00 before discount
        Object[] row = ProductService.usageRow(new Object[]{"7", "R-1", null, "Smith", "", "2", "pcs", "60",
            "", "P-1", "30", "10"});
        assertEquals(0, new BigDecimal("54.00").compareTo((BigDecimal) row[5]));
    }
}
