
package mpv5.utils.tables;

import javax.swing.JTable;
import mpv5.ui.beans.LabeledTextField;
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
public class TableCalculatorTest {

    public TableCalculatorTest() {
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
     * Test of calculate method, of class TableCalculator.
     */
    @Test
    public void testCalculate() {
        System.out.println("calculate");
        int row = 0;
        TableCalculator instance = null;
        double expResult = 0.0;
        double result = instance.calculate(row);
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class TableCalculator.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        TableCalculator instance = null;
        instance.run();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class TableCalculator.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        TableCalculator instance = null;
        instance.start();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateOnce method, of class TableCalculator.
     */
    @Test
    public void testCalculateOnce() {
        System.out.println("calculateOnce");
        TableCalculator instance = null;
        instance.calculateOnce();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isUsed method, of class TableCalculator.
     */
    @Test
    public void testIsUsed() {
        System.out.println("isUsed");
        TableCalculator instance = null;
        boolean expResult = false;
        boolean result = instance.isUsed();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOnScreen method, of class TableCalculator.
     */
    @Test
    public void testSetOnScreen() {
        System.out.println("setOnScreen");
        boolean onScreen = false;
        TableCalculator instance = null;
        instance.setOnScreen(onScreen);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isTargetCell method, of class TableCalculator.
     */
    @Test
    public void testIsTargetCell() {
        System.out.println("isTargetCell");
        int row = 0;
        int column = 0;
        TableCalculator instance = null;
        boolean expResult = false;
        boolean result = instance.isTargetCell(row, column);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addLabel method, of class TableCalculator.
     */
    @Test
    public void testAddLabel() {
        System.out.println("addLabel");
        LabeledTextField value = null;
        int sumColumn = 0;
        TableCalculator instance = null;
        instance.addLabel(value, sumColumn);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTable method, of class TableCalculator.
     */
    @Test
    public void testGetTable() {
        System.out.println("getTable");
        TableCalculator instance = null;
        JTable expResult = null;
        JTable result = instance.getTable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}