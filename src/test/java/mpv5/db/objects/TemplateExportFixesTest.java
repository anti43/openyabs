package mpv5.db.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TemplateExportFixesTest {

    @Test
    void cacheNameOfATemplateIsAscii() {
        assertEquals("Demo_-_Auftragsbestatigung", Template.safeName("Demo - Auftragsbestätigung"));
        assertEquals("Gesprachsnotiz_Strasse", Template.safeName("Gesprächsnotiz Straße"));
        assertEquals("template", Template.safeName(""));
    }

    @Test
    void conversationTextWithoutRtf() {
        String rtf = "{\\rtf1\\ansi {\\fonttbl\\f0\\fnil Monospaced;} Gespr\\'e4chsnotiz: R\\'fcckruf\\par Zeile 2 \\par }";
        assertEquals("Gesprächsnotiz: Rückruf\nZeile 2", Conversation.plainText(rtf));
        //stored by older versions with ` for '
        assertEquals("Gesprächsnotiz: Rückruf\nZeile 2", Conversation.plainText(rtf.replace("'", "`")));
    }
}
