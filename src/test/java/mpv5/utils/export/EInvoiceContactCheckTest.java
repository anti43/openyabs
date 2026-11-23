package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import mpv5.utils.export.EInvoiceContactCheck.Field;
import mpv5.utils.export.EInvoiceContactCheck.Result;
import org.junit.jupiter.api.Test;

public class EInvoiceContactCheckTest {

    private static Result check(String country, String vatId, String iban) {
        return EInvoiceContactCheck.check("Erika Muster", "Muster GmbH", "Weg 1", "12345", "Berlin", country,
                "erika@example.org", vatId, iban, "DE");
    }

    @Test
    public void completeDomesticContactIsOk() {
        assertTrue(check("276", "", "").isOk());
        assertTrue(check("", "DE123456789", "DE02 1203 0000 0000 2020 51").isOk());
    }

    @Test
    public void missingFieldsAreErrors() {
        Result r = EInvoiceContactCheck.check("", " ", "", null, "", "Atlantis", "", "", "", "DE");
        assertTrue(r.hasErrors());
        assertEquals(EnumSet.of(Field.NAME, Field.STREET, Field.ZIP, Field.CITY, Field.COUNTRY, Field.EMAIL), r.getErrors());
        assertEquals(6, r.getMessages().size());
    }

    @Test
    public void invalidIbanIsAnError() {
        Result r = check("276", "", "DE02 1203 0000 0000 2020 52");
        assertEquals(EnumSet.of(Field.IBAN), r.getErrors());
    }

    @Test
    public void taxNumberIsAHint() {
        Result r = check("276", "12/345/67890", "");
        assertFalse(r.hasErrors());
        assertEquals(EnumSet.of(Field.VATID), r.getHints());
        assertEquals(1, r.getMessages().size());
    }

    @Test
    public void foreignContactNeedsVatId() {
        //40 = Austria in the YaBS country list
        Result r = check("40", "", "");
        assertFalse(r.hasErrors());
        assertEquals(EnumSet.of(Field.VATID), r.getHints());
        assertTrue(check("40", "ATU12345678", "").isOk());
        assertTrue(check("AT", "ATU12345678", "").isOk());
        //a tax number does not help: both reasons
        assertEquals(2, check("40", "12/345/67890", "").getMessages().size());
    }

    @Test
    public void peppolIdSatisfiesBuyerElectronicAddressAndRejectsInvalid() {
        Result valid = EInvoiceContactCheck.check("Erika Muster", "Muster GmbH", "Weg 1", "12345", "Berlin", "276",
                "", "DE123456789", "", "DE", "0204:04011000-12345-34");
        assertTrue(valid.isOk(), valid.getMessages().toString());

        Result invalid = EInvoiceContactCheck.check("Erika Muster", "Muster GmbH", "Weg 1", "12345", "Berlin", "276",
                "erika@example.org", "DE123456789", "", "DE", "invalid-peppol");
        assertTrue(invalid.hasErrors());
        assertEquals(EnumSet.of(Field.EMAIL), invalid.getErrors());
    }
}
