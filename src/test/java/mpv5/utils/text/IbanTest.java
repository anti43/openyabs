package mpv5.utils.text;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IbanTest {

    @Test
    void acceptsValidIbans() {
        assertTrue(Iban.isValid("DE02120300000000202051"));
        assertTrue(Iban.isValid("DE02 1203 0000 0000 2020 51"));
        assertTrue(Iban.isValid("de02-1203-0000-0000-2020-51"));
        assertTrue(Iban.isValid("AT611904300234573201"));
        assertTrue(Iban.isValid("CH9300762011623852957"));
        assertTrue(Iban.isValid("GB29NWBK60161331926819"));
        assertTrue(Iban.isValid("NL91ABNA0417164300"));
    }

    @Test
    void rejectsInvalidIbans() {
        assertFalse(Iban.isValid(null));
        assertFalse(Iban.isValid(""));
        assertFalse(Iban.isValid("1234567890"), "account number");
        assertFalse(Iban.isValid("DE03120300000000202051"), "check digits");
        assertFalse(Iban.isValid("DE0212030000000020205"), "length");
        assertFalse(Iban.isValid("XX02120300000000202051"), "country");
        assertFalse(Iban.isValid("BYLADEM1001"), "BIC");
    }

    @Test
    void validatesBic() {
        assertTrue(Iban.isValidBic("BYLADEM1001"));
        assertTrue(Iban.isValidBic("COBADEFF"));
        assertTrue(Iban.isValidBic("cobadeff xxx"));
        assertFalse(Iban.isValidBic("DE02120300000000202051"));
        assertFalse(Iban.isValidBic("COBAXXFF"), "country");
        assertFalse(Iban.isValidBic("COBADEF"));
        assertFalse(Iban.isValidBic(null));
    }

    @Test
    void formats() {
        assertEquals("DE02 1203 0000 0000 2020 51", Iban.format("de02120300000000202051"));
    }
}
