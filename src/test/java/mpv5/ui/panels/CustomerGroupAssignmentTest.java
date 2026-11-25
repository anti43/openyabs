package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.ChipToggle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies assigning a group to a customer in both {@link ModernContactPanel}
 * (including that deferred {@code refresh()} never overwrites the customer's
 * saved group and that all groups are immediately selectable) and
 * {@link ContactsList} (display and inline editing in edit mode).
 */
@Disabled("Heavy DB + Swing UI wiring test; deactivated for faster test runs")
class CustomerGroupAssignmentTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void modernContactPanelListsAllGroupsSavesAndPreservesGroupAfterRefresh() throws Exception {
        newInstallation(tmp);

        Group vip = createGroup("VIP Kunden");
        Group b2b = createGroup("B2B Partner");

        Contact customer = new Contact();
        customer.setCname("Hotel St. Erasmus");
        customer.setCompany("Hotel St. Erasmus");
        customer.setisCustomer(true);
        customer.setisCompany(true);
        customer.setGroupsids(1);
        assertTrue(customer.save());

        // Open existing customer, pick VIP group, save
        ModernContactPanel panel = onEdt(() -> {
            ModernContactPanel p = new ModernContactPanel(Context.getCustomer());
            p.setDataOwner(customer, true);
            return p;
        });
        flushEdt();

        @SuppressWarnings("unchecked")
        JComboBox<Group> groupBox = onEdt(() -> findByName(panel, "groupnameselect", JComboBox.class));
        assertNotNull(groupBox, "groupnameselect should exist in ModernContactPanel");
        assertTrue(onEdt(groupBox::getItemCount) >= 3,
                "group dropdown should list default group plus both custom groups");

        onEdt(() -> {
            for (int i = 0; i < groupBox.getItemCount(); i++) {
                if (groupBox.getItemAt(i).__getIDS() == vip.__getIDS()) {
                    groupBox.setSelectedIndex(i);
                    break;
                }
            }
            assertTrue(customer.getPanelData(panel));
            assertTrue(customer.save());
            return null;
        });

        Contact reloaded = (Contact) DatabaseObject.getObject(Context.getContact(), customer.__getIDS());
        assertEquals(vip.__getIDS(), reloaded.__getGroupsids(), "customer group should be saved to DB");

        // Reopen customer in a fresh panel and let constructor's deferred refresh() run:
        // the saved VIP group must remain selected (not reset to user's default group).
        ModernContactPanel reopened = onEdt(() -> {
            ModernContactPanel p = new ModernContactPanel(Context.getCustomer());
            p.setDataOwner(reloaded, true);
            return p;
        });
        flushEdt();

        @SuppressWarnings("unchecked")
        JComboBox<Group> reopenedBox = onEdt(() -> findByName(reopened, "groupnameselect", JComboBox.class));
        Group selected = onEdt(() -> (Group) reopenedBox.getSelectedItem());
        assertNotNull(selected);
        assertEquals(vip.__getIDS(), selected.__getIDS(),
                "deferred refresh() must preserve the customer's saved group");

        // Create a new group after the panel was opened and simulate opening the popup
        Group newlyAdded = createGroup("Neu Angelegt");
        onEdt(() -> {
            PopupMenuEvent ev = new PopupMenuEvent(reopenedBox);
            for (PopupMenuListener l : reopenedBox.getPopupMenuListeners()) {
                l.popupMenuWillBecomeVisible(ev);
            }
            return null;
        });

        boolean foundNew = onEdt(() -> {
            for (int i = 0; i < reopenedBox.getItemCount(); i++) {
                if (reopenedBox.getItemAt(i).__getIDS() == newlyAdded.__getIDS()) {
                    return true;
                }
            }
            return false;
        });
        assertTrue(foundNew, "opening the group dropdown should pick up newly created groups");
        assertEquals(vip.__getIDS(), onEdt(() -> ((Group) reopenedBox.getSelectedItem()).__getIDS()),
                "reloading dropdown options should keep the current selection");
        assertNotNull(b2b);
    }

    @Test
    void contactsListDisplaysAndEditsCustomerGroupInEditMode() throws Exception {
        newInstallation(tmp);

        Group hotelGroup = createGroup("Hotels");
        Contact customer = new Contact();
        customer.setCname("Gasthof Krone");
        customer.setCompany("Gasthof Krone");
        customer.setisCustomer(true);
        customer.setisCompany(true);
        customer.setGroupsids(1);
        assertTrue(customer.save());

        ContactsList list = onEdt(() -> {
            ContactsList cl = new ContactsList(Context.getContact());
            cl.refresh();
            return cl;
        });

        JTable table = onEdt(() -> findByName(list, "listTable", JTable.class));
        assertNotNull(table, "ContactsList table should exist");

        int groupCol = onEdt(() -> {
            String expectedHeader = Messages.LIST_COL_GROUP.toString();
            for (int c = 0; c < table.getModel().getColumnCount(); c++) {
                if (expectedHeader.equals(table.getModel().getColumnName(c))) {
                    return c;
                }
            }
            return -1;
        });
        assertTrue(groupCol >= 0, "ContactsList should contain a Group column");
        assertEquals(1, onEdt(() -> table.getModel().getRowCount()));

        // Enable edit mode and assign the "Hotels" group via the table model
        ChipToggle editToggle = onEdt(() -> findFirst(list, ChipToggle.class));
        assertNotNull(editToggle, "ContactsList editToggle should exist");
        onEdt(() -> {
            editToggle.doClick();
            table.getModel().setValueAt(hotelGroup, 0, groupCol);
            return null;
        });

        assertEquals(hotelGroup.__getCname(), onEdt(() -> table.getModel().getValueAt(0, groupCol)),
                "cell should display the newly assigned group name");
        Contact reloaded = (Contact) DatabaseObject.getObject(Context.getContact(), customer.__getIDS());
        assertEquals(hotelGroup.__getIDS(), reloaded.__getGroupsids(),
                "editing the Group cell in ContactsList should persist groupsids in DB");
    }

    private static Group createGroup(String name) {
        Group g = new Group(name);
        g.setGroupsids(1);
        assertTrue(g.save(), "group " + name + " should save");
        return g;
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

    private static <T extends Component> T findByName(Container root, String name, Class<T> type) {
        List<Component> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty()) {
            Component c = stack.remove(stack.size() - 1);
            if (type.isInstance(c) && name.equals(c.getName())) {
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
}
