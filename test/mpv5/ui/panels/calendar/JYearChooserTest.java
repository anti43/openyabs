
package mpv5.ui.panels.calendar;

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
public class JYearChooserTest {

    public JYearChooserTest() {
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
     * Test of setYear method, of class JYearChooser.
     */
    @Test
    public void testSetYear() {
        System.out.println("setYear");
        int y = 0;
        JYearChooser instance = new JYearChooser();
        instance.setYear(y);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class JYearChooser.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        int value = 0;
        JYearChooser instance = new JYearChooser();
        instance.setValue(value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getYear method, of class JYearChooser.
     */
    @Test
    public void testGetYear() {
        System.out.println("getYear");
        JYearChooser instance = new JYearChooser();
        int expResult = 0;
        int result = instance.getYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDayChooser method, of class JYearChooser.
     */
    @Test
    public void testSetDayChooser() {
        System.out.println("setDayChooser");
        JDayChooser dayChooser = null;
        JYearChooser instance = new JYearChooser();
        instance.setDayChooser(dayChooser);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndYear method, of class JYearChooser.
     */
    @Test
    public void testGetEndYear() {
        System.out.println("getEndYear");
        JYearChooser instance = new JYearChooser();
        int expResult = 0;
        int result = instance.getEndYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndYear method, of class JYearChooser.
     */
    @Test
    public void testSetEndYear() {
        System.out.println("setEndYear");
        int endYear = 0;
        JYearChooser instance = new JYearChooser();
        instance.setEndYear(endYear);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartYear method, of class JYearChooser.
     */
    @Test
    public void testGetStartYear() {
        System.out.println("getStartYear");
        JYearChooser instance = new JYearChooser();
        int expResult = 0;
        int result = instance.getStartYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStartYear method, of class JYearChooser.
     */
    @Test
    public void testSetStartYear() {
        System.out.println("setStartYear");
        int startYear = 0;
        JYearChooser instance = new JYearChooser();
        instance.setStartYear(startYear);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class JYearChooser.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] s = null;
        JYearChooser.main(s);
        
        fail("The test case is a prototype.");
    }

}