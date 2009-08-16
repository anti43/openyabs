
package mpv5.utils.files;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
public class ZipTest {

    public ZipTest() {
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
     * Test of zip method, of class Zip.
     */
    @Test
    public void testZip() {
        System.out.println("zip");
        String source = "";
        String target = "";
        Zip.zip(source, target);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of extract method, of class Zip.
     */
    @Test
    public void testExtract() throws Exception {
        System.out.println("extract");
        ZipFile zipFile = null;
        ZipEntry zipEntry = null;
        File toDir = null;
        Zip.extract(zipFile, zipEntry, toDir);
        
        fail("The test case is a prototype.");
    }

}