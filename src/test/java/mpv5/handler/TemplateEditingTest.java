package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemplateEditingTest {

    @TempDir
    Path tmp;

    @Test
    void workingFileHasANameAndTheExtension() {
        assertEquals("Rechnung_Muller_GmbH_12.odt", TemplateEditing.fileName("Rechnung Müller GmbH", 12, "a1b2c3.odt"));
        //no extension stored: an ODT file; never a hidden file
        assertEquals("Vorlage_3.odt", TemplateEditing.fileName("~~", 3, "a1b2c3"));
        assertEquals("Formular_4.pdf", TemplateEditing.fileName("Formular", 4, "x.PDF"));
    }

    @Test
    void lockFileOfLibreOfficeMeansOpen() throws Exception {
        File f = tmp.resolve("Rechnung_1.odt").toFile();
        Files.write(f.toPath(), new byte[]{1});
        assertFalse(TemplateEditing.isOpenInOffice(f));
        Files.write(tmp.resolve(".~lock.Rechnung_1.odt#"), new byte[]{1});
        assertTrue(TemplateEditing.isOpenInOffice(f));
    }

    @Test
    void waitsUntilTheFileIsWritten() throws Exception {
        File f = tmp.resolve("a.odt").toFile();
        Files.write(f.toPath(), new byte[]{1, 2, 3});
        assertTrue(TemplateEditing.waitUntilWritten(f));
    }
}
