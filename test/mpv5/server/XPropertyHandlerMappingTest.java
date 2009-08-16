
package mpv5.server;

import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
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
public class XPropertyHandlerMappingTest {

    public XPropertyHandlerMappingTest() {
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
     * Test of newXmlRpcHandlerMapping method, of class XPropertyHandlerMapping.
     */
    @Test
    public void testNewXmlRpcHandlerMapping() throws Exception {
        System.out.println("newXmlRpcHandlerMapping");
        XPropertyHandlerMapping instance = new XPropertyHandlerMapping();
        XmlRpcHandlerMapping expResult = null;
        XmlRpcHandlerMapping result = instance.newXmlRpcHandlerMapping();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}