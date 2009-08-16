
package mpv5.db.objects;

import java.util.ArrayList;
import java.util.HashMap;
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
public class AccountTest {

    public AccountTest() {
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
     * Test of getTypeString method, of class Account.
     */
    @Test
    public void testGetTypeString() {
        System.out.println("getTypeString");
        int type = 0;
        String expResult = "";
        String result = Account.getTypeString(type);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of cacheAccounts method, of class Account.
     */
    @Test
    public void testCacheAccounts() {
        System.out.println("cacheAccounts");
        ArrayList expResult = null;
        ArrayList result = Account.cacheAccounts();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class Account.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        Account instance = new Account();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class Account.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        Account instance = new Account();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class Account.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        Account instance = new Account();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAccounts method, of class Account.
     */
    @Test
    public void testGetAccounts() throws Exception {
        System.out.println("getAccounts");
        ArrayList expResult = null;
        ArrayList result = Account.getAccounts();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemsInAccount method, of class Account.
     */
    @Test
    public void testGetItemsInAccount() throws Exception {
        System.out.println("getItemsInAccount");
        Account instance = new Account();
        ArrayList expResult = null;
        ArrayList result = instance.getItemsInAccount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAccountsOfItem method, of class Account.
     */
    @Test
    public void testGetAccountsOfItem() throws Exception {
        System.out.println("getAccountsOfItem");
        Item item = null;
        ArrayList expResult = null;
        ArrayList result = Account.getAccountsOfItem(item);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntaccounttype method, of class Account.
     */
    @Test
    public void test__getIntaccounttype() {
        System.out.println("__getIntaccounttype");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getIntaccounttype();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntaccounttype method, of class Account.
     */
    @Test
    public void testSetIntaccounttype() {
        System.out.println("setIntaccounttype");
        int accounttype = 0;
        Account instance = new Account();
        instance.setIntaccounttype(accounttype);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntaccountclass method, of class Account.
     */
    @Test
    public void test__getIntaccountclass() {
        System.out.println("__getIntaccountclass");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getIntaccountclass();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntaccountclass method, of class Account.
     */
    @Test
    public void testSetIntaccountclass() {
        System.out.println("setIntaccountclass");
        int intaccountclass = 0;
        Account instance = new Account();
        instance.setIntaccountclass(intaccountclass);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntparentaccount method, of class Account.
     */
    @Test
    public void test__getIntparentaccount() {
        System.out.println("__getIntparentaccount");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getIntparentaccount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntparentaccount method, of class Account.
     */
    @Test
    public void testSetIntparentaccount() {
        System.out.println("setIntparentaccount");
        int intparentaccount = 0;
        Account instance = new Account();
        instance.setIntparentaccount(intparentaccount);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toTreeModel method, of class Account.
     */
    @Test
    public void testToTreeModel() {
        System.out.println("toTreeModel");
        ArrayList<Account> data = null;
        Account rootNode = null;
        DefaultTreeModel expResult = null;
        DefaultTreeModel result = Account.toTreeModel(data, rootNode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getTaxvalue method, of class Account.
     */
    @Test
    public void test__getTaxvalue() {
        System.out.println("__getTaxvalue");
        Account instance = new Account();
        double expResult = 0.0;
        double result = instance.__getTaxvalue();
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTaxvalue method, of class Account.
     */
    @Test
    public void testSetTaxvalue() {
        System.out.println("setTaxvalue");
        double taxvalue = 0.0;
        Account instance = new Account();
        instance.setTaxvalue(taxvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class Account.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        Account instance = new Account();
        boolean expResult = false;
        boolean result = instance.delete();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class Account.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        Account instance = new Account();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFrame method, of class Account.
     */
    @Test
    public void test__getFrame() {
        System.out.println("__getFrame");
        Account instance = new Account();
        String expResult = "";
        String result = instance.__getFrame();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFrame method, of class Account.
     */
    @Test
    public void testSetFrame() {
        System.out.println("setFrame");
        String frame = "";
        Account instance = new Account();
        instance.setFrame(frame);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntprofitfid method, of class Account.
     */
    @Test
    public void test__getIntprofitfid() {
        System.out.println("__getIntprofitfid");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getIntprofitfid();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntprofitfid method, of class Account.
     */
    @Test
    public void testSetIntprofitfid() {
        System.out.println("setIntprofitfid");
        int intprofitfid = 0;
        Account instance = new Account();
        instance.setIntprofitfid(intprofitfid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInttaxfid method, of class Account.
     */
    @Test
    public void test__getInttaxfid() {
        System.out.println("__getInttaxfid");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getInttaxfid();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInttaxfid method, of class Account.
     */
    @Test
    public void testSetInttaxfid() {
        System.out.println("setInttaxfid");
        int inttaxfid = 0;
        Account instance = new Account();
        instance.setInttaxfid(inttaxfid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInttaxuid method, of class Account.
     */
    @Test
    public void test__getInttaxuid() {
        System.out.println("__getInttaxuid");
        Account instance = new Account();
        int expResult = 0;
        int result = instance.__getInttaxuid();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInttaxuid method, of class Account.
     */
    @Test
    public void testSetInttaxuid() {
        System.out.println("setInttaxuid");
        int inttaxuid = 0;
        Account instance = new Account();
        instance.setInttaxuid(inttaxuid);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getHierarchypath method, of class Account.
     */
    @Test
    public void test__getHierarchypath() {
        System.out.println("__getHierarchypath");
        Account instance = new Account();
        String expResult = "";
        String result = instance.__getHierarchypath();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHierarchypath method, of class Account.
     */
    @Test
    public void testSetHierarchypath() {
        System.out.println("setHierarchypath");
        String hierarchypath = "";
        Account instance = new Account();
        instance.setHierarchypath(hierarchypath);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveReferences method, of class Account.
     */
    @Test
    public void testResolveReferences() {
        System.out.println("resolveReferences");
        HashMap<String, Object> map = null;
        Account instance = new Account();
        HashMap expResult = null;
        HashMap result = instance.resolveReferences(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}