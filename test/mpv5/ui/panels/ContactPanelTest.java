
package mpv5.ui.panels;

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
public class ContactPanelTest {

    public ContactPanelTest() {
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
     * Test of getDataOwner method, of class ContactPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        ContactPanel instance = null;
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class ContactPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean populate = false;
        ContactPanel instance = null;
        instance.setDataOwner(object, populate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setType method, of class ContactPanel.
     */
    @Test
    public void testSetType() {
        System.out.println("setType");
        int type = 0;
        ContactPanel instance = null;
//        instance.setType(type);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showRequiredFields method, of class ContactPanel.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        ContactPanel instance = null;
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of collectData method, of class ContactPanel.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        ContactPanel instance = null;
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class ContactPanel.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        ContactPanel instance = null;
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ContactPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ContactPanel instance = null;
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class ContactPanel.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        ContactPanel instance = null;
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class ContactPanel.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        ContactPanel instance = null;
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class ContactPanel.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        ContactPanel instance = null;
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class ContactPanel.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        ContactPanel instance = null;
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

}