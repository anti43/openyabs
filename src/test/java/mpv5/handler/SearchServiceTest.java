package mpv5.handler;

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
}
