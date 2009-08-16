
package mpv5.ui.popups;

import javax.swing.JTable;
import mpv5.db.common.Context;
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
public class DOTablePopUpTest {

    public DOTablePopUpTest() {
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
     * Test of addDefaultPopupMenu method, of class DOTablePopUp.
     */
    @Test
    public void testAddDefaultPopupMenu() {
        System.out.println("addDefaultPopupMenu");
        JTable to = null;
        Context c = null;
        DOTablePopUp.addDefaultPopupMenu(to, c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class DOTablePopUp.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        JTable to = null;
        DOTablePopUp.clear(to);
        
        fail("The test case is a prototype.");
    }

}