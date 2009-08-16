
package mpv5.ui.panels;

import java.awt.Component;
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
public class HistoryPanelTest {

    public HistoryPanelTest() {
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
     * Test of instanceOf method, of class HistoryPanel.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        Component expResult = null;
        Component result = HistoryPanel.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class HistoryPanel.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        HistoryPanel instance = new HistoryPanel();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class HistoryPanel.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        HistoryPanel instance = new HistoryPanel();
        instance.flush();
        
        fail("The test case is a prototype.");
    }

}