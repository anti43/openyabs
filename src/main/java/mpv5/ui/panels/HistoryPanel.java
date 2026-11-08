/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.handler.SearchService;
import mpv5.logging.Log;
import mpv5.ui.beans.PrinitingComboBox;
import mpv5.ui.beans.TimeframeChooser;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.RichList;
import mpv5.ui.misc.RichList.Row;
import mpv5.ui.modern.ModernTheme;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPTableModel;

/**
 * The history of all records: who changed what when, grouped by day,
 * searchable and filterable; a click opens the record
 */
public class HistoryPanel extends JPanel implements ListPanel {

    private static HistoryPanel t;
    private static final long serialVersionUID = 1L;

    public static Component instanceOf() {
        if (t == null) {
            t = new HistoryPanel();
        }
        return t;
    }

    /**
     * One entry of the history
     */
    static final class Event {

        int id;
        String message;
        String user;
        String group;
        Context context;
        int item;
        Date date;
        char action;
        String name;
    }

    private final JTextField search = ModernTheme.roomy(new JTextField());
    private final JComboBox<Object> users = new JComboBox<Object>();
    private final JComboBox<Object> groups = new JComboBox<Object>();
    private final TimeframeChooser timeframe = new TimeframeChooser();
    private final DefaultListModel<Object> model = new DefaultListModel<Object>();
    private final JList<Object> list = new JList<Object>(model);
    private final JLabel status = new JLabel();
    private final JTable table = new JTable();
    private final PrinitingComboBox export = new PrinitingComboBox();
    private List<Event> events = new ArrayList<Event>();
    private String kind = null;
    private int loads;

    /**
     * Creates the history
     */
    public HistoryPanel() {
        super(new BorderLayout());
        setName("historypanel");
        setBackground(ModernTheme.background());

        JPanel head = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        head.setBackground(ModernTheme.cardBackground());
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()), ModernTheme.padding(12, 16)));

        JPanel titleRow = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        titleRow.setOpaque(false);
        JLabel title = new JLabel(Messages.HISTORY_TITLE.toString());
        title.setFont(ModernTheme.font(6, Font.BOLD));
        titleRow.add(title, BorderLayout.WEST);
        search.putClientProperty("JTextField.placeholderText", Messages.HISTORY_SEARCH.toString());
        search.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        titleRow.add(search, BorderLayout.CENTER);
        JPanel exportRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        exportRow.setOpaque(false);
        exportRow.add(export);
        titleRow.add(exportRow, BorderLayout.EAST);
        head.add(titleRow, BorderLayout.NORTH);

        JPanel filters = new JPanel(new mpv5.ui.modern.WrapLayout(FlowLayout.LEFT, ModernTheme.px(6), ModernTheme.px(4)));
        filters.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        Object[][] chips = {{Messages.ACTIVITY_ALL, null}, {Messages.HISTORY_KIND_NEW, "+"}, {Messages.HISTORY_KIND_CHANGED, "~"},
        {Messages.HISTORY_KIND_DELETED, "-"}, {Messages.SEARCH_KIND_CONTACTS, "contacts"}, {Messages.SEARCH_KIND_DOCUMENTS, "items"},
        {Messages.SEARCH_KIND_PRODUCTS, "products"}};
        for (Object[] c : chips) {
            final String k = (String) c[1];
            JToggleButton b = new JToggleButton(c[0].toString(), k == null);
            b.putClientProperty("JButton.buttonType", "roundRect");
            b.setFocusable(false);
            b.addActionListener(e -> {
                kind = k;
                showRows();
            });
            g.add(b);
            filters.add(b);
        }
        filters.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(12)));
        filters.add(new JLabel(Messages.HISTORY_USER.toString()));
        filters.add(users);
        filters.add(new JLabel(Messages.HISTORY_GROUP.toString()));
        filters.add(groups);
        filters.add(new JLabel(Messages.HISTORY_PERIOD.toString()));
        filters.add(timeframe);
        JButton load = new JButton(Messages.HISTORY_LOAD.toString());
        load.addActionListener(e -> refresh());
        filters.add(load);
        head.add(filters, BorderLayout.CENTER);
        add(head, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new RichList.Renderer());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openSelected();
                }
            }
        });
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        add(sp, BorderLayout.CENTER);
        status.setBorder(ModernTheme.padding(6, 16));
        status.setForeground(ModernTheme.muted());
        add(status, BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showRows();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showRows();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showRows();
            }
        });

        timeframe.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));
        export.init(table);
        try {
            List<Object> u = new ArrayList<Object>();
            u.add(User.DEFAULT);
            u.addAll(DatabaseObject.getObjects(Context.getUser()));
            users.setModel(new DefaultComboBoxModel<Object>(u.toArray()));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
        try {
            List<Object> gr = new ArrayList<Object>();
            gr.add(new Group(""));
            gr.addAll(DatabaseObject.getObjects(Context.getGroup()));
            groups.setModel(new DefaultComboBoxModel<Object>(gr.toArray()));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
        users.addActionListener(e -> refresh());
        groups.addActionListener(e -> refresh());
        refresh();
    }

    @Override
    public void refresh() {
        final User forUser = users.getSelectedIndex() > 0 ? (User) users.getSelectedItem() : null;
        final Group forGroup = groups.getSelectedIndex() > 0 ? (Group) groups.getSelectedItem() : null;
        final vTimeframe time = timeframe.getTime();
        final int load = ++loads;
        status.setText(Messages.HISTORY_LOADING.toString());
        new SwingWorker<List<Event>, Object>() {
            @Override
            protected List<Event> doInBackground() throws Exception {
                return load(forUser, forGroup, time);
            }

            @Override
            protected void done() {
                if (load != loads) {
                    return;
                }
                try {
                    events = get();
                } catch (Exception e) {
                    Log.Debug(HistoryPanel.this, e);
                    events = new ArrayList<Event>();
                }
                showRows();
            }
        }.execute();
    }

    private static List<Event> load(User forUser, Group forGroup, vTimeframe time) {
        List<Event> l = new ArrayList<Event>();
        String h = Context.getHistory().getDbIdentity() + ".";
        QueryCriteria2 dh = new QueryCriteria2();
        try {
            if (forUser != null && !forUser.equals(User.DEFAULT)) {
                dh.and(new QueryParameter(Context.getHistory(), "username", forUser.getName(), QueryParameter.EQUALS));
            }
            if (forGroup != null && !forGroup.__getCname().equals("")) {
                dh.and(new QueryParameter(Context.getHistory(), "groupsids", forGroup.__getIDS(), QueryParameter.EQUALS));
            }
            Context c = Context.getHistory();
            c.addReference(Context.getGroup());
            Object[][] d = QueryHandler.instanceOf().clone(c).select(
                    h + "ids, " + h + "cname, " + h + "username, " + Context.getGroup().getDbIdentity() + ".cname, "
                    + h + "dbidentity, " + h + "intitem, " + h + "dateadded", dh, time).getData();
            for (Object[] r : d) {
                Event e = new Event();
                e.id = num(r[0]);
                e.message = s(r[1]);
                e.user = s(r[2]);
                e.group = s(r[3]);
                String db = s(r[4]);
                e.context = db.isEmpty() ? null : Context.getMatchingContext(db);
                e.item = num(r[5]);
                try {
                    e.date = DateConverter.getDate(r[6]);
                } catch (Exception ex) {
                    e.date = null;
                }
                classify(e);
                l.add(e);
            }
        } catch (NodataFoundException ex) {
            Log.Debug(HistoryPanel.class, ex.getMessage());
        }
        //newest first
        l.sort((a, b) -> {
            int c = b.date == null || a.date == null ? 0 : b.date.compareTo(a.date);
            return c != 0 ? c : Integer.compare(b.id, a.id);
        });
        return l;
    }

    /**
     * What happened, from the stored message (in any language)
     */
    private static void classify(Event e) {
        String m = e.message;
        String low = m.toLowerCase(Locale.ROOT);
        String[][] actions = {
            {"+", Messages.INSERTED.toString(), ": created", ": erzeugt"},
            {"-", Messages.TRASHED.toString(), Messages.DELETED.toString(), ": moved to trashbin", ": removed from database",
                ": verschieben in mülleimer", ": aus der datenbank entfernt"},
            {"r", Messages.RESTORED.toString(), Messages.UNTRASHED.toString(), " restored", ": wieder hergestellt", ": moved out of trashbin"},
            {"~", Messages.UPDATED.toString(), ": updated", ": aktualisierung"}};
        e.action = 'i';
        e.name = m;
        for (String[] a : actions) {
            for (int i = 1; i < a.length; i++) {
                String suffix = a[i].toLowerCase(Locale.ROOT);
                if (!suffix.trim().isEmpty() && low.endsWith(suffix)) {
                    e.action = a[0].charAt(0);
                    e.name = m.substring(0, m.length() - suffix.length()).trim();
                    return;
                }
            }
        }
    }

    private void showRows() {
        String n = search.getText().trim();
        Object sel = list.getSelectedValue();
        model.clear();
        DateFormat day = DateFormat.getDateInstance(DateFormat.FULL);
        String lastDay = null;
        int shown = 0;
        List<Object[]> rows = new ArrayList<Object[]>();
        for (Event e : events) {
            if (kind != null) {
                if (kind.length() == 1) {
                    char k = kind.charAt(0);
                    if (!(e.action == k || (k == '~' && e.action == 'r'))) {
                        continue;
                    }
                } else if (e.context == null || !e.context.getDbIdentity().equalsIgnoreCase(kind)) {
                    continue;
                }
            }
            String type = RichList.typeName(e.context);
            if (!n.isEmpty() && !SearchService.matches(e.message + " " + e.user + " " + e.group + " " + type, n)) {
                continue;
            }
            String d = e.date == null ? "" : dayName(e.date, day);
            if (!d.equals(lastDay)) {
                lastDay = d;
                model.addElement(d);
            }
            Row r = new Row();
            r.data = e;
            switch (e.action) {
                case '+':
                    r.badge = "+";
                    r.color = RichList.GREEN;
                    break;
                case '-':
                    r.badge = "−";
                    r.color = RichList.RED;
                    break;
                case 'r':
                    r.badge = "↺";
                    r.color = RichList.PURPLE;
                    break;
                case '~':
                    r.badge = "✎";
                    r.color = ModernTheme.accent();
                    break;
                default:
                    r.badge = "i";
                    r.color = ModernTheme.muted();
            }
            r.title = e.name;
            r.strong = e.context != null && e.item > 0 && e.action != '-';
            String who = e.user.isEmpty() ? "" : Messages.HISTORY_BY + " " + e.user;
            r.detail = join(type, actionName(e.action), who);
            r.right = e.item > 0 ? "#" + e.item : "";
            model.addElement(r);
            rows.add(new Object[]{e.id, e.message, e.user, e.group, e.date});
            shown++;
        }
        if (model.isEmpty()) {
            model.addElement(Messages.HISTORY_EMPTY.toString());
        }
        if (sel != null && model.contains(sel)) {
            list.setSelectedValue(sel, true);
        }
        table.setModel(new MPTableModel(rows.toArray(new Object[0][]), Headers.HISTORY.getValue(),
                new Class<?>[]{String.class, String.class, String.class, String.class, Date.class}));
        status.setText(String.format(Messages.HISTORY_COUNT.toString(), shown) + "  ·  " + Messages.HISTORY_TIP);
    }

    private static String actionName(char a) {
        switch (a) {
            case '+':
                return Messages.ACTIVITY_CREATED.toString();
            case '-':
                return Messages.ACTIVITY_DELETED.toString();
            case 'r':
                return Messages.HISTORY_RESTORED.toString();
            case '~':
                return Messages.ACTIVITY_CHANGED.toString();
            default:
                return "";
        }
    }

    private static String dayName(Date d, DateFormat f) {
        Calendar c = Calendar.getInstance();
        Calendar x = Calendar.getInstance();
        x.setTime(d);
        if (c.get(Calendar.YEAR) == x.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == x.get(Calendar.DAY_OF_YEAR)) {
            return Messages.HISTORY_TODAY.toString();
        }
        c.add(Calendar.DAY_OF_YEAR, -1);
        if (c.get(Calendar.YEAR) == x.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == x.get(Calendar.DAY_OF_YEAR)) {
            return Messages.HISTORY_YESTERDAY.toString();
        }
        return f.format(d);
    }

    private void openSelected() {
        Object o = list.getSelectedValue();
        if (!(o instanceof Row) || !(((Row) o).data instanceof Event)) {
            return;
        }
        Event e = (Event) ((Row) o).data;
        if (e.context == null || e.item <= 0) {
            return;
        }
        try {
            DatabaseObject dbo = DatabaseObject.getObject(e.context, e.item, true);
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(dbo);
        } catch (Exception ex) {
            Log.Debug(this, ex);
            Popup.notice(Messages.HISTORY_GONE + ": " + e.name);
        }
    }

    private static String join(String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                if (b.length() > 0) {
                    b.append("  ·  ");
                }
                b.append(p.trim());
            }
        }
        return b.toString();
    }

    private static String s(Object o) {
        return o == null || "null".equals(String.valueOf(o)) ? "" : String.valueOf(o).trim();
    }

    private static int num(Object o) {
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void flush() {
        events = new ArrayList<Event>();
        showRows();
    }
}
