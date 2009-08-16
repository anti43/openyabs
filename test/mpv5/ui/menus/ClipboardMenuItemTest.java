
package mpv5.ui.menus;

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
public class ClipboardMenuItemTest {

    public ClipboardMenuItemTest() {
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
     * Test of getItem method, of class ClipboardMenuItem.
     */
    @Test
    public void testGetItem() {
        System.out.println("getItem");
        ClipboardMenuItem instance = null;
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}