
package mpv5.utils.files;

import java.io.File;
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
public class FileActionHandlerTest {

    public FileActionHandlerTest() {
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
     * Test of print method, of class FileActionHandler.
     */
    @Test
    public void testPrint() {
        System.out.println("print");
        File f = null;
        FileActionHandler.print(f);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of edit method, of class FileActionHandler.
     */
    @Test
    public void testEdit() {
        System.out.println("edit");
        File f = null;
        FileActionHandler.edit(f);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of open method, of class FileActionHandler.
     */
    @Test
    public void testOpen() {
        System.out.println("open");
        File f = null;
        FileActionHandler.open(f);
        
        fail("The test case is a prototype.");
    }

}