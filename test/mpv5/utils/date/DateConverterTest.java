
package mpv5.utils.date;

import java.text.DateFormat;
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
public class DateConverterTest {

    public DateConverterTest() {
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
     * Test of addDays method, of class DateConverter.
     */
    @Test
    public void testAddDays() {
        System.out.println("addDays");
        Date date = null;
        Integer amount = null;
        Date expResult = null;
        Date result = DateConverter.addDays(date, amount);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addYears method, of class DateConverter.
     */
    @Test
    public void testAddYears() {
        System.out.println("addYears");
        Date date = null;
        int amount = 0;
        Date expResult = null;
        Date result = DateConverter.addYears(date, amount);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDifferenceBetween method, of class DateConverter.
     */
    @Test
    public void testGetDifferenceBetween() {
        System.out.println("getDifferenceBetween");
        Date date1 = null;
        Date date2 = null;
        Integer expResult = null;
        Integer result = DateConverter.getDifferenceBetween(date1, date2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndOfDay method, of class DateConverter.
     */
    @Test
    public void testGetEndOfDay() {
        System.out.println("getEndOfDay");
        Date date = null;
        Date expResult = null;
        Date result = DateConverter.getEndOfDay(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getQuarter method, of class DateConverter.
     */
    @Test
    public void testGetQuarter_0args() {
        System.out.println("getQuarter");
        int expResult = 0;
        int result = DateConverter.getQuarter();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getQuarter method, of class DateConverter.
     */
    @Test
    public void testGetQuarter_Date() {
        System.out.println("getQuarter");
        Date date = null;
        int expResult = 0;
        int result = DateConverter.getQuarter(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTodayDefDate method, of class DateConverter.
     */
    @Test
    public void testGetTodayDefDate() {
        System.out.println("getTodayDefDate");
        String expResult = "";
        String result = DateConverter.getTodayDefDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTodayDBDate method, of class DateConverter.
     */
    @Test
    public void testGetTodayDBDate() {
        System.out.println("getTodayDBDate");
        String expResult = "";
        String result = DateConverter.getTodayDBDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addYear method, of class DateConverter.
     */
    @Test
    public void testAddYear() {
        System.out.println("addYear");
        Date date = null;
        Date expResult = null;
        Date result = DateConverter.addYear(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMonth method, of class DateConverter.
     */
    @Test
    public void testAddMonth() {
        System.out.println("addMonth");
        Date date = null;
        Date expResult = null;
        Date result = DateConverter.addMonth(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addQuarter method, of class DateConverter.
     */
    @Test
    public void testAddQuarter() {
        System.out.println("addQuarter");
        Date date = null;
        Date expResult = null;
        Date result = DateConverter.addQuarter(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addDay method, of class DateConverter.
     */
    @Test
    public void testAddDay() {
        System.out.println("addDay");
        Date date = null;
        Date expResult = null;
        Date result = DateConverter.addDay(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSQLDateString method, of class DateConverter.
     */
    @Test
    public void testGetSQLDateString() {
        System.out.println("getSQLDateString");
        Date date = null;
        String expResult = "";
        String result = DateConverter.getSQLDateString(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefDateString method, of class DateConverter.
     */
    @Test
    public void testGetDefDateString() {
        System.out.println("getDefDateString");
        Date date = null;
        String expResult = "";
        String result = DateConverter.getDefDateString(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDay method, of class DateConverter.
     */
    @Test
    public void testGetDay() {
        System.out.println("getDay");
        Date datum = null;
        String expResult = "";
        String result = DateConverter.getDay(datum);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonth method, of class DateConverter.
     */
    @Test
    public void testGetMonth_Date() {
        System.out.println("getMonth");
        Date date = null;
        String expResult = "";
        String result = DateConverter.getMonth(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonthName method, of class DateConverter.
     */
    @Test
    public void testGetMonthName_Date() {
        System.out.println("getMonthName");
        Date date = null;
        String expResult = "";
        String result = DateConverter.getMonthName(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayOfMonth method, of class DateConverter.
     */
    @Test
    public void testGetDayOfMonth_Date() {
        System.out.println("getDayOfMonth");
        Date date = null;
        String expResult = "";
        String result = DateConverter.getDayOfMonth(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class DateConverter.
     */
    @Test
    public void testGetDate_String() {
        System.out.println("getDate");
        String date = "";
        Date expResult = null;
        Date result = DateConverter.getDate(date);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class DateConverter.
     */
    @Test
    public void testGetDate_Object() {
        System.out.println("getDate");
        Object object = null;
        Date expResult = null;
        Date result = DateConverter.getDate(object);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getYear method, of class DateConverter.
     */
    @Test
    public void testGetYear() {
        System.out.println("getYear");
        String expResult = "";
        String result = DateConverter.getYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayOfMonth method, of class DateConverter.
     */
    @Test
    public void testGetDayOfMonth_0args() {
        System.out.println("getDayOfMonth");
        String expResult = "";
        String result = DateConverter.getDayOfMonth();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayMonthAndYear method, of class DateConverter.
     */
    @Test
    public void testGetDayMonthAndYear() {
        System.out.println("getDayMonthAndYear");
        String expResult = "";
        String result = DateConverter.getDayMonthAndYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonth method, of class DateConverter.
     */
    @Test
    public void testGetMonth_0args() {
        System.out.println("getMonth");
        String expResult = "";
        String result = DateConverter.getMonth();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonthName method, of class DateConverter.
     */
    @Test
    public void testGetMonthName_0args() {
        System.out.println("getMonthName");
        String expResult = "";
        String result = DateConverter.getMonthName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultFormat method, of class DateConverter.
     */
    @Test
    public void testSetDefaultFormat() {
        System.out.println("setDefaultFormat");
        DateFormat df = null;
        DateConverter.setDefaultFormat(df);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultFormatString method, of class DateConverter.
     */
    @Test
    public void testGetDefaultFormatString() {
        System.out.println("getDefaultFormatString");
        String expResult = "";
        String result = DateConverter.getDefaultFormatString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}