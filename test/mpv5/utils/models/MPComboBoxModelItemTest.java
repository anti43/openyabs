
package mpv5.utils.models;

import java.util.List;
import javax.swing.ComboBoxModel;
import mpv5.db.common.DatabaseObject;
import mpv5.handler.MPEnum;
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
public class MPComboBoxModelItemTest {

    public MPComboBoxModelItemTest() {
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
     * Test of getItemID method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetItemID_Integer_ComboBoxModel() {
        System.out.println("getItemID");
        Integer uid = null;
        ComboBoxModel model = null;
        int expResult = 0;
        int result = MPComboBoxModelItem.getItemID(uid, model);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemID method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetItemID_Object_ComboBoxModel() {
        System.out.println("getItemID");
        Object uid = null;
        ComboBoxModel model = null;
        int expResult = 0;
        int result = MPComboBoxModelItem.getItemID(uid, model);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemID method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetItemID_String_ComboBoxModel() {
        System.out.println("getItemID");
        String uid = "";
        ComboBoxModel model = null;
        int expResult = 0;
        int result = MPComboBoxModelItem.getItemID(uid, model);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getItemIDfromValue method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetItemIDfromValue() {
        System.out.println("getItemIDfromValue");
        String value = "";
        ComboBoxModel model = null;
        int expResult = 0;
        int result = MPComboBoxModelItem.getItemIDfromValue(value, model);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_ObjectArrArr() {
        System.out.println("toItems");
        Object[][] items = null;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_ObjectArrArr_int() {
        System.out.println("toItems");
        Object[][] items = null;
        int compareMode = 0;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, compareMode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_3args_1() {
        System.out.println("toItems");
        Object[][] items = null;
        int compareMode = 0;
        boolean convertIndexToInteger = false;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, compareMode, convertIndexToInteger);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_3args_2() {
        System.out.println("toItems");
        Object[][] items = null;
        int compareMode = 0;
        String formatString = "";
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, compareMode, formatString);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_ObjectArrArr_boolean() {
        System.out.println("toItems");
        Object[][] items = null;
        boolean sortValuesNaturally = false;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, sortValuesNaturally);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_3args_3() {
        System.out.println("toItems");
        Object[][] items = null;
        boolean sortValuesNaturally = false;
        boolean convertIndexToInteger = false;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, sortValuesNaturally, convertIndexToInteger);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_4args() {
        System.out.println("toItems");
        Object[][] items = null;
        boolean sortValuesNaturally = false;
        int compareMode = 0;
        String format = "";
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, sortValuesNaturally, compareMode, format);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_List() {
        System.out.println("toItems");
        List<DatabaseObject> items = null;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_List_int() {
        System.out.println("toItems");
        List<DatabaseObject> items = null;
        int compareMode = 0;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, compareMode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_MPEnumArr() {
        System.out.println("toItems");
        MPEnum[] items = null;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toItems method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToItems_MPEnumArr_int() {
        System.out.println("toItems");
        MPEnum[] items = null;
        int compareMode = 0;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = MPComboBoxModelItem.toItems(items, compareMode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_MPEnumArr() {
        System.out.println("toModel");
        MPEnum[] data = null;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_DatabaseObject() {
        System.out.println("toModel");
        DatabaseObject data = null;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_MPComboBoxModelItemArr() {
        System.out.println("toModel");
        MPComboBoxModelItem[] data = null;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_List() {
        System.out.println("toModel");
        List<MPComboBoxModelItem> list = null;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_MPEnumArr_int() {
        System.out.println("toModel");
        MPEnum[] data = null;
        int compareMode = 0;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(data, compareMode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toModel method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToModel_ObjectArrArr() {
        System.out.println("toModel");
        Object[][] data = null;
        MPComboboxModel expResult = null;
        MPComboboxModel result = MPComboBoxModelItem.toModel(data);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIDClass method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetIDClass() {
        System.out.println("getIDClass");
        MPComboBoxModelItem instance = null;
        Class expResult = null;
        Class result = instance.getIDClass();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInstanceOfIDClass method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetInstanceOfIDClass() {
        System.out.println("getInstanceOfIDClass");
        MPComboBoxModelItem instance = null;
        Object expResult = null;
        Object result = instance.getInstanceOfIDClass();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        MPComboBoxModelItem instance = null;
        String expResult = "";
        String result = instance.getId();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIdObject method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetIdObject() {
        System.out.println("getIdObject");
        MPComboBoxModelItem instance = null;
        Object expResult = null;
        Object result = instance.getIdObject();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValid method, of class MPComboBoxModelItem.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        MPComboBoxModelItem instance = null;
        boolean expResult = false;
        boolean result = instance.isValid();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCompareMode method, of class MPComboBoxModelItem.
     */
    @Test
    public void testSetCompareMode() {
        System.out.println("setCompareMode");
        int mode = 0;
        MPComboBoxModelItem instance = null;
        instance.setCompareMode(mode);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class MPComboBoxModelItem.
     */
    @Test
    public void testSetId_Integer() {
        System.out.println("setId");
        Integer id = null;
        MPComboBoxModelItem instance = null;
        instance.setId(id);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setId method, of class MPComboBoxModelItem.
     */
    @Test
    public void testSetId_Object() {
        System.out.println("setId");
        Object id = null;
        MPComboBoxModelItem instance = null;
        instance.setId(id);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class MPComboBoxModelItem.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        MPComboBoxModelItem instance = null;
        String expResult = "";
        String result = instance.getValue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setName method, of class MPComboBoxModelItem.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        String name = "";
        MPComboBoxModelItem instance = null;
        instance.setName(name);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValue method, of class MPComboBoxModelItem.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        String value = "";
        MPComboBoxModelItem instance = null;
        instance.setValue(value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class MPComboBoxModelItem.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        MPComboBoxModelItem instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of compareTo method, of class MPComboBoxModelItem.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        MPComboBoxModelItem to = null;
        MPComboBoxModelItem instance = null;
        int expResult = 0;
        int result = instance.compareTo(to);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class MPComboBoxModelItem.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object anotherObject = null;
        MPComboBoxModelItem instance = null;
        boolean expResult = false;
        boolean result = instance.equals(anotherObject);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class MPComboBoxModelItem.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        MPComboBoxModelItem instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}