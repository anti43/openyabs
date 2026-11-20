/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.frames;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.ui.toolbars.ModernCommandBar;

/**
 * Top-level "Navigation" menu mirroring the entries of the left navigation bar,
 * with global keyboard shortcuts ({@code Ctrl+Shift+<Key>} on Windows/Linux,
 * {@code Cmd+Shift+<Key>} on macOS). Also provides helpers to replace the
 * legacy invoice drop-down button with dedicated invoice and progress-invoice
 * buttons.
 */
public final class NavigationMenu {

    public static final String MENU_NAME = "navigationMenu";
    public static final String NAV_INVOICE_BUTTON = "navInvoice";
    public static final String NAV_PART_PAYMENT_BUTTON = "navPartPayment";
    private static final String BASE_TIP_KEY = "yabs.navBaseTip";

    private static final Map<String, Integer> SHORTCUT_KEYS;

    static {
        Map<String, Integer> keys = new LinkedHashMap<String, Integer>();
        // Contacts
        keys.put("jButton5", KeyEvent.VK_K);            // Kunden / Customers
        keys.put("jButton1", KeyEvent.VK_L);            // Lieferanten / Suppliers
        keys.put("jButton18", KeyEvent.VK_M);           // Hersteller / Manufacturers
        // Sales & accounting
        keys.put("jButton10", KeyEvent.VK_A);           // Neues Angebot / New offer
        keys.put("jButton11", KeyEvent.VK_O);           // Neuer Auftrag / New order
        keys.put(NAV_INVOICE_BUTTON, KeyEvent.VK_R);    // Neue Rechnung / New invoice
        keys.put(NAV_PART_PAYMENT_BUTTON, KeyEvent.VK_T); // Abschlagsrechnung / Progress invoice
        keys.put("jButton30", KeyEvent.VK_G);           // Neue Gutschrift / New credit note
        keys.put("jButton15", KeyEvent.VK_X);           // Ausgaben / Expenses
        keys.put("jButton16", KeyEvent.VK_E);           // Einnahmen / Income
        keys.put("jButton17", KeyEvent.VK_J);           // Journal
        keys.put("jButton23", KeyEvent.VK_V);           // Vorgangssuche / Find documents
        // Products
        keys.put("jButton13", KeyEvent.VK_N);           // Neuer Artikel / New product
        keys.put("jButton14", KeyEvent.VK_B);           // Stückliste / Bill of materials
        keys.put("jButton19", KeyEvent.VK_I);           // Artikel / Products
        keys.put("jButton12", KeyEvent.VK_C);           // Neue Dienstleistung / New service
        SHORTCUT_KEYS = Collections.unmodifiableMap(keys);
    }

    private NavigationMenu() {
    }

    /**
     * Replaces the legacy invoice drop-down component in {@code navAccounting}
     * with two regular buttons: "New invoice" and "Progress invoice"
     * (Abschlagsrechnung).
     *
     * @param navAccounting the accounting navigation panel
     * @param dropDown the drop-down component to replace
     * @param template an existing navigation button whose font/sizing is copied
     * @param onInvoice action for the invoice button
     * @param onPartPayment action for the progress-invoice button
     */
    public static void replaceInvoiceDropDown(JPanel navAccounting, Component dropDown, JButton template,
            ActionListener onInvoice, ActionListener onPartPayment) {
        if (navAccounting == null) {
            return;
        }
        int index = -1;
        Component[] comps = navAccounting.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == dropDown) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            navAccounting.remove(index);
        } else {
            index = navAccounting.getComponentCount();
        }
        JButton invoice = createNavButton(template, NAV_INVOICE_BUTTON,
                Messages.NEW_INVOICE.toString(), Messages.NEW_INVOICE.toString(),
                "/images/32/folder_blue.png", onInvoice);
        JButton partPayment = createNavButton(template, NAV_PART_PAYMENT_BUTTON,
                Item.getTypeString(Item.TYPE_PART_PAYMENT), Messages.NEW_PART_PAYMENT.toString(),
                "/images/32/folder_blue.png", onPartPayment);
        navAccounting.add(invoice, index);
        navAccounting.add(partPayment, index + 1);
        navAccounting.revalidate();
    }

    /**
     * Creates a left-navigation bar button styled like {@code template}.
     */
    static JButton createNavButton(JButton template, String name, String text, String tooltip,
            String iconPath, ActionListener action) {
        JButton b = new JButton(text);
        if (iconPath != null) {
            URL url = NavigationMenu.class.getResource(iconPath);
            if (url != null) {
                b.setIcon(new ImageIcon(url));
            }
        }
        if (template != null) {
            b.setFont(template.getFont());
            b.setMaximumSize(template.getMaximumSize());
            b.setMinimumSize(template.getMinimumSize());
            b.setPreferredSize(template.getPreferredSize());
        }
        b.setToolTipText(tooltip);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setName(name);
        if (action != null) {
            b.addActionListener(action);
        }
        return b;
    }

    /**
     * Installs the "Navigation" menu into {@code bar} right after {@code after}
     * (or at the end if {@code after} is null / not present).
     *
     * @param bar main window menu bar
     * @param after menu after which to insert (e.g. {@code viewMenu})
     * @param sections left navigation bar panels, in display order
     * @return the created menu, or null if {@code bar} is null
     */
    public static JMenu install(JMenuBar bar, JMenu after, JPanel... sections) {
        if (bar == null) {
            return null;
        }
        JMenu menu = new JMenu(Messages.SEARCH_NAVIGATION.toString());
        menu.setName(MENU_NAME);
        populate(menu, sections);
        int insertAt = bar.getMenuCount();
        if (after != null) {
            for (int i = 0; i < bar.getMenuCount(); i++) {
                if (bar.getMenu(i) == after) {
                    insertAt = i + 1;
                    break;
                }
            }
        }
        bar.add(menu, insertAt);
        bar.revalidate();
        return menu;
    }

    /**
     * Rebuilds {@code menu} from the visible buttons of {@code sections}.
     */
    public static void populate(JMenu menu, JPanel... sections) {
        if (menu == null) {
            return;
        }
        menu.removeAll();
        boolean hasPreviousSection = false;
        for (JPanel section : sections) {
            if (section == null) {
                continue;
            }
            boolean addedInSection = false;
            for (Component c : section.getComponents()) {
                if (!(c instanceof AbstractButton) || !c.isVisible()) {
                    continue;
                }
                final AbstractButton button = (AbstractButton) c;
                String label = button.getText() == null ? "" : button.getText().trim();
                if (label.isEmpty()) {
                    continue;
                }
                if (hasPreviousSection && !addedInSection) {
                    menu.addSeparator();
                }
                JMenuItem item = new JMenuItem(label);
                item.setName("navMenu_" + (button.getName() != null ? button.getName() : label));
                Icon scaled = smallIcon(button.getIcon());
                if (scaled != null) {
                    item.setIcon(scaled);
                }
                String baseTip = baseTooltip(button, label);
                if (!baseTip.isEmpty()) {
                    item.setToolTipText(baseTip);
                }
                KeyStroke shortcut = shortcutFor(button.getName());
                if (shortcut != null) {
                    item.setAccelerator(shortcut);
                    button.setToolTipText(ModernCommandBar.tip(baseTip, shortcut));
                }
                item.addActionListener(e -> button.doClick(0));
                menu.add(item);
                addedInSection = true;
            }
            if (addedInSection) {
                hasPreviousSection = true;
            }
        }
    }

    /**
     * @param buttonName the {@link Component#getName()} of a left-bar button
     * @return the bound {@link KeyStroke}, or null if no shortcut is assigned
     */
    public static KeyStroke shortcutFor(String buttonName) {
        if (buttonName == null) {
            return null;
        }
        Integer keyCode = SHORTCUT_KEYS.get(buttonName);
        if (keyCode == null) {
            return null;
        }
        return KeyStroke.getKeyStroke(keyCode, shortcutMask());
    }

    static Map<String, Integer> shortcutKeys() {
        return SHORTCUT_KEYS;
    }

    private static int shortcutMask() {
        int menuMask = GraphicsEnvironment.isHeadless()
                ? InputEvent.CTRL_DOWN_MASK
                : Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        return menuMask | InputEvent.SHIFT_DOWN_MASK;
    }

    private static String baseTooltip(AbstractButton button, String fallback) {
        Object stored = button.getClientProperty(BASE_TIP_KEY);
        if (stored instanceof String) {
            return (String) stored;
        }
        String tip = button.getToolTipText();
        String base = (tip == null || tip.trim().isEmpty()) ? fallback : tip.trim();
        button.putClientProperty(BASE_TIP_KEY, base);
        return base;
    }

    private static Icon smallIcon(Icon icon) {
        if (icon instanceof ImageIcon) {
            Image img = ((ImageIcon) icon).getImage();
            if (img != null) {
                return new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            }
        }
        return null;
    }
}
