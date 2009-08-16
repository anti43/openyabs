
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
public class SaveStringTest {

    public SaveStringTest() {
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
     * Test of toString method, of class SaveString.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SaveString instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWrapper method, of class SaveString.
     */
    @Test
    public void testGetWrapper() {
        System.out.println("getWrapper");
        SaveString instance = null;
        String expResult = "";
        String result = instance.getWrapper();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}