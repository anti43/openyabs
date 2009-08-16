
package mpv5.utils.models;

import java.util.ArrayList;
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
public class ImportTableModelTest {

    public ImportTableModelTest() {
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
     * Test of getModel method, of class ImportTableModel.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        ArrayList<ArrayList<DatabaseObject>> objs = null;
        boolean selectOnlyNonExisting = false;
        ImportTableModel expResult = null;
        ImportTableModel result = ImportTableModel.getModel(objs, selectOnlyNonExisting);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}