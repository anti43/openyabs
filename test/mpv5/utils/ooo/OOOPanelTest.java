
package mpv5.utils.ooo;

import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.text.ITextDocument;
import java.io.File;
import java.util.Hashtable;
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
public class OOOPanelTest {

    public OOOPanelTest() {
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
     * Test of constructOOOPanel method, of class OOOPanel.
     */
    @Test
    public void testConstructOOOPanel() {
        System.out.println("constructOOOPanel");
        File odtFile = null;
        OOOPanel instance = new OOOPanel();
        instance.constructOOOPanel(odtFile);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of configureFrame method, of class OOOPanel.
     */
    @Test
    public void testConfigureFrame() {
        System.out.println("configureFrame");
        IFrame officeFrame = null;
        OOOPanel instance = new OOOPanel();
        instance.configureFrame(officeFrame);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of fillFields method, of class OOOPanel.
     */
    @Test
    public void testFillFields() throws Exception {
        System.out.println("fillFields");
        ITextDocument textDocument = null;
        File template = null;
        Hashtable<String, String> data = null;
        OOOPanel instance = new OOOPanel();
        instance.fillFields(textDocument, template, data);
        
        fail("The test case is a prototype.");
    }

}