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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mpv5.db.objects.Contact;
import mpv5.globals.Messages;
import mpv5.handler.Privacy;
import mpv5.logging.Log;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.date.DateConverter;

/**
 * Deleting a contact whose documents must be kept: explains what the law
 * requires (retention of invoices and business letters, GDPR) and offers the
 * export of the person's data and the blocking with deletion of everything
 * else (see {@link Privacy}). A blocked contact whose retention period is over
 * can be deleted for good here.
 */
public final class ContactPrivacyDialog {

    private static final Color RED = new Color(200, 40, 40);
    private static final Color GREEN = new Color(40, 150, 80);
    private static final Color ORANGE = new Color(214, 140, 20);

    /**
     * What the user did
     */
    public enum Result {
        CANCELLED, BLOCKED, DELETED
    }

    private ContactPrivacyDialog() {
    }

    /**
     * Deletes a contact as far as the law allows: if nothing must be kept, as
     * before (confirmation, trash); else this dialog
     *
     * @param parent
     * @param c
     * @return what happened
     */
    public static Result delete(Component parent, Contact c) {
        Privacy.Report r = Privacy.check(c);
        if (!r.mustKeep() || r.expired(new Date()) && !Privacy.isBlocked(c)) {
            //nothing to keep: the normal deletion (trash)
            if (DeleteConfirmDialog.confirm(parent, c) && c.delete()) {
                return Result.DELETED;
            }
            return Result.CANCELLED;
        }
        return show(parent, c, r);
    }

    /**
     * Only the export of the data of a person (Art. 15, 20 GDPR)
     *
     * @param parent
     * @param c
     */
    public static void export(Component parent, Contact c) {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new File(mpv5.utils.files.LastDirectory.get(), Messages.PRIVACY_EXPORT_FILE + "-"
                + safe(c.__getCNumber().isEmpty() ? String.valueOf(c.__getIDS()) : c.__getCNumber()) + "-"
                + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".zip"));
        if (ch.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION || ch.getSelectedFile() == null) {
            return;
        }
        File f = ch.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".zip")) {
            f = new File(f.getPath() + ".zip");
        }
        try {
            Privacy.export(c, f);
            mpv5.utils.files.LastDirectory.set(f.getParentFile());
            Popup.notice(parent, String.format(Messages.PRIVACY_EXPORTED.toString(), f.getAbsolutePath()));
        } catch (Exception e) {
            Log.Debug(ContactPrivacyDialog.class, e);
            Popup.error(parent, Messages.NOT_POSSIBLE + ": " + e.getMessage());
        }
    }

    private static Result show(Component parent, final Contact c, final Privacy.Report r) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, Messages.PRIVACY_TITLE.toString(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final Result[] result = {Result.CANCELLED};
        boolean blocked = Privacy.isBlocked(c);
        boolean expired = r.expired(new Date());
        String until = DateConverter.getDefDateString(r.keepUntil);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 12));
        p.add(text(String.format(blocked ? Messages.PRIVACY_INTRO_BLOCKED.toString() : Messages.PRIVACY_INTRO.toString(),
                c.__getCname(), until), ModernTheme.font(2, Font.BOLD), ModernTheme.foreground()));
        p.add(Box.createVerticalStrut(ModernTheme.px(6)));
        p.add(text(Messages.PRIVACY_LAW.toString(), ModernTheme.font(-1, Font.PLAIN), ModernTheme.muted()));

        if (!r.open.isEmpty()) {
            List<String> open = new ArrayList<String>();
            for (Privacy.Document doc : r.open) {
                open.add(doc.toString());
            }
            section(p, Messages.PRIVACY_OPEN_INVOICES.toString(), Messages.PRIVACY_OPEN_HINT.toString(), open, RED);
        }
        if (!blocked) {
            List<String> deleted = new ArrayList<String>();
            deleted.add(Messages.PRIVACY_DELETED_FIELDS.toString());
            for (Privacy.Document doc : r.deletable) {
                deleted.add(doc.toString());
            }
            deleted.add(Messages.PRIVACY_DELETED_OTHER.toString());
            section(p, Messages.PRIVACY_WILL_DELETE.toString(), null, deleted, ORANGE);
        }
        List<String> kept = new ArrayList<String>();
        kept.add(Messages.PRIVACY_KEPT_CONTACT.toString());
        for (Privacy.Document doc : r.retained) {
            kept.add(doc + " → " + DateConverter.getDefDateString(doc.keepUntil));
            if (kept.size() > 25) {
                kept.add("…");
                break;
            }
        }
        if (r.revenues > 0) {
            kept.add(String.format(Messages.PRIVACY_KEPT_REVENUES.toString(), r.revenues));
        }
        section(p, String.format(Messages.PRIVACY_WILL_KEEP.toString(), until), Messages.PRIVACY_KEEP_HINT.toString(), kept, GREEN);
        p.add(Box.createVerticalStrut(ModernTheme.px(8)));
        p.add(text(String.format(Messages.PRIVACY_AFTER.toString(), until), ModernTheme.font(-1, Font.PLAIN), ModernTheme.muted()));

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        d.getContentPane().add(scroll, BorderLayout.CENTER);

        JButton export = ModernTheme.actionButton(new JButton(Messages.PRIVACY_EXPORT.toString()), null);
        export.addActionListener(e -> export(d, c));
        JButton cancel = ModernTheme.actionButton(new JButton(javax.swing.UIManager.getString("OptionPane.cancelButtonText")), null);
        cancel.addActionListener(e -> d.dispose());
        PrimaryButton main;
        if (blocked && expired) {
            main = new PrimaryButton(Messages.PRIVACY_DELETE_FINALLY.toString(), "/images/16/edittrash.png");
            main.addActionListener(e -> {
                if (Popup.Y_N_dialog(d, String.format(Messages.PRIVACY_DELETE_FINALLY_ASK.toString(), c.__getCname()),
                        Messages.PRIVACY_DELETE_FINALLY.toString())) {
                    if (Privacy.deleteFinally(c)) {
                        result[0] = Result.DELETED;
                        d.dispose();
                    } else {
                        Popup.warn(d, Messages.NOT_POSSIBLE.toString());
                    }
                }
            });
        } else {
            main = new PrimaryButton(Messages.PRIVACY_BLOCK.toString(), null);
            main.setEnabled(!blocked && r.open.isEmpty());
            main.addActionListener(e -> {
                if (!Popup.Y_N_dialog(d, String.format(Messages.PRIVACY_BLOCK_ASK.toString(), c.__getCname()),
                        Messages.PRIVACY_BLOCK.toString())) {
                    return;
                }
                try {
                    Privacy.block(c);
                    result[0] = Result.BLOCKED;
                    d.dispose();
                    Popup.notice(parent, String.format(Messages.PRIVACY_BLOCKED.toString(), c.__getCname(), until));
                } catch (Exception ex) {
                    Log.Debug(ContactPrivacyDialog.class, ex);
                    Popup.error(d, Messages.NOT_POSSIBLE + ": " + ex.getMessage());
                }
            });
        }
        main.setColor(RED);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        buttons.setBackground(ModernTheme.cardBackground());
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()), ModernTheme.padding(10, 12)));
        buttons.add(export);
        buttons.add(cancel);
        buttons.add(main);
        d.getContentPane().add(buttons, BorderLayout.SOUTH);
        d.getRootPane().setDefaultButton(cancel);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        d.setSize(Math.min(Math.max(d.getWidth(), ModernTheme.px(520)), ModernTheme.px(680)),
                Math.min(d.getHeight(), ModernTheme.px(680)));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return result[0];
    }

    private static void section(JPanel p, String title, String hint, List<String> entries, Color color) {
        JLabel heading = new JLabel(title);
        heading.setFont(ModernTheme.font(1, Font.BOLD));
        heading.setForeground(color);
        Card card = new Card(null).addFull(heading);
        if (hint != null) {
            card.addFull(text(hint, ModernTheme.font(-1, Font.PLAIN), ModernTheme.muted()));
        }
        for (String e : entries) {
            JLabel l = new JLabel(e);
            l.setFont(ModernTheme.font(-1, Font.PLAIN));
            l.setForeground(ModernTheme.foreground());
            l.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(12), 0, 0));
            card.addFull(l);
        }
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Box.createVerticalStrut(ModernTheme.px(10)));
        p.add(card);
    }

    private static JTextArea text(String s, Font font, Color color) {
        JTextArea t = new JTextArea(s);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setFont(font);
        t.setForeground(color);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setColumns(46);
        return t;
    }

    private static String safe(String s) {
        return s.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
