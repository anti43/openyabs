
package mpv5.utils.files;

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
public class JarFinderTest {

    public JarFinderTest() {
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
     * Test of getPathOfJar method, of class JarFinder.
     */
    @Test
    public void testGetPathOfJar() throws Exception {
        System.out.println("getPathOfJar");
        String nameOfJar = "";
        String expResult = "";
        String result = JarFinder.getPathOfJar(nameOfJar);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}