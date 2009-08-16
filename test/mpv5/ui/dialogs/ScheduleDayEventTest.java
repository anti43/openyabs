
package mpv5.ui.dialogs;

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
public class ScheduleDayEventTest {

    public ScheduleDayEventTest() {
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
     * Test of instanceOf method, of class ScheduleDayEvent.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        ScheduleDayEvent expResult = null;
        ScheduleDayEvent result = ScheduleDayEvent.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of dispose method, of class ScheduleDayEvent.
     */
    @Test
    public void testDispose() {
        System.out.println("dispose");
        ScheduleDayEvent instance = null;
        instance.dispose();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class ScheduleDayEvent.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date tday = null;
        ScheduleDayEvent instance = null;
        instance.setDate(tday);
        
        fail("The test case is a prototype.");
    }

}