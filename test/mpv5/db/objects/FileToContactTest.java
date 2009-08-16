
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
public class FileToContactTest {

    public FileToContactTest() {
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
     * Test of __getCName method, of class FileToContact.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        FileToContact instance = new FileToContact();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class FileToContact.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        FileToContact instance = new FileToContact();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class FileToContact.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        FileToContact instance = new FileToContact();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class FileToContact.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        FileToContact instance = new FileToContact();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class FileToContact.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        FileToContact instance = new FileToContact();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getContactsids method, of class FileToContact.
     */
    @Test
    public void test__getContactsids() {
        System.out.println("__getContactsids");
        FileToContact instance = new FileToContact();
        int expResult = 0;
        int result = instance.__getContactsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContactsids method, of class FileToContact.
     */
    @Test
    public void testSetContactsids() {
        System.out.println("setContactsids");
        int contactsids = 0;
        FileToContact instance = new FileToContact();
        instance.setContactsids(contactsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFilename method, of class FileToContact.
     */
    @Test
    public void test__getFilename() {
        System.out.println("__getFilename");
        FileToContact instance = new FileToContact();
        String expResult = "";
        String result = instance.__getFilename();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilename method, of class FileToContact.
     */
    @Test
    public void testSetFilename() {
        System.out.println("setFilename");
        String filename = "";
        FileToContact instance = new FileToContact();
        instance.setFilename(filename);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class FileToContact.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        FileToContact instance = new FileToContact();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class FileToContact.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        FileToContact instance = new FileToContact();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getMimetype method, of class FileToContact.
     */
    @Test
    public void test__getMimetype() {
        System.out.println("__getMimetype");
        FileToContact instance = new FileToContact();
        String expResult = "";
        String result = instance.__getMimetype();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMimetype method, of class FileToContact.
     */
    @Test
    public void testSetMimetype() {
        System.out.println("setMimetype");
        String mimetype = "";
        FileToContact instance = new FileToContact();
        instance.setMimetype(mimetype);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntsize method, of class FileToContact.
     */
    @Test
    public void test__getIntsize() {
        System.out.println("__getIntsize");
        FileToContact instance = new FileToContact();
        int expResult = 0;
        int result = instance.__getIntsize();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntsize method, of class FileToContact.
     */
    @Test
    public void testSetIntsize() {
        System.out.println("setIntsize");
        int intsize = 0;
        FileToContact instance = new FileToContact();
        instance.setIntsize(intsize);
        
        fail("The test case is a prototype.");
    }

}