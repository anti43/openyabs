
package mpv5.utils.images;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
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
public class MPIconTest {

    public MPIconTest() {
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
     * Test of iconToImage method, of class MPIcon.
     */
    @Test
    public void testIconToImage() {
        System.out.println("iconToImage");
        Icon icon = null;
        Image expResult = null;
        Image result = MPIcon.iconToImage(icon);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class MPIcon.
     */
    @Test
    public void testGetIcon_int() {
        System.out.println("getIcon");
        int maxWidthHeigth = 0;
        MPIcon instance = null;
        Icon expResult = null;
        Icon result = instance.getIcon(maxWidthHeigth);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class MPIcon.
     */
    @Test
    public void testGetIcon_int_int() {
        System.out.println("getIcon");
        int maxWidth = 0;
        int maxHeight = 0;
        MPIcon instance = null;
        Icon expResult = null;
        Icon result = instance.getIcon(maxWidth, maxHeight);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getScaledInstance method, of class MPIcon.
     */
    @Test
    public void testGetScaledInstance() {
        System.out.println("getScaledInstance");
        BufferedImage img = null;
        int targetWidth = 0;
        int targetHeight = 0;
        Object hint = null;
        boolean higherQuality = false;
        BufferedImage expResult = null;
        BufferedImage result = MPIcon.getScaledInstance(img, targetWidth, targetHeight, hint, higherQuality);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toBufferedImage method, of class MPIcon.
     */
    @Test
    public void testToBufferedImage() {
        System.out.println("toBufferedImage");
        Image image = null;
        BufferedImage expResult = null;
        BufferedImage result = MPIcon.toBufferedImage(image);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasAlpha method, of class MPIcon.
     */
    @Test
    public void testHasAlpha() {
        System.out.println("hasAlpha");
        Image image = null;
        boolean expResult = false;
        boolean result = MPIcon.hasAlpha(image);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}