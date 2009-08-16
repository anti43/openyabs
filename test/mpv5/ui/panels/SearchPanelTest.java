
package mpv5.ui.panels;

import mpv5.db.common.Context;
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
public class SearchPanelTest {

    public SearchPanelTest() {
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
     * Test of refresh method, of class SearchPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        SearchPanel instance = new SearchPanel();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContextOwner method, of class SearchPanel.
     */
    @Test
    public void testSetContextOwner() {
        System.out.println("setContextOwner");
        DatabaseObject object = null;
        SearchPanel instance = new SearchPanel();
        instance.setContextOwner(object);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContext method, of class SearchPanel.
     */
    @Test
    public void testGetContext() {
        System.out.println("getContext");
        SearchPanel instance = new SearchPanel();
        Context expResult = null;
        Context result = instance.getContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class SearchPanel.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context context = null;
        SearchPanel instance = new SearchPanel();
        instance.setContext(context);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of search method, of class SearchPanel.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        SearchPanel instance = new SearchPanel();
        instance.search();
        
        fail("The test case is a prototype.");
    }

}