
package mpv5.utils.models;

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
public class MPComboboxModelTest {

    public MPComboboxModelTest() {
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
     * Test of getElements method, of class MPComboboxModel.
     */
    @Test
    public void testGetElements() {
        System.out.println("getElements");
        MPComboboxModel instance = null;
        MPComboBoxModelItem[] expResult = null;
        MPComboBoxModelItem[] result = instance.getElements();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSelectedItem method, of class MPComboboxModel.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        MPComboboxModel instance = null;
        MPComboBoxModelItem expResult = null;
        MPComboBoxModelItem result = instance.getSelectedItem();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSelectedItem method, of class MPComboboxModel.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        MPComboBoxModelItem item = null;
        MPComboboxModel instance = null;
        instance.setSelectedItem(item);
        
        fail("The test case is a prototype.");
    }

}