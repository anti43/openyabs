
package mpv5.db.common;

import mpv5.db.objects.User;
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
public class DatabaseObjectLockTest {

    public DatabaseObjectLockTest() {
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
     * Test of aquire method, of class DatabaseObjectLock.
     */
    @Test
    public void testAquire() {
        System.out.println("aquire");
        DatabaseObjectLock instance = null;
        boolean expResult = false;
        boolean result = instance.aquire();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of release method, of class DatabaseObjectLock.
     */
    @Test
    public void testRelease() {
        System.out.println("release");
        DatabaseObjectLock instance = null;
        instance.release();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of releaseAllObjectsFor method, of class DatabaseObjectLock.
     */
    @Test
    public void testReleaseAllObjectsFor() {
        System.out.println("releaseAllObjectsFor");
        User user = null;
        DatabaseObjectLock.releaseAllObjectsFor(user);
        
        fail("The test case is a prototype.");
    }

}