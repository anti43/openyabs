
package mpv5.utils.trees;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
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
public class TreeFormatTest {

    public TreeFormatTest() {
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
     * Test of expandJTreeNode method, of class TreeFormat.
     */
    @Test
    public void testExpandJTreeNode() {
        System.out.println("expandJTreeNode");
        JTree tree = null;
        TreeModel model = null;
        Object node = null;
        int row = 0;
        int depth = 0;
        int expResult = 0;
        int result = TreeFormat.expandJTreeNode(tree, model, node, row, depth);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of expandTree method, of class TreeFormat.
     */
    @Test
    public void testExpandTree() {
        System.out.println("expandTree");
        JTree tree = null;
        TreeFormat.expandTree(tree);
        
        fail("The test case is a prototype.");
    }

}