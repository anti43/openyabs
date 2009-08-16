
package mpv5.webshopinterface;

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
public class WSDaemonJobTest {

    public WSDaemonJobTest() {
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
     * Test of isOneTimeJob method, of class WSDaemonJob.
     */
    @Test
    public void testIsOneTimeJob() {
        System.out.println("isOneTimeJob");
        WSDaemonJob instance = new WSDaemonJobImpl();
        boolean expResult = false;
        boolean result = instance.isOneTimeJob();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDone method, of class WSDaemonJob.
     */
    @Test
    public void testIsDone() {
        System.out.println("isDone");
        WSDaemonJob instance = new WSDaemonJobImpl();
        boolean expResult = false;
        boolean result = instance.isDone();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of work method, of class WSDaemonJob.
     */
    @Test
    public void testWork() {
        System.out.println("work");
        WSConnectionClient client = null;
        WSDaemonJob instance = new WSDaemonJobImpl();
        instance.work(client);
        
        fail("The test case is a prototype.");
    }

    public class WSDaemonJobImpl  implements WSDaemonJob {

        public boolean isOneTimeJob() {
            return false;
        }

        public boolean isDone() {
            return false;
        }

        public void work(WSConnectionClient client) {
        }
    }

}