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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.csv.CsvImport;
import mpv5.utils.csv.CsvImport.Field;
import mpv5.utils.csv.CsvImport.Kind;

/**
 * Imports a CSV file as customers, suppliers, manufacturers or products in
 * three steps: the file (encoding, separator and header detected), the columns
 * (assignment guessed from the column names) with a live preview and a check
 * of every row, and the result (imported, skipped and why).
 */
public final class CsvImportDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final int PREVIEW_ROWS = 50;
    private static final Charset[] CHARSETS = {StandardCharsets.UTF_8, Charset.forName("windows-1252"), StandardCharsets.ISO_8859_1, StandardCharsets.UTF_16};
    private static final char[] SEPARATORS = {';', ',', '\t', '|'};

    /**
     * A choice in the "import as" box of a column
     */
    private static final class Target {

        final Field field;

        Target(Field field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return field == null ? Messages.CSV_NOT_IMPORTED.toString() : field.toString();
        }

        //the box of a column opens with its current choice
        @Override
        public boolean equals(Object o) {
            return o instanceof Target && ((Target) o).field == field;
        }

        @Override
        public int hashCode() {
            return field == null ? 0 : field.hashCode();
        }
    }

    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private final JLabel[] steps = {
        new JLabel(Messages.CSV_STEP_FILE.toString()),
        new JLabel(Messages.CSV_STEP_MAP.toString()),
        new JLabel(Messages.CSV_STEP_RESULT.toString())};
    private int step;

    //step 1
    private final JTextField file = new JTextField(30);
    private final JComboBox<Kind> kind = new JComboBox<Kind>(Kind.values());
    private final JComboBox<String> charset = new JComboBox<String>();
    private final JComboBox<String> separator = new JComboBox<String>(new String[]{
        Messages.CSV_SEP_SEMICOLON.toString(), Messages.CSV_SEP_COMMA.toString(), Messages.CSV_SEP_TAB.toString(), Messages.CSV_SEP_PIPE.toString()});
    private final JCheckBox header = new JCheckBox(Messages.CSV_HEADER.toString(), true);
    private final JLabel detected = new JLabel(" ");
    private final JTable raw = new JTable();
    private final JLabel fileError = new JLabel(" ");

    //step 2
    private final MappingModel mappingModel = new MappingModel();
    private final JTable mappingTable = new JTable(mappingModel);
    private final JTable preview = new JTable();
    private final JLabel mappingError = new JLabel(" ");
    private final JLabel summary = new JLabel(" ");
    private final JTextArea problems = new JTextArea(5, 40);
    private final JScrollPane problemsScroll = new JScrollPane(problems);
    private final JLabel problemsTitle = new JLabel(Messages.CSV_PROBLEMS.toString());

    //step 3
    private final JTextArea result = new JTextArea(12, 50);

    private final JButton back = new JButton(Messages.CSV_BACK.toString());
    private final PrimaryButton next = new PrimaryButton(Messages.CSV_NEXT.toString(), null);
    private final JButton cancel = new JButton(Messages.CSV_CANCEL.toString());

    private CsvImport.Data data;
    private Field[] mapping = new Field[0];
    private java.util.Set<String> existing = new java.util.HashSet<String>();
    private int importable;
    private boolean updating;

    /**
     * Opens the import over the main window
     */
    public static void open() {
        Window owner = null;
        try {
            owner = mpv5.YabsViewProxy.instance().getIdentifierFrame();
        } catch (Exception e) {
            //no main window
        }
        CsvImportDialog d = new CsvImportDialog(owner);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    private CsvImportDialog(Window owner) {
        super(owner, Messages.CSV_TITLE.toString(), ModalityType.MODELESS);
        setName("csvimport");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        root.setBackground(ModernTheme.cardBackground());
        root.setBorder(ModernTheme.padding(14, 16));

        JPanel stepBar = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(18), 0));
        stepBar.setOpaque(false);
        for (JLabel l : steps) {
            l.setFont(ModernTheme.font(2, Font.PLAIN));
            stepBar.add(l);
        }
        root.add(stepBar, BorderLayout.NORTH);

        pages.setOpaque(false);
        pages.add(createFilePage(), "0");
        pages.add(createMappingPage(), "1");
        pages.add(createResultPage(), "2");
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
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setContentPane(root);
        go(0);
        setSize(ModernTheme.px(900), ModernTheme.px(640));
        setMinimumSize(new Dimension(ModernTheme.px(640), ModernTheme.px(480)));
    }

    // ------------------------------------------------------------ step 1
    private JComponent createFilePage() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        p.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int row = 0;
        form.add(wrap(Messages.CSV_FILE_INFO.toString()), gbc(0, row++, 3));
        JButton choose = new JButton(Messages.CSV_CHOOSE.toString());
        choose.addActionListener(e -> chooseFile());
        file.addActionListener(e -> load(new File(file.getText().trim())));
        form.add(label(Messages.CSV_FILE.toString()), gbc(0, row, 1));
        GridBagConstraints c = gbc(1, row, 1);
        c.weightx = 1;
        form.add(file, c);
        form.add(choose, gbc(2, row++, 1));
        form.add(label(Messages.CSV_IMPORT_AS.toString()), gbc(0, row, 1));
        form.add(kind, gbc(1, row++, 1));
        for (Charset cs : CHARSETS) {
            charset.addItem(cs.displayName());
        }
        form.add(label(Messages.CSV_ENCODING.toString()), gbc(0, row, 1));
        form.add(charset, gbc(1, row++, 1));
        form.add(label(Messages.CSV_SEPARATOR.toString()), gbc(0, row, 1));
        form.add(separator, gbc(1, row++, 1));
        header.setOpaque(false);
        form.add(header, gbc(1, row++, 2));
        detected.setForeground(ModernTheme.muted());
        form.add(detected, gbc(0, row++, 3));
        fileError.setForeground(new Color(190, 40, 40));
        form.add(fileError, gbc(0, row++, 3));
        for (JComponent x : new JComponent[]{file, kind, charset, separator, header}) {
            x.setFont(ModernTheme.font(1, Font.PLAIN));
        }
        charset.addActionListener(e -> settingsChanged());
        separator.addActionListener(e -> settingsChanged());
        header.addActionListener(e -> settingsChanged());
        kind.addActionListener(e -> {
            if (data != null && !updating) {
                guessMapping();
            }
        });
        p.add(form, BorderLayout.NORTH);
        raw.setEnabled(false);
        raw.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ModernTheme.style(raw);
        p.add(new JScrollPane(raw), BorderLayout.CENTER);
        return p;
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser(file.getText().isEmpty() ? null : new File(file.getText()).getParentFile());
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV (*.csv, *.txt, *.tsv)", "csv", "txt", "tsv"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            load(fc.getSelectedFile());
        }
    }

    /**
     * Reads the file: encoding, separator and header detected
     *
     * @param f
     */
    void load(File f) {
        fileError.setText(" ");
        file.setText(f.getPath());
        try {
            data = CsvImport.read(f);
        } catch (Exception e) {
            Log.Debug(this, e);
            data = null;
            fileError.setText(String.format(Messages.CSV_READ_FAILED.toString(), e.getMessage()));
            showRaw();
            return;
        }
        updating = true;
        charset.setSelectedIndex(indexOf(data.charset));
        separator.setSelectedIndex(indexOf(data.separator));
        header.setSelected(looksLikeHeader());
        updating = false;
        data.header = header.isSelected();
        detected.setText(String.format(Messages.CSV_DETECTED.toString(), data.rows.size(), data.columns(),
                charset.getSelectedItem(), separator.getSelectedItem()));
        guessMapping();
        showRaw();
    }

    /**
     * @return true if the first row looks like column names (no numbers, and
     * some of them are known)
     */
    private boolean looksLikeHeader() {
        if (data.rows.isEmpty()) {
            return true;
        }
        String[] first = data.rows.get(0);
        int known = 0;
        for (Field f : CsvImport.guess(first, (Kind) kind.getSelectedItem())) {
            if (f != null) {
                known++;
            }
        }
        if (known > 0) {
            return true;
        }
        for (String s : first) {
            try {
                CsvImport.number(s);
                return false;
            } catch (Exception e) {
                //text
            }
        }
        return data.rows.size() > 1;
    }

    private static int indexOf(Charset cs) {
        for (int i = 0; i < CHARSETS.length; i++) {
            if (CHARSETS[i].equals(cs)) {
                return i;
            }
        }
        return 0;
    }

    private static int indexOf(char sep) {
        for (int i = 0; i < SEPARATORS.length; i++) {
            if (SEPARATORS[i] == sep) {
                return i;
            }
        }
        return 0;
    }

    private void settingsChanged() {
        if (updating || data == null) {
            return;
        }
        Charset cs = CHARSETS[Math.max(0, charset.getSelectedIndex())];
        char sep = SEPARATORS[Math.max(0, separator.getSelectedIndex())];
        boolean reparse = !cs.equals(data.charset) || sep != data.separator;
        data.header = header.isSelected();
        if (reparse) {
            data.reparse(cs, sep);
        }
        detected.setText(String.format(Messages.CSV_DETECTED.toString(), data.rows.size(), data.columns(),
                charset.getSelectedItem(), separator.getSelectedItem()));
        guessMapping();
        showRaw();
    }

    private void showRaw() {
        if (data == null) {
            raw.setModel(new DefaultTableModel());
            return;
        }
        int cols = data.columns();
        String[] names = new String[cols];
        for (int i = 0; i < cols; i++) {
            names[i] = data.columnName(i);
        }
        DefaultTableModel m = new DefaultTableModel(names, 0);
        for (int r = data.firstData(); r < Math.min(data.rows.size(), data.firstData() + PREVIEW_ROWS); r++) {
            m.addRow(data.rows.get(r));
        }
        raw.setModel(m);
        widths(raw, 140);
    }

    // ------------------------------------------------------------ step 2
    private JComponent createMappingPage() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setOpaque(false);
        p.add(wrap(Messages.CSV_MAP_INFO.toString()), BorderLayout.NORTH);

        mappingTable.setRowHeight(Math.max(mappingTable.getRowHeight(), ModernTheme.px(26)));
        ModernTheme.style(mappingTable);
        mappingTable.getTableHeader().setReorderingAllowed(false);
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        top.setOpaque(false);
        JScrollPane ms = new JScrollPane(mappingTable);
        ms.setPreferredSize(new Dimension(ModernTheme.px(600), ModernTheme.px(210)));
        top.add(ms, BorderLayout.CENTER);
        mappingError.setForeground(new Color(190, 40, 40));
        top.add(mappingError, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        bottom.setOpaque(false);
        JLabel pl = new JLabel(Messages.CSV_PREVIEW.toString());
        pl.setFont(ModernTheme.font(1, Font.BOLD));
        bottom.add(pl, BorderLayout.NORTH);
        preview.setEnabled(false);
        preview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ModernTheme.style(preview);
        bottom.add(new JScrollPane(preview), BorderLayout.CENTER);
        JPanel check = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        check.setOpaque(false);
        summary.setFont(ModernTheme.font(1, Font.BOLD));
        check.add(summary, BorderLayout.NORTH);
        problemsTitle.setForeground(new Color(190, 40, 40));
        problems.setEditable(false);
        problems.setForeground(new Color(150, 30, 30));
        JPanel pp = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        pp.setOpaque(false);
        pp.add(problemsTitle, BorderLayout.NORTH);
        pp.add(problemsScroll, BorderLayout.CENTER);
        check.add(pp, BorderLayout.CENTER);
        bottom.add(check, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setResizeWeight(0.4);
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private void guessMapping() {
        if (data == null) {
            return;
        }
        String[] headers = new String[data.columns()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = data.header ? data.columnName(i) : "";
        }
        mapping = CsvImport.guess(headers, (Kind) kind.getSelectedItem());
        existing = CsvImport.existingNumbers((Kind) kind.getSelectedItem());
        //the box of every column: not imported, and the fields
        List<Target> targets = new ArrayList<Target>();
        targets.add(new Target(null));
        for (Field f : CsvImport.fields((Kind) kind.getSelectedItem())) {
            targets.add(new Target(f));
        }
        JComboBox<Target> box = new JComboBox<Target>(targets.toArray(new Target[0]));
        box.setFont(ModernTheme.font(1, Font.PLAIN));
        mappingModel.fireTableDataChanged();
        mappingTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(box));
        mappingTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(t, String.valueOf(v) + "  ▾", s, f, r, c);
                if (!s) {
                    x.setForeground(mapping[r] == null ? ModernTheme.muted() : ModernTheme.foreground());
                }
                return x;
            }
        });
        refreshPreview();
    }

    /**
     * The table of columns: name, example, field
     */
    private final class MappingModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return data == null ? 0 : mapping.length;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int c) {
            return c == 0 ? Messages.CSV_MAP_COLUMN.toString() : c == 1 ? Messages.CSV_MAP_SAMPLE.toString() : Messages.CSV_MAP_TARGET.toString();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 2;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return data.columnName(r);
            } else if (c == 1) {
                for (int i = data.firstData(); i < Math.min(data.rows.size(), data.firstData() + 20); i++) {
                    String[] row = data.rows.get(i);
                    if (r < row.length && !row[r].trim().isEmpty()) {
                        return row[r].trim();
                    }
                }
                return "";
            }
            return new Target(mapping[r]);
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            if (c == 2 && v instanceof Target) {
                Field f = ((Target) v).field;
                //a field once only: another column with it is not imported any more
                if (f != null) {
                    for (int i = 0; i < mapping.length; i++) {
                        if (i != r && mapping[i] == f) {
                            mapping[i] = null;
                        }
                    }
                }
                mapping[r] = f;
                fireTableDataChanged();
                refreshPreview();
            }
        }
    }

    /**
     * The preview of the first rows, the check of all rows
     */
    private void refreshPreview() {
        if (data == null) {
            return;
        }
        Kind k = (Kind) kind.getSelectedItem();
        List<Integer> cols = new ArrayList<Integer>();
        List<String> names = new ArrayList<String>();
        names.add("#");
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i] != null) {
                cols.add(i);
                names.add(mapping[i].toString());
            }
        }
        names.add(Messages.CSV_STATUS.toString());
        DefaultTableModel m = new DefaultTableModel(names.toArray(), 0);
        final List<Boolean> bad = new ArrayList<Boolean>();
        StringBuilder problemText = new StringBuilder();
        importable = 0;
        int problemRows = 0;
        java.util.Set<String> taken = new java.util.HashSet<String>(existing);
        for (int r = data.firstData(); r < data.rows.size(); r++) {
            String[] row = data.rows.get(r);
            if (CsvImport.isEmpty(row)) {
                continue;
            }
            List<String> p = CsvImport.check(row, mapping, k, taken);
            if (p.isEmpty()) {
                importable++;
            } else {
                problemRows++;
                problemText.append(String.format(Messages.CSV_LINE.toString(), r + 1)).append(": ").append(String.join("; ", p)).append('\n');
            }
            if (m.getRowCount() < PREVIEW_ROWS) {
                Object[] line = new Object[cols.size() + 2];
                line[0] = r + 1;
                for (int i = 0; i < cols.size(); i++) {
                    int c = cols.get(i);
                    line[i + 1] = CsvImport.display(c < row.length ? row[c] : "", mapping[c]);
                }
                line[line.length - 1] = p.isEmpty() ? Messages.CSV_OK.toString() : String.join("; ", p);
                m.addRow(line);
                bad.add(!p.isEmpty());
            }
        }
        preview.setModel(m);
        preview.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(t, v, false, false, r, c);
                boolean problem = r < bad.size() && bad.get(r);
                x.setBackground(problem ? new Color(0xFDE7E7) : t.getBackground());
                x.setForeground(c == t.getColumnCount() - 1 ? (problem ? new Color(170, 30, 30) : new Color(30, 130, 60)) : t.getForeground());
                return x;
            }
        });
        widths(preview, 130);
        if (preview.getColumnCount() > 0) {
            preview.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(44));
            preview.getColumnModel().getColumn(preview.getColumnCount() - 1).setPreferredWidth(ModernTheme.px(320));
        }
        String mappingProblem = CsvImport.checkMapping(mapping, k);
        mappingError.setText(mappingProblem == null ? " " : mappingProblem);
        if (mappingProblem != null) {
            importable = 0;
        }
        summary.setText(problemRows == 0 ? String.format(Messages.CSV_CHECK_ALL_OK.toString(), importable)
                : String.format(Messages.CSV_CHECK_SUMMARY.toString(), importable, problemRows));
        summary.setForeground(problemRows == 0 && mappingProblem == null ? new Color(30, 130, 60) : new Color(170, 90, 0));
        problems.setText(problemText.toString());
        problems.setCaretPosition(0);
        problemsTitle.setVisible(problemRows > 0);
        problemsScroll.setVisible(problemRows > 0);
        updateButtons();
        revalidate();
    }

    // ------------------------------------------------------------ step 3
    private JComponent createResultPage() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        result.setEditable(false);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setFont(ModernTheme.font(1, Font.PLAIN));
        p.add(new JScrollPane(result), BorderLayout.CENTER);
        return p;
    }

    private void runImport() {
        final Kind k = (Kind) kind.getSelectedItem();
        final Field[] map = mapping.clone();
        final int total = data.rows.size() - data.firstData();
        next.setEnabled(false);
        back.setEnabled(false);
        cancel.setEnabled(false);
        mappingTable.setEnabled(false);
        new SwingWorker<CsvImport.Result, Integer>() {
            @Override
            protected CsvImport.Result doInBackground() {
                return CsvImport.importRows(data, map, k, done -> publish(done));
            }

            @Override
            protected void process(List<Integer> chunks) {
                summary.setText(String.format(Messages.CSV_IMPORTING.toString(), chunks.get(chunks.size() - 1), total));
            }

            @Override
            protected void done() {
                CsvImport.Result r;
                try {
                    r = get();
                } catch (Exception e) {
                    Log.Debug(CsvImportDialog.this, e);
                    r = new CsvImport.Result();
                    r.skipped.add(new CsvImport.Problem(0, String.valueOf(e.getMessage())));
                }
                showResult(r, k);
                cancel.setEnabled(true);
                mappingTable.setEnabled(true);
                try {
                    mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.CSV_RESULT_IMPORTED.toString(), r.imported, k), Color.GREEN);
                } catch (Exception e) {
                    //no main window
                }
            }
        }.execute();
    }

    private void showResult(CsvImport.Result r, Kind k) {
        StringBuilder b = new StringBuilder();
        b.append(String.format(Messages.CSV_RESULT_IMPORTED.toString(), r.imported, k)).append("\n\n");
        if (r.skipped.isEmpty()) {
            b.append(Messages.CSV_RESULT_NONE_SKIPPED).append('\n');
        } else {
            b.append(String.format(Messages.CSV_RESULT_SKIPPED.toString(), r.skipped.size())).append('\n');
            for (CsvImport.Problem p : r.skipped) {
                b.append("  • ").append(p).append('\n');
            }
        }
        if (r.empty > 0) {
            b.append('\n').append(String.format(Messages.CSV_RESULT_EMPTY.toString(), r.empty)).append('\n');
        }
        result.setText(b.toString());
        result.setCaretPosition(0);
        go(2);
    }

    // ------------------------------------------------------------ navigation
    private void nextPressed() {
        if (step == 0) {
            if (data == null) {
                if (file.getText().trim().isEmpty()) {
                    fileError.setText(Messages.CSV_NO_FILE.toString());
                    return;
                }
                load(new File(file.getText().trim()));
                if (data == null) {
                    return;
                }
            }
            if (data.rows.size() <= data.firstData()) {
                fileError.setText(Messages.CSV_EMPTY_FILE.toString());
                return;
            }
            go(1);
        } else if (step == 1) {
            runImport();
        } else {
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
        if (step == 1) {
            refreshPreview();
        }
        updateButtons();
    }

    private void updateButtons() {
        back.setVisible(step == 1);
        back.setEnabled(true);
        cancel.setVisible(step < 2);
        if (step == 0) {
            next.setText(Messages.CSV_NEXT.toString());
            next.setEnabled(true);
        } else if (step == 1) {
            next.setText(String.format(Messages.CSV_IMPORT.toString(), importable));
            next.setEnabled(importable > 0);
        } else {
            next.setText(Messages.CSV_CLOSE.toString());
            next.setEnabled(true);
        }
    }

    // ------------------------------------------------------------ helpers
    private static JLabel wrap(String text) {
        JLabel l = new JLabel("<html><div style='width:" + ModernTheme.px(620) + "px'>"
                + text.replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        l.setFont(ModernTheme.font(1, Font.PLAIN));
        return l;
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(ModernTheme.muted());
        return l;
    }

    private static GridBagConstraints gbc(int x, int y, int w) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        c.anchor = GridBagConstraints.WEST;
        c.fill = w > 1 || x == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.insets = new Insets(ModernTheme.px(3), 0, ModernTheme.px(3), ModernTheme.px(8));
        return c;
    }

    private static void widths(JTable t, int w) {
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(w));
        }
    }
}
