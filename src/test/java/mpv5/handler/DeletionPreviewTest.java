package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DeletionPreviewTest {

    @Test
    void itemIsDescribedWithNumberDateAndValue() {
        String d = DeletionPreview.describeItem(new Object[]{7, "R-2026-040", null, "59.8"});
        assertTrue(d.startsWith("R-2026-040"), d);
        assertTrue(d.contains("59"), d);
        assertEquals("R-1", DeletionPreview.describeItem(new Object[]{7, "R-1", null, null}));
    }

    @Test
    void idListForQueries() {
        assertEquals("1,2,30", DeletionPreview.in(Arrays.asList(1, 2, 30)));
        assertEquals("", DeletionPreview.in(Collections.<Integer>emptyList()));
    }

    @Test
    void nothingForUnsavedRecords() {
        assertTrue(DeletionPreview.of(null).isEmpty());
    }
}
