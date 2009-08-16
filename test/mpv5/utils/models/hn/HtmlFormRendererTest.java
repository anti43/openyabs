
package mpv5.utils.models.hn;

import java.io.OutputStream;
import java.util.Map;
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
public class HtmlFormRendererTest {

    public HtmlFormRendererTest() {
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
     * Test of parseHtml method, of class HtmlFormRenderer.
     */
    @Test
    public void testParseHtml() {
        System.out.println("parseHtml");
        String htmlform = "";
        Map<String, String> map = null;
        HtmlFormRenderer instance = new HtmlFormRenderer();
        OutputStream expResult = null;
        OutputStream result = instance.parseHtml(htmlform, map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}