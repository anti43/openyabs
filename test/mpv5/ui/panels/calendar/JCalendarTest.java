
package mpv5.ui.panels.calendar;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
public class JCalendarTest {

    public JCalendarTest() {
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
     * Test of main method, of class JCalendar.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] s = null;
        JCalendar.main(s);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCalendar method, of class JCalendar.
     */
    @Test
    public void testGetCalendar() {
        System.out.println("getCalendar");
        JCalendar instance = new JCalendar();
        Calendar expResult = null;
        Calendar result = instance.getCalendar();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayChooser method, of class JCalendar.
     */
    @Test
    public void testGetDayChooser() {
        System.out.println("getDayChooser");
        JCalendar instance = new JCalendar();
        JDayChooser expResult = null;
        JDayChooser result = instance.getDayChooser();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocale method, of class JCalendar.
     */
    @Test
    public void testGetLocale() {
        System.out.println("getLocale");
        JCalendar instance = new JCalendar();
        Locale expResult = null;
        Locale result = instance.getLocale();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonthChooser method, of class JCalendar.
     */
    @Test
    public void testGetMonthChooser() {
        System.out.println("getMonthChooser");
        JCalendar instance = new JCalendar();
        JMonthChooser expResult = null;
        JMonthChooser result = instance.getMonthChooser();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getYearChooser method, of class JCalendar.
     */
    @Test
    public void testGetYearChooser() {
        System.out.println("getYearChooser");
        JCalendar instance = new JCalendar();
        JYearChooser expResult = null;
        JYearChooser result = instance.getYearChooser();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isWeekOfYearVisible method, of class JCalendar.
     */
    @Test
    public void testIsWeekOfYearVisible() {
        System.out.println("isWeekOfYearVisible");
        JCalendar instance = new JCalendar();
        boolean expResult = false;
        boolean result = instance.isWeekOfYearVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of propertyChange method, of class JCalendar.
     */
    @Test
    public void testPropertyChange() {
        System.out.println("propertyChange");
        PropertyChangeEvent evt = null;
        JCalendar instance = new JCalendar();
        instance.propertyChange(evt);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBackground method, of class JCalendar.
     */
    @Test
    public void testSetBackground() {
        System.out.println("setBackground");
        Color bg = null;
        JCalendar instance = new JCalendar();
        instance.setBackground(bg);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCalendar method, of class JCalendar.
     */
    @Test
    public void testSetCalendar() {
        System.out.println("setCalendar");
        Calendar c = null;
        JCalendar instance = new JCalendar();
        instance.setCalendar(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class JCalendar.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        JCalendar instance = new JCalendar();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEnabled method, of class JCalendar.
     */
    @Test
    public void testIsEnabled() {
        System.out.println("isEnabled");
        JCalendar instance = new JCalendar();
        boolean expResult = false;
        boolean result = instance.isEnabled();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFont method, of class JCalendar.
     */
    @Test
    public void testSetFont() {
        System.out.println("setFont");
        Font font = null;
        JCalendar instance = new JCalendar();
        instance.setFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setForeground method, of class JCalendar.
     */
    @Test
    public void testSetForeground() {
        System.out.println("setForeground");
        Color fg = null;
        JCalendar instance = new JCalendar();
        instance.setForeground(fg);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocale method, of class JCalendar.
     */
    @Test
    public void testSetLocale() {
        System.out.println("setLocale");
        Locale l = null;
        JCalendar instance = new JCalendar();
        instance.setLocale(l);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWeekOfYearVisible method, of class JCalendar.
     */
    @Test
    public void testSetWeekOfYearVisible() {
        System.out.println("setWeekOfYearVisible");
        boolean weekOfYearVisible = false;
        JCalendar instance = new JCalendar();
        instance.setWeekOfYearVisible(weekOfYearVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDecorationBackgroundVisible method, of class JCalendar.
     */
    @Test
    public void testIsDecorationBackgroundVisible() {
        System.out.println("isDecorationBackgroundVisible");
        JCalendar instance = new JCalendar();
        boolean expResult = false;
        boolean result = instance.isDecorationBackgroundVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBackgroundVisible method, of class JCalendar.
     */
    @Test
    public void testSetDecorationBackgroundVisible() {
        System.out.println("setDecorationBackgroundVisible");
        boolean decorationBackgroundVisible = false;
        JCalendar instance = new JCalendar();
        instance.setDecorationBackgroundVisible(decorationBackgroundVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDecorationBordersVisible method, of class JCalendar.
     */
    @Test
    public void testIsDecorationBordersVisible() {
        System.out.println("isDecorationBordersVisible");
        JCalendar instance = new JCalendar();
        boolean expResult = false;
        boolean result = instance.isDecorationBordersVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBordersVisible method, of class JCalendar.
     */
    @Test
    public void testSetDecorationBordersVisible() {
        System.out.println("setDecorationBordersVisible");
        boolean decorationBordersVisible = false;
        JCalendar instance = new JCalendar();
        instance.setDecorationBordersVisible(decorationBordersVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDecorationBackgroundColor method, of class JCalendar.
     */
    @Test
    public void testGetDecorationBackgroundColor() {
        System.out.println("getDecorationBackgroundColor");
        JCalendar instance = new JCalendar();
        Color expResult = null;
        Color result = instance.getDecorationBackgroundColor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBackgroundColor method, of class JCalendar.
     */
    @Test
    public void testSetDecorationBackgroundColor() {
        System.out.println("setDecorationBackgroundColor");
        Color decorationBackgroundColor = null;
        JCalendar instance = new JCalendar();
        instance.setDecorationBackgroundColor(decorationBackgroundColor);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSundayForeground method, of class JCalendar.
     */
    @Test
    public void testGetSundayForeground() {
        System.out.println("getSundayForeground");
        JCalendar instance = new JCalendar();
        Color expResult = null;
        Color result = instance.getSundayForeground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWeekdayForeground method, of class JCalendar.
     */
    @Test
    public void testGetWeekdayForeground() {
        System.out.println("getWeekdayForeground");
        JCalendar instance = new JCalendar();
        Color expResult = null;
        Color result = instance.getWeekdayForeground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class JCalendar.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        JCalendar instance = new JCalendar();
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class JCalendar.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        JCalendar instance = new JCalendar();
        instance.setDate(date);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectableDateRange method, of class JCalendar.
     */
    @Test
    public void testSetSelectableDateRange() {
        System.out.println("setSelectableDateRange");
        Date min = null;
        Date max = null;
        JCalendar instance = new JCalendar();
        instance.setSelectableDateRange(min, max);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxSelectableDate method, of class JCalendar.
     */
    @Test
    public void testGetMaxSelectableDate() {
        System.out.println("getMaxSelectableDate");
        JCalendar instance = new JCalendar();
        Date expResult = null;
        Date result = instance.getMaxSelectableDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinSelectableDate method, of class JCalendar.
     */
    @Test
    public void testGetMinSelectableDate() {
        System.out.println("getMinSelectableDate");
        JCalendar instance = new JCalendar();
        Date expResult = null;
        Date result = instance.getMinSelectableDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMaxSelectableDate method, of class JCalendar.
     */
    @Test
    public void testSetMaxSelectableDate() {
        System.out.println("setMaxSelectableDate");
        Date max = null;
        JCalendar instance = new JCalendar();
        instance.setMaxSelectableDate(max);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMinSelectableDate method, of class JCalendar.
     */
    @Test
    public void testSetMinSelectableDate() {
        System.out.println("setMinSelectableDate");
        Date min = null;
        JCalendar instance = new JCalendar();
        instance.setMinSelectableDate(min);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxDayCharacters method, of class JCalendar.
     */
    @Test
    public void testGetMaxDayCharacters() {
        System.out.println("getMaxDayCharacters");
        JCalendar instance = new JCalendar();
        int expResult = 0;
        int result = instance.getMaxDayCharacters();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}