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

    /**
     * Loads a language file and collects the keys that occur more than once
     * (java.util.Properties silently keeps the last one)
     */
    private static java.util.Properties load(String resource, java.util.List<String> duplicates) throws Exception {
        java.util.Properties p = new java.util.Properties() {
            private static final long serialVersionUID = 1L;

            @Override
            public synchronized Object put(Object key, Object value) {
                if (containsKey(key)) {
                    duplicates.add(String.valueOf(key));
                }
                return super.put(key, value);
            }
        };
        try (InputStream in = LanguageFilesTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            p.load(in);
        }
        return p;
    }

    private static final String EN = "/languages/Panels.properties";
    private static final String DE = "/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties";

    @Test
    void bothFilesHaveTheSameKeysOnce() throws Exception {
        java.util.List<String> dupEn = new java.util.ArrayList<String>();
        java.util.List<String> dupDe = new java.util.ArrayList<String>();
        java.util.Properties en = load(EN, dupEn);
        java.util.Properties de = load(DE, dupDe);
        org.junit.jupiter.api.Assertions.assertEquals(java.util.Collections.emptyList(), dupEn, "keys twice in the English file");
        org.junit.jupiter.api.Assertions.assertEquals(java.util.Collections.emptyList(), dupDe, "keys twice in the German file");
        java.util.Set<String> onlyEn = new java.util.TreeSet<String>(en.stringPropertyNames());
        onlyEn.removeAll(de.stringPropertyNames());
        java.util.Set<String> onlyDe = new java.util.TreeSet<String>(de.stringPropertyNames());
        onlyDe.removeAll(en.stringPropertyNames());
        //the German file names its version in its first line
        onlyDe.remove("FILE_VERSION");
        org.junit.jupiter.api.Assertions.assertEquals(java.util.Collections.emptySet(), onlyEn, "missing in the German file");
        //keys only in German are often the lines of a text with line breaks not written as \\n
        org.junit.jupiter.api.Assertions.assertEquals(java.util.Collections.emptySet(), onlyDe, "only in the German file");
        for (String key : en.stringPropertyNames()) {
            boolean enEmpty = en.getProperty(key).trim().isEmpty();
            boolean deEmpty = de.getProperty(key).trim().isEmpty();
            assertTrue(enEmpty == deEmpty, key + ": empty in one language only");
        }
    }

    @Test
    void placeholdersAndLineBreaksMatch() throws Exception {
        java.util.Properties en = load(EN, new java.util.ArrayList<String>());
        java.util.Properties de = load(DE, new java.util.ArrayList<String>());
        java.util.regex.Pattern ph = java.util.regex.Pattern.compile("%(\\d+\\$)?[sd]|\\{\\d+\\}");
        for (String key : en.stringPropertyNames()) {
            String e = en.getProperty(key);
            String d = de.getProperty(key);
            if (d == null) {
                continue;
            }
            org.junit.jupiter.api.Assertions.assertEquals(placeholders(ph, e), placeholders(ph, d), key + ": placeholders differ");
            //a text with line breaks in English is not cut after its first line in German
            assertTrue(!e.trim().contains("\n") || d.trim().contains("\n"), key + ": the German text has no line break");
        }
    }

    private static java.util.List<String> placeholders(java.util.regex.Pattern ph, String s) {
        java.util.List<String> l = new java.util.ArrayList<String>();
        java.util.regex.Matcher m = ph.matcher(s);
        while (m.find()) {
            l.add(m.group());
        }
        java.util.Collections.sort(l);
        return l;
    }

    @Test
    void everyMessageHasAnEnglishText() throws Exception {
        java.util.Properties en = load(EN, new java.util.ArrayList<String>());
        java.util.List<String> missing = new java.util.ArrayList<String>();
        for (mpv5.globals.Messages m : mpv5.globals.Messages.values()) {
            //the start message is built in, not translated
            if (m != mpv5.globals.Messages.START_MESSAGE && !en.containsKey(m.name())) {
                missing.add(m.name());
            }
        }
        org.junit.jupiter.api.Assertions.assertEquals(java.util.Collections.emptyList(), missing, "Messages without a text in Panels.properties");
    }
}
