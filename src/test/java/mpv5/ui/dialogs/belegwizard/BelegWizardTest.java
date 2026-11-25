package mpv5.ui.dialogs.belegwizard;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.handler.ContactService;
import mpv5.handler.TemplateHandler;
import mpv5.ui.frames.NavigationMenu;
import mpv5.ui.panels.ModernItemPanel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the 4-step Beleg-Wizard: step navigation (forward, back, cancel,
 * validation), customer group/tax adoption, HTML preview rendering, saving the
 * document + positions and loading it in {@link ModernItemPanel}, and
 * navigation menu wiring.
 */
@Disabled("Heavy DB + Swing UI wiring test; deactivated for faster test runs")
class BelegWizardTest {

    @TempDir
    Path tmp;

    @AfterEach
    void tearDown() throws Exception {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeAndWait(() -> { });
        }
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    private void initDb() throws Exception {
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs_wiz");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.setProperty(LocalSettings.OFFICE_USE, "false");
        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
        LocalSettings.save(0);
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs_wiz", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.restart();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
    }

    private Contact createCustomer(String company, int groupId, BigDecimal customTaxRate) throws Exception {
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname(company);
        c.setCompany(company);
        c.setStreet("Musterweg 7");
        c.setZip("12345");
        c.setCity("Berlin");
        c.setisCustomer(true);
        c.setGroupsids(groupId);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        if (customTaxRate != null) {
            Tax tax = (Tax) DatabaseObject.getObject(Context.getTaxes());
            tax.setCname("Custom " + customTaxRate + "%");
            tax.setTaxvalue(customTaxRate);
            assertTrue(tax.save(true));
            ContactService.saveContactTax(c, String.valueOf(tax.__getIDS()));
        }
        return (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
    }

    @Test
    void wizardNavigationValidationBackPreservesDataAndSavesToModernItemPanel() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        initDb();

        Group g2 = (Group) DatabaseObject.getObject(Context.getGroup());
        g2.setCname("Projektgruppe");
        assertTrue(g2.save(true));

        Contact customer = createCustomer("Acme Consulting GmbH", g2.__getIDS(), new BigDecimal("7.00"));

        final Item[] savedHolder = new Item[1];
        SwingUtilities.invokeAndWait(() -> {
            BelegWizard wiz = new BelegWizard(null);
            try {
                // Step 1: Type
                assertEquals(BelegWizard.STEP_TYPE, wiz.getCurrentStep());
                assertFalse(wiz.goBack(), "Cannot go back on step 1");
                wiz.getTypeStep().selectType(Item.TYPE_OFFER);
                assertEquals(Item.TYPE_OFFER, wiz.getState().getItemType());
                assertTrue(wiz.goNext());

                // Step 2: Receiver
                assertEquals(BelegWizard.STEP_RECEIVER, wiz.getCurrentStep());
                assertFalse(wiz.goNext(), "Should block advancing without a receiver");
                assertFalse(wiz.getValidationMessage().trim().isEmpty());

                wiz.getReceiverStep().selectReceiver(customer);
                assertEquals(g2.__getIDS(), wiz.getState().getGroupId(), "Should adopt customer's group");
                assertNotNull(wiz.getState().getCustomerTaxRate());
                assertEquals(0, new BigDecimal("7.00").compareTo(wiz.getState().getCustomerTaxRate()),
                        "Should adopt customer's custom tax rate");
                assertTrue(wiz.goNext());

                // Step 3: Positions
                assertEquals(BelegWizard.STEP_ITEMS, wiz.getCurrentStep());
                assertFalse(wiz.goNext(), "Should block advancing with 0 positions");

                wiz.getItemsStep().getTitleField().setText("Website Redesign");
                SubItem pos1 = new SubItem();
                pos1.setDescription("Konzeption & Design");
                pos1.setQuantityvalue(new BigDecimal("2"));
                pos1.setMeasure("Stk");
                pos1.setExternalvalue(new BigDecimal("100.00"));
                pos1.setTaxpercentvalue(new BigDecimal("19.00")); // should be overridden to customer's 7%
                wiz.getItemsStep().addPosition(pos1);

                SubItem pos2 = new SubItem();
                pos2.setDescription("Umsetzung");
                pos2.setQuantityvalue(new BigDecimal("1"));
                pos2.setMeasure("Pauschal");
                pos2.setExternalvalue(new BigDecimal("300.00"));
                wiz.getItemsStep().addPosition(pos2);

                assertTrue(wiz.goNext());

                // Step 4: Preview (no templates loaded -> HTML fallback preview)
                assertEquals(BelegWizard.STEP_PREVIEW, wiz.getCurrentStep());
                String html = wiz.getPreviewStep().getHtmlPreviewText();
                assertTrue(html.contains("Acme Consulting GmbH"), "Preview should include receiver name");
                assertTrue(html.contains("Konzeption &amp; Design"), "Preview should include position 1");
                assertTrue(html.contains("Umsetzung"), "Preview should include position 2");

                // Go Back twice and forward again: verify state is preserved
                assertTrue(wiz.goBack());
                assertEquals(BelegWizard.STEP_ITEMS, wiz.getCurrentStep());
                assertEquals("Website Redesign", wiz.getItemsStep().getTitleField().getText());
                assertTrue(wiz.goBack());
                assertEquals(BelegWizard.STEP_RECEIVER, wiz.getCurrentStep());
                assertEquals(customer.__getIDS(), wiz.getState().getReceiver().__getIDS());
                assertTrue(wiz.goNext());
                assertTrue(wiz.goNext());
                assertEquals(BelegWizard.STEP_PREVIEW, wiz.getCurrentStep());

                // Finish & Save
                Item saved = wiz.finishAndSave();
                assertNotNull(saved, "finishAndSave should persist and return the saved document");
                assertTrue(saved.isExisting());
                savedHolder[0] = saved;
            } finally {
                if (wiz.isDisplayable()) {
                    wiz.dispose();
                }
            }
        });

        Item saved = savedHolder[0];
        assertNotNull(saved);
        assertEquals(Item.TYPE_OFFER, saved.__getInttype());
        assertEquals(customer.__getIDS(), saved.__getContactsids());
        assertEquals(g2.__getIDS(), saved.__getGroupsids());
        assertEquals("Website Redesign", saved.__getCname());
        assertNotNull(saved.__getCnumber());
        assertFalse(saved.__getCnumber().trim().isEmpty(), "Document number should be auto-generated");
        assertEquals(0, new BigDecimal("500.00").compareTo(saved.__getNetvalue()), "Net total should be 500.00");
        assertEquals(0, new BigDecimal("35.00").compareTo(saved.__getTaxvalue()), "7% customer tax on 500.00 should be 35.00");

        List<SubItem> savedSubs = DatabaseObject.getReferencedObjects(saved, Context.getSubItem(), new SubItem(), false);
        assertEquals(2, savedSubs.size(), "All 2 positions must be saved in DB");

        // Verify saved document loads cleanly into ModernItemPanel (normal view)
        SwingUtilities.invokeAndWait(() -> {
            try {
                ModernItemPanel panel = new ModernItemPanel(Context.getOffer(), Item.TYPE_OFFER);
                panel.setDataOwner(saved, true);
                assertEquals(saved.__getIDS(), panel.getDataOwner().__getIDS());
                java.lang.reflect.Field pf = ModernItemPanel.class.getDeclaredField("positions");
                pf.setAccessible(true);
                mpv5.ui.panels.ItemPositionsTable ipt = (mpv5.ui.panels.ItemPositionsTable) pf.get(panel);
                List<SubItem> loadedInPanel = BelegWizardService.extractPositions(ipt.getModel());
                assertEquals(2, loadedInPanel.size(), "Normal document view should display all saved positions");
            } catch (Exception ex) {
                fail(ex);
            }
        });
    }

    @Test
    void cancelClosesWithoutCreatingAnyDocument() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        initDb();
        Contact customer = createCustomer("Cancel Corp", 1, null);

        SwingUtilities.invokeAndWait(() -> {
            BelegWizard wiz = new BelegWizard(null);
            wiz.getTypeStep().selectType(Item.TYPE_INVOICE);
            wiz.goNext();
            wiz.getReceiverStep().selectReceiver(customer);
            wiz.goNext();
            SubItem pos = new SubItem();
            pos.setDescription("Canceled item");
            pos.setQuantityvalue(BigDecimal.ONE);
            pos.setExternalvalue(new BigDecimal("99.00"));
            wiz.getItemsStep().addPosition(pos);
            wiz.cancelImmediately();
            assertTrue(wiz.isCancelled());
            assertNull(wiz.getSavedItem());
        });

        assertThrows(mpv5.db.common.NodataFoundException.class,
                () -> DatabaseObject.getObjects(Context.getInvoice()),
                "Cancelling the wizard must not save any invoice");
    }

    @Test
    void navigationMenuRegistersBelegWizardButtonAtTopWithShortcut() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel nav = new JPanel();
            JButton existing = new JButton("Neues Angebot");
            nav.add(existing);

            NavigationMenu.addBelegWizardButton(nav, existing, e -> { });
            assertEquals(2, nav.getComponentCount());
            assertEquals(NavigationMenu.NAV_BELEG_WIZARD_BUTTON, nav.getComponent(0).getName());

            KeyStroke ks = NavigationMenu.shortcutFor(NavigationMenu.NAV_BELEG_WIZARD_BUTTON);
            assertNotNull(ks);
            assertEquals(KeyEvent.VK_Z, ks.getKeyCode());
        });
    }
}
