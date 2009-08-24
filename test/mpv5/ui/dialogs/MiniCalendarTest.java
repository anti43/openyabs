
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
public class MiniCalendarTest {

    public MiniCalendarTest() {
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
     * Test of getDate method, of class MiniCalendar.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        MiniCalendarFrame instance = null;
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class MiniCalendar.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        MiniCalendarFrame instance = null;
        instance.setDate(date);
        
        fail("The test case is a prototype.");
    }

}