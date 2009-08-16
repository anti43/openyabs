
package mpv5.pluginhandling;

import java.awt.Image;
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
public class MP5PluginTest {

    public MP5PluginTest() {
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
     * Test of load method, of class MP5Plugin.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        MPView frame = null;
        MP5Plugin instance = new MP5PluginImpl();
        MP5Plugin expResult = null;
        MP5Plugin result = instance.load(frame);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of unload method, of class MP5Plugin.
     */
    @Test
    public void testUnload() {
        System.out.println("unload");
        MP5Plugin instance = new MP5PluginImpl();
        instance.unload();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MP5Plugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MP5Plugin instance = new MP5PluginImpl();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVendor method, of class MP5Plugin.
     */
    @Test
    public void testGetVendor() {
        System.out.println("getVendor");
        MP5Plugin instance = new MP5PluginImpl();
        String expResult = "";
        String result = instance.getVendor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUID method, of class MP5Plugin.
     */
    @Test
    public void testGetUID() {
        System.out.println("getUID");
        MP5Plugin instance = new MP5PluginImpl();
        Long expResult = null;
        Long result = instance.getUID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEnabled method, of class MP5Plugin.
     */
    @Test
    public void testIsEnabled() {
        System.out.println("isEnabled");
        MP5Plugin instance = new MP5PluginImpl();
        boolean expResult = false;
        boolean result = instance.isEnabled();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isComponent method, of class MP5Plugin.
     */
    @Test
    public void testIsComponent() {
        System.out.println("isComponent");
        MP5Plugin instance = new MP5PluginImpl();
        boolean expResult = false;
        boolean result = instance.isComponent();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isRunnable method, of class MP5Plugin.
     */
    @Test
    public void testIsRunnable() {
        System.out.println("isRunnable");
        MP5Plugin instance = new MP5PluginImpl();
        boolean expResult = false;
        boolean result = instance.isRunnable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isLoaded method, of class MP5Plugin.
     */
    @Test
    public void testIsLoaded() {
        System.out.println("isLoaded");
        MP5Plugin instance = new MP5PluginImpl();
        boolean expResult = false;
        boolean result = instance.isLoaded();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class MP5Plugin.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        MP5Plugin instance = new MP5PluginImpl();
        Image expResult = null;
        Image result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class MP5PluginImpl  implements MP5Plugin {

        public MP5Plugin load(MPView frame) {
            return null;
        }

        public void unload() {
        }

        public String getName() {
            return "";
        }

        public String getVendor() {
            return "";
        }

        public Long getUID() {
            return null;
        }

        public boolean isEnabled() {
            return false;
        }

        public boolean isComponent() {
            return false;
        }

        public boolean isRunnable() {
            return false;
        }

        public boolean isLoaded() {
            return false;
        }

        public Image getIcon() {
            return null;
        }
    }

}