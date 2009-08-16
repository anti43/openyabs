
package mpv5.webshopinterface;

import mpv5.utils.xml.XMLRpcClient;
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
public class WSConnectionClientTest {

    public WSConnectionClientTest() {
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
     * Test of getClient method, of class WSConnectionClient.
     */
    @Test
    public void testGetClient() {
        System.out.println("getClient");
        WSConnectionClient instance = null;
        XMLRpcClient expResult = null;
        XMLRpcClient result = instance.getClient();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of test method, of class WSConnectionClient.
     */
    @Test
    public void testTest() throws Exception {
        System.out.println("test");
        WSConnectionClient instance = null;
        String expResult = "";
        String result = instance.test();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}