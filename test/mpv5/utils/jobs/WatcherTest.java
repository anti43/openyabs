
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
public class WatcherTest {

    public WatcherTest() {
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
     * Test of run method, of class Watcher.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        Watcher instance = null;
        instance.run();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInterval method, of class Watcher.
     */
    @Test
    public void testGetInterval() {
        System.out.println("getInterval");
        Watcher instance = null;
        int expResult = 0;
        int result = instance.getInterval();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInterval method, of class Watcher.
     */
    @Test
    public void testSetInterval() {
        System.out.println("setInterval");
        int interval = 0;
        Watcher instance = null;
        instance.setInterval(interval);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWatched method, of class Watcher.
     */
    @Test
    public void testGetWatched() {
        System.out.println("getWatched");
        Watcher instance = null;
        Object expResult = null;
        Object result = instance.getWatched();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCaller method, of class Watcher.
     */
    @Test
    public void testGetCaller() {
        System.out.println("getCaller");
        Watcher instance = null;
        Object expResult = null;
        Object result = instance.getCaller();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}