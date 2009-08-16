
package mpv5.utils.export;

import java.io.File;
import java.util.HashMap;
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
public class ExportableTest {

    public ExportableTest() {
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
     * Test of setTarget method, of class Exportable.
     */
    @Test
    public void testSetTarget() {
        System.out.println("setTarget");
        File target = null;
        Exportable instance = null;
        instance.setTarget(target);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTarget method, of class Exportable.
     */
    @Test
    public void testGetTarget() {
        System.out.println("getTarget");
        Exportable instance = null;
        File expResult = null;
        File result = instance.getTarget();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setData method, of class Exportable.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        HashMap<String, String> data = null;
        Exportable instance = null;
        instance.setData(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getData method, of class Exportable.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        Exportable instance = null;
        HashMap expResult = null;
        HashMap result = instance.getData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class ExportableImpl extends Exportable {

        public ExportableImpl() {
            super("");
        }
    }

}