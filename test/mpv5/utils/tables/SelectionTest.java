
package mpv5.utils.tables;

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
public class SelectionTest {

    public SelectionTest() {
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
     * Test of getId method, of class Selection.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Selection instance = null;
        int expResult = 0;
        int result = instance.getId();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRowData method, of class Selection.
     */
    @Test
    public void testGetRowData() {
        System.out.println("getRowData");
        Selection instance = null;
        Object[] expResult = null;
        Object[] result = instance.getRowData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeRow method, of class Selection.
     */
    @Test
    public void testRemoveRow() {
        System.out.println("removeRow");
        Selection instance = null;
        instance.removeRow();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowHasData method, of class Selection.
     */
    @Test
    public void testRowHasData() {
        System.out.println("rowHasData");
        int testcol = 0;
        Selection instance = null;
        boolean expResult = false;
        boolean result = instance.rowHasData(testcol);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkID method, of class Selection.
     */
    @Test
    public void testCheckID() {
        System.out.println("checkID");
        Selection instance = null;
        boolean expResult = false;
        boolean result = instance.checkID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}