/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.handler.NumberFormatBuilder;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.models.MPComboBoxModelItem;

/**
 * Number formats (number ranges) made easy: what is numbered and for whom,
 * then prefix, date part, separator, digits, suffix - with a live preview of
 * the next numbers. Whether the counter starts again with a new year is a
 * check box (the old "parse position" is computed). Patterns the building
 * blocks can not express stay editable as expert pattern.
 */
public class ControlPanel_NumberFormats extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public final String UNAME = "formats";

    private final JComboBox<MPEnum> type = new JComboBox<MPEnum>();
    private final JComboBox<Object[]> user = new JComboBox<Object[]>();
    private final JTextField prefix = ModernTheme.roomy(new JTextField(8));
    private final JComboBox<String> date = new JComboBox<String>();
    private final JTextField separator = ModernTheme.roomy(new JTextField("-", 3));
    private final JSpinner digits = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
    private final JTextField suffix = ModernTheme.roomy(new JTextField(6));
    private final JCheckBox restart = new JCheckBox(Messages.NUMFMT_RESTART.toString(), true);
    private final JCheckBox setNext = new JCheckBox(Messages.NUMFMT_SET_NEXT.toString());
    private final JSpinner next = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final JCheckBox expert = new JCheckBox(Messages.NUMFMT_EXPERT.toString());
    private final JTextField pattern = ModernTheme.roomy(new JTextField(24));
    private final JSpinner position = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
    private final JLabel preview = new JLabel(" ");
    private final JComboBox<Object> locales = new JComboBox<Object>();
    private final DefaultTableModel existingModel = new DefaultTableModel(new Object[]{
        Messages.NUMFMT_COL_TYPE.toString(), Messages.NUMFMT_COL_FOR.toString(), Messages.NUMFMT_COL_EXAMPLE.toString()}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable existing = new JTable(existingModel);
    private final JPanel overviewRows = new JPanel(new java.awt.GridBagLayout());
    /**
     * the kinds offered in the overview (product orders and deposits are hidden in the program)
     */
    private static final java.util.Set<Integer> HIDDEN = new java.util.HashSet<Integer>(java.util.Arrays.asList(
            mpv5.globals.Constants.TYPE_PRODUCT_ORDER, mpv5.globals.Constants.TYPE_DEPOSIT));
    private final List<Object[]> existingRows = new ArrayList<Object[]>();
    private final JPanel actions;
    private boolean loading;

    public ControlPanel_NumberFormats() {
        super(new BorderLayout());
        setOpaque(false);
        for (MPEnum t : FormatHandler.TYPES.values()) {
            type.addItem(t);
        }
        type.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof MPEnum ? ((MPEnum) v).getName() : v, i, s, f);
            }
        });
        user.addItem(new Object[]{User.ADMIN_ID, Messages.NUMFMT_ALL_USERS.toString()});
        try {
            for (DatabaseObject u : DatabaseObject.getObjects(Context.getUser())) {
                if (u.__getIDS() != User.ADMIN_ID) {
                    user.addItem(new Object[]{u.__getIDS(), u.__getCname()});
                }
            }
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        user.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof Object[] ? ((Object[]) v)[1] : v, i, s, f);
            }
        });
        Date now = new Date();
        NumberFormatBuilder sample = new NumberFormatBuilder();
        date.addItem(Messages.NUMFMT_DATE_NONE.toString());
        date.addItem(Messages.NUMFMT_DATE_YEAR + " (" + NumberFormatBuilder.format("[YEAR]", 0, now) + ")");
        date.addItem(Messages.NUMFMT_DATE_YEAR_SHORT + " (" + NumberFormatBuilder.format("[YEAR_SHORT]", 0, now) + ")");
        date.addItem(Messages.NUMFMT_DATE_YEAR_MONTH + " (" + NumberFormatBuilder.format("[YEAR]-[MONTH]", 0, now) + ")");
        date.setSelectedIndex(1);
        preview.setFont(ModernTheme.font(2, Font.BOLD));
        restart.setOpaque(false);
        setNext.setOpaque(false);
        expert.setOpaque(false);
        try {
            locales.setModel(LanguageManager.getLocalesAsComboBoxModel());
        } catch (Exception e) {
            Log.Debug(this, e);
        }

        //what and for whom
        Card what = new Card(Messages.NUMFMT_CARD_WHAT.toString())
                .row(Card.field(Messages.NUMFMT_TYPE.toString(), type), Card.field(Messages.NUMFMT_USER.toString(), user))
                .addFull(Applets.hint(Messages.NUMFMT_USER_HINT.toString()));
        //how the number looks
        Card how = new Card(Messages.NUMFMT_CARD_HOW.toString())
                .row(Card.field(Messages.NUMFMT_PREFIX.toString(), prefix), Card.field(Messages.NUMFMT_DATE.toString(), date))
                .row(Card.field(Messages.NUMFMT_SEPARATOR.toString(), separator), Card.field(Messages.NUMFMT_DIGITS.toString(), digits),
                        Card.field(Messages.NUMFMT_SUFFIX.toString(), suffix))
                .addFull(restart)
                .row(Card.field(" ", setNext), Card.field(Messages.NUMFMT_NEXT.toString(), next))
                .addFull(Applets.hint(Messages.NUMFMT_NEXT_HINT.toString()))
                .addFull(expert)
                .row(Card.field(Messages.NUMFMT_PATTERN.toString(), pattern, 3), Card.field(Messages.NUMFMT_POSITION.toString(), position, 1))
                .addFull(Applets.hint(Messages.NUMFMT_EXPERT_HINT.toString()));
        Card result = new Card(Messages.NUMFMT_CARD_PREVIEW.toString()).addFull(preview);
        existing.setRowHeight(ModernTheme.px(24));
        ModernTheme.style(existing);
        existing.getSelectionModel().addListSelectionListener(e -> {
            int r = existing.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0 && r < existingRows.size()) {
                select((Integer) existingRows.get(r)[0], (Integer) existingRows.get(r)[1]);
            }
        });
        JScrollPane sp = new JScrollPane(existing);
        sp.setPreferredSize(new java.awt.Dimension(ModernTheme.px(300), ModernTheme.px(150)));
        Card list = new Card(Messages.NUMFMT_CARD_EXISTING.toString()).addFull(sp, 1)
                .addFull(Applets.hint(Messages.NUMFMT_EXISTING_HINT.toString()));
        Card dates = new Card(Messages.FORMATS_CARD_DATE.toString())
                .row(Card.field(Messages.FORMATS_DATE_LOCALE.toString(), locales))
                .addFull(Applets.hint(Messages.NUMFMT_LOCALE_HINT.toString()));
        //the overview with an assistant per kind; the detailed cards for experts
        Card overview = new Card(Messages.NUMWIZ_OVERVIEW.toString()).addFull(overviewRows);
        overviewRows.setOpaque(false);
        final Card[] advanced = {what, how, result, list, dates};
        javax.swing.JToggleButton more = new javax.swing.JToggleButton(Messages.NUMWIZ_SHOW_ALL.toString());
        more.setFocusable(false);
        more.addActionListener(e -> {
            for (Card c : advanced) {
                c.setVisible(more.isSelected());
            }
            more.setText(more.isSelected() ? Messages.NUMWIZ_HIDE_ALL.toString() : Messages.NUMWIZ_SHOW_ALL.toString());
            revalidate();
            repaint();
        });
        overview.headerAction(more);
        for (Card c : advanced) {
            c.setVisible(false);
        }
        add(Applets.page(Messages.NUMWIZ_INFO.toString(), 380, overview, what, how, result, list, dates), BorderLayout.CENTER);

        PrimaryButton save = new PrimaryButton(Messages.NUMFMT_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        actions = Applets.actionBar(new JComponent[0], new JComponent[]{save});

        Runnable update = () -> {
            if (!loading) {
                updatePreview();
            }
        };
        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update.run();
            }
        };
        for (JTextField t : new JTextField[]{prefix, separator, suffix, pattern}) {
            t.getDocument().addDocumentListener(dl);
        }
        date.addActionListener(e -> update.run());
        digits.addChangeListener(e -> update.run());
        next.addChangeListener(e -> update.run());
        position.addChangeListener(e -> update.run());
        restart.addActionListener(e -> update.run());
        setNext.addActionListener(e -> update.run());
        expert.addActionListener(e -> {
            if (expert.isSelected() && !loading) {
                //start from the building blocks
                pattern.setText(builder().pattern());
                position.setValue(builder().parsePosition());
            }
            update.run();
        });
        type.addActionListener(e -> load());
        user.addActionListener(e -> load());
        reset();
    }

    private int typeId() {
        return ((MPEnum) type.getSelectedItem()).getId();
    }

    private int userId() {
        return (Integer) ((Object[]) user.getSelectedItem())[0];
    }

    private NumberFormatBuilder builder() {
        return new NumberFormatBuilder().setPrefix(prefix.getText()).setDate(NumberFormatBuilder.DatePart.values()[Math.max(0, date.getSelectedIndex())])
                .setSeparator(separator.getText()).setDigits((Integer) digits.getValue()).setSuffix(suffix.getText())
                .setRestart(restart.isSelected());
    }

    private String currentPattern() {
        return expert.isSelected() ? pattern.getText().trim() : builder().pattern();
    }

    private int currentPosition() {
        return expert.isSelected() ? (Integer) position.getValue() : builder().parsePosition();
    }

    private void updatePreview() {
        boolean e = expert.isSelected();
        for (JComponent c : new JComponent[]{prefix, date, separator, digits, suffix}) {
            c.setEnabled(!e);
        }
        restart.setEnabled(!e && date.getSelectedIndex() > 0);
        pattern.setEnabled(e);
        position.setEnabled(e);
        next.setEnabled(setNext.isSelected());
        try {
            int first = setNext.isSelected() ? (Integer) next.getValue() : 1;
            Date now = new Date();
            String p = currentPattern();
            preview.setText(String.format(Messages.NUMFMT_PREVIEW.toString(), NumberFormatBuilder.format(p, first, now),
                    NumberFormatBuilder.format(p, first + 1, now), NumberFormatBuilder.format(p, first + 2, now)));
            preview.setForeground(ModernTheme.foreground());
        } catch (Exception ex) {
            preview.setText(Messages.NUMFMT_INVALID + " " + ex.getMessage());
            preview.setForeground(Applets.DANGER);
        }
    }

    /**
     * Shows the stored format of the selected kind and user
     */
    private void load() {
        if (type.getSelectedItem() == null || user.getSelectedItem() == null) {
            return;
        }
        loading = true;
        try {
            String stored = null;
            try {
                QueryCriteria c = new QueryCriteria("inttype", typeId());
                c.addAndCondition("usersids", userId());
                Object[][] d = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname", c);
                if (d.length > 0 && d[0][0] != null) {
                    stored = String.valueOf(d[0][0]);
                }
            } catch (Exception e) {
                //none stored
            }
            Integer start = null;
            if (stored != null && stored.startsWith(FormatHandler.START_VALUE_IDENTIFIER)) {
                String[] p = stored.split(FormatHandler.START_VALUE_IDENTIFIER);
                try {
                    start = Integer.valueOf(p[1]);
                } catch (Exception e) {
                    //no start value
                }
                stored = p.length > 2 ? p[2] : "";
            }
            String pat = stored == null ? FormatHandler.INTEGERPART_IDENTIFIER : stored;
            int pos = stored == null ? 0 : FormatHandler.position(pat);
            NumberFormatBuilder b = NumberFormatBuilder.fromPattern(pat, pos);
            expert.setSelected(b == null);
            pattern.setText(pat);
            position.setValue(pos);
            if (b != null) {
                prefix.setText(b.getPrefix());
                date.setSelectedIndex(b.getDate().ordinal());
                separator.setText(b.getSeparator());
                digits.setValue(b.getDigits());
                suffix.setText(b.getSuffix());
                restart.setSelected(b.isRestart());
            }
            setNext.setSelected(start != null);
            if (start != null) {
                next.setValue(start);
            }
        } finally {
            loading = false;
        }
        updatePreview();
    }

    private void select(int typeId, int usersid) {
        for (int i = 0; i < type.getItemCount(); i++) {
            if (type.getItemAt(i).getId() == typeId) {
                type.setSelectedIndex(i);
            }
        }
        for (int i = 0; i < user.getItemCount(); i++) {
            if ((Integer) user.getItemAt(i)[0] == usersid) {
                user.setSelectedIndex(i);
            }
        }
    }

    /**
     * One row per kind: name, example of its numbers (for all users), own
     * formats of single users, "Change..." (the assistant)
     */
    private void showOverview() {
        overviewRows.removeAll();
        java.util.Map<Integer, String> forAll = new java.util.HashMap<Integer, String>();
        java.util.Map<Integer, Integer> own = new java.util.HashMap<Integer, Integer>();
        try {
            Object[][] d = QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype, usersids, cname", (String[]) null);
            for (Object[] r : d) {
                int t = Integer.parseInt(String.valueOf(r[0]));
                int u = Integer.parseInt(String.valueOf(r[1]));
                String pat = String.valueOf(r[2]);
                if (pat.startsWith(FormatHandler.START_VALUE_IDENTIFIER)) {
                    String[] p = pat.split(FormatHandler.START_VALUE_IDENTIFIER);
                    pat = p.length > 2 ? p[2] : "";
                }
                if (u == User.ADMIN_ID) {
                    forAll.put(t, pat);
                } else {
                    own.merge(t, 1, Integer::sum);
                }
            }
        } catch (Exception e) {
            //none stored
        }
        Date now = new Date();
        int row = 0;
        for (MPEnum t : FormatHandler.TYPES.values()) {
            if (HIDDEN.contains(t.getId())) {
                continue;
            }
            String pat = forAll.getOrDefault(t.getId(), FormatHandler.INTEGERPART_IDENTIFIER);
            String example;
            try {
                example = NumberFormatBuilder.format(pat, 1, now);
            } catch (Exception e) {
                example = pat;
            }
            JLabel name = new JLabel(t.getName());
            JLabel ex = new JLabel(example);
            ex.setFont(ModernTheme.font(1, Font.BOLD));
            JLabel note = new JLabel(own.containsKey(t.getId()) ? String.format(Messages.NUMWIZ_OWN.toString(), own.get(t.getId())) : "");
            note.setForeground(ModernTheme.muted());
            javax.swing.JButton change = new javax.swing.JButton(Messages.NUMWIZ_CHANGE.toString());
            change.setFocusable(false);
            final MPEnum kind = t;
            change.addActionListener(e -> openWizard(kind));
            JComponent[] cells = {name, ex, note, change};
            double[] weights = {0, 1, 0, 0};
            for (int c = 0; c < cells.length; c++) {
                java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
                g.gridx = c;
                g.gridy = row;
                g.weightx = weights[c];
                g.anchor = java.awt.GridBagConstraints.WEST;
                g.fill = c == 1 ? java.awt.GridBagConstraints.HORIZONTAL : java.awt.GridBagConstraints.NONE;
                g.insets = new java.awt.Insets(ModernTheme.px(3), c == 0 ? 0 : ModernTheme.px(14), ModernTheme.px(3), 0);
                overviewRows.add(cells[c], g);
            }
            row++;
        }
        overviewRows.revalidate();
        overviewRows.repaint();
    }

    /**
     * The assistant for one kind; afterwards the overview and the details show the new format
     *
     * @param kind
     */
    private void openWizard(MPEnum kind) {
        List<Object[]> us = new ArrayList<Object[]>();
        for (int i = 0; i < user.getItemCount(); i++) {
            us.add(user.getItemAt(i));
        }
        NumberFormatWizard w = new NumberFormatWizard(javax.swing.SwingUtilities.getWindowAncestor(this), kind, us, User.ADMIN_ID);
        w.setVisible(true);
        if (w.isSaved()) {
            reset();
        }
    }

    private void showExisting() {
        existingModel.setRowCount(0);
        existingRows.clear();
        try {
            Object[][] d = QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype, usersids, cname", (String[]) null);
            Date now = new Date();
            for (Object[] r : d) {
                int t = Integer.parseInt(String.valueOf(r[0]));
                int u = Integer.parseInt(String.valueOf(r[1]));
                String pat = String.valueOf(r[2]);
                if (pat.startsWith(FormatHandler.START_VALUE_IDENTIFIER)) {
                    String[] p = pat.split(FormatHandler.START_VALUE_IDENTIFIER);
                    pat = p.length > 2 ? p[2] : "";
                }
                String typeName = String.valueOf(t);
                for (MPEnum e : FormatHandler.TYPES.values()) {
                    if (e.getId() == t) {
                        typeName = e.getName();
                    }
                }
                String userName = String.valueOf(u);
                for (int i = 0; i < user.getItemCount(); i++) {
                    if ((Integer) user.getItemAt(i)[0] == u) {
                        userName = String.valueOf(user.getItemAt(i)[1]);
                    }
                }
                String example;
                try {
                    example = NumberFormatBuilder.format(pat, 1, now);
                } catch (Exception e) {
                    example = pat;
                }
                existingModel.addRow(new Object[]{typeName, userName, example});
                existingRows.add(new Object[]{t, u});
            }
        } catch (Exception e) {
            //none stored
        }
    }

    private void save() {
        String p = currentPattern();
        try {
            NumberFormatBuilder.format(p, 1, new Date());
            if (!p.contains("{0")) {
                throw new IllegalArgumentException(Messages.NUMFMT_NO_COUNTER.toString());
            }
        } catch (Exception ex) {
            Popup.notice(Messages.NUMFMT_INVALID + " " + ex.getMessage());
            return;
        }
        FormatHandler.store(typeId(), userId(), p, currentPosition(), setNext.isSelected() ? (Integer) next.getValue() : null);
        try {
            User u = (User) DatabaseObject.getObject(Context.getUser(), userId());
            if (locales.getSelectedItem() instanceof MPComboBoxModelItem) {
                u.getProperties().changeProperty("item.date.locale", ((MPComboBoxModelItem) locales.getSelectedItem()).getId());
                u.saveProperties();
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        setNext.setSelected(false);
        showExisting();
        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.NUMFMT_SAVED.toString(),
                ((MPEnum) type.getSelectedItem()).getName(), NumberFormatBuilder.format(p, 1, new Date())));
    }

    private void showLocale() {
        try {
            User u = (User) DatabaseObject.getObject(Context.getUser(), userId());
            String l = u.getProperties().hasProperty("item.date.locale") ? u.getProperties().getProperty("item.date.locale")
                    : User.getCurrentUser().__getLocale();
            locales.setSelectedIndex(MPComboBoxModelItem.getItemID(l, locales.getModel()));
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public final void reset() {
        showOverview();
        showExisting();
        load();
        showLocale();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }
}
