
package mpv5.utils.date;

import java.util.Date;
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
public class vTimeframeTest {

    public vTimeframeTest() {
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
     * Test of contains method, of class vTimeframe.
     */
    @Test
    public void testContains() {
        System.out.println("contains");
        Date day = null;
        vTimeframe instance = null;
        boolean expResult = false;
        boolean result = instance.contains(day);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStart method, of class vTimeframe.
     */
    @Test
    public void testGetStart() {
        System.out.println("getStart");
        vTimeframe instance = null;
        Date expResult = null;
        Date result = instance.getStart();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEnd method, of class vTimeframe.
     */
    @Test
    public void testGetEnd() {
        System.out.println("getEnd");
        vTimeframe instance = null;
        Date expResult = null;
        Date result = instance.getEnd();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTime method, of class vTimeframe.
     */
    @Test
    public void testGetTime() {
        System.out.println("getTime");
        vTimeframe instance = null;
        long expResult = 0L;
        long result = instance.getTime();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class vTimeframe.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        vTimeframe instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}