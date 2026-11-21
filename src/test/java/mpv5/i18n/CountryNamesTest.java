package mpv5.i18n;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import mpv5.utils.models.MPComboBoxModelItem;
import org.junit.jupiter.api.Test;

/**
 * Country names in the language of the user, and stored countries found in the
 * localized list (the tax rates saved the first country, Austria, when their
 * country was not found)
 */
class CountryNamesTest {

    @Test
    void germanUsersSeeGermanNames() {
        assertEquals("Deutschland", LanguageManager.localizedCountryName("276", "Germany", Locale.GERMANY));
        assertEquals("Österreich", LanguageManager.localizedCountryName("40", "Austria", Locale.GERMANY));
        //without code: by the name
        assertEquals("Frankreich", LanguageManager.localizedCountryName(null, "France", Locale.GERMANY));
        //unknown: as stored
        assertEquals("Atlantis", LanguageManager.localizedCountryName(null, "Atlantis", Locale.GERMANY));
        assertEquals("", LanguageManager.localizedCountryName(null, "", Locale.GERMANY));
    }

    @Test
    void englishUsersSeeTheStoredNames() {
        assertEquals("Czech Republic", LanguageManager.localizedCountryName("203", "Czech Republic", Locale.UK));
    }

    @Test
    void sameCountryInAnyLanguageOrCode() {
        assertTrue(LanguageManager.sameCountry("Germany", "Deutschland"));
        assertTrue(LanguageManager.sameCountry("276", "Deutschland"));
        assertTrue(LanguageManager.sameCountry("DE", "Germany"));
        assertFalse(LanguageManager.sameCountry("Austria", "Germany"));
        //empty is no country (toCountryCode would give DE)
        assertFalse(LanguageManager.sameCountry("", "Germany"));
        assertFalse(LanguageManager.sameCountry(null, "Germany"));
    }

    @Test
    void storedCountryIsFoundInTheLocalizedList() {
        DefaultComboBoxModel<MPComboBoxModelItem> m = new DefaultComboBoxModel<MPComboBoxModelItem>(new MPComboBoxModelItem[]{
            new MPComboBoxModelItem("40", "Österreich"), new MPComboBoxModelItem("250", "Frankreich"),
            new MPComboBoxModelItem("276", "Deutschland")});
        assertEquals(2, LanguageManager.countryIndex("Germany", m));
        assertEquals(2, LanguageManager.countryIndex("Deutschland", m));
        assertEquals(2, LanguageManager.countryIndex("276", m));
        assertEquals(1, LanguageManager.countryIndex("France", m));
        assertEquals(0, LanguageManager.countryIndex("Atlantis", m));
    }
}
