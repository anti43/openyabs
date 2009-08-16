
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
public class FileReaderWriterTest {

    public FileReaderWriterTest() {
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
     * Test of read1Line method, of class FileReaderWriter.
     */
    @Test
    public void testRead1Line() {
        System.out.println("read1Line");
        String skipsign = "";
        FileReaderWriter instance = null;
        String expResult = "";
        String result = instance.read1Line(skipsign);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of readLines method, of class FileReaderWriter.
     */
    @Test
    public void testReadLines() {
        System.out.println("readLines");
        FileReaderWriter instance = null;
        String[] expResult = null;
        String[] result = instance.readLines();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class FileReaderWriter.
     */
    @Test
    public void testWrite_StringArr() {
        System.out.println("write");
        String[] text = null;
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.write(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class FileReaderWriter.
     */
    @Test
    public void testWrite_String() {
        System.out.println("write");
        String text = "";
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.write(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of write0 method, of class FileReaderWriter.
     */
    @Test
    public void testWrite0_StringArr() {
        System.out.println("write0");
        String[] lines = null;
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.write0(lines);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of write0 method, of class FileReaderWriter.
     */
    @Test
    public void testWrite0_String() {
        System.out.println("write0");
        String text = "";
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.write0(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeOnce method, of class FileReaderWriter.
     */
    @Test
    public void testWriteOnce() {
        System.out.println("writeOnce");
        String text = "";
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.writeOnce(text);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class FileReaderWriter.
     */
    @Test
    public void testRead() {
        System.out.println("read");
        FileReaderWriter instance = null;
        String expResult = "";
        String result = instance.read();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteFile method, of class FileReaderWriter.
     */
    @Test
    public void testDeleteFile() {
        System.out.println("deleteFile");
        FileReaderWriter instance = null;
        instance.deleteFile();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeLine method, of class FileReaderWriter.
     */
    @Test
    public void testWriteLine() {
        System.out.println("writeLine");
        String[] text = null;
        String sep = "";
        FileReaderWriter instance = null;
        boolean expResult = false;
        boolean result = instance.writeLine(text, sep);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class FileReaderWriter.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        FileReaderWriter instance = null;
        instance.flush();
        
        fail("The test case is a prototype.");
    }

}