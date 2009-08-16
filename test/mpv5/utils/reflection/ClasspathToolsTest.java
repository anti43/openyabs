
package mpv5.utils.reflection;

import java.io.File;
import java.net.URI;
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
public class ClasspathToolsTest {

    public ClasspathToolsTest() {
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
     * Test of addPath method, of class ClasspathTools.
     */
    @Test
    public void testAddPath_File() {
        System.out.println("addPath");
        File file = null;
        ClasspathTools.addPath(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPath method, of class ClasspathTools.
     */
    @Test
    public void testAddPath_String() throws Exception {
        System.out.println("addPath");
        String s = "";
        ClasspathTools.addPath(s);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPath method, of class ClasspathTools.
     */
    @Test
    public void testAddPath_URI() {
        System.out.println("addPath");
        URI s = null;
        ClasspathTools.addPath(s);
        
        fail("The test case is a prototype.");
    }

}