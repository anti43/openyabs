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
package mpv5.ui.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.Activity;
import mpv5.handler.Activity.Entry;
import mpv5.handler.Activity.Kind;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.ModernTheme;

/**
 * The status bar shows the latest activity; a click opens everything of this
 * session: created files (open, show in folder), created and changed records
 * (open) and the messages, searchable
 */
public final class ActivityButton extends JButton {

    private static final long serialVersionUID = 1L;
    private static final Color GREEN = new Color(0x2E, 0x7D, 0x32);
    private static final Color RED = new Color(0xC6, 0x28, 0x28);
    private static final Color ORANGE = new Color(0xE6, 0x7E, 0x00);
    private ActivityPanel panel;

    public ActivityButton() {
        setName("activity");
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusable(false);
        setFont(ModernTheme.font(-1, Font.PLAIN));
        setMargin(new java.awt.Insets(1, 6, 1, 6));
        setPreferredSize(new Dimension(ModernTheme.px(380), ModernTheme.px(24)));
        setMaximumSize(new Dimension(ModernTheme.px(520), ModernTheme.px(30)));
        setToolTipText(Messages.ACTIVITY_TITLE.toString());
        addActionListener(e -> toggle());
        Activity.addListener(e -> SwingUtilities.invokeLater(this::refresh));
        refresh();
    }

    private void refresh() {
        List<Entry> all = Activity.entries();
        if (all.isEmpty()) {
            setText(Messages.ACTIVITY_TITLE.toString());
            setIcon(null);
        } else {
            Entry e = all.get(0);
            setIcon(new Badge(e, ModernTheme.px(16)));
            setText(e.text + (all.size() > 1 ? "   (" + all.size() + ")" : ""));
            setToolTipText(e.text + (e.detail.isEmpty() ? "" : " · " + e.detail) + "  –  " + Messages.ACTIVITY_TITLE);
        }
        if (panel != null && panel.isVisible()) {
            panel.reload();
        }
    }

    private void toggle() {
        if (panel != null && panel.isVisible()) {
            panel.setVisible(false);
            return;
        }
        Window w = SwingUtilities.getWindowAncestor(this);
        if (panel == null || panel.getOwner() != w) {
            panel = new ActivityPanel(w);
        }
        panel.reload();
        panel.pack();
        Point p = getLocationOnScreen();
        Rectangle screen = getGraphicsConfiguration().getBounds();
        int x = Math.max(screen.x, Math.min(p.x + getWidth() - panel.getWidth(), screen.x + screen.width - panel.getWidth()));
        int y = Math.max(screen.y, p.y - panel.getHeight() - 2);
        panel.setLocation(x, y);
        panel.setVisible(true);
        panel.focusSearch();
    }

    // ------------------------------------------------------------------ actions
    static void open(Entry e) {
        if (e == null) {
            return;
        }
        if (e.file != null) {
            if (!e.file.exists()) {
                Popup.notice(Messages.ACTIVITY_GONE + "\n" + e.file.getAbsolutePath());
                return;
            }
            try {
                Desktop.getDesktop().open(e.file);
            } catch (Exception ex) {
                Log.Debug(ActivityButton.class, ex);
                Popup.notice(Messages.NOT_POSSIBLE + ": " + e.file.getAbsolutePath());
            }
        } else if (e.hasRecord()) {
            openRecord(e);
        }
    }

    static void openRecord(Entry e) {
        try {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(e.context, e.id));
        } catch (Exception ex) {
            Log.Debug(ActivityButton.class, ex);
            Popup.notice(Messages.NOT_POSSIBLE + ": " + e.text);
        }
    }

    static void folder(Entry e) {
        File f = e.file;
        if (f == null) {
            return;
        }
        try {
            if (f.exists() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                Desktop.getDesktop().browseFileDirectory(f);
            } else {
                Desktop.getDesktop().open(f.getAbsoluteFile().getParentFile());
            }
        } catch (Exception ex) {
            Log.Debug(ActivityButton.class, ex);
            Popup.notice(Messages.NOT_POSSIBLE + ": " + f.getParent());
        }
    }

    // ------------------------------------------------------------------ the list
    /**
     * The activity of this session above the status bar
     */
    static final class ActivityPanel extends JDialog {

        private static final long serialVersionUID = 1L;
        private final JTextField search = ModernTheme.roomy(new JTextField());
        private final DefaultListModel<Entry> model = new DefaultListModel<Entry>();
        private final JList<Entry> list = new JList<Entry>(model);
        private final JLabel status = new JLabel();
        private Kind[] filter = null;
        private static final Kind[] FILES = {Kind.FILE};
        private static final Kind[] RECORDS = {Kind.CREATED, Kind.CHANGED, Kind.DELETED};
        private static final Kind[] MESSAGES = {Kind.MESSAGE, Kind.WARNING};

        ActivityPanel(Window owner) {
            super(owner);
            setUndecorated(true);
            setModalityType(ModalityType.MODELESS);
            JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
            p.setBackground(ModernTheme.cardBackground());
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ModernTheme.border()), ModernTheme.padding(10, 12)));

            JPanel top = new JPanel(new BorderLayout(ModernTheme.px(8), ModernTheme.px(6)));
            top.setOpaque(false);
            JLabel title = new JLabel(Messages.ACTIVITY_TITLE.toString());
            title.setFont(ModernTheme.font(2, Font.BOLD));
            top.add(title, BorderLayout.WEST);
            JButton clear = new JButton(Messages.ACTIVITY_CLEAR.toString());
            clear.setBorderPainted(false);
            clear.setContentAreaFilled(false);
            clear.setForeground(ModernTheme.accent());
            clear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clear.addActionListener(e -> Activity.clear());
            top.add(clear, BorderLayout.EAST);

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
            filters.setOpaque(false);
            ButtonGroup g = new ButtonGroup();
            Object[][] chips = {{Messages.ACTIVITY_ALL, null}, {Messages.ACTIVITY_FILES, FILES},
            {Messages.ACTIVITY_RECORDS, RECORDS}, {Messages.ACTIVITY_MESSAGES, MESSAGES}};
            for (Object[] c : chips) {
                final Kind[] k = (Kind[]) c[1];
                JToggleButton b = new JToggleButton(c[0].toString(), k == null);
                b.putClientProperty("JButton.buttonType", "roundRect");
                b.setFocusable(false);
                b.addActionListener(e -> {
                    filter = k;
                    reload();
                });
                g.add(b);
                filters.add(b);
            }
            JPanel head = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
            head.setOpaque(false);
            head.add(top, BorderLayout.NORTH);
            search.putClientProperty("JTextField.placeholderText", Messages.ACTIVITY_SEARCH.toString());
            search.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
            head.add(search, BorderLayout.CENTER);
            head.add(filters, BorderLayout.SOUTH);
            p.add(head, BorderLayout.NORTH);

            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new Renderer());
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = list.locationToIndex(e.getPoint());
                    if (i < 0 || !list.getCellBounds(i, i).contains(e.getPoint())) {
                        return;
                    }
                    list.setSelectedIndex(i);
                    if (SwingUtilities.isRightMouseButton(e)) {
                        menu(model.get(i)).show(list, e.getX(), e.getY());
                    } else if (e.getClickCount() == 2) {
                        openSelected();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        mouseClicked(e);
                    }
                }
            });
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        openSelected();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        setVisible(false);
                    } else if (e.getKeyCode() == KeyEvent.VK_UP && list.getSelectedIndex() <= 0) {
                        search.requestFocusInWindow();
                    }
                }
            });
            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
            sp.setPreferredSize(new Dimension(ModernTheme.px(560), ModernTheme.px(400)));
            p.add(sp, BorderLayout.CENTER);

            status.setFont(ModernTheme.font(-1, Font.PLAIN));
            status.setForeground(ModernTheme.muted());
            p.add(status, BorderLayout.SOUTH);
            setContentPane(p);

            search.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    reload();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    reload();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    reload();
                }
            });
            search.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN && !model.isEmpty()) {
                        list.requestFocusInWindow();
                        list.setSelectedIndex(Math.max(0, list.getSelectedIndex()));
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (list.getSelectedIndex() < 0 && !model.isEmpty()) {
                            list.setSelectedIndex(0);
                        }
                        openSelected();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        setVisible(false);
                    }
                }
            });
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) {
                    //not while its own menu is open
                    Window opposite = e.getOppositeWindow();
                    if (opposite == null || opposite.getOwner() != ActivityPanel.this) {
                        setVisible(false);
                    }
                }
            });
        }

        void focusSearch() {
            SwingUtilities.invokeLater(() -> search.requestFocusInWindow());
        }

        void reload() {
            Entry sel = list.getSelectedValue();
            String n = search.getText().trim();
            model.clear();
            int files = 0, records = 0;
            for (Entry e : Activity.entries()) {
                if (e.kind == Kind.FILE) {
                    files++;
                } else if (e.context != null) {
                    records++;
                }
                if (filter != null && !java.util.Arrays.asList(filter).contains(e.kind)) {
                    continue;
                }
                if (!n.isEmpty() && !e.matches(n)) {
                    continue;
                }
                model.addElement(e);
            }
            if (sel != null && model.contains(sel)) {
                list.setSelectedValue(sel, true);
            }
            status.setText(model.isEmpty() ? Messages.ACTIVITY_EMPTY.toString()
                    : String.format(Messages.ACTIVITY_COUNT.toString(), files, records) + "  ·  " + Messages.ACTIVITY_TIP);
        }

        private void openSelected() {
            Entry e = list.getSelectedValue();
            if (e != null && (e.file != null || e.hasRecord())) {
                setVisible(false);
                open(e);
            }
        }

        private JPopupMenu menu(final Entry e) {
            JPopupMenu m = new JPopupMenu();
            if (e.file != null) {
                JMenuItem o = new JMenuItem(Messages.ACTIVITY_OPEN.toString());
                o.setEnabled(e.file.exists());
                o.addActionListener(a -> {
                    setVisible(false);
                    open(e);
                });
                m.add(o);
                JMenuItem f = new JMenuItem(Messages.ACTIVITY_FOLDER.toString());
                f.addActionListener(a -> {
                    setVisible(false);
                    folder(e);
                });
                m.add(f);
                JMenuItem c = new JMenuItem(Messages.ACTIVITY_COPY_PATH.toString());
                c.addActionListener(a -> Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(e.file.getAbsolutePath()), null));
                m.add(c);
            }
            if (e.hasRecord()) {
                JMenuItem r = new JMenuItem(e.file == null ? Messages.ACTIVITY_OPEN.toString()
                        : Messages.ACTIVITY_OPEN_RECORD + ": " + e.detail);
                r.addActionListener(a -> {
                    setVisible(false);
                    openRecord(e);
                });
                m.add(r);
            }
            if (m.getComponentCount() == 0) {
                JMenuItem c = new JMenuItem(Messages.ACTIVITY_COPY_TEXT.toString());
                c.addActionListener(a -> Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(e.text), null));
                m.add(c);
            }
            return m;
        }
    }

    /**
     * Badge, text, details and time
     */
    private static final class Renderer implements ListCellRenderer<Entry> {

        private final JPanel row = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        private final JLabel badge = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();
        private final JLabel time = new JLabel();
        private final SimpleDateFormat hm = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        Renderer() {
            JPanel texts = new JPanel(new BorderLayout(0, ModernTheme.px(1)));
            texts.setOpaque(false);
            texts.add(title, BorderLayout.NORTH);
            texts.add(detail, BorderLayout.SOUTH);
            row.add(badge, BorderLayout.WEST);
            row.add(texts, BorderLayout.CENTER);
            row.add(time, BorderLayout.EAST);
            row.setBorder(ModernTheme.padding(5, 8));
            time.setVerticalAlignment(SwingConstants.TOP);
            detail.setFont(ModernTheme.font(-1, Font.PLAIN));
            time.setFont(ModernTheme.font(-1, Font.PLAIN));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Entry> list, Entry e, int index, boolean selected, boolean focus) {
            badge.setIcon(new Badge(e, ModernTheme.px(26)));
            title.setText(e.text);
            boolean actionable = e.file != null || e.hasRecord();
            title.setFont(ModernTheme.font(0, actionable ? Font.BOLD : Font.PLAIN));
            String d = e.detail;
            if (e.file != null) {
                String where = e.file.getAbsoluteFile().getParent();
                d = (d.isEmpty() ? "" : d + "  ·  ") + (e.file.exists() ? where : Messages.ACTIVITY_GONE.toString());
            } else if (e.context != null) {
                String what = e.kind == Kind.CREATED ? Messages.ACTIVITY_CREATED.toString()
                        : e.kind == Kind.DELETED ? Messages.ACTIVITY_DELETED.toString() : Messages.ACTIVITY_CHANGED.toString();
                d = (d.isEmpty() ? "" : d + "  ·  ") + what;
            }
            detail.setText(d.isEmpty() ? " " : d);
            time.setText(hm.format(e.time));
            row.setOpaque(true);
            row.setBackground(selected ? ModernTheme.accent() : (index % 2 == 0 ? list.getBackground() : ModernTheme.blend(list.getBackground(), ModernTheme.border(), 0.25f)));
            Color fg = selected ? Color.WHITE : (e.kind == Kind.WARNING ? RED : ModernTheme.foreground());
            title.setForeground(fg);
            detail.setForeground(selected ? new Color(225, 232, 250) : ModernTheme.muted());
            time.setForeground(selected ? new Color(225, 232, 250) : ModernTheme.muted());
            row.setToolTipText(e.file != null ? e.file.getAbsolutePath() : null);
            return row;
        }
    }

    /**
     * A round badge: file type, + created, ✎ changed, − deleted, i message,
     * ! warning
     */
    static final class Badge implements Icon {

        private final String text;
        private final Color color;
        private final int size;

        Badge(Entry e, int size) {
            this.size = size;
            switch (e.kind) {
                case FILE:
                    String n = e.file.getName();
                    int dot = n.lastIndexOf('.');
                    text = dot < 0 ? "FILE" : n.substring(dot + 1).toUpperCase(Locale.ROOT);
                    color = "PDF".equals(text) ? RED : "XML".equals(text) ? ORANGE : ModernTheme.accent();
                    break;
                case CREATED:
                    text = "+";
                    color = GREEN;
                    break;
                case CHANGED:
                    text = "✎";
                    color = ModernTheme.accent();
                    break;
                case DELETED:
                    text = "−";
                    color = RED;
                    break;
                case WARNING:
                    text = "!";
                    color = ORANGE;
                    break;
                default:
                    text = "i";
                    color = ModernTheme.muted();
            }
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y, size, size, size / 2, size / 2);
            g2.setColor(Color.WHITE);
            float fs = text.length() > 2 ? size * 0.34f : text.length() == 2 ? size * 0.42f : size * 0.6f;
            g2.setFont(c.getFont().deriveFont(Font.BOLD, Math.max(7f, fs)));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            g2.drawString(text, x + (size - tw) / 2f, y + (size - fm.getHeight()) / 2f + fm.getAscent());
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

    /**
     * @return the component for the status bar
     */
    public static JComponent create() {
        return new ActivityButton();
    }
}
