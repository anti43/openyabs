
package mpv5.utils.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTextField;
import mpv5.ui.beans.LabeledTextField;
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
public class TextFieldUtilsTest {

    public TextFieldUtilsTest() {
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
     * Test of blinkerGrey method, of class TextFieldUtils.
     */
    @Test
    public void testBlinkerGrey() {
        System.out.println("blinkerGrey");
        LabeledTextField field = null;
        TextFieldUtils.blinkerGrey(field);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of blinkerRed method, of class TextFieldUtils.
     */
    @Test
    public void testBlinkerRed_JTextField() {
        System.out.println("blinkerRed");
        JTextField field = null;
        TextFieldUtils.blinkerRed(field);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of blinkerRed method, of class TextFieldUtils.
     */
    @Test
    public void testBlinkerRed_LabeledTextField() {
        System.out.println("blinkerRed");
        LabeledTextField lfield = null;
        TextFieldUtils.blinkerRed(lfield);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of blink method, of class TextFieldUtils.
     */
    @Test
    public void testBlink() {
        System.out.println("blink");
        Component component = null;
        Color color = null;
        TextFieldUtils.blink(component, color);
        
        fail("The test case is a prototype.");
    }

}