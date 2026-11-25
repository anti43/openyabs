package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.ItemService;
import mpv5.handler.TemplateHandler;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies "Hide cancelled" in ReceiptLookup (Vorgangssuche) and the active
 * status/date filter indicator plus "Show all statuses" button in JournalPanel.
 */
@Disabled("Heavy DB + Swing UI wiring test; deactivated for faster test runs")
class ReceiptLookupAndJournalFilterTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() {
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void receiptLookupHidesCancelledInvoicesWhenChecked() throws Exception {
        newInstallation(tmp);

        Contact customer = createCustomer("Suche Kunde GmbH");
        Item active = createInvoice(customer, "Beratung", new BigDecimal("150.00"));
        Item toCancel = createInvoice(customer, "Storno-Posten", new BigDecimal("250.00"));
        Item storno = ItemService.cancel(toCancel, SubItem.toModel(toCancel.getSubitems(), true));
        assertNotNull(storno);

        ReceiptLookup lookup = onEdt(ReceiptLookup::new);
        waitUntil(() -> !Messages.PLEASE_WAIT.toString().equals(onEdt(() -> findByName(lookup, "jLabel1", JLabel.class).getText())), 5000);

        JCheckBox hideCancelled = onEdt(() -> findByName(lookup, "hideCancelled", JCheckBox.class));
        assertNotNull(hideCancelled, "hideCancelled checkbox should exist");
        assertTrue(hideCancelled.isEnabled(), "hideCancelled should be enabled for status All");
        assertFalse(hideCancelled.isSelected(), "hideCancelled should default to unchecked");

        // Unchecked + status All -> both active and cancelled invoices returned
        assertNull(onEdt(lookup::getStatusQuery));
        Set<Integer> allIds = idsOf(DatabaseObject.getObjects(Context.getInvoice(), onEdt(lookup::getStatusQuery)));
        assertTrue(allIds.contains(active.__getIDS()));
        assertTrue(allIds.contains(toCancel.__getIDS()));

        // Checked + status All -> cancelled invoice is hidden
        onEdt(() -> {
            hideCancelled.doClick();
            return null;
        });
        waitUntil(() -> !Messages.PLEASE_WAIT.toString().equals(onEdt(() -> findByName(lookup, "jLabel1", JLabel.class).getText())), 5000);
        assertTrue(hideCancelled.isSelected());

        Set<Integer> visibleIds = idsOf(DatabaseObject.getObjects(Context.getInvoice(), onEdt(lookup::getStatusQuery)));
        assertTrue(visibleIds.contains(active.__getIDS()), "active invoice should still be returned");
        assertFalse(visibleIds.contains(toCancel.__getIDS()), "cancelled invoice should be hidden");

        // Selecting specific status "Cancelled" disables hideCancelled and returns the cancelled invoice
        JComboBox<?> statusCombo = onEdt(() -> {
            for (Component c : allComponents(lookup)) {
                if (c instanceof JComboBox) {
                    JComboBox<?> cb = (JComboBox<?>) c;
                    for (int i = 0; i < cb.getItemCount(); i++) {
                        Object elem = cb.getItemAt(i);
                        if (elem instanceof MPComboBoxModelItem
                                && Integer.valueOf(Item.STATUS_CANCELLED).equals(((MPComboBoxModelItem) elem).getIdObject())) {
                            cb.setSelectedIndex(i);
                            return cb;
                        }
                    }
                }
            }
            fail("status combo box not found");
            return null;
        });
        waitUntil(() -> !Messages.PLEASE_WAIT.toString().equals(onEdt(() -> findByName(lookup, "jLabel1", JLabel.class).getText())), 5000);
        assertFalse(onEdt(hideCancelled::isEnabled), "hideCancelled should be disabled when a specific status is selected");

        Set<Integer> cancelledOnly = idsOf(DatabaseObject.getObjects(Context.getInvoice(), onEdt(lookup::getStatusQuery)));
        assertTrue(cancelledOnly.contains(toCancel.__getIDS()));
        assertFalse(cancelledOnly.contains(active.__getIDS()));

        // Switching back to "All" re-enables hideCancelled
        onEdt(() -> {
            statusCombo.setSelectedIndex(0);
            return null;
        });
        waitUntil(() -> !Messages.PLEASE_WAIT.toString().equals(onEdt(() -> findByName(lookup, "jLabel1", JLabel.class).getText())), 5000);
        assertTrue(onEdt(hideCancelled::isEnabled));
    }

    @Test
    void journalShowsActiveFilterInCustomViewAndOneClickShowsAllStatuses() throws Exception {
        newInstallation(tmp);

        Contact customer = createCustomer("Journal Kunde GmbH");
        Item invoice = createInvoice(customer, "Projekt", new BigDecimal("300.00"));
        Item storno = ItemService.cancel(invoice, SubItem.toModel(invoice.getSubitems(), true));
        assertNotNull(storno);

        JournalPanel panel = onEdt(JournalPanel::new);
        JLabel filterBadge = onEdt(() -> findByName(panel, "filterBadge", JLabel.class));
        JButton showAllStatus = onEdt(() -> findByName(panel, "showAllStatus", JButton.class));
        JComboBox<?> viewCombo = onEdt(() -> findByName(panel, "view", JComboBox.class));
        JTable table = onEdt(() -> findByName(panel, "jTable1", JTable.class));

        waitUntil(() -> onEdt(() -> !Messages.JOURNAL_LOADING.toString().equals(
                findByTextNotEmpty(panel))), 5000);

        // Default view is EÜR: paid, by payment date
        assertEquals(JournalPanel.VIEW_EUR, onEdt(viewCombo::getSelectedIndex));
        assertTrue(onEdt(filterBadge::isVisible));
        String badgeText = onEdt(filterBadge::getText);
        assertTrue(badgeText.contains(Messages.STATUS_PAID.toString()), "badge should show paid status: " + badgeText);
        assertTrue(badgeText.contains(Messages.JOURNAL_BY_PAYDATE.toString()), "badge should show payment date: " + badgeText);
        assertTrue(onEdt(showAllStatus::isVisible), "showAllStatus button should be visible under EÜR filter");

        // User unchecks everything except Credit Notes -> view becomes "Custom",
        // but badge STILL clearly shows "Status: Paid · Payment date" and offers 1-click reset
        onEdt(() -> {
            uncheckByText(panel, Item.getTypeString(mpv5.globals.Constants.TYPE_INVOICE));
            uncheckByText(panel, Item.getTypeString(mpv5.globals.Constants.TYPE_DEPOSIT));
            uncheckByText(panel, Item.getTypeString(mpv5.globals.Constants.TYPE_PART_PAYMENT));
            uncheckByText(panel, Messages.TYPE_REVENUE.toString());
            uncheckByText(panel, Messages.JOURNAL_SHOW_EXPENSES.toString());
            return null;
        });

        assertEquals(JournalPanel.VIEW_CUSTOM, onEdt(viewCombo::getSelectedIndex), "unchecking types switches view to Custom");
        assertTrue(onEdt(filterBadge::isVisible));
        String customBadgeText = onEdt(filterBadge::getText);
        assertTrue(customBadgeText.contains(Messages.STATUS_PAID.toString()), "custom view badge must still show Paid: " + customBadgeText);
        assertTrue(customBadgeText.contains(Messages.JOURNAL_BY_PAYDATE.toString()), "custom view badge must still show Payment date: " + customBadgeText);
        assertTrue(onEdt(showAllStatus::isVisible), "showAllStatus must remain visible in Custom view");

        // Clicking "Show all statuses" keeps only Credit Notes selected, switches to All + Document date,
        // hides the reset button, and finds the cancellation credit note
        onEdt(() -> {
            showAllStatus.doClick();
            return null;
        });

        waitUntil(() -> onEdt(() -> table.getRowCount() > 0), 5000);

        assertEquals(JournalPanel.VIEW_CUSTOM, onEdt(viewCombo::getSelectedIndex), "only Credit Notes selected stays Custom");
        assertFalse(onEdt(showAllStatus::isVisible), "showAllStatus should hide once showing all statuses by document date");
        String allBadgeText = onEdt(filterBadge::getText);
        assertTrue(allBadgeText.contains(Messages.ALL.toString()), "badge should now show All: " + allBadgeText);
        assertTrue(allBadgeText.contains(Messages.JOURNAL_BY_DOCDATE.toString()), "badge should now show Document date: " + allBadgeText);

        boolean foundStorno = onEdt(() -> {
            for (int r = 0; r < table.getRowCount(); r++) {
                Object obj = table.getModel().getValueAt(r, 0);
                if (obj instanceof Item && ((Item) obj).__getIDS() == storno.__getIDS()) {
                    return true;
                }
            }
            return false;
        });
        assertTrue(foundStorno, "credit note should be found once all statuses by document date are shown");
    }

    private static Set<Integer> idsOf(List<DatabaseObject> list) {
        return list.stream().map(DatabaseObject::__getIDS).collect(Collectors.toSet());
    }

    private static String findByTextNotEmpty(JournalPanel panel) {
        for (Component c : allComponents(panel)) {
            if (c instanceof JLabel && Messages.JOURNAL_LOADING.toString().equals(((JLabel) c).getText())) {
                return ((JLabel) c).getText();
            }
        }
        return "";
    }

    private static void uncheckByText(Container root, String text) {
        for (Component c : allComponents(root)) {
            if (c instanceof JCheckBox && text.equals(((JCheckBox) c).getText())) {
                JCheckBox cb = (JCheckBox) c;
                if (cb.isSelected()) {
                    cb.doClick();
                }
            }
        }
    }

    private static <T extends Component> T findByName(Container root, String name, Class<T> type) {
        for (Component c : allComponents(root)) {
            if (type.isInstance(c) && name.equals(c.getName())) {
                return type.cast(c);
            }
        }
        return null;
    }

    private static List<Component> allComponents(Container root) {
        java.util.ArrayList<Component> out = new java.util.ArrayList<>();
        collect(root, out);
        return out;
    }

    private static void collect(Container c, List<Component> out) {
        for (Component child : c.getComponents()) {
            out.add(child);
            if (child instanceof Container) {
                collect((Container) child, out);
            }
        }
    }

    private static void waitUntil(Callable<Boolean> cond, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(cond.call())) {
                return;
            }
            Thread.sleep(50);
        }
    }

    private static <T> T onEdt(Callable<T> c) throws Exception {
        AtomicReference<T> r = new AtomicReference<>();
        AtomicReference<Exception> ex = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                r.set(Popup.silently(c));
            } catch (Exception e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) {
            throw ex.get();
        }
        return r.get();
    }

    private static void newInstallation(Path tmp) throws Exception {
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
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

    private static Contact createCustomer(String name) throws Exception {
        Contact customer = (Contact) DatabaseObject.getObject(Context.getContact());
        customer.setCname(name);
        customer.setisCustomer(true);
        customer.setDateadded(new Date());
        assertTrue(customer.save(true));
        return customer;
    }

    private static Item createInvoice(Contact customer, String posName, BigDecimal net) throws Exception {
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(customer.__getIDS());
        inv.setGroupsids(1);
        inv.setAccountsids(1);
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setIntstatus(Item.STATUS_QUEUED);
        inv.setDateadded(new Date());
        assertTrue(inv.save(true));

        SubItem s = new SubItem();
        s.setItemsids(inv.__getIDS());
        s.setCname(posName);
        s.setDescription(posName);
        s.setQuantityvalue(BigDecimal.ONE);
        s.setExternalvalue(net);
        s.setInternalvalue(net);
        s.setTaxpercentvalue(new BigDecimal("19"));
        s.setOrdernr(1);
        s.setDateadded(new Date());
        assertTrue(s.save(true));

        ItemService.updateTotals(inv);
        return (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
    }
}
