
package mpv5.db.common;

import java.sql.Connection;
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
public class DatabaseConnectionTest {

    public DatabaseConnectionTest() {
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
     * Test of instanceOf method, of class DatabaseConnection.
     */
    @Test
    public void testInstanceOf() throws Exception {
        System.out.println("instanceOf");
        DatabaseConnection expResult = null;
        DatabaseConnection result = DatabaseConnection.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnection method, of class DatabaseConnection.
     */
    @Test
    public void testGetConnection() {
        System.out.println("getConnection");
        DatabaseConnection instance = new DatabaseConnection();
        Connection expResult = null;
        Connection result = instance.getConnection();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of connect method, of class DatabaseConnection.
     */
    @Test
    public void testConnect() throws Exception {
        System.out.println("connect");
        String predefinedDriver = "";
        String user = "";
        String password = "";
        String location = "";
        String dbname = "";
        boolean create = false;
        DatabaseConnection instance = new DatabaseConnection();
        boolean expResult = false;
        boolean result = instance.connect(predefinedDriver, user, password, location, dbname, create);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of shutdown method, of class DatabaseConnection.
     */
    @Test
    public void testShutdown() {
        System.out.println("shutdown");
        DatabaseConnection.shutdown();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of runQueries method, of class DatabaseConnection.
     */
    @Test
    public void testRunQueries() throws Exception {
        System.out.println("runQueries");
        String[] queries = null;
        DatabaseConnection instance = new DatabaseConnection();
        boolean expResult = false;
        boolean result = instance.runQueries(queries);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}