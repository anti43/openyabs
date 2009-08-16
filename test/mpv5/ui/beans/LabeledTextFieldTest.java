
package mpv5.ui.beans;

import java.awt.Font;
import javax.swing.JTextField;
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
public class LabeledTextFieldTest {

    public LabeledTextFieldTest() {
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
     * Test of getTextField method, of class LabeledTextField.
     */
    @Test
    public void testGetTextField() {
        System.out.println("getTextField");
        LabeledTextField instance = new LabeledTextField();
        JTextField expResult = null;
        JTextField result = instance.getTextField();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasText method, of class LabeledTextField.
     */
    @Test
    public void testHasText() {
        System.out.println("hasText");
        LabeledTextField instance = new LabeledTextField();
        boolean expResult = false;
        boolean result = instance.hasText();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setText method, of class LabeledTextField.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        String text = "";
        LabeledTextField instance = new LabeledTextField();
        instance.setText(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Text method, of class LabeledTextField.
     */
    @Test
    public void testGet_Text() {
        System.out.println("get_Text");
        LabeledTextField instance = new LabeledTextField();
        String expResult = "";
        String result = instance.get_Text();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Text method, of class LabeledTextField.
     */
    @Test
    public void testSet_Text() {
        System.out.println("set_Text");
        Object text = null;
        LabeledTextField instance = new LabeledTextField();
        instance.set_Text(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Label method, of class LabeledTextField.
     */
    @Test
    public void testGet_Label() {
        System.out.println("get_Label");
        LabeledTextField instance = new LabeledTextField();
        String expResult = "";
        String result = instance.get_Label();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Label method, of class LabeledTextField.
     */
    @Test
    public void testSet_Label() {
        System.out.println("set_Label");
        String label = "";
        LabeledTextField instance = new LabeledTextField();
        instance.set_Label(label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_LabelFont method, of class LabeledTextField.
     */
    @Test
    public void testSet_LabelFont() {
        System.out.println("set_LabelFont");
        Font font = null;
        LabeledTextField instance = new LabeledTextField();
        instance.set_LabelFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LabeledTextField.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LabeledTextField instance = new LabeledTextField();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_ValueClass method, of class LabeledTextField.
     */
    @Test
    public void testSet_ValueClass() {
        System.out.println("set_ValueClass");
        Class clazz = null;
        LabeledTextField instance = new LabeledTextField();
        instance.set_ValueClass(clazz);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getText method, of class LabeledTextField.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        LabeledTextField instance = new LabeledTextField();
        String expResult = "";
        String result = instance.getText();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}