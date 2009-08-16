
package mpv5.db.objects;

import javax.swing.JComponent;
import mpv5.utils.images.MPIcon;
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
public class WebShopTest {

    public WebShopTest() {
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
     * Test of getView method, of class WebShop.
     */
    @Test
    public void testGetView() {
        System.out.println("getView");
        WebShop instance = new WebShop();
        JComponent expResult = null;
        JComponent result = instance.getView();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIcon method, of class WebShop.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        WebShop instance = new WebShop();
        MPIcon expResult = null;
        MPIcon result = instance.getIcon();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getDescription method, of class WebShop.
     */
    @Test
    public void test__getDescription() {
        System.out.println("__getDescription");
        WebShop instance = new WebShop();
        String expResult = "";
        String result = instance.__getDescription();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDescription method, of class WebShop.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        String description = "";
        WebShop instance = new WebShop();
        instance.setDescription(description);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getInterval method, of class WebShop.
     */
    @Test
    public void test__getInterval() {
        System.out.println("__getInterval");
        WebShop instance = new WebShop();
        int expResult = 0;
        int result = instance.__getInterval();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setInterval method, of class WebShop.
     */
    @Test
    public void testSetInterval() {
        System.out.println("setInterval");
        int interval = 0;
        WebShop instance = new WebShop();
        instance.setInterval(interval);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of __getUrl method, of class WebShop.
     */
    @Test
    public void test__getUrl() {
        System.out.println("__getUrl");
        WebShop instance = new WebShop();
        String expResult = "";
        String result = instance.__getUrl();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUrl method, of class WebShop.
     */
    @Test
    public void testSetUrl() {
        System.out.println("setUrl");
        String url = "";
        WebShop instance = new WebShop();
        instance.setUrl(url);
        
        fail("The test case is a prototype.");
    }

}