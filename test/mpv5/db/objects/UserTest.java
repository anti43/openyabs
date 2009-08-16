
package mpv5.db.objects;

import java.util.Date;
import javax.swing.JComponent;
import mpv5.data.PropertyStore;
import mpv5.pluginhandling.MP5Plugin;
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
public class UserTest {

    public UserTest() {
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
     * Test of cacheUser method, of class User.
     */
    @Test
    public void testCacheUser() {
        System.out.println("cacheUser");
        User.cacheUser();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUsername method, of class User.
     */
    @Test
    public void testGetUsername() {
        System.out.println("getUsername");
        int forId = 0;
        String expResult = "";
        String result = User.getUsername(forId);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserId method, of class User.
     */
    @Test
    public void testGetUserId() {
        System.out.println("getUserId");
        String username = "";
        int expResult = 0;
        int result = User.getUserId(username);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getID method, of class User.
     */
    @Test
    public void testGetID() {
        System.out.println("getID");
        User instance = new User();
        Integer expResult = null;
        Integer result = instance.getID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class User.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        User instance = new User();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCName method, of class User.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        User instance = new User();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlugins method, of class User.
     */
    @Test
    public void testGetPlugins() {
        System.out.println("getPlugins");
        User instance = new User();
        MP5Plugin[] expResult = null;
        MP5Plugin[] result = instance.getPlugins();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isDefault method, of class User.
     */
    @Test
    public void testIsDefault() {
        System.out.println("isDefault");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.isDefault();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAdmin method, of class User.
     */
    @Test
    public void testIsAdmin() {
        System.out.println("isAdmin");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.isAdmin();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of login method, of class User.
     */
    @Test
    public void testLogin() {
        System.out.println("login");
        User instance = new User();
        instance.login();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of logout method, of class User.
     */
    @Test
    public void testLogout() {
        System.out.println("logout");
        User instance = new User();
        instance.logout();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class User.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        User instance = new User();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class User.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.save();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class User.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getPassword method, of class User.
     */
    @Test
    public void test__getPassword() {
        System.out.println("__getPassword");
        User instance = new User();
        String expResult = "";
        String result = instance.__getPassword();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPassword method, of class User.
     */
    @Test
    public void testSetPassword() {
        System.out.println("setPassword");
        String password = "";
        User instance = new User();
        instance.setPassword(password);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getLaf method, of class User.
     */
    @Test
    public void test__getLaf() {
        System.out.println("__getLaf");
        User instance = new User();
        String expResult = "";
        String result = instance.__getLaf();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLaf method, of class User.
     */
    @Test
    public void testSetLaf() {
        System.out.println("setLaf");
        String laf = "";
        User instance = new User();
        instance.setLaf(laf);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getLocale method, of class User.
     */
    @Test
    public void test__getLocale() {
        System.out.println("__getLocale");
        User instance = new User();
        String expResult = "";
        String result = instance.__getLocale();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocale method, of class User.
     */
    @Test
    public void testSetLocale() {
        System.out.println("setLocale");
        String locale = "";
        User instance = new User();
        instance.setLocale(locale);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getMail method, of class User.
     */
    @Test
    public void test__getMail() {
        System.out.println("__getMail");
        User instance = new User();
        String expResult = "";
        String result = instance.__getMail();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMail method, of class User.
     */
    @Test
    public void testSetMail() {
        System.out.println("setMail");
        String mail = "";
        User instance = new User();
        instance.setMail(mail);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getLanguage method, of class User.
     */
    @Test
    public void test__getLanguage() {
        System.out.println("__getLanguage");
        User instance = new User();
        String expResult = "";
        String result = instance.__getLanguage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLanguage method, of class User.
     */
    @Test
    public void testSetLanguage() {
        System.out.println("setLanguage");
        String language = "";
        User instance = new User();
        instance.setLanguage(language);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIsenabled method, of class User.
     */
    @Test
    public void test__getIsenabled() {
        System.out.println("__getIsenabled");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.__getIsenabled();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIsenabled method, of class User.
     */
    @Test
    public void testSetIsenabled() {
        System.out.println("setIsenabled");
        boolean isenabled = false;
        User instance = new User();
        instance.setIsenabled(isenabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class User.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        User instance = new User();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDatelastlog method, of class User.
     */
    @Test
    public void test__getDatelastlog() {
        System.out.println("__getDatelastlog");
        User instance = new User();
        Date expResult = null;
        Date result = instance.__getDatelastlog();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDatelastlog method, of class User.
     */
    @Test
    public void testSetDatelastlog() {
        System.out.println("setDatelastlog");
        Date lastlogdate = null;
        User instance = new User();
        instance.setDatelastlog(lastlogdate);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIsloggedin method, of class User.
     */
    @Test
    public void test__getIsloggedin() {
        System.out.println("__getIsloggedin");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.__getIsloggedin();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIsloggedin method, of class User.
     */
    @Test
    public void testSetIsloggedin() {
        System.out.println("setIsloggedin");
        boolean isloggedin = false;
        User instance = new User();
        instance.setIsloggedin(isloggedin);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFullname method, of class User.
     */
    @Test
    public void test__getFullname() {
        System.out.println("__getFullname");
        User instance = new User();
        String expResult = "";
        String result = instance.__getFullname();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFullname method, of class User.
     */
    @Test
    public void testSetFullname() {
        System.out.println("setFullname");
        String fullname = "";
        User instance = new User();
        instance.setFullname(fullname);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDefcountry method, of class User.
     */
    @Test
    public void test__getDefcountry() {
        System.out.println("__getDefcountry");
        User instance = new User();
        String expResult = "";
        String result = instance.__getDefcountry();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefcountry method, of class User.
     */
    @Test
    public void testSetDefcountry() {
        System.out.println("setDefcountry");
        String defcountry = "";
        User instance = new User();
        instance.setDefcountry(defcountry);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of equalTo method, of class User.
     */
    @Test
    public void testEqualTo() {
        System.out.println("equalTo");
        User n = null;
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.equalTo(n);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class User.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        User instance = new User();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeProperty method, of class User.
     */
    @Test
    public void testChangeProperty() {
        System.out.println("changeProperty");
        String key = "";
        String value = "";
        User instance = new User();
        instance.changeProperty(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperties method, of class User.
     */
    @Test
    public void testGetProperties() {
        System.out.println("getProperties");
        User instance = new User();
        PropertyStore expResult = null;
        PropertyStore result = instance.getProperties();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveProperties method, of class User.
     */
    @Test
    public void testSaveProperties() {
        System.out.println("saveProperties");
        User instance = new User();
        instance.saveProperties();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteProperties method, of class User.
     */
    @Test
    public void testDeleteProperties() {
        System.out.println("deleteProperties");
        User instance = new User();
        instance.deleteProperties();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIsrgrouped method, of class User.
     */
    @Test
    public void test__getIsrgrouped() {
        System.out.println("__getIsrgrouped");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.__getIsrgrouped();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIsrgrouped method, of class User.
     */
    @Test
    public void testSetIsrgrouped() {
        System.out.println("setIsrgrouped");
        boolean isrgrouped = false;
        User instance = new User();
        instance.setIsrgrouped(isrgrouped);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInthighestright method, of class User.
     */
    @Test
    public void test__getInthighestright() {
        System.out.println("__getInthighestright");
        User instance = new User();
        int expResult = 0;
        int result = instance.__getInthighestright();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInthighestright method, of class User.
     */
    @Test
    public void testSetInthighestright() {
        System.out.println("setInthighestright");
        int inthighestright = 0;
        User instance = new User();
        instance.setInthighestright(inthighestright);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntdefaultaccount method, of class User.
     */
    @Test
    public void test__getIntdefaultaccount() {
        System.out.println("__getIntdefaultaccount");
        User instance = new User();
        int expResult = 0;
        int result = instance.__getIntdefaultaccount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntdefaultaccount method, of class User.
     */
    @Test
    public void testSetIntdefaultaccount() {
        System.out.println("setIntdefaultaccount");
        int intdefaultaccount = 0;
        User instance = new User();
        instance.setIntdefaultaccount(intdefaultaccount);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class User.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        User instance = new User();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isGroupRestricted method, of class User.
     */
    @Test
    public void testIsGroupRestricted() {
        System.out.println("isGroupRestricted");
        User instance = new User();
        boolean expResult = false;
        boolean result = instance.isGroupRestricted();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntdefaultstatus method, of class User.
     */
    @Test
    public void test__getIntdefaultstatus() {
        System.out.println("__getIntdefaultstatus");
        User instance = new User();
        int expResult = 0;
        int result = instance.__getIntdefaultstatus();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntdefaultstatus method, of class User.
     */
    @Test
    public void testSetIntdefaultstatus() {
        System.out.println("setIntdefaultstatus");
        int intdefaultstatus = 0;
        User instance = new User();
        instance.setIntdefaultstatus(intdefaultstatus);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCompsids method, of class User.
     */
    @Test
    public void test__getCompsids() {
        System.out.println("__getCompsids");
        User instance = new User();
        int expResult = 0;
        int result = instance.__getCompsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCompsids method, of class User.
     */
    @Test
    public void testSetCompsids() {
        System.out.println("setCompsids");
        int compsids = 0;
        User instance = new User();
        instance.setCompsids(compsids);
        
        fail("The test case is a prototype.");
    }

}