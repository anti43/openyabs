
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
public class vDoubleTest {

    public vDoubleTest() {
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
     * Test of getValue method, of class vDouble.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        vDouble instance = null;
        Double expResult = null;
        Double result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSvalue method, of class vDouble.
     */
    @Test
    public void testGetSvalue() {
        System.out.println("getSvalue");
        vDouble instance = null;
        String expResult = "";
        String result = instance.getSvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsVerified method, of class vDouble.
     */
    @Test
    public void testIsIsVerified() {
        System.out.println("isIsVerified");
        vDouble instance = null;
        boolean expResult = false;
        boolean result = instance.isIsVerified();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isIsPositive method, of class vDouble.
     */
    @Test
    public void testIsIsPositive() {
        System.out.println("isIsPositive");
        vDouble instance = null;
        boolean expResult = false;
        boolean result = instance.isIsPositive();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDecValue method, of class vDouble.
     */
    @Test
    public void testGetDecValue() {
        System.out.println("getDecValue");
        vDouble instance = null;
        String expResult = "";
        String result = instance.getDecValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}