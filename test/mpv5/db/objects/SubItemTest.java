
package mpv5.db.objects;

import java.util.Date;
import java.util.HashMap;
import javax.swing.JComponent;
import mpv5.utils.images.MPIcon;
import mpv5.utils.models.MPTableModel;
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
public class SubItemTest {

    public SubItemTest() {
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
     * Test of saveModel method, of class SubItem.
     */
    @Test
    public void testSaveModel() {
        System.out.println("saveModel");
        Item dataOwner = null;
        MPTableModel model = null;
        SubItem.saveModel(dataOwner, model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultItem method, of class SubItem.
     */
    @Test
    public void testGetDefaultItem() {
        System.out.println("getDefaultItem");
        SubItem expResult = null;
        SubItem result = SubItem.getDefaultItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCName method, of class SubItem.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        SubItem instance = new SubItem();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class SubItem.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        SubItem instance = new SubItem();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getItemsids method, of class SubItem.
     */
    @Test
    public void test__getItemsids() {
        System.out.println("__getItemsids");
        SubItem instance = new SubItem();
        int expResult = 0;
        int result = instance.__getItemsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setItemsids method, of class SubItem.
     */
    @Test
    public void testSetItemsids() {
        System.out.println("setItemsids");
        int itemsids = 0;
        SubItem instance = new SubItem();
        instance.setItemsids(itemsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getOriginalproductsids method, of class SubItem.
     */
    @Test
    public void test__getOriginalproductsids() {
        System.out.println("__getOriginalproductsids");
        SubItem instance = new SubItem();
        int expResult = 0;
        int result = instance.__getOriginalproductsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOriginalproductsids method, of class SubItem.
     */
    @Test
    public void testSetOriginalproductsids() {
        System.out.println("setOriginalproductsids");
        int originalproductsids = 0;
        SubItem instance = new SubItem();
        instance.setOriginalproductsids(originalproductsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCountvalue method, of class SubItem.
     */
    @Test
    public void test__getCountvalue() {
        System.out.println("__getCountvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getCountvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCountvalue method, of class SubItem.
     */
    @Test
    public void testSetCountvalue() {
        System.out.println("setCountvalue");
        double count = 0.0;
        SubItem instance = new SubItem();
        instance.setCountvalue(count);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getQuantityvalue method, of class SubItem.
     */
    @Test
    public void test__getQuantityvalue() {
        System.out.println("__getQuantityvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getQuantityvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setQuantityvalue method, of class SubItem.
     */
    @Test
    public void testSetQuantityvalue() {
        System.out.println("setQuantityvalue");
        double quantity = 0.0;
        SubItem instance = new SubItem();
        instance.setQuantityvalue(quantity);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getMeasure method, of class SubItem.
     */
    @Test
    public void test__getMeasure() {
        System.out.println("__getMeasure");
        SubItem instance = new SubItem();
        String expResult = "";
        String result = instance.__getMeasure();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMeasure method, of class SubItem.
     */
    @Test
    public void testSetMeasure() {
        System.out.println("setMeasure");
        String measure = "";
        SubItem instance = new SubItem();
        instance.setMeasure(measure);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class SubItem.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        SubItem instance = new SubItem();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class SubItem.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        SubItem instance = new SubItem();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTaxpercentvalue method, of class SubItem.
     */
    @Test
    public void test__getTaxpercentvalue() {
        System.out.println("__getTaxpercentvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getTaxpercentvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTaxpercentvalue method, of class SubItem.
     */
    @Test
    public void testSetTaxpercentvalue() {
        System.out.println("setTaxpercentvalue");
        double taxpercent = 0.0;
        SubItem instance = new SubItem();
        instance.setTaxpercentvalue(taxpercent);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDatedelivery method, of class SubItem.
     */
    @Test
    public void test__getDatedelivery() {
        System.out.println("__getDatedelivery");
        SubItem instance = new SubItem();
        Date expResult = null;
        Date result = instance.__getDatedelivery();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDatedelivery method, of class SubItem.
     */
    @Test
    public void testSetDatedelivery() {
        System.out.println("setDatedelivery");
        Date datedelivery = null;
        SubItem instance = new SubItem();
        instance.setDatedelivery(datedelivery);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class SubItem.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        SubItem instance = new SubItem();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class SubItem.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        SubItem instance = new SubItem();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class SubItem.
     */
    @Test
    public void testToModel() {
        System.out.println("toModel");
        SubItem[] items = null;
        MPTableModel expResult = null;
        MPTableModel result = SubItem.toModel(items);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRowData method, of class SubItem.
     */
    @Test
    public void testGetRowData() {
        System.out.println("getRowData");
        int row = 0;
        SubItem instance = new SubItem();
        Object[] expResult = null;
        Object[] result = instance.getRowData(row);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInternalvalue method, of class SubItem.
     */
    @Test
    public void test__getInternalvalue() {
        System.out.println("__getInternalvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getInternalvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInternalvalue method, of class SubItem.
     */
    @Test
    public void testSetInternalvalue() {
        System.out.println("setInternalvalue");
        double internalvalue = 0.0;
        SubItem instance = new SubItem();
        instance.setInternalvalue(internalvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getExternalvalue method, of class SubItem.
     */
    @Test
    public void test__getExternalvalue() {
        System.out.println("__getExternalvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getExternalvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setExternalvalue method, of class SubItem.
     */
    @Test
    public void testSetExternalvalue() {
        System.out.println("setExternalvalue");
        double externalvalue = 0.0;
        SubItem instance = new SubItem();
        instance.setExternalvalue(externalvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveReferences method, of class SubItem.
     */
    @Test
    public void testResolveReferences() {
        System.out.println("resolveReferences");
        HashMap<String, Object> map = null;
        SubItem instance = new SubItem();
        HashMap expResult = null;
        HashMap result = instance.resolveReferences(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTotalnetvalue method, of class SubItem.
     */
    @Test
    public void test__getTotalnetvalue() {
        System.out.println("__getTotalnetvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getTotalnetvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTotalnetvalue method, of class SubItem.
     */
    @Test
    public void testSetTotalnetvalue() {
        System.out.println("setTotalnetvalue");
        double totalnetvalue = 0.0;
        SubItem instance = new SubItem();
        instance.setTotalnetvalue(totalnetvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTotalbrutvalue method, of class SubItem.
     */
    @Test
    public void test__getTotalbrutvalue() {
        System.out.println("__getTotalbrutvalue");
        SubItem instance = new SubItem();
        double expResult = 0.0;
        double result = instance.__getTotalbrutvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTotalbrutvalue method, of class SubItem.
     */
    @Test
    public void testSetTotalbrutvalue() {
        System.out.println("setTotalbrutvalue");
        double totalbrutvalue = 0.0;
        SubItem instance = new SubItem();
        instance.setTotalbrutvalue(totalbrutvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class SubItem.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        SubItem instance = new SubItem();
        boolean expResult = false;
        boolean result = instance.save();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}