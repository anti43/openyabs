
package mpv5.logging;

import java.io.File;
import javax.swing.JComponent;
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
public class LogConsoleTest {

    public LogConsoleTest() {
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
     * Test of setLogStreams method, of class LogConsole.
     */
    @Test
    public void testSetLogStreams() {
        System.out.println("setLogStreams");
        boolean fileLog = false;
        boolean consoleLog = false;
        boolean windowLog = false;
        LogConsole.setLogStreams(fileLog, consoleLog, windowLog);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogFile method, of class LogConsole.
     */
    @Test
    public void testSetLogFile() throws Exception {
        System.out.println("setLogFile");
        String file = "";
        LogConsole.setLogFile(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogfile method, of class LogConsole.
     */
    @Test
    public void testGetLogfile() {
        System.out.println("getLogfile");
        File expResult = null;
        File result = LogConsole.getLogfile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of log method, of class LogConsole.
     */
    @Test
    public void testLog_0args() throws Exception {
        System.out.println("log");
        LogConsole instance = new LogConsole();
        instance.log();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of log method, of class LogConsole.
     */
    @Test
    public void testLog_Object() throws Exception {
        System.out.println("log");
        Object object = null;
        LogConsole instance = new LogConsole();
        instance.log(object);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of open method, of class LogConsole.
     */
    @Test
    public void testOpen() {
        System.out.println("open");
        LogConsole instance = new LogConsole();
        JComponent expResult = null;
        JComponent result = instance.open();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class LogConsole.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        LogConsole.main(args);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of flush method, of class LogConsole.
     */
    @Test
    public void testFlush() {
        System.out.println("flush");
        LogConsole instance = new LogConsole();
        instance.flush();
        
        fail("The test case is a prototype.");
    }

}