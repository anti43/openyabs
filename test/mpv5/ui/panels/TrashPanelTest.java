
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
public class TrashPanelTest {

    public TrashPanelTest() {
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
     * Test of instanceOf method, of class TrashPanel.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        TrashPanel expResult = null;
        TrashPanel result = TrashPanel.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setData method, of class TrashPanel.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        TrashPanel instance = null;
        instance.setData();
        
        fail("The test case is a prototype.");
    }

}