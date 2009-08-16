
package mpv5.ui.panels;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.utils.models.MPComboBoxModelItem;
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
public class ItemPanelTest {

    public ItemPanelTest() {
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
     * Test of getDataOwner method, of class ItemPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        ItemPanel instance = null;
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class ItemPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean populate = false;
        ItemPanel instance = null;
        instance.setDataOwner(object, populate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showRequiredFields method, of class ItemPanel.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        ItemPanel instance = null;
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of collectData method, of class ItemPanel.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        ItemPanel instance = null;
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class ItemPanel.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        ItemPanel instance = null;
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ItemPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ItemPanel instance = null;
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class ItemPanel.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        ItemPanel instance = null;
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class ItemPanel.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        ItemPanel instance = null;
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class ItemPanel.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        ItemPanel instance = null;
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class ItemPanel.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        ItemPanel instance = null;
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeSelection method, of class ItemPanel.
     */
    @Test
    public void testChangeSelection() {
        System.out.println("changeSelection");
        MPComboBoxModelItem to = null;
        Context c = null;
        ItemPanel instance = null;
        instance.changeSelection(to, c);
        
        fail("The test case is a prototype.");
    }

}