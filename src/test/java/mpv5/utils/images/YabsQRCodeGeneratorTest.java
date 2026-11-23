package mpv5.utils.images;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.odt.ODTConstants;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.db.objects.Item;
import mpv5.utils.text.Iban;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

class YabsQRCodeGeneratorTest {

    @Test
    void imageProviderPreservesTemplateFrameSize() throws Exception {
        String payload = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02500105170137075030", "", new BigDecimal("345.90"), "Rechnung 000005");
        YabsQRCodeGenerator gen = new YabsQRCodeGenerator(payload);
        gen.generate();

        IImageProvider provider = gen.getImageProvider();
        assertFalse(provider.isUseImageSize(), "Must keep ODT template frame size instead of raw pixel dimensions");
        assertNull(provider.getWidth(85.0f), "Must not override template svg:width");
        assertNull(provider.getHeight(92.0f), "Must not override template svg:height");
    }

    @Test
    void invoiceWithQrCodeFitsOnFirstPageAndDecodesAsValidEpcPayment() throws Exception {
        String payload = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02 5001 0517 0137 0750 30", "INGDDEFFXXX", new BigDecimal("345.90"), "Rechnung 000005");
        YabsQRCodeGenerator gen = new YabsQRCodeGenerator(payload);
        gen.generate();

        byte[] odtBytes = renderTemplate("/extra/templates/invoice.odt", gen.getImageProvider());
        String contentXml = extractContentXml(odtBytes);
        assertTrue(contentXml.contains("svg:width=\"2.4cm\""), "QR frame must keep 2.4cm width from template");
        assertTrue(contentXml.contains("svg:height=\"2.596cm\""), "QR frame must keep 2.596cm height from template");

        byte[] pdfBytes = toPdf(odtBytes);
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            assertEquals(1, pdf.getNumberOfPages(), "5-item invoice with QR code must fit on 1 page");

            // Render page 1 at 150 DPI (lower than print/camera) and scan the QR code out of the rendered PDF image
            BufferedImage pageImage = new PDFRenderer(pdf).renderImageWithDPI(0, 150);
            Result decoded = decodeQr(pageImage);
            assertEquals("M", decoded.getResultMetadata().get(ResultMetadataType.ERROR_CORRECTION_LEVEL),
                    "EPC069-12 requires QR Error Correction Level M");
            assertValidEpc06912(decoded.getText(),
                    "Musterfirma GmbH", "DE02500105170137075030", "INGDDEFFXXX", "EUR345.90", "Rechnung 000005");
        }
    }

    @Test
    void demoInvoiceAndSmallBusinessTemplatesKeepQrOnFirstPage() throws Exception {
        String payload = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02500105170137075030", "", new BigDecimal("345.90"), "Rechnung 000005");
        YabsQRCodeGenerator gen = new YabsQRCodeGenerator(payload);
        gen.generate();

        for (String resource : new String[]{"/extra/templates/demo-00.odt", "/extra/templates/demo-00-ku.odt"}) {
            byte[] odtBytes = renderTemplate(resource, gen.getImageProvider());
            byte[] pdfBytes = toPdf(odtBytes);
            try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
                // Page 1 must contain the invoice AND the decodable QR code (reference fields start on page 2)
                BufferedImage page1 = new PDFRenderer(pdf).renderImageWithDPI(0, 150);
                Result decoded = decodeQr(page1);
                assertEquals("M", decoded.getResultMetadata().get(ResultMetadataType.ERROR_CORRECTION_LEVEL));
                assertValidEpc06912(decoded.getText(),
                        "Musterfirma GmbH", "DE02500105170137075030", "", "EUR345.90", "Rechnung 000005");
            }
        }
    }

    @Test
    void payloadNormalizesIbanBicAndOmitsTrailingEmptyLines() {
        String withSpaces = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "de02 5001-0517 0137 0750 30", "ingd deff xxx", new BigDecimal("12.5"), "RE-001");
        assertValidEpc06912(withSpaces, "Musterfirma GmbH", "DE02500105170137075030", "INGDDEFFXXX", "EUR12.50", "RE-001");

        String noRef = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02500105170137075030", "", new BigDecimal("99.00"), "");
        assertFalse(noRef.endsWith("\n"), "EPC069-12 forbids trailing empty lines after last populated element");
        String[] lines = noRef.split("\n", -1);
        assertEquals(8, lines.length);
        assertEquals("EUR99.00", lines[7]);
    }

    private static void assertValidEpc06912(String text, String expectedName, String expectedIban,
            String expectedBic, String expectedAmount, String expectedRef) {
        assertTrue(text.getBytes(StandardCharsets.UTF_8).length <= 331, "EPC069-12 payload must not exceed 331 bytes");
        String[] lines = text.split("\n", -1);
        assertTrue(lines.length >= 11 && lines.length <= 12, "EPC069-12 expects 11-12 lines, got " + lines.length);
        assertEquals("BCD", lines[0], "Line 1: Service Tag must be BCD");
        assertEquals("002", lines[1], "Line 2: Version must be 002");
        assertEquals("1", lines[2], "Line 3: Character set 1 (UTF-8)");
        assertEquals("SCT", lines[3], "Line 4: Identification must be SCT");
        assertEquals(expectedBic, lines[4], "Line 5: BIC");
        assertTrue(expectedBic.isEmpty() || Iban.isValidBic(lines[4]), "Line 5: BIC must be valid when present");
        assertEquals(expectedName, lines[5], "Line 6: Beneficiary name");
        assertFalse(lines[5].isEmpty(), "Line 6: Beneficiary name is mandatory");
        assertTrue(lines[5].length() <= 70, "Line 6: max 70 chars");
        assertEquals(expectedIban, lines[6], "Line 7: Beneficiary IBAN (no spaces)");
        assertTrue(Iban.isValid(lines[6]), "Line 7: IBAN checksum must be valid");
        assertEquals(expectedAmount, lines[7], "Line 8: Amount in EUR");
        assertTrue(lines[7].isEmpty() || lines[7].matches("EUR\\d+\\.\\d{2}"), "Line 8: EUR format");
        assertEquals("", lines[8], "Line 9: Purpose");
        assertEquals("", lines[9], "Line 10: Structured remittance");
        assertEquals(expectedRef, lines[10], "Line 11: Unstructured remittance");
        assertTrue(lines[10].length() <= 140, "Line 11: max 140 chars");
    }

    private static Result decodeQr(BufferedImage image) throws Exception {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(w, h, pixels)));
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        return new QRCodeReader().decode(bitmap, hints);
    }

    private static byte[] renderTemplate(String resource, IImageProvider qrProvider) throws Exception {
        ByteArrayOutputStream filledOdt = new ByteArrayOutputStream();
        try (InputStream in = YabsQRCodeGeneratorTest.class.getResourceAsStream(resource)) {
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
            report.removePreprocessor(ODTConstants.CONTENT_XML_ENTRY);
            report.addPreprocessor(ODTConstants.CONTENT_XML_ENTRY, YabsODTPreprocessor.INSTANCE);
            report.removePreprocessor(ODTConstants.STYLES_XML_ENTRY);
            report.addPreprocessor(ODTConstants.STYLES_XML_ENTRY, YabsODTPreprocessor.INSTANCE);

            FieldsMetadata meta = report.createFieldsMetadata();
            for (int i = 0; i < 6; i++) {
                meta.addFieldAsList("xtable1.C" + i);
            }
            for (String f : Item.VATGROUP_FIELDS) {
                meta.addFieldAsList("vatgroups." + f);
            }
            meta.addFieldAsImage("QRCode.img", NullImageBehaviour.RemoveImageTemplate);
            report.setFieldsMetadata(meta);

            IContext ctx = report.createContext();
            List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
            for (int r = 1; r <= 5; r++) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("C0", String.valueOf(r));
                row.put("C1", "1,00");
                row.put("C2", "Order Nr. RE 2024 - 00149" + r + " created on 03.12.2024");
                row.put("C3", "69,00 €");
                row.put("C4", "19,00%");
                row.put("C5", "69,00 €");
                rows.add(row);
            }
            ctx.put("xtable1", rows);
            List<Map<String, String>> vats = new ArrayList<Map<String, String>>();
            Map<String, String> v = new HashMap<String, String>();
            v.put("name", "19 % MwSt.");
            v.put("netf", "290,67 €");
            v.put("vatf", "55,23 €");
            v.put("grossf", "345,90 €");
            vats.add(v);
            ctx.put("vatgroups", vats);
            ctx.put("QRCode.img", qrProvider);
            ctx.put("item.cnumber", "000005");
            ctx.put("item.dateadded", "14.11.2026");
            ctx.put("item.servicedate", "14.11.2026");
            ctx.put("item.netvaluef", "290,67 €");
            ctx.put("item.taxvaluef", "55,23 €");
            ctx.put("item.grosvaluef", "345,90 €");
            ctx.put("item.discountvaluef", "0,00 €");
            ctx.put("item.discountnetdifferencef", "");
            ctx.put("item.taxnotes", "");
            ctx.put("item.contact.cnumber", "KD000415");
            ctx.put("item.contact.eori", "DE987654321098765");
            ctx.put("item.contact.title", "DARC OV-L23");
            ctx.put("item.contact.fullname", "Michael Punsmann");
            ctx.put("item.contact.invoiceaddress.street", "Kardinal-von-Galen Str. 6");
            ctx.put("item.contact.invoiceaddress.zip", "46514");
            ctx.put("item.contact.invoiceaddress.city", "Schermbeck");
            ctx.put("item.contact.invoiceaddress.country", "DE");
            ctx.put("item.contact.zahlungsziel", "Zahlbar bis 28.11.2026");
            ctx.put("item.contact.spezial", "");
            ctx.put("companyinfo.business", "Musterfirma GmbH");
            ctx.put("companyinfo.name", "Musterfirma");
            ctx.put("companyinfo.street", "Musterstrasse 1");
            ctx.put("companyinfo.zip", "66663");
            ctx.put("companyinfo.city", "Musterheim");
            ctx.put("companyinfo.phone", "45435345435");
            ctx.put("companyinfo.fax", "");
            ctx.put("companyinfo.email", "aaa@fff.dee");
            ctx.put("companyinfo.taxnumber", "");
            ctx.put("companyinfo.vatid", "DE435345435");
            ctx.put("companyinfo.eori", "DE123456789012345");
            ctx.put("companyinfo.bankname", "");
            ctx.put("companyinfo.banknumber", "");
            ctx.put("companyinfo.account", "DE02500105170137075030");
            ctx.put("companyinfo.smallbusinessnote", "Gemäß § 19 UStG wird keine Umsatzsteuer berechnet.");

            report.process(ctx, filledOdt);
        }
        return filledOdt.toByteArray();
    }

    private static byte[] toPdf(byte[] odtBytes) throws Exception {
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        OdfTextDocument doc = OdfTextDocument.loadDocument(new ByteArrayInputStream(odtBytes));
        PdfConverter.getInstance().convert(doc, pdfOut, PdfOptions.create());
        return pdfOut.toByteArray();
    }

    private static String extractContentXml(byte[] odt) throws Exception {
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(odt))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                if ("content.xml".equals(e.getName())) {
                    return new String(z.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError("content.xml not found");
    }
}
