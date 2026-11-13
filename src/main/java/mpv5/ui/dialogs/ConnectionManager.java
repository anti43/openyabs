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
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * The connection manager: all databases of the settings file with name, type
 * and place; sign in to one, create a new database (first start assistant),
 * add an existing one, rename, remove.
 */
public final class ConnectionManager extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * One connection of the settings file
     */
    static final class Conn {

        int id;
        String name;
        String driver;
        String path;
        String dbname;

        String type() {
            if (ConnectionTypeHandler.H2_DRIVER.equalsIgnoreCase(driver)) {
                return "H2";
            } else if (ConnectionTypeHandler.DERBY_DRIVER.equalsIgnoreCase(driver)) {
                return "Derby";
            } else if (ConnectionTypeHandler.MYSQL_DRIVER.equalsIgnoreCase(driver)
                    || ConnectionTypeHandler.MYSQL_DRIVER_CLASS.equalsIgnoreCase(driver)) {
                return "MySQL";
            } else if (ConnectionTypeHandler.POSTGRES_DRIVER.equalsIgnoreCase(driver)) {
                return "PostgreSQL";
            }
            return driver == null || "null".equals(driver) ? "?" : driver;
        }

        boolean local() {
            return "H2".equals(type()) || "Derby".equals(type());
        }

        /**
         * @return true if the database files exist (always true for servers)
         */
        boolean exists() {
            if ("H2".equals(type())) {
                return new File(path, dbname + ".mv.db").isFile();
            } else if ("Derby".equals(type())) {
                return new File(path, dbname).isDirectory();
            }
            return true;
        }
    }

    /**
     * the user name shown at the next start (remember the user)
     */
    private static final String LAST_NAME = "lastusername";
    private final boolean startup;
    private final DefaultListModel<Conn> model = new DefaultListModel<Conn>();
    private final JList<Conn> list = new JList<Conn>(model);
    private final JTextField user = ModernTheme.roomy(new JTextField(18));
    private final JPasswordField password = ModernTheme.roomy(new JPasswordField(18));
    private final JCheckBox remember = new JCheckBox(Messages.CONNMGR_REMEMBER.toString(), true);
    private final JLabel problem = new JLabel(" ");
    /**
     * At start: the database used last, or always one of the list
     */
    private final JComboBox<Object> startChoice = new JComboBox<Object>();
    private boolean loading;
    /**
     * the connection that opens at start (marked in the list), null: none
     */
    private static Integer startId;

    /**
     * Shows the connection manager
     *
     * @param startup true before YaBS runs: closing exits YaBS
     */
    public static void open(boolean startup) {
        ConnectionManager cm = new ConnectionManager(startup);
        do {
            cm.reopen = false;
            cm.setVisible(true);
            //"new database": the dialog is hidden while the first start assistant runs. Returning
            //now let the start of YaBS go on with the new, still empty connection ("null/null")
            java.awt.SecondaryLoop setup = cm.setupRunning;
            if (setup != null) {
                setup.enter();
                cm.setupRunning = null;
            }
        } while (cm.reopen);
    }

    /**
     * the first start assistant for a new database runs, {@link #open} waits
     * for it
     */
    private java.awt.SecondaryLoop setupRunning;
    /**
     * show the dialog again (the new database was cancelled or failed)
     */
    private boolean reopen;

    /**
     * The first start assistant has finished (or was cancelled): {@link #open}
     * goes on
     *
     * @param showAgain true to show the connection manager again
     */
    private void setupEnded(boolean showAgain) {
        reopen = showAgain;
        java.awt.SecondaryLoop setup = setupRunning;
        if (setup != null) {
            setup.exit();
        } else if (showAgain) {
            setVisible(true);
        }
    }

    private ConnectionManager(boolean startup) {
        super((java.awt.Frame) null, Messages.CONNMGR_TITLE.toString(), ModalityType.APPLICATION_MODAL);
        this.startup = startup;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                if (ConnectionManager.this.startup) {
                    Main.getApplication().exit();
                }
            }
        });
        try {
            setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Constants.ICON)));
        } catch (Exception e) {
            Log.Debug(this, e);
        }

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new Renderer());
        list.setOpaque(false);
        list.addListSelectionListener(e -> selected());
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    password.requestFocusInWindow();
                }
            }
        });
        JScrollPane sp = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        list.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                list.setToolTipText(i < 0 ? null : place(model.get(i)));
            }
        });
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.setPreferredSize(new Dimension(ModernTheme.px(380), ModernTheme.px(260)));

        JButton add = new JButton(Messages.CONNMGR_ADD.toString());
        add.addActionListener(e -> addExisting());
        JButton rename = new JButton(Messages.CONNMGR_RENAME.toString());
        rename.addActionListener(e -> rename());
        JButton remove = Applets.danger(new JButton(Messages.CONNMGR_REMOVE.toString()));
        remove.addActionListener(e -> remove());
        JButton create = new JButton(Messages.CONNMGR_NEW.toString());
        create.addActionListener(e -> createNew());
        //two rows: in one row the last button was cut off
        JPanel listButtons = new JPanel(new java.awt.GridLayout(2, 2, ModernTheme.px(6), ModernTheme.px(6)));
        listButtons.setOpaque(false);
        listButtons.add(create);
        listButtons.add(add);
        listButtons.add(rename);
        listButtons.add(remove);

        startChoice.setRenderer(new javax.swing.DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean focus) {
                return super.getListCellRendererComponent(l, v instanceof Conn ? ((Conn) v).name : v, i, sel, focus);
            }
        });
        startChoice.addActionListener(e -> {
            if (loading) {
                return;
            }
            Object v = startChoice.getSelectedItem();
            mpv5.globals.ConnectionStart.setChoice(v instanceof Conn ? ((Conn) v).id : mpv5.globals.ConnectionStart.LAST);
            startId = mpv5.globals.ConnectionStart.atStart();
            list.repaint();
        });
        JPanel startRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        startRow.setOpaque(false);
        startRow.add(new JLabel(Messages.CONNMGR_START + ":"), BorderLayout.WEST);
        startRow.add(startChoice, BorderLayout.CENTER);

        Card databases = new Card(Messages.CONNMGR_LIST.toString()).addFull(sp, 1).addFull(listButtons).addFull(startRow);
        remember.setOpaque(false);
        problem.setForeground(Applets.DANGER);
        password.addActionListener(e -> connect());
        Card login = new Card(Messages.CONNMGR_LOGIN.toString())
                .row(Card.field(Messages.CONNMGR_USER.toString(), user))
                .row(Card.field(Messages.CONNMGR_PASSWORD.toString(), password))
                .addFull(remember)
                .addFull(problem);

        PrimaryButton connect = new PrimaryButton(Messages.CONNMGR_CONNECT.toString(), "/images/16/forward.png");
        connect.addActionListener(e -> connect());
        getRootPane().setDefaultButton(connect);

        JPanel content = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        content.setBackground(ModernTheme.background());
        content.setBorder(ModernTheme.padding(14, 16));
        content.add(Applets.page(Messages.CONNMGR_INFO.toString(), 420, databases, login), BorderLayout.CENTER);
        content.add(Applets.actionBar(new JComponent[0], new JComponent[]{connect}), BorderLayout.SOUTH);
        setContentPane(content);

        load(LocalSettings.getConnectionID());
        //no database access here: at the start the connection is closed
        String last = LocalSettings.getProperty(LAST_NAME);
        user.setText(last == null || "null".equals(last) ? "" : last);
        pack();
        setMinimumSize(new Dimension(ModernTheme.px(520), ModernTheme.px(560)));
        setLocationRelativeTo(null);
        java.awt.EventQueue.invokeLater(() -> (user.getText().isEmpty() ? user : password).requestFocusInWindow());
    }

    /**
     * @return all connections of the settings file
     */
    static List<Conn> connections() {
        List<Conn> l = new ArrayList<Conn>();
        try {
            java.util.Map<Integer, String> names = LocalSettings.getConnectionNames();
            for (PropertyStore p : LocalSettings.getConnectionStores()) {
                Conn c = new Conn();
                c.id = p.getProperty("nodeid", 0);
                c.name = names.get(c.id);
                c.driver = p.getProperty(LocalSettings.DBDRIVER);
                c.path = p.getProperty(LocalSettings.DBPATH);
                c.dbname = p.getProperty(LocalSettings.DBNAME);
                l.add(c);
            }
        } catch (Exception e) {
            Log.Debug(ConnectionManager.class, e);
        }
        l.sort((a, b) -> Integer.compare(a.id, b.id));
        return l;
    }

    private void load(int select) {
        model.clear();
        loading = true;
        startChoice.removeAllItems();
        startChoice.addItem(Messages.CONNMGR_START_LAST.toString());
        int choice = mpv5.globals.ConnectionStart.getChoice();
        for (Conn c : connections()) {
            model.addElement(c);
            startChoice.addItem(c);
            if (c.id == choice) {
                startChoice.setSelectedItem(c);
            }
            if (c.id == select) {
                list.setSelectedIndex(model.size() - 1);
            }
        }
        if (choice != mpv5.globals.ConnectionStart.LAST && !(startChoice.getSelectedItem() instanceof Conn)) {
            //the database that opened at start was removed: the one used last again
            mpv5.globals.ConnectionStart.setChoice(mpv5.globals.ConnectionStart.LAST);
            startChoice.setSelectedIndex(0);
        }
        loading = false;
        startId = mpv5.globals.ConnectionStart.atStart();
        if (list.getSelectedIndex() < 0 && !model.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    private void selected() {
        problem.setText(" ");
    }

    private Conn current() {
        return list.getSelectedValue();
    }

    private static boolean isActive(Conn c) {
        return c != null && c.id == LocalSettings.getConnectionID() && DatabaseConnection.getConnector() != null
                && DatabaseConnection.getConnector().getConnection() != null;
    }

    /**
     * Switches to the selected connection and signs in; back to the previous
     * one if that fails
     */
    private void connect() {
        Conn c = current();
        if (c == null) {
            return;
        }
        if (!c.exists()) {
            problem.setText(String.format(Messages.CONNMGR_NOT_FOUND.toString(), new File(c.path, c.dbname).getPath()));
            return;
        }
        int before = LocalSettings.getConnectionID();
        boolean switched = c.id != before || DatabaseConnection.getConnector() == null
                || DatabaseConnection.getConnector().getConnection() == null;
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        try {
            if (switched) {
                switchTo(c.id);
            }
            User u = mpv5.usermanagement.MPSecurityManager.checkAuth(user.getText(), new String(password.getPassword()));
            if (u == null) {
                problem.setText(Messages.CONNMGR_DENIED.toString());
                return;
            }
            u.login();
            LocalSettings.setProperty(LocalSettings.LAST_USER, remember.isSelected() ? String.valueOf(u.__getIDS()) : "INSTANCE");
            LocalSettings.setProperty(LAST_NAME, remember.isSelected() ? user.getText().trim() : "null");
            LocalSettings.save();
            mpv5.globals.ConnectionStart.used(c.id);
            dispose();
            if (switched && !startup) {
                mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.CONNMGR_SWITCHED.toString(), c.name));
            }
        } catch (Exception e) {
            Log.Debug(this, e);
            problem.setText("<html>" + Messages.CONNMGR_FAILED + " " + e.getMessage());
            if (switched && before != c.id) {
                try {
                    switchTo(before);
                } catch (Exception ex) {
                    Log.Debug(this, ex);
                }
            }
        } finally {
            setCursor(null);
        }
    }

    /**
     * Uses the settings of the connection and connects to its database
     *
     * @param id
     * @throws Exception
     */
    static void switchTo(int id) throws Exception {
        if (LocalSettings.getConnectionID() != id) {
            LocalSettings.save();
        }
        LocalSettings.setConnectionID(id);
        LocalSettings.read();
        LocalSettings.apply();
        DatabaseConnection.shutdown();
        DatabaseConnection.instanceOf();
        //version check and updates of this database, like at start
        QueryHandler.restart();
        GlobalSettings.read();
        mpv5.db.objects.ValueProperty.clearScripts();
    }

    /**
     * A new database: a new connection, set up by the first start assistant
     */
    private void createNew() {
        String name = JOptionPane.showInputDialog(this, Messages.CONNMGR_NAME.toString(),
                Messages.CONNMGR_NEW_NAME + " " + (model.size() + 1));
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            final int before = LocalSettings.getConnectionID();
            PropertyStore p = new PropertyStore();
            p.addProperty(LocalSettings.CONNECTION_NAME, name.trim());
            final int id = LocalSettings.addConnection(p);
            LocalSettings.save();
            LocalSettings.setConnectionID(id);
            LocalSettings.read();
            setupRunning = java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
            setVisible(false);
            FirstStart.open(id, () -> {
                try {
                    QueryHandler.restart();
                    GlobalSettings.read();
                    mpv5.globals.ConnectionStart.used(id);
                    User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
                    admin.login();
                    LocalSettings.save();
                    dispose();
                    //at start: YaBS goes on starting (signed in, the new database); a second
                    //Main.start() here started YaBS twice
                    if (!startup) {
                        mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.CONNMGR_CREATED.toString(), name.trim()));
                    }
                    setupEnded(false);
                } catch (Exception e) {
                    Log.Debug(ConnectionManager.class, e);
                    Popup.notice(Messages.CONNMGR_FAILED + "\n" + e.getMessage());
                    load(id);
                    setupEnded(true);
                }
            }, () -> {
                //cancelled: back to the previous connection, the empty entry is removed
                try {
                    LocalSettings.removeInstance(id);
                    LocalSettings.setConnectionID(before);
                    LocalSettings.read();
                    LocalSettings.apply();
                } catch (Exception e) {
                    Log.Debug(ConnectionManager.class, e);
                }
                load(before);
                setupEnded(true);
            });
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.notice(Messages.CONNMGR_FAILED + "\n" + e.getMessage());
            setupEnded(!isVisible());
        }
    }

    /**
     * Adds a connection to an existing local database (H2 or Derby folder)
     */
    private void addExisting() {
        final JTextField name = ModernTheme.roomy(new JTextField(Messages.CONNMGR_NEW_NAME + " " + (model.size() + 1), 22));
        final JComboBox<String> type = new JComboBox<String>(new String[]{"H2", "Derby"});
        final JTextField folder = ModernTheme.roomy(new JTextField(Main.MPPATH, 22));
        final JTextField dbname = ModernTheme.roomy(new JTextField(Constants.DATABASENAME, 14));
        final JTextField dbuser = ModernTheme.roomy(new JTextField(14));
        final JPasswordField dbpassword = ModernTheme.roomy(new JPasswordField(14));
        JButton choose = new JButton("...");
        choose.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(folder.getText());
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f.isFile() && f.getName().endsWith(".mv.db")) {
                    //H2: the file name is the database name
                    type.setSelectedIndex(0);
                    folder.setText(f.getParent());
                    dbname.setText(f.getName().substring(0, f.getName().length() - ".mv.db".length()));
                } else if (new File(f, "service.properties").isFile()) {
                    //Derby: the database is the folder
                    type.setSelectedIndex(1);
                    folder.setText(f.getParent());
                    dbname.setText(f.getName());
                } else {
                    folder.setText(f.isDirectory() ? f.getPath() : f.getParent());
                }
            }
        });
        JPanel where = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        where.setOpaque(false);
        where.add(folder, BorderLayout.CENTER);
        where.add(choose, BorderLayout.EAST);
        Card form = new Card(Messages.CONNMGR_ADD.toString())
                .row(Card.field(Messages.CONNMGR_NAME.toString(), name))
                .row(Card.field(Messages.CONNMGR_TYPE.toString(), type), Card.field(Messages.CONNMGR_DBNAME.toString(), dbname))
                .row(Card.field(Messages.CONNMGR_FOLDER.toString(), where))
                .row(Card.field(Messages.CONNMGR_DBUSER.toString(), dbuser), Card.field(Messages.CONNMGR_DBPASSWORD.toString(), dbpassword))
                .addFull(Applets.hint(Messages.CONNMGR_ADD_HINT.toString()));
        while (JOptionPane.showConfirmDialog(this, form, Messages.CONNMGR_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            Conn c = new Conn();
            c.driver = type.getSelectedIndex() == 0 ? ConnectionTypeHandler.H2_DRIVER : ConnectionTypeHandler.DERBY_DRIVER;
            c.path = folder.getText().trim();
            c.dbname = dbname.getText().trim();
            if (!c.exists()) {
                Popup.notice(String.format(Messages.CONNMGR_NOT_FOUND.toString(), new File(c.path, c.dbname).getPath()));
                continue;
            }
            try {
                //like the current connection (office, folders ...), with the other database
                PropertyStore p = new PropertyStore();
                p.addAll(LocalSettings.getPropertyStore().getList().toArray(new String[][]{}));
                p.changeProperty(LocalSettings.CONNECTION_NAME, name.getText().trim());
                p.changeProperty(LocalSettings.DBDRIVER, c.driver);
                p.changeProperty(LocalSettings.DBPATH, c.path);
                p.changeProperty(LocalSettings.DBNAME, c.dbname);
                p.changeProperty(LocalSettings.DBUSER, dbuser.getText().trim());
                p.changeProperty(LocalSettings.DBPASSWORD, new String(dbpassword.getPassword()));
                p.changeProperty(LocalSettings.DBPREFIX, "");
                p.changeProperty(LocalSettings.LAST_USER, "INSTANCE");
                p.changeProperty(LocalSettings.LAST_USER_PW, "null");
                p.changeProperty(mpv5.db.common.DatabaseBackup.KEY_ON_EXIT, "false");
                load(LocalSettings.addConnection(p));
            } catch (Exception e) {
                Log.Debug(this, e);
                Popup.notice(Messages.CONNMGR_FAILED + "\n" + e.getMessage());
            }
            return;
        }
    }

    private void rename() {
        Conn c = current();
        if (c == null) {
            return;
        }
        String n = (String) JOptionPane.showInputDialog(this, Messages.CONNMGR_NAME.toString(), Messages.CONNMGR_RENAME.toString(),
                JOptionPane.PLAIN_MESSAGE, null, null, c.name.replaceFirst("\\s*\\(\\d+\\)$", ""));
        if (n != null && !n.trim().isEmpty()) {
            try {
                LocalSettings.renameConnection(c.id, n.trim());
                load(c.id);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }

    private void remove() {
        Conn c = current();
        if (c == null) {
            return;
        }
        if (c.id == LocalSettings.getConnectionID()) {
            Popup.notice(Messages.CONNMGR_REMOVE_CURRENT.toString());
            return;
        }
        if (Popup.Y_N_dialog(String.format(Messages.CONNMGR_REMOVE_ASK.toString(), c.name), Messages.CONNMGR_REMOVE.toString())) {
            try {
                LocalSettings.removeInstance(c.id);
            } catch (Exception e) {
                Log.Debug(this, e);
            }
            load(LocalSettings.getConnectionID());
        }
    }

    static String place(Conn c) {
        return c.local() ? new File(c.path == null ? "" : c.path, c.dbname == null ? "" : c.dbname).getPath()
                : c.path + (c.dbname == null || "null".equals(c.dbname) ? "" : " / " + c.dbname);
    }

    /**
     * @return the text with "..." in the middle if longer than max
     */
    static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max / 3) + "\u2026" + s.substring(s.length() - (max - max / 3 - 1));
    }

    /**
     * Name in bold, type and place below
     */
    private static final class Renderer extends JPanel implements ListCellRenderer<Conn> {

        private static final long serialVersionUID = 1L;
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();

        Renderer() {
            super(new BorderLayout(0, ModernTheme.px(2)));
            setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(8), ModernTheme.px(6), ModernTheme.px(8)));
            title.setFont(ModernTheme.font(1, Font.BOLD));
            detail.setFont(ModernTheme.font(-1, Font.PLAIN));
            add(title, BorderLayout.NORTH);
            add(detail, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Conn> l, Conn c, int index, boolean selected, boolean focus) {
            String place = shorten(place(c), 48);
            title.setText(c.name + (isActive(c) ? "  • " + Messages.CONNMGR_CURRENT : "")
                    + (startId != null && startId == c.id ? "  • " + Messages.CONNMGR_DEFAULT_MARK : ""));
            detail.setText(c.type() + "  ·  " + place + (c.exists() ? "" : "  –  ?"));
            setBackground(selected ? ModernTheme.accentSoft() : ModernTheme.cardBackground());
            title.setForeground(ModernTheme.foreground());
            detail.setForeground(c.exists() ? ModernTheme.muted() : Applets.DANGER);
            setOpaque(true);
            return this;
        }
    }
}
