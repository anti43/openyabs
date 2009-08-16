
package mpv5.ui.misc;

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
public class PositionTest {

    public PositionTest() {
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
     * Test of bottomLeft method, of class Position.
     */
    @Test
    public void testBottomLeft() {
        System.out.println("bottomLeft");
        Position instance = null;
        instance.bottomLeft();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of center method, of class Position.
     */
    @Test
    public void testCenter() {
        System.out.println("center");
        Position instance = null;
        instance.center();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isNotMaximized method, of class Position.
     */
    @Test
    public void testIsNotMaximized() {
        System.out.println("isNotMaximized");
        Position instance = null;
        boolean expResult = false;
        boolean result = instance.isNotMaximized();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of topLeft method, of class Position.
     */
    @Test
    public void testTopLeft() {
        System.out.println("topLeft");
        Position instance = null;
        instance.topLeft();
        
        fail("The test case is a prototype.");
    }

}