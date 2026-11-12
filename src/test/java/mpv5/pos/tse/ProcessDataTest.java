package mpv5.pos.tse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProcessDataTest {

    @Test
    void kassenbelegHasTheTotalsPerRateInTheirSlots() {
        Map<BigDecimal, BigDecimal> m = new LinkedHashMap<BigDecimal, BigDecimal>();
        m.put(new BigDecimal("19"), new BigDecimal("11.9"));
        m.put(new BigDecimal("7"), new BigDecimal("2.14"));
        m.put(BigDecimal.ZERO, new BigDecimal("1"));
        assertEquals("Beleg^11.90_2.14_0.00_0.00_1.00^15.04:Bar",
                ProcessData.kassenbeleg(m, new BigDecimal("15.04"), true));
    }

    @Test
    void averageAndOldRatesGoToTheirSlots() {
        Map<BigDecimal, BigDecimal> m = new LinkedHashMap<BigDecimal, BigDecimal>();
        m.put(new BigDecimal("10.7"), BigDecimal.ONE);
        m.put(new BigDecimal("5.5"), BigDecimal.ONE);
        m.put(new BigDecimal("16"), BigDecimal.ONE);
        m.put(new BigDecimal("5"), BigDecimal.ONE);
        assertEquals("Beleg^1.00_1.00_1.00_1.00_0.00^4.00:Unbar",
                ProcessData.kassenbeleg(m, new BigDecimal("4"), false));
    }

    @Test
    void qrContainsAllFieldsInOrder() {
        Instant s = Instant.parse("2026-11-12T09:15:02.123Z");
        Instant e = Instant.parse("2026-11-12T09:15:03.456Z");
        Tse.Finished f = new Tse.Finished(7, s, e, 42, "SIG", "SER", "ALG", "utcTime", "KEY");
        assertEquals("V0;REG-1;Kassenbeleg-V1;Beleg^1.00_0.00_0.00_0.00_0.00^1.00:Bar;7;42;2026-11-12T09:15:02.123Z;2026-11-12T09:15:03.456Z;ALG;utcTime;SIG;KEY",
                ProcessData.qr("REG-1", Tse.KASSENBELEG, "Beleg^1.00_0.00_0.00_0.00_0.00^1.00:Bar", f));
    }
}
