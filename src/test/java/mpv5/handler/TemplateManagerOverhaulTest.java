package mpv5.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Group;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Templates;
import mpv5.utils.export.ZugpferdFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the Template Manager overhaul: filename type guessing, multi-file
 * and ZIP import with all-users default assignment, multi-delete, bulk edit,
 * and multi-selection UI mode switching in {@link ControlPanel_Templates}.
 */
class TemplateManagerOverhaulTest {

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

    private User initDb() throws Exception {
        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs_tpl");
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
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs_tpl", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.restart();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        return admin;
    }

    private static File createDummyOdt(Path path) throws Exception {
        File f = path.toFile();
        f.getParentFile().mkdirs();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f))) {
            zos.putNextEntry(new ZipEntry("mimetype"));
            zos.write("application/vnd.oasis.opendocument.text".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("content.xml"));
            zos.write("<office:document-content/>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return f;
    }

    @Test
    void guessesTemplateTypeFromGermanAndEnglishFilenames() {
        assertEquals(Constants.TYPE_INVOICE, TemplateImportService.guessTemplateType("Rechnung_2026.odt"));
        assertEquals(Constants.TYPE_INVOICE, TemplateImportService.guessTemplateType("Invoice_Standard.odt"));
        assertEquals(Constants.TYPE_OFFER, TemplateImportService.guessTemplateType("Angebot-Vorlage.odt"));
        assertEquals(Constants.TYPE_OFFER, TemplateImportService.guessTemplateType("Quote_ACME.odt"));
        assertEquals(Constants.TYPE_ORDER, TemplateImportService.guessTemplateType("Auftrag.odt"));
        assertEquals(Constants.TYPE_ORDER_CONFIRMATION, TemplateImportService.guessTemplateType("Auftragsbestaetigung.odt"));
        assertEquals(Constants.TYPE_ORDER_CONFIRMATION, TemplateImportService.guessTemplateType("Auftragsbest\u00e4tigung_Neu.odt"));
        assertEquals(Constants.TYPE_ORDER_CONFIRMATION, TemplateImportService.guessTemplateType("Order_Confirmation.odt"));
        assertEquals(Constants.TYPE_DELIVERY_NOTE, TemplateImportService.guessTemplateType("Lieferschein.odt"));
        assertEquals(Constants.TYPE_DELIVERY_NOTE, TemplateImportService.guessTemplateType("Delivery_Note.odt"));
        assertEquals(Constants.TYPE_CREDIT, TemplateImportService.guessTemplateType("Gutschrift.odt"));
        assertEquals(Constants.TYPE_CREDIT, TemplateImportService.guessTemplateType("Stornorechnung.odt"));
        assertEquals(Constants.TYPE_CREDIT, TemplateImportService.guessTemplateType("CreditNote.odt"));
        assertEquals(Constants.TYPE_PART_PAYMENT, TemplateImportService.guessTemplateType("Abschlagsrechnung.odt"));
        assertEquals(Constants.TYPE_DEPOSIT, TemplateImportService.guessTemplateType("Anzahlung.odt"));
        assertEquals(Constants.TYPE_REMINDER, TemplateImportService.guessTemplateType("Mahnung_1.odt"));
        assertEquals(Constants.TYPE_PRODUCT_ORDER, TemplateImportService.guessTemplateType("Bestellung_Lieferant.odt"));
        assertEquals(Constants.TYPE_CONTACT, TemplateImportService.guessTemplateType("Kontaktdatenblatt.odt"));
        assertEquals(Constants.TYPE_PRODUCT, TemplateImportService.guessTemplateType("Artikelblatt.odt"));
        assertEquals(Constants.TYPE_SERVICE, TemplateImportService.guessTemplateType("Dienstleistung.odt"));
        assertEquals(Constants.TYPE_JOURNAL, TemplateImportService.guessTemplateType("Journal_Export.odt"));
        assertEquals(Constants.TYPE_CONTRACT, TemplateImportService.guessTemplateType("Vertrag_Wartung.odt"));
        assertEquals(Constants.TYPE_CONVERSATION, TemplateImportService.guessTemplateType("Gespraechsnotiz.odt"));
        assertEquals(Constants.TYPE_MASSPRINT, TemplateImportService.guessTemplateType("Serienbrief.odt"));
        assertEquals(Constants.TYPE_ACTIVITY, TemplateImportService.guessTemplateType("Taetigkeitsnachweis.odt"));
        // Unknown filename returns null (importFiles defaults to TYPE_INVOICE)
        assertNull(TemplateImportService.guessTemplateType("custom_template.odt"));
    }

    @Test
    void multiFileAndZipImportGuessesTypesAndAssignsAllUsersByDefault() throws Exception {
        User admin = initDb();
        User secondUser = new User();
        secondUser.setCname("alice");
        secondUser.setFullname("Alice Example");
        secondUser.setPassword("secret123");
        secondUser.setInthighestright(0);
        secondUser.setIsenabled(true);
        secondUser.setGroupsids(1);
        assertTrue(secondUser.save(true));

        List<User> allUsers = TemplateImportService.loadAllUsers();
        assertTrue(allUsers.size() >= 2, "Expected at least 2 users in DB");
        Set<Integer> expectedUserIds = allUsers.stream().map(User::__getIDS).collect(Collectors.toSet());

        // Direct .odt files
        File offerOdt = createDummyOdt(tmp.resolve("src/Angebot_Standard.odt"));
        File creditOdt = createDummyOdt(tmp.resolve("src/Gutschrift_2026.odt"));

        // .zip archive containing nested .odt files plus junk (__MACOSX & .txt)
        File innerDelivery = createDummyOdt(tmp.resolve("zipsrc/Lieferschein_Neu.odt"));
        File innerConfirmation = createDummyOdt(tmp.resolve("zipsrc/Auftragsbestaetigung.odt"));
        File zipFile = tmp.resolve("src/templates_pack.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry("nested/Lieferschein_Neu.odt"));
            zos.write(Files.readAllBytes(innerDelivery.toPath()));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("Auftragsbestaetigung.odt"));
            zos.write(Files.readAllBytes(innerConfirmation.toPath()));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("__MACOSX/._Ignored.odt"));
            zos.write("junk".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("readme.txt"));
            zos.write("not an odt".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        List<Template> imported = TemplateImportService.importFiles(
                new File[]{offerOdt, creditOdt, zipFile}, 1);
        assertEquals(4, imported.size(), "Should import 2 direct .odt + 2 valid .odt from zip");

        Map<String, Template> byName = new HashMap<>();
        for (Template t : imported) {
            byName.put(t.__getCname(), t);
            Set<Integer> assigned = TemplateImportService.loadAssignedUsers(t).stream()
                    .map(User::__getIDS).collect(Collectors.toSet());
            assertEquals(expectedUserIds, assigned, "All users must be assigned by default to " + t.__getCname());
        }

        assertEquals(String.valueOf(Constants.TYPE_OFFER), byName.get("Angebot_Standard").__getMimetype());
        assertEquals(String.valueOf(Constants.TYPE_CREDIT), byName.get("Gutschrift_2026").__getMimetype());
        assertEquals(String.valueOf(Constants.TYPE_DELIVERY_NOTE), byName.get("Lieferschein_Neu").__getMimetype());
        assertEquals(String.valueOf(Constants.TYPE_ORDER_CONFIRMATION), byName.get("Auftragsbestaetigung").__getMimetype());
    }

    @Test
    void bulkEditUpdatesOnlyCheckedFieldsAndMultiDeleteRemovesSelected() throws Exception {
        User admin = initDb();
        User bob = new User();
        bob.setCname("bob");
        bob.setFullname("Bob Builder");
        bob.setPassword("secret123");
        bob.setInthighestright(0);
        bob.setIsenabled(true);
        bob.setGroupsids(1);
        assertTrue(bob.save(true));

        Group g2 = (Group) DatabaseObject.getObject(Context.getGroup());
        g2.setCname("Gruppe Spezial");
        assertTrue(g2.save(true));

        File f1 = createDummyOdt(tmp.resolve("in/Rechnung_A.odt"));
        File f2 = createDummyOdt(tmp.resolve("in/Angebot_B.odt"));
        File f3 = createDummyOdt(tmp.resolve("in/Mahnung_C.odt"));
        List<Template> imported = TemplateImportService.importFiles(new File[]{f1, f2, f3}, 1);
        assertEquals(3, imported.size());
        Template t1 = imported.get(0);
        Template t2 = imported.get(1);
        Template t3 = imported.get(2);

        // Bulk edit t1 and t2: change users (only bob), columns (format), group (g2), printer, payterm — leave type unchanged!
        TemplateImportService.BulkEditSpec spec = new TemplateImportService.BulkEditSpec();
        spec.applyUsers = true;
        spec.users = Collections.singletonList(bob);
        spec.applyFormat = true;
        spec.format = "1,2,3,4,5,6";
        spec.applyGroup = true;
        spec.groupId = g2.__getIDS();
        spec.applyPrinter = true;
        spec.printer = "OfficePrinter";
        spec.applyPayterm = true;
        spec.paytermDays = 21;
        spec.applyType = false;

        int updated = TemplateImportService.applyBulkEdit(Arrays.asList(t1, t2), spec);
        assertEquals(2, updated);

        Template r1 = (Template) DatabaseObject.getObject(Context.getTemplate(), t1.__getIDS(), true);
        Template r2 = (Template) DatabaseObject.getObject(Context.getTemplate(), t2.__getIDS(), true);
        Template r3 = (Template) DatabaseObject.getObject(Context.getTemplate(), t3.__getIDS(), true);

        // Checked fields updated on t1 and t2
        for (Template r : Arrays.asList(r1, r2)) {
            assertEquals("1,2,3,4,5,6", r.__getFormat());
            assertEquals(g2.__getIDS(), r.__getGroupsids());
            assertEquals("OfficePrinter", r.__getPrinter());
            assertEquals(21, ZugpferdFile.paytermOf(r));
            List<User> assigned = TemplateImportService.loadAssignedUsers(r);
            assertEquals(1, assigned.size());
            assertEquals(bob.__getIDS(), assigned.get(0).__getIDS());
        }
        // Unchecked type preserved
        assertEquals(String.valueOf(Constants.TYPE_INVOICE), r1.__getMimetype());
        assertEquals(String.valueOf(Constants.TYPE_OFFER), r2.__getMimetype());
        // Untouched t3 preserved
        assertEquals(1, r3.__getGroupsids());
        assertEquals(2, TemplateImportService.loadAssignedUsers(r3).size());

        // Multi-delete t1 and t2, keep t3
        int deleted = TemplateImportService.deleteTemplates(Arrays.asList(r1, r2));
        assertEquals(2, deleted);
        List<DatabaseObject> remaining = DatabaseObject.getObjects(Context.getTemplate());
        assertEquals(1, remaining.size());
        assertEquals(t3.__getIDS(), remaining.get(0).__getIDS());
    }

    @Test
    void controlPanelTemplatesSwitchesToBulkModeOnMultiSelection() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        initDb();
        File f1 = createDummyOdt(tmp.resolve("ui/Rechnung_1.odt"));
        File f2 = createDummyOdt(tmp.resolve("ui/Angebot_2.odt"));
        TemplateImportService.importFiles(new File[]{f1, f2}, 1);

        final ControlPanel_Templates[] holder = new ControlPanel_Templates[1];
        SwingUtilities.invokeAndWait(() -> {
            ControlPanel_Templates panel = new ControlPanel_Templates();
            holder[0] = panel;
            assertFalse(panel.isBulkMode(), "Initial single selection should not be bulk mode");
            assertEquals(2, panel.getTemplatesTable().getRowCount());
            panel.getTemplatesTable().setRowSelectionInterval(0, 1);
            assertTrue(panel.isBulkMode(), "Selecting 2 rows should switch to bulk edit mode");
            assertEquals(2, panel.getSelectedTemplates().size());
            panel.getTemplatesTable().setRowSelectionInterval(0, 0);
            assertFalse(panel.isBulkMode(), "Selecting 1 row should return to single edit mode");
            panel.cancelPreview();
        });
        SwingUtilities.invokeAndWait(() -> { });
        holder[0].awaitPreviews(5000);
    }
}
