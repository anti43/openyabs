
package mpv5.ui.beans;

import java.awt.Font;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.handler.MPEnum;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
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
public class MPComboboxTest {

    public MPComboboxTest() {
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
     * Test of setTable method, of class MPCombobox.
     */
    @Test
    public void testSetTable() {
        System.out.println("setTable");
        JTable table = null;
        MPCombobox instance = new MPCombobox();
        instance.setTable(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getComboBox method, of class MPCombobox.
     */
    @Test
    public void testGetComboBox() {
        System.out.println("getComboBox");
        MPCombobox instance = new MPCombobox();
        JComboBox expResult = null;
        JComboBox result = instance.getComboBox();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of search method, of class MPCombobox.
     */
    @Test
    public void testSearch_0args() {
        System.out.println("search");
        MPCombobox instance = new MPCombobox();
        instance.search();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of search method, of class MPCombobox.
     */
    @Test
    public void testSearch_boolean() {
        System.out.println("search");
        boolean hidePopup = false;
        MPCombobox instance = new MPCombobox();
        instance.search(hidePopup);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModel method, of class MPCombobox.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        MPCombobox instance = new MPCombobox();
        MPComboboxModel expResult = null;
        MPComboboxModel result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_MPEnumArr() {
        System.out.println("setModel");
        MPEnum[] values = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_MPEnumArr_int() {
        System.out.println("setModel");
        MPEnum[] values = null;
        int compareMode = 0;
        MPCombobox instance = new MPCombobox();
        instance.setModel(values, compareMode);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_EnumArr() {
        System.out.println("setModel");
        Enum[] values = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_ObjectArrArr() {
        System.out.println("setModel");
        Object[][] data = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSelectedItem method, of class MPCombobox.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        MPCombobox instance = new MPCombobox();
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getSelectedItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_MPComboboxModel() {
        System.out.println("setModel");
        MPComboboxModel model = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_DatabaseObject() {
        System.out.println("setModel");
        DatabaseObject obj = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(obj);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_List() {
        System.out.println("setModel");
        List<DatabaseObject> vector = null;
        MPCombobox instance = new MPCombobox();
        instance.setModel(vector);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedIndex method, of class MPCombobox.
     */
    @Test
    public void testSetSelectedIndex() {
        System.out.println("setSelectedIndex");
        int itemID = 0;
        MPCombobox instance = new MPCombobox();
        instance.setSelectedIndex(itemID);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class MPCombobox.
     */
    @Test
    public void testSetSelectedItem_String() {
        System.out.println("setSelectedItem");
        String valueOfItem = "";
        MPCombobox instance = new MPCombobox();
        instance.setSelectedItem(valueOfItem);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class MPCombobox.
     */
    @Test
    public void testSetSelectedItem_Object() {
        System.out.println("setSelectedItem");
        Object ID = null;
        MPCombobox instance = new MPCombobox();
        instance.setSelectedItem(ID);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSearchOnEnterEnabled method, of class MPCombobox.
     */
    @Test
    public void testSetSearchOnEnterEnabled() {
        System.out.println("setSearchOnEnterEnabled");
        boolean enabled = false;
        MPCombobox instance = new MPCombobox();
        instance.setSearchOnEnterEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class MPCombobox.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context c = null;
        MPCombobox instance = new MPCombobox();
        instance.setContext(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class MPCombobox.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        MPCombobox instance = new MPCombobox();
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_LabelFont method, of class MPCombobox.
     */
    @Test
    public void testSet_LabelFont() {
        System.out.println("set_LabelFont");
        Font font = null;
        MPCombobox instance = new MPCombobox();
        instance.set_LabelFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class MPCombobox.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        MPCombobox instance = new MPCombobox();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class MPCombobox.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String text = "";
        MPCombobox instance = new MPCombobox();
        instance.setValue(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class MPCombobox.
     */
    @Test
    public void testSetModel_0args() {
        System.out.println("setModel");
        MPCombobox instance = new MPCombobox();
        instance.setModel();
        
        fail("The test case is a prototype.");
    }

}