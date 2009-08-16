
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
import mpv5.ui.dialogs.ControlApplet;
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
public class ControlPanel_LocalizationTest {

    public ControlPanel_LocalizationTest() {
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
     * Test of setValues method, of class ControlPanel_Localization.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Localization instance = new ControlPanel_Localization();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Localization.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Localization instance = new ControlPanel_Localization();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Localization.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Localization instance = new ControlPanel_Localization();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of instanceOf method, of class ControlPanel_Localization.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        ControlPanel_Localization instance = new ControlPanel_Localization();
        ControlApplet expResult = null;
        ControlApplet result = instance.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Localization.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Localization instance = new ControlPanel_Localization();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}