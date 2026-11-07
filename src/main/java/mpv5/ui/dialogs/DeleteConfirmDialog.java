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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.DeletionPreview;
import mpv5.handler.DeletionPreview.Effect;
import mpv5.handler.DeletionPreview.Group;
import mpv5.logging.Log;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Asks before a record is deleted and lists what is deleted with it: what
 * goes to the trash, what is deleted permanently, what stays
 * ({@link DeletionPreview}).
 */
public final class DeleteConfirmDialog {

    private static final Color TRASH = new Color(214, 140, 20);
    private static final Color PERMANENT = new Color(200, 40, 40);
    private static final Color KEPT = new Color(40, 150, 80);

    private DeleteConfirmDialog() {
    }

    /**
     * @param parent a component of the window, may be null
     * @param o the record to delete
     * @return true if the user confirmed
     */
    public static boolean confirm(Component parent, DatabaseObject o) {
        List<Group> groups;
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        Cursor old = owner == null ? null : owner.getCursor();
        try {
            if (owner != null) {
                owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            groups = DeletionPreview.of(o);
        } catch (Exception e) {
            //without the list, ask as before
            Log.Debug(DeleteConfirmDialog.class, e);
            return Popup.Y_N_dialog(Messages.REALLY_DELETE);
        } finally {
            if (owner != null) {
                owner.setCursor(old);
            }
        }

        final boolean[] result = {false};
        final JDialog d = new JDialog(owner, Messages.DELETE_PREVIEW_TITLE.toString(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.getContentPane().add(content(o, groups), BorderLayout.CENTER);

        JButton cancel = new JButton(javax.swing.UIManager.getString("OptionPane.cancelButtonText"));
        ModernTheme.actionButton(cancel, null);
        cancel.setFocusable(true);
        cancel.addActionListener(e -> d.dispose());
        PrimaryButton delete = new PrimaryButton(Messages.DELETE_PREVIEW_CONFIRM.toString(), "/images/16/edittrash.png");
        delete.setColor(PERMANENT);
        delete.addActionListener(e -> {
            result[0] = true;
            d.dispose();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        buttons.setBackground(ModernTheme.cardBackground());
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()),
                ModernTheme.padding(10, 12)));
        buttons.add(cancel);
        buttons.add(delete);
        d.getContentPane().add(buttons, BorderLayout.SOUTH);
        //safe default: Enter and Escape cancel, deleting needs the button
        d.getRootPane().setDefaultButton(cancel);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        Dimension max = new Dimension(ModernTheme.px(620), ModernTheme.px(640));
        d.setSize(Math.min(Math.max(d.getWidth(), ModernTheme.px(460)), max.width), Math.min(d.getHeight(), max.height));
        d.setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(cancel::requestFocusInWindow);
        d.setVisible(true);
        return result[0];
    }

    /**
     * @param o
     * @param groups
     * @return the list of affected data, grouped by effect
     */
    static JComponent content(DatabaseObject o, List<Group> groups) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 12));
        JTextArea intro = new JTextArea(String.format(Messages.DELETE_PREVIEW_INTRO.toString(), o.__getCname()));
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        intro.setEditable(false);
        intro.setFocusable(false);
        intro.setOpaque(false);
        intro.setFont(ModernTheme.font(2, Font.BOLD));
        intro.setForeground(ModernTheme.foreground());
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(intro);
        section(p, groups, Effect.TRASH, Messages.DELETE_PREVIEW_TRASH.toString(), TRASH);
        section(p, groups, Effect.PERMANENT, Messages.DELETE_PREVIEW_PERMANENT.toString(), PERMANENT);
        section(p, groups, Effect.KEPT, Messages.DELETE_PREVIEW_KEPT.toString(), KEPT);
        JScrollPane s = new JScrollPane(p);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        return s;
    }

    private static void section(JPanel p, List<Group> groups, Effect effect, String title, Color color) {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (Group g : groups) {
            if (g.effect != effect) {
                continue;
            }
            if (!g.title.equals(title)) {
                JLabel t = new JLabel(g.title);
                t.setFont(ModernTheme.font(0, Font.BOLD));
                t.setForeground(ModernTheme.foreground());
                t.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), 0, ModernTheme.px(1), 0));
                list.add(t);
            }
            for (String e : g.entries) {
                JLabel l = new JLabel(e);
                l.setFont(ModernTheme.font(-1, Font.PLAIN));
                l.setForeground(ModernTheme.muted());
                l.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(12), 0, 0));
                list.add(l);
            }
        }
        if (list.getComponentCount() == 0) {
            return;
        }
        JLabel heading = new JLabel(title);
        heading.setFont(ModernTheme.font(1, Font.BOLD));
        heading.setForeground(color);
        heading.setIcon(new Dot(color));
        heading.setIconTextGap(ModernTheme.px(8));
        Card card = new Card(null).addFull(heading).addFull(list);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(ModernTheme.px(10)));
        p.add(card);
    }

    /**
     * A colored dot in front of a heading
     */
    private static final class Dot implements javax.swing.Icon {

        private final Color color;

        Dot(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, getIconWidth(), getIconHeight());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return ModernTheme.px(10);
        }

        @Override
        public int getIconHeight() {
            return ModernTheme.px(10);
        }
    }
}
