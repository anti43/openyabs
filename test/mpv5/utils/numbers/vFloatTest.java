
package mpv5.utils.numbers;

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
public class vFloatTest {

    public vFloatTest() {
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
     * Test of getValue method, of class vFloat.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        vFloat instance = null;
        Float expResult = null;
        Float result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSvalue method, of class vFloat.
     */
    @Test
    public void testGetSvalue() {
        System.out.println("getSvalue");
        vFloat instance = null;
        String expResult = "";
        String result = instance.getSvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsVerified method, of class vFloat.
     */
    @Test
    public void testIsIsVerified() {
        System.out.println("isIsVerified");
        vFloat instance = null;
        boolean expResult = false;
        boolean result = instance.isIsVerified();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsPositive method, of class vFloat.
     */
    @Test
    public void testIsIsPositive() {
        System.out.println("isIsPositive");
        vFloat instance = null;
        boolean expResult = false;
        boolean result = instance.isIsPositive();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOvalue method, of class vFloat.
     */
    @Test
    public void testGetOvalue() {
        System.out.println("getOvalue");
        vFloat instance = null;
        String expResult = "";
        String result = instance.getOvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDecValue method, of class vFloat.
     */
    @Test
    public void testGetDecValue() {
        System.out.println("getDecValue");
        vFloat instance = null;
        String expResult = "";
        String result = instance.getDecValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}