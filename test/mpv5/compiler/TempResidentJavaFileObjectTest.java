
package mpv5.compiler;

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
public class TempResidentJavaFileObjectTest {

    public TempResidentJavaFileObjectTest() {
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
     * Test of getCharContent method, of class TempResidentJavaFileObject.
     */
    @Test
    public void testGetCharContent() throws Exception {
        System.out.println("getCharContent");
        boolean ignoreEncodingErrors = false;
        TempResidentJavaFileObject instance = null;
        CharSequence expResult = null;
        CharSequence result = instance.getCharContent(ignoreEncodingErrors);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}