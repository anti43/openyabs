
package mpv5.ui.misc;

import mpv5.ui.misc.JCalc.FunctionListener;
import mpv5.ui.misc.JCalc.NumberListener;
import mpv5.ui.misc.JCalc.OperationListener;
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
public class JCalcTest {

    public JCalcTest() {
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
     * Test of getNl method, of class JCalc.
     */
    @Test
    public void testGetNl() {
        System.out.println("getNl");
        JCalc instance = null;
        NumberListener expResult = null;
        NumberListener result = instance.getNl();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOl method, of class JCalc.
     */
    @Test
    public void testGetOl() {
        System.out.println("getOl");
        JCalc instance = null;
        OperationListener expResult = null;
        OperationListener result = instance.getOl();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFl method, of class JCalc.
     */
    @Test
    public void testGetFl() {
        System.out.println("getFl");
        JCalc instance = null;
        FunctionListener expResult = null;
        FunctionListener result = instance.getFl();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}