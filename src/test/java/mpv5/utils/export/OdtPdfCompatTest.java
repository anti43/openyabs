package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

class OdtPdfCompatTest {

    @TempDir
    Path tmp;

    /**
     * The bundled delivery note with a rectangle with text in a table cell (as
     * in a user's own template): the converter failed with "Insertion of
     * illegal Element: 12"; as text frame it converts and keeps the text
     */
    @Test
    void shapeWithTextInTableCell() throws Exception {
        File odt = tmp.resolve("shape.odt").toFile();
        try (InputStream in = getClass().getResourceAsStream("/extra/templates/demo-03.odt");
                ZipInputStream zin = new ZipInputStream(in);
                ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(odt))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                byte[] b = zin.readAllBytes();
                if (e.getName().equals("content.xml")) {
                    String s = new String(b, StandardCharsets.UTF_8);
                    int cell = s.indexOf("<table:table-cell", s.indexOf("<office:text"));
                    int p = s.indexOf('>', s.indexOf("<text:p", cell)) + 1;
                    s = s.substring(0, p) + "<draw:rect text:anchor-type=\"paragraph\" svg:width=\"8cm\" svg:height=\"2.5cm\" svg:x=\"1cm\" svg:y=\"0.5cm\">"
                            + "<text:p>Auftrag erteilt</text:p><text:p>Unterschrift</text:p></draw:rect>" + s.substring(p);
                    b = s.getBytes(StandardCharsets.UTF_8);
                }
                ZipEntry n = new ZipEntry(e.getName());
                if (e.getName().equals("mimetype")) {
                    n.setMethod(ZipEntry.STORED);
                    n.setSize(b.length);
                    n.setCompressedSize(b.length);
                    java.util.zip.CRC32 crc = new java.util.zip.CRC32();
                    crc.update(b);
                    n.setCrc(crc.getValue());
                }
                zout.putNextEntry(n);
                zout.write(b);
                zout.closeEntry();
            }
        }
        //as it was: the conversion fails
        OdfTextDocument before = OdfTextDocument.loadDocument(odt);
        Exception failed = assertThrows(Exception.class, () -> PdfConverter.getInstance().convert(before, new ByteArrayOutputStream(), PdfOptions.create()));
        assertTrue(String.valueOf(failed.getMessage()).contains("Insertion of illegal Element"), failed.toString());

        OdfTextDocument doc = OdfTextDocument.loadDocument(odt);
        assertEquals(1, OdtPdfCompat.shapesInTablesToFrames(doc.getContentDom()));
        assertEquals(0, doc.getContentDom().getElementsByTagNameNS(OdtPdfCompat.DRAW_NS, "rect").getLength());
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfConverter.getInstance().convert(doc, pdf, PdfOptions.create());
        try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(pdf.toByteArray())) {
            assertTrue(d.getNumberOfPages() > 0);
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(d);
            assertTrue(text.contains("Auftrag erteilt") && text.contains("Unterschrift"), text);
        }
        //nothing to do in a document without such shapes
        assertEquals(0, OdtPdfCompat.shapesInTablesToFrames(OdfTextDocument.loadDocument(getClass().getResourceAsStream("/extra/templates/demo-00.odt")).getContentDom()));
    }
}
