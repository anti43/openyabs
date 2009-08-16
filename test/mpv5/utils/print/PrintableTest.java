
package mpv5.utils.print;

import java.io.File;
import javax.print.DocFlavor;
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
public class PrintableTest {

    public PrintableTest() {
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
     * Test of getFlavor method, of class Printable.
     */
    @Test
    public void testGetFlavor() {
        System.out.println("getFlavor");
        Printable instance = new PrintableImpl();
        DocFlavor expResult = null;
        DocFlavor result = instance.getFlavor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class Printable.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        Printable instance = new PrintableImpl();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class PrintableImpl  implements Printable {

        public DocFlavor getFlavor() {
            return null;
        }

        public File getFile() {
            return null;
        }
    }

}