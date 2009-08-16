
package mpv5.server;

import java.io.PrintStream;
import java.net.Socket;
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
public class MPServerJobTest {

    public MPServerJobTest() {
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
     * Test of getSock method, of class MPServerJob.
     */
    @Test
    public void testGetSock() {
        System.out.println("getSock");
        MPServerJob instance = new MPServerJobImpl();
        Socket expResult = null;
        Socket result = instance.getSock();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOut method, of class MPServerJob.
     */
    @Test
    public void testGetOut() {
        System.out.println("getOut");
        MPServerJob instance = new MPServerJobImpl();
        PrintStream expResult = null;
        PrintStream result = instance.getOut();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getXmlData method, of class MPServerJob.
     */
    @Test
    public void testGetXmlData() {
        System.out.println("getXmlData");
        MPServerJob instance = new MPServerJobImpl();
        String expResult = "";
        String result = instance.getXmlData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class MPServerJob.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        MPServerJob instance = new MPServerJobImpl();
        instance.start();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSock method, of class MPServerJob.
     */
    @Test
    public void testSetSock() throws Exception {
        System.out.println("setSock");
        Socket sock = null;
        MPServerJob instance = new MPServerJobImpl();
        instance.setSock(sock);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOut method, of class MPServerJob.
     */
    @Test
    public void testSetOut() {
        System.out.println("setOut");
        PrintStream out = null;
        MPServerJob instance = new MPServerJobImpl();
        instance.setOut(out);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setXmlData method, of class MPServerJob.
     */
    @Test
    public void testSetXmlData() {
        System.out.println("setXmlData");
        String xmlData = "";
        MPServerJob instance = new MPServerJobImpl();
        instance.setXmlData(xmlData);
        
        fail("The test case is a prototype.");
    }

    public class MPServerJobImpl extends MPServerJob {

        public void start() {
        }

        @Override
        public Exception waitFor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void set(Object object, Exception exception) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}