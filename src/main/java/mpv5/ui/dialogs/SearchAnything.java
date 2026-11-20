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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.SearchService;
import mpv5.logging.Log;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.panels.ModernContactPanel;
import mpv5.ui.panels.ModernProductPanel;
import mpv5.ui.panels.ProductsOverview;

/**
 * "Search anything" (double Shift, Ctrl+F): one field, results while typing -
 * records (contacts, documents, products, positions, ...) and commands of the
 * menu and the navigation. Up / Down choose, Enter opens, Escape closes.
 */
public final class SearchAnything extends JDialog {

    private static final long serialVersionUID = 1L;
    private static SearchAnything instance;

    private final JTextField field = new JTextField();
    private final DefaultListModel<Object> model = new DefaultListModel<Object>();
    private final JList<Object> list = new JList<Object>(model);
    private final JLabel status = new JLabel(" ");
    private final JToggleButton pin = new JToggleButton("Pin");
    private final JButton closeButton = new JButton("\u2715");
    private final Timer delay;
    private int searches;

    /**
     * A command of the menu or the navigation
     */
    private static final class Command {

        final String title;
        final String path;
        final Runnable run;

        Command(String title, String path, AbstractButton button) {
            this(title, path, (Runnable) button::doClick);
        }

        Command(String title, String path, Runnable run) {
            this.title = title;
            this.path = path;
            this.run = run;
        }
    }

    /**
     * The heading of a group of results (not selectable)
     */
    private static final class Heading {

        final String text;

        Heading(String text) {
            this.text = text;
        }
    }

    public static SearchAnything getInstance() {
        return instance;
    }

    public static SearchAnything ensureInstance() {
        JFrame owner = null;
        try {
            owner = (JFrame) mpv5.YabsViewProxy.instance().getIdentifierFrame();
        } catch (Exception ignore) {
        }
        if (instance == null || (owner != null && instance.getOwner() != owner)) {
            instance = new SearchAnything(owner);
        }
        return instance;
    }

    public static boolean isOpen() {
        return instance != null && instance.isVisible();
    }

    public static void focusField() {
        if (isOpen()) {
            instance.toFront();
            instance.requestFocus();
            instance.field.requestFocusInWindow();
            instance.field.selectAll();
        }
    }

    /**
     * Opens the search window centered on the current screen
     */
    public static void open() {
        openCenter();
    }

    /**
     * Opens the search window at the center of the current screen
     */
    public static void openCenter() {
        mpv5.ui.panels.SettingsIndex.build();
        SearchAnything dialog = ensureInstance();
        dialog.showCenter();
    }

    /**
     * Opens the search window right below the given component (e.g. menu or button),
     * clamped to stay fully on screen
     *
     * @param anchor
     */
    public static void openAt(java.awt.Component anchor) {
        mpv5.ui.panels.SettingsIndex.build();
        SearchAnything dialog = ensureInstance();
        if (anchor == null) {
            dialog.showCenter();
            return;
        }
        Point p = null;
        Rectangle screen = null;
        if (anchor.isShowing()) {
            p = anchor.getLocationOnScreen();
            screen = anchor.getGraphicsConfiguration().getBounds();
        } else if (anchor instanceof JMenuItem && ((JMenuItem) anchor).getParent() instanceof javax.swing.JPopupMenu) {
            Component invoker = ((javax.swing.JPopupMenu) ((JMenuItem) anchor).getParent()).getInvoker();
            if (invoker != null && invoker.isShowing()) {
                p = invoker.getLocationOnScreen();
                screen = invoker.getGraphicsConfiguration().getBounds();
            }
        }
        if (p == null) {
            try {
                java.awt.PointerInfo pi = java.awt.MouseInfo.getPointerInfo();
                if (pi != null) {
                    p = pi.getLocation();
                    screen = pi.getDevice().getDefaultConfiguration().getBounds();
                }
            } catch (Exception ignore) {
            }
        }
        if (p == null) {
            dialog.showCenter();
            return;
        }
        if (screen == null) {
            Window owner = dialog.getOwner();
            if (owner != null && owner.isShowing()) {
                screen = owner.getGraphicsConfiguration().getBounds();
            } else {
                try {
                    screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
                } catch (Exception ignore) {
                    screen = new Rectangle(0, 0, 1024, 768);
                }
            }
        }
        int anchorHeight = anchor.isShowing() ? anchor.getHeight() : ModernTheme.px(24);
        int x = Math.max(screen.x, Math.min(p.x, screen.x + screen.width - dialog.getWidth()));
        int y = p.y + anchorHeight + ModernTheme.px(2);
        if (y + dialog.getHeight() > screen.y + screen.height) {
            y = Math.max(screen.y, p.y - dialog.getHeight() - ModernTheme.px(2));
        }
        dialog.showAt(x, y);
    }

    /**
     * Opens the search window at the location of the clicked menu item or parent menu
     */
    public static void openAtMenu(JMenuItem item, JMenu menu) {
        if (item != null && item.isShowing()) {
            openAt(item);
        } else if (menu != null && menu.isShowing()) {
            openAt(menu);
        } else {
            openAt(item);
        }
    }

    public SearchAnything(JFrame owner) {
        super(owner, Messages.SEARCH_ANYTHING.toString(), false);
        setUndecorated(true);
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setBackground(ModernTheme.cardBackground());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(12), ModernTheme.px(8), ModernTheme.px(12))));

        JLabel title = new JLabel(Messages.SEARCH_ANYTHING.toString());
        title.setFont(ModernTheme.font(0, Font.BOLD));
        title.setForeground(ModernTheme.muted());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        title.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        header.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        controls.setOpaque(false);

        pin.setFont(ModernTheme.font(-1, Font.PLAIN));
        pin.setFocusable(false);
        pin.setToolTipText("Stick to top");
        pin.setMargin(new java.awt.Insets(ModernTheme.px(2), ModernTheme.px(6), ModernTheme.px(2), ModernTheme.px(6)));
        pin.addActionListener(e -> setStickToTop(pin.isSelected()));

        closeButton.setFont(ModernTheme.font(-1, Font.BOLD));
        closeButton.setFocusable(false);
        closeButton.setToolTipText("Close (Esc)");
        closeButton.setMargin(new java.awt.Insets(ModernTheme.px(2), ModernTheme.px(6), ModernTheme.px(2), ModernTheme.px(6)));
        closeButton.addActionListener(e -> close());

        controls.add(pin);
        controls.add(closeButton);
        header.add(controls, BorderLayout.EAST);

        MouseAdapter dragListener = new MouseAdapter() {
            private Point dragOrigin;

            @Override
            public void mousePressed(MouseEvent e) {
                dragOrigin = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOrigin != null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - dragOrigin.x, current.y - dragOrigin.y);
                }
            }
        };
        header.addMouseListener(dragListener);
        header.addMouseMotionListener(dragListener);
        title.addMouseListener(dragListener);
        title.addMouseMotionListener(dragListener);
        status.addMouseListener(dragListener);
        status.addMouseMotionListener(dragListener);

        field.setFont(ModernTheme.font(6, Font.PLAIN));
        field.putClientProperty("JTextField.placeholderText", Messages.SEARCH_ANYTHING_HINT.toString());
        field.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(field, BorderLayout.CENTER);

        list.setCellRenderer(new Renderer());
        list.setFixedCellWidth(-1);
        list.setBackground(ModernTheme.cardBackground());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    int i = list.locationToIndex(e.getPoint());
                    if (i >= 0 && !(model.get(i) instanceof Heading)) {
                        list.setSelectedIndex(i);
                        openSelected();
                    }
                }
            }
        });
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setPreferredSize(new Dimension(ModernTheme.px(640), ModernTheme.px(380)));

        status.setFont(ModernTheme.font(-1, Font.PLAIN));
        status.setForeground(ModernTheme.muted());

        p.add(top, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(status, BorderLayout.SOUTH);
        setContentPane(p);

        delay = new Timer(220, e -> search());
        delay.setRepeats(false);
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                delay.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                delay.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                delay.restart();
            }
        });
        //keys in the field: up / down move in the list, enter opens, escape closes
        bind(KeyEvent.VK_DOWN, () -> move(1));
        bind(KeyEvent.VK_UP, () -> move(-1));
        bind(KeyEvent.VK_PAGE_DOWN, () -> move(8));
        bind(KeyEvent.VK_PAGE_UP, () -> move(-8));
        bind(KeyEvent.VK_ENTER, this::openSelected);
        bind(KeyEvent.VK_ESCAPE, () -> {
            if (!isStickToTop()) {
                close();
            }
        });
        //a click elsewhere closes it, unless stick to top is toggled
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                if (!isStickToTop()) {
                    close();
                }
            }
        });
        pack();
    }

    private void bind(int key, Runnable r) {
        String name = "searchanything." + key;
        field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name);
        list.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name);
        javax.swing.AbstractAction a = new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        };
        field.getActionMap().put(name, a);
        list.getActionMap().put(name, a);
    }

    public boolean isStickToTop() {
        return pin.isSelected();
    }

    public void setStickToTop(boolean sticky) {
        pin.setSelected(sticky);
        setAlwaysOnTop(sticky);
        if (sticky && isVisible()) {
            toFront();
        }
    }

    public void close() {
        delay.stop();
        setVisible(false);
    }

    public JTextField getTextField() {
        return field;
    }

    public JToggleButton getPinButton() {
        return pin;
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    public void showCenter() {
        Rectangle screen = null;
        Window owner = getOwner();
        if (owner != null && owner.isShowing()) {
            screen = owner.getGraphicsConfiguration().getBounds();
        } else {
            try {
                screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            } catch (Exception ignore) {
            }
        }
        if (screen == null) {
            screen = new Rectangle(0, 0, 1024, 768);
        }
        int x = screen.x + (screen.width - getWidth()) / 2;
        int y = screen.y + (screen.height - getHeight()) / 2;
        showAt(x, y);
    }

    public void showAt(int x, int y) {
        setLocation(x, y);
        field.selectAll();
        if (isStickToTop()) {
            setAlwaysOnTop(true);
        }
        setVisible(true);
        toFront();
        requestFocus();
        SwingUtilities.invokeLater(field::requestFocusInWindow);
        if (model.isEmpty()) {
            search();
        }
    }

    public static JTextField findFilterBox(Component root) {
        if (root == null) {
            return null;
        }
        if (root instanceof MPControlPanel) {
            return ((MPControlPanel) root).getSearchField();
        }
        if (root instanceof ProductsOverview) {
            return ((ProductsOverview) root).getSearchField();
        }
        if (root instanceof ContactsList) {
            return ((ContactsList) root).getFilterField();
        }
        if (root instanceof ModernContactPanel) {
            return ((ModernContactPanel) root).getFilterField();
        }
        if (root instanceof ModernProductPanel) {
            return ((ModernProductPanel) root).getFilterField();
        }
        if (root instanceof Container) {
            return findFilterBoxInContainer((Container) root);
        }
        return null;
    }

    private static JTextField findFilterBoxInContainer(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField && c.isEnabled() && c.isVisible()) {
                JTextField tf = (JTextField) c;
                String name = tf.getName() == null ? "" : tf.getName().toLowerCase();
                String tip = tf.getToolTipText() == null ? "" : tf.getToolTipText().toLowerCase();
                Object placeholderObj = tf.getClientProperty("JTextField.placeholderText");
                String placeholder = placeholderObj == null ? "" : placeholderObj.toString().toLowerCase();
                if (isFilterKeyword(name) || isFilterKeyword(tip) || isFilterKeyword(placeholder)) {
                    return tf;
                }
            }
            if (c instanceof Container) {
                JTextField found = findFilterBoxInContainer((Container) c);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean isFilterKeyword(String s) {
        return s.contains("filter") || s.contains("search") || s.contains("such") || s.contains("find");
    }

    /**
     * Contextual find behavior for Ctrl+F:
     * - If active tab has a filter box, focus that filter box.
     * - If no filter box, but Search Anything is open, focus Search Anything.
     * - If no filter box and Search Anything is closed, open Search Anything.
     */
    public static void handleContextualFind() {
        Component activeTab = null;
        try {
            Object showing = mpv5.ui.frames.MPView.getShowingTab();
            if (showing instanceof JScrollPane) {
                activeTab = ((JScrollPane) showing).getViewport().getView();
            } else if (showing instanceof Component) {
                activeTab = (Component) showing;
            }
        } catch (Exception ignore) {
        }

        JTextField filterBox = findFilterBox(activeTab);
        if (filterBox != null && filterBox.isShowing() && filterBox.isEnabled()) {
            filterBox.requestFocusInWindow();
            filterBox.selectAll();
            return;
        }

        if (isOpen()) {
            focusField();
        } else {
            openCenter();
        }
    }

    private void move(int by) {
        if (model.isEmpty()) {
            return;
        }
        int i = Math.max(-1, list.getSelectedIndex());
        int step = by > 0 ? 1 : -1;
        int left = Math.abs(by);
        int next = i;
        while (left > 0) {
            int n = next + step;
            while (n >= 0 && n < model.size() && model.get(n) instanceof Heading) {
                n += step;
            }
            if (n < 0 || n >= model.size()) {
                break;
            }
            next = n;
            left--;
        }
        if (next >= 0) {
            list.setSelectedIndex(next);
            list.ensureIndexIsVisible(next);
        }
    }

    /**
     * The commands now, the records in the background (the database can be
     * slow); only the newest search is shown
     */
    private void search() {
        final String needle = field.getText().trim();
        final int search = ++searches;
        final List<Command> commands = commands(needle);
        if (needle.length() < 2) {
            pluginHits = new ArrayList<Command>();
            show(commands, new ArrayList<SearchService.Hit>(), needle);
            status.setText(Messages.SEARCH_ANYTHING_TIP.toString());
            return;
        }
        status.setText(Messages.PLEASE_WAIT.toString());
        new SwingWorker<List<SearchService.Hit>, Void>() {
            @Override
            protected List<SearchService.Hit> doInBackground() {
                //the results of plugins (their search providers)
                List<Command> fromPlugins = new ArrayList<Command>();
                for (Object[] r : mpv5.plugin.ExtensionManager.instance().search(needle)) {
                    final mpv5.plugin.api.SearchResult sr = (mpv5.plugin.api.SearchResult) r[1];
                    String path = sr.detail.isEmpty() ? String.valueOf(r[0]) : sr.detail + "  \u00b7  " + r[0];
                    fromPlugins.add(new Command(sr.title, path, sr.open));
                }
                pluginHits = fromPlugins;
                return SearchService.search(needle);
            }

            @Override
            protected void done() {
                if (search != searches) {
                    return;
                }
                try {
                    List<SearchService.Hit> hits = get();
                    show(commands, hits, needle);
                    int found = 0;
                    for (int i = 0; i < model.size(); i++) {
                        if (!(model.get(i) instanceof Heading)) {
                            found++;
                        }
                    }
                    status.setText(found == 0
                            ? Messages.SEARCH_ANYTHING_NOTHING.toString()
                            : found + " " + Messages.SEARCH_ANYTHING_RESULTS + "  ·  " + Messages.SEARCH_ANYTHING_TIP);
                } catch (Exception e) {
                    Log.Debug(SearchAnything.this, e);
                    status.setText(Messages.ERROR_OCCURED.toString());
                }
            }
        }.execute();
    }

    private volatile List<Command> pluginHits = new ArrayList<Command>();

    private void show(List<Command> commands, List<SearchService.Hit> hits, String needle) {
        model.clear();
        if (!needle.isEmpty() && !pluginHits.isEmpty()) {
            model.addElement(new Heading(Messages.SETTINGS_GROUP_PLUGINS.toString()));
            for (Command c : pluginHits.subList(0, Math.min(pluginHits.size(), 12))) {
                model.addElement(c);
            }
        }
        String kind = null;
        for (SearchService.Hit h : hits) {
            if (!h.kind.equals(kind)) {
                kind = h.kind;
                model.addElement(new Heading(kind));
            }
            model.addElement(h);
        }
        List<Command> settings = settings(needle);
        if (!settings.isEmpty()) {
            model.addElement(new Heading(Messages.SEARCH_KIND_SETTINGS.toString()));
            for (Command c : settings) {
                model.addElement(c);
            }
        }
        if (!commands.isEmpty()) {
            model.addElement(new Heading(Messages.SEARCH_KIND_COMMANDS.toString()));
            for (Command c : commands.subList(0, Math.min(commands.size(), needle.isEmpty() ? 0 : 12))) {
                model.addElement(c);
            }
            if (needle.isEmpty()) {
                model.removeElementAt(model.size() - 1);
            }
        }
        move(1);
    }

    /**
     * Commands: the entries of the menu and the buttons of the navigation
     * whose text matches
     */
    private List<Command> commands(String needle) {
        List<Command> l = new ArrayList<Command>();
        if (needle.isEmpty() || !(getOwner() instanceof JFrame)) {
            return l;
        }
        JMenuBar bar = ((JFrame) getOwner()).getJMenuBar();
        if (bar != null) {
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu m = bar.getMenu(i);
                if (m != null) {
                    menu(m, m.getText(), needle, l);
                }
            }
        }
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (Command cmd : l) {
            seen.add(cmd.title);
        }
        List<Component> todo = new ArrayList<Component>();
        todo.add(((JFrame) getOwner()).getContentPane());
        while (!todo.isEmpty()) {
            Component c = todo.remove(0);
            if (c instanceof javax.swing.JButton && c.isVisible() && c.getName() != null
                    && SwingUtilities.getAncestorNamed("naviPanel", c) != null) {
                javax.swing.JButton b = (javax.swing.JButton) c;
                String text = b.getText() == null ? "" : b.getText().trim();
                if (!text.isEmpty() && !seen.contains(text) && SearchService.matches(text, needle)) {
                    l.add(new Command(text, Messages.SEARCH_NAVIGATION.toString(), b));
                    seen.add(text);
                }
            }
            if (c instanceof java.awt.Container) {
                for (Component x : ((java.awt.Container) c).getComponents()) {
                    todo.add(x);
                }
            }
        }
        return l;
    }

    /**
     * The settings whose name or description matches
     */
    private static List<Command> settings(String needle) {
        List<Command> l = new ArrayList<Command>();
        if (needle.trim().length() < 2) {
            return l;
        }
        try {
            for (final mpv5.ui.panels.MPControlPanel.Entry e : mpv5.ui.panels.MPControlPanel.entries()) {
                if (e.matches(needle) && l.size() < 8) {
                    l.add(new Command(e.title, e.description, () -> mpv5.ui.panels.MPControlPanel.show(e)));
                }
            }
            //the single options of the pages (check boxes, fields, cards)
            int options = 0;
            for (final mpv5.ui.panels.SettingsIndex.Option o : mpv5.ui.panels.SettingsIndex.options()) {
                if (options < 10 && SearchService.matches(o.text, needle)) {
                    l.add(new Command(o.text, Messages.CONTROL_PANEL + " \u203a " + o.entry.title,
                            () -> mpv5.ui.panels.SettingsIndex.show(o)));
                    options++;
                }
            }
        } catch (Exception ex) {
            Log.Debug(SearchAnything.class, ex);
        }
        return l;
    }

    private static void menu(JMenu m, String path, String needle, List<Command> l) {
        for (Component c : m.getMenuComponents()) {
            if (c instanceof JMenu) {
                menu((JMenu) c, path + " › " + ((JMenu) c).getText(), needle, l);
            } else if (c instanceof JMenuItem) {
                JMenuItem i = (JMenuItem) c;
                if (i.isEnabled() && i.getText() != null && !i.getText().trim().isEmpty()
                        && SearchService.matches(path + " " + i.getText(), needle)) {
                    l.add(new Command(i.getText().trim(), path, i));
                }
            }
        }
    }

    private void openSelected() {
        Object o = list.getSelectedValue();
        if (o == null || o instanceof Heading) {
            return;
        }
        close();
        if (o instanceof Command) {
            ((Command) o).run.run();
        } else if (o instanceof SearchService.Hit) {
            SearchService.Hit h = (SearchService.Hit) o;
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(h.context, h.id));
            } catch (Exception e) {
                Log.Debug(this, e);
                Popup.notice(Messages.NOT_POSSIBLE + ": " + h.title);
            }
        }
    }

    /**
     * Headings small and gray; results with title and details; commands
     * with their menu path
     */
    private static final class Renderer implements ListCellRenderer<Object> {

        private final JPanel row = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();
        private final JLabel heading = new JLabel();

        Renderer() {
            title.setFont(ModernTheme.font(1, Font.PLAIN));
            detail.setFont(ModernTheme.font(-1, Font.PLAIN));
            heading.setFont(ModernTheme.font(-1, Font.BOLD));
            heading.setForeground(ModernTheme.muted());
            heading.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(4), ModernTheme.px(2), 0));
            row.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), ModernTheme.px(10), ModernTheme.px(4), ModernTheme.px(10)));
            row.add(title, BorderLayout.CENTER);
            row.add(detail, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            if (value instanceof Heading) {
                heading.setText(((Heading) value).text.toUpperCase());
                return heading;
            }
            if (value instanceof SearchService.Hit) {
                title.setText(((SearchService.Hit) value).title);
                detail.setText(((SearchService.Hit) value).detail);
            } else if (value instanceof Command) {
                title.setText(((Command) value).title);
                detail.setText(((Command) value).path);
            }
            row.setBackground(selected ? ModernTheme.accent() : list.getBackground());
            title.setForeground(selected ? Color.WHITE : ModernTheme.foreground());
            detail.setForeground(selected ? new Color(225, 232, 250) : ModernTheme.muted());
            return row;
        }
    }

    /**
     * Double Shift (like IntelliJ) opens the search: two presses of Shift
     * alone within 400 ms
     */
    public static void installDoubleShift() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            private long lastShift;
            private boolean other;
            private boolean down;

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    if (e.getID() == KeyEvent.KEY_RELEASED) {
                        down = false;
                        return false;
                    }
                    if (e.getID() != KeyEvent.KEY_PRESSED || down) {
                        //auto repeat while Shift is held
                        return false;
                    }
                    down = true;
                    long now = e.getWhen();
                    if (!other && now - lastShift < 400 && now - lastShift > 30) {
                        lastShift = 0;
                        Window w = e.getComponent() == null ? null : SwingUtilities.getWindowAncestor(e.getComponent());
                        if (!(w instanceof SearchAnything)) {
                            SwingUtilities.invokeLater(SearchAnything::openCenter);
                        }
                        return false;
                    }
                    lastShift = now;
                    other = false;
                } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    other = true;
                    boolean ctrlOrMeta = e.isControlDown() || e.isMetaDown();
                    if (e.getKeyCode() == KeyEvent.VK_F && ctrlOrMeta && e.isShiftDown()) {
                        SwingUtilities.invokeLater(SearchAnything::openCenter);
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
