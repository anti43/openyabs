
package mpv5.webshopinterface.wsdjobs;

import mpv5.webshopinterface.WSConnectionClient;
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
public class newContactsJobTest {

    public newContactsJobTest() {
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
     * Test of isOneTimeJob method, of class newContactsJob.
     */
    @Test
    public void testIsOneTimeJob() {
        System.out.println("isOneTimeJob");
        newContactsJob instance = null;
        boolean expResult = false;
        boolean result = instance.isOneTimeJob();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDone method, of class newContactsJob.
     */
    @Test
    public void testIsDone() {
        System.out.println("isDone");
        newContactsJob instance = null;
        boolean expResult = false;
        boolean result = instance.isDone();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of work method, of class newContactsJob.
     */
    @Test
    public void testWork() {
        System.out.println("work");
        WSConnectionClient client = null;
        newContactsJob instance = null;
        instance.work(client);
        
        fail("The test case is a prototype.");
    }

}