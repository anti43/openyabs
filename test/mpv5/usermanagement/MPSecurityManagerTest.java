
package mpv5.usermanagement;

import javax.swing.ComboBoxModel;
import mpv5.db.common.Context;
import mpv5.db.objects.User;
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
public class MPSecurityManagerTest {

    public MPSecurityManagerTest() {
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
     * Test of check method, of class MPSecurityManager.
     */
    @Test
    public void testCheck() {
        System.out.println("check");
        Context context = null;
        int action = 0;
        Boolean expResult = null;
        Boolean result = MPSecurityManager.check(context, action);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkAdminAccess method, of class MPSecurityManager.
     */
    @Test
    public void testCheckAdminAccess() {
        System.out.println("checkAdminAccess");
        boolean expResult = false;
        boolean result = MPSecurityManager.checkAdminAccess();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkAuth method, of class MPSecurityManager.
     */
    @Test
    public void testCheckAuth() {
        System.out.println("checkAuth");
        String username = "";
        String password = "";
        User expResult = null;
        User result = MPSecurityManager.checkAuth(username, password);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkAuthInternal method, of class MPSecurityManager.
     */
    @Test
    public void testCheckAuthInternal() {
        System.out.println("checkAuthInternal");
        User user = null;
        String passwordhash = "";
        User expResult = null;
        User result = MPSecurityManager.checkAuthInternal(user, passwordhash);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getActionName method, of class MPSecurityManager.
     */
    @Test
    public void testGetActionName() {
        System.out.println("getActionName");
        int action = 0;
        String expResult = "";
        String result = MPSecurityManager.getActionName(action);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRolesAsComboBoxModel method, of class MPSecurityManager.
     */
    @Test
    public void testGetRolesAsComboBoxModel() {
        System.out.println("getRolesAsComboBoxModel");
        ComboBoxModel expResult = null;
        ComboBoxModel result = MPSecurityManager.getRolesAsComboBoxModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}