
package mpv5.ui.dialogs;

import java.io.File;
import javax.swing.JTextField;
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
public class DialogForFileTest {

    public DialogForFileTest() {
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
     * Test of chooseFile method, of class DialogForFile.
     */
    @Test
    public void testChooseFile() {
        System.out.println("chooseFile");
        DialogForFile instance = new DialogForFile();
        boolean expResult = false;
        boolean result = instance.chooseFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveFile method, of class DialogForFile.
     */
    @Test
    public void testSaveFile_0args() {
        System.out.println("saveFile");
        DialogForFile instance = new DialogForFile();
        boolean expResult = false;
        boolean result = instance.saveFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveFile method, of class DialogForFile.
     */
    @Test
    public void testSaveFile_File() {
        System.out.println("saveFile");
        File fileToSave = null;
        DialogForFile instance = new DialogForFile();
        instance.saveFile(fileToSave);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFile method, of class DialogForFile.
     */
    @Test
    public void testGetFile() {
        System.out.println("getFile");
        DialogForFile instance = new DialogForFile();
        File expResult = null;
        File result = instance.getFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilePath method, of class DialogForFile.
     */
    @Test
    public void testGetFilePath() {
        System.out.println("getFilePath");
        JTextField field = null;
        DialogForFile instance = new DialogForFile();
        boolean expResult = false;
        boolean result = instance.getFilePath(field);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of set method, of class DialogForFile.
     */
    @Test
    public void testSet() throws Exception {
        System.out.println("set");
        Object object = null;
        Exception e = null;
        DialogForFile instance = new DialogForFile();
        instance.set(object, e);
        
        fail("The test case is a prototype.");
    }

}