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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mpv5.globals.Messages;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.toolbars.ModernCommandBar;

/**
 * Help: all keyboard shortcuts. The menu shortcuts are read from the menu bar
 * of the main window, so the list is always up to date.
 */
public final class ShortcutsDialog {

    private static JDialog open;

    private ShortcutsDialog() {
    }

    /**
     * Shows the dialog (only once at a time)
     *
     * @param parent any component of the window, may be null
     */
    public static void show(Component parent) {
        if (open != null && open.isDisplayable()) {
            open.toFront();
            return;
        }
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, Messages.KEYBOARD_SHORTCUTS.toString());
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        JScrollPane s = new JScrollPane(createContent(menuBar(owner)));
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        d.getContentPane().add(s, BorderLayout.CENTER);
        d.setSize(ModernTheme.px(640), ModernTheme.px(720));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        open = d;
    }

    private static JMenuBar menuBar(Window owner) {
        if (owner instanceof JFrame && ((JFrame) owner).getJMenuBar() != null) {
            return ((JFrame) owner).getJMenuBar();
        }
        JFrame main = mpv5.YabsViewProxy.instance().getIdentifierFrame();
        return main == null ? null : main.getJMenuBar();
    }

    /**
     * @param menuBar may be null
     * @return the content: explanation and the shortcuts in three sections
     */
    static JComponent createContent(JMenuBar menuBar) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 12));

        JTextArea info = new JTextArea(Messages.SHORTCUTS_INFO.toString());
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setEditable(false);
        info.setOpaque(false);
        info.setFocusable(false);
        info.setForeground(ModernTheme.muted());
        info.setFont(ModernTheme.font(0, Font.PLAIN));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(info);

        add(p, Messages.SHORTCUTS_RECORD.toString(), ModernCommandBar.shortcuts());
        add(p, Messages.SHORTCUTS_MENU.toString(), menuShortcuts(menuBar));
        List<Object[]> text = new ArrayList<Object[]>();
        int ctrl = InputEvent.CTRL_DOWN_MASK;
        text.add(new Object[]{keys(k(KeyEvent.VK_C, ctrl), k(KeyEvent.VK_X, ctrl), k(KeyEvent.VK_V, ctrl)), Messages.SHORTCUT_TEXT_COPY.toString()});
        text.add(new Object[]{keys(k(KeyEvent.VK_A, ctrl)), Messages.SHORTCUT_TEXT_ALL.toString()});
        text.add(new Object[]{keys(k(KeyEvent.VK_BACK_SPACE, ctrl), k(KeyEvent.VK_DELETE, ctrl)), Messages.SHORTCUT_TEXT_WORD.toString()});
        text.add(new Object[]{keys(k(KeyEvent.VK_TAB, 0), k(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK)), Messages.SHORTCUT_TEXT_FIELDS.toString()});
        text.add(new Object[]{keys(k(KeyEvent.VK_ENTER, 0)), Messages.SHORTCUT_TEXT_SEARCH.toString()});
        text.add(new Object[]{keys(k(KeyEvent.VK_C, ctrl)), Messages.SHORTCUT_TABLE_COPY.toString()});
        add(p, Messages.SHORTCUTS_TEXT.toString(), text);
        return p;
    }

    private static String keys(KeyStroke... k) {
        StringBuilder b = new StringBuilder();
        for (KeyStroke x : k) {
            b.append(b.length() == 0 ? "" : "  /  ").append(ModernCommandBar.keyText(x));
        }
        return b.toString();
    }

    private static KeyStroke k(int key, int modifiers) {
        return KeyStroke.getKeyStroke(key, modifiers);
    }

    /**
     * @param bar may be null
     * @return {key text, "Menu › Item"} of every menu item with a shortcut
     */
    static List<Object[]> menuShortcuts(JMenuBar bar) {
        List<Object[]> l = new ArrayList<Object[]>();
        if (bar != null) {
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu m = bar.getMenu(i);
                if (m != null) {
                    collect(m, m.getText(), l);
                }
            }
        }
        return l;
    }

    private static void collect(JMenu menu, String path, List<Object[]> l) {
        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem item = menu.getItem(i);
            if (item == null) {
                continue;
            }
            String p = path + "  \u203A  " + item.getText();
            if (item instanceof JMenu) {
                collect((JMenu) item, p, l);
            } else if (item.getAccelerator() != null) {
                l.add(new Object[]{item.getAccelerator(), p});
            }
        }
    }

    private static void add(JPanel p, String title, List<Object[]> rows) {
        if (rows.isEmpty()) {
            return;
        }
        JPanel table = new JPanel(new GridBagLayout());
        table.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(ModernTheme.px(3), 0, ModernTheme.px(3), ModernTheme.px(14));
        int row = 0;
        for (Object[] r : rows) {
            String key = r[0] instanceof KeyStroke ? ModernCommandBar.keyText((KeyStroke) r[0]) : String.valueOf(r[0]);
            JLabel k = new JLabel(key);
            k.setFont(ModernTheme.font(0, Font.BOLD));
            k.setForeground(ModernTheme.foreground());
            k.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ModernTheme.border(), 1, true),
                    BorderFactory.createEmptyBorder(ModernTheme.px(1), ModernTheme.px(6), ModernTheme.px(1), ModernTheme.px(6))));
            JLabel t = new JLabel(String.valueOf(r[1]));
            t.setFont(ModernTheme.font(0, Font.PLAIN));
            c.gridy = row++;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            table.add(k, c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            table.add(t, c);
        }
        Card card = new Card(title).addFull(table);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(12)));
        p.add(card);
    }
}
