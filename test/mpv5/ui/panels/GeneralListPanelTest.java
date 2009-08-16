
package mpv5.ui.panels;

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
public class GeneralListPanelTest {

    public GeneralListPanelTest() {
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
     * Test of setData method, of class GeneralListPanel.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        ArrayList<DatabaseObject> list = null;
        GeneralListPanel instance = null;
        instance.setData(list);
        
        fail("The test case is a prototype.");
    }

}