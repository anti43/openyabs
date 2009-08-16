
package mpv5.ui.frames;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.ui.panels.DataPanel;
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
public class MPViewTest {

    public MPViewTest() {
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
     * Test of addMessage method, of class MPView.
     */
    @Test
    public void testAddMessage_Messages() {
        System.out.println("addMessage");
        Messages message = null;
        MPView.addMessage(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMessage method, of class MPView.
     */
    @Test
    public void testAddMessage_String() {
        System.out.println("addMessage");
        String message = "";
        MPView.addMessage(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getShowingTab method, of class MPView.
     */
    @Test
    public void testGetShowingTab() {
        System.out.println("getShowingTab");
        Object expResult = null;
        Object result = MPView.getShowingTab();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNavBarAnimated method, of class MPView.
     */
    @Test
    public void testSetNavBarAnimated() {
        System.out.println("setNavBarAnimated");
        boolean animated = false;
        MPView.setNavBarAnimated(animated);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTabPaneScrolled method, of class MPView.
     */
    @Test
    public void testSetTabPaneScrolled() {
        System.out.println("setTabPaneScrolled");
        boolean scroll = false;
        MPView.setTabPaneScrolled(scroll);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showFilesaveDialogFor method, of class MPView.
     */
    @Test
    public void testShowFilesaveDialogFor() {
        System.out.println("showFilesaveDialogFor");
        File f = null;
        MPView.showFilesaveDialogFor(f);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of show method, of class MPView.
     */
    @Test
    public void testShow() {
        System.out.println("show");
        JFrame c = null;
        MPView.show(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showError method, of class MPView.
     */
    @Test
    public void testShowError() {
        System.out.println("showError");
        MPView.showError();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refreshFavouritesMenu method, of class MPView.
     */
    @Test
    public void testRefreshFavouritesMenu() {
        System.out.println("refreshFavouritesMenu");
        MPView instance = null;
        instance.refreshFavouritesMenu();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProgressMaximumValue method, of class MPView.
     */
    @Test
    public void testSetProgressMaximumValue() {
        System.out.println("setProgressMaximumValue");
        int max = 0;
        MPView.setProgressMaximumValue(max);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProgressValue method, of class MPView.
     */
    @Test
    public void testSetProgressValue() {
        System.out.println("setProgressValue");
        int val = 0;
        MPView.setProgressValue(val);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProgressReset method, of class MPView.
     */
    @Test
    public void testSetProgressReset() {
        System.out.println("setProgressReset");
        MPView.setProgressReset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProgressRunning method, of class MPView.
     */
    @Test
    public void testSetProgressRunning() {
        System.out.println("setProgressRunning");
        boolean b = false;
        MPView.setProgressRunning(b);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUser method, of class MPView.
     */
    @Test
    public void testGetUser() {
        System.out.println("getUser");
        User expResult = null;
        User result = MPView.getUser();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUser method, of class MPView.
     */
    @Test
    public void testSetUser() {
        System.out.println("setUser");
        User usern = null;
        MPView.setUser(usern);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWaiting method, of class MPView.
     */
    @Test
    public void testSetWaiting() {
        System.out.println("setWaiting");
        boolean truee = false;
        MPView.setWaiting(truee);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentTab method, of class MPView.
     */
    @Test
    public void testGetCurrentTab() {
        System.out.println("getCurrentTab");
        MPView instance = null;
        DataPanel expResult = null;
        DataPanel result = instance.getCurrentTab();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addToClipBoard method, of class MPView.
     */
    @Test
    public void testAddToClipBoard() {
        System.out.println("addToClipBoard");
        DatabaseObject obj = null;
        MPView instance = null;
        instance.addToClipBoard(obj);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of enableToolBar method, of class MPView.
     */
    @Test
    public void testEnableToolBar() {
        System.out.println("enableToolBar");
        boolean enable = false;
        MPView instance = null;
        instance.enableToolBar(enable);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTab method, of class MPView.
     */
    @Test
    public void testAddTab_DatabaseObject() {
        System.out.println("addTab");
        DatabaseObject item = null;
        MPView instance = null;
        instance.addTab(item);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addOrShowTab method, of class MPView.
     */
    @Test
    public void testAddOrShowTab() {
        System.out.println("addOrShowTab");
        JComponent instanceOf = null;
        String label = "";
        MPView instance = null;
        instance.addOrShowTab(instanceOf, label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTab method, of class MPView.
     */
    @Test
    public void testAddTab_JComponent_String() {
        System.out.println("addTab");
        JComponent tab = null;
        String name = "";
        MPView instance = null;
        instance.addTab(tab, name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMainPanel method, of class MPView.
     */
    @Test
    public void testGetMainPanel() {
        System.out.println("getMainPanel");
        MPView instance = null;
        JPanel expResult = null;
        JPanel result = instance.getMainPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProgressBar method, of class MPView.
     */
    @Test
    public void testGetProgressBar() {
        System.out.println("getProgressBar");
        MPView instance = null;
        JProgressBar expResult = null;
        JProgressBar result = instance.getProgressBar();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatusMessageLabel method, of class MPView.
     */
    @Test
    public void testGetStatusMessageLabel() {
        System.out.println("getStatusMessageLabel");
        MPView instance = null;
        JLabel expResult = null;
        JLabel result = instance.getStatusMessageLabel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatusPanel method, of class MPView.
     */
    @Test
    public void testGetStatusPanel() {
        System.out.println("getStatusPanel");
        MPView instance = null;
        JPanel expResult = null;
        JPanel result = instance.getStatusPanel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFavouritesMenu method, of class MPView.
     */
    @Test
    public void testGetFavouritesMenu() {
        System.out.println("getFavouritesMenu");
        MPView instance = null;
        JMenu expResult = null;
        JMenu result = instance.getFavouritesMenu();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of selectedTabInNewFrame method, of class MPView.
     */
    @Test
    public void testSelectedTabInNewFrame() {
        System.out.println("selectedTabInNewFrame");
        MPView instance = null;
        instance.selectedTabInNewFrame();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showServerStatus method, of class MPView.
     */
    @Test
    public void testShowServerStatus() {
        System.out.println("showServerStatus");
        boolean running = false;
        MPView instance = null;
        instance.showServerStatus(running);
        
        fail("The test case is a prototype.");
    }

}