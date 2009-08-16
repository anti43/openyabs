
package mpv5.utils.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
import org.jdom.Document;
import org.jdom.Element;
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
public class XMLReaderTest {

    public XMLReaderTest() {
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
     * Test of getSubRootElement method, of class XMLReader.
     */
    @Test
    public void testGetSubRootElement() {
        System.out.println("getSubRootElement");
        String name = "";
        XMLReader instance = new XMLReader();
        Element expResult = null;
        Element result = instance.getSubRootElement(name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLReader.
     */
    @Test
    public void testNewDoc_File() throws Exception {
        System.out.println("newDoc");
        File xmlfile = null;
        XMLReader instance = new XMLReader();
        Document expResult = null;
        Document result = instance.newDoc(xmlfile);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLReader.
     */
    @Test
    public void testNewDoc_File_boolean() throws Exception {
        System.out.println("newDoc");
        File xmlfile = null;
        boolean validate = false;
        XMLReader instance = new XMLReader();
        Document expResult = null;
        Document result = instance.newDoc(xmlfile, validate);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getElement method, of class XMLReader.
     */
    @Test
    public void testGetElement() {
        System.out.println("getElement");
        String type = "";
        String nodename = "";
        String attributevalue = "";
        String name = "";
        XMLReader instance = new XMLReader();
        String expResult = "";
        String result = instance.getElement(type, nodename, attributevalue, name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class XMLReader.
     */
    @Test
    public void testGetObjects_GenericType() throws Exception {
        System.out.println("getObjects");
        DatabaseObject template = null;
        XMLReader instance = new XMLReader();
        ArrayList expResult = null;
        ArrayList result = instance.getObjects(template);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjects method, of class XMLReader.
     */
    @Test
    public void testGetObjects_0args() {
        System.out.println("getObjects");
        XMLReader instance = new XMLReader();
        ArrayList expResult = null;
        ArrayList result = instance.getObjects();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class XMLReader.
     */
    @Test
    public void testPrint() {
        System.out.println("print");
        String nodename = "";
        XMLReader instance = new XMLReader();
        instance.print(nodename);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of readInto method, of class XMLReader.
     */
    @Test
    public void testReadInto_4args() {
        System.out.println("readInto");
        String type = "";
        String nodename = "";
        String nodeid = "";
        PropertyStore store = null;
        XMLReader instance = new XMLReader();
        PropertyStore expResult = null;
        PropertyStore result = instance.readInto(type, nodename, nodeid, store);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of readInto method, of class XMLReader.
     */
    @Test
    public void testReadInto_String_String() {
        System.out.println("readInto");
        String type = "";
        String nodename = "";
        XMLReader instance = new XMLReader();
        List expResult = null;
        List result = instance.readInto(type, nodename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toHashTable method, of class XMLReader.
     */
    @Test
    public void testToHashTable() {
        System.out.println("toHashTable");
        Element node = null;
        XMLReader instance = new XMLReader();
        Hashtable expResult = null;
        Hashtable result = instance.toHashTable(node);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of toArray method, of class XMLReader.
     */
    @Test
    public void testToArray() {
        System.out.println("toArray");
        Element node = null;
        XMLReader instance = new XMLReader();
        String[][] expResult = null;
        String[][] result = instance.toArray(node);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of isOverwriteExisting method, of class XMLReader.
     */
    @Test
    public void testIsOverwriteExisting() {
        System.out.println("isOverwriteExisting");
        XMLReader instance = new XMLReader();
        boolean expResult = false;
        boolean result = instance.isOverwriteExisting();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setOverwriteExisting method, of class XMLReader.
     */
    @Test
    public void testSetOverwriteExisting() {
        System.out.println("setOverwriteExisting");
        boolean overwriteExisting = false;
        XMLReader instance = new XMLReader();
        instance.setOverwriteExisting(overwriteExisting);
        
        fail("The test case is a prototype.");
    }

}