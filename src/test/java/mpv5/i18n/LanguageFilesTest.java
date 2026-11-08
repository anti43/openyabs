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
}
