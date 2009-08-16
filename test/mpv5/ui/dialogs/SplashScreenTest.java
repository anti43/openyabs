
package mpv5.ui.dialogs;

import java.awt.Image;
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
public class SplashScreenTest {

    public SplashScreenTest() {
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
     * Test of init method, of class SplashScreen.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        int steps = 0;
        SplashScreen instance = null;
        instance.init(steps);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getImage method, of class SplashScreen.
     */
    @Test
    public void testGetImage() {
        System.out.println("getImage");
        SplashScreen instance = null;
        Image expResult = null;
        Image result = instance.getImage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setImage method, of class SplashScreen.
     */
    @Test
    public void testSetImage() {
        System.out.println("setImage");
        Image image = null;
        SplashScreen instance = null;
        instance.setImage(image);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGrayImage method, of class SplashScreen.
     */
    @Test
    public void testGetGrayImage() {
        System.out.println("getGrayImage");
        SplashScreen instance = null;
        Image expResult = null;
        Image result = instance.getGrayImage();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGrayImage method, of class SplashScreen.
     */
    @Test
    public void testSetGrayImage() {
        System.out.println("setGrayImage");
        Image grayImage = null;
        SplashScreen instance = null;
        instance.setGrayImage(grayImage);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInfo method, of class SplashScreen.
     */
    @Test
    public void testGetInfo() {
        System.out.println("getInfo");
        SplashScreen instance = null;
        String expResult = "";
        String result = instance.getInfo();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInfo method, of class SplashScreen.
     */
    @Test
    public void testSetInfo() {
        System.out.println("setInfo");
        String info = "";
        SplashScreen instance = null;
        instance.setInfo(info);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProgress method, of class SplashScreen.
     */
    @Test
    public void testGetProgress() {
        System.out.println("getProgress");
        SplashScreen instance = null;
        String expResult = "";
        String result = instance.getProgress();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProgress method, of class SplashScreen.
     */
    @Test
    public void testSetProgress() {
        System.out.println("setProgress");
        String progress = "";
        SplashScreen instance = null;
        instance.setProgress(progress);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextStep method, of class SplashScreen.
     */
    @Test
    public void testNextStep() {
        System.out.println("nextStep");
        String message = "";
        SplashScreen instance = null;
        instance.nextStep(message);
        
        fail("The test case is a prototype.");
    }

}