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

    @Test
    void detailLineJoinsPositionsWithQuantityAndMeasure() {
        String line = ContactService.detailLine(Arrays.asList(
                new Object[]{12.0, "h", "Web design", ""},
                new Object[]{1.0, "", "<p>Hosting <b>Business</b></p>", ""},
                new Object[]{2.0, "", "", "Printer cartridge"},
                new Object[]{0.0, "", "Shipping &amp; handling", null}));
        assertEquals("12 h Web design \u00b7 Hosting Business \u00b7 2 Printer cartridge \u00b7 Shipping & handling", line);
    }

    @Test
    void detailLineSkipsEmptyPositionsAndIsLimited() {
        assertEquals("", ContactService.detailLine(java.util.Collections.singletonList(new Object[]{1.0, "", null, "null"})));
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("word ");
        }
        Object[] p = {1.0, "", longText.toString(), ""};
        String line = ContactService.detailLine(Arrays.asList(p, p, p, p, p, p));
        assertTrue(line.length() <= ContactService.MAX_DETAIL_LENGTH, line);
        assertTrue(line.endsWith("\u2026"), line);
    }
}
