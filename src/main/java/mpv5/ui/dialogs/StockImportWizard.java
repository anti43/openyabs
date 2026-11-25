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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import mpv5.globals.Messages;
import mpv5.handler.StockService;
import mpv5.handler.StockService.Entry;
import mpv5.logging.Log;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.csv.CsvImport;
import mpv5.utils.csv.CsvImport.Field;
import mpv5.utils.csv.CsvImport.Kind;

/**
 * Wizard of the stock management: a pasted list or a CSV file, its settings
 * (separator; columns of code and quantity), a preview with the products
 * found and the ones to create. The entries go to the stock list, nothing is
 * booked or created here.
 */
public final class StockImportWizard extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Color RED = new Color(190, 40, 40);
    private static final Color GREEN = new Color(30, 130, 60);

    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final JLabel[] steps = {
        new JLabel(Messages.STOCK_WIZ_STEP_SOURCE.toString()),
        new JLabel(Messages.STOCK_WIZ_STEP_SETTINGS.toString()),
        new JLabel(Messages.STOCK_WIZ_STEP_PREVIEW.toString())};
    private int step;

    //step 1
    private final JRadioButton pasteSource = new JRadioButton(Messages.STOCK_WIZ_PASTE.toString(), true);
    private final JRadioButton fileSource = new JRadioButton(Messages.STOCK_WIZ_FILE.toString());
    private final JTextArea text = new JTextArea(10, 50);
    private final JTextField file = new JTextField(36);
    private final JLabel sourceError = new JLabel(" ");

    //step 2
    private final CardLayout settingsCards = new CardLayout();
    private final JPanel settings = new JPanel(settingsCards);
    private final JComboBox<String> separator = new JComboBox<String>(new String[]{
        Messages.STOCK_SEP_COMMA.toString(), Messages.STOCK_SEP_SEMICOLON.toString(),
        Messages.STOCK_SEP_SPACE.toString(), Messages.STOCK_SEP_LINE.toString()});
    private final JLabel textInfo = new JLabel(" ");
    private final JLabel detected = new JLabel(" ");
    private final JCheckBox header = new JCheckBox(Messages.CSV_HEADER.toString(), true);
    private final JComboBox<String> codeColumn = new JComboBox<String>();
    private final JComboBox<String> quantityColumn = new JComboBox<String>();
    private final JTable raw = new JTable();

    //step 3
    private final JCheckBox create;
    private final JCheckBox ignoreCase;
    private final JLabel summary = new JLabel(" ");
    private final PreviewModel previewModel = new PreviewModel();
    private final JTable preview = new JTable(previewModel);

    private final JButton back = ModernTheme.actionButton(new JButton(Messages.CSV_BACK.toString()), null);
    private final PrimaryButton next = new PrimaryButton(Messages.CSV_NEXT.toString(), null);
    private final JButton cancel = ModernTheme.actionButton(new JButton(Messages.CSV_CANCEL.toString()), null);

    private CsvImport.Data data;
    private Field[] mapping = new Field[0];
    private List<Entry> entries = new ArrayList<Entry>();
    private final List<Integer> skipped = new ArrayList<Integer>();
    private final Consumer<List<Entry>> target;
    private boolean updating;

    /**
     * Opens the wizard
     *
     * @param createUnknown the setting "create unknown products" of the stock
     * management (changed here as well)
     * @param target gets the entries (with product found and create flag)
     */
    public static void open(JCheckBox createUnknown, JCheckBox ignoreCaseSetting, Consumer<List<Entry>> target) {
        Window owner = null;
        try {
            owner = mpv5.YabsViewProxy.instance().getIdentifierFrame();
        } catch (Exception e) {
            //no main window
        }
        StockImportWizard d = new StockImportWizard(owner, createUnknown, ignoreCaseSetting, target);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    private StockImportWizard(Window owner, JCheckBox createUnknown, JCheckBox ignoreCaseSetting, Consumer<List<Entry>> target) {
        super(owner, Messages.STOCK_WIZARD_TITLE.toString(), ModalityType.APPLICATION_MODAL);
        this.target = target;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //the same setting as in the stock management, kept in sync
        create = new JCheckBox(Messages.STOCK_AUTOCREATE.toString(), createUnknown.isSelected());
        create.setToolTipText(Messages.STOCK_AUTOCREATE_INFO.toString());
        //the same setting as in the stock management: changing it here changes it there (and looks up again)
        ignoreCase = new JCheckBox(Messages.STOCK_IGNORE_CASE.toString(), ignoreCaseSetting.isSelected());
        ignoreCase.setToolTipText(Messages.STOCK_IGNORE_CASE_INFO.toString());
        ignoreCase.addActionListener(e -> {
            if (ignoreCaseSetting.isSelected() != ignoreCase.isSelected()) {
                ignoreCaseSetting.doClick();
            }
            if (step == 2) {
                buildEntries();
            }
        });
        create.addActionListener(e -> {
            createUnknown.setSelected(create.isSelected());
            for (Entry en : entries) {
                if (en.product == null) {
                    en.create = create.isSelected();
                }
            }
            refreshSummary();
            previewModel.fireTableDataChanged();
        });

        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        root.setBackground(ModernTheme.cardBackground());
        root.setBorder(ModernTheme.padding(14, 16));
        JPanel stepBar = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(18), 0));
        stepBar.setOpaque(false);
        for (JLabel l : steps) {
            stepBar.add(l);
        }
        root.add(stepBar, BorderLayout.NORTH);
        pages.setOpaque(false);
        pages.add(sourcePage(), "0");
        pages.add(settingsPage(), "1");
        pages.add(previewPage(), "2");
        root.add(pages, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(back);
        buttons.add(next);
        root.add(buttons, BorderLayout.SOUTH);
        back.addActionListener(e -> go(step - 1));
        next.addActionListener(e -> nextPressed());
        cancel.addActionListener(e -> dispose());
        getRootPane().registerKeyboardAction(e -> dispose(), javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setContentPane(root);
        go(0);
        setSize(ModernTheme.px(860), ModernTheme.px(600));
        setMinimumSize(new java.awt.Dimension(ModernTheme.px(620), ModernTheme.px(460)));
    }

    private static JComponent wrapped(String text) {
        JComponent h = Applets.plainHint(text);
        //a first width: the text wraps already in the first layout
        h.setSize(ModernTheme.px(780), 1);
        return h;
    }

    private static GridBagConstraints gbc(int x, int y, int w) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(ModernTheme.px(3), ModernTheme.px(4), ModernTheme.px(3), ModernTheme.px(4));
        return c;
    }

    // ------------------------------------------------------------ step 1
    private JComponent sourcePage() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        g.add(pasteSource);
        g.add(fileSource);
        JButton clipboard = ModernTheme.actionButton(new JButton(Messages.STOCK_WIZ_FROM_CLIPBOARD.toString()), "/images/16/editpaste.png");
        clipboard.addActionListener(e -> {
            String t = clipboard();
            if (t != null) {
                text.setText(t);
                pasteSource.setSelected(true);
            }
        });
        JButton choose = ModernTheme.actionButton(new JButton(Messages.CSV_CHOOSE.toString()), null);
        choose.addActionListener(e -> chooseFile());
        text.setFont(ModernTheme.font(1, Font.PLAIN));
        text.setLineWrap(true);
        String t = clipboard();
        if (t != null && t.trim().length() > 0 && t.length() < 200000) {
            text.setText(t);
        }
        text.getDocument().addDocumentListener(new SimpleListener(() -> pasteSource.setSelected(true)));
        file.getDocument().addDocumentListener(new SimpleListener(() -> {
            fileSource.setSelected(true);
            data = null;
        }));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int y = 0;
        form.add(wrapped(Messages.STOCK_WIZ_SOURCE_INFO.toString()), gbc(0, y++, 3));
        pasteSource.setOpaque(false);
        fileSource.setOpaque(false);
        form.add(pasteSource, gbc(0, y, 2));
        GridBagConstraints c = gbc(2, y++, 1);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        form.add(clipboard, c);
        JScrollPane sp = new JScrollPane(text);
        c = gbc(0, y++, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        form.add(sp, c);
        form.add(fileSource, gbc(0, y++, 3));
        c = gbc(0, y, 2);
        c.weightx = 1;
        form.add(file, c);
        form.add(choose, gbc(2, y++, 1));
        sourceError.setForeground(RED);
        form.add(sourceError, gbc(0, y, 3));
        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private static String clipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV", "csv", "txt", "tsv"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file.setText(fc.getSelectedFile().getAbsolutePath());
            fileSource.setSelected(true);
        }
    }

    // ------------------------------------------------------------ step 2
    private JComponent settingsPage() {
        settings.setOpaque(false);
        JPanel textPage = new JPanel(new GridBagLayout());
        textPage.setOpaque(false);
        textPage.add(new JLabel(Messages.STOCK_PASTE_SEPARATOR.toString()), gbc(0, 0, 1));
        textPage.add(separator, gbc(1, 0, 1));
        textInfo.setForeground(ModernTheme.muted());
        textPage.add(textInfo, gbc(0, 1, 2));
        GridBagConstraints fill = gbc(2, 2, 1);
        fill.weighty = 1;
        fill.weightx = 1;
        textPage.add(new JLabel(), fill);
        separator.addActionListener(e -> textInfo.setText(String.format(Messages.STOCK_WIZ_CODES.toString(),
                StockService.fromText(text.getText(), sep(), ignoreCase.isSelected()).size())));

        JPanel filePage = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        filePage.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        detected.setForeground(ModernTheme.muted());
        form.add(detected, gbc(0, 0, 2));
        header.setOpaque(false);
        form.add(header, gbc(0, 1, 2));
        form.add(new JLabel(Messages.STOCK_WIZ_CODE_COLUMN.toString()), gbc(0, 2, 1));
        form.add(codeColumn, gbc(1, 2, 1));
        form.add(new JLabel(Messages.STOCK_WIZ_QTY_COLUMN.toString()), gbc(0, 3, 1));
        form.add(quantityColumn, gbc(1, 3, 1));
        GridBagConstraints wide = gbc(0, 4, 3);
        wide.weightx = 1;
        form.add(wrapped(Messages.STOCK_WIZ_COLUMNS_INFO.toString()), wide);
        header.addActionListener(e -> {
            if (data != null) {
                data.header = header.isSelected();
                columns();
            }
        });
        filePage.add(form, BorderLayout.NORTH);
        raw.setEnabled(false);
        raw.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ModernTheme.style(raw);
        filePage.add(new JScrollPane(raw), BorderLayout.CENTER);

        settings.add(textPage, "text");
        settings.add(filePage, "file");
        return settings;
    }

    private String sep() {
        return StockService.SEPARATORS[Math.max(0, separator.getSelectedIndex())];
    }

    private boolean loadSource() {
        sourceError.setText(" ");
        if (pasteSource.isSelected()) {
            if (text.getText().trim().isEmpty()) {
                sourceError.setText(Messages.STOCK_WIZ_NO_DATA.toString());
                return false;
            }
            int i = java.util.Arrays.asList(StockService.SEPARATORS).indexOf(StockService.guessSeparator(text.getText()));
            separator.setSelectedIndex(Math.max(0, i));
            settingsCards.show(settings, "text");
            return true;
        }
        File f = new File(file.getText().trim());
        if (file.getText().trim().isEmpty() || !f.isFile()) {
            sourceError.setText(Messages.CSV_NO_FILE.toString());
            return false;
        }
        if (data == null) {
            try {
                data = CsvImport.read(f);
            } catch (Exception e) {
                Log.Debug(this, e);
                sourceError.setText(String.valueOf(e.getMessage()));
                return false;
            }
            header.setSelected(data.header);
            columns();
        }
        if (data.rows.isEmpty()) {
            sourceError.setText(Messages.CSV_EMPTY_FILE.toString());
            return false;
        }
        String sep = data.separator == '\t' ? "Tab" : String.valueOf(data.separator);
        detected.setText(String.format(Messages.STOCK_WIZ_DETECTED.toString(), data.charset.displayName(), sep, data.rows.size()));
        settingsCards.show(settings, "file");
        return true;
    }

    /**
     * Column names, guessed mapping, code and quantity column, raw preview
     */
    private void columns() {
        updating = true;
        try {
            int n = data.columns();
            String[] names = new String[n];
            for (int i = 0; i < n; i++) {
                names[i] = data.columnName(i);
            }
            mapping = data.header ? CsvImport.guess(names, Kind.PRODUCTS) : new Field[n];
            codeColumn.removeAllItems();
            quantityColumn.removeAllItems();
            quantityColumn.addItem(Messages.STOCK_WIZ_QTY_NONE.toString());
            for (String s : names) {
                codeColumn.addItem(s);
                quantityColumn.addItem(s);
            }
            if (n > 0) {
                codeColumn.setSelectedIndex(StockService.codeColumn(mapping));
            }
            quantityColumn.setSelectedIndex(StockService.quantityColumn(mapping) + 1);
            DefaultTableModel m = new DefaultTableModel(names, 0);
            for (int r = data.firstData(); r < data.rows.size() && m.getRowCount() < 30; r++) {
                m.addRow(data.rows.get(r));
            }
            raw.setModel(m);
            for (int i = 0; i < raw.getColumnCount(); i++) {
                raw.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(130));
            }
        } finally {
            updating = false;
        }
    }

    // ------------------------------------------------------------ step 3
    private JComponent previewPage() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setOpaque(false);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.PAGE_AXIS));
        create.setOpaque(false);
        create.setFont(ModernTheme.font(1, Font.BOLD));
        JComponent info = wrapped(Messages.STOCK_AUTOCREATE_INFO.toString());
        summary.setFont(ModernTheme.font(1, Font.PLAIN));
        ignoreCase.setOpaque(false);
        ignoreCase.setFont(ModernTheme.font(1, Font.PLAIN));
        for (JComponent c : new JComponent[]{ignoreCase, create, info, summary}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(4)));
        }
        p.add(top, BorderLayout.NORTH);
        preview.setRowHeight(ModernTheme.px(24));
        preview.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                Entry e = entries.get(r);
                if (!sel) {
                    setForeground(e.product != null ? ModernTheme.foreground() : e.create ? GREEN : RED);
                }
                setHorizontalAlignment(c == 1 ? RIGHT : LEFT);
                return this;
            }
        });
        ModernTheme.style(preview);
        int[] w = {180, 70, 320, 110};
        for (int i = 0; i < w.length; i++) {
            preview.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(w[i]));
        }
        p.add(new JScrollPane(preview), BorderLayout.CENTER);
        return p;
    }

    private void buildEntries() {
        skipped.clear();
        if (pasteSource.isSelected()) {
            entries = StockService.fromText(text.getText(), sep(), ignoreCase.isSelected());
        } else {
            int q = quantityColumn.getSelectedIndex() - 1;
            entries = StockService.fromCsv(data, Math.max(0, codeColumn.getSelectedIndex()), q, mapping, skipped, ignoreCase.isSelected());
        }
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        try {
            StockService.lookup(entries, ignoreCase.isSelected());
        } finally {
            setCursor(java.awt.Cursor.getDefaultCursor());
        }
        for (Entry e : entries) {
            e.create = e.product == null && create.isSelected();
        }
        previewModel.fireTableDataChanged();
        refreshSummary();
    }

    private void refreshSummary() {
        int found = 0;
        int created = 0;
        for (Entry e : entries) {
            found += e.product != null ? 1 : 0;
            created += e.product == null && e.create ? 1 : 0;
        }
        String s = String.format(Messages.STOCK_WIZ_SUMMARY.toString(), entries.size(), found, entries.size() - found, created);
        if (!skipped.isEmpty()) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < skipped.size() && i < 20; i++) {
                b.append(i > 0 ? ", " : "").append(skipped.get(i));
            }
            if (skipped.size() > 20) {
                b.append(", ...");
            }
            s += " · " + String.format(Messages.STOCK_SKIPPED_ROWS.toString(), skipped.size(), b);
        }
        summary.setText(s);
        updateButtons();
    }

    private final class PreviewModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return Messages.STOCK_COL_CODE.toString();
                case 1:
                    return Messages.STOCK_COL_QUANTITY.toString();
                case 2:
                    return Messages.STOCK_COL_PRODUCT.toString();
                default:
                    return Messages.STOCK_COL_CREATE.toString();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 3 && entries.get(r).product == null;
        }

        @Override
        public Object getValueAt(int r, int c) {
            Entry e = entries.get(r);
            switch (c) {
                case 0:
                    return e.code;
                case 1:
                    return e.quantity.stripTrailingZeros().toPlainString();
                case 2:
                    return e.product == null ? Messages.STOCK_NOT_FOUND.toString()
                            : e.product.__getCname() + " (" + e.product.__getCnumber() + ")";
                default:
                    return e.product == null && e.create;
            }
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            entries.get(r).create = Boolean.TRUE.equals(v);
            fireTableRowsUpdated(r, r);
            refreshSummary();
        }
    }

    // ------------------------------------------------------------ navigation
    private void nextPressed() {
        if (step == 0) {
            if (loadSource()) {
                if (pasteSource.isSelected()) {
                    textInfo.setText(String.format(Messages.STOCK_WIZ_CODES.toString(), StockService.fromText(text.getText(), sep(), ignoreCase.isSelected()).size()));
                }
                go(1);
            }
        } else if (step == 1) {
            buildEntries();
            go(2);
        } else {
            if (preview.isEditing()) {
                preview.getCellEditor().stopCellEditing();
            }
            target.accept(entries);
            dispose();
        }
    }

    private void go(int s) {
        step = Math.max(0, Math.min(2, s));
        cards.show(pages, String.valueOf(step));
        for (int i = 0; i < steps.length; i++) {
            steps[i].setForeground(i == step ? ModernTheme.accent() : ModernTheme.muted());
            steps[i].setFont(ModernTheme.font(2, i == step ? Font.BOLD : Font.PLAIN));
        }
        updateButtons();
    }

    private void updateButtons() {
        back.setVisible(step > 0);
        if (step < 2) {
            next.setText(Messages.CSV_NEXT.toString());
            next.setEnabled(true);
        } else {
            next.setText(String.format(Messages.STOCK_WIZARD_TAKE.toString(), entries.size()));
            next.setEnabled(!entries.isEmpty());
        }
    }

    /**
     * Runs an action on every change of a document
     */
    private static final class SimpleListener implements javax.swing.event.DocumentListener {

        private final Runnable r;

        SimpleListener(Runnable r) {
            this.r = r;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            r.run();
        }
    }
}
