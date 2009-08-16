
package mpv5.utils.html;

import java.awt.Color;
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
public class TableHtmlWriterTest {

    public TableHtmlWriterTest() {
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
     * Test of createHtml method, of class TableHtmlWriter.
     */
    @Test
    public void testCreateHtml_Integer_Color() {
        System.out.println("createHtml");
        Integer border = null;
        Color bordercolor = null;
        TableHtmlWriter instance = null;
        File expResult = null;
        File result = instance.createHtml(border, bordercolor);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createHtml method, of class TableHtmlWriter.
     */
    @Test
    public void testCreateHtml_0args() {
        System.out.println("createHtml");
        TableHtmlWriter instance = null;
        File expResult = null;
        File result = instance.createHtml();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModel method, of class TableHtmlWriter.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        TableHtmlWriter instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class TableHtmlWriter.
     */
    @Test
    public void testSetModel() {
        System.out.println("setModel");
        Object[][] model = null;
        TableHtmlWriter instance = null;
        instance.setModel(model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHeader method, of class TableHtmlWriter.
     */
    @Test
    public void testGetHeader() {
        System.out.println("getHeader");
        TableHtmlWriter instance = null;
        String[] expResult = null;
        String[] result = instance.getHeader();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHeader method, of class TableHtmlWriter.
     */
    @Test
    public void testSetHeader() {
        System.out.println("setHeader");
        String[] header = null;
        TableHtmlWriter instance = null;
        instance.setHeader(header);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class TableHtmlWriter.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        TableHtmlWriter instance = null;
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFile method, of class TableHtmlWriter.
     */
    @Test
    public void testSetFile() {
        System.out.println("setFile");
        File file = null;
        TableHtmlWriter instance = null;
        instance.setFile(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPrefix method, of class TableHtmlWriter.
     */
    @Test
    public void testGetPrefix() {
        System.out.println("getPrefix");
        TableHtmlWriter instance = null;
        String expResult = "";
        String result = instance.getPrefix();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPrefix method, of class TableHtmlWriter.
     */
    @Test
    public void testSetPrefix() {
        System.out.println("setPrefix");
        String prefix = "";
        TableHtmlWriter instance = null;
        instance.setPrefix(prefix);
        
        fail("The test case is a prototype.");
    }

}