
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
public class DataPanelTest {

    public DataPanelTest() {
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
     * Test of collectData method, of class DataPanel.
     */
    @Test
    public void testCollectData() {
        System.out.println("collectData");
        DataPanel instance = new DataPanelImpl();
        instance.collectData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataOwner method, of class DataPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        DataPanel instance = new DataPanelImpl();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class DataPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject object = null;
        boolean populateData = false;
        DataPanel instance = new DataPanelImpl();
        instance.setDataOwner(object, populateData);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class DataPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        DataPanel instance = new DataPanelImpl();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of exposeData method, of class DataPanel.
     */
    @Test
    public void testExposeData() {
        System.out.println("exposeData");
        DataPanel instance = new DataPanelImpl();
        instance.exposeData();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paste method, of class DataPanel.
     */
    @Test
    public void testPaste() {
        System.out.println("paste");
        DatabaseObject dbo = null;
        DataPanel instance = new DataPanelImpl();
        instance.paste(dbo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showRequiredFields method, of class DataPanel.
     */
    @Test
    public void testShowRequiredFields() {
        System.out.println("showRequiredFields");
        DataPanel instance = new DataPanelImpl();
        instance.showRequiredFields();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showSearchBar method, of class DataPanel.
     */
    @Test
    public void testShowSearchBar() {
        System.out.println("showSearchBar");
        boolean show = false;
        DataPanel instance = new DataPanelImpl();
        instance.showSearchBar(show);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterSave method, of class DataPanel.
     */
    @Test
    public void testActionAfterSave() {
        System.out.println("actionAfterSave");
        DataPanel instance = new DataPanelImpl();
        instance.actionAfterSave();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionAfterCreate method, of class DataPanel.
     */
    @Test
    public void testActionAfterCreate() {
        System.out.println("actionAfterCreate");
        DataPanel instance = new DataPanelImpl();
        instance.actionAfterCreate();
        
        fail("The test case is a prototype.");
    }

    public class DataPanelImpl  implements DataPanel {

        public void collectData() {
        }

        public DatabaseObject getDataOwner() {
            return null;
        }

        public void setDataOwner(DatabaseObject object, boolean populateData) {
        }

        public void refresh() {
        }

        public void exposeData() {
        }

        public void paste(DatabaseObject dbo) {
        }

        public void showRequiredFields() {
        }

        public void showSearchBar(boolean show) {
        }

        public void actionAfterSave() {
        }

        public void actionAfterCreate() {
        }
    }

}