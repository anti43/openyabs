package mpv5.i18n;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GermanNameTest {

    @Test
    void recognisesGermanPacks() {
        for (String n : new String[]{"de", "DE-Yabs-2.0.properties (Tue Nov 25)", "language-de", "Deutsch", "German",
            "Yabs-1.2-beta7-Sprachdatei-DEU.txt", "1508740910373_Yabs-1.2.7.2_DEU", "yabs_de.properties", "Sprachdatei"}) {
            assertTrue(LanguageManager.isGermanName(n), n);
        }
        for (String n : new String[]{"EN-Yabs-2.0.properties", "French", "Yabs-1.2-FRA", "demo", "Dänisch-DK", "buildin_en"}) {
            assertFalse(LanguageManager.isGermanName(n), n);
        }
    }
}
