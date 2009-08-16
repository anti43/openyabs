
package mpv5.ui.misc;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
public class CalcTest {

    public CalcTest() {
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
     * Test of main method, of class Calc.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Calc.main(args);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of DisplayError method, of class Calc.
     */
    @Test
    public void testDisplayError() {
        System.out.println("DisplayError");
        String err_msg = "";
        Calc instance = new Calc();
        instance.DisplayError(err_msg);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of NumericButton method, of class Calc.
     */
    @Test
    public void testNumericButton() {
        System.out.println("NumericButton");
        int i = 0;
        Calc instance = new Calc();
        instance.NumericButton(i);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of OperatorButton method, of class Calc.
     */
    @Test
    public void testOperatorButton() {
        System.out.println("OperatorButton");
        int i = 0;
        Calc instance = new Calc();
        instance.OperatorButton(i);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of DecimalButton method, of class Calc.
     */
    @Test
    public void testDecimalButton() {
        System.out.println("DecimalButton");
        Calc instance = new Calc();
        instance.DecimalButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PercentButton method, of class Calc.
     */
    @Test
    public void testPercentButton() {
        System.out.println("PercentButton");
        Calc instance = new Calc();
        instance.PercentButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of Clicked_Clear method, of class Calc.
     */
    @Test
    public void testClicked_Clear() {
        System.out.println("Clicked_Clear");
        Calc instance = new Calc();
        instance.Clicked_Clear();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of PlusMinusButton method, of class Calc.
     */
    @Test
    public void testPlusMinusButton() {
        System.out.println("PlusMinusButton");
        Calc instance = new Calc();
        instance.PlusMinusButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of SqrButton method, of class Calc.
     */
    @Test
    public void testSqrButton() {
        System.out.println("SqrButton");
        Calc instance = new Calc();
        instance.SqrButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of SqrRootButton method, of class Calc.
     */
    @Test
    public void testSqrRootButton() {
        System.out.println("SqrRootButton");
        Calc instance = new Calc();
        instance.SqrRootButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of OneOverXButton method, of class Calc.
     */
    @Test
    public void testOneOverXButton() {
        System.out.println("OneOverXButton");
        Calc instance = new Calc();
        instance.OneOverXButton();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of MemoryButton method, of class Calc.
     */
    @Test
    public void testMemoryButton() {
        System.out.println("MemoryButton");
        int i = 0;
        Calc instance = new Calc();
        instance.MemoryButton(i);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionPerformed method, of class Calc.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");
        ActionEvent event = null;
        Calc instance = new Calc();
        instance.actionPerformed(event);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyD method, of class Calc.
     */
    @Test
    public void testKeyD() {
        System.out.println("keyD");
        KeyEvent evt = null;
        char key = ' ';
        Calc instance = new Calc();
        instance.keyD(evt, key);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyTyped method, of class Calc.
     */
    @Test
    public void testKeyTyped() {
        System.out.println("keyTyped");
        KeyEvent evt = null;
        Calc instance = new Calc();
        instance.keyTyped(evt);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyPressed method, of class Calc.
     */
    @Test
    public void testKeyPressed() {
        System.out.println("keyPressed");
        KeyEvent e = null;
        Calc instance = new Calc();
        instance.keyPressed(e);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of keyReleased method, of class Calc.
     */
    @Test
    public void testKeyReleased() {
        System.out.println("keyReleased");
        KeyEvent e = null;
        Calc instance = new Calc();
        instance.keyReleased(e);
        
        fail("The test case is a prototype.");
    }

}