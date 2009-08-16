
package mpv5.data;

import java.util.ArrayList;
import javax.swing.JComponent;
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
public class PropertyStoreTest {

    public PropertyStoreTest() {
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
     * Test of addAll method, of class PropertyStore.
     */
    @Test
    public void testAddAll() {
        System.out.println("addAll");
        Object[][] data = null;
        PropertyStore instance = new PropertyStore();
        instance.addAll(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addProperty method, of class PropertyStore.
     */
    @Test
    public void testAddProperty() {
        System.out.println("addProperty");
        String name = "";
        String value = "";
        PropertyStore instance = new PropertyStore();
        instance.addProperty(name, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getList method, of class PropertyStore.
     */
    @Test
    public void testGetList() {
        System.out.println("getList");
        PropertyStore instance = new PropertyStore();
        ArrayList expResult = null;
        ArrayList result = instance.getList();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_String() {
        System.out.println("getProperty");
        String name = "";
        PropertyStore instance = new PropertyStore();
        String expResult = "";
        String result = instance.getProperty(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_String_double() {
        System.out.println("getProperty");
        String key = "";
        double desiredClass = 0.0;
        PropertyStore instance = new PropertyStore();
        double expResult = 0.0;
        double result = instance.getProperty(key, desiredClass);
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_3args_1() {
        System.out.println("getProperty");
        JComponent comp = null;
        String source = "";
        double b = 0.0;
        PropertyStore instance = new PropertyStore();
        double expResult = 0.0;
        double result = instance.getProperty(comp, source, b);
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_String_int() {
        System.out.println("getProperty");
        String key = "";
        int desiredClass = 0;
        PropertyStore instance = new PropertyStore();
        int expResult = 0;
        int result = instance.getProperty(key, desiredClass);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_3args_2() {
        System.out.println("getProperty");
        JComponent comp = null;
        String source = "";
        int b = 0;
        PropertyStore instance = new PropertyStore();
        int expResult = 0;
        int result = instance.getProperty(comp, source, b);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_String_boolean() {
        System.out.println("getProperty");
        String key = "";
        boolean desiredClass = false;
        PropertyStore instance = new PropertyStore();
        boolean expResult = false;
        boolean result = instance.getProperty(key, desiredClass);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class PropertyStore.
     */
    @Test
    public void testGetProperty_3args_3() {
        System.out.println("getProperty");
        JComponent comp = null;
        String source = "";
        boolean b = false;
        PropertyStore instance = new PropertyStore();
        boolean expResult = false;
        boolean result = instance.getProperty(comp, source, b);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeProperty method, of class PropertyStore.
     */
    @Test
    public void testChangeProperty_String_String() {
        System.out.println("changeProperty");
        String name = "";
        String newvalue = "";
        PropertyStore instance = new PropertyStore();
        instance.changeProperty(name, newvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeProperty method, of class PropertyStore.
     */
    @Test
    public void testChangeProperty_3args() {
        System.out.println("changeProperty");
        JComponent comp = null;
        String source = "";
        Object newvalue = null;
        PropertyStore instance = new PropertyStore();
        instance.changeProperty(comp, source, newvalue);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasProperty method, of class PropertyStore.
     */
    @Test
    public void testHasProperty() {
        System.out.println("hasProperty");
        String propertyname = "";
        PropertyStore instance = new PropertyStore();
        boolean expResult = false;
        boolean result = instance.hasProperty(propertyname);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class PropertyStore.
     */
    @Test
    public void testPrint() {
        System.out.println("print");
        PropertyStore instance = new PropertyStore();
        String expResult = "";
        String result = instance.print();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setChanged method, of class PropertyStore.
     */
    @Test
    public void testSetChanged() {
        System.out.println("setChanged");
        boolean b = false;
        PropertyStore instance = new PropertyStore();
        instance.setChanged(b);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isChanged method, of class PropertyStore.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");
        PropertyStore instance = new PropertyStore();
        boolean expResult = false;
        boolean result = instance.isChanged();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}