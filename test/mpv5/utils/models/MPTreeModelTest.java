
package mpv5.utils.models;

import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
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
public class MPTreeModelTest {

    public MPTreeModelTest() {
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
     * Test of getRenderer method, of class MPTreeModel.
     */
    @Test
    public void testGetRenderer() {
        System.out.println("getRenderer");
        DefaultTreeCellRenderer expResult = null;
        DefaultTreeCellRenderer result = MPTreeModel.getRenderer();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultTreeListener method, of class MPTreeModel.
     */
    @Test
    public void testGetDefaultTreeListener() {
        System.out.println("getDefaultTreeListener");
        JTree tree = null;
        MouseListener expResult = null;
        MouseListener result = MPTreeModel.getDefaultTreeListener(tree);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toTreeModel method, of class MPTreeModel.
     */
    @Test
    public void testToTreeModel() {
        System.out.println("toTreeModel");
        ArrayList data = null;
        DatabaseObject rootNode = null;
        DefaultTreeModel expResult = null;
        DefaultTreeModel result = MPTreeModel.toTreeModel(data, rootNode);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}