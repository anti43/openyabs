package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;

/**
 * A cell editor of the positions opened without a mouse click (e.g. by Ctrl+S
 * on the selected VAT cell: the table starts editing on keys it does not use)
 * starts empty; saving stops the editing and must not turn the VAT, the price
 * or the unit of the position into 0 / empty.
 */
class ItemPositionsTableEditorKeepsValueTest {

    private static ItemPositionsTable positions() {
        ItemPositionsTable table = new ItemPositionsTable();
        table.setDefaultPositions();
        MPTableModel model = table.getModel();
        model.setValueAt("Position 1", 0, 4);
        model.setValueAt(new BigDecimal("100.00"), 0, 5);
        model.setValueAt(new BigDecimal("19.00"), 0, 6);
        model.setValueAt("Stunden", 0, 3);
        return table;
    }

    /**
     * Opens the editor of the model column as a key would (no mouse event)
     */
    private static JTextComponent edit(ItemPositionsTable positions, int column) {
        JTable t = positions.getTable();
        assertTrue(t.editCellAt(0, t.convertColumnIndexToView(column)), "editor of column " + column);
        return (JTextComponent) t.getEditorComponent();
    }

    private static BigDecimal number(ItemPositionsTable positions, int column) {
        return new BigDecimal(String.valueOf(positions.getModel().getValueAt(0, column)));
    }

    @Test
    void editorOpenedWithoutTypingKeepsTheValues() {
        ItemPositionsTable positions = positions();
        //the VAT cell selected, Ctrl+S: the editor is open (empty), saving stops it
        edit(positions, 6);
        positions.prepareSave();
        assertEquals(0, new BigDecimal("19.00").compareTo(number(positions, 6)), "VAT kept");

        edit(positions, 5);
        positions.prepareSave();
        assertEquals(0, new BigDecimal("100.00").compareTo(number(positions, 5)), "price kept");

        edit(positions, 3);
        positions.prepareSave();
        assertEquals("Stunden", positions.getModel().getValueAt(0, 3), "unit kept");
    }

    @Test
    void typedValueIsTaken() {
        ItemPositionsTable positions = positions();
        edit(positions, 6).setText("7");
        positions.prepareSave();
        assertEquals(0, new BigDecimal("7").compareTo(number(positions, 6)), "typed VAT");

        edit(positions, 3).setText("Stück");
        positions.prepareSave();
        assertEquals("Stück", positions.getModel().getValueAt(0, 3), "typed unit");
    }

    @Test
    void clearedValueIsZero() {
        ItemPositionsTable positions = positions();
        JTextComponent f = edit(positions, 6);
        f.setText("1");
        f.setText("");
        positions.prepareSave();
        assertEquals(0, BigDecimal.ZERO.compareTo(number(positions, 6)), "VAT deleted by the user");
    }
}
