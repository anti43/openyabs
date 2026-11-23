package mpv5.utils.images;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.oned.Code39Reader;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.db.objects.Item;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

class YabsBarcodeGeneratorTest {

    @Test
    void normalizeCode39HandlesValidAndInvalidInputs() {
        assertEquals("RE-2026-00001", YabsBarcodeGenerator.normalizeCode39("  re-2026-00001 \t"));
        assertEquals("RE-01", YabsBarcodeGenerator.normalizeCode39("*re-01*"));
        assertEquals("A B", YabsBarcodeGenerator.normalizeCode39("a\r\n\tb"));
        String allValid = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";
        assertEquals(allValid, YabsBarcodeGenerator.normalizeCode39(allValid));

        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39(null));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39(""));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39("   "));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39("**"));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39("RE_001"));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39("RE*001"));
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39("ÄÖÜ"));
        StringBuilder tooLong = new StringBuilder();
        for (int i = 0; i < 81; i++) {
            tooLong.append('A');
        }
        assertThrows(IllegalArgumentException.class, () -> YabsBarcodeGenerator.normalizeCode39(tooLong.toString()));
    }

    @Test
    void code39ImageAndProviderPreserveTemplateFrameSizeAndDecode() throws Exception {
        for (String value : new String[]{"000005", "RE-2026-00001", "KD-2026/0042"}) {
            BufferedImage img = YabsBarcodeGenerator.code39Image(value);
            Result decoded = decodeCode39(img);
            assertEquals(BarcodeFormat.CODE_39, decoded.getBarcodeFormat());
            assertEquals(value, decoded.getText());
        }

        IImageProvider provider = YabsBarcodeGenerator.code39("RE-2026-00001");
        assertFalse(provider.isUseImageSize(), "Must keep ODT template frame size instead of raw pixel dimensions");
        assertNull(provider.getWidth(141.0f), "Must not override template svg:width");
        assertNull(provider.getHeight(28.0f), "Must not override template svg:height");
    }

    @Test
    void documentNumberResolvesPrimaryCnumberWithoutFallingBackToNestedEntities() {
        Map<String, Object> invoice = new LinkedHashMap<String, Object>();
        invoice.put("item.contact.cnumber", "KD-000415");
        invoice.put("item.reference.cnumber", "AN-2026-00002");
        invoice.put("item.cnumber", "RE-2026-00001");
        assertEquals("RE-2026-00001", ODTFile2.documentNumber(invoice));

        // Blank primary cnumber must NOT fall back to nested contact.cnumber
        Map<String, Object> blankInvoice = new LinkedHashMap<String, Object>();
        blankInvoice.put("item.cnumber", "   ");
        blankInvoice.put("item.contact.cnumber", "KD-000415");
        assertNull(ODTFile2.documentNumber(blankInvoice));

        Map<String, Object> reminder = new LinkedHashMap<String, Object>();
        reminder.put("reminder.item.contact.cnumber", "KD-000415");
        reminder.put("reminder.item.cnumber", "RE-2026-00007");
        assertEquals("RE-2026-00007", ODTFile2.documentNumber(reminder));

        Map<String, Object> product = new LinkedHashMap<String, Object>();
        product.put("product.cnumber", "ART-4711");
        assertEquals("ART-4711", ODTFile2.documentNumber(product));

        Map<String, Object> contact = new LinkedHashMap<String, Object>();
        contact.put("contact.cnumber", "KD-0042");
        assertEquals("KD-0042", ODTFile2.documentNumber(contact));

        Map<String, Object> journal = new LinkedHashMap<String, Object>();
        journal.put("journal.datefrom", "01.01.2026");
        assertNull(ODTFile2.documentNumber(journal));
    }

    @Test
    void demoInvoiceTemplatesRenderScannableBarcodeAndQrCodeOnFirstPage() throws Exception {
        String cnumber = "RE-2026-00001";
        String qrPayload = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02500105170137075030", "", new BigDecimal("345.90"), "Rechnung " + cnumber);
        YabsQRCodeGenerator qrGen = new YabsQRCodeGenerator(qrPayload);
        qrGen.generate();

        for (String resource : new String[]{"/extra/templates/demo-00.odt", "/extra/templates/demo-00-ku.odt"}) {
            Map<String, Object> fields = sampleInvoiceFields(cnumber);
            byte[] odtBytes = renderTemplate(resource, fields, YabsBarcodeGenerator.code39(cnumber), qrGen.getImageProvider());
            String contentXml = extractEntry(odtBytes, "content.xml");
            assertTrue(contentXml.contains("svg:width=\"5.0cm\""), resource + " must keep 5.0cm barcode width");
            assertTrue(contentXml.contains("svg:height=\"1.0cm\""), resource + " must keep 1.0cm barcode height");

            byte[] pdfBytes = toPdf(odtBytes);
            try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
                BufferedImage page1 = new PDFRenderer(pdf).renderImageWithDPI(0, 200);
                Result barcode = decodeCode39(page1);
                assertEquals(BarcodeFormat.CODE_39, barcode.getBarcodeFormat());
                assertEquals(cnumber, barcode.getText(), resource + " Code 39 barcode on page 1");

                Result qr = decodeQr(page1);
                assertTrue(qr.getText().contains("Rechnung " + cnumber), resource + " EPC QR code must stay on page 1");
            }
        }
    }

    @Test
    void demoNonInvoiceTemplatesIncludeBarcodeAndRemoveItWhenBlank() throws Exception {
        Map<String, Object> productFields = new HashMap<String, Object>();
        productFields.put("product.cnumber", "ART-4711");
        productFields.put("product.cname", "Musterartikel");
        productFields.put("companyinfo.name", "Musterfirma");

        byte[] productOdt = renderTemplate("/extra/templates/demo-05.odt", productFields,
                YabsBarcodeGenerator.code39("ART-4711"), null);
        try (PDDocument pdf = Loader.loadPDF(toPdf(productOdt))) {
            BufferedImage page1 = new PDFRenderer(pdf).renderImageWithDPI(0, 200);
            assertEquals("ART-4711", decodeCode39(page1).getText());
        }

        Map<String, Object> contactFields = new HashMap<String, Object>();
        contactFields.put("contact.cnumber", "KD-0042");
        contactFields.put("contact.company", "Beispiel GmbH");
        contactFields.put("companyinfo.name", "Musterfirma");

        byte[] contactOdt = renderTemplate("/extra/templates/demo-08.odt", contactFields,
                YabsBarcodeGenerator.code39("KD-0042"), null);
        try (PDDocument pdf = Loader.loadPDF(toPdf(contactOdt))) {
            BufferedImage page1 = new PDFRenderer(pdf).renderImageWithDPI(0, 200);
            assertEquals("KD-0042", decodeCode39(page1).getText());
        }

        // Without Barcode.img in context, NullImageBehaviour.RemoveImageTemplate removes the placeholder frame
        byte[] withoutBarcode = renderTemplate("/extra/templates/demo-05.odt", productFields, null, null);
        String xml = extractEntry(withoutBarcode, "content.xml");
        assertFalse(xml.contains("draw:name=\"Barcode.img\""), "Barcode frame must be removed when cnumber is missing");
    }

    @Test
    void allNonJournalDemoTemplatesContainBarcodePlaceholder() throws Exception {
        String[] withBarcode = {
            "demo-00.odt", "demo-00-ku.odt", "demo-01.odt", "demo-02.odt", "demo-03.odt", "demo-04.odt",
            "demo-05.odt", "demo-06.odt", "demo-07.odt", "demo-08.odt", "demo-10.odt", "demo-11.odt",
            "demo-12.odt", "demo-13.odt", "demo-14.odt", "demo-20.odt", "demo-21.odt", "demo-22.odt"
        };
        for (String name : withBarcode) {
            byte[] odt = readResource("/extra/templates/" + name);
            String xml = extractEntry(odt, "content.xml");
            assertTrue(xml.contains("draw:name=\"Barcode.img\""), name + " must contain Barcode.img frame");
            assertTrue(xml.contains("Pictures/barcode.png"), name + " must reference Pictures/barcode.png");
            assertTrue(hasEntry(odt, "Pictures/barcode.png"), name + " must bundle Pictures/barcode.png");
        }
        byte[] journal = readResource("/extra/templates/demo-09.odt");
        assertFalse(extractEntry(journal, "content.xml").contains("draw:name=\"Barcode.img\""),
                "Journal template has no cnumber and must not contain Barcode.img");
    }

    private static Map<String, Object> sampleInvoiceFields(String cnumber) {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("item.cnumber", cnumber);
        fields.put("item.dateadded", "14.11.2026");
        fields.put("item.servicedate", "14.11.2026");
        fields.put("item.netvaluef", "290,67 €");
        fields.put("item.taxvaluef", "55,23 €");
        fields.put("item.grosvaluef", "345,90 €");
        fields.put("item.discountvaluef", "0,00 €");
        fields.put("item.discountnetdifferencef", "");
        fields.put("item.taxnotes", "");
        fields.put("item.contact.cnumber", "KD000415");
        fields.put("item.contact.eori", "DE987654321098765");
        fields.put("item.contact.title", "DARC OV-L23");
        fields.put("item.contact.fullname", "Michael Punsmann");
        fields.put("item.contact.invoiceaddress.street", "Kardinal-von-Galen Str. 6");
        fields.put("item.contact.invoiceaddress.zip", "46514");
        fields.put("item.contact.invoiceaddress.city", "Schermbeck");
        fields.put("item.contact.invoiceaddress.country", "DE");
        fields.put("item.contact.zahlungsziel", "Zahlbar bis 28.11.2026");
        fields.put("item.contact.spezial", "");
        fields.put("companyinfo.business", "Musterfirma GmbH");
        fields.put("companyinfo.name", "Musterfirma");
        fields.put("companyinfo.street", "Musterstrasse 1");
        fields.put("companyinfo.zip", "66663");
        fields.put("companyinfo.city", "Musterheim");
        fields.put("companyinfo.phone", "45435345435");
        fields.put("companyinfo.fax", "");
        fields.put("companyinfo.email", "aaa@fff.dee");
        fields.put("companyinfo.taxnumber", "");
        fields.put("companyinfo.vatid", "DE435345435");
        fields.put("companyinfo.eori", "DE123456789012345");
        fields.put("companyinfo.bankname", "");
        fields.put("companyinfo.banknumber", "");
        fields.put("companyinfo.account", "DE02500105170137075030");
        fields.put("companyinfo.smallbusinessnote", "Gemäß § 19 UStG wird keine Umsatzsteuer berechnet.");
        return fields;
    }

    private static byte[] renderTemplate(String resource, Map<String, Object> fields,
            IImageProvider barcodeProvider, IImageProvider qrProvider) throws Exception {
        ByteArrayOutputStream filledOdt = new ByteArrayOutputStream();
        try (InputStream in = YabsBarcodeGeneratorTest.class.getResourceAsStream(resource)) {
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
            meta.addFieldAsImage("Barcode.img", NullImageBehaviour.RemoveImageTemplate);
            meta.addFieldAsImage("Code39.img", NullImageBehaviour.RemoveImageTemplate);
            report.setFieldsMetadata(meta);

            IContext ctx = report.createContext();
            List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
            for (int r = 1; r <= 5; r++) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("C0", String.valueOf(r));
                row.put("C1", "1,00");
                row.put("C2", "Position " + r + " Musterleistung");
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
            if (qrProvider != null) {
                ctx.put("QRCode.img", qrProvider);
            }
            if (barcodeProvider != null) {
                ctx.put("Barcode.img", barcodeProvider);
                ctx.put("Code39.img", barcodeProvider);
            }
            for (Map.Entry<String, Object> e : fields.entrySet()) {
                ctx.put(e.getKey(), e.getValue());
            }

            report.process(ctx, filledOdt);
        }
        return filledOdt.toByteArray();
    }

    private static Result decodeCode39(BufferedImage image) throws Exception {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(w, h, pixels)));
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        return new Code39Reader().decode(bitmap, hints);
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

    private static byte[] toPdf(byte[] odtBytes) throws Exception {
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        OdfTextDocument doc = OdfTextDocument.loadDocument(new ByteArrayInputStream(odtBytes));
        PdfConverter.getInstance().convert(doc, pdfOut, PdfOptions.create());
        return pdfOut.toByteArray();
    }

    private static byte[] readResource(String resource) throws Exception {
        try (InputStream in = YabsBarcodeGeneratorTest.class.getResourceAsStream(resource)) {
            return in.readAllBytes();
        }
    }

    private static String extractEntry(byte[] odt, String name) throws Exception {
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(odt))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                if (name.equals(e.getName())) {
                    return new String(z.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError(name + " not found");
    }

    private static boolean hasEntry(byte[] odt, String name) throws Exception {
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(odt))) {
            for (ZipEntry e; (e = z.getNextEntry()) != null;) {
                if (name.equals(e.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
