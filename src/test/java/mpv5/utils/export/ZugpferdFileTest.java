package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import java.io.FileOutputStream;
import java.io.OutputStream;
import mpv5.utils.date.DateConverter;
import mpv5.utils.xdocreport.EmbeddingFontRegistry;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.junit.jupiter.api.io.TempDir;
import org.mustangproject.Invoice;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

class ZugpferdFileTest {

    @TempDir
    Path tmp;

    static Map<String, String> company() {
        Map<String, String> c = new HashMap<String, String>();
        c.put("business", "Muster Software GmbH");
        c.put("firstname", "Erika");
        c.put("name", "Mustermann");
        c.put("street", "Hauptstraße 1");
        c.put("zip", "10115");
        c.put("city", "Berlin");
        c.put("country", "Deutschland");
        c.put("phone", "+49 30 123456");
        c.put("email", "rechnung@muster.example");
        c.put("taxnumber", "30/123/45678");
        c.put("account1", "DE123456789");
        c.put("account", "DE02 1203 0000 0000 2020 51");
        c.put("banknumber", "BYLADEM1001");
        return c;
    }

    static Item invoice() {
        Item item = new Item();
        item.setCnumber("R-2026-0042");
        item.setInttype(Item.TYPE_INVOICE);
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 4, 0, 0, 0);
        item.setDateadded(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 14);
        item.setDatetodo(cal.getTime());
        return item;
    }

    static SubItem line(String name, String text, String qty, String measure, String price, String tax, String discount) {
        SubItem s = new SubItem();
        s.setCname(name);
        s.setDescription(text);
        s.setQuantityvalue(new BigDecimal(qty));
        s.setMeasure(measure);
        s.setExternalvalue(new BigDecimal(price));
        s.setTaxpercentvalue(new BigDecimal(tax));
        s.setDiscount(new BigDecimal(discount));
        return s;
    }

    static TradeParty buyer() {
        Contact c = new Contact();
        c.setCNumber("K-1001");
        c.setMailaddress("einkauf@kunde.example");
        c.setMainphone("+49 89 987654");
        Address a = new Address();
        a.setCompany("Kunde AG");
        a.setPrename("Max");
        a.setCname("Kunde");
        a.setStreet("Marienplatz 2");
        a.setZip("80331");
        a.setCity("München");
        a.setCountry("276");
        a.setTaxnumber("DE987654321");
        return c.asZugpferdTradeparty(a);
    }

    static Invoice build() {
        SubItem[] lines = {
            line("Softwarelizenz", "<b>Softwarelizenz</b> Jahresabo", "2", "Stk", "100", "19", "10"),
            line("   ", "Beratung vor Ort", "1.5", "Std.", "80", "19", "0"),
            line("   ", "Hinweis: Lieferung frei Haus", "0", "", "0", "0", "0")
        };
        Map<String, String> c = company();
        return ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), buyer(), "04011000-12345-34", 0, null);
    }

    @Test
    void mapsHelpers() {
        assertEquals("DE", ZugpferdFile.toCountryCode("276"));
        assertEquals("AT", ZugpferdFile.toCountryCode("040"));
        assertEquals("CH", ZugpferdFile.toCountryCode("Switzerland"));
        assertEquals("DE", ZugpferdFile.toCountryCode("Deutschland"));
        assertEquals("DE", ZugpferdFile.toCountryCode(null));
        assertEquals("HUR", ZugpferdFile.toUnitCode("Std."));
        assertEquals("C62", ZugpferdFile.toUnitCode("Stück"));
        assertEquals("LS", ZugpferdFile.toUnitCode("pauschal"));
        assertEquals("KGM", ZugpferdFile.toUnitCode("KGM"));
        assertEquals("C62", ZugpferdFile.toUnitCode("irgendwas"));
        assertEquals("C62", ZugpferdFile.toUnitCode("EUR"), "not a unit code");
        assertEquals("US", ZugpferdFile.toCountryCode("USA"));
        assertEquals("GB", ZugpferdFile.toCountryCode("UK"));
        assertEquals("AT", ZugpferdFile.toCountryCode("Österreich"));
        assertEquals("", ZugpferdFile.toCountryCode("Atlantis"), "unknown must not silently become DE");
        assertEquals("", ZugpferdFile.toCountryCode("999"));
        assertEquals("", ZugpferdFile.toCountryCode("99999999999"));
        assertEquals("a & b\nc", ZugpferdFile.plainText("<p>a &amp; <b>b</b></p><div>c</div>"));
    }

    @Test
    void createsXRechnungXml() throws Exception {
        Invoice invoice = build();
        assertTrue(ZugpferdFile.validate(invoice).isEmpty(), String.valueOf(ZugpferdFile.validate(invoice)));
        assertEquals(2, invoice.getZFItems().length, "text row must be skipped");

        File xml = tmp.resolve("R-2026-0042.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(s.contains("urn:xeinkauf.de:kosit:xrechnung"), "XRechnung guideline id");
        assertTrue(s.contains("04011000-12345-34"), "buyer reference");
        assertTrue(s.contains("DE123456789"), "seller VAT id");
        assertTrue(s.contains("DE987654321"), "buyer VAT id");
        assertTrue(s.contains("DE02120300000000202051"), "IBAN");
        assertTrue(s.contains("München"), "UTF-8 encoding");
        assertTrue(s.contains("unitCode=\"HUR\""), "unit code");
        assertTrue(s.matches("(?s).*<ram:TaxBasisTotalAmount>300.00</ram:TaxBasisTotalAmount>.*"), s);
        assertTrue(s.matches("(?s).*<ram:GrandTotalAmount>357.00</ram:GrandTotalAmount>.*"), s);
        assertValid(xml);
    }

    @Test
    void dueDateIsInvoiceDatePlusPaymentTerm() throws Exception {
        //YaBS sets "datetodo" (last edited) to the day of saving - it must not be used as due date
        Item item = invoice();
        item.setDatetodo(item.__getDateadded());
        SubItem[] lines = {line("a", "", "1", "Stk", "100", "19", "0")};
        Map<String, String> c = company();
        Invoice withTerm = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", 30, null);
        assertEquals(DateConverter.addDays(item.__getDateadded(), 30), withTerm.getDueDate());

        item.setDatetodo(null);
        Invoice noTerm = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        assertEquals(DateConverter.addDays(item.__getDateadded(), ZugpferdFile.DEFAULT_PAYTERM_DAYS), noTerm.getDueDate());

        File xml = tmp.resolve("noterm.xml").toFile();
        ZugpferdFile.writeXml(noTerm, xml);
        assertValid(xml);
    }

    @Test
    void sellerNeedsVatId() throws Exception {
        Map<String, String> c = company();
        c.remove("account1");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        List<String> problems = ZugpferdFile.validate(invoice);
        assertTrue(problems.toString().contains(mpv5.globals.Messages.ZUGFERD_FIELD_VATID.toString()), problems.toString());

        //and the official validator agrees (BR-CO-26)
        File xml = tmp.resolve("novat.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        EInvoiceValidator.Result result = EInvoiceValidator.validate(xml);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().toString().contains("BR-CO-26"), result.getErrors().toString());
    }

    @Test
    void findsIbanAndBicWhateverFieldTheyAreIn() {
        //older German labels: "Konto" = account, "IBAN" = banknumber
        Map<String, String> c = company();
        c.put("account", "1234567890");
        c.put("banknumber", "DE02 1203 0000 0000 2020 51");
        c.put("bic", "BYLADEM1001");
        TradeParty sender = ZugpferdFile.createSender(c::get);
        assertEquals("DE02120300000000202051", sender.getBankDetails().get(0).getIBAN());
        assertEquals("BYLADEM1001", sender.getBankDetails().get(0).getBIC());
    }

    @Test
    void reportsInvalidIban() {
        Map<String, String> c = company();
        c.put("account", "DE03 1203 0000 0000 2020 51");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        String problems = ZugpferdFile.validate(invoice).toString();
        assertTrue(problems.contains(mpv5.globals.Messages.ZUGFERD_INVALID_IBAN.toString()), problems);
    }

    @Test
    void reportsUnknownCountry() {
        Map<String, String> c = company();
        c.put("country", "Atlantis");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        String problems = ZugpferdFile.validate(invoice).toString();
        assertTrue(problems.contains(mpv5.globals.Messages.ZUGFERD_FIELD_COUNTRY.toString()), problems);
    }

    @Test
    void depositInvoiceCodes() throws Exception {
        assertNull(ZugpferdFile.documentCode(Item.TYPE_DEPOSIT, ZugpferdFile.Format.XRECHNUNG), "386 violates BR-DE-17");
        assertEquals("386", ZugpferdFile.documentCode(Item.TYPE_DEPOSIT, ZugpferdFile.Format.ZUGFERD));
        assertEquals("326", ZugpferdFile.documentCode(Item.TYPE_PART_PAYMENT, ZugpferdFile.Format.XRECHNUNG));
        assertNull(ZugpferdFile.documentCode(Item.TYPE_INVOICE, ZugpferdFile.Format.ZUGFERD));

        Item deposit = invoice();
        deposit.setInttype(Item.TYPE_DEPOSIT);
        Map<String, String> c = company();
        Invoice invoice = ZugpferdFile.createInvoice(deposit, new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null, ZugpferdFile.Format.XRECHNUNG);
        File xml = tmp.resolve("deposit.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        assertValid(xml);
    }

    @Test
    void checksVatIdFormat() {
        assertTrue(ZugpferdFile.isValidVatId("DE123456789"));
        assertTrue(ZugpferdFile.isValidVatId("de 123 456 789"));
        assertTrue(ZugpferdFile.isValidVatId("ATU12345678"));
        assertTrue(ZugpferdFile.isValidVatId("EL123456789"));
        assertTrue(ZugpferdFile.isValidVatId("FRXX123456789"));
        assertFalse(ZugpferdFile.isValidVatId("123456789"), "no prefix");
        assertFalse(ZugpferdFile.isValidVatId("DE12345678"), "German VAT ID needs 9 digits");
        assertFalse(ZugpferdFile.isValidVatId("30/123/45678"), "tax number");
        assertFalse(ZugpferdFile.isValidVatId("XX123456789"));
        assertFalse(ZugpferdFile.isValidVatId(null));
    }

    /**
     * Reported case: seller VAT ID without country prefix -> BR-CO-09, buyer
     * with a tax number -> FX-SCH-A-000570. The pre-check must catch both and
     * the validator result must say what to fix.
     */
    @Test
    void vatIdWithoutPrefixIsExplained() throws Exception {
        Map<String, String> c = company();
        c.put("account1", "123456789");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        String problems = ZugpferdFile.validate(invoice).toString();
        assertTrue(problems.contains(mpv5.globals.Messages.ZUGFERD_INVALID_VATID.toString()), problems);
        assertTrue(problems.contains("'123456789'"), problems);

        File xml = tmp.resolve("prefix.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        EInvoiceValidator.Result result = EInvoiceValidator.validate(xml);
        assertFalse(result.isValid());
        assertTrue(result.getProblems().toString().contains(mpv5.globals.Messages.ZUGFERD_HINT_VATID_PREFIX.toString()),
                result.getProblems().toString());
        assertTrue(result.getProblems().toString().contains("BR-CO-09"), result.getProblems().toString());
    }

    @Test
    void buyerTaxNumberIsLeftOutAndReported() throws Exception {
        Contact contact = new Contact();
        contact.setCNumber("K-1002");
        contact.setCompany("Handwerk GmbH");
        contact.setMailaddress("info@handwerk.example");
        Address a = new Address();
        a.setCompany("Handwerk GmbH");
        a.setStreet("Dorfstr. 1");
        a.setZip("12345");
        a.setCity("Dorf");
        a.setCountry("276");
        a.setTaxnumber("12/345/67890");
        TradeParty buyer = contact.asZugpferdTradeparty(a);
        assertTrue(ZugpferdFile.isEmpty(buyer.getVATID()));
        assertTrue(ZugpferdFile.isEmpty(buyer.getTaxID()), "a tax number is not allowed for the buyer");
        String problem = ZugpferdFile.checkBuyerVatId(contact, a);
        assertNotNull(problem);
        assertTrue(problem.contains("12/345/67890") && problem.contains("Handwerk GmbH"), problem);
        assertNull(ZugpferdFile.checkBuyerVatId(contact, new Address()), "empty is fine");

        Map<String, String> c = company();
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer, "ref", 0, null, ZugpferdFile.Format.ZUGFERD);
        File plain = tmp.resolve("plain2.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(plain);
        }
        File pdf = tmp.resolve("taxnumber.pdf").toFile();
        ZugpferdFile.writePdf(invoice, plain, pdf);
        assertValid(pdf);
    }

    @Test
    void explainsKnownRules() {
        EInvoiceValidator.Result result = new EInvoiceValidator.Result(false, java.util.Arrays.asList(
                "[BR-DE-5] Das Element \"Seller contact point\" (BT-41) muss übermittelt werden.",
                "[BR-DE-7] Das Element \"Seller contact email address\" (BT-43) muss übermittelt werden.",
                "Value of '@schemeID' is not allowed. [ID FX-SCH-A-000570]",
                "PDF/A: The font programs for all fonts used shall be embedded",
                "[XY-1] something unknown"), java.util.Collections.<String>emptyList(), "");
        List<String> problems = result.getProblems();
        assertEquals(4, problems.size(), problems.toString());
        assertEquals(mpv5.globals.Messages.ZUGFERD_HINT_SELLER_CONTACT + " [BR-DE-5, BR-DE-7]", problems.get(0));
        assertTrue(problems.get(1).startsWith(mpv5.globals.Messages.ZUGFERD_HINT_BUYER_TAXNUMBER.toString()));
        assertTrue(problems.get(2).startsWith(mpv5.globals.Messages.ZUGFERD_HINT_FONTS.toString()));
        assertEquals("[XY-1] something unknown", problems.get(3));
    }

    @Test
    void reportsMissingData() {
        Map<String, String> c = new HashMap<String, String>();
        c.put("business", "Nur Name");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(),
                new SubItem[]{line("x", "x", "1", "", "1", "19", "0")},
                ZugpferdFile.createSender(c::get), new Contact().asZugpferdTradeparty(new Address()), null, 0, null);
        List<String> problems = ZugpferdFile.validate(invoice);
        assertTrue(problems.size() >= 8, String.valueOf(problems));
    }

    @Test
    void rejectsInvoiceWithoutItems() {
        Map<String, String> c = company();
        assertThrows(IllegalStateException.class, () -> ZugpferdFile.createInvoice(invoice(),
                new SubItem[]{line("   ", "", "0", "", "0", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), null, 0, null));
    }

    @Test
    void embedsXmlIntoPdf() throws Exception {
        File plain = tmp.resolve("plain.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(plain);
        }
        File pdf = tmp.resolve("R-2026-0042.pdf").toFile();
        ZugpferdFile.writePdf(build(), plain, pdf);

        ZUGFeRDImporter importer = new ZUGFeRDImporter(pdf.getAbsolutePath());
        String xml = new String(importer.getRawXML(), StandardCharsets.UTF_8);
        assertTrue(xml.contains("urn:cen.eu:en16931:2017"), xml);
        assertTrue(xml.contains("357.00"));

        //the blank test page has no fonts, so the whole file must be valid PDF/A-3 + EN16931
        assertValid(pdf);
    }

    /**
     * The template PDF must embed all fonts (PDF/A-3), even those which are not
     * installed (the bundled template uses Arial)
     */
    @Test
    void templatePdfIsPdfA3() throws Exception {
        File plain = tmp.resolve("template.pdf").toFile();
        OdfTextDocument doc = OdfTextDocument.loadDocument(new File("ext/templates/Yabs-1.2-Template-DE.odt"));
        PdfOptions options = PdfOptions.create();
        options.fontProvider(EmbeddingFontRegistry.getRegistry());
        try (OutputStream out = new FileOutputStream(plain)) {
            PdfConverter.getInstance().convert(doc, out, options);
        }
        File pdf = tmp.resolve("R-2026-0042-template.pdf").toFile();
        ZugpferdFile.writePdf(build(), plain, pdf);
        assertValid(pdf);
    }

    @Test
    void validatorReportsPdfAErrors() throws Exception {
        String report = "<validation><pdf>ValidationResult [flavour=3u, assertions=[TestAssertion [ruleId=RuleId "
                + "[specification=ISO 19005-3:2012, clause=6.2.11.4.1, testNumber=1], status=failed, message=The font "
                + "programs for all fonts used shall be embedded, location=Location [level=CosDocument]]]<summary "
                + "status=\"invalid\"/></pdf><xml><messages></messages><summary status=\"valid\"/></xml>"
                + "<summary status=\"valid\"/></validation>";
        EInvoiceValidator.Result result = EInvoiceValidator.parse(report);
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).startsWith("PDF/A: The font programs"));
    }

    /**
     * Runs the official validator (schema, EN16931 + XRechnung schematron,
     * PDF/A-3)
     */
    private static void assertValid(File file) throws Exception {
        EInvoiceValidator.Result result = EInvoiceValidator.validate(file);
        Files.write(new File(file.getPath() + ".report.xml").toPath(), result.getReport().getBytes(StandardCharsets.UTF_8));
        assertTrue(result.isValid(), file.getName() + ": " + result.getErrors());
    }
}
