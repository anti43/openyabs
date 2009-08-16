
package mpv5.ui.misc;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
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
public class FadeOnChangeLabelTest {

    public FadeOnChangeLabelTest() {
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
     * Test of setParams method, of class FadeOnChangeLabel.
     */
    @Test
    public void testSetParams() {
        System.out.println("setParams");
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        float fStep = 0.0F;
        float iFade = 0.0F;
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.setParams(r, g, b, fStep, iFade);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRepaintContainer method, of class FadeOnChangeLabel.
     */
    @Test
    public void testSetRepaintContainer() {
        System.out.println("setRepaintContainer");
        Container c = null;
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.setRepaintContainer(c);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setText method, of class FadeOnChangeLabel.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        String text = "";
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.setText(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of paintComponent method, of class FadeOnChangeLabel.
     */
    @Test
    public void testPaintComponent() {
        System.out.println("paintComponent");
        Graphics g = null;
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.paintComponent(g);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFadeColor method, of class FadeOnChangeLabel.
     */
    @Test
    public void testSetFadeColor() {
        System.out.println("setFadeColor");
        Color color = null;
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.setFadeColor(color);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of reset method, of class FadeOnChangeLabel.
     */
    @Test
    public void testReset() {
        System.out.println("reset");
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.reset();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of run method, of class FadeOnChangeLabel.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        FadeOnChangeLabel instance = new FadeOnChangeLabel();
        instance.run();
        
        fail("The test case is a prototype.");
    }

}