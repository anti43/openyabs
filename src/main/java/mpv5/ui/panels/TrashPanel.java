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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.SearchService;
import mpv5.handler.TrashHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.RichList;
import mpv5.ui.misc.RichList.Row;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * The trash: deleted records, searchable; restore them or delete them for
 * good
 */
public class TrashPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static TrashPanel ident;

    public static TrashPanel instanceOf() {
        if (ident == null) {
            ident = new TrashPanel();
        }
        ident.setData();
        return ident;
    }

    /**
     * A record in the trash
     */
    static final class Trashed {

        String type;
        int id;
        String name;
        Context context;
    }

    private final JTextField search = ModernTheme.roomy(new JTextField());
    private final DefaultListModel<Object> model = new DefaultListModel<Object>();
    private final JList<Object> list = new JList<Object>(model);
    private final JLabel status = new JLabel();
    private final PrimaryButton restore = new PrimaryButton(Messages.TRASH_RESTORE.toString(), null);
    private final JButton delete = new JButton(Messages.TRASH_DELETE.toString());
    private final JButton empty = new JButton(Messages.TRASH_EMPTY.toString());
    private List<Trashed> items = new ArrayList<Trashed>();
    private String kind;
    private int loads;

    private TrashPanel() {
        super(new BorderLayout());
        setName("trashpanel");
        setBackground(ModernTheme.background());

        JPanel head = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        head.setBackground(ModernTheme.cardBackground());
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()), ModernTheme.padding(12, 16)));
        JPanel titleRow = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        titleRow.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        JLabel title = new JLabel(Messages.TRASH_TITLE.toString());
        title.setFont(ModernTheme.font(6, Font.BOLD));
        titles.add(title, BorderLayout.NORTH);
        JLabel hint = new JLabel(Messages.TRASH_HINT.toString());
        hint.setForeground(ModernTheme.muted());
        titles.add(hint, BorderLayout.SOUTH);
        titleRow.add(titles, BorderLayout.WEST);
        search.putClientProperty("JTextField.placeholderText", Messages.TRASH_SEARCH.toString());
        search.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search, BorderLayout.SOUTH);
        titleRow.add(searchWrap, BorderLayout.CENTER);
        head.add(titleRow, BorderLayout.NORTH);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        filters.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        Object[][] chips = {{Messages.ACTIVITY_ALL, null}, {Messages.SEARCH_KIND_CONTACTS, Context.getContact().getDbIdentity()},
        {Messages.SEARCH_KIND_DOCUMENTS, Context.getInvoice().getDbIdentity()}, {Messages.SEARCH_KIND_PRODUCTS, Context.getProduct().getDbIdentity()}};
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
        head.add(filters, BorderLayout.CENTER);
        add(head, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new RichList.Renderer());
        list.addListSelectionListener(e -> buttons());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
                    open();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                popup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                popup(e);
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    open();
                } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelected();
                }
            }
        });
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        add(sp, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(ModernTheme.cardBackground());
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()), ModernTheme.padding(8, 16)));
        status.setForeground(ModernTheme.muted());
        foot.add(status, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        actions.setOpaque(false);
        empty.setForeground(RichList.RED);
        delete.setForeground(RichList.RED);
        empty.addActionListener(e -> emptyTrash());
        delete.addActionListener(e -> deleteSelected());
        restore.addActionListener(e -> restoreSelected());
        actions.add(empty);
        actions.add(delete);
        actions.add(restore);
        foot.add(actions, BorderLayout.EAST);
        add(foot, BorderLayout.SOUTH);

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
        buttons();
    }

    /**
     * Loads the trash again
     */
    public void setData() {
        final int load = ++loads;
        new SwingWorker<List<Trashed>, Object>() {
            @Override
            protected List<Trashed> doInBackground() {
                List<Trashed> l = new ArrayList<Trashed>();
                for (Object[] r : TrashHandler.getData()) {
                    Trashed t = new Trashed();
                    t.type = String.valueOf(r[0]);
                    try {
                        t.id = Integer.parseInt(String.valueOf(r[1]).trim());
                    } catch (Exception e) {
                        continue;
                    }
                    t.name = r[2] == null ? "" : String.valueOf(r[2]);
                    t.context = Context.getMatchingContext(t.type);
                    l.add(t);
                }
                return l;
            }

            @Override
            protected void done() {
                if (load != loads) {
                    return;
                }
                try {
                    items = get();
                } catch (Exception e) {
                    Log.Debug(TrashPanel.this, e);
                    items = new ArrayList<Trashed>();
                }
                showRows();
            }
        }.execute();
    }

    private void showRows() {
        String n = search.getText().trim();
        model.clear();
        String group = null;
        List<Trashed> sorted = new ArrayList<Trashed>(items);
        sorted.sort((a, b) -> {
            int c = RichList.typeName(a.context).compareToIgnoreCase(RichList.typeName(b.context));
            return c != 0 ? c : a.name.compareToIgnoreCase(b.name);
        });
        int shown = 0;
        for (Trashed t : sorted) {
            if (kind != null && (t.context == null || !t.context.getDbIdentity().equalsIgnoreCase(kind))) {
                continue;
            }
            String type = RichList.typeName(t.context);
            if (!n.isEmpty() && !SearchService.matches(t.name + " " + type + " " + t.id, n)) {
                continue;
            }
            if (!type.equals(group)) {
                group = type;
                model.addElement(type);
            }
            Row r = new Row();
            Object[] b = RichList.typeBadge(t.context);
            r.badge = (String) b[0];
            r.color = (Color) b[1];
            r.title = t.name.isEmpty() ? "#" + t.id : t.name;
            r.strong = true;
            r.detail = type;
            r.right = "#" + t.id;
            r.data = t;
            model.addElement(r);
            shown++;
        }
        if (model.isEmpty()) {
            model.addElement(Messages.TRASH_NOTHING.toString());
        }
        status.setText(items.isEmpty() ? Messages.TRASH_NOTHING.toString()
                : String.format(Messages.TRASH_COUNT.toString(), shown, items.size()) + "  ·  " + Messages.TRASH_TIP);
        empty.setEnabled(!items.isEmpty());
        buttons();
    }

    private void buttons() {
        boolean any = !selected().isEmpty();
        restore.setEnabled(any);
        delete.setEnabled(any);
    }

    private List<Trashed> selected() {
        List<Trashed> l = new ArrayList<Trashed>();
        for (Object o : list.getSelectedValuesList()) {
            if (o instanceof Row && ((Row) o).data instanceof Trashed) {
                l.add((Trashed) ((Row) o).data);
            }
        }
        return l;
    }

    private void restoreSelected() {
        List<Trashed> l = selected();
        for (Trashed t : l) {
            try {
                TrashHandler.restore(t.type, t.id, t.name + Messages.RESTORED);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        if (!l.isEmpty()) {
            mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.TRASH_RESTORED.toString(), l.size()));
            setData();
        }
    }

    private void deleteSelected() {
        List<Trashed> l = selected();
        if (l.isEmpty()) {
            return;
        }
        String what = l.size() == 1 ? l.get(0).name : String.valueOf(l.size());
        if (!Popup.Y_N_dialog(String.format(Messages.TRASH_REALLY_DELETE.toString(), what), Messages.TRASH_DELETE.toString())) {
            return;
        }
        for (Trashed t : l) {
            try {
                TrashHandler.delete(t.type, t.id, t.name + Messages.DELETED);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        setData();
    }

    private void emptyTrash() {
        if (items.isEmpty() || !Popup.Y_N_dialog(String.format(Messages.TRASH_REALLY_EMPTY.toString(), items.size()), Messages.TRASH_EMPTY.toString())) {
            return;
        }
        for (Trashed t : new ArrayList<Trashed>(items)) {
            try {
                TrashHandler.delete(t.type, t.id, t.name + Messages.DELETED);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        setData();
    }

    private void open() {
        List<Trashed> l = selected();
        if (l.size() != 1 || l.get(0).context == null) {
            return;
        }
        try {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(l.get(0).context, l.get(0).id, true));
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.notice(Messages.NOT_POSSIBLE + ": " + l.get(0).name);
        }
    }

    private void popup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int i = list.locationToIndex(e.getPoint());
        if (i >= 0 && !list.isSelectedIndex(i)) {
            list.setSelectedIndex(i);
        }
        if (selected().isEmpty()) {
            return;
        }
        JPopupMenu m = new JPopupMenu();
        JMenuItem r = new JMenuItem(Messages.TRASH_RESTORE.toString());
        r.addActionListener(a -> restoreSelected());
        m.add(r);
        JMenuItem o = new JMenuItem(Messages.ACTIVITY_OPEN.toString());
        o.setEnabled(selected().size() == 1);
        o.addActionListener(a -> open());
        m.add(o);
        m.addSeparator();
        JMenuItem d = new JMenuItem(Messages.TRASH_DELETE.toString());
        d.setForeground(RichList.RED);
        d.addActionListener(a -> deleteSelected());
        m.add(d);
        m.show(list, e.getX(), e.getY());
    }
}
