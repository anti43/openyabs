
package mpv5.ui.panels;

import mpv5.db.common.Context;
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
public class ContactsListTest {

    public ContactsListTest() {
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
     * Test of getContext method, of class ContactsList.
     */
    @Test
    public void testGetContext() {
        System.out.println("getContext");
        ContactsList instance = new ContactsList();
        Context expResult = null;
        Context result = instance.getContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class ContactsList.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context context = null;
        ContactsList instance = new ContactsList();
        instance.setContext(context);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of refresh method, of class ContactsList.
     */
    @Test
    public void testRefresh() {
        System.out.println("refresh");
        ContactsList instance = new ContactsList();
        instance.refresh();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class ContactsList.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        ContactsList instance = new ContactsList();
        instance.flush();
        
        fail("The test case is a prototype.");
    }

}