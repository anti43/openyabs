
package mpv5.utils.models.hn;

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
public class StartEndDaysTest {

    public StartEndDaysTest() {
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
     * Test of getStartDay method, of class StartEndDays.
     */
    @Test
    public void testGetStartDay() {
        System.out.println("getStartDay");
        StartEndDays instance = null;
        Date expResult = null;
        Date result = instance.getStartDay();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndDay method, of class StartEndDays.
     */
    @Test
    public void testGetEndDay() {
        System.out.println("getEndDay");
        StartEndDays instance = null;
        Date expResult = null;
        Date result = instance.getEndDay();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMode method, of class StartEndDays.
     */
    @Test
    public void testGetMode() {
        System.out.println("getMode");
        StartEndDays instance = null;
        int expResult = 0;
        int result = instance.getMode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}