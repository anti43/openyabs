
package mpv5.ui.beans;

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
public class LightMPComboBoxTest {

    public LightMPComboBoxTest() {
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
     * Test of setTable method, of class LightMPComboBox.
     */
    @Test
    public void testSetTable() {
        System.out.println("setTable");
        JTable table = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setTable(table);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getComboBox method, of class LightMPComboBox.
     */
    @Test
    public void testGetComboBox() {
        System.out.println("getComboBox");
        LightMPComboBox instance = new LightMPComboBox();
        JComboBox expResult = null;
        JComboBox result = instance.getComboBox();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of search method, of class LightMPComboBox.
     */
    @Test
    public void testSearch() {
        System.out.println("search");
        LightMPComboBox instance = new LightMPComboBox();
        instance.search();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMPModel method, of class LightMPComboBox.
     */
    @Test
    public void testGetMPModel() {
        System.out.println("getMPModel");
        LightMPComboBox instance = new LightMPComboBox();
        MPComboboxModel expResult = null;
        MPComboboxModel result = instance.getMPModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_MPEnumArr() {
        System.out.println("setModel");
        MPEnum[] values = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(values);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_MPEnumArr_int() {
        System.out.println("setModel");
        MPEnum[] values = null;
        int compareMode = 0;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(values, compareMode);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_ObjectArrArr() {
        System.out.println("setModel");
        Object[][] data = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_MPComboboxModel() {
        System.out.println("setModel");
        MPComboboxModel model = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(model);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_DatabaseObject() {
        System.out.println("setModel");
        DatabaseObject obj = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(obj);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_List() {
        System.out.println("setModel");
        List<DatabaseObject> vector = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel(vector);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class LightMPComboBox.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        String valueOfItem = "";
        LightMPComboBox instance = new LightMPComboBox();
        instance.setSelectedItem(valueOfItem);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedIndex method, of class LightMPComboBox.
     */
    @Test
    public void testSetSelectedIndex() {
        System.out.println("setSelectedIndex");
        int index = 0;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setSelectedIndex(index);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addSelectionChangeReceiver method, of class LightMPComboBox.
     */
    @Test
    public void testAddSelectionChangeReceiver() {
        System.out.println("addSelectionChangeReceiver");
        MPCBSelectionChangeReceiver rec = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.addSelectionChangeReceiver(rec);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSearchOnEnterEnabled method, of class LightMPComboBox.
     */
    @Test
    public void testSetSearchOnEnterEnabled() {
        System.out.println("setSearchOnEnterEnabled");
        boolean enabled = false;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setSearchOnEnterEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setContext method, of class LightMPComboBox.
     */
    @Test
    public void testSetContext() {
        System.out.println("setContext");
        Context c = null;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setContext(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class LightMPComboBox.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        LightMPComboBox instance = new LightMPComboBox();
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnabled method, of class LightMPComboBox.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        boolean enabled = false;
        LightMPComboBox instance = new LightMPComboBox();
        instance.setEnabled(enabled);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class LightMPComboBox.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String text = "";
        LightMPComboBox instance = new LightMPComboBox();
        instance.setValue(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setModel method, of class LightMPComboBox.
     */
    @Test
    public void testSetModel_0args() {
        System.out.println("setModel");
        LightMPComboBox instance = new LightMPComboBox();
        instance.setModel();
        
        fail("The test case is a prototype.");
    }

}