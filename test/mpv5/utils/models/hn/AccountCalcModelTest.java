
package mpv5.utils.models.hn;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
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
public class AccountCalcModelTest {

    public AccountCalcModelTest() {
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
     * Test of getHeader method, of class AccountCalcModel.
     */
    @Test
    public void testGetHeader() {
        System.out.println("getHeader");
        AccountCalcModel instance = null;
        String[] expResult = null;
        String[] result = instance.getHeader();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculatePeriod method, of class AccountCalcModel.
     */
    @Test
    public void testCalculatePeriod() {
        System.out.println("calculatePeriod");
        AccountCalcModel instance = null;
        instance.calculatePeriod();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModel method, of class AccountCalcModel.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        AccountCalcModel instance = null;
        DefaultTableModel expResult = null;
        DefaultTableModel result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculate method, of class AccountCalcModel.
     */
    @Test
    public void testCalculate() {
        System.out.println("calculate");
        String start = "";
        String end = "";
        String query = "";
        AccountCalcModel instance = null;
        instance.calculate(start, end, query);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillMap method, of class AccountCalcModel.
     */
    @Test
    public void testFillMap() {
        System.out.println("fillMap");
        AccountCalcModel instance = null;
        instance.fillMap();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createHtml method, of class AccountCalcModel.
     */
    @Test
    public void testCreateHtml() {
        System.out.println("createHtml");
        AccountCalcModel instance = null;
        ByteArrayOutputStream expResult = null;
        ByteArrayOutputStream result = instance.createHtml();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of simpleHtml method, of class AccountCalcModel.
     */
    @Test
    public void testSimpleHtml() {
        System.out.println("simpleHtml");
        String select = "";
        AccountCalcModel instance = null;
        instance.simpleHtml(select);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEnd method, of class AccountCalcModel.
     */
    @Test
    public void testGetEnd() {
        System.out.println("getEnd");
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.getEnd();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEnd method, of class AccountCalcModel.
     */
    @Test
    public void testSetEnd() {
        System.out.println("setEnd");
        String end = "";
        AccountCalcModel instance = null;
        instance.setEnd(end);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStart method, of class AccountCalcModel.
     */
    @Test
    public void testGetStart() {
        System.out.println("getStart");
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.getStart();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setStart method, of class AccountCalcModel.
     */
    @Test
    public void testSetStart() {
        System.out.println("setStart");
        String start = "";
        AccountCalcModel instance = null;
        instance.setStart(start);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getResultMap method, of class AccountCalcModel.
     */
    @Test
    public void testGetResultMap() {
        System.out.println("getResultMap");
        AccountCalcModel instance = null;
        Map expResult = null;
        Map result = instance.getResultMap();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setResultMap method, of class AccountCalcModel.
     */
    @Test
    public void testSetResultMap() {
        System.out.println("setResultMap");
        Map<String, String> resultMap = null;
        AccountCalcModel instance = null;
        instance.setResultMap(resultMap);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSkr method, of class AccountCalcModel.
     */
    @Test
    public void testIsSkr() {
        System.out.println("isSkr");
        AccountCalcModel instance = null;
        boolean expResult = false;
        boolean result = instance.isSkr();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSkr method, of class AccountCalcModel.
     */
    @Test
    public void testGetSkr() {
        System.out.println("getSkr");
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.getSkr();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHtmlform method, of class AccountCalcModel.
     */
    @Test
    public void testGetHtmlform() {
        System.out.println("getHtmlform");
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.getHtmlform();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPdfform method, of class AccountCalcModel.
     */
    @Test
    public void testGetPdfform() {
        System.out.println("getPdfform");
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.getPdfform();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createPrintName method, of class AccountCalcModel.
     */
    @Test
    public void testCreatePrintName() {
        System.out.println("createPrintName");
        DateComboBoxModel dates = null;
        AccountCalcModel instance = null;
        String expResult = "";
        String result = instance.createPrintName(dates);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createPdf method, of class AccountCalcModel.
     */
    @Test
    public void testCreatePdf() {
        System.out.println("createPdf");
        AccountCalcModel instance = null;
        instance.createPdf();
        
        fail("The test case is a prototype.");
    }

    public class AccountCalcModelImpl extends AccountCalcModel {

        public AccountCalcModelImpl() {
            super("");
        }

        public String[] getHeader() {
            return null;
        }

        public void calculatePeriod() {
        }
    }

}