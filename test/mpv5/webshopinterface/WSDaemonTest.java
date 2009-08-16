
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
public class WSDaemonTest {

    public WSDaemonTest() {
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
     * Test of getWebShopID method, of class WSDaemon.
     */
    @Test
    public void testGetWebShopID() {
        System.out.println("getWebShopID");
        WSDaemon instance = null;
        int expResult = 0;
        int result = instance.getWebShopID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addJob method, of class WSDaemon.
     */
    @Test
    public void testAddJob() {
        System.out.println("addJob");
        WSDaemonJob job = null;
        WSDaemon instance = null;
        instance.addJob(job);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of kill method, of class WSDaemon.
     */
    @Test
    public void testKill() {
        System.out.println("kill");
        WSDaemon instance = null;
        instance.kill();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class WSDaemon.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        WSDaemon instance = null;
        instance.run();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWaitTime method, of class WSDaemon.
     */
    @Test
    public void testGetWaitTime() {
        System.out.println("getWaitTime");
        WSDaemon instance = null;
        long expResult = 0L;
        long result = instance.getWaitTime();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWaitTime method, of class WSDaemon.
     */
    @Test
    public void testSetWaitTime() {
        System.out.println("setWaitTime");
        long waitTime = 0L;
        WSDaemon instance = null;
        instance.setWaitTime(waitTime);
        
        fail("The test case is a prototype.");
    }

}