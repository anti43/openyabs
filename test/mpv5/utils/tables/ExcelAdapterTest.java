
package mpv5.utils.tables;

import java.awt.event.ActionEvent;
import javax.swing.JTable;
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
public class ExcelAdapterTest {

    public ExcelAdapterTest() {
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
     * Test of getJTable method, of class ExcelAdapter.
     */
    @Test
    public void testGetJTable() {
        System.out.println("getJTable");
        ExcelAdapter instance = null;
        JTable expResult = null;
        JTable result = instance.getJTable();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of setJTable method, of class ExcelAdapter.
     */
    @Test
    public void testSetJTable() {
        System.out.println("setJTable");
        JTable jTable1 = null;
        ExcelAdapter instance = null;
        instance.setJTable(jTable1);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of actionPerformed method, of class ExcelAdapter.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");
        ActionEvent e = null;
        ExcelAdapter instance = null;
        instance.actionPerformed(e);
        
        fail("The test case is a prototype.");
    }

}