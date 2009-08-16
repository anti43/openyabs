
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
public class ProductPanelTest {

    public ProductPanelTest() {
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
     * Test of getDataOwner method, of class ProductPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        ProductPanel instance = null;
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class ProductPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean populate = false;
        ProductPanel instance = null;
        instance.setDataOwner(object, populate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showRequiredFields method, of class ProductPanel.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        ProductPanel instance = null;
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of collectData method, of class ProductPanel.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        ProductPanel instance = null;
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class ProductPanel.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        ProductPanel instance = null;
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ProductPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ProductPanel instance = null;
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class ProductPanel.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        ProductPanel instance = null;
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class ProductPanel.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        ProductPanel instance = null;
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class ProductPanel.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        ProductPanel instance = null;
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class ProductPanel.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        ProductPanel instance = null;
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeSelection method, of class ProductPanel.
     */
    @Test
    public void testChangeSelection() {
        System.out.println("changeSelection");
        MPComboBoxModelItem to = null;
        Context c = null;
        ProductPanel instance = null;
        instance.changeSelection(to, c);
        
        fail("The test case is a prototype.");
    }

}