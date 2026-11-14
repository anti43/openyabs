package mpv5.ui.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import org.junit.jupiter.api.Test;

/**
 * From a position to its product: the product id of the product column
 */
class PositionProductLinkTest {

    private static Product product(int id) {
        Product p = new Product();
        p.setIDS(id);
        return p;
    }

    @Test
    void productIdOfTheProductColumn() {
        assertEquals(7, SubItem.productIdOf(product(7)));
        //rows of product lists and deleted products hold the id only
        assertEquals(12, SubItem.productIdOf(Integer.valueOf(12)));
        assertEquals(12, SubItem.productIdOf(Long.valueOf(12)));
        //free text positions: no product
        assertEquals(0, SubItem.productIdOf(null));
        assertEquals(0, SubItem.productIdOf(""));
        assertEquals(0, SubItem.productIdOf("4711"));
        //"deleted meanwhile" marker of an invoice position
        assertEquals(0, SubItem.productIdOf(Integer.valueOf(-1)));
        assertEquals(0, SubItem.productIdOf(product(-1)));
    }

    @Test
    void productIdOfTheSelectedRow() {
        DefaultTableModel m = new DefaultTableModel(3, 17);
        m.setValueAt(product(5), 0, PositionProductLink.PRODUCT_COLUMN);
        m.setValueAt(Integer.valueOf(9), 2, PositionProductLink.PRODUCT_COLUMN);
        JTable t = new JTable(m);
        //columns moved as in the positions table: still the model column
        t.moveColumn(10, 3);
        assertEquals(5, PositionProductLink.productId(t, 0));
        assertEquals(0, PositionProductLink.productId(t, 1));
        assertEquals(9, PositionProductLink.productId(t, 2));
        assertEquals(0, PositionProductLink.productId(t, -1));
        assertEquals(0, PositionProductLink.productId(t, 3));
    }

    @Test
    void noProductNeedsNoDatabase() {
        assertEquals(PositionProductLink.State.NONE, PositionProductLink.state(0));
        assertEquals(PositionProductLink.State.NONE, PositionProductLink.state(-1));
    }
}
