
package mpv5.utils.models;

import java.util.HashMap;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
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
public class MPTableModelRowTest {

    public MPTableModelRowTest() {
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
     * Test of toRows method, of class MPTableModelRow.
     */
    @Test
    public void testToRows_List() {
        System.out.println("toRows");
        List<DatabaseObject> list = null;
        MPTableModelRow[] expResult = null;
        MPTableModelRow[] result = MPTableModelRow.toRows(list);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toRows method, of class MPTableModelRow.
     */
    @Test
    public void testToRows_Context_ObjectArrArr() {
        System.out.println("toRows");
        Context context = null;
        Object[][] object = null;
        MPTableModelRow[] expResult = null;
        MPTableModelRow[] result = MPTableModelRow.toRows(context, object);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getContext method, of class MPTableModelRow.
     */
    @Test
    public void testGetContext() {
        System.out.println("getContext");
        MPTableModelRow instance = null;
        Context expResult = null;
        Context result = instance.getContext();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIds method, of class MPTableModelRow.
     */
    @Test
    public void testGetIds() {
        System.out.println("getIds");
        MPTableModelRow instance = null;
        int expResult = 0;
        int result = instance.getIds();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObj method, of class MPTableModelRow.
     */
    @Test
    public void testGetObj() {
        System.out.println("getObj");
        MPTableModelRow instance = null;
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getObj();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class MPTableModelRow.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        MPTableModelRow instance = null;
        HashMap expResult = null;
        HashMap result = instance.getValues();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValueAt method, of class MPTableModelRow.
     */
    @Test
    public void testGetValueAt() {
        System.out.println("getValueAt");
        int column = 0;
        MPTableModelRow instance = null;
        Object expResult = null;
        Object result = instance.getValueAt(column);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setValueAt method, of class MPTableModelRow.
     */
    @Test
    public void testSetValueAt() {
        System.out.println("setValueAt");
        Object value = null;
        int column = 0;
        MPTableModelRow instance = null;
        instance.setValueAt(value, column);
        
        fail("The test case is a prototype.");
    }

}