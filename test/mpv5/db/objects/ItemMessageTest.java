
package mpv5.db.objects;

import java.util.ArrayList;
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
public class ItemMessageTest {

    public ItemMessageTest() {
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
     * Test of getView method, of class ItemMessage.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        ItemMessage instance = new ItemMessage();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemsWithMessage method, of class ItemMessage.
     */
    @Test
    public void testGetItemsWithMessage() throws Exception {
        System.out.println("getItemsWithMessage");
        ItemMessage instance = new ItemMessage();
        ArrayList expResult = null;
        ArrayList result = instance.getItemsWithMessage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMessagesOfItem method, of class ItemMessage.
     */
    @Test
    public void testGetMessagesOfItem() throws Exception {
        System.out.println("getMessagesOfItem");
        Item item = null;
        ArrayList expResult = null;
        ArrayList result = ItemMessage.getMessagesOfItem(item);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class ItemMessage.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        ItemMessage instance = new ItemMessage();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}