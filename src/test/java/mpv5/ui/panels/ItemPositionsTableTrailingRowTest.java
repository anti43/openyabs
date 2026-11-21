package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;

/**
 * Entering a value into the last free row of the subitems table automatically
 * appends a new empty row so there is always a free row at the bottom.
 */
class ItemPositionsTableTrailingRowTest {

    @Test
    void enteringTextInLastRowAppendsNewEmptyRow() {
        ItemPositionsTable table = new ItemPositionsTable();
        table.setDefaultPositions();
        MPTableModel model = table.getModel();
        assertEquals(6, model.getRowCount());

        // Editing an earlier row does not append a row
        model.setValueAt("First position", 0, 4);
        assertEquals(6, model.getRowCount());

        // Editing the last row appends a new empty row
        model.setValueAt("Last position", 5, 4);
        assertEquals(7, model.getRowCount());
        Object newDesc = model.getValueAt(6, 4);
        assertTrue(newDesc == null || newDesc.toString().isEmpty(), "New trailing row must have empty description");
        assertEquals(7, model.getValueAt(6, 1), "Auto-count column increments");

        // Entering text into the newly appended last row adds another one
        model.setValueAt("Another position", 6, 4);
        assertEquals(8, model.getRowCount());
    }

    @Test
    void enteringPriceUnitOrQuantityInLastRowAppendsNewEmptyRow() {
        ItemPositionsTable table = new ItemPositionsTable();
        table.setDefaultPositions();
        MPTableModel model = table.getModel();

        model.setValueAt(new BigDecimal("49.90"), 5, 5);
        assertEquals(7, model.getRowCount(), "Non-zero price in last row appends empty row");

        model.setValueAt("Stunden", 6, 3);
        assertEquals(8, model.getRowCount(), "Custom unit in last row appends empty row");

        model.setValueAt(new BigDecimal("3"), 7, 2);
        assertEquals(9, model.getRowCount(), "Non-default quantity in last row appends empty row");
    }

    @Test
    void emptyValuesTaxOverrideAndReadOnlyDoNotAppendRows() {
        ItemPositionsTable table = new ItemPositionsTable();
        table.setDefaultPositions();
        MPTableModel model = table.getModel();

        // Blank text or zero price in last row should not add rows
        model.setValueAt("", 5, 4);
        model.setValueAt("   ", 5, 4);
        model.setValueAt(BigDecimal.ZERO, 5, 5);
        assertEquals(6, model.getRowCount());

        // Applying a customer tax rate override updates column 6 without adding rows
        table.applyCustomerTaxRate(new BigDecimal("7.00"));
        assertEquals(6, model.getRowCount());

        // Once a value IS entered in the last row, the newly added row inherits the customer tax rate
        model.setValueAt("Taxed item", 5, 4);
        assertEquals(7, model.getRowCount());
        assertEquals(0, new BigDecimal("7.00").compareTo(new BigDecimal(model.getValueAt(6, 6).toString())));

        // When read-only, updating the last row does not append a row
        table.setEditable(false);
        model.setValueAt("Ignored while read-only", 6, 4);
        assertEquals(7, model.getRowCount());
    }

    @Test
    void syncTrailingEmptyRowDropsEmptyRowsWhenReadOnlyAndRestoresOnUnlock() {
        mpv5.db.objects.SubItem existing = new mpv5.db.objects.SubItem();
        existing.setIDS(42);
        existing.setDescription("Beratung");
        existing.setQuantityvalue(BigDecimal.ONE);
        existing.setExternalvalue(new BigDecimal("100.00"));
        existing.setTaxpercentvalue(new BigDecimal("19.00"));

        ItemPositionsTable table = new ItemPositionsTable();
        table.setPositions(new mpv5.db.objects.SubItem[]{existing});
        assertEquals(2, table.getModel().getRowCount(), "Editable table shows saved row plus trailing empty row");

        // Locking drops the trailing unsaved empty row
        table.setEditable(false);
        table.syncTrailingEmptyRow();
        assertEquals(1, table.getModel().getRowCount(), "Locked table drops trailing empty row");
        assertEquals("Beratung", table.getModel().getValueAt(0, 4));

        // Reloading positions while already read-only does not add an empty row
        table.setPositions(new mpv5.db.objects.SubItem[]{existing});
        assertEquals(1, table.getModel().getRowCount(), "Read-only setPositions must not add empty row");

        // Unlocking (e.g. Copy) restores a trailing empty row
        table.setEditable(true);
        table.syncTrailingEmptyRow();
        assertEquals(2, table.getModel().getRowCount(), "Unlocking restores a trailing empty row");
    }
}
