package mpv5.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaceholderListTest {

    @Test
    void placeholdersSortedWithoutTheTable() {
        Map<String, Object> f = new HashMap<String, Object>();
        f.put("item.paymentiban", "DE12 3456");
        f.put("item.contact.cname", "Christ");
        f.put("item.discountvalue", null);
        f.put("table1", new ArrayList<String[]>());
        List<String[]> l = PlaceholderList.placeholders(f);
        assertEquals(3, l.size());
        assertEquals("<item.contact.cname>", l.get(0)[0]);
        assertEquals("", l.get(1)[1]);
        assertEquals("<item.paymentiban>", l.get(2)[0]);
    }

    @Test
    void searchByNameOrValue() {
        String[] iban = {"<item.paymentiban>", "DE12 3456"};
        String[] empty = {"<item.discountvalue>", ""};
        assertTrue(PlaceholderList.shown(iban, "IBAN", false));
        assertTrue(PlaceholderList.shown(iban, "de12", false));
        assertTrue(PlaceholderList.shown(iban, "payment iban", false));
        assertFalse(PlaceholderList.shown(iban, "bic", false));
        assertTrue(PlaceholderList.shown(empty, "", false));
        assertFalse(PlaceholderList.shown(empty, "", true));
    }
}
