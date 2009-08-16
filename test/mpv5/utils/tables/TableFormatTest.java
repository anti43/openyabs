
package mpv5.utils.tables;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
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
public class TableFormatTest {

    public TableFormatTest() {
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
     * Test of changeToClassValue method, of class TableFormat.
     */
    @Test
    public void testChangeToClassValue() {
        System.out.println("changeToClassValue");
        Object[][] values = null;
        Class aClass = null;
        int[] cols = null;
        Object[][] expResult = null;
        Object[][] result = TableFormat.changeToClassValue(values, aClass, cols);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeUneditable method, of class TableFormat.
     */
    @Test
    public void testMakeUneditable() {
        System.out.println("makeUneditable");
        JTable table = null;
        TableFormat.makeUneditable(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeUneditableColumns method, of class TableFormat.
     */
    @Test
    public void testMakeUneditableColumns() {
        System.out.println("makeUneditableColumns");
        JTable table = null;
        Integer[] desiredCol = null;
        TableFormat.makeUneditableColumns(table, desiredCol);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopEditing method, of class TableFormat.
     */
    @Test
    public void testStopEditing() {
        System.out.println("stopEditing");
        JTable jTable1 = null;
        TableFormat.stopEditing(jTable1);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resizeCols method, of class TableFormat.
     */
    @Test
    public void testResizeCols_3args_1() {
        System.out.println("resizeCols");
        JTable table = null;
        Integer[] desiredColSizes = null;
        boolean fixed = false;
        TableFormat.resizeCols(table, desiredColSizes, fixed);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUneditableTable method, of class TableFormat.
     */
    @Test
    public void testGetUneditableTable() {
        System.out.println("getUneditableTable");
        String[][] data = null;
        String[] header = null;
        DefaultTableModel expResult = null;
        DefaultTableModel result = TableFormat.getUneditableTable(data, header);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripColumn method, of class TableFormat.
     */
    @Test
    public void testStripColumn() {
        System.out.println("stripColumn");
        JTable table = null;
        int columnToHide = 0;
        TableFormat.stripColumn(table, columnToHide);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripColumns method, of class TableFormat.
     */
    @Test
    public void testStripColumns() {
        System.out.println("stripColumns");
        JTable resulttable = null;
        int[] i = null;
        TableFormat.stripColumns(resulttable, i);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of stripFirstColumn method, of class TableFormat.
     */
    @Test
    public void testStripFirstColumn() {
        System.out.println("stripFirstColumn");
        JTable table = null;
        TableFormat.stripFirstColumn(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of format method, of class TableFormat.
     */
    @Test
    public void testFormat() {
        System.out.println("format");
        JTable table = null;
        int column = 0;
        int width = 0;
        TableFormat.format(table, column, width);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resizeCols method, of class TableFormat.
     */
    @Test
    public void testResizeCols_3args_2() {
        System.out.println("resizeCols");
        JTable table = null;
        Integer[] desiredColSizes = null;
        Boolean[] fixedCols = null;
        TableFormat.resizeCols(table, desiredColSizes, fixedCols);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeBackground method, of class TableFormat.
     */
    @Test
    public void testChangeBackground() {
        System.out.println("changeBackground");
        JTable table = null;
        int column = 0;
        Color color = null;
        TableFormat.changeBackground(table, column, color);
        
        fail("The test case is a prototype.");
    }

}