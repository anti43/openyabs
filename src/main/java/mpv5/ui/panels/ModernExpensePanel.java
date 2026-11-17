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
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;
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
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.ui.TextFieldUtils;

/**
 * Modern view for expenses ({@link Expense}) based on {@link ModernDataPanel}.
 * <p>
 * Key features:
 * <ul>
 *   <li>Card-based modern layout with responsive cards (details, amounts, bookkeeping).</li>
 *   <li>Integrated receipts section: list of imported receipt files with SHA-256 checksums,
 *       file sizes and timestamps, directly openable with system viewer.</li>
 *   <li>Drag & drop receipt import directly onto the receipts card or receipt table.</li>
 *   <li>Support for plugin banner, badge and content-lock hooks (e.g. GoBD compliance).</li>
 *   <li>Split view with expense master list on the left and detail on the right.</li>
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

    private Expense dataOwner;
    private final ModernCommandBar tb;

    // Header components
    private final JLabel titleLabel = new JLabel();
    private final JLabel statusPill = new JLabel();
    private final JLabel metaLabel = new JLabel();

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

    // Master list
    private final JTable expenseTable = new MPTable(this);

    // Receipts table
    private final DefaultTableModel receiptsModel = new DefaultTableModel(
            new Object[]{"ID", "Dateiname", "Groesse", "SHA-256", "Importiert am", "Benutzer"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable receiptsTable = new JTable(receiptsModel);
    private final List<Receipt> currentReceipts = new ArrayList<Receipt>();
    private final JButton addReceiptBtn = new JButton("Beleg hinzufuegen...");
    private final JButton removeReceiptBtn = new JButton("Entfernen");
    private final JButton openReceiptBtn = new JButton("Oeffnen");
    private final JButton verifyReceiptsBtn = new JButton("Pruefen");

    private BigDecimal currentTaxRate = BigDecimal.ZERO;
    private ArrayList<DatabaseObject> accountObjects;

    public ModernExpensePanel() {
        super(new BorderLayout());
        setName("modernexpensepanel");
        dataOwner = new Expense();

        initFields();

        // Main layout
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(createHeader(), BorderLayout.NORTH);

        // Body split: list on left, detail on right
        JPanel listPanel = createListPanel();
        JPanel detailPanel = createDetailPanel();
        JSplitPane split = split("expense", listPanel, detailPanel, 0.32);
        center.add(split, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        tb = new ModernCommandBar(this);
        tb.setExportButtonsEnabled(false);
        add(tb, BorderLayout.NORTH);

        // Listen for lock changes to update editability
        addPropertyChangeListener(PROPERTY_CONTENT_LOCKED, evt -> {
            boolean locked = Boolean.TRUE.equals(evt.getNewValue());
            applyContentLock(locked);
        });

        setupDragAndDrop();
        refresh();
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
        paidCheckBox.addActionListener(e -> updateStatusPill());

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

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(ModernTheme.px(14), 0));
        header.setOpaque(false);
        header.setBorder(ModernTheme.padding(8, 16));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        titleLabel.setFont(ModernTheme.font(13, Font.BOLD));
        titleLabel.setForeground(ModernTheme.foreground());
        titleLabel.setText("Neue Ausgabe");

        metaLabel.setFont(ModernTheme.font(2, Font.PLAIN));
        metaLabel.setForeground(ModernTheme.muted());

        statusPill.setOpaque(true);
        statusPill.setFont(ModernTheme.font(0, Font.BOLD));
        statusPill.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(2), ModernTheme.px(8), ModernTheme.px(2), ModernTheme.px(8)));
        updateStatusPill();

        JPanel second = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        second.setOpaque(false);
        second.add(statusPill);
        second.add(createPluginBadgeSlot());
        second.add(metaLabel);

        center.add(titleLabel);
        center.add(Box.createVerticalStrut(ModernTheme.px(4)));
        center.add(second);

        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private void updateStatusPill() {
        boolean paid = paidCheckBox.isSelected();
        if (paid) {
            statusPill.setText("BEZAHLT");
            statusPill.setBackground(new Color(220, 252, 231));
            statusPill.setForeground(new Color(22, 101, 52));
        } else {
            statusPill.setText("OFFEN");
            statusPill.setBackground(new Color(254, 243, 199));
            statusPill.setForeground(new Color(146, 64, 14));
        }
    }

    private JPanel createListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(ModernTheme.padding(6, 6));

        ModernTheme.style(expenseTable);
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

        Card card = new Card("Ausgaben");
        card.addFull(new JScrollPane(expenseTable), 1);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private JPanel createDetailPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        ResponsiveGrid grid = new ResponsiveGrid(340, 2).arrangeable("expense");

        // Card 1: Basisdaten & Betraege
        Card c1 = new Card("Ausgabendaten")
                .row(Card.field("ID", idField, 1), Card.field("Belegnummer", cnumberField, 2))
                .row(Card.field("Brutto", brutValueField), Card.field("Netto", netValueField))
                .row(Card.field("Steuersatz", taxRateSelect), Card.field("Konto", accountSelect))
                .row(Card.field("Belegdatum", issueDateChooser), Card.field("Zahlungsdatum", payDateChooser))
                .row(Card.field(null, paidCheckBox));
        grid.add(c1);

        // Card 2: Organisation & Notizen
        Card c2 = new Card("Organisation & Notizen")
                .row(Card.field("Gruppe", groupSelect), Card.field("Erfasst von", addedByLabel))
                .row(Card.field("Beschreibung / Verwendungszweck", new JScrollPane(notesArea)));
        grid.add(c2);

        // Card 3: Belege & Scans
        Card c3 = createReceiptsCard();
        grid.add(c3);

        p.add(scroll(grid), BorderLayout.CENTER);
        return p;
    }

    private Card createReceiptsCard() {
        Card card = new Card("Eingangsbelege & Scans (GoBD Rz. 61–64: Keine Buchung ohne Beleg)");

        ModernTheme.style(receiptsTable);
        receiptsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        receiptsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        receiptsTable.getColumnModel().getColumn(2).setMaxWidth(80);
        receiptsTable.getColumnModel().getColumn(3).setPreferredWidth(180);

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

        JLabel hint = new JLabel("<html><small>Dateien koennen per Drag &amp; Drop direkt in diese Tabelle gezogen werden. "
                + "Die Belege werden unveraenderbar im Jahresarchiv abgelegt und per SHA256SUMS gesichert.</small></html>");
        hint.setForeground(ModernTheme.muted());
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(4), 0));

        card.addFull(hint);
        card.addFull(new JScrollPane(receiptsTable), 1);
        return card;
    }

    private void setupDragAndDrop() {
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
    }

    private void importReceiptDialog() {
        if (dataOwner == null || dataOwner.__getIDS() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Bitte speichern Sie die Ausgabe zuerst, bevor Sie Belege anhaengen.",
                    "Ausgabe nicht gespeichert", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Eingangsbeleg / Scan auswaehlen (PDF, XML, Bild)");
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File f : files) {
                importReceiptFile(f);
            }
        }
    }

    private void importReceiptFile(File source) {
        if (dataOwner == null || dataOwner.__getIDS() <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Bitte speichern Sie die Ausgabe zuerst, bevor Sie Belege anhaengen.",
                    "Ausgabe nicht gespeichert", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (source == null || !source.isFile()) {
            return;
        }
        try {
            Receipt r = ExpenseReceipts.attach(dataOwner, source);
            if (r != null) {
                loadReceipts();
                Popup.notice("Beleg archiviert: " + r.originalName + "\nSHA-256: " + r.sha256.substring(0, 16) + "...");
            }
        } catch (Exception ex) {
            Log.Debug(ModernExpensePanel.class, ex);
            JOptionPane.showMessageDialog(this,
                    "Fehler beim Importieren des Belegs:\n" + ex.getMessage(),
                    "Importfehler", JOptionPane.ERROR_MESSAGE);
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
                    "Ausgabe ist gesperrt; Belege koennen nicht entfernt werden.",
                    "Gesperrt", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = receiptsTable.getSelectedRow();
        if (row < 0 || row >= currentReceipts.size()) {
            return;
        }
        Receipt r = currentReceipts.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Soll die Verknuepfung zum Beleg '" + r.originalName + "' geloescht werden?\n(Die physische Datei verbleibt zur Revisionssicherheit im Archiv.)",
                "Belegverknuepfung loeschen", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ExpenseReceipts.detach(dataOwner, r.id);
            loadReceipts();
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
                errors.add(r.originalName + ": SHA-256 Checksumme stimmt NICHT ueberein!");
            } else {
                ok++;
            }
        }
        if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Integritaet OK: Alle " + ok + " Belege vorhanden und kryptographisch unversehrt.",
                    "GoBD-Belegpruefung", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Integritaetsfehler bei Belegen:\n- " + String.join("\n- ", errors),
                    "GoBD-Pruefung FEHLGESCHLAGEN", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReceipts() {
        receiptsModel.setRowCount(0);
        currentReceipts.clear();
        if (dataOwner != null && dataOwner.__getIDS() > 0) {
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
        }
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

        addedByLabel.setText(User.getUsername(intaddedby_));
        if (dateadded_ != null) {
            issueDateChooser.setDate(dateadded_);
        }
        if (dateend_ != null) {
            payDateChooser.setDate(dateend_);
        }
        paidCheckBox.setSelected(ispaid_);

        try {
            taxRateSelect.setSelectedItem(Tax.getTaxId(taxpercentvalue_));
        } catch (Exception ignored) {
        }
        updateTaxRate();
        updateStatusPill();

        String title = cnumber_ != null && !cnumber_.isEmpty() ? "Ausgabe " + cnumber_ : (ids_ > 0 ? "Ausgabe #" + ids_ : "Neue Ausgabe");
        titleLabel.setText(title);
        metaLabel.setText("ID: " + (ids_ > 0 ? ids_ : "-") + " | Erfasst am: "
                + (dateadded_ != null ? DateConverter.getSQLDateString(dateadded_) : "-")
                + " | Benutzer: " + User.getUsername(intaddedby_));
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
        cname_ = cnumber_.isEmpty() ? (ids_ > 0 ? String.valueOf(ids_) : "Ausgabe") : cnumber_;

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
        SwingUtilities.invokeLater(() -> {
            groupSelect.triggerSearch();
            taxRateSelect.triggerSearch();
            try {
                MPTableModel x = new MPTableModel(Expense.getExpenses(), Headers.EXPENSE);
                x.setTypes(Expense.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
                expenseTable.setModel(x);
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
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
    }
}
