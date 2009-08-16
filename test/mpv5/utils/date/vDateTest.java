
package mpv5.utils.date;

import java.util.Date;
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
public class vDateTest {

    public vDateTest() {
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
     * Test of isIsVerified method, of class vDate.
     */
    @Test
    public void testIsIsVerified() {
        System.out.println("isIsVerified");
        vDate instance = null;
        boolean expResult = false;
        boolean result = instance.isIsVerified();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefDate method, of class vDate.
     */
    @Test
    public void testGetDefDate() {
        System.out.println("getDefDate");
        vDate instance = null;
        String expResult = "";
        String result = instance.getDefDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSqlDate method, of class vDate.
     */
    @Test
    public void testGetSqlDate() {
        System.out.println("getSqlDate");
        vDate instance = null;
        String expResult = "";
        String result = instance.getSqlDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDate method, of class vDate.
     */
    @Test
    public void testGetDate() {
        System.out.println("getDate");
        vDate instance = null;
        Date expResult = null;
        Date result = instance.getDate();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOvalue method, of class vDate.
     */
    @Test
    public void testGetOvalue() {
        System.out.println("getOvalue");
        vDate instance = null;
        String expResult = "";
        String result = instance.getOvalue();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}