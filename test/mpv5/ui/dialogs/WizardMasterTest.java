
package mpv5.ui.dialogs;

import java.awt.Cursor;
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
public class WizardMasterTest {

    public WizardMasterTest() {
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
     * Test of dispose method, of class WizardMaster.
     */
    @Test
    public void testDispose() {
        System.out.println("dispose");
        WizardMaster instance = new WizardMasterImpl();
        instance.dispose();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNext method, of class WizardMaster.
     */
    @Test
    public void testGetNext() {
        System.out.println("getNext");
        WizardMaster instance = new WizardMasterImpl();
        Wizardable expResult = null;
        Wizardable result = instance.getNext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEnd method, of class WizardMaster.
     */
    @Test
    public void testIsEnd() {
        System.out.println("isEnd");
        boolean end = false;
        WizardMaster instance = new WizardMasterImpl();
        instance.isEnd(end);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStore method, of class WizardMaster.
     */
    @Test
    public void testGetStore() {
        System.out.println("getStore");
        WizardMaster instance = new WizardMasterImpl();
        PropertyStore expResult = null;
        PropertyStore result = instance.getStore();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCursor method, of class WizardMaster.
     */
    @Test
    public void testSetCursor_int() {
        System.out.println("setCursor");
        int DEFAULT_CURSOR = 0;
        WizardMaster instance = new WizardMasterImpl();
        instance.setCursor(DEFAULT_CURSOR);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCursor method, of class WizardMaster.
     */
    @Test
    public void testSetCursor_Cursor() {
        System.out.println("setCursor");
        Cursor cursor = null;
        WizardMaster instance = new WizardMasterImpl();
        instance.setCursor(cursor);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMessage method, of class WizardMaster.
     */
    @Test
    public void testSetMessage() {
        System.out.println("setMessage");
        String message = "";
        WizardMaster instance = new WizardMasterImpl();
        instance.setMessage(message);
        
        fail("The test case is a prototype.");
    }

    public class WizardMasterImpl  implements WizardMaster {

        public void dispose() {
        }

        public Wizardable getNext() {
            return null;
        }

        public void isEnd(boolean end) {
        }

        public PropertyStore getStore() {
            return null;
        }

        public void setCursor(int DEFAULT_CURSOR) {
        }

        public void setCursor(Cursor cursor) {
        }

        public void setMessage(String message) {
        }
    }

}