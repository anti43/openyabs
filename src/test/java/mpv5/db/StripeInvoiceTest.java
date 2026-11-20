package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import mpv5.db.objects.Expense;
import mpv5.handler.EInvoiceImport;
import mpv5.handler.ZugpferdExpenseImport;
import mpv5.utils.export.EInvoiceValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The e-invoice examples downloaded from Stripe
 * (https://stripe.com/de/resources/more/e-invoice-example-germany, see
 * src/test/resources/stripe/README.md), issue #227: an XRechnung (CII) and a
 * ZUGFeRD PDF of the same invoice. YaBS reads and imports them; they are not
 * valid XRechnung (no IBAN for the SEPA transfer, no electronic addresses),
 * which the validator reports.
 */
class StripeInvoiceTest {

    static final String XML = "stripe-xrechnung-cii.xml";
    static final String PDF = "stripe-zugferd-xrechnung.pdf";

    @TempDir
    Path tmp;

    private File resource(String name) throws Exception {
        File f = tmp.resolve(name).toFile();
        try (InputStream in = getClass().getResourceAsStream("/stripe/" + name)) {
            assertNotNull(in, "test resource /stripe/" + name);
            Files.copy(in, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return f;
    }

    private static void money(String expected, BigDecimal actual, String what) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), what + ": " + actual);
    }

    @Test
    void bothExamplesAreRead() throws Exception {
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        for (String name : new String[]{XML, PDF}) {
            ZugpferdExpenseImport.ParsedInvoice p = ZugpferdExpenseImport.parse(resource(name));
            assertNotNull(p, name);
            assertEquals("001", p.invoiceNumber, name);
            assertEquals("Unternehmen 1", p.sellerName, name);
            assertEquals("DE123456789", p.sellerTaxId, name);
            assertTrue(p.sellerIds.contains("DE123456789"), name);
            assertEquals("Unternehmen B", p.buyerName, name);
            assertTrue(p.buyerIds.contains("DE987654321"), name);
            money("1000.00", p.netAmount, name + " net");
            money("190.00", p.taxAmount, name + " VAT");
            money("1190.00", p.grossAmount, name + " gross");
            money("19", p.taxRate, name + " rate");
            assertEquals("2025-07-01", d.format(p.issueDate), name);
            assertEquals("2025-08-01", d.format(p.dueDate), name);
            assertEquals(Arrays.asList("Lieferung von Produkt X"), p.lineDescriptions, name);
            assertEquals("", p.sellerIban, name + ": the example has no IBAN");
        }
    }

    @Test
    void transcribedXmlIsTheXmlOfThePdf() throws Exception {
        String embedded;
        try (InputStream in = Files.newInputStream(resource(PDF).toPath())) {
            org.mustangproject.ZUGFeRD.ZUGFeRDImporter importer = new org.mustangproject.ZUGFeRD.ZUGFeRDImporter(in);
            embedded = new String(importer.getRawXML(), StandardCharsets.UTF_8);
        }
        String transcribed = new String(Files.readAllBytes(resource(XML).toPath()), StandardCharsets.UTF_8);
        assertEquals(normalize(embedded), normalize(transcribed));
    }

    /**
     * @return the XML without the declaration and the whitespace between tags
     */
    private static String normalize(String xml) {
        return xml.replaceFirst("^\\uFEFF?<\\?xml[^>]*\\?>", "").replaceAll(">\\s+<", "><").replaceAll("\\s+", " ").trim();
    }

    @Test
    void notValidXRechnung() throws Exception {
        for (String name : new String[]{XML, PDF}) {
            EInvoiceValidator.Result r = EInvoiceValidator.validate(resource(name));
            assertFalse(r.isValid(), name);
            String errors = String.join("\n", r.getErrors());
            //SEPA credit transfer without an account (IBAN, BT-84)
            assertTrue(errors.contains("BR-61"), name + ": " + errors);
            assertTrue(errors.contains("BR-50"), name + ": " + errors);
            //no electronic address of seller and buyer (BT-34, BT-49)
            assertTrue(errors.contains("PEPPOL-EN16931-R020"), name + ": " + errors);
            assertTrue(errors.contains("PEPPOL-EN16931-R010"), name + ": " + errors);
        }
    }

    @Test
    void importedAsExpenseOnce() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp.resolve("db"));
        //neither party is the company: an expense (the default)
        EInvoiceImport.Result xml = EInvoiceImport.importFile(resource(XML), null);
        assertEquals(EInvoiceImport.Outcome.IMPORTED, xml.outcome, xml.message());
        assertEquals(EInvoiceImport.Kind.EXPENSE, xml.kind);
        Expense e = (Expense) xml.record;
        assertEquals("001", e.__getCnumber());
        money("1000.00", e.__getNetvalue(), "expense net");
        money("1190.00", e.__getBrutvalue(), "expense gross");
        money("19", e.__getTaxpercentvalue(), "expense rate");

        //the ZUGFeRD PDF is the same invoice: not booked twice
        EInvoiceImport.Result pdf = EInvoiceImport.importFile(resource(PDF), null);
        assertEquals(EInvoiceImport.Outcome.DUPLICATE_NUMBER, pdf.outcome, pdf.message());
        //the XML again: the same file
        List<EInvoiceImport.Result> again = EInvoiceImport.importFiles(Arrays.asList(resource(XML)), null);
        assertEquals(EInvoiceImport.Outcome.DUPLICATE_FILE, again.get(0).outcome);
    }
}
