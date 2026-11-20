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
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Scrollable;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.common.Templateable;
import mpv5.db.objects.Account;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import mpv5.handler.JournalLoader;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.JournalExport;
import mpv5.plugin.api.JournalExportContext;
import mpv5.ui.beans.TimeframeChooser;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.popups.TablePopUp;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.export.EmailBatch;
import mpv5.utils.export.Export;
import mpv5.utils.export.JournalAdviserPackage;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.jobs.Job;
import mpv5.utils.jobs.Waiter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.models.MPTreeModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.ExcelAdapter;
import mpv5.utils.text.TypeConversion;

/**
 * The journal: invoices, credit notes, income, expenses and payments of a
 * period, for the EÜR, an overview and the export to the tax adviser.
 * <p>
 * Outgoing documents (income side) and expenses are different records and
 * have their own filters (which documents, status, date: document or payment
 * date). Groups and accounts are filters of both and look the same: a
 * searchable list each, none selected means all. The view presets set the
 * filters for the usual tasks (EÜR: paid, by payment date; all documents;
 * open items). Every change reloads the journal.
 * <p>
 * The journal of a contact (history) shows the outgoing documents of the
 * contact and, as second tab, the overview tree of the contact.
 */
public class JournalPanel extends JPanel implements ListPanel, Scrollable {

    private static final long serialVersionUID = 2L;
    private final java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    static final int VIEW_EUR = 0;
    static final int VIEW_ALL = 1;
    static final int VIEW_OPEN = 2;
    static final int VIEW_CUSTOM = 3;
    static final int STATUS_ALL = 0;
    static final int STATUS_PAID = 1;
    static final int STATUS_OPEN = 2;
    static final int STATUS_CANCELLED = 3;
    /**
     * the table model columns (the journal template refers to them)
     */
    private static final int COL_EFFECT = 15;
    private static final int MIN_WIDTH = 980;
    private static final int MIN_HEIGHT = 620;

    public static JComponent instanceOf() {
        return new JournalPanel();
    }
    private final Contact dataOwner;
    private Template journalOrContactTemplate;

    //period and view
    private final TimeframeChooser timeframeChooser1 = new TimeframeChooser();
    private final ButtonGroup periodGroup = new ButtonGroup();
    private final List<ChipToggle> periodChips = new ArrayList<ChipToggle>();
    private final JComboBox<String> view = new JComboBox<String>();
    //income side
    private final JCheckBox incInvoices = new JCheckBox(Item.getTypeString(Constants.TYPE_INVOICE));
    private final JCheckBox incCredits = new JCheckBox(Item.getTypeString(Constants.TYPE_CREDIT));
    private final JCheckBox incDeposits = new JCheckBox(Item.getTypeString(Constants.TYPE_DEPOSIT));
    private final JCheckBox incPartPayments = new JCheckBox(Item.getTypeString(Constants.TYPE_PART_PAYMENT));
    private final JCheckBox incRevenues = new JCheckBox(Messages.TYPE_REVENUE.toString());
    //offers and orders: only when ticked, never saved (start unticked)
    private final JCheckBox incOrders = new JCheckBox(Messages.JOURNAL_SHOW_ORDERS.toString());
    private final JCheckBox incOffers = new JCheckBox(Messages.JOURNAL_SHOW_OFFERS.toString());
    private final JCheckBox othersOpenOnly = new JCheckBox(Messages.JOURNAL_OPEN_ONLY.toString(), true);
    private final JComboBox<Object> incomeStatus = new JComboBox<Object>(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID, Messages.STATUS_CANCELLED});
    private final JRadioButton incomeByDoc = new JRadioButton(Messages.JOURNAL_BY_DOCDATE.toString());
    private final JRadioButton incomeByPay = new JRadioButton(Messages.JOURNAL_BY_PAYDATE.toString());
    //expense side
    private final JCheckBox incExpenses = new JCheckBox(Messages.JOURNAL_SHOW_EXPENSES.toString());
    private final JComboBox<Object> expenseStatus = new JComboBox<Object>(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID});
    private final JRadioButton expenseByDoc = new JRadioButton(Messages.JOURNAL_BY_DOCDATE.toString());
    private final JRadioButton expenseByPay = new JRadioButton(Messages.JOURNAL_BY_PAYDATE.toString());
    //groups and accounts: the same control
    private final SelectionList groups = new SelectionList(Messages.JOURNAL_GROUPS_HINT.toString());
    private final SelectionList accounts = new SelectionList(Messages.JOURNAL_ACCOUNTS_HINT.toString());
    private final JCheckBox includechildgroups = new JCheckBox(Messages.JOURNAL_CHILD_GROUPS.toString(), true);
    private final JCheckBox includechildaccounts = new JCheckBox(Messages.JOURNAL_CHILD_ACCOUNTS.toString(), true);
    //table
    private final JTable jTable1;
    private final JScrollPane tableScroll;
    private final JTextField find = new JTextField(18);
    private final JLabel count = new JLabel(" ");
    private final JButton loadButton = new mpv5.ui.modern.PrimaryButton(Messages.JOURNAL_RELOAD.toString(), null);
    private final JButton exportButton = new JButton(Messages.JOURNAL_EXPORT_BUTTON.toString());
    //sums
    private final JLabel revenueNetto = sum(Messages.JOURNAL_SUM_INCOME_NET);
    private final JLabel revenueTax = sum(Messages.JOURNAL_SUM_INCOME_TAX);
    private final JLabel revenueBrut = sum(Messages.JOURNAL_SUM_INCOME_GROSS);
    private final JLabel expNetto = sum(Messages.JOURNAL_SUM_EXPENSE_NET);
    private final JLabel expTax = sum(Messages.JOURNAL_SUM_EXPENSE_TAX);
    private final JLabel expBrut = sum(Messages.JOURNAL_SUM_EXPENSE_GROSS);
    private final JLabel profitNetto = sum(Messages.JOURNAL_SUM_PROFIT_NET);
    private final JLabel profitTax = sum(Messages.JOURNAL_SUM_PROFIT_TAX);
    private final JLabel profitBrut = sum(Messages.JOURNAL_SUM_PROFIT_GROSS);
    private final JLabel openReceivables = sum(Messages.JOURNAL_SUM_OPEN_RECEIVABLES);
    private final JLabel openPayables = sum(Messages.JOURNAL_SUM_OPEN_PAYABLES);
    private final JLabel volumeDiscount = sum(Messages.JOURNAL_SUM_DISCOUNT);
    private final JLabel volumeNetto = sum(Messages.JOURNAL_SUM_VOLUME_NET);
    private final JLabel taxVolume = sum(Messages.JOURNAL_SUM_VOLUME_TAX);
    private final JLabel volumeBrutto = sum(Messages.JOURNAL_SUM_VOLUME_GROSS);
    private final JLabel openOffers = sum(Messages.JOURNAL_SUM_OPEN_OFFERS);
    private final JLabel openOrders = sum(Messages.JOURNAL_SUM_OPEN_ORDERS);
    private final JLabel openOthersGross = sum(Messages.JOURNAL_SUM_OPEN_OTHERS_GROSS);
    /**
     * the components of the column of offers and orders: shown when they are
     * ticked
     */
    private final List<Component> othersColumn = new ArrayList<Component>();
    private final JLabel taxRates = new JLabel(" ");
    private final JLabel exportResult = new JLabel(" ");
    private final mpv5.ui.modern.PrimaryButton filtersToggle = new mpv5.ui.modern.PrimaryButton(" ", null);
    private File exportedFile;
    //contact overview
    private final JTree jTree1 = new JTree();
    private final JRadioButton filternone = new JRadioButton();
    private final JRadioButton filterinvoice = new JRadioButton();
    private final JRadioButton filterorders = new JRadioButton();
    private final JRadioButton filteroffers = new JRadioButton();

    //state
    private List<Entry> entries = new ArrayList<Entry>();
    private Map<BigDecimal, String> rateLabels = new HashMap<BigDecimal, String>();
    private Totals totals = new Totals();
    private boolean adjusting;
    private int generation;
    private final Timer reloadTimer = new Timer(350, e -> setData());

    /**
     * The general journal
     */
    public JournalPanel() {
        this(null);
    }

    /**
     * @param dataOwner the contact of the journal (history), null for the
     * general journal
     */
    public JournalPanel(Contact dataOwner) {
        this.dataOwner = dataOwner;
        setName("journalpanel"); //NOI18N
        reloadTimer.setRepeats(false);
        jTable1 = new MPTable(this) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    ((JComponent) c).setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                if (!isRowSelected(rowIndex)) {
                    int m = convertRowIndexToModel(rowIndex);
                    Entry e = m >= 0 && m < entries.size() ? entries.get(m) : null;
                    Color fg = getForeground();
                    if (e != null && !e.counted) {
                        fg = ModernTheme.muted();
                    } else if (e != null && convertColumnIndexToModel(vColIndex) == COL_EFFECT) {
                        fg = e.effect.signum() < 0 ? Applets.DANGER : new Color(21, 128, 61);
                    }
                    c.setForeground(fg);
                }
                return c;
            }
        };
        jTable1.setName("jTable1"); //NOI18N
        jTable1.setAutoCreateRowSorter(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() > 1 && jTable1.getSelectedRow() >= 0) {
                    int index = jTable1.convertRowIndexToModel(jTable1.getSelectedRow());
                    Object obj = jTable1.getModel().getValueAt(index, 0);
                    if (obj instanceof DatabaseObject) {
                        YabsViewProxy.instance().addTab((DatabaseObject) obj);
                    }
                }
            }
        });
        tableScroll = new JScrollPane(jTable1);
        new ExcelAdapter(jTable1);
        setPopup();

        initFilters();
        build();
        loadTemplate();
        ((MPTable) jTable1).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) jTable1, this));
        if (dataOwner != null) {
            jTree1.setCellRenderer(MPTreeModel.getRenderer());
            jTree1.setModel(new MPTreeModel(dataOwner, null, null));
            jTree1.addMouseListener(MPTreeModel.getDefaultTreeListener(jTree1, dataOwner));
        }
        setData();
    }

    /**
     * A sum: the value (the label text is its name, shown next to it)
     */
    private static JLabel sum(Messages label) {
        JLabel f = new JLabel("0");
        f.setName(label.name());
        f.putClientProperty("label", label.toString()); //NOI18N
        f.setHorizontalAlignment(JLabel.RIGHT);
        f.setFont(ModernTheme.font(0, Font.BOLD));
        return f;
    }

    /**
     * The sums in a compact grid: one column each for income, expenses,
     * result, open items and volume, three rows (label left, value right)
     */
    private JPanel sumsGrid() {
        JLabel[][] cols = {
            {revenueNetto, revenueTax, revenueBrut},
            {expNetto, expTax, expBrut},
            {profitNetto, profitTax, profitBrut},
            {openReceivables, openPayables, volumeDiscount},
            {volumeNetto, taxVolume, volumeBrutto},
            {openOffers, openOrders, openOthersGross}};
        JPanel g = new JPanel(new GridBagLayout());
        g.setOpaque(false);
        int gap = ModernTheme.px(18);
        for (int c = 0; c < cols.length; c++) {
            for (int r = 0; r < cols[c].length; r++) {
                JLabel value = cols[c][r];
                JLabel name = new JLabel(String.valueOf(value.getClientProperty("label")));
                name.setForeground(ModernTheme.muted());
                name.setFont(ModernTheme.font(-1, Font.PLAIN));
                GridBagConstraints gc = new GridBagConstraints();
                gc.gridy = r;
                gc.gridx = c * 2;
                gc.anchor = GridBagConstraints.WEST;
                gc.insets = new Insets(0, c == 0 ? 0 : gap, 0, ModernTheme.px(8));
                g.add(name, gc);
                gc.gridx = c * 2 + 1;
                gc.anchor = GridBagConstraints.EAST;
                gc.weightx = 1;
                gc.fill = GridBagConstraints.HORIZONTAL;
                gc.insets = new Insets(0, 0, 0, 0);
                g.add(value, gc);
                if (c == cols.length - 1) {
                    othersColumn.add(name);
                    othersColumn.add(value);
                }
            }
            //a thin line between the columns
            if (c > 0) {
                javax.swing.JSeparator s = new javax.swing.JSeparator(javax.swing.SwingConstants.VERTICAL);
                s.setForeground(ModernTheme.border());
                GridBagConstraints gc = new GridBagConstraints();
                gc.gridx = c * 2;
                gc.gridy = 0;
                gc.gridheight = 3;
                gc.fill = GridBagConstraints.VERTICAL;
                gc.anchor = GridBagConstraints.WEST;
                gc.insets = new Insets(0, gap / 2, 0, 0);
                g.add(s, gc);
                if (c == cols.length - 1) {
                    othersColumn.add(s);
                }
            }
        }
        for (Component x : othersColumn) {
            x.setVisible(false);
        }
        return g;
    }

    // <editor-fold defaultstate="collapsed" desc="Filters">
    private void initFilters() {
        mpv5.data.PropertyStore p = User.getCurrentUser().getProperties();
        //period
        hideArrow(timeframeChooser1);
        String[] presets = {Messages.JOURNAL_MONTH.toString(), Messages.JOURNAL_LAST_MONTH.toString(), Messages.JOURNAL_QUARTER.toString(),
            Messages.JOURNAL_LAST_QUARTER.toString(), Messages.JOURNAL_YEAR.toString(), Messages.JOURNAL_LAST_YEAR.toString()};
        for (int i = 0; i < presets.length; i++) {
            final int preset = i;
            ChipToggle chip = new ChipToggle(presets[i]);
            chip.addActionListener(e -> {
                timeframeChooser1.setTime(period(preset, LocalDate.now()));
                scheduleReload();
            });
            periodGroup.add(chip);
            periodChips.add(chip);
        }
        if (dataOwner == null) {
            timeframeChooser1.setTime(period(4, LocalDate.now()));
        } else {
            //a contact: the last ten years
            LocalDate now = LocalDate.now();
            timeframeChooser1.setTime(new vTimeframe(date(now.minusYears(10).withDayOfYear(1)), date(now.withMonth(12).withDayOfMonth(31))));
        }

        //view presets
        view.setModel(new DefaultComboBoxModel<String>(new String[]{Messages.JOURNAL_VIEW_EUR.toString(),
            Messages.JOURNAL_VIEW_ALL.toString(), Messages.JOURNAL_VIEW_OPEN.toString(), Messages.JOURNAL_VIEW_CUSTOM.toString()}));
        view.setName("view"); //NOI18N

        //date bases
        ButtonGroup ig = new ButtonGroup();
        ig.add(incomeByDoc);
        ig.add(incomeByPay);
        ButtonGroup eg = new ButtonGroup();
        eg.add(expenseByDoc);
        eg.add(expenseByPay);

        //groups and accounts
        List<Object> gl = new ArrayList<Object>();
        try {
            gl.addAll(DatabaseObject.getObjects(Context.getGroup()));
        } catch (NodataFoundException e) {
            Log.Debug(JournalPanel.class, e.getMessage());
        }
        groups.setItems(gl);
        List<Object> al = new ArrayList<Object>();
        try {
            al.addAll(DatabaseObject.getObjects(Context.getAccounts()));
        } catch (NodataFoundException e) {
            Log.Debug(JournalPanel.class, e.getMessage());
        }
        accounts.setItems(al);

        //the last settings of the user
        int v = p.getProperty(this, "view", Integer.valueOf(VIEW_EUR));
        if (dataOwner != null) {
            v = VIEW_ALL;
        }
        adjusting = true;
        view.setSelectedIndex(v < 0 || v > VIEW_CUSTOM ? VIEW_EUR : v);
        if (v == VIEW_CUSTOM) {
            AbstractButton[] bs = {incInvoices, incCredits, incDeposits, incPartPayments, incRevenues, incOrders, incOffers, incExpenses, incomeByPay, expenseByPay};
            boolean[] defaults = {true, true, true, true, true, false, false, true, false, false};
            for (int i = 0; i < bs.length; i++) {
                if (bs[i] != incOrders && bs[i] != incOffers) {
                    bs[i].setSelected(p.getProperty(this, "custom" + i, Boolean.valueOf(defaults[i])));
                }
            }
            if (!incomeByPay.isSelected()) {
                incomeByDoc.setSelected(true);
            }
            if (!expenseByPay.isSelected()) {
                expenseByDoc.setSelected(true);
            }
            incomeStatus.setSelectedIndex(Math.max(0, Math.min(3, p.getProperty(this, "incomestatus", Integer.valueOf(0)))));
            expenseStatus.setSelectedIndex(Math.max(0, Math.min(2, p.getProperty(this, "expensestatus", Integer.valueOf(0)))));
        } else {
            applyView(view.getSelectedIndex());
        }
        adjusting = false;

        //every change: custom view (if a filter of the view) and reload
        view.addActionListener(e -> {
            if (!adjusting) {
                adjusting = true;
                applyView(view.getSelectedIndex());
                adjusting = false;
                remember();
                scheduleReload();
            }
        });
        for (AbstractButton b : new AbstractButton[]{incInvoices, incCredits, incDeposits, incPartPayments, incRevenues,
            incExpenses, incomeByDoc, incomeByPay, expenseByDoc, expenseByPay}) {
            b.setOpaque(false);
            b.addActionListener(e -> filterChanged(true));
        }
        //offers and orders are no filter of the views
        for (AbstractButton b : new AbstractButton[]{incOrders, incOffers, othersOpenOnly}) {
            b.setOpaque(false);
            b.addActionListener(e -> filterChanged(false));
        }
        incomeStatus.addActionListener(e -> filterChanged(true));
        expenseStatus.addActionListener(e -> filterChanged(true));
        includechildgroups.setOpaque(false);
        includechildgroups.addActionListener(e -> filterChanged(false));
        includechildaccounts.setOpaque(false);
        includechildaccounts.addActionListener(e -> filterChanged(false));
        groups.setOnChange(() -> filterChanged(false));
        accounts.setOnChange(() -> filterChanged(false));
        timeframeChooser1.addPropertyChangeListener(e -> {
        });
        loadButton.addActionListener(e -> setData());
        find.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFind();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFind();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFind();
            }
        });
        updateEnabled();
    }

    /**
     * The arrow icon of the time frame chooser (a "go" button which is none)
     */
    private static void hideArrow(TimeframeChooser t) {
        for (Component c : t.getComponents()) {
            if (c instanceof JLabel) {
                c.setVisible(false);
            }
        }
        t.setOpaque(false);
    }

    /**
     * @param preset 0 this month, 1 last month, 2 this quarter, 3 last
     * quarter, 4 this year, 5 last year
     * @param today
     * @return the period
     */
    static vTimeframe period(int preset, LocalDate today) {
        LocalDate start;
        LocalDate end;
        switch (preset) {
            case 0:
                start = today.withDayOfMonth(1);
                end = start.plusMonths(1).minusDays(1);
                break;
            case 1:
                start = today.withDayOfMonth(1).minusMonths(1);
                end = start.plusMonths(1).minusDays(1);
                break;
            case 2:
            case 3: {
                int q = (today.getMonthValue() - 1) / 3;
                start = LocalDate.of(today.getYear(), q * 3 + 1, 1);
                if (preset == 3) {
                    start = start.minusMonths(3);
                }
                end = start.plusMonths(3).minusDays(1);
                break;
            }
            case 5:
                start = LocalDate.of(today.getYear() - 1, 1, 1);
                end = LocalDate.of(today.getYear() - 1, 12, 31);
                break;
            default:
                start = LocalDate.of(today.getYear(), 1, 1);
                end = LocalDate.of(today.getYear(), 12, 31);
        }
        return new vTimeframe(date(start), DateConverter.getEndOfDay(date(end)));
    }

    private static Date date(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Sets the filters of a view
     *
     * @param v
     */
    private void applyView(int v) {
        if (v == VIEW_CUSTOM) {
            return;
        }
        for (JCheckBox b : new JCheckBox[]{incInvoices, incCredits, incDeposits, incPartPayments, incRevenues, incExpenses}) {
            b.setSelected(true);
        }
        switch (v) {
            case VIEW_EUR:
                incomeStatus.setSelectedIndex(STATUS_PAID);
                expenseStatus.setSelectedIndex(STATUS_PAID);
                incomeByPay.setSelected(true);
                expenseByPay.setSelected(true);
                break;
            case VIEW_OPEN:
                incomeStatus.setSelectedIndex(STATUS_OPEN);
                expenseStatus.setSelectedIndex(STATUS_OPEN);
                incomeByDoc.setSelected(true);
                expenseByDoc.setSelected(true);
                break;
            default:
                incomeStatus.setSelectedIndex(STATUS_ALL);
                expenseStatus.setSelectedIndex(STATUS_ALL);
                incomeByDoc.setSelected(true);
                expenseByDoc.setSelected(true);
        }
    }

    /**
     * A filter changed: a filter of the views makes the view "custom"
     *
     * @param ofView
     */
    private void filterChanged(boolean ofView) {
        if (adjusting) {
            return;
        }
        if (ofView && view.getSelectedIndex() != VIEW_CUSTOM) {
            adjusting = true;
            view.setSelectedIndex(VIEW_CUSTOM);
            adjusting = false;
        }
        updateEnabled();
        remember();
        scheduleReload();
    }

    private void updateEnabled() {
        boolean income = incInvoices.isSelected() || incCredits.isSelected() || incDeposits.isSelected() || incPartPayments.isSelected()
                || incRevenues.isSelected();
        incomeStatus.setEnabled(income);
        incomeByDoc.setEnabled(income);
        incomeByPay.setEnabled(income);
        expenseStatus.setEnabled(incExpenses.isSelected());
        expenseByDoc.setEnabled(incExpenses.isSelected());
        expenseByPay.setEnabled(incExpenses.isSelected());
        othersOpenOnly.setEnabled(incOrders.isSelected() || incOffers.isSelected());
    }

    private void remember() {
        if (dataOwner != null) {
            return;
        }
        try {
            mpv5.data.PropertyStore p = User.getCurrentUser().getProperties();
            p.changeProperty(this, "view", view.getSelectedIndex());
            AbstractButton[] bs = {incInvoices, incCredits, incDeposits, incPartPayments, incRevenues, incOrders, incOffers, incExpenses, incomeByPay, expenseByPay};
            for (int i = 0; i < bs.length; i++) {
                if (bs[i] != incOrders && bs[i] != incOffers) {
                    p.changeProperty(this, "custom" + i, bs[i].isSelected());
                }
            }
            p.changeProperty(this, "incomestatus", incomeStatus.getSelectedIndex());
            p.changeProperty(this, "expensestatus", expenseStatus.getSelectedIndex());
        } catch (Exception e) {
            Log.Debug(JournalPanel.class, e.getMessage());
        }
    }

    private void scheduleReload() {
        reloadTimer.restart();
    }

    /**
     * Highlights the period chip matching the period, none if it matches none
     */
    private void markPeriod() {
        vTimeframe t = timeframeChooser1.getTime();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < periodChips.size(); i++) {
            vTimeframe p = period(i, today);
            if (sameDay(p.getStart(), t.getStart()) && sameDay(p.getEnd(), t.getEnd())) {
                periodChips.get(i).setSelected(true);
                return;
            }
        }
        periodGroup.clearSelection();
    }

    private static boolean sameDay(Date a, Date b) {
        return a != null && b != null && DateConverter.getDefDateString(a).equals(DateConverter.getDefDateString(b));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void build() {
        removeAll();
        int gap = ModernTheme.px(10);
        setLayout(new BorderLayout(gap, gap));
        setBorder(ModernTheme.padding(12, 14));
        setBackground(ModernTheme.background());

        //header: title and count
        JLabel title = new JLabel(dataOwner == null ? Messages.JOURNAL_TITLE.toString()
                : String.format(Messages.JOURNAL_TITLE_CONTACT.toString(), dataOwner.__getCname()));
        title.setFont(ModernTheme.font(9, Font.BOLD));
        count.setForeground(ModernTheme.muted());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(count);
        exportButton.addActionListener(e -> exportMenu().show(exportButton, 0, exportButton.getHeight()));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(left, BorderLayout.WEST);

        //filters
        for (JComponent c : new JComponent[]{view, incomeStatus, expenseStatus}) {
            ModernTheme.roomyCombo(c);
        }
        JPanel chips = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        chips.setOpaque(false);
        for (ChipToggle c : periodChips) {
            chips.add(c);
        }
        //period, shortcuts, view and user in one line (wraps in narrow windows)
        timeframeChooser1.setPreferredSize(new Dimension(ModernTheme.px(250), timeframeChooser1.getPreferredSize().height));
        JPanel line = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(2)));
        line.setOpaque(false);
        line.add(ModernTheme.fieldLabel(Messages.JOURNAL_PERIOD.toString()));
        line.add(timeframeChooser1);
        line.add(chips);
        line.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(10)));
        line.add(ModernTheme.fieldLabel(Messages.JOURNAL_VIEW.toString()));
        line.add(view);
        //export and reload: at the right of the filter line
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        buttons.setOpaque(false);
        buttons.add(exportButton);
        buttons.add(loadButton);
        JPanel bar = new JPanel(new BorderLayout(gap, 0));
        bar.setOpaque(false);
        bar.add(line, BorderLayout.CENTER);
        bar.add(buttons, BorderLayout.EAST);
        Card period = new Card(null).addFull(bar);

        Card income = new Card(Messages.JOURNAL_INCOME.toString())
                .addFull(flow(incInvoices, incCredits))
                .addFull(flow(incDeposits, incPartPayments))
                .addFull(flow(incRevenues))
                .row(Card.field(Messages.JOURNAL_STATUS.toString(), incomeStatus))
                .row(Card.field(Messages.JOURNAL_DATE_BASIS.toString(), flow(incomeByDoc, incomeByPay)))
                .section(Messages.JOURNAL_NOT_ACCOUNTING.toString())
                .addFull(flow(incOffers, incOrders, othersOpenOnly));
        Card expenses = new Card(Messages.JOURNAL_EXPENSES.toString())
                .addFull(flow(incExpenses))
                .row(Card.field(Messages.JOURNAL_STATUS.toString(), expenseStatus))
                .row(Card.field(Messages.JOURNAL_DATE_BASIS.toString(), flow(expenseByDoc, expenseByPay)));
        Card groupsCard = new Card(Messages.JOURNAL_GROUPS.toString())
                .addFull(groups, 1)
                .addFull(includechildgroups);
        Card accountsCard = new Card(Messages.JOURNAL_ACCOUNTS.toString())
                .addFull(accounts, 1)
                .addFull(includechildaccounts);
        Dimension listSize = new Dimension(ModernTheme.px(160), ModernTheme.px(110));
        groups.setListSize(listSize);
        accounts.setListSize(listSize);

        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        List<Card> columns = new ArrayList<Card>();
        columns.add(income);
        if (dataOwner == null) {
            columns.add(expenses);
        } else {
            //a contact: outgoing documents only
            incRevenues.setSelected(false);
            incRevenues.setVisible(false);
            incExpenses.setSelected(false);
        }
        columns.add(groupsCard);
        columns.add(accountsCard);
        for (int i = 0; i < columns.size(); i++) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = i;
            gc.gridy = 0;
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = i == 0 ? 1.2 : 1;
            gc.weighty = 1;
            gc.insets = new Insets(0, i == 0 ? 0 : gap, 0, 0);
            columns.get(i).setPreferredSize(new Dimension(ModernTheme.px(10), columns.get(i).getPreferredSize().height));
            cards.add(columns.get(i), gc);
        }
        JPanel filters = new JPanel(new BorderLayout(0, gap));
        filters.setOpaque(false);
        filters.add(period, BorderLayout.NORTH);
        filters.add(cards, BorderLayout.CENTER);
        //the filter cards can be collapsed (more room for the table); remembered per user
        setFiltersCollapsed(cards, filtersCollapsed());
        filtersToggle.addActionListener(e -> {
            boolean collapse = cards.isVisible();
            setFiltersCollapsed(cards, collapse);
            try {
                mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(JournalPanel.class.getSimpleName(), FILTERS_COLLAPSED, collapse);
            } catch (Exception ex) {
                Log.Debug(JournalPanel.class, ex.getMessage());
            }
        });

        //the table: one scroll bar (the panel follows the height of the tab)
        ModernTheme.style(jTable1);
        tableScroll.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        tableScroll.setPreferredSize(new Dimension(ModernTheme.px(400), ModernTheme.px(200)));
        ModernTheme.roomy(find);
        find.setToolTipText(Messages.JOURNAL_FIND.toString());
        JPanel tools = new JPanel(new BorderLayout(gap, 0));
        tools.setOpaque(false);
        JPanel findRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        findRow.setOpaque(false);
        findRow.add(new JLabel(Messages.JOURNAL_FIND.toString()));
        findRow.add(find);
        //show / hide the filter cards: next to the search, green
        filtersToggle.setColor(ModernTheme.isDark() ? new java.awt.Color(34, 160, 80) : new java.awt.Color(22, 140, 64));
        filtersToggle.setFont(ModernTheme.font(0, Font.BOLD));
        filtersToggle.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(5), ModernTheme.px(12), ModernTheme.px(5), ModernTheme.px(12)));
        findRow.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(6)));
        findRow.add(filtersToggle);
        tools.add(findRow, BorderLayout.WEST);
        JLabel legend = new JLabel(Messages.JOURNAL_LEGEND.toString());
        legend.setForeground(ModernTheme.muted());
        legend.setFont(ModernTheme.font(-1, Font.PLAIN));
        tools.add(legend, BorderLayout.EAST);
        JPanel table = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        table.setOpaque(false);
        table.add(tools, BorderLayout.NORTH);
        table.add(tableScroll, BorderLayout.CENTER);

        //the sums: income, expenses, result, open items, volume
        taxRates.setFont(ModernTheme.font(-1, Font.PLAIN));
        taxRates.setForeground(ModernTheme.muted());
        exportResult.setName("exportResult"); //NOI18N
        exportResult.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (exportedFile != null && exportedFile.exists()) {
                    mpv5.utils.files.FileActionHandler.open(exportedFile);
                }
            }
        });
        exportResult.setFont(ModernTheme.font(-1, Font.PLAIN));
        //tax rates and export result share one line below the sums
        JPanel info = new JPanel(new BorderLayout(gap, 0));
        info.setOpaque(false);
        info.add(taxRates, BorderLayout.CENTER);
        info.add(exportResult, BorderLayout.EAST);
        Card sums = new Card(null)
                .addFull(sumsGrid())
                .addFull(info);
        //compact: less padding than the filter cards
        sums.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(12), ModernTheme.px(6), ModernTheme.px(12)));

        JPanel journal = new JPanel(new BorderLayout(0, gap));
        journal.setOpaque(false);
        journal.add(filters, BorderLayout.NORTH);
        journal.add(table, BorderLayout.CENTER);
        journal.add(sums, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        if (dataOwner == null) {
            //the journal and its analysis (chart, loaded when first shown)
            final JournalChartPanel analysis = new JournalChartPanel();
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab(Messages.JOURNAL_TAB_JOURNAL.toString(), journal);
            tabs.addTab(Messages.JOURNAL_TAB_CHART.toString(), analysis);
            tabs.addChangeListener(e -> {
                if (tabs.getSelectedComponent() == analysis) {
                    analysis.showing();
                }
            });
            add(tabs, BorderLayout.CENTER);
        } else {
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab(bundle.getString("JournalPanel.jPanel4.TabConstraints.tabTitle"), journal);
            tabs.addTab(bundle.getString("JournalPanel.jPanel5.TabConstraints.tabTitle"), contactOverview());
            add(tabs, BorderLayout.CENTER);
        }
        revalidate();
    }

    private static final String FILTERS_COLLAPSED = "filterscollapsed"; //NOI18N

    /**
     * @return true if the user collapsed the filter cards
     */
    private static boolean filtersCollapsed() {
        try {
            return mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(JournalPanel.class.getSimpleName(), FILTERS_COLLAPSED);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Shows or hides the filter cards (the period line stays)
     */
    private void setFiltersCollapsed(JComponent cards, boolean collapsed) {
        cards.setVisible(!collapsed);
        filtersToggle.setText((collapsed ? Messages.JOURNAL_FILTERS_SHOW : Messages.JOURNAL_FILTERS_HIDE).toString());
        filtersToggle.setIcon(new ArrowIcon(collapsed));
        revalidate();
        repaint();
    }

    /**
     * A small painted arrow (right: collapsed, down: expanded); painted, not a
     * font glyph, so it shows with every font
     */
    private static final class ArrowIcon implements javax.swing.Icon {

        private final boolean right;
        private final int size = ModernTheme.px(8);

        ArrowIcon(boolean right) {
            this.right = right;
        }

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.isEnabled() ? c.getForeground() : ModernTheme.muted());
            java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
            if (right) {
                p.moveTo(x + size * 0.25f, y);
                p.lineTo(x + size * 0.85f, y + size / 2f);
                p.lineTo(x + size * 0.25f, y + size);
            } else {
                p.moveTo(x, y + size * 0.25f);
                p.lineTo(x + size, y + size * 0.25f);
                p.lineTo(x + size / 2f, y + size * 0.85f);
            }
            p.closePath();
            g2.fill(p);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private static JPanel flow(JComponent... cs) {
        JPanel p = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        p.setOpaque(false);
        for (JComponent c : cs) {
            c.setOpaque(false);
            p.add(c);
        }
        return p;
    }

    /**
     * The overview of a contact: its documents as tree, filtered by the radio
     * buttons
     */
    private JPanel contactOverview() {
        JPanel p = new JPanel(new BorderLayout());
        ButtonGroup g = new ButtonGroup();
        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radios.setBorder(BorderFactory.createTitledBorder(bundle.getString("JournalPanel.jPanel10.border.title")));
        filternone.setText(bundle.getString("JournalPanel.filternone.text"));
        filterinvoice.setText(bundle.getString("JournalPanel.filterinvoice.text"));
        filterorders.setText(bundle.getString("JournalPanel.filterorders.text"));
        filteroffers.setText(bundle.getString("JournalPanel.filteroffers.text"));
        filternone.setSelected(true);
        for (JRadioButton b : new JRadioButton[]{filternone, filterinvoice, filterorders, filteroffers}) {
            g.add(b);
            radios.add(b);
            b.addItemListener(e -> filter());
        }
        p.add(radios, BorderLayout.NORTH);
        p.add(new JScrollPane(jTree1), BorderLayout.CENTER);
        return p;
    }

    // Scrollable: the journal follows the size of the tab, the table scrolls;
    // only a very small window scrolls the whole panel
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ModernTheme.px(16);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ModernTheme.px(160);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport && getParent().getWidth() >= ModernTheme.px(MIN_WIDTH);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && getParent().getHeight() >= Math.min(super.getPreferredSize().height, ModernTheme.px(MIN_HEIGHT));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.min(d.width, ModernTheme.px(MIN_WIDTH)), d.height);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Loading">
    /**
     * @param a
     * @return the account and all accounts below it (the accounts have a
     * parent: "All accounts" is the parent of the others)
     */
    private List<Account> subAccounts(Account a) {
        List<Account> r = new ArrayList<Account>();
        r.add(a);
        Set<Integer> seen = new HashSet<Integer>();
        seen.add(a.__getIDS());
        for (int i = 0; i < r.size(); i++) {
            int parent = r.get(i).__getIDS();
            for (Object o : accounts.getItems()) {
                Account c = (Account) o;
                if (c.__getIntparentaccount() == parent && seen.add(c.__getIDS())) {
                    r.add(c);
                }
            }
        }
        return r;
    }

    private JournalLoader.Filter readFilter() {
        JournalLoader.Filter f = new JournalLoader.Filter();
        f.time = timeframeChooser1.getTime();
        if (incInvoices.isSelected()) {
            f.items.add(Context.getInvoice());
        }
        if (incCredits.isSelected()) {
            f.items.add(Context.getCredit());
        }
        if (incDeposits.isSelected()) {
            f.items.add(Context.getDeposit());
        }
        if (incPartPayments.isSelected()) {
            f.items.add(Context.getPartPayment());
        }
        if (incOffers.isSelected()) {
            f.others.add(Context.getOffer());
        }
        if (incOrders.isSelected()) {
            f.others.add(Context.getOrder());
        }
        f.othersOpenOnly = othersOpenOnly.isSelected();
        f.includeInvoices = incInvoices.isSelected();
        f.revenues = incRevenues.isSelected() && dataOwner == null;
        f.expenses = incExpenses.isSelected() && dataOwner == null;
        f.incomeStatus = incomeStatus.getSelectedIndex();
        f.expenseStatus = expenseStatus.getSelectedIndex();
        f.incomeByPay = incomeByPay.isSelected();
        f.expenseByPay = expenseByPay.isSelected();
        //all selected: no restriction (also records without group or account)
        for (Object o : groups.getFilter()) {
            f.groups.add((Group) o);
        }
        f.childGroups = includechildgroups.isSelected();
        for (Object o : accounts.getFilter()) {
            if (includechildaccounts.isSelected()) {
                for (Account c : subAccounts((Account) o)) {
                    f.accounts.add(c.__getIDS());
                }
            }
            f.accounts.add(((Account) o).__getIDS());
        }
        f.contact = dataOwner;
        return f;
    }

    private void setData() {
        reloadTimer.stop();
        markPeriod();
        final JournalLoader.Filter f = readFilter();
        final String key = filterKey();
        final int gen = ++generation;
        loading = true;
        loadButton.setEnabled(false);
        count.setText(Messages.JOURNAL_LOADING.toString());
        new SwingWorker<JournalLoader.Loaded, Void>() {
            @Override
            protected JournalLoader.Loaded doInBackground() throws Exception {
                return JournalLoader.load(f);
            }

            @Override
            protected void done() {
                if (gen != generation) {
                    return;
                }
                loading = false;
                loadButton.setEnabled(true);
                Runnable after = afterLoad;
                afterLoad = null;
                try {
                    show(get());
                    loadedKey = key;
                } catch (Exception e) {
                    Log.Debug(JournalPanel.class, e);
                    count.setText(" ");
                    Popup.error(e);
                    return;
                }
                if (after != null) {
                    after.run();
                }
            }
        }.execute();
    }

    private boolean loading;
    private String loadedKey;
    private Runnable afterLoad;

    /**
     * @return the filters as shown (also dates typed in without reloading)
     */
    private String filterKey() {
        StringBuilder b = new StringBuilder();
        vTimeframe t = timeframeChooser1.getTime();
        b.append(t.getStart().getTime()).append('-').append(t.getEnd().getTime());
        for (AbstractButton x : new AbstractButton[]{incInvoices, incCredits, incDeposits, incPartPayments, incRevenues, incOrders, incOffers,
            incExpenses, incomeByPay, expenseByPay, includechildgroups, includechildaccounts, othersOpenOnly}) {
            b.append(x.isSelected() ? '1' : '0');
        }
        b.append(incomeStatus.getSelectedIndex()).append(expenseStatus.getSelectedIndex());
        for (Object o : groups.getSelected()) {
            b.append('g').append(((Group) o).__getIDS());
        }
        for (Object o : accounts.getSelected()) {
            b.append('a').append(((Account) o).__getIDS());
        }
        return b.toString();
    }

    /**
     * Runs an export on the journal of the filters as shown: reloads first if
     * a filter changed since the last loading (or a loading is running)
     *
     * @param export
     */
    private void withCurrentData(Runnable export) {
        if (!loading && !reloadTimer.isRunning() && filterKey().equals(loadedKey)) {
            export.run();
        } else {
            afterLoad = export;
            setData();
        }
    }

    /**
     * The whole journal of a period, without filters: all outgoing
     * documents (also offers and orders), income, expenses and payments, any
     * status, with their amounts by tax rate and whether they count
     *
     * @param time the period
     * @param byPayDate true: by payment date (EÜR), false: by document date
     * @return the entries, sorted by date
     * @throws Exception
     */
    public static List<Entry> loadAll(vTimeframe time, boolean byPayDate) throws Exception {
        return JournalLoader.loadAll(time, byPayDate);
    }

    /**
     * Shows a loaded journal
     */
    private void show(JournalLoader.Loaded l) {
        entries = l.entries;
        totals = l.totals;
        rateLabels = l.labels;
        Object[][] d = new Object[entries.size()][];
        for (int i = 0; i < d.length; i++) {
            d[i] = row(entries.get(i));
        }
        jTable1.setModel(new MPTableModel(d, Headers.JOURNAL.getValue(),
                new Class[]{DatabaseObject.class, String.class, String.class, String.class, String.class, String.class, Date.class, String.class,
                    String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class}));
        applyFind();

        Totals t = totals;
        revenueNetto.setText(FormatNumber.formatDezimal(t.incomeNet));
        revenueTax.setText(FormatNumber.formatDezimal(t.incomeTax));
        revenueBrut.setText(FormatNumber.formatDezimal(t.incomeGross));
        expNetto.setText(FormatNumber.formatDezimal(t.expenseNet));
        expTax.setText(FormatNumber.formatDezimal(t.expenseTax));
        expBrut.setText(FormatNumber.formatDezimal(t.expenseGross));
        profitNetto.setText(FormatNumber.formatDezimal(t.profitNet()));
        profitTax.setText(FormatNumber.formatDezimal(t.profitTax()));
        profitBrut.setText(FormatNumber.formatDezimal(t.profitGross()));
        openReceivables.setText(FormatNumber.formatDezimal(t.openReceivables));
        openPayables.setText(FormatNumber.formatDezimal(t.openPayables));
        volumeDiscount.setText(FormatNumber.formatDezimal(t.discount));
        volumeNetto.setText(FormatNumber.formatDezimal(t.volumeNet));
        taxVolume.setText(FormatNumber.formatDezimal(t.volumeTax));
        volumeBrutto.setText(FormatNumber.formatDezimal(t.volumeGross));
        openOffers.setText(FormatNumber.formatDezimal(t.openOffersNet));
        openOrders.setText(FormatNumber.formatDezimal(t.openOrdersNet));
        openOthersGross.setText(FormatNumber.formatDezimal(t.openOffersGross.add(t.openOrdersGross)));
        boolean others = incOffers.isSelected() || incOrders.isSelected();
        for (Component c : othersColumn) {
            c.setVisible(others);
        }
        taxRates.setText(rates(Messages.JOURNAL_TAX_RATES_INCOME.toString(), t.incomeByRate, l.labels)
                + "     " + rates(Messages.JOURNAL_TAX_RATES_EXPENSES.toString(), t.expensesByRate, l.labels));
    }

    private static String rates(String label, Map<BigDecimal, BigDecimal[]> m, Map<BigDecimal, String> labels) {
        if (m.isEmpty()) {
            return label + " -";
        }
        StringBuilder b = new StringBuilder(label);
        boolean first = true;
        for (Map.Entry<BigDecimal, BigDecimal[]> r : m.entrySet()) {
            b.append(first ? " " : "  ·  ");
            b.append(JournalAdviserPackage.rateLabel(r.getKey(), labels, Messages.JOURNAL_TAX_RATE_UNKNOWN.toString()));
            b.append(": ").append(FormatNumber.formatDezimal(r.getValue()[0])).append(" / ").append(FormatNumber.formatDezimal(r.getValue()[1]));
            first = false;
        }
        return b.toString();
    }

    /**
     * @param e
     * @return the table row (the columns of {@link Headers#JOURNAL})
     */
    private static Object[] row(Entry e) {
        String type;
        switch (e.type) {
            case Constants.TYPE_REVENUE:
                type = Messages.TYPE_REVENUE.toString();
                break;
            case Constants.TYPE_EXPENSE:
                type = Messages.TYPE_EXPENSE.toString();
                break;
            case Constants.TYPE_PAYMENT:
                type = Messages.TYPE_PAYMENT.toString();
                break;
            default:
                type = Item.getTypeString(e.type);
        }
        String status;
        if (e.type == Constants.TYPE_EXPENSE) {
            status = e.status == JournalCalc.EXPENSE_PAID ? Messages.STATUS_PAID.toString() : Messages.STATUS_UNPAID.toString();
        } else {
            status = Item.getStatusString(e.status);
        }
        return new Object[]{e.obj, e.contact, e.prename, e.street, e.city, e.country, e.date, e.group, e.account, e.number, type, status,
            FormatNumber.formatLokalCurrency(e.net), FormatNumber.formatLokalCurrency(e.tax), FormatNumber.formatLokalCurrency(e.gross),
            FormatNumber.formatLokalCurrency(e.effect), FormatNumber.formatLokalCurrency(e.discount), e.description};
    }

    /**
     * Filters the rows by the text of the search field
     */
    private void applyFind() {
        if (!(jTable1.getRowSorter() instanceof TableRowSorter)) {
            count.setText(String.format(Messages.JOURNAL_COUNT.toString(), entries.size()));
            return;
        }
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> s = (TableRowSorter<TableModel>) jTable1.getRowSorter();
        String text = find.getText().trim();
        if (text.isEmpty()) {
            s.setRowFilter(null);
            count.setText(String.format(Messages.JOURNAL_COUNT.toString(), entries.size()));
        } else {
            s.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text))); //NOI18N
            count.setText(String.format(Messages.JOURNAL_COUNT_FILTERED.toString(), jTable1.getRowCount(), entries.size()));
        }
    }

    @Override
    public void refresh() {
        setData();
    }

    @Override
    public void flush() {
        jTable1.setModel(new DefaultTableModel());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Export">
    private JPopupMenu exportMenu() {
        JPopupMenu m = new JPopupMenu();
        m.add(item(Messages.JOURNAL_EXPORT_ADVISER, Messages.JOURNAL_EXPORT_ADVISER_TIP, () -> withCurrentData(() -> exportAdviser())));
        List<JournalExport> pluginExports = ExtensionManager.instance().journalExports();
        if (!pluginExports.isEmpty()) {
            m.addSeparator();
            for (final JournalExport pe : pluginExports) {
                JMenuItem mi = new JMenuItem(pe.title());
                if (pe.tooltip() != null && !pe.tooltip().isEmpty()) {
                    mi.setToolTipText("<html><div style='width:360px'>" + pe.tooltip() + "</div>"); //NOI18N
                }
                mi.addActionListener(ev -> withCurrentData(() -> {
                    vTimeframe t = timeframeChooser1.getTime();
                    pe.export(new JournalExportContext(new ArrayList<Entry>(entries), totals,
                            t.getStart(), t.getEnd(), filterLines(), rateLabels, this, this::showExported));
                }));
                m.add(mi);
            }
        }
        m.addSeparator();
        m.add(item(Messages.JOURNAL_EXPORT_CSV, null, () -> withCurrentData(() -> exportTable(true))));
        m.add(item(Messages.JOURNAL_EXPORT_HTML, null, () -> withCurrentData(() -> exportTable(false))));
        m.add(item(Messages.JOURNAL_EXPORT_TEMPLATE_PDF, null, () -> withCurrentData(() -> exportTemplate(true))));
        m.add(item(Messages.JOURNAL_EXPORT_TEMPLATE_ODT, null, () -> withCurrentData(() -> exportTemplate(false))));
        m.addSeparator();
        m.add(item(Messages.JOURNAL_EXPORT_PRINT, null, () -> withCurrentData(() -> {
            try {
                jTable1.print();
            } catch (Exception e) {
                failed(e);
            }
        })));
        return m;
    }

    private static JMenuItem item(Messages text, Messages tip, Runnable r) {
        JMenuItem i = new JMenuItem(text.toString());
        if (tip != null) {
            i.setToolTipText("<html><div style='width:360px'>" + tip + "</div>"); //NOI18N
        }
        i.addActionListener(e -> r.run());
        return i;
    }

    private void failed(Exception e) {
        Log.Debug(e);
        exportedFile = null;
        exportResult.setText(Messages.JOURNAL_EXPORT_FAILED + ": " + e.getMessage());
        exportResult.setCursor(Cursor.getDefaultCursor());
    }

    /**
     * CSV (semicolon, for Excel) or HTML of the table as shown (sorted,
     * searched, columns in their order)
     *
     * @param csv
     */
    private void exportTable(boolean csv) {
        try {
            String ext = csv ? "csv" : "html"; //NOI18N
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "journal-" + DateConverter.getTodayFileDate() + "." + ext); //NOI18N
            if (!d.saveFile()) {
                return;
            }
            File f = d.getFile();
            if (csv) {
                StringBuilder b = new StringBuilder();
                for (int c = 0; c < jTable1.getColumnCount(); c++) {
                    b.append(c > 0 ? ";" : "").append(JournalAdviserPackage.cell(jTable1.getColumnName(c)));
                }
                b.append("\r\n");
                for (int r = 0; r < jTable1.getRowCount(); r++) {
                    Entry e = entries.get(jTable1.convertRowIndexToModel(r));
                    for (int c = 0; c < jTable1.getColumnCount(); c++) {
                        int mc = jTable1.convertColumnIndexToModel(c);
                        Object v;
                        switch (mc) {
                            case 6:
                                v = e.date == null ? "" : DateConverter.getDefDateString(e.date);
                                break;
                            case 12:
                                v = JournalAdviserPackage.amount(e.net);
                                break;
                            case 13:
                                v = JournalAdviserPackage.amount(e.tax);
                                break;
                            case 14:
                                v = JournalAdviserPackage.amount(e.gross);
                                break;
                            case COL_EFFECT:
                                v = JournalAdviserPackage.amount(e.effect);
                                break;
                            case 16:
                                v = JournalAdviserPackage.amount(e.discount);
                                break;
                            default:
                                v = jTable1.getModel().getValueAt(jTable1.convertRowIndexToModel(r), mc);
                        }
                        b.append(c > 0 ? ";" : "").append(JournalAdviserPackage.cell(v));
                    }
                    b.append("\r\n");
                }
                try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(f.toPath())) {
                    JournalAdviserPackage.writeCsv(out, b.toString());
                }
            } else {
                f = new mpv5.utils.files.TableHtmlWriter(jTable1, f, jTable1.getShowHorizontalLines(), jTable1.getShowVerticalLines()).createHtml();
            }
            showExported(f);
        } catch (Exception e) {
            failed(e);
        }
    }

    /**
     * @return the filters as text (for the summary of the package)
     */
    private List<String> filterLines() {
        List<String> l = new ArrayList<String>();
        l.add(Messages.JOURNAL_VIEW + ": " + view.getSelectedItem());
        List<String> types = new ArrayList<String>();
        for (JCheckBox b : new JCheckBox[]{incInvoices, incCredits, incDeposits, incPartPayments, incRevenues, incOrders, incOffers, incExpenses}) {
            if (b.isSelected() && b.isVisible()) {
                types.add(b.getText());
            }
        }
        l.add(Messages.JOURNAL_INCOME + ": " + String.join(", ", types) + " / " + Messages.JOURNAL_STATUS + " " + incomeStatus.getSelectedItem()
                + " / " + (incomeByPay.isSelected() ? incomeByPay.getText() : incomeByDoc.getText()));
        if (incExpenses.isSelected()) {
            l.add(Messages.JOURNAL_EXPENSES + ": " + Messages.JOURNAL_STATUS + " " + expenseStatus.getSelectedItem()
                    + " / " + (expenseByPay.isSelected() ? expenseByPay.getText() : expenseByDoc.getText()));
        }
        l.add(Messages.JOURNAL_GROUPS + ": " + (groups.getFilter().isEmpty() ? Messages.ALL.toString() : groups.getFilter().toString()
                + (includechildgroups.isSelected() ? " (+)" : "")));
        l.add(Messages.JOURNAL_ACCOUNTS + ": " + (accounts.getFilter().isEmpty() ? Messages.ALL.toString() : accounts.getFilter().toString()
                + (includechildaccounts.isSelected() ? " (+)" : "")));
        if (dataOwner != null) {
            l.add(String.valueOf(dataOwner.__getCname()));
        }
        return l;
    }

    /**
     * The ZIP for the tax adviser: journal (CSV), summary, receipts and
     * e-invoices of the loaded journal
     */
    private void exportAdviser() {
        vTimeframe t = timeframeChooser1.getTime();
        String name = "journal-package-" + new java.text.SimpleDateFormat("yyyyMMdd").format(t.getStart()) + "-" //NOI18N
                + new java.text.SimpleDateFormat("yyyyMMdd").format(t.getEnd()) + ".zip"; //NOI18N
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, name);
        if (!d.saveFile()) {
            return;
        }
        File f = d.getFile();
        final File target = f.getName().toLowerCase().endsWith(".zip") ? f : new File(f.getParentFile(), f.getName() + ".zip"); //NOI18N
        final JournalAdviserPackage p = new JournalAdviserPackage(new ArrayList<Entry>(entries), totals, t.getStart(), t.getEnd(), filterLines(), rateLabels);
        exportedFile = null;
        exportResult.setText(Messages.JOURNAL_EXPORT_RUNNING.toString());
        exportResult.setCursor(Cursor.getDefaultCursor());
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                p.write(target);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    showExported(target);
                    String msg = String.format(Messages.JOURNAL_ADVISER_DONE.toString(), entries.size(), p.getReceipts(), p.getEInvoices());
                    if (!p.getProblems().isEmpty()) {
                        Popup.notice(p.getProblems(), msg);
                    } else {
                        YabsViewProxy.instance().addMessage(msg);
                    }
                } catch (Exception e) {
                    failed(e);
                }
            }
        }.execute();
    }

    /**
     * Show the link to the saved file below the totals
     *
     * @param f
     */
    private void showExported(File f) {
        exportedFile = f;
        if (f == null || !f.exists()) {
            exportResult.setText(Messages.JOURNAL_EXPORT_FAILED.toString());
            exportResult.setToolTipText(null);
            exportResult.setCursor(Cursor.getDefaultCursor());
            return;
        }
        String path = f.getAbsolutePath().replace("&", "&amp;").replace("<", "&lt;"); //NOI18N
        exportResult.setText("<html>" + Messages.JOURNAL_EXPORT_SAVED + " <a href=\"#\">" + path + "</a>"); //NOI18N
        exportResult.setToolTipText(Messages.JOURNAL_EXPORT_OPEN.toString());
        exportResult.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * @return the file the link below the totals points to, or null
     */
    public File getExportedFile() {
        return exportedFile;
    }

    /**
     * Fill the journal template and save it as PDF or ODT where the user wants
     *
     * @param pdf
     */
    private void exportTemplate(boolean pdf) {
        if (journalOrContactTemplate != null) {
            String ext = pdf ? "pdf" : "odt"; //NOI18N
            DialogForFile dialog = new DialogForFile(DialogForFile.FILES_ONLY, "journal-" + DateConverter.getTodayFileDate() + "." + ext); //NOI18N
            if (!dialog.saveFile()) {
                return;
            }
            File target = dialog.getFile();
            if (!target.getName().toLowerCase().endsWith("." + ext)) { //NOI18N
                target = new File(target.getParentFile(), target.getName() + "." + ext); //NOI18N
            }
            exportTemplate(pdf, target);
        } else {
            Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")"); //NOI18N
        }
    }

    /**
     * Fill the journal template and save it to the given file (without asking)
     *
     * @param pdf PDF or ODT
     * @param target
     */
    void exportTemplate(final boolean pdf, final File target) {
        if (journalOrContactTemplate == null) {
            return;
        }
        exportedFile = null;
        exportResult.setText(Messages.JOURNAL_EXPORT_RUNNING.toString());
        exportResult.setCursor(Cursor.getDefaultCursor());
        final Waiter save = Export.wait(target);
        Waiter pr = new Waiter() {
            @Override
            public void set(Object object, Exception exception) throws Exception {
                save.set(object, exception);
                showExported(exception == null ? target : null);
            }
        };
        TreeMap<String, Object> map = new TreeMap<String, Object>();
        map.put("journal.netvalue", volumeNetto.getText()); //NOI18N
        map.put("journal.taxvalue", taxVolume.getText()); //NOI18N
        map.put("journal.grosvalue", volumeBrutto.getText()); //NOI18N
        map.put("journal.revenuevalue", revenueNetto.getText()); //NOI18N
        map.put("journal.revenuetax", revenueTax.getText()); //NOI18N
        map.put("journal.revenuegrosvalue", revenueBrut.getText()); //NOI18N
        map.put("journal.expensevalue", expNetto.getText()); //NOI18N
        map.put("journal.expensetax", expTax.getText()); //NOI18N
        map.put("journal.expensegrosvalue", expBrut.getText()); //NOI18N
        map.put("journal.profitvalue", profitNetto.getText()); //NOI18N
        map.put("journal.profittax", profitTax.getText()); //NOI18N
        map.put("journal.profitgrosvalue", profitBrut.getText()); //NOI18N
        map.put("journal.discountvalue", volumeDiscount.getText()); //NOI18N
        map.put("journal.openreceivables", openReceivables.getText()); //NOI18N
        map.put("journal.openpayables", openPayables.getText()); //NOI18N
        map.put("journal.openoffers", openOffers.getText()); //NOI18N
        map.put("journal.openorders", openOrders.getText()); //NOI18N
        map.put("journal.datenow", DateConverter.getDefDateString(new Date())); //NOI18N
        map.put("journal.datefrom", DateConverter.getDefDateString(timeframeChooser1.getTime().getStart())); //NOI18N
        map.put("journal.dateto", DateConverter.getDefDateString(timeframeChooser1.getTime().getEnd())); //NOI18N
        if (!groups.getFilter().isEmpty()) {
            map.put("journal.group", join(groups.getFilter())); //NOI18N
            if (includechildgroups.isSelected()) {
                List<Object> children = new ArrayList<Object>();
                for (Object g : groups.getFilter()) {
                    try {
                        children.addAll(((Group) g).getChildGroups());
                    } catch (NodataFoundException e) {
                        Log.Debug(JournalPanel.class, e.getMessage());
                    }
                }
                map.put("journal.childgroups", String.valueOf(children)); //NOI18N
            }
        }
        map.put("journal.user", Messages.ALL.toString()); //NOI18N
        map.put("journal.accounts", accounts.getFilter().isEmpty() ? Messages.ALL.toString() : join(accounts.getFilter())); //NOI18N

        journalOrContactTemplate.injectData(map);
        //rows as sorted on screen, columns in the order of the model (not as moved
        //on screen): the column format of the template refers to them
        TableModel xx = jTable1.getModel();
        Object[][] d = new Object[jTable1.getRowCount()][];
        for (int i = 0; i < d.length; i++) {
            Object[] nd = new Object[xx.getColumnCount()];
            for (int j = 1; j < xx.getColumnCount(); j++) {
                Object object = xx.getValueAt(jTable1.convertRowIndexToModel(i), j);
                if (object instanceof Date) {
                    nd[j - 1] = DateConverter.getDefDateString((Date) object);
                } else {
                    //empty, not the field name (e.g. the name of an expense)
                    nd[j - 1] = object == null ? "" : object;
                }
            }
            d[i] = nd;
        }
        journalOrContactTemplate.setContextName(Messages.TYPE_JOURNAL.toString());
        journalOrContactTemplate.injectTable(ODTFile2.KEY_TABLE + "1", new MPTableModel(d)); //NOI18N
        if (pdf) {
            final Template jt = journalOrContactTemplate;
            Export.createFileWithProgress(() -> jt, dataOwner, pr);
        } else {
            final Template jt = journalOrContactTemplate;
            mpv5.utils.export.ExportProgress.execute(Messages.TYPE_JOURNAL + " ODT", Messages.EXPORT_STEP_FIELDS,
                    () -> new Job(Export.sourceFile(jt, dataOwner), pr), Messages.EXPORT_STEP_OPEN);
        }
    }

    private static String join(List<Object> l) {
        String s = l.toString();
        return s.substring(1, s.length() - 1);
    }

    private void loadTemplate() {
        new Thread(() -> {
            if (dataOwner != null) {
                journalOrContactTemplate = TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_CONTACT);
            } else {
                journalOrContactTemplate = TemplateHandler.loadTemplate(Long.valueOf(User.getCurrentUser().__getGroupsids()), Constants.TYPE_JOURNAL);
            }
        }, "journal-template").start(); //NOI18N
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Table actions">
    private DatabaseObject objectAt(int viewRow) {
        Object o = jTable1.getModel().getValueAt(jTable1.convertRowIndexToModel(viewRow), 0);
        return o instanceof DatabaseObject ? (DatabaseObject) o : null;
    }

    private void setPopup() {
        new TablePopUp(jTable1,
                new String[]{Messages.RELOAD.getValue(),
                    null, Messages.EMAIL_SEND.getValue(),
                    Messages.SEPA_DD_CREATE.getValue(),
                    Messages.PDF_CREATE.getValue(),
                    Messages.ODT_CREATE.getValue(),
                    null, Messages.MARK_PAID.toString()},
                new java.awt.event.ActionListener[]{e -> setData(), null, e -> email(), e -> sepa(),
                    e -> export(true), e -> export(false), null, e -> setPaid()});
    }

    /**
     * Marks the selected documents as paid, with a preview (a payment row
     * stands for its invoice)
     */
    private void setPaid() {
        List<DatabaseObject> l = new ArrayList<DatabaseObject>();
        for (int row : jTable1.getSelectedRows()) {
            Entry e = entries.get(jTable1.convertRowIndexToModel(row));
            if (e.obj instanceof DatabaseObject && !l.contains(e.obj)) {
                l.add((DatabaseObject) e.obj);
            }
        }
        mpv5.ui.dialogs.MarkPaidDialog.show(this, l, this::setData);
    }

    /**
     * @return the selected unpaid invoices (no payments, no other records)
     */
    private Map<String, Object> selectedUnpaidInvoices() {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int row : jTable1.getSelectedRows()) {
            Entry e = entries.get(jTable1.convertRowIndexToModel(row));
            if (e.obj instanceof Item && e.type == Constants.TYPE_INVOICE && ((Item) e.obj).__getIntstatus() != Item.STATUS_PAID) {
                map.put(((Item) e.obj).__getCnumber(), e.obj);
            }
        }
        return map;
    }

    /**
     * SEPA direct debit file for the selected unpaid invoices
     */
    private void sepa() {
        if (jTable1.getSelectedRowCount() < 1) {
            Popup.notice(Messages.SELECT_AN_INVOICE);
            return;
        }
        HashMap<String, Object> map = new HashMap<String, Object>(selectedUnpaidInvoices());
        if (map.isEmpty()) {
            Popup.notice(Messages.SEPA_DD_NOTHING);
            return;
        }
        //check first: without the company's data or a debit, no file and no save dialog
        mpv5.utils.export.SepaDirectDebit sepa = new mpv5.utils.export.SepaDirectDebit(map);
        String problem = sepa.check();
        if (problem != null) {
            Popup.notice(problem);
            return;
        }
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "sepa-lastschrift-" + LocalDate.now() + ".xml"); //NOI18N
        mpv5.utils.export.ExportProgress.execute("SEPA", Messages.EXPORT_STEP_SEPA, () -> new Job(sepa, d), Messages.EXPORT_STEP_SAVE); //NOI18N
    }

    private void email() {
        if (jTable1.getSelectedRowCount() < 1) {
            Popup.notice(Messages.SELECT_AN_INVOICE);
            return;
        }
        HashMap<String, Object> map = new HashMap<String, Object>(selectedUnpaidInvoices());
        if (map.isEmpty()) {
            Popup.notice(Messages.SELECT_AN_INVOICE);
            return;
        }
        YabsViewProxy.instance().setWaiting(true);
        mpv5.utils.export.ExportProgress.execute(Messages.EXPORT_MAIL + " (" + map.size() + ")", null, () -> new Job(new EmailBatch(map), new Waiter() {
            @Override
            public void set(Object object, Exception exceptions) throws Exception {
                if (exceptions != null) {
                    Notificator.raiseNotification(exceptions, false);
                    Log.Debug(exceptions);
                }
                YabsViewProxy.instance().setWaiting(false);
            }
        }, "Emails: " + Messages.DONE), null); //NOI18N
    }

    /**
     * PDF or ODT of the selected documents, as from the document itself: each
     * file goes to the folder and gets the name of the file name defaults
     * (user properties); one question for all instead of a folder dialog
     *
     * @param pdf true: PDF, false: ODT
     */
    private void export(final boolean pdf) {
        if (jTable1.getSelectedRowCount() < 1) {
            Popup.notice(Messages.SELECT_AN_INVOICE);
            return;
        }
        final List<DatabaseObject> docs = new ArrayList<DatabaseObject>();
        List<String> noTemplate = new ArrayList<String>();
        Set<String> dirs = new java.util.TreeSet<String>();
        Set<DatabaseObject> seen = new HashSet<DatabaseObject>();
        for (int row : jTable1.getSelectedRows()) {
            Entry e = entries.get(jTable1.convertRowIndexToModel(row));
            if (!(e.obj instanceof Templateable) || e.type == Constants.TYPE_PAYMENT || !seen.add((DatabaseObject) e.obj)) {
                continue;
            }
            DatabaseObject dob = (DatabaseObject) e.obj;
            //loads (and caches) the template; the export checks the cache
            if (TemplateHandler.loadTemplate((Templateable) dob) == null) {
                noTemplate.add(dob.__getCname() + " (" + TemplateHandler.getName(((Templateable) dob).templateType()) + ")"); //NOI18N
                continue;
            }
            docs.add(dob);
            dirs.add(User.getSaveDir(dob).getPath());
        }
        if (!noTemplate.isEmpty()) {
            Popup.notice(noTemplate, Messages.NO_TEMPLATE_DEFINDED);
        }
        if (docs.isEmpty()) {
            return;
        }
        StringBuilder q = new StringBuilder(String.format(Messages.JOURNAL_EXPORT_CONFIRM.toString(), docs.size(), pdf ? "PDF" : "ODT")); //NOI18N
        for (String d : dirs) {
            q.append("\n  ").append(d); //NOI18N
        }
        if (!Popup.Y_N_dialog(q.toString())) {
            return;
        }
        new Thread(() -> {
            for (DatabaseObject dob : docs) {
                if (pdf) {
                    dob.toPdf(false);
                } else {
                    dob.toOdt(false);
                }
            }
            YabsViewProxy.instance().addMessage(docs.size() + (pdf ? " PDF " : " ODT ") + Messages.SAVED); //NOI18N
        }, "journal-export").start(); //NOI18N
    }

    private void filter() {
        if (dataOwner == null) {
            return;
        }
        if (filterinvoice.isSelected()) {
            QueryCriteria2 p = new QueryCriteria2();
            p.and(new QueryParameter(Context.getInvoice(), "intstatus", Item.STATUS_PAID, QueryParameter.NOTEQUAL)); //NOI18N
            jTree1.setModel(new MPTreeModel(dataOwner, Context.getInvoice(), p));
        } else if (filteroffers.isSelected()) {
            QueryCriteria2 p = new QueryCriteria2();
            p.and(new QueryParameter(Context.getOffer(), "intstatus", Item.STATUS_FINISHED, QueryParameter.NOTEQUAL)); //NOI18N
            jTree1.setModel(new MPTreeModel(dataOwner, Context.getOffer(), p));
        } else if (filterorders.isSelected()) {
            QueryCriteria2 p = new QueryCriteria2();
            p.and(new QueryParameter(Context.getOrder(), "intstatus", Item.STATUS_FINISHED, QueryParameter.NOTEQUAL)); //NOI18N
            jTree1.setModel(new MPTreeModel(dataOwner, Context.getOrder(), p));
        } else {
            jTree1.setModel(new MPTreeModel(dataOwner, null, null));
        }
    }
    // </editor-fold>

    /**
     * A searchable multiple-selection list (groups, accounts): a search field,
     * the list and a hint. All are selected at first, which means no
     * restriction; a click selects one, Ctrl+click several. The selection
     * survives the search.
     */
    static final class SelectionList extends JPanel {

        private static final long serialVersionUID = 1L;
        private final JTextField search = new JTextField();
        private final JList<Object> list = new JList<Object>(new DefaultListModel<Object>());
        private final JScrollPane scroll = new JScrollPane(list);
        private final JLabel state = new JLabel(" ");
        private final JButton clear = new JButton(Messages.JOURNAL_SELECT_ALL.toString());
        private final List<Object> all = new ArrayList<Object>();
        private final Set<Object> selected = new LinkedHashSet<Object>();
        private final String hint;
        private boolean filling;
        private Runnable onChange;

        SelectionList(String hint) {
            super(new BorderLayout(0, ModernTheme.px(4)));
            this.hint = hint;
            setOpaque(false);
            ModernTheme.roomy(search);
            search.setToolTipText(Messages.JOURNAL_FIND.toString());
            search.putClientProperty("JTextField.placeholderText", Messages.JOURNAL_FIND.toString()); //NOI18N
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setFont(ModernTheme.font(0, Font.PLAIN));
            list.setToolTipText(hint);
            list.addListSelectionListener(e -> {
                if (filling || e.getValueIsAdjusting()) {
                    return;
                }
                DefaultListModel<Object> m = (DefaultListModel<Object>) list.getModel();
                for (int i = 0; i < m.size(); i++) {
                    if (list.isSelectedIndex(i)) {
                        selected.add(m.get(i));
                    } else {
                        selected.remove(m.get(i));
                    }
                }
                changed();
            });
            scroll.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
            search.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    fill();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    fill();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    fill();
                }
            });
            clear.setBorderPainted(false);
            clear.setContentAreaFilled(false);
            clear.setFocusPainted(false);
            clear.setForeground(ModernTheme.accent());
            clear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clear.setMargin(new Insets(0, 0, 0, 0));
            clear.addActionListener(e -> {
                selected.clear();
                selected.addAll(all);
                fill();
                changed();
            });
            state.setForeground(ModernTheme.muted());
            state.setFont(ModernTheme.font(-1, Font.PLAIN));
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(state, BorderLayout.CENTER);
            bottom.add(clear, BorderLayout.EAST);
            add(search, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
            updateState();
        }

        void setListSize(Dimension d) {
            scroll.setPreferredSize(d);
        }

        void setOnChange(Runnable r) {
            onChange = r;
        }

        void setItems(List<Object> items) {
            all.clear();
            all.addAll(items);
            selected.clear();
            selected.addAll(all);
            fill();
        }

        /**
         * @return all items
         */
        List<Object> getItems() {
            return new ArrayList<Object>(all);
        }

        /**
         * @return the selected items to filter by: empty if all (or none) are
         * selected - no restriction
         */
        List<Object> getFilter() {
            return selected.isEmpty() || selected.size() == all.size() ? new ArrayList<Object>() : new ArrayList<Object>(selected);
        }

        /**
         * @return the selected items
         */
        List<Object> getSelected() {
            return new ArrayList<Object>(selected);
        }

        private void fill() {
            filling = true;
            try {
                String q = search.getText().trim().toLowerCase();
                DefaultListModel<Object> m = new DefaultListModel<Object>();
                List<Integer> sel = new ArrayList<Integer>();
                for (Object o : all) {
                    if (q.isEmpty() || String.valueOf(o).toLowerCase().contains(q)) {
                        if (selected.contains(o)) {
                            sel.add(m.size());
                        }
                        m.addElement(o);
                    }
                }
                list.setModel(m);
                int[] idx = new int[sel.size()];
                for (int i = 0; i < idx.length; i++) {
                    idx[i] = sel.get(i);
                }
                list.setSelectedIndices(idx);
            } finally {
                filling = false;
            }
            updateState();
        }

        private void changed() {
            updateState();
            if (onChange != null) {
                onChange.run();
            }
        }

        private void updateState() {
            boolean everything = selected.isEmpty() || selected.size() == all.size();
            state.setText(everything ? hint : String.format(Messages.JOURNAL_SELECTED.toString(), selected.size()));
            clear.setVisible(!everything);
        }
    }
}
