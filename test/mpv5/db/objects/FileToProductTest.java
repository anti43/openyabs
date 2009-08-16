
package mpv5.db.objects;

import java.io.File;
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
public class FileToProductTest {

    public FileToProductTest() {
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
     * Test of __getCName method, of class FileToProduct.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        FileToProduct instance = new FileToProduct();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class FileToProduct.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        FileToProduct instance = new FileToProduct();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class FileToProduct.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        FileToProduct instance = new FileToProduct();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class FileToProduct.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        FileToProduct instance = new FileToProduct();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class FileToProduct.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        FileToProduct instance = new FileToProduct();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFilename method, of class FileToProduct.
     */
    @Test
    public void test__getFilename() {
        System.out.println("__getFilename");
        FileToProduct instance = new FileToProduct();
        String expResult = "";
        String result = instance.__getFilename();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilename method, of class FileToProduct.
     */
    @Test
    public void testSetFilename() {
        System.out.println("setFilename");
        String filename = "";
        FileToProduct instance = new FileToProduct();
        instance.setFilename(filename);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class FileToProduct.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        FileToProduct instance = new FileToProduct();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class FileToProduct.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        FileToProduct instance = new FileToProduct();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getProductsids method, of class FileToProduct.
     */
    @Test
    public void test__getProductsids() {
        System.out.println("__getProductsids");
        FileToProduct instance = new FileToProduct();
        int expResult = 0;
        int result = instance.__getProductsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProductsids method, of class FileToProduct.
     */
    @Test
    public void testSetProductsids() {
        System.out.println("setProductsids");
        int productsids = 0;
        FileToProduct instance = new FileToProduct();
        instance.setProductsids(productsids);
        
        fail("The test case is a prototype.");
    }

}