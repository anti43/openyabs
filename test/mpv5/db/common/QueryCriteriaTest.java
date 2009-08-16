
package mpv5.db.common;

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
public class QueryCriteriaTest {

    public QueryCriteriaTest() {
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
     * Test of getSaveStringFor method, of class QueryCriteria.
     */
    @Test
    public void testGetSaveStringFor() {
        System.out.println("getSaveStringFor");
        String s = "";
        SaveString expResult = null;
        SaveString result = QueryCriteria.getSaveStringFor(s);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryCriteria.
     */
    @Test
    public void testAdd_String_GenericType() {
        System.out.println("add");
        String key = "";
        Number value = null;
        QueryCriteria instance = new QueryCriteria();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryCriteria.
     */
    @Test
    public void testAdd_String_boolean() {
        System.out.println("add");
        String key = "";
        boolean value = false;
        QueryCriteria instance = new QueryCriteria();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of add method, of class QueryCriteria.
     */
    @Test
    public void testAdd_String_String() {
        System.out.println("add");
        String key = "";
        String value = "";
        QueryCriteria instance = new QueryCriteria();
        instance.add(key, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeys method, of class QueryCriteria.
     */
    @Test
    public void testGetKeys() {
        System.out.println("getKeys");
        QueryCriteria instance = new QueryCriteria();
        String[] expResult = null;
        String[] result = instance.getKeys();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValue method, of class QueryCriteria.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        String key = "";
        QueryCriteria instance = new QueryCriteria();
        SaveString expResult = null;
        SaveString result = instance.getValue(key);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOrder method, of class QueryCriteria.
     */
    @Test
    public void testSetOrder() {
        System.out.println("setOrder");
        String column = "";
        boolean asc = false;
        QueryCriteria instance = new QueryCriteria();
        instance.setOrder(column, asc);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValues method, of class QueryCriteria.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        QueryCriteria instance = new QueryCriteria();
        String[] expResult = null;
        String[] result = instance.getValues();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOrder method, of class QueryCriteria.
     */
    @Test
    public void testGetOrder() {
        System.out.println("getOrder");
        QueryCriteria instance = new QueryCriteria();
        String expResult = "";
        String result = instance.getOrder();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}