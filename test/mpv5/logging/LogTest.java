
package mpv5.logging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;
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
public class LogTest {

    public LogTest() {
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
     * Test of Debug method, of class Log.
     */
    @Test
    public void testDebug_File() {
        System.out.println("Debug");
        File file = null;
        Log.Debug(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Debug method, of class Log.
     */
    @Test
    public void testDebug_3args_1() {
        System.out.println("Debug");
        Object source = null;
        Object message = null;
        boolean alwaysToKonsole = false;
        Log.Debug(source, message, alwaysToKonsole);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Debug method, of class Log.
     */
    @Test
    public void testDebug_Object_Object() {
        System.out.println("Debug");
        Object source = null;
        Object message = null;
        Log.Debug(source, message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Debug method, of class Log.
     */
    @Test
    public void testDebug_3args_2() {
        System.out.println("Debug");
        Class source = null;
        Object message = null;
        boolean alwaysToKonsole = false;
        Log.Debug(source, message, alwaysToKonsole);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Print method, of class Log.
     */
    @Test
    public void testPrint() {
        System.out.println("Print");
        Object message = null;
        Log.Print(message);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_ArrayList() {
        System.out.println("PrintArray");
        ArrayList data = null;
        Log.PrintArray(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_ObjectArrArrArr() {
        System.out.println("PrintArray");
        Object[][][] array = null;
        Log.PrintArray(array);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_ObjectArrArr() {
        System.out.println("PrintArray");
        Object[][] array = null;
        Log.PrintArray(array);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_ObjectArr() {
        System.out.println("PrintArray");
        Object[] string = null;
        Log.PrintArray(string);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_List() {
        System.out.println("PrintArray");
        List lst = null;
        Log.PrintArray(lst);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Debug method, of class Log.
     */
    @Test
    public void testDebug_Exception() {
        System.out.println("Debug");
        Exception ex = null;
        Log.Debug(ex);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogger method, of class Log.
     */
    @Test
    public void testGetLogger() {
        System.out.println("getLogger");
        LogConsole expResult = null;
        LogConsole result = Log.getLogger();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogLevel method, of class Log.
     */
    @Test
    public void testSetLogLevel() {
        System.out.println("setLogLevel");
        int level = 0;
        Log.setLogLevel(level);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PrintArray method, of class Log.
     */
    @Test
    public void testPrintArray_TableModel() {
        System.out.println("PrintArray");
        TableModel model = null;
        Log.PrintArray(model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLoglevel method, of class Log.
     */
    @Test
    public void testGetLoglevel() {
        System.out.println("getLoglevel");
        int expResult = 0;
        int result = Log.getLoglevel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStackTrace method, of class Log.
     */
    @Test
    public void testGetStackTrace() {
        System.out.println("getStackTrace");
        Throwable t = null;
        String expResult = "";
        String result = Log.getStackTrace(t);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}