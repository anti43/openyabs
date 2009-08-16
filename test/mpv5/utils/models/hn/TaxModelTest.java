
package mpv5.utils.models.hn;

import java.io.ByteArrayOutputStream;
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
public class TaxModelTest {

    public TaxModelTest() {
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
     * Test of calculatePeriod method, of class TaxModel.
     */
    @Test
    public void testCalculatePeriod() {
        System.out.println("calculatePeriod");
        TaxModel instance = null;
        instance.calculatePeriod();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormHtml method, of class TaxModel.
     */
    @Test
    public void testGetFormHtml() {
        System.out.println("getFormHtml");
        TaxModel instance = null;
        ByteArrayOutputStream expResult = null;
        ByteArrayOutputStream result = instance.getFormHtml();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPdf method, of class TaxModel.
     */
    @Test
    public void testGetPdf() {
        System.out.println("getPdf");
        TaxModel instance = null;
        instance.getPdf();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHeader method, of class TaxModel.
     */
    @Test
    public void testGetHeader() {
        System.out.println("getHeader");
        TaxModel instance = null;
        String[] expResult = null;
        String[] result = instance.getHeader();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSkr method, of class TaxModel.
     */
    @Test
    public void testIsSkr() {
        System.out.println("isSkr");
        TaxModel instance = null;
        boolean expResult = false;
        boolean result = instance.isSkr();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}