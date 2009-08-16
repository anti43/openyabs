
package mpv5.utils.html;

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
public class HtmlParserTest {

    public HtmlParserTest() {
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
     * Test of getHtml method, of class HtmlParser.
     */
    @Test
    public void testGetHtml() {
        System.out.println("getHtml");
        HtmlParser instance = new HtmlParser();
        String expResult = "";
        String result = instance.getHtml();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of markHtml method, of class HtmlParser.
     */
    @Test
    public void testMarkHtml() {
        System.out.println("markHtml");
        String key = "";
        HtmlParser instance = new HtmlParser();
        String expResult = "";
        String result = instance.markHtml(key);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMarkedHtml method, of class HtmlParser.
     */
    @Test
    public void testGetMarkedHtml() {
        System.out.println("getMarkedHtml");
        Object[][] data = null;
        int column = 0;
        String keyToBeMarked = "";
        String[][] expResult = null;
        String[][] result = HtmlParser.getMarkedHtml(data, column, keyToBeMarked);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}