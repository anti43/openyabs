
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
public class ControlPanel_FormatsTest {

    public ControlPanel_FormatsTest() {
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
     * Test of setValues method, of class ControlPanel_Formats.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Formats instance = new ControlPanel_Formats();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Formats.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Formats instance = new ControlPanel_Formats();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Formats.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Formats instance = new ControlPanel_Formats();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ControlPanel_Formats.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ControlPanel_Formats instance = new ControlPanel_Formats();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Formats.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Formats instance = new ControlPanel_Formats();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}