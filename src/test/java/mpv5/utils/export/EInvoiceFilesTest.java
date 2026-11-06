package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.globals.Messages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mustangproject.Invoice;

/**
 * E-invoice files of the wizard "Combine into new invoice"
 */
class EInvoiceFilesTest {

    @TempDir
    Path tmp;

    private static Invoice invoice(Item item) {
        Map<String, String> c = ZugpferdFileTest.company();
        return ZugpferdFile.createInvoice(item, new SubItem[]{ZugpferdFileTest.line("a", "", "1", "Stk", "100", "19", "0")},
                ZugpferdFile.createSender(c::get), ZugpferdFileTest.buyer(), "ref", ZugpferdFile.PAYTERM_UNSET, null);
    }

    @Test
    void writesAValidXRechnung() {
        Item item = ZugpferdFileTest.invoice();
        File dir = tmp.resolve("rechnungen").toFile();
        File xml = new File(dir, "Rechnung R-2026-0042.xml");
        List<String> report = EInvoiceFiles.write(item, invoice(item), EInvoiceDelivery.XRECHNUNG, new ArrayList<String>(), xml);
        assertEquals(1, report.size());
        assertTrue(report.get(0).contains(Messages.EINVOICE_CREATED.toString()), report.get(0));
        assertTrue(xml.isFile(), Arrays.toString(dir.list()));
    }

    @Test
    void incompleteEInvoiceIsNotWritten() {
        Item item = ZugpferdFileTest.invoice();
        File dir = tmp.resolve("rechnungen").toFile();
        List<String> report = EInvoiceFiles.write(item, invoice(item), EInvoiceDelivery.XRECHNUNG,
                new ArrayList<String>(Arrays.asList("USt-IdNr. fehlt")), new File(dir, "R.xml"));
        assertEquals(1, report.size());
        assertTrue(report.get(0).contains(Messages.EINVOICE_NOT_CREATED.toString()), report.get(0));
        assertTrue(report.get(0).contains("USt-IdNr. fehlt"), report.get(0));
        assertFalse(dir.exists() && dir.list().length > 0, "nothing written");
    }
}
