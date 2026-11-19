package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Revenue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The same run as {@link EInvoiceRoundTripTest} with the GoBD plugin: all of
 * it works as without the plugin, and the plugin locks the bookings with the
 * receipts YaBS archived (no copy of its own), chains the receipts, finalizes
 * the exported invoice and writes the audit log; its checks find no error.
 */
class EInvoiceGobdTest {

    @TempDir
    Path tmp;

    private static Object one(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return d == null || d.length == 0 ? null : d[0][0];
    }

    private static int n(String sql) throws Exception {
        return Integer.parseInt(String.valueOf(one(sql)));
    }

    private static void flushUi() throws Exception {
        for (int i = 0; i < 3; i++) {
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
        }
    }

    @Test
    void theSameRunWithThePlugin() throws Exception {
        mpv5.plugin.ExtensionManager[] m = new mpv5.plugin.ExtensionManager[1];
        mpv5.plugin.ExtensionManager.Installed[] gobd = new mpv5.plugin.ExtensionManager.Installed[1];
        mpv5.db.objects.Item inv = EInvoiceRoundTripTest.scenario(tmp, () -> {
            m[0] = mpv5.plugin.ExtensionManager.instance();
            gobd[0] = m[0].register(new org.openyabs.gobd.GobdCompliance());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m[0].start(gobd[0])), String.valueOf(gobd[0].getError()));
            flushUi();
            return null;
        });
        flushUi();
        Object g = field(gobd[0].extension, "gobd");

        //income: locked with the receipt of YaBS (same file, same hash), no own copy
        assertEquals(n("SELECT COUNT(*) FROM revenues"), n("SELECT COUNT(*) FROM gobd_income_lock"), "every imported income is locked");
        assertEquals(0, n("SELECT COUNT(*) FROM gobd_income_lock l WHERE NOT EXISTS (SELECT 1 FROM revenue_receipts r"
                + " WHERE r.revenuesids = l.revenue_id AND r.archivepath = l.archive_path AND r.sha256 = l.file_sha256)"),
                "the locks refer to the receipts of YaBS");
        //every receipt is chained
        assertEquals(n("SELECT COUNT(*) FROM expense_receipts"), n("SELECT COUNT(*) FROM gobd_receipt"));
        assertEquals(n("SELECT COUNT(*) FROM revenue_receipts"), n("SELECT COUNT(*) FROM gobd_revenue_receipt"));
        //the exported invoice is finalized (setting: lock on export)
        assertEquals(1, n("SELECT COUNT(*) FROM gobd_document_lock WHERE document_id = " + inv.__getIDS()));
        //the audit log
        assertTrue(n("SELECT COUNT(*) FROM gobd_audit_log WHERE action = 'INCOME_LOCKED'") >= 1);
        assertTrue(n("SELECT COUNT(*) FROM gobd_audit_log WHERE action = 'RECEIPT_CHAINED'") >= 3);

        //the checks of the plugin
        Object audit = newAudit(g);
        String archive = String.valueOf(call(audit, "checkArchive"));
        String receipts = String.valueOf(call(audit, "checkReceipts"));
        assertTrue(archive.startsWith("OK"), archive);
        assertTrue(receipts.startsWith("OK"), receipts);

        //locked bookings can not be changed
        Revenue rev = (Revenue) DatabaseObject.getObjects(Context.getRevenue()).get(0);
        rev.setBrutvalue(new java.math.BigDecimal("1.00"));
        assertFalse(mpv5.ui.dialogs.Popup.silently(() -> rev.save(true)), "locked income");
        mpv5.db.objects.Expense exp = (mpv5.db.objects.Expense) DatabaseObject.getObjects(Context.getExpense()).get(0);
        exp.setBrutvalue(new java.math.BigDecimal("1.00"));
        assertFalse(mpv5.ui.dialogs.Popup.silently(() -> exp.save(true)), "locked expense");

        //a receipt linked to an income without receipt (button in the view of the plugin)
        Revenue plain = new Revenue();
        plain.setCname("Bar");
        plain.setBrutvalue(new java.math.BigDecimal("50"));
        plain.setNetvalue(new java.math.BigDecimal("42.02"));
        plain.setTaxpercentvalue(new java.math.BigDecimal("19"));
        plain.setAccountsids(1);
        plain.setGroupsids(1);
        plain.setDateadded(new java.util.Date());
        assertTrue(plain.save(true));
        File scan = tmp.resolve("quittung.png").toFile();
        Files.write(scan.toPath(), new byte[]{1, 2, 3});
        java.lang.reflect.Method link = g.getClass().getDeclaredMethod("importIncomeFile", Path.class, Revenue.class, boolean.class);
        link.setAccessible(true);
        assertNotNull(mpv5.ui.dialogs.Popup.silently(() -> link.invoke(g, scan.toPath(), plain, true)));
        flushUi();
        assertEquals("RECEIPT_FILE", String.valueOf(one("SELECT artifact_format FROM gobd_income_lock WHERE revenue_id = " + plain.__getIDS())));
        receipts = String.valueOf(call(audit, "checkReceipts"));
        assertTrue(receipts.startsWith("OK"), receipts);
        m[0].stop(gobd[0]);
    }

    private static Object field(Object o, String name) throws Exception {
        java.lang.reflect.Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    private static Object newAudit(Object g) throws Exception {
        java.lang.reflect.Constructor<?> c = Class.forName("org.openyabs.gobd.Audit").getDeclaredConstructor(g.getClass());
        c.setAccessible(true);
        return c.newInstance(g);
    }

    private static Object call(Object o, String method) throws Exception {
        java.lang.reflect.Method mm = o.getClass().getDeclaredMethod(method);
        mm.setAccessible(true);
        return mpv5.ui.dialogs.Popup.silently(() -> mm.invoke(o));
    }
}
