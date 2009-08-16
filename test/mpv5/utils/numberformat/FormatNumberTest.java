
package mpv5.utils.numberformat;

import java.text.NumberFormat;
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
public class FormatNumberTest {

    public FormatNumberTest() {
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
     * Test of checkDezimal method, of class FormatNumber.
     */
    @Test
    public void testCheckDezimal() {
        System.out.println("checkDezimal");
        String text = "";
        boolean expResult = false;
        boolean result = FormatNumber.checkDezimal(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultDecimalFormat method, of class FormatNumber.
     */
    @Test
    public void testGetDefaultDecimalFormat() {
        System.out.println("getDefaultDecimalFormat");
        NumberFormat expResult = null;
        NumberFormat result = FormatNumber.getDefaultDecimalFormat();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatDezimal method, of class FormatNumber.
     */
    @Test
    public void testFormatDezimal_Double() {
        System.out.println("formatDezimal");
        Double number = null;
        String expResult = "";
        String result = FormatNumber.formatDezimal(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of round method, of class FormatNumber.
     */
    @Test
    public void testRound() {
        System.out.println("round");
        double number = 0.0;
        Double expResult = null;
        Double result = FormatNumber.round(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parseDezimal method, of class FormatNumber.
     */
    @Test
    public void testParseDezimal() {
        System.out.println("parseDezimal");
        String number = "";
        Double expResult = null;
        Double result = FormatNumber.parseDezimal(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatDezimal method, of class FormatNumber.
     */
    @Test
    public void testFormatDezimal_Float() {
        System.out.println("formatDezimal");
        Float number = null;
        String expResult = "";
        String result = FormatNumber.formatDezimal(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatLokalCurrency method, of class FormatNumber.
     */
    @Test
    public void testFormatLokalCurrency() {
        System.out.println("formatLokalCurrency");
        Double betrag = null;
        String expResult = "";
        String result = FormatNumber.formatLokalCurrency(betrag);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatPercent method, of class FormatNumber.
     */
    @Test
    public void testFormatPercent() {
        System.out.println("formatPercent");
        double number = 0.0;
        String expResult = "";
        String result = FormatNumber.formatPercent(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkNumber method, of class FormatNumber.
     */
    @Test
    public void testCheckNumber() {
        System.out.println("checkNumber");
        Object number = null;
        boolean expResult = false;
        boolean result = FormatNumber.checkNumber(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parseNumber method, of class FormatNumber.
     */
    @Test
    public void testParseNumber() {
        System.out.println("parseNumber");
        Object number = null;
        Double expResult = null;
        Double result = FormatNumber.parseNumber(number);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}