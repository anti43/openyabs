package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Template;
import org.junit.jupiter.api.Test;

/**
 * Printed documents get a name (the print job too: a PDF printer suggests it
 * as file name) instead of a random one
 */
class ExportNameTest {

    @Test
    void fileNameJoinsTheNonEmptyPartsWithoutForbiddenCharacters() {
        assertEquals("Kontaktdatenblatt-000001-Muster GmbH", Export.fileName("Kontaktdatenblatt", "000001", "Muster GmbH"));
        assertEquals("Rechnung-RE_2026_1", Export.fileName("Rechnung", "RE/2026:1", ""));
        assertEquals("a-b_c", Export.fileName(" a ", null, "b\\c"));
        assertEquals("document", Export.fileName("", null));
        assertEquals("x", Export.fileName("<undefined> [99]", "x"));
    }

    @Test
    void contactSheetIsNamedAfterNumberAndCompanyOrPerson() {
        Template t = new Template();
        t.setMimetype(String.valueOf(mpv5.globals.Constants.TYPE_CONTACT));
        Contact c = new Contact();
        c.setCNumber("K-1001");
        c.setCompany("Kunde AG");
        c.setPrename("Max");
        c.setCname("Kunde");
        String sheet = mpv5.globals.Messages.CONTACT_SHEET.toString();
        assertEquals(sheet + "-K-1001-Kunde AG", Export.documentName(t, c));
        c.setCompany("");
        assertEquals(sheet + "-K-1001-Max Kunde", Export.documentName(t, c));
    }

    @Test
    void invoiceIsNamedAfterItsNumberNotTheFormatter() {
        Template t = new Template();
        t.setMimetype(String.valueOf(mpv5.globals.Constants.TYPE_INVOICE));
        mpv5.db.objects.Item i = new mpv5.db.objects.Item();
        i.setCnumber("RE-2026-0001");
        i.setCname("RE-2026-0001");
        assertEquals(mpv5.handler.TemplateHandler.getName(mpv5.globals.Constants.TYPE_INVOICE) + "-RE-2026-0001",
                Export.documentName(t, i));
    }

    @Test
    void printJobIsNamedLikeTheFile() {
        assertEquals("Kontaktdatenblatt-000001-Muster GmbH",
                mpv5.utils.print.PrintJob2Access.jobName(new File("/tmp/Kontaktdatenblatt-000001-Muster GmbH.pdf")));
    }
}
