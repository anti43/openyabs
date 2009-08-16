
package enoa.handler;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.ITextTableService;
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
public class TableHandlerTest {

    public TableHandlerTest() {
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
     * Test of getRowCount method, of class TableHandler.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        TableHandler instance = null;
        int expResult = 0;
        int result = instance.getRowCount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnCount method, of class TableHandler.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
        TableHandler instance = null;
        int expResult = 0;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addRows method, of class TableHandler.
     */
    @Test
    public void testAddRows() throws Exception {
        System.out.println("addRows");
        int count = 0;
        TableHandler instance = null;
        instance.addRows(count);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHeaderLines method, of class TableHandler.
     */
    @Test
    public void testSetHeaderLines() throws Exception {
        System.out.println("setHeaderLines");
        int lines = 0;
        TableHandler instance = null;
        instance.setHeaderLines(lines);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValueAt method, of class TableHandler.
     */
    @Test
    public void testSetValueAt() throws Exception {
        System.out.println("setValueAt");
        Object value = null;
        int column = 0;
        int row = 0;
        TableHandler instance = null;
        instance.setValueAt(value, column, row);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValueAt method, of class TableHandler.
     */
    @Test
    public void testGetValueAt() throws Exception {
        System.out.println("getValueAt");
        int column = 0;
        int row = 0;
        TableHandler instance = null;
        Object expResult = null;
        Object result = instance.getValueAt(column, row);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setColumnWidth method, of class TableHandler.
     */
    @Test
    public void testSetColumnWidth() throws Exception {
        System.out.println("setColumnWidth");
        int column = 0;
        short width = 0;
        TableHandler instance = null;
        instance.setColumnWidth(column, width);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTable method, of class TableHandler.
     */
    @Test
    public void testAddTable() throws Exception {
        System.out.println("addTable");
        Object[][] values = null;
        TableHandler instance = null;
        ITextTable expResult = null;
        ITextTable result = instance.addTable(values);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDoc method, of class TableHandler.
     */
    @Test
    public void testGetDoc() {
        System.out.println("getDoc");
        TableHandler instance = null;
        ITextDocument expResult = null;
        ITextDocument result = instance.getDoc();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableService method, of class TableHandler.
     */
    @Test
    public void testGetTableService() {
        System.out.println("getTableService");
        TableHandler instance = null;
        ITextTableService expResult = null;
        ITextTableService result = instance.getTableService();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTable method, of class TableHandler.
     */
    @Test
    public void testGetTable() {
        System.out.println("getTable");
        TableHandler instance = null;
        ITextTable expResult = null;
        ITextTable result = instance.getTable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}