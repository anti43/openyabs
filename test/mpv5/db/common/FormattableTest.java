
package mpv5.db.common;

import mpv5.handler.FormatHandler;
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
public class FormattableTest {

    public FormattableTest() {
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
     * Test of getFormatHandler method, of class Formattable.
     */
    @Test
    public void testGetFormatHandler() {
        System.out.println("getFormatHandler");
        Formattable instance = new FormattableImpl();
        FormatHandler expResult = null;
        FormatHandler result = instance.getFormatHandler();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class FormattableImpl  implements Formattable {

        public FormatHandler getFormatHandler() {
            return null;
        }
    }

}