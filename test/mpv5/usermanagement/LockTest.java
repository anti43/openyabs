
package mpv5.usermanagement;

import java.awt.Component;
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
public class LockTest {

    public LockTest() {
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
     * Test of lock method, of class Lock.
     */
    @Test
    public void testLock() {
        System.out.println("lock");
        Component comp = null;
        Lock.lock(comp);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of unlock method, of class Lock.
     */
    @Test
    public void testUnlock() {
        System.out.println("unlock");
        Component frame = null;
        Lock.unlock(frame);
        
        fail("The test case is a prototype.");
    }

}