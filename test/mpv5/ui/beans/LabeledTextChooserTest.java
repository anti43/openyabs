
package mpv5.ui.beans;

import java.awt.Font;
import javax.swing.filechooser.FileFilter;
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
public class LabeledTextChooserTest {

    public LabeledTextChooserTest() {
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
     * Test of hasText method, of class LabeledTextChooser.
     */
    @Test
    public void testHasText() {
        System.out.println("hasText");
        LabeledTextChooser instance = new LabeledTextChooser();
        boolean expResult = false;
        boolean result = instance.hasText();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilter method, of class LabeledTextChooser.
     */
    @Test
    public void testSetFilter() {
        System.out.println("setFilter");
        FileFilter f = null;
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.setFilter(f);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Text method, of class LabeledTextChooser.
     */
    @Test
    public void testGet_Text() {
        System.out.println("get_Text");
        boolean check = false;
        LabeledTextChooser instance = new LabeledTextChooser();
        String expResult = "";
        String result = instance.get_Text(check);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Text method, of class LabeledTextChooser.
     */
    @Test
    public void testSet_Text() {
        System.out.println("set_Text");
        String text = "";
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.set_Text(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Label method, of class LabeledTextChooser.
     */
    @Test
    public void testGet_Label() {
        System.out.println("get_Label");
        LabeledTextChooser instance = new LabeledTextChooser();
        String expResult = "";
        String result = instance.get_Label();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Label method, of class LabeledTextChooser.
     */
    @Test
    public void testSet_Label() {
        System.out.println("set_Label");
        String label = "";
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.set_Label(label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LabeledTextChooser.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_LabelFont method, of class LabeledTextChooser.
     */
    @Test
    public void testSet_LabelFont() {
        System.out.println("set_LabelFont");
        Font ignore = null;
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.set_LabelFont(ignore);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMode method, of class LabeledTextChooser.
     */
    @Test
    public void testGetMode() {
        System.out.println("getMode");
        LabeledTextChooser instance = new LabeledTextChooser();
        int expResult = 0;
        int result = instance.getMode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMode method, of class LabeledTextChooser.
     */
    @Test
    public void testSetMode() {
        System.out.println("setMode");
        int mode = 0;
        LabeledTextChooser instance = new LabeledTextChooser();
        instance.setMode(mode);
        
        fail("The test case is a prototype.");
    }

}