
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
import mpv5.ui.dialogs.ControlApplet;
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
public class ControlPanel_AccountsTest {

    public ControlPanel_AccountsTest() {
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
     * Test of showRequiredFields method, of class ControlPanel_Accounts.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValues method, of class ControlPanel_Accounts.
     */
    @Test
    public void testSetValues() {
        System.out.println("setValues");
        PropertyStore values = null;
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.setValues(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUname method, of class ControlPanel_Accounts.
     */
    @Test
    public void testGetUname() {
        System.out.println("getUname");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        String expResult = "";
        String result = instance.getUname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class ControlPanel_Accounts.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of instanceOf method, of class ControlPanel_Accounts.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        ControlApplet expResult = null;
        ControlApplet result = instance.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ControlPanel_Accounts.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of collectData method, of class ControlPanel_Accounts.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataOwner method, of class ControlPanel_Accounts.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class ControlPanel_Accounts.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean pop = false;
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.setDataOwner(object, pop);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class ControlPanel_Accounts.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class ControlPanel_Accounts.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class ControlPanel_Accounts.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAndRemoveActionPanel method, of class ControlPanel_Accounts.
     */
    @Test
    public void testGetAndRemoveActionPanel() {
        System.out.println("getAndRemoveActionPanel");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        Component expResult = null;
        Component result = instance.getAndRemoveActionPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class ControlPanel_Accounts.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class ControlPanel_Accounts.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        ControlPanel_Accounts instance = new ControlPanel_Accounts();
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

}