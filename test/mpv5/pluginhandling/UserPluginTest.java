
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
public class UserPluginTest {

    public UserPluginTest() {
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
     * Test of __getCName method, of class UserPlugin.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        UserPlugin instance = new UserPlugin();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class UserPlugin.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        UserPlugin instance = new UserPlugin();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class UserPlugin.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        UserPlugin instance = new UserPlugin();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUsersids method, of class UserPlugin.
     */
    @Test
    public void test__getUsersids() {
        System.out.println("__getUsersids");
        UserPlugin instance = new UserPlugin();
        int expResult = 0;
        int result = instance.__getUsersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsersids method, of class UserPlugin.
     */
    @Test
    public void testSetUsersids() {
        System.out.println("setUsersids");
        int contactsids = 0;
        UserPlugin instance = new UserPlugin();
        instance.setUsersids(contactsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getPluginsids method, of class UserPlugin.
     */
    @Test
    public void test__getPluginsids() {
        System.out.println("__getPluginsids");
        UserPlugin instance = new UserPlugin();
        int expResult = 0;
        int result = instance.__getPluginsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPluginsids method, of class UserPlugin.
     */
    @Test
    public void testSetPluginsids() {
        System.out.println("setPluginsids");
        int pluginsids = 0;
        UserPlugin instance = new UserPlugin();
        instance.setPluginsids(pluginsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class UserPlugin.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        UserPlugin instance = new UserPlugin();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}