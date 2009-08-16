
package mpv5.utils.renderer;

import java.awt.Component;
import javax.swing.JList;
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
public class ComboBoxRendererForTooltipTest {

    public ComboBoxRendererForTooltipTest() {
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
     * Test of getListCellRendererComponent method, of class ComboBoxRendererForTooltip.
     */
    @Test
    public void testGetListCellRendererComponent() {
        System.out.println("getListCellRendererComponent");
        JList list = null;
        Object value = null;
        int index = 0;
        boolean isSelected = false;
        boolean cellHasFocus = false;
        ComboBoxRendererForTooltip instance = new ComboBoxRendererForTooltip();
        Component expResult = null;
        Component result = instance.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}