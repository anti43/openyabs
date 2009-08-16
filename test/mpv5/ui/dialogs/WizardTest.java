
package mpv5.ui.dialogs;

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
public class WizardTest {

    public WizardTest() {
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
     * Test of showWiz method, of class Wizard.
     */
    @Test
    public void testShowWiz() {
        System.out.println("showWiz");
        Wizard instance = null;
        instance.showWiz();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPanel method, of class Wizard.
     */
    @Test
    public void testAddPanel() {
        System.out.println("addPanel");
        Wizardable panel = null;
        Wizard instance = null;
        instance.addPanel(panel);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEnd method, of class Wizard.
     */
    @Test
    public void testIsEnd() {
        System.out.println("isEnd");
        boolean end = false;
        Wizard instance = null;
        instance.isEnd(end);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStore method, of class Wizard.
     */
    @Test
    public void testGetStore() {
        System.out.println("getStore");
        Wizard instance = null;
        PropertyStore expResult = null;
        PropertyStore result = instance.getStore();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMessage method, of class Wizard.
     */
    @Test
    public void testSetMessage() {
        System.out.println("setMessage");
        String message = "";
        Wizard instance = null;
        instance.setMessage(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class Wizard.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Wizard.main(args);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNext method, of class Wizard.
     */
    @Test
    public void testGetNext() {
        System.out.println("getNext");
        Wizard instance = null;
        Wizardable expResult = null;
        Wizardable result = instance.getNext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}