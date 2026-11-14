package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class SettingsIndexTest {

    @Test
    void optionsOfAPage() {
        JPanel page = new JPanel();
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createTitledBorder("Proxy"));
        card.add(new JCheckBox("Beim Start nach neuen Versionen suchen"));
        card.add(new JLabel("<html><b>Basisverzeichnis:</b></html>"));
        card.add(new JButton("Speichern"));
        card.add(new JLabel("12"));
        card.add(new JLabel("Updates"));
        page.add(card);
        page.add(new JLabel("Updates"));
        MPControlPanel.Entry e = new MPControlPanel.Entry("System", "Lokale Einstellungen", "", null, null, null, null, null);
        List<SettingsIndex.Option> out = new ArrayList<SettingsIndex.Option>();
        SettingsIndex.collect(page, e, out, new HashSet<String>());
        List<String> texts = new ArrayList<String>();
        for (SettingsIndex.Option o : out) {
            texts.add(o.text);
        }
        //check boxes, labels and card titles; no buttons, numbers or doubles
        assertEquals(java.util.Arrays.asList("Proxy", "Beim Start nach neuen Versionen suchen", "Basisverzeichnis", "Updates"), texts);
    }

    @Test
    void longHintsAreNoOptions() {
        assertNull(SettingsIndex.clean("x".repeat(30) + " " + "y".repeat(70)));
        assertEquals("Proxy-Port", SettingsIndex.clean(" Proxy-Port: "));
    }
}
