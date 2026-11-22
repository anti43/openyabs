package mpv5.globals;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.DefaultComboBoxModel;
import mpv5.i18n.LanguageManager;
import mpv5.utils.models.MPComboBoxModelItem;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for global {@link CompanyInfo} storage and country dropdown
 * matching ({@link LanguageManager#countryIndex}).
 */
class CompanyInfoAndCountryTest {

    @Test
    void companyInfoReadsAndWritesGloballyAcrossUsers() {
        GlobalSettings.removeAll();
        try {
            CompanyInfo.setProperty("business", "Muster GmbH");
            CompanyInfo.setProperty("country", "DE");

            assertEquals("Muster GmbH", CompanyInfo.getProperty("business"));
            assertEquals("Muster GmbH", CompanyInfo.getProperty("companyinfo.business"));
            assertEquals("DE", CompanyInfo.getProperty("country"));
            assertEquals("Muster GmbH", CompanyInfo.getProperties().getProperty("business"));
        } finally {
            GlobalSettings.removeAll();
        }
    }

    @Test
    void countryIndexMatchesNameAlpha2AndNumericIsoInsteadOfFallingBackToFirstEntry() {
        DefaultComboBoxModel<MPComboBoxModelItem> model = new DefaultComboBoxModel<MPComboBoxModelItem>();
        model.addElement(new MPComboBoxModelItem("4", "Afghanistan"));
        model.addElement(new MPComboBoxModelItem("40", "\u00d6sterreich"));
        model.addElement(new MPComboBoxModelItem("276", "Deutschland"));
        model.addElement(new MPComboBoxModelItem("756", "Schweiz"));

        assertEquals(2, LanguageManager.countryIndex("276", model));
        assertEquals(2, LanguageManager.countryIndex("DE", model));
        assertEquals(2, LanguageManager.countryIndex("Deutschland", model));
        assertEquals(2, LanguageManager.countryIndex("Germany", model));
        assertEquals(1, LanguageManager.countryIndex("AT", model));
        assertEquals(1, LanguageManager.countryIndex("Austria", model));
        assertEquals(3, LanguageManager.countryIndex("CH", model));
        assertEquals(3, LanguageManager.countryIndex("Switzerland", model));
    }
}
