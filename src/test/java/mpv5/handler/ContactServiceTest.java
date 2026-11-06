package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import mpv5.handler.ContactService.AccountOption;
import mpv5.handler.ContactService.Related;
import org.junit.jupiter.api.Test;

class ContactServiceTest {

    @Test
    void companyTakesTheNameAndTheOtherWayRound() {
        assertArrayEquals(new String[]{"ACME", "ACME"}, ContactService.companyAndName(true, "", "ACME"));
        assertArrayEquals(new String[]{"ACME", "ACME"}, ContactService.companyAndName(true, "ACME", null));
        assertArrayEquals(new String[]{"ACME", "Smith"}, ContactService.companyAndName(true, "ACME", "Smith"));
    }

    @Test
    void personKeepsCompanyAndNameAsEntered() {
        assertArrayEquals(new String[]{"", "Smith"}, ContactService.companyAndName(false, null, "Smith"));
        assertArrayEquals(new String[]{"ACME", ""}, ContactService.companyAndName(false, "ACME", ""));
    }

    @Test
    void nothingEnteredCannotBeSaved() {
        assertArrayEquals(new String[]{"", ""}, ContactService.companyAndName(true, null, null));
    }

    @Test
    void relatedRecordsByRole() {
        for (Related r : Related.values()) {
            assertEquals(r != Related.PRODUCTS, r.isForCustomers(), r.name());
        }
        assertTrue(Related.INVOICES.isItem());
        assertTrue(Related.CREDITS.isItem());
        assertFalse(Related.ACTIVITIES.isItem());
        assertFalse(Related.PRODUCTS.isItem());
    }

    @Test
    void selectedAccountFallsBackToDefault() {
        List<AccountOption> options = Arrays.asList(
                new AccountOption("", "Default"),
                new AccountOption("DE02120300000000202051", "DKB"),
                new AccountOption("DE89370400440532013000", "Commerzbank"));
        assertEquals(2, ContactService.selectedAccount(options, "DE89370400440532013000"));
        assertEquals(0, ContactService.selectedAccount(options, ""));
        assertEquals(0, ContactService.selectedAccount(options, "DE00000000000000000000"));
    }
}
