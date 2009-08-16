
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
public class WaiterTest {

    public WaiterTest() {
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
     * Test of set method, of class Waiter.
     */
    @Test
    public void testSet() throws Exception {
        System.out.println("set");
        Object object = null;
        Exception exception = null;
        Waiter instance = new WaiterImpl();
        instance.set(object, exception);
        
        fail("The test case is a prototype.");
    }

    public class WaiterImpl  implements Waiter {

        public void set(Object object, Exception exception) throws Exception {
        }
    }

}