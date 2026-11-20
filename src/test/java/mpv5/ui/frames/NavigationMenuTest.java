package mpv5.ui.frames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.ui.beans.BigDropDownButton;
import mpv5.ui.toolbars.ModernCommandBar;
import org.junit.jupiter.api.Test;

class NavigationMenuTest {

    @Test
    void replaceInvoiceDropDownInstallsTwoRealButtonsInPlace() {
        JPanel navAccounting = new JPanel();
        JButton offer = button("jButton10", "Neues Angebot", "Neues Angebot anlegen");
        JButton order = button("jButton11", "Neuer Auftrag", "Neuen Auftrag anlegen");
        BigDropDownButton ddb = new BigDropDownButton();
        JButton credit = button("jButton30", "Neue Gutschrift", "Gutschrift");
        navAccounting.add(offer);
        navAccounting.add(order);
        navAccounting.add(ddb);
        navAccounting.add(credit);

        AtomicInteger invoiceClicks = new AtomicInteger();
        AtomicInteger partPaymentClicks = new AtomicInteger();
        NavigationMenu.replaceInvoiceDropDown(navAccounting, ddb, offer,
                e -> invoiceClicks.incrementAndGet(),
                e -> partPaymentClicks.incrementAndGet());

        assertEquals(5, navAccounting.getComponentCount());
        assertSame(offer, navAccounting.getComponent(0));
        assertSame(order, navAccounting.getComponent(1));
        assertTrue(navAccounting.getComponent(2) instanceof JButton);
        assertTrue(navAccounting.getComponent(3) instanceof JButton);
        assertSame(credit, navAccounting.getComponent(4));

        JButton invoiceBtn = (JButton) navAccounting.getComponent(2);
        JButton partBtn = (JButton) navAccounting.getComponent(3);
        assertEquals(NavigationMenu.NAV_INVOICE_BUTTON, invoiceBtn.getName());
        assertEquals(Messages.NEW_INVOICE.toString(), invoiceBtn.getText());
        assertEquals(NavigationMenu.NAV_PART_PAYMENT_BUTTON, partBtn.getName());
        assertEquals(Item.getTypeString(Item.TYPE_PART_PAYMENT), partBtn.getText());
        assertEquals(Messages.NEW_PART_PAYMENT.toString(), partBtn.getToolTipText());

        invoiceBtn.doClick(0);
        partBtn.doClick(0);
        assertEquals(1, invoiceClicks.get());
        assertEquals(1, partPaymentClicks.get());
    }

    @Test
    void installBuildsMenuAfterViewWithShortcutsAndTriggersButtons() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu view = new JMenu("View");
        JMenu tools = new JMenu("Tools");
        bar.add(file);
        bar.add(edit);
        bar.add(view);
        bar.add(tools);

        AtomicInteger customerClicks = new AtomicInteger();
        AtomicInteger invoiceClicks = new AtomicInteger();
        AtomicInteger partClicks = new AtomicInteger();

        JPanel contacts = new JPanel();
        JButton customers = button("jButton5", "Kunden", "Kunden verwalten");
        customers.addActionListener(e -> customerClicks.incrementAndGet());
        contacts.add(customers);

        JPanel accounting = new JPanel();
        JButton invoice = button(NavigationMenu.NAV_INVOICE_BUTTON, "Neue Rechnung", "Neue Rechnung");
        invoice.addActionListener(e -> invoiceClicks.incrementAndGet());
        JButton partPayment = button(NavigationMenu.NAV_PART_PAYMENT_BUTTON, "Abschlagsrechnung", "Neue Abschlagsrechnung");
        partPayment.addActionListener(e -> partClicks.incrementAndGet());
        accounting.add(invoice);
        accounting.add(partPayment);

        JPanel extras = new JPanel();
        JButton history = button("jButton6", "Verlauf", "Verlauf");
        JButton hidden = button("jButton9", "Schriftverkehr", "Hidden");
        hidden.setVisible(false);
        extras.add(history);
        extras.add(hidden);

        JMenu nav = NavigationMenu.install(bar, view, contacts, accounting, extras);
        assertNotNull(nav);
        assertEquals(NavigationMenu.MENU_NAME, nav.getName());
        assertEquals(Messages.SEARCH_NAVIGATION.toString(), nav.getText());
        assertSame(nav, bar.getMenu(3), "Navigation menu must be inserted right after View");
        assertSame(tools, bar.getMenu(4));

        // Expected menu components: Kunden | sep | Neue Rechnung, Abschlagsrechnung | sep | Verlauf
        Component[] items = nav.getMenuComponents();
        assertEquals(6, items.length);
        assertTrue(items[1] instanceof JSeparator);
        assertTrue(items[4] instanceof JSeparator);

        JMenuItem miCustomers = (JMenuItem) items[0];
        JMenuItem miInvoice = (JMenuItem) items[2];
        JMenuItem miPart = (JMenuItem) items[3];
        JMenuItem miHistory = (JMenuItem) items[5];

        assertEquals("Kunden", miCustomers.getText());
        assertEquals(KeyEvent.VK_K, miCustomers.getAccelerator().getKeyCode());
        assertEquals(KeyEvent.VK_R, miInvoice.getAccelerator().getKeyCode());
        assertEquals(KeyEvent.VK_T, miPart.getAccelerator().getKeyCode());
        assertNull(miHistory.getAccelerator());

        // Tooltip gets shortcut hint once, even after repopulate
        String tipOnce = customers.getToolTipText();
        assertTrue(tipOnce.contains("Kunden verwalten") && tipOnce.contains("K"));
        NavigationMenu.populate(nav, contacts, accounting, extras);
        assertEquals(tipOnce, customers.getToolTipText());

        // Clicking menu items triggers the underlying left-bar buttons
        ((JMenuItem) nav.getMenuComponents()[0]).doClick(0);
        ((JMenuItem) nav.getMenuComponents()[2]).doClick(0);
        ((JMenuItem) nav.getMenuComponents()[3]).doClick(0);
        assertEquals(1, customerClicks.get());
        assertEquals(1, invoiceClicks.get());
        assertEquals(1, partClicks.get());
    }

    @Test
    void shortcutsAreUniqueAndDoNotCollideWithExistingCtrlShiftBindings() {
        Set<Integer> used = new HashSet<Integer>();
        // Existing Ctrl+Shift bindings in MPView and ModernCommandBar
        Set<Integer> reserved = new HashSet<Integer>(Arrays.asList(
                ModernCommandBar.SAVE_AS_NEW.getKeyCode(),
                ModernCommandBar.PDF.getKeyCode(),
                ModernCommandBar.SEARCH_PANEL.getKeyCode(),
                ModernCommandBar.HISTORY.getKeyCode(),
                KeyEvent.VK_D, // View > Open dashboard
                KeyEvent.VK_W  // View > Tabs > Close all tabs
        ));
        for (java.util.Map.Entry<String, Integer> e : NavigationMenu.shortcutKeys().entrySet()) {
            Integer code = e.getValue();
            assertTrue(used.add(code), "Duplicate navigation shortcut key for " + e.getKey());
            assertFalse(reserved.contains(code), "Navigation shortcut collides with existing Ctrl+Shift binding: " + KeyEvent.getKeyText(code));
            KeyStroke ks = NavigationMenu.shortcutFor(e.getKey());
            assertNotNull(ks);
            assertEquals(code.intValue(), ks.getKeyCode());
        }
    }

    private static JButton button(String name, String text, String tip) {
        JButton b = new JButton(text);
        b.setName(name);
        b.setToolTipText(tip);
        return b;
    }
}
