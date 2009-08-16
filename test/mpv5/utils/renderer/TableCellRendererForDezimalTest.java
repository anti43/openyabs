
package mpv5.utils.renderer;

import java.awt.Component;
import javax.swing.JTable;
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
public class TableCellRendererForDezimalTest {

    public TableCellRendererForDezimalTest() {
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
     * Test of setRendererTo method, of class TableCellRendererForDezimal.
     */
    @Test
    public void testSetRendererTo() {
        System.out.println("setRendererTo");
        int column = 0;
        TableCellRendererForDezimal instance = null;
        instance.setRendererTo(column);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class TableCellRendererForDezimal.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        Object value = null;
        TableCellRendererForDezimal instance = null;
        instance.setValue(value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableCellRendererComponent method, of class TableCellRendererForDezimal.
     */
    @Test
    public void testGetTableCellRendererComponent() {
        System.out.println("getTableCellRendererComponent");
        JTable table = null;
        Object value = null;
        boolean isSelected = false;
        boolean hasFocus = false;
        int row = 0;
        int col = 0;
        TableCellRendererForDezimal instance = null;
        Component expResult = null;
        Component result = instance.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}