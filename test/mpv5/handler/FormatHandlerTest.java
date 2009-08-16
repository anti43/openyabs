
package mpv5.handler;

import java.text.MessageFormat;
import mpv5.db.common.DatabaseObject;
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
public class FormatHandlerTest {

    public FormatHandlerTest() {
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
     * Test of determineType method, of class FormatHandler.
     */
    @Test
    public void testDetermineType() {
        System.out.println("determineType");
        DatabaseObject obj = null;
        int expResult = 0;
        int result = FormatHandler.determineType(obj);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class FormatHandler.
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        FormatHandler instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormat method, of class FormatHandler.
     */
    @Test
    public void testGetFormat() {
        System.out.println("getFormat");
        FormatHandler instance = null;
        MessageFormat expResult = null;
        MessageFormat result = instance.getFormat();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNextNumber method, of class FormatHandler.
     */
    @Test
    public void testGetNextNumber() {
        System.out.println("getNextNumber");
        FormatHandler instance = null;
        int expResult = 0;
        int result = instance.getNextNumber();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class FormatHandler.
     */
    @Test
    public void testToString_int() {
        System.out.println("toString");
        int number = 0;
        FormatHandler instance = null;
        String expResult = "";
        String result = instance.toString(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getType method, of class FormatHandler.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        FormatHandler instance = null;
        int expResult = 0;
        int result = instance.getType();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setType method, of class FormatHandler.
     */
    @Test
    public void testSetType() {
        System.out.println("setType");
        int type = 0;
        FormatHandler instance = null;
        instance.setType(type);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStartCount method, of class FormatHandler.
     */
    @Test
    public void testGetStartCount() {
        System.out.println("getStartCount");
        FormatHandler instance = null;
        Integer expResult = null;
        Integer result = instance.getStartCount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStartCount method, of class FormatHandler.
     */
    @Test
    public void testSetStartCount() {
        System.out.println("setStartCount");
        Integer startCount = null;
        FormatHandler instance = null;
        instance.setStartCount(startCount);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFormat method, of class FormatHandler.
     */
    @Test
    public void testSetFormat_MessageFormat() {
        System.out.println("setFormat");
        MessageFormat format = null;
        FormatHandler instance = null;
        instance.setFormat(format);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFormat method, of class FormatHandler.
     */
    @Test
    public void testSetFormat_String() {
        System.out.println("setFormat");
        String formatPattern = "";
        FormatHandler instance = null;
        instance.setFormat(formatPattern);
        
        fail("The test case is a prototype.");
    }

}