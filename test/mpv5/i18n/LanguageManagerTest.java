
package mpv5.i18n;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.utils.models.MPComboboxModel;
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
public class LanguageManagerTest {

    public LanguageManagerTest() {
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
     * Test of flushLanguageCache method, of class LanguageManager.
     */
    @Test
    public void testFlushLanguageCache() {
        System.out.println("flushLanguageCache");
        LanguageManager.flushLanguageCache();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of importCountries method, of class LanguageManager.
     */
    @Test
    public void testImportCountries() {
        System.out.println("importCountries");
        File file = null;
        LanguageManager.importCountries(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of checkLanguage method, of class LanguageManager.
     */
    @Test
    public void testCheckLanguage() {
        System.out.println("checkLanguage");
        String languagename = "";
        boolean expResult = false;
        boolean result = LanguageManager.checkLanguage(languagename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundle method, of class LanguageManager.
     */
    @Test
    public void testGetBundle_String() {
        System.out.println("getBundle");
        String langid = "";
        ResourceBundle expResult = null;
        ResourceBundle result = LanguageManager.getBundle(langid);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundle method, of class LanguageManager.
     */
    @Test
    public void testGetBundle_0args() {
        System.out.println("getBundle");
        ResourceBundle expResult = null;
        ResourceBundle result = LanguageManager.getBundle();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEditorModel method, of class LanguageManager.
     */
    @Test
    public void testGetEditorModel() {
        System.out.println("getEditorModel");
        String langid = "";
        Object[][] expResult = null;
        Object[][] result = LanguageManager.getEditorModel(langid);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLanguagesAsComboBoxModel method, of class LanguageManager.
     */
    @Test
    public void testGetLanguagesAsComboBoxModel() {
        System.out.println("getLanguagesAsComboBoxModel");
        ComboBoxModel expResult = null;
        ComboBoxModel result = LanguageManager.getLanguagesAsComboBoxModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCountriesAsComboBoxModel method, of class LanguageManager.
     */
    @Test
    public void testGetCountriesAsComboBoxModel() {
        System.out.println("getCountriesAsComboBoxModel");
        MPComboboxModel expResult = null;
        MPComboboxModel result = LanguageManager.getCountriesAsComboBoxModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocalesAsComboBoxModel method, of class LanguageManager.
     */
    @Test
    public void testGetLocalesAsComboBoxModel() {
        System.out.println("getLocalesAsComboBoxModel");
        DefaultComboBoxModel expResult = null;
        DefaultComboBoxModel result = LanguageManager.getLocalesAsComboBoxModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of importLanguage method, of class LanguageManager.
     */
    @Test
    public void testImportLanguage() {
        System.out.println("importLanguage");
        String langname = "";
        File file = null;
        LanguageManager.importLanguage(langname, file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeLanguage method, of class LanguageManager.
     */
    @Test
    public void testRemoveLanguage() throws Exception {
        System.out.println("removeLanguage");
        String langid = "";
        LanguageManager.removeLanguage(langid);
        
        fail("The test case is a prototype.");
    }

}