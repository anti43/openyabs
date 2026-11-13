package mpv5.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SimpleMailTest {

    @Test
    void keepsSeveralAttachments() {
        SimpleMail m = new SimpleMail();
        m.setAttachments(Arrays.asList(new File("R-1.pdf"), null, new File("R-1.xml")));
        assertEquals(Arrays.asList(new File("R-1.pdf"), new File("R-1.xml")), m.getAttachments());
        assertEquals(new File("R-1.pdf"), m.getAttachment());
        m.setAttachment(new File("other.pdf"));
        assertEquals(1, m.getAttachments().size(), "setAttachment replaces all");
        m.setAttachment(null);
        assertTrue(m.getAttachments().isEmpty());
    }

    @Test
    void failedAttachmentJobIsReportedInBatchLog() throws Exception {
        List<String> log = new ArrayList<String>();
        SimpleMail m = new SimpleMail(log);
        m.setRecipientsAddress("kunde@example.com");
        m.set(null, new IllegalStateException("E-invoice not sent: invalid"));
        assertEquals(1, log.size());
        assertTrue(log.get(0).contains("kunde@example.com") && log.get(0).contains("E-invoice not sent"), log.get(0));
    }

    @TempDir
    Path tmp;

    /**
     * Sends through a local SMTP server: the archive BCC gets the mail with
     * all attachments, CC works on the SMTP path too
     */
    @Test
    void sendsBccCopyWithAllAttachments() throws Exception {
        File pdf = tmp.resolve("R-2026-0042.pdf").toFile();
        File xml = tmp.resolve("R-2026-0042.xml").toFile();
        Files.write(pdf.toPath(), "%PDF-1.4 test".getBytes());
        Files.write(xml.toPath(), "<Invoice/>".getBytes());

        try (SmtpSink sink = new SmtpSink()) {
            List<String> log = new ArrayList<String>();
            SimpleMail m = new SimpleMail(log);
            m.setSmtpHost("localhost");
            m.setSmtpPort(String.valueOf(sink.port()));
            m.setUsername(null);
            m.setSenderAddress("rechnung@muster.example");
            m.setRecipientsAddress("kunde@example.com");
            m.setBccAddress(" archiv@muster.example ; ");
            m.setCCAddress("buchhaltung@muster.example");
            m.setSubject("Rechnung R-2026-0042");
            m.setText("Anbei Ihre Rechnung");
            m.set((MailAttachments) () -> Arrays.asList(pdf, xml), null);
            sink.await();

            assertEquals(Arrays.asList("kunde@example.com", "buchhaltung@muster.example", "archiv@muster.example"),
                    sink.recipients, String.valueOf(log));
            String data = sink.data.toString();
            assertTrue(data.contains("filename=R-2026-0042.pdf"), data);
            assertTrue(data.contains("filename=R-2026-0042.xml"), data);
            assertFalse(data.contains("archiv@muster.example"), "BCC must not be visible to the customer");
            assertTrue(log.toString().contains("kunde@example.com"), String.valueOf(log));
            assertTrue(data.contains("text/plain; charset=utf-8"), "plain text is sent as plain text: " + data);
        }
    }

    @Test
    void detectsHtmlBodies() {
        assertFalse(SimpleMail.isHtml(null));
        assertFalse(SimpleMail.isHtml("Hallo,\nanbei Ihre Rechnung.\n\nGruß"));
        assertFalse(SimpleMail.isHtml("Betrag < 100 und > 50, a<b"));
        assertTrue(SimpleMail.isHtml("<html><body><p>Hallo</p></body></html>"));
        assertTrue(SimpleMail.isHtml("Hallo<br>Welt"));
        assertTrue(SimpleMail.isHtml("<P style='color:red'>x</P>"));
        assertTrue(SimpleMail.isHtml("<!DOCTYPE html><html></html>"));
        assertEquals("text/plain; charset=utf-8", SimpleMail.contentType("Hallo"));
        assertEquals("text/html; charset=utf-8", SimpleMail.contentType("<b>Hallo</b>"));
    }
}
