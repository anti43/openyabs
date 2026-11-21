package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.ui.toolbars.RecordActions;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies that all invoice export actions save unsaved changes first, abort
 * export if saving fails or is rejected, and skip saving when the invoice is
 * unchanged, read-only, or GoBD-locked.
 */
class InvoiceSaveBeforeExportTest {

    @TempDir
    Path tempDir;

    /**
     * Minimal in-memory Item subclass that records save() calls without touching
     * the database.
     */
    private static class StubItem extends Item {

        private static final long serialVersionUID = 1L;
        int nextId = 100;
        boolean saveResult = true;
        final List<String> events;

        StubItem(List<String> events, int initialId) {
            this.events = events;
            setContext(mpv5.db.common.Context.getInvoice());
            setIDS(initialId);
            setInttype(Item.TYPE_INVOICE);
            setCname("RE-2026-001");
            setCnumber("RE-2026-001");
        }

        @Override
        public boolean getPanelData(DataPanel view) {
            return view.collectData();
        }

        @Override
        public boolean save() {
            events.add("save");
            if (!saveResult) {
                return false;
            }
            if (!isExisting()) {
                setIDS(nextId++);
            }
            return true;
        }
    }

    /**
     * Minimal ExportablePanel backed by StubItem so every RecordActions export
     * entry point can be verified deterministically.
     */
    private static class StubInvoicePanel extends JPanel implements DataPanel, ExportablePanel {

        private static final long serialVersionUID = 1L;
        StubItem owner;
        boolean dirty;
        boolean collectOk = true;
        boolean rejectBeforeSave;
        final List<String> events;

        StubInvoicePanel(List<String> events, StubItem owner) {
            this.events = events;
            this.owner = owner;
        }

        @Override
        public boolean collectData() {
            events.add("collect");
            return collectOk;
        }

        @Override
        public DatabaseObject getDataOwner() {
            return owner;
        }

        @Override
        public void setDataOwner(DatabaseObject object, boolean populateData) {
            this.owner = (StubItem) object;
            this.dirty = false;
            events.add("setDataOwner");
        }

        @Override
        public void refresh() {
        }

        @Override
        public void exposeData() {
        }

        @Override
        public void paste(DatabaseObject... dbo) {
        }

        @Override
        public void showRequiredFields() {
            events.add("required");
        }

        @Override
        public void showSearchBar(boolean show) {
        }

        @Override
        public void actionAfterSave() {
            events.add("afterSave");
        }

        @Override
        public void actionAfterCreate() {
            events.add("afterCreate");
        }

        @Override
        public void actionBeforeCreate() {
            events.add("beforeCreate");
        }

        @Override
        public void actionBeforeSave() throws ChangeNotApprovedException {
            events.add("beforeSave");
            if (rejectBeforeSave) {
                throw new ChangeNotApprovedException(owner);
            }
        }

        @Override
        public boolean hasUnsavedChanges() {
            return dirty;
        }

        @Override
        public void mail() {
            if (!RecordActions.saveBeforeExport(this)) {
                return;
            }
            events.add("mail");
        }

        @Override
        public void print() {
            if (!RecordActions.saveBeforeExport(this)) {
                return;
            }
            events.add("print");
        }

        @Override
        public void pdf() {
            if (!RecordActions.saveBeforeExport(this)) {
                return;
            }
            events.add("pdf");
        }

        @Override
        public void odt() {
            if (!RecordActions.saveBeforeExport(this)) {
                return;
            }
            events.add("odt");
        }

        @Override
        public void preview() {
            if (!RecordActions.saveBeforeExport(this)) {
                return;
            }
            events.add("preview");
        }
    }

    @Test
    void dirtyExistingInvoiceSavesOnceBeforeEveryExportAction() {
        for (String action : Arrays.asList("pdf", "odt", "preview", "print", "mail")) {
            List<String> events = new ArrayList<>();
            StubItem item = new StubItem(events, 42);
            StubInvoicePanel panel = new StubInvoicePanel(events, item);
            panel.dirty = true;

            invokeAction(action, panel);

            assertEquals(Arrays.asList("beforeSave", "collect", "save", "afterSave", "setDataOwner", action),
                    events, "Action " + action + " must save changes once before exporting");
            assertFalse(panel.hasUnsavedChanges(), "Panel must be clean after save-before-export");
        }
    }

    @Test
    void directPanelExportCallAlsoSavesDirtyInvoiceFirst() {
        List<String> events = new ArrayList<>();
        StubItem item = new StubItem(events, 42);
        StubInvoicePanel panel = new StubInvoicePanel(events, item);
        panel.dirty = true;

        panel.pdf();

        assertEquals(Arrays.asList("beforeSave", "collect", "save", "afterSave", "setDataOwner", "pdf"), events);
    }

    @Test
    void unchangedExistingInvoiceDoesNotResaveOnExport() {
        for (String action : Arrays.asList("pdf", "odt", "preview", "print", "mail")) {
            List<String> events = new ArrayList<>();
            StubItem item = new StubItem(events, 42);
            item.setIntstatus(Item.STATUS_PAID);
            StubInvoicePanel panel = new StubInvoicePanel(events, item);
            panel.dirty = false;
            // Even if actionBeforeSave would reject (e.g. PAID dialog declined),
            // an unchanged invoice must not trigger save at all.
            panel.rejectBeforeSave = true;

            invokeAction(action, panel);

            assertEquals(Collections.singletonList(action), events,
                    "Unchanged invoice must export directly without saving on " + action);
        }
    }

    @Test
    void brandNewInvoiceIsSavedBeforeExport() {
        for (String action : Arrays.asList("pdf", "odt", "preview", "print", "mail")) {
            List<String> events = new ArrayList<>();
            StubItem item = new StubItem(events, -1);
            StubInvoicePanel panel = new StubInvoicePanel(events, item);
            assertFalse(item.isExisting());

            invokeAction(action, panel);

            assertTrue(item.isExisting(), "New invoice must be saved before " + action);
            assertEquals(Arrays.asList("beforeSave", "collect", "save", "afterSave", "setDataOwner", action), events);
        }
    }

    @Test
    void exportAbortsWhenValidationFailsOrUserCancelsSave() {
        // 1. Missing required fields (collectData returns false)
        List<String> events1 = new ArrayList<>();
        StubItem item1 = new StubItem(events1, 42);
        StubInvoicePanel panel1 = new StubInvoicePanel(events1, item1);
        panel1.dirty = true;
        panel1.collectOk = false;

        RecordActions.pdf(panel1);
        assertEquals(Arrays.asList("beforeSave", "collect", "required"), events1);

        // 2. User cancels in actionBeforeSave (ChangeNotApprovedException)
        List<String> events2 = new ArrayList<>();
        StubItem item2 = new StubItem(events2, 42);
        StubInvoicePanel panel2 = new StubInvoicePanel(events2, item2);
        panel2.dirty = true;
        panel2.rejectBeforeSave = true;

        RecordActions.odt(panel2);
        assertEquals(Collections.singletonList("beforeSave"), events2);

        // 3. Database save fails
        List<String> events3 = new ArrayList<>();
        StubItem item3 = new StubItem(events3, -1);
        item3.saveResult = false;
        StubInvoicePanel panel3 = new StubInvoicePanel(events3, item3);

        RecordActions.mail(panel3);
        assertEquals(Arrays.asList("beforeSave", "collect", "save", "required"), events3);
        assertFalse(item3.isExisting());
    }

    @Test
    void contentLockedReadOnlyOrArchivedInvoiceNeverSavesOnExport() throws Exception {
        // Content-locked (GoBD finalized): must NOT save, but still allows export (e.g. mail/delivery note)
        List<String> events1 = new ArrayList<>();
        StubItem item1 = new StubItem(events1, 42);
        StubInvoicePanel panel1 = new StubInvoicePanel(events1, item1);
        panel1.dirty = true;
        panel1.setContentLocked(true);

        assertTrue(RecordActions.saveBeforeExport(panel1));
        assertTrue(events1.isEmpty(), "Content-locked invoice must never invoke save()");

        // Read-only record: must NOT save
        List<String> events2 = new ArrayList<>();
        StubItem item2 = new StubItem(events2, 42);
        item2.ReadOnly(true);
        StubInvoicePanel panel2 = new StubInvoicePanel(events2, item2);
        panel2.dirty = true;

        assertTrue(RecordActions.saveBeforeExport(panel2));
        assertTrue(events2.isEmpty(), "Read-only invoice must never invoke save()");

        // Archived locked document: must NOT save
        List<String> events3 = new ArrayList<>();
        StubItem item3 = new StubItem(events3, 42);
        StubInvoicePanel panel3 = new StubInvoicePanel(events3, item3);
        panel3.dirty = true;
        panel3.setContentLocked(true);
        File archived = Files.writeString(tempDir.resolve("RE-2026-001.pdf"), "pdf").toFile();
        panel3.setLockedDocument(archived);

        assertTrue(RecordActions.saveBeforeExport(panel3));
        assertTrue(events3.isEmpty(), "Archived locked invoice must never invoke save()");
    }

    @Test
    void pendingCopySavesAsCopyBeforeExport() {
        List<String> events = new ArrayList<>();
        StubItem item = new StubItem(events, 42);
        StubInvoicePanel panel = new StubInvoicePanel(events, item);

        assertTrue(RecordActions.createCopy(panel));
        assertTrue(RecordActions.isPendingCopy(panel));
        assertFalse(item.isExisting());
        events.clear();

        RecordActions.pdf(panel);

        assertFalse(RecordActions.isPendingCopy(panel), "Pending copy flag must be cleared after save");
        assertTrue(item.isExisting(), "Copy must have received a new database ID");
        assertEquals(100, item.__getIDS());
        assertEquals(Arrays.asList("collect", "save", "afterCreate", "setDataOwner", "pdf"), events);
    }

    @Test
    void snapshotNormalizesDatesDecimalsAndIgnoresTrailingEmptyRows() {
        // Date normalization ignores millisecond differences on the same calendar day
        Calendar c1 = Calendar.getInstance();
        c1.set(2026, Calendar.NOVEMBER, 21, 9, 15, 0);
        c1.set(Calendar.MILLISECOND, 10);
        Calendar c2 = Calendar.getInstance();
        c2.set(2026, Calendar.NOVEMBER, 21, 18, 45, 59);
        c2.set(Calendar.MILLISECOND, 999);
        Calendar c3 = Calendar.getInstance();
        c3.set(2026, Calendar.NOVEMBER, 22, 9, 15, 0);

        LabeledDateChooser d1 = new LabeledDateChooser();
        d1.setDate(c1.getTime());
        String snapDay1 = ItemPanelSnapshot.date(d1);
        d1.setDate(c2.getTime());
        assertEquals(snapDay1, ItemPanelSnapshot.date(d1), "Same calendar day must produce identical snapshot");
        d1.setDate(c3.getTime());
        assertNotEquals(snapDay1, ItemPanelSnapshot.date(d1), "Different calendar day must change snapshot");

        // Positions snapshot: trailing empty rows and decimal formatting do not cause false positives
        SubItem s1 = new SubItem();
        s1.setIDS(10);
        s1.setCountvalue(BigDecimal.ONE);
        s1.setQuantityvalue(new BigDecimal("2.00"));
        s1.setMeasure("Stk");
        s1.setDescription("Consulting");
        s1.setExternalvalue(new BigDecimal("100.00"));
        s1.setTaxpercentvalue(new BigDecimal("19.0"));
        s1.setDiscount(BigDecimal.ZERO);

        MPTableModel m1 = SubItem.toModel(new SubItem[]{s1});
        String baseSnap = ItemPanelSnapshot.positions(m1);

        // Appending an empty trailing row does not change the persisted snapshot
        m1.addRow(1);
        assertEquals(baseSnap, ItemPanelSnapshot.positions(m1), "Empty trailing row must not mark invoice dirty");

        // Equivalent decimal formatting ("2" vs "2.00") does not change snapshot
        m1.setValueAt(new BigDecimal("2"), 0, 2);
        assertEquals(baseSnap, ItemPanelSnapshot.positions(m1), "Equivalent BigDecimal scale must not mark invoice dirty");

        // Changing description, price, quantity, tax, or discount DOES change snapshot
        m1.setValueAt("Consulting updated", 0, 4);
        assertNotEquals(baseSnap, ItemPanelSnapshot.positions(m1), "Editing description must mark invoice dirty");

        m1.setValueAt("Consulting", 0, 4);
        assertEquals(baseSnap, ItemPanelSnapshot.positions(m1));

        m1.setValueAt(new BigDecimal("125.00"), 0, 5);
        assertNotEquals(baseSnap, ItemPanelSnapshot.positions(m1), "Editing unit price must mark invoice dirty");

        m1.setValueAt(new BigDecimal("100.00"), 0, 5);
        m1.setValueAt(new BigDecimal("5"), 0, 15);
        assertNotEquals(baseSnap, ItemPanelSnapshot.positions(m1), "Editing discount must mark invoice dirty");
    }

    private static void invokeAction(String action, StubInvoicePanel panel) {
        switch (action) {
            case "pdf":
                RecordActions.pdf(panel);
                break;
            case "odt":
                RecordActions.odt(panel);
                break;
            case "preview":
                RecordActions.preview(panel);
                break;
            case "print":
                RecordActions.print(panel);
                break;
            case "mail":
                RecordActions.mail(panel);
                break;
            default:
                throw new IllegalArgumentException(action);
        }
    }
}
