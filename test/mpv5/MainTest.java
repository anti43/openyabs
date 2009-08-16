
package mpv5;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import java.awt.Window;
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
public class MainTest {

    public MainTest() {
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
     * Test of cache method, of class Main.
     */
    @Test
    public void testCache() {
        System.out.println("cache");
        Main.cache();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addOfficeApplicationToClose method, of class Main.
     */
    @Test
    public void testAddOfficeApplicationToClose() {
        System.out.println("addOfficeApplicationToClose");
        IOfficeApplication officeApplication = null;
        Main.addOfficeApplicationToClose(officeApplication);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class Main.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        Main.start();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of startup method, of class Main.
     */
    @Test
    public void testStartup() {
        System.out.println("startup");
        Main instance = new Main();
        instance.startup();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of configureWindow method, of class Main.
     */
    @Test
    public void testConfigureWindow() {
        System.out.println("configureWindow");
        Window root = null;
        Main instance = new Main();
        instance.configureWindow(root);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getApplication method, of class Main.
     */
    @Test
    public void testGetApplication() {
        System.out.println("getApplication");
        Main expResult = null;
        Main result = Main.getApplication();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of shutdown method, of class Main.
     */
    @Test
    public void testShutdown() {
        System.out.println("shutdown");
        Main instance = new Main();
        instance.shutdown();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        Main.main(args);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnv method, of class Main.
     */
    @Test
    public void testSetEnv() {
        System.out.println("setEnv");
        String rootDir = "";
        Main.setEnv(rootDir);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDerbyLog method, of class Main.
     */
    @Test
    public void testSetDerbyLog() {
        System.out.println("setDerbyLog");
        Main.setDerbyLog();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLaF method, of class Main.
     */
    @Test
    public void testSetLaF() {
        System.out.println("setLaF");
        String lafname = "";
        Main.setLaF(lafname);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of printEnv method, of class Main.
     */
    @Test
    public void testPrintEnv() {
        System.out.println("printEnv");
        Main.printEnv();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of go method, of class Main.
     */
    @Test
    public void testGo() {
        System.out.println("go");
        boolean firststart = false;
        Main instance = new Main();
        instance.go(firststart);
        
        fail("The test case is a prototype.");
    }

}