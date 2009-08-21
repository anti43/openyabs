
package mpv5.ui.panels;

import java.io.File;
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
public class PreviewPanelTest {

    public PreviewPanelTest() {
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
     * Test of openPdf method, of class PreviewPanel.
     */
    @Test
    public void testOpenPdf() {
        System.out.println("openPdf");
        File pdf = null;
        PreviewPanel instance = new PreviewPanel();
        instance.openPdf(pdf);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of openOdt method, of class PreviewPanel.
     */
    @Test
    public void testOpenOdt() {
        System.out.println("openOdt");
        File file = null;
        PreviewPanel instance = new PreviewPanel();
//        instance.openOdt(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of open method, of class PreviewPanel.
     */
    @Test
    public void testOpen() {
        System.out.println("open");
        File file = null;
        PreviewPanel instance = new PreviewPanel();
        instance.open(file);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of showInNewFrame method, of class PreviewPanel.
     */
    @Test
    public void testShowInNewFrame() {
        System.out.println("showInNewFrame");
        PreviewPanel instance = new PreviewPanel();
//        instance.showInNewFrame();
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataOwner method, of class PreviewPanel.
     */
    @Test
    public void testGetDataOwner() {
        System.out.println("getDataOwner");
        PreviewPanel instance = new PreviewPanel();
        DatabaseObject expResult = null;
        DatabaseObject result = instance.getDataOwner();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setDataOwner method, of class PreviewPanel.
     */
    @Test
    public void testSetDataOwner() {
        System.out.println("setDataOwner");
        DatabaseObject dataOwner = null;
        PreviewPanel instance = new PreviewPanel();
        instance.setDataOwner(dataOwner);
        
        fail("The test case is a prototype.");
    }

}