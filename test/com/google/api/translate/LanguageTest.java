
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
public class LanguageTest {

    public LanguageTest() {
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
     * Test of isValidLanguage method, of class Language.
     */
    @Test
    public void testIsValidLanguage() {
        System.out.println("isValidLanguage");
        String language = "";
        boolean expResult = false;
        boolean result = Language.isValidLanguage(language);
        assertEquals(expResult, result);
        
        fail("The test case is a prototype.");
    }

}