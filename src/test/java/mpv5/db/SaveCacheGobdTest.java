package mpv5.db;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Date;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.plugin.ExtensionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A record is read with its saved values right after it was saved, also when
 * a plugin read the record while it was saved: the GoBD plugin reads a contact
 * in modifyOnSave (for the log of the change), which put the old row back into
 * the cache, so the next invoice showed the old address.
 */
class SaveCacheGobdTest {

    @TempDir
    Path tmp;

    private static int n(String sql) throws Exception {
        Object[][] d = QueryHandler.getConnection().freeQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getData();
        return Integer.parseInt(String.valueOf(d[0][0]));
    }

    @Test
    void aSavedContactIsReadWithItsNewValues() throws Exception {
        EInvoiceRoundTripTest.newInstallation(tmp);
        ExtensionManager m = ExtensionManager.instance();
        ExtensionManager.Installed gobd = m.register(new org.openyabs.gobd.GobdCompliance());
        assertTrue(mpv5.ui.dialogs.Popup.silently(() -> m.start(gobd)), String.valueOf(gobd.getError()));

        Contact c = new Contact();
        c.setCname("Kunde");
        c.setCompany("Kunde AG");
        c.setStreet("Ring 2");
        c.setZip("10115");
        c.setCity("Berlin");
        c.setCountry("DE");
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));

        //read (and cached), changed and saved as a view does it
        Contact loaded = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS());
        assertEquals("Ring 2", loaded.__getStreet());
        Contact edit = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS());
        edit.setStreet("Neuer Weg 9");
        edit.setCity("Hamburg");
        assertTrue(edit.save(true));

        Contact again = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS());
        assertEquals("Neuer Weg 9", again.__getStreet(), "the saved street, not the cached old one");
        assertEquals("Hamburg", again.__getCity());
        assertEquals(1, n("SELECT COUNT(*) FROM gobd_audit_log WHERE action = 'MASTERDATA_CHANGED'"), "the plugin logged the change");

        m.stop(gobd);
    }
}
