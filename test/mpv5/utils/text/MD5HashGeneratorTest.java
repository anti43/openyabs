
package mpv5.utils.text;

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
public class MD5HashGeneratorTest {

    public MD5HashGeneratorTest() {
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
     * Test of getInstance method, of class MD5HashGenerator.
     */
    @Test
    public void testGetInstance() throws Exception {
        System.out.println("getInstance");
        MD5HashGenerator expResult = null;
        MD5HashGenerator result = MD5HashGenerator.getInstance();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashData method, of class MD5HashGenerator.
     */
    @Test
    public void testHashData_charArr() {
        System.out.println("hashData");
        char[] password = null;
        MD5HashGenerator instance = null;
        String expResult = "";
        String result = instance.hashData(password);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashData method, of class MD5HashGenerator.
     */
    @Test
    public void testHashData_byteArr() {
        System.out.println("hashData");
        byte[] dataToHash = null;
        MD5HashGenerator instance = null;
        String expResult = "";
        String result = instance.hashData(dataToHash);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hexStringFromBytes method, of class MD5HashGenerator.
     */
    @Test
    public void testHexStringFromBytes() {
        System.out.println("hexStringFromBytes");
        byte[] b = null;
        MD5HashGenerator instance = null;
        String expResult = "";
        String result = instance.hexStringFromBytes(b);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}