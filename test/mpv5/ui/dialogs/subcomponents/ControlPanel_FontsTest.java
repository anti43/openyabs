
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.Font;
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
public class ControlPanel_FontsTest {

    public ControlPanel_FontsTest() {
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
     * Test of applyFont method, of class ControlPanel_Fonts.
     */
    @Test
    public void testApplyFont() {
        System.out.println("applyFont");
        Font font = null;
        ControlPanel_Fonts.applyFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class ControlPanel_Fonts.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Fonts instance = new ControlPanel_Fonts();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Fonts.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Fonts instance = new ControlPanel_Fonts();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Fonts.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Fonts instance = new ControlPanel_Fonts();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Fonts.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Fonts instance = new ControlPanel_Fonts();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}