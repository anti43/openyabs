package mpv5.ui.dialogs;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JFileChooser;
import mpv5.db.objects.Item;
import mpv5.handler.Activity;
import mpv5.utils.export.Export;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that saving an exported document through {@link DialogForFile#set}
 * reports a non-draft {@link Activity#file} entry for the saved file and owning
 * record, and reports nothing when the user cancels.
 */
class DialogForFileActivityTest {

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

    private static DialogForFile stubDialog(final File chosen, final int result) {
        return new DialogForFile(DialogForFile.FILES_ONLY) {
            private static final long serialVersionUID = 1L;
            private boolean approved;

            @Override
            public int showSaveDialog(Component parent) {
                approved = (result == JFileChooser.APPROVE_OPTION);
                return result;
            }

            @Override
            public File getSelectedFile() {
                return approved ? chosen : super.getSelectedFile();
            }
        };
    }

    private static Item invoice(int id, String number) {
        Item invoice = (Item) mpv5.db.common.DatabaseObject.getObject(mpv5.db.common.Context.getInvoice());
        invoice.setIDS(id);
        invoice.setCnumber(number);
        return invoice;
    }

    @Test
    void savingExportReportsIssuedActivityWithOwner() throws Exception {
        Item invoice = invoice(42, "RE-2026-0042");

        File tempPdf = tmp.resolve("draft-temp.pdf").toFile();
        Files.write(tempPdf.toPath(), "pdf-content".getBytes(StandardCharsets.UTF_8));
        File chosen = tmp.resolve("RE-2026-0042.pdf").toFile();

        Export export = new Export(new mpv5.db.objects.Template());
        export.setTargetFile(tempPdf);

        DialogForFile dialog = stubDialog(chosen, JFileChooser.APPROVE_OPTION);
        dialog.setDataOwner(invoice);
        dialog.set(export, null);

        assertTrue(chosen.isFile());
        List<Activity.Entry> entries = Activity.entries();
        assertEquals(1, entries.size(), String.valueOf(entries));
        Activity.Entry entry = entries.get(0);
        assertEquals(Activity.Kind.FILE, entry.kind);
        assertFalse(entry.draft);
        assertEquals(chosen, entry.file);
        assertEquals(invoice.getContext(), entry.context);
        assertEquals(42, entry.id);
    }

    @Test
    void savingMergedFileReportsIssuedActivityWithOwner() throws Exception {
        Item invoice = invoice(43, "RE-2026-0043");

        File mergedPdf = tmp.resolve("merged.pdf").toFile();
        Files.write(mergedPdf.toPath(), "merged-content".getBytes(StandardCharsets.UTF_8));
        File chosen = tmp.resolve("RE-2026-0043.pdf").toFile();

        DialogForFile dialog = stubDialog(chosen, JFileChooser.APPROVE_OPTION);
        dialog.setDataOwner(invoice);
        dialog.set(mergedPdf, null);

        assertTrue(chosen.isFile());
        List<Activity.Entry> entries = Activity.entries();
        assertEquals(1, entries.size(), String.valueOf(entries));
        Activity.Entry entry = entries.get(0);
        assertEquals(Activity.Kind.FILE, entry.kind);
        assertFalse(entry.draft);
        assertEquals(chosen, entry.file);
        assertEquals(invoice.getContext(), entry.context);
        assertEquals(43, entry.id);
    }

    @Test
    void cancellingDialogDoesNotReportIssuedActivity() throws Exception {
        Item invoice = invoice(44, "RE-2026-0044");

        File tempPdf = tmp.resolve("cancelled-temp.pdf").toFile();
        Files.write(tempPdf.toPath(), "pdf-content".getBytes(StandardCharsets.UTF_8));
        File chosen = tmp.resolve("RE-2026-0044.pdf").toFile();

        Export export = new Export(new mpv5.db.objects.Template());
        export.setTargetFile(tempPdf);

        DialogForFile dialog = stubDialog(chosen, JFileChooser.CANCEL_OPTION);
        dialog.setDataOwner(invoice);
        dialog.set(export, null);

        assertFalse(chosen.exists());
        assertTrue(Activity.entries().isEmpty(), String.valueOf(Activity.entries()));
    }
}
