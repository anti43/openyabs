
package mpv5.ui.panels.calendar;

import java.util.Date;
import mpv5.db.objects.Schedule;
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
public class CalendarButtonTest {

    public CalendarButtonTest() {
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
     * Test of getDate method, of class CalendarButton.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        CalendarButton instance = null;
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class CalendarButton.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        CalendarButton instance = null;
        instance.setDate(date);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSchedule method, of class CalendarButton.
     */
    @Test
    public void testGetSchedule() {
        System.out.println("getSchedule");
        CalendarButton instance = null;
        Schedule expResult = null;
        Schedule result = instance.getSchedule();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSchedule method, of class CalendarButton.
     */
    @Test
    public void testSetSchedule() {
        System.out.println("setSchedule");
        Schedule schedule = null;
        CalendarButton instance = null;
        instance.setSchedule(schedule);
        
        fail("The test case is a prototype.");
    }

}