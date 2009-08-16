
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
public class DatabaseSearchTest {

    public DatabaseSearchTest() {
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
     * Test of getValuesFor method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor_0args() {
        System.out.println("getValuesFor");
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor_3args_1() {
        System.out.println("getValuesFor");
        String resultingFieldNames = "";
        String what = "";
        String where = "";
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor(resultingFieldNames, what, where);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor_3args_2() {
        System.out.println("getValuesFor");
        String resultingFieldNames = "";
        String what = "";
        Number value = null;
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor(resultingFieldNames, what, value);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor_4args_1() {
        System.out.println("getValuesFor");
        String resultingFieldNames = "";
        String[] possibleColumns = null;
        String where = "";
        boolean searchForLike = false;
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor(resultingFieldNames, possibleColumns, where, searchForLike);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor2 method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor2() {
        System.out.println("getValuesFor2");
        String resultingFieldNames = "";
        String[] possibleColumns = null;
        String where = "";
        boolean searchForLike = false;
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor2(resultingFieldNames, possibleColumns, where, searchForLike);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValuesFor method, of class DatabaseSearch.
     */
    @Test
    public void testGetValuesFor_4args_2() {
        System.out.println("getValuesFor");
        String resultingFieldNames = "";
        String what = "";
        String where = "";
        boolean searchForLike = false;
        DatabaseSearch instance = null;
        Object[][] expResult = null;
        Object[][] result = instance.getValuesFor(resultingFieldNames, what, where, searchForLike);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of searchFor method, of class DatabaseSearch.
     */
    @Test
    public void testSearchFor_String_String() throws Exception {
        System.out.println("searchFor");
        String what = "";
        String needle = "";
        DatabaseSearch instance = null;
        Object[] expResult = null;
        Object[] result = instance.searchFor(what, needle);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of searchFor method, of class DatabaseSearch.
     */
    @Test
    public void testSearchFor_4args() throws Exception {
        System.out.println("searchFor");
        String[] columns = null;
        String what = "";
        String needle = "";
        boolean exactMatch = false;
        DatabaseSearch instance = null;
        Object[] expResult = null;
        Object[] result = instance.searchFor(columns, what, needle, exactMatch);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of searchFor method, of class DatabaseSearch.
     */
    @Test
    public void testSearchFor_3args() throws Exception {
        System.out.println("searchFor");
        String[] columns = null;
        String what = "";
        String needle = "";
        DatabaseSearch instance = null;
        Object[] expResult = null;
        Object[] result = instance.searchFor(columns, what, needle);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of searchForID method, of class DatabaseSearch.
     */
    @Test
    public void testSearchForID() {
        System.out.println("searchForID");
        String what = "";
        String needle = "";
        DatabaseSearch instance = null;
        Integer expResult = null;
        Integer result = instance.searchForID(what, needle);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}