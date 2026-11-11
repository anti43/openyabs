package mpv5.ui.panels;

import groovy.console.ui.Console;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.SearchService;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Accounts;
import mpv5.ui.dialogs.subcomponents.ControlPanel_AdvancedGlobalProperties;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Company;
import mpv5.ui.dialogs.subcomponents.ControlPanel_External;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Formats;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Konsole;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Local;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Locale;
import mpv5.ui.dialogs.subcomponents.ControlPanel_MailTemplates;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Massprint;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Plugins;
import mpv5.ui.dialogs.subcomponents.ControlPanel_ProductGroups;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Reminder;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Schedule;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Taxes;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Templates;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Userproperties;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Users;
import mpv5.ui.dialogs.subcomponents.ControlPanel_ValueProps;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * The settings: a searchable list of all settings on the left, an overview
 * or the chosen setting on the right
 */
public class MPControlPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static MPControlPanel cpanel;

    /**
     * @return the settings panel
     */
    public static synchronized MPControlPanel instanceOf() {
        if (cpanel == null) {
            cpanel = new MPControlPanel();
        }
        return cpanel;
    }

    /**
     * One setting
     */
    public static final class Entry {

        public final String group;
        public final String title;
        public final String description;
        final String icon;
        final Icon customIcon;
        /**
         * creates the panel to show, null for an action
         */
        final Supplier<ControlApplet> panel;
        final Class<?> panelClass;
        final Runnable action;

        Entry(String group, String title, String description, String icon, Class<?> panelClass, Supplier<ControlApplet> panel, Runnable action, Icon customIcon) {
            this.group = group;
            this.title = title;
            this.description = description == null ? "" : description;
            this.icon = icon;
            this.panelClass = panelClass;
            this.panel = panel;
            this.action = action;
            this.customIcon = customIcon;
        }

        private final java.util.Map<Integer, Icon> icons = new java.util.HashMap<Integer, Icon>();

        Icon icon(int size) {
            if (!icons.containsKey(size)) {
                icons.put(size, scaled(size));
            }
            return icons.get(size);
        }

        private Icon scaled(int size) {
            Icon i = customIcon;
            if (i == null && icon != null) {
                java.net.URL u = MPControlPanel.class.getResource("/images/48/" + icon + ".png");
                i = u == null ? null : new ImageIcon(u);
            }
            if (i instanceof ImageIcon && i.getIconWidth() != size) {
                return new ImageIcon(((ImageIcon) i).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
            return i;
        }

        /**
         * @param needle
         * @return true if every word of the needle is in the title, the
         * description or the group
         */
        public boolean matches(String needle) {
            return SearchService.matches(title + " " + description + " " + group, needle);
        }

        /**
         * @param c
         * @return true if this setting shows a panel of the class
         */
        public boolean isPanel(Class<?> c) {
            return panelClass == c;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * A settings page of a plugin
     */
    public static final class PluginPage extends JPanel implements ControlApplet {

        private static final long serialVersionUID = 1L;
        private final JPanel actions = new JPanel();

        PluginPage(mpv5.plugin.ExtensionManager.Page page) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(ModernTheme.padding(12, 14));
            JComponent c;
            try {
                c = page.page.get();
            } catch (Throwable t) {
                Log.Debug(MPControlPanel.class, t);
                c = new JLabel(String.format(Messages.PLUGINS_FAILED.toString(), page.title) + " " + t);
            }
            add(c, BorderLayout.CENTER);
            actions.setOpaque(false);
        }

        @Override
        public Component getAndRemoveActionPanel() {
            return actions;
        }

        @Override
        public void setValues(mpv5.data.PropertyStore values) {
        }

        @Override
        public String getUname() {
            return "pluginpage";
        }

        @Override
        public void reset() {
        }
    }

    private static String label(String key) {
        try {
            return LanguageManager.getBundle().getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private static Entry panel(String group, String key, Messages desc, String icon, Class<?> c, Supplier<ControlApplet> s) {
        return new Entry(group, label("MPControlPanel." + key + ".text"), desc.toString(), icon, c, s, null, null);
    }

    private static Entry action(String group, String key, Messages desc, String icon, Runnable r) {
        return new Entry(group, label("MPControlPanel." + key + ".text"), desc.toString(), icon, null, null, r, null);
    }

    private final List<Entry> plugins = new ArrayList<Entry>();

    /**
     * @return all settings, grouped
     */
    public static List<Entry> entries() {
        List<Entry> l = new ArrayList<Entry>();
        String business = Messages.SETTINGS_GROUP_BUSINESS.toString();
        l.add(panel(business, "jButton23", Messages.SETTINGS_D_COMPANY, "advancedsettings", ControlPanel_Company.class, ControlPanel_Company::new));
        l.add(panel(business, "jButton20", Messages.SETTINGS_D_TEMPLATES, "advancedsettings", ControlPanel_Templates.class, ControlPanel_Templates::new));
        l.add(new Entry(business, Messages.SETTINGS_MAIL_TEMPLATES.toString(), Messages.SETTINGS_D_MAILTEMPLATES.toString(), "advancedsettings",
                ControlPanel_MailTemplates.class, ControlPanel_MailTemplates::new, null, null));
        l.add(panel(business, "jButton21", Messages.SETTINGS_D_TAXES, "advancedsettings", ControlPanel_Taxes.class, ControlPanel_Taxes::new));
        l.add(panel(business, "jButton18", Messages.SETTINGS_D_REMINDER, "kxconfig", ControlPanel_Reminder.class, ControlPanel_Reminder::new));
        l.add(panel(business, "jButton13", Messages.SETTINGS_D_FORMATS, "blockdevice", ControlPanel_Formats.class,
                () -> new ControlPanel_Formats(User.getCurrentUser())));
        l.add(panel(business, "jButton12", Messages.SETTINGS_D_ACCOUNTS, "gkrellm2", ControlPanel_Accounts.class, ControlPanel_Accounts::new));
        l.add(panel(business, "jButton17", Messages.SETTINGS_D_PRODUCTGROUPS, "preferences_system_session_services", ControlPanel_ProductGroups.class, ControlPanel_ProductGroups::new));
        l.add(panel(business, "jButton10", Messages.SETTINGS_D_GROUPS, "flag", ControlPanel_Groups.class, ControlPanel_Groups::new));
        l.add(panel(business, "jButton25", Messages.SETTINGS_D_MASSPRINT, "advancedsettings", ControlPanel_Massprint.class, ControlPanel_Massprint::new));
        l.add(panel(business, "jButton22", Messages.SETTINGS_D_SCHEDULE, "style", ControlPanel_Schedule.class, ControlPanel_Schedule::new));

        String people = Messages.SETTINGS_GROUP_PEOPLE.toString();
        l.add(panel(people, "jButton6", Messages.SETTINGS_D_USERS, "evolution-contacts", ControlPanel_Users.class, ControlPanel_Users::new));
        l.add(panel(people, "jButton16", Messages.SETTINGS_D_USERPROPERTIES, "preferences_plugin", ControlPanel_Userproperties.class, ControlPanel_Userproperties::new));
        l.add(panel(people, "jButton2", Messages.SETTINGS_D_LOCALE, "locale", ControlPanel_Locale.class, ControlPanel_Locale::new));
        l.add(panel(people, "jButton9", Messages.SETTINGS_D_FONT, "fonts", ControlPanel_Fonts.class, ControlPanel_Fonts::new));
        //inline instead of a separate window
        l.add(panel(people, "jButton5", Messages.SETTINGS_D_TRANSLATE, "babelfish", mpv5.ui.dialogs.subcomponents.ControlPanel_Translate.class,
                mpv5.ui.dialogs.subcomponents.ControlPanel_Translate::new));

        //was Tools > Migration in the main menu
        String database = Messages.SETTINGS_GROUP_DATABASE.toString();
        l.add(new Entry(database, Messages.DB_BACKUP.toString(), Messages.DB_BACKUP_INFO.toString(), "folder_tar", null, null,
                () -> mpv5.db.migration.MigrationWB.instanceOf().doExportToDerby(), null));
        l.add(new Entry(database, Messages.DB_EXPORT_SQL.toString(), Messages.DB_EXPORT_SQL_INFO.toString(), "kwikdisk", null, null,
                () -> mpv5.db.migration.MigrationWB.instanceOf().doExportToMySQL(), null));
        l.add(new Entry(database, Messages.DB_RESTORE.toString(), Messages.DB_RESTORE_INFO.toString(), "folder_open", null, null,
                () -> mpv5.db.migration.MigrationWB.instanceOf().doImport(), null));

        String system = Messages.SETTINGS_GROUP_SYSTEM.toString();
        l.add(panel(system, "jButton1", Messages.SETTINGS_D_LOCAL, "internet", ControlPanel_Local.class, ControlPanel_Local::new));
        l.add(panel(system, "jButton7", Messages.SETTINGS_D_EXTERNAL, "openofficeorg-20", ControlPanel_External.class, ControlPanel_External::new));
        l.add(panel(system, "jButton11", Messages.SETTINGS_D_PLUGINS, "usb", ControlPanel_Plugins.class, ControlPanel_Plugins::new));
        //Shopify instead of the old XML-RPC shop interface
        l.add(new Entry(system, "Shopify", Messages.SHOPIFY_DESC.toString(), "krfb",
                mpv5.ui.dialogs.subcomponents.ControlPanel_Shopify.class, mpv5.ui.dialogs.subcomponents.ControlPanel_Shopify::new, null, null));
        l.add(panel(system, "jButton15", Messages.SETTINGS_D_GLOBAL, "advancedsettings", ControlPanel_AdvancedGlobalProperties.class, ControlPanel_AdvancedGlobalProperties::new));
        l.add(panel(system, "jButton24", Messages.SETTINGS_D_SCRIPTS, "konsole", ControlPanel_ValueProps.class, ControlPanel_ValueProps::new));
        l.add(panel(system, "jButton14", Messages.SETTINGS_D_SQL, "konsole", ControlPanel_Konsole.class, ControlPanel_Konsole::new));
        l.add(action(system, "jButton26", Messages.SETTINGS_D_GROOVY, "konsole", MPControlPanel::groovy));
        //"delete local files" is part of the local options now
        synchronized (MPControlPanel.class) {
            if (cpanel != null) {
                l.addAll(cpanel.plugins);
            }
        }
        //the settings pages of the running plugins
        for (final mpv5.plugin.ExtensionManager.Page pg : mpv5.plugin.ExtensionManager.instance().pages()) {
            l.add(new Entry(Messages.SETTINGS_GROUP_PLUGINS.toString(), pg.title, pg.description, "preferences_plugin",
                    PluginPage.class, () -> new PluginPage(pg), null, null));
        }
        return l;
    }

    private static void groovy() {
        if (MPSecurityManager.checkAdminAccess()) {
            Log.setLogLevel(Log.LOGLEVEL_DEBUG);
            Console console = new Console();
            console.setVariable("yabs", YabsViewProxy.instance());
            console.setVariable("db", QueryHandler.instanceOf());
            console.setVariable("QueryHandler", QueryHandler.instanceOf());
            console.setVariable("dbo", DatabaseObject.getObject(Context.getInvoice()));
            console.setVariable("DatabaseObject", DatabaseObject.getObject(Context.getInvoice()));
            console.setVariable("Context", Context.getInvoice());
            console.setVariable("User", User.getCurrentUser());
            console.setFrame(YabsViewProxy.instance().getIdentifierFrame());
            console.run();
        }
    }

    /**
     * Shows the settings tab with the given setting
     *
     * @param e
     */
    public static void show(Entry e) {
        if (e.panel != null) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(instanceOf(), Messages.CONTROL_PANEL.toString());
        }
        instanceOf().open(e);
    }

    // ------------------------------------------------------------------ view
    private final JTextField search = ModernTheme.roomy(new JTextField());
    private final DefaultListModel<Object> model = new DefaultListModel<Object>();
    private final JList<Object> list = new JList<Object>(model);
    private final CardLayout cards = new CardLayout();
    private final JPanel right = new JPanel(cards);
    private final JPanel overview = new JPanel();
    private final JLabel detailIcon = new JLabel();
    private final JLabel detailTitle = new JLabel();
    private final JLabel detailText = new JLabel();
    private final JScrollPane scroller = new JScrollPane();
    private final JPanel actions = new JPanel(new BorderLayout());
    private List<Entry> all = new ArrayList<Entry>();
    private boolean listing;

    private MPControlPanel() {
        super(new BorderLayout());
        setName("Form");
        setBackground(ModernTheme.background());

        //left: search and list
        JPanel side = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        side.setBackground(ModernTheme.cardBackground());
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, ModernTheme.border()),
                ModernTheme.padding(12, 10)));
        search.putClientProperty("JTextField.placeholderText", Messages.SETTINGS_SEARCH_HINT.toString());
        search.putClientProperty("JTextField.showClearButton", Boolean.TRUE);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    list.requestFocusInWindow();
                    selectNext(list.getSelectedIndex(), 1);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Entry first = firstEntry();
                    if (first != null) {
                        open(first);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    search.setText("");
                }
            }
        });
        side.add(search, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new Renderer());
        list.setOpaque(false);
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || listing) {
                return;
            }
            Object o = list.getSelectedValue();
            if (o instanceof Entry) {
                open((Entry) o);
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int d = e.getKeyCode() == KeyEvent.VK_DOWN ? 1 : e.getKeyCode() == KeyEvent.VK_UP ? -1 : 0;
                if (d != 0) {
                    if (!selectNext(list.getSelectedIndex(), d) && d < 0) {
                        search.requestFocusInWindow();
                    }
                    e.consume();
                }
            }
        });
        JScrollPane ls = new JScrollPane(list);
        ls.setBorder(BorderFactory.createEmptyBorder());
        ls.setPreferredSize(new Dimension(ModernTheme.px(200), ModernTheme.px(300)));
        ls.setOpaque(false);
        ls.getViewport().setOpaque(false);
        side.add(ls, BorderLayout.CENTER);
        JLabel home = new JLabel(Messages.SETTINGS_OVERVIEW.toString());
        home.setForeground(ModernTheme.accent());
        home.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        home.setBorder(ModernTheme.padding(4, 4));
        home.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showOverview();
            }
        });
        side.add(home, BorderLayout.SOUTH);
        side.setPreferredSize(new Dimension(ModernTheme.px(270), ModernTheme.px(300)));
        add(side, BorderLayout.WEST);

        //right: overview or a setting
        overview.setLayout(new BoxLayout(overview, BoxLayout.Y_AXIS));
        overview.setBackground(ModernTheme.background());
        overview.setBorder(ModernTheme.padding(16, 20));
        JScrollPane os = new JScrollPane(overview);
        os.setBorder(BorderFactory.createEmptyBorder());
        os.setPreferredSize(new Dimension(ModernTheme.px(400), ModernTheme.px(300)));
        os.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        right.add(os, "overview");

        JPanel detail = new JPanel(new BorderLayout());
        detail.setOpaque(false);
        JPanel head = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        head.setBackground(ModernTheme.cardBackground());
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()),
                ModernTheme.padding(10, 16)));
        detailTitle.setFont(ModernTheme.font(5, Font.BOLD));
        detailText.setForeground(ModernTheme.muted());
        //a long description is shortened, the whole text is the tooltip
        detailText.setMinimumSize(new Dimension(ModernTheme.px(60), detailText.getPreferredSize().height));
        detailTitle.setMinimumSize(new Dimension(ModernTheme.px(60), detailTitle.getPreferredSize().height));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(detailTitle);
        texts.add(detailText);
        head.add(detailIcon, BorderLayout.WEST);
        head.add(texts, BorderLayout.CENTER);
        detail.add(head, BorderLayout.NORTH);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        //fits the tab, the panel scrolls (the actions stay visible)
        scroller.setPreferredSize(new Dimension(ModernTheme.px(400), ModernTheme.px(300)));
        scroller.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        detail.add(scroller, BorderLayout.CENTER);
        actions.setOpaque(false);
        detail.add(actions, BorderLayout.SOUTH);
        right.add(detail, "detail");
        add(right, BorderLayout.CENTER);

        reload();
        mpv5.plugin.ExtensionManager.instance().addChangeListener(() -> SwingUtilities.invokeLater(this::reload));
    }

    private void reload() {
        all = entries();
        filter();
    }

    private void filter() {
        String n = search.getText().trim();
        Object selected = list.getSelectedValue();
        listing = true;
        try {
            model.clear();
            String group = null;
            for (Entry e : all) {
                if (n.isEmpty() || e.matches(n)) {
                    if (!e.group.equals(group)) {
                        group = e.group;
                        model.addElement(group);
                    }
                    model.addElement(e);
                }
            }
            if (model.isEmpty()) {
                model.addElement(Messages.SETTINGS_NOTHING.toString());
            }
            if (selected != null && model.contains(selected)) {
                list.setSelectedValue(selected, true);
            }
        } finally {
            listing = false;
        }
        buildOverview(n);
    }

    private Entry firstEntry() {
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i) instanceof Entry) {
                return (Entry) model.get(i);
            }
        }
        return null;
    }

    private boolean selectNext(int from, int d) {
        for (int i = from + d; i >= 0 && i < model.size(); i += d) {
            if (model.get(i) instanceof Entry) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                return true;
            }
        }
        return false;
    }

    private void buildOverview(String n) {
        overview.removeAll();
        String group = null;
        ResponsiveGrid grid = null;
        for (Entry e : all) {
            if (!n.isEmpty() && !e.matches(n)) {
                continue;
            }
            if (!e.group.equals(group)) {
                group = e.group;
                JLabel h = new JLabel(group);
                h.setFont(ModernTheme.font(2, Font.BOLD));
                h.setAlignmentX(LEFT_ALIGNMENT);
                h.setBorder(BorderFactory.createEmptyBorder(overview.getComponentCount() == 0 ? 0 : ModernTheme.px(18), 2, ModernTheme.px(8), 0));
                overview.add(h);
                grid = new ResponsiveGrid(ModernTheme.px(250), 4);
                grid.setOpaque(false);
                grid.setAlignmentX(LEFT_ALIGNMENT);
                overview.add(grid);
            }
            grid.add(new Tile(e));
        }
        if (overview.getComponentCount() == 0) {
            JLabel none = new JLabel(Messages.SETTINGS_NOTHING.toString());
            none.setForeground(ModernTheme.muted());
            overview.add(none);
        }
        overview.add(Box.createVerticalGlue());
        overview.revalidate();
        overview.repaint();
    }

    private void showOverview() {
        listing = true;
        list.clearSelection();
        listing = false;
        cards.show(right, "overview");
    }

    /**
     * Opens a setting
     *
     * @param e
     */
    public void open(Entry e) {
        if (e.panel == null) {
            if (e.action != null) {
                e.action.run();
            }
            return;
        }
        if (e.panelClass == PluginPage.class) {
            //plugin pages share their class: select the entry by its title
            selectTitle(e.title);
        } else {
            select(e.panelClass);
        }
        show(e.panel.get(), e);
    }

    private void selectTitle(String title) {
        listing = true;
        try {
            list.clearSelection();
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i) instanceof Entry && ((Entry) model.get(i)).title.equals(title)) {
                    list.setSelectedIndex(i);
                    list.ensureIndexIsVisible(i);
                    break;
                }
            }
        } finally {
            listing = false;
        }
    }

    private void select(Class<?> c) {
        listing = true;
        try {
            list.clearSelection();
            for (int i = 0; i < model.size(); i++) {
                if (model.get(i) instanceof Entry && ((Entry) model.get(i)).panelClass == c && c != null) {
                    list.setSelectedIndex(i);
                    list.ensureIndexIsVisible(i);
                    break;
                }
            }
        } finally {
            listing = false;
        }
    }

    private void show(ControlApplet panel, Entry e) {
        mpv5.YabsViewProxy.instance().setWaiting(true);
        try {
            if (e == null) {
                for (Entry x : all) {
                    if (x.panelClass == panel.getClass()) {
                        e = x;
                    }
                }
            }
            detailTitle.setText(e == null ? ((Component) panel).getClass().getSimpleName().replace("ControlPanel_", "") : e.title);
            detailText.setText(e == null ? "" : e.description);
            detailText.setToolTipText(e == null || e.description.isEmpty() ? null : e.description);
            //a long description must not widen the tab: it takes the width there is
            detailText.setPreferredSize(null);
            Dimension pd = detailText.getPreferredSize();
            detailText.setPreferredSize(new Dimension(Math.min(pd.width, ModernTheme.px(300)), pd.height));
            detailText.setVisible(e != null && !e.description.isEmpty());
            detailIcon.setIcon(e == null ? null : e.icon(32));
            actions.removeAll();
            Component a = panel.getAndRemoveActionPanel();
            if (a != null) {
                actions.add(a, BorderLayout.CENTER);
            }
            scroller.setViewportView(new WidthTracking((Component) panel));
            cards.show(right, "detail");
            revalidate();
            repaint();
        } catch (Exception ex) {
            //no rights for this
            Log.Debug(this, ex);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * Shows a Control Panel Applet
     *
     * @param panel
     */
    public synchronized void openDetails(ControlApplet panel) {
        select(panel.getClass());
        show(panel, null);
    }

    /**
     * Adds a setting (for plugins): a click shows the panel
     *
     * @param icon
     * @param text
     * @param panel
     */
    public void addShortcut(Icon icon, String text, final ControlApplet panel) {
        plugins.add(new Entry(Messages.SETTINGS_GROUP_PLUGINS.toString(), text, "", null, panel.getClass(), () -> panel, null, icon));
        reload();
    }

    /**
     * Adds a setting (for plugins): a click creates a new instance of the
     * class
     *
     * @param icon
     * @param text
     * @param clazz
     */
    public void addShortcut(Icon icon, String text, final Class<?> clazz) {
        plugins.add(new Entry(Messages.SETTINGS_GROUP_PLUGINS.toString(), text, "", null, null, null, () -> {
            try {
                clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                Log.Debug(MPControlPanel.class, ex);
            }
        }, icon));
        reload();
    }

    // ------------------------------------------------------------------ parts
    /**
     * Takes the width of the view (the panel wraps and its cards reflow),
     * unless the panel needs more: then it scrolls sideways
     */
    private static final class WidthTracking extends JPanel implements javax.swing.Scrollable {

        private static final long serialVersionUID = 1L;

        WidthTracking(Component c) {
            super(new BorderLayout());
            setOpaque(false);
            add(c, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
            return ModernTheme.px(16);
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
            return o == SwingConstants.VERTICAL ? r.height : r.width;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() != null && getParent().getWidth() >= getMinimumSize().width;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() >= getPreferredSize().height;
        }
    }

    /**
     * A setting in the overview
     */
    private final class Tile extends JPanel {

        private static final long serialVersionUID = 1L;
        private boolean hover;

        Tile(final Entry e) {
            super(new BorderLayout(ModernTheme.px(12), 0));
            setOpaque(false);
            setBorder(ModernTheme.padding(12, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(e.description.isEmpty() ? null : e.description);
            JLabel i = new JLabel(e.icon(32));
            i.setVerticalAlignment(SwingConstants.TOP);
            add(i, BorderLayout.WEST);
            JPanel t = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
            t.setOpaque(false);
            JLabel title = new JLabel(e.title);
            title.setFont(ModernTheme.font(1, Font.BOLD));
            t.add(title, BorderLayout.NORTH);
            JTextArea d = new JTextArea(e.description);
            d.setLineWrap(true);
            d.setWrapStyleWord(true);
            d.setEditable(false);
            d.setFocusable(false);
            d.setOpaque(false);
            d.setBorder(null);
            d.setFont(ModernTheme.font(-1, Font.PLAIN));
            d.setForeground(ModernTheme.muted());
            d.setRows(2);
            t.add(d, BorderLayout.CENTER);
            add(t, BorderLayout.CENTER);
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent ev) {
                    open(e);
                }

                @Override
                public void mouseEntered(MouseEvent ev) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent ev) {
                    hover = getMousePosition(true) != null;
                    repaint();
                }
            };
            for (JComponent c : new JComponent[]{this, i, title, d, t}) {
                c.addMouseListener(m);
                if (c != this) {
                    c.setCursor(getCursor());
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = ModernTheme.px(12);
            g2.setColor(ModernTheme.cardBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(hover ? ModernTheme.accent() : ModernTheme.border());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Groups small and gray, settings with icon
     */
    private static final class Renderer implements ListCellRenderer<Object> {

        private final JLabel label = new JLabel();

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            label.setOpaque(selected);
            label.setIconTextGap(ModernTheme.px(8));
            if (value instanceof Entry) {
                Entry e = (Entry) value;
                label.setText(e.title);
                label.setIcon(e.icon(20));
                label.setFont(ModernTheme.font(0, Font.PLAIN));
                label.setForeground(selected ? Color.WHITE : ModernTheme.foreground());
                label.setBackground(ModernTheme.accent());
                label.setBorder(ModernTheme.padding(5, 8));
                label.setToolTipText(e.description.isEmpty() ? null : e.description);
            } else {
                label.setText(String.valueOf(value).toUpperCase());
                label.setIcon(null);
                label.setOpaque(false);
                label.setFont(ModernTheme.font(-2, Font.BOLD));
                label.setForeground(ModernTheme.muted());
                label.setBorder(BorderFactory.createEmptyBorder(index == 0 ? ModernTheme.px(2) : ModernTheme.px(12), ModernTheme.px(8), ModernTheme.px(4), 0));
                label.setToolTipText(null);
            }
            return label;
        }
    }
}
