
package mpv5.ui.popups;

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
public class CopyPasteMenuTest {

    public CopyPasteMenuTest() {
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
     * Test of getClipboard method, of class CopyPasteMenu.
     */
    @Test
    public void testGetClipboard() {
        System.out.println("getClipboard");
        String expResult = "";
        String result = CopyPasteMenu.getClipboard();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}