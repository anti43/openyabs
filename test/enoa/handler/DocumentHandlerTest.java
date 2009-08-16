
package enoa.handler;

import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import java.io.File;
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
public class DocumentHandlerTest {

    public DocumentHandlerTest() {
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
     * Test of loadDocument method, of class DocumentHandler.
     */
    @Test
    public void testLoadDocument() throws Exception {
        System.out.println("loadDocument");
        File file = null;
        boolean asTemplate = false;
        DocumentHandler instance = null;
        IDocument expResult = null;
        IDocument result = instance.loadDocument(file, asTemplate);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newTextDocument method, of class DocumentHandler.
     */
    @Test
    public void testNewTextDocument() throws Exception {
        System.out.println("newTextDocument");
        DocumentHandler instance = null;
        ITextDocument expResult = null;
        ITextDocument result = instance.newTextDocument();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveAs method, of class DocumentHandler.
     */
    @Test
    public void testSaveAs() throws Exception {
        System.out.println("saveAs");
        IDocument doc = null;
        File file = null;
        DocumentHandler.saveAs(doc, file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillFormFields method, of class DocumentHandler.
     */
    @Test
    public void testFillFormFields() throws Exception {
        System.out.println("fillFormFields");
        ITextDocument textDocument = null;
        HashMap<String, String> data = null;
        DocumentHandler.fillFormFields(textDocument, data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillPlaceholderFields method, of class DocumentHandler.
     */
    @Test
    public void testFillPlaceholderFields() throws Exception {
        System.out.println("fillPlaceholderFields");
        ITextDocument textDocument = null;
        HashMap<String, String> data = null;
        DocumentHandler.fillPlaceholderFields(textDocument, data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillTextVariableFields method, of class DocumentHandler.
     */
    @Test
    public void testFillTextVariableFields() throws Exception {
        System.out.println("fillTextVariableFields");
        ITextDocument textDocument = null;
        HashMap<String, String> data = null;
        DocumentHandler.fillTextVariableFields(textDocument, data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of export method, of class DocumentHandler.
     */
    @Test
    public void testExport() throws Exception {
        System.out.println("export");
        File source = null;
        File target = null;
        DocumentHandler instance = null;
        File expResult = null;
        File result = instance.export(source, target);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}