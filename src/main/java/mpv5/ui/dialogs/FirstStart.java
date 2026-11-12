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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.FirstStartSetup;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.logging.YConsole;
import mpv5.ui.dialogs.subcomponents.wizard_DBSettings_1;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * The first start: one window, the database on this computer is one click
 * away, a database server is possible. The setup (database, countries,
 * languages, template) runs in the background, then YaBS starts with the
 * administrator signed in.
 */
public final class FirstStart extends JFrame {

    private static final long serialVersionUID = 1L;
    private final Integer forConnId;
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    //where
    private final Tile local = new Tile(tr(Messages.FIRSTSTART_LOCAL), "");
    private final Tile server = new Tile(tr(Messages.FIRSTSTART_SERVER), tr(Messages.FIRSTSTART_SERVER_HINT));
    private final JLabel folderLabel = new JLabel();
    /**
     * the H2 database of a new installation: &lt;user home&gt;/.yabs/h2
     */
    private File folder = new File(Main.MPPATH == null ? System.getProperty("user.home") : Main.MPPATH, "h2");
    //server
    private final JComboBox<String> type = new JComboBox<String>(new String[]{
        tr(Messages.FIRSTSTART_TYPE_MYSQL), tr(Messages.FIRSTSTART_TYPE_POSTGRES), tr(Messages.FIRSTSTART_TYPE_DERBY), tr(Messages.FIRSTSTART_TYPE_OTHER)});
    private final JTextField address = ModernTheme.roomy(new JTextField("localhost:3306"));
    private final JTextField dbname = ModernTheme.roomy(new JTextField());
    private final JTextField user = ModernTheme.roomy(new JTextField());
    private final JPasswordField password = ModernTheme.roomy(new JPasswordField());
    private final JTextField prefix = ModernTheme.roomy(new JTextField());
    private final JCheckBox shared = new JCheckBox(tr(Messages.FIRSTSTART_SHARED), true);
    private final JCheckBox existing = new JCheckBox(tr(Messages.FIRSTSTART_EXISTING));
    private final Card serverCard = new Card(null);
    //progress
    private final Step[] steps = {
        new Step(tr(Messages.FIRSTSTART_CONNECTING)),
        new Step(tr(Messages.FIRSTSTART_CREATING)),
        new Step(tr(Messages.FIRSTSTART_COUNTRIES)),
        new Step(tr(Messages.FIRSTSTART_LANGUAGES)),
        new Step(tr(Messages.FIRSTSTART_TEMPLATES)),
        new Step(tr(Messages.FIRSTSTART_LAUNCH))};
    private final JProgressBar bar = new JProgressBar();
    private final JTextArea error = text("");
    private final JPanel errorButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
    private final PrimaryButton start = new PrimaryButton(tr(Messages.FIRSTSTART_START), null);

    /**
     * Shows the first start
     *
     * @param forConnId the connection to configure, null for the default one
     */
    public static void open(Integer forConnId) {
        if (!(javax.swing.UIManager.getLookAndFeel() instanceof mpv5.ui.modern.YabsLookAndFeel)) {
            Main.setLaF("");
        }
        FirstStart f = new FirstStart(forConnId, null, null);
        f.setVisible(true);
        f.toFront();
    }

    /**
     * Sets up a new database while YaBS runs (connection manager): instead of
     * starting YaBS at the end, done is run; closing before the start runs
     * cancelled instead of exiting
     *
     * @param forConnId the (new) connection
     * @param done after the database was created (connected to it)
     * @param cancelled closed without a database
     */
    public static void open(Integer forConnId, Runnable done, Runnable cancelled) {
        FirstStart f = new FirstStart(forConnId, done, cancelled);
        f.setVisible(true);
        f.toFront();
    }

    private final Runnable whenDone;
    private final Runnable whenCancelled;

    private FirstStart(Integer forConnId, Runnable done, Runnable cancelled) {
        super(Constants.TITLE);
        this.forConnId = forConnId;
        this.whenDone = done;
        this.whenCancelled = cancelled;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (start.isEnabled() || error.isVisible()) {
                    if (whenCancelled != null) {
                        dispose();
                        whenCancelled.run();
                    } else {
                        System.exit(1);
                    }
                }
            }
        });
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Constants.ICON));
            setIconImage(icon);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        String name = Constants.DATABASENAME + (forConnId == null ? "" : String.valueOf(forConnId));
        dbname.setText(name);

        content.setBackground(ModernTheme.background());
        content.add(form(), "form");
        content.add(progress(), "progress");
        setContentPane(content);
        setMinimumSize(new Dimension(ModernTheme.px(560), ModernTheme.px(460)));
        pack();
        setSize(Math.max(getWidth(), ModernTheme.px(640)), getHeight());
        setLocationRelativeTo(null);
        choose(false);
    }

    // ---------------------------------------------------------------- form
    private JPanel form() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(14)));
        p.setOpaque(false);
        p.setBorder(ModernTheme.padding(26, 30));

        JPanel head = column();
        JLabel title = new JLabel(tr(Messages.FIRSTSTART_TITLE));
        title.setFont(ModernTheme.font(11, Font.BOLD));
        head.add(title);
        head.add(Box.createVerticalStrut(ModernTheme.px(6)));
        head.add(text(tr(Messages.FIRSTSTART_INTRO)));
        p.add(head, BorderLayout.NORTH);

        JPanel where = column();
        JLabel q = new JLabel(tr(Messages.FIRSTSTART_WHERE));
        q.setFont(ModernTheme.font(1, Font.BOLD));
        where.add(q);
        where.add(Box.createVerticalStrut(ModernTheme.px(8)));
        ButtonGroup g = new ButtonGroup();
        g.add(local.radio);
        g.add(server.radio);

        JButton change = ModernTheme.actionButton(new JButton(tr(Messages.FIRSTSTART_CHANGE)), null);
        change.addActionListener(e -> chooseFolder());
        JPanel folderRow = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        folderRow.setOpaque(false);
        folderLabel.setForeground(ModernTheme.muted());
        folderRow.add(folderLabel, BorderLayout.CENTER);
        folderRow.add(change, BorderLayout.EAST);
        //developer mode: no folder, the database is in memory
        change.setVisible(!mpv5.db.common.ConnectionTypeHandler.DEVMODE);
        local.extra(folderRow);
        showFolder();
        where.add(local);
        where.add(Box.createVerticalStrut(ModernTheme.px(8)));

        serverCard.flat();
        serverCard.row(Card.field(tr(Messages.FIRSTSTART_TYPE), type, 1), Card.field(tr(Messages.FIRSTSTART_ADDRESS), address, 2));
        serverCard.row(Card.field(tr(Messages.FIRSTSTART_DBNAME), dbname), Card.field(tr(Messages.FIRSTSTART_PREFIX), prefix));
        serverCard.row(Card.field(tr(Messages.FIRSTSTART_USER), user), Card.field(tr(Messages.FIRSTSTART_PASSWORD), password));
        shared.setOpaque(false);
        existing.setOpaque(false);
        serverCard.addFull(shared);
        serverCard.addFull(existing);
        JButton more = link(tr(Messages.FIRSTSTART_MORE));
        more.addActionListener(e -> advanced());
        JPanel moreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        moreRow.setOpaque(false);
        moreRow.add(more);
        serverCard.addFull(moreRow);
        server.extra(serverCard);
        where.add(server);
        type.addActionListener(e -> typeChanged());

        local.radio.addActionListener(e -> choose(false));
        server.radio.addActionListener(e -> choose(true));
        p.add(where, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout(ModernTheme.px(16), 0));
        foot.setOpaque(false);
        JTextArea auto = text(tr(Messages.FIRSTSTART_AUTO));
        auto.setForeground(ModernTheme.muted());
        auto.setFont(ModernTheme.font(-1, Font.PLAIN));
        foot.add(auto, BorderLayout.CENTER);
        start.addActionListener(e -> go());
        JPanel startRow = new JPanel(new GridBagLayout());
        startRow.setOpaque(false);
        startRow.add(start);
        foot.add(startRow, BorderLayout.EAST);
        p.add(foot, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(start);
        return p;
    }

    private void choose(boolean onServer) {
        local.setSelected(!onServer);
        server.setSelected(onServer);
        serverCard.setVisible(onServer);
        if (isShowing()) {
            Dimension pref = getPreferredSize();
            if (pref.height > getHeight()) {
                setSize(getWidth(), pref.height);
            }
            validate();
            repaint();
        }
    }

    private void typeChanged() {
        switch (type.getSelectedIndex()) {
            case 0:
                address.setText("localhost:3306");
                shared.setSelected(true);
                break;
            case 1:
                address.setText("localhost:5432");
                shared.setSelected(true);
                break;
            case 2:
                address.setText(folder.getAbsolutePath());
                break;
            default:
                address.setText("jdbc:<driver>://<host>:<port>");
        }
    }

    private void chooseFolder() {
        JFileChooser c = new JFileChooser(folder);
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && c.getSelectedFile() != null) {
            folder = c.getSelectedFile();
            showFolder();
        }
    }

    private void showFolder() {
        if (mpv5.db.common.ConnectionTypeHandler.DEVMODE) {
            folderLabel.setText("<html>" + tr(Messages.FIRSTSTART_DEVMODE));
            folderLabel.setToolTipText(null);
            return;
        }
        folderLabel.setText(String.format(tr(Messages.FIRSTSTART_LOCAL_HINT), folder.getAbsolutePath()));
        folderLabel.setToolTipText(folder.getAbsolutePath());
    }

    private void advanced() {
        Wizard w = new Wizard(true);
        w.addPanel(new wizard_DBSettings_1(w, forConnId));
        w.showWiz();
        dispose();
    }

    // ---------------------------------------------------------------- progress
    private JPanel progress() {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(18)));
        p.setOpaque(false);
        p.setBorder(ModernTheme.padding(30, 34));
        JLabel title = new JLabel(tr(Messages.FIRSTSTART_WORKING));
        title.setFont(ModernTheme.font(9, Font.BOLD));
        p.add(title, BorderLayout.NORTH);

        JPanel list = column();
        for (Step s : steps) {
            list.add(s);
            list.add(Box.createVerticalStrut(ModernTheme.px(10)));
        }
        list.add(Box.createVerticalStrut(ModernTheme.px(6)));
        bar.setAlignmentX(LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.px(8)));
        bar.setIndeterminate(true);
        list.add(bar);
        list.add(Box.createVerticalStrut(ModernTheme.px(14)));
        error.setForeground(new Color(0xC6, 0x28, 0x28));
        error.setVisible(false);
        list.add(error);
        p.add(list, BorderLayout.CENTER);

        errorButtons.setOpaque(false);
        JButton log = ModernTheme.actionButton(new JButton(tr(Messages.FIRSTSTART_LOG)), null);
        log.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(YConsole.getLogfile());
            } catch (Exception ex) {
                Log.Debug(this, ex);
            }
        });
        PrimaryButton back = new PrimaryButton(tr(Messages.FIRSTSTART_BACK), null);
        back.addActionListener(e -> {
            pages.show(content, "form");
            start.setEnabled(true);
        });
        errorButtons.add(log);
        errorButtons.add(back);
        errorButtons.setVisible(false);
        p.add(errorButtons, BorderLayout.SOUTH);
        return p;
    }

    // ---------------------------------------------------------------- setup
    private void go() {
        final Settings s = new Settings();
        if (server.isSelected()) {
            //MySQL, PostgreSQL, Derby, other
            s.driver = ConnectionTypeHandler.DRIVERS[new int[]{2, 4, 1, 3}[type.getSelectedIndex()]];
            s.url = address.getText().trim();
            s.user = user.getText().trim();
            s.password = new String(password.getPassword());
            s.dbname = dbname.getText().trim();
            s.prefix = prefix.getText().trim();
            s.network = shared.isSelected();
            s.onlyConnect = existing.isSelected();
        } else {
            s.driver = ConnectionTypeHandler.H2_DRIVER;
            s.url = folder.getAbsolutePath();
            folder.mkdirs();
            s.user = "";
            s.password = "";
            s.dbname = dbname.getText().trim();
            s.prefix = "";
            s.network = false;
            s.onlyConnect = false;
        }
        if (s.url.isEmpty() || s.dbname.isEmpty()) {
            return;
        }
        start.setEnabled(false);
        for (Step st : steps) {
            st.set(Step.TODO);
        }
        error.setVisible(false);
        errorButtons.setVisible(false);
        bar.setIndeterminate(true);
        bar.setVisible(true);
        pages.show(content, "progress");

        new SwingWorker<Boolean, Object[]>() {
            private String problem;

            void at(int step) {
                publish(new Object[]{step});
            }

            @Override
            protected Boolean doInBackground() {
                try {
                    at(0);
                    DatabaseConnection conn = connect(s);
                    if (conn == null) {
                        problem = tr(Messages.CONNECTION_FAILED);
                        return false;
                    }
                    boolean exists;
                    try {
                        exists = conn.runQueries(new String[]{"select * from groups where ids = 1"});
                    } catch (Exception e) {
                        exists = false;
                    }
                    if (s.onlyConnect && !exists) {
                        problem = tr(Messages.FIRSTSTART_NO_YABS);
                        return false;
                    }
                    boolean created = false;
                    if (!exists) {
                        at(1);
                        conn.setProgressbar(bar);
                        if (!(conn.runQueries(new DatabaseInstallation().getStructure())
                                && conn.runQueries(new DatabaseInstallation().getInitialData()))) {
                            problem = tr(Messages.CREATING_DATABASE_FAILED);
                            return false;
                        }
                        created = true;
                    }
                    cacheDir();
                    if (created) {
                        prepare();
                    }
                    at(5);
                    return true;
                } catch (Throwable e) {
                    Log.Debug(FirstStart.class, e);
                    problem = tr(Messages.CONNECTION_FAILED) + "\n" + e.getMessage();
                    return false;
                }
            }

            private void prepare() throws Exception {
                User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
                User before = User.getCurrentUser();
                User._setUser(admin);
                try {
                    FirstStartSetup.prepare(admin, LanguageManager.isGermanUser(null), new FirstStartSetup.Progress() {
                        @Override
                        public void step(FirstStartSetup.Step st) {
                            at(2 + st.ordinal());
                        }
                    });
                    //signed in as admin at the start
                    LocalSettings.setProperty(LocalSettings.LAST_USER, String.valueOf(admin.__getIDS()));
                    LocalSettings.setProperty(LocalSettings.LAST_USER_PW, admin.__getPassword());
                    LocalSettings.save(forConnId);
                    System.setProperty("yabs_welcome", "true");
                } finally {
                    User._setUser(before);
                }
            }

            @Override
            protected void process(List<Object[]> chunks) {
                int now = (Integer) chunks.get(chunks.size() - 1)[0];
                for (int i = 0; i < steps.length; i++) {
                    steps[i].set(i < now ? Step.DONE : i == now ? Step.NOW : Step.TODO);
                }
                if (now != 1) {
                    bar.setIndeterminate(true);
                }
            }

            @Override
            protected void done() {
                boolean ok;
                try {
                    ok = get();
                } catch (Exception e) {
                    ok = false;
                }
                if (ok) {
                    for (Step st : steps) {
                        st.set(Step.DONE);
                    }
                    System.clearProperty("yabs_firststart");
                    dispose();
                    if (whenDone != null) {
                        whenDone.run();
                    } else {
                        //the log level and file of the start options (not NONE: -debug was ignored after the setup)
                        Main.restoreLogging();
                        Main.start();
                    }
                } else {
                    for (Step st : steps) {
                        if (st.state == Step.NOW) {
                            st.set(Step.FAILED);
                        }
                    }
                    bar.setVisible(false);
                    error.setText(tr(Messages.FIRSTSTART_FAILED) + "\n" + (problem == null ? tr(Messages.CONNECTION_FAILED) : problem));
                    error.setVisible(true);
                    errorButtons.setVisible(true);
                }
            }
        }.execute();
    }

    /**
     * @return the connection, null if it failed
     */
    private DatabaseConnection connect(Settings s) throws Exception {
        LocalSettings.setProperty(LocalSettings.DBPATH, s.url);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, s.driver);
        LocalSettings.setProperty(LocalSettings.DBUSER, s.user);
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, s.password);
        LocalSettings.setProperty(LocalSettings.DBNAME, s.dbname);
        LocalSettings.setProperty(LocalSettings.DBPREFIX, s.prefix);
        LocalSettings.setProperty(LocalSettings.OFFICE_USE, "false");
        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
        LocalSettings.setProperty(LocalSettings.DBTYPE, s.network ? "network" : "single");
        DatabaseConnection conn = new DatabaseConnection();
        if (!conn.connect(s.driver, s.user, s.password, s.url, s.dbname, s.prefix, !s.onlyConnect)) {
            return null;
        }
        LocalSettings.save(forConnId);
        LocalSettings.apply();
        return conn;
    }

    private void cacheDir() {
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, Main.MPPATH + File.separator + "Cache");
        LocalSettings.save(forConnId);
        new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR)).mkdirs();
    }

    private static final class Settings {

        String driver, url, user, password, dbname, prefix;
        boolean network, onlyConnect;
    }

    // ---------------------------------------------------------------- texts
    private static java.util.Properties german;

    /**
     * Before there is a database (and a user) the texts come from the bundled
     * German language file on German systems, else from the default
     *
     * @param m
     * @return the text
     */
    static String tr(Messages m) {
        if (LanguageManager.isGermanUser(null)) {
            synchronized (FirstStart.class) {
                if (german == null) {
                    german = new java.util.Properties();
                    try (java.io.InputStream in = Main.class.getResourceAsStream(
                            "/extra/DE-" + Constants.EXTRA_FILES_VERSION + ".properties")) {
                        if (in != null) {
                            german.load(in);
                        }
                    } catch (Exception e) {
                        Log.Debug(FirstStart.class, e);
                    }
                }
            }
            String s = german.getProperty(m.name());
            if (s != null && !s.isEmpty()) {
                return s;
            }
        }
        return m.toString();
    }

    // ---------------------------------------------------------------- parts
    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JTextArea text(String s) {
        JTextArea t = new JTextArea(s);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setBorder(null);
        t.setFont(ModernTheme.baseFont());
        t.setAlignmentX(LEFT_ALIGNMENT);
        return t;
    }

    private static JButton link(String s) {
        JButton b = new JButton(s);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setForeground(ModernTheme.accent());
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * A choice drawn as a card; a click anywhere selects it
     */
    private static final class Tile extends JPanel {

        private static final long serialVersionUID = 1L;
        final JRadioButton radio = new JRadioButton();
        private final JPanel body = column();
        private boolean selected;

        Tile(String title, String hint) {
            super(new BorderLayout(ModernTheme.px(6), 0));
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setBorder(ModernTheme.padding(12, 14));
            radio.setOpaque(false);
            radio.setVerticalAlignment(SwingConstants.TOP);
            add(radio, BorderLayout.WEST);
            JLabel t = new JLabel(title);
            t.setFont(ModernTheme.font(1, Font.BOLD));
            t.setAlignmentX(LEFT_ALIGNMENT);
            body.add(t);
            if (!hint.isEmpty()) {
                body.add(Box.createVerticalStrut(ModernTheme.px(2)));
                JLabel h = new JLabel(hint);
                h.setForeground(ModernTheme.muted());
                h.setAlignmentX(LEFT_ALIGNMENT);
                body.add(h);
            }
            add(body, BorderLayout.CENTER);
            MouseAdapter click = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!radio.isSelected()) {
                        radio.doClick();
                    }
                }
            };
            addMouseListener(click);
            t.addMouseListener(click);
        }

        void extra(JComponent c) {
            if (c instanceof JPanel) {
                ((JPanel) c).setAlignmentX(LEFT_ALIGNMENT);
            }
            body.add(Box.createVerticalStrut(ModernTheme.px(6)));
            body.add(c);
        }

        void setSelected(boolean s) {
            selected = s;
            radio.setSelected(s);
            repaint();
        }

        boolean isSelected() {
            return selected;
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ModernTheme.px(12);
            g2.setColor(selected ? ModernTheme.blend(ModernTheme.cardBackground(), ModernTheme.accent(), 0.06f) : ModernTheme.cardBackground());
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.setColor(selected ? ModernTheme.accent() : ModernTheme.border());
            g2.setStroke(new BasicStroke(selected ? 2f : 1f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * One line of the progress
     */
    private static final class Step extends JPanel {

        private static final long serialVersionUID = 1L;
        static final int TODO = 0, NOW = 1, DONE = 2, FAILED = 3;
        private final JLabel mark = new JLabel();
        private final JLabel label;
        int state = -1;

        Step(String text) {
            super(new BorderLayout(ModernTheme.px(10), 0));
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            mark.setHorizontalAlignment(SwingConstants.CENTER);
            mark.setPreferredSize(new Dimension(ModernTheme.px(18), ModernTheme.px(18)));
            mark.setFont(ModernTheme.font(2, Font.BOLD));
            label = new JLabel(text);
            label.setFont(ModernTheme.font(1, Font.PLAIN));
            add(mark, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
            set(TODO);
        }

        void set(int s) {
            if (s == state) {
                return;
            }
            state = s;
            switch (s) {
                case DONE:
                    mark.setText("✓");
                    mark.setForeground(new Color(0x2E, 0x7D, 0x32));
                    label.setForeground(ModernTheme.foreground());
                    label.setFont(ModernTheme.font(1, Font.PLAIN));
                    break;
                case NOW:
                    mark.setText("›");
                    mark.setForeground(ModernTheme.accent());
                    label.setForeground(ModernTheme.foreground());
                    label.setFont(ModernTheme.font(1, Font.BOLD));
                    break;
                case FAILED:
                    mark.setText("✕");
                    mark.setForeground(new Color(0xC6, 0x28, 0x28));
                    label.setForeground(new Color(0xC6, 0x28, 0x28));
                    break;
                default:
                    mark.setText("•");
                    mark.setForeground(ModernTheme.muted());
                    label.setForeground(ModernTheme.muted());
                    label.setFont(ModernTheme.font(1, Font.PLAIN));
            }
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }
}
