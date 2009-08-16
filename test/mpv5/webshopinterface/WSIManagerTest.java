
package mpv5.webshopinterface;

import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.DatabaseObject;
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
public class WSIManagerTest {

    public WSIManagerTest() {
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
     * Test of instanceOf method, of class WSIManager.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        WSIManager expResult = null;
        WSIManager result = WSIManager.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class WSIManager.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        WSIManager instance = null;
        instance.start();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getShops method, of class WSIManager.
     */
    @Test
    public void testGetShops() {
        System.out.println("getShops");
        WSIManager instance = null;
        ArrayList expResult = null;
        ArrayList result = instance.getShops();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createObjects method, of class WSIManager.
     */
    @Test
    public void testCreateObjects() {
        System.out.println("createObjects");
        Object data = null;
        DatabaseObject template = null;
        List expResult = null;
        List result = WSIManager.createObjects(data, template);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class WSIManager.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        WSIManager instance = null;
        instance.reset();
        
        fail("The test case is a prototype.");
    }

}