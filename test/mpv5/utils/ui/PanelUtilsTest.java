
package mpv5.utils.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
public class PanelUtilsTest {

    public PanelUtilsTest() {
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
     * Test of cut method, of class PanelUtils.
     */
    @Test
    public void testCut() {
        System.out.println("cut");
        JTextField jTextField = null;
        int length = 0;
        PanelUtils.cut(jTextField, length);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearText method, of class PanelUtils.
     */
    @Test
    public void testClearText() {
        System.out.println("clearText");
        JPanel panel = null;
        PanelUtils.clearText(panel);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of enableSubComponents method, of class PanelUtils.
     */
    @Test
    public void testEnableSubComponents() {
        System.out.println("enableSubComponents");
        JComponent component = null;
        boolean state = false;
        PanelUtils.enableSubComponents(component, state);
        
        fail("The test case is a prototype.");
    }

}