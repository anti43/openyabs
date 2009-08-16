
package mpv5.ui.dialogs;

import java.awt.Component;
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
public class PopupTest {

    public PopupTest() {
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
     * Test of Enter_Value method, of class Popup.
     */
    @Test
    public void testEnter_Value() {
        System.out.println("Enter_Value");
        Object message = null;
        String expResult = "";
        String result = Popup.Enter_Value(message);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Y_N_dialog method, of class Popup.
     */
    @Test
    public void testY_N_dialog_Object() {
        System.out.println("Y_N_dialog");
        Object text = null;
        boolean expResult = false;
        boolean result = Popup.Y_N_dialog(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Y_N_dialog method, of class Popup.
     */
    @Test
    public void testY_N_dialog_Object_Object() {
        System.out.println("Y_N_dialog");
        Object text = null;
        Object label = null;
        boolean expResult = false;
        boolean result = Popup.Y_N_dialog(text, label);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Y_N_dialog method, of class Popup.
     */
    @Test
    public void testY_N_dialog_3args() {
        System.out.println("Y_N_dialog");
        Component parent = null;
        Object text = null;
        Object label = null;
        boolean expResult = false;
        boolean result = Popup.Y_N_dialog(parent, text, label);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of OK_dialog method, of class Popup.
     */
    @Test
    public void testOK_dialog() {
        System.out.println("OK_dialog");
        Object text = null;
        String label = "";
        boolean expResult = false;
        boolean result = Popup.OK_dialog(text, label);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of notice method, of class Popup.
     */
    @Test
    public void testNotice_Component_Object() {
        System.out.println("notice");
        Component parent = null;
        Object text = null;
        Popup.notice(parent, text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of warn method, of class Popup.
     */
    @Test
    public void testWarn_Component_Object() {
        System.out.println("warn");
        Component parent = null;
        Object text = null;
        Popup.warn(parent, text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of error method, of class Popup.
     */
    @Test
    public void testError_Component_Object() {
        System.out.println("error");
        Component parent = null;
        Object text = null;
        Popup.error(parent, text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of warn method, of class Popup.
     */
    @Test
    public void testWarn_Object() {
        System.out.println("warn");
        Object text = null;
        Popup.warn(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of notice method, of class Popup.
     */
    @Test
    public void testNotice_Object() {
        System.out.println("notice");
        Object text = null;
        Popup.notice(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of notice method, of class Popup.
     */
    @Test
    public void testNotice_3args() {
        System.out.println("notice");
        Object text = null;
        int boxWidth = 0;
        int boxLength = 0;
        Popup.notice(text, boxWidth, boxLength);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of error method, of class Popup.
     */
    @Test
    public void testError_Exception() {
        System.out.println("error");
        Exception x = null;
        Popup.error(x);
        
        fail("The test case is a prototype.");
    }

}