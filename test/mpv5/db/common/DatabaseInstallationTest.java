
package mpv5.db.common;

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
public class DatabaseInstallationTest {

    public DatabaseInstallationTest() {
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
     * Test of getStructure method, of class DatabaseInstallation.
     */
    @Test
    public void testGetStructure() {
        System.out.println("getStructure");
        DatabaseInstallation instance = new DatabaseInstallation();
        String[] expResult = null;
        String[] result = instance.getStructure();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCUSTOM method, of class DatabaseInstallation.
     */
    @Test
    public void testSetCUSTOM() {
        System.out.println("setCUSTOM");
        String[] CUSTOM_STRUCTURE = null;
        DatabaseInstallation instance = new DatabaseInstallation();
        instance.setCUSTOM(CUSTOM_STRUCTURE);
        
        fail("The test case is a prototype.");
    }

}