
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
public class vIntegerTest {

    public vIntegerTest() {
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
     * Test of getValue method, of class vInteger.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        vInteger instance = null;
        Integer expResult = null;
        Integer result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsVerified method, of class vInteger.
     */
    @Test
    public void testIsIsVerified() {
        System.out.println("isIsVerified");
        vInteger instance = null;
        boolean expResult = false;
        boolean result = instance.isIsVerified();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsPositive method, of class vInteger.
     */
    @Test
    public void testIsIsPositive() {
        System.out.println("isIsPositive");
        vInteger instance = null;
        boolean expResult = false;
        boolean result = instance.isIsPositive();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOvalue method, of class vInteger.
     */
    @Test
    public void testGetOvalue() {
        System.out.println("getOvalue");
        vInteger instance = null;
        String expResult = "";
        String result = instance.getOvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSvalue method, of class vInteger.
     */
    @Test
    public void testGetSvalue() {
        System.out.println("getSvalue");
        vInteger instance = null;
        String expResult = "";
        String result = instance.getSvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}