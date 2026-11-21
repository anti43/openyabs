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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.data.MPList;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Favourite;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;
import mpv5.utils.xml.XMLReader;
import mpv5.utils.xml.XMLWriter;

/**
 * The "Ablage": the favourites (kept) and the clipboard (records copied with
 * Ctrl+C or "Copy", until YaBS is closed) in one list. The star of a row
 * makes it a favourite or not; a filter searches both. Open (double click,
 * Enter), paste the selection or the whole clipboard into the current view,
 * remove, export / import the clipboard as XML. The clipboard list is the
 * one of the clipboard menu, the favourites the ones of the favourites menu.
 */
public class ListView extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color STAR = new Color(0xE8A317);
    /**
     * the width of the star at the left of a row (a click there toggles)
     */
    private static final int STAR_WIDTH = 28;
    private final MPList list;
    private final List<DatabaseObject> favourites = new ArrayList<DatabaseObject>();
    private final DefaultListModel<Object> rows = new DefaultListModel<Object>();
    private final JList<Object> view = new JList<Object>(rows);
    private final JTextField filter = new JTextField();
    private final JPanel cards = new JPanel(new CardLayout());
    private final JButton remove = new JButton(Messages.CLIPBOARD_REMOVE.toString());
    private final JButton clear = new JButton(Messages.CLIPBOARD_CLEAR.toString());
    private final JButton pasteAndClear = new JButton(Messages.CLIPBOARD_PASTE_AND_CLEAR.toString());
    private final PrimaryButton paste = new PrimaryButton(Messages.CLIPBOARD_PASTE.toString(), null);
    private final JPanel titleBar = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
    private Runnable onChange;

    /**
     * A row: a record of the favourites or the clipboard
     */
    private static final class Row {

        final DatabaseObject record;
        final boolean favourite;

        Row(DatabaseObject record, boolean favourite) {
            this.record = record;
            this.favourite = favourite;
        }
    }

    /**
     * The title of a section, with a hint when it is empty
     */
    private static final class Section {

        final String text;
        final String hint;

        Section(String text, String hint) {
            this.text = text;
            this.hint = hint;
        }
    }

    /**
     * @param list the records of the clipboard
     */
    public ListView(MPList list) {
        super(new BorderLayout(0, ModernTheme.px(8)));
        this.list = list;
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(12, 14));

        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        top.setOpaque(false);
        titleBar.setOpaque(false);
        titleBar.add(ModernTheme.heading(Messages.CLIPBOARD_TITLE.toString()), BorderLayout.WEST);
        top.add(titleBar, BorderLayout.NORTH);
        filter.putClientProperty("JTextField.placeholderText", Messages.CLIPBOARD_FILTER.toString());
        filter.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        filter.setToolTipText(Messages.CLIPBOARD_INFO.toString());
        filter.getDocument().addDocumentListener(new DocumentListener() {
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
        filter.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "yabs.list");
        filter.getActionMap().put("yabs.list", action(() -> {
            int first = nextRow(-1, 1);
            if (first >= 0) {
                view.setSelectedIndex(first);
                view.requestFocusInWindow();
            }
        }));
        top.add(filter, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        view.setCellRenderer(new Renderer());
        view.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        view.setBackground(ModernTheme.cardBackground());
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = view.locationToIndex(e.getPoint());
                Rectangle cell = i >= 0 ? view.getCellBounds(i, i) : null;
                if (i < 0 || i >= rows.size() || !(rows.get(i) instanceof Row) || cell == null || !cell.contains(e.getPoint())) {
                    return;
                }
                if (e.getX() - cell.x < ModernTheme.px(STAR_WIDTH)) {
                    toggleFavourite((Row) rows.get(i));
                } else if (e.getClickCount() > 1) {
                    openSelected();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int i = view.locationToIndex(e.getPoint());
                Rectangle cell = i >= 0 ? view.getCellBounds(i, i) : null;
                boolean onStar = i >= 0 && i < rows.size() && rows.get(i) instanceof Row && cell != null && cell.contains(e.getPoint())
                        && e.getX() - cell.x < ModernTheme.px(STAR_WIDTH);
                view.setCursor(Cursor.getPredefinedCursor(onStar ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                view.setToolTipText(onStar ? (((Row) rows.get(i)).favourite || isFavourite(((Row) rows.get(i)).record)
                        ? Messages.CLIPBOARD_UNSTAR.toString() : Messages.CLIPBOARD_STAR.toString()) : null);
            }
        };
        view.addMouseListener(mouse);
        view.addMouseMotionListener(mouse);
        //sections are no rows to select
        view.addListSelectionListener(e -> {
            for (int i : view.getSelectedIndices()) {
                if (i >= 0 && i < rows.size() && !(rows.get(i) instanceof Row)) {
                    view.removeSelectionInterval(i, i);
                }
            }
            updateButtons();
        });
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "yabs.open");
        view.getActionMap().put("yabs.open", action(this::openSelected));
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "yabs.remove");
        view.getActionMap().put("yabs.remove", action(this::removeSelected));
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "yabs.star");
        view.getActionMap().put("yabs.star", action(() -> {
            for (Object o : view.getSelectedValuesList()) {
                if (o instanceof Row) {
                    toggleFavourite((Row) o);
                }
            }
        }));
        JScrollPane scroll = new JScrollPane(view);
        scroll.setPreferredSize(new Dimension(ModernTheme.px(400), ModernTheme.px(320)));

        JLabel empty = new JLabel("<html><div style='text-align:center;width:" + ModernTheme.px(280) + "px'>"
                + escape(Messages.CLIPBOARD_EMPTY.toString()) + "</div></html>", SwingConstants.CENTER);
        empty.setForeground(ModernTheme.muted());
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(ModernTheme.cardBackground());
        emptyPanel.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        emptyPanel.add(empty, BorderLayout.CENTER);
        cards.setOpaque(false);
        cards.add(scroll, "list");
        cards.add(emptyPanel, "empty");
        add(cards, BorderLayout.CENTER);

        remove.setToolTipText(Messages.CLIPBOARD_REMOVE_TIP.toString());
        remove.addActionListener(e -> removeSelected());
        clear.setToolTipText(Messages.CLIPBOARD_CLEAR_TIP.toString());
        clear.addActionListener(e -> clearAll());
        JButton more = new JButton(Messages.MORE_ACTIONS + " ▾");
        JPopupMenu menu = new JPopupMenu();
        JMenuItem export = new JMenuItem(Messages.CLIPBOARD_EXPORT.toString());
        export.addActionListener(e -> exportXml());
        JMenuItem imp = new JMenuItem(Messages.CLIPBOARD_IMPORT.toString());
        imp.addActionListener(e -> importXml());
        menu.add(export);
        menu.add(imp);
        more.addActionListener(e -> {
            export.setEnabled(!list.isEmpty());
            menu.show(more, 0, more.getHeight());
        });
        paste.addActionListener(e -> pasteInto(false));
        pasteAndClear.addActionListener(e -> pasteInto(true));
        paste.setToolTipText(Messages.CLIPBOARD_PASTE_TIP.toString());
        add(Applets.actionBar(new JComponent[]{remove, clear, more}, new JComponent[]{pasteAndClear, paste}), BorderLayout.SOUTH);
        refresh();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    private static javax.swing.AbstractAction action(final Runnable r) {
        return new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        };
    }

    /**
     * @return the line with the title (a window adds its controls and moves
     * by it)
     */
    public JPanel getTitleBar() {
        return titleBar;
    }

    /**
     * @param onChange runs when the view changed the clipboard (e.g. to
     * rebuild the clipboard menu)
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public void addElement(DatabaseObject obj) {
        if (obj != null && !list.contains(obj)) {
            list.add(obj);
        }
        changed();
    }

    /**
     * Reads the favourites again and shows both lists
     */
    public void refresh() {
        favourites.clear();
        try {
            //only when logged in (the favourites of the user, from the database)
            for (Favourite f : !mpv5.Main.INSTANTIATED ? new Favourite[0] : Favourite.getUserFavourites()) {
                try {
                    favourites.add(DatabaseObject.getObject(f.getFavContext(), f.__getItemsids()));
                } catch (Exception e) {
                    //deleted meanwhile
                }
            }
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        fill();
    }

    /**
     * The rows: favourites, then the clipboard, as filtered; keeps the
     * selection
     */
    private void fill() {
        List<DatabaseObject> keepRecords = new ArrayList<DatabaseObject>();
        List<Boolean> keepFav = new ArrayList<Boolean>();
        for (Object o : view.getSelectedValuesList()) {
            if (o instanceof Row) {
                keepRecords.add(((Row) o).record);
                keepFav.add(((Row) o).favourite);
            }
        }
        String needle = filter.getText().trim().toLowerCase();
        rows.clear();
        List<DatabaseObject> favs = matching(favourites, needle);
        List<DatabaseObject> clip = matching(list, needle);
        rows.addElement(new Section(String.format(Messages.CLIPBOARD_SECTION_FAVOURITES.toString(), favourites.size()),
                favourites.isEmpty() ? Messages.CLIPBOARD_FAVOURITES_EMPTY.toString() : favs.isEmpty() ? Messages.CLIPBOARD_NO_MATCH.toString() : null));
        for (DatabaseObject o : favs) {
            rows.addElement(new Row(o, true));
        }
        rows.addElement(new Section(String.format(Messages.CLIPBOARD_SECTION_CLIPBOARD.toString(), list.size()),
                list.isEmpty() ? Messages.CLIPBOARD_CLIPBOARD_EMPTY.toString() : clip.isEmpty() ? Messages.CLIPBOARD_NO_MATCH.toString() : null));
        for (DatabaseObject o : clip) {
            rows.addElement(new Row(o, false));
        }
        List<Integer> sel = new ArrayList<Integer>();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i) instanceof Row) {
                Row r = (Row) rows.get(i);
                for (int k = 0; k < keepRecords.size(); k++) {
                    if (keepRecords.get(k).equals(r.record) && keepFav.get(k) == r.favourite) {
                        sel.add(i);
                    }
                }
            }
        }
        view.setSelectedIndices(sel.stream().mapToInt(Integer::intValue).toArray());
        ((CardLayout) cards.getLayout()).show(cards, favourites.isEmpty() && list.isEmpty() ? "empty" : "list");
        updateButtons();
    }

    private static List<DatabaseObject> matching(List<DatabaseObject> from, String needle) {
        List<DatabaseObject> l = new ArrayList<DatabaseObject>();
        for (DatabaseObject o : from) {
            if (needle.isEmpty() || (String.valueOf(o.__getCname()) + " " + mpv5.handler.Activity.typeName(o)).toLowerCase().contains(needle)) {
                l.add(o);
            }
        }
        return l;
    }

    private int nextRow(int from, int step) {
        for (int i = from + step; i >= 0 && i < rows.size(); i += step) {
            if (rows.get(i) instanceof Row) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFavourite(DatabaseObject o) {
        return favourites.contains(o);
    }

    private void changed() {
        fill();
        if (onChange != null) {
            onChange.run();
        }
    }

    private List<Row> selectedRows() {
        List<Row> l = new ArrayList<Row>();
        for (Object o : view.getSelectedValuesList()) {
            if (o instanceof Row) {
                l.add((Row) o);
            }
        }
        return l;
    }

    private void updateButtons() {
        List<Row> sel = selectedRows();
        remove.setEnabled(!sel.isEmpty());
        clear.setEnabled(!list.isEmpty());
        paste.setEnabled(!sel.isEmpty() || !list.isEmpty());
        pasteAndClear.setEnabled(!list.isEmpty());
        paste.setText(sel.isEmpty() ? Messages.CLIPBOARD_PASTE.toString() : Messages.CLIPBOARD_PASTE_SELECTED.toString());
    }

    /**
     * Makes the record a favourite or not (the favourites menu follows)
     */
    private void toggleFavourite(Row r) {
        DatabaseObject o = r.record;
        try {
            if (mpv5.db.objects.User.getCurrentUser().isDefault()) {
                Popup.notice(this, Messages.DEFAULT_USER);
                return;
            }
            if (isFavourite(o)) {
                Favourite.removeFavourite(o);
            } else if (o.isExisting()) {
                new Favourite(o).save();
            }
            mpv5.YabsViewProxy.instance().reloadFavorites();
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        refresh();
    }

    private void openSelected() {
        for (Row r : selectedRows()) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(r.record);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }

    /**
     * Selected clipboard rows leave the clipboard, selected favourites stop
     * being favourites
     */
    private void removeSelected() {
        boolean favs = false;
        for (Row r : selectedRows()) {
            if (r.favourite) {
                Favourite.removeFavourite(r.record);
                favs = true;
            } else {
                list.remove(r.record);
            }
        }
        if (favs) {
            mpv5.YabsViewProxy.instance().reloadFavorites();
            refresh();
        }
        changed();
    }

    private void clearAll() {
        list.clear();
        changed();
    }

    /**
     * Pastes the selected records (the whole clipboard if none is selected)
     * into the current view
     */
    private void pasteInto(boolean andClear) {
        DataPanel target = mpv5.YabsViewProxy.instance().getIdentifierView().getCurrentTab();
        if (target == null) {
            Popup.notice(this, Messages.CLIPBOARD_NO_VIEW);
            return;
        }
        List<Row> sel = selectedRows();
        List<DatabaseObject> items = new ArrayList<DatabaseObject>();
        if (sel.isEmpty() || andClear) {
            items.addAll(list);
        } else {
            for (Row r : sel) {
                if (!items.contains(r.record)) {
                    items.add(r.record);
                }
            }
        }
        target.paste(items.toArray(new DatabaseObject[0]));
        if (andClear) {
            list.clear();
            changed();
        }
        try {
            BigPopup.hide(this);
        } catch (Exception e) {
            //not in a popup
        }
    }

    private void exportXml() {
        if (!(MPSecurityManager.check(Context.getContact(), MPSecurityManager.EXPORT)
                && MPSecurityManager.check(Context.getInvoice(), MPSecurityManager.EXPORT)
                && MPSecurityManager.check(Context.getProduct(), MPSecurityManager.EXPORT))) {
            return;
        }
        XMLWriter xmlw = new XMLWriter();
        xmlw.newDoc(true);
        String name = Messages.ACTION_EXPORT.toString() + "-" + DateConverter.getTodayFileDate();
        for (Context context : Context.getImportableContexts()) {
            ArrayList<DatabaseObject> d = new ArrayList<DatabaseObject>();
            for (DatabaseObject o : list) {
                if (o.getContext().equals(context)) {
                    d.add(o);
                }
            }
            xmlw.add(d);
        }
        mpv5.YabsViewProxy.instance().showFilesaveDialogFor(xmlw.createFile(name));
    }

    private void importXml() {
        SwingUtilities.invokeLater(() -> {
            try {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                mpv5.YabsViewProxy.instance().setWaiting(true);
                DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
                d.setFileFilter(DialogForFile.XML_FILES);
                if (d.chooseFile()) {
                    XMLReader x = new XMLReader();
                    x.newDoc(d.getFile(), false);
                    x.setOverwriteExisting(true);
                    ArrayList<ArrayList<DatabaseObject>> objs = x.getObjects();
                    if (objs != null) {
                        for (ArrayList<DatabaseObject> l : objs) {
                            for (DatabaseObject o : l) {
                                if (!list.contains(o)) {
                                    list.add(o);
                                }
                            }
                        }
                    }
                    changed();
                }
            } catch (Exception e) {
                Popup.error(e);
                Log.Debug(e);
            } finally {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                mpv5.YabsViewProxy.instance().setWaiting(false);
            }
        });
    }

    /**
     * A row: star, icon and name, the kind of record at the right; a section:
     * its title and a hint if empty
     */
    private final class Renderer implements ListCellRenderer<Object> {

        private final JPanel row = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        private final JLabel star = new JLabel("", SwingConstants.CENTER);
        private final JLabel name = new JLabel();
        private final JLabel kind = new JLabel();
        private final JPanel section = new JPanel(new BorderLayout());
        private final JLabel sectionTitle = new JLabel();
        private final JLabel sectionHint = new JLabel();

        Renderer() {
            star.setPreferredSize(new Dimension(ModernTheme.px(STAR_WIDTH), ModernTheme.px(22)));
            star.setFont(ModernTheme.font(2, Font.PLAIN));
            name.setFont(ModernTheme.font(0, Font.PLAIN));
            kind.setFont(ModernTheme.font(-1, Font.PLAIN));
            row.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(2), 0, ModernTheme.px(2), ModernTheme.px(10)));
            row.add(star, BorderLayout.WEST);
            row.add(name, BorderLayout.CENTER);
            row.add(kind, BorderLayout.EAST);
            sectionTitle.setFont(ModernTheme.font(-1, Font.BOLD));
            sectionTitle.setForeground(ModernTheme.muted());
            sectionHint.setFont(ModernTheme.font(-1, Font.ITALIC));
            sectionHint.setForeground(ModernTheme.muted());
            section.setOpaque(false);
            section.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(3), ModernTheme.px(8)));
            section.add(sectionTitle, BorderLayout.NORTH);
            section.add(sectionHint, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
            if (value instanceof Section) {
                Section s = (Section) value;
                sectionTitle.setText(s.text.toUpperCase());
                sectionHint.setText(s.hint == null ? "" : s.hint);
                sectionHint.setVisible(s.hint != null);
                return section;
            }
            Row r = (Row) value;
            boolean fav = r.favourite || isFavourite(r.record);
            star.setText(fav ? "★" : "☆");
            star.setForeground(fav ? STAR : ModernTheme.muted());
            name.setText(String.valueOf(r.record.__getCname()));
            name.setIcon(r.record.getIcon());
            kind.setText(mpv5.handler.Activity.typeName(r.record));
            row.setOpaque(true);
            row.setBackground(selected ? l.getSelectionBackground() : l.getBackground());
            name.setForeground(selected ? l.getSelectionForeground() : l.getForeground());
            kind.setForeground(selected ? l.getSelectionForeground() : ModernTheme.muted());
            return row;
        }
    }
}
