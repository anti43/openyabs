
package mpv5.db.objects;

import java.util.ArrayList;
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
public class MailMessageTest {

    public MailMessageTest() {
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
     * Test of getView method, of class MailMessage.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        MailMessage instance = new MailMessage();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemsWithMessage method, of class MailMessage.
     */
    @Test
    public void testGetItemsWithMessage() throws Exception {
        System.out.println("getItemsWithMessage");
        MailMessage instance = new MailMessage();
        ArrayList expResult = null;
        ArrayList result = instance.getItemsWithMessage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class MailMessage.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        MailMessage instance = new MailMessage();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getIntstatus method, of class MailMessage.
     */
    @Test
    public void test__getIntstatus() {
        System.out.println("__getIntstatus");
        MailMessage instance = new MailMessage();
        int expResult = 0;
        int result = instance.__getIntstatus();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setIntstatus method, of class MailMessage.
     */
    @Test
    public void testSetIntstatus() {
        System.out.println("setIntstatus");
        int intstatus = 0;
        MailMessage instance = new MailMessage();
        instance.setIntstatus(intstatus);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUsersids method, of class MailMessage.
     */
    @Test
    public void test__getUsersids() {
        System.out.println("__getUsersids");
        MailMessage instance = new MailMessage();
        int expResult = 0;
        int result = instance.__getUsersids();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUsersids method, of class MailMessage.
     */
    @Test
    public void testSetUsersids() {
        System.out.println("setUsersids");
        int usersids = 0;
        MailMessage instance = new MailMessage();
        instance.setUsersids(usersids);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getMailid method, of class MailMessage.
     */
    @Test
    public void test__getMailid() {
        System.out.println("__getMailid");
        MailMessage instance = new MailMessage();
        String expResult = "";
        String result = instance.__getMailid();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMailid method, of class MailMessage.
     */
    @Test
    public void testSetMailid() {
        System.out.println("setMailid");
        String mailid = "";
        MailMessage instance = new MailMessage();
        instance.setMailid(mailid);
        
        fail("The test case is a prototype.");
    }

}