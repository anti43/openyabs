
package mpv5.ui.panels;

import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
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
public class AddressPanelTest {

    public AddressPanelTest() {
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
     * Test of collectData method, of class AddressPanel.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        AddressPanel instance = new AddressPanel();
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class AddressPanel.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        AddressPanel instance = new AddressPanel();
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataOwner method, of class AddressPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        AddressPanel instance = new AddressPanel();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class AddressPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean populate = false;
        AddressPanel instance = new AddressPanel();
        instance.setDataOwner(object, populate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class AddressPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        AddressPanel instance = new AddressPanel();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class AddressPanel.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        AddressPanel instance = new AddressPanel();
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showRequiredFields method, of class AddressPanel.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        AddressPanel instance = new AddressPanel();
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeAddress method, of class AddressPanel.
     */
    @Test
    public void testRemoveAddress() {
        System.out.println("removeAddress");
        AddressPanel instance = new AddressPanel();
        instance.removeAddress();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAddress method, of class AddressPanel.
     */
    @Test
    public void testAddAddress() {
        System.out.println("addAddress");
        AddressPanel instance = new AddressPanel();
        instance.addAddress();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataParent method, of class AddressPanel.
     */
    @Test
    public void testGetDataParent() {
        System.out.println("getDataParent");
        AddressPanel instance = new AddressPanel();
        Contact expResult = null;
        Contact result = instance.getDataParent();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataParent method, of class AddressPanel.
     */
    @Test
    public void testSetDataParent() {
        System.out.println("setDataParent");
        Contact dataParent = null;
        AddressPanel instance = new AddressPanel();
        instance.setDataParent(dataParent);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class AddressPanel.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        AddressPanel instance = new AddressPanel();
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class AddressPanel.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        AddressPanel instance = new AddressPanel();
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class AddressPanel.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        AddressPanel instance = new AddressPanel();
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

}