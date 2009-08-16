
package mpv5.ui.popups;

import javax.swing.JTable;
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
public class FileTablePopUpTest {

    public FileTablePopUpTest() {
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
     * Test of instanceOf method, of class FileTablePopUp.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        JTable t = null;
        FileTablePopUp expResult = null;
        FileTablePopUp result = FileTablePopUp.instanceOf(t);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addDefaultPopupMenu method, of class FileTablePopUp.
     */
    @Test
    public void testAddDefaultPopupMenu() {
        System.out.println("addDefaultPopupMenu");
        JTable dataTable = null;
        FileTablePopUp instance = new FileTablePopUp();
        instance.addDefaultPopupMenu(dataTable);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class FileTablePopUp.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        JTable to = null;
        FileTablePopUp.clear(to);
        
        fail("The test case is a prototype.");
    }

}