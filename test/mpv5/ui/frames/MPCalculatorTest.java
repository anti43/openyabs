
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
public class MPCalculatorTest {

    public MPCalculatorTest() {
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
     * Test of instanceOf method, of class MPCalculator.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        MPCalculator expResult = null;
        MPCalculator result = MPCalculator.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}