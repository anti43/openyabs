package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The working day of {@link WorkflowTest} with the GoBD plugin, then an audit
 * as a tax auditor would do it: the plugin's checks, the Z3 export, and
 * manipulations (database, archive file, receipt, audit log) that must be
 * found.
 */
class WorkflowGobdTest {

    @TempDir
    Path tmp;

    private static Object[][] rows(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null ? new Object[0][] : d;
    }

    private static Object field(Object o, String name) throws Exception {
        java.lang.reflect.Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static Object call(Object o, String method, Class<?>[] types, Object... args) throws Exception {
        java.lang.reflect.Method m = o.getClass().getDeclaredMethod(method, types);
        m.setAccessible(true);
        return EInvoiceRoundTripTest.onEdt(() -> m.invoke(o, args));
    }

    private static String check(Object audit, String method) throws Exception {
        return String.valueOf(call(audit, method, new Class<?>[0]));
    }

    private static List<String> auditLogErrors(Object audit) throws Exception {
        List<String> errors = new ArrayList<String>();
        call(audit, "checkAuditLog", new Class<?>[]{List.class}, errors);
        return errors;
    }

    private static void sql(String s) throws Exception {
        Connection c = DatabaseConnection.instanceOf().getConnection();
        try (Statement st = c.createStatement()) {
            st.execute(s);
        }
    }

    @Test
    void aWorkingDayWithGobdAndAnAudit() throws Exception {
        mpv5.plugin.ExtensionManager.Installed[] gobd = new mpv5.plugin.ExtensionManager.Installed[1];
        Item invoice = WorkflowTest.workingDay(tmp, () -> {
            mpv5.plugin.ExtensionManager m = mpv5.plugin.ExtensionManager.instance();
            gobd[0] = m.register(new org.openyabs.gobd.GobdCompliance());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m.start(gobd[0])), String.valueOf(gobd[0].getError()));
            return null;
        }, true, null);
        Object g = field(gobd[0].extension, "gobd");
        java.lang.reflect.Constructor<?> ac = Class.forName("org.openyabs.gobd.Audit").getDeclaredConstructor(g.getClass());
        ac.setAccessible(true);
        Object audit = ac.newInstance(g);

        //--- what the plugin recorded during the day
        Object[][] lock = rows("SELECT archive_path, status FROM gobd_document_lock WHERE document_id = " + invoice.__getIDS());
        assertEquals(1, lock.length, "the exported invoice is finalized");
        Path archived = Paths.get(String.valueOf(lock[0][0]));
        assertTrue(Files.isRegularFile(archived), "the original is archived: " + archived);
        assertEquals(Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM expense_receipts")[0][0])),
                Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM gobd_receipt")[0][0])), "every expense receipt chained");
        for (String action : new String[]{"PLUGIN_STARTED", "RECEIPT_CHAINED"}) {
            assertTrue(Integer.parseInt(String.valueOf(rows("SELECT COUNT(*) FROM gobd_audit_log WHERE action = '" + action + "'")[0][0])) > 0, action);
        }
        System.out.println("AUDIT LOG: " + java.util.Arrays.deepToString(rows("SELECT action, COUNT(*) FROM gobd_audit_log GROUP BY action ORDER BY action")));

        //--- the auditor: the checks of the plugin find nothing
        String archive = check(audit, "checkArchive");
        String receipts = check(audit, "checkReceipts");
        System.out.println("CHECK ARCHIVE: " + archive + "\nCHECK RECEIPTS: " + receipts);
        assertTrue(archive.startsWith("OK"), archive);
        assertTrue(receipts.startsWith("OK"), receipts);
        assertEquals(List.of(), auditLogErrors(audit));

        //--- the Z3 export (data carrier for the auditor)
        int year = java.time.LocalDate.now().getYear();
        Path zip = (Path) call(audit, "exportZ3", new Class<?>[]{int.class}, year);
        assertTrue(Files.isRegularFile(zip), "Z3 export");
        List<String> entries = new ArrayList<String>();
        StringBuilder csv = new StringBuilder();
        try (java.util.zip.ZipFile z = new java.util.zip.ZipFile(zip.toFile())) {
            for (java.util.Enumeration<? extends java.util.zip.ZipEntry> e = z.entries(); e.hasMoreElements();) {
                java.util.zip.ZipEntry ze = e.nextElement();
                entries.add(ze.getName());
                if (ze.getName().endsWith(".csv")) {
                    csv.append(new String(z.getInputStream(ze).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).append('\n');
                }
            }
        }
        System.out.println("Z3: " + entries);
        assertTrue(entries.contains("index.xml"), "index.xml");
        assertTrue(csv.indexOf(invoice.__getCnumber()) >= 0, "the invoice in the export");
        assertTrue(csv.indexOf("LIEF-7") >= 0, "the imported expense in the export");

        //--- manipulations an auditor tries: each one is found, undone it is fine again
        String net = String.valueOf(rows("SELECT netvalue FROM items WHERE ids = " + invoice.__getIDS())[0][0]);
        sql("UPDATE items SET netvalue = 1 WHERE ids = " + invoice.__getIDS());
        String found = check(audit, "checkArchive");
        assertTrue(found.startsWith("FEHLER"), "amount changed in the database: " + found);
        sql("UPDATE items SET netvalue = " + net + " WHERE ids = " + invoice.__getIDS());
        assertTrue(check(audit, "checkArchive").startsWith("OK"));

        byte[] original = Files.readAllBytes(archived);
        archived.toFile().setWritable(true);
        Files.write(archived, new byte[]{'x'}, java.nio.file.StandardOpenOption.APPEND);
        found = check(audit, "checkArchive");
        assertTrue(found.startsWith("FEHLER"), "archived file changed: " + found);
        Files.write(archived, original);
        assertTrue(check(audit, "checkArchive").startsWith("OK"));

        Object[] receipt = rows("SELECT ids, expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by FROM expense_receipts")[0];
        sql("DELETE FROM expense_receipts WHERE ids = " + receipt[0]);
        found = check(audit, "checkReceipts");
        assertTrue(found.startsWith("FEHLER"), "receipt removed from the database: " + found);
        try (java.sql.PreparedStatement p = DatabaseConnection.instanceOf().getConnection().prepareStatement(
                "INSERT INTO expense_receipts (ids, expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            for (int i = 0; i < receipt.length; i++) {
                p.setObject(i + 1, receipt[i]);
            }
            p.executeUpdate();
        }
        assertTrue(check(audit, "checkReceipts").startsWith("OK"), "receipt back");

        Object[] entry = rows("SELECT id, action FROM gobd_audit_log WHERE row_hash IS NOT NULL ORDER BY id")[0];
        sql("UPDATE gobd_audit_log SET action = 'MANIPULATED' WHERE id = " + entry[0]);
        assertFalse(auditLogErrors(audit).isEmpty(), "audit log changed");
        sql("UPDATE gobd_audit_log SET action = '" + entry[1] + "' WHERE id = " + entry[0]);
        assertEquals(List.of(), auditLogErrors(audit));

        //--- a correction is a cancellation invoice, finalized as well
        assertEquals(Boolean.TRUE, call(g, "stornoDocument", new Class<?>[]{Item.class}, invoice));
        Object[][] storno = rows("SELECT document_id, status FROM gobd_document_lock WHERE cancels_document_id = " + invoice.__getIDS());
        assertEquals(1, storno.length, "the cancellation invoice is finalized");
        assertEquals("CANCELLED", String.valueOf(rows("SELECT status FROM gobd_document_lock WHERE document_id = " + invoice.__getIDS())[0][0]));
        java.util.Date now = new java.util.Date();
        mpv5.handler.JournalCalc.Totals t = mpv5.handler.JournalLoader.loadAllResult(new mpv5.utils.date.vTimeframe(
                mpv5.utils.date.DateConverter.getStartOfYear(now), mpv5.utils.date.DateConverter.getEndOfYear(now)), false).totals;
        System.out.println("JOURNAL AFTER CANCELLATION: income " + t.incomeNet + "/" + t.incomeTax + " receivables " + t.openReceivables);
        //the money was received (cash basis): income stays until it is paid back; the cancellation is
        //owed to the customer, no receivable
        assertEquals(0, new java.math.BigDecimal("-328.40").compareTo(t.openReceivables), "refund owed: " + t.openReceivables);
        Object[][] st = rows("SELECT netvalue, taxvalue FROM items WHERE ids = " + storno[0][0]);
        assertEquals(0, new java.math.BigDecimal("280").compareTo(new java.math.BigDecimal(String.valueOf(st[0][0]))), "credit note amounts positive");
        assertTrue(check(audit, "checkArchive").startsWith("OK"), "after the cancellation");
        assertEquals(List.of(), auditLogErrors(audit));
    }
}
