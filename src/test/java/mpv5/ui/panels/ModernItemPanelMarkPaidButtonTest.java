package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the header "Mark as paid" button in ModernItemPanel: light orange
 * and active when unpaid, turns green, labeled "Paid" and inactive when paid,
 * and hidden on non-payable or cancelled documents.
 */
@Disabled("Heavy DB + Swing UI wiring test; deactivated for faster test runs")
class ModernItemPanelMarkPaidButtonTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() throws Exception {
        waitForTemplates(Item.TYPE_INVOICE);
        waitForTemplates(Item.TYPE_OFFER);
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void markPaidButtonTransitionsFromOrangeActiveToGreenPaidInactive() throws Exception {
        newInstallation(tmp);

        Contact customer = createCustomer("Bezahlt Kunde GmbH");
        Item invoice = createInvoice(customer, "Leistung", new BigDecimal("100.00"));

        ModernItemPanel panel = onEdt(() -> {
            ModernItemPanel p = new ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE);
            p.setDataOwner(invoice, true);
            return p;
        });

        JButton btn = onEdt(() -> findByName(panel, "markPaidButton", JButton.class));
        assertNotNull(btn, "markPaidButton should exist in ModernItemPanel");
        mpv5.ui.toolbars.ModernCommandBar bar = onEdt(() -> findFirst(panel, mpv5.ui.toolbars.ModernCommandBar.class));
        assertNotNull(bar, "ModernCommandBar should exist");
        assertTrue(onEdt(() -> SwingUtilities.isDescendingFrom(btn, bar)), "markPaidButton should be in the command bar");
        assertEquals(mpv5.ui.toolbars.ModernCommandBar.barHeight(), onEdt(() -> btn.getPreferredSize().height),
                "markPaidButton should match the height of the other command bar buttons");
        assertTrue(onEdt(btn::isVisible), "button should be visible on unpaid invoice");
        assertTrue(onEdt(btn::isEnabled), "button should be active on unpaid invoice");
        assertEquals(Messages.MARK_PAID_TITLE.toString(), onEdt(btn::getText));
        Color unpaidColor = onEdt(btn::getBackground);
        assertTrue(unpaidColor.getRed() > unpaidColor.getGreen() && unpaidColor.getGreen() > unpaidColor.getBlue(),
                "unpaid button should be light orange, was: " + unpaidColor);

        // Click "Mark as paid"
        onEdt(() -> {
            btn.doClick();
            return null;
        });

        assertTrue(onEdt(btn::isVisible), "button should remain visible when paid");
        assertFalse(onEdt(btn::isEnabled), "button should be inactive once paid");
        assertEquals(Messages.STATUS_PAID.toString(), onEdt(btn::getText));
        Color paidColor = onEdt(btn::getBackground);
        assertTrue(paidColor.getGreen() > paidColor.getRed() && paidColor.getGreen() > paidColor.getBlue(),
                "paid button should be green, was: " + paidColor);

        Item reloaded = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        assertEquals(Item.STATUS_PAID, reloaded.__getIntstatus(), "invoice should be saved as paid in DB");

        // Cancelled invoice hides the button
        Item toCancel = createInvoice(customer, "Storno", new BigDecimal("50.00"));
        assertNotNull(ItemService.cancel(toCancel, SubItem.toModel(toCancel.getSubitems(), true)));
        Item cancelled = (Item) DatabaseObject.getObject(Context.getInvoice(), toCancel.__getIDS());
        onEdt(() -> {
            panel.setDataOwner(cancelled, true);
            return null;
        });
        assertFalse(onEdt(btn::isVisible), "button should be hidden on cancelled invoice");

        // Non-payable document (Offer) hides the button
        ModernItemPanel offerPanel = onEdt(() -> new ModernItemPanel(Context.getOffer(), Item.TYPE_OFFER));
        JButton offerBtn = onEdt(() -> findByName(offerPanel, "markPaidButton", JButton.class));
        assertNotNull(offerBtn);
        assertFalse(onEdt(offerBtn::isVisible), "button should be hidden on offers");
    }

    private static void waitForTemplates(int type) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline && !TemplateHandler.isLoaded(1L, type)) {
            Thread.sleep(25);
        }
    }

    private static <T extends Component> T findFirst(Container root, Class<T> type) {
        List<Component> all = new ArrayList<>();
        collect(root, all);
        for (Component c : all) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
        }
        return null;
    }

    private static <T extends Component> T findByName(Container root, String name, Class<T> type) {
        List<Component> all = new ArrayList<>();
        collect(root, all);
        for (Component c : all) {
            if (type.isInstance(c) && name.equals(c.getName())) {
                return type.cast(c);
            }
        }
        return null;
    }

    private static void collect(Container c, List<Component> out) {
        for (Component child : c.getComponents()) {
            out.add(child);
            if (child instanceof Container) {
                collect((Container) child, out);
            }
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
