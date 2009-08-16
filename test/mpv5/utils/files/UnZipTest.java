
package mpv5.utils.files;

import java.util.zip.ZipEntry;
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
public class UnZipTest {

    public UnZipTest() {
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
     * Test of deflate method, of class UnZip.
     */
    @Test
    public void testDeflate() {
        System.out.println("deflate");
        String zipfiles = "";
        String toDir = "";
        UnZip.deflate(zipfiles, toDir);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMode method, of class UnZip.
     */
    @Test
    public void testSetMode() {
        System.out.println("setMode");
        int m = 0;
        UnZip instance = new UnZip();
        instance.setMode(m);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of unZip method, of class UnZip.
     */
    @Test
    public void testUnZip() {
        System.out.println("unZip");
        String fileName = "";
        UnZip instance = new UnZip();
        instance.unZip(fileName);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class UnZip.
     */
    @Test
    public void testGetFile() throws Exception {
        System.out.println("getFile");
        ZipEntry e = null;
        UnZip instance = new UnZip();
        instance.getFile(e);
        
        fail("The test case is a prototype.");
    }

}