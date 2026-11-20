package mpv5.ui.modern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.jupiter.api.Test;

class OverflowBarTest {

    private static JButton sizedButton(String text, int width) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(width, 30));
        return b;
    }

    @Test
    void overflowsTrailingActionsIntoMenuWithoutClobberingVisibility() {
        OverflowBar bar = new OverflowBar(false, false);
        JButton pinned = sizedButton("Save", 80);
        JButton a = sizedButton("Mail", 100);
        JButton b = sizedButton("Print", 100);
        JButton c = sizedButton("Export", 120);
        AtomicInteger exportClicks = new AtomicInteger();
        c.addActionListener(e -> exportClicks.incrementAndGet());

        bar.addPinned(pinned);
        bar.addSeparator();
        bar.addOverflowable(a, null);
        bar.addOverflowable(b, null);
        bar.addOverflowable(c, null);

        // Wide enough for all buttons
        bar.setSize(500, 34);
        bar.doLayout();
        assertFalse(bar.hasOverflow());
        assertTrue(c.getWidth() > 0);

        // Narrow: pinned + a fit, b and c overflow
        bar.setSize(220, 34);
        bar.doLayout();
        assertTrue(bar.hasOverflow());
        assertEquals(2, bar.getOverflowedComponents().size());
        assertTrue(bar.getOverflowedComponents().contains(b));
        assertTrue(bar.getOverflowedComponents().contains(c));
        // Caller-controlled visibility stays true even though bounds are zero
        assertTrue(c.isVisible());
        assertEquals(0, c.getWidth());

        JPopupMenu menu = new JPopupMenu();
        assertEquals(2, bar.populateMenu(menu));
        JMenuItem exportItem = (JMenuItem) menu.getComponent(1);
        assertEquals("Export", exportItem.getText());
        exportItem.doClick();
        assertEquals(1, exportClicks.get());
    }

    @Test
    void hiddenComponentsAreSkippedInBarAndMenu() {
        OverflowBar bar = new OverflowBar(true, true);
        JButton a = sizedButton("A", 100);
        JButton hidden = sizedButton("Hidden", 100);
        hidden.setVisible(false);
        JButton b = sizedButton("B", 100);

        bar.addOverflowable(a, null);
        bar.addOverflowable(hidden, null);
        bar.addOverflowable(b, null);

        bar.setSize(120, 34);
        bar.doLayout();
        assertTrue(bar.hasOverflow());
        assertFalse(bar.getOverflowedComponents().contains(hidden));
        JPopupMenu menu = new JPopupMenu();
        bar.populateMenu(menu);
        for (int i = 0; i < menu.getComponentCount(); i++) {
            assertFalse("Hidden".equals(((JMenuItem) menu.getComponent(i)).getText()));
        }
    }

    @Test
    void menuButtonAndSplitButtonPopulateSubmenuAndActions() {
        OverflowBar bar = new OverflowBar(true, true);
        JPopupMenu subPopup = new JPopupMenu();
        JMenuItem xml = new JMenuItem("XRechnung");
        AtomicInteger xmlClicks = new AtomicInteger();
        xml.addActionListener(e -> xmlClicks.incrementAndGet());
        subPopup.add(xml);

        JButton einvoice = sizedButton("E-Rechnung \u25BE", 120);
        einvoice.putClientProperty(OverflowBar.PROPERTY_POPUP_MENU, subPopup);

        SplitButton split = new SplitButton("Neue Rechnung", null);
        split.setPreferredSize(new Dimension(140, 30));
        AtomicInteger mainClicks = new AtomicInteger();
        AtomicInteger subClicks = new AtomicInteger();
        split.addActionListener(e -> mainClicks.incrementAndGet());
        split.add("Abschlagsrechnung", e -> subClicks.incrementAndGet());

        bar.addOverflowable(einvoice, null);
        bar.addOverflowable(split, null);

        bar.setSize(90, 34);
        bar.doLayout();
        assertEquals(2, bar.getOverflowedComponents().size());

        JPopupMenu menu = new JPopupMenu();
        int count = bar.populateMenu(menu);
        assertEquals(3, count); // 1 JMenu + 1 SplitButton main + 1 SplitButton extra
        assertTrue(menu.getComponent(0) instanceof JMenu);
        JMenu sub = (JMenu) menu.getComponent(0);
        assertEquals("E-Rechnung", sub.getText());
        sub.getItem(0).doClick();
        assertEquals(1, xmlClicks.get());

        ((JMenuItem) menu.getComponent(1)).doClick();
        ((JMenuItem) menu.getComponent(2)).doClick();
        assertEquals(1, mainClicks.get());
        assertEquals(1, subClicks.get());
    }
}
