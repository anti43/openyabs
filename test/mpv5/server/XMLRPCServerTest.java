
package mpv5.server;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlrpc.webserver.WebServer;
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
public class XMLRPCServerTest {

    public XMLRPCServerTest() {
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
     * Test of getPort method, of class XMLRPCServer.
     */
    @Test
    public void testGetPort() {
        System.out.println("getPort");
        int expResult = 0;
        int result = XMLRPCServer.getPort();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWebServer method, of class XMLRPCServer.
     */
    @Test
    public void testGetWebServer() {
        System.out.println("getWebServer");
        XMLRPCServer instance = null;
        try {
            instance = new XMLRPCServer();
        } catch (Exception ex) {
            Logger.getLogger(XMLRPCServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        WebServer expResult = null;
        WebServer result = instance.getWebServer();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}