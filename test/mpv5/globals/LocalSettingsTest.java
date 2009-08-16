
package mpv5.globals;

import java.io.File;
import mpv5.data.PropertyStore;
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
public class LocalSettingsTest {

    public LocalSettingsTest() {
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
     * Test of apply method, of class LocalSettings.
     */
    @Test
    public void testApply() {
        System.out.println("apply");
        LocalSettings.apply();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getIntegerProperty method, of class LocalSettings.
     */
    @Test
    public void testGetIntegerProperty() {
        System.out.println("getIntegerProperty");
        String name = "";
        int expResult = 0;
        int result = LocalSettings.getIntegerProperty(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBooleanProperty method, of class LocalSettings.
     */
    @Test
    public void testGetBooleanProperty() {
        System.out.println("getBooleanProperty");
        String name = "";
        boolean expResult = false;
        boolean result = LocalSettings.getBooleanProperty(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDoubleProperty method, of class LocalSettings.
     */
    @Test
    public void testGetDoubleProperty() {
        System.out.println("getDoubleProperty");
        String name = "";
        double expResult = 0.0;
        double result = LocalSettings.getDoubleProperty(name);
        assertEquals(expResult, result, 0.0);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class LocalSettings.
     */
    @Test
    public void testGetProperty() {
        System.out.println("getProperty");
        String name = "";
        String expResult = "";
        String result = LocalSettings.getProperty(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setConnectionID method, of class LocalSettings.
     */
    @Test
    public void testSetConnectionID() {
        System.out.println("setConnectionID");
        Integer id = null;
        LocalSettings.setConnectionID(id);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionID method, of class LocalSettings.
     */
    @Test
    public void testGetConnectionID() {
        System.out.println("getConnectionID");
        int expResult = 0;
        int result = LocalSettings.getConnectionID();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPropertyStore method, of class LocalSettings.
     */
    @Test
    public void testGetPropertyStore() {
        System.out.println("getPropertyStore");
        PropertyStore expResult = null;
        PropertyStore result = LocalSettings.getPropertyStore();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasProperty method, of class LocalSettings.
     */
    @Test
    public void testHasProperty() {
        System.out.println("hasProperty");
        String propertyname = "";
        boolean expResult = false;
        boolean result = LocalSettings.hasProperty(propertyname);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProperty method, of class LocalSettings.
     */
    @Test
    public void testSetProperty() {
        System.out.println("setProperty");
        String name = "";
        String value = "";
        LocalSettings.setProperty(name, value);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class LocalSettings.
     */
    @Test
    public void testSave_0args() {
        System.out.println("save");
        LocalSettings.save();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class LocalSettings.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        LocalSettings.read();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocalFile method, of class LocalSettings.
     */
    @Test
    public void testGetLocalFile() {
        System.out.println("getLocalFile");
        File expResult = null;
        File result = LocalSettings.getLocalFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasConnectionID method, of class LocalSettings.
     */
    @Test
    public void testHasConnectionID() throws Exception {
        System.out.println("hasConnectionID");
        Integer connectionID = null;
        boolean expResult = false;
        boolean result = LocalSettings.hasConnectionID(connectionID);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class LocalSettings.
     */
    @Test
    public void testSave_Integer() {
        System.out.println("save");
        Integer forConnId = null;
        LocalSettings.save(forConnId);
        
        fail("The test case is a prototype.");
    }

}