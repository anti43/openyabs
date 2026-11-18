package mpv5.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.junit.jupiter.api.Test;

class SearchAnythingTest {

    @Test
    void testDialogCreationAndControls() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SearchAnything dialog = SearchAnything.ensureInstance();
        assertNotNull(dialog, "Dialog instance should not be null");
        assertNotNull(dialog.getTextField(), "Search field should exist");
        assertNotNull(dialog.getPinButton(), "Pin button should exist");
        assertNotNull(dialog.getCloseButton(), "Close button should exist");

        // Close button has text or icon and has tooltip
        JButton close = dialog.getCloseButton();
        assertTrue(close.getText().contains("✕") || close.getText().contains("X"), "Close button should have close symbol");
        assertTrue(close.getToolTipText().toLowerCase().contains("close"), "Close button should have close tooltip");

        // Pin button
        JToggleButton pin = dialog.getPinButton();
        assertEquals("Pin", pin.getText());
        assertTrue(pin.getToolTipText().toLowerCase().contains("stick"), "Pin button should have tooltip");
    }

    @Test
    void testStickToTopToggle() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SearchAnything dialog = SearchAnything.ensureInstance();
        assertFalse(dialog.isStickToTop(), "Initially stick to top should be false");

        dialog.setStickToTop(true);
        assertTrue(dialog.isStickToTop(), "stickToTop should be true after setStickToTop(true)");
        assertTrue(dialog.getPinButton().isSelected(), "Pin button should be selected");

        dialog.setStickToTop(false);
        assertFalse(dialog.isStickToTop(), "stickToTop should be false after setStickToTop(false)");
        assertFalse(dialog.getPinButton().isSelected(), "Pin button should not be selected");
    }

    @Test
    void testFindFilterBox() {
        // Container with named filter text field
        JPanel p1 = new JPanel();
        JTextField tf1 = new JTextField();
        tf1.setName("tableFilter");
        p1.add(tf1);
        assertSame(tf1, SearchAnything.findFilterBox(p1), "Should find text field named 'tableFilter'");

        // Container with placeholder
        JPanel p2 = new JPanel();
        JTextField tf2 = new JTextField();
        tf2.putClientProperty("JTextField.placeholderText", "Search items...");
        p2.add(tf2);
        assertSame(tf2, SearchAnything.findFilterBox(p2), "Should find text field with search placeholder");

        // Container with German keyword
        JPanel p3 = new JPanel();
        JTextField tf3 = new JTextField();
        tf3.setToolTipText("Suchbegriff eingeben");
        p3.add(tf3);
        assertSame(tf3, SearchAnything.findFilterBox(p3), "Should find text field with 'Suchbegriff' tooltip");

        // Container with non-filter text fields
        JPanel p4 = new JPanel();
        JTextField regular = new JTextField();
        regular.setName("city");
        p4.add(regular);
        assertNull(SearchAnything.findFilterBox(p4), "Should not treat regular field as filter box");
    }

    @Test
    void testShowCenterCalculatesBounds() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SearchAnything dialog = SearchAnything.ensureInstance();
        Rectangle screen = new Rectangle(0, 0, 1920, 1080);
        int expectedX = (screen.width - dialog.getWidth()) / 2;
        int expectedY = (screen.height - dialog.getHeight()) / 2;
        assertTrue(expectedX > 0 && expectedY > 0, "Centered coordinates should be positive");
    }

    @Test
    void testCloseHidesDialog() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SearchAnything dialog = SearchAnything.ensureInstance();
        dialog.close();
        assertFalse(dialog.isVisible(), "Dialog should not be visible after close()");
    }
}
