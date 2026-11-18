/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.utils.export.ZugpferdFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mustangproject.Invoice;
import org.mustangproject.TradeParty;

/**
 * Tests for {@link ZugpferdExpenseImport}: parsing ZUGFeRD PDFs, CII/UBL XRechnung XMLs,
 * skipping plain PDFs/non-invoice XMLs, creating {@link Expense} objects, and folder
 * import with SHA-256 deduplication.
 */
class ZugpferdExpenseImportTest {

    @TempDir
    Path tmp;

    private static Invoice sampleInvoice(String number, String grossPrice, String taxRate) {
        Map<String, String> c = new HashMap<String, String>();
        c.put("business", "Muster Software GmbH");
        c.put("firstname", "Erika");
        c.put("name", "Mustermann");
        c.put("street", "Hauptstrasse 1");
        c.put("zip", "10115");
        c.put("city", "Berlin");
        c.put("country", "Deutschland");
        c.put("phone", "+49 30 123456");
        c.put("email", "rechnung@muster.example");
        c.put("account1", "DE123456789");
        c.put("account", "DE02 1203 0000 0000 2020 51");
        c.put("banknumber", "BYLADEM1001");

        Item item = new Item();
        item.setCnumber(number);
        item.setInttype(Item.TYPE_INVOICE);
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.NOVEMBER, 4, 0, 0, 0);
        item.setDateadded(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 14);
        item.setDatetodo(cal.getTime());

        SubItem s1 = new SubItem();
        s1.setCname("Softwarelizenz");
        s1.setDescription("Softwarelizenz Jahresabo");
        s1.setQuantityvalue(new BigDecimal("2"));
        s1.setMeasure("Stk");
        s1.setExternalvalue(new BigDecimal(grossPrice));
        s1.setTaxpercentvalue(new BigDecimal(taxRate));
        s1.setDiscount(BigDecimal.ZERO);

        Contact buyerContact = new Contact();
        buyerContact.setCNumber("K-1001");
        buyerContact.setMailaddress("einkauf@kunde.example");
        Address a = new Address();
        a.setCompany("Kunde AG");
        a.setStreet("Marienplatz 2");
        a.setZip("80331");
        a.setCity("Muenchen");
        a.setCountry("276");
        a.setTaxnumber("DE987654321");
        TradeParty buyer = buyerContact.asZugpferdTradeparty(a);

        return ZugpferdFile.createInvoice(item, new SubItem[]{s1},
                ZugpferdFile.createSender(c::get), buyer, "04011000-12345-34",
                ZugpferdFile.PAYTERM_UNSET, null);
    }

    private static File createPlainPdf(Path target) throws Exception {
        File pdf = target.toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(pdf);
        }
        return pdf;
    }

    @Test
    void parsesZugferdPdfAndCreatesExpense() throws Exception {
        File plainPdf = createPlainPdf(tmp.resolve("plain.pdf"));
        File zugferdPdf = tmp.resolve("ER-2026-0042.pdf").toFile();
        Invoice inv = sampleInvoice("ER-2026-0042", "100.00", "19");
        ZugpferdFile.writePdf(inv, plainPdf, zugferdPdf);

        ZugpferdExpenseImport.ParsedInvoice parsed = ZugpferdExpenseImport.parse(zugferdPdf);
        assertNotNull(parsed);
        assertEquals("ER-2026-0042", parsed.invoiceNumber);
        assertEquals("Muster Software GmbH", parsed.sellerName);
        assertEquals("DE123456789", parsed.sellerTaxId);
        assertEquals("DE02120300000000202051", parsed.sellerIban);
        assertEquals(0, new BigDecimal("200.00").compareTo(parsed.netAmount));
        assertEquals(0, new BigDecimal("238.00").compareTo(parsed.grossAmount));
        assertEquals(0, new BigDecimal("38.00").compareTo(parsed.taxAmount));
        assertEquals(0, new BigDecimal("19").compareTo(parsed.taxRate));
        assertEquals("2026-11-04", new SimpleDateFormat("yyyy-MM-dd").format(parsed.issueDate));
        assertEquals("2026-11-18", new SimpleDateFormat("yyyy-MM-dd").format(parsed.dueDate));
        assertTrue(parsed.lineDescriptions.stream().anyMatch(d -> d.contains("Softwarelizenz")));

        Expense expense = parsed.toExpense(5, 1);
        assertEquals("ER-2026-0042", expense.__getCnumber());
        assertEquals("ER-2026-0042", expense.__getCname());
        assertEquals(0, new BigDecimal("200.00").compareTo(expense.__getNetvalue()));
        assertEquals(0, new BigDecimal("238.00").compareTo(expense.__getBrutvalue()));
        assertEquals(0, new BigDecimal("19").compareTo(expense.__getTaxpercentvalue()));
        assertEquals(5, expense.__getAccountsids());
        assertFalse(expense.__getIspaid());
        assertTrue(expense.__getDescription().contains("Muster Software GmbH"));
        assertTrue(expense.__getDescription().contains("ER-2026-0042"));
    }

    @Test
    void parsesXRechnungCiiXml() throws Exception {
        File xml = tmp.resolve("xrechnung.xml").toFile();
        Invoice inv = sampleInvoice("XR-2026-100", "50.00", "7");
        ZugpferdFile.writeXml(inv, xml);

        ZugpferdExpenseImport.ParsedInvoice parsed = ZugpferdExpenseImport.parse(xml);
        assertNotNull(parsed);
        assertEquals("XR-2026-100", parsed.invoiceNumber);
        assertEquals("Muster Software GmbH", parsed.sellerName);
        assertEquals(0, new BigDecimal("100.00").compareTo(parsed.netAmount));
        assertEquals(0, new BigDecimal("107.00").compareTo(parsed.grossAmount));
        assertEquals(0, new BigDecimal("7.00").compareTo(parsed.taxAmount));
        assertEquals(0, new BigDecimal("7").compareTo(parsed.taxRate));
    }

    @Test
    void parsesUblXRechnungXml() throws Exception {
        String ubl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n"
                + "         xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n"
                + "         xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">\n"
                + "  <cbc:ID>UBL-2026-007</cbc:ID>\n"
                + "  <cbc:IssueDate>2026-05-10</cbc:IssueDate>\n"
                + "  <cbc:DueDate>2026-05-24</cbc:DueDate>\n"
                + "  <cbc:DocumentCurrencyCode>EUR</cbc:DocumentCurrencyCode>\n"
                + "  <cac:AccountingSupplierParty>\n"
                + "    <cac:Party>\n"
                + "      <cac:PartyLegalEntity>\n"
                + "        <cbc:RegistrationName>Cloud Hosting AG</cbc:RegistrationName>\n"
                + "      </cac:PartyLegalEntity>\n"
                + "    </cac:Party>\n"
                + "  </cac:AccountingSupplierParty>\n"
                + "  <cac:TaxTotal>\n"
                + "    <cbc:TaxAmount currencyID=\"EUR\">19.00</cbc:TaxAmount>\n"
                + "    <cac:TaxSubtotal>\n"
                + "      <cbc:TaxableAmount currencyID=\"EUR\">100.00</cbc:TaxableAmount>\n"
                + "      <cbc:TaxAmount currencyID=\"EUR\">19.00</cbc:TaxAmount>\n"
                + "      <cac:TaxCategory>\n"
                + "        <cbc:Percent>19.00</cbc:Percent>\n"
                + "      </cac:TaxCategory>\n"
                + "    </cac:TaxSubtotal>\n"
                + "  </cac:TaxTotal>\n"
                + "  <cac:LegalMonetaryTotal>\n"
                + "    <cbc:TaxExclusiveAmount currencyID=\"EUR\">100.00</cbc:TaxExclusiveAmount>\n"
                + "    <cbc:TaxInclusiveAmount currencyID=\"EUR\">119.00</cbc:TaxInclusiveAmount>\n"
                + "    <cbc:PayableAmount currencyID=\"EUR\">119.00</cbc:PayableAmount>\n"
                + "  </cac:LegalMonetaryTotal>\n"
                + "  <cac:InvoiceLine>\n"
                + "    <cbc:ID>1</cbc:ID>\n"
                + "    <cac:Item>\n"
                + "      <cbc:Name>Dedicated Server Mai 2026</cbc:Name>\n"
                + "    </cac:Item>\n"
                + "  </cac:InvoiceLine>\n"
                + "</Invoice>";
        File ublFile = tmp.resolve("ubl_invoice.xml").toFile();
        Files.write(ublFile.toPath(), ubl.getBytes(StandardCharsets.UTF_8));

        ZugpferdExpenseImport.ParsedInvoice parsed = ZugpferdExpenseImport.parse(ublFile);
        assertNotNull(parsed);
        assertEquals("UBL-2026-007", parsed.invoiceNumber);
        assertEquals("Cloud Hosting AG", parsed.sellerName);
        assertEquals("2026-05-10", new SimpleDateFormat("yyyy-MM-dd").format(parsed.issueDate));
        assertEquals("2026-05-24", new SimpleDateFormat("yyyy-MM-dd").format(parsed.dueDate));
        assertEquals(0, new BigDecimal("100.00").compareTo(parsed.netAmount));
        assertEquals(0, new BigDecimal("119.00").compareTo(parsed.grossAmount));
        assertEquals(0, new BigDecimal("19").compareTo(parsed.taxRate));
        assertTrue(parsed.lineDescriptions.contains("Dedicated Server Mai 2026"));
    }

    @Test
    void skipsPlainPdfAndNonInvoiceXml() throws Exception {
        File plainPdf = createPlainPdf(tmp.resolve("normal_document.pdf"));
        assertNull(ZugpferdExpenseImport.parse(plainPdf));

        File otherXml = tmp.resolve("config.xml").toFile();
        Files.write(otherXml.toPath(), "<configuration><setting>true</setting></configuration>".getBytes(StandardCharsets.UTF_8));
        assertNull(ZugpferdExpenseImport.parse(otherXml));
    }

    @Test
    void importsFolderAndDeduplicatesRepeatedRuns() throws Exception {
        Path inbox = tmp.resolve("inbox");
        Files.createDirectories(inbox);

        // 1. Plain PDF (should be skipped)
        File plainPdf = createPlainPdf(inbox.resolve("scan_without_xml.pdf"));

        // 2. ZUGFeRD PDF
        File zugferdPdf = inbox.resolve("invoice_1.pdf").toFile();
        ZugpferdFile.writePdf(sampleInvoice("RE-101", "80.00", "19"), plainPdf, zugferdPdf);

        // 3. XRechnung XML
        File xrechnungXml = inbox.resolve("invoice_2.xml").toFile();
        ZugpferdFile.writeXml(sampleInvoice("RE-102", "40.00", "7"), xrechnungXml);

        final Set<String> importedHashes = new HashSet<String>();
        ZugpferdExpenseImport.ImportSink testSink = new ZugpferdExpenseImport.ImportSink() {
            @Override
            public boolean isAlreadyImported(String sha256) {
                return importedHashes.contains(sha256);
            }

            @Override
            public Expense save(ZugpferdExpenseImport.ParsedInvoice invoice, File sourceFile, String sha256) {
                importedHashes.add(sha256);
                return invoice.toExpense(1, 1);
            }
        };

        // First scan imports both valid e-invoices and skips the plain PDF
        ZugpferdExpenseImport.Result first = ZugpferdExpenseImport.importFromFolder(inbox.toFile(), testSink);
        assertEquals(2, first.imported);
        assertEquals(0, first.alreadyImported);
        assertEquals(1, first.skippedNotEInvoice);
        assertEquals(2, first.expenses.size());
        assertEquals("RE-101", first.expenses.get(0).__getCnumber());
        assertEquals("RE-102", first.expenses.get(1).__getCnumber());

        // Second scan of the same folder must skip the already-imported invoices
        ZugpferdExpenseImport.Result second = ZugpferdExpenseImport.importFromFolder(inbox.toFile(), testSink);
        assertEquals(0, second.imported);
        assertEquals(2, second.alreadyImported);
        assertEquals(1, second.skippedNotEInvoice);
        assertTrue(second.expenses.isEmpty());
    }

    @Test
    void configuresFolderAndAutoImportSettings() {
        String prevFolder = ZugpferdExpenseImport.folderPath();
        boolean prevAuto = ZugpferdExpenseImport.isAutoImportEnabled();
        try {
            ZugpferdExpenseImport.setFolderPath("[HOME]/zugferd-inbox");
            assertEquals("[HOME]/zugferd-inbox", ZugpferdExpenseImport.folderPath());
            File resolved = ZugpferdExpenseImport.folder();
            assertNotNull(resolved);
            assertTrue(resolved.getPath().contains(System.getProperty("user.home")));

            ZugpferdExpenseImport.setAutoImportEnabled(false);
            assertFalse(ZugpferdExpenseImport.isAutoImportEnabled());

            ZugpferdExpenseImport.setAutoImportEnabled(true);
            assertTrue(ZugpferdExpenseImport.isAutoImportEnabled());
        } finally {
            ZugpferdExpenseImport.setFolderPath(prevFolder);
            ZugpferdExpenseImport.setAutoImportEnabled(prevAuto);
        }
    }
}
