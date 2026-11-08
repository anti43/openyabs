package mpv5.i18n;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import mpv5.globals.Constants;
import org.junit.jupiter.api.Test;

/**
 * The language files must be plain ASCII (umlauts as ü): they are read
 * with different encodings, raw UTF-8 showed as "Verkn?pfte"
 */
class LanguageFilesTest {

    private static void assertAscii(String resource) throws Exception {
        try (InputStream in = LanguageFilesTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            byte[] b = in.readAllBytes();
            int line = 1;
            for (byte x : b) {
                if (x == '\n') {
                    line++;
                }
                assertTrue(x >= 0, resource + ": line " + line + " has a non-ASCII character, write it as \\uXXXX");
            }
        }
    }

    @Test
    void germanFileOfThisVersionExistsAndIsAscii() throws Exception {
        assertAscii("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties");
    }

    @Test
    void englishFileIsAscii() throws Exception {
        assertAscii("/languages/Panels.properties");
    }

    @Test
    void repairFixesMisreadUtf8AndKeepsCorrectText() {
        //UTF-8 bytes read as ISO-8859-1 (old files): repaired
        org.junit.jupiter.api.Assertions.assertEquals("Zur\u00fcck", ResourceBundleUtf8.repair("Zur\u00c3\u00bcck"));
        //correct umlauts stay (they became "gem??" before)
        org.junit.jupiter.api.Assertions.assertEquals("gem\u00e4\u00df", ResourceBundleUtf8.repair("gem\u00e4\u00df"));
        org.junit.jupiter.api.Assertions.assertEquals("Stra\u00dfe", ResourceBundleUtf8.repair("Stra\u00dfe"));
        org.junit.jupiter.api.Assertions.assertEquals("plain", ResourceBundleUtf8.repair("plain"));
        org.junit.jupiter.api.Assertions.assertEquals("\u20ac 5", ResourceBundleUtf8.repair("\u20ac 5"));
    }

    @Test
    void germanTextsHaveRealUmlauts() throws Exception {
        java.util.Properties p = new java.util.Properties();
        try (InputStream in = LanguageFilesTest.class.getResourceAsStream("/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties")) {
            p.load(in);
        }
        org.junit.jupiter.api.Assertions.assertEquals("Zur\u00fcck", ResourceBundleUtf8.repair(p.getProperty("Wizard.back.text")));
        org.junit.jupiter.api.Assertions.assertEquals("Weiter", p.getProperty("Wizard.next.text"));
        assertTrue(ResourceBundleUtf8.repair(p.getProperty("MASSEXPORT_EINVOICE")).contains("gem\u00e4\u00df"));
        for (String key : p.stringPropertyNames()) {
            String v = p.getProperty(key);
            //no UTF-8 bytes read as Latin-1 left (e.g. "\u00c3\u00bc" for "\u00fc")
            assertTrue(v.equals(ResourceBundleUtf8.repair(v)), key + " is double encoded: " + v);
            assertTrue(v.indexOf('\u00c3') < 0, key + " contains a broken umlaut: " + v);
        }
    }
}
