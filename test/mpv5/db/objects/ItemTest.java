
package mpv5.db.objects;

import java.util.Date;
import java.util.HashMap;
import javax.swing.JComponent;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
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
public class ItemTest {

    public ItemTest() {
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
     * Test of getStatusString method, of class Item.
     */
    @Test
    public void testGetStatusString() {
        System.out.println("getStatusString");
        int status = 0;
        String expResult = "";
        String result = Item.getStatusString(status);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatusStrings method, of class Item.
     */
    @Test
    public void testGetStatusStrings() {
        System.out.println("getStatusStrings");
        MPEnum[] expResult = null;
        MPEnum[] result = Item.getStatusStrings();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTypeString method, of class Item.
     */
    @Test
    public void testGetTypeString() {
        System.out.println("getTypeString");
        int type = 0;
        String expResult = "";
        String result = Item.getTypeString(type);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTaxValue method, of class Item.
     */
    @Test
    public void testGetTaxValue() {
        System.out.println("getTaxValue");
        Integer taxid = null;
        Double expResult = null;
        Double result = Item.getTaxValue(taxid);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getContactsids method, of class Item.
     */
    @Test
    public void test__getContactsids() {
        System.out.println("__getContactsids");
        Item instance = new Item();
        int expResult = 0;
        int result = instance.__getContactsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContactsids method, of class Item.
     */
    @Test
    public void testSetContactsids() {
        System.out.println("setContactsids");
        int contactsids = 0;
        Item instance = new Item();
        instance.setContactsids(contactsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTaxvalue method, of class Item.
     */
    @Test
    public void test__getTaxvalue() {
        System.out.println("__getTaxvalue");
        Item instance = new Item();
        double expResult = 0.0;
        double result = instance.__getTaxvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTaxvalue method, of class Item.
     */
    @Test
    public void testSetTaxvalue() {
        System.out.println("setTaxvalue");
        double taxvalue = 0.0;
        Item instance = new Item();
        instance.setTaxvalue(taxvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDatetodo method, of class Item.
     */
    @Test
    public void test__getDatetodo() {
        System.out.println("__getDatetodo");
        Item instance = new Item();
        Date expResult = null;
        Date result = instance.__getDatetodo();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDatetodo method, of class Item.
     */
    @Test
    public void testSetDatetodo() {
        System.out.println("setDatetodo");
        Date datetodo = null;
        Item instance = new Item();
        instance.setDatetodo(datetodo);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntreminders method, of class Item.
     */
    @Test
    public void test__getIntreminders() {
        System.out.println("__getIntreminders");
        Item instance = new Item();
        int expResult = 0;
        int result = instance.__getIntreminders();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntreminders method, of class Item.
     */
    @Test
    public void testSetIntreminders() {
        System.out.println("setIntreminders");
        int intreminders = 0;
        Item instance = new Item();
        instance.setIntreminders(intreminders);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDateend method, of class Item.
     */
    @Test
    public void test__getDateend() {
        System.out.println("__getDateend");
        Item instance = new Item();
        Date expResult = null;
        Date result = instance.__getDateend();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDateend method, of class Item.
     */
    @Test
    public void testSetDateend() {
        System.out.println("setDateend");
        Date dateend = null;
        Item instance = new Item();
        instance.setDateend(dateend);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Item.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Item instance = new Item();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDefaultaccountsids method, of class Item.
     */
    @Test
    public void test__getDefaultaccountsids() {
        System.out.println("__getDefaultaccountsids");
        Item instance = new Item();
        int expResult = 0;
        int result = instance.__getDefaultaccountsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDefaultaccountsids method, of class Item.
     */
    @Test
    public void testSetDefaultaccountsids() {
        System.out.println("setDefaultaccountsids");
        int defaultaccountsids = 0;
        Item instance = new Item();
        instance.setDefaultaccountsids(defaultaccountsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class Item.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        Item instance = new Item();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class Item.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Item instance = new Item();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntstatus method, of class Item.
     */
    @Test
    public void test__getIntstatus() {
        System.out.println("__getIntstatus");
        Item instance = new Item();
        int expResult = 0;
        int result = instance.__getIntstatus();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntstatus method, of class Item.
     */
    @Test
    public void testSetIntstatus() {
        System.out.println("setIntstatus");
        int intstatus = 0;
        Item instance = new Item();
        instance.setIntstatus(intstatus);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInttype method, of class Item.
     */
    @Test
    public void test__getInttype() {
        System.out.println("__getInttype");
        Item instance = new Item();
        int expResult = 0;
        int result = instance.__getInttype();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInttype method, of class Item.
     */
    @Test
    public void testSetInttype() {
        System.out.println("setInttype");
        int inttype = 0;
        Item instance = new Item();
        instance.setInttype(inttype);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getNetvalue method, of class Item.
     */
    @Test
    public void test__getNetvalue() {
        System.out.println("__getNetvalue");
        Item instance = new Item();
        double expResult = 0.0;
        double result = instance.__getNetvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setNetvalue method, of class Item.
     */
    @Test
    public void testSetNetvalue() {
        System.out.println("setNetvalue");
        double netvalue = 0.0;
        Item instance = new Item();
        instance.setNetvalue(netvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Item.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Item instance = new Item();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormatHandler method, of class Item.
     */
    @Test
    public void testGetFormatHandler() {
        System.out.println("getFormatHandler");
        Item instance = new Item();
        FormatHandler expResult = null;
        FormatHandler result = instance.getFormatHandler();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ensureUniqueness method, of class Item.
     */
    @Test
    public void testEnsureUniqueness() {
        System.out.println("ensureUniqueness");
        Item instance = new Item();
        instance.ensureUniqueness();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSubitems method, of class Item.
     */
    @Test
    public void testGetSubitems() {
        System.out.println("getSubitems");
        Item instance = new Item();
        SubItem[] expResult = null;
        SubItem[] result = instance.getSubitems();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getCnumber method, of class Item.
     */
    @Test
    public void test__getCnumber() {
        System.out.println("__getCnumber");
        Item instance = new Item();
        String expResult = "";
        String result = instance.__getCnumber();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCnumber method, of class Item.
     */
    @Test
    public void testSetCnumber() {
        System.out.println("setCnumber");
        String cnumber = "";
        Item instance = new Item();
        instance.setCnumber(cnumber);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveReferences method, of class Item.
     */
    @Test
    public void testResolveReferences() {
        System.out.println("resolveReferences");
        HashMap<String, Object> map = null;
        Item instance = new Item();
        HashMap expResult = null;
        HashMap result = instance.resolveReferences(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}