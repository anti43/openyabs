
package mpv5.utils.files;

import java.io.File;
import java.util.ArrayList;
import javax.print.DocFlavor;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.DatabaseObject;
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
public class TextDatFileTest {

    public TextDatFileTest() {
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
     * Test of parse method, of class TextDatFile.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        ArrayList<DatabaseObject> dbobjarr = null;
        TextDatFile instance = new TextDatFile();
        instance.parse(dbobjarr);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of waitFor method, of class TextDatFile.
     */
    @Test
    public void testWaitFor() {
        System.out.println("waitFor");
        TextDatFile instance = new TextDatFile();
        Exception expResult = null;
        Exception result = instance.waitFor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of print method, of class TextDatFile.
     */
    @Test
    public void testPrint() {
        System.out.println("print");
        TextDatFile instance = new TextDatFile();
        instance.print();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class TextDatFile.
     */
    @Test
    public void testRead() {
        System.out.println("read");
        TextDatFile instance = new TextDatFile();
        String[][] expResult = null;
        String[][] result = instance.read();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of readToTable method, of class TextDatFile.
     */
    @Test
    public void testReadToTable() {
        System.out.println("readToTable");
        TextDatFile instance = new TextDatFile();
        DefaultTableModel expResult = null;
        DefaultTableModel result = instance.readToTable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFlavor method, of class TextDatFile.
     */
    @Test
    public void testGetFlavor() {
        System.out.println("getFlavor");
        TextDatFile instance = new TextDatFile();
        DocFlavor expResult = null;
        DocFlavor result = instance.getFlavor();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class TextDatFile.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        TextDatFile instance = new TextDatFile();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFieldSeparator method, of class TextDatFile.
     */
    @Test
    public void testGetFieldSeparator() {
        System.out.println("getFieldSeparator");
        TextDatFile instance = new TextDatFile();
        String expResult = "";
        String result = instance.getFieldSeparator();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFieldSeparator method, of class TextDatFile.
     */
    @Test
    public void testSetFieldSeparator() {
        System.out.println("setFieldSeparator");
        String fieldSeparator = "";
        TextDatFile instance = new TextDatFile();
        instance.setFieldSeparator(fieldSeparator);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getData method, of class TextDatFile.
     */
    @Test
    public void testGetData() {
        System.out.println("getData");
        TextDatFile instance = new TextDatFile();
        String[][] expResult = null;
        String[][] result = instance.getData();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setData method, of class TextDatFile.
     */
    @Test
    public void testSetData() {
        System.out.println("setData");
        String[][] data = null;
        TextDatFile instance = new TextDatFile();
        instance.setData(data);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHeader method, of class TextDatFile.
     */
    @Test
    public void testGetHeader() {
        System.out.println("getHeader");
        TextDatFile instance = new TextDatFile();
        String[] expResult = null;
        String[] result = instance.getHeader();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHeader method, of class TextDatFile.
     */
    @Test
    public void testSetHeader() {
        System.out.println("setHeader");
        String[] header = null;
        TextDatFile instance = new TextDatFile();
        instance.setHeader(header);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModel method, of class TextDatFile.
     */
    @Test
    public void testGetModel() {
        System.out.println("getModel");
        TextDatFile instance = new TextDatFile();
        DefaultTableModel expResult = null;
        DefaultTableModel result = instance.getModel();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of createFile method, of class TextDatFile.
     */
    @Test
    public void testCreateFile() {
        System.out.println("createFile");
        String filename = "";
        TextDatFile instance = new TextDatFile();
        File expResult = null;
        File result = instance.createFile(filename);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}