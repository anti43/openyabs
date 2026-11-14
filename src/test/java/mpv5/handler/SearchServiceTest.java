package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SearchServiceTest {

    @Test
    void everyWordInAnyOrder() {
        assertTrue(SearchService.matches("Neue Rechnung anlegen", "rechnung neu"));
        assertFalse(SearchService.matches("Neue Rechnung anlegen", "rechnung angebot"));
    }

    @Test
    void ignoresCaseAndAccents() {
        assertTrue(SearchService.matches("Schließen", "SCHLIESSEN"));
        assertTrue(SearchService.matches("Gespräche", "gesprache"));
        assertTrue(SearchService.matches("anything", "  "));
    }

    @Test
    void snippetShowsTheMatchingPartOfALongText() {
        String text = "Lieferung und Montage der Regale im Lager, wie besprochen, inklusive Anfahrt und Entsorgung. Ihre Bestellnummer: AB-4711-2026";
        String s = SearchService.snippet(text, "4711");
        assertTrue(s.contains("AB-4711-2026"), s);
        assertTrue(s.length() <= 90, s);
        assertEquals("Kurz 4711", SearchService.snippet("<b>Kurz</b> 4711", "4711"));
    }
}
