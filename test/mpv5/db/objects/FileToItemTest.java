
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
public class FileToItemTest {

    public FileToItemTest() {
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
     * Test of __getCName method, of class FileToItem.
     */
    @Test
    public void test__getCName() {
        System.out.println("__getCName");
        FileToItem instance = new FileToItem();
        String expResult = "";
        String result = instance.__getCName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getView method, of class FileToItem.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        FileToItem instance = new FileToItem();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCName method, of class FileToItem.
     */
    @Test
    public void testSetCName() {
        System.out.println("setCName");
        String name = "";
        FileToItem instance = new FileToItem();
        instance.setCName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class FileToItem.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        FileToItem instance = new FileToItem();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class FileToItem.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        FileToItem instance = new FileToItem();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getContactsids method, of class FileToItem.
     */
    @Test
    public void test__getContactsids() {
        System.out.println("__getContactsids");
        FileToItem instance = new FileToItem();
        int expResult = 0;
        int result = instance.__getContactsids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContactsids method, of class FileToItem.
     */
    @Test
    public void testSetContactsids() {
        System.out.println("setContactsids");
        int contactsids = 0;
        FileToItem instance = new FileToItem();
        instance.setContactsids(contactsids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getFilename method, of class FileToItem.
     */
    @Test
    public void test__getFilename() {
        System.out.println("__getFilename");
        FileToItem instance = new FileToItem();
        String expResult = "";
        String result = instance.__getFilename();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFilename method, of class FileToItem.
     */
    @Test
    public void testSetFilename() {
        System.out.println("setFilename");
        String filename = "";
        FileToItem instance = new FileToItem();
        instance.setFilename(filename);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class FileToItem.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        FileToItem instance = new FileToItem();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class FileToItem.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        FileToItem instance = new FileToItem();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}