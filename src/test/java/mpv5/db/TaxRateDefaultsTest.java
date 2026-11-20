package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Tax;
import mpv5.handler.FirstStartSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The tax rates of the installation (and of older databases) get the VAT
 * category S / E and the country Germany
 */
class TaxRateDefaultsTest {

    @TempDir
    Path tmp;

    @Test
    void ratesAreCompleted() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        assertFalse(Tax.all().isEmpty());
        for (Tax t : Tax.all()) {
            boolean zero = t.__getTaxvalue().signum() == 0;
            assertEquals(zero ? "E" : "S", t.__getCategorycode(), t.__getCname());
            assertEquals(FirstStartSetup.TAX_COUNTRY, t.__getCountry(), t.__getCname());
        }

        //a rate of another country without country: the category, but no country
        Tax at = (Tax) DatabaseObject.getObject(Context.getTaxes());
        at.setCname("AT 20%");
        at.setIdentifier("AT 20%");
        at.setTaxvalue(new BigDecimal("20"));
        at.setDateadded(new Date());
        assertTrue(at.save(true));
        //a rate of 0 % chosen as reverse charge keeps its category
        Tax rc = (Tax) DatabaseObject.getObject(Context.getTaxes());
        rc.setCname("Reverse Charge");
        rc.setIdentifier("Reverse Charge");
        rc.setTaxvalue(BigDecimal.ZERO);
        rc.setCategorycode("AE");
        rc.setCountry("France");
        rc.setDateadded(new Date());
        assertTrue(rc.save(true));
        FirstStartSetup.completeTaxRates();
        Tax at2 = (Tax) DatabaseObject.getObject(Context.getTaxes(), at.__getIDS(), true);
        assertEquals("S", at2.__getCategorycode());
        assertEquals("", at2.__getCountry() == null ? "" : at2.__getCountry());
        Tax rc2 = (Tax) DatabaseObject.getObject(Context.getTaxes(), rc.__getIDS(), true);
        assertEquals("AE", rc2.__getCategorycode());
        assertEquals("France", rc2.__getCountry());
    }
}
