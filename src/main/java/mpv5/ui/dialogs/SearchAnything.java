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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
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
    private final Timer delay;
    private int searches;

    /**
     * A command of the menu or the navigation
     */
    private static final class Command {

        final String title;
        final String path;
        final AbstractButton button;

        Command(String title, String path, AbstractButton button) {
            this.title = title;
            this.path = path;
            this.button = button;
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

    /**
     * Opens the search window over the main window
     */
    public static void open() {
        JFrame owner = (JFrame) mpv5.YabsViewProxy.instance().getIdentifierFrame();
        if (instance == null || instance.getOwner() != owner) {
            instance = new SearchAnything(owner);
        }
        instance.showOver(owner);
    }

    private SearchAnything(JFrame owner) {
        super(owner, Messages.SEARCH_ANYTHING.toString(), false);
        setUndecorated(true);
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        p.setBackground(ModernTheme.cardBackground());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(12), ModernTheme.px(8), ModernTheme.px(12))));

        JLabel title = new JLabel(Messages.SEARCH_ANYTHING.toString());
        title.setFont(ModernTheme.font(0, Font.BOLD));
        title.setForeground(ModernTheme.muted());
        field.setFont(ModernTheme.font(6, Font.PLAIN));
        field.putClientProperty("JTextField.placeholderText", Messages.SEARCH_ANYTHING_HINT.toString());
        field.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        JPanel top = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
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
        bind(KeyEvent.VK_ESCAPE, this::close);
        //a click elsewhere closes it, like a popup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                close();
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

    private void showOver(JFrame owner) {
        Point o = owner.getLocationOnScreen();
        setLocation(o.x + (owner.getWidth() - getWidth()) / 2, o.y + Math.max(ModernTheme.px(60), owner.getHeight() / 8));
        field.selectAll();
        setVisible(true);
        SwingUtilities.invokeLater(field::requestFocusInWindow);
        if (model.isEmpty()) {
            search();
        }
    }

    private void close() {
        delay.stop();
        setVisible(false);
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
            show(commands, new ArrayList<SearchService.Hit>(), needle);
            status.setText(Messages.SEARCH_ANYTHING_TIP.toString());
            return;
        }
        status.setText(Messages.PLEASE_WAIT.toString());
        new SwingWorker<List<SearchService.Hit>, Void>() {
            @Override
            protected List<SearchService.Hit> doInBackground() {
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
                    status.setText(hits.size() + commands.size() == 0
                            ? Messages.SEARCH_ANYTHING_NOTHING.toString()
                            : (hits.size() + commands.size()) + " " + Messages.SEARCH_ANYTHING_RESULTS + "  ·  " + Messages.SEARCH_ANYTHING_TIP);
                } catch (Exception e) {
                    Log.Debug(SearchAnything.this, e);
                    status.setText(Messages.ERROR_OCCURED.toString());
                }
            }
        }.execute();
    }

    private void show(List<Command> commands, List<SearchService.Hit> hits, String needle) {
        model.clear();
        String kind = null;
        for (SearchService.Hit h : hits) {
            if (!h.kind.equals(kind)) {
                kind = h.kind;
                model.addElement(new Heading(kind));
            }
            model.addElement(h);
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
        List<Component> todo = new ArrayList<Component>();
        todo.add(((JFrame) getOwner()).getContentPane());
        while (!todo.isEmpty()) {
            Component c = todo.remove(0);
            if (c instanceof javax.swing.JButton && c.getName() != null && c.getName().startsWith("jButton")
                    && SwingUtilities.getAncestorNamed("naviPanel", c) != null) {
                javax.swing.JButton b = (javax.swing.JButton) c;
                if (b.getText() != null && !b.getText().trim().isEmpty() && SearchService.matches(b.getText(), needle)) {
                    l.add(new Command(b.getText().trim(), Messages.SEARCH_NAVIGATION.toString(), b));
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
            ((Command) o).button.doClick();
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
                            SwingUtilities.invokeLater(SearchAnything::open);
                        }
                        return false;
                    }
                    lastShift = now;
                    other = false;
                } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    other = true;
                }
                return false;
            }
        });
    }
}
