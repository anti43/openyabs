
package mpv5.handler;

import java.util.HashMap;
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
public class FormFieldsHandlerTest {

    public FormFieldsHandlerTest() {
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
     * Test of getFormFields method, of class FormFieldsHandler.
     */
    @Test
    public void testGetFormFields() {
        System.out.println("getFormFields");
        FormFieldsHandler instance = null;
        HashMap expResult = null;
        HashMap result = instance.getFormFields();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFormattedFormFields method, of class FormFieldsHandler.
     */
    @Test
    public void testGetFormattedFormFields() {
        System.out.println("getFormattedFormFields");
        String identifier = "";
        FormFieldsHandler instance = null;
        HashMap expResult = null;
        HashMap result = instance.getFormattedFormFields(identifier);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}