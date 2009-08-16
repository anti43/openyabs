
package mpv5.data;

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
public class TrashHandlerTest {

    public TrashHandlerTest() {
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
     * Test of delete method, of class TrashHandler.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        String type = "";
        int id = 0;
        String message = "";
        TrashHandler.delete(type, id, message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getData method, of class TrashHandler.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        Object[][] expResult = null;
        Object[][] result = TrashHandler.getData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of restore method, of class TrashHandler.
     */
    @Test
    public void testRestore() {
        System.out.println("restore");
        String type = "";
        int id = 0;
        String message = "";
        TrashHandler.restore(type, id, message);
        
        fail("The test case is a prototype.");
    }

}