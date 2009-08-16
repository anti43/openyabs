
package mpv5.utils.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
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
public class XMLWriterTest {

    public XMLWriterTest() {
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
     * Test of add method, of class XMLWriter.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        ArrayList<DatabaseObject> dbobjarr = null;
        XMLWriter instance = new XMLWriter();
        instance.add(dbobjarr);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addNode method, of class XMLWriter.
     */
    @Test
    public void testAddNode_Element() {
        System.out.println("addNode");
        Element e = null;
        XMLWriter instance = new XMLWriter();
        Element expResult = null;
        Element result = instance.addNode(e);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addNode method, of class XMLWriter.
     */
    @Test
    public void testAddNode_Element_String() {
        System.out.println("addNode");
        Element parent = null;
        String name = "";
        XMLWriter instance = new XMLWriter();
        Element expResult = null;
        Element result = instance.addNode(parent, name);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addNode method, of class XMLWriter.
     */
    @Test
    public void testAddNode_3args() {
        System.out.println("addNode");
        Element parent = null;
        Element name = null;
        String attribute = "";
        XMLWriter instance = new XMLWriter();
        Element expResult = null;
        Element result = instance.addNode(parent, name, attribute);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of append method, of class XMLWriter.
     */
    @Test
    public void testAppend() {
        System.out.println("append");
        File file = null;
        String nodename = "";
        String nodeid = "";
        PropertyStore cookie = null;
        XMLWriter instance = new XMLWriter();
        instance.append(file, nodename, nodeid, cookie);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createOrReplace method, of class XMLWriter.
     */
    @Test
    public void testCreateOrReplace() throws Exception {
        System.out.println("createOrReplace");
        File file = null;
        XMLWriter instance = new XMLWriter();
        instance.createOrReplace(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLWriter.
     */
    @Test
    public void testNewDoc_boolean() {
        System.out.println("newDoc");
        boolean withDocTypeDeclaration = false;
        XMLWriter instance = new XMLWriter();
        instance.newDoc(withDocTypeDeclaration);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLWriter.
     */
    @Test
    public void testNewDoc_String() {
        System.out.println("newDoc");
        String defaultSubRootElementName = "";
        XMLWriter instance = new XMLWriter();
        instance.newDoc(defaultSubRootElementName);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLWriter.
     */
    @Test
    public void testNewDoc_String_boolean() {
        System.out.println("newDoc");
        String defaultSubRootElementName = "";
        boolean withDocTypeDeclaration = false;
        XMLWriter instance = new XMLWriter();
        instance.newDoc(defaultSubRootElementName, withDocTypeDeclaration);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of newDoc method, of class XMLWriter.
     */
    @Test
    public void testNewDoc_0args() {
        System.out.println("newDoc");
        XMLWriter instance = new XMLWriter();
        instance.newDoc();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createFile method, of class XMLWriter.
     */
    @Test
    public void testCreateFile() {
        System.out.println("createFile");
        String filename = "";
        XMLWriter instance = new XMLWriter();
        File expResult = null;
        File result = instance.createFile(filename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of addElement method, of class XMLWriter.
     */
    @Test
    public void testAddElement() {
        System.out.println("addElement");
        Element parent = null;
        Element nodename = null;
        String attributevalue = "";
        String name = "";
        String value = "";
        XMLWriter instance = new XMLWriter();
        boolean expResult = false;
        boolean result = instance.addElement(parent, nodename, attributevalue, name, value);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class XMLWriter.
     */
    @Test
    public void testParse_3args() {
        System.out.println("parse");
        String nodename = "";
        String nodeid = "";
        PropertyStore cookie = null;
        XMLWriter instance = new XMLWriter();
        instance.parse(nodename, nodeid, cookie);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of parse method, of class XMLWriter.
     */
    @Test
    public void testParse_String_List() {
        System.out.println("parse");
        String nodename = "";
        List<PropertyStore> cookie = null;
        XMLWriter instance = new XMLWriter();
        instance.parse(nodename, cookie);
        
        fail("The test case is a prototype.");
    }

}