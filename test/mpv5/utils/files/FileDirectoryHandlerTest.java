
package mpv5.utils.files;

import java.io.File;
import java.net.URI;
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
public class FileDirectoryHandlerTest {

    public FileDirectoryHandlerTest() {
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
     * Test of deleteFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testDeleteFile() {
        System.out.println("deleteFile");
        File localFile = null;
        boolean now = false;
        FileDirectoryHandler.deleteFile(localFile, now);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteTree method, of class FileDirectoryHandler.
     */
    @Test
    public void testDeleteTree() throws Exception {
        System.out.println("deleteTree");
        File path = null;
        FileDirectoryHandler.deleteTree(path);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyDirectory method, of class FileDirectoryHandler.
     */
    @Test
    public void testCopyDirectory() throws Exception {
        System.out.println("copyDirectory");
        File sourceLocation = null;
        File targetLocation = null;
        URI expResult = null;
        URI result = FileDirectoryHandler.copyDirectory(sourceLocation, targetLocation);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyFile2 method, of class FileDirectoryHandler.
     */
    @Test
    public void testCopyFile2() throws Exception {
        System.out.println("copyFile2");
        File source = null;
        File target = null;
        File expResult = null;
        File result = FileDirectoryHandler.copyFile2(source, target);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testCopyFile_4args() throws Exception {
        System.out.println("copyFile");
        File sourceFile = null;
        File targetDirectory = null;
        String targetFilename = "";
        boolean deleteOnExit = false;
        URI expResult = null;
        URI result = FileDirectoryHandler.copyFile(sourceFile, targetDirectory, targetFilename, deleteOnExit);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteTreeOnExit method, of class FileDirectoryHandler.
     */
    @Test
    public void testDeleteTreeOnExit() throws Exception {
        System.out.println("deleteTreeOnExit");
        File path = null;
        FileDirectoryHandler.deleteTreeOnExit(path);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilesOfDirectory method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetFilesOfDirectory() {
        System.out.println("getFilesOfDirectory");
        String directory = "";
        String identifier = "";
        File[] expResult = null;
        File[] result = FileDirectoryHandler.getFilesOfDirectory(directory, identifier);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of open method, of class FileDirectoryHandler.
     */
    @Test
    public void testOpen() {
        System.out.println("open");
        File file = null;
        FileDirectoryHandler.open(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of edit method, of class FileDirectoryHandler.
     */
    @Test
    public void testEdit() {
        System.out.println("edit");
        File file = null;
        FileDirectoryHandler.edit(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class FileDirectoryHandler.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        File file = null;
        FileDirectoryHandler.save(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of tempFileClone method, of class FileDirectoryHandler.
     */
    @Test
    public void testTempFileClone_File() {
        System.out.println("tempFileClone");
        File file = null;
        File expResult = null;
        File result = FileDirectoryHandler.tempFileClone(file);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of tempFileClone method, of class FileDirectoryHandler.
     */
    @Test
    public void testTempFileClone_File_String() {
        System.out.println("tempFileClone");
        File file = null;
        String suffix = "";
        File expResult = null;
        File result = FileDirectoryHandler.tempFileClone(file, suffix);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTempFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetTempFile_String() {
        System.out.println("getTempFile");
        String suffix = "";
        File expResult = null;
        File result = FileDirectoryHandler.getTempFile(suffix);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTempFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetTempFile_0args() {
        System.out.println("getTempFile");
        File expResult = null;
        File result = FileDirectoryHandler.getTempFile();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTempFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetTempFile_String_String() {
        System.out.println("getTempFile");
        String filename = "";
        String suffix = "";
        File expResult = null;
        File result = FileDirectoryHandler.getTempFile(filename, suffix);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTempDir method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetTempDir() {
        System.out.println("getTempDir");
        String expResult = "";
        String result = FileDirectoryHandler.getTempDir();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTempDir2 method, of class FileDirectoryHandler.
     */
    @Test
    public void testGetTempDir2() {
        System.out.println("getTempDir2");
        String expResult = "";
        String result = FileDirectoryHandler.getTempDir2();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of download method, of class FileDirectoryHandler.
     */
    @Test
    public void testDownload_String_String() {
        System.out.println("download");
        String address = "";
        String localFileName = "";
        File expResult = null;
        File result = FileDirectoryHandler.download(address, localFileName);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of download method, of class FileDirectoryHandler.
     */
    @Test
    public void testDownload_String() {
        System.out.println("download");
        String address = "";
        File expResult = null;
        File result = FileDirectoryHandler.download(address);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of copyFile method, of class FileDirectoryHandler.
     */
    @Test
    public void testCopyFile_File_File() throws Exception {
        System.out.println("copyFile");
        File sourceFile = null;
        File outp = null;
        URI expResult = null;
        URI result = FileDirectoryHandler.copyFile(sourceFile, outp);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}