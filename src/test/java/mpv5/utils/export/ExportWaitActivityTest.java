package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import mpv5.db.objects.Item;
import mpv5.handler.Activity;
import mpv5.utils.jobs.Waiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that {@link Export#wait(File, mpv5.db.common.DatabaseObject)}
 * records non-draft file activity with the owning record for merged plain PDFs
 * and single exports so GoBD auto-finalization receives the issued file.
 */
class ExportWaitActivityTest {

    @TempDir
    Path tmp;

    @BeforeEach
    void clearBefore() {
        Activity.clear();
    }

    @AfterEach
    void clearAfter() {
        Activity.clear();
    }

    @Test
    void waitReportsMergedFileWithOwner() throws Exception {
        Item invoice = (Item) mpv5.db.common.DatabaseObject.getObject(mpv5.db.common.Context.getInvoice());
        invoice.setIDS(77);
        invoice.setCnumber("RE-2026-0077");

        File src = tmp.resolve("merged-temp.pdf").toFile();
        Files.write(src.toPath(), "pdf-bytes".getBytes(StandardCharsets.UTF_8));
        File target = tmp.resolve("RE-2026-0077.pdf").toFile();

        Waiter waiter = Export.wait(target, invoice);
        waiter.set(src, null);

        assertTrue(target.isFile());
        List<Activity.Entry> entries = Activity.entries();
        assertEquals(1, entries.size(), String.valueOf(entries));
        Activity.Entry entry = entries.get(0);
        assertEquals(Activity.Kind.FILE, entry.kind);
        assertFalse(entry.draft, "issued copy must not be marked draft");
        assertEquals(target, entry.file);
        assertEquals(invoice.getContext(), entry.context);
        assertEquals(77, entry.id);
    }
}
