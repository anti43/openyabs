/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.handler.NumberFormatBuilder;
import mpv5.handler.NumberFormatBuilder.DatePart;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * The number format of one kind of record in three steps: a style chosen by
 * its example (RE-2026-00001 ...), fine tuning with a live preview, for whom
 * - then it is stored like the detailed form does
 * ({@link FormatHandler#store}).
 */
public final class NumberFormatWizard extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * A style: prefix?, date part, separator, digits, restart
     */
    private static final class Style {

        final boolean prefix;
        final DatePart date;
        final int digits;
        final String explanation;

        Style(boolean prefix, DatePart date, int digits, String explanation) {
            this.prefix = prefix;
            this.date = date;
            this.digits = digits;
            this.explanation = explanation;
        }
    }

    private final MPEnum kind;
    private final List<Object[]> users;
    private final Style[] styles;
    private final JRadioButton[] styleButtons;
    private final JTextField prefix = ModernTheme.roomy(new JTextField(8));
    private final JSpinner digits = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
    private final JCheckBox restart = new JCheckBox();
    private final JCheckBox setNext = new JCheckBox(Messages.NUMWIZ_CONTINUE.toString());
    private final JSpinner next = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JLabel preview = new JLabel(" ");
    private final JRadioButton allUsers = new JRadioButton(Messages.NUMWIZ_ALL_USERS.toString(), true);
    private final JRadioButton oneUser = new JRadioButton(Messages.NUMWIZ_ONE_USER.toString());
    private final JComboBox<Object[]> user = new JComboBox<Object[]>();
    private final JLabel summary = new JLabel();
    private final CardLayout steps = new CardLayout();
    private final JPanel stepPanel = new JPanel(steps);
    private final JLabel stepTitle = new JLabel();
    private final JButton back = new JButton(Messages.NUMWIZ_BACK.toString());
    private final PrimaryButton forward = new PrimaryButton(Messages.NUMWIZ_NEXT.toString(), null);
    private int step;
    private DatePart date = DatePart.YEAR;
    private boolean saved;

    /**
     * @param owner
     * @param kind the kind of record (invoice, customer ...)
     * @param users {usersids, name}, the first is "all users"
     * @param preselectedUser the user to preselect (all users: the first entry)
     */
    public NumberFormatWizard(java.awt.Window owner, MPEnum kind, List<Object[]> users, int preselectedUser) {
        super(owner, String.format(Messages.NUMWIZ_TITLE.toString(), kind.getName()), ModalityType.APPLICATION_MODAL);
        this.kind = kind;
        this.users = users;
        String p = defaultPrefix(kind.getId());
        styles = new Style[]{
            new Style(true, DatePart.YEAR, 5, Messages.NUMWIZ_STYLE_PREFIX_YEAR.toString()),
            new Style(true, DatePart.NONE, 5, Messages.NUMWIZ_STYLE_PREFIX.toString()),
            new Style(false, DatePart.YEAR, 5, Messages.NUMWIZ_STYLE_YEAR.toString()),
            new Style(true, DatePart.YEAR_MONTH, 3, Messages.NUMWIZ_STYLE_MONTH.toString()),
            new Style(false, DatePart.NONE, 6, Messages.NUMWIZ_STYLE_PLAIN.toString())};
        styleButtons = new JRadioButton[styles.length];
        prefix.setText(p);

        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        root.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(16), ModernTheme.px(18), ModernTheme.px(14), ModernTheme.px(18)));
        root.setBackground(ModernTheme.background());
        stepTitle.setFont(ModernTheme.font(3f, Font.BOLD));
        root.add(stepTitle, BorderLayout.NORTH);
        stepPanel.setOpaque(false);
        stepPanel.add(styleStep(p), "0");
        stepPanel.add(tuneStep(), "1");
        stepPanel.add(whoStep(preselectedUser), "2");
        root.add(stepPanel, BorderLayout.CENTER);

        JButton cancel = new JButton(Messages.NUMWIZ_CANCEL.toString());
        cancel.addActionListener(e -> dispose());
        back.addActionListener(e -> show(step - 1));
        forward.addActionListener(e -> {
            if (step < 2) {
                show(step + 1);
            } else {
                finish();
            }
        });
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        right.setOpaque(false);
        right.add(back);
        right.add(forward);
        buttons.add(cancel, BorderLayout.WEST);
        buttons.add(right, BorderLayout.EAST);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(forward);
        getRootPane().registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        };
        prefix.getDocument().addDocumentListener(dl);
        digits.addChangeListener(e -> update());
        restart.addActionListener(e -> update());
        setNext.addActionListener(e -> update());
        next.addChangeListener(e -> update());
        allUsers.addActionListener(e -> update());
        oneUser.addActionListener(e -> update());
        user.addActionListener(e -> update());
        styleButtons[0].setSelected(true);
        applyStyle(0);
        show(0);
        pack();
        //html labels tend to report a little less height than they need
        setSize(getWidth() + ModernTheme.px(20), getHeight() + ModernTheme.px(48));
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    /**
     * @return true if the format was saved
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * A short prefix for the kind of record, in German or English
     *
     * @param type
     * @return e.g. RE for invoices (German)
     */
    static String defaultPrefix(int type) {
        boolean de = Locale.getDefault().getLanguage().equals("de");
        switch (type) {
            case Constants.TYPE_INVOICE:
                return de ? "RE" : "INV";
            case Constants.TYPE_OFFER:
                return de ? "AN" : "OFF";
            case Constants.TYPE_ORDER:
                return de ? "AU" : "ORD";
            case Constants.TYPE_CREDIT:
                return de ? "GS" : "CN";
            case Constants.TYPE_DELIVERY_NOTE:
                return de ? "LS" : "DN";
            case Constants.TYPE_ORDER_CONFIRMATION:
                return de ? "AB" : "OC";
            case Constants.TYPE_CONTRACT:
                return de ? "VT" : "CT";
            case Constants.TYPE_PART_PAYMENT:
                return de ? "AR" : "PI";
            case Constants.TYPE_DEPOSIT:
                return de ? "AZ" : "DP";
            case Constants.TYPE_CUSTOMER:
                return de ? "K" : "C";
            case Constants.TYPE_SUPPLIER:
                return de ? "L" : "S";
            case Constants.TYPE_MANUFACTURER:
                return de ? "H" : "M";
            case Constants.TYPE_CONTACT:
                return de ? "KO" : "CO";
            case Constants.TYPE_PRODUCT:
                return de ? "A" : "P";
            case Constants.TYPE_SERVICE:
                return de ? "D" : "SV";
            case Constants.TYPE_REVENUE:
                return de ? "E" : "IN";
            case Constants.TYPE_EXPENSE:
                return de ? "AG" : "EX";
            case Constants.TYPE_CONVERSATION:
                return de ? "G" : "NO";
            case Constants.TYPE_ACTIVITY:
                return de ? "T" : "AL";
            case Constants.TYPE_PRODUCT_ORDER:
                return de ? "B" : "PO";
            default:
                return "N";
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Steps">
    private JComponent styleStep(String p) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        Date now = new Date();
        for (int i = 0; i < styles.length; i++) {
            Style s = styles[i];
            String example = builder(s, p, s.digits, true).pattern();
            JRadioButton b = new JRadioButton("<html><b style='font-size:110%'>" + esc(NumberFormatBuilder.format(example, 1, now)) + "</b>"
                    + (i == 0 ? " &nbsp;<font color='#2563eb'>" + Messages.NUMWIZ_RECOMMENDED + "</font>" : "")
                    + "<br><font color='#6b7280'>" + esc(s.explanation) + "</font></html>");
            b.setOpaque(false);
            b.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), 0, ModernTheme.px(6), 0));
            final int index = i;
            b.addActionListener(e -> applyStyle(index));
            g.add(b);
            styleButtons[i] = b;
            b.setAlignmentX(LEFT_ALIGNMENT);
            list.add(b);
        }
        JPanel page = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        page.setOpaque(false);
        page.add(hint(Messages.NUMWIZ_STYLE_HINT.toString()), BorderLayout.NORTH);
        page.add(list, BorderLayout.CENTER);
        return page;
    }

    private JComponent tuneStep() {
        JPanel form = new JPanel(new GridLayout(0, 2, ModernTheme.px(10), ModernTheme.px(8)));
        form.setOpaque(false);
        form.add(new JLabel(Messages.NUMWIZ_PREFIX.toString()));
        form.add(prefix);
        form.add(new JLabel(Messages.NUMWIZ_DIGITS.toString()));
        form.add(digits);
        restart.setOpaque(false);
        setNext.setOpaque(false);
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        JPanel nextRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        nextRow.setOpaque(false);
        nextRow.add(setNext);
        nextRow.add(next);
        preview.setFont(ModernTheme.font(3f, Font.BOLD));
        preview.setForeground(ModernTheme.accent());
        JLabel previewLabel = new JLabel(Messages.NUMWIZ_PREVIEW.toString());
        previewLabel.setForeground(ModernTheme.muted());
        for (JComponent c : new JComponent[]{form, restart, nextRow, hint(Messages.NUMWIZ_CONTINUE_HINT.toString()), previewLabel, preview}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            if (c == previewLabel) {
                page.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(10)));
            }
            page.add(c);
            page.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        }
        return page;
    }

    private JComponent whoStep(int preselectedUser) {
        ButtonGroup g = new ButtonGroup();
        g.add(allUsers);
        g.add(oneUser);
        allUsers.setOpaque(false);
        oneUser.setOpaque(false);
        for (int i = 1; i < users.size(); i++) {
            user.addItem(users.get(i));
            if ((Integer) users.get(i)[0] == preselectedUser) {
                user.setSelectedIndex(i - 1);
                oneUser.setSelected(true);
            }
        }
        user.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof Object[] ? ((Object[]) v)[1] : v, i, s, f);
            }
        });
        oneUser.setEnabled(user.getItemCount() > 0);
        JPanel one = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        one.setOpaque(false);
        one.add(oneUser);
        one.add(user);
        summary.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);
        for (JComponent c : new JComponent[]{allUsers, one, hint(Messages.NUMWIZ_USERS_HINT.toString()), summary}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            if (c == summary) {
                page.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(14)));
            }
            page.add(c);
            page.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        }
        return page;
    }
    // </editor-fold>

    private static JComponent hint(String text) {
        javax.swing.JTextArea a = new javax.swing.JTextArea(text);
        a.setEditable(false);
        a.setFocusable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setColumns(46);
        a.setFont(ModernTheme.baseFont());
        a.setForeground(ModernTheme.muted());
        a.setBorder(BorderFactory.createEmptyBorder());
        return a;
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static NumberFormatBuilder builder(Style s, String p, int digits, boolean restart) {
        return new NumberFormatBuilder().setPrefix(s.prefix && !p.isEmpty() ? p + "-" : "").setDate(s.date).setSeparator("-")
                .setDigits(digits).setRestart(restart);
    }

    private Style style() {
        for (int i = 0; i < styleButtons.length; i++) {
            if (styleButtons[i].isSelected()) {
                return styles[i];
            }
        }
        return styles[0];
    }

    private void applyStyle(int i) {
        Style s = styles[i];
        date = s.date;
        digits.setValue(s.digits);
        prefix.setEnabled(s.prefix);
        restart.setVisible(s.date != DatePart.NONE);
        restart.setSelected(s.date != DatePart.NONE);
        restart.setText(s.date == DatePart.YEAR_MONTH ? Messages.NUMWIZ_RESTART_MONTH.toString() : Messages.NUMWIZ_RESTART_YEAR.toString());
        update();
    }

    /**
     * @return the format as chosen
     */
    NumberFormatBuilder result() {
        Style s = style();
        return builder(s, prefix.getText().trim(), (Integer) digits.getValue(), restart.isSelected() && s.date != DatePart.NONE);
    }

    private int usersids() {
        if (oneUser.isSelected() && user.getSelectedItem() != null) {
            return (Integer) ((Object[]) user.getSelectedItem())[0];
        }
        return (Integer) users.get(0)[0];
    }

    private void update() {
        next.setEnabled(setNext.isSelected());
        int first = setNext.isSelected() ? (Integer) next.getValue() : 1;
        Date now = new Date();
        String p = result().pattern();
        String a = NumberFormatBuilder.format(p, first, now), b = NumberFormatBuilder.format(p, first + 1, now),
                c = NumberFormatBuilder.format(p, first + 2, now);
        preview.setText(a + ",  " + b + ",  " + c + " …");
        StringBuilder sb = new StringBuilder("<html><div style='width:" + ModernTheme.px(380) + "px'>");
        sb.append(String.format(Messages.NUMWIZ_SUMMARY.toString(), esc(kind.getName()), "<b>" + esc(a) + "</b>", "<b>" + esc(b) + "</b>"));
        if (result().isRestart()) {
            sb.append(' ').append(date == DatePart.YEAR_MONTH ? Messages.NUMWIZ_SUMMARY_MONTH : Messages.NUMWIZ_SUMMARY_YEAR);
        }
        sb.append(' ').append(usersids() == (Integer) users.get(0)[0] ? Messages.NUMWIZ_SUMMARY_ALL
                : String.format(Messages.NUMWIZ_SUMMARY_ONE.toString(), esc(String.valueOf(((Object[]) user.getSelectedItem())[1]))));
        summary.setText(sb.append("</div></html>").toString());
    }

    private void show(int s) {
        step = Math.max(0, Math.min(2, s));
        steps.show(stepPanel, String.valueOf(step));
        String[] titles = {Messages.NUMWIZ_STEP_STYLE.toString(), Messages.NUMWIZ_STEP_TUNE.toString(), Messages.NUMWIZ_STEP_WHO.toString()};
        stepTitle.setText(String.format(Messages.NUMWIZ_STEP.toString(), step + 1, titles[step]));
        back.setEnabled(step > 0);
        forward.setText(step < 2 ? Messages.NUMWIZ_NEXT.toString() : Messages.NUMWIZ_FINISH.toString());
        update();
    }

    private void finish() {
        NumberFormatBuilder b = result();
        FormatHandler.store(kind.getId(), usersids(), b.pattern(), b.parsePosition(), setNext.isSelected() ? (Integer) next.getValue() : null);
        saved = true;
        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.NUMFMT_SAVED.toString(), kind.getName(),
                NumberFormatBuilder.format(b.pattern(), 1, new Date())), new Color(22, 163, 74));
        dispose();
    }
}
