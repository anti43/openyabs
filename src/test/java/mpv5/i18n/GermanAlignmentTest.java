package mpv5.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Properties;
import mpv5.globals.Constants;
import mpv5.globals.Headers;
import org.junit.jupiter.api.Test;

class GermanAlignmentTest {

    @Test
    void productDefaultHeaderMatchesQueryColumnOrder() {
        // Context.DEFAULT_PRODUCT_SEARCH is "ids, cnumber, cname, description"
        // Index 1 = Number, Index 2 = Name
        assertEquals("Number", Headers.PRODUCT_DEFAULT.getValue(1));
        assertEquals("Name", Headers.PRODUCT_DEFAULT.getValue(2));
    }

    @Test
    void allHeaderColumnCountsMatchBetweenEnglishAndGerman() throws Exception {
        Properties de = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties")) {
            assertNotNull(in, "German properties file missing");
            de.load(in);
        }

        for (Headers h : Headers.values()) {
            int count = h.getKeys().length;
            for (int i = 0; i < count; i++) {
                String key = h.name() + "." + i;
                assertTrue(de.containsKey(key), "Missing key in German properties: " + key);
            }
        }
    }

    @Test
    void contactDetailsHasLandAndIbanInGermanProperties() throws Exception {
        Properties de = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties")) {
            de.load(in);
        }
        assertEquals("Land", de.getProperty("CONTACT_DETAILS.8"));
        assertEquals("Haupttelefon", de.getProperty("CONTACT_DETAILS.9"));
        assertEquals("IBAN", de.getProperty("CONTACT_DETAILS.18"));
    }

    @Test
    void legacyContactDetailsShiftRecovery() {
        // Simulate legacy unshifted header where index 8 was Haupttelefon
        String[] legacy = new String[]{
            "Interne ID", "Kundennummer", "Titel", "Vorname", "Name",
            "Straße", "PLZ", "Ort", "Haupttelefon", "Fax",
            "Mobiltelefon", "Telefon Büro", "Firma", "Mailadresse", "Webseite",
            "Notizen", "USt-IdNr./Steuernr.", "IBAN", "Extra"
        };
        Headers.CONTACT_DETAILS.setValue(legacy);

        String[] recovered = Headers.CONTACT_DETAILS.getValue();
        assertEquals(19, recovered.length);
        assertEquals("Land", recovered[8], "Legacy index 8 should be recovered to Land/Country");
        assertEquals("Haupttelefon", recovered[9], "Haupttelefon should be shifted to index 9");
        assertEquals("Fax", recovered[10], "Fax should be shifted to index 10");
        assertEquals("IBAN", recovered[18], "IBAN should be at index 18");
    }

    @Test
    void fallbackReturnsBundledGermanProperty() {
        System.setProperty("user.language", "de");
        String de = LanguageManager.fallback("CONTACT_DETAILS.8");
        assertEquals("Land", de);
    }
}
