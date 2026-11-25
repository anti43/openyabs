package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Date;
import javax.swing.SwingUtilities;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.TemplateHandler;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.panels.ModernItemPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that selecting or pasting a customer on a new invoice automatically
 * adopts the customer's group, while changing the customer on an already-saved
 * invoice keeps the invoice's existing group.
 */
@Disabled("Heavy DB + Swing UI wiring test; deactivated for faster test runs")
class InvoiceCustomerGroupTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() throws Exception {
        long deadline = System.currentTimeMillis() + 10_000;
        int[] types = {
            Constants.TYPE_INVOICE,
            Constants.TYPE_DELIVERY_NOTE,
            Constants.TYPE_ORDER_CONFIRMATION,
            Constants.TYPE_REMINDER
        };
        while (System.currentTimeMillis() < deadline) {
            boolean allLoaded = true;
            for (int t : types) {
                if (!TemplateHandler.isLoaded(1L, t)) {
                    allLoaded = false;
                    break;
                }
            }
            if (allLoaded) {
                break;
            }
            Thread.sleep(50);
        }
        flushEdt();
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    private static void flushEdt() throws Exception {
        for (int i = 0; i < 4; i++) {
            SwingUtilities.invokeAndWait(() -> { });
        }
    }

    private static LabeledCombobox combo(ModernItemPanel panel, String fieldName) throws Exception {
        Field f = ModernItemPanel.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (LabeledCombobox) f.get(panel);
    }

    @Test
    void selectingCustomerOnNewInvoiceAdoptsCustomersGroup() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        EInvoiceRoundTripTest.newInstallation(tmp);

        Group g2 = (Group) DatabaseObject.getObject(Context.getGroup());
        g2.setCname("Kundengruppe B");
        assertTrue(g2.save(true));
        assertNotEquals(1, g2.__getIDS());

        Group g3 = (Group) DatabaseObject.getObject(Context.getGroup());
        g3.setCname("Kundengruppe C");
        assertTrue(g3.save(true));
        assertNotEquals(g2.__getIDS(), g3.__getIDS());

        Contact c2 = (Contact) DatabaseObject.getObject(Context.getContact());
        c2.setCname("Kunde Gruppe B");
        c2.setisCustomer(true);
        c2.setGroupsids(g2.__getIDS());
        c2.setDateadded(new Date());
        assertTrue(c2.save(true));

        Contact c3 = (Contact) DatabaseObject.getObject(Context.getContact());
        c3.setCname("Kunde Gruppe C");
        c3.setisCustomer(true);
        c3.setGroupsids(g3.__getIDS());
        c3.setDateadded(new Date());
        assertTrue(c3.save(true));

        ModernItemPanel panel = EInvoiceRoundTripTest.onEdt(() -> new ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE));
        flushEdt();

        LabeledCombobox contactCombo = combo(panel, "contactname");
        LabeledCombobox groupCombo = combo(panel, "groupnameselect");

        // Selecting c2 on a new invoice sets the invoice's group to g2
        EInvoiceRoundTripTest.onEdt(() -> {
            contactCombo.setModel(c2);
            return null;
        });
        flushEdt();

        assertNotNull(groupCombo.getSelectedItem());
        assertEquals(String.valueOf(g2.__getIDS()), String.valueOf(groupCombo.getSelectedItem().getId()),
                "group combobox should adopt customer's group");
        assertEquals(g2.__getIDS(), panel.getDataOwner().__getGroupsids(),
                "unsaved invoice should adopt customer's group");

        // Saving persists the adopted group
        EInvoiceRoundTripTest.onEdt(() -> {
            assertTrue(panel.getDataOwner().getPanelData(panel));
            assertTrue(panel.getDataOwner().save(true));
            panel.setDataOwner(panel.getDataOwner(), true);
            return null;
        });
        flushEdt();

        Item reloaded = (Item) DatabaseObject.getObject(Context.getInvoice(), panel.getDataOwner().__getIDS());
        assertEquals(g2.__getIDS(), reloaded.__getGroupsids(), "saved invoice should have customer's group");

        // Changing customer on an existing invoice must not overwrite its saved group
        EInvoiceRoundTripTest.onEdt(() -> {
            contactCombo.setModel(c3);
            return null;
        });
        flushEdt();

        assertEquals(String.valueOf(g2.__getIDS()), String.valueOf(groupCombo.getSelectedItem().getId()),
                "existing invoice should keep its group when customer changes");

        // Pasting a customer onto a new invoice also adopts the customer's group
        ModernItemPanel pastedPanel = EInvoiceRoundTripTest.onEdt(() -> new ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE));
        flushEdt();
        EInvoiceRoundTripTest.onEdt(() -> {
            pastedPanel.paste(c3);
            return null;
        });
        flushEdt();

        LabeledCombobox pastedGroupCombo = combo(pastedPanel, "groupnameselect");
        assertEquals(String.valueOf(g3.__getIDS()), String.valueOf(pastedGroupCombo.getSelectedItem().getId()),
                "pasting customer onto new invoice should adopt customer's group");
        assertEquals(g3.__getIDS(), pastedPanel.getDataOwner().__getGroupsids());
    }
}
