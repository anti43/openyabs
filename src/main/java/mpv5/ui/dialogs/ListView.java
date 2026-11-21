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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import mpv5.data.MPList;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
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
 * The clipboard of records ("Zwischenablage"): records copied with Ctrl+C
 * (outside text fields) or "Copy". Open with a double click or Enter, paste
 * all (or the selected) into the current view, remove, clear, export and
 * import as XML. The list is the one of the clipboard menu too.
 */
public class ListView extends JPanel {

    private static final long serialVersionUID = 1L;
    private final MPList list;
    private final JList<Object> view = new JList<Object>();
    private final JLabel count = new JLabel();
    private final JPanel cards = new JPanel(new CardLayout());
    private final JButton remove = new JButton(Messages.CLIPBOARD_REMOVE.toString());
    private final JButton clear = new JButton(Messages.CLIPBOARD_CLEAR.toString());
    private final JButton pasteAndClear = new JButton(Messages.CLIPBOARD_PASTE_AND_CLEAR.toString());
    private final PrimaryButton paste = new PrimaryButton(Messages.CLIPBOARD_PASTE.toString(), null);
    private Runnable onChange;
    private final JPanel titleBar = new JPanel(new BorderLayout(ModernTheme.px(8), 0));

    /**
     * @param list the records of the clipboard
     */
    public ListView(MPList list) {
        super(new BorderLayout(0, ModernTheme.px(10)));
        this.list = list;
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(12, 14));

        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        top.setOpaque(false);
        titleBar.setOpaque(false);
        titleBar.add(ModernTheme.heading(Messages.CLIPBOARD_TITLE.toString()), BorderLayout.WEST);
        count.setForeground(ModernTheme.muted());
        titleBar.add(count, BorderLayout.CENTER);
        top.add(titleBar, BorderLayout.NORTH);
        JLabel info = new JLabel("<html><div style='width:" + ModernTheme.px(340) + "px'>"
                + Messages.CLIPBOARD_INFO.toString().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        info.setForeground(ModernTheme.muted());
        top.add(info, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        view.setCellRenderer(MPList.getDBORenderer());
        view.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        view.setFixedCellHeight(Math.max(ModernTheme.px(24), 22));
        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    openSelected();
                }
            }
        });
        view.addListSelectionListener(e -> updateButtons());
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "yabs.open");
        view.getActionMap().put("yabs.open", action(this::openSelected));
        view.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "yabs.remove");
        view.getActionMap().put("yabs.remove", action(this::removeSelected));
        JScrollPane scroll = new JScrollPane(view);
        scroll.setPreferredSize(new Dimension(ModernTheme.px(380), ModernTheme.px(260)));

        JLabel empty = new JLabel("<html><div style='text-align:center;width:" + ModernTheme.px(260) + "px'>"
                + Messages.CLIPBOARD_EMPTY.toString().replace("&", "&amp;").replace("<", "&lt;") + "</div></html>", SwingConstants.CENTER);
        empty.setForeground(ModernTheme.muted());
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(ModernTheme.cardBackground());
        emptyPanel.setBorder(javax.swing.BorderFactory.createLineBorder(ModernTheme.border()));
        emptyPanel.add(empty, BorderLayout.CENTER);
        cards.setOpaque(false);
        cards.add(scroll, "list");
        cards.add(emptyPanel, "empty");
        add(cards, BorderLayout.CENTER);

        remove.addActionListener(e -> removeSelected());
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
     * @param onChange runs when the view changed the list (e.g. to rebuild
     * the clipboard menu)
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
     * Shows the records of the list again
     */
    public void refresh() {
        List<Object> keep = view.getSelectedValuesList();
        view.setModel(list.getListModel());
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < view.getModel().getSize(); i++) {
            if (keep.contains(view.getModel().getElementAt(i))) {
                indices.add(i);
            }
        }
        view.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
        int n = list.size();
        count.setText(n == 0 ? "" : String.format(Messages.CLIPBOARD_COUNT.toString(), n));
        ((CardLayout) cards.getLayout()).show(cards, n == 0 ? "empty" : "list");
        updateButtons();
    }

    private void changed() {
        refresh();
        if (onChange != null) {
            onChange.run();
        }
    }

    private void updateButtons() {
        boolean any = !list.isEmpty();
        remove.setEnabled(!view.isSelectionEmpty());
        clear.setEnabled(any);
        paste.setEnabled(any);
        pasteAndClear.setEnabled(any);
        paste.setText(view.isSelectionEmpty() || view.getSelectedIndices().length == list.size()
                ? Messages.CLIPBOARD_PASTE.toString() : Messages.CLIPBOARD_PASTE_SELECTED.toString());
    }

    private void openSelected() {
        for (Object o : view.getSelectedValuesList()) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab((DatabaseObject) o);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }

    private void removeSelected() {
        for (Object o : view.getSelectedValuesList()) {
            list.remove((DatabaseObject) o);
        }
        changed();
    }

    private void clearAll() {
        list.clear();
        changed();
    }

    /**
     * Pastes the selected records (all if none is selected) into the current
     * view
     */
    private void pasteInto(boolean andClear) {
        DataPanel target = mpv5.YabsViewProxy.instance().getIdentifierView().getCurrentTab();
        if (target == null) {
            Popup.notice(this, Messages.CLIPBOARD_NO_VIEW);
            return;
        }
        List<Object> sel = view.getSelectedValuesList();
        DatabaseObject[] items = sel.isEmpty() ? list.toArray() : sel.toArray(new DatabaseObject[0]);
        target.paste(items);
        if (andClear) {
            for (DatabaseObject o : items) {
                list.remove(o);
            }
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
}
