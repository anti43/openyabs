package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Expense;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * E-invoices on a new installation: incoming invoices imported as expenses
 * and income, own invoices exported as e-invoice and imported again.
 */
class EInvoiceRoundTripTest {

    @TempDir
    Path tmp;

    static String supplierInvoice(String number, String date, String seller, String sellerVat, String buyer, String buyerVat, String net, String gross) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rsm:CrossIndustryInvoice xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\""
                + " xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\""
                + " xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\">\n"
                + "<rsm:ExchangedDocument><ram:ID>" + number + "</ram:ID><ram:TypeCode>380</ram:TypeCode>"
                + "<ram:IssueDateTime><udt:DateTimeString format=\"102\">" + date + "</udt:DateTimeString></ram:IssueDateTime></rsm:ExchangedDocument>\n"
                + "<rsm:SupplyChainTradeTransaction>"
                + "<ram:IncludedSupplyChainTradeLineItem><ram:SpecifiedTradeProduct><ram:Name>Leistung</ram:Name></ram:SpecifiedTradeProduct></ram:IncludedSupplyChainTradeLineItem>"
                + "<ram:ApplicableHeaderTradeAgreement>"
                + "<ram:SellerTradeParty><ram:Name>" + seller + "</ram:Name>"
                + (sellerVat == null ? "" : "<ram:SpecifiedTaxRegistration><ram:ID schemeID=\"VA\">" + sellerVat + "</ram:ID></ram:SpecifiedTaxRegistration>")
                + "</ram:SellerTradeParty>"
                + "<ram:BuyerTradeParty><ram:Name>" + buyer + "</ram:Name>"
                + (buyerVat == null ? "" : "<ram:SpecifiedTaxRegistration><ram:ID schemeID=\"VA\">" + buyerVat + "</ram:ID></ram:SpecifiedTaxRegistration>")
                + "</ram:BuyerTradeParty>"
                + "</ram:ApplicableHeaderTradeAgreement>"
                + "<ram:ApplicableHeaderTradeSettlement><ram:InvoiceCurrencyCode>EUR</ram:InvoiceCurrencyCode>"
                + "<ram:ApplicableTradeTax><ram:TypeCode>VAT</ram:TypeCode><ram:RateApplicablePercent>19.00</ram:RateApplicablePercent></ram:ApplicableTradeTax>"
                + "<ram:SpecifiedTradeSettlementHeaderMonetarySummation><ram:TaxBasisTotalAmount>" + net + "</ram:TaxBasisTotalAmount>"
                + "<ram:GrandTotalAmount>" + gross + "</ram:GrandTotalAmount></ram:SpecifiedTradeSettlementHeaderMonetarySummation>"
                + "</ram:ApplicableHeaderTradeSettlement></rsm:SupplyChainTradeTransaction>\n"
                + "</rsm:CrossIndustryInvoice>\n";
    }

    private void newInstallation() throws Exception {
        newInstallation(tmp);
    }

    static void newInstallation(Path tmp) throws Exception {
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
        QueryHandler.instanceOf();
        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        mpv5.handler.FirstStartSetup.prepare(admin, true, null);
    }

    static File write(Path dir, String name, String xml) throws Exception {
        File f = dir.resolve(name).toFile();
        f.getParentFile().mkdirs();
        Files.write(f.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    static int count(String table) throws Exception {
        return Integer.parseInt(String.valueOf(QueryHandler.getConnection().freeQuery("SELECT COUNT(*) FROM " + table,
                mpv5.usermanagement.MPSecurityManager.VIEW, null).getData()[0][0]));
    }

    static <T> T onEdt(java.util.concurrent.Callable<T> c) throws Exception {
        java.util.concurrent.atomic.AtomicReference<T> r = new java.util.concurrent.atomic.AtomicReference<>();
        java.util.concurrent.atomic.AtomicReference<Exception> ex = new java.util.concurrent.atomic.AtomicReference<>();
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            try {
                r.set(mpv5.ui.dialogs.Popup.silently(c));
            } catch (Exception e) {
                ex.set(e);
            }
        });
        if (ex.get() != null) {
            throw ex.get();
        }
        return r.get();
    }

    @Test
    void eInvoicesOnANewInstallation() throws Exception {
        scenario(tmp, null);
    }

    /**
     * The whole run: a new installation, e-invoices imported as expenses and
     * income, an own invoice exported as XRechnung and ZUGFeRD and imported
     * again, the e-invoice folder, changed files. The same with and without
     * plugins.
     *
     * @param tmp
     * @param afterInstall runs after the installation (e.g. starts a plugin),
     * may be null
     * @return the exported invoice
     */
    static mpv5.db.objects.Item scenario(Path tmp, java.util.concurrent.Callable<?> afterInstall) throws Exception {
        //files written outside the temp folder are recorded with their checksum
        Path sysTmp = tmp.resolve("systemp");
        Files.createDirectories(sysTmp);
        System.setProperty("java.io.tmpdir", sysTmp.toString());
        newInstallation(tmp);
        User admin = User.getCurrentUser();
        String[][] company = {{"business", "Wir GmbH"}, {"street", "Hauptstr. 1"}, {"zip", "50667"}, {"city", "Köln"},
            {"country", "DE"}, {"vatid", "DE 999 999 999"}, {"taxnumber", "214/5678/9012"}, {"email", "info@wir.example"},
            {"phone", "0221 123"}, {"iban", "DE02120300000000202051"}, {"bic", "BYLADEM1001"}, {"bankname", "Bank"},
            {"eori", "DE123456789012345"}, {"peppolid", "9930:DE999999999"}};
        for (String[] kv : company) {
            admin.changeProperty("companyinfo." + kv[0], kv[1]);
        }
        admin.saveProperties();
        if (afterInstall != null) {
            afterInstall.call();
        }
        Path in = tmp.resolve("in");

        //1. an incoming invoice of last year (buyer: the company) through the button of the expense panel:
        //   an expense with the date of the invoice, listed although the list shows this year
        File e1 = write(in, "lieferant-1.xml", supplierInvoice("LIEF-1", "20250310", "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999", "100.00", "119.00"));
        mpv5.ui.panels.ModernExpensePanel ep = onEdt(() -> new mpv5.ui.panels.ModernExpensePanel());
        java.lang.reflect.Method imp = mpv5.ui.panels.ModernExpensePanel.class.getDeclaredMethod("importZugferdFiles", java.util.List.class);
        imp.setAccessible(true);
        onEdt(() -> imp.invoke(ep, Collections.singletonList(e1)));
        javax.swing.SwingUtilities.invokeAndWait(() -> { });
        javax.swing.SwingUtilities.invokeAndWait(() -> { });
        assertEquals(1, count("expenses"));
        Expense exp = (Expense) DatabaseObject.getObjects(Context.getExpense()).get(0);
        assertEquals("LIEF-1", exp.__getCnumber());
        assertEquals("2025-03-10", new java.text.SimpleDateFormat("yyyy-MM-dd").format(exp.__getDateadded()));
        assertEquals(1, onEdt(() -> ep.getExpenseTable().getRowCount()), "the imported expense is listed");
        assertEquals(1, mpv5.handler.ExpenseReceipts.of(exp.__getIDS()).size());

        //2. an invoice the company wrote with another program (seller: the company), dropped on the expense
        //   panel: recognised as income
        File r1 = write(in, "eigene-alt.xml", supplierInvoice("ALT-77", "20260115", "Wir GmbH", "DE999999999", "Kunde AG", "DE222222222", "200.00", "238.00"));
        mpv5.handler.EInvoiceImport.Result rr = mpv5.handler.EInvoiceImport.importFile(r1, mpv5.handler.EInvoiceImport.Kind.EXPENSE);
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.IMPORTED, rr.outcome, rr.message());
        assertEquals(mpv5.handler.EInvoiceImport.Kind.INCOME, rr.kind);
        assertEquals(1, count("revenues"));
        assertEquals(1, count("revenue_receipts"));

        //3. again: the same file, the same invoice in another file
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.DUPLICATE_FILE, mpv5.handler.EInvoiceImport.importFile(e1, null).outcome);
        File e1b = write(in, "lieferant-1-kopie.xml", supplierInvoice("LIEF-1", "20250310", "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999", "100.00", "119.00") + "\n");
        mpv5.handler.EInvoiceImport.Result dup = mpv5.handler.EInvoiceImport.importFile(e1b, null);
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.DUPLICATE_NUMBER, dup.outcome, dup.message());
        assertTrue(dup.message().contains("LIEF-1") && dup.message().contains("2025"), dup.message());
        //another supplier with the same number is another invoice
        File e2 = write(in, "anderer.xml", supplierInvoice("LIEF-1", "20260401", "Anderer KG", "DE333333333", "Wir GmbH", "DE999999999", "10.00", "11.90"));
        assertEquals(mpv5.handler.EInvoiceImport.Outcome.IMPORTED, mpv5.handler.EInvoiceImport.importFile(e2, null).outcome);
        assertEquals(2, count("expenses"));
        assertEquals(1, count("revenues"));

        //4. an own invoice of YaBS, exported as XRechnung and ZUGFeRD
        mpv5.db.objects.Contact c = (mpv5.db.objects.Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname("Kunde AG");
        c.setCompany("Kunde AG");
        c.setStreet("Ring 2");
        c.setZip("10115");
        c.setCity("Berlin");
        c.setCountry("DE");
        c.setMailaddress("buchhaltung@kunde.example");
        c.setisCustomer(true);
        c.setDateadded(new java.util.Date());
        assertTrue(c.save(true));
        mpv5.handler.ContactService.savePeppolId(c, "0204:04011000-12345-34");
        mpv5.handler.ContactService.saveEori(c, "DE987654321098765");
        mpv5.db.objects.Item inv = (mpv5.db.objects.Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(c.__getIDS());
        inv.setCname("Beratung");
        inv.setInttype(mpv5.db.objects.Item.TYPE_INVOICE);
        inv.setDatetodo(new java.util.Date());
        inv.setDateend(new java.util.Date());
        inv.setDateadded(new java.util.Date());
        inv.setAccountsids(1);
        assertTrue(inv.save(true));
        for (int i = 1; i <= 2; i++) {
            mpv5.db.objects.SubItem s = new mpv5.db.objects.SubItem();
            s.setItemsids(inv.__getIDS());
            s.setCname("Pos " + i);
            s.setQuantityvalue(new java.math.BigDecimal(i));
            s.setExternalvalue(new java.math.BigDecimal("50.00"));
            s.setInternalvalue(new java.math.BigDecimal("50.00"));
            s.setTaxpercentvalue(new java.math.BigDecimal("19"));
            s.setOrdernr(i);
            s.setDateadded(new java.util.Date());
            assertTrue(s.save(true));
        }
        mpv5.handler.ItemService.updateTotals(inv);
        inv = (mpv5.db.objects.Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
        java.util.Map<String, Object> fields = inv.getFormFields();
        assertEquals("DE123456789012345", fields.get("companyinfo.eori"));
        assertEquals("9930:DE999999999", fields.get("companyinfo.peppolid"));
        assertEquals("DE987654321098765", fields.get("item.contact.eori"));
        assertEquals("0204:04011000-12345-34", fields.get("item.contact.peppolid"));
        Path out = tmp.resolve("out");
        Files.createDirectories(out);
        File xml = out.resolve(inv.__getCnumber() + ".xml").toFile();
        File pdf = out.resolve(inv.__getCnumber() + ".pdf").toFile();
        for (Object[] f : new Object[][]{{xml, mpv5.utils.export.ZugpferdFile.Format.XRECHNUNG}, {pdf, mpv5.utils.export.ZugpferdFile.Format.ZUGFERD}}) {
            mpv5.utils.export.ZugpferdFile z = new mpv5.utils.export.ZugpferdFile(inv, (File) f[0], (mpv5.utils.export.ZugpferdFile.Format) f[1]);
            Exception ex = z.waitFor();
            assertNull(ex, String.valueOf(ex));
            assertTrue(((File) f[0]).isFile() && ((File) f[0]).length() > 0);
            System.out.println("EXPORT " + f[1] + ": " + (z.getValidation() == null ? "not validated" : z.getValidation()));
            assertNotNull(z.getValidation(), "validated");
            assertTrue(z.getValidation().isValid(), f[1] + ": " + z.getValidation());
        }
        String exportedXml = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(exportedXml.contains("<ram:URIID schemeID=\"9930\">DE999999999</ram:URIID>"), exportedXml);
        assertTrue(exportedXml.contains("<ram:URIID schemeID=\"0204\">04011000-12345-34</ram:URIID>"), exportedXml);
        assertTrue(exportedXml.contains("<ram:Content>Seller EORI: DE123456789012345</ram:Content>"), exportedXml);
        assertTrue(exportedXml.contains("<ram:Content>Buyer EORI: DE987654321098765</ram:Content>"), exportedXml);
        //the checksums of both files are recorded and verified
        java.util.List<mpv5.handler.DocumentFiles.Entry> files = mpv5.handler.DocumentFiles.verify(inv);
        assertEquals(2, files.size(), String.valueOf(files));
        assertTrue(files.stream().allMatch(mpv5.handler.DocumentFiles.Entry::ok));

        //5. the exported files imported again: the own invoice, not a second booking
        mpv5.handler.ZugpferdExpenseImport.ParsedInvoice parsedXml = mpv5.handler.ZugpferdExpenseImport.parse(xml);
        assertNotNull(parsedXml, "the exported XRechnung is read");
        assertEquals(inv.__getCnumber(), parsedXml.invoiceNumber);
        assertEquals(0, new java.math.BigDecimal("178.50").compareTo(parsedXml.grossAmount), String.valueOf(parsedXml.grossAmount));
        assertNotNull(mpv5.handler.ZugpferdExpenseImport.parse(pdf), "the exported ZUGFeRD PDF is read");
        for (File f : new File[]{xml, pdf}) {
            mpv5.handler.EInvoiceImport.Result own = mpv5.handler.EInvoiceImport.importFile(f, mpv5.handler.EInvoiceImport.Kind.INCOME);
            assertEquals(mpv5.handler.EInvoiceImport.Outcome.OWN_INVOICE, own.outcome, own.message());
        }
        assertEquals(1, count("revenues"));
        assertEquals(2, count("expenses"));

        //6. the e-invoice folder: all of it again, nothing new
        mpv5.handler.ZugpferdExpenseImport.Result folder = mpv5.handler.ZugpferdExpenseImport.importFromFolder(in.toFile());
        System.out.println("FOLDER: " + folder.summary());
        assertEquals(0, folder.imported, folder.summary());
        File e3 = write(in, "neu.xml", supplierInvoice("LIEF-9", "20261101", "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999", "1.00", "1.19"));
        folder = mpv5.handler.ZugpferdExpenseImport.importFromFolder(in.toFile());
        assertEquals(1, folder.imported, folder.summary());
        assertEquals(1, folder.expenses.size());
        assertEquals(3, count("expenses"));

        //7. a changed file is found
        Files.write(xml.toPath(), "x".getBytes(StandardCharsets.UTF_8), java.nio.file.StandardOpenOption.APPEND);
        files = mpv5.handler.DocumentFiles.verify(inv);
        assertEquals(1, files.stream().filter(f -> "CHANGED".equals(f.state)).count());
        //receipts unchanged
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getExpense())) {
            for (mpv5.handler.ExpenseReceipts.Receipt r : mpv5.handler.ExpenseReceipts.of(o.__getIDS())) {
                assertTrue(mpv5.handler.ExpenseReceipts.verify(r));
            }
        }
        return inv;
    }
}
