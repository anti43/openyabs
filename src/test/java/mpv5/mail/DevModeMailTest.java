package mpv5.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import mpv5.db.common.ConnectionTypeHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * -devmode never sends mail: the message is written to devmode-mails/*.eml
 */
class DevModeMailTest {

    @TempDir
    Path tmp;

    @Test
    void developerModeOnlyPretends() throws Exception {
        String before = mpv5.Main.MPPATH;
        mpv5.Main.MPPATH = tmp.toString();
        MimeMessage m = new MimeMessage(Session.getInstance(new java.util.Properties()));
        m.setSubject("Rechnung RE-1");
        m.setText("Hallo");
        try {
            ConnectionTypeHandler.DEVMODE = false;
            assertFalse(SimpleMail.pretend(m), "normal mode: really sent");
            ConnectionTypeHandler.DEVMODE = true;
            assertTrue(SimpleMail.pretend(m), "developer mode: not sent");
            File[] files = new File(tmp.toFile(), "devmode-mails").listFiles();
            assertEquals(1, files.length);
            assertTrue(Files.readString(files[0].toPath()).contains("Subject: Rechnung RE-1"));
        } finally {
            ConnectionTypeHandler.DEVMODE = false;
            mpv5.Main.MPPATH = before;
        }
    }
}
