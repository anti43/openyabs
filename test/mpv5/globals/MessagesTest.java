
package mpv5.globals;

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
public class MessagesTest {

    public MessagesTest() {
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
     * Test of values method, of class Messages.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        Messages[] expResult = null;
        Messages[] result = Messages.values();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of valueOf method, of class Messages.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "";
        Messages expResult = null;
        Messages result = Messages.valueOf(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addMessage method, of class Messages.
     */
    @Test
    public void testAddMessage() {
        System.out.println("addMessage");
        String message = "";
        Messages instance = null;
        instance.addMessage(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Messages.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Messages instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class Messages.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        Messages instance = null;
        String expResult = "";
        String result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}