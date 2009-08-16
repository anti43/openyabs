
package mpv5.db.objects;

import java.util.HashMap;
import javax.swing.JComponent;
import mpv5.handler.FormatHandler;
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
public class ProductTest {

    public ProductTest() {
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
     * Test of getTypeString method, of class Product.
     */
    @Test
    public void testGetTypeString() {
        System.out.println("getTypeString");
        int type = 0;
        String expResult = "";
        String result = Product.getTypeString(type);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Product.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Product instance = new Product();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Product.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Product instance = new Product();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTaxids method, of class Product.
     */
    @Test
    public void test__getTaxids() {
        System.out.println("__getTaxids");
        Product instance = new Product();
        int expResult = 0;
        int result = instance.__getTaxids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTaxids method, of class Product.
     */
    @Test
    public void testSetTaxids() {
        System.out.println("setTaxids");
        int inttaxids = 0;
        Product instance = new Product();
        instance.setTaxids(inttaxids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInttype method, of class Product.
     */
    @Test
    public void test__getInttype() {
        System.out.println("__getInttype");
        Product instance = new Product();
        int expResult = 0;
        int result = instance.__getInttype();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInttype method, of class Product.
     */
    @Test
    public void testSetInttype() {
        System.out.println("setInttype");
        int inttype = 0;
        Product instance = new Product();
        instance.setInttype(inttype);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getExternalnetvalue method, of class Product.
     */
    @Test
    public void test__getExternalnetvalue() {
        System.out.println("__getExternalnetvalue");
        Product instance = new Product();
        double expResult = 0.0;
        double result = instance.__getExternalnetvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExternalnetvalue method, of class Product.
     */
    @Test
    public void testSetExternalnetvalue() {
        System.out.println("setExternalnetvalue");
        double externalnetvalue = 0.0;
        Product instance = new Product();
        instance.setExternalnetvalue(externalnetvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInternalnetvalue method, of class Product.
     */
    @Test
    public void test__getInternalnetvalue() {
        System.out.println("__getInternalnetvalue");
        Product instance = new Product();
        double expResult = 0.0;
        double result = instance.__getInternalnetvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInternalnetvalue method, of class Product.
     */
    @Test
    public void testSetInternalnetvalue() {
        System.out.println("setInternalnetvalue");
        double internalnetvalue = 0.0;
        Product instance = new Product();
        instance.setInternalnetvalue(internalnetvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class Product.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class Product.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Product instance = new Product();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCnumber method, of class Product.
     */
    @Test
    public void test__getCnumber() {
        System.out.println("__getCnumber");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getCnumber();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCnumber method, of class Product.
     */
    @Test
    public void testSetCnumber() {
        System.out.println("setCnumber");
        String cnumber = "";
        Product instance = new Product();
        instance.setCnumber(cnumber);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getMeasure method, of class Product.
     */
    @Test
    public void test__getMeasure() {
        System.out.println("__getMeasure");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getMeasure();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMeasure method, of class Product.
     */
    @Test
    public void testSetMeasure() {
        System.out.println("setMeasure");
        String measure = "";
        Product instance = new Product();
        instance.setMeasure(measure);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUrl method, of class Product.
     */
    @Test
    public void test__getUrl() {
        System.out.println("__getUrl");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getUrl();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUrl method, of class Product.
     */
    @Test
    public void testSetUrl() {
        System.out.println("setUrl");
        String url = "";
        Product instance = new Product();
        instance.setUrl(url);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getEan method, of class Product.
     */
    @Test
    public void test__getEan() {
        System.out.println("__getEan");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getEan();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEan method, of class Product.
     */
    @Test
    public void testSetEan() {
        System.out.println("setEan");
        String ean = "";
        Product instance = new Product();
        instance.setEan(ean);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getReference method, of class Product.
     */
    @Test
    public void test__getReference() {
        System.out.println("__getReference");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getReference();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setReference method, of class Product.
     */
    @Test
    public void testSetReference() {
        System.out.println("setReference");
        String reference = "";
        Product instance = new Product();
        instance.setReference(reference);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormatHandler method, of class Product.
     */
    @Test
    public void testGetFormatHandler() {
        System.out.println("getFormatHandler");
        Product instance = new Product();
        FormatHandler expResult = null;
        FormatHandler result = instance.getFormatHandler();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ensureUniqueness method, of class Product.
     */
    @Test
    public void testEnsureUniqueness() {
        System.out.println("ensureUniqueness");
        Product instance = new Product();
        instance.ensureUniqueness();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getManufacturersids method, of class Product.
     */
    @Test
    public void test__getManufacturersids() {
        System.out.println("__getManufacturersids");
        Product instance = new Product();
        int expResult = 0;
        int result = instance.__getManufacturersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setManufacturersids method, of class Product.
     */
    @Test
    public void testSetManufacturersids() {
        System.out.println("setManufacturersids");
        int manufacturersids = 0;
        Product instance = new Product();
        instance.setManufacturersids(manufacturersids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getSuppliersids method, of class Product.
     */
    @Test
    public void test__getSuppliersids() {
        System.out.println("__getSuppliersids");
        Product instance = new Product();
        int expResult = 0;
        int result = instance.__getSuppliersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSuppliersids method, of class Product.
     */
    @Test
    public void testSetSuppliersids() {
        System.out.println("setSuppliersids");
        int suppliersids = 0;
        Product instance = new Product();
        instance.setSuppliersids(suppliersids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDefaultimage method, of class Product.
     */
    @Test
    public void test__getDefaultimage() {
        System.out.println("__getDefaultimage");
        Product instance = new Product();
        String expResult = "";
        String result = instance.__getDefaultimage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultimage method, of class Product.
     */
    @Test
    public void testSetDefaultimage() {
        System.out.println("setDefaultimage");
        String defaultimage = "";
        Product instance = new Product();
        instance.setDefaultimage(defaultimage);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getProductgroupsids method, of class Product.
     */
    @Test
    public void test__getProductgroupsids() {
        System.out.println("__getProductgroupsids");
        Product instance = new Product();
        int expResult = 0;
        int result = instance.__getProductgroupsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProductgroupsids method, of class Product.
     */
    @Test
    public void testSetProductgroupsids() {
        System.out.println("setProductgroupsids");
        int productgroupsids = 0;
        Product instance = new Product();
        instance.setProductgroupsids(productgroupsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveReferences method, of class Product.
     */
    @Test
    public void testResolveReferences() {
        System.out.println("resolveReferences");
        HashMap<String, Object> map = null;
        Product instance = new Product();
        HashMap expResult = null;
        HashMap result = instance.resolveReferences(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}