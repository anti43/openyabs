
package mpv5.db.common;

import mpv5.data.PropertyStore;
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
public class QueryDataTest {

    public QueryDataTest() {
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
     * Test of getSaveStringFor method, of class QueryData.
     */
    @Test
    public void testGetSaveStringFor() {
        System.out.println("getSaveStringFor");
        String s = "";
        SaveString expResult = null;
        SaveString result = QueryData.getSaveStringFor(s);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryData.
     */
    @Test
    public void testAdd_String_GenericType() {
        System.out.println("add");
        String key = "";
        Number value = null;
        QueryData instance = new QueryData();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryData.
     */
    @Test
    public void testAdd_String_boolean() {
        System.out.println("add");
        String key = "";
        boolean value = false;
        QueryData instance = new QueryData();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryData.
     */
    @Test
    public void testAdd_String_String() {
        System.out.println("add");
        String key = "";
        String value = "";
        QueryData instance = new QueryData();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasValues method, of class QueryData.
     */
    @Test
    public void testHasValues() {
        System.out.println("hasValues");
        QueryData instance = new QueryData();
        boolean expResult = false;
        boolean result = instance.hasValues();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of maskBackslashes method, of class QueryData.
     */
    @Test
    public void testMaskBackslashes() {
        System.out.println("maskBackslashes");
        String string = "";
        QueryData instance = new QueryData();
        String expResult = "";
        String result = instance.maskBackslashes(string);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeys method, of class QueryData.
     */
    @Test
    public void testGetKeys() {
        System.out.println("getKeys");
        QueryData instance = new QueryData();
        String[] expResult = null;
        String[] result = instance.getKeys();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class QueryData.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        String key = "";
        QueryData instance = new QueryData();
        SaveString expResult = null;
        SaveString result = instance.getValue(key);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesString method, of class QueryData.
     */
    @Test
    public void testGetValuesString() {
        System.out.println("getValuesString");
        QueryData instance = new QueryData();
        String expResult = "";
        String result = instance.getValuesString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class QueryData.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        QueryData instance = new QueryData();
        String[] expResult = null;
        String[] result = instance.getValues();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeysString method, of class QueryData.
     */
    @Test
    public void testGetKeysString() {
        System.out.println("getKeysString");
        QueryData instance = new QueryData();
        String expResult = "";
        String result = instance.getKeysString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class QueryData.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        PropertyStore properties = null;
        QueryData instance = new QueryData();
        instance.parse(properties);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryData.
     */
    @Test
    public void testAdd_String_Object() {
        System.out.println("add");
        String columnName = "";
        Object value = null;
        QueryData instance = new QueryData();
        instance.add(columnName, value);
        
        fail("The test case is a prototype.");
    }

}