
package mpv5.ui.misc;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
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
public class CloseableTabbedPaneTest {

    public CloseableTabbedPaneTest() {
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
     * Test of setCloseIcons method, of class CloseableTabbedPane.
     */
    @Test
    public void testSetCloseIcons() {
        System.out.println("setCloseIcons");
        Icon normal = null;
        Icon hoover = null;
        Icon pressed = null;
        CloseableTabbedPane instance = null;
        instance.setCloseIcons(normal, hoover, pressed);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedComponent method, of class CloseableTabbedPane.
     */
    @Test
    public void testSetSelectedComponent() {
        System.out.println("setSelectedComponent");
        Component component = null;
        CloseableTabbedPane instance = null;
        instance.setSelectedComponent(component);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedIndex method, of class CloseableTabbedPane.
     */
    @Test
    public void testSetSelectedIndex() {
        System.out.println("setSelectedIndex");
        int index = 0;
        CloseableTabbedPane instance = null;
        instance.setSelectedIndex(index);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTab method, of class CloseableTabbedPane.
     */
    @Test
    public void testAddTab_String_Component() {
        System.out.println("addTab");
        String title = "";
        Component component = null;
        CloseableTabbedPane instance = null;
        instance.addTab(title, component);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTab method, of class CloseableTabbedPane.
     */
    @Test
    public void testAddTab_3args() {
        System.out.println("addTab");
        String title = "";
        Component component = null;
        Icon extraIcon = null;
        CloseableTabbedPane instance = null;
        instance.addTab(title, component, extraIcon);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseClicked method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseClicked() {
        System.out.println("mouseClicked");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseClicked(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseEntered method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseEntered() {
        System.out.println("mouseEntered");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseEntered(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseExited method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseExited() {
        System.out.println("mouseExited");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseExited(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mousePressed method, of class CloseableTabbedPane.
     */
    @Test
    public void testMousePressed() {
        System.out.println("mousePressed");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mousePressed(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseReleased method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseReleased() {
        System.out.println("mouseReleased");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseReleased(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseDragged method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseDragged() {
        System.out.println("mouseDragged");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseDragged(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of mouseMoved method, of class CloseableTabbedPane.
     */
    @Test
    public void testMouseMoved() {
        System.out.println("mouseMoved");
        MouseEvent e = null;
        CloseableTabbedPane instance = null;
        instance.mouseMoved(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCloseableTabbedPaneListener method, of class CloseableTabbedPane.
     */
    @Test
    public void testAddCloseableTabbedPaneListener() {
        System.out.println("addCloseableTabbedPaneListener");
        CloseableTabbedPaneListener l = null;
        CloseableTabbedPane instance = null;
        instance.addCloseableTabbedPaneListener(l);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeCloseableTabbedPaneListener method, of class CloseableTabbedPane.
     */
    @Test
    public void testRemoveCloseableTabbedPaneListener() {
        System.out.println("removeCloseableTabbedPaneListener");
        CloseableTabbedPaneListener l = null;
        CloseableTabbedPane instance = null;
        instance.removeCloseableTabbedPaneListener(l);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCloseableTabbedPaneListener method, of class CloseableTabbedPane.
     */
    @Test
    public void testGetCloseableTabbedPaneListener() {
        System.out.println("getCloseableTabbedPaneListener");
        CloseableTabbedPane instance = null;
        CloseableTabbedPaneListener[] expResult = null;
        CloseableTabbedPaneListener[] result = instance.getCloseableTabbedPaneListener();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fireCloseTab method, of class CloseableTabbedPane.
     */
    @Test
    public void testFireCloseTab() {
        System.out.println("fireCloseTab");
        int tabIndexToClose = 0;
        CloseableTabbedPane instance = null;
        boolean expResult = false;
        boolean result = instance.fireCloseTab(tabIndexToClose);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}