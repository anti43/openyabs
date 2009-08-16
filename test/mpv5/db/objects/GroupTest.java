
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
public class GroupTest {

    public GroupTest() {
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
     * Test of __getDescription method, of class Group.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        Group instance = new Group();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class Group.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Group instance = new Group();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDefaults method, of class Group.
     */
    @Test
    public void test__getDefaults() {
        System.out.println("__getDefaults");
        Group instance = new Group();
        String expResult = "";
        String result = instance.__getDefaults();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaults method, of class Group.
     */
    @Test
    public void testSetDefaults() {
        System.out.println("setDefaults");
        String defaultvalue = "";
        Group instance = new Group();
        instance.setDefaults(defaultvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Group.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Group instance = new Group();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class Group.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        Group instance = new Group();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Group.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Group instance = new Group();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Group.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Group instance = new Group();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getHierarchypath method, of class Group.
     */
    @Test
    public void test__getHierarchypath() {
        System.out.println("__getHierarchypath");
        Group instance = new Group();
        String expResult = "";
        String result = instance.__getHierarchypath();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHierarchypath method, of class Group.
     */
    @Test
    public void testSetHierarchypath() {
        System.out.println("setHierarchypath");
        String hierarchypath = "";
        Group instance = new Group();
        instance.setHierarchypath(hierarchypath);
        
        fail("The test case is a prototype.");
    }

}