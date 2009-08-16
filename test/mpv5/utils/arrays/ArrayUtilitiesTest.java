
package mpv5.utils.arrays;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import mpv5.utils.numbers.Ip;
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
public class ArrayUtilitiesTest {

    public ArrayUtilitiesTest() {
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
     * Test of addToTable method, of class ArrayUtilities.
     */
    @Test
    public void testAddToTable_JTable_ObjectArr() {
        System.out.println("addToTable");
        JTable table = null;
        Object[] row = null;
        ArrayUtilities.addToTable(table, row);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addToTable method, of class ArrayUtilities.
     */
    @Test
    public void testAddToTable_JTable_ObjectArrArr() {
        System.out.println("addToTable");
        JTable table = null;
        Object[][] rows = null;
        ArrayUtilities.addToTable(table, rows);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeToClassValue method, of class ArrayUtilities.
     */
    @Test
    public void testChangeToClassValue() {
        System.out.println("changeToClassValue");
        Object[][] d = null;
        int column = 0;
        Class aClass = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.changeToClassValue(d, column, aClass);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnAsArray method, of class ArrayUtilities.
     */
    @Test
    public void testGetColumnAsArray() {
        System.out.println("getColumnAsArray");
        JTable table = null;
        int column = 0;
        Object[] expResult = null;
        Object[] result = ArrayUtilities.getColumnAsArray(table, column);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashTableToArray method, of class ArrayUtilities.
     */
    @Test
    public void testHashTableToArray() {
        System.out.println("hashTableToArray");
        Hashtable<String, Object> table = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.hashTableToArray(table);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeColumn method, of class ArrayUtilities.
     */
    @Test
    public void testRemoveColumn() {
        System.out.println("removeColumn");
        JTable table = null;
        int vColIndex = 0;
        ArrayUtilities.removeColumn(table, vColIndex);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of replaceColumn method, of class ArrayUtilities.
     */
    @Test
    public void testReplaceColumn() {
        System.out.println("replaceColumn");
        JTable table = null;
        int column = 0;
        Object[] columndata = null;
        ArrayUtilities.replaceColumn(table, column, columndata);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of tableModelToFile method, of class ArrayUtilities.
     */
    @Test
    public void testTableModelToFile() {
        System.out.println("tableModelToFile");
        JTable table = null;
        String separator = "";
        File expResult = null;
        File result = ArrayUtilities.tableModelToFile(table, separator);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toTableModel method, of class ArrayUtilities.
     */
    @Test
    public void testToTableModel() {
        System.out.println("toTableModel");
        HashMap data = null;
        DefaultTableModel expResult = null;
        DefaultTableModel result = ArrayUtilities.toTableModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeDuplicates method, of class ArrayUtilities.
     */
    @Test
    public void testRemoveDuplicates() {
        System.out.println("removeDuplicates");
        ArrayList arlList = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.removeDuplicates(arlList);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toTreeModel method, of class ArrayUtilities.
     */
    @Test
    public void testToTreeModel() {
        System.out.println("toTreeModel");
        HashMap<String, Ip> data = null;
        DefaultTreeModel expResult = null;
        DefaultTreeModel result = ArrayUtilities.toTreeModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSelectionFromTree method, of class ArrayUtilities.
     */
    @Test
    public void testGetSelectionFromTree() {
        System.out.println("getSelectionFromTree");
        JTree tree = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.getSelectionFromTree(tree);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of tableModelToArray method, of class ArrayUtilities.
     */
    @Test
    public void testTableModelToArray_JTable() {
        System.out.println("tableModelToArray");
        JTable table = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.tableModelToArray(table);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of tableModelToArray method, of class ArrayUtilities.
     */
    @Test
    public void testTableModelToArray_JTable_boolean() {
        System.out.println("tableModelToArray");
        JTable table = null;
        boolean onlyTheSelectedRows = false;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.tableModelToArray(table, onlyTheSelectedRows);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeSelectedRowFromTable method, of class ArrayUtilities.
     */
    @Test
    public void testRemoveSelectedRowFromTable() {
        System.out.println("removeSelectedRowFromTable");
        JTable table = null;
        ArrayUtilities.removeSelectedRowFromTable(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reverseArray method, of class ArrayUtilities.
     */
    @Test
    public void testReverseArray_ObjectArrArr() {
        System.out.println("reverseArray");
        Object[][] array = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.reverseArray(array);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addRowToTable method, of class ArrayUtilities.
     */
    @Test
    public void testAddRowToTable() {
        System.out.println("addRowToTable");
        JTable table = null;
        ArrayUtilities.addRowToTable(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of inserValue method, of class ArrayUtilities.
     */
    @Test
    public void testInserValue() {
        System.out.println("inserValue");
        Object[][] original_array = null;
        Object value = null;
        int place = 0;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.inserValue(original_array, value, place);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of formatTableArrayYesNo method, of class ArrayUtilities.
     */
    @Test
    public void testFormatTableArrayYesNo() {
        System.out.println("formatTableArrayYesNo");
        String[][] table = null;
        int[] columns = null;
        String[][] expResult = null;
        String[][] result = ArrayUtilities.formatTableArrayYesNo(table, columns);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toObjectArray method, of class ArrayUtilities.
     */
    @Test
    public void testToObjectArray() {
        System.out.println("toObjectArray");
        String[][] originalarray = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.toObjectArray(originalarray);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashTableKeysToArray method, of class ArrayUtilities.
     */
    @Test
    public void testHashTableKeysToArray() {
        System.out.println("hashTableKeysToArray");
        Hashtable<String, Object> data = null;
        String[] expResult = null;
        String[] result = ArrayUtilities.hashTableKeysToArray(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashMapToArray method, of class ArrayUtilities.
     */
    @Test
    public void testHashMapToArray() {
        System.out.println("hashMapToArray");
        HashMap<String, String> map = null;
        ArrayUtilities instance = new ArrayUtilities();
        Object[][] expResult = null;
        Object[][] result = instance.hashMapToArray(map);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of merge method, of class ArrayUtilities.
     */
    @Test
    public void testMerge_ObjectArrArr_ObjectArrArr() {
        System.out.println("merge");
        Object[][] array1 = null;
        Object[][] array2 = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.merge(array1, array2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of merge method, of class ArrayUtilities.
     */
    @Test
    public void testMerge_ObjectArr_ObjectArrArr() {
        System.out.println("merge");
        Object[] array1 = null;
        Object[][] array2 = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.merge(array1, array2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of merge method, of class ArrayUtilities.
     */
    @Test
    public void testMerge_ObjectArr_ObjectArr() {
        System.out.println("merge");
        Object[] array1 = null;
        Object[] array2 = null;
        Object[] expResult = null;
        Object[] result = ArrayUtilities.merge(array1, array2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reverseArray method, of class ArrayUtilities.
     */
    @Test
    public void testReverseArray_StringArr() {
        System.out.println("reverseArray");
        String[] str = null;
        String[] expResult = null;
        String[] result = ArrayUtilities.reverseArray(str);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reverseArray method, of class ArrayUtilities.
     */
    @Test
    public void testReverseArray_StringArrArr() {
        System.out.println("reverseArray");
        String[][] str = null;
        String[][] expResult = null;
        String[][] result = ArrayUtilities.reverseArray(str);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ObjectToStringArray method, of class ArrayUtilities.
     */
    @Test
    public void testObjectToStringArray() {
        System.out.println("ObjectToStringArray");
        Object[][] array1 = null;
        String[][] expResult = null;
        String[][] result = ArrayUtilities.ObjectToStringArray(array1);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of SmallObjectToStringArray method, of class ArrayUtilities.
     */
    @Test
    public void testSmallObjectToStringArray() {
        System.out.println("SmallObjectToStringArray");
        Object[] array1 = null;
        String[] expResult = null;
        String[] result = ArrayUtilities.SmallObjectToStringArray(array1);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of ObjectToSingleColumnArray method, of class ArrayUtilities.
     */
    @Test
    public void testObjectToSingleColumnArray() {
        System.out.println("ObjectToSingleColumnArray");
        Object[][] array1 = null;
        Object[] expResult = null;
        Object[] result = ArrayUtilities.ObjectToSingleColumnArray(array1);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of sort method, of class ArrayUtilities.
     */
    @Test
    public void testSort() {
        System.out.println("sort");
        Object[] items = null;
        Object[] expResult = null;
        Object[] result = ArrayUtilities.sort(items);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of StringArrayToList method, of class ArrayUtilities.
     */
    @Test
    public void testStringArrayToList() {
        System.out.println("StringArrayToList");
        String[][] array = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.StringArrayToList(array);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of TableModelToList method, of class ArrayUtilities.
     */
    @Test
    public void testTableModelToList() {
        System.out.println("TableModelToList");
        JTable mode = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.TableModelToList(mode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of listToIntegerArray method, of class ArrayUtilities.
     */
    @Test
    public void testListToIntegerArray() {
        System.out.println("listToIntegerArray");
        ArrayList list = null;
        Integer[][][] expResult = null;
        Integer[][][] result = ArrayUtilities.listToIntegerArray(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of listToStringArray method, of class ArrayUtilities.
     */
    @Test
    public void testListToStringArray() {
        System.out.println("listToStringArray");
        ArrayList list = null;
        String[] expResult = null;
        String[] result = ArrayUtilities.listToStringArray(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of listToStringArrayArray method, of class ArrayUtilities.
     */
    @Test
    public void testListToStringArrayArray() {
        System.out.println("listToStringArrayArray");
        ArrayList<String[]> list = null;
        String[][] expResult = null;
        String[][] result = ArrayUtilities.listToStringArrayArray(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of listToTableArray method, of class ArrayUtilities.
     */
    @Test
    public void testListToTableArray() {
        System.out.println("listToTableArray");
        ArrayList list = null;
        Object[][] expResult = null;
        Object[][] result = ArrayUtilities.listToTableArray(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of StringListToTableArray method, of class ArrayUtilities.
     */
    @Test
    public void testStringListToTableArray() {
        System.out.println("StringListToTableArray");
        ArrayList list = null;
        String[][] expResult = null;
        String[][] result = ArrayUtilities.StringListToTableArray(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of merge method, of class ArrayUtilities.
     */
    @Test
    public void testMerge_ArrayList_ArrayList() {
        System.out.println("merge");
        ArrayList list1 = null;
        ArrayList list2 = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.merge(list1, list2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of substract method, of class ArrayUtilities.
     */
    @Test
    public void testSubstract() throws Exception {
        System.out.println("substract");
        ArrayList<Double> list1 = null;
        ArrayList<Double> list2 = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.substract(list1, list2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class ArrayUtilities.
     */
    @Test
    public void testAdd() throws Exception {
        System.out.println("add");
        ArrayList<Double> list1 = null;
        ArrayList<Double> list2 = null;
        ArrayList expResult = null;
        ArrayList result = ArrayUtilities.add(list1, list2);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}