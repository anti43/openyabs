
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
public class BackGroundJobTest {

    public BackGroundJobTest() {
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
     * Test of doInBackground method, of class BackGroundJob.
     */
    @Test
    public void testDoInBackground() {
        System.out.println("doInBackground");
        BackGroundJob instance = null;
        Void expResult = null;
        Void result = instance.doInBackground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of done method, of class BackGroundJob.
     */
    @Test
    public void testDone() {
        System.out.println("done");
        BackGroundJob instance = null;
        instance.done();
        
        fail("The test case is a prototype.");
    }

}