
package mpv5.utils.export;

import java.io.File;
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
public class ExportTest {

    public ExportTest() {
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
     * Test of addData method, of class Export.
     */
    @Test
    public void testAddData() {
        System.out.println("addData");
        String[][] data = null;
        Export instance = new Export();
        instance.addData(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFile method, of class Export.
     */
    @Test
    public void testSetFile() {
        System.out.println("setFile");
        Exportable templateFile = null;
        Export instance = new Export();
        instance.setFile(templateFile);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of processData method, of class Export.
     */
    @Test
    public void testProcessData() throws Exception {
        System.out.println("processData");
        File toFile = null;
        Export instance = new Export();
        instance.processData(toFile);
        
        fail("The test case is a prototype.");
    }

}