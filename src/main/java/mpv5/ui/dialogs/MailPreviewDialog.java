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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mpv5.globals.Messages;
import mpv5.mail.SimpleMail;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * Shows an e-mail before it is sent: recipient, copies, subject, text and the
 * attached files (a click opens them). Recipient, subject and text can still
 * be changed. Nothing is sent without "Send".
 */
public final class MailPreviewDialog {

    private static final Pattern MAIL = Pattern.compile("^[^@\\s;,]+@[^@\\s;,]+\\.[^@\\s;,]+$");

    private MailPreviewDialog() {
    }

    /**
     * @param mail the prepared mail; recipient, subject and text are taken over
     * from the dialog when the user sends
     * @param files the attachments
     * @return true: send, false: canceled
     */
    public static boolean confirm(SimpleMail mail, List<File> files) {
        Window owner = null;
        try {
            owner = mpv5.YabsViewProxy.instance().getIdentifierFrame();
        } catch (Exception e) {
            //no main window (tests)
        }
        final JDialog d = new JDialog(owner, Messages.MAIL_PREVIEW_TITLE.toString(), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] send = {false};

        JTextField to = new JTextField(nz(mail.getRecipientsAddress()));
        JTextField subject = new JTextField(nz(mail.getSubject()));
        subject.setFont(ModernTheme.font(2, Font.BOLD));
        JTextArea text = new JTextArea(nz(mail.getText()), 14, 60);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setCaretPosition(0);
        //an HTML mail is shown as the customer sees it; its source can be edited
        JComponent body = SimpleMail.isHtml(text.getText()) ? htmlBody(text) : new JScrollPane(text);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        row = add(form, row, Messages.MAIL_PREVIEW_TO.toString(), to);
        if (!nz(mail.getCCAddress()).isEmpty()) {
            row = add(form, row, "Cc", muted(mail.getCCAddress()));
        }
        if (!nz(mail.getBccAddress()).isEmpty()) {
            row = add(form, row, "Bcc", muted(mail.getBccAddress()));
        }
        row = add(form, row, Messages.MAIL_PREVIEW_SUBJECT.toString(), subject);

        //the attachments: a click opens the file
        JPanel attachments = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        attachments.setOpaque(false);
        if (files.isEmpty()) {
            attachments.add(muted(Messages.MAIL_PREVIEW_NO_FILES.toString()));
        }
        for (final File f : files) {
            JButton b = new JButton(f.getName() + "  (" + size(f) + ")");
            b.setToolTipText(f.getPath());
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> FileDirectoryHandler.open(f));
            attachments.add(b);
        }
        row = add(form, row, Messages.MAIL_PREVIEW_FILES.toString(), attachments);

        JPanel center = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(body, BorderLayout.CENTER);

        JButton cancel = new JButton(Messages.MAIL_PREVIEW_CANCEL.toString());
        cancel.addActionListener(e -> d.dispose());
        PrimaryButton ok = new PrimaryButton(Messages.MAIL_PREVIEW_SEND.toString(), null);
        ok.addActionListener(e -> {
            String address = to.getText().trim();
            if (!MAIL.matcher(address).matches()) {
                Popup.notice(Messages.NO_MAIL_DEFINED + ": " + address);
                to.requestFocusInWindow();
                return;
            }
            mail.setRecipientsAddress(address);
            mail.setSubject(subject.getText());
            mail.setText(text.getText());
            send[0] = true;
            d.dispose();
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(ok);

        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(14), ModernTheme.px(16), ModernTheme.px(14), ModernTheme.px(16)));
        p.add(center, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        d.setContentPane(p);
        d.getRootPane().setDefaultButton(null);//Enter in the text must not send
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.pack();
        d.setMinimumSize(new Dimension(ModernTheme.px(560), ModernTheme.px(420)));
        d.setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(ok::requestFocusInWindow);
        d.setVisible(true);
        return send[0];
    }

    /**
     * The rendered HTML mail and, in a second tab, its source
     *
     * @param source the text of the mail (edited there)
     * @return the tabs
     */
    static javax.swing.JTabbedPane htmlBody(JTextArea source) {
        javax.swing.JEditorPane view = new javax.swing.JEditorPane();
        view.setName("mailpreview.html");
        view.setEditable(false);
        view.setContentType("text/html");
        view.putClientProperty(javax.swing.JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        view.setFont(ModernTheme.font(1, Font.PLAIN));
        view.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED && e.getURL() != null) {
                try {
                    java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    mpv5.logging.Log.Debug(MailPreviewDialog.class, ex);
                }
            }
        });
        render(view, source.getText());
        source.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ModernTheme.font(0, Font.PLAIN).getSize()));
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        tabs.addTab(Messages.MAIL_PREVIEW_RENDERED.toString(), new JScrollPane(view));
        tabs.addTab(Messages.MAIL_PREVIEW_SOURCE.toString(), new JScrollPane(source));
        //changes in the source show in the preview
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) {
                render(view, source.getText());
            }
        });
        return tabs;
    }

    private static void render(javax.swing.JEditorPane view, String html) {
        try {
            view.setText(html);
        } catch (Exception e) {
            //broken HTML: show it as text rather than nothing
            mpv5.logging.Log.Debug(MailPreviewDialog.class, e);
            view.setContentType("text/plain");
            view.setText(html);
        }
        view.setCaretPosition(0);
    }

    private static int add(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(ModernTheme.px(3), 0, ModernTheme.px(3), ModernTheme.px(12));
        JLabel l = new JLabel(label);
        l.setForeground(ModernTheme.muted());
        form.add(l, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(ModernTheme.px(3), 0, ModernTheme.px(3), 0);
        form.add(field, c);
        return row + 1;
    }

    private static JLabel muted(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(ModernTheme.muted());
        return l;
    }

    private static String size(File f) {
        long b = f.length();
        return b >= 1024 * 1024 ? String.format("%.1f MB", b / 1048576d) : Math.max(1, Math.round(b / 1024d)) + " KB";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
