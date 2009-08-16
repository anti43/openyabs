
package mpv5.utils.models;

import java.util.List;
import java.util.Vector;
import mpv5.db.common.Context;
import mpv5.utils.tables.TableCalculator;
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
public class MPTableModelTest {

    public MPTableModelTest() {
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
     * Test of addCalculator method, of class MPTableModel.
     */
    @Test
    public void testAddCalculator() {
        System.out.println("addCalculator");
        TableCalculator cv = null;
        MPTableModel instance = new MPTableModel();
        instance.addCalculator(cv);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnClass method, of class MPTableModel.
     */
    @Test
    public void testGetColumnClass() {
        System.out.println("getColumnClass");
        int columnIndex = 0;
        MPTableModel instance = new MPTableModel();
        Class expResult = null;
        Class result = instance.getColumnClass(columnIndex);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCellEditable method, of class MPTableModel.
     */
    @Test
    public void testIsCellEditable() {
        System.out.println("isCellEditable");
        int rowIndex = 0;
        int columnIndex = 0;
        MPTableModel instance = new MPTableModel();
        boolean expResult = false;
        boolean result = instance.isCellEditable(rowIndex, columnIndex);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTypes method, of class MPTableModel.
     */
    @Test
    public void testGetTypes() {
        System.out.println("getTypes");
        MPTableModel instance = new MPTableModel();
        Class[] expResult = null;
        Class[] result = instance.getTypes();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTypes method, of class MPTableModel.
     */
    @Test
    public void testSetTypes() {
        System.out.println("setTypes");
        Class[] types = null;
        MPTableModel instance = new MPTableModel();
        instance.setTypes(types);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCanEdits method, of class MPTableModel.
     */
    @Test
    public void testGetCanEdits() {
        System.out.println("getCanEdits");
        MPTableModel instance = new MPTableModel();
        boolean[] expResult = null;
        boolean[] result = instance.getCanEdits();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCanEdits method, of class MPTableModel.
     */
    @Test
    public void testSetCanEdits() {
        System.out.println("setCanEdits");
        boolean[] canEdits = null;
        MPTableModel instance = new MPTableModel();
        instance.setCanEdits(canEdits);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnIdentifiers method, of class MPTableModel.
     */
    @Test
    public void testGetColumnIdentifiers() {
        System.out.println("getColumnIdentifiers");
        MPTableModel instance = new MPTableModel();
        Vector expResult = null;
        Vector result = instance.getColumnIdentifiers();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEditable method, of class MPTableModel.
     */
    @Test
    public void testSetEditable() {
        System.out.println("setEditable");
        boolean bool = false;
        MPTableModel instance = new MPTableModel();
        instance.setEditable(bool);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValueAt method, of class MPTableModel.
     */
    @Test
    public void testGetValueAt() {
        System.out.println("getValueAt");
        int row = 0;
        int column = 0;
        MPTableModel instance = new MPTableModel();
        Object expResult = null;
        Object result = instance.getValueAt(row, column);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValueAt method, of class MPTableModel.
     */
    @Test
    public void testSetValueAt() {
        System.out.println("setValueAt");
        Object aValue = null;
        int row = 0;
        int column = 0;
        boolean dontFire = false;
        MPTableModel instance = new MPTableModel();
        instance.setValueAt(aValue, row, column, dontFire);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fireTableCellUpdated method, of class MPTableModel.
     */
    @Test
    public void testFireTableCellUpdated() {
        System.out.println("fireTableCellUpdated");
        int row = 0;
        int column = 0;
        MPTableModel instance = new MPTableModel();
        instance.fireTableCellUpdated(row, column);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasEmptyRows method, of class MPTableModel.
     */
    @Test
    public void testHasEmptyRows() {
        System.out.println("hasEmptyRows");
        int[] columnsToCheck = null;
        MPTableModel instance = new MPTableModel();
        boolean expResult = false;
        boolean result = instance.hasEmptyRows(columnsToCheck);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addRow method, of class MPTableModel.
     */
    @Test
    public void testAddRow() {
        System.out.println("addRow");
        int count = 0;
        MPTableModel instance = new MPTableModel();
        instance.addRow(count);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContext method, of class MPTableModel.
     */
    @Test
    public void testGetContext() {
        System.out.println("getContext");
        MPTableModel instance = new MPTableModel();
        Context expResult = null;
        Context result = instance.getContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class MPTableModel.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context context = null;
        MPTableModel instance = new MPTableModel();
        instance.setContext(context);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of defineRow method, of class MPTableModel.
     */
    @Test
    public void testDefineRow() {
        System.out.println("defineRow");
        Object[] object = null;
        MPTableModel instance = new MPTableModel();
        instance.defineRow(object);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValidRows method, of class MPTableModel.
     */
    @Test
    public void testGetValidRows() {
        System.out.println("getValidRows");
        int[] columns = null;
        MPTableModel instance = new MPTableModel();
        List expResult = null;
        List result = instance.getValidRows(columns);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRowAt method, of class MPTableModel.
     */
    @Test
    public void testSetRowAt() {
        System.out.println("setRowAt");
        Object[] rowData = null;
        int row = 0;
        int columnToIgnore = 0;
        MPTableModel instance = new MPTableModel();
        instance.setRowAt(rowData, row, columnToIgnore);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAutoCountColumn method, of class MPTableModel.
     */
    @Test
    public void testSetAutoCountColumn() {
        System.out.println("setAutoCountColumn");
        int column = 0;
        MPTableModel instance = new MPTableModel();
        instance.setAutoCountColumn(column);
        
        fail("The test case is a prototype.");
    }

}