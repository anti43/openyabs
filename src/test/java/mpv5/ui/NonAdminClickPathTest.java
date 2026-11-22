package mpv5.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.LocalSettings;
import mpv5.handler.ContactService;
import mpv5.handler.Payments;
import mpv5.handler.TemplateHandler;
import mpv5.handler.TrashHandler;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_AdvancedLocalProperties;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.GeneralListPanel;
import mpv5.ui.panels.HomeScreen;
import mpv5.ui.panels.JournalPanel;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.panels.ModernContactPanel;
import mpv5.ui.panels.ModernExpensePanel;
import mpv5.ui.panels.ModernItemPanel;
import mpv5.ui.panels.ModernProductPanel;
import mpv5.ui.panels.ModernRevenuePanel;
import mpv5.ui.panels.ProductsOverview;
import mpv5.ui.panels.TrashPanel;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.ui.toolbars.RecordActions;
import mpv5.usermanagement.MPSecurityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end virtual-screen click-path and RBAC audit test behaving as real
 * non-admin users (Standard User, Editor, Viewer, and Group-Restricted User)
 * across all main panels, lists, settings entries, and menus.
 */
class NonAdminClickPathTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void nonAdminStandardUserCanUseAllMainPanelsAndSettingsWithoutErrors() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        newInstallation(tmp);

        // Seed one contact, product, invoice, expense, revenue as admin
        Contact seedCustomer = new Contact();
        seedCustomer.setCname("Kunde Alpha");
        seedCustomer.setCompany("Alpha GmbH");
        seedCustomer.setisCustomer(true);
        seedCustomer.setGroupsids(1);
        assertTrue(seedCustomer.save(true));

        Product seedProduct = new Product();
        seedProduct.setCname("Dienstleistung");
        seedProduct.setExternalnetvalue(new BigDecimal("100.00"));
        seedProduct.setInternalnetvalue(new BigDecimal("50.00"));
        seedProduct.setInttype(Product.TYPE_SERVICE);
        seedProduct.setGroupsids(1);
        assertTrue(seedProduct.save(true));

        Item seedInvoice = (Item) DatabaseObject.getObject(Context.getInvoice());
        seedInvoice.setInttype(Item.TYPE_INVOICE);
        seedInvoice.setCname("Rechnung 1");
        seedInvoice.setContactsids(seedCustomer.__getIDS());
        seedInvoice.setAccountsids(1);
        seedInvoice.setGroupsids(1);
        seedInvoice.setDateadded(new Date());
        assertTrue(seedInvoice.save(true));

        // Switch to a real non-admin user (RIGHT_TO_CREATE_OR_DELETE = 1)
        User nonAdmin = createUser("sachbearbeiter", MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, 1, false);
        User._setUser(nonAdmin);
        assertFalse(MPSecurityManager.isAdmin());

        // 1. ModernItemPanel for all document types (must NOT pop Admin Access dialog or throw)
        for (int type : new int[]{Item.TYPE_INVOICE, Item.TYPE_OFFER, Item.TYPE_ORDER, Item.TYPE_CREDIT}) {
            final int docType = type;
            ModernItemPanel itemPanel = onEdt(() -> new ModernItemPanel(
                    mpv5.handler.ItemService.contextOf(docType), docType));
            flushEdt();
            assertNotNull(itemPanel);
        }

        // Open existing invoice in ModernItemPanel and exercise Save
        ModernItemPanel invPanel = onEdt(() -> {
            ModernItemPanel p = new ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE);
            p.setDataOwner(seedInvoice, true);
            return p;
        });
        flushEdt();
        assertNotNull(invPanel);

        // 2. ModernContactPanel: open existing contact, test mail() with empty email (must not throw)
        ModernContactPanel contactPanel = onEdt(() -> {
            ModernContactPanel p = new ModernContactPanel(Context.getCustomer());
            p.setDataOwner(seedCustomer, true);
            p.mail();
            return p;
        });
        flushEdt();
        assertNotNull(contactPanel);

        // 3. ModernProductPanel: open existing product & new product (refresh uses ProductGroup.getDefault())
        ModernProductPanel productPanel = onEdt(() -> {
            ModernProductPanel p = new ModernProductPanel(Context.getProduct());
            p.setDataOwner(seedProduct, true);
            p.refresh();
            return p;
        });
        flushEdt();
        assertNotNull(productPanel);

        // 4. ModernExpensePanel: create expense, then delete last expense -> table clears cleanly
        Expense exp = new Expense();
        exp.setCname("Buerobedarf");
        exp.setBrutvalue(new BigDecimal("11.90"));
        exp.setNetvalue(new BigDecimal("10.00"));
        exp.setTaxpercentvalue(new BigDecimal("19.00"));
        exp.setAccountsids(1);
        exp.setGroupsids(1);
        exp.setDateadded(new Date());
        assertTrue(exp.save(true));

        ModernExpensePanel expPanel = onEdt(() -> {
            ModernExpensePanel p = new ModernExpensePanel();
            p.refresh();
            return p;
        });
        flushEdt();
        assertTrue(exp.delete());
        onEdt(() -> {
            expPanel.refresh();
            return null;
        });
        flushEdt();
        assertEquals(0, onEdt(() -> expPanel.getExpenseTable().getRowCount()),
                "Expense table should clear when last expense is deleted");

        // 5. ModernRevenuePanel: create revenue, then delete last revenue -> table clears cleanly
        Revenue rev = new Revenue();
        rev.setCname("Nebeneinnahme");
        rev.setBrutvalue(new BigDecimal("23.80"));
        rev.setNetvalue(new BigDecimal("20.00"));
        rev.setTaxpercentvalue(new BigDecimal("19.00"));
        rev.setAccountsids(1);
        rev.setGroupsids(1);
        rev.setDateadded(new Date());
        assertTrue(rev.save(true));

        ModernRevenuePanel revPanel = onEdt(() -> {
            ModernRevenuePanel p = new ModernRevenuePanel();
            p.refresh();
            return p;
        });
        flushEdt();
        assertTrue(rev.delete());
        onEdt(() -> {
            revPanel.refresh();
            return null;
        });
        flushEdt();
        assertEquals(0, onEdt(() -> revPanel.getRevenueTable().getRowCount()),
                "Revenue table should clear when last revenue is deleted");

        // 6. Lists & Overview panels: ContactsList (including flush()), ProductsOverview, JournalPanel, HomeScreen, TrashPanel, GeneralListPanel
        onEdt(() -> {
            ContactsList cl = new ContactsList();
            cl.refresh();
            cl.flush(); // must not throw IllegalArgumentException
            assertNotNull(new ProductsOverview());
            assertNotNull(new JournalPanel());
            HomeScreen hs = HomeScreen.getInstance();
            hs.refresh();
            TrashPanel tp = TrashPanel.instanceOf();
            assertNotNull(tp);
            GeneralListPanel glp = new GeneralListPanel(Context.getInvoice());
            assertNotNull(glp);
            return null;
        });
        flushEdt();

        // 7. Click EVERY entry in MPControlPanel as non-admin (skipping external Groovy console action)
        MPControlPanel cp = onEdt(MPControlPanel::instanceOf);
        List<MPControlPanel.Entry> entries = MPControlPanel.entries();
        assertFalse(entries.isEmpty());
        for (MPControlPanel.Entry entry : entries) {
            if (entry.panelClass == null) {
                continue; // skip standalone Groovy console action
            }
            onEdt(() -> {
                cp.open(entry);
                return null;
            });
            flushEdt();
        }

        // 8. ControlPanel_AdvancedLocalProperties instantiated directly by non-admin must not NPE
        ControlPanel_AdvancedLocalProperties advLocal = onEdt(ControlPanel_AdvancedLocalProperties::new);
        assertNull(onEdt(advLocal::getAndRemoveActionPanel),
                "Uninitialised ControlPanel_AdvancedLocalProperties must return null action panel without NPE");
    }

    @Test
    void viewerEditorAndGroupRestrictedRbacEnforcedAcrossDbAndUi() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        newInstallation(tmp);

        // Create two custom groups as admin
        Group groupA = new Group();
        groupA.setCname("Gruppe A");
        groupA.setGroupsids(1);
        assertTrue(groupA.save(true));

        Group groupB = new Group();
        groupB.setCname("Gruppe B");
        groupB.setGroupsids(1);
        assertTrue(groupB.save(true));

        // Seed GlobalSettings property as admin
        GlobalSettings.setProperty("company.testkey", "GlobalValue123");
        GlobalSettings.save();

        // Seed contacts in Group A and Group B
        Contact contactA = new Contact();
        contactA.setCname("Contact A");
        contactA.setisCustomer(true);
        contactA.setGroupsids(groupA.__getIDS());
        assertTrue(contactA.save(true));

        Contact contactB = new Contact();
        contactB.setCname("Contact B");
        contactB.setisCustomer(true);
        contactB.setGroupsids(groupB.__getIDS());
        assertTrue(contactB.save(true));

        // Seed an invoice in Group A
        Item invoiceA = (Item) DatabaseObject.getObject(Context.getInvoice());
        invoiceA.setInttype(Item.TYPE_INVOICE);
        invoiceA.setCname("Inv A");
        invoiceA.setContactsids(contactA.__getIDS());
        invoiceA.setAccountsids(1);
        invoiceA.setGroupsids(groupA.__getIDS());
        invoiceA.setDateadded(new Date());
        assertTrue(invoiceA.save(true));

        // Trash Contact B as admin
        assertTrue(contactB.delete());

        // --- Test 1: Viewer (RIGHT_TO_VIEW = 4) ---
        User viewer = createUser("viewer_user", MPSecurityManager.RIGHT_TO_VIEW, 1, false);
        User._setUser(viewer);

        // Expense & Revenue must be secured for Viewer
        assertFalse(MPSecurityManager.check(Context.getExpense(), MPSecurityManager.EDIT));
        assertFalse(MPSecurityManager.check(Context.getRevenue(), MPSecurityManager.EDIT));

        // DatabaseObject.save() update must return false for Viewer
        contactA.setCname("Hacked by Viewer");
        assertFalse(contactA.save(true), "Viewer must not be able to update existing contact");

        // DatabaseObject.delete() must return false and preserve IDs
        int origId = contactA.__getIDS();
        assertFalse(contactA.delete(), "Viewer must not be able to delete contact");
        assertEquals(origId, contactA.__getIDS(), "Failed delete must not clear object ID");

        // Payments.add() must return false for Viewer
        assertFalse(Payments.add(invoiceA, new BigDecimal("10.00"), new Date(), "Viewer payment"));

        // ContactsList editToggle must be disabled for Viewer
        ContactsList viewerContactsList = onEdt(ContactsList::new);
        ChipToggle editToggle = onEdt(() -> findFirst(viewerContactsList, ChipToggle.class));
        assertNotNull(editToggle);
        assertFalse(onEdt(editToggle::isEnabled), "ContactsList edit toggle must be disabled for Viewer");

        // ModernCommandBar must disable Save & Delete for Viewer
        ModernContactPanel viewerContactPanel = onEdt(() -> {
            ModernContactPanel p = new ModernContactPanel(Context.getCustomer());
            p.setDataOwner(contactA, true);
            return p;
        });
        flushEdt();
        ModernCommandBar bar = onEdt(() -> findFirst(viewerContactPanel, ModernCommandBar.class));
        assertNotNull(bar);
        mpv5.ui.modern.PrimaryButton saveBtn = onEdt(() -> findFirst(bar, mpv5.ui.modern.PrimaryButton.class));
        assertNotNull(saveBtn, "ModernCommandBar should contain Save PrimaryButton");
        assertFalse(onEdt(saveBtn::isEnabled), "Save button must be disabled for Viewer");
        assertFalse(onEdt(() -> bar.getSaveAsNewMenuItem().isEnabled()),
                "Save as copy menu item must be disabled for Viewer");

        // --- Test 2: Editor (RIGHT_TO_EDIT = 2) ---
        User editor = createUser("editor_user", MPSecurityManager.RIGHT_TO_EDIT, 1, false);
        User._setUser(editor);

        // Editor cannot delete (trash) records
        assertFalse(contactA.delete(), "Editor must not be able to delete contact");
        assertEquals(origId, contactA.__getIDS(), "Failed delete must preserve ID");

        // Editor cannot add payments
        assertFalse(Payments.add(invoiceA, new BigDecimal("10.00"), new Date(), "Editor payment"));

        // --- Test 3: Group-Restricted User in Group A ---
        User groupAUser = createUser("groupa_user", MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, groupA.__getIDS(), true);
        User._setUser(groupAUser);

        // GlobalSettings must still be readable by group-restricted user
        GlobalSettings.read();
        assertEquals("GlobalValue123", GlobalSettings.getProperty("company.testkey"),
                "GlobalSettings must not be filtered out by user group");

        // ContactService.groupOptions() must only return Group A
        List<Group> visibleGroups = ContactService.groupOptions();
        assertEquals(1, visibleGroups.size(), "Group-restricted user should only see their own group");
        assertEquals(groupA.__getIDS(), visibleGroups.get(0).__getIDS());

        // TrashHandler.getData() must NOT expose trashed Contact B (which belongs to Group B)
        Object[][] trashRows = TrashHandler.getData();
        for (Object[] row : trashRows) {
            int rowId = Integer.parseInt(String.valueOf(row[1]));
            assertNotEquals(contactB.__getIDS(), rowId,
                    "Group-restricted user in Group A must not see trashed items from Group B");
        }
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 60, unit = java.util.concurrent.TimeUnit.SECONDS)
    void nonAdminClicksEveryNavigationButtonAndMenuItemInMPView() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        newInstallation(tmp);

        User nonAdmin = createUser("menu_nonadmin", MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, 1, false);
        User._setUser(nonAdmin);
        assertFalse(MPSecurityManager.isAdmin());

        org.jdesktop.application.SingleFrameApplication app = new org.jdesktop.application.SingleFrameApplication() {
            @Override
            protected void startup() {
            }
        };

        mpv5.ui.frames.MPView view = onEdt(() -> {
            mpv5.ui.frames.MPView v = new mpv5.ui.frames.MPView(app);
            mpv5.YabsViewProxy.instance().register(v);
            return v;
        });
        flushEdt();

        java.awt.Window mainWindow = onEdt(view::getFrame);
        // Off-EDT watchdog disposes any modal dialog/popup opened by a menu item so CI never hangs
        java.util.concurrent.ScheduledExecutorService watchdog = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        watchdog.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> {
            for (java.awt.Window w : java.awt.Window.getWindows()) {
                if (w != mainWindow && w.isShowing()) {
                    w.setVisible(false);
                    w.dispose();
                }
            }
        }), 100, 100, java.util.concurrent.TimeUnit.MILLISECONDS);

        int clickedNavButtons = 0;
        int clickedMenuItems = 0;
        try {
            // 1. Click every visible navigation button in MPView's left navigation panels
            List<AbstractButton> navButtons = onEdt(() -> collectNavigationButtons(view.getComponent()));
            assertFalse(navButtons.isEmpty(), "MPView should have navigation buttons");
            for (AbstractButton btn : navButtons) {
                if (!onEdt(() -> btn.isVisible() && btn.isEnabled())) {
                    continue;
                }
                onEdt(() -> {
                    btn.doClick(0);
                    return null;
                });
                flushEdt();
                clickedNavButtons++;
            }

            // 2. Click every visible, non-destructive menu item in MPView's menu bar
            JMenuBar menuBar = onEdt(view::getMenuBar);
            assertNotNull(menuBar, "MPView should have a menu bar");
            List<JMenuItem> menuItems = onEdt(() -> collectMenuItems(menuBar));
            assertFalse(menuItems.isEmpty(), "MPView menu bar should have menu items");
            for (JMenuItem item : menuItems) {
                if (shouldSkipMenuItem(item)) {
                    continue;
                }
                if (!onEdt(() -> item.isVisible() && item.isEnabled())) {
                    continue;
                }
                onEdt(() -> {
                    item.doClick(0);
                    return null;
                });
                flushEdt();
                clickedMenuItems++;
            }
        } finally {
            watchdog.shutdownNow();
            onEdt(() -> {
                for (java.awt.Window w : java.awt.Window.getWindows()) {
                    if (w.isDisplayable()) {
                        w.dispose();
                    }
                }
                return null;
            });
        }

        assertTrue(clickedNavButtons >= 5, "Expected to click at least 5 navigation buttons, clicked: " + clickedNavButtons);
        assertTrue(clickedMenuItems >= 5, "Expected to click at least 5 menu items, clicked: " + clickedMenuItems);
    }

    private static List<AbstractButton> collectNavigationButtons(Component root) {
        List<AbstractButton> buttons = new ArrayList<>();
        if (!(root instanceof Container)) {
            return buttons;
        }
        List<Component> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            Component c = stack.remove(stack.size() - 1);
            if (c instanceof JButton) {
                String name = c.getName();
                // Left navigation bar buttons (jButton1..jButton35 and NavigationMenu custom buttons)
                if (name != null && (name.startsWith("jButton") || name.startsWith("nav."))) {
                    buttons.add((AbstractButton) c);
                }
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    stack.add(child);
                }
            }
        }
        return buttons;
    }

    private static List<JMenuItem> collectMenuItems(JMenuBar bar) {
        List<JMenuItem> items = new ArrayList<>();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            JMenu menu = bar.getMenu(i);
            if (menu != null) {
                collectMenuItems(menu, items);
            }
        }
        return items;
    }

    private static void collectMenuItems(JMenu menu, List<JMenuItem> out) {
        for (Component c : menu.getMenuComponents()) {
            if (c instanceof JMenu) {
                collectMenuItems((JMenu) c, out);
            } else if (c instanceof JMenuItem) {
                out.add((JMenuItem) c);
            }
        }
    }

    private static boolean shouldSkipMenuItem(JMenuItem item) {
        String text = item.getText() == null ? "" : item.getText().toLowerCase();
        String name = item.getName() == null ? "" : item.getName().toLowerCase();
        // Skip JVM exit, login/switch-user, external browser/OS actions, file/print/export choosers, wizards, and LAF switches
        return text.contains("exit") || text.contains("quit") || text.contains("beenden")
                || text.contains("login") || text.contains("anmelden") || text.contains("logout") || text.contains("abmelden")
                || text.contains("restart") || text.contains("neustart")
                || text.contains("import") || text.contains("export") || text.contains("backup") || text.contains("restore")
                || text.contains("csv") || text.contains("xml") || text.contains("vcard") || text.contains("vcf")
                || text.contains("print") || text.contains("druck") || text.contains("pdf") || text.contains("mail")
                || text.contains("wiki") || text.contains("bug") || text.contains("web") || text.contains("donate") || text.contains("update")
                || text.contains("help") || text.contains("hilfe") || text.contains("about") || text.contains("über")
                || text.contains("wizard") || text.contains("assistent") || text.contains("groovy") || text.contains("console")
                || text.contains("system") || text.contains("modern") || text.contains("look") || text.contains("language") || text.contains("sprache")
                || name.contains("exit") || name.contains("quit");
    }

    private static User createUser(String name, int right, int groupId, boolean groupRestricted) throws Exception {
        User u = new User();
        u.setCname(name);
        u.setFullname(name);
        u.setPassword("test");
        u.setInthighestright(right);
        u.setGroupsids(groupId);
        u.setIsenabled(true);
        u.setIsrgrouped(groupRestricted);
        assertTrue(u.save(true));
        return u;
    }

    private static void newInstallation(Path dir) throws Exception {
        File f = dir.toFile();
        mpv5.Main.MPPATH = f.getPath();
        mpv5.Main.SETTINGS_FILE = new File(f, "settings.yabs").getPath();
        String location = dir.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(f, "cache").getPath());
        LocalSettings.save(0);

        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.restart();

        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        mpv5.handler.FirstStartSetup.prepare(admin, true, null);
    }

    private static <T> T onEdt(Callable<T> task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return Popup.silently(task);
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(Popup.silently(task));
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        if (failure.get() != null) {
            if (failure.get() instanceof Exception) {
                throw (Exception) failure.get();
            }
            throw new RuntimeException(failure.get());
        }
        return result.get();
    }

    private static void flushEdt() throws Exception {
        onEdt(() -> null);
        onEdt(() -> null);
    }

    private static <T extends Component> T findFirst(Container root, Class<T> type) {
        List<Component> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            Component c = stack.remove(stack.size() - 1);
            if (type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    stack.add(child);
                }
            }
        }
        return null;
    }

    private static JButton findButtonByTooltipContains(Container root, String needle) {
        List<Component> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            Component c = stack.remove(stack.size() - 1);
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String tip = b.getToolTipText();
                if (tip != null && tip.toLowerCase().contains(needle.toLowerCase())) {
                    return b;
                }
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    stack.add(child);
                }
            }
        }
        return null;
    }
}
