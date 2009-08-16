
package mpv5.server;

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
public class MPServerTest {

    public MPServerTest() {
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
     * Test of stopAllInstances method, of class MPServer.
     */
    @Test
    public void testStopAllInstances() {
        System.out.println("stopAllInstances");
        MPServer.stopAllInstances();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class MPServer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        MPServer instance = new MPServer();
        instance.run();
        
        fail("The test case is a prototype.");
    }

}