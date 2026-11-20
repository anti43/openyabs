package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.LockedDocumentProvider;
import mpv5.ui.panels.ModernItemPanel;
import mpv5.ui.toolbars.RecordActions;
import mpv5.utils.export.LockedDocumentCopy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A finalized invoice opens its archived original instead of a new file, also
 * in a view that was not refreshed after the invoice was locked (another tab
 * or a progress window was in front). If the archived file is missing, a
 * duplicate stamped as a copy is possible, refused when the data no longer
 * match what was finalized, and recorded in the audit log.
 */
class LockedDocumentGobdTest {

    @TempDir
    Path tmp;

    private static Object one(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null || d.length == 0 ? null : d[0][0];
    }

    private static void flushUi() throws Exception {
        for (int i = 0; i < 3; i++) {
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
        }
    }

    private static void sql(String s) throws Exception {
        try (java.sql.Statement st = mpv5.db.common.DatabaseConnection.instanceOf().getConnection().createStatement()) {
            st.execute(s);
        }
    }

    @Test
    void aLockedInvoiceOpensItsArchivedFile() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless(), "needs a display");
        ExtensionManager[] m = new ExtensionManager[1];
        ExtensionManager.Installed[] gobd = new ExtensionManager.Installed[1];
        Item invoice = MixedRatesTest.scenario(tmp, () -> {
            m[0] = ExtensionManager.instance();
            gobd[0] = m[0].register(new org.openyabs.gobd.GobdCompliance());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m[0].start(gobd[0])), String.valueOf(gobd[0].getError()));
            flushUi();
            return null;
        });
        flushUi();
        File archived = new File(String.valueOf(one("SELECT archive_path FROM gobd_document_lock WHERE document_id = " + invoice.__getIDS())));
        assertTrue(archived.isFile(), "archived on export: " + archived);
        byte[] original = Files.readAllBytes(archived.toPath());

        //a view that was not refreshed after the invoice was locked: nothing set on the view itself
        ModernItemPanel view = EInvoiceRoundTripTest.onEdt(() -> new ModernItemPanel(Context.getInvoice(), Item.TYPE_INVOICE));
        Item fresh = (Item) DatabaseObject.getObject(Context.getInvoice(), invoice.__getIDS());
        EInvoiceRoundTripTest.onEdt(() -> {
            view.setDataOwner(fresh, true);
            view.setContentLocked(false);
            view.setLockedDocument(null);
            return null;
        });
        assertEquals(archived.getCanonicalFile(), view.getLockedDocument().getCanonicalFile(),
                "the plugin is asked: the archived file, not a new one");
        //PDF, print, preview, ODT and e-invoice stop here: the archived file is opened
        assertTrue(EInvoiceRoundTripTest.onEdt(() -> RecordActions.openLockedDocument(view)));
        assertArrayEquals(original, Files.readAllBytes(archived.toPath()), "the original is not touched");

        //other records are not locked
        Item draft = (Item) DatabaseObject.getObject(Context.getInvoice());
        assertNull(m[0].lockedDocument(draft), "a new record");

        //--- the archived file is missing on disk
        LockedDocumentProvider provider = m[0].lockedDocument(fresh).provider;
        Path backup = tmp.resolve("backup-" + archived.getName());
        archived.setWritable(true);
        Files.move(archived.toPath(), backup, StandardCopyOption.REPLACE_EXISTING);
        assertEquals(archived.getCanonicalFile(), view.getLockedDocument().getCanonicalFile(), "still locked, file missing");
        assertNull(provider.duplicateRefused(fresh), "the data are as finalized: a duplicate is possible");
        assertEquals(List.of(), provider.duplicateWarnings(fresh));

        //the customer moved: the duplicate shows the new address, the user is told
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), fresh.__getContactsids());
        String street = c.__getStreet();
        c.setStreet("Neuer Weg 9");
        assertTrue(EInvoiceRoundTripTest.onEdt(() -> c.save(true)));
        List<String> warnings = provider.duplicateWarnings(fresh);
        assertEquals(1, warnings.size(), String.valueOf(warnings));
        assertTrue(warnings.get(0).contains("Neuer Weg 9"), warnings.get(0));
        c.setStreet(street);
        assertTrue(EInvoiceRoundTripTest.onEdt(() -> c.save(true)));

        //the invoice was changed in the database: no duplicate (it would differ from the original)
        String net = String.valueOf(one("SELECT netvalue FROM items WHERE ids = " + fresh.__getIDS()));
        sql("UPDATE items SET netvalue = 1 WHERE ids = " + fresh.__getIDS());
        String refused = provider.duplicateRefused(fresh);
        assertNotNull(refused, "changed data");
        assertTrue(refused.contains("Belegdaten"), refused);
        sql("UPDATE items SET netvalue = " + net + " WHERE ids = " + fresh.__getIDS());
        assertNull(provider.duplicateRefused(fresh));
        assertEquals(1, Integer.parseInt(String.valueOf(one("SELECT COUNT(*) FROM gobd_audit_log WHERE action = 'DUPLICATE_REFUSED'"))));

        //the duplicate: the PDF of the template, stamped on every page
        mpv5.db.objects.Template tpl = mpv5.handler.TemplateHandler.loadTemplate((long) fresh.templateGroupIds(), Item.TYPE_INVOICE);
        assertNotNull(tpl, "invoice template");
        File pdf = tmp.resolve("from-template.pdf").toFile();
        mpv5.utils.export.Export ex = (mpv5.utils.export.Export) mpv5.utils.export.Export.createFile(tpl, fresh);
        assertNull(EInvoiceRoundTripTest.onEdt(() -> ex.waitFor()), "PDF");
        mpv5.utils.export.Export.wait(pdf).set(ex, null);
        assertTrue(pdf.isFile());
        File duplicate = tmp.resolve("duplicate.pdf").toFile();
        String line = LockedDocumentCopy.stampText(new Date());
        LockedDocumentCopy.stamp(pdf, duplicate, line);
        try (org.apache.pdfbox.pdmodel.PDDocument d = org.apache.pdfbox.Loader.loadPDF(duplicate);
                org.apache.pdfbox.pdmodel.PDDocument o = org.apache.pdfbox.Loader.loadPDF(pdf)) {
            assertEquals(o.getNumberOfPages(), d.getNumberOfPages());
            org.apache.pdfbox.text.PDFTextStripper text = new org.apache.pdfbox.text.PDFTextStripper();
            for (int p = 1; p <= d.getNumberOfPages(); p++) {
                text.setStartPage(p);
                text.setEndPage(p);
                String t = text.getText(d);
                assertTrue(t.contains(line), "stamp line on page " + p);
                assertTrue(t.contains(mpv5.globals.Messages.LOCKED_DOCUMENT_DUPLICATE_WORD.toString()), "stamp word on page " + p);
            }
            assertTrue(d.getDocumentInformation().getTitle().contains(mpv5.globals.Messages.LOCKED_DOCUMENT_DUPLICATE_WORD.toString()));
        }
        provider.duplicateCreated(fresh, archived, duplicate);
        assertEquals(1, Integer.parseInt(String.valueOf(one("SELECT COUNT(*) FROM gobd_audit_log WHERE action = 'DUPLICATE_CREATED'"))));
        assertFalse(archived.exists(), "the duplicate never takes the place of the original");

        //the original restored from the backup: opened again
        Files.move(backup, archived.toPath());
        assertTrue(archived.isFile());
        assertEquals(archived.getCanonicalFile(), view.getLockedDocument().getCanonicalFile());

        //the plugin stopped: nothing is locked any more
        EInvoiceRoundTripTest.onEdt(() -> {
            m[0].stop(gobd[0]);
            return null;
        });
        assertNull(m[0].lockedDocument(fresh), "provider removed with the plugin");
    }
}
