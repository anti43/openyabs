package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EInvoiceDeliveryTest {

    @Test
    void defaultIsFirstEnumOption() {
        assertEquals(EInvoiceDelivery.DEFAULT, EInvoiceDelivery.values()[0]);
    }

    @Test
    void parsingDefaultsGracefully() {
        assertEquals(EInvoiceDelivery.DEFAULT, EInvoiceDelivery.parse(null));
        assertEquals(EInvoiceDelivery.DEFAULT, EInvoiceDelivery.parse(""));
        assertEquals(EInvoiceDelivery.DEFAULT, EInvoiceDelivery.parse("UNKNOWN_VALUE"));
        assertEquals(EInvoiceDelivery.DEFAULT, EInvoiceDelivery.parse("DEFAULT"));
        assertEquals(EInvoiceDelivery.PDF, EInvoiceDelivery.parse("PDF"));
        assertEquals(EInvoiceDelivery.ZUGFERD, EInvoiceDelivery.parse("ZUGFERD"));
        assertEquals(EInvoiceDelivery.XRECHNUNG, EInvoiceDelivery.parse("XRECHNUNG"));
        assertEquals(EInvoiceDelivery.PDF_AND_XRECHNUNG, EInvoiceDelivery.parse("PDF_AND_XRECHNUNG"));
    }

    @Test
    void resolveRespectsGlobalDefaultSetting() {
        boolean original = EInvoiceDelivery.isZugpferdDefault();
        try {
            // Disabled: DEFAULT resolves to normal PDF
            EInvoiceDelivery.setZugpferdDefault(false);
            assertFalse(EInvoiceDelivery.isZugpferdDefault());
            assertEquals(EInvoiceDelivery.PDF, EInvoiceDelivery.DEFAULT.resolve());
            assertEquals(EInvoiceDelivery.ZUGFERD, EInvoiceDelivery.ZUGFERD.resolve());

            // Enabled: DEFAULT resolves to ZUGFeRD
            EInvoiceDelivery.setZugpferdDefault(true);
            assertTrue(EInvoiceDelivery.isZugpferdDefault());
            assertEquals(EInvoiceDelivery.ZUGFERD, EInvoiceDelivery.DEFAULT.resolve());

            // Customer explicit override takes precedence over global default
            assertEquals(EInvoiceDelivery.PDF, EInvoiceDelivery.PDF.resolve());
            assertEquals(EInvoiceDelivery.XRECHNUNG, EInvoiceDelivery.XRECHNUNG.resolve());
            assertEquals(EInvoiceDelivery.PDF_AND_XRECHNUNG, EInvoiceDelivery.PDF_AND_XRECHNUNG.resolve());
        } finally {
            EInvoiceDelivery.setZugpferdDefault(original);
        }
    }
}
