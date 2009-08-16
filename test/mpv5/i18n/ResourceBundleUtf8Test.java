
package mpv5.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
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
public class ResourceBundleUtf8Test {

    public ResourceBundleUtf8Test() {
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
     * Test of getBundle method, of class ResourceBundleUtf8.
     */
    @Test
    public void testGetBundle_String() {
        System.out.println("getBundle");
        String pBaseName = "";
        ResourceBundle expResult = null;
        ResourceBundle result = ResourceBundleUtf8.getBundle(pBaseName);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundle method, of class ResourceBundleUtf8.
     */
    @Test
    public void testGetBundle_String_Locale() {
        System.out.println("getBundle");
        String pBaseName = "";
        Locale pLocale = null;
        ResourceBundle expResult = null;
        ResourceBundle result = ResourceBundleUtf8.getBundle(pBaseName, pLocale);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundle method, of class ResourceBundleUtf8.
     */
    @Test
    public void testGetBundle_3args() {
        System.out.println("getBundle");
        String pBaseName = "";
        Locale pLocale = null;
        ClassLoader pLoader = null;
        ResourceBundle expResult = null;
        ResourceBundle result = ResourceBundleUtf8.getBundle(pBaseName, pLocale, pLoader);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}