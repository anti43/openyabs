package mpv5.utils.importers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Fakturama2StockTest {

    @Test
    void fakturamaKeepsTheStockWithTheOppositeSign() {
        assertEquals(new BigDecimal("10"), Fakturama2Importer.stockOf(new BigDecimal("-10")));
        assertEquals(new BigDecimal("-3"), Fakturama2Importer.stockOf(new BigDecimal("3")));
        assertNull(Fakturama2Importer.stockOf(BigDecimal.ZERO));
        assertNull(Fakturama2Importer.stockOf(null));
    }
}
