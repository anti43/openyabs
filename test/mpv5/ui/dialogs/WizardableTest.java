
package mpv5.ui.dialogs;

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
public class WizardableTest {

    public WizardableTest() {
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
     * Test of next method, of class Wizardable.
     */
    @Test
    public void testNext() {
        System.out.println("next");
        Wizardable instance = new WizardableImpl();
        boolean expResult = false;
        boolean result = instance.next();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of back method, of class Wizardable.
     */
    @Test
    public void testBack() {
        System.out.println("back");
        Wizardable instance = new WizardableImpl();
        boolean expResult = false;
        boolean result = instance.back();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of load method, of class Wizardable.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        Wizardable instance = new WizardableImpl();
        instance.load();
        
        fail("The test case is a prototype.");
    }

    public class WizardableImpl implements Wizardable {

        public boolean next() {
            return false;
        }

        public boolean back() {
            return false;
        }

        public void load() {
        }
    }

}