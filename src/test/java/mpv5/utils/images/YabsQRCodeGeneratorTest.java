package mpv5.utils.images;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.db.objects.Item;
import mpv5.utils.xdocreport.YabsODTPreprocessor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
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
    void invoiceWithQrCodeKeepsCompactSizeAndFitsOnFirstPage() throws Exception {
        String payload = YabsQRCodeGenerator.payload(
                "Musterfirma GmbH", "DE02500105170137075030", "", new BigDecimal("345.90"), "Rechnung 000005");
        YabsQRCodeGenerator gen = new YabsQRCodeGenerator(payload);
        gen.generate();

        ByteArrayOutputStream filledOdt = new ByteArrayOutputStream();
        try (InputStream in = getClass().getResourceAsStream("/extra/templates/invoice.odt")) {
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
            ctx.put("QRCode.img", gen.getImageProvider());
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
            ctx.put("companyinfo.bankname", "");
            ctx.put("companyinfo.banknumber", "");
            ctx.put("companyinfo.account", "DE02500105170137075030");

            report.process(ctx, filledOdt);
        }

        byte[] odtBytes = filledOdt.toByteArray();
        String contentXml = extractContentXml(odtBytes);
        assertTrue(contentXml.contains("svg:width=\"3cm\""), "QR frame must keep 3cm width from template");
        assertTrue(contentXml.contains("svg:height=\"3.245cm\""), "QR frame must keep 3.245cm height from template");

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        OdfTextDocument doc = OdfTextDocument.loadDocument(new ByteArrayInputStream(odtBytes));
        PdfConverter.getInstance().convert(doc, pdfOut, PdfOptions.create());
        byte[] pdfBytes = pdfOut.toByteArray();
        if (Files.isDirectory(Paths.get("scratch"))) {
            Files.write(Paths.get("scratch/invoice_qr_preview.pdf"), pdfBytes);
        }
        try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
            assertEquals(1, pdf.getNumberOfPages(), "5-item invoice with QR code must fit on 1 page");
        }
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
