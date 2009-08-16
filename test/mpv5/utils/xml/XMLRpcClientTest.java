
package mpv5.utils.xml;

import java.util.HashMap;
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
public class XMLRpcClientTest {

    public XMLRpcClientTest() {
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
     * Test of invokeGetCommand method, of class XMLRpcClient.
     */
    @Test
    public void testInvokeGetCommand_3args() throws Exception {
        System.out.println("invokeGetCommand");
        String commandName = "";
        Object[] params = null;
        Object expectedReturnType = null;
        XMLRpcClient instance = null;
        Object expResult = null;
        Object result = instance.invokeGetCommand(commandName, params, expectedReturnType);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of invokeGetCommand method, of class XMLRpcClient.
     */
    @Test
    public void testInvokeGetCommand_String_ObjectArr() throws Exception {
        System.out.println("invokeGetCommand");
        String commandName = "";
        Object[] params = null;
        XMLRpcClient instance = null;
        HashMap expResult = null;
        HashMap result = instance.invokeGetCommand(commandName, params);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of invokeSetCommand method, of class XMLRpcClient.
     */
    @Test
    public void testInvokeSetCommand() throws Exception {
        System.out.println("invokeSetCommand");
        String commandName = "";
        Object[] params = null;
        XMLRpcClient instance = null;
        boolean expResult = false;
        boolean result = instance.invokeSetCommand(commandName, params);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}