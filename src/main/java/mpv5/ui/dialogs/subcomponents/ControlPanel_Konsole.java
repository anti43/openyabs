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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.SqlConsole;
import mpv5.db.common.SqlConsole.ColumnInfo;
import mpv5.db.common.SqlConsole.ExecutionResult;
import mpv5.db.common.SqlConsole.RiskLevel;
import mpv5.db.common.SqlConsole.StatementInfo;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.popups.CopyPasteMenu;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileReaderWriter;

/**
 * Unified SQL Console &amp; Query workbench with schema browser, templates,
 * history, sortable result table, CSV export, DDL/DML safety warnings and
 * confirmations, and mandatory audit logging (including GoBD hash-chained
 * audit log integration). All users can run read-only {@code SELECT} queries;
 * modifying SQL statements and Groovy scripts are strictly restricted to
 * administrators.
 */
public class ControlPanel_Konsole extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 2L;

    public final String UNAME = "console";
    private static final int MAX_HISTORY = 25;

    private static final Color BADGE_READ_BG = new Color(220, 252, 231);
    private static final Color BADGE_READ_FG = new Color(22, 101, 52);
    private static final Color BADGE_CONFIRM_BG = new Color(254, 243, 199);
    private static final Color BADGE_CONFIRM_FG = new Color(146, 64, 14);
    private static final Color BADGE_DDL_BG = new Color(254, 226, 226);
    private static final Color BADGE_DDL_FG = new Color(153, 27, 27);
    private static final Color BADGE_GOBD_BG = new Color(219, 234, 254);
    private static final Color BADGE_GOBD_FG = new Color(30, 64, 175);

    private GroovyShell groovyShell;
    private JPanel actions = new JPanel();

    // Schema browser
    private JTextField tableFilterField;
    private DefaultListModel<String> tableListModel;
    private JList<String> tableList;
    private DefaultListModel<ColumnInfo> columnListModel;
    private JList<ColumnInfo> columnList;
    private List<String> allTables = new ArrayList<String>();

    // Editor & toolbar
    private JTextArea editorArea;
    private JComboBox<TemplateItem> templatesCombo;
    private JComboBox<HistoryItem> historyCombo;
    private JComboBox<LimitItem> limitCombo;
    private JComboBox<TimeoutItem> timeoutCombo;
    private JLabel riskBadge;
    private JLabel gobdBadge;

    // Results & log
    private JTabbedPane outputTabs;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    // Actions
    private PrimaryButton runButton;
    private JButton stopButton;
    private JButton exportCsvButton;
    private JButton clearButton;
    private JButton loadButton;
    private JButton saveButton;

    // State
    private final LinkedList<String> history = new LinkedList<String>();
    private String[] lastColumns = new String[0];
    private Object[][] lastRows = new Object[0][0];
    private boolean updatingCombos = false;
    private volatile boolean busy = false;
    private SwingWorker<List<ExecutionResult>, Integer> activeWorker;
    private final AtomicReference<Statement> activeStatement = new AtomicReference<Statement>();
    private AtomicBoolean activeCancelFlag;
    private Timer elapsedTimer;
    private long execStartMs = 0L;
    private int execTotalStmts = 1;
    private int execCurrentStmt = 1;

    public ControlPanel_Konsole() {
        buildUi();
        loadTablesAsync();
        setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ModernTheme.px(760), ModernTheme.px(520));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(ModernTheme.px(480), ModernTheme.px(320));
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, ModernTheme.px(8)));
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(10, 12));

        JComponent schemaPanel = buildSchemaPanel();
        JComponent editorPanel = buildEditorPanel();
        JComponent outputPanel = buildOutputPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorPanel, outputPanel);
        rightSplit.setResizeWeight(0.38);
        rightSplit.setDividerLocation(ModernTheme.px(210));
        rightSplit.setBorder(null);
        rightSplit.setOpaque(false);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, schemaPanel, rightSplit);
        mainSplit.setResizeWeight(0.0);
        mainSplit.setDividerLocation(ModernTheme.px(220));
        mainSplit.setBorder(null);
        mainSplit.setOpaque(false);

        add(mainSplit, BorderLayout.CENTER);
        buildActionBar();
        add(actions, BorderLayout.SOUTH);
        updateLiveRiskBadge();
    }

    private JComponent buildSchemaPanel() {
        Card card = new Card(Messages.KONSOLE_TABLES.toString());

        JPanel topRow = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        topRow.setOpaque(false);
        tableFilterField = new JTextField();
        tableFilterField.putClientProperty("JTextField.placeholderText", Messages.KONSOLE_FILTER_TABLES.toString());
        tableFilterField.setToolTipText(Messages.KONSOLE_FILTER_TABLES.toString());
        tableFilterField.getDocument().addDocumentListener(new SimpleDocListener(this::applyTableFilter));

        JButton refreshBtn = new JButton("↻");
        refreshBtn.setToolTipText(Messages.KONSOLE_REFRESH.toString());
        refreshBtn.setMargin(new java.awt.Insets(2, 6, 2, 6));
        refreshBtn.addActionListener(e -> loadTablesAsync());

        topRow.add(tableFilterField, BorderLayout.CENTER);
        topRow.add(refreshBtn, BorderLayout.EAST);

        tableListModel = new DefaultListModel<String>();
        tableList = new JList<String>(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.setFont(ModernTheme.font(-1, Font.PLAIN));
        tableList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String name = value == null ? "" : String.valueOf(value);
                if (!isSelected && name.toLowerCase(Locale.ROOT).startsWith("gobd_")) {
                    lbl.setForeground(BADGE_GOBD_FG);
                    lbl.setText(name + "  [GoBD]");
                }
                return lbl;
            }
        });
        tableList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = tableList.getSelectedValue();
                if (sel != null) {
                    loadColumnsAsync(sel);
                }
            }
        });
        tableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String tbl = tableList.getSelectedValue();
                    if (tbl != null) {
                        if (editorArea.getText().trim().isEmpty()) {
                            editorArea.setText("SELECT * FROM " + tbl + ";");
                        } else {
                            insertAtCaret(tbl);
                        }
                        editorArea.requestFocusInWindow();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowTablePopup(e);
            }
        });

        columnListModel = new DefaultListModel<ColumnInfo>();
        columnList = new JList<ColumnInfo>(columnListModel);
        columnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Math.max(11, ModernTheme.baseFont().getSize() - 1)));
        columnList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    ColumnInfo col = columnList.getSelectedValue();
                    if (col != null) {
                        insertAtCaret(col.name);
                        editorArea.requestFocusInWindow();
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableList);
        JPanel colBox = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        colBox.setOpaque(false);
        JLabel colLbl = new JLabel(Messages.KONSOLE_COLUMNS.toString());
        colLbl.setFont(ModernTheme.font(-1, Font.BOLD));
        colLbl.setForeground(ModernTheme.muted());
        colBox.add(colLbl, BorderLayout.NORTH);
        colBox.add(new JScrollPane(columnList), BorderLayout.CENTER);

        JSplitPane schemaSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, colBox);
        schemaSplit.setResizeWeight(0.60);
        schemaSplit.setDividerLocation(ModernTheme.px(240));
        schemaSplit.setBorder(null);
        schemaSplit.setOpaque(false);

        card.addFull(topRow);
        card.addFull(schemaSplit, 1);
        card.setMinimumSize(new Dimension(ModernTheme.px(170), ModernTheme.px(200)));
        return card;
    }

    private void maybeShowTablePopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int idx = tableList.locationToIndex(e.getPoint());
        if (idx < 0) {
            return;
        }
        tableList.setSelectedIndex(idx);
        final String tbl = tableList.getSelectedValue();
        if (tbl == null) {
            return;
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem preview = new JMenuItem(Messages.KONSOLE_SELECT_TOP.toString());
        preview.addActionListener(ev -> {
            editorArea.setText("SELECT * FROM " + tbl + ";");
            executeCurrentSql();
        });
        JMenuItem count = new JMenuItem(Messages.KONSOLE_COUNT_ROWS.toString());
        count.addActionListener(ev -> {
            editorArea.setText("SELECT COUNT(*) AS row_count FROM " + tbl + ";");
            executeCurrentSql();
        });
        menu.add(preview);
        menu.add(count);
        menu.show(tableList, e.getX(), e.getY());
    }

    private JComponent buildEditorPanel() {
        Card card = new Card(Messages.KONSOLE_QUERY.toString());

        JPanel rightBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        rightBadges.setOpaque(false);

        riskBadge = createBadge(Messages.KONSOLE_BADGE_READ.toString(), BADGE_READ_BG, BADGE_READ_FG);
        gobdBadge = createBadge(Messages.KONSOLE_BADGE_GOBD.toString(), BADGE_GOBD_BG, BADGE_GOBD_FG);
        gobdBadge.setVisible(SqlConsole.isGobdActive());
        rightBadges.add(riskBadge);
        rightBadges.add(gobdBadge);
        card.headerAction(rightBadges);

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        leftTools.setOpaque(false);

        templatesCombo = new JComboBox<TemplateItem>();
        populateTemplates();
        templatesCombo.setPrototypeDisplayValue(new TemplateItem("Kontenrahmen (Konten)  ", null));
        templatesCombo.addActionListener(e -> {
            if (updatingCombos) {
                return;
            }
            TemplateItem item = (TemplateItem) templatesCombo.getSelectedItem();
            if (item != null && item.sql != null) {
                editorArea.setText(item.sql);
                editorArea.setCaretPosition(editorArea.getText().length());
                editorArea.requestFocusInWindow();
                updatingCombos = true;
                templatesCombo.setSelectedIndex(0);
                updatingCombos = false;
            }
        });

        historyCombo = new JComboBox<HistoryItem>();
        refreshHistoryCombo();
        historyCombo.setPrototypeDisplayValue(new HistoryItem(Messages.KONSOLE_HISTORY.toString() + "          ", null));
        historyCombo.addActionListener(e -> {
            if (updatingCombos) {
                return;
            }
            HistoryItem item = (HistoryItem) historyCombo.getSelectedItem();
            if (item != null && item.sql != null) {
                editorArea.setText(item.sql);
                editorArea.setCaretPosition(editorArea.getText().length());
                editorArea.requestFocusInWindow();
                updatingCombos = true;
                historyCombo.setSelectedIndex(0);
                updatingCombos = false;
            }
        });

        limitCombo = new JComboBox<LimitItem>(new LimitItem[]{
            new LimitItem("200", 200),
            new LimitItem("1000", 1000),
            new LimitItem("5000", 5000),
            new LimitItem(Messages.KONSOLE_LIMIT_ALL.toString(), 0)
        });
        limitCombo.setSelectedIndex(1);

        timeoutCombo = new JComboBox<TimeoutItem>(new TimeoutItem[]{
            new TimeoutItem("30 s", 30),
            new TimeoutItem("60 s", 60),
            new TimeoutItem("5 min", 300),
            new TimeoutItem("15 min", 900),
            new TimeoutItem(Messages.KONSOLE_TIMEOUT_NONE.toString(), 0)
        });
        timeoutCombo.setSelectedIndex(0);

        leftTools.add(templatesCombo);
        leftTools.add(historyCombo);
        leftTools.add(new JLabel(Messages.KONSOLE_LIMIT.toString()));
        leftTools.add(limitCombo);
        leftTools.add(new JLabel(Messages.KONSOLE_TIMEOUT.toString()));
        leftTools.add(timeoutCombo);

        editorArea = new JTextArea(5, 40);
        editorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ModernTheme.baseFont().getSize()));
        editorArea.setTabSize(4);
        editorArea.setLineWrap(false);
        new CopyPasteMenu(editorArea);

        // Ctrl+Enter and F5 execute SQL; plain Enter inserts a newline!
        AbstractAction runAction = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                executeCurrentSql();
            }
        };
        editorArea.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "sql-run");
        editorArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "sql-run");
        editorArea.getActionMap().put("sql-run", runAction);

        editorArea.getDocument().addDocumentListener(new SimpleDocListener(this::updateLiveRiskBadge));

        JScrollPane editorScroll = new JScrollPane(editorArea);
        editorScroll.setPreferredSize(new Dimension(ModernTheme.px(360), ModernTheme.px(110)));
        editorScroll.setMinimumSize(new Dimension(ModernTheme.px(200), ModernTheme.px(70)));

        String hint = MPSecurityManager.isAdmin()
                ? Messages.KONSOLE_HINT_NEW.toString()
                : Messages.KONSOLE_HINT_READONLY.toString();
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(ModernTheme.font(-1, Font.PLAIN));
        hintLabel.setForeground(ModernTheme.muted());
        hintLabel.setToolTipText(hint);
        hintLabel.setMinimumSize(new Dimension(0, ModernTheme.px(16)));
        hintLabel.setPreferredSize(new Dimension(ModernTheme.px(200), ModernTheme.px(18)));

        card.addFull(leftTools);
        card.addFull(editorScroll, 1);
        card.addFull(hintLabel);
        card.setMinimumSize(new Dimension(ModernTheme.px(240), ModernTheme.px(160)));
        return card;
    }

    private JComponent buildOutputPanel() {
        Card card = new Card(Messages.KONSOLE_RESULT.toString());

        resultModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultModel);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultTable.setRowHeight(ModernTheme.px(24));
        resultTable.setFillsViewportHeight(true);
        resultTable.setPreferredScrollableViewportSize(new Dimension(ModernTheme.px(360), ModernTheme.px(160)));
        resultTable.setDefaultRenderer(Object.class, new SqlCellRenderer());
        installTableCopyMenu();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Math.max(11, ModernTheme.baseFont().getSize() - 1)));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        new CopyPasteMenu(logArea);

        outputTabs = new JTabbedPane();
        outputTabs.addTab(Messages.KONSOLE_RESULT.toString(), new JScrollPane(resultTable));
        outputTabs.addTab(Messages.KONSOLE_LOG.toString(), new JScrollPane(logArea));
        outputTabs.setPreferredSize(new Dimension(ModernTheme.px(360), ModernTheme.px(180)));
        outputTabs.setMinimumSize(new Dimension(ModernTheme.px(200), ModernTheme.px(100)));

        statusLabel = new JLabel(Messages.KONSOLE_STATUS_READY.toString()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void setText(String text) {
                super.setText(text);
                setToolTipText(text);
            }
        };
        statusLabel.setFont(ModernTheme.font(-1, Font.PLAIN));
        statusLabel.setForeground(ModernTheme.muted());
        statusLabel.setMinimumSize(new Dimension(0, ModernTheme.px(18)));
        statusLabel.setPreferredSize(new Dimension(ModernTheme.px(200), ModernTheme.px(18)));

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(ModernTheme.px(180), ModernTheme.px(14)));
        progressBar.setVisible(false);

        JPanel statusBar = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        statusBar.setOpaque(false);
        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);

        card.addFull(outputTabs, 1);
        card.addFull(statusBar);
        card.setMinimumSize(new Dimension(ModernTheme.px(240), ModernTheme.px(140)));
        return card;
    }

    private void buildActionBar() {
        clearButton = new JButton(Messages.KONSOLE_CLEAR.toString());
        clearButton.addActionListener(e -> {
            editorArea.setText("");
            resultModel.setDataVector(new Object[0][0], new Object[0]);
            lastColumns = new String[0];
            lastRows = new Object[0][0];
            exportCsvButton.setEnabled(false);
            statusLabel.setText(Messages.KONSOLE_STATUS_READY.toString());
            updateLiveRiskBadge();
        });

        loadButton = new JButton(Messages.KONSOLE_LOAD.toString());
        loadButton.addActionListener(e -> loadSqlFile());

        saveButton = new JButton(Messages.KONSOLE_SAVE.toString());
        saveButton.addActionListener(e -> saveSqlFile());

        exportCsvButton = new JButton(Messages.KONSOLE_EXPORT_CSV.toString());
        exportCsvButton.setEnabled(false);
        exportCsvButton.addActionListener(e -> exportResultCsv());

        stopButton = new JButton(Messages.KONSOLE_STOP.toString());
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopExecution());

        runButton = new PrimaryButton(Messages.KONSOLE_RUN.toString(), null);
        runButton.addActionListener(e -> executeCurrentSql());

        actions = Applets.actionBar(
                new JComponent[]{clearButton, loadButton, saveButton},
                new JComponent[]{exportCsvButton, stopButton, runButton});
    }

    private void installTableCopyMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyCell = new JMenuItem(Messages.KONSOLE_COPY_CELL.toString());
        copyCell.addActionListener(e -> {
            int r = resultTable.getSelectedRow();
            int c = resultTable.getSelectedColumn();
            if (r >= 0 && c >= 0) {
                Object v = resultTable.getValueAt(r, c);
                copyToClipboard(v == null ? "" : String.valueOf(v));
            }
        });
        JMenuItem copyRow = new JMenuItem(Messages.KONSOLE_COPY_ROW.toString());
        copyRow.addActionListener(e -> {
            int r = resultTable.getSelectedRow();
            if (r >= 0) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < resultTable.getColumnCount(); c++) {
                    if (c > 0) {
                        sb.append('\t');
                    }
                    Object v = resultTable.getValueAt(r, c);
                    sb.append(v == null ? "" : String.valueOf(v));
                }
                copyToClipboard(sb.toString());
            }
        });
        menu.add(copyCell);
        menu.add(copyRow);
        resultTable.setComponentPopupMenu(menu);
    }

    private static void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (Throwable ignored) {
        }
    }

    private static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(ModernTheme.font(-1, Font.BOLD));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void populateTemplates() {
        updatingCombos = true;
        templatesCombo.removeAllItems();
        boolean de = isGermanUi();
        templatesCombo.addItem(new TemplateItem(Messages.KONSOLE_TEMPLATES.toString(), null));
        templatesCombo.addItem(new TemplateItem(
                de ? "Rechnungen" : "Invoices",
                "SELECT ids, cnumber, cname, dateadded, dateend, intstatus, netvalue, taxvalue\nFROM items\nWHERE inttype IN (0, 20, 21, 22)\nORDER BY ids DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Offene Rechnungen" : "Open invoices",
                "SELECT ids, cnumber, cname, dateadded, dateend, intstatus, netvalue, taxvalue\nFROM items\nWHERE inttype IN (0, 21, 22) AND intstatus NOT IN (4, 5)\nORDER BY dateadded DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Kontakte" : "Contacts",
                "SELECT ids, cnumber, company, prename, cname, city, mailaddress\nFROM contacts\nORDER BY ids DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Produkte / Lager" : "Products / stock",
                "SELECT ids, cnumber, cname, externalnetvalue, stockvalue\nFROM products\nORDER BY ids DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Einnahmen" : "Revenues",
                "SELECT ids, cnumber, cname, dateadded, dateend, netvalue, brutvalue\nFROM revenues\nORDER BY ids DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Ausgaben" : "Expenses",
                "SELECT ids, cnumber, cname, dateadded, dateend, netvalue, brutvalue\nFROM expenses\nORDER BY ids DESC;"));
        templatesCombo.addItem(new TemplateItem(
                de ? "Kontenrahmen (Konten)" : "Chart of accounts",
                "SELECT ids, intaccountclass, cname, frame, taxvalue\nFROM accounts\nORDER BY intaccountclass;"));
        if (SqlConsole.isGobdActive()) {
            templatesCombo.addItem(new TemplateItem(
                    de ? "GoBD Audit-Log" : "GoBD Audit Log",
                    "SELECT id, created_at, user_id, entity_type, action, old_value, new_value, details\nFROM gobd_audit_log\nORDER BY id DESC;"));
        }
        updatingCombos = false;
    }

    private static boolean isGermanUi() {
        return "Tabellen".equalsIgnoreCase(Messages.KONSOLE_TABLES.toString())
                || "Rechnung".equalsIgnoreCase(Messages.TYPE_INVOICE.toString());
    }

    private String effectiveInput() {
        return editorArea.getText().trim();
    }

    private void updateLiveRiskBadge() {
        if (riskBadge == null || editorArea == null) {
            return;
        }
        boolean gobdActive = SqlConsole.isGobdActive();
        if (gobdBadge != null) {
            gobdBadge.setVisible(gobdActive);
        }
        String text = effectiveInput();
        if (text.isEmpty()) {
            setBadge(riskBadge, Messages.KONSOLE_BADGE_READ.toString(), BADGE_READ_BG, BADGE_READ_FG);
            return;
        }
        boolean isAdmin = MPSecurityManager.isAdmin();
        if (text.toLowerCase(Locale.ROOT).startsWith("groovy")) {
            if (!isAdmin) {
                setBadge(riskBadge, Messages.KONSOLE_BADGE_ADMIN_ONLY.toString(), BADGE_DDL_BG, BADGE_DDL_FG);
            } else {
                setBadge(riskBadge, "Groovy (" + Messages.KONSOLE_BADGE_CONFIRM.toString() + ")", BADGE_CONFIRM_BG, BADGE_CONFIRM_FG);
            }
            return;
        }
        List<StatementInfo> stmts = SqlConsole.analyzeScript(text);
        RiskLevel highest = RiskLevel.READ_ONLY;
        boolean anyModifying = false;
        for (StatementInfo s : stmts) {
            if (s.modifying) {
                anyModifying = true;
            }
            RiskLevel r = s.riskLevel(gobdActive);
            if (r.ordinal() > highest.ordinal()) {
                highest = r;
            }
        }
        if (!isAdmin && anyModifying) {
            setBadge(riskBadge, Messages.KONSOLE_BADGE_ADMIN_ONLY.toString(), BADGE_DDL_BG, BADGE_DDL_FG);
            return;
        }
        switch (highest) {
            case CRITICAL_GOBD:
            case WARNING_DDL:
                setBadge(riskBadge, Messages.KONSOLE_BADGE_DDL.toString(), BADGE_DDL_BG, BADGE_DDL_FG);
                break;
            case CONFIRM:
                setBadge(riskBadge, Messages.KONSOLE_BADGE_CONFIRM.toString(), BADGE_CONFIRM_BG, BADGE_CONFIRM_FG);
                break;
            default:
                setBadge(riskBadge, Messages.KONSOLE_BADGE_READ.toString(), BADGE_READ_BG, BADGE_READ_FG);
                break;
        }
    }

    private static void setBadge(JLabel lbl, String text, Color bg, Color fg) {
        lbl.setText(text);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(2, 7, 2, 7)));
    }

    private void insertAtCaret(String text) {
        if (editorArea == null || text == null) {
            return;
        }
        int pos = editorArea.getCaretPosition();
        editorArea.insert(text, pos);
    }

    // ------------------------------------------------------------------ Schema loading

    private void loadTablesAsync() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return SqlConsole.listTables(activeConnection());
            }

            @Override
            protected void done() {
                try {
                    allTables = get();
                    applyTableFilter();
                } catch (Exception ex) {
                    Log.Debug(ControlPanel_Konsole.class, ex.getMessage());
                }
            }
        }.execute();
    }

    private void applyTableFilter() {
        String needle = tableFilterField == null ? "" : tableFilterField.getText().trim().toLowerCase(Locale.ROOT);
        tableListModel.clear();
        for (String t : allTables) {
            if (needle.isEmpty() || t.toLowerCase(Locale.ROOT).contains(needle)) {
                tableListModel.addElement(t);
            }
        }
    }

    private void loadColumnsAsync(final String tableName) {
        new SwingWorker<List<ColumnInfo>, Void>() {
            @Override
            protected List<ColumnInfo> doInBackground() {
                return SqlConsole.listColumns(activeConnection(), tableName);
            }

            @Override
            protected void done() {
                try {
                    columnListModel.clear();
                    for (ColumnInfo c : get()) {
                        columnListModel.addElement(c);
                    }
                } catch (Exception ex) {
                    Log.Debug(ControlPanel_Konsole.class, ex.getMessage());
                }
            }
        }.execute();
    }

    private static Connection activeConnection() {
        try {
            if (DatabaseConnection.getConnector() != null) {
                return DatabaseConnection.getConnector().getConnection();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // ------------------------------------------------------------------ Execution & safety confirmations

    private void executeCurrentSql() {
        if (busy) {
            return;
        }
        String input = effectiveInput();
        if (input.isEmpty()) {
            return;
        }

        if (input.toLowerCase(Locale.ROOT).startsWith("groovy")) {
            if (!MPSecurityManager.checkAdminAccess()) {
                appendLog("DENIED (admin required): GROOVY");
                statusLabel.setText(Messages.KONSOLE_ADMIN_ONLY_ERROR.toString());
                outputTabs.setSelectedIndex(1);
                return;
            }
            executeGroovy(input);
            return;
        }

        final List<StatementInfo> statements = SqlConsole.analyzeScript(input);
        if (statements.isEmpty()) {
            return;
        }

        final boolean isAdmin = MPSecurityManager.isAdmin();
        boolean hasModifying = false;
        for (StatementInfo s : statements) {
            if (s.modifying) {
                hasModifying = true;
                break;
            }
        }
        if (hasModifying && !isAdmin) {
            MPSecurityManager.checkAdminAccess();
            appendLog("DENIED (admin required): " + summarizeStatements(statements));
            statusLabel.setText(Messages.KONSOLE_ADMIN_ONLY_ERROR.toString());
            outputTabs.setSelectedIndex(1);
            return;
        }

        final boolean gobdActive = SqlConsole.isGobdActive();
        if (!confirmIfModifying(statements, gobdActive)) {
            appendLog("CANCELLED: " + summarizeStatements(statements));
            statusLabel.setText(Messages.KONSOLE_STATUS_CANCELLED.toString());
            return;
        }

        rememberHistory(input);
        final int maxRows = selectedLimit();
        final int timeoutSec = selectedTimeoutSeconds();
        final AtomicBoolean cancelFlag = new AtomicBoolean(false);
        activeCancelFlag = cancelFlag;
        activeStatement.set(null);
        setBusy(true, statements.size());

        SwingWorker<List<ExecutionResult>, Integer> worker = new SwingWorker<List<ExecutionResult>, Integer>() {
            @Override
            protected List<ExecutionResult> doInBackground() {
                Connection conn = activeConnection();
                List<ExecutionResult> results = new ArrayList<ExecutionResult>();
                for (int i = 0; i < statements.size(); i++) {
                    if (cancelFlag.get() || isCancelled()) {
                        break;
                    }
                    publish(i + 1);
                    StatementInfo stmt = statements.get(i);
                    ExecutionResult r = SqlConsole.execute(
                            conn, stmt, maxRows, timeoutSec, true, isAdmin, activeStatement, cancelFlag);
                    results.add(r);
                    if (!r.success) {
                        // Stop script on first error, timeout, or cancel to avoid partial follow-up mutations
                        break;
                    }
                }
                return results;
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty() && busy) {
                    execCurrentStmt = chunks.get(chunks.size() - 1);
                    if (execTotalStmts > 1) {
                        progressBar.setIndeterminate(false);
                        progressBar.setMaximum(execTotalStmts);
                        progressBar.setValue(Math.max(0, execCurrentStmt - 1));
                    }
                    updateRunningStatusText();
                }
            }

            @Override
            protected void done() {
                activeWorker = null;
                activeCancelFlag = null;
                activeStatement.set(null);
                setBusy(false, 0);
                try {
                    if (isCancelled()) {
                        appendLog("STOPPED: " + summarizeStatements(statements));
                        statusLabel.setText(Messages.KONSOLE_STATUS_STOPPED.toString());
                        outputTabs.setSelectedIndex(1);
                        return;
                    }
                    List<ExecutionResult> results = get();
                    handleResults(results, gobdActive, timeoutSec);
                } catch (java.util.concurrent.CancellationException ex) {
                    appendLog("STOPPED: " + summarizeStatements(statements));
                    statusLabel.setText(Messages.KONSOLE_STATUS_STOPPED.toString());
                    outputTabs.setSelectedIndex(1);
                } catch (Exception ex) {
                    Log.Debug(ControlPanel_Konsole.this, ex.getMessage());
                    appendLog("ERROR: " + ex.getMessage());
                    outputTabs.setSelectedIndex(1);
                    statusLabel.setText("ERROR: " + ex.getMessage());
                }
            }
        };
        activeWorker = worker;
        worker.execute();
    }

    private void stopExecution() {
        if (!busy) {
            return;
        }
        stopButton.setEnabled(false);
        statusLabel.setText(Messages.KONSOLE_STATUS_STOPPING.toString());
        AtomicBoolean flag = activeCancelFlag;
        if (flag != null) {
            flag.set(true);
        }
        // Cancel JDBC statement off the EDT so a slow driver never freezes the UI
        Thread cancelThread = new Thread(() -> SqlConsole.cancelQuietly(activeStatement), "sql-console-cancel");
        cancelThread.setDaemon(true);
        cancelThread.start();
    }

    /**
     * Prompts the user whenever one or more statements modify data or table structure:
     * <ul>
     * <li>Strong warning dialog for {@code ALTER TABLE}, {@code DROP}, {@code TRUNCATE}, {@code CREATE}, or {@code gobd_*} targets</li>
     * <li>Confirmation dialog for {@code UPDATE}, {@code DELETE}, {@code INSERT}, etc. (with extra warning if {@code WHERE} is missing)</li>
     * <li>Default button is always Cancel so pressing Enter never executes accidentally</li>
     * </ul>
     */
    private boolean confirmIfModifying(List<StatementInfo> statements, boolean gobdActive) {
        List<StatementInfo> modifying = new ArrayList<StatementInfo>();
        RiskLevel highest = RiskLevel.READ_ONLY;
        boolean anyMissingWhere = false;
        boolean anyGobdTable = false;
        boolean anyTaxTable = false;
        List<String> kinds = new ArrayList<String>();
        List<String> tables = new ArrayList<String>();
        List<String> gobdTables = new ArrayList<String>();

        for (StatementInfo s : statements) {
            if (!s.modifying) {
                continue;
            }
            modifying.add(s);
            RiskLevel r = s.riskLevel(gobdActive);
            if (r.ordinal() > highest.ordinal()) {
                highest = r;
            }
            if (!kinds.contains(s.label())) {
                kinds.add(s.label());
            }
            for (String t : s.tables) {
                if (!tables.contains(t)) {
                    tables.add(t);
                }
                if (t.toLowerCase(Locale.ROOT).startsWith("gobd_") && !gobdTables.contains(t)) {
                    gobdTables.add(t);
                }
            }
            anyMissingWhere |= s.missingWhere;
            anyGobdTable |= s.touchesGobdTable;
            anyTaxTable |= s.touchesTaxTable;
        }
        if (modifying.isEmpty()) {
            return true;
        }

        boolean isDdlWarning = highest == RiskLevel.WARNING_DDL || highest == RiskLevel.CRITICAL_GOBD;
        String kindText = String.join(", ", kinds);
        String tableText = tables.isEmpty() ? "-" : String.join(", ", tables);

        String title = isDdlWarning
                ? Messages.KONSOLE_WARN_ALTER_TITLE.toString()
                : Messages.KONSOLE_CONFIRM_UPDATE_TITLE.toString();
        String body = isDdlWarning
                ? String.format(Messages.KONSOLE_WARN_ALTER_BODY.toString(), kindText, tableText)
                : String.format(Messages.KONSOLE_CONFIRM_UPDATE_BODY.toString(), kindText, tableText);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextArea msgArea = new JTextArea(body);
        msgArea.setEditable(false);
        msgArea.setOpaque(false);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setColumns(56);
        msgArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(msgArea);

        if (anyMissingWhere) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(warningBox(Messages.KONSOLE_WARN_NO_WHERE.toString(), BADGE_DDL_BG, BADGE_DDL_FG));
        }
        if (anyGobdTable) {
            panel.add(Box.createVerticalStrut(8));
            String gt = gobdTables.isEmpty() ? "gobd_*" : String.join(", ", gobdTables);
            panel.add(warningBox(String.format(Messages.KONSOLE_WARN_GOBD_TABLE.toString(), gt), BADGE_DDL_BG, BADGE_DDL_FG));
        }
        if (gobdActive && (anyTaxTable || anyGobdTable || isDdlWarning)) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(warningBox(Messages.KONSOLE_WARN_GOBD_ACTIVE.toString(), BADGE_GOBD_BG, BADGE_GOBD_FG));
        }

        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < modifying.size(); i++) {
            if (i > 0) {
                preview.append("\n\n");
            }
            preview.append(modifying.get(i).sql).append(';');
        }
        JTextArea sqlPreview = new JTextArea(preview.toString(), Math.min(10, Math.max(4, modifying.size() * 2 + 2)), 56);
        sqlPreview.setEditable(false);
        sqlPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ModernTheme.baseFont().getSize()));
        sqlPreview.setCaretPosition(0);
        JScrollPane previewScroll = new JScrollPane(sqlPreview);
        previewScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewScroll.setPreferredSize(new Dimension(ModernTheme.px(520), ModernTheme.px(130)));

        panel.add(Box.createVerticalStrut(10));
        panel.add(previewScroll);

        Object[] options = new Object[]{
            Messages.KONSOLE_CANCEL.toString(),
            Messages.KONSOLE_CONFIRM_EXECUTE.toString()
        };
        int msgType = (anyGobdTable || anyMissingWhere)
                ? JOptionPane.ERROR_MESSAGE
                : (isDdlWarning ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE);

        int choice = JOptionPane.showOptionDialog(
                this,
                panel,
                title,
                JOptionPane.DEFAULT_OPTION,
                msgType,
                null,
                options,
                options[0] // Default button = Cancel
        );
        return choice == 1;
    }

    private static JComponent warningBox(String text, Color bg, Color fg) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(54);
        area.setFont(ModernTheme.font(0, Font.BOLD));
        area.setBackground(bg);
        area.setForeground(fg);
        area.setOpaque(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    private void handleResults(List<ExecutionResult> results, boolean gobdActive, int timeoutSec) {
        ExecutionResult lastTableResult = null;
        ExecutionResult lastResult = null;
        boolean anyDdl = false;
        boolean anyError = false;

        for (ExecutionResult r : results) {
            lastResult = r;
            if (r.info.alterOrDdl && r.success) {
                anyDdl = true;
            }
            if (!r.success) {
                anyError = true;
            }
            String auditNote = (r.info.modifying && gobdActive) ? " [GoBD-Audit-Log]" : "";
            String tbl = r.info.targetTablesSummary();
            appendLog(r.info.label() + (tbl.isEmpty() ? "" : " [" + tbl + "]")
                    + " -> " + r.outcomeSummary() + auditNote + "\n    " + r.info.sql.replace("\n", "\n    "));
            if (r.success && r.hasResultSet) {
                lastTableResult = r;
            }
        }

        if (lastTableResult != null) {
            lastColumns = lastTableResult.columns;
            lastRows = lastTableResult.rows;
            resultModel.setDataVector(lastRows, lastColumns);
            autoSizeColumns();
            exportCsvButton.setEnabled(lastColumns.length > 0);
        }

        if (lastResult != null && !lastResult.success) {
            if (lastResult.cancelled) {
                statusLabel.setText(Messages.KONSOLE_STATUS_STOPPED.toString());
            } else if (lastResult.timedOut && timeoutSec > 0) {
                statusLabel.setText(String.format(Messages.KONSOLE_STATUS_TIMEOUT.toString(), timeoutSec));
            } else {
                statusLabel.setText("ERROR: " + lastResult.errorMessage);
            }
            outputTabs.setSelectedIndex(1);
        } else if (lastTableResult != null) {
            statusLabel.setText(String.format(
                    lastTableResult.truncated
                            ? Messages.KONSOLE_STATUS_ROWS_TRUNC.toString()
                            : Messages.KONSOLE_STATUS_ROWS.toString(),
                    lastTableResult.rows.length, lastTableResult.durationMs));
            outputTabs.setSelectedIndex(anyError ? 1 : 0);
        } else if (lastResult != null) {
            if (lastResult.updateCount >= 0) {
                statusLabel.setText(String.format(Messages.KONSOLE_STATUS_AFFECTED.toString(),
                        lastResult.updateCount, lastResult.durationMs));
            } else {
                statusLabel.setText(lastResult.outcomeSummary());
            }
            outputTabs.setSelectedIndex(1);
        }

        if (anyDdl) {
            loadTablesAsync();
        }
    }

    private void executeGroovy(String raw) {
        if (!MPSecurityManager.isAdmin()) {
            return;
        }
        String script = raw.replaceFirst("(?i)^groovy\\b", "").trim();
        if (script.isEmpty()) {
            return;
        }
        boolean gobdActive = SqlConsole.isGobdActive();
        String msg = Messages.KONSOLE_CONFIRM_GROOVY_BODY.toString()
                + (gobdActive ? "\n\n" + Messages.KONSOLE_WARN_GOBD_ACTIVE.toString() : "");
        Object[] options = new Object[]{Messages.KONSOLE_CANCEL.toString(), Messages.KONSOLE_CONFIRM_EXECUTE.toString()};
        int choice = JOptionPane.showOptionDialog(this, msg, Messages.KONSOLE_CONFIRM_GROOVY_TITLE.toString(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice != 1) {
            statusLabel.setText(Messages.KONSOLE_STATUS_CANCELLED.toString());
            return;
        }
        rememberHistory(raw);
        try {
            Object val = getGroovyShell().evaluate(script);
            String out = String.valueOf(val);
            SqlConsole.logGroovyExecution(script, true, "OK");
            appendLog("GROOVY -> OK" + (gobdActive ? " [GoBD-Audit-Log]" : "") + "\n    " + out);
            statusLabel.setText("Groovy OK");
            outputTabs.setSelectedIndex(1);
        } catch (Exception ex) {
            Log.Debug(ex);
            SqlConsole.logGroovyExecution(script, false, "ERROR: " + ex.getMessage());
            appendLog("GROOVY -> ERROR: " + ex.getMessage());
            statusLabel.setText("ERROR: " + ex.getMessage());
            outputTabs.setSelectedIndex(1);
        }
    }

    private void autoSizeColumns() {
        int colCount = resultTable.getColumnCount();
        int rowCount = Math.min(resultTable.getRowCount(), 100);
        for (int c = 0; c < colCount; c++) {
            TableColumn tc = resultTable.getColumnModel().getColumn(c);
            int width = 80;
            TableCellRenderer hr = resultTable.getTableHeader().getDefaultRenderer();
            Component hc = hr.getTableCellRendererComponent(resultTable, tc.getHeaderValue(), false, false, -1, c);
            width = Math.max(width, hc.getPreferredSize().width + 20);
            for (int r = 0; r < rowCount; r++) {
                TableCellRenderer cr = resultTable.getCellRenderer(r, c);
                Component cc = resultTable.prepareRenderer(cr, r, c);
                width = Math.max(width, cc.getPreferredSize().width + 16);
            }
            tc.setPreferredWidth(Math.min(360, Math.max(70, width)));
        }
    }

    private void setBusy(boolean b, int totalStatements) {
        busy = b;
        runButton.setEnabled(!b);
        stopButton.setEnabled(b);
        if (b) {
            execStartMs = System.currentTimeMillis();
            execTotalStmts = Math.max(1, totalStatements);
            execCurrentStmt = 1;
            progressBar.setIndeterminate(execTotalStmts <= 1);
            if (execTotalStmts > 1) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(execTotalStmts);
                progressBar.setValue(0);
            }
            progressBar.setVisible(true);
            updateRunningStatusText();
            if (elapsedTimer == null) {
                elapsedTimer = new Timer(250, e -> {
                    if (busy && (activeCancelFlag == null || !activeCancelFlag.get())) {
                        updateRunningStatusText();
                    }
                });
            }
            elapsedTimer.restart();
        } else {
            if (elapsedTimer != null) {
                elapsedTimer.stop();
            }
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
            statusLabel.setText(Messages.KONSOLE_STATUS_READY.toString());
        }
    }

    private void updateRunningStatusText() {
        long secs = Math.max(0L, (System.currentTimeMillis() - execStartMs) / 1000L);
        if (execTotalStmts > 1) {
            statusLabel.setText(String.format(Messages.KONSOLE_STATUS_RUNNING_STEP.toString(),
                    execCurrentStmt, execTotalStmts, secs));
        } else if (secs > 0) {
            statusLabel.setText(String.format(Messages.KONSOLE_STATUS_RUNNING_TIME.toString(), secs));
        } else {
            statusLabel.setText(Messages.KONSOLE_STATUS_RUNNING.toString());
        }
    }

    private int selectedLimit() {
        LimitItem item = (LimitItem) limitCombo.getSelectedItem();
        return item == null ? 1000 : item.limit;
    }

    private int selectedTimeoutSeconds() {
        TimeoutItem item = (TimeoutItem) timeoutCombo.getSelectedItem();
        return item == null ? 30 : item.seconds;
    }

    private void appendLog(String line) {
        String ts = new SimpleDateFormat("HH:mm:ss").format(new Date());
        if (logArea.getText().length() > 0) {
            logArea.append("\n");
        }
        logArea.append("[" + ts + "] " + line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private static String summarizeStatements(List<StatementInfo> stmts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stmts.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            StatementInfo s = stmts.get(i);
            sb.append(s.label());
            if (!s.tables.isEmpty()) {
                sb.append(' ').append(s.targetTablesSummary());
            }
        }
        return sb.toString();
    }

    private void rememberHistory(String sql) {
        String trimmed = sql.trim();
        history.remove(trimmed);
        history.addFirst(trimmed);
        while (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
        refreshHistoryCombo();
    }

    private void refreshHistoryCombo() {
        updatingCombos = true;
        historyCombo.removeAllItems();
        historyCombo.addItem(new HistoryItem(Messages.KONSOLE_HISTORY.toString(), null));
        for (String s : history) {
            String oneLine = s.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s+", " ");
            String label = oneLine.length() > 55 ? oneLine.substring(0, 52) + "…" : oneLine;
            historyCombo.addItem(new HistoryItem(label, s));
        }
        updatingCombos = false;
    }

    // ------------------------------------------------------------------ File I/O

    private void loadSqlFile() {
        DialogForFile dialog = new DialogForFile(DialogForFile.FILES_ONLY);
        dialog.setFileFilter(DialogForFile.SQL_FILES);
        if (dialog.chooseFile()) {
            try {
                File f = dialog.getFile();
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                editorArea.setText(content);
                editorArea.setCaretPosition(0);
            } catch (Exception ex) {
                Popup.error(ex);
            }
        }
    }

    private void saveSqlFile() {
        DialogForFile dialog = new DialogForFile(DialogForFile.FILES_ONLY, "query.sql");
        if (dialog.saveFile()) {
            File f = dialog.getFile();
            if (!f.getName().toLowerCase(Locale.ROOT).endsWith(".sql")
                    && !f.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                f = new File(f.getParentFile(), f.getName() + ".sql");
            }
            new FileReaderWriter(f).write(editorArea.getText());
        }
    }

    private void exportResultCsv() {
        if (lastColumns.length == 0) {
            return;
        }
        DialogForFile dialog = new DialogForFile(DialogForFile.FILES_ONLY, "sql_result.csv");
        if (dialog.saveFile()) {
            try {
                File f = dialog.getFile();
                if (!f.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
                    f = new File(f.getParentFile(), f.getName() + ".csv");
                }
                Files.write(f.toPath(), SqlConsole.toCsv(lastColumns, lastRows).getBytes(StandardCharsets.UTF_8));
                appendLog("CSV exported: " + f.getAbsolutePath());
            } catch (Exception ex) {
                Popup.error(ex);
            }
        }
    }

    private GroovyShell getGroovyShell() {
        synchronized (this) {
            if (groovyShell == null) {
                Binding binding = new Binding();
                binding.setVariable("db", QueryHandler.instanceOf());
                groovyShell = new GroovyShell(binding);
            }
            return groovyShell;
        }
    }

    // ------------------------------------------------------------------ ControlApplet

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        remove(actions);
        revalidate();
        return actions;
    }

    // ------------------------------------------------------------------ Inner helpers

    private static final class TemplateItem {
        final String label;
        final String sql;

        TemplateItem(String label, String sql) {
            this.label = label;
            this.sql = sql;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class HistoryItem {
        final String label;
        final String sql;

        HistoryItem(String label, String sql) {
            this.label = label;
            this.sql = sql;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class LimitItem {
        final String label;
        final int limit;

        LimitItem(String label, int limit) {
            this.label = label;
            this.limit = limit;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class TimeoutItem {
        final String label;
        final int seconds;

        TimeoutItem(String label, int seconds) {
            this.label = label;
            this.seconds = seconds;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class SqlCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value == null) {
                lbl.setText("NULL");
                lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
                if (!isSelected) {
                    lbl.setForeground(ModernTheme.muted());
                }
                lbl.setToolTipText("NULL");
            } else {
                String text = String.valueOf(value);
                lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                if (!isSelected) {
                    lbl.setForeground(table.getForeground());
                }
                lbl.setToolTipText(text.length() > 300 ? text.substring(0, 300) + "…" : text);
            }
            return lbl;
        }
    }

    private static final class SimpleDocListener implements DocumentListener {
        private final Runnable action;

        SimpleDocListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            action.run();
        }
    }
}
