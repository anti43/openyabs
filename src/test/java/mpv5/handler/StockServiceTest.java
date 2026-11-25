package mpv5.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import mpv5.utils.csv.CsvImport;
import mpv5.utils.csv.CsvImport.Field;
import mpv5.utils.csv.CsvImport.Kind;
import org.junit.jupiter.api.Test;

class StockServiceTest {

    @Test
    void splitsPastedLists() {
        assertEquals(Arrays.asList("4006381333931", "A-100", "Ring"), StockService.split("4006381333931, A-100 ,Ring", ","));
        assertEquals(Arrays.asList("a", "b c", "d"), StockService.split("a;b c;\n d", ";"));
        assertEquals(Arrays.asList("a", "b", "c"), StockService.split("  a  b\tc ", " "));
        assertEquals(Arrays.asList("Ring gold", "Kette"), StockService.split("Ring gold\r\n\r\nKette\n", "\n"));
        assertTrue(StockService.split(null, ",").isEmpty());
    }

    @Test
    void guessesTheSeparator() {
        assertEquals(";", StockService.guessSeparator("a;b,c"));
        assertEquals(",", StockService.guessSeparator("a, b"));
        assertEquals("\n", StockService.guessSeparator("Ring gold\nKette"));
        assertEquals(" ", StockService.guessSeparator("123 456"));
    }

    private static Field[] mapping(String... headers) {
        return CsvImport.guess(headers, Kind.PRODUCTS);
    }

    @Test
    void codeAndQuantityOfACsvRow() {
        Field[] m = mapping("Bezeichnung", "EAN", "Bestand");
        //the EAN comes before the name
        assertEquals("4006381333931", StockService.code(new String[]{"Ring", "4006381333931", "3"}, m));
        assertEquals("Ring", StockService.code(new String[]{"Ring", "", "3"}, m));
        assertEquals(0, new BigDecimal("3").compareTo(StockService.quantity(new String[]{"Ring", "", "3"}, m)));
        assertEquals(0, BigDecimal.ONE.compareTo(StockService.quantity(new String[]{"Ring", "", ""}, m)));
        assertTrue(StockService.check(new String[]{"Ring", "", "2,5"}, m).isEmpty());
        assertFalse(StockService.check(new String[]{"", "", "2"}, m).isEmpty());
        assertFalse(StockService.check(new String[]{"Ring", "", "viele"}, m).isEmpty());
        assertNull(StockService.checkMapping(m));
        assertNotNull(StockService.checkMapping(mapping("Bestand", "Preis")));
        //no stock column: 1 per row
        assertEquals(0, BigDecimal.ONE.compareTo(StockService.quantity(new String[]{"A-1"}, mapping("Artikelnummer"))));
    }

    @Test
    void stocktakingExport() {
        List<StockService.Count> c = Arrays.asList(
                new StockService.Count("A-1", "Ring; gold", "4006381333931", "Stk", new BigDecimal("5"), new BigDecimal("4")),
                new StockService.Count("A-2", "Kette", "", "Stk", new BigDecimal("2"), null));
        String csv = StockService.inventoryCsv(c, ';');
        String[] lines = csv.split("\r\n");
        assertEquals(3, lines.length, csv);
        assertEquals("A-1;\"Ring; gold\";4006381333931;Stk;5;4;-1", lines[1]);
        assertEquals("A-2;Kette;;Stk;2;;", lines[2]);
        String html = StockService.inventoryHtml(c, "Inventur <1>");
        assertTrue(html.contains("<h1>Inventur &lt;1&gt;</h1>"), html);
        assertTrue(html.contains("<tr class=\"d\"><td>A-1</td>"), html);
        assertTrue(html.contains("<tr class=\"u\"><td>A-2</td>"), html);
        assertEquals(0, new BigDecimal("-1").compareTo(c.get(0).difference()));
        assertNull(c.get(1).difference());
    }
}
