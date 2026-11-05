package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ODTFile2TableTest {

    @Test
    void invoiceTable() {
        assertEquals("item.xtable1", ODTFile2.mainTableKey(Arrays.asList("item.reference.xtable1", "item.xtable1", "item.cname")));
    }

    @Test
    void journalTable() {
        //invoice overview ("Rechnungsübersicht"): the context name is the localized journal name
        assertEquals("Journal.xtable1", ODTFile2.mainTableKey(Arrays.asList("journal.netvalue", "Journal.xtable1")));
        assertEquals("Buchungsjournal.xtable1", ODTFile2.mainTableKey(Arrays.asList("Buchungsjournal.xtable1")));
    }

    @Test
    void referenceTableIsNotTheMainTable() {
        assertNull(ODTFile2.mainTableKey(Arrays.asList("item.reference.xtable1", "item.cname")));
        assertNull(ODTFile2.mainTableKey(Arrays.asList("item.xtable10")));
    }
}
