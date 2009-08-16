
package mpv5.db.common;

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
public class ConnectionTypeHandlerTest {

    public ConnectionTypeHandlerTest() {
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
     * Test of getDriverName method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetDriverName() {
        System.out.println("getDriverName");
        String expResult = "";
        String result = ConnectionTypeHandler.getDriverName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDriverType method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetDriverType() {
        System.out.println("getDriverType");
        int expResult = 0;
        int result = ConnectionTypeHandler.getDriverType();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionString method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetConnectionString() {
        System.out.println("getConnectionString");
        boolean withCreate = false;
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        String expResult = "";
        String result = instance.getConnectionString(withCreate);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableCreating_SQLCommand method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetTableCreating_SQLCommand() {
        System.out.println("getTableCreating_SQLCommand");
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        String[] expResult = null;
        String[] result = instance.getTableCreating_SQLCommand();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setConnectionString method, of class ConnectionTypeHandler.
     */
    @Test
    public void testSetConnectionString() {
        System.out.println("setConnectionString");
        String conn_string = "";
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        instance.setConnectionString(conn_string);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getURL method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetURL() {
        System.out.println("getURL");
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        String expResult = "";
        String result = instance.getURL();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setURL method, of class ConnectionTypeHandler.
     */
    @Test
    public void testSetURL() {
        System.out.println("setURL");
        String URL = "";
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        instance.setURL(URL);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDRIVER method, of class ConnectionTypeHandler.
     */
    @Test
    public void testSetDRIVER_int() {
        System.out.println("setDRIVER");
        int predefinedDriver = 0;
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        instance.setDRIVER(predefinedDriver);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDriver method, of class ConnectionTypeHandler.
     */
    @Test
    public void testGetDriver() {
        System.out.println("getDriver");
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        String expResult = "";
        String result = instance.getDriver();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDRIVER method, of class ConnectionTypeHandler.
     */
    @Test
    public void testSetDRIVER_String() {
        System.out.println("setDRIVER");
        String predefinedDriver = "";
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        instance.setDRIVER(predefinedDriver);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDBName method, of class ConnectionTypeHandler.
     */
    @Test
    public void testSetDBName() {
        System.out.println("setDBName");
        String dbname = "";
        ConnectionTypeHandler instance = new ConnectionTypeHandler();
        instance.setDBName(dbname);
        
        fail("The test case is a prototype.");
    }

}