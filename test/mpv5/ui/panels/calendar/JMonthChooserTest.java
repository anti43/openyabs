
package mpv5.ui.panels.calendar;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.Locale;
import javax.swing.event.ChangeEvent;
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
public class JMonthChooserTest {

    public JMonthChooserTest() {
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
     * Test of initNames method, of class JMonthChooser.
     */
    @Test
    public void testInitNames() {
        System.out.println("initNames");
        JMonthChooser instance = new JMonthChooser();
        instance.initNames();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stateChanged method, of class JMonthChooser.
     */
    @Test
    public void testStateChanged() {
        System.out.println("stateChanged");
        ChangeEvent e = null;
        JMonthChooser instance = new JMonthChooser();
        instance.stateChanged(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of itemStateChanged method, of class JMonthChooser.
     */
    @Test
    public void testItemStateChanged() {
        System.out.println("itemStateChanged");
        ItemEvent e = null;
        JMonthChooser instance = new JMonthChooser();
        instance.itemStateChanged(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMonth method, of class JMonthChooser.
     */
    @Test
    public void testSetMonth() {
        System.out.println("setMonth");
        int newMonth = 0;
        JMonthChooser instance = new JMonthChooser();
        instance.setMonth(newMonth);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonth method, of class JMonthChooser.
     */
    @Test
    public void testGetMonth() {
        System.out.println("getMonth");
        JMonthChooser instance = new JMonthChooser();
        int expResult = 0;
        int result = instance.getMonth();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDayChooser method, of class JMonthChooser.
     */
    @Test
    public void testSetDayChooser() {
        System.out.println("setDayChooser");
        JDayChooser dayChooser = null;
        JMonthChooser instance = new JMonthChooser();
        instance.setDayChooser(dayChooser);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setYearChooser method, of class JMonthChooser.
     */
    @Test
    public void testSetYearChooser() {
        System.out.println("setYearChooser");
        JYearChooser yearChooser = null;
        JMonthChooser instance = new JMonthChooser();
        instance.setYearChooser(yearChooser);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocale method, of class JMonthChooser.
     */
    @Test
    public void testGetLocale() {
        System.out.println("getLocale");
        JMonthChooser instance = new JMonthChooser();
        Locale expResult = null;
        Locale result = instance.getLocale();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocale method, of class JMonthChooser.
     */
    @Test
    public void testSetLocale() {
        System.out.println("setLocale");
        Locale l = null;
        JMonthChooser instance = new JMonthChooser();
        instance.setLocale(l);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class JMonthChooser.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        JMonthChooser instance = new JMonthChooser();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getComboBox method, of class JMonthChooser.
     */
    @Test
    public void testGetComboBox() {
        System.out.println("getComboBox");
        JMonthChooser instance = new JMonthChooser();
        Component expResult = null;
        Component result = instance.getComboBox();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpinner method, of class JMonthChooser.
     */
    @Test
    public void testGetSpinner() {
        System.out.println("getSpinner");
        JMonthChooser instance = new JMonthChooser();
        Component expResult = null;
        Component result = instance.getSpinner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasSpinner method, of class JMonthChooser.
     */
    @Test
    public void testHasSpinner() {
        System.out.println("hasSpinner");
        JMonthChooser instance = new JMonthChooser();
        boolean expResult = false;
        boolean result = instance.hasSpinner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFont method, of class JMonthChooser.
     */
    @Test
    public void testSetFont() {
        System.out.println("setFont");
        Font font = null;
        JMonthChooser instance = new JMonthChooser();
        instance.setFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateUI method, of class JMonthChooser.
     */
    @Test
    public void testUpdateUI() {
        System.out.println("updateUI");
        JMonthChooser instance = new JMonthChooser();
        instance.updateUI();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class JMonthChooser.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] s = null;
        JMonthChooser.main(s);
        
        fail("The test case is a prototype.");
    }

}