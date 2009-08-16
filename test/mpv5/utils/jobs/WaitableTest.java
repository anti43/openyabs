
package mpv5.utils.jobs;

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
public class WaitableTest {

    public WaitableTest() {
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
     * Test of waitFor method, of class Waitable.
     */
    @Test
    public void testWaitFor() {
        System.out.println("waitFor");
        Waitable instance = new WaitableImpl();
        Exception expResult = null;
        Exception result = instance.waitFor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class WaitableImpl  implements Waitable {

        public Exception waitFor() {
            return null;
        }
    }

}