package mpv5.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

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
}
