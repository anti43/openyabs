package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.ScriptFields;
import mpv5.db.common.ScriptScope;
import mpv5.db.objects.Group;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;

/**
 * The script manager: Groovy scripts for a kind of record ("For", e.g. all
 * documents) and a group - template fields, hooks on save/delete, list
 * names. Left the list of all scripts, right the editor with examples and a
 * test that shows the result and the variables of a record.
 */
public class ControlPanel_ValueProps extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "valueproperties";
    private static final String DOCS = "https://github.com/anti43/openyabs/wiki/Scripts-(EN)";
    private PropertyStore oldvalues;
    private ValueProperty dataOwner;

    /**
     * A kind of record a script is for: the stored context id, the template
     * prefix of its fields and the context to pick a test record from
     */
    static final class Kind {

        final int id;
        final String label;
        final String prefix;
        final Context test;

        Kind(int id, String label, String prefix, Context test) {
            this.id = id;
            this.label = label;
            this.prefix = prefix;
            this.test = test;
        }

        boolean isDocument() {
            return "item".equals(prefix);
        }

        boolean isContact() {
            return "contact".equals(prefix);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * What a name does: template field (any name), hooks, list names
     */
    private static final String[] USE_KEYS = {"", "modifyOnSave", "executeAfterSave", "executeAfterCreate",
        "modifyOnDelete", "IntItemLabel", "IntRevenueLabel"};

    private final DefaultListModel<ValueProperty> listModel = new DefaultListModel<>();
    private final JList<ValueProperty> list = new JList<>(listModel);
    private final JTextField name = ModernTheme.roomy(new JTextField(18));
    private final JComboBox<String> use = ModernTheme.roomyCombo(new JComboBox<String>());
    private final JComboBox<Kind> kind = ModernTheme.roomyCombo(new JComboBox<Kind>());
    private final JComboBox<Object> group = ModernTheme.roomyCombo(new JComboBox<Object>());
    private final JTextArea code = new JTextArea(14, 60);
    private final JTextArea info = (JTextArea) Applets.plainHint(" ");
    private final JTextArea vars = (JTextArea) Applets.plainHint(" ");
    private JPanel actions = new JPanel();
    private boolean loading;
    private final java.util.Map<Integer, String> groupNames = new java.util.HashMap<>();

    public ControlPanel_ValueProps() {
        build();
        refresh(null);
        setVisible(true);
    }

    /**
     * @return the kinds of record, "All documents" first
     */
    static synchronized List<Kind> kinds() {
        if (KINDS != null) {
            return KINDS;
        }
        List<Kind> k = new ArrayList<>();
        k.add(new Kind(ScriptScope.ALL_DOCUMENTS, Messages.SCRIPTS_ALL_DOCUMENTS.toString(), "item", Context.getInvoice()));
        k.add(new Kind(Context.getInvoice().getId(), Messages.TYPE_INVOICE.getValue(), "item", Context.getInvoice()));
        k.add(new Kind(Context.getOffer().getId(), Messages.TYPE_OFFER.getValue(), "item", Context.getOffer()));
        k.add(new Kind(Context.getOrder().getId(), Messages.TYPE_ORDER.getValue(), "item", Context.getOrder()));
        k.add(new Kind(Context.getCredit().getId(), Messages.TYPE_CREDIT.getValue(), "item", Context.getCredit()));
        k.add(new Kind(Context.getPartPayment().getId(), Messages.TYPE_PART_PAYMENT.getValue(), "item", Context.getPartPayment()));
        k.add(new Kind(Context.getDeposit().getId(), Messages.TYPE_DEPOSIT.getValue(), "item", Context.getDeposit()));
        k.add(new Kind(Context.getContact().getId(), Messages.SCRIPTS_ALL_CONTACTS.toString(), "contact", Context.getContact()));
        k.add(new Kind(Context.getCustomer().getId(), Messages.TYPE_CUSTOMER.getValue(), "contact", Context.getCustomer()));
        k.add(new Kind(Context.getSupplier().getId(), Messages.TYPE_SUPPLIER.getValue(), "contact", Context.getSupplier()));
        k.add(new Kind(Context.getManufacturer().getId(), Messages.TYPE_MANUFACTURER.getValue(), "contact", Context.getManufacturer()));
        k.add(new Kind(Context.getProduct().getId(), Messages.TYPE_PRODUCT.getValue(), "product", Context.getProduct()));
        k.add(new Kind(Context.getProductOrder().getId(), Messages.TYPE_PRODUCT_ORDER.getValue(), "productorder", Context.getProductOrder()));
        k.add(new Kind(Context.getReminder().getId(), Messages.TYPE_REMINDER.getValue(), "reminder", Context.getReminder()));
        k.add(new Kind(Context.getConversation().getId(), Messages.TYPE_CONVERSATION.getValue(), "conversation", Context.getConversation()));
        k.add(new Kind(Context.getMassprint().getId(), Messages.TYPE_MASSPRINT.getValue(), "massprintrule", Context.getMassprint()));
        KINDS = java.util.Collections.unmodifiableList(k);
        return KINDS;
    }

    private static List<Kind> KINDS;

    /**
     * @param id a stored context id
     * @return its kind, null if unknown
     */
    static Kind kindOf(int id) {
        for (Kind k : kinds()) {
            if (k.id == id) {
                return k;
            }
        }
        return null;
    }

    /**
     * @param key the name of a script
     * @return true if it is a hook or list name, not a template field
     */
    static boolean isSpecial(String key) {
        for (int i = 1; i < USE_KEYS.length; i++) {
            if (USE_KEYS[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param key
     * @return the name as stored: special names as they are, template fields
     * with letters, digits and _ only
     */
    static String cleanName(String key) {
        String k = key == null ? "" : key.trim();
        return isSpecial(k) ? k : k.replaceAll("[^\\w]", "");
    }

    /**
     * @param k the kind
     * @param key a template field name
     * @return where the result appears, e.g. "&lt;item.property.anrede&gt;"
     */
    static String fieldText(Kind k, String key) {
        if (k != null && k.isContact()) {
            return String.format(Messages.SCRIPTS_FIELD_CONTACT.toString(),
                    "<item.contact.property." + key + ">", "<contact.property." + key + ">");
        }
        String prefix = k == null ? "item" : k.prefix;
        return String.format(Messages.SCRIPTS_KEY_FIELD.toString(), "<" + prefix + ".property." + key + ">");
    }

    /**
     * @param stored a stored script value (#...#)
     * @return the script without the outer script symbols
     */
    static String unwrap(String stored) {
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        String s = stored == null ? "" : stored;
        if (s.length() >= 2 * sm.length() && s.startsWith(sm) && s.endsWith(sm)) {
            s = s.substring(sm.length(), s.length() - sm.length());
        }
        return s;
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void build() {
        setLayout(new BorderLayout());
        setOpaque(false);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(l, value, index, selected, focus);
                if (value instanceof ValueProperty) {
                    ValueProperty p = (ValueProperty) value;
                    Kind k = kindOf(p.__getContextids());
                    String g = p.__getGroupsids() > 1 && groupNames.containsKey(p.__getGroupsids())
                            ? " \u00b7 " + groupNames.get(p.__getGroupsids()) : "";
                    setText("<html><b>" + esc(p.getKey()) + "</b><br><small>"
                            + esc((k == null ? "? (" + p.__getContextids() + ")" : k.label) + g) + "</small></html>");
                }
                return this;
            }
        });
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading && list.getSelectedValue() != null) {
                show(list.getSelectedValue());
            }
        });
        JScrollPane listScroll = new JScrollPane(list);

        Messages[] labels = {Messages.SCRIPTS_USE_FIELD, Messages.SCRIPTS_USE_BEFORESAVE,
            Messages.SCRIPTS_USE_AFTERSAVE, Messages.SCRIPTS_USE_AFTERCREATE,
            Messages.SCRIPTS_USE_BEFOREDELETE, Messages.SCRIPTS_USE_ITEMLABEL,
            Messages.SCRIPTS_USE_REVENUELABEL};
        for (Messages m : labels) {
            use.addItem(m.toString());
        }
        use.addActionListener(e -> {
            if (loading) {
                return;
            }
            int i = use.getSelectedIndex();
            if (i > 0) {
                name.setText(USE_KEYS[i]);
            } else if (isSpecial(name.getText().trim())) {
                name.setText("");
            }
            explain();
        });
        name.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                explainLater();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                explainLater();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                explainLater();
            }
        });
        kind.setModel(new DefaultComboBoxModel<>(kinds().toArray(new Kind[0])));
        kind.addActionListener(e -> explain());

        code.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ModernTheme.baseFont().getSize()));
        code.setTabSize(4);
        code.setLineWrap(false);
        JScrollPane codeScroll = new JScrollPane(code);
        codeScroll.setPreferredSize(new java.awt.Dimension(ModernTheme.px(520), ModernTheme.px(260)));

        JButton examples = new JButton(Messages.SCRIPTS_EXAMPLES.toString());
        examples.addActionListener(e -> examples(examples));
        JButton test = new JButton(Messages.SCRIPTS_TEST.toString());
        test.addActionListener(e -> test());
        JPanel codeButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        codeButtons.setOpaque(false);
        codeButtons.add(examples);
        codeButtons.add(test);

        Card editor = new Card(Messages.SCRIPTS_EDITOR.toString())
                .row(Card.field(Messages.SCRIPTS_NAME.toString(), name, 1), Card.field(Messages.SCRIPTS_USE_AS.toString(), use, 1))
                .addFull(info)
                .row(Card.field(Messages.SCRIPTS_FOR.toString(), kind, 1), Card.field(Messages.SCRIPTS_GROUP.toString(), group, 1))
                .addFull(codeScroll, 1)
                .addFull(vars)
                .addFull(codeButtons)
                .addFull(Applets.plainHint(Messages.SCRIPTS_HELP.toString()));

        add(Applets.treePage(Messages.SCRIPTS_LIST.toString(), listScroll, Messages.SCRIPTS_LIST_HINT.toString(), editor),
                BorderLayout.CENTER);

        JButton toolbar = new JButton(Messages.SCRIPTS_TOOLBAR.toString());
        toolbar.setToolTipText(Messages.SCRIPTS_TOOLBAR_INFO.toString());
        toolbar.addActionListener(e -> toolbarActions());
        JButton docs = new JButton(Messages.SCRIPTS_DOCS.toString());
        docs.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(DOCS));
            } catch (Exception ex) {
                Popup.notice(DOCS);
            }
        });
        JButton newScript = new JButton(Messages.SCRIPTS_NEW.toString());
        newScript.addActionListener(e -> newScript());
        JButton delete = Applets.danger(new JButton(Messages.ACTION_DELETE.toString()));
        delete.addActionListener(e -> delete());
        JButton saveNew = new JButton(Messages.SCRIPTS_SAVE_NEW.toString());
        saveNew.addActionListener(e -> save(true));
        JButton save = new mpv5.ui.modern.PrimaryButton(Messages.SCRIPTS_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save(false));
        actions = Applets.actionBar(new JComponent[]{toolbar, docs}, new JComponent[]{newScript, delete, saveNew, save});
        newScript();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    // </editor-fold>

    private void explainLater() {
        SwingUtilities.invokeLater(this::explain);
    }

    /**
     * The line under the name (what it does) and the variables of the kind
     */
    private void explain() {
        String key = name.getText() == null ? "" : name.getText().trim();
        int found = 0;
        for (int i = 1; i < USE_KEYS.length; i++) {
            if (USE_KEYS[i].equals(key)) {
                found = i;
            }
        }
        if (use.getSelectedIndex() != found && (found > 0 || !isSpecial(key))) {
            boolean l = loading;
            loading = true;
            use.setSelectedIndex(found);
            loading = l;
        }
        Kind k = (Kind) kind.getSelectedItem();
        String text;
        if (found == 0) {
            text = key.isEmpty() ? Messages.SCRIPTS_KEY_EMPTY.toString() : fieldText(k, cleanName(key));
        } else if (found >= 5) {
            text = Messages.SCRIPTS_KEY_LABEL.toString();
        } else {
            text = Messages.SCRIPTS_KEY_HOOK.toString();
        }
        info.setText(text);
        if (k == null || k.isDocument()) {
            vars.setText(Messages.SCRIPTS_VARS_ITEM.toString());
        } else if (k.isContact()) {
            vars.setText(Messages.SCRIPTS_VARS_CONTACT.toString());
        } else {
            vars.setText(String.format(Messages.SCRIPTS_VARS_OTHER.toString(), k.prefix));
        }
    }

    private void refresh(final ValueProperty select) {
        loading = true;
        try {
            listModel.clear();
            List<ValueProperty> l = new ArrayList<>(ValueProperty.getGroupProperties());
            final List<Kind> order = kinds();
            l.sort(Comparator.comparingInt((ValueProperty p) -> {
                Kind k = kindOf(p.__getContextids());
                return k == null ? 999 : order.indexOf(k);
            }).thenComparing(p -> String.valueOf(p.getKey()).toLowerCase()));
            for (ValueProperty p : l) {
                listModel.addElement(p);
            }
            DefaultComboBoxModel<Object> groups = new DefaultComboBoxModel<>();
            try {
                groupNames.clear();
                for (DatabaseObject g : Group.getObjects(Context.getGroup())) {
                    groups.addElement(g);
                    groupNames.put(g.__getIDS(), String.valueOf(g));
                }
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }
            Object sel = group.getSelectedItem();
            group.setModel(groups);
            if (sel != null) {
                group.setSelectedItem(sel);
            }
            if (select != null) {
                for (int i = 0; i < listModel.size(); i++) {
                    if (listModel.get(i).__getIDS().equals(select.__getIDS())) {
                        list.setSelectedIndex(i);
                        list.ensureIndexIsVisible(i);
                    }
                }
            }
        } finally {
            loading = false;
        }
    }

    private void selectGroup(int id) {
        for (int i = 0; i < group.getItemCount(); i++) {
            Object g = group.getItemAt(i);
            if (g instanceof Group && ((Group) g).__getIDS() == id) {
                group.setSelectedIndex(i);
                return;
            }
        }
        if (group.getItemCount() > 0) {
            group.setSelectedIndex(0);
        }
    }

    private void show(ValueProperty p) {
        dataOwner = p;
        loading = true;
        try {
            name.setText(p.getKey());
            Kind k = kindOf(p.__getContextids());
            if (k != null) {
                for (int i = 0; i < kind.getItemCount(); i++) {
                    if (kind.getItemAt(i).id == k.id) {
                        kind.setSelectedIndex(i);
                    }
                }
            }
            selectGroup(p.__getGroupsids());
            code.setText(unwrap(String.valueOf(p.getValue())));
            code.setCaretPosition(0);
        } finally {
            loading = false;
        }
        explain();
    }

    private void newScript() {
        dataOwner = null;
        loading = true;
        try {
            list.clearSelection();
            name.setText("");
            kind.setSelectedIndex(0);
            selectGroup(1);
            code.setText("");
        } finally {
            loading = false;
        }
        explain();
        name.requestFocusInWindow();
    }

    private void save(boolean asNew) {
        String key = cleanName(name.getText());
        if (key.isEmpty()) {
            Popup.notice(Messages.SCRIPTS_NAME_MISSING);
            return;
        }
        name.setText(key);
        Kind k = (Kind) kind.getSelectedItem();
        Object g = group.getSelectedItem();
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        String script = code.getText().trim().isEmpty() ? "return \"\"" : code.getText();
        String val = sm + script + sm;
        ValueProperty p = asNew || dataOwner == null ? null : dataOwner;
        Group grp = g instanceof Group ? (Group) g : Group.getDefault();
        if (p == null) {
            p = new ValueProperty(key, val, Context.getValueProperties(), grp);
        } else {
            p.setValueObj(val);
            p.setCname(key);
            p.setGroup(grp);
        }
        p.setContextids(k.id);
        if (p.save()) {
            dataOwner = p;
            refresh(p);
            mpv5.ui.dialogs.Notificator.raiseNotification(String.format(Messages.SCRIPTS_SAVED.toString(), key), false);
        }
    }

    private void delete() {
        if (dataOwner != null && Popup.Y_N_dialog(String.format(Messages.SCRIPTS_DELETE_Q.toString(), dataOwner.getKey()))) {
            dataOwner.delete();
            refresh(null);
            newScript();
        }
    }

    /**
     * The examples for the chosen kind: a click puts name and script into the
     * editor (not saved yet)
     */
    private void examples(JComponent at) {
        Kind k = (Kind) kind.getSelectedItem();
        JPopupMenu m = new JPopupMenu();
        for (final String[] ex : Examples.of(k == null ? "item" : k.prefix)) {
            JMenuItem i = new JMenuItem(ex[0]);
            i.addActionListener(e -> {
                if (dataOwner == null || code.getText().trim().isEmpty() || Popup.Y_N_dialog(Messages.SCRIPTS_EXAMPLE_REPLACE)) {
                    if (code.getText().trim().isEmpty() || dataOwner != null) {
                        dataOwner = null;
                        list.clearSelection();
                    }
                    name.setText(ex[1]);
                    code.setText(ex[2]);
                    code.setCaretPosition(0);
                    explain();
                }
            });
            m.add(i);
        }
        m.show(at, 0, at.getHeight());
    }

    /**
     * Runs the script with a chosen record: the result, and the variables of
     * the record as the script writes them
     */
    private void test() {
        Kind k = (Kind) kind.getSelectedItem();
        if (k == null) {
            return;
        }
        DatabaseObject s = Search2.showSearchFor(k.test);
        if (s == null) {
            return;
        }
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        String result;
        try {
            result = s.evaluateAll(sm + code.getText() + sm, true);
        } catch (Exception e) {
            result = Messages.SCRIPT_ERROR + " " + (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        String key = cleanName(name.getText());
        JTextArea res = new JTextArea(result, Math.min(8, Math.max(2, result.split("\n").length)), 50);
        res.setEditable(false);
        res.setLineWrap(true);
        res.setWrapStyleWord(true);
        String prefix = s.getType();
        List<String[]> rows = new ScriptFields(s).describe(prefix);
        JTable t = new JTable(rows.toArray(new Object[0][]),
                new Object[]{Messages.SCRIPTS_VARIABLE.toString(), Messages.VALUE.toString()}) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ModernTheme.style(t);
        t.setAutoCreateRowSorter(true);
        JScrollPane ts = new JScrollPane(t);
        ts.setPreferredSize(new java.awt.Dimension(ModernTheme.px(620), ModernTheme.px(320)));
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        top.add(new JLabel(key.isEmpty() || isSpecial(key) ? Messages.SCRIPTS_RESULT.toString()
                : Messages.SCRIPTS_RESULT + " " + (k.isContact() ? "<item.contact.property." + key + ">" : "<" + k.prefix + ".property." + key + ">")), BorderLayout.NORTH);
        top.add(new JScrollPane(res), BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        JPanel bottom = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        bottom.add(new JLabel(Messages.SCRIPTS_VARIABLES.toString()), BorderLayout.NORTH);
        bottom.add(ts, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.CENTER);
        javax.swing.JOptionPane.showMessageDialog(this, p, String.valueOf(s), javax.swing.JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Buttons with a script in a toolbar (was View > Add toolbar action)
     */
    private void toolbarActions() {
        mpv5.ui.dialogs.Wizard w = new mpv5.ui.dialogs.Wizard(false);
        w.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        w.setAlwaysOnTop(false);
        w.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        w.setLocationRelativeTo(mpv5.YabsViewProxy.instance().getIdentifierFrame());
        w.addPanel(new wizard_Toolbar1(w));
        w.showWizAsChild();
    }

    @Override
    public void setValues(PropertyStore values) {
        oldvalues = values;
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        setValues(oldvalues);
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }

    /**
     * Ready-made scripts per kind: label, name, script
     */
    static final class Examples {

        private Examples() {
        }

        static List<String[]> of(String prefix) {
            List<String[]> l = new ArrayList<>();
            if ("item".equals(prefix)) {
                l.add(new String[]{Messages.SCRIPTS_EX_SALUTATION.toString(), "anrede", Messages.SCRIPTS_EX_SALUTATION_ITEM_CODE.toString()});
                l.add(new String[]{Messages.SCRIPTS_EX_AMOUNT.toString(), "betragshinweis", Messages.SCRIPTS_EX_AMOUNT_CODE.toString()});
                l.add(new String[]{Messages.SCRIPTS_EX_CUSTOMERNO.toString(), "kundennr", Messages.SCRIPTS_EX_CUSTOMERNO_CODE.toString()});
                l.add(new String[]{Messages.SCRIPTS_EX_DATE.toString(), "datum", Messages.SCRIPTS_EX_DATE_CODE.toString()});
                l.add(new String[]{Messages.SCRIPTS_EX_TRIM.toString(), "modifyOnSave", "dbo.setDescription(String.valueOf(dbo.__getDescription() ?: '').trim())"});
                l.add(new String[]{Messages.SCRIPTS_EX_LABEL.toString(), "IntItemLabel", "dbo.__getCnumber() + ' ' + (item.contact?.cname ?: '')"});
            } else if ("contact".equals(prefix)) {
                l.add(new String[]{Messages.SCRIPTS_EX_SALUTATION.toString(), "anrede", Messages.SCRIPTS_EX_SALUTATION_CONTACT_CODE.toString()});
                l.add(new String[]{Messages.SCRIPTS_EX_FULLNAME.toString(), "vollername", "[contact.title, contact.prename, contact.cname].findAll { it }.join(' ')"});
            } else {
                l.add(new String[]{Messages.SCRIPTS_EX_NUMBER.toString(), "nummer", prefix + ".cnumber + ' - ' + " + prefix + ".cname"});
            }
            return l;
        }
    }
}
