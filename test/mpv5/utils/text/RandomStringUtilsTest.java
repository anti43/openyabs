
package mpv5.utils.text;

import java.util.Random;
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
public class RandomStringUtilsTest {

    public RandomStringUtilsTest() {
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
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_int() {
        System.out.println("random");
        int count = 0;
        String expResult = "";
        String result = RandomStringUtils.random(count);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of randomAscii method, of class RandomStringUtils.
     */
    @Test
    public void testRandomAscii() {
        System.out.println("randomAscii");
        int count = 0;
        String expResult = "";
        String result = RandomStringUtils.randomAscii(count);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of randomAlphabetic method, of class RandomStringUtils.
     */
    @Test
    public void testRandomAlphabetic() {
        System.out.println("randomAlphabetic");
        int count = 0;
        String expResult = "";
        String result = RandomStringUtils.randomAlphabetic(count);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of randomAlphanumeric method, of class RandomStringUtils.
     */
    @Test
    public void testRandomAlphanumeric() {
        System.out.println("randomAlphanumeric");
        int count = 0;
        String expResult = "";
        String result = RandomStringUtils.randomAlphanumeric(count);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of randomNumeric method, of class RandomStringUtils.
     */
    @Test
    public void testRandomNumeric() {
        System.out.println("randomNumeric");
        int count = 0;
        String expResult = "";
        String result = RandomStringUtils.randomNumeric(count);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_3args() {
        System.out.println("random");
        int count = 0;
        boolean letters = false;
        boolean numbers = false;
        String expResult = "";
        String result = RandomStringUtils.random(count, letters, numbers);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_5args() {
        System.out.println("random");
        int count = 0;
        int start = 0;
        int end = 0;
        boolean letters = false;
        boolean numbers = false;
        String expResult = "";
        String result = RandomStringUtils.random(count, start, end, letters, numbers);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_6args() {
        System.out.println("random");
        int count = 0;
        int start = 0;
        int end = 0;
        boolean letters = false;
        boolean numbers = false;
        char[] chars = null;
        String expResult = "";
        String result = RandomStringUtils.random(count, start, end, letters, numbers, chars);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_7args() {
        System.out.println("random");
        int count = 0;
        int start = 0;
        int end = 0;
        boolean letters = false;
        boolean numbers = false;
        char[] chars = null;
        Random random = null;
        String expResult = "";
        String result = RandomStringUtils.random(count, start, end, letters, numbers, chars, random);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_int_String() {
        System.out.println("random");
        int count = 0;
        String chars = "";
        String expResult = "";
        String result = RandomStringUtils.random(count, chars);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

    /**
     * Test of random method, of class RandomStringUtils.
     */
    @Test
    public void testRandom_int_charArr() {
        System.out.println("random");
        int count = 0;
        char[] chars = null;
        String expResult = "";
        String result = RandomStringUtils.random(count, chars);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}