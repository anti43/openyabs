
package mpv5.pluginhandling;

import java.awt.Image;
import java.io.File;
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
public class MPPLuginLoaderTest {

    public MPPLuginLoaderTest() {
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
     * Test of getDefaultPluginImage method, of class MPPLuginLoader.
     */
    @Test
    public void testGetDefaultPluginImage() {
        System.out.println("getDefaultPluginImage");
        Image expResult = null;
        Image result = MPPLuginLoader.getDefaultPluginImage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getErrorImage method, of class MPPLuginLoader.
     */
    @Test
    public void testGetErrorImage() {
        System.out.println("getErrorImage");
        Image expResult = null;
        Image result = MPPLuginLoader.getErrorImage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlugins method, of class MPPLuginLoader.
     */
    @Test
    public void testGetPlugins() {
        System.out.println("getPlugins");
        MPPLuginLoader instance = new MPPLuginLoader();
        MP5Plugin[] expResult = null;
        MP5Plugin[] result = instance.getPlugins();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCachedPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testIsCachedPlugin() {
        System.out.println("isCachedPlugin");
        String filename = "";
        MPPLuginLoader instance = new MPPLuginLoader();
        boolean expResult = false;
        boolean result = instance.isCachedPlugin(filename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testCheckPlugin() {
        System.out.println("checkPlugin");
        File pluginCandidate = null;
        MPPLuginLoader instance = new MPPLuginLoader();
        MP5Plugin expResult = null;
        MP5Plugin result = instance.checkPlugin(pluginCandidate);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testGetPlugin() {
        System.out.println("getPlugin");
        File file = null;
        MPPLuginLoader instance = new MPPLuginLoader();
        MP5Plugin expResult = null;
        MP5Plugin result = instance.getPlugin(file);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadPlugins method, of class MPPLuginLoader.
     */
    @Test
    public void testLoadPlugins() {
        System.out.println("loadPlugins");
        MPPLuginLoader instance = new MPPLuginLoader();
        instance.loadPlugins();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of queuePlugins method, of class MPPLuginLoader.
     */
    @Test
    public void testQueuePlugins() {
        System.out.println("queuePlugins");
        MP5Plugin[] plugins = null;
        MPPLuginLoader.queuePlugins(plugins);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of unLoadPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testUnLoadPlugin() {
        System.out.println("unLoadPlugin");
        MP5Plugin mP5Plugin = null;
        MPPLuginLoader instance = new MPPLuginLoader();
        instance.unLoadPlugin(mP5Plugin);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testLoadPlugin_Plugin() {
        System.out.println("loadPlugin");
        Plugin gin = null;
        MPPLuginLoader instance = new MPPLuginLoader();
        instance.loadPlugin(gin);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadPlugin method, of class MPPLuginLoader.
     */
    @Test
    public void testLoadPlugin_MP5Plugin() {
        System.out.println("loadPlugin");
        MP5Plugin mP5Plugin = null;
        MPPLuginLoader instance = new MPPLuginLoader();
        instance.loadPlugin(mP5Plugin);
        
        fail("The test case is a prototype.");
    }

}