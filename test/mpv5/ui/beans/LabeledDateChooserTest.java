
package mpv5.ui.beans;

import java.util.Date;
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
public class LabeledDateChooserTest {

    public LabeledDateChooserTest() {
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
     * Test of getDateChooser method, of class LabeledDateChooser.
     */
    @Test
    public void testGetDateChooser() {
        System.out.println("getDateChooser");
        LabeledDateChooser instance = new LabeledDateChooser();
        DateChooser expResult = null;
        DateChooser result = instance.getDateChooser();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class LabeledDateChooser.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        LabeledDateChooser instance = new LabeledDateChooser();
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class LabeledDateChooser.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        LabeledDateChooser instance = new LabeledDateChooser();
        instance.setDate(date);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Label method, of class LabeledDateChooser.
     */
    @Test
    public void testGet_Label() {
        System.out.println("get_Label");
        LabeledDateChooser instance = new LabeledDateChooser();
        String expResult = "";
        String result = instance.get_Label();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Label method, of class LabeledDateChooser.
     */
    @Test
    public void testSet_Label() {
        System.out.println("set_Label");
        String label = "";
        LabeledDateChooser instance = new LabeledDateChooser();
        instance.set_Label(label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LabeledDateChooser.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LabeledDateChooser instance = new LabeledDateChooser();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

}