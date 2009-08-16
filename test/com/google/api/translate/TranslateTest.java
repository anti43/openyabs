
package com.google.api.translate;

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
public class TranslateTest {

    public TranslateTest() {
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
     * Test of translate method, of class Translate.
     */
    @Test
    public void testTranslate() throws Exception {
        System.out.println("translate");
        String text = "";
        String from = "";
        String to = "";
        String expResult = "";
        String result = Translate.translate(text, from, to);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}