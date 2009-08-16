
package mpv5.ui.dialogs;

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
public class SearchTest {

    public SearchTest() {
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
     * Test of instanceOf method, of class Search.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        Search expResult = null;
        Search result = Search.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of dispose method, of class Search.
     */
    @Test
    public void testDispose() {
        System.out.println("dispose");
        Search instance = new Search();
        instance.dispose();
        
        fail("The test case is a prototype.");
    }

}