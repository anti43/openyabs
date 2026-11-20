package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import mpv5.db.objects.Item;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link MixedRatesTest} with the GoBD plugin: everything as without, the
 * invoice finalized on export, the bookings of each rate chained, an income
 * booked in parts locked once, the plugin's checks OK.
 */
class MixedRatesGobdTest {

    @TempDir
    Path tmp;

    private static int n(String sql) throws Exception {
        return Integer.parseInt(String.valueOf(MixedRatesTest.rows(sql)[0][0]));
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

    @Test
    void threeRatesWithGobd() throws Exception {
        mpv5.plugin.ExtensionManager.Installed[] gobd = new mpv5.plugin.ExtensionManager.Installed[1];
        Item invoice = MixedRatesTest.scenario(tmp, () -> {
            mpv5.plugin.ExtensionManager m = mpv5.plugin.ExtensionManager.instance();
            gobd[0] = m.register(new org.openyabs.gobd.GobdCompliance());
            assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m.start(gobd[0])), String.valueOf(gobd[0].getError()));
            return null;
        });
        for (int i = 0; i < 3; i++) {
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
        }
        Object g = field(gobd[0].extension, "gobd");
        assertEquals(1, n("SELECT COUNT(*) FROM gobd_document_lock WHERE document_id = " + invoice.__getIDS()), "finalized on export");
        assertEquals(n("SELECT COUNT(*) FROM expense_receipts"), n("SELECT COUNT(*) FROM gobd_receipt"), "expense receipts chained");
        assertEquals(n("SELECT COUNT(*) FROM revenue_receipts"), n("SELECT COUNT(*) FROM gobd_revenue_receipt"), "income receipts chained");
        assertEquals(1, n("SELECT COUNT(*) FROM gobd_income_lock"), "the income booked in parts is locked once (one file)");
        java.lang.reflect.Constructor<?> ac = Class.forName("org.openyabs.gobd.Audit").getDeclaredConstructor(g.getClass());
        ac.setAccessible(true);
        Object audit = ac.newInstance(g);
        String archive = String.valueOf(call(audit, "checkArchive", new Class<?>[0]));
        String receipts = String.valueOf(call(audit, "checkReceipts", new Class<?>[0]));
        System.out.println("CHECK " + archive + "\n" + receipts);
        assertTrue(archive.startsWith("OK"), archive);
        assertTrue(receipts.startsWith("OK"), receipts);
        List<String> errors = new ArrayList<String>();
        call(audit, "checkAuditLog", new Class<?>[]{List.class}, errors);
        assertEquals(List.of(), errors);
        //every part of an income is protected: none can be changed
        for (mpv5.db.common.DatabaseObject o : mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getRevenue())) {
            mpv5.db.objects.Revenue r = (mpv5.db.objects.Revenue) o;
            r.setBrutvalue(new java.math.BigDecimal("1"));
            assertFalse(mpv5.ui.dialogs.Popup.silently(() -> r.save(true)), "income part " + r.__getIDS() + " locked");
        }
    }
}
