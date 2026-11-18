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
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Account;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.handler.ExpenseReceipts;
import mpv5.handler.ExpenseReceipts.Receipt;
import mpv5.handler.ZugpferdExpenseImport;
import mpv5.logging.Log;
import mpv5.ui.beans.DateChooser;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.MPTable;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.modern.WrapLayout;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.ui.toolbars.RecordActions;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.ui.TextFieldUtils;

/**
 * Modern view for expenses ({@link Expense}) based on {@link ModernDataPanel}.
 * <p>
 * Key features:
 * <ul>
 *   <li>Horizontal split view with expense master list and filter on the left
 *       and expense details on the right.</li>
 *   <li>Card-based modern layout with 4 responsive cards (details, organization/notes,
 *       receipts, and PDF preview).</li>
 *   <li>Integrated receipts section: list of imported receipt files with SHA-256 checksums,
 *       file sizes and timestamps, directly openable with system viewer.</li>
 *   <li>Drag &amp; drop and file import automatically check for ZUGFeRD / XRechnung
 *       format and populate/import the expense data when available.</li>
 *   <li>Support for plugin banner, badge and content-lock hooks.</li>
 * </ul>
 * <p>
 * Public fields ending with an underscore are data-bound via
 * {@link DatabaseObject#setPanelData(DataPanel)} and {@link DatabaseObject#getPanelData(DataPanel)}.
 */
public class ModernExpensePanel extends ModernDataPanel {

    private static final long serialVersionUID = 1L;
    private static ModernExpensePanel instance;

    public static synchronized ModernExpensePanel instanceOf() {
        if (instance == null) {
            instance = new ModernExpensePanel();
        }
        instance.refresh();
        return instance;
    }

    /**
     * Refreshes the expense panel if it has already been instantiated.
     */
    public static synchronized void refreshIfOpen() {
        if (instance != null) {
            instance.refresh();
        }
    }

    private enum DateFilterMode {
        ALL,
        CURRENT_YEAR,
        CURRENT_MONTH,
        CUSTOM
    }

    private Expense dataOwner;
    private final ModernCommandBar tb;
    private final JSplitPane splitPane;
    private final JPanel listPanel;
    private final JPanel detailPanel;
    private final ResponsiveGrid detailGrid = new ResponsiveGrid(340, 2).arrangeable("expense");
    private String loadedCnumber = "";

    // Bound fields (names must match Expense fields with underscore)
    public String cname_;
    public String description_;
    public String cnumber_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public BigDecimal netvalue_;
    public BigDecimal taxpercentvalue_;
    public BigDecimal brutvalue_;
    public int accountsids_;
    public boolean ispaid_;
    public Date dateend_;

    // Form inputs
    private final LabeledTextField idField = field();
    private final LabeledTextField cnumberField = field();
    private final LabeledTextField brutValueField = field();
    private final LabeledTextField netValueField = field();
    private final LabeledCombobox taxRateSelect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox accountSelect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox groupSelect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledDateChooser issueDateChooser = new LabeledDateChooser();
    private final LabeledDateChooser payDateChooser = new LabeledDateChooser();
    private final JCheckBox paidCheckBox = new JCheckBox("Bezahlt / Ausgeglichen");
    private final JTextArea notesArea = new JTextArea(4, 25);
    private final JLabel addedByLabel = new JLabel();

    // Master list & filters
    private final JTable expenseTable = new MPTable(this);
    private final JTextField searchField = new JTextField(14);
    private final DateChooser fromDateChooser = new DateChooser();
    private final DateChooser toDateChooser = new DateChooser();
    private final ChipToggle filterAllChip = new ChipToggle("Alle");
    private final ChipToggle filterYearChip = new ChipToggle("Akt. Jahr");
    private final ChipToggle filterMonthChip = new ChipToggle("Akt. Monat");
    private final ChipToggle filterDateChip = new ChipToggle("Zeitraum");
    private final ButtonGroup filterGroup = new ButtonGroup();
    private DateFilterMode dateFilterMode = DateFilterMode.ALL;
    private boolean updatingFilterDates = false;
    private final PrimaryButton importZugferdBtn = new PrimaryButton(Messages.ZUGFERD_IMPORT_BUTTON.toString(), "/images/16/xml.png");

    // Receipts table
    private final DefaultTableModel receiptsModel = new DefaultTableModel(
            new Object[]{"ID", "Dateiname", "Gr\u00f6\u00dfe", "SHA-256", "Importiert am", "Benutzer"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable receiptsTable = new JTable(receiptsModel);
    private final List<Receipt> currentReceipts = new ArrayList<Receipt>();
    private final JButton addReceiptBtn = new JButton("Beleg verkn\u00fcpfen...");
    private final JButton removeReceiptBtn = new JButton("Entfernen");
    private final JButton openReceiptBtn = new JButton("\u00d6ffnen");
    private final JButton verifyReceiptsBtn = new JButton("Pr\u00fcfen");

    // PDF preview card components (like template manager)
    private final JLabel previewImage = new JLabel();
    private final JLabel previewCaption = new JLabel();
    private final JButton previewLargerBtn = new JButton(Messages.TEMPLATES_PREVIEW_LARGER.toString());
    private File previewPdf;
    private File lastImportedPreviewFile;
    private int previewRun;

    private BigDecimal currentTaxRate = BigDecimal.ZERO;
    private ArrayList<DatabaseObject> accountObjects;

    public ModernExpensePanel() {
        super(new BorderLayout());
        setName("modernexpensepanel");
        dataOwner = new Expense();

        initFields();

        // Horizontal split: list of expenses on the left, details on the right
        listPanel = createListPanel();
        detailPanel = createDetailPanel();
        splitPane = horizontalSplit("expense", listPanel, detailPanel, 0.38);
        add(splitPane, BorderLayout.CENTER);

        tb = new ModernCommandBar(this);
        tb.setExportButtonsEnabled(false);
        tb.setSaveAsNewAction(() -> RecordActions.saveAsNew(this));
        tb.setFindAction(() -> {
            searchField.requestFocusInWindow();
            searchField.selectAll();
        });
        add(tb, BorderLayout.NORTH);

        // Listen for lock changes to update editability
        addPropertyChangeListener(PROPERTY_CONTENT_LOCKED, evt -> {
            boolean locked = Boolean.TRUE.equals(evt.getNewValue());
            applyContentLock(locked);
        });

        setupDragAndDrop();
        refresh();
    }

    public ModernCommandBar getCommandBar() {
        return tb;
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public JPanel getListPanel() {
        return listPanel;
    }

    public JPanel getDetailPanel() {
        return detailPanel;
    }

    public ResponsiveGrid getDetailGrid() {
        return detailGrid;
    }

    public JTable getExpenseTable() {
        return expenseTable;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JTextField getFilterField() {
        return searchField;
    }

    public JLabel getPreviewImage() {
        return previewImage;
    }

    public JLabel getPreviewCaption() {
        return previewCaption;
    }

    public File getPreviewPdf() {
        return previewPdf;
    }

    public PrimaryButton getImportZugferdButton() {
        return importZugferdBtn;
    }

    public JButton getAddReceiptButton() {
        return addReceiptBtn;
    }

    private void initFields() {
        idField.setEnabled(false);
        idField.setFocusable(false);
        idField.set_Label("ID:");

        cnumberField.set_Label("Belegnummer:");
        cnumberField.getTextField().setToolTipText("Interne oder externe Belegnummer");

        brutValueField.set_Label("Bruttobetrag:");
        netValueField.set_Label("Nettobetrag:");
        netValueField.getTextField().setEditable(false);

        taxRateSelect.set_Label("Steuersatz:");
        taxRateSelect.setSearchEnabled(true);
        taxRateSelect.setContext(Context.getTaxes());
        taxRateSelect.getComboBox().addActionListener(e -> {
            updateTaxRate();
            calculate();
        });

        accountSelect.set_Label("Konto:");
        accountSelect.setSearchEnabled(true);
        accountSelect.setContext(Context.getAccounts());

        groupSelect.set_Label("Gruppe:");
        groupSelect.setSearchEnabled(true);
        groupSelect.setContext(Context.getGroup());

        issueDateChooser.set_Label("Belegdatum:");
        payDateChooser.set_Label("Zahlungsdatum:");

        paidCheckBox.setOpaque(false);
        paidCheckBox.setFont(ModernTheme.font(1, Font.BOLD));

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        ModernTheme.roomy(notesArea);
        ModernTheme.tabLeaves(notesArea);

        addedByLabel.setFont(ModernTheme.font(0, Font.PLAIN));
        addedByLabel.setForeground(ModernTheme.muted());

        brutValueField.getTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculate();
            }
        });
    }

    private void updateTaxRate() {
        try {
            if (taxRateSelect.getSelectedItem() != null) {
                currentTaxRate = Tax.getTaxValue(Integer.valueOf(taxRateSelect.getSelectedItem().getId()));
            } else {
                currentTaxRate = BigDecimal.ZERO;
            }
        } catch (Exception ex) {
            currentTaxRate = BigDecimal.ZERO;
        }
    }

    private void calculate() {
        try {
            BigDecimal brut = FormatNumber.parseDezimal(brutValueField.getText());
            if (brut == null) brut = BigDecimal.ZERO;
            BigDecimal net = brut.divide(currentTaxRate.divide(Constants.BD100, 9, RoundingMode.HALF_UP).add(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
            netValueField.setText(FormatNumber.formatLokalCurrency(net));
        } catch (Exception e) {
            netValueField.setText(FormatNumber.formatLokalCurrency(0d));
        }
    }

    private JPanel createListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(ModernTheme.padding(6, 6));

        ModernTheme.style(expenseTable);
        expenseTable.setAutoCreateRowSorter(true);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = expenseTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = expenseTable.convertRowIndexToModel(row);
                    Object val = expenseTable.getModel().getValueAt(modelRow, 0);
                    if (val instanceof DatabaseObject) {
                        setDataOwner((DatabaseObject) val, true);
                    }
                }
            }
        });

        JComponent filterBar = createFilterBar();

        Card card = new Card("Ausgaben");
        card.addFull(filterBar);
        card.addFull(new JScrollPane(expenseTable), 1);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JComponent createFilterBar() {
        JPanel filterBar = new JPanel();
        filterBar.setOpaque(false);
        filterBar.setLayout(new BoxLayout(filterBar, BoxLayout.Y_AXIS));
        filterBar.setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(6), 0));

        // Row 0: Large prominent green button for ZUGFeRD / E-Rechnung import
        importZugferdBtn.setColor(new Color(22, 163, 74));
        importZugferdBtn.setFont(ModernTheme.font(1, Font.BOLD));
        importZugferdBtn.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(9), ModernTheme.px(16), ModernTheme.px(9), ModernTheme.px(16)));
        importZugferdBtn.setToolTipText(Messages.ZUGFERD_IMPORT_HINT.toString());
        importZugferdBtn.addActionListener(e -> importZugferdDialog());

        JPanel importRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        importRow.setOpaque(false);
        importRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        importRow.setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(8), 0));
        importRow.add(importZugferdBtn);
        filterBar.add(importRow);

        // Row 1: Content search field
        ModernTheme.roomy(searchField);
        searchField.setToolTipText("Filter nach Inhalt (Nummer, Beschreibung, Konto, Betrag, Datum)");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyExpenseFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyExpenseFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyExpenseFilter();
            }
        });
        searchField.addActionListener(e -> applyExpenseFilter());

        JPanel searchRow = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        searchRow.setOpaque(false);
        searchRow.add(ModernTheme.fieldLabel("Filter:"), BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);
        JButton clearBtn = link("Zur\u00fccksetzen", null);
        clearBtn.addActionListener(e -> clearFilter());
        searchRow.add(clearBtn, BorderLayout.EAST);

        // Row 2: Quick date filter chips (Alle, Akt. Jahr, Akt. Monat, Zeitraum)
        filterGroup.add(filterAllChip);
        filterGroup.add(filterYearChip);
        filterGroup.add(filterMonthChip);
        filterGroup.add(filterDateChip);
        filterAllChip.setSelected(true);

        filterAllChip.addActionListener(e -> {
            dateFilterMode = DateFilterMode.ALL;
            applyExpenseFilter();
        });
        filterYearChip.addActionListener(e -> filterCurrentYear());
        filterMonthChip.addActionListener(e -> filterCurrentMonth());
        filterDateChip.addActionListener(e -> {
            dateFilterMode = DateFilterMode.CUSTOM;
            applyExpenseFilter();
        });

        JPanel chipsRow = new JPanel(new WrapLayout(FlowLayout.LEFT, ModernTheme.px(4), ModernTheme.px(2)));
        chipsRow.setOpaque(false);
        chipsRow.add(filterAllChip);
        chipsRow.add(filterYearChip);
        chipsRow.add(filterMonthChip);
        chipsRow.add(filterDateChip);

        // Row 3: Custom date range (Von - Bis)
        updatingFilterDates = true;
        try {
            Date now = new Date();
            fromDateChooser.setDate(DateConverter.getStartOfYear(now));
            toDateChooser.setDate(now);
        } finally {
            updatingFilterDates = false;
        }
        fromDateChooser.getSpinner().addChangeListener(e -> {
            if (!updatingFilterDates) {
                dateFilterMode = DateFilterMode.CUSTOM;
                filterDateChip.setSelected(true);
                applyExpenseFilter();
            }
        });
        toDateChooser.getSpinner().addChangeListener(e -> {
            if (!updatingFilterDates) {
                dateFilterMode = DateFilterMode.CUSTOM;
                filterDateChip.setSelected(true);
                applyExpenseFilter();
            }
        });

        JPanel dateRow = new JPanel(new WrapLayout(FlowLayout.LEFT, ModernTheme.px(4), ModernTheme.px(2)));
        dateRow.setOpaque(false);
        dateRow.add(ModernTheme.fieldLabel("Von:"));
        dateRow.add(fromDateChooser);
        dateRow.add(ModernTheme.fieldLabel("Bis:"));
        dateRow.add(toDateChooser);

        filterBar.add(searchRow);
        filterBar.add(Box.createVerticalStrut(ModernTheme.px(4)));
        filterBar.add(chipsRow);
        filterBar.add(dateRow);
        return filterBar;
    }

    /**
     * Filters the expense table by text content.
     */
    public void filterByContent(String query) {
        searchField.setText(query == null ? "" : query);
        applyExpenseFilter();
    }

    /**
     * Filters the expense table to the current calendar year.
     */
    public void filterCurrentYear() {
        Date now = new Date();
        updatingFilterDates = true;
        try {
            fromDateChooser.setDate(DateConverter.getStartOfYear(now));
            toDateChooser.setDate(DateConverter.getEndOfYear(now));
        } finally {
            updatingFilterDates = false;
        }
        dateFilterMode = DateFilterMode.CURRENT_YEAR;
        filterYearChip.setSelected(true);
        applyExpenseFilter();
    }

    /**
     * Filters the expense table to the current calendar month.
     */
    public void filterCurrentMonth() {
        Date now = new Date();
        updatingFilterDates = true;
        try {
            fromDateChooser.setDate(DateConverter.getStartOfMonth(now));
            toDateChooser.setDate(DateConverter.getEndOfMonth(now));
        } finally {
            updatingFilterDates = false;
        }
        dateFilterMode = DateFilterMode.CURRENT_MONTH;
        filterMonthChip.setSelected(true);
        applyExpenseFilter();
    }

    /**
     * Filters the expense table to the given inclusive date range.
     */
    public void filterByDateRange(Date from, Date to) {
        updatingFilterDates = true;
        try {
            if (from != null) {
                fromDateChooser.setDate(from);
            }
            if (to != null) {
                toDateChooser.setDate(to);
            }
        } finally {
            updatingFilterDates = false;
        }
        dateFilterMode = DateFilterMode.CUSTOM;
        filterDateChip.setSelected(true);
        applyExpenseFilter();
    }

    /**
     * Clears both content and date filters.
     */
    public void clearFilter() {
        dateFilterMode = DateFilterMode.ALL;
        filterAllChip.setSelected(true);
        searchField.setText("");
        applyExpenseFilter();
    }

    @SuppressWarnings("unchecked")
    public void applyExpenseFilter() {
        TableModel model = expenseTable.getModel();
        if (model == null) {
            return;
        }
        TableRowSorter<TableModel> sorter;
        if (expenseTable.getRowSorter() instanceof TableRowSorter
                && ((TableRowSorter<?>) expenseTable.getRowSorter()).getModel() == model) {
            sorter = (TableRowSorter<TableModel>) expenseTable.getRowSorter();
        } else {
            sorter = new TableRowSorter<TableModel>(model);
            expenseTable.setRowSorter(sorter);
        }

        final String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        final DateFilterMode mode = dateFilterMode;
        final Date start;
        final Date end;
        Date now = new Date();
        if (mode == DateFilterMode.CURRENT_YEAR) {
            start = DateConverter.getStartOfYear(now);
            end = DateConverter.getEndOfYear(now);
        } else if (mode == DateFilterMode.CURRENT_MONTH) {
            start = DateConverter.getStartOfMonth(now);
            end = DateConverter.getEndOfMonth(now);
        } else if (mode == DateFilterMode.CUSTOM) {
            Date f = fromDateChooser.getDate();
            Date t = toDateChooser.getDate();
            if (f != null && t != null && f.after(t)) {
                Date tmp = f;
                f = t;
                t = tmp;
            }
            start = f != null ? DateConverter.getStartOfDay(f) : null;
            end = t != null ? DateConverter.getEndOfDay(t) : null;
        } else {
            start = null;
            end = null;
        }

        if (query.isEmpty() && mode == DateFilterMode.ALL) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                if (mode != DateFilterMode.ALL) {
                    Date rowDate = extractRowDate(entry);
                    if (rowDate == null) {
                        return false;
                    }
                    if (start != null && rowDate.before(start)) {
                        return false;
                    }
                    if (end != null && rowDate.after(end)) {
                        return false;
                    }
                }
                if (!query.isEmpty()) {
                    return matchesContent(entry, query);
                }
                return true;
            }
        });
    }

    private static Date extractRowDate(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        if (entry.getValueCount() > 6 && entry.getValue(6) instanceof Date) {
            return (Date) entry.getValue(6);
        }
        if (entry.getValueCount() > 0 && entry.getValue(0) instanceof Expense) {
            Expense exp = (Expense) entry.getValue(0);
            if (exp.__getDateadded() != null) {
                return exp.__getDateadded();
            }
            if (exp.__getDateend() != null) {
                return exp.__getDateend();
            }
        }
        if (entry.getValueCount() > 7 && entry.getValue(7) instanceof Date) {
            return (Date) entry.getValue(7);
        }
        return null;
    }

    private static boolean matchesContent(RowFilter.Entry<? extends TableModel, ? extends Integer> entry, String query) {
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < entry.getValueCount(); i++) {
            Object val = entry.getValue(i);
            if (val == null) {
                continue;
            }
            sb.append(val).append(' ');
            if (val instanceof Expense) {
                Expense exp = (Expense) val;
                if (exp.__getCnumber() != null) sb.append(exp.__getCnumber()).append(' ');
                if (exp.__getCname() != null) sb.append(exp.__getCname()).append(' ');
                if (exp.__getDescription() != null) sb.append(exp.__getDescription()).append(' ');
            } else if (val instanceof Date) {
                Date d = (Date) val;
                sb.append(DateConverter.getDefDateString(d)).append(' ');
                sb.append(DateConverter.getSQLDateString(d)).append(' ');
            }
        }
        String haystack = sb.toString().toLowerCase(Locale.ROOT);
        for (String token : query.split("\\s+")) {
            if (!token.isEmpty() && !haystack.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private JPanel createDetailPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        // Card 1: Basisdaten & Betr\u00e4ge
        Card c1 = new Card("Ausgabendaten")
                .headerAction(createPluginBadgeSlot())
                .row(Card.field("ID", idField, 1), Card.field("Belegnummer", cnumberField, 2))
                .row(Card.field("Brutto", brutValueField), Card.field("Netto", netValueField))
                .row(Card.field("Steuersatz", taxRateSelect), Card.field("Konto", accountSelect))
                .row(Card.field("Belegdatum", issueDateChooser), Card.field("Zahlungsdatum", payDateChooser))
                .row(Card.field(null, paidCheckBox));
        detailGrid.add(c1);

        // Card 2: Organisation & Notizen
        Card c2 = new Card("Organisation & Notizen")
                .row(Card.field("Gruppe", groupSelect), Card.field("Erfasst von", addedByLabel))
                .row(Card.field("Beschreibung / Verwendungszweck", new JScrollPane(notesArea)));
        detailGrid.add(c2);

        // Card 3: Belege & Scans
        Card c3 = createReceiptsCard();
        detailGrid.add(c3);

        // Card 4: PDF Preview (like template manager)
        Card c4 = createPreviewCard();
        detailGrid.add(c4);

        p.add(scroll(detailGrid), BorderLayout.CENTER);
        return p;
    }

    private Card createReceiptsCard() {
        Card card = new Card("Eingangsbelege & Scans");

        ModernTheme.style(receiptsTable);
        receiptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        receiptsTable.getColumnModel().getColumn(2).setMaxWidth(80);
        receiptsTable.getColumnModel().getColumn(3).setPreferredWidth(180);

        receiptsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshPreview();
            }
        });

        receiptsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedReceipt();
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        actions.setOpaque(false);

        addReceiptBtn.addActionListener(e -> importReceiptDialog());
        removeReceiptBtn.addActionListener(e -> removeSelectedReceipt());
        openReceiptBtn.addActionListener(e -> openSelectedReceipt());
        verifyReceiptsBtn.addActionListener(e -> verifyAllReceipts());

        actions.add(addReceiptBtn);
        actions.add(openReceiptBtn);
        actions.add(removeReceiptBtn);
        actions.add(verifyReceiptsBtn);

        card.headerAction(actions);

        JLabel hint = new JLabel("<html><small>Dateien k\u00f6nnen per Drag &amp; Drop direkt in diese Tabelle gezogen werden. "
                + "Die Belege werden unver\u00e4nderbar im Jahresarchiv abgelegt und per SHA256SUMS gesichert.</small></html>");
        hint.setForeground(ModernTheme.muted());
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(4), 0));

        card.addFull(hint);
        card.addFull(new JScrollPane(receiptsTable), 1);
        return card;
    }

    private Card createPreviewCard() {
        previewImage.setHorizontalAlignment(JLabel.CENTER);
        previewImage.setVerticalAlignment(JLabel.TOP);
        previewImage.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        previewImage.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        previewImage.setToolTipText(Messages.TEMPLATES_PREVIEW_LARGER.toString());
        previewImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showLargerPreview();
            }
        });
        previewCaption.setFont(ModernTheme.font(-1, Font.PLAIN));
        previewCaption.setForeground(ModernTheme.muted());

        previewLargerBtn.addActionListener(e -> showLargerPreview());
        previewLargerBtn.setEnabled(false);

        JPanel previewButtons = new JPanel(new WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(4)));
        previewButtons.setOpaque(false);
        previewButtons.add(previewLargerBtn);

        return new Card(Messages.TEMPLATES_PREVIEW.toString())
                .addFull(previewImage)
                .addFull(previewCaption)
                .addFull(previewButtons);
    }

    private File resolvePreviewCandidate() {
        int row = receiptsTable.getSelectedRow();
        if (row >= 0 && row < currentReceipts.size()) {
            Receipt r = currentReceipts.get(row);
            if (r.exists()) {
                return r.file();
            }
        }
        for (Receipt r : currentReceipts) {
            if (r.exists() && isPdfFile(r.file())) {
                return r.file();
            }
        }
        for (Receipt r : currentReceipts) {
            if (r.exists()) {
                return r.file();
            }
        }
        if (lastImportedPreviewFile != null && lastImportedPreviewFile.isFile()) {
            return lastImportedPreviewFile;
        }
        return null;
    }

    private static boolean isPdfFile(File file) {
        return file != null && file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    /**
     * Updates the PDF preview card for the currently selected or attached receipt.
     */
    public void refreshPreview() {
        refreshPreview(resolvePreviewCandidate());
    }

    /**
     * Renders the first page of the given PDF receipt into the preview card
     * (just like the template manager).
     */
    public void refreshPreview(final File file) {
        final int run = ++previewRun;
        previewPdf = null;
        if (file == null || !file.isFile() || !isPdfFile(file)) {
            previewImage.setIcon(null);
            previewImage.setText(" ");
            previewImage.setPreferredSize(null);
            previewCaption.setText(" ");
            previewLargerBtn.setEnabled(false);
            return;
        }
        previewPdf = file;
        previewLargerBtn.setEnabled(true);
        final int width = Math.max(ModernTheme.px(240), Math.min(ModernTheme.px(520),
                previewImage.getParent() == null || previewImage.getParent().getWidth() <= 0
                ? ModernTheme.px(320) : previewImage.getParent().getWidth() - ModernTheme.px(24)));
        previewImage.setIcon(null);
        previewImage.setText(Messages.TEMPLATES_PREVIEW_LOADING.toString());
        previewImage.setPreferredSize(new Dimension(width, (int) (width * 1.414)));
        previewCaption.setText(" ");

        new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(file)) {
                    float pageWidth = d.getPage(0).getMediaBox().getWidth();
                    java.awt.image.BufferedImage img = new org.apache.pdfbox.rendering.PDFRenderer(d)
                            .renderImage(0, width / pageWidth);
                    return new Object[]{file, img, d.getNumberOfPages()};
                }
            }

            @Override
            protected void done() {
                if (run != previewRun) {
                    return;
                }
                try {
                    Object[] r = get();
                    previewPdf = (File) r[0];
                    java.awt.image.BufferedImage img = (java.awt.image.BufferedImage) r[1];
                    previewImage.setText(null);
                    previewImage.setIcon(new javax.swing.ImageIcon(img));
                    previewImage.setPreferredSize(new Dimension(img.getWidth() + 2, img.getHeight() + 2));
                    previewCaption.setText(String.format(Messages.TEMPLATES_PREVIEW_CAPTION.toString(), r[2], file.getName()));
                } catch (Exception e) {
                    Log.Debug(ModernExpensePanel.this, e);
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    previewImage.setIcon(null);
                    previewImage.setText("<html><div style='padding:8px'>" + Messages.TEMPLATES_PREVIEW_FAILED + "<br><font color='#b91c1c'>"
                            + String.valueOf(c.getMessage()).replace("<", "&lt;") + "</font></div></html>");
                    previewImage.setPreferredSize(null);
                }
                previewImage.revalidate();
            }
        }.execute();
    }

    private void showLargerPreview() {
        if (previewPdf != null && previewPdf.exists()) {
            mpv5.ui.dialogs.PdfPreview.show(previewPdf);
        }
    }

    private void setupDragAndDrop() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        new DropTarget(receiptsTable, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    if (dtde.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (File f : files) {
                            importReceiptFile(f);
                        }
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception ex) {
                    Log.Debug(ModernExpensePanel.class, ex);
                    dtde.dropComplete(false);
                }
            }
        });
        new DropTarget(expenseTable, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    if (dtde.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        importZugferdFiles(files);
                        dtde.dropComplete(true);
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception ex) {
                    Log.Debug(ModernExpensePanel.class, ex);
                    dtde.dropComplete(false);
                }
            }
        });
    }

    private void importZugferdDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Messages.ZUGFERD_IMPORT_CHOOSER.toString());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("E-Rechnungen (ZUGFeRD PDF, XRechnung XML)", "pdf", "xml"));
        chooser.setMultiSelectionEnabled(true);
        File configured = ZugpferdExpenseImport.folder();
        if (configured != null && configured.exists()) {
            chooser.setCurrentDirectory(configured.isDirectory() ? configured : configured.getParentFile());
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selected = chooser.getSelectedFiles();
            if (selected != null && selected.length > 0) {
                importZugferdFiles(Arrays.asList(selected));
            } else if (chooser.getSelectedFile() != null) {
                importZugferdFiles(Collections.singletonList(chooser.getSelectedFile()));
            }
        }
    }

    private void importZugferdFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        List<Expense> importedExpenses = new ArrayList<Expense>();
        List<String> alreadyImported = new ArrayList<String>();
        List<String> deniedFiles = new ArrayList<String>();

        for (File f : files) {
            if (f == null || !f.isFile()) {
                continue;
            }
            ZugpferdExpenseImport.ParsedInvoice parsed = ZugpferdExpenseImport.parse(f);
            if (parsed == null) {
                deniedFiles.add(f.getName());
                continue;
            }
            try {
                String sha = ExpenseReceipts.sha256(f);
                if (ExpenseReceipts.existsBySha256(sha)) {
                    alreadyImported.add(f.getName() + " (" + (parsed.invoiceNumber != null && !parsed.invoiceNumber.isEmpty() ? parsed.invoiceNumber : sha.substring(0, 10)) + ")");
                    continue;
                }
                Expense newExp = parsed.toExpense(defaultExpenseAccountId(), 1);
                if (!newExp.save(true) || newExp.__getIDS() <= 0) {
                    throw new IllegalStateException(Messages.RECEIPT_SAVE_FAILED.toString() + " (" + f.getName() + ")");
                }
                ExpenseReceipts.attach(newExp, f);
                importedExpenses.add(newExp);
            } catch (Exception ex) {
                Log.Debug(ModernExpensePanel.class, ex);
                deniedFiles.add(f.getName() + " (" + ex.getMessage() + ")");
            }
        }

        if (!importedExpenses.isEmpty()) {
            refresh();
            setDataOwner(importedExpenses.get(importedExpenses.size() - 1), true);
            Popup.notice(String.format(Messages.RECEIPT_IMPORTED_EXPENSES_SUCCESS.toString(), importedExpenses.size()));
        }

        if (!deniedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    String.format(Messages.RECEIPT_INVALID_INVOICE_BODY.toString(), String.join("\n- ", deniedFiles)),
                    Messages.RECEIPT_INVALID_INVOICE_TITLE.toString(), JOptionPane.WARNING_MESSAGE);
        } else if (!alreadyImported.isEmpty() && importedExpenses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    String.format(Messages.RECEIPT_ALREADY_IMPORTED_NOTICE.toString(), String.join("\n- ", alreadyImported)),
                    Messages.RECEIPT_ALREADY_IMPORTED_NOTICE.toString(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void importReceiptDialog() {
        if (dataOwner == null || dataOwner.__getIDS() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Bitte speichern Sie die Ausgabe zuerst, bevor Sie Belege verkn\u00fcpfen.",
                    "Ausgabe nicht gespeichert", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Beleg verkn\u00fcpfen (PDF, XML, Bild)");
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    importReceiptFile(f);
                }
            } else if (chooser.getSelectedFile() != null) {
                importReceiptFile(chooser.getSelectedFile());
            }
        }
    }

    /**
     * Imports a file into the expense panel: checks whether the file contains
     * ZUGFeRD / XRechnung invoice data and imports/populates the expense data
     * if possible; otherwise attaches it as a receipt to the current expense.
     */
    public void importReceiptFile(File source) {
        if (source == null || !source.isFile()) {
            return;
        }
        if (isPdfFile(source)) {
            lastImportedPreviewFile = source;
        }
        if (dataOwner == null || dataOwner.__getIDS() <= 0) {
            ZugpferdExpenseImport.ParsedInvoice parsed = ZugpferdExpenseImport.parse(source);
            if (parsed != null) {
                importParsedZugferdFile(source, parsed);
                refreshPreview();
                return;
            }
            refreshPreview();
            JOptionPane.showMessageDialog(this,
                    Messages.RECEIPT_NOT_SAVED_BEFORE_ATTACH.toString(),
                    Messages.RECEIPT_NOT_SAVED_TITLE.toString(), JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            boolean isPlainPdf = isPdfFile(source) && ZugpferdExpenseImport.parse(source) == null;
            Receipt r = ExpenseReceipts.attach(dataOwner, source);
            if (r != null) {
                if (isPlainPdf) {
                    String checkNote = String.format(Messages.RECEIPT_PLAIN_PDF_NOTE.toString(), source.getName());
                    String curNotes = notesArea.getText();
                    if (!curNotes.contains("Rechnerische Richtigkeit") && !curNotes.contains("correctness of receipt")) {
                        String updated = (curNotes == null || curNotes.trim().isEmpty()) ? checkNote : curNotes.trim() + "\n" + checkNote;
                        notesArea.setText(updated);
                        dataOwner.setDescription(updated);
                        try {
                            dataOwner.save(true);
                        } catch (Exception ignored) {
                        }
                    }
                    loadReceipts();
                    notifyViewOpened();
                    refreshPreview(source);
                    Popup.notice(String.format(Messages.RECEIPT_PLAIN_PDF_ARCHIVED.toString(), r.originalName));
                } else {
                    loadReceipts();
                    notifyViewOpened();
                    refreshPreview(source);
                    Popup.notice(String.format(Messages.RECEIPT_ARCHIVED.toString(), r.originalName) + "\nSHA-256: " + r.sha256.substring(0, 16) + "...");
                }
            }
        } catch (Exception ex) {
            Log.Debug(ModernExpensePanel.class, ex);
            JOptionPane.showMessageDialog(this,
                    "Fehler beim Verkn\u00fcpfen des Belegs:\n" + ex.getMessage(),
                    "Importfehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importParsedZugferdFile(File source, ZugpferdExpenseImport.ParsedInvoice parsed) {
        if (dataOwner == null) {
            dataOwner = new Expense();
        }
        cnumber_ = parsed.invoiceNumber != null && !parsed.invoiceNumber.trim().isEmpty()
                ? parsed.invoiceNumber.trim()
                : source.getName();
        cname_ = cnumber_;
        description_ = parsed.buildDescription();
        brutvalue_ = parsed.grossAmount != null ? parsed.grossAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        taxpercentvalue_ = parsed.taxRate != null ? parsed.taxRate.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        currentTaxRate = taxpercentvalue_;
        if (parsed.netAmount != null && parsed.netAmount.compareTo(BigDecimal.ZERO) > 0) {
            netvalue_ = parsed.netAmount.setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal divisor = BigDecimal.ONE.add(taxpercentvalue_.divide(Constants.BD100, 9, RoundingMode.HALF_UP));
            netvalue_ = brutvalue_.divide(divisor, 2, RoundingMode.HALF_UP);
        }
        dateadded_ = parsed.issueDate != null ? parsed.issueDate : new Date();
        dateend_ = parsed.dueDate != null ? parsed.dueDate : dateadded_;
        ispaid_ = false;

        dataOwner.setCnumber(cnumber_);
        dataOwner.setCname(cname_);
        dataOwner.setDescription(description_);
        dataOwner.setBrutvalue(brutvalue_);
        dataOwner.setNetvalue(netvalue_);
        dataOwner.setTaxpercentvalue(taxpercentvalue_);
        dataOwner.setDateadded(dateadded_);
        dataOwner.setDateend(dateend_);
        dataOwner.setIspaid(ispaid_);

        exposeData();

        if (hasDatabaseConnection()) {
            try {
                if (dataOwner.__getAccountsids() <= 0) {
                    dataOwner.setAccountsids(accountsids_ > 0 ? accountsids_ : 1);
                }
                if (dataOwner.__getGroupsids() <= 0) {
                    dataOwner.setGroupsids(1);
                }
                if (dataOwner.save(true) && dataOwner.__getIDS() > 0) {
                    ids_ = dataOwner.__getIDS();
                    ExpenseReceipts.attach(dataOwner, source);
                    refresh();
                    setDataOwner(dataOwner, true);
                    Popup.notice(String.format(Messages.RECEIPT_IMPORTED_SINGLE_EXPENSE.toString(), source.getName()));
                } else {
                    Popup.error(this, Messages.RECEIPT_SAVE_FAILED.toString());
                }
            } catch (Exception ex) {
                Log.Debug(ModernExpensePanel.class, ex);
            }
        }
    }

    private int defaultExpenseAccountId() {
        if (accountObjects != null && !accountObjects.isEmpty()) {
            return accountObjects.get(0).__getIDS();
        }
        if (accountsids_ > 0) {
            return accountsids_;
        }
        try {
            ArrayList<DatabaseObject> exp = DatabaseObject.getObjects(Context.getAccounts(), new QueryCriteria("intaccounttype", Account.EXPENSE));
            if (exp != null && !exp.isEmpty()) {
                accountObjects = exp;
                return exp.get(0).__getIDS();
            }
            ArrayList<DatabaseObject> all = DatabaseObject.getObjects(Context.getAccounts());
            if (all != null && !all.isEmpty()) {
                return all.get(0).__getIDS();
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    private static boolean hasDatabaseConnection() {
        try {
            return DatabaseConnection.getConnector() != null && DatabaseConnection.getConnector().getConnection() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void openSelectedReceipt() {
        int row = receiptsTable.getSelectedRow();
        if (row < 0 || row >= currentReceipts.size()) {
            return;
        }
        Receipt r = currentReceipts.get(row);
        if (r.exists()) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(r.file());
                } catch (Exception ex) {
                    Popup.notice("Belegdatei:\n" + r.archivePath);
                }
            } else {
                Popup.notice("Belegdatei:\n" + r.archivePath);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Belegdatei im Archiv nicht gefunden:\n" + r.archivePath,
                    "Datei fehlt", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedReceipt() {
        if (isContentLocked()) {
            JOptionPane.showMessageDialog(this,
                    "Ausgabe ist gesperrt; Belege k\u00f6nnen nicht entfernt werden.",
                    "Gesperrt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = receiptsTable.getSelectedRow();
        if (row < 0 || row >= currentReceipts.size()) {
            return;
        }
        Receipt r = currentReceipts.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Soll die Verkn\u00fcpfung zum Beleg '" + r.originalName + "' gel\u00f6scht werden?\n(Die physische Datei verbleibt zur Revisionssicherheit im Archiv.)",
                "Belegverkn\u00fcpfung l\u00f6schen", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ExpenseReceipts.detach(dataOwner, r.id);
            loadReceipts();
            notifyViewOpened();
        }
    }

    private void verifyAllReceipts() {
        if (currentReceipts.isEmpty()) {
            Popup.notice("Keine Belege vorhanden.");
            return;
        }
        int ok = 0;
        List<String> errors = new ArrayList<String>();
        for (Receipt r : currentReceipts) {
            if (!r.exists()) {
                errors.add(r.originalName + ": Datei fehlt im Archiv (" + r.archivePath + ")");
            } else if (!ExpenseReceipts.verify(r)) {
                errors.add(r.originalName + ": SHA-256 Checksumme stimmt NICHT \u00fcberein!");
            } else {
                ok++;
            }
        }
        if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Integrit\u00e4t OK: Alle " + ok + " Belege vorhanden und kryptographisch unversehrt.",
                    "Belegpr\u00fcfung", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Integrit\u00e4tsfehler bei Belegen:\n- " + String.join("\n- ", errors),
                    "Belegpr\u00fcfung fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReceipts() {
        receiptsModel.setRowCount(0);
        currentReceipts.clear();
        if (dataOwner != null && dataOwner.__getIDS() > 0 && hasDatabaseConnection()) {
            List<Receipt> list = ExpenseReceipts.of(dataOwner.__getIDS());
            currentReceipts.addAll(list);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Receipt r : list) {
                receiptsModel.addRow(new Object[]{
                    r.id,
                    r.originalName,
                    r.formattedSize(),
                    r.sha256,
                    df.format(r.importedAt),
                    r.importedBy
                });
            }
            if (!currentReceipts.isEmpty() && receiptsTable.getSelectedRow() < 0) {
                receiptsTable.setRowSelectionInterval(0, 0);
            }
        }
        refreshPreview();
    }

    private void applyContentLock(boolean locked) {
        cnumberField.getTextField().setEditable(!locked);
        brutValueField.getTextField().setEditable(!locked);
        taxRateSelect.setEnabled(!locked);
        accountSelect.setEnabled(!locked);
        groupSelect.setEnabled(!locked);
        issueDateChooser.setEnabled(!locked);
        notesArea.setEditable(!locked);
        removeReceiptBtn.setEnabled(!locked);
        // Payment status & date remain editable for open-item workflow
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        lastImportedPreviewFile = null;
        dataOwner = (Expense) object;
        if (populate && dataOwner != null) {
            dataOwner.setPanelData(this);
            exposeData();

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }
            loadReceipts();
            notifyViewOpened();
            validate();
        }
    }

    @Override
    public void exposeData() {
        loadedCnumber = cnumber_ != null ? cnumber_ : "";
        idField.setText(cname_ != null ? cname_ : (ids_ > 0 ? String.valueOf(ids_) : ""));
        cnumberField.setText(cnumber_ != null ? cnumber_ : "");
        notesArea.setText(description_ != null ? description_ : "");
        brutValueField.setText(brutvalue_ != null ? FormatNumber.formatDezimal(brutvalue_) : "0.00");
        netValueField.setText(netvalue_ != null ? FormatNumber.formatLokalCurrency(netvalue_) : "0.00");

        try {
            if (accountsids_ > 0) {
                accountSelect.setSelectedItem(accountsids_);
            }
        } catch (Exception ignored) {
        }

        try {
            if (group_ != null) {
                groupSelect.setModel(group_);
            }
        } catch (Exception ignored) {
        }

        String username = hasDatabaseConnection() ? User.getUsername(intaddedby_) : "";
        addedByLabel.setText(username);
        if (dateadded_ != null) {
            issueDateChooser.setDate(dateadded_);
        }
        if (dateend_ != null) {
            payDateChooser.setDate(dateend_);
        }
        paidCheckBox.setSelected(ispaid_);

        try {
            if (hasDatabaseConnection()) {
                taxRateSelect.setSelectedItem(Tax.getTaxId(taxpercentvalue_));
            }
        } catch (Exception ignored) {
        }
        if (hasDatabaseConnection()) {
            updateTaxRate();
        }

        String title = cnumber_ != null && !cnumber_.isEmpty() ? "Ausgabe " + cnumber_ : (ids_ > 0 ? "Ausgabe #" + ids_ : "Ausgaben");
        setTabTitle(title);
    }

    @Override
    public synchronized boolean collectData() {
        calculate();

        try {
            accountsids_ = Integer.parseInt(accountSelect.getSelectedItem().getId());
        } catch (Exception e) {
            accountsids_ = 1;
        }

        if (groupSelect.getSelectedItem() != null) {
            try {
                int g = Integer.parseInt(groupSelect.getSelectedItem().getId());
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(), g);
            } catch (Exception ignored) {
            }
        }

        dateadded_ = issueDateChooser.getDate();
        dateend_ = payDateChooser.getDate();
        ispaid_ = paidCheckBox.isSelected();

        intaddedby_ = User.getUserId(addedByLabel.getText());
        if (intaddedby_ <= 0) {
            intaddedby_ = User.getCurrentUser().__getIDS();
        }

        description_ = notesArea.getText();
        cnumber_ = cnumberField.getText().trim();
        cname_ = cnumber_.isEmpty() ? (ids_ > 0 ? String.valueOf(ids_) : "") : cnumber_;

        try {
            netvalue_ = FormatNumber.parseDezimal(netValueField.getText());
        } catch (Exception e) {
            netvalue_ = BigDecimal.ZERO;
        }

        try {
            brutvalue_ = FormatNumber.parseDezimal(brutValueField.getText());
        } catch (Exception e) {
            brutvalue_ = BigDecimal.ZERO;
        }

        try {
            taxpercentvalue_ = Tax.getTaxValue(Integer.parseInt(taxRateSelect.getSelectedItem().getId()));
        } catch (Exception e) {
            taxpercentvalue_ = BigDecimal.ZERO;
        }

        return true;
    }

    @Override
    public void refresh() {
        if (!hasDatabaseConnection()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            groupSelect.triggerSearch();
            taxRateSelect.triggerSearch();
            try {
                MPTableModel x = new MPTableModel(Expense.getExpenses(), Headers.EXPENSE);
                x.setTypes(Expense.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
                expenseTable.setModel(x);
                applyExpenseFilter();
            } catch (NodataFoundException e) {
                Log.Debug(ModernExpensePanel.class, e.getMessage());
            }
            try {
                accountObjects = DatabaseObject.getObjects(Context.getAccounts(), new QueryCriteria("intaccounttype", Account.EXPENSE));
                accountSelect.setModel(accountObjects);
            } catch (NodataFoundException e) {
                Log.Debug(ModernExpensePanel.class, e.getMessage());
            }
            updateTaxRate();
        });
    }

    @Override
    public void paste(DatabaseObject... dbo) {
        mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(brutValueField);
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public void actionAfterSave() {
        refresh();
        loadReceipts();
    }

    @Override
    public void actionAfterCreate() {
        refresh();
        loadReceipts();
    }

    @Override
    public void actionBeforeCreate() {
        ids_ = -1;
        String entered = cnumberField.getText().trim();
        if (entered.isEmpty() || entered.equals(loadedCnumber)) {
            cnumber_ = "";
            cname_ = "";
            if (dataOwner != null) {
                dataOwner.setCnumber("");
                dataOwner.setCname("");
            }
        }
    }

    @Override
    public void actionBeforeSave() {
        if (ids_ <= 0 && cnumberField.getText().trim().isEmpty() && dataOwner != null) {
            dataOwner.setCnumber("");
            dataOwner.setCname("");
        }
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
    }
}
