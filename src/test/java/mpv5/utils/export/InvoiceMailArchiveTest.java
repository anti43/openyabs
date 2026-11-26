package mpv5.utils.export;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.CompanyInfo;
import mpv5.globals.LocalSettings;
import mpv5.handler.Activity;
import mpv5.handler.TemplateHandler;
import mpv5.mail.SimpleMail;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Company;
import mpv5.usermanagement.MPSecurityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the "Invoice email archive" card in {@link ControlPanel_Company}
 * and automatic ZUGFeRD PDF + XRechnung XML email archiving in
 * {@link InvoiceMailArchive} upon invoice or cancellation/credit note export /
 * finalization.
 */
class InvoiceMailArchiveTest {

    private Path tempDir;

    private final List<SimpleMail> sentMails = new ArrayList<>();
    private final List<byte[]> capturedPdfBytes = new ArrayList<>();
    private final List<String> capturedXmlContents = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        sentMails.clear();
        capturedPdfBytes.clear();
        capturedXmlContents.clear();
        InvoiceMailArchive.clearSentHistory();

        tempDir = Files.createTempDirectory("yabs-mailarch-test-");
        File dir = tempDir.toFile();
        dir.deleteOnExit();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tempDir.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs_mailarch");
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
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs_mailarch", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.restart();
        mpv5.globals.GlobalSettings.read();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);

        CompanyInfo.setProperty("business", "Archiv Test GmbH");
        CompanyInfo.setProperty("name", "Mustermann");
        CompanyInfo.setProperty("firstname", "Max");
        CompanyInfo.setProperty("street", "Hauptstr. 1");
        CompanyInfo.setProperty("zip", "10115");
        CompanyInfo.setProperty("city", "Berlin");
        CompanyInfo.setProperty("country", "DE");
        CompanyInfo.setProperty("email", "info@archiv-test.de");
        CompanyInfo.setProperty("phone", "030123456");
        CompanyInfo.setProperty("account", "DE89370400440532013000");
        CompanyInfo.setProperty("banknumber", "COBADEFFXXX");
        CompanyInfo.setProperty("account1", "DE123456789");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_ADDRESS, "");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_FORMAT, "");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_SUBJECT, "");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_BODY, "");
        CompanyInfo.save();

        InvoiceMailArchive.setMailSender(mail -> {
            sentMails.add(mail);
            for (File f : mail.getAttachments()) {
                assertTrue(f.isFile() && f.length() > 0, "Attachment must exist and be non-empty: " + f);
                try {
                    if (f.getName().toLowerCase().endsWith(".pdf")) {
                        assertTrue(InvoiceMailArchive.hasEmbeddedXml(f),
                                "PDF attachment must be a ZUGFeRD PDF with embedded XML: " + f);
                        capturedPdfBytes.add(Files.readAllBytes(f.toPath()));
                    } else if (f.getName().toLowerCase().endsWith(".xml")) {
                        capturedXmlContents.add(Files.readString(f.toPath(), StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @AfterEach
    void tearDown() {
        InvoiceMailArchive.setMailSender(null);
        InvoiceMailArchive.clearSentHistory();
        DatabaseConnection.shutdown();
        DatabaseObject.clearCache();
        TemplateHandler.clearCache();
    }

    @Test
    void companyPanel_savesAndLoadsMailArchiveCardAndHasSmtpButton() throws Exception {
        ControlPanel_Company panel = new ControlPanel_Company();
        assertNotNull(panel.getMailArchiveAddressField());
        assertNotNull(panel.getMailArchiveFormatCombo());
        assertNotNull(panel.getMailArchiveSubjectField());
        assertNotNull(panel.getMailArchiveBodyArea());
        assertNotNull(panel.getSmtpSettingsButton());
        assertTrue(panel.getSmtpSettingsButton().getActionListeners().length > 0,
                "SMTP settings button must have an action listener linking to SMTP settings");
        assertEquals(InvoiceMailArchive.ArchiveFormat.BOTH, panel.getMailArchiveFormatCombo().getSelectedItem(),
                "Default archive format must be BOTH");

        panel.getMailArchiveAddressField().setText("archive@example.com; tax@example.com");
        panel.getMailArchiveFormatCombo().setSelectedItem(InvoiceMailArchive.ArchiveFormat.XRECHNUNG);
        panel.getMailArchiveSubjectField().setText("Archiv: [type] [cnumber] ([contact.cname])");
        panel.getMailArchiveBodyArea().setText("Beleg [cnumber] fuer [contact.cname] ueber [grosvaluef]");

        Method setSettings = ControlPanel_Company.class.getDeclaredMethod("setSettings");
        setSettings.setAccessible(true);
        setSettings.invoke(panel);
        CompanyInfo.save();

        assertEquals("archive@example.com; tax@example.com",
                CompanyInfo.getProperty(InvoiceMailArchive.KEY_ADDRESS));
        assertEquals("xrechnung",
                CompanyInfo.getProperty(InvoiceMailArchive.KEY_FORMAT));
        assertEquals(InvoiceMailArchive.ArchiveFormat.XRECHNUNG, InvoiceMailArchive.getFormat());
        assertEquals("Archiv: [type] [cnumber] ([contact.cname])",
                CompanyInfo.getProperty(InvoiceMailArchive.KEY_SUBJECT));
        assertEquals("Beleg [cnumber] fuer [contact.cname] ueber [grosvaluef]",
                CompanyInfo.getProperty(InvoiceMailArchive.KEY_BODY));
        assertEquals(List.of("archive@example.com", "tax@example.com"),
                InvoiceMailArchive.getValidAddresses());

        // Reload in a fresh ControlPanel_Company instance
        ControlPanel_Company reloaded = new ControlPanel_Company();
        assertEquals("archive@example.com; tax@example.com",
                reloaded.getMailArchiveAddressField().getText());
        assertEquals(InvoiceMailArchive.ArchiveFormat.XRECHNUNG,
                reloaded.getMailArchiveFormatCombo().getSelectedItem());
        assertEquals("Archiv: [type] [cnumber] ([contact.cname])",
                reloaded.getMailArchiveSubjectField().getText());
        assertEquals("Beleg [cnumber] fuer [contact.cname] ueber [grosvaluef]",
                reloaded.getMailArchiveBodyArea().getText());

        // Switch to ZUGFeRD-only, save and reload
        reloaded.getMailArchiveFormatCombo().setSelectedItem(InvoiceMailArchive.ArchiveFormat.ZUGFERD);
        setSettings.invoke(reloaded);
        CompanyInfo.save();
        assertEquals(InvoiceMailArchive.ArchiveFormat.ZUGFERD, InvoiceMailArchive.getFormat());
        ControlPanel_Company reloaded2 = new ControlPanel_Company();
        assertEquals(InvoiceMailArchive.ArchiveFormat.ZUGFERD,
                reloaded2.getMailArchiveFormatCombo().getSelectedItem());
    }

    @Test
    void exportAndFinalize_sendsZugferdPdfAndXmlEInvoiceWithHistoryEntryForInvoiceAndStorno() throws Exception {
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_ADDRESS, "archive@example.com");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_SUBJECT, "Archiv [type] [cnumber] - [contact.cname]");
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_BODY, "Beleg [cnumber] ([type]) fuer [contact.cname]");
        CompanyInfo.save();

        Contact customer = (Contact) DatabaseObject.getObject(Context.getContact());
        customer.setCname("Kunde Archiv AG");
        customer.setCompany("Kunde Archiv AG");
        customer.setisCompany(true);
        customer.setisCustomer(true);
        customer.setStreet("Kundenweg 5");
        customer.setZip("80331");
        customer.setCity("Muenchen");
        customer.setCountry("DE");
        customer.setMailaddress("kunde@example.com");
        customer.setDateadded(new Date());
        assertTrue(customer.save(true));

        // 1. Export an invoice
        Item invoice = (Item) DatabaseObject.getObject(Context.getInvoice());
        invoice.avoidNulls();
        invoice.setInttype(Item.TYPE_INVOICE);
        invoice.setCnumber("RE-2026-9001");
        invoice.setCname("RE-2026-9001");
        invoice.setContactsids(customer.__getIDS());
        invoice.setGroupsids(1);
        invoice.setAccountsids(1);
        invoice.setDateadded(new Date());
        invoice.setDatetodo(new Date());
        invoice.setDateend(new Date());
        invoice.setIntstatus(Item.STATUS_FINISHED);
        invoice.setNetvalue(new BigDecimal("100.00"));
        invoice.setTaxvalue(new BigDecimal("19.00"));
        assertTrue(invoice.save(true));
        invoice.setCnumber("RE-2026-9001");
        invoice.setCname("RE-2026-9001");
        assertTrue(invoice.save(true));

        SubItem pos = new SubItem();
        pos.setOrdernr(0);
        pos.setCname("");
        pos.setItemsids(invoice.__getIDS());
        pos.setCountvalue(BigDecimal.ONE);
        pos.setDatedelivery(invoice.__getDatetodo());
        pos.setDescription("Beratungsleistung");
        pos.setExternalvalue(new BigDecimal("100.00"));
        pos.setInternalvalue(new BigDecimal("100.00"));
        pos.setMeasure("Stk");
        pos.setOriginalproductsids(-1);
        pos.setLinkurl("");
        pos.setQuantityvalue(BigDecimal.ONE);
        pos.setTaxpercentvalue(new BigDecimal("19.00"));
        SubItem.calculate(pos);
        pos.setDateadded(new Date());
        pos.setGroupsids(invoice.__getGroupsids());
        assertTrue(pos.save(true));

        Path exportDir = Files.createDirectories(Path.of("build", "tmp", "mailarchive-test-exports"));
        File exportedXml = exportDir.resolve("RE-2026-9001.xml").toFile();
        exportedXml.deleteOnExit();
        Files.writeString(exportedXml.toPath(), "<xml/>", StandardCharsets.UTF_8);
        Activity.file(exportedXml, invoice);

        assertEquals(1, sentMails.size(), () -> "Exporting an invoice must send one archive email; lastError="
                + InvoiceMailArchive.getLastError() + ", isTemp=" + mpv5.handler.DocumentFiles.isTemporary(exportedXml)
                + ", isEnabled=" + InvoiceMailArchive.isEnabled()
                + ", supports=" + ZugpferdFile.supports(invoice));
        SimpleMail invMail = sentMails.get(0);
        assertEquals("archive@example.com", invMail.getRecipientsAddress());
        assertTrue(invMail.getSubject().contains("RE-2026-9001"), "Subject should resolve [cnumber]: " + invMail.getSubject());
        assertTrue(invMail.getSubject().contains("Kunde Archiv AG"), "Subject should resolve [contact.cname]: " + invMail.getSubject());
        assertTrue(invMail.getText().contains("RE-2026-9001"), "Body should resolve [cnumber]: " + invMail.getText());
        assertEquals(2, invMail.getAttachments().size(), "Archive email must contain both ZUGFeRD PDF and XML e-invoice");
        assertEquals(1, capturedPdfBytes.size(), "Must have captured 1 ZUGFeRD PDF attachment");
        assertEquals(1, capturedXmlContents.size(), "Must have captured 1 XRechnung XML attachment");
        assertTrue(capturedXmlContents.get(0).contains("RE-2026-9001"), "XML attachment must contain invoice number");

        // Deduplication: second file event for same invoice must not send a second email
        File exportedPdf = exportDir.resolve("RE-2026-9001.pdf").toFile();
        exportedPdf.deleteOnExit();
        Activity.file(exportedPdf, invoice);
        exportedXml.delete();
        exportedPdf.delete();
        assertEquals(1, sentMails.size(), "Duplicate export event for the same invoice must not send another archive email");

        // Offers (TYPE_OFFER) and Orders (TYPE_ORDER) must be excluded when exported/printed
        Item offer = (Item) DatabaseObject.getObject(Context.getOffer());
        offer.avoidNulls();
        offer.setInttype(Item.TYPE_OFFER);
        offer.setCnumber("AN-2026-0001");
        offer.setCname("AN-2026-0001");
        offer.setContactsids(customer.__getIDS());
        offer.setGroupsids(1);
        offer.setAccountsids(1);
        offer.setDateadded(new Date());
        offer.setDatetodo(new Date());
        offer.setDateend(new Date());
        assertTrue(offer.save(true));
        File offerPdf = exportDir.resolve("AN-2026-0001.pdf").toFile();
        offerPdf.deleteOnExit();
        Files.writeString(offerPdf.toPath(), "pdf", StandardCharsets.UTF_8);
        Activity.file(offerPdf, offer);
        offerPdf.delete();

        Item order = (Item) DatabaseObject.getObject(Context.getOrder());
        order.avoidNulls();
        order.setInttype(Item.TYPE_ORDER);
        order.setCnumber("AU-2026-0001");
        order.setCname("AU-2026-0001");
        order.setContactsids(customer.__getIDS());
        order.setGroupsids(1);
        order.setAccountsids(1);
        order.setDateadded(new Date());
        order.setDatetodo(new Date());
        order.setDateend(new Date());
        assertTrue(order.save(true));
        File orderPdf = exportDir.resolve("AU-2026-0001.pdf").toFile();
        orderPdf.deleteOnExit();
        Files.writeString(orderPdf.toPath(), "pdf", StandardCharsets.UTF_8);
        Activity.file(orderPdf, order);
        orderPdf.delete();

        assertEquals(1, sentMails.size(), "Exporting/printing an Offer or Order must NOT send an archive email");

        // Verify YaBS database history entry for invoice
        ReturnValue invHist = QueryHandler.instanceOf().clone(Context.getHistory()).freeSelectQuery(
                "SELECT cname, dbidentity, intitem FROM history WHERE dbidentity = 'items' AND intitem = " + invoice.__getIDS(),
                MPSecurityManager.VIEW, null);
        assertTrue(invHist != null && invHist.hasData(), "History table must contain an entry for the archived invoice");
        boolean foundInvHistory = false;
        for (Object[] row : invHist.getData()) {
            String msg = String.valueOf(row[0]);
            if (msg.contains("archive@example.com") && msg.contains("RE-2026-9001")) {
                foundInvHistory = true;
                break;
            }
        }
        assertTrue(foundInvHistory, "History entry must mention recipient and invoice number");

        // 2. Export/print a Storno-Beleg / cancellation credit note (TYPE_CREDIT)
        Item storno = (Item) DatabaseObject.getObject(Context.getCredit());
        storno.avoidNulls();
        storno.setInttype(Item.TYPE_CREDIT);
        storno.setCnumber("ST-2026-9001");
        storno.setCname("ST-2026-9001");
        storno.setDescription("Storno zu Rechnung Nr. RE-2026-9001");
        storno.setContactsids(customer.__getIDS());
        storno.setGroupsids(1);
        storno.setAccountsids(1);
        storno.setDateadded(new Date());
        storno.setDatetodo(new Date());
        storno.setDateend(new Date());
        storno.setIntstatus(Item.STATUS_FINISHED);
        storno.setNetvalue(new BigDecimal("100.00"));
        storno.setTaxvalue(new BigDecimal("19.00"));
        assertTrue(storno.save(true));
        storno.setCnumber("ST-2026-9001");
        storno.setCname("ST-2026-9001");
        assertTrue(storno.save(true));

        SubItem stornoPos = new SubItem();
        stornoPos.setOrdernr(0);
        stornoPos.setCname("");
        stornoPos.setItemsids(storno.__getIDS());
        stornoPos.setCountvalue(BigDecimal.ONE);
        stornoPos.setDatedelivery(storno.__getDatetodo());
        stornoPos.setDescription("Storno Beratungsleistung");
        stornoPos.setExternalvalue(new BigDecimal("100.00"));
        stornoPos.setInternalvalue(new BigDecimal("100.00"));
        stornoPos.setMeasure("Stk");
        stornoPos.setOriginalproductsids(-1);
        stornoPos.setLinkurl("");
        stornoPos.setQuantityvalue(BigDecimal.ONE);
        stornoPos.setTaxpercentvalue(new BigDecimal("19.00"));
        SubItem.calculate(stornoPos);
        stornoPos.setDateadded(new Date());
        stornoPos.setGroupsids(storno.__getGroupsids());
        assertTrue(stornoPos.save(true));

        File stornoXml = exportDir.resolve("ST-2026-9001.xml").toFile();
        stornoXml.deleteOnExit();
        Files.writeString(stornoXml.toPath(), "<xml/>", StandardCharsets.UTF_8);
        Activity.file(stornoXml, storno);
        stornoXml.delete();

        assertEquals(2, sentMails.size(), "Exporting/printing a Storno-Beleg (TYPE_CREDIT) must send an archive email");
        SimpleMail stornoMail = sentMails.get(1);
        assertTrue(stornoMail.getSubject().contains("ST-2026-9001"));
        assertEquals(2, stornoMail.getAttachments().size());
        assertEquals(2, capturedPdfBytes.size());
        assertEquals(2, capturedXmlContents.size());
        assertTrue(capturedXmlContents.get(1).contains("ST-2026-9001"));
        assertTrue(capturedXmlContents.get(1).contains("381"),
                "Credit note / Storno XML must use TypeCode 381 (credit note / cancellation invoice)");

        // Verify YaBS database history entry for Storno-Beleg
        ReturnValue stornoHist = QueryHandler.instanceOf().clone(Context.getHistory()).freeSelectQuery(
                "SELECT cname FROM history WHERE dbidentity = 'items' AND intitem = " + storno.__getIDS(),
                MPSecurityManager.VIEW, null);
        assertTrue(stornoHist != null && stornoHist.hasData(), "History table must contain an entry for the archived Storno-Beleg");
    }

    @Test
    void archiveFormatSelection_sendsOnlyConfiguredAttachments() throws Exception {
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_ADDRESS, "archive@example.com");
        CompanyInfo.save();

        Contact customer = (Contact) DatabaseObject.getObject(Context.getContact());
        customer.setCname("Format Kunde GmbH");
        customer.setCompany("Format Kunde GmbH");
        customer.setisCompany(true);
        customer.setisCustomer(true);
        customer.setStreet("Teststr. 10");
        customer.setZip("10115");
        customer.setCity("Berlin");
        customer.setCountry("DE");
        customer.setMailaddress("format@example.com");
        customer.setDateadded(new Date());
        assertTrue(customer.save(true));

        Item invoice = (Item) DatabaseObject.getObject(Context.getInvoice());
        invoice.avoidNulls();
        invoice.setInttype(Item.TYPE_INVOICE);
        invoice.setCnumber("RE-2026-9050");
        invoice.setCname("RE-2026-9050");
        invoice.setContactsids(customer.__getIDS());
        invoice.setGroupsids(1);
        invoice.setAccountsids(1);
        invoice.setDateadded(new Date());
        invoice.setDatetodo(new Date());
        invoice.setDateend(new Date());
        invoice.setIntstatus(Item.STATUS_FINISHED);
        invoice.setNetvalue(new BigDecimal("200.00"));
        invoice.setTaxvalue(new BigDecimal("38.00"));
        assertTrue(invoice.save(true));
        invoice.setCnumber("RE-2026-9050");
        invoice.setCname("RE-2026-9050");
        assertTrue(invoice.save(true));

        SubItem pos = new SubItem();
        pos.setOrdernr(0);
        pos.setCname("");
        pos.setItemsids(invoice.__getIDS());
        pos.setCountvalue(BigDecimal.ONE);
        pos.setDatedelivery(invoice.__getDatetodo());
        pos.setDescription("Software-Wartung");
        pos.setExternalvalue(new BigDecimal("200.00"));
        pos.setInternalvalue(new BigDecimal("200.00"));
        pos.setMeasure("Stk");
        pos.setOriginalproductsids(-1);
        pos.setLinkurl("");
        pos.setQuantityvalue(BigDecimal.ONE);
        pos.setTaxpercentvalue(new BigDecimal("19.00"));
        SubItem.calculate(pos);
        pos.setDateadded(new Date());
        pos.setGroupsids(invoice.__getGroupsids());
        assertTrue(pos.save(true));

        // 1. ZUGFeRD PDF only
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_FORMAT, InvoiceMailArchive.ArchiveFormat.ZUGFERD.key());
        CompanyInfo.save();
        assertTrue(InvoiceMailArchive.sendArchiveMail(invoice, null));
        assertEquals(1, sentMails.size());
        assertEquals(1, sentMails.get(0).getAttachments().size(), "ZUGFERD-only must attach exactly 1 file");
        assertTrue(sentMails.get(0).getAttachments().get(0).getName().toLowerCase().endsWith(".pdf"));
        assertEquals(1, capturedPdfBytes.size());
        assertEquals(0, capturedXmlContents.size());

        // 2. XRechnung XML only (e-invoice)
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_FORMAT, InvoiceMailArchive.ArchiveFormat.XRECHNUNG.key());
        CompanyInfo.save();
        assertTrue(InvoiceMailArchive.sendArchiveMail(invoice, null));
        assertEquals(2, sentMails.size());
        assertEquals(1, sentMails.get(1).getAttachments().size(), "XRECHNUNG-only must attach exactly 1 file");
        assertTrue(sentMails.get(1).getAttachments().get(0).getName().toLowerCase().endsWith(".xml"));
        assertEquals(1, capturedPdfBytes.size(), "No PDF should be generated for XRECHNUNG-only");
        assertEquals(1, capturedXmlContents.size());
        assertTrue(capturedXmlContents.get(0).contains("RE-2026-9050"));

        // 3. Both (ZUGFeRD PDF + XRechnung XML)
        CompanyInfo.setProperty(InvoiceMailArchive.KEY_FORMAT, InvoiceMailArchive.ArchiveFormat.BOTH.key());
        CompanyInfo.save();
        assertTrue(InvoiceMailArchive.sendArchiveMail(invoice, null));
        assertEquals(3, sentMails.size());
        assertEquals(2, sentMails.get(2).getAttachments().size(), "BOTH must attach ZUGFeRD PDF and XRechnung XML");
        assertEquals(2, capturedPdfBytes.size());
        assertEquals(2, capturedXmlContents.size());
    }
}
