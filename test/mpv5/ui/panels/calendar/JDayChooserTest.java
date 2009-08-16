
package mpv5.ui.panels.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.JPanel;
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
public class JDayChooserTest {

    public JDayChooserTest() {
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
     * Test of init method, of class JDayChooser.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        JDayChooser instance = new JDayChooser();
        instance.init();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawDayNames method, of class JDayChooser.
     */
    @Test
    public void testDrawDayNames() {
        System.out.println("drawDayNames");
        JDayChooser instance = new JDayChooser();
        instance.drawDayNames();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of initDecorations method, of class JDayChooser.
     */
    @Test
    public void testInitDecorations() {
        System.out.println("initDecorations");
        JDayChooser instance = new JDayChooser();
        instance.initDecorations();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawWeeks method, of class JDayChooser.
     */
    @Test
    public void testDrawWeeks() {
        System.out.println("drawWeeks");
        JDayChooser instance = new JDayChooser();
        instance.drawWeeks();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of drawDays method, of class JDayChooser.
     */
    @Test
    public void testDrawDays() {
        System.out.println("drawDays");
        ArrayList<Schedule> list = null;
        JDayChooser instance = new JDayChooser();
        instance.drawDays(list);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocale method, of class JDayChooser.
     */
    @Test
    public void testGetLocale() {
        System.out.println("getLocale");
        JDayChooser instance = new JDayChooser();
        Locale expResult = null;
        Locale result = instance.getLocale();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocale method, of class JDayChooser.
     */
    @Test
    public void testSetLocale() {
        System.out.println("setLocale");
        Locale locale = null;
        JDayChooser instance = new JDayChooser();
        instance.setLocale(locale);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDay method, of class JDayChooser.
     */
    @Test
    public void testSetDay() {
        System.out.println("setDay");
        int d = 0;
        JDayChooser instance = new JDayChooser();
        instance.setDay(d);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAlwaysFireDayProperty method, of class JDayChooser.
     */
    @Test
    public void testSetAlwaysFireDayProperty() {
        System.out.println("setAlwaysFireDayProperty");
        boolean alwaysFire = false;
        JDayChooser instance = new JDayChooser();
        instance.setAlwaysFireDayProperty(alwaysFire);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDay method, of class JDayChooser.
     */
    @Test
    public void testGetDay() {
        System.out.println("getDay");
        JDayChooser instance = new JDayChooser();
        int expResult = 0;
        int result = instance.getDay();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMonth method, of class JDayChooser.
     */
    @Test
    public void testSetMonth() {
        System.out.println("setMonth");
        int month = 0;
        JDayChooser instance = new JDayChooser();
        instance.setMonth(month);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setYear method, of class JDayChooser.
     */
    @Test
    public void testSetYear() {
        System.out.println("setYear");
        int year = 0;
        JDayChooser instance = new JDayChooser();
        instance.setYear(year);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCalendar method, of class JDayChooser.
     */
    @Test
    public void testSetCalendar() {
        System.out.println("setCalendar");
        Calendar calendar = null;
        JDayChooser instance = new JDayChooser();
        instance.setCalendar(calendar);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFont method, of class JDayChooser.
     */
    @Test
    public void testSetFont() {
        System.out.println("setFont");
        Font font = null;
        JDayChooser instance = new JDayChooser();
        instance.setFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setForeground method, of class JDayChooser.
     */
    @Test
    public void testSetForeground() {
        System.out.println("setForeground");
        Color foreground = null;
        JDayChooser instance = new JDayChooser();
        instance.setForeground(foreground);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionPerformed method, of class JDayChooser.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");
        ActionEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.actionPerformed(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of focusGained method, of class JDayChooser.
     */
    @Test
    public void testFocusGained() {
        System.out.println("focusGained");
        FocusEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.focusGained(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of focusLost method, of class JDayChooser.
     */
    @Test
    public void testFocusLost() {
        System.out.println("focusLost");
        FocusEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.focusLost(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyPressed method, of class JDayChooser.
     */
    @Test
    public void testKeyPressed() {
        System.out.println("keyPressed");
        KeyEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.keyPressed(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyTyped method, of class JDayChooser.
     */
    @Test
    public void testKeyTyped() {
        System.out.println("keyTyped");
        KeyEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.keyTyped(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyReleased method, of class JDayChooser.
     */
    @Test
    public void testKeyReleased() {
        System.out.println("keyReleased");
        KeyEvent e = null;
        JDayChooser instance = new JDayChooser();
        instance.keyReleased(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class JDayChooser.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        JDayChooser instance = new JDayChooser();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isWeekOfYearVisible method, of class JDayChooser.
     */
    @Test
    public void testIsWeekOfYearVisible() {
        System.out.println("isWeekOfYearVisible");
        JDayChooser instance = new JDayChooser();
        boolean expResult = false;
        boolean result = instance.isWeekOfYearVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWeekOfYearVisible method, of class JDayChooser.
     */
    @Test
    public void testSetWeekOfYearVisible() {
        System.out.println("setWeekOfYearVisible");
        boolean weekOfYearVisible = false;
        JDayChooser instance = new JDayChooser();
        instance.setWeekOfYearVisible(weekOfYearVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayPanel method, of class JDayChooser.
     */
    @Test
    public void testGetDayPanel() {
        System.out.println("getDayPanel");
        JDayChooser instance = new JDayChooser();
        JPanel expResult = null;
        JPanel result = instance.getDayPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDecorationBackgroundColor method, of class JDayChooser.
     */
    @Test
    public void testGetDecorationBackgroundColor() {
        System.out.println("getDecorationBackgroundColor");
        JDayChooser instance = new JDayChooser();
        Color expResult = null;
        Color result = instance.getDecorationBackgroundColor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBackgroundColor method, of class JDayChooser.
     */
    @Test
    public void testSetDecorationBackgroundColor() {
        System.out.println("setDecorationBackgroundColor");
        Color decorationBackgroundColor = null;
        JDayChooser instance = new JDayChooser();
        instance.setDecorationBackgroundColor(decorationBackgroundColor);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSundayForeground method, of class JDayChooser.
     */
    @Test
    public void testGetSundayForeground() {
        System.out.println("getSundayForeground");
        JDayChooser instance = new JDayChooser();
        Color expResult = null;
        Color result = instance.getSundayForeground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWeekdayForeground method, of class JDayChooser.
     */
    @Test
    public void testGetWeekdayForeground() {
        System.out.println("getWeekdayForeground");
        JDayChooser instance = new JDayChooser();
        Color expResult = null;
        Color result = instance.getWeekdayForeground();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFocus method, of class JDayChooser.
     */
    @Test
    public void testSetFocus() {
        System.out.println("setFocus");
        JDayChooser instance = new JDayChooser();
        instance.setFocus();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDecorationBackgroundVisible method, of class JDayChooser.
     */
    @Test
    public void testIsDecorationBackgroundVisible() {
        System.out.println("isDecorationBackgroundVisible");
        JDayChooser instance = new JDayChooser();
        boolean expResult = false;
        boolean result = instance.isDecorationBackgroundVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBackgroundVisible method, of class JDayChooser.
     */
    @Test
    public void testSetDecorationBackgroundVisible() {
        System.out.println("setDecorationBackgroundVisible");
        boolean decorationBackgroundVisible = false;
        JDayChooser instance = new JDayChooser();
        instance.setDecorationBackgroundVisible(decorationBackgroundVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDecorationBordersVisible method, of class JDayChooser.
     */
    @Test
    public void testIsDecorationBordersVisible() {
        System.out.println("isDecorationBordersVisible");
        JDayChooser instance = new JDayChooser();
        boolean expResult = false;
        boolean result = instance.isDecorationBordersVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDayBordersVisible method, of class JDayChooser.
     */
    @Test
    public void testIsDayBordersVisible() {
        System.out.println("isDayBordersVisible");
        JDayChooser instance = new JDayChooser();
        boolean expResult = false;
        boolean result = instance.isDayBordersVisible();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDecorationBordersVisible method, of class JDayChooser.
     */
    @Test
    public void testSetDecorationBordersVisible() {
        System.out.println("setDecorationBordersVisible");
        boolean decorationBordersVisible = false;
        JDayChooser instance = new JDayChooser();
        instance.setDecorationBordersVisible(decorationBordersVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDayBordersVisible method, of class JDayChooser.
     */
    @Test
    public void testSetDayBordersVisible() {
        System.out.println("setDayBordersVisible");
        boolean dayBordersVisible = false;
        JDayChooser instance = new JDayChooser();
        instance.setDayBordersVisible(dayBordersVisible);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateUI method, of class JDayChooser.
     */
    @Test
    public void testUpdateUI() {
        System.out.println("updateUI");
        JDayChooser instance = new JDayChooser();
        instance.updateUI();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectableDateRange method, of class JDayChooser.
     */
    @Test
    public void testSetSelectableDateRange() {
        System.out.println("setSelectableDateRange");
        Date min = null;
        Date max = null;
        JDayChooser instance = new JDayChooser();
        instance.setSelectableDateRange(min, max);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMaxSelectableDate method, of class JDayChooser.
     */
    @Test
    public void testSetMaxSelectableDate() {
        System.out.println("setMaxSelectableDate");
        Date max = null;
        JDayChooser instance = new JDayChooser();
        Date expResult = null;
        Date result = instance.setMaxSelectableDate(max);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMinSelectableDate method, of class JDayChooser.
     */
    @Test
    public void testSetMinSelectableDate() {
        System.out.println("setMinSelectableDate");
        Date min = null;
        JDayChooser instance = new JDayChooser();
        Date expResult = null;
        Date result = instance.setMinSelectableDate(min);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxSelectableDate method, of class JDayChooser.
     */
    @Test
    public void testGetMaxSelectableDate() {
        System.out.println("getMaxSelectableDate");
        JDayChooser instance = new JDayChooser();
        Date expResult = null;
        Date result = instance.getMaxSelectableDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMinSelectableDate method, of class JDayChooser.
     */
    @Test
    public void testGetMinSelectableDate() {
        System.out.println("getMinSelectableDate");
        JDayChooser instance = new JDayChooser();
        Date expResult = null;
        Date result = instance.getMinSelectableDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMaxDayCharacters method, of class JDayChooser.
     */
    @Test
    public void testGetMaxDayCharacters() {
        System.out.println("getMaxDayCharacters");
        JDayChooser instance = new JDayChooser();
        int expResult = 0;
        int result = instance.getMaxDayCharacters();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}