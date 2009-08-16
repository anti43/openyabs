
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
public class ControlPanel_AdvancedTest {

    public ControlPanel_AdvancedTest() {
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
     * Test of setData method, of class ControlPanel_Advanced.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        PropertyStore vals = null;
        ControlPanel_Advanced instance = new ControlPanel_Advanced();
        instance.setData(vals);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Advanced.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Advanced instance = new ControlPanel_Advanced();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class ControlPanel_Advanced.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Advanced instance = new ControlPanel_Advanced();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Advanced.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Advanced instance = new ControlPanel_Advanced();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Advanced.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Advanced instance = new ControlPanel_Advanced();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

}