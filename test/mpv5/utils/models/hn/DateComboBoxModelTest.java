
package mpv5.utils.models.hn;

import javax.swing.ComboBoxModel;
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
public class DateComboBoxModelTest {

    public DateComboBoxModelTest() {
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
     * Test of getModel method, of class DateComboBoxModel.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        DateComboBoxModel instance = new DateComboBoxModel();
        ComboBoxModel expResult = null;
        ComboBoxModel result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartDay method, of class DateComboBoxModel.
     */
    @Test
    public void testGetStartDay() {
        System.out.println("getStartDay");
        DateComboBoxModel instance = new DateComboBoxModel();
        String expResult = "";
        String result = instance.getStartDay();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEndDay method, of class DateComboBoxModel.
     */
    @Test
    public void testGetEndDay() {
        System.out.println("getEndDay");
        DateComboBoxModel instance = new DateComboBoxModel();
        String expResult = "";
        String result = instance.getEndDay();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMode method, of class DateComboBoxModel.
     */
    @Test
    public void testGetMode() {
        System.out.println("getMode");
        DateComboBoxModel instance = new DateComboBoxModel();
        int expResult = 0;
        int result = instance.getMode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getYear method, of class DateComboBoxModel.
     */
    @Test
    public void testGetYear() {
        System.out.println("getYear");
        DateComboBoxModel instance = new DateComboBoxModel();
        String expResult = "";
        String result = instance.getYear();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSelectedItem method, of class DateComboBoxModel.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        DateComboBoxModel instance = new DateComboBoxModel();
        Object expResult = null;
        Object result = instance.getSelectedItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setYear method, of class DateComboBoxModel.
     */
    @Test
    public void testSetYear() {
        System.out.println("setYear");
        String year = "";
        DateComboBoxModel instance = new DateComboBoxModel();
        instance.setYear(year);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMonth method, of class DateComboBoxModel.
     */
    @Test
    public void testGetMonth() {
        System.out.println("getMonth");
        DateComboBoxModel instance = new DateComboBoxModel();
        String expResult = "";
        String result = instance.getMonth();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getQuarter method, of class DateComboBoxModel.
     */
    @Test
    public void testGetQuarter() {
        System.out.println("getQuarter");
        DateComboBoxModel instance = new DateComboBoxModel();
        String expResult = "";
        String result = instance.getQuarter();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}