
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.io.File;
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
public class ControlPanel_PluginsTest {

    public ControlPanel_PluginsTest() {
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
     * Test of importf method, of class ControlPanel_Plugins.
     */
    @Test
    public void testImportf() throws Exception {
        System.out.println("importf");
        File file = null;
        ControlPanel_Plugins instance = new ControlPanel_Plugins();
        instance.importf(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class ControlPanel_Plugins.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Plugins instance = new ControlPanel_Plugins();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Plugins.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Plugins instance = new ControlPanel_Plugins();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Plugins.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Plugins instance = new ControlPanel_Plugins();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Plugins.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Plugins instance = new ControlPanel_Plugins();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}