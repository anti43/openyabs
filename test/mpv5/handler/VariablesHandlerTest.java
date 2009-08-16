
package mpv5.handler;

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
public class VariablesHandlerTest {

    public VariablesHandlerTest() {
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
     * Test of getSpecialVarsOf method, of class VariablesHandler.
     */
    @Test
    public void testGetSpecialVarsOf() {
        System.out.println("getSpecialVarsOf");
        DatabaseObject target = null;
        String[] expResult = null;
        String[] result = VariablesHandler.getSpecialVarsOf(target);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of resolveVarsFor method, of class VariablesHandler.
     */
    @Test
    public void testResolveVarsFor() {
        System.out.println("resolveVarsFor");
        DatabaseObject target = null;
        String[][] expResult = null;
        String[][] result = VariablesHandler.resolveVarsFor(target);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class VariablesHandler.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String text = "";
        DatabaseObject source = null;
        String expResult = "";
        String result = VariablesHandler.parse(text, source);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}