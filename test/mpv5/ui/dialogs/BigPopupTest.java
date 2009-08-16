
package mpv5.ui.dialogs;

import javax.swing.JComponent;
import javax.swing.JPanel;
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
public class BigPopupTest {

    public BigPopupTest() {
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
     * Test of showPopup method, of class BigPopup.
     */
    @Test
    public void testShowPopup() {
        System.out.println("showPopup");
        JComponent parent = null;
        JPanel content = null;
        BigPopup.showPopup(parent, content);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class BigPopup.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        JPanel panel = null;
        BigPopup.close(panel);
        
        fail("The test case is a prototype.");
    }

}