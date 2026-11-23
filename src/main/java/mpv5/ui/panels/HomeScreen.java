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
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.Scheduler;
import mpv5.handler.SearchService;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.panels.calendar.ScheduleCalendar;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 * The dashboard ("Schreibtisch"): tiles with what is overdue and open, the
 * open documents in one table (status, document, customer, date, days open,
 * amount) with filters, the calendar and the recurring invoices due this
 * month. Which documents appear and when they turn yellow or red is set in
 * the settings "Dashboard, deadlines and appointments".
 */
public class HomeScreen extends JPanel {

    private static final long serialVersionUID = 2L;
    private static HomeScreen icke;

    /**
     * Status of an open document (the colors of {@link Scheduler})
     */
    static final int OVERDUE = 0, WARNING = 1, OPEN = 2;

    /**
     * A row of the open documents
     */
    static final class Row {

        final DatabaseObject object;
        final int status;
        final String kind;
        final int type;
        final String document;
        final String contact;
        final Date date;
        final long days;
        final BigDecimal amount;

        Row(DatabaseObject object, int status, String kind, int type, String document, String contact, Date date, long days, BigDecimal amount) {
            this.object = object;
            this.status = status;
            this.kind = kind;
            this.type = type;
            this.document = document;
            this.contact = contact;
            this.date = date;
            this.days = days;
            this.amount = amount;
        }
    }

    private final List<Row> rows = new ArrayList<Row>();
    private final List<Row> shown = new ArrayList<Row>();
    private final List<Schedule> recurring = new ArrayList<Schedule>();
    private int loadRun;

    //header and tiles
    private final JLabel dateLabel = new JLabel();
    private final Tile overdueTile = new Tile(Messages.DASH_OVERDUE.toString(), new Color(185, 28, 28));
    private final Tile warningTile = new Tile(Messages.DASH_WARNING.toString(), new Color(180, 120, 0));
    private final Tile invoicesTile = new Tile(Messages.DASH_OPEN_INVOICES.toString(), null);
    private final Tile offersTile = new Tile(Messages.DASH_OPEN_OFFERS.toString(), null);
    private final Tile ordersTile = new Tile(Messages.DASH_OPEN_ORDERS.toString(), null);
    //open documents
    private final ChipToggle chipAll = new ChipToggle(Messages.DASH_ALL.toString());
    private final ChipToggle chipOverdue = new ChipToggle(Messages.DASH_OVERDUE.toString());
    private final ChipToggle chipWarning = new ChipToggle(Messages.DASH_WARNING.toString());
    private final ChipToggle chipOpen = new ChipToggle(Messages.DASH_OPEN.toString());
    private final JComboBox<String> kind = new JComboBox<String>();
    private final JTextField search = ModernTheme.roomy(new JTextField());
    private final OpenModel openModel = new OpenModel();
    private final JTable openTable = new JTable(openModel);
    private final JLabel openEmpty = new JLabel();
    private JPanel tableCards;
    private JScrollPane recurringScroll;
    private JPanel recurringButtons;
    //the main action of the dashboard: blue
    private final JButton openDoc = new mpv5.ui.modern.PrimaryButton(Messages.DASH_OPEN_DOC.toString(), null);
    private final JButton reminder = new JButton(Messages.DASH_REMINDER.toString());
    //recurring invoices
    private final RecurringModel recurringModel = new RecurringModel();
    private final JTable recurringTable = new JTable(recurringModel);
    private final JLabel recurringEmpty = new JLabel();
    private final JButton createBill = new JButton(Messages.DASH_CREATE_BILL.toString());
    private final JButton deleteSchedule = new JButton(Messages.DASH_DELETE_SCHEDULE.toString());

    public static HomeScreen getInstance() {
        if (icke == null) {
            icke = new HomeScreen();
        }
        icke.refresh();
        return icke;
    }

    private HomeScreen() {
        super(new BorderLayout(ModernTheme.px(12), ModernTheme.px(12)));
        setName("homescreenpanel");
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(12, 14));
        add(header(), BorderLayout.NORTH);
        add(openCard(), BorderLayout.CENTER);
        add(rightColumn(), BorderLayout.EAST);
    }

    // ------------------------------------------------------------------ layout
    private JComponent header() {
        JLabel title = new JLabel(Messages.HOMESCREEN.toString());
        title.setFont(ModernTheme.font(9, Font.BOLD));
        dateLabel.setForeground(ModernTheme.muted());
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(title);
        left.add(dateLabel);

        mpv5.ui.modern.PrimaryButton wizardBtn = new mpv5.ui.modern.PrimaryButton(Messages.BELEG_WIZARD_TITLE.toString(), null);
        wizardBtn.setToolTipText(Messages.BELEG_WIZARD_TIP.toString());
        wizardBtn.addActionListener(e -> mpv5.ui.dialogs.belegwizard.BelegWizard.showWizard());
        JButton reload = new JButton(Messages.DASH_REFRESH.toString());
        reload.addActionListener(e -> refresh());
        JButton settings = new JButton(Messages.DASH_SETTINGS.toString());
        settings.setToolTipText(Messages.SETTINGS_D_SCHEDULE.toString());
        settings.addActionListener(e -> openSettings());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(wizardBtn);
        buttons.add(reload);
        buttons.add(settings);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(buttons, BorderLayout.EAST);

        JPanel tiles = new JPanel(new GridLayout(1, 0, ModernTheme.px(10), 0));
        tiles.setOpaque(false);
        tiles.add(overdueTile);
        tiles.add(warningTile);
        tiles.add(invoicesTile);
        tiles.add(offersTile);
        tiles.add(ordersTile);
        overdueTile.onClick(() -> filter(chipOverdue, -1));
        warningTile.onClick(() -> filter(chipWarning, -1));
        invoicesTile.onClick(() -> filter(chipAll, Item.TYPE_INVOICE));
        offersTile.onClick(() -> filter(chipAll, Item.TYPE_OFFER));
        ordersTile.onClick(() -> filter(chipAll, Item.TYPE_ORDER));

        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        p.setOpaque(false);
        p.add(top, BorderLayout.NORTH);
        p.add(tiles, BorderLayout.CENTER);
        return p;
    }

    private JComponent openCard() {
        ButtonGroup g = new ButtonGroup();
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        chips.setOpaque(false);
        for (ChipToggle c : new ChipToggle[]{chipAll, chipOverdue, chipWarning, chipOpen}) {
            g.add(c);
            chips.add(c);
            c.addActionListener(e -> applyFilter());
        }
        chipAll.setSelected(true);
        kind.addActionListener(e -> applyFilter());
        search.putClientProperty("JTextField.placeholderText", Messages.DASH_SEARCH.toString());
        search.putClientProperty("JTextField.showClearButton", true);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        search.setPreferredSize(new Dimension(ModernTheme.px(220), search.getPreferredSize().height));
        JPanel filters = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        filters.setOpaque(false);
        filters.add(chips, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        right.setOpaque(false);
        right.add(kind);
        right.add(search);
        filters.add(right, BorderLayout.EAST);

        ModernTheme.style(openTable);
        openTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        openTable.setAutoCreateRowSorter(true);
        //for every column class: the table style sets its own number and date renderers
        OpenRenderer renderer = new OpenRenderer();
        for (Class<?> c : new Class<?>[]{Object.class, String.class, Integer.class, Long.class, Number.class, BigDecimal.class, Date.class}) {
            openTable.setDefaultRenderer(c, renderer);
        }
        openTable.getColumnModel().getColumn(0).setPreferredWidth(ModernTheme.px(110));
        openTable.getColumnModel().getColumn(1).setPreferredWidth(ModernTheme.px(200));
        openTable.getColumnModel().getColumn(2).setPreferredWidth(ModernTheme.px(200));
        openTable.getColumnModel().getColumn(3).setPreferredWidth(ModernTheme.px(90));
        openTable.getColumnModel().getColumn(4).setPreferredWidth(ModernTheme.px(80));
        openTable.getColumnModel().getColumn(5).setPreferredWidth(ModernTheme.px(110));
        openTable.getSelectionModel().addListSelectionListener(e -> updateButtons());
        openTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
        openTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "open");
        openTable.getActionMap().put("open", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openSelected();
            }
        });
        JScrollPane sp = new JScrollPane(openTable);
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        openEmpty.setHorizontalAlignment(SwingConstants.CENTER);
        openEmpty.setForeground(ModernTheme.muted());

        openDoc.addActionListener(e -> openSelected());
        reminder.addActionListener(e -> createReminder());
        reminder.setToolTipText(Messages.DASH_REMINDER_TIP.toString());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        buttons.add(openDoc);
        buttons.add(reminder);

        openEmpty.setFont(ModernTheme.font(1, Font.PLAIN));
        openEmpty.setBorder(ModernTheme.padding(30, 10));
        JPanel table = new JPanel(new java.awt.CardLayout());
        table.setOpaque(false);
        table.add(sp, "table");
        table.add(openEmpty, "empty");
        tableCards = table;
        table.setPreferredSize(new Dimension(ModernTheme.px(600), ModernTheme.px(320)));
        return new Card(Messages.DASH_LIST.toString())
                .addFull(filters)
                .addFull(table, 1)
                .addFull(buttons)
                .addFull(Applets.hint(Messages.DASH_LIST_HINT.toString()));
    }

    private JComponent rightColumn() {
        ScheduleCalendar sc = ScheduleCalendar.instanceOf();
        sc.setDate(new Date());
        JPanel calendar = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        calendar.setOpaque(false);
        calendar.add(sc, BorderLayout.NORTH);
        calendar.add(ScheduleDayEventsPanel.instanceOf(), BorderLayout.CENTER);
        Card calendarCard = new Card(Messages.DASH_CALENDAR.toString()).addFull(calendar, 1);

        ModernTheme.style(recurringTable);
        recurringTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recurringTable.getSelectionModel().addListSelectionListener(e -> updateButtons());
        recurringTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    createBill();
                }
            }
        });
        JScrollPane sp = new JScrollPane(recurringTable);
        recurringScroll = sp;
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        sp.setPreferredSize(new Dimension(ModernTheme.px(340), ModernTheme.px(110)));
        recurringEmpty.setForeground(ModernTheme.muted());
        createBill.addActionListener(e -> createBill());
        deleteSchedule.addActionListener(e -> deleteSchedule());
        JPanel buttons = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(2)));
        buttons.setOpaque(false);
        buttons.add(createBill);
        buttons.add(Applets.danger(deleteSchedule));
        recurringButtons = buttons;
        Card recurringCard = new Card(Messages.DASH_RECURRING.toString())
                .addFull(Applets.hint(Messages.DASH_RECURRING_HINT.toString()))
                .addFull(recurringEmpty)
                .addFull(sp)
                .addFull(buttons);

        //a column of fixed width: the calendar, below it the recurring invoices
        JPanel col = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
        col.setOpaque(false);
        col.add(calendarCard, BorderLayout.CENTER);
        col.add(recurringCard, BorderLayout.SOUTH);
        col.setPreferredSize(new Dimension(ModernTheme.px(420), ModernTheme.px(400)));
        return col;
    }

    // ------------------------------------------------------------------ data
    /**
     * Loads the open documents and the recurring invoices again, in the
     * background
     */
    public void refresh() {
        final int run = ++loadRun;
        dateLabel.setText(DateConverter.getDefDateString(new Date()) + " · " + Messages.DASH_LOADING);
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                //no date filter: all open documents; recurring invoices due until the end of
                //this month, also those of past months that were not created yet
                return new Object[]{rowsOf(Scheduler.getOverdueEvents(null)),
                    schedulesOf(Scheduler.getScheduledBills(dueUntilEndOfMonth()))};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                if (run != loadRun) {
                    return;
                }
                try {
                    Object[] r = get();
                    setRows((List<Row>) r[0]);
                    setRecurring((List<Schedule>) r[1]);
                } catch (Exception e) {
                    Log.Debug(HomeScreen.this, e);
                }
                dateLabel.setText(DateConverter.getDefDateString(new Date()));
            }
        }.execute();
    }

    /**
     * For {@link Scheduler}: the open documents (red, yellow, green)
     */
    public JTable getOverdue() {
        return openTable;
    }

    /**
     * For {@link Scheduler}: the recurring invoices
     */
    public JTable getNextEvents() {
        return recurringTable;
    }

    /**
     * Shows the result of {@link Scheduler} (from any thread)
     *
     * @param map color → objects
     * @param table {@link #getOverdue()} or {@link #getNextEvents()}
     */
    public <T extends DatabaseObject> void show(final HashMap<Color, List<? extends DatabaseObject>> map, final JTable table) {
        //the scheduler asks with its own time frame: load everything the dashboard shows instead
        SwingUtilities.invokeLater(this::refresh);
    }

    /**
     * @return from the beginning to the end of this month
     */
    static mpv5.utils.date.vTimeframe dueUntilEndOfMonth() {
        return new mpv5.utils.date.vTimeframe(new Date(0), DateConverter.getEndOfMonth(new Date()));
    }

    private static List<Schedule> schedulesOf(Map<Color, List<? extends DatabaseObject>> map) {
        List<Schedule> l = new ArrayList<Schedule>();
        for (List<? extends DatabaseObject> v : map.values()) {
            for (DatabaseObject o : v) {
                if (o instanceof Schedule) {
                    l.add((Schedule) o);
                }
            }
        }
        Collections.sort(l, (a, b) -> String.valueOf(a.__getNextdate()).compareTo(String.valueOf(b.__getNextdate())));
        return l;
    }

    /**
     * @return the rows, most urgent first (overdue, warning, open), then the
     * oldest
     */
    static List<Row> rowsOf(Map<Color, List<? extends DatabaseObject>> map) {
        List<Row> l = new ArrayList<Row>();
        Date today = new Date();
        Map<Integer, String> names = new HashMap<Integer, String>();
        for (Map.Entry<Color, List<? extends DatabaseObject>> e : map.entrySet()) {
            int status = Color.red.equals(e.getKey()) ? OVERDUE : Color.yellow.equals(e.getKey()) ? WARNING : OPEN;
            for (DatabaseObject o : e.getValue()) {
                try {
                    if (o instanceof Item) {
                        Item i = (Item) o;
                        String number = i.__getCnumber() == null || i.__getCnumber().isEmpty() ? i.__getCname() : i.__getCnumber();
                        l.add(new Row(o, status, Item.getTypeString(i.__getInttype()), i.__getInttype(),
                                Item.getTypeString(i.__getInttype()) + " " + number, contactName(i.__getContactsids(), names),
                                i.__getDateadded(), daysOpen(i.__getDateadded(), today), i.getGrossAmount()));
                    } else if (o instanceof Revenue) {
                        Revenue r = (Revenue) o;
                        l.add(new Row(o, status, Messages.TYPE_REVENUE.toString(), -1, Messages.TYPE_REVENUE + " " + r.__getCname(), "",
                                r.__getDateadded(), daysOpen(r.__getDateadded(), today), r.__getBrutvalue()));
                    }
                } catch (Exception ex) {
                    Log.Debug(HomeScreen.class, ex.getMessage());
                }
            }
        }
        return dedupe(l);
    }

    /**
     * The scheduler lists a document in every list it reaches (open, warning,
     * overdue): once, with its most urgent status
     */
    static List<Row> dedupe(List<Row> l) {
        sort(l);
        List<Row> out = new ArrayList<Row>();
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (Row r : l) {
            String key = r.object == null ? String.valueOf(System.identityHashCode(r))
                    : r.object.getDbIdentity() + "#" + r.object.__getIDS();
            if (seen.add(key)) {
                out.add(r);
            }
        }
        return out;
    }

    static void sort(List<Row> l) {
        Collections.sort(l, (a, b) -> a.status != b.status ? Integer.compare(a.status, b.status) : Long.compare(b.days, a.days));
    }

    /**
     * @return whole days from the date to today, 0 if none or in the future
     */
    static long daysOpen(Date from, Date today) {
        if (from == null || today == null || from.after(today)) {
            return 0;
        }
        return TimeUnit.MILLISECONDS.toDays(today.getTime() - from.getTime());
    }

    private static String contactName(int id, Map<Integer, String> names) {
        if (id <= 0) {
            return "";
        }
        String n = names.get(id);
        if (n == null) {
            try {
                Contact c = (Contact) DatabaseObject.getObject(mpv5.db.common.Context.getContact(), id);
                String company = c.__getCompany() == null ? "" : c.__getCompany().trim();
                n = company.isEmpty() ? ((c.__getPrename() == null ? "" : c.__getPrename() + " ") + c.__getCname()).trim() : company;
            } catch (Exception e) {
                n = "";
            }
            names.put(id, n);
        }
        return n;
    }

    private void setRows(List<Row> l) {
        rows.clear();
        rows.addAll(l);
        //the kinds that occur, for the choice
        String selected = (String) kind.getSelectedItem();
        List<String> kinds = new ArrayList<String>();
        for (Row r : rows) {
            if (!kinds.contains(r.kind)) {
                kinds.add(r.kind);
            }
        }
        Collections.sort(kinds);
        kinds.add(0, Messages.DASH_KIND_ALL.toString());
        kind.setModel(new javax.swing.DefaultComboBoxModel<String>(kinds.toArray(new String[0])));
        kind.setSelectedItem(selected != null && kinds.contains(selected) ? selected : kinds.get(0));
        updateTiles();
        applyFilter();
    }

    private void updateTiles() {
        int over = 0, warn = 0;
        BigDecimal overSum = BigDecimal.ZERO, warnSum = BigDecimal.ZERO, inv = BigDecimal.ZERO, off = BigDecimal.ZERO, ord = BigDecimal.ZERO;
        int invN = 0, offN = 0, ordN = 0;
        for (Row r : rows) {
            BigDecimal a = r.amount == null ? BigDecimal.ZERO : r.amount;
            if (r.status == OVERDUE) {
                over++;
                overSum = overSum.add(a);
            } else if (r.status == WARNING) {
                warn++;
                warnSum = warnSum.add(a);
            }
            if (r.type == Item.TYPE_INVOICE || r.type == Item.TYPE_PART_PAYMENT || r.type == Item.TYPE_DEPOSIT) {
                inv = inv.add(a);
                invN++;
            } else if (r.type == Item.TYPE_OFFER) {
                off = off.add(a);
                offN++;
            } else if (r.type == Item.TYPE_ORDER) {
                ord = ord.add(a);
                ordN++;
            }
        }
        overdueTile.set(over, overSum);
        warningTile.set(warn, warnSum);
        invoicesTile.set(invN, inv);
        offersTile.set(offN, off);
        ordersTile.set(ordN, ord);
    }

    /**
     * From a tile: the chip and the kind of document (-1: all kinds)
     */
    private void filter(ChipToggle chip, int type) {
        chip.setSelected(true);
        String k = type < 0 ? Messages.DASH_KIND_ALL.toString() : Item.getTypeString(type);
        boolean found = false;
        for (int i = 0; i < kind.getItemCount(); i++) {
            if (kind.getItemAt(i).equals(k)) {
                kind.setSelectedIndex(i);
                found = true;
            }
        }
        if (!found && kind.getItemCount() > 0) {
            kind.setSelectedIndex(0);
        }
        search.setText("");
        applyFilter();
    }

    private void applyFilter() {
        shown.clear();
        String k = (String) kind.getSelectedItem();
        boolean allKinds = k == null || k.equals(Messages.DASH_KIND_ALL.toString());
        String needle = search.getText();
        for (Row r : rows) {
            if (chipOverdue.isSelected() && r.status != OVERDUE
                    || chipWarning.isSelected() && r.status != WARNING
                    || chipOpen.isSelected() && r.status != OPEN) {
                continue;
            }
            if (!allKinds && !k.equals(r.kind)) {
                continue;
            }
            if (!SearchService.matches(r.document + " " + r.contact + " " + FormatNumber.formatLokalCurrency(r.amount), needle)) {
                continue;
            }
            shown.add(r);
        }
        openModel.fireTableDataChanged();
        boolean empty = shown.isEmpty();
        openEmpty.setText(rows.isEmpty() ? Messages.DASH_EMPTY.toString() : Messages.DASH_NOTHING_FOUND.toString());
        ((java.awt.CardLayout) tableCards.getLayout()).show(tableCards, empty ? "empty" : "table");
        updateButtons();
    }

    private void setRecurring(List<Schedule> l) {
        recurring.clear();
        recurring.addAll(l);
        recurringModel.fireTableDataChanged();
        recurringEmpty.setText(Messages.DASH_RECURRING_EMPTY.toString());
        recurringEmpty.setVisible(recurring.isEmpty());
        recurringScroll.setVisible(!recurring.isEmpty());
        recurringButtons.setVisible(!recurring.isEmpty());
        revalidate();
        updateButtons();
    }

    private Row selectedRow() {
        int v = openTable.getSelectedRow();
        return v < 0 ? null : shown.get(openTable.convertRowIndexToModel(v));
    }

    private Schedule selectedSchedule() {
        int v = recurringTable.getSelectedRow();
        return v < 0 ? null : recurring.get(recurringTable.convertRowIndexToModel(v));
    }

    private void updateButtons() {
        Row r = selectedRow();
        openDoc.setEnabled(r != null);
        reminder.setEnabled(r != null && r.status == OVERDUE
                && (r.type == Item.TYPE_INVOICE || r.type == Item.TYPE_PART_PAYMENT || r.type == Item.TYPE_DEPOSIT));
        Schedule s = selectedSchedule();
        createBill.setEnabled(s != null && !s.__getIsdone());
        deleteSchedule.setEnabled(s != null);
    }

    // ------------------------------------------------------------------ actions
    private void openSelected() {
        Row r = selectedRow();
        if (r != null) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(r.object);
        }
    }

    private void createReminder() {
        Row r = selectedRow();
        if (r == null || !(r.object instanceof Item)) {
            return;
        }
        if (Popup.Y_N_dialog(String.format(Messages.DASH_REMINDER_CONFIRM.toString(), r.document))) {
            Reminder rem = new Reminder();
            rem.setCname(Messages.REMINDER.toString() + "@" + r.object.__getCname());
            rem.setDateadded(new Date());
            rem.setDescription("");
            rem.setGroupsids(r.object.__getGroupsids());
            rem.setIntaddedby(User.getCurrentUser().getID());
            rem.setItemsids(r.object.__getIDS());
            if (rem.save()) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.REMINDER + ": " + r.document);
            }
        }
    }

    private void openSettings() {
        for (MPControlPanel.Entry e : MPControlPanel.entries()) {
            if (e.panelClass == mpv5.ui.dialogs.subcomponents.ControlPanel_Schedule.class) {
                MPControlPanel.show(e);
                return;
            }
        }
    }

    private void deleteSchedule() {
        Schedule s = selectedSchedule();
        if (s != null && Popup.Y_N_dialog(String.format(Messages.DASH_DELETE_SCHEDULE_CONFIRM.toString(), scheduleName(s)))) {
            s.delete();
            refresh();
        }
    }

    private static String scheduleName(Schedule s) {
        try {
            Item i = s.getItem();
            return Item.getTypeString(i.__getInttype()) + " " + (i.__getCnumber() == null || i.__getCnumber().isEmpty() ? i.__getCname() : i.__getCnumber());
        } catch (Exception e) {
            return String.valueOf(s);
        }
    }

    /**
     * A new invoice from a recurring one; the next date is planned
     */
    private void createBill() {
        Schedule schedule = selectedSchedule();
        if (schedule == null || schedule.__getIsdone()
                || !Popup.Y_N_dialog(String.format(Messages.DASH_CREATE_BILL_CONFIRM.toString(), scheduleName(schedule)))) {
            return;
        }
        try {
            Item item = schedule.getItem();
            SubItem[] subs = item.getSubitems();
            item.setIDS(-1);
            item.setDateadded(new Date());
            //the service date (Leistungsdatum): the date of the new invoice
            item.setDatetodo(null);
            item.setDatelastupdated(null);
            try {
                item.setDateend(DateConverter.addDays(new Date(), Integer.valueOf(User.getCurrentUser().getProperties().getProperty("bills.alert.days"))));
            } catch (Exception e) {
                item.setDateend(DateConverter.addDays(new Date(), 30));
            }
            item.setIntreminders(0);
            item.setIntstatus(Item.STATUS_IN_PROGRESS);
            item.setDescription(item.__getDescription() + "\n" + Messages.SCHEDULE_GENERATED + "\n" + item.__getCname());
            if (!item.save()) {
                return;
            }
            for (SubItem subItem : subs) {
                subItem.setItemsids(item.__getIDS());
                subItem.setIDS(-1);
                subItem.save(true);
            }
            Date olddate = schedule.__getNextdate();
            schedule.setNextdate(DateConverter.addMonths(schedule.__getNextdate(), schedule.__getIntervalmonth()));
            if (schedule.__getNextdate().before(schedule.__getStopdate())) {
                Schedule s2 = (Schedule) schedule.clone();
                s2.setIsdone(false);
                s2.setIDS(-1);
                s2.save(true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.SCHEDULE_ITEM_REMOVED + " " + schedule);
            }
            schedule.setNextdate(olddate);
            schedule.setIsdone(true);
            schedule.save(true);
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(item);
            refresh();
        } catch (Exception ex) {
            Log.Debug(this, ex);
            Popup.error(ex);
        }
    }

    // ------------------------------------------------------------------ parts
    /**
     * A tile: title, count and sum; a click filters the list
     */
    private static final class Tile extends JPanel {

        private static final long serialVersionUID = 1L;
        private final JLabel value = new JLabel("–");
        private final JLabel detail = new JLabel(" ");
        private Runnable click;

        Tile(String title, Color color) {
            super(new BorderLayout(0, ModernTheme.px(2)));
            setBackground(ModernTheme.cardBackground());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, ModernTheme.px(4), 0, 0, color == null ? ModernTheme.accent() : color),
                    BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, ModernTheme.border()),
                            ModernTheme.padding(8, 12))));
            JLabel t = new JLabel(title);
            t.setForeground(ModernTheme.muted());
            t.setFont(ModernTheme.font(-1, Font.BOLD));
            value.setFont(ModernTheme.font(8, Font.BOLD));
            if (color != null) {
                value.setForeground(color);
            }
            detail.setForeground(ModernTheme.muted());
            add(t, BorderLayout.NORTH);
            add(value, BorderLayout.CENTER);
            add(detail, BorderLayout.SOUTH);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (click != null) {
                        click.run();
                    }
                }
            });
        }

        void onClick(Runnable r) {
            click = r;
            setToolTipText(Messages.DASH_TILE_TIP.toString());
        }

        void set(int count, BigDecimal sum) {
            value.setText(FormatNumber.formatLokalCurrency(sum));
            detail.setText(String.format(Messages.DASH_COUNT.toString(), count));
        }
    }

    private final class OpenModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        private final String[] cols = {Messages.DASH_COL_STATUS.toString(), Messages.DASH_COL_DOCUMENT.toString(),
            Messages.DASH_COL_CONTACT.toString(), Messages.DASH_COL_DATE.toString(), Messages.DASH_COL_DAYS.toString(),
            Messages.DASH_COL_AMOUNT.toString()};

        @Override
        public int getRowCount() {
            return shown.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 3 ? Date.class : c == 4 ? Long.class : c == 5 ? BigDecimal.class : c == 0 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int c) {
            Row r = shown.get(row);
            switch (c) {
                case 0:
                    return r.status;
                case 1:
                    return r.document;
                case 2:
                    return r.contact;
                case 3:
                    return r.date;
                case 4:
                    return r.days;
                default:
                    return r.amount;
            }
        }
    }

    private final class RecurringModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        private final String[] cols = {Messages.DASH_COL_DOCUMENT.toString(), Messages.DASH_COL_NEXT.toString(),
            Messages.DASH_COL_EVERY.toString()};

        @Override
        public int getRowCount() {
            return recurring.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int c) {
            return cols[c];
        }

        @Override
        public Object getValueAt(int row, int c) {
            Schedule s = recurring.get(row);
            switch (c) {
                case 0:
                    return scheduleName(s);
                case 1:
                    return s.__getNextdate() == null ? "" : DateConverter.getDefDateString(s.__getNextdate());
                default:
                    return String.format(Messages.DASH_EVERY_MONTHS.toString(), s.__getIntervalmonth());
            }
        }
    }

    /**
     * Status as a colored word, dates and amounts formatted
     */
    private static final class OpenRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            Object v = value;
            int col = table.convertColumnIndexToModel(column);
            if (col == 0 && value instanceof Integer) {
                int s = (Integer) value;
                v = "● " + (s == OVERDUE ? Messages.DASH_OVERDUE : s == WARNING ? Messages.DASH_WARNING : Messages.DASH_OPEN);
            } else if (value instanceof Date) {
                v = DateConverter.getDefDateString((Date) value);
            } else if (value instanceof BigDecimal) {
                v = FormatNumber.formatLokalCurrency((BigDecimal) value);
            }
            Component c = super.getTableCellRendererComponent(table, v, selected, focus, row, column);
            setHorizontalAlignment(col >= 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (col == 0 && !selected && value instanceof Integer) {
                int s = (Integer) value;
                c.setForeground(s == OVERDUE ? new Color(185, 28, 28) : s == WARNING ? new Color(180, 120, 0) : new Color(21, 128, 61));
            } else if (!selected) {
                c.setForeground(table.getForeground());
            }
            return c;
        }
    }
}
