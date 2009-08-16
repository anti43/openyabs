
package mpv5.utils.print;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.DatabaseObject;
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
public class PrintJobTest {

    public PrintJobTest() {
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
     * Test of print method, of class PrintJob.
     */
    @Test
    public void testPrint_ArrayList() {
        System.out.println("print");
        ArrayList<File> filelist = null;
        PrintJob instance = new PrintJob();
        instance.print(filelist);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of printl method, of class PrintJob.
     */
    @Test
    public void testPrintl() {
        System.out.println("printl");
        List<DatabaseObject> dbobjarr = null;
        PrintJob instance = new PrintJob();
        instance.printl(dbobjarr);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class PrintJob.
     */
    @Test
    public void testPrint_DatabaseObject() {
        System.out.println("print");
        DatabaseObject dbobj = null;
        PrintJob instance = new PrintJob();
        instance.print(dbobj);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class PrintJob.
     */
    @Test
    public void testPrint_File() throws Exception {
        System.out.println("print");
        File file = null;
        PrintJob instance = new PrintJob();
        instance.print(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class PrintJob.
     */
    @Test
    public void testPrint_Printable() {
        System.out.println("print");
        Printable printable = null;
        PrintJob instance = new PrintJob();
        instance.print(printable);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set method, of class PrintJob.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        Object object = null;
        Exception e = null;
        PrintJob instance = new PrintJob();
        instance.set(object, e);
        
        fail("The test case is a prototype.");
    }

}