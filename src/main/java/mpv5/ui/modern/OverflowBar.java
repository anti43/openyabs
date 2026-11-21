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
package mpv5.ui.modern;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mpv5.globals.Messages;
import mpv5.ui.beans.PrinitingComboBox;

/**
 * A single-row horizontal action bar that never wraps to a second line: any
 * overflowable action that does not fit in the available width is hidden via
 * zero bounds (preserving caller-controlled {@code isVisible()}) and made
 * available in an overflow dropdown menu.
 * <p>
 * Can either manage its own {@code More ▾} overflow button (shown only while at
 * least one item overflows) or feed an external menu such as
 * {@link mpv5.ui.toolbars.ModernCommandBar}'s {@code More ▾} dropdown.
 */
public class OverflowBar extends JPanel {

    private static final long serialVersionUID = 1L;
    /**
     * Client property key on a menu-opening {@link JButton} pointing to its
     * {@link JPopupMenu} so an overflowed menu button becomes a submenu.
     */
    public static final String PROPERTY_POPUP_MENU = "yabs.popupMenu";

    private static final class Entry {

        final JComponent component;
        final KeyStroke shortcut;
        final boolean overflowable;
        final boolean separator;

        Entry(JComponent component, KeyStroke shortcut, boolean overflowable, boolean separator) {
            this.component = component;
            this.shortcut = shortcut;
            this.overflowable = overflowable;
            this.separator = separator;
        }
    }

    private final List<Entry> entries = new ArrayList<Entry>();
    private final List<Entry> overflowed = new ArrayList<Entry>();
    private final boolean alignRight;
    private final int gap;
    private final JButton overflowButton;
    private final JPopupMenu overflowMenu;

    /**
     * @param alignRight true to right-align the row, false to left-align
     * @param showOverflowButton true to manage a built-in {@code More ▾} button
     * when items overflow; false when an external menu calls
     * {@link #populateMenu(JPopupMenu)}
     */
    public OverflowBar(boolean alignRight, boolean showOverflowButton) {
        super(null);
        this.alignRight = alignRight;
        this.gap = ModernTheme.px(4);
        setOpaque(false);
        if (showOverflowButton) {
            overflowMenu = new JPopupMenu();
            overflowButton = ModernTheme.actionButton(new JButton(Messages.MORE_ACTIONS.toString() + " \u25BE"), null);
            overflowButton.addActionListener(e -> {
                overflowMenu.removeAll();
                populateMenu(overflowMenu);
                if (overflowMenu.getComponentCount() > 0) {
                    overflowMenu.show(overflowButton, 0, overflowButton.getHeight());
                }
            });
            add(overflowButton);
        } else {
            overflowMenu = null;
            overflowButton = null;
        }
    }

    /**
     * Adds a component that stays in the bar and never overflows.
     *
     * @param c
     */
    public void addPinned(JComponent c) {
        entries.add(new Entry(c, null, false, false));
        add(c);
        revalidate();
    }

    /**
     * Adds an action component that overflows into the dropdown when the bar is
     * too narrow.
     *
     * @param c
     * @param shortcut optional accelerator shown in the overflow menu
     */
    public void addOverflowable(JComponent c, KeyStroke shortcut) {
        entries.add(new Entry(c, shortcut, true, false));
        add(c);
        revalidate();
    }

    /**
     * Adds a vertical separator; hidden automatically if leading, trailing, or
     * adjacent to another separator.
     *
     * @return the separator component
     */
    public JSeparator addSeparator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(ModernTheme.px(8), ModernTheme.px(24)));
        s.setForeground(ModernTheme.border());
        entries.add(new Entry(s, null, false, true));
        add(s);
        revalidate();
        return s;
    }

    /**
     * @return true if at least one visible item is currently overflowed
     */
    public boolean hasOverflow() {
        return !overflowed.isEmpty();
    }

    /**
     * @return unmodifiable list of currently overflowed components (in bar order)
     */
    public List<JComponent> getOverflowedComponents() {
        List<JComponent> list = new ArrayList<JComponent>(overflowed.size());
        for (Entry e : overflowed) {
            list.add(e.component);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * @return the built-in overflow button, or null if using an external menu
     */
    public JButton getOverflowButton() {
        return overflowButton;
    }

    /**
     * Appends menu items for all currently overflowed components to {@code menu}.
     *
     * @param menu target popup menu
     * @return number of menu entries added
     */
    public int populateMenu(JPopupMenu menu) {
        int added = 0;
        for (Entry e : overflowed) {
            if (!e.component.isVisible()) {
                continue;
            }
            added += addToMenu(menu, e.component, e.shortcut);
        }
        return added;
    }

    private static int addToMenu(JPopupMenu menu, JComponent comp, KeyStroke shortcut) {
        if (comp instanceof SplitButton) {
            SplitButton sb = (SplitButton) comp;
            int count = 0;
            JButton main = sb.getMainButton();
            menu.add(buttonMenuItem(main, shortcut));
            count++;
            JPopupMenu sub = sb.getPopupMenu();
            if (sub != null) {
                for (Component c : sub.getComponents()) {
                    if (c instanceof JMenuItem && c.isVisible()) {
                        menu.add(mirrorMenuItem((JMenuItem) c));
                        count++;
                    }
                }
            }
            return count;
        }
        if (comp instanceof PrinitingComboBox) {
            PrinitingComboBox pcb = (PrinitingComboBox) comp;
            JComboBox<?> cb = pcb.getComboBox();
            String label = pcb.getLabelText();
            if (label == null || label.trim().isEmpty()) {
                label = "Print";
            } else {
                label = label.trim().replaceAll(":$", "");
            }
            JMenu sub = new JMenu(label);
            sub.setFont(ModernTheme.font(1, Font.PLAIN));
            sub.setEnabled(pcb.isEnabled() && cb.isEnabled());
            for (int i = 0; i < cb.getItemCount(); i++) {
                Object item = cb.getItemAt(i);
                String text = item == null ? "" : item.toString().trim();
                if (text.isEmpty()) {
                    continue;
                }
                final int idx = i;
                JMenuItem mi = new JMenuItem(text);
                mi.setFont(ModernTheme.font(1, Font.PLAIN));
                mi.addActionListener(ev -> cb.setSelectedIndex(idx));
                sub.add(mi);
            }
            if (sub.getItemCount() > 0) {
                menu.add(sub);
                return 1;
            }
            return 0;
        }
        if (comp instanceof javax.swing.JCheckBox) {
            //a setting in the bar: a check box item in the menu
            final javax.swing.JCheckBox cb = (javax.swing.JCheckBox) comp;
            javax.swing.JCheckBoxMenuItem mi = new javax.swing.JCheckBoxMenuItem(cb.getText(), cb.isSelected());
            mi.setFont(ModernTheme.font(1, Font.PLAIN));
            mi.setEnabled(cb.isEnabled());
            mi.setToolTipText(cb.getToolTipText());
            mi.addActionListener(ev -> cb.doClick());
            menu.add(mi);
            return 1;
        }
        if (comp instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) comp;
            Object popupObj = b.getClientProperty(PROPERTY_POPUP_MENU);
            if (popupObj instanceof JPopupMenu) {
                JPopupMenu subPopup = (JPopupMenu) popupObj;
                firePopupWillBecomeVisible(subPopup);
                String label = cleanButtonLabel(b).replaceAll("\\s*\u25BE$", "").trim();
                JMenu sub = new JMenu(label);
                sub.setFont(ModernTheme.font(1, Font.PLAIN));
                sub.setIcon(b.getIcon());
                sub.setEnabled(b.isEnabled());
                boolean pendingSep = false;
                for (Component c : subPopup.getComponents()) {
                    if (c instanceof JSeparator) {
                        if (sub.getItemCount() > 0) {
                            pendingSep = true;
                        }
                    } else if (c instanceof JMenuItem && c.isVisible()) {
                        if (pendingSep) {
                            sub.addSeparator();
                            pendingSep = false;
                        }
                        sub.add(mirrorMenuItem((JMenuItem) c));
                    }
                }
                menu.add(sub);
                return 1;
            }
            menu.add(buttonMenuItem(b, shortcut));
            return 1;
        }
        return 0;
    }

    private static void firePopupWillBecomeVisible(JPopupMenu popup) {
        PopupMenuEvent ev = new PopupMenuEvent(popup);
        for (PopupMenuListener l : popup.getPopupMenuListeners()) {
            l.popupMenuWillBecomeVisible(ev);
        }
    }

    private static JMenuItem buttonMenuItem(final AbstractButton b, KeyStroke shortcut) {
        JMenuItem mi = new JMenuItem(cleanButtonLabel(b));
        mi.setFont(ModernTheme.font(1, Font.PLAIN));
        mi.setIcon(b.getIcon());
        mi.setToolTipText(b.getToolTipText());
        mi.setEnabled(b.isEnabled());
        if (shortcut != null) {
            mi.setAccelerator(shortcut);
        }
        mi.addActionListener(e -> {
            if (b.isEnabled()) {
                b.doClick();
            }
        });
        return mi;
    }

    private static JMenuItem mirrorMenuItem(final JMenuItem orig) {
        JMenuItem mi;
        if (orig instanceof JCheckBoxMenuItem) {
            mi = new JCheckBoxMenuItem(orig.getText(), orig.isSelected());
        } else {
            mi = new JMenuItem(orig.getText());
        }
        mi.setFont(ModernTheme.font(1, Font.PLAIN));
        mi.setIcon(orig.getIcon());
        mi.setToolTipText(orig.getToolTipText());
        mi.setEnabled(orig.isEnabled());
        if (orig.getAccelerator() != null) {
            mi.setAccelerator(orig.getAccelerator());
        }
        mi.addActionListener(e -> {
            if (orig.isEnabled()) {
                orig.doClick();
            }
        });
        return mi;
    }

    private static String cleanButtonLabel(AbstractButton b) {
        String t = b.getText();
        if (t != null && !t.trim().isEmpty()) {
            return t.trim();
        }
        String tip = b.getToolTipText();
        return tip == null ? "" : tip.trim();
    }

    private int rowHeight() {
        int h = 0;
        for (Entry e : entries) {
            if (!e.separator && e.component.isVisible()) {
                h = Math.max(h, e.component.getPreferredSize().height);
            }
        }
        if (overflowButton != null) {
            h = Math.max(h, overflowButton.getPreferredSize().height);
        }
        return h > 0 ? h : ModernTheme.px(32);
    }

    @Override
    public Dimension getPreferredSize() {
        Insets in = getInsets();
        int w = 0;
        boolean prevWasItem = false;
        boolean pendingSep = false;
        int sepW = 0;
        for (Entry e : entries) {
            if (e.separator) {
                if (prevWasItem) {
                    pendingSep = true;
                    sepW = e.component.getPreferredSize().width;
                }
                continue;
            }
            if (!e.component.isVisible()) {
                continue;
            }
            if (pendingSep) {
                w += gap + sepW;
                pendingSep = false;
            }
            if (w > 0) {
                w += gap;
            }
            w += e.component.getPreferredSize().width;
            prevWasItem = true;
        }
        return new Dimension(w + in.left + in.right, rowHeight() + in.top + in.bottom);
    }

    @Override
    public Dimension getMinimumSize() {
        Insets in = getInsets();
        int w = 0;
        for (Entry e : entries) {
            if (!e.separator && !e.overflowable && e.component.isVisible()) {
                if (w > 0) {
                    w += gap;
                }
                w += e.component.getPreferredSize().width;
            }
        }
        if (overflowButton != null) {
            if (w > 0) {
                w += gap;
            }
            w += overflowButton.getPreferredSize().width;
        }
        return new Dimension(w + in.left + in.right, rowHeight() + in.top + in.bottom);
    }

    @Override
    public void doLayout() {
        Insets in = getInsets();
        int avail = Math.max(0, getWidth() - in.left - in.right);
        int innerH = Math.max(0, getHeight() - in.top - in.bottom);
        overflowed.clear();

        // Collect all logically visible entries, pruning leading/trailing/consecutive separators
        List<Entry> active = pruneSeparators(entries, null);
        int fullWidth = measure(active);

        List<Entry> placed;
        boolean showOverflow = false;
        if (fullWidth <= avail) {
            placed = active;
        } else {
            int ovReserve = overflowButton != null ? overflowButton.getPreferredSize().width + gap : 0;
            int budget = Math.max(0, avail - ovReserve);

            // First measure pinned non-separator items
            int used = 0;
            for (Entry e : active) {
                if (!e.separator && !e.overflowable) {
                    used += (used > 0 ? gap : 0) + e.component.getPreferredSize().width;
                }
            }
            // Account for separators between pinned items
            List<Entry> keep = new ArrayList<Entry>();
            boolean overflowing = false;
            int running = 0;
            Entry pendingSep = null;
            for (Entry e : active) {
                if (e.separator) {
                    if (!keep.isEmpty()) {
                        pendingSep = e;
                    }
                    continue;
                }
                int ew = e.component.getPreferredSize().width;
                int sepAdd = pendingSep != null ? gap + pendingSep.component.getPreferredSize().width : 0;
                int add = (running > 0 ? gap : 0) + sepAdd + ew;
                if (!e.overflowable) {
                    if (pendingSep != null) {
                        keep.add(pendingSep);
                        pendingSep = null;
                    }
                    keep.add(e);
                    running += add;
                } else if (!overflowing && running + add <= budget) {
                    if (pendingSep != null) {
                        keep.add(pendingSep);
                        pendingSep = null;
                    }
                    keep.add(e);
                    running += add;
                } else {
                    overflowing = true;
                    pendingSep = null;
                    overflowed.add(e);
                }
            }
            placed = pruneSeparators(keep, null);
            showOverflow = overflowButton != null && !overflowed.isEmpty();
        }

        // Zero-bounds everything not placed
        for (Entry e : entries) {
            if (!placed.contains(e)) {
                e.component.setBounds(0, 0, 0, 0);
            }
        }

        int placedWidth = measure(placed);
        int totalShown = placedWidth + (showOverflow ? (placedWidth > 0 ? gap : 0) + overflowButton.getPreferredSize().width : 0);
        int x = alignRight ? in.left + Math.max(0, avail - totalShown) : in.left;
        for (Entry e : placed) {
            Dimension d = e.component.getPreferredSize();
            int ch = Math.min(d.height, innerH > 0 ? innerH : d.height);
            int y = in.top + Math.max(0, (innerH - ch) / 2);
            e.component.setBounds(x, y, d.width, ch);
            x += d.width + gap;
        }
        if (overflowButton != null) {
            if (showOverflow) {
                Dimension d = overflowButton.getPreferredSize();
                int ch = Math.min(d.height, innerH > 0 ? innerH : d.height);
                int y = in.top + Math.max(0, (innerH - ch) / 2);
                overflowButton.setBounds(x, y, d.width, ch);
            } else {
                overflowButton.setBounds(0, 0, 0, 0);
            }
        }
    }

    private List<Entry> pruneSeparators(List<Entry> list, List<Entry> excluded) {
        List<Entry> out = new ArrayList<Entry>(list.size());
        Entry pendingSep = null;
        for (Entry e : list) {
            if (excluded != null && excluded.contains(e)) {
                continue;
            }
            if (e.separator) {
                if (!out.isEmpty()) {
                    pendingSep = e;
                }
                continue;
            }
            if (!e.component.isVisible()) {
                continue;
            }
            if (pendingSep != null) {
                out.add(pendingSep);
                pendingSep = null;
            }
            out.add(e);
        }
        return out;
    }

    private int measure(List<Entry> list) {
        int w = 0;
        for (Entry e : list) {
            if (w > 0) {
                w += gap;
            }
            w += e.component.getPreferredSize().width;
        }
        return w;
    }
}
