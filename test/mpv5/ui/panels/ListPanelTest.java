
package mpv5.ui.panels;

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
public class ListPanelTest {

    public ListPanelTest() {
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
     * Test of refresh method, of class ListPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ListPanel instance = new ListPanelImpl();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class ListPanel.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        ListPanel instance = new ListPanelImpl();
        instance.flush();
        
        fail("The test case is a prototype.");
    }

    public class ListPanelImpl  implements ListPanel {

        public void refresh() {
        }

        public void flush() {
        }
    }

}