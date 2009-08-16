
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
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
public class ControlPanel_LocaleTest {

    public ControlPanel_LocaleTest() {
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
     * Test of setValues method, of class ControlPanel_Locale.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Locale instance = new ControlPanel_Locale();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Locale.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Locale instance = new ControlPanel_Locale();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Locale.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Locale instance = new ControlPanel_Locale();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Locale.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Locale instance = new ControlPanel_Locale();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}