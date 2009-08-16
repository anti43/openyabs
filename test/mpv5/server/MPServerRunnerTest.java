
package mpv5.server;

import java.net.Socket;
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
public class MPServerRunnerTest {

    public MPServerRunnerTest() {
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
     * Test of addJob method, of class MPServerRunner.
     */
    @Test
    public void testAddJob() {
        System.out.println("addJob");
        String name = "";
        MPServerJob job = null;
        MPServerRunner.addJob(name, job);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class MPServerRunner.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        MPServerRunner instance = null;
        instance.run();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSocket method, of class MPServerRunner.
     */
    @Test
    public void testGetSocket() {
        System.out.println("getSocket");
        MPServerRunner instance = null;
        Socket expResult = null;
        Socket result = instance.getSocket();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}