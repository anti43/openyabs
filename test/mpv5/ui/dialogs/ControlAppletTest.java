
package mpv5.ui.dialogs;

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
public class ControlAppletTest {

    public ControlAppletTest() {
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
     * Test of getAndRemoveActionPanel method, of class ControlApplet.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlApplet instance = new ControlAppletImpl();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class ControlApplet.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlApplet instance = new ControlAppletImpl();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlApplet.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlApplet instance = new ControlAppletImpl();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlApplet.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlApplet instance = new ControlAppletImpl();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    public class ControlAppletImpl  implements ControlApplet {

        public Component getAndRemoveActionPanel() {
            return null;
        }

        public void setValues(PropertyStore values) {
        }

        public String getUname() {
            return "";
        }

        public void reset() {
        }
    }

}