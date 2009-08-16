
package mpv5.utils.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
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
public class StreamsTest {

    public StreamsTest() {
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
     * Test of readLines method, of class Streams.
     */
    @Test
    public void testReadLines_InputStream() throws Exception {
        System.out.println("readLines");
        InputStream input = null;
        Streams instance = new Streams();
        String[] expResult = null;
        String[] result = instance.readLines(input);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of readLines method, of class Streams.
     */
    @Test
    public void testReadLines_Reader() throws Exception {
        System.out.println("readLines");
        Reader reader = null;
        Streams instance = new Streams();
        String[] expResult = null;
        String[] result = instance.readLines(reader);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of copy method, of class Streams.
     */
    @Test
    public void testCopy() throws Exception {
        System.out.println("copy");
        InputStream in = null;
        OutputStream out = null;
        Streams instance = new Streams();
        instance.copy(in, out);
        
        fail("The test case is a prototype.");
    }

}