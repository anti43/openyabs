
package mpv5.pluginhandling;

import javax.swing.JComponent;
import mpv5.utils.images.MPIcon;
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
public class PluginTest {

    public PluginTest() {
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
     * Test of __getCName method, of class Plugin.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        Plugin instance = new Plugin();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Plugin.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Plugin instance = new Plugin();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class Plugin.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        Plugin instance = new Plugin();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class Plugin.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        Plugin instance = new Plugin();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class Plugin.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Plugin instance = new Plugin();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFilename method, of class Plugin.
     */
    @Test
    public void test__getFilename() {
        System.out.println("__getFilename");
        Plugin instance = new Plugin();
        String expResult = "";
        String result = instance.__getFilename();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilename method, of class Plugin.
     */
    @Test
    public void testSetFilename() {
        System.out.println("setFilename");
        String filename = "";
        Plugin instance = new Plugin();
        instance.setFilename(filename);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class Plugin.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        Plugin instance = new Plugin();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Plugin.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Plugin instance = new Plugin();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Plugin.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Plugin instance = new Plugin();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}