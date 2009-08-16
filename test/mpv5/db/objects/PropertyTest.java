
package mpv5.db.objects;

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
public class PropertyTest {

    public PropertyTest() {
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
     * Test of getView method, of class Property.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Property instance = new Property();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getValue method, of class Property.
     */
    @Test
    public void test__getValue() {
        System.out.println("__getValue");
        Property instance = new Property();
        String expResult = "";
        String result = instance.__getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class Property.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String value = "";
        Property instance = new Property();
        instance.setValue(value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class Property.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        Property instance = new Property();
        boolean expResult = false;
        boolean result = instance.save();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUsersids method, of class Property.
     */
    @Test
    public void test__getUsersids() {
        System.out.println("__getUsersids");
        Property instance = new Property();
        int expResult = 0;
        int result = instance.__getUsersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsersids method, of class Property.
     */
    @Test
    public void testSetUsersids() {
        System.out.println("setUsersids");
        int usersids = 0;
        Property instance = new Property();
        instance.setUsersids(usersids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Property.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Property instance = new Property();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}