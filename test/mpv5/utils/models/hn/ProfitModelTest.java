
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
public class ProfitModelTest {

    public ProfitModelTest() {
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
     * Test of fetchResults method, of class ProfitModel.
     */
    @Test
    public void testFetchResults() {
        System.out.println("fetchResults");
        ProfitModel instance = null;
        instance.fetchResults();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHeader method, of class ProfitModel.
     */
    @Test
    public void testGetHeader() {
        System.out.println("getHeader");
        ProfitModel instance = null;
        String[] expResult = null;
        String[] result = instance.getHeader();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculatePeriod method, of class ProfitModel.
     */
    @Test
    public void testCalculatePeriod() {
        System.out.println("calculatePeriod");
        ProfitModel instance = null;
        instance.calculatePeriod();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormHtml method, of class ProfitModel.
     */
    @Test
    public void testGetFormHtml() {
        System.out.println("getFormHtml");
        ProfitModel instance = null;
        ByteArrayOutputStream expResult = null;
        ByteArrayOutputStream result = instance.getFormHtml();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPdf method, of class ProfitModel.
     */
    @Test
    public void testGetPdf() {
        System.out.println("getPdf");
        ProfitModel instance = null;
        instance.getPdf();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOutFileName method, of class ProfitModel.
     */
    @Test
    public void testGetOutFileName() {
        System.out.println("getOutFileName");
        ProfitModel instance = null;
        String expResult = "";
        String result = instance.getOutFileName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}