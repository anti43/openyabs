
package mpv5.db.objects;

import java.util.ArrayList;
import java.util.Date;
import javax.swing.JComponent;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.images.MPIcon;
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
public class ScheduleTest {

    public ScheduleTest() {
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
     * Test of getDate method, of class Schedule.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        Schedule instance = new Schedule();
        vTimeframe expResult = null;
        vTimeframe result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItem method, of class Schedule.
     */
    @Test
    public void testGetItem() throws Exception {
        System.out.println("getItem");
        Schedule instance = new Schedule();
        Item expResult = null;
        Item result = instance.getItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEvents method, of class Schedule.
     */
    @Test
    public void testGetEvents_vTimeframe() {
        System.out.println("getEvents");
        vTimeframe date = null;
        ArrayList expResult = null;
        ArrayList result = Schedule.getEvents(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEvents method, of class Schedule.
     */
    @Test
    public void testGetEvents_Date() {
        System.out.println("getEvents");
        Date day = null;
        ArrayList expResult = null;
        ArrayList result = Schedule.getEvents(day);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Schedule.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Schedule instance = new Schedule();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUsersids method, of class Schedule.
     */
    @Test
    public void test__getUsersids() {
        System.out.println("__getUsersids");
        Schedule instance = new Schedule();
        int expResult = 0;
        int result = instance.__getUsersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsersids method, of class Schedule.
     */
    @Test
    public void testSetUsersids() {
        System.out.println("setUsersids");
        int usersids = 0;
        Schedule instance = new Schedule();
        instance.setUsersids(usersids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getItemsids method, of class Schedule.
     */
    @Test
    public void test__getItemsids() {
        System.out.println("__getItemsids");
        Schedule instance = new Schedule();
        int expResult = 0;
        int result = instance.__getItemsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setItemsids method, of class Schedule.
     */
    @Test
    public void testSetItemsids() {
        System.out.println("setItemsids");
        int itemsids = 0;
        Schedule instance = new Schedule();
        instance.setItemsids(itemsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getNextdate method, of class Schedule.
     */
    @Test
    public void test__getNextdate() {
        System.out.println("__getNextdate");
        Schedule instance = new Schedule();
        Date expResult = null;
        Date result = instance.__getNextdate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNextdate method, of class Schedule.
     */
    @Test
    public void testSetNextdate() {
        System.out.println("setNextdate");
        Date nextdate = null;
        Schedule instance = new Schedule();
        instance.setNextdate(nextdate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntervalmonth method, of class Schedule.
     */
    @Test
    public void test__getIntervalmonth() {
        System.out.println("__getIntervalmonth");
        Schedule instance = new Schedule();
        int expResult = 0;
        int result = instance.__getIntervalmonth();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntervalmonth method, of class Schedule.
     */
    @Test
    public void testSetIntervalmonth() {
        System.out.println("setIntervalmonth");
        int intervalmonth = 0;
        Schedule instance = new Schedule();
        instance.setIntervalmonth(intervalmonth);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Schedule.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Schedule instance = new Schedule();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}