
package mpv5.db.objects;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.tree.DefaultTreeModel;
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
public class ProductGroupTest {

    public ProductGroupTest() {
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
     * Test of __getDescription method, of class ProductGroup.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        ProductGroup instance = new ProductGroup();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class ProductGroup.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        ProductGroup instance = new ProductGroup();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDefaults method, of class ProductGroup.
     */
    @Test
    public void test__getDefaults() {
        System.out.println("__getDefaults");
        ProductGroup instance = new ProductGroup();
        String expResult = "";
        String result = instance.__getDefaults();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaults method, of class ProductGroup.
     */
    @Test
    public void testSetDefaults() {
        System.out.println("setDefaults");
        String defaultvalue = "";
        ProductGroup instance = new ProductGroup();
        instance.setDefaults(defaultvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class ProductGroup.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        ProductGroup instance = new ProductGroup();
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class ProductGroup.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        ProductGroup instance = new ProductGroup();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class ProductGroup.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        ProductGroup instance = new ProductGroup();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class ProductGroup.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        ProductGroup instance = new ProductGroup();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getHierarchypath method, of class ProductGroup.
     */
    @Test
    public void test__getHierarchypath() {
        System.out.println("__getHierarchypath");
        ProductGroup instance = new ProductGroup();
        String expResult = "";
        String result = instance.__getHierarchypath();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHierarchypath method, of class ProductGroup.
     */
    @Test
    public void testSetHierarchypath() {
        System.out.println("setHierarchypath");
        String hierarchypath = "";
        ProductGroup instance = new ProductGroup();
        instance.setHierarchypath(hierarchypath);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getProductgroupsids method, of class ProductGroup.
     */
    @Test
    public void test__getProductgroupsids() {
        System.out.println("__getProductgroupsids");
        ProductGroup instance = new ProductGroup();
        int expResult = 0;
        int result = instance.__getProductgroupsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProductgroupsids method, of class ProductGroup.
     */
    @Test
    public void testSetProductgroupsids() {
        System.out.println("setProductgroupsids");
        int productgroupsids = 0;
        ProductGroup instance = new ProductGroup();
        instance.setProductgroupsids(productgroupsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toTreeModel method, of class ProductGroup.
     */
    @Test
    public void testToTreeModel() {
        System.out.println("toTreeModel");
        ArrayList<ProductGroup> data = null;
        ProductGroup rootNode = null;
        DefaultTreeModel expResult = null;
        DefaultTreeModel result = ProductGroup.toTreeModel(data, rootNode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}