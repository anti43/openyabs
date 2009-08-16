
package mpv5.ui.beans;

import mpv5.utils.date.vTimeframe;
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
public class TimeframeChooserTest {

    public TimeframeChooserTest() {
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
     * Test of getTime method, of class TimeframeChooser.
     */
    @Test
    public void testGetTime() {
        System.out.println("getTime");
        TimeframeChooser instance = new TimeframeChooser();
        vTimeframe expResult = null;
        vTimeframe result = instance.getTime();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTime method, of class TimeframeChooser.
     */
    @Test
    public void testSetTime() {
        System.out.println("setTime");
        vTimeframe time = null;
        TimeframeChooser instance = new TimeframeChooser();
        instance.setTime(time);
        
        fail("The test case is a prototype.");
    }

}