package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import mpv5.db.objects.SubItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mustangproject.Invoice;
import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;

class InvoiceMailTest {

    @TempDir
    Path tmp;
    final AtomicInteger rendered = new AtomicInteger();

    /**
     * Mail for the given delivery, the "template PDF" is a blank page
     */
    InvoiceMail mail(Invoice invoice, EInvoiceDelivery delivery) {
        final File pdf = tmp.resolve("R-2026-0042.pdf").toFile();
        return new InvoiceMail(() -> {
            rendered.incrementAndGet();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
                doc.addPage(page);
                doc.save(pdf);
            } catch (Exception e) {
                return e;
            }
            return null;
        }, () -> pdf, invoice, delivery);
    }

    @Test
    void pdfOnly() {
        InvoiceMail m = mail(null, EInvoiceDelivery.PDF);
        assertNull(m.waitFor());
        assertEquals(1, m.getAttachments().size());
        assertEquals("R-2026-0042.pdf", m.getAttachments().get(0).getName());
    }

    @Test
    void zugferdIsOnePdfWithEmbeddedXml() throws Exception {
        InvoiceMail m = mail(ZugpferdFileTest.build(), EInvoiceDelivery.ZUGFERD);
        assertNull(m.waitFor());
        List<File> files = m.getAttachments();
        assertEquals(1, files.size());
        assertEquals("R-2026-0042.pdf", files.get(0).getName(), "named like the invoice");
        String xml = new String(new ZUGFeRDImporter(files.get(0).getAbsolutePath()).getRawXML(), StandardCharsets.UTF_8);
        assertTrue(xml.contains("R-2026-0042"));
    }

    @Test
    void xrechnungOnlyNeedsNoPdf() throws Exception {
        InvoiceMail m = mail(ZugpferdFileTest.build(), EInvoiceDelivery.XRECHNUNG);
        assertNull(m.waitFor());
        assertEquals(0, rendered.get(), "the PDF is not rendered");
        assertEquals(1, m.getAttachments().size());
        assertEquals("R-2026-0042.xml", m.getAttachments().get(0).getName());
        assertTrue(new String(Files.readAllBytes(m.getAttachments().get(0).toPath()), StandardCharsets.UTF_8)
                .contains("urn:xeinkauf.de:kosit:xrechnung"));
    }

    @Test
    void pdfAndXrechnungAreTwoAttachments() {
        InvoiceMail m = mail(ZugpferdFileTest.build(), EInvoiceDelivery.PDF_AND_XRECHNUNG);
        assertNull(m.waitFor());
        List<File> files = m.getAttachments();
        assertEquals(2, files.size());
        assertEquals("R-2026-0042.pdf", files.get(0).getName());
        assertEquals("R-2026-0042.xml", files.get(1).getName());
    }

    @Test
    void invalidEInvoiceIsNotAttached() {
        Map<String, String> c = ZugpferdFileTest.company();
        c.remove("account1");//no VAT ID -> BR-CO-26
        Invoice invoice = ZugpferdFile.createInvoice(ZugpferdFileTest.invoice(),
                new SubItem[]{ZugpferdFileTest.line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), ZugpferdFileTest.buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
        InvoiceMail m = mail(invoice, EInvoiceDelivery.XRECHNUNG);
        Exception e = m.waitFor();
        assertNotNull(e, "must not be sent");
        assertTrue(m.getAttachments().isEmpty());
        assertTrue(e.getMessage().contains(mpv5.globals.Messages.ZUGFERD_HINT_SELLER_VATID.toString()), e.getMessage());
    }

    @Test
    void parsesStoredDelivery() {
        assertEquals(EInvoiceDelivery.ZUGFERD, EInvoiceDelivery.parse("zugferd"));
        assertEquals(EInvoiceDelivery.PDF_AND_XRECHNUNG, EInvoiceDelivery.parse("PDF_AND_XRECHNUNG"));
        assertEquals(EInvoiceDelivery.PDF, EInvoiceDelivery.parse(null));
        assertEquals(EInvoiceDelivery.PDF, EInvoiceDelivery.parse("something"));
        assertFalse(EInvoiceDelivery.PDF.isEInvoice());
        assertEquals(ZugpferdFile.Format.ZUGFERD, EInvoiceDelivery.ZUGFERD.getFormat());
    }
}
