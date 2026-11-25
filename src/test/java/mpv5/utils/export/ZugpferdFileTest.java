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
import mpv5.globals.Messages;
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
        //the service date (Leistungsdatum): the work was done in October
        cal.set(2026, Calendar.OCTOBER, 21, 0, 0, 0);
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
        return ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), buyer(), "04011000-12345-34", ZugpferdFile.PAYTERM_UNSET, null);
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
        //"datetodo" is the service date - it must not be used as due date
        Item item = invoice();
        item.setDatetodo(item.__getDateadded());
        SubItem[] lines = {line("a", "", "1", "Stk", "100", "19", "0")};
        Map<String, String> c = company();
        Invoice withTerm = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", 30, null);
        assertEquals(DateConverter.addDays(item.__getDateadded(), 30), withTerm.getDueDate());

        item.setDatetodo(null);
        Invoice noTerm = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        assertEquals(DateConverter.addDays(item.__getDateadded(), ZugpferdFile.DEFAULT_PAYTERM_DAYS), noTerm.getDueDate());

        File xml = tmp.resolve("noterm.xml").toFile();
        ZugpferdFile.writeXml(noTerm, xml);
        assertValid(xml);
    }

    @Test
    void serviceDateIsTheDeliveryDate() throws Exception {
        //BT-72 (ActualDeliverySupplyChainEvent): the service date, not the invoice date (#208)
        Invoice invoice = build();
        File xml = tmp.resolve("servicedate.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(s.matches("(?s).*<ram:ActualDeliverySupplyChainEvent>\\s*<ram:OccurrenceDateTime>\\s*<udt:DateTimeString format=\"102\">20261021<.*"), s);
        assertValid(xml);

        //no service date: the invoice date
        Item item = invoice();
        item.setDatetodo(null);
        Invoice noDate = ZugpferdFile.createInvoice(item, new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(company()::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        File xml2 = tmp.resolve("noservicedate.xml").toFile();
        ZugpferdFile.writeXml(noDate, xml2);
        String s2 = new String(Files.readAllBytes(xml2.toPath()), StandardCharsets.UTF_8);
        assertTrue(s2.matches("(?s).*<ram:ActualDeliverySupplyChainEvent>\\s*<ram:OccurrenceDateTime>\\s*<udt:DateTimeString format=\"102\">20261104<.*"), s2);
    }

    @Test
    void sellerNeedsVatIdOrTaxNumber() throws Exception {
        Map<String, String> c = company();
        c.remove("account1");
        c.remove("taxnumber");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
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
    void taxNumberWithoutVatIdIsEnough() throws Exception {
        //e.g. a small business (Kleinunternehmer) without USt-IdNr. (#210)
        Map<String, String> c = company();
        c.remove("account1");
        TradeParty sender = ZugpferdFile.createSender(c::get);
        assertTrue(ZugpferdFile.isEmpty(sender.getVATID()), sender.getVATID());
        assertEquals("30/123/45678", sender.getTaxID());
        assertEquals("30/123/45678", sender.getID(), "seller identifier BT-29 (BR-CO-26)");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                sender, buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        assertTrue(ZugpferdFile.validate(invoice).isEmpty(), ZugpferdFile.validate(invoice).toString());
        File xml = tmp.resolve("taxnumberonly.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(s.contains("schemeID=\"FC\">30/123/45678"), s);
        assertValid(xml);
    }

    @Test
    void smallBusinessInvoice() throws Exception {
        Map<String, String> c = company();
        c.remove("account1");
        c.put(SmallBusiness.KEY_ENABLED, "true");
        String note = SmallBusiness.note(c::get);
        assertTrue(note.contains("\u00a7 19 UStG"), note);
        SubItem[] lines = {line("Beratung", "", "2", "Std.", "50", "0", "0")};
        for (ZugpferdFile.Format format : ZugpferdFile.Format.values()) {
            Invoice invoice = ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), buyer(), "ref",
                    ZugpferdFile.PAYTERM_UNSET, null, note, format);
            assertTrue(ZugpferdFile.validate(invoice, true).isEmpty(), ZugpferdFile.validate(invoice, true).toString());
            File xml = tmp.resolve("ku-" + format + ".xml").toFile();
            ZugpferdFile.writeXml(invoice, xml);
            String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
            assertTrue(s.contains("<ram:CategoryCode>E</ram:CategoryCode>"), s);
            assertTrue(s.contains("<ram:ExemptionReason>" + note + "</ram:ExemptionReason>"), s);
            assertTrue(s.contains("<ram:Content>" + note + "</ram:Content>"), "invoice note BT-22: " + s);
            assertValid(xml);
        }

        //a position with VAT is not allowed
        Invoice withVat = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null, note,
                ZugpferdFile.Format.XRECHNUNG);
        assertTrue(ZugpferdFile.validate(withVat, true).toString().contains(Messages.SMALLBUSINESS_LINE_HAS_VAT.toString()));
        assertTrue(ZugpferdFile.validate(withVat, false).isEmpty());
    }

    /**
     * A business customer in France: the VAT category, reason code and note
     * come from the tax rate of the position (#209)
     */
    static TradeParty frenchBuyer() {
        Contact c = new Contact();
        c.setCNumber("K-2001");
        c.setMailaddress("achats@client.example");
        c.setMainphone("+33 1 23456789");
        Address a = new Address();
        a.setCompany("Client SARL");
        a.setPrename("Jean");
        a.setCname("Client");
        a.setStreet("1 Rue de Rivoli");
        a.setZip("75001");
        a.setCity("Paris");
        a.setCountry("250");
        a.setTaxnumber("FR40303265045");
        return c.asZugpferdTradeparty(a);
    }

    @Test
    void vatCategoryOfTheTaxRate() throws Exception {
        Map<String, String> c = company();
        mpv5.db.objects.Tax regular = new mpv5.db.objects.Tax();
        regular.setTaxvalue(new BigDecimal("19"));
        for (String category : new String[]{"AE", "K", "G"}) {
            mpv5.db.objects.Tax zero = new mpv5.db.objects.Tax();
            zero.setTaxvalue(BigDecimal.ZERO);
            zero.setCategorycode(category);
            VatCategory vc = zero.category();
            SubItem[] lines = {line("Beratung", "", "2", "Std.", "100", "0", "0")};
            Invoice invoice = ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), frenchBuyer(), "ref",
                    ZugpferdFile.PAYTERM_UNSET, null, null, ZugpferdFile.Format.XRECHNUNG,
                    s -> mpv5.db.objects.Tax.forRate(s.__getTaxpercentvalue(), java.util.Arrays.asList(regular, zero)));
            File xml = tmp.resolve("cat-" + category + ".xml").toFile();
            ZugpferdFile.writeXml(invoice, xml);
            String x = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
            assertTrue(x.contains("<ram:CategoryCode>" + category + "</ram:CategoryCode>"), x);
            assertTrue(x.contains("<ram:ExemptionReasonCode>" + vc.defaultReasonCode() + "</ram:ExemptionReasonCode>"), x);
            assertTrue(x.contains("<ram:ExemptionReason>" + vc.defaultNote() + "</ram:ExemptionReason>"), x);
            //the wording required on the invoice (§ 14a UStG) as invoice note (BT-22)
            assertTrue(x.contains("<ram:Content>" + vc.defaultNote() + "</ram:Content>"), x);
            assertFalse(x.contains("<ram:CategoryCode>E</ram:CategoryCode>"), x);
            if (!"K".equals(category)) {
                //intra-community supplies also need the deliver-to address (BR-IC-12)
                assertValid(xml);
            }
        }
        //without tax rates as before: 0 % is exempt (E), with the default reason of the language
        Invoice plain = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("Beratung", "", "1", "Std.", "100", "0", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        File xml = tmp.resolve("cat-E.xml").toFile();
        ZugpferdFile.writeXml(plain, xml);
        String x = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(x.contains("<ram:CategoryCode>E</ram:CategoryCode>"), x);
        assertTrue(x.contains("<ram:ExemptionReason>" + VatCategory.E.defaultNote() + "</ram:ExemptionReason>"), x);
        assertValid(xml);
    }

    /**
     * A credit note / cancellation invoice: type 381, positive amounts, the
     * corrected invoice as preceding invoice reference (BT-25, BT-26), #211
     */
    @Test
    void creditNoteWithPrecedingInvoice() throws Exception {
        Map<String, String> c = company();
        Item credit = invoice();
        credit.setCnumber("GS-2026-0007");
        credit.setInttype(Item.TYPE_CREDIT);
        assertTrue(ZugpferdFile.supports(credit));
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.OCTOBER, 30, 0, 0, 0);
        for (ZugpferdFile.Format format : ZugpferdFile.Format.values()) {
            Invoice invoice = ZugpferdFile.createInvoice(credit, new SubItem[]{line("Beratung", "", "2", "Std.", "100", "19", "0")},
                    ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null, format);
            ZugpferdFile.applyPrecedingInvoice(invoice, "R-2026-0042", cal.getTime());
            assertEquals("381", invoice.getDocumentCode());
            File xml = tmp.resolve("credit-" + format + ".xml").toFile();
            ZugpferdFile.writeXml(invoice, xml);
            String x = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
            assertTrue(x.contains("<ram:TypeCode>381</ram:TypeCode>"), x);
            assertTrue(x.contains("<ram:IssuerAssignedID>R-2026-0042</ram:IssuerAssignedID>"), x);
            assertTrue(x.contains("20261030"), "date of the corrected invoice: " + x);
            assertTrue(x.contains("<ram:GrandTotalAmount>238.00</ram:GrandTotalAmount>"), "positive amounts: " + x);
            assertValid(xml);
        }
        //no number: no reference
        Invoice none = ZugpferdFile.createInvoice(credit, new SubItem[]{line("Beratung", "", "1", "Std.", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        ZugpferdFile.applyPrecedingInvoice(none, " ", null);
        assertTrue(none.getInvoiceReferencedDocuments() == null || none.getInvoiceReferencedDocuments().isEmpty());
    }

    /**
     * A final invoice: the deducted deposit is a negative quantity at a
     * positive price (BR-27: the net price must not be negative) and a
     * preceding invoice (BG-3), #221
     */
    @Test
    void finalInvoiceWithDeductedDeposit() throws Exception {
        Map<String, String> c = company();
        SubItem[] lines = {
            line("Montage", "", "100", "Std.", "100", "19", "0"),
            line("Material", "", "1", "Stk", "1000", "7", "0"),
            line("Abzug", "Abzüglich Anzahlungsrechnung Nr. AZ-2026-00001 vom 01.10.2026", "1", "", "-3000", "19", "0")
        };
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.OCTOBER, 1, 0, 0, 0);
        for (ZugpferdFile.Format format : ZugpferdFile.Format.values()) {
            Invoice invoice = ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), buyer(), "ref",
                    ZugpferdFile.PAYTERM_UNSET, null, format);
            ZugpferdFile.addPrecedingInvoice(invoice, "AZ-2026-00001", cal.getTime());
            File xml = tmp.resolve("final-" + format + ".xml").toFile();
            ZugpferdFile.writeXml(invoice, xml);
            String x = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
            assertFalse(x.contains("<ram:ChargeAmount>-"), "no negative net price (BR-27): " + x);
            assertTrue(java.util.regex.Pattern.compile(">-1(\\.0+)?</ram:BilledQuantity>").matcher(x).find(), "negative quantity: " + x);
            //(10.000 - 3.000) x 1,19 + 1.000 x 1,07 = 8.330 + 1.070
            assertTrue(x.contains("<ram:GrandTotalAmount>9400.00</ram:GrandTotalAmount>"), x);
            assertTrue(x.contains("<ram:IssuerAssignedID>AZ-2026-00001</ram:IssuerAssignedID>"), x);
            assertValid(xml);
        }
    }

    /**
     * A negative line with a discount: the line net is the one of the printed
     * invoice (InvoiceMath), not a 2-decimal unit price times the quantity
     */
    @Test
    void negativeLineWithDiscountMatchesThePrintedTotal() throws Exception {
        SubItem[] lines = {
            line("Ware", "", "10", "Stk", "20", "19", "0"),
            line("Abzug", "", "7", "Stk", "-1.13", "19", "5")
        };
        //7 x 1,13 = 7,91 - 5 % (0,40) = 7,51 (a unit price of 1,07 would give 7,49)
        assertEquals(0, new BigDecimal("-7.51").compareTo(mpv5.utils.numberformat.InvoiceMath.lineNet(
                new BigDecimal("7"), new BigDecimal("-1.13"), new BigDecimal("5"))));
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(company()::get), buyer(), "ref",
                ZugpferdFile.PAYTERM_UNSET, null, ZugpferdFile.Format.values()[0]);
        File xml = tmp.resolve("negdiscount.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String x = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        //200 - 7,51 = 192,49; + 19 % (36,57) = 229,06
        assertTrue(x.contains("<ram:TaxBasisTotalAmount>192.49</ram:TaxBasisTotalAmount>"), x);
        assertTrue(x.contains("<ram:GrandTotalAmount>229.06</ram:GrandTotalAmount>"), x);
        assertValid(xml);
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
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        String problems = ZugpferdFile.validate(invoice).toString();
        assertTrue(problems.contains(mpv5.globals.Messages.ZUGFERD_INVALID_IBAN.toString()), problems);
    }

    @Test
    void reportsUnknownCountry() {
        Map<String, String> c = company();
        c.put("country", "Atlantis");
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
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
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null, ZugpferdFile.Format.XRECHNUNG);
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
        assertFalse(ZugpferdFile.isValidVatId("345435"), "reported case");
        assertTrue(ZugpferdFile.isValidVatId("CHE-123.456.789 MWST"), "Swiss UID");
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
                ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
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
                ZugpferdFile.createSender(c::get), buyer, "ref", ZugpferdFile.PAYTERM_UNSET, null, ZugpferdFile.Format.ZUGFERD);
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
    void vatIdInTaxNumberFieldIsFound() throws Exception {
        //VAT ID entered in "Tax number", VAT ID field empty
        Map<String, String> c = company();
        c.remove("account1");
        c.put("taxnumber", "DE 123 456 789");
        TradeParty sender = ZugpferdFile.createSender(c::get);
        assertEquals("DE123456789", sender.getVATID());
        assertTrue(ZugpferdFile.isEmpty(sender.getTaxID()), sender.getTaxID());

        //fields swapped
        c.put("account1", "30/123/45678");
        sender = ZugpferdFile.createSender(c::get);
        assertEquals("DE123456789", sender.getVATID());
        assertEquals("30/123/45678", sender.getTaxID());

        Invoice invoice = ZugpferdFile.createInvoice(invoice(), new SubItem[]{line("a", "", "1", "Stk", "100", "19", "0")},
                sender, buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        assertTrue(ZugpferdFile.validate(invoice).isEmpty(), ZugpferdFile.validate(invoice).toString());
        File xml = tmp.resolve("swapped.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        assertValid(xml);
    }

    @Test
    void paytermOrderContactTemplateDefault() {
        //the users write the payment term into templates per customer group
        int unset = ZugpferdFile.PAYTERM_UNSET;
        assertEquals(10, ZugpferdFile.paytermDays(10, 30, 21), "contact first");
        assertEquals(30, ZugpferdFile.paytermDays(unset, 30, 21), "then the template");
        assertEquals(21, ZugpferdFile.paytermDays(unset, unset, 21), "then the company default");
        assertEquals(ZugpferdFile.DEFAULT_PAYTERM_DAYS, ZugpferdFile.paytermDays(unset, unset, unset));
        assertEquals(0, ZugpferdFile.paytermDays(0, 30, 21), "contact payable immediately");
        assertEquals(0, ZugpferdFile.paytermDays(unset, 0, 21), "template payable immediately");
        assertEquals(0, ZugpferdFile.paytermDays(unset, unset, 0), "company default payable immediately");
        assertEquals(unset, ZugpferdFile.parseTerm("abc"));
        assertEquals(unset, ZugpferdFile.parseTerm(" "));
        assertEquals(unset, ZugpferdFile.parseTerm("-3"));
        assertEquals(0, ZugpferdFile.parseTerm("0"));
        assertEquals(30, ZugpferdFile.parseTerm(" 30 "));
        assertEquals(unset, ZugpferdFile.paytermOf(null));
    }

    @Test
    void payableImmediatelyIsDueOnTheInvoiceDate() throws Exception {
        Item item = invoice();
        SubItem[] lines = {line("a", "", "1", "Stk", "100", "19", "0")};
        Map<String, String> c = company();
        Invoice invoice = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", 0, null);
        assertEquals(item.__getDateadded(), invoice.getDueDate());
        assertEquals(Messages.ZUGFERD_PAYABLE_IMMEDIATELY.toString(), invoice.getPaymentTermDescription());
        File xml = tmp.resolve("immediate.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        assertValid(xml);

        Invoice term = ZugpferdFile.createInvoice(item, lines, ZugpferdFile.createSender(c::get), buyer(), "ref", 30, null);
        assertTrue(term.getPaymentTermDescription().startsWith(Messages.ZUGFERD_PAYABLE_UNTIL.toString()),
                term.getPaymentTermDescription());
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

    /**
     * Merging two PDF/A-compatible template PDFs (e.g. invoice + order
     * confirmation) must preserve embedded fonts so the resulting ZUGFeRD
     * e-invoice passes veraPDF PDF/A-3 validation.
     */
    @Test
    void mergedTemplatePdfIsPdfA3() throws Exception {
        PdfOptions options = PdfOptions.create();
        options.fontProvider(EmbeddingFontRegistry.getRegistry());
        File part1 = tmp.resolve("part1.pdf").toFile();
        File part2 = tmp.resolve("part2.pdf").toFile();
        for (File part : new File[]{part1, part2}) {
            OdfTextDocument doc = OdfTextDocument.loadDocument(new File("ext/templates/Yabs-1.2-Template-DE.odt"));
            try (OutputStream out = new FileOutputStream(part)) {
                PdfConverter.getInstance().convert(doc, out, options);
            }
        }
        int expectedPages;
        try (PDDocument d1 = org.apache.pdfbox.Loader.loadPDF(part1);
                PDDocument d2 = org.apache.pdfbox.Loader.loadPDF(part2)) {
            expectedPages = d1.getNumberOfPages() + d2.getNumberOfPages();
        }
        File merged = Export.mergeFiles(java.util.Arrays.asList(part1, part2));
        assertNotNull(merged, "merged PDF should be created");
        try {
            File pdf = tmp.resolve("R-2026-0042-merged.pdf").toFile();
            ZugpferdFile.writePdf(build(), merged, pdf);
            try (PDDocument resultDoc = org.apache.pdfbox.Loader.loadPDF(pdf)) {
                assertEquals(expectedPages, resultDoc.getNumberOfPages(), "merged page count");
            }
            ZUGFeRDImporter importer = new ZUGFeRDImporter(pdf.getAbsolutePath());
            String xml = new String(importer.getRawXML(), StandardCharsets.UTF_8);
            assertTrue(xml.contains("357.00"), xml);
            assertValid(pdf);
        } finally {
            merged.delete();
        }
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

    @Test
    void groupsTaxRatesWithHiddenDecimals() throws Exception {
        //BR-S-08: rates stored as e.g. 19.0000000001 (shown as 19.00) gave one
        //VAT breakdown per line instead of one for all 19 % lines
        SubItem[] lines = {
            line("a", "", "1", "Stk", "314.90", "19", "0"),
            line("b", "", "1", "Stk", "251.80", "19.0000000001", "0"),
            line("c", "", "1", "Stk", "151.80", "18.9999999999", "0"),
            line("d", "", "1", "Stk", "10", "7.000000001", "0")
        };
        Map<String, String> c = company();
        Invoice invoice = ZugpferdFile.createInvoice(invoice(), lines, ZugpferdFile.createSender(c::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        File xml = tmp.resolve("rates.xml").toFile();
        ZugpferdFile.writeXml(invoice, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        String trade = s.substring(s.indexOf("<ram:ApplicableHeaderTradeSettlement"));
        assertEquals(2, trade.split("<ram:ApplicableTradeTax>", -1).length - 1, trade);
        assertTrue(trade.contains("<ram:BasisAmount>718.50</ram:BasisAmount>"), trade);
        assertValid(xml);
    }

    @Test
    void peppolIdParsingAndValidation() {
        assertTrue(ZugpferdFile.isValidPeppolId("0204:04011000-12345-34"));
        assertTrue(ZugpferdFile.isValidPeppolId("9930:DE123456789"));
        assertTrue(ZugpferdFile.isValidPeppolId("0088:4012345000018"));
        assertTrue(ZugpferdFile.isValidPeppolId("0208:0123456789"));
        assertTrue(ZugpferdFile.isValidPeppolId("em:billing@example.org"));
        assertTrue(ZugpferdFile.isValidPeppolId("iso6523-actorid-upis::0204:04011000-12345-34"));
        assertEquals("0204:04011000-12345-34", ZugpferdFile.normalizePeppolId(" iso6523-actorid-upis::0204:04011000-12345-34 "));
        assertEquals("EM:billing@example.org", ZugpferdFile.normalizePeppolId("em:billing@example.org"));

        assertFalse(ZugpferdFile.isValidPeppolId(null));
        assertFalse(ZugpferdFile.isValidPeppolId(""));
        assertFalse(ZugpferdFile.isValidPeppolId("04011000-12345-34"), "missing scheme");
        assertFalse(ZugpferdFile.isValidPeppolId("9999:DE123456789"), "unknown EAS scheme");
        assertFalse(ZugpferdFile.isValidPeppolId("9930:"), "missing id");
        assertFalse(ZugpferdFile.isValidPeppolId(":DE123456789"), "missing scheme");
        assertFalse(ZugpferdFile.isValidPeppolId("9930:DE 123456789"), "whitespace in id");
    }

    @Test
    void peppolIdAssignedToSellerAndBuyerInXRechnungAndZugferd() throws Exception {
        Map<String, String> c = company();
        c.put(ZugpferdFile.KEY_COMPANY_PEPPOL_ID, "9930:DE123456789");
        TradeParty sender = ZugpferdFile.createSender(c::get);
        assertEquals("9930", sender.getUriUniversalCommunicationIDScheme());
        assertEquals("DE123456789", sender.getUriUniversalCommunicationID());
        assertEquals("rechnung@muster.example", sender.getContact().getEMail(), "seller contact email (BT-43) stays");

        Contact buyerContact = new Contact();
        buyerContact.setCNumber("K-1001");
        buyerContact.setMailaddress("einkauf@kunde.example");
        buyerContact.setMainphone("+49 89 987654");
        Address a = new Address();
        a.setCompany("Kunde AG");
        a.setPrename("Max");
        a.setCname("Kunde");
        a.setStreet("Marienplatz 2");
        a.setZip("80331");
        a.setCity("München");
        a.setCountry("276");
        a.setTaxnumber("DE987654321");
        TradeParty recipient = buyerContact.asZugpferdTradeparty(a, "0204:04011000-12345-34");
        assertEquals("0204", recipient.getUriUniversalCommunicationIDScheme());
        assertEquals("04011000-12345-34", recipient.getUriUniversalCommunicationID());
        assertEquals("einkauf@kunde.example", recipient.getContact().getEMail(), "buyer contact email (BT-58) stays");

        SubItem[] lines = {line("Beratung", "", "2", "Std.", "100", "19", "0")};
        Invoice xrechnung = ZugpferdFile.createInvoice(invoice(), lines, sender, recipient,
                "04011000-12345-34", ZugpferdFile.PAYTERM_UNSET, null, ZugpferdFile.Format.XRECHNUNG);
        assertTrue(ZugpferdFile.validate(xrechnung).isEmpty(), ZugpferdFile.validate(xrechnung).toString());

        File xml = tmp.resolve("peppol-xr.xml").toFile();
        ZugpferdFile.writeXml(xrechnung, xml);
        String xmlText = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        String sellerXml = xmlText.substring(xmlText.indexOf("<ram:SellerTradeParty>"), xmlText.indexOf("</ram:SellerTradeParty>"));
        String buyerXml = xmlText.substring(xmlText.indexOf("<ram:BuyerTradeParty>"), xmlText.indexOf("</ram:BuyerTradeParty>"));
        assertTrue(sellerXml.contains("<ram:URIID schemeID=\"9930\">DE123456789</ram:URIID>"), sellerXml);
        assertTrue(buyerXml.contains("<ram:URIID schemeID=\"0204\">04011000-12345-34</ram:URIID>"), buyerXml);
        assertTrue(xmlText.contains("<ram:BuyerReference>04011000-12345-34</ram:BuyerReference>"), xmlText);
        assertValid(xml);

        //ZUGFeRD PDF with the same Peppol-IDs
        Invoice zugferd = ZugpferdFile.createInvoice(invoice(), lines, sender, recipient,
                "04011000-12345-34", ZugpferdFile.PAYTERM_UNSET, null, ZugpferdFile.Format.ZUGFERD);
        File plain = tmp.resolve("peppol-plain.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(plain);
        }
        File pdf = tmp.resolve("peppol-zf.pdf").toFile();
        ZugpferdFile.writePdf(zugferd, plain, pdf);
        String embedded = new String(new ZUGFeRDImporter(pdf.getAbsolutePath()).getRawXML(), StandardCharsets.UTF_8);
        assertTrue(embedded.contains("<ram:URIID schemeID=\"9930\">DE123456789</ram:URIID>"), embedded);
        assertTrue(embedded.contains("<ram:URIID schemeID=\"0204\">04011000-12345-34</ram:URIID>"), embedded);
        assertValid(pdf);
    }

    @Test
    void buyerWithPeppolIdNeedsNoEmail() throws Exception {
        Contact buyerContact = new Contact();
        buyerContact.setCNumber("K-2002");
        Address a = new Address();
        a.setCompany("Belgian Partner NV");
        a.setStreet("Wetstraat 16");
        a.setZip("1000");
        a.setCity("Brussels");
        a.setCountry("BE");
        a.setTaxnumber("BE0123456789");
        TradeParty recipient = buyerContact.asZugpferdTradeparty(a, "0208:0123456789");

        Invoice inv = ZugpferdFile.createInvoice(invoice(),
                new SubItem[]{line("Beratung", "", "1", "Std.", "100", "19", "0")},
                ZugpferdFile.createSender(company()::get), recipient, "REF-BE-1", ZugpferdFile.PAYTERM_UNSET, null);
        assertTrue(ZugpferdFile.validate(inv).isEmpty(), ZugpferdFile.validate(inv).toString());

        File xml = tmp.resolve("peppol-noemail.xml").toFile();
        ZugpferdFile.writeXml(inv, xml);
        String s = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(s.contains("<ram:URIID schemeID=\"0208\">0123456789</ram:URIID>"), s);
        assertValid(xml);
    }

    @Test
    void invalidPeppolIdIsRejectedByValidation() {
        Map<String, String> c = company();
        c.put(ZugpferdFile.KEY_COMPANY_PEPPOL_ID, "DE123456789");
        TradeParty badSeller = ZugpferdFile.createSender(c::get);
        List<String> sellerErrs = ZugpferdFile.sellerProblems(badSeller);
        assertTrue(sellerErrs.toString().contains(Messages.ZUGFERD_INVALID_PEPPOL_ID.toString()), sellerErrs.toString());

        TradeParty badBuyer = buyer();
        ZugpferdFile.applyPeppolId(badBuyer, "9999:unknown");
        Invoice inv = ZugpferdFile.createInvoice(invoice(),
                new SubItem[]{line("a", "", "1", "Stk", "10", "19", "0")},
                ZugpferdFile.createSender(company()::get), badBuyer, "ref", ZugpferdFile.PAYTERM_UNSET, null);
        List<String> buyerErrs = ZugpferdFile.validate(inv);
        assertTrue(buyerErrs.toString().contains(Messages.ZUGFERD_INVALID_PEPPOL_ID.toString()), buyerErrs.toString());
        assertTrue(buyerErrs.toString().contains("9999:unknown"), buyerErrs.toString());
    }

    @Test
    void eoriValidationAndCustomsNotesInXRechnungAndZugferd() throws Exception {
        assertTrue(ZugpferdFile.isValidEori("DE123456789012345"));
        assertTrue(ZugpferdFile.isValidEori("DE1234567"));
        assertTrue(ZugpferdFile.isValidEori("fr 12345678901234"));
        assertTrue(ZugpferdFile.isValidEori("NL820646660B01"));
        assertTrue(ZugpferdFile.isValidEori("GB123456789000"));
        assertTrue(ZugpferdFile.isValidEori("XI123456789000"));
        assertEquals("DE123456789012345", ZugpferdFile.normalizeEori("de 1234-5678.9012345"));

        assertFalse(ZugpferdFile.isValidEori(null));
        assertFalse(ZugpferdFile.isValidEori(""));
        assertFalse(ZugpferdFile.isValidEori("123456789"), "missing country prefix");
        assertFalse(ZugpferdFile.isValidEori("DE"), "missing identifier");
        assertFalse(ZugpferdFile.isValidEori("DE1234567890123456"), "exceeds 15 chars after country code");
        assertFalse(ZugpferdFile.isValidEori("XX123456789"), "invalid country code");

        Invoice xr = build();
        ZugpferdFile.applyEoriNotes(xr, "DE 123456789012345", "GB123456789000");
        File xml = tmp.resolve("eori-xr.xml").toFile();
        ZugpferdFile.writeXml(xr, xml);
        String xmlText = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlText.contains("<ram:Content>Seller EORI: DE123456789012345</ram:Content>"), xmlText);
        assertTrue(xmlText.contains("<ram:Content>Buyer EORI: GB123456789000</ram:Content>"), xmlText);
        assertTrue(xmlText.contains("<ram:SubjectCode>CUS</ram:SubjectCode>"), xmlText);
        assertValid(xml);

        Invoice zf = ZugpferdFile.createInvoice(invoice(),
                new SubItem[]{line("Exportware", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(company()::get), buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null,
                ZugpferdFile.Format.ZUGFERD);
        ZugpferdFile.applyEoriNotes(zf, "DE123456789012345", "GB123456789000");
        File plain = tmp.resolve("eori-plain.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(plain);
        }
        File pdf = tmp.resolve("eori-zf.pdf").toFile();
        ZugpferdFile.writePdf(zf, plain, pdf);
        String embedded = new String(new ZUGFeRDImporter(pdf.getAbsolutePath()).getRawXML(), StandardCharsets.UTF_8);
        assertTrue(embedded.contains("<ram:Content>Seller EORI: DE123456789012345</ram:Content>"), embedded);
        assertTrue(embedded.contains("<ram:Content>Buyer EORI: GB123456789000</ram:Content>"), embedded);
        assertTrue(embedded.contains("<ram:SubjectCode>CUS</ram:SubjectCode>"), embedded);
        assertValid(pdf);
    }

    @Test
    void currencyCodeAndDeliveryAddressAndCustomsProductFieldsInXRechnung() throws Exception {
        assertEquals(CurrencyCode.CHF, CurrencyCode.parse("CHF"));
        assertEquals(CurrencyCode.CHF, CurrencyCode.parse("chf (CHF) – Swiss Franc"));
        assertEquals(CurrencyCode.EUR, CurrencyCode.parse("€"));
        assertEquals("CHF", CurrencyCode.normalizeCode("chf"));
        assertEquals("", CurrencyCode.normalizeCode("invalid"));
        assertTrue(CurrencyCode.isValidCode("CHF"));
        assertTrue(CurrencyCode.isValidCode("USD"));
        assertFalse(CurrencyCode.isValidCode("XX1"));

        Contact chBuyer = new Contact();
        chBuyer.setCname("Alpen AG");
        chBuyer.setCompany("Alpen AG");
        chBuyer.setStreet("Bahnhofstrasse 10");
        chBuyer.setZip("8001");
        chBuyer.setCity("Zürich");
        chBuyer.setCountry("CH");
        chBuyer.setBankcurrency("CHF");
        assertEquals("CHF", ZugpferdFile.findCurrency(new Item(), chBuyer));
        assertEquals("EUR", ZugpferdFile.findCurrency(new Item(), new Contact()));

        Invoice inv = build();
        inv.setCurrency("CHF");
        inv.setDeliveryAddress(new TradeParty("Alpen Lager AG", "Industriestrasse 5", "4053", "Basel", "CH"));
        inv.addCustomsNote("Incoterms: DAP Basel");
        inv.addCustomsNote("Net weight: 12.5 kg, Gross weight: 14.0 kg, Packages: 2 boxes");
        org.mustangproject.Product p = (org.mustangproject.Product) inv.getZFItems()[0].getProduct();
        p.addClassification(new org.mustangproject.DesignatedProductClassification(
                new org.mustangproject.ClassCode("HS", "84713000")));
        p.setCountryOfOrigin("DE");

        File xml = tmp.resolve("customs-chf.xml").toFile();
        ZugpferdFile.writeXml(inv, xml);
        String xmlText = new String(Files.readAllBytes(xml.toPath()), StandardCharsets.UTF_8);
        assertTrue(xmlText.contains("<ram:InvoiceCurrencyCode>CHF</ram:InvoiceCurrencyCode>"), xmlText);
        assertTrue(xmlText.contains("<ram:ShipToTradeParty>"), xmlText);
        assertTrue(xmlText.contains("<ram:Name>Alpen Lager AG</ram:Name>"), xmlText);
        assertTrue(xmlText.contains("<ram:ClassCode listID=\"HS\">84713000</ram:ClassCode>"), xmlText);
        assertTrue(xmlText.replaceAll("\\s+", "").contains("<ram:OriginTradeCountry><ram:ID>DE</ram:ID></ram:OriginTradeCountry>"), xmlText);
        assertTrue(xmlText.contains("<ram:Content>Incoterms: DAP Basel</ram:Content>"), xmlText);
        assertValid(xml);
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
