
package mpv5.ui.beans;

import java.awt.Font;
import java.util.List;
import javax.swing.JComboBox;
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
public class LabeledComboboxTest {

    public LabeledComboboxTest() {
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
     * Test of getComboBox method, of class LabeledCombobox.
     */
    @Test
    public void testGetComboBox() {
        System.out.println("getComboBox");
        LabeledCombobox instance = new LabeledCombobox();
        JComboBox expResult = null;
        JComboBox result = instance.getComboBox();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModel method, of class LabeledCombobox.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        LabeledCombobox instance = new LabeledCombobox();
        MPComboboxModel expResult = null;
        MPComboboxModel result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_MPEnumArr() {
        System.out.println("setModel");
        MPEnum[] values = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_MPEnumArr_int() {
        System.out.println("setModel");
        MPEnum[] values = null;
        int compareMode = 0;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(values, compareMode);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_EnumArr() {
        System.out.println("setModel");
        Enum[] values = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_ObjectArrArr() {
        System.out.println("setModel");
        Object[][] data = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSelectedItem method, of class LabeledCombobox.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        LabeledCombobox instance = new LabeledCombobox();
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getSelectedItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_MPComboboxModel() {
        System.out.println("setModel");
        MPComboboxModel model = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_DatabaseObject() {
        System.out.println("setModel");
        DatabaseObject obj = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(obj);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LabeledCombobox.
     */
    @Test
    public void testSetModel_List() {
        System.out.println("setModel");
        List<DatabaseObject> vector = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setModel(vector);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedIndex method, of class LabeledCombobox.
     */
    @Test
    public void testSetSelectedIndex() {
        System.out.println("setSelectedIndex");
        int itemindex = 0;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setSelectedIndex(itemindex);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class LabeledCombobox.
     */
    @Test
    public void testSetSelectedItem_String() {
        System.out.println("setSelectedItem");
        String valueOfItem = "";
        LabeledCombobox instance = new LabeledCombobox();
        instance.setSelectedItem(valueOfItem);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class LabeledCombobox.
     */
    @Test
    public void testSetSelectedItem_Object() {
        System.out.println("setSelectedItem");
        Object ID = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setSelectedItem(ID);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSearchOnEnterEnabled method, of class LabeledCombobox.
     */
    @Test
    public void testSetSearchOnEnterEnabled() {
        System.out.println("setSearchOnEnterEnabled");
        boolean enabled = false;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setSearchOnEnterEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class LabeledCombobox.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context c = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setContext(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class LabeledCombobox.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        LabeledCombobox instance = new LabeledCombobox();
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_Label method, of class LabeledCombobox.
     */
    @Test
    public void testGet_Label() {
        System.out.println("get_Label");
        LabeledCombobox instance = new LabeledCombobox();
        String expResult = "";
        String result = instance.get_Label();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_Label method, of class LabeledCombobox.
     */
    @Test
    public void testSet_Label() {
        System.out.println("set_Label");
        String label = "";
        LabeledCombobox instance = new LabeledCombobox();
        instance.set_Label(label);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_LabelFont method, of class LabeledCombobox.
     */
    @Test
    public void testSet_LabelFont() {
        System.out.println("set_LabelFont");
        Font font = null;
        LabeledCombobox instance = new LabeledCombobox();
        instance.set_LabelFont(font);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LabeledCombobox.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LabeledCombobox instance = new LabeledCombobox();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class LabeledCombobox.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String text = "";
        LabeledCombobox instance = new LabeledCombobox();
        instance.setValue(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of triggerSearch method, of class LabeledCombobox.
     */
    @Test
    public void testTriggerSearch() {
        System.out.println("triggerSearch");
        LabeledCombobox instance = new LabeledCombobox();
        instance.triggerSearch();
        
        fail("The test case is a prototype.");
    }

}