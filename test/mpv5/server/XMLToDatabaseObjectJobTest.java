
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
public class XMLToDatabaseObjectJobTest {

    public XMLToDatabaseObjectJobTest() {
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
     * Test of waitFor method, of class XMLToDatabaseObjectJob.
     */
    @Test
    public void testWaitFor() {
        System.out.println("waitFor");
        XMLToDatabaseObjectJob instance = new XMLToDatabaseObjectJob();
        Exception expResult = null;
        Exception result = instance.waitFor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set method, of class XMLToDatabaseObjectJob.
     */
    @Test
    public void testSet() throws Exception {
        System.out.println("set");
        Object object = null;
        Exception e = null;
        XMLToDatabaseObjectJob instance = new XMLToDatabaseObjectJob();
        instance.set(object, e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class XMLToDatabaseObjectJob.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        XMLToDatabaseObjectJob instance = new XMLToDatabaseObjectJob();
        instance.start();
        
        fail("The test case is a prototype.");
    }

}