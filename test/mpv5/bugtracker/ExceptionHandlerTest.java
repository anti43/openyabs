
package mpv5.bugtracker;

import java.util.List;
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
public class ExceptionHandlerTest {

    public ExceptionHandlerTest() {
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
     * Test of add method, of class ExceptionHandler.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        Exception exception = null;
        ExceptionHandler.add(exception);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getExceptions method, of class ExceptionHandler.
     */
    @Test
    public void testGetExceptions() {
        System.out.println("getExceptions");
        List expResult = null;
        List result = ExceptionHandler.getExceptions();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}