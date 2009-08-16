
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
public class JobTest {

    public JobTest() {
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
     * Test of doInBackground method, of class Job.
     */
    @Test
    public void testDoInBackground() throws Exception {
        System.out.println("doInBackground");
        Job instance = null;
        Object expResult = null;
        Object result = instance.doInBackground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of done method, of class Job.
     */
    @Test
    public void testDone() {
        System.out.println("done");
        Job instance = null;
        instance.done();
        
        fail("The test case is a prototype.");
    }

}