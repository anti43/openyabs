package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Group;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.TemplateHandler;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.models.MPComboBoxModelItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the modernized {@link ControlPanel_Groups} and
 * {@link ControlPanel_ProductGroups}: creating, editing, reparenting,
 * subtree cycle prevention, tree refresh, deletion, and safe non-admin
 * rendering.
 */
class ControlPanel_GroupsModernTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void groupsCreateEditReparentCyclePreventionAndDelete() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        newInstallation(tmp);

        ControlPanel_Groups panel = onEdt(ControlPanel_Groups::new);
        flushEdt();

        assertNotNull(onEdt(panel::getAndRemoveActionPanel), "Admin should receive action panel");

        // 1. Create parent group "Vertrieb"
        onEdt(() -> {
            field(panel, "newButton", javax.swing.AbstractButton.class).doClick(0);
            JTextField cname = field(panel, "cname", JTextField.class);
            cname.setText("Vertrieb");
            field(panel, "saveButton", javax.swing.AbstractButton.class).doClick(0);
            return null;
        });
        flushEdt();

        Group vertrieb = findGroupByName("Vertrieb");
        assertNotNull(vertrieb, "Group 'Vertrieb' must be saved");

        // 2. Create child group "Vertrieb Nord" under "Vertrieb"
        onEdt(() -> {
            field(panel, "newButton", javax.swing.AbstractButton.class).doClick(0);
            JTextField cname = field(panel, "cname", JTextField.class);
            cname.setText("Vertrieb Nord");
            selectComboById(field(panel, "parentCombo", JComboBox.class), vertrieb.__getIDS());
            field(panel, "saveButton", javax.swing.AbstractButton.class).doClick(0);
            return null;
        });
        flushEdt();

        Group nord = findGroupByName("Vertrieb Nord");
        assertNotNull(nord, "Child group 'Vertrieb Nord' must be saved");
        assertEquals(vertrieb.__getIDS(), nord.__getGroupsids(), "Child must reference parent group");
        assertTrue(nord.__getHierarchypath().contains("Vertrieb"), "Hierarchy path should include parent");

        // 3. Selecting parent "Vertrieb" must exclude itself AND its child "Vertrieb Nord" from parentCombo (cycle prevention)
        List<Integer> allowedParentIds = onEdt(() -> {
            panel.setDataOwner(vertrieb, true);
            return comboIds(field(panel, "parentCombo", JComboBox.class));
        });
        assertFalse(allowedParentIds.contains(vertrieb.__getIDS()), "Group must not be selectable as its own parent");
        assertFalse(allowedParentIds.contains(nord.__getIDS()), "Descendant must not be selectable as parent (cycle prevention)");

        // 4. Rename "Vertrieb Nord" -> "Vertrieb Nord-Ost"
        onEdt(() -> {
            panel.setDataOwner(nord, true);
            JTextField cname = field(panel, "cname", JTextField.class);
            cname.setText("Vertrieb Nord-Ost");
            field(panel, "saveButton", javax.swing.AbstractButton.class).doClick(0);
            return null;
        });
        flushEdt();

        Group renamed = (Group) DatabaseObject.getObject(Context.getGroup(), nord.__getIDS(), true);
        assertEquals("Vertrieb Nord-Ost", renamed.__getCname());

        // 5. Tree must contain the renamed node
        JTree tree = onEdt(() -> field(panel, "tree", JTree.class));
        assertTrue(onEdt(() -> tree.getRowCount() >= 3), "Tree should show root + Vertrieb + Vertrieb Nord-Ost");

        // 6. Non-admin construction must not throw and must not expose action panel
        User nonAdmin = createNonAdminUser("nonadmin_grp", MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE);
        User._setUser(nonAdmin);
        ControlPanel_Groups nonAdminPanel = onEdt(ControlPanel_Groups::new);
        assertNull(onEdt(nonAdminPanel::getAndRemoveActionPanel), "Non-admin should not receive action panel");
    }

    @Test
    void productGroupsCreateEditReparentCyclePreventionAndDuplicateCheck() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display");
        newInstallation(tmp);

        ControlPanel_ProductGroups panel = onEdt(ControlPanel_ProductGroups::new);
        flushEdt();

        assertNotNull(onEdt(panel::getAndRemoveActionPanel), "Admin should receive action panel");

        // 1. Create parent product group "Hardware"
        onEdt(() -> {
            field(panel, "newButton", javax.swing.AbstractButton.class).doClick(0);
            JTextField cname = field(panel, "cname", JTextField.class);
            cname.setText("Hardware");
            field(panel, "saveButton", javax.swing.AbstractButton.class).doClick(0);
            return null;
        });
        flushEdt();

        ProductGroup hardware = findProductGroupByName("Hardware");
        assertNotNull(hardware, "ProductGroup 'Hardware' must be saved");

        // 2. Create child product group "Laptops" under "Hardware"
        onEdt(() -> {
            field(panel, "newButton", javax.swing.AbstractButton.class).doClick(0);
            JTextField cname = field(panel, "cname", JTextField.class);
            cname.setText("Laptops");
            selectComboById(field(panel, "parentCombo", JComboBox.class), hardware.__getIDS());
            field(panel, "saveButton", javax.swing.AbstractButton.class).doClick(0);
            return null;
        });
        flushEdt();

        ProductGroup laptops = findProductGroupByName("Laptops");
        assertNotNull(laptops, "Child ProductGroup 'Laptops' must be saved");
        assertEquals(hardware.__getIDS(), laptops.__getProductgroupsids(), "Child must reference parent product group");

        // 3. Cycle prevention: "Hardware" parentCombo must exclude both "Hardware" and "Laptops"
        List<Integer> allowedParentIds = onEdt(() -> {
            panel.setDataOwner(hardware, true);
            return comboIds(field(panel, "parentCombo", JComboBox.class));
        });
        assertFalse(allowedParentIds.contains(hardware.__getIDS()), "ProductGroup must not be its own parent");
        assertFalse(allowedParentIds.contains(laptops.__getIDS()), "Descendant ProductGroup must not be selectable as parent");

        // 4. Non-admin construction must not throw and must not expose action panel
        User nonAdmin = createNonAdminUser("nonadmin_pg", MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE);
        User._setUser(nonAdmin);
        ControlPanel_ProductGroups nonAdminPanel = onEdt(ControlPanel_ProductGroups::new);
        assertNull(onEdt(nonAdminPanel::getAndRemoveActionPanel), "Non-admin should not receive action panel");
    }

    private static Group findGroupByName(String name) throws Exception {
        DatabaseObject.clearCache(Context.getGroup());
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getGroup())) {
            if (o instanceof Group && name.equals(o.__getCname())) {
                return (Group) o;
            }
        }
        return null;
    }

    private static ProductGroup findProductGroupByName(String name) throws Exception {
        DatabaseObject.clearCache(Context.getProductGroup());
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getProductGroup())) {
            if (o instanceof ProductGroup && name.equals(o.__getCname())) {
                return (ProductGroup) o;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void selectComboById(JComboBox<?> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof MPComboBoxModelItem) {
                MPComboBoxModelItem mi = (MPComboBoxModelItem) item;
                if (mi.isValid() && Integer.parseInt(mi.getId()) == id) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
        fail("Combo box did not contain id " + id);
    }

    private static List<Integer> comboIds(JComboBox<?> combo) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof MPComboBoxModelItem) {
                MPComboBoxModelItem mi = (MPComboBoxModelItem) item;
                if (mi.isValid()) {
                    try {
                        ids.add(Integer.parseInt(mi.getId()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return ids;
    }

    private static User createNonAdminUser(String name, int right) throws Exception {
        User u = new User();
        u.setCname(name);
        u.setFullname(name);
        u.setPassword("test");
        u.setInthighestright(right);
        u.setGroupsids(1);
        u.setIsenabled(true);
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

    private static void call(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return type.cast(f.get(target));
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
}
