
package mpv5.globals;

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
public class HeadersTest {

    public HeadersTest() {
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
     * Test of values method, of class Headers.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        Headers[] expResult = null;
        Headers[] result = Headers.values();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of valueOf method, of class Headers.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        String name = "";
        Headers expResult = null;
        Headers result = Headers.valueOf(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class Headers.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        Headers instance = null;
        String[] expResult = null;
        String[] result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class Headers.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String[] header = null;
        Headers instance = null;
        instance.setValue(header);
        
        fail("The test case is a prototype.");
    }

}