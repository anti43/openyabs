
package mpv5.utils.text;

import java.util.Locale;
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
public class TypeConversionTest {

    public TypeConversionTest() {
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
     * Test of booleanToString method, of class TypeConversion.
     */
    @Test
    public void testBooleanToString() {
        System.out.println("booleanToString");
        boolean bool = false;
        String expResult = "";
        String result = TypeConversion.booleanToString(bool);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stringToBoolean method, of class TypeConversion.
     */
    @Test
    public void testStringToBoolean() {
        System.out.println("stringToBoolean");
        String string = "";
        boolean expResult = false;
        boolean result = TypeConversion.stringToBoolean(string);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stringToLocale method, of class TypeConversion.
     */
    @Test
    public void testStringToLocale() {
        System.out.println("stringToLocale");
        String localestring = "";
        Locale expResult = null;
        Locale result = TypeConversion.stringToLocale(localestring);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}