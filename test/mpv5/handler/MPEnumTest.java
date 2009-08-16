
package mpv5.handler;

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
public class MPEnumTest {

    public MPEnumTest() {
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
     * Test of getId method, of class MPEnum.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        MPEnum instance = (MPEnum) new MPEnumImpl();
        Integer expResult = null;
        Integer result = instance.getId();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MPEnum.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        MPEnum instance = (MPEnum) new MPEnumImpl();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    public class MPEnumImpl implements MPEnum {

        public Integer getId() {
            return null;
        }

        public String getName() {
            return "";
        }
    }

}