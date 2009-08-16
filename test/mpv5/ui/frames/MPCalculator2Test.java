
package mpv5.ui.frames;

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
public class MPCalculator2Test {

    public MPCalculator2Test() {
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
     * Test of instanceOf method, of class MPCalculator2.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        MPCalculator2 expResult = null;
        MPCalculator2 result = MPCalculator2.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of dispose method, of class MPCalculator2.
     */
    @Test
    public void testDispose() {
        System.out.println("dispose");
        MPCalculator2 instance = null;
        instance.dispose();
        
        fail("The test case is a prototype.");
    }

}