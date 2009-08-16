
package mpv5.ui.beans;

import java.awt.Font;
import javax.swing.JSpinner;
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
public class LabeledSpinnerTest {

    public LabeledSpinnerTest() {
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
     * Test of getSpinner method, of class LabeledSpinner.
     */
    @Test
    public void testGetSpinner() {
        System.out.println("getSpinner");
        LabeledSpinner instance = new LabeledSpinner();
        JSpinner expResult = null;
        JSpinner result = instance.getSpinner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Value method, of class LabeledSpinner.
     */
    @Test
    public void testGet_Value() {
        System.out.println("get_Value");
        LabeledSpinner instance = new LabeledSpinner();
        Object expResult = null;
        Object result = instance.get_Value();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Value method, of class LabeledSpinner.
     */
    @Test
    public void testSet_Value() {
        System.out.println("set_Value");
        Object text = null;
        LabeledSpinner instance = new LabeledSpinner();
        instance.set_Value(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Label method, of class LabeledSpinner.
     */
    @Test
    public void testGet_Label() {
        System.out.println("get_Label");
        LabeledSpinner instance = new LabeledSpinner();
        String expResult = "";
        String result = instance.get_Label();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Label method, of class LabeledSpinner.
     */
    @Test
    public void testSet_Label() {
        System.out.println("set_Label");
        String label = "";
        LabeledSpinner instance = new LabeledSpinner();
        instance.set_Label(label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_LabelFont method, of class LabeledSpinner.
     */
    @Test
    public void testSet_LabelFont() {
        System.out.println("set_LabelFont");
        Font font = null;
        LabeledSpinner instance = new LabeledSpinner();
        instance.set_LabelFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LabeledSpinner.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LabeledSpinner instance = new LabeledSpinner();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

}