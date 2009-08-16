
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
public class SDBObjectGeneratorTest {

    public SDBObjectGeneratorTest() {
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
     * Test of getObjectFrom method, of class SDBObjectGenerator.
     */
    @Test
    public void testGetObjectFrom() {
        System.out.println("getObjectFrom");
        DatabaseObject dos = null;
        SimpleDatabaseObject expResult = null;
        SimpleDatabaseObject result = SDBObjectGenerator.getObjectFrom(dos);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}