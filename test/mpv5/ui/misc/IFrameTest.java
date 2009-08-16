
package mpv5.ui.misc;

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
public class IFrameTest {

    public IFrameTest() {
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
     * Test of setContent method, of class IFrame.
     */
    @Test
    public void testSetContent() {
        System.out.println("setContent");
        JPanel panel = null;
        IFrame instance = new IFrame();
        instance.setContent(panel);
        
        fail("The test case is a prototype.");
    }

}