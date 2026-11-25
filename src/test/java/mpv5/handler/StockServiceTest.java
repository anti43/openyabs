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
    void entriesFromAPastedList() {
        List<StockService.Entry> e = StockService.fromText("A-1, a-1, 4006381333931,A-1", ",", false);
        assertEquals(3, e.size());
        assertEquals(0, new BigDecimal("2").compareTo(e.get(0).quantity));
        e = StockService.fromText("A-1, a-1, 4006381333931,A-1", ",", true);
        assertEquals(2, e.size());
        assertEquals("A-1", e.get(0).code);
        assertEquals(0, new BigDecimal("3").compareTo(e.get(0).quantity));
        assertNull(e.get(0).row);
    }

    @Test
    void entriesFromACsvFile() {
        CsvImport.Data d = CsvImport.read("Bezeichnung;EAN;Bestand\nRing;4006381333931;3\nKette;;2,5\nRing;4006381333931;1\n;;4\nUhr;;viele\n"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String[] head = d.rows.get(0);
        Field[] m = CsvImport.guess(head, Kind.PRODUCTS);
        //the EAN identifies before the name; the stock column is the quantity
        assertEquals(1, StockService.codeColumn(m));
        assertEquals(2, StockService.quantityColumn(m));
        List<Integer> skipped = new java.util.ArrayList<Integer>();
        List<StockService.Entry> e = StockService.fromCsv(d, 1, 2, m, skipped, true);
        //"Kette" has no EAN, the empty row and the row with "viele" are skipped
        assertEquals(Arrays.asList(3, 5, 6), skipped);
        assertEquals(1, e.size());
        assertEquals(0, new BigDecimal("4").compareTo(e.get(0).quantity));
        //by name, no quantity column: 1 per row
        e = StockService.fromCsv(d, 0, -1, m, skipped = new java.util.ArrayList<Integer>(), true);
        assertEquals(3, e.size());
        assertEquals(0, new BigDecimal("2").compareTo(e.get(0).quantity));
        assertEquals(Arrays.asList(5), skipped);
        assertEquals(0, new BigDecimal("2.5").compareTo(StockService.quantity("2,5")));
        assertEquals(0, BigDecimal.ONE.compareTo(StockService.quantity(" ")));
    }

    @Test
    void sameCode() {
        assertTrue(StockService.sameCode("Ring", "RING", true));
        assertFalse(StockService.sameCode("Ring", "RING", false));
        assertTrue(StockService.sameCode("Ring", "Ring", false));
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
