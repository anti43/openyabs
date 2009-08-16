
package enoa.handler;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.ITextService;
import java.awt.Font;
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
public class TextHandlerTest {

    public TextHandlerTest() {
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
     * Test of append method, of class TextHandler.
     */
    @Test
    public void testAppend() throws Exception {
        System.out.println("append");
        String text = "";
        TextHandler instance = null;
        instance.append(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of appendBefore method, of class TextHandler.
     */
    @Test
    public void testAppendBefore() throws Exception {
        System.out.println("appendBefore");
        String text = "";
        TextHandler instance = null;
        instance.appendBefore(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAlign method, of class TextHandler.
     */
    @Test
    public void testSetAlign() throws Exception {
        System.out.println("setAlign");
        int paragraph = 0;
        short align = 0;
        TextHandler instance = null;
        instance.setAlign(paragraph, align);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTextFormat method, of class TextHandler.
     */
    @Test
    public void testSetTextFormat() throws Exception {
        System.out.println("setTextFormat");
        int paragraph = 0;
        Font font = null;
        int color = 0;
        TextHandler instance = null;
        instance.setTextFormat(paragraph, font, color);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addParagraphs method, of class TextHandler.
     */
    @Test
    public void testAddParagraphs() throws Exception {
        System.out.println("addParagraphs");
        String[] text = null;
        TextHandler instance = null;
        instance.addParagraphs(text);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getParagraph method, of class TextHandler.
     */
    @Test
    public void testGetParagraph() throws Exception {
        System.out.println("getParagraph");
        int paragraph = 0;
        TextHandler instance = null;
        String expResult = "";
        String result = instance.getParagraph(paragraph);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDoc method, of class TextHandler.
     */
    @Test
    public void testGetDoc() {
        System.out.println("getDoc");
        TextHandler instance = null;
        ITextDocument expResult = null;
        ITextDocument result = instance.getDoc();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTextservice method, of class TextHandler.
     */
    @Test
    public void testGetTextservice() {
        System.out.println("getTextservice");
        TextHandler instance = null;
        ITextService expResult = null;
        ITextService result = instance.getTextservice();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTextfieldservice method, of class TextHandler.
     */
    @Test
    public void testGetTextfieldservice() {
        System.out.println("getTextfieldservice");
        TextHandler instance = null;
        ITextFieldService expResult = null;
        ITextFieldService result = instance.getTextfieldservice();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}