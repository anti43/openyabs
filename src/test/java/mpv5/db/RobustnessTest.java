package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.handler.EInvoiceImport;
import mpv5.utils.export.ZugpferdFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Trying to break it: broken and malicious e-invoice files, a broken invoice
 * template, e-invoices with missing data, incomplete customers when
 * e-invoices are the default.
 */
class RobustnessTest {

    @TempDir
    Path tmp;

    private static int count(String table) throws Exception {
        return EInvoiceRoundTripTest.count(table);
    }

    private static Object[][] rows(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null ? new Object[0][] : d;
    }

    private Item invoice(Contact c) throws Exception {
        Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
        inv.setContactsids(c.__getIDS());
        inv.setCname("Test");
        inv.setInttype(Item.TYPE_INVOICE);
        inv.setDatetodo(new Date());
        inv.setDateend(new Date());
        inv.setDateadded(new Date());
        inv.setAccountsids(1);
        assertTrue(inv.save(true));
        SubItem s = new SubItem();
        s.setItemsids(inv.__getIDS());
        s.setCname("Pos");
        s.setQuantityvalue(java.math.BigDecimal.ONE);
        s.setExternalvalue(new java.math.BigDecimal("100"));
        s.setInternalvalue(new java.math.BigDecimal("100"));
        s.setTaxpercentvalue(new java.math.BigDecimal("19"));
        s.setOrdernr(1);
        s.setDateadded(new Date());
        assertTrue(s.save(true));
        mpv5.handler.ItemService.updateTotals(inv);
        return (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());
    }

    @Test
    void tryingToBreakIt() throws Exception {
        Path sysTmp = tmp.resolve("systemp");
        Files.createDirectories(sysTmp);
        System.setProperty("java.io.tmpdir", sysTmp.toString());
        EInvoiceRoundTripTest.newInstallation(tmp);
        User admin = User.getCurrentUser();
        for (String[] kv : new String[][]{{"business", "Wir GmbH"}, {"street", "Hauptstr. 1"}, {"zip", "50667"}, {"city", "Köln"},
            {"country", "DE"}, {"vatid", "DE999999999"}, {"email", "info@wir.example"}, {"phone", "0221 123"}, {"iban", "DE02120300000000202051"}}) {
            admin.changeProperty("companyinfo." + kv[0], kv[1]);
        }
        admin.saveProperties();

        //--- 1. broken and malicious e-invoice files
        Path in = tmp.resolve("in");
        Files.createDirectories(in);
        Path secret = tmp.resolve("secret.txt");
        Files.write(secret, "TOP-SECRET-CONTENT".getBytes(StandardCharsets.UTF_8));
        String good = EInvoiceRoundTripTest.supplierInvoice("OK-1", "20260301", "Lieferant GmbH", "DE111111111", "Wir GmbH", "DE999999999", "10.00", "11.90");
        String[][] files = {
            {"empty.xml", ""},
            {"garbage.pdf", "this is not a pdf"},
            {"binary.xml", "\u0000\u0001\u0002ÿþ"},
            {"truncated.xml", good.substring(0, good.length() / 2)},
            {"wrong-root.xml", "<?xml version=\"1.0\"?><Rechnung><Nr>1</Nr></Rechnung>"},
            {"bad-amounts.xml", good.replace("OK-1", "BAD-1").replace("<ram:GrandTotalAmount>11.90", "<ram:GrandTotalAmount>abc")
                .replace("<ram:TaxBasisTotalAmount>10.00", "<ram:TaxBasisTotalAmount>x")},
            {"no-totals.xml", good.replace("OK-1", "NOTOT-1").replaceAll("<ram:SpecifiedTradeSettlementHeaderMonetarySummation>.*</ram:SpecifiedTradeSettlementHeaderMonetarySummation>", "")},
            {"xxe.xml", good.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE rsm:CrossIndustryInvoice [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>\n")
                .replace("<ram:ID>OK-1</ram:ID>", "<ram:ID>&xxe;</ram:ID>").replace("<ram:Name>Leistung</ram:Name>", "<ram:Name>&xxe;</ram:Name>")},
            {"bomb.xml", "<?xml version=\"1.0\"?><!DOCTYPE lolz [<!ENTITY lol \"lol\"><!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">"
                + "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\"><!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">"
                + "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\"><!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">"
                + "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\"><!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">"
                + "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">]>"
                + "<rsm:CrossIndustryInvoice xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\">&lol9;</rsm:CrossIndustryInvoice>"}
        };
        for (String[] f : files) {
            Files.write(in.resolve(f[0]), f[1].getBytes(StandardCharsets.ISO_8859_1));
        }
        //a real PDF without e-invoice data
        try (org.apache.pdfbox.pdmodel.PDDocument d = new org.apache.pdfbox.pdmodel.PDDocument()) {
            d.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            d.save(in.resolve("plain.pdf").toFile());
        }
        for (File f : in.toFile().listFiles()) {
            long start = System.currentTimeMillis();
            EInvoiceImport.Result r = assertDoesNotThrow(() -> mpv5.ui.dialogs.Popup.silently(() -> EInvoiceImport.importFile(f, null)), f.getName());
            System.out.println("IMPORT " + f.getName() + ": " + r.outcome + " - " + r.message() + " (" + (System.currentTimeMillis() - start) + " ms)");
            assertTrue(System.currentTimeMillis() - start < 30000, f.getName() + " took too long");
        }
        assertEquals(0, rows("SELECT ids FROM expenses WHERE cnumber IN ('BAD-1', 'NOTOT-1')").length, "broken amounts are not booked");
        //the folder import as well
        mpv5.handler.ZugpferdExpenseImport.Result folder = assertDoesNotThrow(() -> mpv5.handler.ZugpferdExpenseImport.importFromFolder(in.toFile()));
        System.out.println("FOLDER: " + folder.summary());
        //nothing half: every booking has its receipt, the secret never reaches the database
        assertEquals(count("expenses"), count("expense_receipts"), "every expense has its receipt");
        assertEquals(count("revenues"), count("revenue_receipts"), "every income has its receipt");
        for (Object[] r : rows("SELECT cnumber, description FROM expenses UNION ALL SELECT cnumber, description FROM revenues")) {
            assertFalse(String.valueOf(r[0]).contains("TOP-SECRET") || String.valueOf(r[1]).contains("TOP-SECRET"),
                    "external entities are not resolved (XXE): " + java.util.Arrays.toString(r));
            System.out.println("BOOKED: " + java.util.Arrays.toString(r));
        }

        //--- 2. e-invoices with missing data are not created
        Contact incomplete = new Contact();
        incomplete.setCname("Unvollstaendig");
        incomplete.setisCustomer(true);
        incomplete.setDateadded(new Date());
        assertTrue(incomplete.save(true));
        Item inv1 = invoice(incomplete);
        File x1 = tmp.resolve("out").resolve("missing.xml").toFile();
        x1.getParentFile().mkdirs();
        Exception e1 = new ZugpferdFile(inv1, x1, ZugpferdFile.Format.XRECHNUNG).waitFor();
        assertNotNull(e1, "refused");
        System.out.println("REFUSED: " + e1.getMessage());
        assertFalse(x1.exists(), "no file with missing data");
        assertTrue(mpv5.handler.DocumentFiles.verify(inv1).isEmpty(), "nothing recorded");

        Contact ok = new Contact();
        ok.setCname("Kunde");
        ok.setCompany("Kunde AG");
        ok.setStreet("Ring 2");
        ok.setZip("10115");
        ok.setCity("Berlin");
        ok.setCountry("DE");
        ok.setMailaddress("k@kunde.example");
        ok.setisCustomer(true);
        ok.setDateadded(new Date());
        assertTrue(ok.save(true));
        Item inv2 = invoice(ok);
        //the company without phone (BR-DE-6): refused as well
        admin.changeProperty("companyinfo.phone", "");
        admin.saveProperties();
        File x2 = tmp.resolve("out").resolve("nophone.xml").toFile();
        Exception e2 = new ZugpferdFile(inv2, x2, ZugpferdFile.Format.XRECHNUNG).waitFor();
        assertNotNull(e2, "refused without the company's phone");
        assertFalse(x2.exists());
        admin.changeProperty("companyinfo.phone", "0221 123");
        admin.saveProperties();
        File x3 = tmp.resolve("out").resolve("ok.xml").toFile();
        assertNull(new ZugpferdFile(inv2, x3, ZugpferdFile.Format.XRECHNUNG).waitFor(), "complete data: created");
        assertTrue(x3.isFile());

        //--- 3. a broken invoice template
        mpv5.db.objects.Template tpl = mpv5.handler.TemplateHandler.loadTemplate((long) inv2.templateGroupIds(), Item.TYPE_INVOICE);
        assertNotNull(tpl);
        //the file of the template damaged (as a broken file imported in the template manager)
        File tf = tpl.getFile();
        Files.write(tf.toPath(), "PK\u0003\u0004 this is no odt".getBytes(StandardCharsets.ISO_8859_1));
        mpv5.handler.TemplateHandler.clearCache();
        mpv5.db.objects.Template broken = assertDoesNotThrow(() -> mpv5.handler.TemplateHandler.loadTemplate((long) inv2.templateGroupIds(), Item.TYPE_INVOICE));
        assertNull(broken, "a broken template is not used");
        long start = System.currentTimeMillis();
        File zf = tmp.resolve("out").resolve("broken.pdf").toFile();
        Exception ze = assertDoesNotThrow(() -> mpv5.ui.dialogs.Popup.silently(() -> new ZugpferdFile(inv2, zf, ZugpferdFile.Format.ZUGFERD).waitFor()));
        System.out.println("BROKEN TEMPLATE ZUGFERD: " + ze);
        assertNotNull(ze, "no ZUGFeRD PDF without its PDF");
        assertFalse(zf.exists(), "no half written file");
        assertTrue(System.currentTimeMillis() - start < 60000, "no hang");

        //--- 4. e-invoices by default: an incomplete customer is not saved (contact panel)
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            mpv5.utils.export.EInvoiceDelivery.setZugpferdDefault(true);
            int before = count("contacts");
            mpv5.ui.panels.ModernContactPanel cp = EInvoiceRoundTripTest.onEdt(() -> new mpv5.ui.panels.ModernContactPanel(Context.getCustomer()));
            Contact c = new Contact();
            c.setCname("Ohne Adresse");
            c.setisCustomer(true);
            EInvoiceRoundTripTest.onEdt(() -> {
                cp.setDataOwner(c, true);
                mpv5.ui.toolbars.RecordActions.save(cp);
                return null;
            });
            assertEquals(before, count("contacts"), "an incomplete e-invoice customer is not saved");
            mpv5.utils.export.EInvoiceDelivery.setZugpferdDefault(false);
        }
    }
}
