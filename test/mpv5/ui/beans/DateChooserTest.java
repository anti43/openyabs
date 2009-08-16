
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
public class DateChooserTest {

    public DateChooserTest() {
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
     * Test of hasDate method, of class DateChooser.
     */
    @Test
    public void testHasDate() {
        System.out.println("hasDate");
        DateChooser instance = new DateChooser();
        boolean expResult = false;
        boolean result = instance.hasDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDate method, of class DateChooser.
     */
    @Test
    public void testSetDate() {
        System.out.println("setDate");
        Date date = null;
        DateChooser instance = new DateChooser();
        instance.setDate(date);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class DateChooser.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        DateChooser instance = new DateChooser();
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}