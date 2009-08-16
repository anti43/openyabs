
package mpv5.utils.renderer;

import java.awt.Component;
import javax.swing.JTable;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author anti
 */
public class CellRendererWithMPComboBoxTest {

    public CellRendererWithMPComboBoxTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setRendererTo method, of class CellRendererWithMPComboBox.
     */
    @Test
    public void testSetRendererTo() {
        System.out.println("setRendererTo");
        int column = 0;
        MPCBSelectionChangeReceiver r = null;
        CellRendererWithMPComboBox instance = null;
        instance.setRendererTo(column, r);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableCellRendererComponent method, of class CellRendererWithMPComboBox.
     */
    @Test
    public void testGetTableCellRendererComponent() {
        System.out.println("getTableCellRendererComponent");
        JTable table = null;
        Object value = null;
        boolean isSelected = false;
        boolean hasFocus = false;
        int row = 0;
        int column = 0;
        CellRendererWithMPComboBox instance = null;
        Component expResult = null;
        Component result = instance.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}