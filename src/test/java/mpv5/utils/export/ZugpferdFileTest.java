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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
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
        validateIfPossible(xml);
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
        validateIfPossible(pdf);
    }

    /**
     * Runs the official Mustang validator (schema + schematron) if it is on
     * the test classpath
     */
    private static void validateIfPossible(File file) throws Exception {
        Class<?> c;
        try {
            c = Class.forName("org.mustangproject.validator.ZUGFeRDValidator");
        } catch (ClassNotFoundException e) {
            return;
        }
        Object validator = c.getConstructor().newInstance();
        String report = (String) c.getMethod("validate", String.class).invoke(validator, file.getAbsolutePath());
        Files.write(new File(file.getPath() + ".report.xml").toPath(), report.getBytes(StandardCharsets.UTF_8));
        System.out.println(file.getName() + ": " + report);
        if (file.getName().endsWith(".xml")) {
            assertTrue(report.contains("status=\"valid\""), report);
        } else {
            //the PDF is a plain PDF/A-less test document, only check the XML part
            assertFalse(report.matches("(?s).*<xml[^>]*>.*?<summary status=\"invalid\".*?</xml>.*"), report);
        }
    }
}
