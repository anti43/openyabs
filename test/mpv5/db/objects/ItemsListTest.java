
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
public class ItemsListTest {

    public ItemsListTest() {
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
     * Test of getView method, of class ItemsList.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        ItemsList instance = new ItemsList();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class ItemsList.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        ItemsList instance = new ItemsList();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class ItemsList.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        ItemsList instance = new ItemsList();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getItemsids method, of class ItemsList.
     */
    @Test
    public void test__getItemsids() {
        System.out.println("__getItemsids");
        ItemsList instance = new ItemsList();
        int expResult = 0;
        int result = instance.__getItemsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setItemsids method, of class ItemsList.
     */
    @Test
    public void testSetItemsids() {
        System.out.println("setItemsids");
        int itemsids = 0;
        ItemsList instance = new ItemsList();
        instance.setItemsids(itemsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class ItemsList.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        ItemsList instance = new ItemsList();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}