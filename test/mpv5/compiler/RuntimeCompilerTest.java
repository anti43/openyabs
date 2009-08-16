
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
public class RuntimeCompilerTest {

    public RuntimeCompilerTest() {
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
     * Test of getClassFor method, of class RuntimeCompiler.
     */
    @Test
    public void testGetClassFor() throws Exception {
        System.out.println("getClassFor");
        String className = "";
        String classString = "";
        String packageName = "";
        Class expResult = null;
        Class result = RuntimeCompiler.getClassFor(className, classString, packageName);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjectFor method, of class RuntimeCompiler.
     */
    @Test
    public void testGetObjectFor() throws Exception {
        System.out.println("getObjectFor");
        Class clazz = null;
        Object expResult = null;
        Object result = RuntimeCompiler.getObjectFor(clazz);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}