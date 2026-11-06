package mpv5.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Company settings with "[" must not be resolved endlessly
 */
class FormFieldsHandlerTest {

    @Test
    void bankAccountsJsonIsNotResolved() {
        String json = "[{\"name\":\"Sparkasse\",\"iban\":\"DE02120300000000202051\"}]";
        assertTrue(FormFieldsHandler.isData(mpv5.utils.export.BankAccount.KEY_ACCOUNTS));
        //obj null: would fail if it was resolved
        assertSame(json, FormFieldsHandler.resolve(mpv5.utils.export.BankAccount.KEY_ACCOUNTS, json, null));
        assertEquals("Musterbank", FormFieldsHandler.resolve("bankname", "Musterbank", null));
    }
}
