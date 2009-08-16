
package enoa.connection;

import ag.ion.bion.officelayer.desktop.IDesktopService;
import ag.ion.bion.officelayer.document.IDocumentService;
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
public class NoaConnectionTest {

    public NoaConnectionTest() {
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
     * Test of getConnection method, of class NoaConnection.
     */
    @Test
    public void testGetConnection() {
        System.out.println("getConnection");
        NoaConnection expResult = null;
        NoaConnection result = NoaConnection.getConnection();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getType method, of class NoaConnection.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        NoaConnection instance = null;
        int expResult = 0;
        int result = instance.getType();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDocumentService method, of class NoaConnection.
     */
    @Test
    public void testGetDocumentService() {
        System.out.println("getDocumentService");
        NoaConnection instance = null;
        IDocumentService expResult = null;
        IDocumentService result = instance.getDocumentService();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDesktopService method, of class NoaConnection.
     */
    @Test
    public void testGetDesktopService() {
        System.out.println("getDesktopService");
        NoaConnection instance = null;
        IDesktopService expResult = null;
        IDesktopService result = instance.getDesktopService();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of startOOServer method, of class NoaConnection.
     */
    @Test
    public void testStartOOServer() throws Exception {
        System.out.println("startOOServer");
        String path = "";
        int port = 0;
        NoaConnection.startOOServer(path, port);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopOOOServer method, of class NoaConnection.
     */
    @Test
    public void testStopOOOServer() {
        System.out.println("stopOOOServer");
        NoaConnection.stopOOOServer();
        
        fail("The test case is a prototype.");
    }

}