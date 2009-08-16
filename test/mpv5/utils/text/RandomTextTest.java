
package mpv5.utils.text;

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
public class RandomTextTest {

    public RandomTextTest() {
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
     * Test of getString method, of class RandomText.
     */
    @Test
    public void testGetString() {
        System.out.println("getString");
        RandomText instance = new RandomText();
        String expResult = "";
        String result = instance.getString();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getText method, of class RandomText.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        String expResult = "";
        String result = RandomText.getText();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}