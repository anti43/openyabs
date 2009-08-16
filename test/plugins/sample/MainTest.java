
package plugins.sample;

import java.awt.Image;
import mpv5.pluginhandling.MP5Plugin;
import mpv5.ui.frames.MPView;
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
public class MainTest {

    public MainTest() {
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
     * Test of load method, of class Main.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        MPView frame = null;
        Main instance = new Main();
        MP5Plugin expResult = null;
        MP5Plugin result = instance.load(frame);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of unload method, of class Main.
     */
    @Test
    public void testUnload() {
        System.out.println("unload");
        Main instance = new Main();
        instance.unload();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class Main.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        Main instance = new Main();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVendor method, of class Main.
     */
    @Test
    public void testGetVendor() {
        System.out.println("getVendor");
        Main instance = new Main();
        String expResult = "";
        String result = instance.getVendor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUID method, of class Main.
     */
    @Test
    public void testGetUID() {
        System.out.println("getUID");
        Main instance = new Main();
        Long expResult = null;
        Long result = instance.getUID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEnabled method, of class Main.
     */
    @Test
    public void testIsEnabled() {
        System.out.println("isEnabled");
        Main instance = new Main();
        boolean expResult = false;
        boolean result = instance.isEnabled();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isComponent method, of class Main.
     */
    @Test
    public void testIsComponent() {
        System.out.println("isComponent");
        Main instance = new Main();
        boolean expResult = false;
        boolean result = instance.isComponent();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isRunnable method, of class Main.
     */
    @Test
    public void testIsRunnable() {
        System.out.println("isRunnable");
        Main instance = new Main();
        boolean expResult = false;
        boolean result = instance.isRunnable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class Main.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        Main instance = new Main();
        instance.run();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isLoaded method, of class Main.
     */
    @Test
    public void testIsLoaded() {
        System.out.println("isLoaded");
        Main instance = new Main();
        boolean expResult = false;
        boolean result = instance.isLoaded();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Main.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Main instance = new Main();
        Image expResult = null;
        Image result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}