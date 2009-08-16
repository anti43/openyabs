
package mpv5.utils.renderer;

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
public class LazyCellEditorTest {

    public LazyCellEditorTest() {
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
     * Test of stopCellEditing method, of class LazyCellEditor.
     */
    @Test
    public void testStopCellEditing() {
        System.out.println("stopCellEditing");
        LazyCellEditor instance = null;
        boolean expResult = false;
        boolean result = instance.stopCellEditing();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopCellEditingSilent method, of class LazyCellEditor.
     */
    @Test
    public void testStopCellEditingSilent() {
        System.out.println("stopCellEditingSilent");
        LazyCellEditor instance = null;
        instance.stopCellEditingSilent();
        
        fail("The test case is a prototype.");
    }

}