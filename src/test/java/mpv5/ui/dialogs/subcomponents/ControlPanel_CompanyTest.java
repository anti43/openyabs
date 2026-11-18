package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import javax.swing.JComboBox;
import mpv5.data.PropertyStore;
import org.junit.jupiter.api.Test;

class ControlPanel_CompanyTest {

    @Test
    void countryDropdownIsPresentAndPopulated() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Company panel = new ControlPanel_Company();
        JComboBox combo = panel.getCountryComboBox();
        assertNotNull(combo, "Country combo box must be initialized");
        assertTrue(combo.getItemCount() > 0, "Country combo box must have country options");
    }

    @Test
    void countrySelectionByIsoAndName() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Company panel = new ControlPanel_Company();

        PropertyStore store = new PropertyStore();
        store.addProperty("country", "40"); // Austria numeric ISO
        panel.setValues(store);
        assertEquals("Austria", panel.getSelectedCountry());

        store = new PropertyStore();
        store.addProperty("country", "CH"); // Switzerland ISO alpha-2
        panel.setValues(store);
        assertEquals("Switzerland", panel.getSelectedCountry());

        store = new PropertyStore();
        store.addProperty("country", "Germany");
        panel.setValues(store);
        assertEquals("Germany", panel.getSelectedCountry());
    }

    @Test
    void countrySelectionCustomFallback() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Company panel = new ControlPanel_Company();

        PropertyStore store = new PropertyStore();
        store.addProperty("country", "Utopia");
        panel.setValues(store);
        assertEquals("Utopia", panel.getSelectedCountry());
    }

    @Test
    void countryResetRestoresOldValue() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_Company panel = new ControlPanel_Company();

        PropertyStore store1 = new PropertyStore();
        store1.addProperty("country", "Germany");
        panel.setValues(store1);
        assertEquals("Germany", panel.getSelectedCountry());

        PropertyStore store2 = new PropertyStore();
        store2.addProperty("country", "Austria");
        panel.setValues(store2);
        assertEquals("Austria", panel.getSelectedCountry());

        panel.reset();
        assertEquals("Austria", panel.getSelectedCountry());
    }
}
