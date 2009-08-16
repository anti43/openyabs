
package mpv5.ui.misc;

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
public class CloseableTabbedPaneListenerTest {

    public CloseableTabbedPaneListenerTest() {
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
     * Test of closeTab method, of class CloseableTabbedPaneListener.
     */
    @Test
    public void testCloseTab() {
        System.out.println("closeTab");
        int tabIndexToClose = 0;
        CloseableTabbedPaneListener instance = new CloseableTabbedPaneListenerImpl();
        boolean expResult = false;
        boolean result = instance.closeTab(tabIndexToClose);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class CloseableTabbedPaneListenerImpl   implements CloseableTabbedPaneListener {

        public boolean closeTab(int tabIndexToClose) {
            return false;
        }
    }

}