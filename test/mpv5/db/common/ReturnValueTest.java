
package mpv5.db.common;

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
public class ReturnValueTest {

    public ReturnValueTest() {
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
     * Test of getId method, of class ReturnValue.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        ReturnValue instance = new ReturnValue();
        int expResult = 0;
        int result = instance.getId();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getData method, of class ReturnValue.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        ReturnValue instance = new ReturnValue();
        Object[][] expResult = null;
        Object[][] result = instance.getData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnnames method, of class ReturnValue.
     */
    @Test
    public void testGetColumnnames() {
        System.out.println("getColumnnames");
        ReturnValue instance = new ReturnValue();
        String[] expResult = null;
        String[] result = instance.getColumnnames();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set method, of class ReturnValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        ReturnValue returnValue = null;
        ReturnValue instance = new ReturnValue();
        instance.set(returnValue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMessage method, of class ReturnValue.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        ReturnValue instance = new ReturnValue();
        String expResult = "";
        String result = instance.getMessage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setMessage method, of class ReturnValue.
     */
    @Test
    public void testSetMessage() {
        System.out.println("setMessage");
        String message = "";
        ReturnValue instance = new ReturnValue();
        instance.setMessage(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class ReturnValue.
     */
    @Test
    public void testSetId() {
        System.out.println("setId");
        int id = 0;
        ReturnValue instance = new ReturnValue();
        instance.setId(id);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setData method, of class ReturnValue.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        Object[][] data = null;
        ReturnValue instance = new ReturnValue();
        instance.setData(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setColumnnames method, of class ReturnValue.
     */
    @Test
    public void testSetColumnnames() {
        System.out.println("setColumnnames");
        String[] columnnames = null;
        ReturnValue instance = new ReturnValue();
        instance.setColumnnames(columnnames);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasData method, of class ReturnValue.
     */
    @Test
    public void testHasData() {
        System.out.println("hasData");
        ReturnValue instance = new ReturnValue();
        boolean expResult = false;
        boolean result = instance.hasData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}