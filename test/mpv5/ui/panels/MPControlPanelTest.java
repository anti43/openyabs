
package mpv5.ui.panels;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
public class MPControlPanelTest {

    public MPControlPanelTest() {
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
     * Test of instanceOf method, of class MPControlPanel.
     */
    @Test
    public void testInstanceOf() {
        System.out.println("instanceOf");
        JComponent expResult = null;
        JComponent result = MPControlPanel.instanceOf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addShortcut method, of class MPControlPanel.
     */
    @Test
    public void testAddShortcut_3args_1() {
        System.out.println("addShortcut");
        Icon icon = null;
        String text = "";
        Class clazz = null;
        MPControlPanel instance = null;
        instance.addShortcut(icon, text, clazz);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addShortcut method, of class MPControlPanel.
     */
    @Test
    public void testAddShortcut_3args_2() {
        System.out.println("addShortcut");
        Icon icon = null;
        String text = "";
        JPanel panel = null;
        MPControlPanel instance = null;
        instance.addShortcut(icon, text, panel);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of openDetails method, of class MPControlPanel.
     */
    @Test
    public void testOpenDetails() {
        System.out.println("openDetails");
        ControlApplet panel = null;
        MPControlPanel instance = null;
        instance.openDetails(panel);
        
        fail("The test case is a prototype.");
    }

}