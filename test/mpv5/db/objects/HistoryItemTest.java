
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.utils.images.MPIcon;
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
public class HistoryItemTest {

    public HistoryItemTest() {
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
     * Test of __getUsername method, of class HistoryItem.
     */
    @Test
    public void test__getUsername() {
        System.out.println("__getUsername");
        HistoryItem instance = new HistoryItem();
        String expResult = "";
        String result = instance.__getUsername();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsername method, of class HistoryItem.
     */
    @Test
    public void testSetUsername() {
        System.out.println("setUsername");
        String user = "";
        HistoryItem instance = new HistoryItem();
        instance.setUsername(user);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDbidentity method, of class HistoryItem.
     */
    @Test
    public void test__getDbidentity() {
        System.out.println("__getDbidentity");
        HistoryItem instance = new HistoryItem();
        String expResult = "";
        String result = instance.__getDbidentity();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDbidentity method, of class HistoryItem.
     */
    @Test
    public void testSetDbidentity() {
        System.out.println("setDbidentity");
        String dbidentity = "";
        HistoryItem instance = new HistoryItem();
        instance.setDbidentity(dbidentity);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntitem method, of class HistoryItem.
     */
    @Test
    public void test__getIntitem() {
        System.out.println("__getIntitem");
        HistoryItem instance = new HistoryItem();
        int expResult = 0;
        int result = instance.__getIntitem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntitem method, of class HistoryItem.
     */
    @Test
    public void testSetIntitem() {
        System.out.println("setIntitem");
        int item = 0;
        HistoryItem instance = new HistoryItem();
        instance.setIntitem(item);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class HistoryItem.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        HistoryItem instance = new HistoryItem();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class HistoryItem.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        HistoryItem instance = new HistoryItem();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}