
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
public class WSContactsMappingTest {

    public WSContactsMappingTest() {
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
     * Test of getView method, of class WSContactsMapping.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        WSContactsMapping instance = new WSContactsMapping();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class WSContactsMapping.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        WSContactsMapping instance = new WSContactsMapping();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getWebshopsids method, of class WSContactsMapping.
     */
    @Test
    public void test__getWebshopsids() {
        System.out.println("__getWebshopsids");
        WSContactsMapping instance = new WSContactsMapping();
        int expResult = 0;
        int result = instance.__getWebshopsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWebshopsids method, of class WSContactsMapping.
     */
    @Test
    public void testSetWebshopsids() {
        System.out.println("setWebshopsids");
        int webshopsids = 0;
        WSContactsMapping instance = new WSContactsMapping();
        instance.setWebshopsids(webshopsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getContactsids method, of class WSContactsMapping.
     */
    @Test
    public void test__getContactsids() {
        System.out.println("__getContactsids");
        WSContactsMapping instance = new WSContactsMapping();
        int expResult = 0;
        int result = instance.__getContactsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContactsids method, of class WSContactsMapping.
     */
    @Test
    public void testSetContactsids() {
        System.out.println("setContactsids");
        int contactsids = 0;
        WSContactsMapping instance = new WSContactsMapping();
        instance.setContactsids(contactsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getWscontact method, of class WSContactsMapping.
     */
    @Test
    public void test__getWscontact() {
        System.out.println("__getWscontact");
        WSContactsMapping instance = new WSContactsMapping();
        String expResult = "";
        String result = instance.__getWscontact();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setWscontact method, of class WSContactsMapping.
     */
    @Test
    public void testSetWscontact() {
        System.out.println("setWscontact");
        String wscontact = "";
        WSContactsMapping instance = new WSContactsMapping();
        instance.setWscontact(wscontact);
        
        fail("The test case is a prototype.");
    }

}