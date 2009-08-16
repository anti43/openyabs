
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
public class DatabaseObjectModifierTest {

    public DatabaseObjectModifierTest() {
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
     * Test of modify method, of class DatabaseObjectModifier.
     */
    @Test
    public void testModify() {
        System.out.println("modify");
        DatabaseObject object = null;
        DatabaseObjectModifier instance = new DatabaseObjectModifierImpl();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.modify(object);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class DatabaseObjectModifierImpl  implements DatabaseObjectModifier {

        public DatabaseObject modify(DatabaseObject object) {
            return null;
        }
    }

}