
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
public class IpTest {

    public IpTest() {
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
     * Test of isValidIP method, of class Ip.
     */
    @Test
    public void testIsValidIP() {
        System.out.println("isValidIP");
        String valueOf = "";
        boolean expResult = false;
        boolean result = Ip.isValidIP(valueOf);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPart0 method, of class Ip.
     */
    @Test
    public void testGetPart0() {
        System.out.println("getPart0");
        Ip instance = null;
        int expResult = 0;
        int result = instance.getPart0();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPart1 method, of class Ip.
     */
    @Test
    public void testGetPart1() {
        System.out.println("getPart1");
        Ip instance = null;
        int expResult = 0;
        int result = instance.getPart1();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPart2 method, of class Ip.
     */
    @Test
    public void testGetPart2() {
        System.out.println("getPart2");
        Ip instance = null;
        int expResult = 0;
        int result = instance.getPart2();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPart3 method, of class Ip.
     */
    @Test
    public void testGetPart3() {
        System.out.println("getPart3");
        Ip instance = null;
        int expResult = 0;
        int result = instance.getPart3();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFullip method, of class Ip.
     */
    @Test
    public void testGetFullip() {
        System.out.println("getFullip");
        Ip instance = null;
        String[] expResult = null;
        String[] result = instance.getFullip();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Ip.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Ip instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Ip.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        Ip instance = null;
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class Ip.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Ip instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}