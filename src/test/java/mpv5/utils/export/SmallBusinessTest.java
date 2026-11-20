package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.globals.Messages;
import org.junit.jupiter.api.Test;

class SmallBusinessTest {

    private static Map<String, String> company(String country) {
        Map<String, String> c = new HashMap<String, String>();
        c.put("country", country);
        return c;
    }

    @Test
    void offWithoutTheSetting() {
        Map<String, String> c = company("Deutschland");
        assertFalse(SmallBusiness.isEnabled(c::get));
        assertEquals("", SmallBusiness.note(c::get), "no note without the exemption");
        c.put(SmallBusiness.KEY_ENABLED, "false");
        c.put(SmallBusiness.KEY_NOTE, "Kein Ausweis der USt.");
        assertEquals("", SmallBusiness.note(c::get));
    }

    @Test
    void noteOfTheCountryOrOwnNote() {
        Map<String, String> c = company("Deutschland");
        c.put(SmallBusiness.KEY_ENABLED, "true");
        assertTrue(SmallBusiness.note(c::get).contains("§ 19 UStG"), SmallBusiness.note(c::get));
        c.put(SmallBusiness.KEY_KU_ID, "DE123456789-EX");
        assertTrue(SmallBusiness.note(c::get).endsWith("KU-IdNr.: DE123456789-EX"), SmallBusiness.note(c::get));

        c.put("country", "Austria");
        assertTrue(SmallBusiness.note(c::get).contains("§ 6 Abs. 1 Z 27 UStG"), SmallBusiness.note(c::get));
        c.put("country", "France");
        c.remove(SmallBusiness.KEY_KU_ID);
        assertEquals(Messages.SMALLBUSINESS_DEFAULT_NOTE.toString(), SmallBusiness.note(c::get));

        c.put(SmallBusiness.KEY_NOTE, "  TVA non applicable, art. 293 B du CGI  ");
        assertEquals("TVA non applicable, art. 293 B du CGI", SmallBusiness.note(c::get));
    }

    @Test
    void germanLimits() {
        assertTrue(SmallBusiness.checkLimits("DE", new BigDecimal("20000"), new BigDecimal("50000"), new BigDecimal("1000")).isEmpty());
        assertTrue(SmallBusiness.checkLimits("DE", new BigDecimal("25000"), new BigDecimal("99000"), new BigDecimal("1000")).isEmpty(),
                "exactly at the limits is fine");

        List<String> prior = SmallBusiness.checkLimits("DE", new BigDecimal("25000.01"), BigDecimal.ZERO, BigDecimal.TEN);
        assertEquals(1, prior.size(), prior.toString());

        //the invoice which exceeds 100.000 EUR: regular taxation starts with it
        List<String> exceeds = SmallBusiness.checkLimits("DE", BigDecimal.ZERO, new BigDecimal("99000"), new BigDecimal("1000.01"));
        assertEquals(1, exceeds.size(), exceeds.toString());
        assertTrue(exceeds.get(0).startsWith(Messages.SMALLBUSINESS_THIS_INVOICE_EXCEEDS.toString().substring(0, 20)), exceeds.get(0));

        List<String> already = SmallBusiness.checkLimits("DE", BigDecimal.ZERO, new BigDecimal("100000.01"), BigDecimal.TEN);
        assertTrue(already.get(0).startsWith(Messages.SMALLBUSINESS_CURRENT_YEAR_EXCEEDED.toString().substring(0, 20)), already.get(0));

        assertTrue(SmallBusiness.checkLimits("FR", new BigDecimal("1000000"), new BigDecimal("1000000"), BigDecimal.TEN).isEmpty(),
                "no limits known for other countries");
    }
}
