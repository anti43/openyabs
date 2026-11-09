package mpv5.pos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Arrays;
import mpv5.utils.numberformat.InvoiceMath;
import org.junit.jupiter.api.Test;

class PosServiceTest {

    @Test
    void grossPriceAndTotalsLikeTheInvoice() {
        PosService.Article coffee = new PosService.Article(1, "Coffee", "", "", new BigDecimal("2.52"), new BigDecimal("19"), "");
        PosService.Article cake = new PosService.Article(2, "Cake", "", "", new BigDecimal("3.27"), new BigDecimal("7"), "");
        assertEquals(new BigDecimal("3.00"), coffee.gross);
        assertEquals(new BigDecimal("3.50"), cake.gross);
        InvoiceMath.Totals t = PosService.totals(Arrays.asList(new PosService.Line(coffee, new BigDecimal("2")), new PosService.Line(cake, BigDecimal.ONE)));
        assertEquals(new BigDecimal("8.31"), t.net);
        assertEquals(new BigDecimal("9.50"), t.gross);
    }

    @Test
    void amounts() {
        assertEquals(new BigDecimal("12.50"), PosService.amount("12,5"));
        assertEquals(new BigDecimal("20.00"), PosService.amount(" 20 "));
        assertNull(PosService.amount(""));
        assertNull(PosService.amount("abc"));
    }

    @Test
    void jsonStrings() {
        assertEquals("\"a\\\"b\\\\c\\n\\u003cx\"", PosServer.q("a\"b\\c\n<x"));
    }
}
