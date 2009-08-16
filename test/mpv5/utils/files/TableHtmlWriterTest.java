
package mpv5.utils.files;

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

    /**
     * Test of isShowHorizontalLines method, of class TableHtmlWriter.
     */
    @Test
    public void testIsShowHorizontalLines() {
        System.out.println("isShowHorizontalLines");
        TableHtmlWriter instance = null;
        boolean expResult = false;
        boolean result = instance.isShowHorizontalLines();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setShowHorizontalLines method, of class TableHtmlWriter.
     */
    @Test
    public void testSetShowHorizontalLines() {
        System.out.println("setShowHorizontalLines");
        boolean showHorizontalLines = false;
        TableHtmlWriter instance = null;
        instance.setShowHorizontalLines(showHorizontalLines);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isShowVerticalLines method, of class TableHtmlWriter.
     */
    @Test
    public void testIsShowVerticalLines() {
        System.out.println("isShowVerticalLines");
        TableHtmlWriter instance = null;
        boolean expResult = false;
        boolean result = instance.isShowVerticalLines();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setShowVerticalLines method, of class TableHtmlWriter.
     */
    @Test
    public void testSetShowVerticalLines() {
        System.out.println("setShowVerticalLines");
        boolean showVerticalLines = false;
        TableHtmlWriter instance = null;
        instance.setShowVerticalLines(showVerticalLines);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBorderColor method, of class TableHtmlWriter.
     */
    @Test
    public void testGetBorderColor() {
        System.out.println("getBorderColor");
        TableHtmlWriter instance = null;
        Color expResult = null;
        Color result = instance.getBorderColor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBorderColor method, of class TableHtmlWriter.
     */
    @Test
    public void testSetBorderColor() {
        System.out.println("setBorderColor");
        Color borderColor = null;
        TableHtmlWriter instance = null;
        instance.setBorderColor(borderColor);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createHtml2 method, of class TableHtmlWriter.
     */
    @Test
    public void testCreateHtml2() {
        System.out.println("createHtml2");
        Integer border = null;
        Color bordercolor = null;
        TableHtmlWriter instance = null;
        File expResult = null;
        File result = instance.createHtml2(border, bordercolor);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}